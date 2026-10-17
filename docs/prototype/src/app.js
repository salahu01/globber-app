/* =====================================================================
   GLOBBER — redesign prototype (app.js). Contract: see engine.js header.
   "Today" is pinned to Sat 17 Oct 2026, 9:41. Demo moment: Simulate call
   from +91 140 4482 917 → matches rule "+91 140*" → blocked, logged.
   ===================================================================== */
const R={},A={};
const APP={tabs:["home","rules","log","settings"],home:"home",full:["launcher","splash","onboard"],noLive:["launcher","splash","onboard"],
  bumpIds:["kToday","kWeek","kRules","stgCount"],
  theme:scr=>(scr==="splash"||scr==="launcher"||scr==="onboard"?"light":(S.theme==="system"?"light":S.theme))};

/* ---------- sound: outcomes only + 2 domain cues ---------- */
const SFX={...SFX_BASE,
  ring(){if(!gate("ring",900))return;[0,.18].forEach(w=>{warm(880,.14,.03,w,{lp:3000});warm(1175,.14,.025,w+.07,{lp:3000})})},
  block(){if(!MOMENT())return;tone(160,.22,"triangle",.06,0,80,{lp:900,a:.008});warm(1318,.18,.03,.06,{lp:3400,rev:.25});warm(988,.4,.035,.16,{rev:.3});hap("heavy")},
  shieldOn(){if(!MOMENT())return;[392,523,659,784].forEach((f,i)=>warm(f,.5,.04,i*.09,{rev:.3}));hap("success")}};

/* ---------- data ---------- */
const TODAY=new Date(2026,9,17,9,41);
const TYPES={STARTS:["Starts with","↦"],CONTAINS:["Contains","∗"],ENDS:["Ends with","↤"],EXACT:["Exact","="],REGEX:["Regex",".*"]};
const ACTS={REJECT:["Reject","forbidden","warn","rejected"],SILENCE:["Silence","mute","info","silenced"],VOICEMAIL:["Voicemail","voicemail","acc","sent to voicemail"]};
const digits=s=>(s||"").replace(/[^\d*]/g,"");
function natural(n){const d=digits(n);return d.startsWith("91")&&d.length>10?d.slice(2):d}
function matches(rule,num){const p=rule.pattern.trim();if(!p)return false;
  if(rule.type==="REGEX"){try{return new RegExp(p).test(num.replace(/\s/g,""))}catch{return false}}
  const pd=digits(p).replace(/\*+$/,"").replace(/^\*+/,""),nd=digits(num),nn=natural(num);if(!pd)return false;
  const pn=pd.startsWith("91")&&pd.length>4?pd.slice(2):pd;
  const t={STARTS:x=>x.startsWith(pd)||x.startsWith(pn),CONTAINS:x=>x.includes(pd),ENDS:x=>x.endsWith(pd),EXACT:x=>x===pd||x===pn}[rule.type];
  return t(nd)||t(nn)}
const validRegex=p=>{try{new RegExp(p);return true}catch{return false}};
function seedRules(){return[
  {id:1,pattern:"+91 140*",type:"STARTS",label:"Telemarketing series",action:"REJECT",on:true,hits:38},
  {id:2,pattern:"1900*",type:"STARTS",label:"Premium-rate lines",action:"REJECT",on:true,hits:12},
  {id:3,pattern:"*0000",type:"ENDS",label:"Robo-dialers",action:"SILENCE",on:true,hits:9},
  {id:4,pattern:"+1 800 555*",type:"STARTS",label:"US toll-free spam block",action:"REJECT",on:true,hits:6},
  {id:5,pattern:"7788",type:"CONTAINS",label:"Loan offers",action:"VOICEMAIL",on:true,hits:4},
  {id:6,pattern:"^\\+44",type:"REGEX",label:"UK numbers",action:"SILENCE",on:false,hits:0},
  {id:7,pattern:"+91 98470 12345",type:"EXACT",label:"Old landlord",action:"REJECT",on:true,hits:2}]}
const at=(dd,h,m)=>{const d=new Date(TODAY);d.setDate(d.getDate()-dd);d.setHours(h,m,0,0);return d.getTime()};
function seedLog(){let id=100;const L=(n,r,dd,h,m)=>({id:id++,number:n,rule:r,t:at(dd,h,m)});return[
  L("+91 140 2210 455",1,0,9,12),L("+91 80 4410 0000",3,0,8,47),L("+91 140 6612 003",1,0,7,30),
  L("1900 266 1180",2,1,19,5),L("+91 140 9921 470",1,1,14,22),L("+91 99 7788 1204",5,1,11,3),L("+91 140 3304 129",1,1,10,40),
  L("+1 800 555 0199",4,2,22,15),L("+91 140 1180 761",1,2,13,2),L("1900 300 4410",2,2,9,55),
  L("+91 98470 12345",7,4,18,30),L("+91 22 6120 0000",3,4,12,10),L("+91 140 5521 870",1,5,16,44),L("+91 140 7708 301",1,6,10,5)]}
function init(){$$(".sheet,.sys,.inc").forEach(e=>e.remove());$("#island").className="island";
  S={scr:null,theme:"light",role:false,contacts:false,notify:true,unknown:false,haptics:true,
    rules:seedRules(),log:seedLog(),allow:[],ob:0,q:"",f:"all",lq:"",lf:"all",sort:"hits",logReady:false,simIdx:0,nextId:8};
  go("launcher")}
const ruleById=id=>S.rules.find(r=>r.id===id);
const dayKey=t=>{const d=new Date(t),diff=Math.round((new Date(TODAY).setHours(0,0,0,0)-new Date(d).setHours(0,0,0,0))/864e5);
  return diff===0?"Today":diff===1?"Yesterday":d.toLocaleDateString("en-GB",{weekday:"short",day:"numeric",month:"short"})};
const hm=t=>new Date(t).toLocaleTimeString("en-GB",{hour:"2-digit",minute:"2-digit"});
const todayCount=()=>S.log.filter(l=>dayKey(l.t)==="Today").length;
const weekCount=()=>S.log.filter(l=>l.t>at(7,0,0)).length;
const activeRules=()=>S.rules.filter(r=>r.on).length;

/* ---------- hero: Globber shield with glob star ---------- */
function shieldSVG(id="s"){return`<svg viewBox="0 0 120 140" class="shield-hero"><defs>
  <linearGradient id="${id}g" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="var(--shield-1)"/><stop offset="1" stop-color="var(--shield-2)"/></linearGradient>
  <linearGradient id="${id}h" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fff" stop-opacity=".55"/><stop offset=".5" stop-color="#fff" stop-opacity="0"/></linearGradient>
  <radialGradient id="${id}r" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="var(--acc)" stop-opacity=".45"/><stop offset="1" stop-color="var(--acc)" stop-opacity="0"/></radialGradient></defs>
  <ellipse class="glow" cx="60" cy="64" rx="60" ry="60" fill="url(#${id}r)"/>
  <ellipse class="shadow" cx="60" cy="134" rx="34" ry="5" fill="#000" opacity=".35"/>
  <g class="bb"><path d="M60 6 104 22v38c0 32-19 56-44 68C35 116 16 92 16 60V22z" fill="url(#${id}g)"/>
  <path d="M60 6 104 22v38c0 32-19 56-44 68C35 116 16 92 16 60V22z" fill="none" stroke="#fff" stroke-opacity=".35" stroke-width="1.5"/>
  <path d="M60 12 98 26v14C80 48 40 46 22 38V26z" fill="url(#${id}h)"/>
  <g stroke="var(--shield-ink)" stroke-width="7" stroke-linecap="round"><path d="M60 46v36"/><path d="M44.4 55 75.6 73"/><path d="M75.6 55 44.4 73"/></g></g></svg>`}

/* ---------- shared bits ---------- */
const typeTile=t=>`<span class="tile acc type-tile">${TYPES[t][1]}</span>`;
const actBadge=a=>bd(ACTS[a][0],ACTS[a][2],ACTS[a][1]);
const toggle=(id,on,lbl)=>`<button class="toggle" role="switch" id="${id}" aria-checked="${on}" aria-label="${lbl}"></button>`;
function bindToggle(id,key,cb){const t=$("#"+id);if(!t)return;t.onclick=e=>{e.stopPropagation();S[key]=!S[key];t.setAttribute("aria-checked",S[key]);hap(S[key]?"light":"soft");cb&&cb(S[key])}}
const logRow=(l,i)=>{const r=ruleById(l.rule),a=r?r.action:"REJECT",allowed=S.allow.includes(natural(l.number));
  return`<button class="txr press" data-log="${l.id}" style="--i:${i}"><span class="tile ${ACTS[a][2]}">${ic(ACTS[a][1])}</span>
  <span class="main"><b class="t-num num-call ell">${l.number}</b><span class="t-meta mute ell">${r?r.pattern:"Unknown"} · ${r?TYPES[r.type][0]:""}</span></span>
  <span class="end"><span class="t-label mute">${hm(l.t)}</span>${allowed?bd("Allowed","ok"):bd(ACTS[a][0],ACTS[a][2])}</span></button>`};

/* ================= SPLASH ================= */
function lensSVG(){const st=[0,60,120].map(d=>`<path d="M60 34V86" transform="rotate(${d} 60 60)"/>`).join("");
  return`<svg viewBox="0 0 120 120" class="lens" aria-hidden="true"><defs>
  <radialGradient id="obR" cx=".5" cy=".36" r=".64"><stop offset="0" stop-color="#ff7a63"/><stop offset=".45" stop-color="#e8392b"/><stop offset="1" stop-color="#6e0f09"/></radialGradient><linearGradient id="obS" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fff" stop-opacity=".8"/><stop offset="1" stop-color="#fff" stop-opacity="0"/></linearGradient><linearGradient id="obE" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fff" stop-opacity=".6"/><stop offset=".5" stop-color="#fff" stop-opacity=".05"/><stop offset="1" stop-color="#fff" stop-opacity=".25"/></linearGradient><radialGradient id="armG" gradientUnits="userSpaceOnUse" cx="60" cy="60" r="30"><stop offset="0" stop-color="#5a5b61"/><stop offset=".45" stop-color="#232327"/><stop offset="1" stop-color="#050506"/></radialGradient><filter id="wet" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="1.3"/></filter><filter id="armSh" x="-30%" y="-30%" width="160%" height="160%"><feGaussianBlur stdDeviation="2.2"/></filter><linearGradient id="cerT" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fdfcfa"/><stop offset="1" stop-color="#e2ded8"/></linearGradient>
  <linearGradient id="cerE" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fff"/><stop offset="1" stop-color="#fff" stop-opacity="0"/></linearGradient>
  <filter id="cerD" x="-20%" y="-20%" width="140%" height="140%"><feOffset dy="-1.4" in="SourceAlpha" result="up"/><feFlood flood-color="#fff" flood-opacity=".95"/><feComposite in2="up" operator="in" result="hl"/><feOffset dy="1.8" in="SourceAlpha" result="dn"/><feGaussianBlur in="dn" stdDeviation=".9" result="dnb"/><feFlood flood-color="#000" flood-opacity=".38"/><feComposite in2="dnb" operator="in" result="sh"/><feMerge><feMergeNode in="hl"/><feMergeNode in="sh"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
  <filter id="cerS" x="-30%" y="-30%" width="160%" height="160%"><feGaussianBlur stdDeviation="1.6" result="b"/><feOffset dy="2" result="o"/><feFlood flood-color="#7a0f08" flood-opacity=".45"/><feComposite in2="o" operator="in"/><feMerge><feMergeNode/><feMergeNode in="SourceGraphic"/></feMerge></filter></defs>
  <rect x="4" y="4" width="112" height="112" rx="27" fill="url(#cerT)"/><rect x="4.5" y="4.5" width="111" height="111" rx="26.5" fill="none" stroke="url(#cerE)" stroke-width="1"/>
  <g stroke="#000" stroke-opacity=".28" stroke-width="12" stroke-linecap="round" filter="url(#armSh)" transform="translate(0 3)"><path d="M60 34V86" transform="rotate(0 60 60)"/><path d="M60 34V86" transform="rotate(60 60 60)"/><path d="M60 34V86" transform="rotate(120 60 60)"/></g><g class="lens-st" stroke="#141414" stroke-width="12" stroke-linecap="round"><path d="M60 34V86" transform="rotate(0 60 60)"/><path d="M60 34V86" transform="rotate(60 60 60)"/><path d="M60 34V86" transform="rotate(120 60 60)"/></g>
  <g class="lens-dot"><circle cx="60" cy="60" r="7.5" fill="url(#obR)"/></g></svg>`}
/* lanes: [number html, match?, y offset, delay] — matching numbers highlight 140 and break apart at the lens */
const LANES=[["+91 <em>140</em> 2210 455",1,-48,0],["+91 98470 55120",0,0,.35],["+91 <em>140</em> 6612 003",1,48,.7],["+44 20 7946 0018",0,-24,1.05],["+91 <em>140</em> 9921 470",1,24,1.4]];
/* ================= LAUNCHER (sample Android home screen) ================= */
function appIcon(id){return`<svg viewBox="0 0 108 108" aria-hidden="true"><defs><linearGradient id="${id}b" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#FDFCFA"/><stop offset="1" stop-color="#E2DED8"/></linearGradient>
  <radialGradient id="${id}e" gradientUnits="userSpaceOnUse" cx="54" cy="52.3" r="5.9"><stop offset="0" stop-color="#FF7A63"/><stop offset=".45" stop-color="#E8392B"/><stop offset="1" stop-color="#6E0F09"/></radialGradient></defs>
  <rect width="108" height="108" fill="url(#${id}b)"/><g stroke="#141414" stroke-width="7.2" stroke-linecap="round"><path d="M54 38V70"/><path d="M54 38V70" transform="rotate(60 54 54)"/><path d="M54 38V70" transform="rotate(120 54 54)"/></g><circle cx="54" cy="54" r="4.6" fill="url(#${id}e)"/></svg>`}
const LAPPS=[["Clock","clock","#E8EEF9","#2F5BB7"],["Calendar","calendar","#FCEDE6","#C2532B"],["Camera","camera","#ECECEC","#333"],["Gallery","gallery","#E7F4EC","#227A4C"],
  ["Weather","cloud","#E4F1FB","#2B78B8"],["Maps","map","#E8F5EA","#2E8B47"],["Music","music","#F6E9F3","#A43C86"],["Files","folder","#FFF3D9","#A2700B"],
  ["Wallet","wallet","#EAE8F8","#5048A8"],["Settings","settings","#ECECEC","#444"],["Mail","mail","#FDECEC","#B83A3A"],["GLOBBER"]];
const DOCK=[["Phone","call","#E5F4EA","#1E8A4B"],["Messages","chat","#E6EEFB","#2F5BB7"],["Browser","search","#F1F1F1","#333"],["Camera","camera","#ECECEC","#333"]];
const lapp=([n,i,bg,fg],k)=>n==="GLOBBER"?`<button class="la la-g press ovv" id="gIcon" aria-label="Open Globber"><span class="la-ic">${appIcon("lg"+k)}</span><span class="la-n">Globber</span></button>`
  :`<button class="la press ovv" data-app="${n}"><span class="la-ic" style="background:${bg};color:${fg}">${ic(i)}</span><span class="la-n">${n}</span></button>`;
R.launcher=()=>`<div class="lnch" id="lnch"><div class="lw" aria-hidden="true"></div>
  <div class="l-clock"><b>9:41</b><span>Saturday, 17 October</span><span class="l-wx">${ic("cloud","xs")} 27° Kozhikode</span></div>
  <div class="l-grid">${LAPPS.map(lapp).join("")}</div>
  <div class="l-search">${ic("search","s")}<span>Search</span></div>
  <div class="l-dock">${DOCK.map(lapp).join("")}</div>
  <div class="l-hint t-label">Tap Globber to open</div></div>`;
A.launcher=()=>{const g=$("#gIcon");$$("[data-app]").forEach(b=>b.onclick=()=>{hap("light");island(b.dataset.app,"Sample app in this prototype","info","var(--info)",1600)});
  g.onclick=()=>{hap("light");const r=g.querySelector(".la-ic").getBoundingClientRect(),P=PH.getBoundingClientRect(),sc=P.width/PH.offsetWidth;
    const z=document.createElement("div");z.className="l-zoom";z.style.cssText=`left:${(r.left-P.left)/sc}px;top:${(r.top-P.top)/sc}px;width:${r.width/sc}px;height:${r.height/sc}px`;z.innerHTML=appIcon("lz");PH.appendChild(z);
    requestAnimationFrame(()=>z.classList.add("go"));setTimeout(()=>{go("splash");setTimeout(()=>z.remove(),350)},420)}};
R.splash=()=>`<div class="sp3" id="spl">
  <div class="sp3-glow" aria-hidden="true"></div>
  <div class="sp3-lanes" aria-hidden="true">${LANES.map(([n,m,y,d])=>`<div class="ln ${m?"m":"p"}" style="--y:${y}px;--d:${d}s"><span class="t-mono">${n}</span>${m?`<span class="x t-label">blocked</span>`:""}</div>`).join("")}</div>
  <div class="sp3-core">${lensSVG()}<span class="sp3-pat t-mono"><span>+91 140*</span></span></div>
  <div class="sp3-brand"><b class="sp3-wm">Globber</b><span class="sp3-tag t-meta">Spam caught by its pattern. Real calls ring.</span></div>
  <div class="sp3-foot t-label"><span class="sp3-dot"></span>Screening on this phone · no internet</div></div>`;
A.splash=()=>{const t=setTimeout(()=>S.scr==="splash"&&go("onboard"),4600);$("#spl").onclick=()=>{clearTimeout(t);go("onboard")}};

/* ================= ONBOARDING (3 steps) ================= */
const OB=[
  {k:"Before",t:"Spam calls, all day",b:"Telemarketers rotate through hundreds of numbers. Blocking them one by one never ends.",cta:"Show me the fix"},
  {k:"Setup",t:"One pattern stops them all",b:"Add a rule like +91 140*. Set Globber as your call screening app, and it rejects every match before it rings.",cta:"Set as call screening app"},
  {k:"After",t:"Your phone, quiet again",b:"Spam is stopped silently. Real people still get through. Everything stays on your phone.",cta:"Start blocking"}];
/* ---------- detailed character kit (ceramic palette + skin, one red for spam) ---------- */
const SK="#DDA27E",SK2="#C48562",SK3="#B4734F",HAIR="#221d1b",HAIR2="#3a312c",SHIRT="#2D3138",SHIRT2="#23262c",PANTS="#B3A99D",PANTS2="#9f958a",SHOE="#161414",RED="#E8392B",INK="#141414";
function face(x,y,mood){
  const eyeO=(ex)=>`<ellipse cx="${ex}" cy="${y+4}" rx="3.6" ry="2.7" fill="#fff"/><circle cx="${ex+.4}" cy="${y+4.3}" r="1.9" fill="${HAIR}"/><circle cx="${ex+1}" cy="${y+3.6}" r=".6" fill="#fff"/>`;
  const eyeC=(ex)=>`<path d="M${ex-3.6} ${y+4}q3.6 2.8 7.2 0" stroke="${HAIR}" stroke-width="1.8" fill="none" stroke-linecap="round"/>`;
  const M={stress:{br:`<path d="M${x-14} ${y-3}l8 2.6M${x+14} ${y-3}l-8 2.6" stroke="${HAIR}" stroke-width="2.2" stroke-linecap="round"/>`,ey:eyeO(x-8)+eyeO(x+8),mo:`<path d="M${x-6} ${y+18}q3-3 6 0t6 0" stroke="${SK3}" stroke-width="1.9" fill="none" stroke-linecap="round"/>`},
    calm:{br:`<path d="M${x-14} ${y-2}q4-2 8-1M${x+14} ${y-2}q-4-2-8-1" stroke="${HAIR}" stroke-width="2.2" fill="none" stroke-linecap="round"/>`,ey:eyeO(x-8)+eyeO(x+8),mo:`<path d="M${x-5} ${y+17}q5 4 10 0" stroke="${SK3}" stroke-width="1.9" fill="none" stroke-linecap="round"/>`},
    bliss:{br:`<path d="M${x-14} ${y-3}q4-2.5 8-1M${x+14} ${y-3}q-4-2.5-8-1" stroke="${HAIR}" stroke-width="2.2" fill="none" stroke-linecap="round"/>`,ey:eyeC(x-8)+eyeC(x+8),mo:`<path d="M${x-6} ${y+16}q6 6 12 0" stroke="${SK3}" stroke-width="2" fill="none" stroke-linecap="round"/>`}}[mood];
  return`<g class="hd">
   <path d="M${x-6} ${y+22}h12v12h-12z" fill="${SK2}"/>
   <ellipse cx="${x-20}" cy="${y+6}" rx="4.2" ry="6" fill="${SK}"/><path d="M${x-21} ${y+3}q2 3 0 6" stroke="${SK3}" stroke-width="1.2" fill="none"/>
   <ellipse cx="${x+20}" cy="${y+6}" rx="4.2" ry="6" fill="${SK}"/><path d="M${x+21} ${y+3}q-2 3 0 6" stroke="${SK3}" stroke-width="1.2" fill="none"/>
   <path d="M${x-19} ${y-8}C${x-20} ${y+12} ${x-11} ${y+26} ${x} ${y+27}C${x+11} ${y+26} ${x+20} ${y+12} ${x+19} ${y-8}C${x+18} ${y-24} ${x-18} ${y-24} ${x-19} ${y-8}z" fill="${SK}"/>
   <path d="M${x+19} ${y-8}C${x+20} ${y+12} ${x+11} ${y+26} ${x} ${y+27}C${x+8} ${y+22} ${x+15} ${y+10} ${x+16} ${y-6}z" fill="${SK2}" opacity=".35"/>
   <path d="M${x-22} ${y-2}C${x-26} ${y-24} ${x-10} ${y-32} ${x+3} ${y-31}C${x+20} ${y-30} ${x+27} ${y-18} ${x+22} ${y-1}C${x+19} ${y-12} ${x+12} ${y-16} ${x+4} ${y-15}C${x-2} ${y-10} ${x-12} ${y-11} ${x-17} ${y-8}C${x-19} ${y-6} ${x-20} ${y-4} ${x-22} ${y-2}z" fill="${HAIR}"/>
   <path d="M${x-12} ${y-24}q8-6 18-4M${x-4} ${y-27}q10-3 18 3M${x+6} ${y-17}q6 2 10 8" stroke="${HAIR2}" stroke-width="2" fill="none" stroke-linecap="round"/>
   ${M.br}${M.ey}
   <path d="M${x} ${y+5}q-3 6-1 8.5q2 1 4 0" stroke="${SK3}" stroke-width="1.6" fill="none" stroke-linecap="round"/>
   <circle cx="${x-11}" cy="${y+12}" r="3.4" fill="${RED}" opacity=".12"/><circle cx="${x+11}" cy="${y+12}" r="3.4" fill="${RED}" opacity=".12"/>${M.mo}</g>`}
const hand=(x,y,r=0)=>`<g transform="rotate(${r} ${x} ${y})"><ellipse cx="${x}" cy="${y}" rx="7" ry="6.2" fill="${SK}"/><ellipse cx="${x-5}" cy="${y-3}" rx="2.6" ry="4" fill="${SK}" transform="rotate(-30 ${x-5} ${y-3})"/><path d="M${x-2} ${y+2}h5M${x-2} ${y+4.5}h4" stroke="${SK2}" stroke-width="1" stroke-linecap="round"/></g>`;
/* shirt torso: collar + placket + folds */
const torso=(x,y,w=74,h=86)=>`<path d="M${x-w/2} ${y+14}C${x-w/2} ${y+2} ${x-w/2+12} ${y-4} ${x-14} ${y-6}L${x} ${y+10}L${x+14} ${y-6}C${x+w/2-12} ${y-4} ${x+w/2} ${y+2} ${x+w/2} ${y+14}L${x+w/2-4} ${y+h}H${x-w/2+4}z" fill="${SHIRT}"/>
  <path d="M${x-14} ${y-6}l-6 10 14 6zM${x+14} ${y-6}l6 10-14 6z" fill="${SHIRT2}"/><path d="M${x} ${y+10}V${y+h}" stroke="${SHIRT2}" stroke-width="1.6"/>
  <circle cx="${x+3}" cy="${y+24}" r="1.4" fill="#4a4f58"/><circle cx="${x+3}" cy="${y+42}" r="1.4" fill="#4a4f58"/><circle cx="${x+3}" cy="${y+60}" r="1.4" fill="#4a4f58"/>
  <path d="M${x-w/2+10} ${y+40}q8 10 6 26M${x+w/2-10} ${y+44}q-6 10-4 22" stroke="${SHIRT2}" stroke-width="1.6" fill="none" stroke-linecap="round"/>`;
const bubble=(x,y,txt,cls="",d=0)=>`<g class="bub ${cls}" style="--d:${d}s;transform-origin:${x+44}px ${y+14}px"><rect x="${x}" y="${y}" width="${txt.length*7+30}" height="28" rx="14" fill="#fff" stroke="${INK}" stroke-opacity=".08"/>
  <circle cx="${x+14}" cy="${y+14}" r="6" fill="${RED}"/><path d="M${x+11} ${y+12.5}q3 4 6 0" stroke="#fff" stroke-width="1.6" fill="none" stroke-linecap="round"/>
  <text x="${x+26}" y="${y+18.5}" font-family="Geist Mono,monospace" font-size="11.5" font-weight="600" fill="${INK}">${txt}</text></g>`;
const star=(cx,cy,r,w,c=INK)=>`<g stroke="${c}" stroke-width="${w}" stroke-linecap="round">${[0,60,120].map(d=>`<path d="M${cx} ${cy-r}V${cy+r}" transform="rotate(${d} ${cx} ${cy})"/>`).join("")}</g>`;
const phone=(x,y,scr,r=0,cls="")=>`<g class="${cls}" transform="rotate(${r} ${x+13} ${y+21})"><rect x="${x}" y="${y}" width="26" height="42" rx="6" fill="${INK}"/><rect x="${x+2.5}" y="${y+3}" width="21" height="36" rx="3.5" fill="${scr}"/></g>`;

/* ---------- 1 BEFORE · busy office desk ---------- */
function sceneBefore(){return`<svg viewBox="0 0 360 330" class="scn s-before" aria-hidden="true">
  <!-- wall: window + shelf + clock -->
  <rect x="18" y="22" width="110" height="86" rx="10" fill="#E3DED6"/><rect x="24" y="28" width="98" height="74" rx="6" fill="#F3EFEA"/><path d="M73 28v74M24 65h98" stroke="#E3DED6" stroke-width="4"/>
  <rect x="236" y="56" width="104" height="6" rx="3" fill="#CFC6BA"/><rect x="246" y="30" width="12" height="26" rx="2" fill="#B9AFA3"/><rect x="260" y="36" width="10" height="20" rx="2" fill="#D3C9BD"/><rect x="272" y="26" width="14" height="30" rx="2" fill="#A89D90"/>
  <circle cx="314" cy="40" r="13" fill="#F7F5F2" stroke="#CFC6BA" stroke-width="2"/><path d="M314 32v8l5 3" stroke="${INK}" stroke-width="1.8" stroke-linecap="round" fill="none"/>
  <!-- chair back -->
  <rect x="122" y="146" width="96" height="104" rx="24" fill="#BDB3A6"/>
  <!-- person -->
  ${torso(170,170,72,86)}
  ${face(170,118,"stress")}
  <path class="drip" d="M196 96c0 0-5 7-5 10a5 5 0 0 0 10 0c0-3-5-10-5-10z" fill="#8DBFE6"/>
  <!-- arms: one holds buzzing phone to the side, other on desk -->
  <path d="M138 192c-10 22-6 40 14 46" stroke="${SHIRT}" stroke-width="15" stroke-linecap="round" fill="none"/>
  <path d="M204 186c14 4 22-6 26-26" stroke="${SHIRT}" stroke-width="15" stroke-linecap="round" fill="none"/>
  <g class="buzz">${phone(220,118,RED,-14)}<path d="M214 128a18 18 0 0 0 0 26M252 122a18 18 0 0 1 2 26" stroke="${RED}" stroke-width="2" fill="none" class="wave"/>
   <g><circle cx="248" cy="118" r="10" fill="${INK}"/><text x="248" y="122" text-anchor="middle" font-family="Plus Jakarta Sans" font-weight="800" font-size="10" fill="#fff">12</text></g></g>
  ${hand(229,160,-20)}
  <!-- desk -->
  <rect x="30" y="236" width="300" height="14" rx="5" fill="#C7BBAC"/><rect x="30" y="250" width="300" height="6" fill="#B5A898"/><path d="M52 256v52M308 256v52" stroke="#B5A898" stroke-width="8" stroke-linecap="round"/>
  ${hand(156,236,10)}
  <!-- laptop -->
  <path d="M60 236l10-52h70l-10 52z" fill="#3a3d43"/><path d="M68 230l8-40h56l-8 40z" fill="#E9E5DF"/><rect x="52" y="234" width="96" height="5" rx="2" fill="#2a2c31"/>
  <path d="M84 204h30M82 212h24M80 220h28" stroke="#CFC7BD" stroke-width="3" stroke-linecap="round"/>
  <!-- papers + mug -->
  <rect x="246" y="226" width="44" height="10" rx="2" fill="#F7F5F2" transform="rotate(-6 268 231)"/><rect x="252" y="220" width="40" height="10" rx="2" fill="#EFEBE5" transform="rotate(4 272 225)"/>
  <rect x="300" y="210" width="18" height="24" rx="4" fill="#F7F5F2" stroke="${INK}" stroke-opacity=".12"/><path d="M318 216a6 6 0 0 1 0 12" stroke="#F7F5F2" stroke-width="3" fill="none"/>
  ${bubble(10,128,"+91 140 2210","",.3)}${bubble(236,170,"Loan offer?","",1.1)}${bubble(150,40,"Spam likely","",1.9)}
  </svg>`}

/* ---------- 2 SETUP · home sofa, adds the rule ---------- */
function sceneSetup(){return`<svg viewBox="0 0 360 330" class="scn s-setup" aria-hidden="true">
  <!-- wall art + floor lamp + rug -->
  <rect x="34" y="34" width="70" height="50" rx="6" fill="#E3DED6"/><path d="M42 74l16-18 12 12 8-8 18 14z" fill="#CFC6BA"/><circle cx="88" cy="48" r="6" fill="#E8C9A0"/>
  <path d="M318 300V96" stroke="#A89D90" stroke-width="4"/><path d="M296 96h44l-8-30h-28z" fill="#E8DCC8"/><ellipse cx="318" cy="102" rx="34" ry="8" fill="#F6D9A4" opacity=".35" class="lampglow"/>
  <ellipse cx="170" cy="304" rx="150" ry="12" fill="#E2DCD3"/>
  <!-- sofa -->
  <rect x="26" y="168" width="270" height="70" rx="26" fill="#C9BFB2"/><rect x="40" y="206" width="244" height="54" rx="18" fill="#BCB1A3"/>
  <rect x="18" y="196" width="34" height="76" rx="16" fill="#C9BFB2"/><rect x="272" y="196" width="34" height="76" rx="16" fill="#C9BFB2"/>
  <path d="M54 272v20M270 272v20" stroke="#8f8579" stroke-width="7" stroke-linecap="round"/>
  <rect x="56" y="180" width="54" height="40" rx="12" fill="#E7DFD3" transform="rotate(-8 83 200)"/>
  <!-- person seated, looking at phone -->
  <path d="M150 238c18 2 40 2 56 0l4 52M150 238l-4 52" stroke="${PANTS}" stroke-width="20" stroke-linecap="round" fill="none"/>
  <path d="M188 240h18" stroke="${PANTS2}" stroke-width="2"/>
  <ellipse cx="144" cy="294" rx="14" ry="6" fill="${SHOE}"/><ellipse cx="214" cy="294" rx="14" ry="6" fill="${SHOE}"/>
  ${torso(166,166,72,80)}
  ${face(166,114,"calm")}
  <path d="M134 184c-6 24 4 40 24 40" stroke="${SHIRT}" stroke-width="15" stroke-linecap="round" fill="none"/>
  <path d="M198 182c8 16 8 30 0 42" stroke="${SHIRT}" stroke-width="15" stroke-linecap="round" fill="none"/>
  ${phone(170,186,"#F7F5F2",-10)}<g transform="rotate(-10 183 207)">${star(183,202,5.5,2.6)}<circle cx="183" cy="202" r="1.6" fill="${RED}"/><rect x="175" y="212" width="16" height="4" rx="2" fill="#1E9E5A" class="ok-bar"/></g>
  ${hand(162,222,0)}${hand(196,220,-10)}
  <!-- rule card, rises once -->
  <g class="card-in"><rect x="214" y="104" width="136" height="96" rx="18" fill="#FBFAF8" stroke="${INK}" stroke-opacity=".08"/>
   <text x="228" y="126" font-family="Plus Jakarta Sans" font-weight="700" font-size="10" fill="#8a847d" letter-spacing=".08em">NEW RULE</text>
   <rect x="228" y="134" width="108" height="28" rx="14" fill="#EFEBE5"/><text x="240" y="152.5" font-family="Geist Mono,monospace" font-weight="600" font-size="12.5" fill="${INK}" class="typed">+91 140*</text>
   <rect x="228" y="170" width="56" height="22" rx="11" fill="${INK}"/><text x="256" y="185" text-anchor="middle" font-family="Plus Jakarta Sans" font-weight="700" font-size="10" fill="#F7F5F2">Reject</text>
   <rect x="302" y="172" width="34" height="18" rx="9" class="tgl"/><circle cx="327" cy="181" r="6.5" fill="#fff" class="tgl-k"/></g>
  </svg>`}

/* ---------- 3 AFTER · balcony at sunset ---------- */
function sceneAfter(){return`<svg viewBox="0 0 360 330" class="scn s-after" aria-hidden="true">
  <defs><linearGradient id="dusk" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#F3E3D3"/><stop offset=".7" stop-color="#F6D7B8"/><stop offset="1" stop-color="#EFC6A2"/></linearGradient></defs>
  <rect x="10" y="10" width="340" height="220" rx="22" fill="url(#dusk)"/>
  <circle cx="250" cy="150" r="34" fill="#F4B27A" class="sun"/>
  <path d="M10 190q40-18 80-6t90-8 90 4 80-6v66H10z" fill="#E5BE9C" opacity=".8"/><path d="M10 206q60-12 120-2t120-4 100 2v28H10z" fill="#D9AE8C"/>
  <path d="M120 70q6-4 12 0M136 64q5-3 10 0" stroke="#B49A85" stroke-width="1.8" fill="none" stroke-linecap="round" class="birds"/>
  <!-- railing -->
  <rect x="10" y="226" width="340" height="8" rx="4" fill="#A89D90"/>${Array.from({length:12},(_,i)=>`<rect x="${22+i*28}" y="234" width="5" height="60" fill="#B9AFA3"/>`).join("")}
  <rect x="10" y="292" width="340" height="10" rx="4" fill="#A89D90"/>
  <!-- plants -->
  <path d="M26 300h34l-4-32h-26z" fill="#C9A88C"/><path d="M43 268c-12-16-14-32-6-44 8 12 8 28 6 44zM43 268c8-18 20-26 32-24-6 14-16 22-32 24zM43 268c-10-6-22-6-28 0 8 7 18 7 28 0z" fill="#6F8660"/>
  <!-- lounge chair -->
  <path d="M96 300l20-70h70" stroke="#8f8579" stroke-width="6" stroke-linecap="round" fill="none"/><path d="M110 250l-6 50M196 230v70" stroke="#8f8579" stroke-width="6" stroke-linecap="round"/>
  <path d="M104 160c4-14 14-20 26-18l20 90h-50z" fill="#D8CEC2"/>
  <!-- person reclining with tea -->
  <path d="M140 232c22 2 44 2 58-2l26 24 -2 40" stroke="${PANTS}" stroke-width="20" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
  <ellipse cx="226" cy="296" rx="15" ry="6" fill="${SHOE}"/>
  <g transform="rotate(-6 142 236)">${torso(142,166,66,70)}</g>
  ${face(140,128,"bliss")}
  <path d="M168 184c14 8 24 18 26 30" stroke="${SHIRT}" stroke-width="14" stroke-linecap="round" fill="none"/>
  <rect x="186" y="200" width="20" height="22" rx="4" fill="#F7F5F2" stroke="${INK}" stroke-opacity=".15"/><path d="M206 205a6 6 0 0 1 0 11" stroke="#F7F5F2" stroke-width="3" fill="none"/>
  ${hand(194,216,0)}
  <path class="steam" d="M192 194c-4-6 4-8 0-14M200 194c-4-6 4-8 0-14" stroke="#B9A795" stroke-width="2.2" fill="none" stroke-linecap="round"/>
  <!-- side table with quiet phone -->
  <rect x="258" y="250" width="70" height="8" rx="4" fill="#A89D90"/><path d="M272 258v40M314 258v40" stroke="#A89D90" stroke-width="5" stroke-linecap="round"/>
  <rect x="270" y="236" width="48" height="12" rx="4" fill="${INK}"/><rect x="274" y="238.5" width="40" height="7" rx="2.5" fill="#F7F5F2"/><circle cx="279" cy="242" r="2" fill="${RED}"/>
  <g class="calm"><rect x="262" y="190" width="76" height="30" rx="15" fill="#FBFAF8" stroke="${INK}" stroke-opacity=".08"/>${star(280,205,6,2.6)}<circle cx="280" cy="205" r="1.7" fill="${RED}"/>
   <text x="292" y="203" font-family="Plus Jakarta Sans" font-weight="800" font-size="9.5" fill="${INK}">38 blocked</text><text x="292" y="213" font-family="Plus Jakarta Sans" font-weight="600" font-size="7.5" fill="#8a847d">today · quiet</text></g>
  </svg>`}
function obArt(){return[sceneBefore,sceneSetup,sceneAfter][S.ob]()}
R.onboard=()=>{const o=OB[S.ob];return`<div class="ob"><div class="ob-top"><div class="dots">${OB.map((_,i)=>`<i class="${i===S.ob?"on":""}"></i>`).join("")}</div>
  ${S.ob<2?`<button class="skip t-label" id="skip">Skip</button>`:`<span></span>`}</div>
  <div class="ob-art st" id="obArt">${obArt()}</div>
  <div class="ob-copy st"><span class="ob-k t-label">${S.ob+1} · ${o.k}</span><h1 class="t-title">${o.t}</h1><p class="t-body mute">${o.b}</p></div>
  <div class="dock st"><button class="btn brand press" id="obNext">${S.ob===1&&S.role?"Continue":o.cta}${ic("arrow-right","s")}</button>
  ${S.ob===1&&!S.role?`<button class="btn ghost press" id="obLater">Not now</button>`:""}</div></div>`};
function finishOb(){SFX.shieldOn();go("home");setTimeout(()=>island(S.role?"Globber is protecting you":"Screening is off",S.role?`${activeRules()} rules active · calls screened silently`:"Set Globber as call screening app to block",S.role?"shield":"warning",S.role?"var(--ok)":"var(--warn)",3200),450)}
A.onboard=()=>{$("#skip")&&($("#skip").onclick=()=>{hap("light");S.ob=2;go("onboard")});
  $("#obLater")&&($("#obLater").onclick=()=>{hap("soft");S.ob=2;go("onboard")});
  ["contacts","notify","unknown"].forEach(k=>bindToggle("ob-"+k,k));
  $("#obNext").onclick=()=>{hap("light");if(S.ob===0){S.ob=1;go("onboard");return}
    if(S.ob===1&&!S.role){roleDialog(ok=>{if(ok){S.role=true;S.ob=2;go("onboard")}});return}
    if(S.ob===1){S.ob=2;go("onboard");return}finishOb()}};

/* Android system role dialog */
function roleDialog(done){const d=document.createElement("div");d.className="sys";let pick="globber";
  d.innerHTML=`<div class="sys-pn" role="dialog"><span class="sys-ic">${appIcon("sys")}</span><b class="t-h">Set Globber as your default caller ID & spam app?</b>
  ${[["globber","Globber","Blocks calls by pattern"],["phone","Phone by Google","Caller ID & spam"]].map(([v,t,s])=>`<button class="opt ${v===pick?"on":""}" data-v="${v}"><span class="rd"></span><span style="display:grid;text-align:left"><b class="t-num">${t}</b><span class="t-meta" style="opacity:.7">${s}</span></span></button>`).join("")}
  <div class="sys-acts"><button id="sysNo">Cancel</button><button class="pri" id="sysOk">Set as default</button></div></div>`;
  PH.appendChild(d);hap("soft");
  d.querySelectorAll(".opt").forEach(b=>b.onclick=()=>{pick=b.dataset.v;hap("select");d.querySelectorAll(".opt").forEach(x=>x.classList.toggle("on",x===b))});
  const close=ok=>{d.style.animation="fadeout .2s forwards";setTimeout(()=>d.remove(),200);done(ok)};
  d.querySelector("#sysNo").onclick=()=>{SFX.deny();close(false);island("Screening not enabled","Globber can't block calls until it's set","warning","var(--warn)")};
  d.querySelector("#sysOk").onclick=()=>{if(pick!=="globber"){SFX.deny();close(false);island("Phone by Google kept","Globber needs the screening role to block","info","var(--info)",3000);return}
    SFX.allow();close(true);island("Call screening on","Globber now checks every incoming call","shield","var(--ok)")}}

/* ================= HOME ================= */
function weekBars(){const d=[];for(let i=6;i>=0;i--){const day=new Date(TODAY);day.setDate(day.getDate()-i);
  const n=S.log.filter(l=>new Date(l.t).toDateString()===day.toDateString()).length+[2,0,3,1,2,0,0][6-i];d.push([day.toLocaleDateString("en-GB",{weekday:"narrow"}),n,i===0])}
  const mx=Math.max(...d.map(x=>x[1]),1);return d.map(([l,n,on],i)=>`<div class="c ${on?"on":""}" style="--i:${i}"><i style="--h:${Math.max(6,n/mx*100)}"></i><b>${l}</b></div>`).join("")}
function topRule(){return[...S.rules].sort((a,b)=>b.hits-a.hits)[0]}
R.home=()=>{const tr=topRule();return`
  <div class="hd st"><div class="brand-m"><span class="logo">${appIcon("lgo")}</span><div class="stack-1"><b class="t-h">Globber</b><span class="t-meta mute">Sat, 17 Oct</span></div></div>
    <button class="prot press ${S.role?"":"off"}" id="protPill"><i></i><b class="t-label">${S.role?"Protected":"Not screening"}</b></button></div>
  ${S.role?"":`<div class="banner st"><span class="tile warn">${ic("warning")}</span><div class="grow stack-1"><b class="t-num">Calls aren't being screened</b><span class="t-meta mute">Set Globber as call screening app</span></div><button class="btn sm brand press" id="fixRole">Fix</button></div>`}
  <div class="grid2 st">
    <div class="kpi hot"><div class="row"><span class="tile sm">${ic("forbidden","s")}</span><span class="t-label mute">Today</span></div><b class="t-num-l" id="kToday">${todayCount()}</b><span class="t-meta mute">calls blocked</span></div>
    <div class="kpi"><div class="row"><span class="tile sm acc">${ic("calendar","s")}</span><span class="t-label mute">7 days</span></div><b class="t-num-l" id="kWeek">${weekCount()}</b><span class="t-meta mute">blocked this week</span></div>
    <button class="kpi press" data-go="rules"><div class="row"><span class="tile sm info">${ic("rules","s")}</span>${ic("chevron","xs")}</div><b class="t-num-l" id="kRules">${activeRules()}<small class="t-meta mute"> / ${S.rules.length}</small></b><span class="t-meta mute">rules active</span></button>
    <button class="kpi press" id="topR"><div class="row"><span class="tile sm warn">${ic("star","s")}</span>${ic("chevron","xs")}</div><b class="t-num num-call ell">${tr.pattern}</b><span class="t-meta mute">top rule · ${tr.hits} hits</span></button></div>
  <div class="card st"><div class="sec"><span class="t-label">This week</span><span class="t-label mute">${weekCount()} blocked</span></div><div class="chart">${weekBars()}</div></div>
  <div class="sec st"><span class="t-label">Recent activity</span><button class="t-label acc-t" data-go="log">See all</button></div>
  ${S.log.length?`<div class="txcard st">${S.log.slice(0,3).map(logRow).join("")}</div>`:`<div class="card empty st">${ic("shield","l")}<b class="t-h">No blocked calls yet</b><span class="t-meta mute">They'll appear here the moment a rule fires.</span></div>`}
  <button class="btn ghost press st" id="sim">${ic("incoming","s")} Simulate a spam call</button>`};
A.home=()=>{$("#fixRole")&&($("#fixRole").onclick=()=>roleDialog(ok=>{if(ok){S.role=true;rerender()}}));
  $("#protPill").onclick=()=>{hap("light");S.role?island("Protected",`${activeRules()} rules · ${S.contacts?"contacts allowed":"contacts not allowed"}`,"shield","var(--ok)"):roleDialog(ok=>{if(ok){S.role=true;rerender()}})};
  $("#topR").onclick=()=>ruleSheet(topRule());$("#sim").onclick=simulateCall;bindLogRows()};

/* demo-critical moment: incoming spam call blocked by "+91 140*" */
const SIM=["+91 140 4482 917","1900 410 2266","+91 44 2300 0000"];
function simulateCall(){const n=SIM[S.simIdx%SIM.length];S.simIdx++;const r=S.rules.find(x=>x.on&&matches(x,n));
  const o=document.createElement("div");o.className="inc";
  o.innerHTML=`<span class="t-label" style="opacity:.6">Incoming call</span><div class="av">${ic("user","l")}</div><b class="t-title num-call">${n}</b><span class="t-meta" style="opacity:.6">Mobile · India</span>
   <div class="stamp"><span class="seal">${ic("forbidden")}</span><b class="t-h">Blocked by Globber</b><span class="t-meta" style="opacity:.7">Matched <span class="num-call">${r?r.pattern:""}</span> · never rang</span></div>
   <div class="ans"><span class="r">${ic("endcall")}</span><span>${ic("call")}</span></div>`;
  PH.appendChild(o);SFX.ring();hap("medium");
  if(!S.role||!r){setTimeout(()=>{o.classList.add("out");setTimeout(()=>o.remove(),450);
      island(S.role?"Call let through":"Not screening",S.role?"No rule matched this number":"Set Globber as call screening app first",S.role?"call":"warning",S.role?"var(--info)":"var(--warn)");SFX.alert()},1500);return}
  setTimeout(()=>{o.classList.add("blocked");SFX.block()},900);
  setTimeout(()=>{o.classList.add("out");setTimeout(()=>o.remove(),450);r.hits++;S.log.unshift({id:Date.now(),number:n,rule:r.id,t:TODAY.getTime()+S.simIdx*60000});
    if(S.scr==="home")rerender();
    if(S.notify)island("Blocked "+n,`Matched ${r.pattern} · ${ACTS[r.action][3]}`,"forbidden","var(--warn)",3200,{l:"View",f:()=>go("log")})},2600)}

/* ================= RULES ================= */
const RF=[["all","All"],["on","Active"],["off","Paused"],["STARTS","Starts with"],["CONTAINS","Contains"],["ENDS","Ends with"],["EXACT","Exact"],["REGEX","Regex"]];
function visibleRules(){const q=S.q.toLowerCase();let l=S.rules.filter(r=>(!q||r.pattern.toLowerCase().includes(q)||r.label.toLowerCase().includes(q))&&
  (S.f==="all"||(S.f==="on"&&r.on)||(S.f==="off"&&!r.on)||r.type===S.f));
  return S.sort==="hits"?l.sort((a,b)=>b.hits-a.hits):l.sort((a,b)=>b.id-a.id)}
const ruleCard=(r,i)=>`<div class="rule press st ${r.on?"":"off"} ${r.fresh?"fresh":""}" data-rule="${r.id}" style="--i:${i}">${typeTile(r.type)}
  <span class="main"><b class="pat ell">${r.pattern}</b><span class="t-meta mute ell">${r.label||TYPES[r.type][0]}</span><span class="meta">${actBadge(r.action)}${bd(TYPES[r.type][0])}</span></span>
  <span class="hits">${toggle("tg"+r.id,r.on,"Rule "+r.pattern)}<span class="t-label mute">${r.hits} hits</span></span></div>`;
function rulesList(){const l=visibleRules();if(!S.rules.length)return`<div class="card empty">${ic("filter","l")}<b class="t-h">No rules yet</b><span class="t-meta mute">Add a number or pattern to start blocking calls.</span><button class="btn brand press" id="first">${ic("plus","s")} Add your first rule</button></div>`;
  if(!l.length)return`<div class="card empty">${ic("search","l")}<b class="t-h">No rules match</b><span class="t-meta mute">Try a different search or filter.</span><button class="btn ghost sm press" id="clr">Clear filters</button></div>`;
  return l.map(ruleCard).join("")}
R.rules=()=>`${cbar("Rules")}<div class="row st"><div class="stack-1"><h1 class="t-title">Rules</h1><span class="t-meta mute">${activeRules()} of ${S.rules.length} active · first match wins</span></div>
  <button class="icon-btn press" id="sortB" aria-label="Sort">${ic("sort","s")}</button></div>
  <label class="field st">${ic("search","s")}<input id="rq" placeholder="Search pattern or label" value="${S.q}">${S.q?`<button id="rqx" aria-label="Clear">${ic("close","s")}</button>`:""}</label>
  <div class="chips st">${RF.map(([v,l])=>`<button class="chip ${S.f===v?"on":""}" data-f="${v}">${l}</button>`).join("")}</div>
  <div class="stack" id="rlist">${rulesList()}</div><div style="height:64px" aria-hidden="true"></div>
  <button class="fab press ovv" id="add">${ic("plus","s")} Add rule</button>`;
function bindRuleList(){$$("[data-rule]").forEach(c=>c.onclick=()=>ruleSheet(ruleById(+c.dataset.rule)));
  S.rules.forEach(r=>bindToggle("tg"+r.id,"_t",()=>{r.on=!r.on;island(r.on?"Rule active":"Rule paused",r.pattern,r.on?"check":"mute",r.on?"var(--ok)":"var(--info)",2000);rerender()}));
  $("#first")&&($("#first").onclick=()=>ruleSheet());$("#clr")&&($("#clr").onclick=()=>{S.q="";S.f="all";rerender()})}
A.rules=()=>{$("#add").onclick=()=>ruleSheet();
  $("#sortB").onclick=()=>{S.sort=S.sort==="hits"?"new":"hits";hap("select");rerender();island("Sorted by "+(S.sort==="hits"?"most hits":"newest"),"","sort","var(--info)",1600)};
  const q=$("#rq");q.oninput=()=>{S.q=q.value;$("#rlist").innerHTML=rulesList();bindRuleList()};
  $("#rqx")&&($("#rqx").onclick=()=>{S.q="";rerender()});
  $$("[data-f]").forEach(b=>b.onclick=()=>{S.f=b.dataset.f;rerender()});bindRuleList();S.rules.forEach(r=>delete r.fresh)};

/* add / edit rule sheet with live tester */
function ruleSheet(rule,prefill){const ed=!!rule;const d=rule?{...rule}:{pattern:prefill?.pattern||"",type:prefill?.type||"STARTS",label:prefill?.label||"",action:"REJECT",on:true,hits:0};
  let test=prefill?.test||"+91 140 4482 917";
  const testHTML=()=>{const ok=d.pattern.trim()&&matches(d,test);return`<span class="tile sm ${ok?"warn":"ok"}">${ic(ok?"forbidden":"call","s")}</span><span class="grow stack-1"><input id="tn" class="t-num num-call" style="border:0;background:none;outline:0;color:inherit;width:100%" value="${test}" aria-label="Test number"><span class="t-meta mute">${!d.pattern.trim()?"Type a pattern to test":ok?"Would be "+ACTS[d.action][3]:"Would ring normally"}</span></span>`};
  sheet(`<div class="sheet-h"><b class="t-title">${ed?"Edit rule":"New rule"}</b>${ed?`<button class="icon-btn press" id="del" aria-label="Delete rule">${ic("trash","s")}</button>`:""}</div>
   <div class="lbl-f"><span class="t-label">Pattern</span><label class="field mono" id="pf">${ic("hash","s")}<input id="pat" placeholder="+91 140*" value="${d.pattern.replace(/"/g,"&quot;")}"></label><span class="err-msg t-meta" id="perr" hidden></span></div>
   <div class="lbl-f"><span class="t-label">Match type</span><div class="chips" id="types">${Object.entries(TYPES).map(([k,[l]])=>`<button class="chip ${d.type===k?"on":""}" data-t="${k}">${l}</button>`).join("")}</div></div>
   <div class="lbl-f"><span class="t-label">Test a number</span><div class="tester" id="tester">${testHTML()}</div></div>
   <div class="lbl-f"><span class="t-label">When it matches</span><div class="opts3">${Object.entries(ACTS).map(([k,[l,i]])=>`<button class="opt press ${d.action===k?"on":""}" data-a="${k}">${ic(i)}<b class="t-label">${l}</b></button>`).join("")}</div></div>
   <div class="lbl-f"><span class="t-label">Label (optional)</span><label class="field">${ic("pen","s")}<input id="lab" placeholder="e.g. Loan offers" value="${d.label}"></label></div>
   <button class="btn brand press" id="save">${ic("check","s")} ${ed?"Save changes":"Add rule"}</button>`,s=>{
    const tester=s.querySelector("#tester");
    const paint=()=>{const ok=d.pattern.trim()&&matches(d,test);tester.className="tester "+(ok?"hit":"miss");tester.innerHTML=testHTML();const tn=tester.querySelector("#tn");tn.oninput=()=>{test=tn.value;const p=tn.selectionStart;paint();const n=s.querySelector("#tn");n.focus();n.setSelectionRange(p,p)}};paint();
    const err=m=>{const e=s.querySelector("#perr");e.hidden=!m;e.innerHTML=m?ic("warning","xs")+m:"";s.querySelector("#pf").classList.toggle("err",!!m)};
    const pat=s.querySelector("#pat");pat.oninput=()=>{d.pattern=pat.value;err("");paint()};
    s.querySelector("#lab").oninput=e=>d.label=e.target.value;
    s.querySelectorAll("[data-t]").forEach(b=>b.onclick=()=>{d.type=b.dataset.t;hap("select");s.querySelectorAll("[data-t]").forEach(x=>x.classList.toggle("on",x===b));pat.placeholder=d.type==="REGEX"?"^\\+91 ?140":"+91 140*";err("");paint()});
    s.querySelectorAll("[data-a]").forEach(b=>b.onclick=()=>{d.action=b.dataset.a;hap("select");s.querySelectorAll("[data-a]").forEach(x=>x.classList.toggle("on",x===b));paint()});
    s.querySelector("#save").onclick=()=>{const p=d.pattern.trim();
      const m=!p?"Enter a number or pattern":d.type==="REGEX"&&!validRegex(p)?"That regex isn't valid":d.type!=="REGEX"&&digits(p).replace(/\*/g,"").length<3?"Use at least 3 digits":
        S.rules.some(r=>r.id!==d.id&&r.pattern===p&&r.type===d.type)?"You already have this rule":"";
      if(m){err(m);SFX.err();s.querySelector("#pf").classList.remove("shake");void s.offsetWidth;s.querySelector("#pf").classList.add("shake");return}
      d.pattern=p;if(ed){Object.assign(rule,d)}else{d.id=S.nextId++;d.fresh=true;S.rules.unshift(d)}
      closeSheet(s);SFX.success();if(S.scr!=="rules")go("rules");else rerender();
      island(ed?"Rule saved":"Rule added",`${p} · ${ACTS[d.action][0]}`,"check","var(--ok)")};
    s.querySelector("#del")&&(s.querySelector("#del").onclick=()=>{closeSheet(s);confirmSheet("Delete this rule?",`<span class="num-call">${rule.pattern}</span> will stop blocking calls. Past log entries stay.`,"Delete rule",()=>{
      S.rules=S.rules.filter(r=>r!==rule);rerender();SFX.bye();island("Rule deleted",rule.pattern,"trash","var(--warn)",3000,{l:"Undo",f:()=>{S.rules.push(rule);rerender();SFX.ok()}})})})})}
function confirmSheet(title,body,cta,ok){sheet(`<div class="center stack" style="padding-top:4px"><span class="tile lg warn">${ic("trash")}</span><b class="t-title">${title}</b><p class="t-body mute">${body}</p></div>
  <div class="stack"><button class="btn danger press" id="cok">${cta}</button><button class="btn ghost press" data-close>Cancel</button></div>`,s=>{hap("warning");s.querySelector("#cok").onclick=()=>{hap("medium");closeSheet(s);ok()}})}

/* ================= LOG ================= */
const LF=[["all","All"],["REJECT","Rejected"],["SILENCE","Silenced"],["VOICEMAIL","Voicemail"]];
function visibleLog(){const q=S.lq.replace(/\s/g,"").toLowerCase();return S.log.filter(l=>{const r=ruleById(l.rule);
  return(!q||l.number.replace(/\s/g,"").toLowerCase().includes(q)||(r&&r.pattern.replace(/\s/g,"").toLowerCase().includes(q)))&&(S.lf==="all"||(r&&r.action===S.lf))})}
function logBody(){if(!S.logReady)return`<div class="txcard">${[0,1,2,3,4].map(()=>`<div class="skel skrow"></div>`).join("")}</div>`;
  if(!S.log.length)return`<div class="card empty"><svg class="art" viewBox="0 0 150 110">${'<rect x="20" y="20" width="110" height="70" rx="18" fill="var(--surface-2)"/><circle cx="48" cy="55" r="12" fill="var(--acc-soft)"/><rect x="68" y="47" width="46" height="6" rx="3" fill="var(--line)"/><rect x="68" y="59" width="30" height="6" rx="3" fill="var(--line)"/>'}</svg><b class="t-h">No blocked calls yet</b><span class="t-meta mute">Blocked calls will show up here.</span></div>`;
  const l=visibleLog();if(!l.length)return`<div class="card empty">${ic("search","l")}<b class="t-h">Nothing matches</b><span class="t-meta mute">No blocked call fits that search.</span><button class="btn ghost sm press" id="lclr">Clear search</button></div>`;
  const g={};l.forEach(x=>(g[dayKey(x.t)]=g[dayKey(x.t)]||[]).push(x));
  return Object.entries(g).map(([k,v])=>`<div class="day"><span class="t-label">${k}</span><span class="t-label mute">${v.length} blocked</span></div><div class="txcard">${v.map(logRow).join("")}</div>`).join("")}
R.log=()=>`${cbar("Blocked calls")}<div class="row st"><div class="stack-1"><h1 class="t-title">Blocked calls</h1><span class="t-meta mute">${S.log.length} screened · last 7 days</span></div>
  <button class="icon-btn press" id="lclear" aria-label="Clear log" ${S.log.length?"":"disabled"}>${ic("trash","s")}</button></div>
  <label class="field st">${ic("search","s")}<input id="lq" placeholder="Search number or pattern" value="${S.lq}">${S.lq?`<button id="lqx" aria-label="Clear search">${ic("close","s")}</button>`:""}</label>
  <div class="chips st">${LF.map(([v,l])=>`<button class="chip ${S.lf===v?"on":""}" data-lf="${v}">${l}</button>`).join("")}</div>
  <div class="stack st" id="lbody">${logBody()}</div>`;
function bindLogRows(){$$("[data-log]").forEach(b=>b.onclick=()=>callSheet(S.log.find(l=>l.id===+b.dataset.log)));$("#lclr")&&($("#lclr").onclick=()=>{S.lq="";S.lf="all";rerender()})}
A.log=()=>{if(!S.logReady)setTimeout(()=>{S.logReady=true;if(S.scr==="log"){$("#lbody").innerHTML=logBody();bindLogRows()}},700);
  const q=$("#lq");q.oninput=()=>{S.lq=q.value;$("#lbody").innerHTML=logBody();bindLogRows()};
  $("#lqx")&&($("#lqx").onclick=()=>{S.lq="";rerender()});
  $$("[data-lf]").forEach(b=>b.onclick=()=>{S.lf=b.dataset.lf;rerender()});
  $("#lclear").onclick=()=>confirmSheet("Clear the log?",`All ${S.log.length} blocked-call entries will be removed. Your rules stay.`,"Clear log",()=>{const old=S.log;S.log=[];rerender();SFX.empty();
    island("Log cleared",old.length+" entries removed","trash","var(--warn)",3200,{l:"Undo",f:()=>{S.log=old;rerender();SFX.ok()}})});
  bindLogRows()};
function callSheet(l){const r=ruleById(l.rule),a=r?r.action:"REJECT",nn=natural(l.number),allowed=S.allow.includes(nn);
  const ps=l.number.split(" "),pref=ps.length>3?ps.slice(0,2).join(" ")+" "+ps[2].slice(0,2):ps.slice(0,-1).join(" ");
  sheet(`<div class="det-hero"><span class="tile ${ACTS[a][2]}">${ic(ACTS[a][1],"l")}</span><b class="t-title num-call">${l.number}</b><span class="t-meta mute">${dayKey(l.t)} · ${hm(l.t)} · ${ACTS[a][0]}</span></div>
   <div class="det-grid"><div><span class="t-label mute">Matched rule</span><b class="t-num num-call ell">${r?r.pattern:"Deleted rule"}</b></div><div><span class="t-label mute">Match type</span><b class="t-num">${r?TYPES[r.type][0]:"—"}</b></div>
    <div><span class="t-label mute">Times blocked</span><b class="t-num">${S.log.filter(x=>natural(x.number)===nn).length}</b></div><div><span class="t-label mute">Rule label</span><b class="t-num ell">${r&&r.label||"—"}</b></div></div>
   <div class="act-list">
    <button class="act press" id="allowN"><span class="tile sm ok">${ic(allowed?"undo":"check","s")}</span><span class="grow stack-1"><b class="t-num">${allowed?"Block this number again":"Always allow this number"}</b><span class="t-meta mute">${allowed?"Remove it from your allow list":"Adds an exception above every rule"}</span></span>${ic("chevron","xs")}</button>
    <button class="act press" id="mkRule"><span class="tile sm acc">${ic("plus","s")}</span><span class="grow stack-1"><b class="t-num">Block numbers like this</b><span class="t-meta mute num-call">New rule · starts with ${pref}*</span></span>${ic("chevron","xs")}</button>
    <button class="act press" id="cpN"><span class="tile sm info">${ic("copy","s")}</span><span class="grow stack-1"><b class="t-num">Copy number</b><span class="t-meta mute">To report or look it up</span></span>${ic("chevron","xs")}</button></div>`,s=>{
    s.querySelector("#allowN").onclick=()=>{closeSheet(s);if(allowed){S.allow=S.allow.filter(x=>x!==nn);SFX.ok();island("Blocking again",l.number,"forbidden","var(--warn)")}
      else{S.allow.push(nn);SFX.allow();island("Number allowed",l.number+" will always ring","check","var(--ok)",3000,{l:"Undo",f:()=>{S.allow=S.allow.filter(x=>x!==nn);rerender()}})}rerender()};
    s.querySelector("#mkRule").onclick=()=>{closeSheet(s);setTimeout(()=>ruleSheet(null,{pattern:pref+"*",type:"STARTS",label:"From blocked call",test:l.number}),250)};
    s.querySelector("#cpN").onclick=()=>{SFX.copy();closeSheet(s);island("Copied",l.number,"copy","var(--info)",1800)}})}

/* ================= SETTINGS ================= */
R.settings=()=>`${cbar("Settings")}<div class="stack-1 st"><h1 class="t-title">Settings</h1></div>
  <div class="prot-card st"><div class="row start"><span class="tile lg ${S.role?"solid":"warn"}">${ic(S.role?"shield":"shieldx")}</span><div class="grow stack-1"><b class="t-h">${S.role?"Call screening is on":"Call screening is off"}</b><span class="t-meta mute">${S.role?"Globber checks every incoming call":"Globber can't block until it's the screening app"}</span></div></div>
   <button class="btn ${S.role?"ghost":"brand"} press" id="roleB">${S.role?"Change screening app":"Set as call screening app"}</button></div>
  <div class="sec st"><span class="t-label">Calls</span></div>
  <div class="txcard st">
   <div class="set-row"><span class="tile info">${ic("users")}</span><span class="main"><b class="t-h">Let contacts through</b><span class="t-meta mute">${S.contacts?"Saved contacts always ring":"Needs contacts permission"}</span></span>${toggle("sContacts",S.contacts,"Let contacts through")}</div>
   <div class="set-row"><span class="tile warn">${ic("unknown")}</span><span class="main"><b class="t-h">Block hidden numbers</b><span class="t-meta mute">Private or withheld caller ID</span></span>${toggle("sUnknown",S.unknown,"Block hidden numbers")}</div>
   <div class="set-row"><span class="tile acc">${ic("bell")}</span><span class="main"><b class="t-h">Notify when blocked</b><span class="t-meta mute">One quiet notification per call</span></span>${toggle("sNotify",S.notify,"Notify when blocked")}</div>
   <button class="set-row press" id="allowL"><span class="tile ok">${ic("check")}</span><span class="main"><b class="t-h">Allowed numbers</b><span class="t-meta mute">${S.allow.length?S.allow.length+" always ring":"None yet"}</span></span>${ic("chevron","xs")}</button></div>
  <div class="sec st"><span class="t-label">Appearance</span></div>
  <div class="card stack st">${seg("th",[["system","System"],["light","Light"],["dark","Dark"]],S.theme)}
   <div class="row"><span class="t-meta mute">Haptic feedback</span>${toggle("sHap",S.haptics,"Haptics")}</div></div>
  <div class="sec st"><span class="t-label">Backup</span><span class="t-label mute">${S.rules.length} rules</span></div>
  <div class="txcard st">
   <button class="set-row press" id="exp"><span class="tile acc">${ic("export")}</span><span class="main"><b class="t-h">Export rules</b><span class="t-meta mute">Save all rules to a JSON file</span></span>${ic("chevron","xs")}</button>
   <button class="set-row press" id="imp"><span class="tile info">${ic("import")}</span><span class="main"><b class="t-h">Import rules</b><span class="t-meta mute">Add rules from an exported file</span></span>${ic("chevron","xs")}</button></div>
  <div class="privacy st"><span class="tile ok">${ic("lock")}</span><span class="t-meta mute grow">No internet permission, no analytics, no ads. Everything stays on this phone.</span></div>
  <p class="ver t-label mute st">Globber 1.0.6 · app.globber</p>`;
A.settings=()=>{$("#roleB").onclick=()=>roleDialog(ok=>{if(ok){S.role=true;rerender()}});
  bindToggle("sContacts","contacts",on=>{if(on){S.contacts=false;$("#sContacts").setAttribute("aria-checked",false);contactsSheet()}else rerender()});
  bindToggle("sUnknown","unknown",on=>island(on?"Hidden numbers blocked":"Hidden numbers allowed","Applies to private & withheld IDs",on?"forbidden":"call",on?"var(--warn)":"var(--info)",2000));
  bindToggle("sNotify","notify");bindToggle("sHap","haptics");
  segBind(V,"[data-seg=th]",v=>{S.theme=v;setTheme("settings")});
  $("#allowL").onclick=()=>sheet(`<b class="t-title">Allowed numbers</b>${S.allow.length?`<div class="txcard">${S.allow.map(n=>`<div class="txr"><span class="tile ok">${ic("check")}</span><span class="main"><b class="t-num num-call">${n}</b><span class="t-meta mute">Always rings</span></span><span></span></div>`).join("")}</div>`:`<div class="empty">${ic("check","l")}<b class="t-h">No exceptions yet</b><span class="t-meta mute">Open a blocked call and tap “Always allow” to add one.</span></div>`}<button class="btn ghost press" data-close>Done</button>`);
  $("#exp").onclick=()=>{SFX.success();island("Rules exported","globber-rules-2026-10-17.json · Downloads","download","var(--ok)",3000)};
  $("#imp").onclick=importSheet};
function contactsSheet(){sheet(`<div class="center stack"><span class="tile lg info">${ic("users")}</span><b class="t-title">Allow Globber to see contacts?</b>
  <p class="t-body mute">Only used to let saved contacts through. Contacts never leave your phone.</p></div>
  <div class="stack"><button class="btn brand press" id="cAllow">Allow</button><button class="btn ghost press" id="cDeny">Don't allow</button></div>`,s=>{
  s.querySelector("#cAllow").onclick=()=>{S.contacts=true;closeSheet(s);SFX.allow();rerender();island("Contacts let through","Saved contacts will always ring","users","var(--ok)")};
  s.querySelector("#cDeny").onclick=()=>{closeSheet(s);SFX.deny();island("Permission denied","Contacts can't bypass rules","warning","var(--warn)")}})}
function importSheet(){const inc=[{pattern:"+91 160*",type:"STARTS",label:"Service-call series",action:"SILENCE"},{pattern:"*9999",type:"ENDS",label:"Fake bank IVR",action:"REJECT"},{pattern:"+91 140*",type:"STARTS",label:"Telemarketing series",action:"REJECT"}];
  const dup=r=>S.rules.some(x=>x.pattern===r.pattern&&x.type===r.type);
  sheet(`<div class="sheet-h"><b class="t-title">Import rules</b>${bd("JSON","info")}</div>
   <div class="card glass row start" style="background:var(--surface-2)"><span class="tile acc">${ic("download")}</span><span class="grow stack-1"><b class="t-num ell">globber-rules-2026-09-02.json</b><span class="t-meta mute">Downloads · 3 rules · 2 KB</span></span></div>
   <div class="txcard">${inc.map(r=>`<div class="txr"><span class="tile acc type-tile">${TYPES[r.type][1]}</span><span class="main"><b class="t-num num-call">${r.pattern}</b><span class="t-meta mute">${r.label}</span></span><span class="end">${dup(r)?bd("Duplicate"):bd("New","ok")}</span></div>`).join("")}</div>
   <button class="btn brand press" id="doImp">${ic("import","s")} Import ${inc.filter(r=>!dup(r)).length} new rules</button>`,s=>{
   s.querySelector("#doImp").onclick=()=>{const add=inc.filter(r=>!dup(r)).map(r=>({...r,id:S.nextId++,on:true,hits:0,fresh:true}));S.rules.unshift(...add);closeSheet(s);SFX.success();rerender();
     island(add.length+" rules imported","1 duplicate skipped","import","var(--ok)")}})}

const go0=go;go=(scr,dir)=>{$$(".sheet,.sys").forEach(e=>e.remove());go0(scr,dir)};
window.Gx={simulateCall,ruleSheet,callSheet,roleDialog,importSheet,contactsSheet,matches};
boot();
