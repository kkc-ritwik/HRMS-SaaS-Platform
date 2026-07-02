const puppeteer = require('puppeteer-core');
const CHROME = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const BASE = 'http://localhost:5173';

(async () => {
  const browser = await puppeteer.launch({ executablePath: CHROME, headless: 'new', args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.goto(BASE + '/login', { waitUntil: 'networkidle2' });
  await page.waitForSelector('input[type=email]');
  await page.type('input[type=email]', 'admin@demo.com');
  await page.type('input[type=password]', 'Admin@123');
  await page.click('button[type=submit]');
  await new Promise(r => setTimeout(r, 4000));
  console.log('URL after login: ' + page.url());
  const ls = await page.evaluate(() => ({
    accessToken: localStorage.getItem('accessToken'),
    keys: Object.keys(localStorage),
    authStorage: localStorage.getItem('auth-storage'),
  }));
  console.log('accessToken present: ' + !!ls.accessToken + ' (len ' + (ls.accessToken? ls.accessToken.length : 0) + ')');
  console.log('localStorage keys: ' + ls.keys.join(','));
  console.log('auth-storage: ' + (ls.authStorage ? ls.authStorage.slice(0,200) : 'null'));

  // Capture the Authorization header on the next API request
  let authHeaderSeen = 'NONE';
  page.on('request', req => {
    if (req.url().includes('/api/v1/employees') && !authHeaderSeen.startsWith('Bearer')) {
      const h = req.headers();
      authHeaderSeen = h['authorization'] || h['Authorization'] || ('MISSING (headers: ' + Object.keys(h).join(',') + ')');
    }
  });
  await page.goto(BASE + '/employees', { waitUntil: 'networkidle2' }).catch(()=>{});
  await new Promise(r => setTimeout(r, 2500));
  console.log('Authorization header on /employees request: ' + (authHeaderSeen.length > 60 ? authHeaderSeen.slice(0,60)+'...' : authHeaderSeen));
  await browser.close();
})().catch(e => { console.error('FATAL', e.message); process.exit(1); });
