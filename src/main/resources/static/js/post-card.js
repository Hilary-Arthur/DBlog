/* ═══ DBlog 通用博客卡片 — Vue 3 组件 ═══ */
const PostCard = {
  name: 'PostCard',
  props: {
    post: { type: Object, required: true }
  },
  emits: ['like'],
  methods: {
    snippet(content) {
      return (content || '').replace(/<[^>]*>/g, '').substring(0, 150);
    }
  },
  template: `
    <article class="post-card">
      <slot name="before"></slot>
      <div style="flex:1;min-width:0;">
        <div class="post-meta mb-2">
          <span>{{ post.createdAt }}</span>
          <span class="meta-dot"></span>
          <span>{{ post.author }}</span>
          <slot name="meta-after"></slot>
        </div>
        <h3 class="post-title mb-2"><a href="#">{{ post.title }}</a></h3>
        <p class="post-excerpt mb-3">{{ snippet(post.content) }}</p>
        <slot name="footer">
          <div class="post-footer d-flex align-items-center gap-3">
            <button class="like-btn" :class="{ liked: post.liked }" @click="$emit('like', post.pid)">
              <svg v-if="post.liked" viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              <span>{{ post.likeCount || 0 }}</span>
            </button>
            <span class="d-flex align-items-center gap-1" style="opacity:0.5;">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> 评论
            </span>
          </div>
        </slot>
      </div>
    </article>
  `
};
