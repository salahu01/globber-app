# Generates public/music.wav: original synth track synced to scene cuts (120 BPM, 30s).
import math, random, struct, wave
SR = 44100; DUR = 30.0; N = int(SR * DUR)
L = [0.0] * N; R = [0.0] * N
BEAT = 0.5  # 120 BPM
CUTS = [5.0, 8.5, 15.0, 21.0, 26.0]
random.seed(1)

def add(t0, samples, gain=1.0, pan=0.0):
    i0 = int(t0 * SR)
    gl, gr = gain * (1 - max(pan, 0)), gain * (1 + min(pan, 0))
    for k, s in enumerate(samples):
        i = i0 + k
        if 0 <= i < N: L[i] += s * gl; R[i] += s * gr

def kick():
    n = int(0.35 * SR); out = []; ph = 0
    for k in range(n):
        t = k / SR; f = 45 + 110 * math.exp(-t * 30); ph += 2 * math.pi * f / SR
        out.append(math.sin(ph) * math.exp(-t * 9))
    return out

def hat():
    n = int(0.06 * SR); return [(random.random() * 2 - 1) * math.exp(-k / SR * 70) for k in range(n)]

def clap():
    n = int(0.2 * SR); return [(random.random() * 2 - 1) * math.exp(-k / SR * 22) for k in range(n)]

def saw(f, t): return 2 * ((f * t) % 1) - 1

def tone(freqs, dur, a=0.02, r=0.3, kind='saw', det=0.004):
    n = int(dur * SR); out = []
    lp = 0.0
    for k in range(n):
        t = k / SR
        env = min(1, t / a) * (1 if t < dur - r else max(0, (dur - t) / r))
        s = 0
        for f in freqs:
            if kind == 'saw': s += saw(f * (1 + det), t) + saw(f * (1 - det), t)
            else: s += math.sin(2 * math.pi * f * t)
        lp += 0.08 * (s - lp)  # simple low-pass
        out.append(lp * env / len(freqs))
    return out

def whoosh(dur=0.8):
    n = int(dur * SR); out = []; lp = 0
    for k in range(n):
        t = k / SR; c = 0.01 + 0.3 * (t / dur) ** 2
        lp += c * ((random.random() * 2 - 1) - lp)
        out.append(lp * (t / dur) ** 1.5)
    return out

def hz(m): return 440 * 2 ** ((m - 69) / 12)
# progression (Am - F - C - G), 2 bars each = 4s per chord
prog = [[57, 60, 64], [53, 57, 60], [48, 52, 55], [55, 59, 62]]
bass = [45, 41, 48, 43]

for c in range(8):
    t0 = c * 4.0
    if t0 >= DUR: break
    ch = prog[c % 4]
    add(t0, tone([hz(m) for m in ch], 4.0, a=0.6, r=0.8), 0.22 if t0 >= 5 else 0.15)
    if t0 + 4 > 5 or True:
        for b in range(8):
            tt = t0 + b * BEAT + BEAT / 2
            if tt >= 5 and tt < 28.5:
                add(tt, tone([hz(bass[c % 4] - 12)], 0.22, a=0.005, r=0.1), 0.35)

# intro tension: ticking pulse before drop
for b in range(10):
    add(b * BEAT, tone([hz(81)], 0.08, a=0.002, r=0.06, kind='sin'), 0.12 * (0.5 + b / 20))

# drums from drop (5s) to 28.5s
t = 5.0
while t < 28.5:
    add(t, kick(), 0.8)
    add(t + BEAT / 2, hat(), 0.18, pan=0.3)
    if int(round((t - 5) / BEAT)) % 2 == 1: add(t, clap(), 0.25, pan=-0.2)
    t += BEAT

# arp over main part
arp_t = 8.5
while arp_t < 26:
    c = int(arp_t // 4) % 4; notes = prog[c] + [prog[c][0] + 12]
    step = int(round((arp_t - 8.5) / (BEAT / 2)))
    add(arp_t, tone([hz(notes[step % 4] + 12)], 0.2, a=0.003, r=0.15, kind='sin'), 0.1, pan=0.4 * math.sin(step))
    arp_t += BEAT / 2

for c in CUTS:
    add(c - 0.8, whoosh(0.8), 0.35)
# final impact + ringing chord
add(26.0, kick(), 1.0); add(26.0, tone([hz(m) for m in [57, 64, 69, 72]], 4.0, a=0.01, r=3.0), 0.3)

exec(open('sfx.py').read())

peak = max(max(abs(x) for x in L), max(abs(x) for x in R))
fade = lambda i: min(1, i / (0.3 * SR), (N - i) / (1.5 * SR))
with wave.open('public/music.wav', 'w') as w:
    w.setnchannels(2); w.setsampwidth(2); w.setframerate(SR)
    w.writeframes(b''.join(struct.pack('<hh', int(L[i] / peak * 0.89 * fade(i) * 32767), int(R[i] / peak * 0.89 * fade(i) * 32767)) for i in range(N)))
print('ok', peak)
