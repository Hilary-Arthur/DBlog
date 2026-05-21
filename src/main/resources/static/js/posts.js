/* ═══ 星码记 所有文章 — Vue 3 应用 ═══ */
const { createApp, ref, computed, onMounted } = Vue;

const app = createApp({
  setup() {
    const posts = ref([]);
    const currentPage = ref(1);
    const totalPages = ref(0);
    const totalCount = ref(0);
    const loading = ref(true);
    const searchQuery = ref('');
    const archives = ref([]);
    const activeMonth = ref('');

    const params = new URLSearchParams(window.location.search);
    const isSearch = params.has('q') && params.get('q').trim() !== '';
    if (isSearch) searchQuery.value = params.get('q').trim();

    async function loadPosts(page) {
      currentPage.value = page;
      loading.value = true;
      try {
        let url;
        if (isSearch) {
          url = '/api/posts/search?q=' + encodeURIComponent(searchQuery.value) + '&page=' + page;
        } else if (activeMonth.value) {
          url = '/api/posts?page=' + page + '&month=' + activeMonth.value;
        } else {
          url = '/api/posts?page=' + page;
        }
        const r = await fetch(url);
        const data = await r.json();
        if (!data.ok || !data.posts || data.posts.length === 0) {
          posts.value = [];
          totalPages.value = 0;
          totalCount.value = 0;
        } else {
          posts.value = data.posts;
          totalPages.value = data.totalPages || 0;
          totalCount.value = data.total || 0;
        }
      } catch (_) { posts.value = []; }
      loading.value = false;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    async function loadArchives() {
      try {
        const r = await fetch('/api/posts/stats');
        const data = await r.json();
        if (data.ok && data.months) {
          archives.value = data.months;
        }
      } catch (_) { /* ignore */ }
    }

    function toggleMonth(month) {
      if (activeMonth.value === month) {
        activeMonth.value = '';
      } else {
        activeMonth.value = month;
      }
      loadPosts(1);
    }

    function clearFilter() {
      activeMonth.value = '';
      loadPosts(1);
    }

    const pageNumbers = computed(() => {
      const arr = [];
      const total = totalPages.value;
      const curr = currentPage.value;
      let start = Math.max(1, curr - 2);
      let end = Math.min(total, start + 4);
      if (end - start < 4) start = Math.max(1, end - 4);
      for (let i = start; i <= end; i++) arr.push(i);
      return arr;
    });

    const showPagination = computed(() => totalPages.value > 1);

    const pageTitle = computed(() => {
      if (isSearch) return '搜索: ' + searchQuery.value;
      if (activeMonth.value) {
        const m = archives.value.find(a => a.month === activeMonth.value);
        return m ? m.label : activeMonth.value;
      }
      return '所有文章';
    });

    onMounted(() => {
      loadPosts(1);
      loadArchives();
    });

    return {
      posts, currentPage, totalPages, totalCount, pageNumbers, showPagination,
      loading, loadPosts, pageTitle, searchQuery, isSearch,
      archives, activeMonth, toggleMonth, clearFilter
    };
  }
});

app.component('post-card', PostCard);
app.mount('#app');
