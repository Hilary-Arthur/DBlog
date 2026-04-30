/* ═══ DBlog 个人主页 — Vue 3 应用 ═══ */
const { createApp, ref, computed, onMounted } = Vue;

const app = createApp({
  setup() {
    const loggedIn = ref(false);
    const account = ref('');
    const avatarLetter = computed(() => (account.value || '?').charAt(0).toUpperCase());

    /* ── 个人资料 ── */
    const editing = ref(false);
    const profile = ref({ uname: '', tel: '', email: '' });
    const editForm = ref({ uname: '', tel: '', email: '' });

    /* ── 文章 ── */
    const posts = ref([]);
    const selecting = ref(false);
    const selectedPids = ref([]);
    const selectedCount = computed(() => selectedPids.value.length);

    /* ── 删除弹窗 ── */
    const showDeleteModal = ref(false);
    const deletePassword = ref('');
    const deleteError = ref('');
    const deleting = ref(false);

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

    const statusMap = { PENDING: '审核中', APPROVED: '已通过', REJECTED: '已驳回' };

    /* ── 状态 ── */
    function setLoggedIn(acc) { loggedIn.value = true; account.value = acc; }

    /* ── 退出 ── */
    async function doLogout() {
      await fetch('/api/logout', { method: 'POST' });
      window.location.href = '/';
    }

    /* ── 个人资料 ── */
    async function loadProfile() {
      try {
        const r = await fetch('/api/user/profile');
        const d = await r.json();
        if (d.ok) {
          profile.value = { uname: d.uname || '', tel: d.tel || '', email: d.email || '' };
          editForm.value = { ...profile.value };
        }
      } catch (_) { /* ignore */ }
    }

    function toggleEdit() {
      editing.value = !editing.value;
      if (editing.value) {
        editForm.value = { ...profile.value };
      }
    }

    function cancelEdit() {
      editing.value = false;
      editForm.value = { ...profile.value };
    }

    async function saveProfile() {
      try {
        const r = await fetch('/api/user/profile', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            uname: editForm.value.uname.trim(),
            tel: editForm.value.tel.trim(),
            email: editForm.value.email.trim()
          })
        });
        const d = await r.json();
        if (d.ok) {
          editing.value = false;
          await loadProfile();
          showToast('保存成功');
        }
      } catch (_) { /* ignore */ }
    }

    /* ── 文章 ── */
    async function loadPosts() {
      try {
        const r = await fetch('/api/user/posts');
        const d = await r.json();
        if (d.ok && d.posts && d.posts.length > 0) {
          posts.value = d.posts;
        } else {
          posts.value = [];
        }
      } catch (_) { posts.value = []; }
    }

    function startSelecting() {
      selecting.value = true;
      selectedPids.value = [];
    }

    function cancelSelecting() {
      selecting.value = false;
      selectedPids.value = [];
    }

    function toggleCheck(pid) {
      const idx = selectedPids.value.indexOf(pid);
      if (idx === -1) selectedPids.value.push(pid);
      else selectedPids.value.splice(idx, 1);
    }

    function openDeleteModal() {
      if (selectedPids.value.length === 0) { showToast('请先勾选要删除的文章'); return; }
      deletePassword.value = '';
      deleteError.value = '';
      showDeleteModal.value = true;
    }

    async function confirmDelete() {
      if (!deletePassword.value) { deleteError.value = '请输入密码'; return; }
      deleting.value = true;
      try {
        const r = await fetch('/api/posts/batch', {
          method: 'DELETE',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ pids: selectedPids.value, password: deletePassword.value })
        });
        const d = await r.json();
        if (d.ok) {
          showDeleteModal.value = false;
          selecting.value = false;
          selectedPids.value = [];
          showToast(d.msg);
          await loadPosts();
        } else {
          deleteError.value = d.msg;
        }
      } catch (_) { deleteError.value = '网络错误'; }
      deleting.value = false;
    }

    /* ── 初始化 ── */
    onMounted(async () => {
      try {
        const r = await fetch('/api/user/me');
        const data = await r.json();
        if (data.loggedIn) {
          setLoggedIn(data.account);
          loadProfile();
          loadPosts();
        } else {
          window.location.href = '/';
        }
      } catch (_) { window.location.href = '/'; }
    });

    return {
      loggedIn, account, avatarLetter,
      editing, profile, editForm,
      posts, selecting, selectedPids, selectedCount,
      showDeleteModal, deletePassword, deleteError, deleting,
      toastMsg, toastVisible,
      statusMap,
      doLogout, toggleEdit, cancelEdit, saveProfile,
      startSelecting, cancelSelecting, toggleCheck, openDeleteModal, confirmDelete,
      showToast
    };
  }
});

app.component('post-card', PostCard);
app.mount('#app');
