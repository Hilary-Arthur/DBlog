/* ═══ 星码记 搜索框交互 ═══ */
(function () {
  const box = document.getElementById('searchBox');
  const toggle = document.getElementById('searchToggle');
  const input = document.getElementById('searchInput');
  if (!box || !toggle || !input) return;

  let locked = false;

  /* 点击图标：切换锁定 */
  toggle.addEventListener('click', function (e) {
    e.stopPropagation();
    locked = !locked;
    box.classList.toggle('open', locked);
    if (locked) input.focus();
  });

  /* 点击输入框/表单区域：阻止冒泡，保持展开 */
  box.addEventListener('click', function (e) {
    e.stopPropagation();
  });

  /* 输入框获得焦点时确保展开 */
  input.addEventListener('focus', function () {
    locked = true;
    box.classList.add('open');
  });

  /* 回车搜索 */
  input.addEventListener('keydown', function (e) {
    if (e.key === 'Enter') {
      e.preventDefault();
      var q = input.value.trim();
      if (q) window.location.href = '/posts.html?q=' + encodeURIComponent(q);
    }
  });

  /* 点击页面其他区域：关闭 */
  document.addEventListener('click', function () {
    if (document.activeElement === input) return;
    locked = false;
    box.classList.remove('open');
  });

  /* 输入框失焦且未锁定时关闭 */
  input.addEventListener('blur', function () {
    setTimeout(function () {
      if (!locked) box.classList.remove('open');
    }, 150);
  });

  /* posts.html 有 ?q= 参数时回填 */
  var params = new URLSearchParams(window.location.search);
  if (params.has('q')) {
    input.value = params.get('q');
    box.classList.add('open');
    locked = true;
  }
})();
