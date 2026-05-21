/* ═══ 星码记 首页 — Vue 3 应用 ═══ */
const { createApp, ref, onMounted } = Vue;

const app = createApp({
  setup() {
    const posts = ref([]);
    const loading = ref(true);

    async function loadFeatured() {
      loading.value = true;
      try {
        const r = await fetch('/api/posts/featured');
        const data = await r.json();
        if (data.ok && data.posts) {
          posts.value = data.posts;
        } else {
          posts.value = [];
        }
      } catch (_) { posts.value = []; }
      loading.value = false;
    }

    onMounted(() => { loadFeatured(); });

    return { posts, loading };
  }
});

app.component('post-card', PostCard);
app.mount('#app');
