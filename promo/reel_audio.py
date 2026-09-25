# Generates public/reel.wav for the Reel composition. 120 BPM -> 1 beat = 15 frames @30fps.
import numpy as np, wave
SR = 44100; DUR = 40.0; N = int(SR * DUR); FPS = 30
rng = np.random.default_rng(7)
mus = np.zeros((N, 2)); fx = np.zeros((N, 2))
fr = lambda f: f / FPS
T = lambda d: np.arange(int(d * SR)) / SR
hz = lambda m: 440 * 2 ** ((m - 69) / 12)

def put(buf, t0, x, g=1.0, pan=0.0):
    i0 = int(t0 * SR); x = np.asarray(x) * g
    if i0 < 0: x = x[-i0:]; i0 = 0
    n = min(len(x), N - i0)
    if n <= 0: return
    buf[i0:i0 + n, 0] += x[:n] * (1 - max(pan, 0)); buf[i0:i0 + n, 1] += x[:n] * (1 + min(pan, 0))

def onepole(x, a):  # simple lowpass, a in (0,1]
    y = np.empty_like(x); s = 0.0
    for i, v in enumerate(x): s += a * (v - s); y[i] = s
    return y

def biquad(x, kind, f, q=0.7):
    w = 2 * np.pi * f / SR; al = np.sin(w) / (2 * q); c = np.cos(w)
    b = {'lp': [(1 - c) / 2, 1 - c, (1 - c) / 2], 'hp': [(1 + c) / 2, -(1 + c), (1 + c) / 2], 'bp': [al, 0, -al]}[kind]
    a0, a1, a2 = 1 + al, -2 * c, 1 - al
    y = np.zeros_like(x); x1 = x2 = y1 = y2 = 0.0
    for i, v in enumerate(x):
        o = (b[0] * v + b[1] * x1 + b[2] * x2 - a1 * y1 - a2 * y2) / a0
        x2, x1, y2, y1 = x1, v, y1, o; y[i] = o
    return y

def sweep(f0, f1, d, decay, shape='sin'):
    t = T(d); f = f0 * (f1 / f0) ** (t / d); ph = 2 * np.pi * np.cumsum(f) / SR
    w = np.sin(ph) if shape == 'sin' else (2 / np.pi) * np.arcsin(np.sin(ph))
    return w * np.exp(-t * decay) * np.minimum(1, t / 0.002)

noise = lambda d: rng.uniform(-1, 1, int(d * SR))

def mix(*xs):
    o = np.zeros(max(len(x) for x in xs))
    for x in xs: o[:len(x)] += x
    return o

# ---- instruments ----
def kick():
    t = T(0.45); x = np.sin(2 * np.pi * np.cumsum(42 + 130 * np.exp(-t * 28)) / SR) * np.exp(-t * 7)
    c = biquad(noise(0.01), 'hp', 2000); x[:len(c)] += 0.3 * c; return x
def clap():
    n = noise(0.25); e = np.exp(-T(0.25) * 18)
    for d in (0.0, 0.012, 0.024): e[int(d * SR):int(d * SR) + 200] += 0.8
    return biquad(n * e, 'bp', 1400, 0.9) * 1.6
def hat(open_=False): d = 0.25 if open_ else 0.05; return biquad(noise(d) * np.exp(-T(d) * (12 if open_ else 80)), 'hp', 7000)
def snare(): return mix(biquad(noise(0.18) * np.exp(-T(0.18) * 25), 'bp', 2500, 0.8), 0.5 * sweep(260, 180, 0.12, 30))
def saw(f, d): t = T(d); return sum(2 * ((f * (1 + dt) * t) % 1) - 1 for dt in (-0.006, 0, 0.006)) / 3
def pad(notes, d):
    x = sum(saw(hz(m), d) for m in notes) / len(notes)
    t = T(d); env = np.minimum(1, t / 0.4) * np.minimum(1, (d - t) / 0.6)
    return biquad(x, 'lp', 1800) * env
def bass(m, d):
    t = T(d); x = saw(hz(m), d) + 0.6 * np.sin(2 * np.pi * hz(m) / 2 * t)
    return biquad(x, 'lp', 500) * np.exp(-t * 4) * np.minimum(1, t / 0.005)
def pluck(m, d=0.25): t = T(d); return (np.sin(2 * np.pi * hz(m) * t) + 0.3 * np.sin(4 * np.pi * hz(m) * t)) * np.exp(-t * 14)
def riser(d):
    t = T(d); x = biquad(noise(d), 'bp', 800, 0.5) * 0.6
    x = np.array([v for v in x]) * (t / d) ** 2
    return x + 0.25 * sweep(200, 1600, d, 0) * (t / d) ** 2
def downsweep(d=0.5): t = T(d); return biquad(noise(d), 'lp', 3000) * np.exp(-t * 6) * np.minimum(1, t / 0.005)
def boom(): return mix(sweep(110, 28, 1.6, 2.8) * 1.2, 0.4 * biquad(noise(1.0) * np.exp(-T(1.0) * 4), 'lp', 900))
def shimmer(): return sum(sweep(f, f * 1.005, 1.8, 2.5) for f in (2093, 2637, 3136, 4186, 5274)) * 0.14
def swoosh(d=0.35):
    t = T(d); x = t / d; env = np.sin(np.pi * x) ** 2
    return biquad(noise(d), 'bp', 900, 0.6) * env * 1.4
def key():
    r = rng.uniform(0.85, 1.15)
    click = biquad(noise(0.012) * np.exp(-T(0.012) * 900), 'hp', 3500 * r)
    body = np.sin(2 * np.pi * rng.uniform(170, 230) * T(0.06)) * np.exp(-T(0.06) * 70)
    thock = biquad(noise(0.04) * np.exp(-T(0.04) * 120), 'bp', 1200 * r, 1.5)
    out = np.zeros(int(0.08 * SR)); out[:len(click)] += 0.9 * click; out[:len(body)] += 0.5 * body; out[:len(thock)] += 1.4 * thock
    rel = biquad(noise(0.008) * np.exp(-T(0.008) * 1200), 'hp', 4500) * 0.35; i = int(rng.uniform(0.045, 0.06) * SR); out[i:i + len(rel)] += rel
    return out
def tick(f=2600): return sweep(f, f * 0.8, 0.03, 120)
def pop(f=900): return mix(sweep(f, f * 0.33, 0.1, 35), 0.2 * biquad(noise(0.02), 'hp', 3000))
def stamp(): return mix(0.8 * sweep(180, 60, 0.18, 22), 0.6 * biquad(noise(0.08) * np.exp(-T(0.08) * 40), 'bp', 1800, 0.8))
def ding(): return mix(0.6 * sweep(1568, 1568, 1.0, 5), 0.3 * sweep(3136, 3136, 0.6, 8))
def lock(): return mix(0.8 * biquad(noise(0.02) * np.exp(-T(0.02) * 300), 'bp', 3000, 2), 0.7 * sweep(420, 300, 0.06, 60))

# ---- music ----
BEAT = 0.5
prog = [([57, 60, 64, 67], 45), ([53, 57, 60, 64], 41), ([48, 52, 55, 59], 36), ([55, 59, 62, 66], 43)]  # Am7 Fmaj7 Cmaj7 G
kicks = []
for bar in range(20):
    t0 = bar * 2.0
    chord, root = prog[bar % 4]
    if t0 < 2 or t0 >= 33: continue
    breakdown = 12 <= t0 < 15
    put(mus, t0, pad(chord, 2.0), 0.16 if breakdown else 0.11)
    for b in range(4):
        tb = t0 + b * BEAT
        if tb >= 33: break
        if not breakdown or b == 0:
            put(mus, tb, kick(), 0.9); kicks.append(tb)
        if not breakdown:
            if b % 2: put(mus, tb, clap(), 0.35, -0.1)
            put(mus, tb + BEAT / 2, hat(open_=(b == 3)), 0.12, 0.3)
            put(mus, tb + BEAT / 4, hat(), 0.05, -0.3); put(mus, tb + 3 * BEAT / 4, hat(), 0.05, -0.3)
            for s8 in (0.5, 0.75): put(mus, tb + s8 * BEAT, bass(root - 12, 0.2), 0.32)
            put(mus, tb, bass(root - 12, 0.2), 0.3)
    if t0 >= 15 and not breakdown:  # arp
        for s in range(16):
            put(mus, t0 + s * BEAT / 4, pluck(chord[s % 4] + 12 * (1 + (s // 4) % 2)), 0.05, np.sin(s) * 0.5)
# intro pad + tension
put(mus, 0, pad([57, 64, 69], 2.0), 0.12)
put(mus, 0, riser(2.0), 0.35)
# snare roll into outro
for i in range(24):
    t = 31.0 + 2.0 * (1 - (1 - i / 24) ** 1.6)
    put(mus, t, snare(), 0.1 + 0.3 * i / 24)
put(mus, 33 - 1.5, riser(1.5), 0.4)
# outro
put(mus, 33, pad([45, 57, 64, 69, 72], 6.5), 0.22); put(mus, 33, bass(33, 3.0), 0.5)
# sidechain duck
env = np.ones(N)
for k in kicks:
    i = int(k * SR); d = int(0.28 * SR); n = min(d, N - i)
    env[i:i + n] = np.minimum(env[i:i + n], 0.35 + 0.65 * (np.arange(n) / d) ** 0.7)
mus *= env[:, None]

# ---- sfx, frame-synced to src/Reel.tsx ----
put(fx, fr(0), tick(1800), 0.3)
put(fx, fr(44), swoosh(0.55), 0.5)
KIN_WORDS = 8
for i in range(KIN_WORDS):
    t = fr(60 + i * 15)
    put(fx, t, downsweep(0.35), 0.45, [-0.3, 0.3][i % 2]); put(fx, t, sweep(160, 50, 0.3, 12), 0.5)
    if i in (1, 5): put(fx, t - 0.12, swoosh(0.15), 0.4, -0.5)
put(fx, fr(135), biquad(noise(0.3), 'bp', 3000, 3) * np.exp(-T(0.3) * 10), 0.3)  # glitch on NUMBER.
# pattern scene
put(fx, fr(180), boom(), 0.6)
put(fx, fr(180), shimmer(), 0.25)
put(fx, fr(28 + 180), pop(700), 0.35)
for i in range(15): put(fx, fr(180 + 32 + i * 40 / 15) + rng.uniform(-0.008, 0.008), key(), 0.55, rng.uniform(-0.25, 0.25))
put(fx, fr(180 + 74), swoosh(0.3), 0.4)
for i in range(26):  # blocked counter ticks, rising
    put(fx, fr(180 + 78 + i * 2.6), tick(900 * 1.05 ** i), 0.18, np.sin(i) * 0.4)
for i in range(4): put(fx, fr(180 + 80 + i * 18), stamp(), 0.35, (i - 1.5) * 0.2)
put(fx, fr(338), riser(0.7), 0.45)
# logo
put(fx, fr(352), swoosh(0.45), 0.55)
put(fx, fr(372), boom(), 0.9); put(fx, fr(372), shimmer(), 0.5)
for i in range(7): put(fx, fr(378 + i * 2), tick(1600 + i * 150), 0.12)
# phone
put(fx, fr(442), swoosh(0.45), 0.55)
put(fx, fr(452), sweep(90, 40, 0.4, 8), 0.5)
for k in range(1, 4):
    put(fx, fr(450 + k * 45) - 0.08, swoosh(0.3), 0.5, [0.5, -0.5, 0.5][k - 1]); put(fx, fr(450 + k * 45 + 8), sweep(140, 60, 0.15, 30), 0.35)
# match modes
put(fx, fr(622), swoosh(0.45), 0.5)
for i in range(4):
    put(fx, fr(640 + i * 6), pop(700 + i * 120), 0.35, [-0.4, 0.4, -0.4, 0.4][i])
    put(fx, fr(670 + i * 10), sweep(500, 1400, 0.25, 6, 'tri'), 0.12, [-0.4, 0.4, -0.4, 0.4][i])
    put(fx, fr(690 + i * 10), stamp(), 0.45, [-0.4, 0.4, -0.4, 0.4][i])
# privacy
put(fx, fr(742), swoosh(0.45), 0.5)
for j in range(14): put(fx, fr(750 + j * 2), tick(1400 + (j % 3) * 300), 0.15, rng.uniform(-0.5, 0.5))
for i in range(4): put(fx, fr(750 + 12 + i * 5), key(), 0.6, (i - 1.5) * 0.3)
put(fx, fr(784), lock(), 0.8); put(fx, fr(786), ding(), 0.35)
for i in range(4): put(fx, fr(750 + 44 + i * 8), pop(600 + i * 90), 0.3, -0.3)
# montage
for k in range(8):
    put(fx, fr(870 + k * 15), sweep(200, 45, 0.25, 14), 0.55); put(fx, fr(870 + k * 15), downsweep(0.2), 0.3, [-0.4, 0.4][k % 2])
# outro
put(fx, fr(990), boom(), 1.0); put(fx, fr(990), shimmer(), 0.55)
for i in range(18): put(fx, fr(990) + i * 0.025, tick(2000 + rng.uniform(0, 2000)), 0.08, rng.uniform(-0.8, 0.8))
put(fx, fr(1048), pop(800), 0.4); put(fx, fr(1048), ding(), 0.35)
put(fx, fr(1070), swoosh(0.5), 0.25)

# ---- fx echo + master ----
for d, g in ((0.12, 0.22), (0.19, 0.15)):
    i = int(d * SR); fx[i:, 0] += fx[:-i, 1] * g; fx[i:, 1] += fx[:-i, 0] * g
out = mus * 0.55 + fx
out = np.tanh(out * 1.3) / np.tanh(1.3)
fade = np.minimum(1, np.minimum(np.arange(N) / (0.05 * SR), (N - np.arange(N)) / (2.5 * SR)))
out *= fade[:, None]; out /= np.abs(out).max() / 0.9
with wave.open('public/reel.wav', 'w') as w:
    w.setnchannels(2); w.setsampwidth(2); w.setframerate(SR); w.writeframes((out * 32767).astype('<i2').tobytes())
print('ok')
