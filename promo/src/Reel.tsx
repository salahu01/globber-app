import React from 'react';
import {AbsoluteFill, Audio, Easing, Img, Sequence, interpolate, random, spring, staticFile, useCurrentFrame, useVideoConfig} from 'remotion';
import {loadFont} from '@remotion/google-fonts/Inter';
import {loadFont as loadMono} from '@remotion/google-fonts/JetBrainsMono';

const {fontFamily: INTER} = loadFont('normal', {weights: ['400', '600', '800', '900'], subsets: ['latin']});
const {fontFamily: MONO} = loadMono('normal', {weights: ['500', '700'], subsets: ['latin']});

const LIME = '#C6FF00';
const INK = '#0a0c07';
const RED = '#FF4D4D';
const W = 1080;
const H = 1350;

const EXPO = Easing.bezier(0.16, 1, 0.3, 1);
const INOUT = Easing.bezier(0.83, 0, 0.17, 1);
const BACK = Easing.bezier(0.34, 1.56, 0.64, 1);
const ci = (f: number, i: number[], o: number[], e: (t: number) => number = EXPO) =>
  interpolate(f, i, o, {extrapolateLeft: 'clamp', extrapolateRight: 'clamp', easing: e});
const useSp = () => {
  const {fps} = useVideoConfig();
  return (f: number, damping = 12, mass = 1) => spring({frame: f, fps, config: {damping, mass}});
};
const shake = (f: number, seed: string, amt: number) => ({
  x: (random(`${seed}x${f}`) - 0.5) * 2 * amt,
  y: (random(`${seed}y${f}`) - 0.5) * 2 * amt,
});

// ---------- shared pieces ----------

const Asterisk: React.FC<{size: number; color?: string; rot?: number; spread?: number; arm?: number; thick?: number}> = ({
  size, color = LIME, rot = 0, spread = 1, arm = 1, thick = 0.22,
}) => (
  <svg width={size} height={size} viewBox="-50 -50 100 100" style={{overflow: 'visible', display: 'block'}}>
    <g transform={`rotate(${rot})`}>
      {[90, 30, 150].map((a) => (
        <rect key={a} x={-50 * arm} y={-thick * 50} width={100 * arm} height={thick * 100} rx={thick * 50} fill={color}
          transform={`rotate(${90 + (a - 90) * spread})`} />
      ))}
    </g>
  </svg>
);

const Mask: React.FC<{children: React.ReactNode; p: number; from?: 'bottom' | 'top'}> = ({children, p, from = 'bottom'}) => (
  <span style={{display: 'inline-block', overflow: 'hidden', verticalAlign: 'bottom', paddingBottom: '0.08em'}}>
    <span style={{display: 'inline-block', transform: `translateY(${(1 - p) * (from === 'bottom' ? 110 : -110)}%)`}}>{children}</span>
  </span>
);

const Vignette = () => (
  <AbsoluteFill style={{background: 'radial-gradient(ellipse at center, transparent 45%, rgba(0,0,0,0.55) 100%)', pointerEvents: 'none'}} />
);

const Grain: React.FC = () => {
  const f = useCurrentFrame();
  return (
    <AbsoluteFill style={{opacity: 0.07, mixBlendMode: 'overlay', pointerEvents: 'none'}}>
      <svg width={W} height={H}>
        <filter id="g"><feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="2" seed={f % 12} /></filter>
        <rect width="100%" height="100%" filter="url(#g)" />
      </svg>
    </AbsoluteFill>
  );
};

const SECTIONS: [number, string][] = [[0, 'INTRO'], [60, 'THE PROBLEM'], [180, 'THE PATTERN'], [360, 'GLOBBER'], [450, 'THE APP'],
  [630, 'MATCH MODES'], [750, 'PRIVACY'], [870, 'FEATURES'], [990, 'GET IT']];

const HUD: React.FC = () => {
  const f = useCurrentFrame();
  const idx = SECTIONS.filter(([s]) => f >= s).length - 1;
  const [start, name] = SECTIONS[idx];
  const p = ci(f - start, [0, 12], [0, 1]);
  const tc = `00:${String(Math.floor(f / 30)).padStart(2, '0')}:${String(f % 30).padStart(2, '0')}`;
  const o = ci(f, [20, 40], [0, 1]) * ci(f, [1170, 1195], [1, 0]);
  const st: React.CSSProperties = {position: 'absolute', fontFamily: MONO, fontSize: 20, letterSpacing: 2, color: '#fff'};
  return (
    <AbsoluteFill style={{mixBlendMode: 'difference', opacity: o, pointerEvents: 'none'}}>
      <div style={{...st, left: 44, top: 40}}>GLOBBER<span style={{opacity: 0.5}}> / REEL</span></div>
      <div style={{...st, right: 44, top: 40}}>● {tc}</div>
      <div style={{...st, left: 44, bottom: 40}}>
        <span style={{opacity: 0.5}}>{String(idx + 1).padStart(2, '0')} — </span><Mask p={p}>{name}</Mask>
      </div>
      <div style={{...st, right: 44, bottom: 40, opacity: 0.5}}>v1.0.5</div>
      <div style={{position: 'absolute', left: 44, right: 44, bottom: 28, height: 2, background: 'rgba(255,255,255,0.15)'}}>
        <div style={{width: `${(f / 1200) * 100}%`, height: '100%', background: '#fff'}} />
      </div>
    </AbsoluteFill>
  );
};

const Flash: React.FC<{color?: string}> = ({color = '#fff'}) => {
  const f = useCurrentFrame();
  return <AbsoluteFill style={{background: color, opacity: ci(f, [0, 6], [0.7, 0], Easing.out(Easing.quad))}} />;
};

const Wipe: React.FC<{dir: 'up' | 'left' | 'diag'; color?: string}> = ({dir, color = LIME}) => {
  const f = useCurrentFrame();
  const a = ci(f, [0, 8], [1, 0], INOUT);
  const b = ci(f, [8, 16], [0, -1], INOUT);
  const t = f < 8 ? a : b;
  const tf = dir === 'up' ? `translateY(${t * 100}%)` : dir === 'left' ? `translateX(${t * 100}%)` : `translate(${t * 130}%, ${t * 130}%) rotate(45deg) scale(2.2)`;
  return (
    <AbsoluteFill style={{overflow: 'hidden'}}>
      <AbsoluteFill style={{background: color, transform: tf}} />
      <AbsoluteFill style={{background: INK, transform: tf, opacity: 0.0}} />
    </AbsoluteFill>
  );
};

// ---------- 01 intro: line → asterisk → zoom through ----------

const Intro: React.FC = () => {
  const f = useCurrentFrame();
  const arm = ci(f, [0, 16], [0.02, 1]);
  const spread = ci(f, [14, 30], [0, 1], BACK);
  const rot = ci(f, [14, 48], [0, 240], INOUT);
  const zoom = ci(f, [44, 60], [1, 60], Easing.in(Easing.exp));
  const thick = ci(f, [0, 14], [0.03, 0.22]);
  return (
    <AbsoluteFill style={{background: INK, alignItems: 'center', justifyContent: 'center'}}>
      <div style={{transform: `scale(${zoom})`}}>
        <Asterisk size={420} arm={arm} spread={spread} rot={rot} thick={thick} />
      </div>
      <div style={{position: 'absolute', bottom: 300, fontFamily: MONO, color: '#fff', fontSize: 26, letterSpacing: 8,
        opacity: ci(f, [18, 26], [0, 0.7]) * ci(f, [40, 46], [1, 0])}}>
        glob&nbsp;<span style={{color: LIME}}>*</span>&nbsp;pattern
      </div>
    </AbsoluteFill>
  );
};

// ---------- 02 kinetic words on the beat ----------

const WORDS = ['SPAM', 'ROTATES', 'EVERY', 'SINGLE', 'NUMBER.', 'SO WE', 'BLOCK', 'THE'];

const Kinetic: React.FC = () => {
  const f = useCurrentFrame();
  const i = Math.min(WORDS.length - 1, Math.floor(f / 15));
  const lf = f - i * 15;
  const w = WORDS[i];
  const lime = i % 2 === 0;
  const fs = Math.min(330, 980 / (w.length * 0.66));
  const s = shake(f, 'k', Math.max(0, 1 - lf / 6) * 16);
  const v = i % 4;
  let tf = '';
  let extra: React.CSSProperties = {};
  if (v === 0) {
    tf = `scale(${ci(lf, [0, 7], [1.7, 1])})`;
    extra = {filter: `blur(${ci(lf, [0, 5], [14, 0])}px)`};
  } else if (v === 1) {
    tf = `translateX(${ci(lf, [0, 7], [-1200, 0])}px) skewX(${ci(lf, [0, 9], [-30, 0])}deg)`;
  } else if (v === 2) {
    tf = `translateY(${ci(lf, [0, 7], [60, 0])}%)`;
    extra = {clipPath: `inset(0 0 ${ci(lf, [0, 7], [100, 0])}% 0)`};
  } else {
    tf = `scaleY(${ci(lf, [0, 8], [0, 1], BACK)})`;
    extra = {letterSpacing: `${ci(lf, [0, 10], [0.4, -0.04])}em`};
  }
  const hold = ci(lf, [0, 15], [1, 1.06], Easing.linear);
  const glitch = w === 'NUMBER.';
  const text = (color: string, dx = 0, blend?: React.CSSProperties['mixBlendMode']) => (
    <div style={{position: 'absolute', fontFamily: INTER, fontWeight: 900, fontSize: fs, color, letterSpacing: '-0.04em', lineHeight: 1,
      transform: `translateX(${dx}px) ${tf} scale(${hold})`, mixBlendMode: blend, whiteSpace: 'nowrap', ...extra}}>{w}</div>
  );
  const g = glitch && lf % 4 < 2 ? (random(`gl${f}`) - 0.5) * 30 : 0;
  return (
    <AbsoluteFill style={{background: lime ? LIME : INK, alignItems: 'center', justifyContent: 'center',
      transform: `translate(${s.x}px, ${s.y}px)`}}>
      {/* ghost trail */}
      <div style={{position: 'absolute', fontFamily: INTER, fontWeight: 900, fontSize: fs * 2.6, whiteSpace: 'nowrap', lineHeight: 1,
        color: 'transparent', WebkitTextStroke: `2px ${lime ? 'rgba(10,12,7,0.18)' : 'rgba(198,255,0,0.18)'}`,
        transform: `translateX(${(lf - 7) * -14}px)`}}>{w}</div>
      {glitch ? (
        <>
          {text('#ff2d55', g, 'screen')}
          {text('#00e5ff', -g, 'screen')}
          {text(LIME)}
        </>
      ) : text(lime ? INK : '#fff')}
      <div style={{position: 'absolute', bottom: 170, fontFamily: MONO, fontSize: 22, letterSpacing: 6, color: lime ? INK : LIME, opacity: 0.8}}>
        {String(i + 1).padStart(2, '0')} / {String(WORDS.length).padStart(2, '0')}
      </div>
    </AbsoluteFill>
  );
};

// ---------- 03 number wall + pattern scan ----------

const COLS = 5;
const ROWS = 34;
const cell = (c: number, r: number) => {
  const rnd = (k: string) => Math.floor(random(`${k}${c}-${r}`) * 10);
  const d4 = `${rnd('a')}${rnd('b')}${rnd('c')}${rnd('d')}`;
  const m = random(`m${c}-${r}`) < 0.42;
  if (m) return {t: `+1 800 555 ${d4}`, m};
  const kind = rnd('k') % 3;
  return {t: kind === 0 ? `+1 212 555 ${d4}` : kind === 1 ? `+91 98${rnd('e')}${rnd('f')}${rnd('g')} ${d4}` : `+44 20 79${rnd('h')} ${d4}`, m};
};
const PATTERN = '+1 800 555 ****';

const NumberWall: React.FC = () => {
  const f = useCurrentFrame();
  const sp = useSp();
  const hitAt = (c: number, r: number) => 78 + c * 5 + r * 1.6;
  let blocked = 0;
  for (let c = 0; c < COLS; c++) for (let r = 0; r < ROWS; r++) if (cell(c, r).m && f >= hitAt(c, r)) blocked++;
  const typed = PATTERN.slice(0, Math.floor(ci(f, [32, 72], [0, PATTERN.length], Easing.linear)));
  const box = sp(f - 28, 13);
  const boxY = ci(f, [74, 92], [0, -440]);
  const exit = ci(f, [158, 180], [0, 1], Easing.in(Easing.cubic));
  const titleP = sp(f, 10);
  return (
    <AbsoluteFill style={{background: INK, overflow: 'hidden'}}>
      <AbsoluteFill style={{perspective: 1400}}>
        <div style={{position: 'absolute', left: -260, top: -500, width: 1600, display: 'flex', gap: 24,
          transform: `rotateX(28deg) rotateZ(-14deg) scale(${ci(f, [0, 180], [1.05, 1.25], Easing.linear) + exit * 1.5})`,
          opacity: 1 - exit, filter: `blur(${exit * 10}px)`}}>
          {Array.from({length: COLS}).map((_, c) => (
            <div key={c} style={{display: 'flex', flexDirection: 'column', gap: 16,
              transform: `translateY(${(c % 2 ? -1 : 1) * f * 1.4 - (c % 2 ? 0 : 200)}px)`}}>
              {Array.from({length: ROWS}).map((__, r) => {
                const {t, m} = cell(c, r);
                const h = f - hitAt(c, r);
                const hp = ci(h, [0, 8], [0, 1]);
                return (
                  <div key={r} style={{fontFamily: MONO, fontSize: 30, fontWeight: 500, width: 300, whiteSpace: 'nowrap', position: 'relative',
                    padding: '6px 12px', borderRadius: 10,
                    color: m && h >= 0 ? RED : `rgba(255,255,255,${h >= 0 ? 0.18 : 0.55})`,
                    background: m && h >= 0 ? `rgba(255,77,77,${0.14 * hp})` : 'transparent',
                    textShadow: m && h >= 0 && h < 10 ? `0 0 ${20 * (1 - h / 10)}px ${RED}` : 'none',
                    transform: `scale(${m && h >= 0 ? 1 + 0.15 * Math.max(0, 1 - h / 8) : 1})`}}>
                    {t}
                    {m && <div style={{position: 'absolute', left: 12, top: '50%', height: 3, background: RED, width: `${hp * 250}px`}} />}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </AbsoluteFill>
      <Vignette />
      {/* PATTERN title */}
      <AbsoluteFill style={{alignItems: 'center', justifyContent: 'center', opacity: ci(f, [22, 30], [1, 0])}}>
        <div style={{fontFamily: INTER, fontWeight: 900, fontSize: 250, letterSpacing: '-0.04em', color: LIME,
          transform: `scale(${1.6 - 0.6 * titleP})`, textShadow: '0 0 80px rgba(198,255,0,0.5)'}}>PATTERN</div>
      </AbsoluteFill>
      {/* search box */}
      <AbsoluteFill style={{alignItems: 'center', justifyContent: 'center'}}>
        <div style={{transform: `translateY(${boxY}px) scale(${box * (1 + exit * 0.3)})`, opacity: 1 - exit,
          background: 'rgba(22,23,15,0.85)', backdropFilter: 'blur(12px)', border: `3px solid ${LIME}`, borderRadius: 40,
          padding: '30px 50px', minWidth: 760, boxShadow: `0 0 ${60 + 30 * Math.sin(f / 5)}px rgba(198,255,0,0.35)`}}>
          <div style={{fontFamily: MONO, fontSize: 20, color: 'rgba(255,255,255,0.5)', letterSpacing: 4, marginBottom: 10}}>NEW RULE · STARTS WITH</div>
          <div style={{fontFamily: MONO, fontWeight: 700, fontSize: 72, color: LIME, whiteSpace: 'nowrap'}}>
            {typed}<span style={{opacity: Math.floor(f / 6) % 2 ? 1 : 0}}>▍</span>
          </div>
        </div>
      </AbsoluteFill>
      {/* counter */}
      <div style={{position: 'absolute', left: 0, right: 0, bottom: 150, textAlign: 'center', opacity: ci(f, [76, 84], [0, 1]) * (1 - exit)}}>
        <div style={{fontFamily: INTER, fontWeight: 900, fontSize: 170, color: '#fff', lineHeight: 1, letterSpacing: '-0.04em',
          textShadow: `0 0 40px ${RED}`}}>{String(blocked).padStart(3, '0')}</div>
        <div style={{fontFamily: MONO, fontSize: 24, color: RED, letterSpacing: 8, marginTop: 8}}>CALLS BLOCKED · 1 RULE</div>
      </div>
    </AbsoluteFill>
  );
};

// ---------- 04 logo reveal ----------

const Logo: React.FC = () => {
  const f = useCurrentFrame();
  const sp = useSp();
  const icon = sp(f - 6, 9);
  const exit = ci(f, [80, 90], [0, 1], Easing.in(Easing.cubic));
  const ringCols = ['#5f7a05', '#3f5a06', '#2d4105'];
  const letters = 'Globber'.split('');
  const s = shake(f, 'lg', Math.max(0, 1 - (f - 14) / 8) * (f >= 14 ? 18 : 0));
  return (
    <AbsoluteFill style={{background: INK, alignItems: 'center', justifyContent: 'center', overflow: 'hidden',
      transform: `translate(${s.x}px, ${s.y}px) scale(${1 - exit * 0.15})`, opacity: 1 - exit}}>
      {ringCols.map((c, i) => (
        <div key={i} style={{position: 'absolute', width: 1600, height: 1600, borderRadius: '50%', background: c,
          opacity: 0.5, transform: `translate(${[-420, 460, -300][i]}px, ${[-520, 480, 620][i]}px) scale(${ci(f, [i * 4, 30 + i * 4], [0, 0.6 + i * 0.1])})`}} />
      ))}
      {[0, 6].map((d) => (
        <div key={d} style={{position: 'absolute', width: 400, height: 400, borderRadius: '50%', border: `6px solid ${LIME}`, top: 280,
          opacity: ci(f, [14 + d, 40 + d], [0.9, 0]), transform: `scale(${ci(f, [14 + d, 40 + d], [0.6, 3.2])})`}} />
      ))}
      <Img src={staticFile('globber-icon-1024.png')} style={{position: 'absolute', top: 260, width: 440,
        transform: `scale(${icon}) rotate(${(1 - icon) * -40}deg)`, filter: 'drop-shadow(0 40px 90px rgba(198,255,0,0.45))'}} />
      <div style={{position: 'absolute', top: 760, fontFamily: INTER, fontWeight: 900, fontSize: 200, color: '#fff', letterSpacing: '-0.05em'}}>
        {letters.map((l, i) => <Mask key={i} p={ci(f, [18 + i * 2, 36 + i * 2], [0, 1])}>{l}</Mask>)}
      </div>
      <div style={{position: 'absolute', top: 1010, fontFamily: INTER, fontWeight: 600, fontSize: 40, color: LIME}}>
        <Mask p={ci(f, [40, 58], [0, 1])}>Pattern-matching call blocker for Android</Mask>
      </div>
      <div style={{position: 'absolute', top: 1085, height: 6, borderRadius: 3, background: LIME, width: ci(f, [48, 70], [0, 520])}} />
    </AbsoluteFill>
  );
};

// ---------- 05 phone in 3D ----------

const SCREENS = [
  ['01-dashboard.png', 'DASHBOARD', 'Every block, at a glance.'],
  ['02-rules.png', 'RULES', 'Wildcards. Prefixes. Regex.'],
  ['03-blocked-calls.png', 'BLOCK LOG', 'Every screened call, searchable.'],
  ['04-settings.png', 'SETTINGS', 'Your theme. Your backup.'],
];

const PhoneScene: React.FC = () => {
  const f = useCurrentFrame();
  const sp = useSp();
  const SEG = 45;
  const idx = Math.min(3, Math.floor(f / SEG));
  const target = (k: number) => (k % 2 ? 20 : -20);
  const enter = sp(f, 14);
  let ry = -85 + (target(0) + 85) * enter;
  for (let k = 1; k <= idx; k++) ry += (target(k) - target(k - 1)) * sp(f - k * SEG, 13);
  const rx = 8 + Math.sin(f / 22) * 3;
  const floatY = Math.sin(f / 18) * 12;
  const lf = f - idx * SEG;
  const [, label, sub] = SCREENS[idx];
  return (
    <AbsoluteFill style={{background: INK, overflow: 'hidden'}}>
      {/* perspective floor */}
      <div style={{position: 'absolute', left: -1000, right: -1000, top: 760, height: 1400, transformOrigin: 'top',
        transform: 'perspective(700px) rotateX(72deg)',
        backgroundImage: `linear-gradient(rgba(198,255,0,0.22) 2px, transparent 2px), linear-gradient(90deg, rgba(198,255,0,0.22) 2px, transparent 2px)`,
        backgroundSize: '90px 90px', backgroundPosition: `0 ${f * 4}px`,
        maskImage: 'linear-gradient(to bottom, transparent, black 30%, black 60%, transparent)',
        WebkitMaskImage: 'linear-gradient(to bottom, black, transparent 80%)'}} />
      <div style={{position: 'absolute', left: '50%', top: 800, width: 900, height: 900, marginLeft: -450, marginTop: -450, borderRadius: '50%',
        background: 'radial-gradient(circle, rgba(198,255,0,0.28), transparent 60%)', transform: `scale(${1 + 0.08 * Math.sin(f / 7.5)})`}} />
      {/* labels */}
      <div style={{position: 'absolute', top: 110, left: 0, right: 0, textAlign: 'center'}}>
        <div style={{fontFamily: MONO, fontSize: 24, letterSpacing: 6, color: LIME}}>
          <Mask key={`n${idx}`} p={ci(lf, [0, 10], [0, 1])}>0{idx + 1}</Mask> / 04
        </div>
        <div style={{fontFamily: INTER, fontWeight: 900, fontSize: 120, color: '#fff', letterSpacing: '-0.045em', lineHeight: 1.05}}>
          <Mask key={`l${idx}`} p={ci(lf, [0, 12], [0, 1])}>{label}</Mask>
        </div>
        <div style={{fontFamily: INTER, fontWeight: 400, fontSize: 36, color: 'rgba(255,255,255,0.65)', marginTop: 8}}>
          <Mask key={`s${idx}`} p={ci(lf, [4, 16], [0, 1])}>{sub}</Mask>
        </div>
      </div>
      {/* phone */}
      <div style={{position: 'absolute', left: '50%', top: 390, marginLeft: -205, perspective: 2200}}>
        <div style={{width: 410, height: 900, borderRadius: 62, padding: 13, background: 'linear-gradient(145deg,#3a3d35,#101110)',
          boxShadow: `${-ry * 1.5}px 60px 120px rgba(0,0,0,0.7), 0 0 0 2px #444`,
          transform: `translateY(${(1 - enter) * 1000 + floatY}px) rotateY(${ry}deg) rotateX(${rx}deg)`, transformStyle: 'preserve-3d'}}>
          <div style={{position: 'relative', width: '100%', height: '100%', borderRadius: 50, overflow: 'hidden', background: '#000'}}>
            {SCREENS.map(([src], k) => (
              f >= k * SEG ? (
                <Img key={src} src={staticFile(src)} style={{position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover',
                  transform: `translateY(${k === 0 ? 0 : ci(f - k * SEG, [0, 14], [100, 0], INOUT)}%) scale(${k === idx ? 1 : 0.94})`,
                  filter: k < idx ? 'brightness(0.4)' : 'none'}} />
              ) : null
            ))}
            <div style={{position: 'absolute', inset: 0, background: `linear-gradient(${110 + ry * 2}deg, transparent 35%, rgba(255,255,255,0.14) 50%, transparent 65%)`}} />
          </div>
        </div>
      </div>
    </AbsoluteFill>
  );
};

// ---------- 06 match modes ----------

const MODES = [
  ['STARTS WITH', '1900*', ['', '1900', ' 555 2211']],
  ['CONTAINS', '*555*', ['+1 212 ', '555', ' 0198']],
  ['ENDS WITH', '*0000', ['+44 20 7946 ', '0000', '']],
  ['REGEX', '^\\+1(800|888)', ['', '+1 888', ' 301 4410']],
] as const;

const MatchModes: React.FC = () => {
  const f = useCurrentFrame();
  const sp = useSp();
  return (
    <AbsoluteFill style={{background: INK}}>
      <div style={{position: 'absolute', top: 120, left: 0, right: 0, textAlign: 'center', fontFamily: INTER, fontWeight: 900,
        fontSize: 108, color: '#fff', letterSpacing: '-0.045em'}}>
        <Mask p={ci(f, [0, 12], [0, 1])}>MATCH</Mask>{' '}<Mask p={ci(f, [4, 16], [0, 1])}><span style={{color: LIME}}>ANYTHING.</span></Mask>
      </div>
      <div style={{position: 'absolute', top: 330, left: 50, right: 50, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 30}}>
        {MODES.map(([label, pat, [pre, hit, post]], i) => {
          const a = sp(f - (10 + i * 6), 13);
          const hp = ci(f, [40 + i * 10, 56 + i * 10], [0, 1], INOUT);
          const stamp = sp(f - (60 + i * 10), 8);
          return (
            <div key={label} style={{height: 400, borderRadius: 40, background: '#15170f', border: '1px solid #2c2f23', padding: 40,
              position: 'relative', overflow: 'hidden', transform: `scale(${a}) rotate(${(1 - a) * (i % 2 ? 8 : -8)}deg)`, opacity: a}}>
              <div style={{fontFamily: MONO, fontSize: 22, color: 'rgba(255,255,255,0.5)', letterSpacing: 5}}>{String(i + 1).padStart(2, '0')} · {label}</div>
              <div style={{fontFamily: MONO, fontWeight: 700, fontSize: pat.length > 8 ? 44 : 76, color: LIME, marginTop: 30, whiteSpace: 'nowrap'}}>{pat}</div>
              <div style={{fontFamily: MONO, fontSize: 32, color: '#fff', marginTop: 50, whiteSpace: 'nowrap'}}>
                {pre}
                <span style={{color: hp > 0.5 ? INK : '#fff', padding: '2px 4px', borderRadius: 6,
                  background: `linear-gradient(${LIME},${LIME}) no-repeat left / ${hp * 100}% 100%`}}>{hit}</span>
                {post}
              </div>
              <div style={{position: 'absolute', right: 30, bottom: 34, fontFamily: INTER, fontWeight: 900, fontSize: 30, color: RED,
                border: `4px solid ${RED}`, borderRadius: 12, padding: '6px 16px', letterSpacing: 2,
                transform: `scale(${stamp * 1}) rotate(-10deg)`, opacity: Math.min(1, stamp * 2)}}>BLOCKED</div>
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

// ---------- 07 privacy ----------

const Privacy: React.FC = () => {
  const f = useCurrentFrame();
  const sp = useSp();
  const shackle = ci(f, [26, 34], [-40, 0], Easing.in(Easing.cubic));
  const lockHit = sp(f - 34, 7);
  const items = ['No INTERNET permission', 'No analytics', 'No ads', '100% on-device'];
  return (
    <AbsoluteFill style={{background: INK, alignItems: 'center', overflow: 'hidden'}}>
      {[0, 1, 2, 3].map((k) => {
        const t = ((f + k * 30) % 120) / 120;
        return <div key={k} style={{position: 'absolute', top: 470, width: 300, height: 300, marginTop: -150, borderRadius: '50%',
          border: '2px solid rgba(198,255,0,0.35)', transform: `scale(${1 + t * 4})`, opacity: 1 - t}} />;
      })}
      <svg width="150" height="190" viewBox="0 0 150 190" style={{position: 'absolute', top: 120, transform: `scale(${1 + (1 - lockHit) * 0.25})`}}>
        <path d={`M35 ${95 + shackle} V${60 + shackle} a40 40 0 0 1 80 0 V${95 + shackle}`} fill="none" stroke={LIME} strokeWidth="16" strokeLinecap="round" />
        <rect x="10" y="88" width="130" height="100" rx="24" fill={LIME} />
        <circle cx="75" cy="130" r="12" fill={INK} /><rect x="70" y="130" width="10" height="28" rx="5" fill={INK} />
      </svg>
      <div style={{position: 'absolute', top: 360, display: 'flex', gap: 6}}>
        {Array.from({length: 4}).map((_, i) => {
          const land = 12 + i * 5;
          const rolling = f < land;
          const d = rolling ? Math.floor(random(`d${i}${f}`) * 10) : 0;
          return (
            <div key={i} style={{width: 150, height: 220, borderRadius: 26, background: '#15170f', border: '1px solid #2c2f23', display: 'grid',
              placeItems: 'center', fontFamily: MONO, fontWeight: 700, fontSize: 170, color: rolling ? 'rgba(255,255,255,0.4)' : LIME,
              filter: rolling ? 'blur(3px)' : 'none', transform: `translateY(${rolling ? (random(`y${i}${f}`) - 0.5) * 30 : ci(f - land, [0, 6], [-20, 0], BACK)}px)`}}>{d}</div>
          );
        })}
      </div>
      <div style={{position: 'absolute', top: 610, fontFamily: MONO, fontSize: 28, letterSpacing: 8, color: '#fff', opacity: ci(f, [30, 40], [0, 1])}}>
        BYTES SENT. EVER.
      </div>
      <div style={{position: 'absolute', top: 730, display: 'flex', flexDirection: 'column', gap: 22}}>
        {items.map((t, i) => {
          const p = sp(f - (44 + i * 8), 14);
          return (
            <div key={t} style={{display: 'flex', alignItems: 'center', gap: 26, transform: `translateX(${(1 - p) * -700}px)`, opacity: p,
              fontFamily: INTER, fontWeight: 800, fontSize: 58, color: '#fff', letterSpacing: '-0.02em'}}>
              <div style={{width: 58, height: 58, borderRadius: 29, background: LIME, display: 'grid', placeItems: 'center'}}>
                <svg width="30" height="30" viewBox="0 0 30 30"><path d="M6 16 L12 22 L24 8" fill="none" stroke={INK} strokeWidth="5" strokeLinecap="round" strokeLinejoin="round"
                  strokeDasharray="40" strokeDashoffset={40 * (1 - ci(f - (50 + i * 8), [0, 8], [0, 1]))} /></svg>
              </div>
              {t}
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

// ---------- 08 feature montage: marquee cuts on every beat ----------

const MONT = ['WILDCARDS', 'REGEX', 'PREFIXES', 'SILENT', 'ON-DEVICE', 'BLOCK LOG', 'BACKUP', 'OPEN SOURCE'];

const Montage: React.FC = () => {
  const f = useCurrentFrame();
  const k = Math.min(MONT.length - 1, Math.floor(f / 15));
  const lf = f - k * 15;
  const w = MONT[k];
  const lime = k % 2 === 0;
  const fg = lime ? INK : LIME;
  const punch = ci(lf, [0, 6], [1.18, 1]);
  const row = `${w} ✱ ${w} ✱ ${w} ✱ ${w} ✱ `;
  return (
    <AbsoluteFill style={{background: lime ? LIME : INK, overflow: 'hidden', alignItems: 'center', justifyContent: 'center'}}>
      <div style={{transform: `rotate(${k % 2 ? 8 : -8}deg) scale(${punch})`, display: 'flex', flexDirection: 'column', gap: 0}}>
        {Array.from({length: 9}).map((_, r) => {
          const dir = r % 2 ? 1 : -1;
          const center = r === 4;
          return (
            <div key={r} style={{whiteSpace: 'nowrap', fontFamily: INTER, fontWeight: 900, fontSize: 150, lineHeight: 1.02, letterSpacing: '-0.04em',
              transform: `translateX(${-900 + dir * (lf * 22 + k * 60) + (r % 3) * 120}px)`,
              color: center ? fg : 'transparent', WebkitTextStroke: center ? 'none' : `2px ${lime ? 'rgba(10,12,7,0.35)' : 'rgba(198,255,0,0.35)'}`}}>
              {row}
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

// ---------- 09 outro ----------

const Outro: React.FC = () => {
  const f = useCurrentFrame();
  const sp = useSp();
  const icon = sp(f, 10);
  const cta = sp(f - 58, 10);
  const fadeOut = ci(f, [185, 210], [1, 0]);
  const letters = 'Globber'.split('');
  return (
    <AbsoluteFill style={{background: INK, alignItems: 'center', overflow: 'hidden', opacity: fadeOut}}>
      <div style={{position: 'absolute', top: 400, width: 1400, height: 1400, marginTop: -700, borderRadius: '50%',
        background: 'radial-gradient(circle, rgba(198,255,0,0.22), transparent 60%)'}} />
      {Array.from({length: 56}).map((_, i) => {
        const a = (i / 56) * Math.PI * 2 + random(`a${i}`) * 0.3;
        const d = ci(f, [0, 60], [0, 250 + random(`d${i}`) * 750]);
        const sz = 16 + random(`s${i}`) * 50;
        return (
          <div key={i} style={{position: 'absolute', left: W / 2 - sz / 2, top: 400 - sz / 2, opacity: ci(f, [0, 3], [0, 1]) * ci(f, [35, 80], [1, 0]),
            transform: `translate(${Math.cos(a) * d}px, ${Math.sin(a) * d}px)`}}>
            <Asterisk size={sz} rot={f * (4 + random(`r${i}`) * 8)} color={i % 3 ? LIME : '#fff'} />
          </div>
        );
      })}
      {[0, 5, 10].map((d) => (
        <div key={d} style={{position: 'absolute', top: 400, width: 360, height: 360, marginTop: -180, borderRadius: '50%', border: `5px solid ${LIME}`,
          opacity: ci(f, [d, d + 30], [0.8, 0]), transform: `scale(${ci(f, [d, d + 30], [0.5, 3.5])})`}} />
      ))}
      <Img src={staticFile('globber-icon-1024.png')} style={{position: 'absolute', top: 220, width: 360,
        transform: `scale(${icon}) rotate(${(1 - icon) * 30}deg) translateY(${Math.sin(f / 20) * 6}px)`,
        filter: 'drop-shadow(0 30px 80px rgba(198,255,0,0.4))'}} />
      <div style={{position: 'absolute', top: 630, fontFamily: INTER, fontWeight: 900, fontSize: 170, color: '#fff', letterSpacing: '-0.05em'}}>
        {letters.map((l, i) => <Mask key={i} p={ci(f, [12 + i * 2, 30 + i * 2], [0, 1])}>{l}</Mask>)}
      </div>
      <div style={{position: 'absolute', top: 850, fontFamily: INTER, fontWeight: 600, fontSize: 38, color: 'rgba(255,255,255,0.75)'}}>
        <Mask p={ci(f, [36, 52], [0, 1])}>Block the pattern, not the number.</Mask>
      </div>
      <div style={{position: 'absolute', top: 960, transform: `scale(${cta})`, background: LIME, color: INK, borderRadius: 999,
        padding: '28px 56px', fontFamily: INTER, fontWeight: 900, fontSize: 40, overflow: 'hidden', letterSpacing: '-0.01em'}}>
        github.com/salahu01/Globber
        <div style={{position: 'absolute', top: 0, bottom: 0, width: 120, left: ci(f, [80, 110], [-200, 900], INOUT),
          background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.8), transparent)', transform: 'skewX(-20deg)'}} />
      </div>
      <div style={{position: 'absolute', top: 1100, fontFamily: MONO, fontSize: 24, letterSpacing: 6, color: LIME, opacity: ci(f, [70, 85], [0, 1])}}>
        FREE · OPEN SOURCE · ANDROID 10+
      </div>
    </AbsoluteFill>
  );
};

// ---------- timeline ----------

const TIMELINE: [React.FC, number, number][] = [
  [Intro, 0, 60], [Kinetic, 60, 120], [NumberWall, 180, 180], [Logo, 360, 90], [PhoneScene, 450, 180],
  [MatchModes, 630, 120], [Privacy, 750, 120], [Montage, 870, 120], [Outro, 990, 210],
];

export const Reel: React.FC = () => (
  <AbsoluteFill style={{background: INK}}>
    <Audio src={staticFile('reel.wav')} />
    {TIMELINE.map(([C, from, dur], i) => (
      <Sequence key={i} from={from} durationInFrames={dur}><C /></Sequence>
    ))}
    <Sequence from={352} durationInFrames={16}><Wipe dir="up" /></Sequence>
    <Sequence from={442} durationInFrames={16}><Wipe dir="diag" /></Sequence>
    <Sequence from={622} durationInFrames={16}><Wipe dir="left" /></Sequence>
    <Sequence from={742} durationInFrames={16}><Wipe dir="up" color="#fff" /></Sequence>
    {[60, 180, 870, 990].map((t) => <Sequence key={t} from={t} durationInFrames={8}><Flash /></Sequence>)}
    <Grain />
    <HUD />
  </AbsoluteFill>
);

// ---------- thumbnail (still) ----------

export const Thumbnail: React.FC = () => (
  <AbsoluteFill style={{background: INK, overflow: 'hidden'}}>
    {/* struck-through number wall */}
    <AbsoluteFill style={{perspective: 1400, opacity: 0.55}}>
      <div style={{position: 'absolute', left: -300, top: -420, width: 1700, display: 'flex', gap: 24,
        transform: 'rotateX(28deg) rotateZ(-14deg) scale(1.15)'}}>
        {Array.from({length: COLS}).map((_, c) => (
          <div key={c} style={{display: 'flex', flexDirection: 'column', gap: 16, transform: `translateY(${c % 2 ? -60 : -200}px)`}}>
            {Array.from({length: ROWS}).map((__, r) => {
              const {t, m} = cell(c, r);
              return (
                <div key={r} style={{fontFamily: MONO, fontSize: 30, width: 300, whiteSpace: 'nowrap', position: 'relative', padding: '6px 12px',
                  borderRadius: 10, color: m ? RED : 'rgba(255,255,255,0.22)', background: m ? 'rgba(255,77,77,0.14)' : 'transparent'}}>
                  {t}{m && <div style={{position: 'absolute', left: 12, top: '50%', height: 3, width: 250, background: RED}} />}
                </div>
              );
            })}
          </div>
        ))}
      </div>
    </AbsoluteFill>
    <AbsoluteFill style={{background: 'linear-gradient(180deg, rgba(10,12,7,0.95) 0%, rgba(10,12,7,0.6) 45%, rgba(10,12,7,0.2) 70%, rgba(10,12,7,0.85) 100%)'}} />
    <div style={{position: 'absolute', right: -200, bottom: -250, width: 1000, height: 1000, borderRadius: '50%',
      background: 'radial-gradient(circle, rgba(198,255,0,0.35), transparent 60%)'}} />
    {/* brand */}
    <div style={{position: 'absolute', top: 70, left: 70, display: 'flex', alignItems: 'center', gap: 22}}>
      <Img src={staticFile('globber-icon-1024.png')} style={{width: 96}} />
      <div style={{fontFamily: INTER, fontWeight: 900, fontSize: 56, color: '#fff', letterSpacing: '-0.04em'}}>Globber</div>
    </div>
    {/* pattern pill */}
    <div style={{position: 'absolute', top: 830, left: 70, background: 'rgba(22,23,15,0.9)', border: `3px solid ${LIME}`, borderRadius: 30,
      padding: '18px 34px', fontFamily: MONO, fontWeight: 700, fontSize: 50, color: LIME, boxShadow: '0 0 60px rgba(198,255,0,0.35)', transform: 'rotate(-3deg)'}}>
      +1 800 555 ****
    </div>
    <div style={{position: 'absolute', top: 1000, left: 70, fontFamily: INTER, fontWeight: 600, fontSize: 38, color: 'rgba(255,255,255,0.8)', lineHeight: 1.3}}>
      Not the number.<br /><span style={{color: LIME}}>Open-source call blocker · Android</span>
    </div>
    {/* phone */}
    <div style={{position: 'absolute', right: -60, top: 620, perspective: 2000}}>
      <div style={{width: 380, height: 836, borderRadius: 58, padding: 12, background: 'linear-gradient(145deg,#3a3d35,#101110)',
        boxShadow: '-30px 60px 120px rgba(0,0,0,0.8), 0 0 0 2px #444', transform: 'rotateY(-22deg) rotateX(8deg) rotateZ(6deg)'}}>
        <div style={{position: 'relative', width: '100%', height: '100%', borderRadius: 46, overflow: 'hidden'}}>
          <Img src={staticFile('01-dashboard.png')} style={{width: '100%', height: '100%', objectFit: 'cover'}} />
          <div style={{position: 'absolute', inset: 0, background: 'linear-gradient(120deg, transparent 35%, rgba(255,255,255,0.14) 50%, transparent 65%)'}} />
        </div>
      </div>
    </div>
    {/* headline */}
    <div style={{position: 'absolute', top: 220, left: 64, fontFamily: INTER, fontWeight: 900, fontSize: 190, lineHeight: 0.9,
      letterSpacing: '-0.055em', color: '#fff', textShadow: '0 10px 40px rgba(0,0,0,0.6)'}}>
      BLOCK<br />THE<br /><span style={{color: LIME, textShadow: '0 0 70px rgba(198,255,0,0.45)'}}>PATTERN.</span>
    </div>
    {/* play badge */}
    <div style={{position: 'absolute', top: 80, right: 70, display: 'flex', alignItems: 'center', gap: 14, background: LIME, color: INK,
      borderRadius: 999, padding: '14px 28px 14px 18px', fontFamily: INTER, fontWeight: 900, fontSize: 30}}>
      <svg width="34" height="34" viewBox="0 0 34 34"><circle cx="17" cy="17" r="17" fill={INK} /><path d="M13 10 L25 17 L13 24 Z" fill={LIME} /></svg>
      40s REEL
    </div>
    <Grain />
  </AbsoluteFill>
);
