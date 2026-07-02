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

  // ---- Login ----
  await page.goto(BASE + '/login', { waitUntil: 'networkidle2', timeout: 30000 });
  await page.waitForSelector('input[type=email]', { timeout: 15000 });
  await page.type('input[type=email]', 'admin@demo.com');
  await page.type('input[type=password]', 'Admin@123');
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 30000 }).catch(()=>{}),
    page.click('button[type=submit]'),
  ]);
  await new Promise(r => setTimeout(r, 2000));
  const url = page.url();
  if (url.includes('/login')) { console.log('LOGIN FAILED — still on /login'); await browser.close(); return; }
  console.log('LOGIN OK -> ' + url + '\n');

  const report = {};
  for (const route of routes) {
    const failures = [];
    const consoleErrs = [];
    const onResp = async (resp) => {
      try {
        const u = resp.url(); const s = resp.status();
        if (u.includes('/api/') && s >= 400) failures.push(s + ' ' + u.replace(BASE,'').replace('http://localhost:8080',''));
      } catch {}
    };
    const onConsole = (msg) => { if (msg.type() === 'error') { const t = msg.text(); if (!t.includes('favicon')) consoleErrs.push(t.slice(0,160)); } };
    page.on('response', onResp);
    page.on('console', onConsole);
    try {
      await page.goto(BASE + route, { waitUntil: 'networkidle2', timeout: 25000 });
    } catch (e) { /* timeout still captures requests */ }
    await new Promise(r => setTimeout(r, 1500));
    page.off('response', onResp);
    page.off('console', onConsole);
    // dedupe
    const uniqFail = [...new Set(failures)];
    const uniqErr = [...new Set(consoleErrs)];
    if (uniqFail.length || uniqErr.length) report[route] = { api: uniqFail, console: uniqErr };
  }

  console.log('==== PAGES WITH ISSUES ====\n');
  const keys = Object.keys(report);
  if (!keys.length) { console.log('No API failures or console errors on any page.'); }
  for (const k of keys) {
    console.log('### ' + k);
    for (const f of report[k].api) console.log('   API  ' + f);
    for (const c of report[k].console) console.log('   ERR  ' + c);
  }
  console.log('\n==== SUMMARY: ' + keys.length + ' / ' + routes.length + ' pages have issues ====');
  await browser.close();
})().catch(e => { console.error('FATAL', e.message); process.exit(1); });
