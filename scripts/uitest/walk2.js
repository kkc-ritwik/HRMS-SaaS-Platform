// Enhanced page auditor: logs in as admin, visits every route, and for each failing /api call
// captures METHOD, STATUS, PATH and a snippet of the error body so failures can be root-caused.
const puppeteer = require('puppeteer-core');

const CHROME = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const BASE = 'http://localhost:5173';

const routes = [
  '/dashboard','/profile','/my-leave','/my-team','/payslips','/documents','/timesheet',
  '/tax/declaration','/tax/proofs','/tax/proof-verification','/policies','/employees','/departments',
  '/designations','/locations','/org-chart','/cost-centers','/skills','/attendance','/regularizations',
  '/leave/apply','/leave-approvals','/leave-balances','/leave-types','/holidays','/calendar','/shifts',
  '/pay-runs','/salary-structures','/pay-grades','/benefits','/loans','/jobs','/candidates','/applications',
  '/pipeline','/interviews','/offers','/hiring-loops','/onboarding','/offboarding','/goals','/goal-cascade',
  '/nine-box','/reviews','/review-cycles','/one-on-ones','/competencies','/pip','/courses','/enrollments',
  '/certifications','/social','/kudos','/suggestions','/pulse','/awards','/rewards-catalog','/wellness',
  '/stay-interviews','/polls','/heatmap','/assets','/asset-categories','/asset-requests','/asset-maintenance',
  '/amc-contracts','/vendors','/expenses','/advances','/helpdesk','/kb','/desk-booking','/floor-plan',
  '/visitors','/travel','/approvals','/workflows','/out-of-office','/compliance','/cases','/gdpr',
  '/compliance-items','/licenses','/letters','/forms','/settings/templates','/reports','/dei','/dashboards',
  '/saved-reports','/job-cost-reports','/recruitment-analytics','/performance-analytics','/notifications',
  '/notification-preferences','/announcements','/email-templates','/settings','/users'
];

(async () => {
  const browser = await puppeteer.launch({ executablePath: CHROME, headless: 'new', args: ['--no-sandbox','--disable-dev-shm-usage'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 900 });

  await page.goto(BASE + '/login', { waitUntil: 'networkidle2', timeout: 30000 });
  await page.waitForSelector('input[type=email]', { timeout: 15000 });
  await page.type('input[type=email]', 'admin@demo.com');
  await page.type('input[type=password]', 'Admin@123');
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 30000 }).catch(()=>{}),
    page.click('button[type=submit]'),
  ]);
  await new Promise(r => setTimeout(r, 2000));
  if (page.url().includes('/login')) { console.log('LOGIN FAILED'); await browser.close(); return; }
  console.log('LOGIN OK\n');

  const report = {};
  const byPath = {}; // aggregate: "METHOD path" -> {status, body, routes:Set}

  for (const route of routes) {
    const failures = [];
    const onResp = async (resp) => {
      try {
        const u = resp.url(); const s = resp.status();
        if (!u.includes('/api/') || s < 400) return;
        const method = resp.request().method();
        const p = u.replace(BASE,'').replace('http://localhost:8080','').split('?')[0];
        let body = '';
        try { body = (await resp.text()).slice(0, 200).replace(/\s+/g,' '); } catch {}
        const key = method + ' ' + p;
        failures.push(s + ' ' + key);
        if (!byPath[key]) byPath[key] = { status: s, body, routes: new Set() };
        byPath[key].routes.add(route);
      } catch {}
    };
    page.on('response', onResp);
    try { await page.goto(BASE + route, { waitUntil: 'networkidle2', timeout: 25000 }); } catch {}
    await new Promise(r => setTimeout(r, 1200));
    page.off('response', onResp);
    const uniq = [...new Set(failures)];
    if (uniq.length) report[route] = uniq;
  }

  console.log('==== PER-PAGE FAILURES ====\n');
  for (const k of Object.keys(report)) {
    console.log('### ' + k);
    for (const f of report[k]) console.log('   ' + f);
  }

  console.log('\n==== UNIQUE FAILING ENDPOINTS (with cause) ====\n');
  const sorted = Object.keys(byPath).sort();
  for (const k of sorted) {
    const e = byPath[k];
    console.log(e.status + ' ' + k);
    console.log('     body: ' + (e.body || '(empty)'));
    console.log('     on:   ' + [...e.routes].join(', '));
  }
  console.log('\n==== SUMMARY: ' + Object.keys(report).length + '/' + routes.length + ' pages, ' + sorted.length + ' unique failing endpoints ====');
  await browser.close();
})().catch(e => { console.error('FATAL', e.message); process.exit(1); });
