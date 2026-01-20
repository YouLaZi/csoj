<template>
  <div id="postsView">
    <a-card title="帖子列表">
      <a-row :gutter="[16, 16]" style="margin-bottom: 16px">
        <a-col :span="8">
          <a-input-search
            v-model="searchParams.searchText"
            placeholder="请输入搜索关键词"
            @search="doSubmit"
          />
        </a-col>
        <a-col :span="16" style="text-align: right">
          <a-button type="primary" @click="toAddPost">创建帖子</a-button>
        </a-col>
      </a-row>
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
        <template #title="{ record }">
          <a @click="viewPost(record)">{{ record.title }}</a>
        </template>
        <template #createTime="{ record }">
          {{ moment(record.createTime).format("YYYY-MM-DD") }}
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button type="primary" @click="viewPost(record)"> 查看 </a-button>
            <a-button v-if="isMyPost(record)" @click="doFavour(record)">
              {{ record.hasFavour ? "取消收藏" : "收藏" }}
            </a-button>
            <a-button v-if="isMyPost(record)" @click="doThumb(record)">
              {{ record.hasThumb ? "取消点赞" : "点赞" }}
            </a-button>
            <a-button @click="toggleComments(record)">
              {{ record.showComments ? "收起评论" : "查看评论" }}
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 选中帖子的评论区域 -->
    <div v-if="selectedPost">
      <a-divider>{{ selectedPost.title }} - 评论区</a-divider>
      <CommentList
        :objectId="selectedPost.id"
        objectType="post"
        @refresh="refreshPostData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  PostControllerService,
  PostQueryRequest,
  PostVO,
  PostFavourControllerService,
  PostFavourAddRequest,
  PostThumbControllerService,
  PostThumbAddRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";
import { useStore } from "vuex";
import CommentList from "@/components/CommentList.vue";

const tableRef = ref();
const store = useStore();
const router = useRouter();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<PostQueryRequest>({
  searchText: "",
  pageSize: 10,
  current: 1,
});

// 选中的帖子（用于显示评论）
const selectedPost = ref(null);

const loadData = async () => {
  const res = await PostControllerService.listPostVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    // 为每个帖子添加showComments属性，用于控制评论的显示和隐藏
    dataList.value = res.data.records.map((post) => ({
      ...post,
      showComments: false,
    }));
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

const columns = [
  {
    title: "标题",
    slotName: "title",
  },
  {
    title: "内容",
    dataIndex: "content",
    ellipsis: true,
  },
  {
    title: "点赞数",
    dataIndex: "thumbNum",
  },
  {
    title: "收藏数",
    dataIndex: "favourNum",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
  {
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

/**
 * 确认搜索，重新加载数据
 */
const doSubmit = () => {
  // 这里需要重置搜索页号
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};

/**
 * 跳转到帖子详情页
 * @param post
 */
const viewPost = (post: PostVO) => {
  router.push({
    path: `/view/post/${post.id}`,
  });
};

/**
 * 跳转到创建帖子页
 */
const toAddPost = () => {
  router.push({
    path: `/add/post`,
  });
};

/**
 * 判断是否是当前用户的帖子
 * @param post
 */
const isMyPost = (post: PostVO) => {
  const loginUser = store.state.user.loginUser;
  return loginUser && loginUser.id === post.userId;
};

/**
 * 收藏/取消收藏帖子
 * @param post
 */
const doFavour = async (post: PostVO) => {
  const postFavourAddRequest: PostFavourAddRequest = {
    postId: post.id,
  };
  const res = await PostFavourControllerService.doPostFavourUsingPost(
    postFavourAddRequest
  );
  if (res.code === 0) {
    message.success(post.hasFavour ? "取消收藏成功" : "收藏成功");
    // 重新加载数据
    loadData();
  } else {
    message.error("操作失败，" + res.message);
  }
};

/**
 * 点赞/取消点赞帖子
 * @param post
 */
const doThumb = async (post: PostVO) => {
  const postThumbAddRequest: PostThumbAddRequest = {
    postId: post.id,
  };
  const res = await PostThumbControllerService.doThumbUsingPost(
    postThumbAddRequest
  );
  if (res.code === 0) {
    message.success(post.hasThumb ? "取消点赞成功" : "点赞成功");
    // 重新加载数据
    loadData();
  } else {
    message.error("操作失败，" + res.message);
  }
};

/**
 * 切换评论区的显示和隐藏
 * @param post
 */
const toggleComments = (post) => {
  // 更新帖子的showComments状态
  post.showComments = !post.showComments;

  if (post.showComments) {
    // 如果显示评论，则设置当前选中的帖子
    selectedPost.value = post;
  } else {
    // 如果隐藏评论，则清空当前选中的帖子
    selectedPost.value = null;
  }
};

/**
 * 刷新帖子数据
 */
const refreshPostData = () => {
  // 评论操作后刷新帖子数据
  loadData();
};
</script>

<style scoped>
#postsView {
  max-width: 1280px;
  margin: 0 auto;
  padding: 16px;
}
</style>
