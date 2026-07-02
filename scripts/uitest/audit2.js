/* Finds frontend service methods (which call backend endpoints) that NO page/component/hook uses.
   These are "backend done + FE service stub exists, but not integrated into any UI page". */
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

const all = walk(ROOT);
const serviceFiles = all.filter(f => f.includes(path.sep + 'services' + path.sep));
const usageFiles = all.filter(f => !f.includes(path.sep + 'services' + path.sep)); // pages, components, hooks, lib

// Build usage corpus (everything outside services/)
const usageText = usageFiles.map(f => fs.readFileSync(f, 'utf8')).join('\n');
function usedSomewhere(method) {
  // matches `.method(`  or `method:`-style references in pages/components
  const re = new RegExp('[.\\s{,(]' + method.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '\\s*[(,)]');
  return re.test(usageText) || new RegExp('\\.' + method + '\\b').test(usageText);
}

// Extract `methodName: async (...) => ... '/api/...'` or `methodName: (...) => ... '/api/...'`
const methodRe = /([A-Za-z0-9_]+)\s*:\s*(?:async\s*)?\([^)]*\)\s*=>([\s\S]*?)(?=\n\s*[A-Za-z0-9_]+\s*:\s*(?:async\s*)?\(|\n\s*\}\s*$|\n\}\n)/g;
const pathRe = /['"`](\/api\/[A-Za-z0-9_\-/${}.:]+)/;

const unused = {};
let totalMethods = 0;
for (const file of serviceFiles) {
  const src = fs.readFileSync(file, 'utf8');
  const base = path.basename(file);
  let m;
  const re = /([A-Za-z0-9_]+)\s*:\s*(?:async\s*)?\(/g;
  while ((m = re.exec(src)) !== null) {
    const name = m[1];
    if (['then','catch','map','filter','forEach','async'].includes(name)) continue;
    // get the snippet after this method to find its api path
    const snippet = src.slice(m.index, m.index + 300);
    const pm = snippet.match(pathRe);
    if (!pm) continue; // only count methods that hit an /api path
    totalMethods++;
    if (!usedSomewhere(name)) {
      (unused[base] = unused[base] || []).push(`${name.padEnd(26)} -> ${pm[1].split('?')[0]}`);
    }
  }
}

const files = Object.keys(unused).sort();
let count = 0; files.forEach(f => count += unused[f].length);
console.log(`FE service methods hitting /api: ${totalMethods} | NOT used by any page/component: ${count}\n`);
for (const f of files) {
  console.log(`### ${f}  (${unused[f].length} unused)`);
  for (const line of unused[f].sort()) console.log('   ' + line);
  console.log('');
}
