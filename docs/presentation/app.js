/* HowLens presentation enhancements. All demo media are local; no AI requests. */
(() => {
  'use strict';
  const ready = (fn) => document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', fn) : fn();
  ready(() => {
    const preview = document.documentElement.hasAttribute('data-preview');
    const slides = [...document.querySelectorAll('.deck > .slide')];
    const controls = document.querySelector('.presentation-controls');
    function fit() {
      const available = window.innerHeight - (preview ? 0 : controls.getBoundingClientRect().height);
      document.documentElement.style.setProperty('--deck-scale', Math.min(window.innerWidth / 1600, available / 900));
    }
    fit(); window.addEventListener('resize', fit);
    const demo = window.UR5E_DEMO;
    demo.steps.forEach(scene => { const image = new Image(); image.src = scene.image_url; });
    let sceneIndex = 0;
    let playInterval = null;
    const sceneImage = document.getElementById('scene-image');
    const dialog = document.getElementById('image-dialog');
    const playButton = document.getElementById('demo-play');
    function showScene(index) {
      sceneIndex = Math.max(0, Math.min(8, index));
      const scene = demo.steps[sceneIndex];
      sceneImage.src = scene.image_url; sceneImage.alt = scene.title;
      document.getElementById('scene-title').textContent = scene.title;
      document.getElementById('scene-number').textContent = String(sceneIndex + 1).padStart(2, '0');
      document.getElementById('scene-description').textContent = scene.description;
      document.getElementById('scene-kicker').textContent = {photo:'사진에서 확인', manual:'공식 매뉴얼 · PDF p.12', general:'일반 지식 · 담당자 확인 범위'}[scene.evidence_basis];
      document.querySelectorAll('.deck [data-scene]').forEach(el => el.setAttribute('aria-pressed', String(Number(el.dataset.scene) === sceneIndex)));
      document.querySelectorAll('.deck [data-group]').forEach(el => el.setAttribute('aria-selected', String(Number(el.dataset.group) === Math.floor(sceneIndex / 3))));
    }
    function stopDemo() {
      clearInterval(playInterval); playInterval = null;
      playButton.innerHTML = '자동 시연 <span>▶</span>';
      playButton.setAttribute('aria-pressed', 'false');
    }
    document.querySelectorAll('.deck [data-scene]').forEach(button => button.addEventListener('click', () => { stopDemo(); showScene(Number(button.dataset.scene)); }));
    document.querySelectorAll('.deck [data-group]').forEach(button => button.addEventListener('click', () => { stopDemo(); showScene(Number(button.dataset.group) * 3); }));
    playButton.addEventListener('click', () => {
      if (playInterval) return stopDemo();
      if (sceneIndex === 8) showScene(0);
      playButton.innerHTML = '시연 일시 정지 <span>Ⅱ</span>'; playButton.setAttribute('aria-pressed', 'true');
      playInterval = setInterval(() => {
        if (sceneIndex === 8) return stopDemo();
        showScene(sceneIndex + 1);
      }, 3600);
    });
    document.getElementById('scene-zoom').addEventListener('click', () => {
      stopDemo();
      document.getElementById('dialog-image').src = sceneImage.src;
      document.getElementById('dialog-image').alt = sceneImage.alt;
      document.getElementById('dialog-caption').textContent = demo.steps[sceneIndex].description;
      dialog.showModal();
    });
    document.getElementById('close-dialog').addEventListener('click', () => dialog.close());
    dialog.addEventListener('click', e => { if (e.target === dialog) { const r = dialog.getBoundingClientRect(); if(e.clientX < r.left || e.clientX > r.right || e.clientY < r.top || e.clientY > r.bottom) dialog.close(); } });
    if (preview) return;
    const deck = window.HowLensDeck;
    let accumulated = 0, start = null;
    const formatTime = milliseconds => {
      const seconds = Math.ceil(Math.abs(milliseconds) / 1000);
      return (milliseconds < 0 ? '+' : '') + String(Math.floor(seconds / 60)).padStart(2,'0') + ':' + String(seconds % 60).padStart(2,'0');
    };
    const state = () => {
      const remaining = 180000 - accumulated - (start === null ? 0 : Date.now() - start);
      return { remaining, running: start !== null, display: formatTime(remaining) };
    };
    function renderTimer() {
      const s = state();
      document.getElementById('countdown').textContent = s.display;
      document.getElementById('timer-icon').textContent = s.running ? 'Ⅱ' : '▶';
      document.getElementById('timer-toggle').classList.toggle('running', s.running);
      document.getElementById('timer-toggle').classList.toggle('over-time', s.remaining < 0);
      document.getElementById('timer-toggle').setAttribute('aria-label', (s.running ? '타이머 일시 정지 ' : '타이머 시작 ') + s.display);
    }
    function toggleTimer() {
      if (start === null) start = Date.now(); else { accumulated += Date.now() - start; start = null; }
      renderTimer();
    }
    function resetTimer() { accumulated = 0; start = null; renderTimer(); }
    window.HowLensTimer = { state, toggle: toggleTimer, reset: resetTimer };
    setInterval(renderTimer, 200); renderTimer();
    document.getElementById('previous').onclick = () => deck.previous();
    document.getElementById('next').onclick = () => deck.next();
    document.getElementById('overview').onclick = () => deck.overview();
    document.getElementById('presenter').onclick = () => deck.presenter();
    document.getElementById('fullscreen').onclick = () => deck.fullscreen();
    document.getElementById('timer-toggle').onclick = toggleTimer;
    document.getElementById('timer-reset').onclick = resetTimer;
    document.addEventListener('keydown', e => {
      if (e.metaKey || e.ctrlKey || e.altKey || dialog.open || e.target.closest('button,a,input,textarea')) return;
      if (e.key.toLowerCase() === 'p') { toggleTimer(); e.preventDefault(); }
      if (e.key.toLowerCase() === 'r') { resetTimer(); e.preventDefault(); }
    });
    function refreshControls() {
      const i = deck.index;
      document.getElementById('control-count').textContent = String(i+1).padStart(2,'0') + ' / 06';
      document.getElementById('control-title').textContent = slides[i].dataset.title;
      document.getElementById('previous').disabled = i === 0;
      document.getElementById('next').disabled = i === slides.length - 1;
      if (i !== 2) stopDemo();
    }
    document.addEventListener('deck:change', refreshControls); refreshControls();
    let touchStart = null;
    document.querySelector('.deck').addEventListener('touchstart', e => { if(!e.target.closest('button,a') && e.touches.length===1) touchStart = [e.touches[0].clientX,e.touches[0].clientY]; }, {passive:true});
    document.querySelector('.deck').addEventListener('touchend', e => {
      if(!touchStart || dialog.open) return;
      const dx=e.changedTouches[0].clientX-touchStart[0],dy=e.changedTouches[0].clientY-touchStart[1];touchStart=null;
      if(Math.abs(dx)>65 && Math.abs(dx)>Math.abs(dy)*1.4) dx<0?deck.next():deck.previous();
    }, {passive:true});
  });
})();
