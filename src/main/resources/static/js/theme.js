/* ═══ 星码记 主题切换 ═══ */
(function () {
  var STORAGE_KEY = 'dblog-theme';

  function getPreferred() {
    var saved = localStorage.getItem(STORAGE_KEY);
    if (saved === 'dark' || saved === 'light') return saved;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  function apply(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    updateIcons(theme);
  }

  function updateIcons(theme) {
    var sun = document.getElementById('themeSun');
    var moon = document.getElementById('themeMoon');
    if (!sun || !moon) return;
    if (theme === 'dark') {
      sun.style.display = 'none';
      moon.style.display = 'inline';
    } else {
      sun.style.display = 'inline';
      moon.style.display = 'none';
    }
  }

  /* 尽早应用主题，防止闪烁 */
  apply(getPreferred());

  /* ── 暗色模式点击波纹 ── */
  document.addEventListener('click', function (e) {
    if (document.documentElement.getAttribute('data-theme') !== 'dark') return;
    if (e.target.closest('a, button, input, .search-box, .page-link, .month-list li, .theme-toggle')) return;

    var ripple = document.createElement('div');
    ripple.className = 'dblog-click-ripple';
    ripple.style.left = e.clientX + 'px';
    ripple.style.top = e.clientY + 'px';
    document.body.appendChild(ripple);
    ripple.addEventListener('animationend', function () { ripple.remove(); });
  });

  document.addEventListener('DOMContentLoaded', function () {
    var btn = document.getElementById('themeToggle');
    if (!btn) return;

    updateIcons(getPreferred());

    btn.addEventListener('click', function () {
      var current = document.documentElement.getAttribute('data-theme') || 'light';
      var next = current === 'dark' ? 'light' : 'dark';
      localStorage.setItem(STORAGE_KEY, next);
      apply(next);
    });
  });
})();
