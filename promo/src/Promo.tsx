import React from 'react';
import {AbsoluteFill, Audio, Img, Sequence, interpolate, spring, staticFile, useCurrentFrame, useVideoConfig, Easing} from 'remotion';

const LIME = '#C6FF00';
const BG = '#0a0c07';
const FONT = '"SF Pro Display", "Inter", Helvetica, Arial, sans-serif';
const MONO = '"SF Mono", Menlo, monospace';

const useSpring = (delay = 0, damping = 14) => {
  const f = useCurrentFrame();
  const {fps} = useVideoConfig();
  return spring({frame: f - delay, fps, config: {damping}});
};

const Glow: React.FC = () => {
  const f = useCurrentFrame();
  return (
    <AbsoluteFill style={{background: BG, overflow: 'hidden'}}>
      <div style={{position: 'absolute', width: 900, height: 900, borderRadius: '50%', left: -300 + Math.sin(f / 60) * 60, top: -350,
        background: 'radial-gradient(circle, rgba(198,255,0,0.18), transparent 65%)'}} />
      <div style={{position: 'absolute', width: 800, height: 800, borderRadius: '50%', right: -300, bottom: -300 + Math.cos(f / 70) * 50,
        background: 'radial-gradient(circle, rgba(120,160,0,0.22), transparent 65%)'}} />
    </AbsoluteFill>
  );
};

const Words: React.FC<{text: string; size?: number; color?: string; delay?: number; highlight?: string[]}> = ({text, size = 92, color = '#fff', delay = 0, highlight = []}) => {
  const f = useCurrentFrame();
  return (
    <div style={{display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '0 22px', padding: '0 80px', fontFamily: FONT, fontWeight: 800, fontSize: size, lineHeight: 1.1, letterSpacing: -2, textAlign: 'center'}}>
      {text.split(' ').map((w, i) => {
        const p = interpolate(f - delay - i * 4, [0, 12], [0, 1], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp', easing: Easing.out(Easing.cubic)});
        return (
          <span key={i} style={{opacity: p, transform: `translateY(${(1 - p) * 40}px)`, filter: `blur(${(1 - p) * 8}px)`, color: highlight.includes(w) ? LIME : color}}>{w}</span>
        );
      })}
    </div>
  );
};

// Scene 1: incoming spam numbers
const numbers = ['+1 800 555 0142', '+1 800 555 7719', '+1 800 555 3301', '+1 800 555 9024', '+1 800 555 6150', '+1 800 555 2288'];
const Hook: React.FC = () => {
  const f = useCurrentFrame();
  return (
    <AbsoluteFill style={{alignItems: 'center', justifyContent: 'center'}}>
      <div style={{position: 'absolute', top: 150}}><Words text="Spam never calls from one number." size={76} highlight={['one']} /></div>
      <div style={{marginTop: 180, display: 'flex', flexDirection: 'column', gap: 18}}>
        {numbers.map((n, i) => {
          const p = interpolate(f - 20 - i * 9, [0, 10], [0, 1], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'});
          const shake = Math.sin((f + i * 7) * 1.4) * 3 * p;
          return (
            <div key={n} style={{opacity: p, transform: `translateX(${(1 - p) * 200 + shake}px)`, display: 'flex', alignItems: 'center', gap: 22,
              background: '#1c1d1a', border: '1px solid #2c2e27', borderRadius: 26, padding: '16px 32px', width: 560}}>
              <div style={{width: 42, height: 42, borderRadius: 21, background: '#ff5a5a', display: 'grid', placeItems: 'center', fontSize: 22}}>📞</div>
              <span style={{fontFamily: MONO, color: '#fff', fontSize: 34}}>{n}</span>
              <span style={{marginLeft: 'auto', color: '#ff5a5a', fontFamily: FONT, fontSize: 22, fontWeight: 600}}>Incoming</span>
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

// Scene 2: logo reveal
const Logo: React.FC = () => {
  const s = useSpring(0, 12);
  const t = useSpring(12);
  return (
    <AbsoluteFill style={{alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 40}}>
      <Img src={staticFile('globber-icon-1024.png')} style={{width: 300, transform: `scale(${s}) rotate(${(1 - s) * -30}deg)`, filter: 'drop-shadow(0 30px 80px rgba(198,255,0,0.35))'}} />
      <div style={{opacity: t, transform: `translateY(${(1 - t) * 30}px)`, textAlign: 'center', fontFamily: FONT}}>
        <div style={{fontSize: 150, fontWeight: 800, color: '#fff', letterSpacing: -5}}>Globber</div>
        <div style={{fontSize: 40, color: LIME, marginTop: 6}}>Block the <b>shape</b> of a number.</div>
      </div>
    </AbsoluteFill>
  );
};

// Scene 3: pattern typing & matches blocked
const PATTERN = '+1 800 555 ****';
const Pattern: React.FC = () => {
  const f = useCurrentFrame();
  const typed = PATTERN.slice(0, Math.floor(interpolate(f, [10, 55], [0, PATTERN.length], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'})));
  const list = [...numbers.slice(0, 4), '+91 98470 12345'];
  return (
    <AbsoluteFill style={{alignItems: 'center', paddingTop: 110}}>
      <Words text="One rule. Every variant." size={80} highlight={['rule.']} />
      <div style={{marginTop: 60, background: '#16170f', border: `3px solid ${LIME}`, borderRadius: 34, padding: '26px 44px', minWidth: 640,
        fontFamily: MONO, fontSize: 60, color: LIME, boxShadow: '0 0 60px rgba(198,255,0,0.25)'}}>
        {typed}<span style={{opacity: Math.floor(f / 8) % 2 ? 1 : 0}}>▍</span>
      </div>
      <div style={{marginTop: 50, display: 'flex', flexDirection: 'column', gap: 14}}>
        {list.map((n, i) => {
          const hit = n.startsWith('+1 800 555');
          const p = interpolate(f - 70 - i * 8, [0, 12], [0, 1], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'});
          return (
            <div key={n} style={{display: 'flex', alignItems: 'center', gap: 20, width: 620, padding: '12px 28px', borderRadius: 22,
              background: hit ? `rgba(255,90,90,${0.12 * p})` : `rgba(198,255,0,${0.1 * p})`, fontFamily: MONO, fontSize: 32, color: '#ddd'}}>
              <span style={{position: 'relative'}}>{n}
                {hit && <span style={{position: 'absolute', left: 0, top: '52%', height: 3, width: `${p * 100}%`, background: '#ff5a5a'}} />}
              </span>
              <span style={{marginLeft: 'auto', opacity: p, fontFamily: FONT, fontWeight: 700, fontSize: 24, color: hit ? '#ff5a5a' : LIME}}>{hit ? 'BLOCKED' : 'ALLOWED'}</span>
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

// Scene 4: phone screens
const Phone: React.FC<{src: string; delay: number; x: number; rot: number; z: number}> = ({src, delay, x, rot, z}) => {
  const s = useSpring(delay, 15);
  const f = useCurrentFrame();
  return (
    <div style={{position: 'absolute', left: '50%', top: 250, zIndex: z,
      transform: `translateX(calc(-50% + ${x * s}px)) translateY(${(1 - s) * 700 + Math.sin((f + delay) / 20) * 8}px) rotate(${rot * s}deg)`}}>
      <div style={{width: 300, height: 667, borderRadius: 44, padding: 10, background: '#222', boxShadow: '0 40px 80px rgba(0,0,0,.6), 0 0 0 2px #333'}}>
        <Img src={staticFile(src)} style={{width: '100%', height: '100%', borderRadius: 36, objectFit: 'cover'}} />
      </div>
    </div>
  );
};
const Screens: React.FC = () => (
  <AbsoluteFill>
    <div style={{position: 'absolute', top: 90, width: '100%'}}><Words text="Silent. Before it even rings." size={72} highlight={['Silent.']} /></div>
    <Phone src="02-rules.png" delay={10} x={-330} rot={-8} z={1} />
    <Phone src="03-blocked-calls.png" delay={16} x={330} rot={8} z={1} />
    <Phone src="01-dashboard.png" delay={4} x={0} rot={0} z={2} />
  </AbsoluteFill>
);

// Scene 5: features
const feats = ['Wildcards & regex', '100% on-device', 'No INTERNET permission', 'Contacts-aware', 'Block log & search', 'JSON backup'];
const Features: React.FC = () => {
  const f = useCurrentFrame();
  return (
    <AbsoluteFill style={{alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 60}}>
      <Words text="Private by design." size={96} highlight={['Private']} />
      <div style={{display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: 22, width: 900}}>
        {feats.map((t, i) => {
          const p = interpolate(f - 18 - i * 6, [0, 12], [0, 1], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp', easing: Easing.out(Easing.back(2))});
          const accent = i === 2;
          return (
            <div key={t} style={{transform: `scale(${p})`, opacity: p, padding: '20px 36px', borderRadius: 999, fontFamily: FONT, fontSize: 34, fontWeight: 700,
              background: accent ? LIME : '#1c1d1a', color: accent ? BG : '#fff', border: `1px solid ${accent ? LIME : '#34362e'}`}}>{t}</div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

// Scene 6: CTA
const CTA: React.FC = () => {
  const s = useSpring(0);
  const b = useSpring(15);
  return (
    <AbsoluteFill style={{alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 34, fontFamily: FONT}}>
      <Img src={staticFile('globber-icon-1024.png')} style={{width: 200, transform: `scale(${s})`}} />
      <div style={{fontSize: 110, fontWeight: 800, color: '#fff', letterSpacing: -4, opacity: s}}>Globber</div>
      <div style={{fontSize: 38, color: '#bbb', opacity: s}}>Free & open source · Android 10+</div>
      <div style={{transform: `scale(${b})`, marginTop: 20, background: LIME, color: BG, fontWeight: 800, fontSize: 38, padding: '24px 54px', borderRadius: 999}}>
        github.com/salahu01/Globber
      </div>
    </AbsoluteFill>
  );
};

const Fade: React.FC<{dur: number; children: React.ReactNode}> = ({dur, children}) => {
  const f = useCurrentFrame();
  const o = interpolate(f, [0, 8, dur - 8, dur], [0, 1, 1, 0], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'});
  const sc = interpolate(f, [0, dur], [1.0, 1.04]);
  return <AbsoluteFill style={{opacity: o, transform: `scale(${sc})`}}>{children}</AbsoluteFill>;
};

const scenes: [React.FC, number][] = [[Hook, 150], [Logo, 105], [Pattern, 195], [Screens, 180], [Features, 150], [CTA, 120]];

export const Promo: React.FC = () => {
  let at = 0;
  return (
    <AbsoluteFill>
      <Glow />
      <Audio src={staticFile('music.wav')} />
      {scenes.map(([C, d], i) => {
        const from = at; at += d;
        return <Sequence key={i} from={from} durationInFrames={d}><Fade dur={d}><C /></Fade></Sequence>;
      })}
    </AbsoluteFill>
  );
};
