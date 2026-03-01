<template>
  <div id="questionsView">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">{{ t("questions.pageTitle") }}</h1>
      <p class="page-subtitle">{{ t("questions.pageSubtitle") }}</p>
    </div>

    <!-- 搜索筛选区 -->
    <div class="search-section">
      <div class="search-form">
        <div class="search-item">
          <label class="search-label">{{ t("questions.titleLabel") }}</label>
          <a-input
            v-model="searchParams.title"
            :placeholder="t('questions.searchPlaceholder')"
            allow-clear
            @press-enter="doSubmit"
          >
            <template #prefix>
              <icon-search />
            </template>
          </a-input>
        </div>
        <div class="search-item">
          <label class="search-label">{{ t("questions.tagFilter") }}</label>
          <a-input-tag
            v-model="searchParams.tags"
            :placeholder="t('questions.tagPlaceholder')"
            allow-clear
          />
        </div>
        <div class="search-actions">
          <a-button type="primary" @click="doSubmit">
            <template #icon><icon-search /></template>
            {{ t("questions.search") }}
          </a-button>
          <a-button @click="resetSearch">
            <template #icon><icon-refresh /></template>
            {{ t("questions.reset") }}
          </a-button>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="questions-section">
      <div class="section-header">
        <span class="section-title">{{ t("questions.allQuestions") }}</span>
        <span class="section-count">{{
          t("questions.totalQuestions", { total })
        }}</span>
      </div>

      <div class="questions-list">
        <div
          v-for="question in dataList"
          :key="question.id"
          class="question-item"
          @click="toQuestionPage(question)"
        >
          <div class="question-main">
            <div class="question-header">
              <span class="question-id">#{{ question.id }}</span>
              <h3 class="question-title">{{ question.title }}</h3>
            </div>
            <div class="question-tags">
              <span
                v-for="(tag, index) in question.tags"
                :key="index"
                class="tag"
              >
                {{ tag }}
              </span>
            </div>
          </div>
          <div class="question-stats">
            <div class="stat-item">
              <span class="stat-value">{{ formatAcceptRate(question) }}</span>
              <span class="stat-label">{{ t("questions.acceptRate") }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ question.submitNum || 0 }}</span>
              <span class="stat-label">{{ t("questions.submissions") }}</span>
            </div>
            <a-button type="primary" size="small" class="start-btn">
              {{ t("questions.startQuestion") }}
            </a-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="dataList.length === 0" class="empty-state">
        <icon-empty class="empty-icon" />
        <p class="empty-text">{{ t("questions.noData") }}</p>
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <a-pagination
          :total="total"
          :current="searchParams.current"
          :page-size="searchParams.pageSize"
          show-total
          show-jumper
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Question,
  QuestionControllerService,
  QuestionQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import {
  IconSearch,
  IconRefresh,
  IconEmpty,
} from "@arco-design/web-vue/es/icon";

const { t } = useI18n();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<QuestionQueryRequest>({
  title: "",
  tags: [],
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error(t("questions.loadFailed") + "，" + res.message);
  }
};

watchEffect(() => {
  loadData();
});

onMounted(() => {
  loadData();
});

const formatAcceptRate = (question: any) => {
  if (!question.submitNum || question.submitNum === 0) return "0%";
  const rate = Math.round((question.acceptedNum / question.submitNum) * 100);
  return `${rate}%`;
};

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const router = useRouter();

const toQuestionPage = (question: Question) => {
  router.push({
    path: `/view/question/${question.id}`,
  });
};

const doSubmit = () => {
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};

const resetSearch = () => {
  searchParams.value = {
    title: "",
    tags: [],
    pageSize: 10,
    current: 1,
  };
};
</script>

<style scoped>
/* ========================================
   题目列表页面 - 简约大方
   ======================================== */

#questionsView {
  padding: var(--spacing-xl);
  max-width: var(--content-max-width);
  margin: 0 auto;
}

/* ========================================
   页面头部
   ======================================== */

.page-header {
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
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  align-items: flex-end;
}

.search-item {
  flex: 1;
  min-width: 200px;
}

.search-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-secondary);
  margin-bottom: var(--spacing-xs);
}

.search-actions {
  display: flex;
  gap: var(--spacing-sm);
}

/* ========================================
   题目列表
   ======================================== */

.questions-section {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-color-light);
}

.section-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
}

.section-count {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.questions-list {
  padding: var(--spacing-md);
}

.question-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.question-item:hover {
  background: var(--bg-color-tertiary);
}

.question-main {
  flex: 1;
  min-width: 0;
}

.question-header {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.question-id {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-placeholder);
  flex-shrink: 0;
}

.question-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.question-item:hover .question-title {
  color: var(--primary-color);
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: var(--primary-lighter-color);
  color: var(--primary-color);
  font-size: var(--font-size-xs);
  border-radius: var(--radius-sm);
}

.question-stats {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  flex-shrink: 0;
  margin-left: var(--spacing-lg);
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-family: var(--font-family-serif);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--text-color-primary);
}

.stat-label {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-color-secondary);
}

.start-btn {
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.question-item:hover .start-btn {
  opacity: 1;
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
  margin: 0;
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
  #questionsView {
    padding: var(--spacing-md);
  }

  .search-form {
    flex-direction: column;
  }

  .search-item {
    width: 100%;
  }

  .search-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .question-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }

  .question-stats {
    width: 100%;
    justify-content: space-between;
    margin-left: 0;
    padding-top: var(--spacing-md);
    border-top: 1px solid var(--border-color-light);
  }

  .start-btn {
    opacity: 1;
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] .search-section,
[data-theme="dark"] .questions-section {
  background: var(--bg-color-secondary);
  border-color: var(--border-color);
}

[data-theme="dark"] .question-item:hover {
  background: var(--bg-color-tertiary);
}

[data-theme="dark"] .tag {
  background: var(--primary-light-color);
}
</style>
