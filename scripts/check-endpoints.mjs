const base=(process.argv[2]||'http://localhost:8080/api/v1').replace(/\/$/,'');
const paths=['/estado','/categorias','/mercados?page=0&size=10','/ediciones?page=0&size=10','/ediciones/hoy'];
let failed=false;
for(const path of paths){const times=[];let status=0;for(let i=0;i<3;i++){const start=performance.now();try{const response=await fetch(base+path,{signal:AbortSignal.timeout(70000)});status=response.status;await response.text();times.push(Math.round(performance.now()-start));if(!response.ok)failed=true;}catch{failed=true;status=0;break;}}const average=times.length?Math.round(times.reduce((a,b)=>a+b,0)/times.length):null;console.log(JSON.stringify({path,status,latencyMs:times,averageMs:average,targetMet:status===200&&average<500}));}
if(failed)process.exitCode=1;
