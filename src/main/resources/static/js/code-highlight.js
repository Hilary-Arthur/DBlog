/* ═══ 星码记 代码高亮 + 复制按钮 ═══ */
(function () {
  function init() {
    document.querySelectorAll('pre').forEach(function (pre) {
      if (pre.dataset.hasCopyBtn) return;
      var block = pre.querySelector('code');
      if (!block) return;

      pre.dataset.hasCopyBtn = '1';

      /* Prism 高亮 */
      if (typeof Prism !== 'undefined' && !block.classList.contains('prism-highlighted')) {
        Prism.highlightElement(block);
        block.classList.add('prism-highlighted');
      }

      /* 创建复制按钮 */
      var btn = document.createElement('button');
      btn.className = 'code-copy-btn';
      btn.innerHTML = '<i class="bi bi-clipboard"></i>';
      btn.title = '复制代码';

      btn.addEventListener('click', function () {
        var text = block.textContent;
        navigator.clipboard.writeText(text).then(function () {
          btn.innerHTML = '<i class="bi bi-check2"></i>';
          btn.classList.add('copied');
          setTimeout(function () {
            btn.innerHTML = '<i class="bi bi-clipboard"></i>';
            btn.classList.remove('copied');
          }, 1500);
        });
      });

      pre.style.position = 'relative';
      pre.appendChild(btn);
    });
  }

  /* Vue 渲染后执行（v-html 更新后） */
  if (typeof Vue !== 'undefined') {
    var observer = new MutationObserver(function () { init(); });
    var appEl = document.getElementById('app');
    if (appEl) observer.observe(appEl, { childList: true, subtree: true });
  }

  /* 首次加载 */
  document.addEventListener('DOMContentLoaded', function () {
    setTimeout(init, 300);
  });
})();
