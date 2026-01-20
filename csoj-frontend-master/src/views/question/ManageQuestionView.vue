<template>
  <div id="manageQuestionView">
    <!-- 搜索区域 -->
    <div class="search-container">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-input-search
            v-model="searchParams.title"
            placeholder="搜索题目标题"
            search-button
            @search="onSearch"
          />
        </a-col>
        <a-col :span="8">
          <a-input-search
            v-model="searchParams.content"
            placeholder="搜索题目内容"
            search-button
            @search="onSearch"
          />
        </a-col>
        <a-col :span="8">
          <a-button type="primary" @click="onSearch">搜索</a-button>
          <a-button style="margin-left: 8px" @click="resetSearch"
            >重置</a-button
          >
        </a-col>
      </a-row>
      <a-row style="margin-top: 16px">
        <a-col :span="24">
          <span style="margin-right: 8px">标签：</span>
          <TagSelect
            v-model="searchParams.tags"
            category="题目"
            placeholder="选择标签进行筛选"
            @change="onTagChange"
          />
        </a-col>
      </a-row>
    </div>

    <a-table
      :ref="tableRef"
      :columns="columns"
      :data="dataList"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
      }"
      @page-change="onPageChange"
    >
      <template #optional="{ record }">
        <a-space>
          <a-button type="primary" @click="doUpdate(record)"> 修改</a-button>
          <a-button status="danger" @click="doDelete(record)">删除</a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Page_Question_,
  Question,
  QuestionControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import * as querystring from "querystring";
import { useRouter } from "vue-router";
import TagSelect from "@/components/TagSelect.vue";

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  pageSize: 10,
  current: 1,
  title: "",
  content: "",
  tags: [] as string[],
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

// {id: "1", title: "A+ D", content: "新的题目内容", tags: "[\"二叉树\"]", answer: "新的答案", submitNum: 0,…}

const columns = [
  {
    title: "id",
    dataIndex: "id",
  },
  {
    title: "标题",
    dataIndex: "title",
  },
  {
    title: "内容",
    dataIndex: "content",
  },
  {
    title: "标签",
    dataIndex: "tags",
  },
  {
    title: "答案",
    dataIndex: "answer",
  },
  {
    title: "提交数",
    dataIndex: "submitNum",
  },
  {
    title: "通过数",
    dataIndex: "acceptedNum",
  },
  {
    title: "判题配置",
    dataIndex: "judgeConfig",
  },
  {
    title: "判题用例",
    dataIndex: "judgeCase",
  },
  {
    title: "用户id",
    dataIndex: "userId",
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

// 搜索处理函数
const onSearch = () => {
  searchParams.value = {
    ...searchParams.value,
    current: 1, // 重置到第一页
  };
};

// 重置搜索条件
const resetSearch = () => {
  searchParams.value = {
    ...searchParams.value,
    title: "",
    content: "",
    tags: [],
    current: 1,
  };
};

// 标签变化处理函数
const onTagChange = (tags: string[]) => {
  searchParams.value = {
    ...searchParams.value,
    tags: tags,
    current: 1, // 重置到第一页
  };
};

const doDelete = async (question: Question) => {
  const res = await QuestionControllerService.deleteQuestionUsingPost({
    id: question.id,
  });
  if (res.code === 0) {
    message.success("删除成功");
    loadData();
  } else {
    message.error("删除失败");
  }
};

const router = useRouter();

const doUpdate = (question: Question) => {
  router.push({
    path: "/update/question",
    query: {
      id: question.id,
    },
  });
};
</script>

<style scoped>
#manageQuestionView {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.search-container {
  margin-bottom: 24px;
  padding: 16px;
  background-color: var(--color-fill-1);
  border-radius: 4px;
}
</style>
