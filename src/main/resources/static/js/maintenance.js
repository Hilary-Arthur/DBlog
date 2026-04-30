/* ═══ DBlog 维护页 — Vue 3 应用 ═══ */
const { createApp, ref } = Vue;

const app = createApp({
  setup() {
    const showAdminModal = ref(false);
    const adminAccount = ref('');
    const adminPassword = ref('');
    const adminError = ref('');

    function openAdmin() { showAdminModal.value = true; }
    function closeAdmin() { showAdminModal.value = false; }

    async function doAdminLogin() {
      adminError.value = '';
      if (!adminAccount.value.trim() || !adminPassword.value) {
        adminError.value = '请填写账号和密码';
        return;
      }
      try {
        const r = await fetch('/api/admin/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ account: adminAccount.value.trim(), password: adminPassword.value })
        });
        const data = await r.json();
        if (data.ok) {
          window.location.href = '/admin.html';
        } else {
          adminError.value = data.msg;
        }
      } catch (_) { adminError.value = '网络错误'; }
    }

    return { showAdminModal, adminAccount, adminPassword, adminError, openAdmin, closeAdmin, doAdminLogin };
  }
});

app.mount('#app');
