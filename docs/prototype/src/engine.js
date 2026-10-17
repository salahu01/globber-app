/* =====================================================================
   STARTER engine.js — production-prototype skill (proven on Biko + Kilo)
   Provides: $ $$ ic() bd() · sound bus (ac/tone/bell/warm/gate/MOMENT) · hap() + HLOG
             router (go/rerender/stagger/sliver/heroReact) · Dynamic Island (island/closeIsland/LIVE hooks) + ISLLOG
             sheet()/closeSheet() · ripple + selection haptics · seg()/segBind() · swipe() · chrome (tabs/mute/home/reset/fit/bump)
   Your app.js must define BEFORE this file runs its last line (boot()):
     R={},A={}           screen render / bind functions
     APP={tabs:[...], home:"today", theme:scr=>"dark"|"light"|..., noLive:[...screens hiding the live pill], bumpIds:[...]}
     SFX={...}            outcome cues (start from SFX_BASE below)
     init()               builds S and calls go(first)
     LIVE (optional)      {on:()=>bool, compact:()=>html, card:()=>html, bind:(el)=>void} for an ongoing session
   Expose to window for QA: go, S-reading helpers, sheets.
   ===================================================================== */
const $=s=>document.querySelector(s),$$=s=>[...document.querySelectorAll(s)];
const V=$("#view"),TABS=$("#tabs"),PH=$("#phone");
/* Solar: bold duotone by default (tiles, active, island); linear for "s", "xs", "lin" (inline + chrome) */
const ic=(n,c="")=>`<svg class="ic ${c}" aria-hidden="true"><use href="#i-${n}${/(^| )(s|xs|lin)( |$)/.test(c)?"-l":""}"/></svg>`;
const bd=(txt,kind="",icon="")=>`<span class="badge t-label ${kind}">${icon?ic(icon,"xs"):""}${txt}</span>`;

/* ================= SOUND (Web Audio, no assets) ================= */
let AC=null,muted=false,BUS=null,REV=null;
function ac(){if(!AC){try{AC=new (window.AudioContext||window.webkitAudioContext)();
    const comp=AC.createDynamicsCompressor();comp.threshold.value=-20;comp.knee.value=12;comp.ratio.value=4;comp.attack.value=.003;comp.release.value=.15;
    const mg=AC.createGain();mg.gain.value=.6;comp.connect(mg).connect(AC.destination);BUS=comp;
    const len=AC.sampleRate*1.1,ir=AC.createBuffer(2,len,AC.sampleRate);for(let ch=0;ch<2;ch++){const d=ir.getChannelData(ch);for(let i=0;i<len;i++)d[i]=(Math.random()*2-1)*Math.pow(1-i/len,3.2)}
    REV=AC.createConvolver();REV.buffer=ir;const rg=AC.createGain();rg.gain.value=.2;REV.connect(rg).connect(comp)}catch{return null}}
  if(AC.state==="suspended")AC.resume();return AC}
function route(c,node,o){let n=node;if(o.lp){const f=c.createBiquadFilter();f.type="lowpass";f.frequency.value=o.lp;n.connect(f);n=f}
  n.connect(BUS);if(o.rev){const g=c.createGain();g.gain.value=o.rev;n.connect(g).connect(REV)}}
function tone(f,d=.08,type="sine",v=.12,when=0,to=null,o={}){if(muted)return;const c=ac();if(!c)return;const t=c.currentTime+when;
  const os=c.createOscillator(),g=c.createGain();os.type=type;os.frequency.setValueAtTime(f,t);if(to)os.frequency.exponentialRampToValueAtTime(to,t+d);
  g.gain.setValueAtTime(0,t);g.gain.linearRampToValueAtTime(v,t+(o.a||.006));g.gain.exponentialRampToValueAtTime(.0001,t+d);os.connect(g);route(c,g,o);os.start(t);os.stop(t+d+.03)}
function bell(f,d=.5,v=.07,when=0,rev=.3){tone(f,d,"sine",v,when,null,{rev,a:.004});tone(f*2.01,d*.55,"sine",v*.32,when,null,{rev});tone(f*3.92,d*.22,"sine",v*.1,when,null,{rev})}
const warm=(f,d,v,when=0,o={})=>tone(f,d,"sine",v,when,null,Object.assign({lp:2400,a:.012,rev:.18},o));
const GATE={};const gate=(k,ms)=>{const n=performance.now();if(GATE[k]&&n-GATE[k]<ms)return false;GATE[k]=n;return true};
const MOMENT=()=>gate("moment",220);
/* haptics: Apple-HIG vocabulary, rate-limited; notifications share one 300ms gate */
const HAPT={select:[3],light:[6],soft:[9],medium:[12],rigid:[7],heavy:[18],success:[8,70,16],warning:[16,90,16],error:[20,50,20,50,26]};
const HGAP={select:30,light:45,soft:60,medium:80,rigid:60,heavy:120,success:300,warning:300,error:300};
const HLOG=[];
const buzz=ms=>{try{if(navigator.userActivation?.hasBeenActive&&navigator.vibrate)navigator.vibrate(ms)}catch{}};
function hap(k){if(typeof S!=="undefined"&&S&&S.haptics===false)return;const p=HAPT[k];if(!p)return;if(!gate("h-"+(p.length>1?"n":k),HGAP[k]))return;HLOG.push(k);buzz(p);
  if(k==="error"||k==="warning"||k==="success"||k==="heavy"){const d=$("#device");d.classList.remove("hx-"+k);void d.offsetWidth;d.classList.add("hx-"+k);setTimeout(()=>d.classList.remove("hx-"+k),420)}}
/* plate clack: rubber bumper on platform — low thud + tiny steel tick */
/* generic outcome cues — copy into your SFX and add 2–3 domain one-shots (see motion-sound-haptics.md) */
const SFX_BASE={
  ok(){if(!MOMENT())return;warm(659,.32,.05);warm(988,.42,.045,.09);hap("medium")},
  success(){if(!MOMENT())return;[523,659,784].forEach((f,i)=>warm(f,.55,.045,i*.11,{rev:.28}));warm(1047,.8,.035,.33,{rev:.35});hap("success")},
  err(){if(!MOMENT())return;tone(330,.18,"triangle",.045,0,294,{lp:1200,a:.012});tone(262,.26,"triangle",.045,.16,233,{lp:1000,a:.012});hap("error")},
  alert(){if(!MOMENT())return;warm(784,.35,.045);warm(587,.5,.045,.16);hap("warning")},
  copy(){if(!gate("copy",500))return;warm(1760,.1,.03,0,{lp:3600,rev:.1});warm(2349,.16,.025,.06,{lp:3600,rev:.15});hap("light")},
  empty(){if(!MOMENT())return;warm(392,.3,.03,0,{lp:1400});warm(330,.45,.028,.12,{lp:1200});hap("soft")},
  allow(){if(!MOMENT())return;warm(784,.2,.035,0,{lp:3000});warm(1047,.35,.03,.08,{lp:3200,rev:.25});hap("success")},
  deny(){if(!gate("deny",300))return;warm(523,.2,.022,0,{lp:1800});warm(440,.3,.02,.08,{lp:1600});hap("soft")},
  bye(){if(!MOMENT())return;[784,659,523].forEach((f,i)=>warm(f,.4,.04,i*.12,{lp:2200,rev:.3}));hap("medium")},
  tick(){if(!gate("rtick",600))return;warm(1568,.08,.012,0,{lp:3600,rev:.05})},
  expand(){if(!gate("expand",250))return;tone(440,.25,"sine",.018,0,880,{lp:2200,a:.03});hap("light")},
  collapse(){if(!gate("expand",250))return;tone(880,.2,"sine",.016,0,440,{lp:2000,a:.03});hap("soft")},
};

/* ================= ROUTER ================= */
let S;
function setTheme(scr){PH.dataset.theme=APP.theme(scr)}
function go(scr,dir){S.scr=scr;dir=dir||"f";
  const t=APP.tabs.indexOf(scr),tabbed=t>=0;TABS.hidden=!tabbed;PH.classList.toggle("notabs",!tabbed);
  $$(".tabs button").forEach((b,i)=>{const on=i===t;b.classList.toggle("on",on);b.querySelector("use").setAttribute("href",`#i-${b.dataset.ic}${on?"":"-l"}`)});
  const ind=$("#tabind");ind.style.transform=`translateX(${Math.max(t,0)*100}%)`;ind.style.opacity=t<0?0:1;
  setTheme(scr);PH.classList.toggle("nosf",!!(APP.full||[]).includes(scr));
  V.className="view"+((APP.full||[]).includes(scr)?" full":"");void V.offsetWidth;V.classList.add(dir);V.scrollTop=0;
  V.innerHTML=R[scr]();stagger();(A[scr]||(()=>{}))();sliver();islandSync()}
function rerender(){const sc=V.scrollTop;V.classList.add("still");V.innerHTML=R[S.scr]();V.scrollTop=sc;(A[S.scr]||(()=>{}))();sliver();islandSync()}
function stagger(){V.querySelectorAll(".st").forEach((e,i)=>e.style.setProperty("--i",i))}
/* sliver: put cbar(title,back) first in a scrolling screen; it pins (top:-60px, see pitfalls) once the big title scrolls away */
const cbar=(title,back)=>`<div class="cbar ${back?"":"nb"}">${back||""}<b class="t-h ell">${title}</b></div>`;
function sliver(){const c=V.querySelector(".cbar");V.onscroll=c?()=>c.classList.toggle("on",V.scrollTop>52):null;if(c)c.classList.toggle("on",V.scrollTop>52)}
/* hero reacts to state: "stall" (error) · "hop" (success) · "rep"/custom (physical event); returns to "idle" */
function heroReact(cls,sel=".hero"){V.querySelectorAll(sel).forEach(h=>{h.classList.remove("rep","stall","hop","idle");void h.offsetWidth;h.classList.add(cls);setTimeout(()=>{h.classList.remove(cls);h.classList.add("idle")},1000)})}

/* ---------- Dynamic Island (engine from reference: queue, pre-grow, hover-pause, swipe-up dismiss, live activity) ---------- */
const TINT={ok:"var(--ok)",warn:"var(--warn)",info:"var(--info)",acc:"var(--acc)"};
const ISLQ=[],ISLLOG=[];let islShownAt=0,islQT=null,islT;
function island(title,sub,icon="check",bg="var(--ok)",ms=2800,act){ISLLOG.push({title,sub,icon,bg,act:act&&act.l});const I=$("#island");const age=performance.now()-islShownAt;
  if((I.classList.contains("open")||I.classList.contains("pre"))&&!I.classList.contains("closing")&&age<900){ISLQ.push([title,sub,icon,bg,ms,act]);clearTimeout(islQT);islQT=setTimeout(()=>{const n=ISLQ.shift();if(n)islandShow(...n)},900-age);return}
  islandShow(title,sub,icon,bg,ms,act)}
function islandShow(title,sub,icon="check",bg="var(--ok)",ms=2800,act){const I=$("#island"),IN=$("#islandIn");islShownAt=performance.now();
  clearTimeout(islT);clearTimeout(island.t2);clearTimeout(island.t3);I.classList.remove("closing","big");
  const swap=I.classList.contains("open");
  IN.innerHTML=`<span class="isl-ic" style="background:${bg}">${ic(icon,"s")}</span><span class="isl-tx"><b>${title}</b><small>${sub||""}</small></span>${act?`<button class="isl-act" style="background:${bg}">${act.l}</button>`:""}<span class="isl-pr" style="--ms:${ms}ms"></span>`;
  if(act)IN.querySelector(".isl-act").onclick=e=>{e.stopPropagation();hap("medium");closeIsland();act.f()};
  I.style.setProperty("--tint",bg);const lim=act?24:36;I.classList.toggle("tall",(sub||"").length>lim||title.length>(act?18:28));
  if(swap){IN.classList.remove("swap");void IN.offsetWidth;IN.classList.add("swap")}
  else{IN.classList.remove("swap");I.classList.remove("drop","open");I.classList.add("pre");island.t3=setTimeout(()=>{I.classList.remove("pre");void I.offsetWidth;I.classList.add("open","drop")},140)}
  islT=setTimeout(closeIsland,ms)}
(function(){const I=$("#island");let y0=null;
  I.addEventListener("pointerenter",()=>{if(!I.classList.contains("open"))return;clearTimeout(islT);I.classList.add("hold")});
  I.addEventListener("pointerleave",()=>{if(!I.classList.contains("hold"))return;I.classList.remove("hold");islT=setTimeout(closeIsland,1400)});
  I.addEventListener("pointerdown",e=>{y0=e.clientY});
  I.addEventListener("pointermove",e=>{if(y0==null||!I.classList.contains("open"))return;const dy=Math.min(0,e.clientY-y0);I.style.translate=`-50% ${dy*.4}px`});
  I.addEventListener("pointerup",e=>{if(y0==null)return;const dy=e.clientY-y0;y0=null;I.style.translate="";if(dy<-18&&I.classList.contains("open")){I.dataset.swiped=1;closeIsland();setTimeout(()=>delete I.dataset.swiped,300)}});
})();
function closeIsland(){const I=$("#island");
  if(I.classList.contains("big")){I.classList.remove("big");islandSync();return}
  if(!I.classList.contains("open")||I.classList.contains("closing"))return;clearTimeout(islT);
  I.classList.add("closing");island.t2=setTimeout(()=>{I.classList.remove("open","closing","drop","tall","hold");I.classList.add("settle");setTimeout(()=>I.classList.remove("settle"),500);islandSync()},190)}
/* live activity: workout in progress (rest ring when resting) on every screen except the workout itself */
/* live activity (ongoing session: ride, workout, delivery, timer). Provide LIVE in app.js; hidden on APP.noLive screens */
let LIVE=null;
const liveOn=()=>!!(LIVE&&S&&LIVE.on())&&!(APP.noLive||[]).includes(S.scr);
function islandSync(){const I=$("#island");const on=liveOn();if(!on)I.classList.remove("big");I.classList.toggle("cmpt",on);if(on)paintLive()}
function paintLive(){if(!LIVE)return;$("#islCmp").innerHTML=LIVE.compact();const I=$("#island"),L=$("#islLive");if(!I.classList.contains("big"))return;
  if(L.firstElementChild){LIVE.update&&LIVE.update(L);return}L.innerHTML=LIVE.card();LIVE.bind&&LIVE.bind(L,()=>{I.classList.remove("big");L.innerHTML="";islandSync()})}
function islandTap(){const I=$("#island");if(I.dataset.swiped)return;
  if(I.classList.contains("open"))return closeIsland();
  if(I.classList.contains("big")){I.classList.remove("big");$("#islLive").innerHTML="";SFX.collapse&&SFX.collapse();return}
  if(I.classList.contains("cmpt")){$("#islLive").innerHTML="";I.classList.add("big");paintLive();SFX.expand&&SFX.expand()}}

/* ---------- sheets ---------- */
function sheet(html,after,opts={}){hap("soft");const s=document.createElement("div");s.className="sheet"+(opts.list?" list":"");
  s.innerHTML=`<div class="sheet-bd"></div><div class="sheet-pn" role="dialog" aria-modal="true"><span class="grab" aria-hidden="true"></span>${html}</div>`;
  PH.appendChild(s);
  s.querySelector(".sheet-bd").onclick=()=>{if(!opts.lock)closeSheet(s)};
  s.querySelectorAll("[data-close]").forEach(b=>b.onclick=()=>closeSheet(s));
  const pn=s.querySelector(".sheet-pn"),g=s.querySelector(".grab");let y0=null,dy=0;
  g.onpointerdown=e=>{y0=e.clientY;g.setPointerCapture(e.pointerId);pn.style.animation="none"};
  g.onpointermove=e=>{if(y0===null)return;const sc=PH.getBoundingClientRect().width/PH.offsetWidth||1;dy=Math.max(0,(e.clientY-y0)/sc);pn.style.transform=`translateY(${dy}px)`};
  g.onpointerup=()=>{if(y0===null)return;y0=null;if(dy>90)closeSheet(s);else{pn.style.transition="transform .3s";pn.style.transform="";setTimeout(()=>pn.style.transition="",300)}dy=0};
  after&&after(s);return s}
function closeSheet(s){s=s||$(".sheet:not(.out)");if(!s||s.classList.contains("out"))return;const pn=s.querySelector(".sheet-pn");pn.style.animation="";pn.style.transform="";s.classList.add("out");setTimeout(()=>s.remove(),320)}

/* ripple on pressables + selection haptics (UI is silent) */
PH.addEventListener("pointerdown",e=>{const b=e.target.closest(".press,.btn,.tabs button,.seg button,.chip");if(!b||b.disabled)return;
  ac();const r=b.getBoundingClientRect(),sc=r.width/b.offsetWidth||1,sz=Math.max(b.offsetWidth,b.offsetHeight);
  if(getComputedStyle(b).position==="static")b.style.position="relative";if(!b.classList.contains("ovv"))b.style.overflow="hidden";
  const rp=document.createElement("span");rp.className="ripple";rp.style.cssText=`width:${sz}px;height:${sz}px;left:${(e.clientX-r.left)/sc-sz/2}px;top:${(e.clientY-r.top)/sc-sz/2}px`;
  b.appendChild(rp);setTimeout(()=>rp.remove(),600);
  if(b.closest(".tabs,.seg,.chips"))hap("select");else if(b.classList.contains("brand"))hap("light")});
V.addEventListener("click",e=>{const g=e.target.closest("[data-go]");if(g)go(g.dataset.go,g.dataset.dir||"f")});
function segBind(root,sel,cb){root.querySelectorAll(sel).forEach(sg=>{const bs=[...sg.querySelectorAll("button")];bs.forEach((b,i)=>b.onclick=()=>{sg.style.setProperty("--k",i);bs.forEach(x=>x.classList.toggle("on",x===b));cb(b.dataset.v,sg)})})}
const seg=(name,opts,cur)=>{const k=Math.max(0,opts.findIndex(o=>o[0]===cur));return`<div class="seg" data-seg="${name}" style="--n:${opts.length};--k:${k}"><span class="si"></span>${opts.map(([v,l])=>`<button data-v="${v}" class="${v===cur?"on":""}">${l}</button>`).join("")}</div>`};

/* swipe-to-confirm: <div class="swipe" id="x"><span class="fill"></span><span class="lbl">Swipe to …</span><span class="knob">${ic("arrow-right","s")}</span></div> */
/* swipe-to-confirm (from reference) */
function swipe(el,done){const k=el.querySelector(".knob"),fill=el.querySelector(".fill");let x0=null,dx=0,max=0,last=0;
  k.onpointerdown=e=>{x0=e.clientX;max=el.offsetWidth-k.offsetWidth-10;el.classList.remove("snap");k.setPointerCapture(e.pointerId);ac();hap("light")};
  k.onpointermove=e=>{if(x0===null)return;const sc=el.getBoundingClientRect().width/el.offsetWidth;dx=Math.max(0,Math.min(max,(e.clientX-x0)/sc));
    k.style.transform=`translateX(${dx}px)`;fill.style.width=dx+60+"px";const step=Math.floor(dx/max*8);if(step!==last){last=step;hap(dx>max*.85?"rigid":"select")}};
  k.onpointerup=()=>{if(x0===null)return;x0=null;el.classList.add("snap");
    if(dx>max*.85){k.style.transform=`translateX(${max}px)`;fill.style.width="100%";el.classList.add("done");k.innerHTML=ic("check","s");hap("heavy");setTimeout(done,300)}
    else{k.style.transform="";fill.style.width="0";if(dx>8)hap("soft")}dx=0;last=0};
  el.tabIndex=0;el.setAttribute("role","button");el.setAttribute("aria-label","Swipe to finish workout");
  el.onkeydown=e=>{if(e.key==="Enter"||e.key===" "){e.preventDefault();el.classList.add("done","snap");k.innerHTML=ic("check","s");hap("heavy");setTimeout(done,200)}}}

/* ================= CHROME + BOOT ================= */
function setMute(m){muted=m;$("#mute").innerHTML=`<svg class="ic s"><use href="#i-${muted?"volume-off":"volume"}-l"/></svg><span>${muted?"Sound off":"Sound on"}</span>`;$("#mute").setAttribute("aria-pressed",muted);if(!muted){ac();SFX.ok&&SFX.ok()}}
function fit(){const W=418,H=890,sc=Math.min(1,(innerWidth-32)/W,(innerHeight-96)/H);$("#device").style.transform=`scale(${sc})`;$("#fit").style.width=W*sc+"px";$("#fit").style.height=H*sc+"px"}
function boot(){
  TABS.addEventListener("click",e=>{const b=e.target.closest("button");if(!b)return;const t=b.dataset.t,cur=APP.tabs.indexOf(S.scr);if(t===S.scr)return;go(t,APP.tabs.indexOf(t)<cur?"b":"f")});
  $("#island").onclick=islandTap;
  $("#mute").onclick=()=>{setMute(!muted);if(S.scr==="profile")rerender()};
  $("#homebtn").onclick=()=>{if(S.scr&&APP.tabs.includes(APP.home))go(APP.home,"b")};
  $("#reset").onclick=()=>{ac();init()};
  addEventListener("resize",fit);fit();
  /* live numbers bump when their text changes (list ids in APP.bumpIds) */
  const ids=new Set(APP.bumpIds||[]),last=new WeakMap();
  new MutationObserver(ms=>{for(const m of ms){let el=m.target.nodeType===3?m.target.parentElement:m.target;while(el&&el!==PH&&!ids.has(el.id))el=el.parentElement;
    if(!el||el===PH)continue;const t=performance.now();if(last.get(el)>t-350)continue;last.set(el,t);el.classList.remove("bump");void el.offsetWidth;el.classList.add("bump")}}).observe(PH,{subtree:true,childList:true,characterData:true});
  init();
  Object.assign(window,{go,R,A,sheet,closeSheet,island,SFX,HLOG,ISLLOG,init,rerender})}
