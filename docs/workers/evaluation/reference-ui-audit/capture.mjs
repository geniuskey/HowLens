import fs from 'node:fs';
const dir='docs/workers/evaluation/reference-ui-audit/evidence';
const pages=await(await fetch('http://127.0.0.1:19347/json/list')).json();
const ws=new WebSocket(pages.find(x=>x.type==='page').webSocketDebuggerUrl);await new Promise(r=>ws.onopen=r);let seq=0;const q=new Map();ws.onmessage=e=>{const x=JSON.parse(e.data);if(q.has(x.id)){q.get(x.id)(x);q.delete(x.id)}};
const c=(method,params={})=>new Promise((resolve,reject)=>{let id=++seq;q.set(id,x=>x.error?reject(x.error):resolve(x.result));ws.send(JSON.stringify({id,method,params}))});
const ev=async expression=>(await c('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true})).result?.value;
await c('Page.enable');await c('Runtime.enable');
const mode=process.argv[2]||'home';const log=[];
for(const width of [320,390,1280]){
 await c('Emulation.setDeviceMetricsOverride',{width,height:width===1280?900:844,deviceScaleFactor:1,mobile:false});
 await c('Page.navigate',{url:'file:///Users/runixs/HowLens/docs/mockups/reference-ui/index.html'});await new Promise(r=>setTimeout(r,650));
 log.push(await ev(`({width:innerWidth,scrollWidth:document.documentElement.scrollWidth,title:document.title,text:document.body.innerText,buttons:[...document.querySelectorAll('button,a,input')].map(e=>({tag:e.tagName,text:e.innerText,aria:e.getAttribute('aria-label'),id:e.id,rect:{w:e.getBoundingClientRect().width,h:e.getBoundingClientRect().height}}))})`));
 let shot=await c('Page.captureScreenshot',{format:'png'});fs.writeFileSync(`${dir}/home-${width}.png`,Buffer.from(shot.data,'base64'));
}
fs.writeFileSync(`${dir}/home-dom.json`,JSON.stringify(log,null,2));ws.close();
