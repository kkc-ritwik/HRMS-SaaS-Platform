/* Reverse integration audit: backend endpoints vs frontend API calls.
   Reports backend endpoints that NO frontend code references. */
const fs = require('fs');
const path = require('path');

const ROOT = 'C:\\Users\\ritwi\\Desktop\\HRMS-SaaS-Platform';

function walk(dir, filter, acc = []) {
  let entries = [];
  try { entries = fs.readdirSync(dir, { withFileTypes: true }); } catch { return acc; }
  for (const e of entries) {
    const full = path.join(dir, e.name);
    if (e.isDirectory()) { if (e.name !== 'node_modules' && e.name !== 'target') walk(full, filter, acc); }
    else if (filter(full)) acc.push(full);
  }
  return acc;
}

// ---- Backend endpoints ----
const controllers = walk(ROOT, f => f.endsWith('Controller.java') && f.includes('service-'));
const backend = []; // {method, path, file, svc}
for (const file of controllers) {
  const src = fs.readFileSync(file, 'utf8');
  const svc = (file.match(/service-[^\\]+/) || ['?'])[0];
  const clsMatch = src.match(/@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]+)"/);
  const base = clsMatch ? clsMatch[1] : '';
  const re = /@(Get|Post|Put|Patch|Delete)Mapping(?:\(\s*(?:value\s*=\s*)?"([^"]*)")?/g;
  let m;
  while ((m = re.exec(src)) !== null) {
    const method = m[1].toUpperCase();
    const sub = m[2] || '';
    let full = (base + (sub.startsWith('/') || sub === '' ? sub : '/' + sub)) || base;
    if (!full.startsWith('/')) full = '/' + full;
    backend.push({ method, path: full, file: path.basename(file), svc });
  }
}

// ---- Frontend API calls ----
const feFiles = walk(path.join(ROOT, 'hrms-frontend', 'src'), f => f.endsWith('.ts') || f.endsWith('.tsx'));
const feCalls = new Set();   // normalized path strings the FE references
for (const file of feFiles) {
  const src = fs.readFileSync(file, 'utf8');
  const re = /['"`](\/api\/[A-Za-z0-9_\-/${}.:?&=]+)['"`]/g;
  let m;
  while ((m = re.exec(src)) !== null) {
    let p = m[1].split('?')[0];
    feCalls.add(normalize(p));
  }
}

function normalize(p) {
  return p.replace(/\$\{[^}]*\}/g, '*').replace(/\{[^}]*\}/g, '*').replace(/\/+$/,'') || '/';
}

const feSet = new Set([...feCalls]);
function feHas(norm) {
  if (feSet.has(norm)) return true;
  // also treat a FE call to a deeper/parent path as covering (e.g. base list vs /me)
  for (const fe of feSet) {
    if (fe === norm) return true;
  }
  return false;
}

// ---- Diff ----
const gaps = {};
let total = 0, integrated = 0;
for (const ep of backend) {
  total++;
  const norm = normalize(ep.path);
  if (feHas(norm)) { integrated++; continue; }
  (gaps[ep.svc] = gaps[ep.svc] || []).push(`${ep.method.padEnd(6)} ${ep.path}  (${ep.file})`);
}

console.log(`BACKEND ENDPOINTS: ${total} | matched in frontend: ${integrated} | NOT referenced: ${total - integrated}\n`);
const svcs = Object.keys(gaps).sort();
for (const s of svcs) {
  console.log(`### ${s}  (${gaps[s].length} not referenced)`);
  for (const line of gaps[s].sort()) console.log('   ' + line);
  console.log('');
}
