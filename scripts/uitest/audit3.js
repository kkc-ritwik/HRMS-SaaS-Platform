/* Feature-level gap audit: a backend endpoint PATH is a real gap only if NO frontend service
   method that calls it is used by any page/component (dedupes duplicate service stubs). */
const fs = require('fs');
const path = require('path');
const ROOT = 'C:\\Users\\ritwi\\Desktop\\HRMS-SaaS-Platform\\hrms-frontend\\src';

function walk(dir, acc = []) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, e.name);
    if (e.isDirectory()) walk(full, acc);
    else if (full.endsWith('.ts') || full.endsWith('.tsx')) acc.push(full);
  }
  return acc;
}
function norm(p) { return p.split('?')[0].replace(/\$\{[^}]*\}/g,'*').replace(/\{[^}]*\}/g,'*').replace(/\/+$/,'')||'/'; }

const all = walk(ROOT);
const serviceFiles = all.filter(f => f.includes(path.sep + 'services' + path.sep));
const usageFiles = all.filter(f => !f.includes(path.sep + 'services' + path.sep));
const usageText = usageFiles.map(f => fs.readFileSync(f,'utf8')).join('\n');
function used(method) { return new RegExp('\\.'+method.replace(/[.*+?^${}()|[\]\\]/g,'\\$&')+'\\b').test(usageText); }

// path -> { anyUsed: bool, methods: Set }
const byPath = {};
for (const file of serviceFiles) {
  const src = fs.readFileSync(file,'utf8');
  const re = /([A-Za-z0-9_]+)\s*:\s*(?:async\s*)?\(/g;
  let m;
  while ((m = re.exec(src)) !== null) {
    const name = m[1];
    const snippet = src.slice(m.index, m.index + 320);
    const pm = snippet.match(/['"`](\/api\/[A-Za-z0-9_\-/${}.:]+)/);
    if (!pm) continue;
    const p = norm(pm[1]);
    byPath[p] = byPath[p] || { anyUsed: false, methods: new Set() };
    byPath[p].methods.add(name);
    if (used(name)) byPath[p].anyUsed = true;
  }
}

// Also: any path referenced directly inside a page/component counts as integrated
const directPagePaths = new Set();
{ const re=/['"`](\/api\/[A-Za-z0-9_\-/${}.:?&=]+)/g; let m; while((m=re.exec(usageText))!==null) directPagePaths.add(norm(m[1])); }

const gaps = [];
for (const [p, info] of Object.entries(byPath)) {
  if (info.anyUsed) continue;
  if (directPagePaths.has(p)) continue;
  gaps.push(p);
}
gaps.sort();

// group by module (segment 3)
const byMod = {};
for (const g of gaps) { const k=g.split('/').slice(0,4).join('/'); (byMod[k]=byMod[k]||[]).push(g); }
console.log(`Distinct endpoint paths with a FE service stub: ${Object.keys(byPath).length}`);
console.log(`Paths NOT reachable from any page (real feature gaps): ${gaps.length}\n`);
for (const k of Object.keys(byMod).sort()) {
  console.log(`### ${k}  (${byMod[k].length})`);
  byMod[k].forEach(g => console.log('   ' + g));
}
