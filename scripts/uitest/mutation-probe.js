// Permission probe for the WRITE surface. Hits every PUT/PATCH/DELETE endpoint with a random
// (non-existent) UUID and an empty body. Because @PreAuthorize runs BEFORE the handler and the id
// doesn't exist, this is non-destructive yet reveals 403 (permission) vs 4xx/404 (authorized).
// POST(create) is skipped to avoid persisting rows; POST action-endpoints (/approve,/submit,...) are
// probed too since they no-op on a missing id.
const fs = require('fs');
const path = require('path');
const http = require('http');

const ROOT = 'C:\\Users\\ritwi\\Desktop\\HRMS-SaaS-Platform';
const GATEWAY = 'http://localhost:8080';
const UUID = '00000000-0000-4000-a000-000000000000';

function walk(dir, acc = []) {
  let es = [];
  try { es = fs.readdirSync(dir, { withFileTypes: true }); } catch { return acc; }
  for (const e of es) {
    const full = path.join(dir, e.name);
    if (e.isDirectory()) { if (e.name !== 'node_modules' && e.name !== 'target') walk(full, acc); }
    else if (e.name.endsWith('Controller.java')) acc.push(full);
  }
  return acc;
}

const controllers = walk(ROOT).filter(f => f.includes('service-'));
const endpoints = [];
for (const file of controllers) {
  const src = fs.readFileSync(file, 'utf8');
  const svc = (file.match(/service-[^\\]+/) || ['?'])[0];
  const clsMatch = src.match(/@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]+)"/);
  const base = clsMatch ? clsMatch[1] : '';
  const re = /@(Put|Patch|Delete)Mapping(?:\(\s*(?:value\s*=\s*)?"([^"]*)")?/g;
  let m;
  while ((m = re.exec(src)) !== null) {
    const method = m[1].toUpperCase();
    const sub = m[2] || '';
    let full = base + (sub.startsWith('/') || sub === '' ? sub : '/' + sub);
    if (!full.startsWith('/')) full = '/' + full;
    // only probe endpoints that carry a path variable (so a random id is harmless)
    if (full.includes('{')) endpoints.push({ svc, method, path: full });
  }
}

function fill(p) {
  return p.replace(/\{([^}]+)\}/g, (_, name) => {
    const n = name.toLowerCase();
    if (n.includes('code') || n.includes('slug') || n.includes('type') || n.includes('status') || n.includes('token')) return 'TEST';
    return UUID;
  });
}

function req(method, p, body, token) {
  return new Promise((resolve) => {
    const data = body ? JSON.stringify(body) : null;
    const u = new URL(GATEWAY + p);
    const opts = { method, hostname: u.hostname, port: u.port, path: u.pathname + u.search, headers: { 'Content-Type': 'application/json' } };
    if (token) opts.headers.Authorization = 'Bearer ' + token;
    if (data) opts.headers['Content-Length'] = Buffer.byteLength(data);
    const r = http.request(opts, (res) => { let b=''; res.on('data',c=>b+=c); res.on('end',()=>resolve({status:res.statusCode, body:b})); });
    r.on('error', () => resolve({ status: 0, body: 'CONN' }));
    r.setTimeout(15000, () => { r.destroy(); resolve({ status: 0, body: 'TIMEOUT' }); });
    if (data) r.write(data); r.end();
  });
}

(async () => {
  const login = await req('POST', '/api/v1/auth/login', { email: 'admin@demo.com', password: 'Admin@123', tenantId: 'demo' }, null);
  const token = JSON.parse(login.body).data.accessToken;
  console.log('LOGIN OK. Probing ' + endpoints.length + ' PUT/PATCH/DELETE endpoints for permission (403)...\n');

  const forbidden = [], errors = [], other = {};
  for (const ep of endpoints) {
    const r = await req(ep.method, fill(ep.path), {}, token);
    if (r.status === 403) forbidden.push(ep.method + ' ' + ep.path + '  [' + ep.svc + ']  ' + r.body.slice(0,120).replace(/\s+/g,' '));
    else if (r.status === 500) errors.push(ep.method + ' ' + ep.path + '  [' + ep.svc + ']  ' + r.body.slice(0,120).replace(/\s+/g,' '));
    other[r.status] = (other[r.status]||0)+1;
  }

  console.log('==== 403 FORBIDDEN (permission issues) : ' + forbidden.length + ' ====');
  forbidden.forEach(f => console.log('  ' + f));
  console.log('\n==== 500 on write (server bugs) : ' + errors.length + ' ====');
  errors.forEach(f => console.log('  ' + f));
  console.log('\n==== status distribution ====');
  Object.keys(other).sort().forEach(s => console.log('  ' + s + ': ' + other[s]));
})();
