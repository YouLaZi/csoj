<template>
  <div id="postsView">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">{{ t("posts.pageTitle") }}</h1>
        <p class="page-subtitle">{{ t("posts.pageSubtitle") }}</p>
      </div>
      <a-button type="primary" @click="toAddPost">
        <template #icon><icon-plus /></template>
        {{ t("posts.createPost") }}
      </a-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-input-search
        v-model="searchParams.searchText"
        :placeholder="t('posts.searchPlaceholder')"
        size="large"
        @search="doSubmit"
        @press-enter="doSubmit"
      />
    </div>

    <!-- 帖子列表 -->
    <div class="posts-section">
      <div class="posts-list">
        <div
          v-for="post in dataList"
          :key="post.id"
          class="post-card"
          @click="viewPost(post)"
        >
          <div class="post-main">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-content-preview">{{ post.content }}</p>
            <div class="post-footer">
              <div class="post-stats">
                <span class="stat-item">
                  <icon-thumb-up />
                  {{ post.thumbNum || 0 }}
                </span>
                <span class="stat-item">
                  <icon-star />
                  {{ post.favourNum || 0 }}
                </span>
              </div>
              <span class="post-time">
                {{ moment(post.createTime).format("YYYY-MM-DD") }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="dataList.length === 0" class="empty-state">
        <icon-file class="empty-icon" />
        <p class="empty-text">{{ t("posts.noData") }}</p>
        <a-button type="primary" @click="toAddPost">{{
          t("posts.createFirst")
        }}</a-button>
      </div>

      <!-- 分页 -->
      <div v-if="total > searchParams.pageSize" class="pagination-section">
        <a-pagination
          :total="total"
          :current="searchParams.current"
          :page-size="searchParams.pageSize"
          show-total
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  PostControllerService,
  PostQueryRequest,
  PostVO,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import moment from "moment";
import {
  IconPlus,
  IconThumbUp,
  IconStar,
  IconFile,
} from "@arco-design/web-vue/es/icon";

const { t } = useI18n();

const router = useRouter();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<PostQueryRequest>({
  searchText: "",
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  const res = await PostControllerService.listPostVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records.map((post) => ({
      ...post,
      showComments: false,
    }));
    total.value = res.data.total;
  } else {
    message.error(t("posts.loadFailed") + "，" + res.message);
  }
};

watchEffect(() => {
  loadData();
});

onMounted(() => {
  loadData();
});

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const doSubmit = () => {
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};

const viewPost = (post: PostVO) => {
  router.push({
    path: `/view/post/${post.id}`,
  });
};

const toAddPost = () => {
  router.push({
    path: `/add/post`,
  });
};
</script>

<style scoped>
/* ========================================
   帖子列表页面 - 简约大方
   ======================================== */

#postsView {
  padding: var(--spacing-xl);
  max-width: var(--content-max-width);
  margin: 0 auto;
}

/* ========================================
   页面头部
   ======================================== */

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.page-subtitle {
  font-size: var(--font-size-base);
  color: var(--text-color-secondary);
  margin: 0;
}

/* ========================================
   搜索区域
   ======================================== */

.search-section {
  margin-bottom: var(--spacing-xl);
}

.search-section :deep(.arco-input-wrapper) {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
}

.search-section :deep(.arco-input-wrapper:hover) {
  border-color: var(--border-color);
}

.search-section :deep(.arco-input-wrapper:focus-within) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--focus-ring-color);
}

/* ========================================
   帖子列表
   ======================================== */

.posts-section {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.posts-list {
  padding: var(--spacing-md);
}

.post-card {
  padding: var(--spacing-lg);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.post-card:hover {
  background: var(--bg-color-tertiary);
}

.post-main {
  max-width: 100%;
}

.post-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-sm) 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-card:hover .post-title {
  color: var(--primary-color);
}

.post-content-preview {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
  margin: 0 0 var(--spacing-md) 0;
  line-height: var(--line-height-relaxed);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  display: flex;
  gap: var(--spacing-md);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--text-color-placeholder);
}

.post-time {
  font-size: var(--font-size-sm);
  color: var(--text-color-placeholder);
}

/* ========================================
   空状态
   ======================================== */

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-3xl);
}

.empty-icon {
  font-size: 48px;
  color: var(--text-color-placeholder);
  margin-bottom: var(--spacing-md);
}

.empty-text {
  font-size: var(--font-size-base);
  color: var(--text-color-secondary);
  margin: 0 0 var(--spacing-lg) 0;
}

/* ========================================
   分页
   ======================================== */

.pagination-section {
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg);
  border-top: 1px solid var(--border-color-light);
}

/* ========================================
   响应式设计
   ======================================== */

@media (max-width: 768px) {
  #postsView {
    padding: var(--spacing-md);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] .posts-section {
  background: var(--bg-color-secondary);
  border-color: var(--border-color);
}

[data-theme="dark"] .post-card:hover {
  background: var(--bg-color-tertiary);
}

[data-theme="dark"] .search-section :deep(.arco-input-wrapper) {
  background: var(--bg-color-tertiary);
}
</style>
