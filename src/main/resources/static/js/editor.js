/* ═══ DBlog 写博客 — Vue 3 应用 ═══ */
const { createApp, ref, computed, onMounted } = Vue;

const app = createApp({
  setup() {
    const loggedIn = ref(false);
    const account = ref('');
    const avatarLetter = computed(() => (account.value || '?').charAt(0).toUpperCase());
    const checking = ref(true);

    /* ── 模态 ── */
    const showLoginRequired = ref(false);
    const showLoginModal = ref(false);

    /* ── 登录表单 ── */
    const loginAccount = ref('');
    const loginPassword = ref('');
    const loginError = ref('');

    /* ── 编辑器 ── */
    const title = ref('');
    const content = ref('');
    const publishing = ref(false);
    const titleCount = computed(() => title.value.length);
    const contentCount = computed(() => content.value.length);

    /* ── Toast ── */
    const toastMsg = ref('');
    const toastError = ref(false);
    const toastVisible = ref(false);
    let toastTimer = null;

    function showToast(msg, isError) {
      toastMsg.value = msg;
      toastError.value = !!isError;
      toastVisible.value = true;
      clearTimeout(toastTimer);
      toastTimer = setTimeout(() => { toastVisible.value = false; }, 2000);
    }

    /* ── 状态 ── */
    function setLoggedIn(acc) {
      loggedIn.value = true;
      account.value = acc;
      showLoginModal.value = false;
      showLoginRequired.value = false;
    }

    /* ── 登录 ── */
    function openLogin() {
      showLoginRequired.value = false;
      showLoginModal.value = true;
    }
    function backToLoginRequired() {
      showLoginModal.value = false;
      showLoginRequired.value = true;
    }

    async function doLogin() {
      loginError.value = '';
      if (!loginAccount.value.trim() || !loginPassword.value) {
        loginError.value = '请填写账号和密码'; return;
      }
      try {
        const r = await fetch('/api/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ account: loginAccount.value.trim(), password: loginPassword.value })
        });
        const data = await r.json();
        if (data.ok) {
          setLoggedIn(data.account);
          loginAccount.value = '';
          loginPassword.value = '';
        } else { loginError.value = data.msg; }
      } catch (_) { loginError.value = '网络错误'; }
    }

    /* ── 退出 ── */
    async function doLogout() {
      await fetch('/api/logout', { method: 'POST' });
      window.location.href = '/';
    }

    /* ── 发布 ── */
    async function publish() {
      if (!title.value.trim()) { showToast('请输入标题', true); return; }
      if (!content.value.trim()) { showToast('请输入内容', true); return; }
      publishing.value = true;
      try {
        const r = await fetch('/api/posts', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ title: title.value.trim(), content: content.value.trim() })
        });
        const data = await r.json();
        if (data.ok) {
          showToast(data.msg || '已提交审核，通过后可见');
          setTimeout(() => { window.location.href = '/'; }, 1000);
        } else {
          showToast(data.msg, true);
          publishing.value = false;
        }
      } catch (_) {
        showToast('网络错误', true);
        publishing.value = false;
      }
    }

    /* ── 初始化 ── */
    onMounted(async () => {
      try {
        const r = await fetch('/api/user/me');
        const data = await r.json();
        if (data.loggedIn) {
          setLoggedIn(data.account);
        } else {
          showLoginRequired.value = true;
        }
      } catch (_) { /* ignore */ }
      checking.value = false;
    });

    return {
      loggedIn, account, avatarLetter, checking,
      showLoginRequired, showLoginModal,
      loginAccount, loginPassword, loginError,
      title, content, publishing, titleCount, contentCount,
      openLogin, backToLoginRequired, doLogin, doLogout, publish, showToast,
      toastMsg, toastError, toastVisible
    };
  }
});

app.mount('#app');
