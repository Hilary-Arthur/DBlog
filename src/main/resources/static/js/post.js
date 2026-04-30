/* ═══ DBlog 文章详情 — Vue 3 应用 ═══ */
const { createApp, ref, computed, onMounted } = Vue;

const app = createApp({
  setup() {
    /* ── 用户状态 ── */
    const loggedIn = ref(false);
    const account = ref('');
    const currentUid = ref(null);
    const avatarLetter = computed(() => (account.value || '?').charAt(0).toUpperCase());

    /* ── 文章 ── */
    const post = ref(null);
    const loading = ref(true);
    const notFound = ref(false);

    /* ── 评论 ── */
    const comments = ref([]);
    const commentCount = ref(0);
    const newComment = ref('');
    const submitting = ref(false);
    const commentError = ref('');

    /* ── 模态 ── */
    const showLoginModal = ref(false);
    const showRegisterModal = ref(false);

    /* ── 登录表单 ── */
    const loginAccount = ref('');
    const loginPassword = ref('');
    const loginError = ref('');

    /* ── 注册表单 ── */
    const regAccount = ref('');
    const regPassword = ref('');
    const regCaptcha = ref('');
    const registerError = ref('');
    const captchaSrc = ref('/api/captcha');

    /* ── Toast ── */
    const toastMsg = ref('');
    const toastVisible = ref(false);
    let toastTimer = null;

    function showToast(msg) {
      toastMsg.value = msg;
      toastVisible.value = true;
      clearTimeout(toastTimer);
      toastTimer = setTimeout(() => { toastVisible.value = false; }, 2000);
    }

    /* ── 状态 ── */
    function setLoggedIn(acc, uid) {
      loggedIn.value = true;
      account.value = acc;
      currentUid.value = uid;
    }
    function setLoggedOut() {
      loggedIn.value = false;
      account.value = '';
      currentUid.value = null;
    }

    /* ── 退出 ── */
    async function doLogout() {
      await fetch('/api/logout', { method: 'POST' });
      window.location.href = '/';
    }

    /* ── 点赞 ── */
    const liking = ref(false);
    async function toggleLike() {
      if (!loggedIn.value) { showToast('请先登录'); return; }
      if (liking.value) return;
      liking.value = true;
      try {
        const r = await fetch('/api/posts/' + post.value.pid + '/like', { method: 'POST' });
        const d = await r.json();
        if (d.ok) {
          post.value.liked = d.liked;
          post.value.likeCount = d.likeCount;
        }
      } catch (_) { /* ignore */ }
      liking.value = false;
    }

    /* ── 评论 ── */
    async function loadComments() {
      try {
        const r = await fetch('/api/posts/' + post.value.pid + '/comments');
        const d = await r.json();
        if (d.ok) {
          comments.value = d.comments || [];
          commentCount.value = comments.value.length;
        }
      } catch (_) { /* ignore */ }
    }

    async function submitComment() {
      const content = newComment.value.trim();
      if (!content) return;
      if (!loggedIn.value) { showToast('请先登录'); return; }
      submitting.value = true;
      commentError.value = '';
      try {
        const r = await fetch('/api/posts/' + post.value.pid + '/comments', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ content })
        });
        const d = await r.json();
        if (d.ok) {
          comments.value.push(d.comment);
          commentCount.value = comments.value.length;
          newComment.value = '';
        } else {
          commentError.value = d.msg;
        }
      } catch (_) { commentError.value = '网络错误'; }
      submitting.value = false;
    }

    async function deleteComment(cid) {
      if (!confirm('确定删除这条评论？')) return;
      try {
        const r = await fetch('/api/comments/' + cid, { method: 'DELETE' });
        const d = await r.json();
        if (d.ok) {
          comments.value = comments.value.filter(c => c.cid !== cid);
          commentCount.value = comments.value.length;
          showToast('评论已删除');
        } else {
          showToast(d.msg);
        }
      } catch (_) { showToast('网络错误'); }
    }

    /* ── 模态逻辑 ── */
    function openLogin() { showLoginModal.value = true; showRegisterModal.value = false; }
    function openRegister() { showRegisterModal.value = true; showLoginModal.value = false; refreshCaptcha(); }
    function closeModals() { showLoginModal.value = false; showRegisterModal.value = false; }
    function switchToRegister() { showLoginModal.value = false; openRegister(); }
    function switchToLogin() { showRegisterModal.value = false; openLogin(); }
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
        const d = await r.json();
        if (d.ok) {
          closeModals();
          setLoggedIn(d.account, d.uid);
          loginAccount.value = '';
          loginPassword.value = '';
          showToast('登录成功');
        } else { loginError.value = d.msg; }
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
        const d = await r.json();
        if (d.ok) {
          closeModals();
          setLoggedIn(d.account, d.uid);
          regAccount.value = '';
          regPassword.value = '';
          regCaptcha.value = '';
          showToast('注册成功');
        } else { registerError.value = d.msg; refreshCaptcha(); }
      } catch (_) { registerError.value = '网络错误'; }
    }

    /* ── 初始化 ── */
    onMounted(async () => {
      const params = new URLSearchParams(window.location.search);
      const pid = params.get('pid');
      if (!pid) { notFound.value = true; loading.value = false; return; }

      try {
        const [postR, userR] = await Promise.all([
          fetch('/api/posts/' + pid),
          fetch('/api/user/me')
        ]);
        const postData = await postR.json();
        if (postData.ok) {
          post.value = postData.post;
        } else {
          notFound.value = true;
        }

        const userData = await userR.json();
        if (userData.loggedIn) setLoggedIn(userData.account, userData.uid);
      } catch (_) { notFound.value = true; }

      if (post.value) await loadComments();
      loading.value = false;
    });

    return {
      loggedIn, account, currentUid, avatarLetter,
      post, loading, notFound,
      comments, commentCount, newComment, submitting, commentError,
      showLoginModal, showRegisterModal,
      loginAccount, loginPassword, loginError,
      regAccount, regPassword, regCaptcha, registerError, captchaSrc,
      toastMsg, toastVisible,
      doLogout, toggleLike,
      loadComments, submitComment, deleteComment,
      openLogin, openRegister, closeModals, switchToRegister, switchToLogin,
      refreshCaptcha, doLogin, doRegister, showToast
    };
  }
});

app.mount('#app');
