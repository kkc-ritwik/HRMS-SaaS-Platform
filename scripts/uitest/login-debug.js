const puppeteer = require('puppeteer-core');
const CHROME = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const BASE = 'http://localhost:5173';

(async () => {
  const browser = await puppeteer.launch({ executablePath: CHROME, headless: 'new', args: ['--no-sandbox'] });
  const page = await browser.newPage();
  page.on('console', m => console.log('CONSOLE['+m.type()+'] ' + m.text().slice(0,200)));
  page.on('pageerror', e => console.log('PAGEERROR ' + e.message.slice(0,200)));
  page.on('response', async r => {
    if (r.url().includes('/api/v1/auth/login')) {
      let body = ''; try { body = (await r.text()).slice(0,300); } catch {}
      console.log('LOGIN RESP ' + r.status() + ' | ' + body);
    }
  });
  await page.goto(BASE + '/login', { waitUntil: 'networkidle2', timeout: 30000 });
  await page.waitForSelector('input[type=email]', { timeout: 15000 });
  await page.type('input[type=email]', 'admin@demo.com', { delay: 20 });
  await page.type('input[type=password]', 'Admin@123', { delay: 20 });
  console.log('Filled form. Clicking submit...');
  await page.click('button[type=submit]');
  await new Promise(r => setTimeout(r, 5000));
  console.log('URL after login: ' + page.url());
  console.log('localStorage.accessToken present: ' + await page.evaluate(() => !!localStorage.getItem('accessToken')));
  console.log('localStorage keys: ' + await page.evaluate(() => Object.keys(localStorage).join(',')));
  await browser.close();
})().catch(e => { console.error('FATAL', e.message); process.exit(1); });
