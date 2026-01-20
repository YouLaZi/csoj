<template>
  <div id="adminPostsView">
    <a-card title="帖子管理">
      <a-row :gutter="[16, 16]" style="margin-bottom: 16px">
        <a-col :span="8">
          <a-input-search
            v-model="searchParams.searchText"
            placeholder="请输入搜索关键词"
            @search="doSubmit"
          />
        </a-col>
        <a-col :span="16" style="text-align: right">
          <a-button type="primary" @click="refreshData">刷新数据</a-button>
        </a-col>
      </a-row>
      <a-table
        :loading="loading"
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
        <template #title="{ record }">
          <a @click="viewPost(record)">{{ record.title }}</a>
        </template>
        <template #userName="{ record }">
          {{ record.user?.userName || "匿名" }}
        </template>
        <template #createTime="{ record }">
          {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
        </template>
        <template #updateTime="{ record }">
          {{ moment(record.updateTime).format("YYYY-MM-DD HH:mm:ss") }}
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button type="primary" @click="viewPost(record)"> 查看 </a-button>
            <a-button type="warning" @click="editPost(record)"> 编辑 </a-button>
            <a-popconfirm
              content="确定要删除这个帖子吗？"
              @ok="deletePost(record)"
            >
              <a-button type="danger"> 删除 </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from "vue";
import { useRouter } from "vue-router";
import {
  PostControllerService,
  PostVO,
  PostQueryRequest,
  DeleteRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";

const router = useRouter();
const loading = ref(false);
const dataList = ref([]);
const total = ref(0);

// 表格列定义
const columns = [
  {
    title: "标题",
    dataIndex: "title",
    slotName: "title",
  },
  {
    title: "作者",
    dataIndex: "userName",
    slotName: "userName",
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    slotName: "createTime",
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    slotName: "updateTime",
  },
  {
    title: "操作",
    slotName: "optional",
    width: 200,
  },
];

// 搜索参数
const searchParams = reactive<PostQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: "createTime",
  sortOrder: "descend",
  searchText: "",
});

// 加载帖子数据
const loadData = async () => {
  loading.value = true;
  try {
    const res = await PostControllerService.listPostVoByPageUsingPost(
      searchParams
    );
    if (res.code === 0) {
      dataList.value = res.data.records;
      total.value = res.data.total;
    } else {
      message.error("获取帖子列表失败：" + res.message);
    }
  } catch (error) {
    console.error("获取帖子列表失败", error);
    message.error("获取帖子列表失败，请稍后重试");
  } finally {
    loading.value = false;
  }
};

// 查看帖子详情
const viewPost = (post: PostVO) => {
  router.push({
    path: `/view/post/${post.id}`,
  });
};

// 编辑帖子
const editPost = (post: PostVO) => {
  router.push({
    path: `/update/post`,
    query: {
      id: post.id,
    },
  });
};

// 删除帖子
const deletePost = async (post: PostVO) => {
  try {
    const deleteRequest: DeleteRequest = {
      id: post.id,
    };
    const res = await PostControllerService.deletePostUsingPost(deleteRequest);
    if (res.code === 0) {
      message.success("删除成功");
      // 重新加载数据
      loadData();
    } else {
      message.error("删除失败：" + res.message);
    }
  } catch (error) {
    console.error("删除帖子失败", error);
    message.error("删除帖子失败，请稍后重试");
  }
};

// 刷新数据
const refreshData = () => {
  searchParams.current = 1;
  loadData();
};

// 提交搜索
const doSubmit = () => {
  searchParams.current = 1;
  loadData();
};

// 页码变化
const onPageChange = (page: number) => {
  searchParams.current = page;
  loadData();
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
#adminPostsView {
  max-width: 1280px;
  margin: 0 auto;
  padding: 16px;
}
</style>
