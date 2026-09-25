# SFX layer, exec'd from make_music.py. Frame times match src/Promo.tsx (30 fps).
FPS = 30
MUSIC_GAIN = 0.45
for i in range(N): L[i] *= MUSIC_GAIN; R[i] *= MUSIC_GAIN
fr = lambda f: f / FPS

def env_exp(n, rate): return [math.exp(-k / SR * rate) for k in range(n)]

def sweep(f0, f1, dur, rate=20, wave_='sin'):
    n = int(dur * SR); out = []; ph = 0
    for k in range(n):
        t = k / SR; f = f0 * (f1 / f0) ** (t / dur); ph += 2 * math.pi * f / SR
        v = math.sin(ph) if wave_ == 'sin' else (1 if math.sin(ph) > 0 else -1) * 0.5
        out.append(v * math.exp(-t * rate) * min(1, t / 0.002))
    return out

def noise_bp(dur, center, rate, q=0.15):
    n = int(dur * SR); out = []; lp = hp = 0
    for k in range(n):
        x = random.random() * 2 - 1
        lp += q * (x - lp); hp = lp - hp * (1 - center)
        out.append(hp * math.exp(-k / SR * rate))
    return out

def mix(*parts):
    n = max(len(p) for p in parts); o = [0.0] * n
    for p in parts:
        for k, v in enumerate(p): o[k] += v
    return o

def scale(p, g): return [v * g for v in p]

tick   = lambda: sweep(2400, 1800, 0.04, 90)
pop    = lambda: mix(sweep(1100, 320, 0.09, 40), scale(noise_bp(0.03, 0.5, 150), 0.3))
def biquad(x, kind, f, q=0.7):
    w = 2 * math.pi * f / SR; a = math.sin(w) / (2 * q); c = math.cos(w)
    if kind == 'lp': b = [(1 - c) / 2, 1 - c, (1 - c) / 2]
    elif kind == 'hp': b = [(1 + c) / 2, -(1 + c), (1 + c) / 2]
    else: b = [a, 0, -a]  # bandpass
    a0, a1, a2 = 1 + a, -2 * c, 1 - a
    y = []; x1 = x2 = y1 = y2 = 0
    for v in x:
        o = (b[0] * v + b[1] * x1 + b[2] * x2 - a1 * y1 - a2 * y2) / a0
        x2, x1, y2, y1 = x1, v, y1, o; y.append(o)
    return y

def key():
    # mechanical keyboard: bright click + wooden thock body + quiet release click
    r = random.uniform(0.85, 1.15)
    click = biquad([(random.random() * 2 - 1) * math.exp(-k / SR * 900) for k in range(int(0.012 * SR))], 'hp', 3500 * r)
    body_f = random.uniform(170, 230)
    body = [math.sin(2 * math.pi * body_f * k / SR) * math.exp(-k / SR * 70) for k in range(int(0.06 * SR))]
    thock = biquad([(random.random() * 2 - 1) * math.exp(-k / SR * 120) for k in range(int(0.04 * SR))], 'bp', 1200 * r, 1.5)
    rel_at = int(random.uniform(0.045, 0.06) * SR)
    rel = [0] * rel_at + scale(biquad([(random.random() * 2 - 1) * math.exp(-k / SR * 1200) for k in range(int(0.008 * SR))], 'hp', 4500), 0.35)
    return mix(scale(click, 0.9), scale(body, 0.5), scale(thock, 1.4), rel)

def buzz():
    n = int(0.32 * SR); out = []
    for k in range(n):
        t = k / SR; v = math.sin(2 * math.pi * 170 * t) + 0.4 * math.sin(2 * math.pi * 340 * t)
        am = 0.5 + 0.5 * math.sin(2 * math.pi * 28 * t)
        out.append(v * am * min(1, t / 0.01) * max(0, 1 - t / 0.32) * 0.6)
    return mix(out, scale(sweep(1320, 1320, 0.12, 25), 0.25), [0] * int(0.13 * SR) + scale(sweep(1560, 1560, 0.12, 25), 0.25))
def denied():
    # strike swipe (matches the red line) + soft two-note "nope" + low muted thump
    n = int(0.16 * SR)
    sw = biquad([(random.random() * 2 - 1) * math.sin(math.pi * k / n) ** 2 for k in range(n)], 'bp', 2500, 1.2)
    def tri(f, dur, g):
        m = int(dur * SR)
        return [g * (2 / math.pi) * math.asin(math.sin(2 * math.pi * f * k / SR)) * min(1, k / (0.004 * SR)) * math.exp(-k / SR * 18) for k in range(m)]
    nope = mix(tri(659, 0.14, 0.5), [0] * int(0.09 * SR) + tri(523, 0.22, 0.5))
    nope = biquad(nope, 'lp', 3000)
    thump = [math.sin(2 * math.pi * (70 + 60 * math.exp(-k / SR * 40)) * k / SR) * math.exp(-k / SR * 22) for k in range(int(0.2 * SR))]
    return mix(scale(sw, 0.7), [0] * int(0.1 * SR) + nope, [0] * int(0.1 * SR) + scale(thump, 0.6))
chime  = lambda: mix(sweep(1047, 1047, 0.5, 7), [0] * int(0.08 * SR) + sweep(1568, 1568, 0.6, 6))
def riser(dur=1.2):
    n = int(dur * SR); out = []; lp = 0
    for k in range(n):
        t = k / SR; lp += (0.02 + 0.5 * (t / dur) ** 2) * ((random.random() * 2 - 1) - lp)
        out.append(lp * (t / dur) ** 2)
    return out
def swoosh(dur=0.35):
    n = int(dur * SR); out = []; lp = 0
    for k in range(n):
        t = k / SR; x = t / dur; c = 0.05 + 0.4 * math.sin(math.pi * x)
        lp += c * ((random.random() * 2 - 1) - lp); out.append(lp * math.sin(math.pi * x))
    return out
boom    = lambda: mix(sweep(120, 30, 1.2, 3.5), scale(noise_bp(0.4, 0.2, 10), 0.4))
shimmer = lambda: mix(*[scale(sweep(f, f * 1.01, 1.5, 3), 0.15) for f in (2093, 2637, 3136, 4186)])
thud    = lambda: sweep(160, 60, 0.15, 30)

S = []  # (time, sound, gain, pan)
# Scene 1: word ticks + phones buzzing in
for i in range(6): S.append((fr(i * 4), tick(), 0.25, 0))
for i in range(6): S.append((fr(20 + i * 9), buzz(), 0.45, (-0.4, 0.4)[i % 2]))
# Logo drop
S += [(fr(150) - 1.2, riser(1.2), 0.5, 0), (fr(150), boom(), 0.9, 0), (fr(150), shimmer(), 0.6, 0), (fr(162), pop(), 0.35, 0)]
# Pattern scene
for i in range(4): S.append((fr(255 + i * 4), tick(), 0.25, 0))
for i in range(15): S.append((fr(265 + i * 3) + random.uniform(-0.01, 0.01), key(), 0.55, random.uniform(-0.3, 0.3)))
for i in range(4): S.append((fr(325 + i * 8) - 0.1, denied(), 0.45, (i - 1.5) * 0.15))
S.append((fr(325 + 4 * 8), chime(), 0.4, 0))
# Screens: whoosh + land for each phone
for i in range(5): S.append((fr(450 + i * 4), tick(), 0.2, 0))
for d, pan in ((4, 0), (10, -0.5), (16, 0.5)):
    S.append((fr(450 + d) - 0.05, swoosh(), 0.6, pan)); S.append((fr(450 + d + 11), thud(), 0.5, pan))
# Features: pills pop with rising pitch
for i in range(3): S.append((fr(630 + i * 4), tick(), 0.25, 0))
for i in range(6):
    S.append((fr(648 + i * 6), mix(sweep(900 * 1.12 ** i, 300 * 1.12 ** i, 0.09, 40)), 0.4, (i % 3 - 1) * 0.4))
# CTA
S += [(fr(780) - 0.35, swoosh(0.35), 0.5, 0), (fr(780), pop(), 0.5, 0), (fr(795), chime(), 0.45, 0), (fr(795), shimmer(), 0.4, 0)]

# SFX bus with small stereo echo for space
SL = [0.0] * N; SR_ = [0.0] * N
for t0, snd, g, pan in S:
    i0 = int(t0 * SR); gl, gr = g * (1 - max(pan, 0)), g * (1 + min(pan, 0))
    for k, v in enumerate(snd):
        i = i0 + k
        if 0 <= i < N: SL[i] += v * gl; SR_[i] += v * gr
dl, dr = int(0.11 * SR), int(0.17 * SR)
for i in range(N):
    if i >= dl: SL[i] += SR_[i - dl] * 0.25
    if i >= dr: SR_[i] += SL[i - dr] * 0.25
for i in range(N): L[i] += SL[i]; R[i] += SR_[i]
