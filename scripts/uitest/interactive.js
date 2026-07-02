// Interactive test of the newly-built/enhanced pages: visits each, clicks every tab/segmented
// control, and records any failed /api calls or console errors triggered by the interaction.
const puppeteer = require('puppeteer-core');
const CHROME = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const BASE = 'http://localhost:5173';

// routes that gained interactive tabs/dialogs this session (+ the new /surveys route)
const routes = [
  '/surveys', '/polls', '/kudos', '/reviews', '/one-on-ones', '/helpdesk',
  '/advances', '/assets', '/asset-requests', '/tax/declaration', '/tax/proof-verification',
  '/interviews', '/goal-cascade', '/offboarding', '/holidays', '/settings', '/letters',
  '/users', '/compliance-tasks', '/goals', '/workflows', '/wellness',
  '/rewards-catalog', '/skills', '/competencies', '/jobs',
  '/awards', '/offers', '/events',
];

(async () => {
  const browser = await puppeteer.launch({ executablePath: CHROME, headless: 'new', args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900 });

  await page.goto(BASE + '/login', { waitUntil: 'networkidle2', timeout: 30000 });
  await page.waitForSelector('input[type=email]', { timeout: 15000 });
  await page.type('input[type=email]', 'admin@demo.com');
  await page.type('input[type=password]', 'Admin@123');
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 30000 }).catch(() => {}),
    page.click('button[type=submit]'),
  ]);
  await new Promise(r => setTimeout(r, 2000));
  if (page.url().includes('/login')) { console.log('LOGIN FAILED'); await browser.close(); return; }
  console.log('LOGIN OK\n');

  const report = {};
  for (const route of routes) {
    const fails = [];
    const errs = [];
    const onResp = async (resp) => {
      const u = resp.url(); const s = resp.status();
      if (u.includes('/api/') && s >= 400) {
        let body = ''; try { body = (await resp.text()).slice(0, 120).replace(/\s+/g, ' '); } catch {}
        fails.push(s + ' ' + resp.request().method() + ' ' + u.replace('http://localhost:8080', '').split('?')[0] + '  ' + body);
      }
    };
    const onConsole = (m) => { if (m.type() === 'error') { const t = m.text(); if (!t.includes('favicon') && !t.includes('WebSocket')) errs.push(t.slice(0, 140)); } };
    page.on('response', onResp);
    page.on('console', onConsole);
    try {
      await page.goto(BASE + route, { waitUntil: 'networkidle2', timeout: 25000 });
      await new Promise(r => setTimeout(r, 800));
      // click every tab trigger and let each fetch
      const tabs = await page.$$('[role="tab"], button[data-state]');
      for (const t of tabs) {
        try { await t.click(); await new Promise(r => setTimeout(r, 700)); } catch {}
      }
    } catch (e) { errs.push('NAV: ' + e.message.slice(0, 80)); }
    await new Promise(r => setTimeout(r, 500));
    page.off('response', onResp);
    page.off('console', onConsole);
    const uf = [...new Set(fails)], ue = [...new Set(errs)];
    if (uf.length || ue.length) report[route] = { api: uf, console: ue };
  }

  const keys = Object.keys(report);
  if (!keys.length) console.log('CLEAN: no API failures or console errors on any interactive page.');
  for (const k of keys) {
    console.log('### ' + k);
    for (const f of report[k].api) console.log('   API  ' + f);
    for (const c of report[k].console) console.log('   ERR  ' + c);
  }
  console.log('\n==== ' + keys.length + '/' + routes.length + ' interactive pages have issues ====');
  await browser.close();
})().catch(e => { console.error('FATAL', e.message); process.exit(1); });
