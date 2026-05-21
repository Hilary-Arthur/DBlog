/* ═══ 星码记 通用博客卡片 — Vue 3 组件 ═══ */
const PostCard = {
  name: 'PostCard',
  props: {
    post: { type: Object, required: true }
  },
  methods: {
    snippet(content) {
      return (content || '').replace(/<[^>]*>/g, '').substring(0, 100);
    }
  },
  template: `
    <article class="post-card">
      <slot name="before"></slot>
      <div style="flex:1;min-width:0;">
        <h3 class="post-title mb-1"><a :href="'/p/' + post.hash">{{ post.title }}</a></h3>
        <div class="post-meta mb-2">
          <span>{{ post.createdAt }}</span>
          <slot name="meta-after"></slot>
        </div>
        <p class="post-excerpt">{{ snippet(post.content) }}</p>
        <slot name="footer"></slot>
      </div>
    </article>
  `
};
