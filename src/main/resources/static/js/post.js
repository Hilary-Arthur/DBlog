/* ═══ 星码记 文章详情 — Vue 3 应用 ═══ */
const { createApp, ref, computed, onMounted } = Vue;

/* ── 轻量 Markdown 渲染器 ── */
function renderMarkdown(text) {
  if (!text) return '';

  // 转义 HTML
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');

  // 代码块 ```...```
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    const cls = lang ? ' class="language-' + lang + '"' : '';
    return '<pre><code' + cls + '>' + code.trim() + '</code></pre>';
  });

  // 行内代码 `...`
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

  // 标题 ## / ###（生成 id 用于目录锚点）
  html = html.replace(/^### (.+)$/gm, (_, text) => {
    const id = 'toc-' + text.replace(/<[^>]+>/g, '').replace(/&\w+;/g, '').replace(/[^\w一-鿿]+/g, '-').replace(/^-|-$/g, '').toLowerCase();
    return '<h3 id="' + id + '">' + text + '</h3>';
  });
  html = html.replace(/^## (.+)$/gm, (_, text) => {
    const id = 'toc-' + text.replace(/<[^>]+>/g, '').replace(/&\w+;/g, '').replace(/[^\w一-鿿]+/g, '-').replace(/^-|-$/g, '').toLowerCase();
    return '<h2 id="' + id + '">' + text + '</h2>';
  });

  // 粗体 / 斜体
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');

  // 链接 [text](url)
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');

  // 图片 ![alt](url)
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" style="max-width:100%;border-radius:8px;margin:8px 0;">');

  // 引用块 > ...
  html = html.replace(/^&gt; (.+)$/gm, '<blockquote>$1</blockquote>');

  // 水平线 ---
  html = html.replace(/^---$/gm, '<hr>');

  // 有序列表 1. item（先处理，避免被无序列表正则误匹配）
  html = html.replace(/^\d+\. (.+)$/gm, '<oli>$1</oli>');
  html = html.replace(/(<oli>[\s\S]*?<\/oli>[\s]*)+/g, (match) => {
    return '<ol>' + match.replace(/<\/?oli>/g, (t) => t.replace('oli', 'li')) + '</ol>';
  });

  // 无序列表 - item
  html = html.replace(/^- (.+)$/gm, '<uli>$1</uli>');
  html = html.replace(/(<uli>[\s\S]*?<\/uli>[\s]*)+/g, (match) => {
    return '<ul>' + match.replace(/<\/?uli>/g, (t) => t.replace('uli', 'li')) + '</ul>';
  });

  // 表格（简单支持）
  html = html.replace(/^\|(.+)\|$/gm, (match, content) => {
    const cells = content.split('|').map(c => c.trim());
    if (cells.every(c => /^[-:]+$/.test(c))) return ''; // 分隔行跳过
    const tag = 'td';
    return '<tr>' + cells.map(c => '<' + tag + '>' + c + '</' + tag + '>').join('') + '</tr>';
  });
  html = html.replace(/(<tr>.*<\/tr>\n?)+/g, (match) => '<table>' + match + '</table>');

  // 段落：连续非标签文本行合并为 <p>
  html = html.split('\n').map(line => {
    if (line.trim() === '') return '';
    if (/^<[a-z]/.test(line.trim())) return line;
    return '<p>' + line + '</p>';
  }).join('\n');

  // 清理空段落
  html = html.replace(/<p><\/p>/g, '');

  return html;
}

const app = createApp({
  setup() {
    const post = ref(null);
    const loading = ref(true);
    const notFound = ref(false);
    const recommended = ref([]);
    const activeId = ref('');

    const renderedContent = computed(() => {
      return post.value ? renderMarkdown(post.value.content) : '';
    });

    const tocItems = computed(() => {
      if (!renderedContent.value) return [];
      var items = [];
      var re = /<h([23]) id="([^"]+)">([\s\S]*?)<\/h[23]>/g;
      var m;
      while ((m = re.exec(renderedContent.value)) !== null) {
        var text = m[3].replace(/<[^>]+>/g, '').replace(/&\w+;/g, '');
        items.push({ level: +m[1], id: m[2], text: text });
      }
      return items;
    });

    function scrollTo(id) {
      var el = document.getElementById(id);
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    const readingTime = computed(() => {
      if (!post.value || !post.value.content) return '';
      const chars = post.value.content.replace(/\s/g, '').length;
      const minutes = Math.floor(chars / 300);
      if (minutes <= 0) return '1 分钟';
      if (minutes > 30) return '30 分钟以上';
      return minutes + ' 分钟';
    });

    onMounted(async () => {
      // 从 URL 路径 /p/{hash} 提取 hash
      const pathMatch = window.location.pathname.match(/\/p\/([^/?#]+)/);
      const hash = pathMatch ? pathMatch[1] : null;
      if (!hash) { notFound.value = true; loading.value = false; return; }

      try {
        const r = await fetch('/api/posts/' + hash);
        const data = await r.json();
        if (data.ok) {
          post.value = data.post;
          document.title = data.post.title + ' - 星码记';
        } else {
          notFound.value = true;
        }
      } catch (_) { notFound.value = true; }

      loading.value = false;

      /* ── 目录 scroll-spy ── */
      if (tocItems.value.length) {
        var headings = document.querySelectorAll('.post-body h2, .post-body h3');
        if (headings.length) {
          activeId.value = headings[0].id;
          var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
              if (entry.isIntersecting) activeId.value = entry.target.id;
            });
          }, { rootMargin: '-80px 0px -70% 0px', threshold: 0 });
          headings.forEach(function (h) { observer.observe(h); });
        }
      }

      /* ── 加载推荐文章 ── */
      try {
        const fr = await fetch('/api/posts/featured');
        const fd = await fr.json();
        if (fd.ok && fd.posts) {
          const others = fd.posts.filter(p => p.hash !== hash);
          const shuffled = others.sort(() => 0.5 - Math.random());
          recommended.value = shuffled.slice(0, 3).map(p => ({
            hash: p.hash,
            title: p.title,
            createdAt: p.createdAt,
            snippet: (p.content || '').replace(/[#*`>\-\[\]()!|]/g, '').replace(/\s+/g, ' ').slice(0, 50) + '...'
          }));
        }
      } catch (_) {}

      /* ── 回到顶部按钮 ── */
      const btn = document.getElementById('backToTop');
      if (btn) {
        window.addEventListener('scroll', function () {
          btn.classList.toggle('visible', window.scrollY > 300);
        });
        btn.addEventListener('click', function () {
          window.scrollTo({ top: 0, behavior: 'smooth' });
        });
      }
    });

    return { post, loading, notFound, renderedContent, readingTime, recommended, tocItems, activeId, scrollTo };
  }
});

app.mount('#app');
