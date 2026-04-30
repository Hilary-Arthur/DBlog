/* ═══ DBlog 首页 — Vue 3 应用 ═══ */
const { createApp, ref, computed, onMounted, nextTick } = Vue;

const app = createApp({
  setup() {
    /* ── 用户状态 ── */
    const loggedIn = ref(false);
    const account = ref('');
    const avatarLetter = computed(() => (account.value || '?').charAt(0).toUpperCase());

    /* ── 文章 & 分页 ── */
    const posts = ref([]);
    const currentPage = ref(1);
    const totalPages = ref(0);
    const totalCount = ref(0);
    const loading = ref(true);

    /* ── 归档 ── */
    const archives = ref([]);

    /* ── 模态 ── */
    const showLoginModal = ref(false);
    const showRegisterModal = ref(false);
    const showAdminModal = ref(false);

    /* ── 表单 ── */
    const loginAccount = ref('');
    const loginPassword = ref('');
    const loginError = ref('');

    const regAccount = ref('');
    const regPassword = ref('');
    const regCaptcha = ref('');
    const registerError = ref('');

    const adminAccount = ref('');
    const adminPassword = ref('');
    const adminError = ref('');

    const captchaSrc = ref('/api/captcha');

    /* ── Toast ── */
    const toastMsg = ref('');
    const toastError = ref(false);
    const toastVisible = ref(false);

    /* ── 辅助 ── */
    const esc = (s) => (s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

    /* ── Toast ── */
    function showToast(msg, isError) {
      toastMsg.value = msg;
      toastError.value = !!isError;
      toastVisible.value = true;
      setTimeout(() => { toastVisible.value = false; }, 2000);
    }

    /* ── 模态逻辑 ── */
    function openLogin() { showLoginModal.value = true; showRegisterModal.value = false; }
    function openRegister() { showRegisterModal.value = true; showLoginModal.value = false; refreshCaptcha(); }
    function closeModals() { showLoginModal.value = false; showRegisterModal.value = false; showAdminModal.value = false; }
    function switchToRegister() { showLoginModal.value = false; openRegister(); }
    function switchToLogin() { showRegisterModal.value = false; openLogin(); }

    /* ── 验证码 ── */
    function refreshCaptcha() { captchaSrc.value = '/api/captcha?t=' + Date.now(); }

    /* ── 登录 ── */
    async function doLogin() {
      loginError.value = '';
      if (!loginAccount.value.trim() || !loginPassword.value) { loginError.value = '请填写账号和密码'; return; }
      try {
        const r = await fetch('/api/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ account: loginAccount.value.trim(), password: loginPassword.value })
        });
        const data = await r.json();
        if (data.ok) {
          closeModals();
          setLoggedIn(data.account);
          loginAccount.value = '';
          loginPassword.value = '';
        } else { loginError.value = data.msg; }
      } catch (_) { loginError.value = '网络错误'; }
    }

    /* ── 注册 ── */
    async function doRegister() {
      registerError.value = '';
      if (!regAccount.value.trim() || !regPassword.value || !regCaptcha.value.trim()) {
        registerError.value = '请填写所有字段'; return;
      }
      try {
        const r = await fetch('/api/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ account: regAccount.value.trim(), password: regPassword.value, captcha: regCaptcha.value.trim() })
        });
        const data = await r.json();
        if (data.ok) {
          closeModals();
          setLoggedIn(data.account);
          regAccount.value = '';
          regPassword.value = '';
          regCaptcha.value = '';
        } else { registerError.value = data.msg; refreshCaptcha(); }
      } catch (_) { registerError.value = '网络错误'; }
    }

    /* ── 退出 ── */
    async function doLogout() {
      await fetch('/api/logout', { method: 'POST' });
      setLoggedOut();
    }

    /* ── 管理员登录 ── */
    async function doAdminLogin() {
      adminError.value = '';
      if (!adminAccount.value.trim() || !adminPassword.value) { adminError.value = '请填写账号和密码'; return; }
      try {
        const r = await fetch('/api/admin/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ account: adminAccount.value.trim(), password: adminPassword.value })
        });
        const data = await r.json();
        if (data.ok) { window.location.href = '/admin.html'; }
        else { adminError.value = data.msg; }
      } catch (_) { adminError.value = '网络错误'; }
    }

    /* ── 状态切换 ── */
    function setLoggedIn(acc) {
      loggedIn.value = true;
      account.value = acc;
    }
    function setLoggedOut() {
      loggedIn.value = false;
      account.value = '';
    }

    /* ── 加载文章 ── */
    async function loadPosts(page) {
      currentPage.value = page;
      loading.value = true;
      try {
        const r = await fetch('/api/posts?page=' + page);
        const data = await r.json();
        if (!data.ok || !data.posts || data.posts.length === 0) {
          posts.value = [];
          totalPages.value = 0;
        } else {
          posts.value = data.posts;
          totalPages.value = data.totalPages || 0;
          totalCount.value = data.total || 0;
        }
      } catch (_) { posts.value = []; }
      loading.value = false;
    }

    /* ── 点赞 ── */
    const likingPids = ref(new Set());

    async function likePost(pid, idx) {
      if (likingPids.value.has(pid)) return; // 防连点
      likingPids.value.add(pid);
      try {
        const r = await fetch('/api/posts/' + pid + '/like', { method: 'POST' });
        const d = await r.json();
        if (d.ok) {
          posts.value[idx].liked = d.liked;
          posts.value[idx].likeCount = d.likeCount;
        } else if (d.msg === '请先登录') { openLogin(); }
      } catch (_) { /* ignore */ }
      likingPids.value.delete(pid);
    }

    /* ── 分页范围 ── */
    const pageNumbers = computed(() => {
      const arr = [];
      for (let i = 1; i <= totalPages.value; i++) arr.push(i);
      return arr;
    });
    const showPagination = computed(() => totalCount.value > 6);

    /* ── 归档 ── */
    async function loadArchives() {
      try {
        const r = await fetch('/api/posts/stats');
        const data = await r.json();
        if (data.ok && data.months && data.months.length > 0) {
          archives.value = data.months;
        }
      } catch (_) { /* ignore */ }
    }

    /* ── 管理入口 ── */
    function openAdmin() { showAdminModal.value = true; }

    /* ── 初始化 ── */
    onMounted(async () => {
      // 检查登录状态
      try {
        const r = await fetch('/api/user/me');
        const data = await r.json();
        if (data.loggedIn) setLoggedIn(data.account);
      } catch (_) { /* ignore */ }
      // 加载数据
      loadPosts(1);
      loadArchives();
    });

    return {
      loggedIn, account, avatarLetter,
      posts, currentPage, totalPages, pageNumbers, showPagination, loading,
      archives,
      showLoginModal, showRegisterModal, showAdminModal,
      loginAccount, loginPassword, loginError,
      regAccount, regPassword, regCaptcha, registerError,
      adminAccount, adminPassword, adminError,
      captchaSrc,
      toastMsg, toastError, toastVisible,
      esc,
      openLogin, openRegister, closeModals, switchToRegister, switchToLogin,
      refreshCaptcha, doLogin, doRegister, doLogout, doAdminLogin,
      setLoggedIn, setLoggedOut, loadPosts, likePost, loadArchives, openAdmin,
      showToast
    };
  }
});

app.component('post-card', PostCard);
app.mount('#app');
