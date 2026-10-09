// npm install --no-save playwright, then: node docs/presentation/export-pdf.cjs
// Optional HOWLENS_CHROMIUM_PATH selects an already installed Chromium executable.
const { chromium } = require('playwright');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
(async () => {
  const browser = await chromium.launch({ headless: true, ...(process.env.HOWLENS_CHROMIUM_PATH ? { executablePath: process.env.HOWLENS_CHROMIUM_PATH } : {}) });
  try {
    const page = await browser.newPage({ viewport: { width: 1600, height: 900 }, reducedMotion: 'reduce' });
    await page.goto(pathToFileURL(path.join(__dirname, 'index.html')).href);
    await page.emulateMedia({ media: 'print' });
    await page.evaluate(() => {
      document.getElementById('scene-image').src = 'assets/ur5e-grid.png';
      document.getElementById('scene-image').alt = 'UR5e 실제 생성 결과 9장면';
      document.getElementById('scene-kicker').textContent = '실제 대화에서 생성된 결과';
      document.getElementById('scene-number').textContent = '01 → 09';
      document.getElementById('scene-title').textContent = '9개의 장면, 3개의 이해';
      document.getElementById('scene-description').innerHTML = '현재 장착 상태 이해<br>매뉴얼의 부품 위치 이해<br>설치 담당자의 확인 범위';
      document.querySelectorAll('.slide').forEach(slide => { slide.inert = false; slide.removeAttribute('aria-hidden'); });
    });
    await page.addStyleTag({ content: '@media print { html,body{-webkit-print-color-adjust:exact;print-color-adjust:exact} .demo .scene-view{grid-template-columns:380px 1fr} .demo .scene-zoom{width:380px;height:380px} .demo .scene-pager,.demo #demo-play,.demo .zoom-hint{display:none} .demo .scene-copy{padding-top:30px} .demo .scene-copy p{font-size:25px;line-height:1.9;margin-top:25px} .demo .scene-copy h3{font-size:32px} .slide:last-child{break-after:auto;page-break-after:auto} }' });
    await page.evaluate(async () => { await document.fonts.ready; await Promise.all([...document.querySelectorAll('img[src]')].map(img => img.decode().catch(() => {}))); });
    const output = path.join(__dirname, 'HowLens-3min.pdf');
    await page.pdf({ path: output, preferCSSPageSize: true, printBackground: true, displayHeaderFooter: false, margin: { top: 0, right: 0, bottom: 0, left: 0 }, tagged: true });
    console.log(output);
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
