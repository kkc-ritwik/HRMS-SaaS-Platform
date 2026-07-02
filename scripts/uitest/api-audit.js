// Backend read-surface audit: enumerate every GET endpoint from controllers, hit each through the
// gateway with an admin token, and report non-2xx with the error body. This surfaces permission (403),
// server (500), validation (400) and routing (404) issues across the WHOLE API — not just initial-load.
const fs = require('fs');
const path = require('path');
const http = require('http');

const ROOT = 'C:\\Users\\ritwi\\Desktop\\HRMS-SaaS-Platform';
const GATEWAY = 'http://localhost:8080';
const EMP = 'c0000000-0000-4000-a000-000000000001'; // admin employeeId (== user id)
const UUID = '00000000-0000-4000-a000-000000000000';
const YEAR = '2026';

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

// Build GET endpoint list
const controllers = walk(ROOT).filter(f => f.includes('service-'));
const endpoints = [];
for (const file of controllers) {
  const src = fs.readFileSync(file, 'utf8');
  const svc = (file.match(/service-[^\\]+/) || ['?'])[0];
  const clsMatch = src.match(/@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]+)"/);
  const base = clsMatch ? clsMatch[1] : '';
  const re = /@GetMapping(?:\(\s*(?:value\s*=\s*)?"([^"]*)")?/g;
  let m;
  while ((m = re.exec(src)) !== null) {
    const sub = m[1] || '';
    let full = base + (sub.startsWith('/') || sub === '' ? sub : '/' + sub);
    if (!full.startsWith('/')) full = '/' + full;
    endpoints.push({ svc, path: full, file: path.basename(file) });
  }
}

// Substitute path variables with plausible test values
function fill(p) {
  return p.replace(/\{([^}]+)\}/g, (_, name) => {
    const n = name.toLowerCase();
    if (n.includes('year')) return YEAR;
    if (n.includes('employee')) return EMP;
    if (n.includes('code') || n.includes('slug') || n.includes('key') || n.includes('type') || n.includes('status') || n.includes('stage') || n.includes('role')) return 'TEST';
    return UUID;
  });
}

function login() {
  return req('POST', '/api/v1/auth/login', { email: 'admin@demo.com', password: 'Admin@123', tenantId: 'demo' }, null)
    .then(r => { try { return JSON.parse(r.body).data.accessToken; } catch { throw new Error('login failed: ' + r.body); } });
}

function req(method, p, body, token) {
  return new Promise((resolve) => {
    const data = body ? JSON.stringify(body) : null;
    const u = new URL(GATEWAY + p);
    const opts = { method, hostname: u.hostname, port: u.port, path: u.pathname + u.search,
      headers: { 'Content-Type': 'application/json' } };
    if (token) opts.headers.Authorization = 'Bearer ' + token;
    if (data) opts.headers['Content-Length'] = Buffer.byteLength(data);
    const r = http.request(opts, (res) => {
      let b = ''; res.on('data', c => b += c); res.on('end', () => resolve({ status: res.statusCode, body: b }));
    });
    r.on('error', () => resolve({ status: 0, body: 'CONNECTION_ERROR' }));
    r.setTimeout(15000, () => { r.destroy(); resolve({ status: 0, body: 'TIMEOUT' }); });
    if (data) r.write(data); r.end();
  });
}

(async () => {
  const token = await login();
  console.log('LOGIN OK. Testing ' + endpoints.length + ' GET endpoints...\n');
  const results = [];
  for (const ep of endpoints) {
    const url = fill(ep.path);
    const r = await req('GET', url, null, token);
    results.push({ ...ep, url, status: r.status, body: r.body.slice(0, 180).replace(/\s+/g, ' ') });
  }

  // Report non-2xx, separating "real" issues from expected 404 (random-UUID not found)
  const real = results.filter(r => r.status === 403 || r.status === 500 || r.status === 400 || r.status === 0);
  const notFound = results.filter(r => r.status === 404);
  const ok = results.filter(r => r.status >= 200 && r.status < 300);

  console.log('==== REAL ISSUES (403 / 500 / 400 / conn) : ' + real.length + ' ====\n');
  for (const r of real.sort((a,b)=> a.status-b.status || a.path.localeCompare(b.path))) {
    console.log(r.status + ' ' + r.path + '   [' + r.svc + ']');
    console.log('     ' + r.body);
  }
  console.log('\n==== 404 (likely just no-such-id, review for routing) : ' + notFound.length + ' ====\n');
  for (const r of notFound.sort((a,b)=>a.path.localeCompare(b.path))) console.log('404 ' + r.path + '   [' + r.svc + ']');

  console.log('\n==== SUMMARY: ' + ok.length + ' ok, ' + real.length + ' real issues, ' + notFound.length + ' not-found, of ' + results.length + ' GET endpoints ====');
})();
