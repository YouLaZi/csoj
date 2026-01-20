<template>
  <GlobalHeader />
  <div id="userProfileView">
    <a-card title="个人信息">
      <a-descriptions :column="{ xs: 1, md: 2, lg: 3 }" bordered>
        <a-descriptions-item label="头像">
          <a-avatar :size="64" :image-url="user.userAvatar">{{
            user.userName ? user.userName.charAt(0) : "U"
          }}</a-avatar>
        </a-descriptions-item>
        <a-descriptions-item label="用户名">
          {{ user.userName || "未设置" }}
        </a-descriptions-item>
        <a-descriptions-item label="用户账号">
          {{ user.userAccount || "未设置" }}
        </a-descriptions-item>
        <a-descriptions-item label="用户角色">
          {{ user.userRole === "admin" ? "管理员" : "普通用户" }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{
            user.createTime
              ? moment(user.createTime).format("YYYY-MM-DD HH:mm:ss")
              : "未知"
          }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{
            user.updateTime
              ? moment(user.updateTime).format("YYYY-MM-DD HH:mm:ss")
              : "未知"
          }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <a-form :model="form" label-align="left" auto-label-width>
        <a-form-item field="userName" label="用户名">
          <a-input v-model="form.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item field="userProfile" label="个人简介">
          <a-textarea
            v-model="form.userProfile"
            placeholder="请输入个人简介"
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>
        <a-form-item field="userAvatar" label="头像地址">
          <a-input v-model="form.userAvatar" placeholder="请输入头像URL地址" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="updateUserInfo">保存修改</a-button>
        </a-form-item>
      </a-form>
    </a-card>
    <a-card title="我的题目" style="margin-top: 16px">
      <a-table
        :columns="questionColumns"
        :data="myQuestions"
        :pagination="{
          showTotal: true,
          pageSize: 5,
        }"
      >
        <template #title="{ record }">
          <a @click="viewQuestion(record)">{{ record.title }}</a>
        </template>
        <template #createTime="{ record }">
          {{ moment(record.createTime).format("YYYY-MM-DD") }}
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button type="primary" @click="viewQuestion(record)">
              查看
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-card title="我的帖子" style="margin-top: 16px">
      <a-table
        :columns="postColumns"
        :data="myPosts"
        :pagination="{
          showTotal: true,
          pageSize: 5,
        }"
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
          </a-space>
        </template>
      </a-table>
    </a-card>
    <!-- 积分显示组件 -->
    <PointsDisplay />

    <!-- 学习数据统计组件 -->
    <UserStatistics />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from "vue";
import {
  UserControllerService,
  UserUpdateMyRequest,
  QuestionControllerService,
  QuestionQueryRequest,
  PostControllerService,
  PostQueryRequest,
} from "../../../generated";
// GlobalHeader已通过BasicLayout提供，不需要在此导入
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import moment from "moment";
import PointsDisplay from "@/components/PointsDisplay.vue";
import UserStatistics from "@/components/UserStatistics.vue";
import GlobalHeader from "@/components/GlobalHeader.vue";

const router = useRouter();
const store = useStore();

// 用户信息
const user = ref(store.state.user.loginUser || {});

// 表单数据
const form = reactive<UserUpdateMyRequest>({
  userName: user.value.userName || "",
  userProfile: user.value.userProfile || "",
  userAvatar: user.value.userAvatar || "",
});

// 我的题目列表
const myQuestions = ref([]);

// 我的帖子列表
const myPosts = ref([]);

// 题目表格列定义
const questionColumns = [
  {
    title: "题目名称",
    slotName: "title",
  },
  {
    title: "难度",
    dataIndex: "difficulty",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
  {
    slotName: "optional",
  },
];

// 帖子表格列定义
const postColumns = [
  {
    title: "标题",
    slotName: "title",
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

// 加载用户信息
const loadUserInfo = async () => {
  await store.dispatch("user/getLoginUser");
  user.value = store.state.user.loginUser;
  // 更新表单数据
  form.userName = user.value.userName || "";
  form.userProfile = user.value.userProfile || "";
  form.userAvatar = user.value.userAvatar || "";
};

// 加载我的题目
const loadMyQuestions = async () => {
  const queryRequest: QuestionQueryRequest = {
    current: 1,
    pageSize: 5,
    userId: user.value.id,
  };

  try {
    const res = await QuestionControllerService.listMyQuestionVoByPageUsingPost(
      queryRequest
    );
    if (res.code === 0) {
      myQuestions.value = res.data.records;
    }
  } catch (error) {
    console.error("加载我的题目失败", error);
  }
};

// 加载我的帖子
const loadMyPosts = async () => {
  const queryRequest: PostQueryRequest = {
    current: 1,
    pageSize: 5,
  };

  try {
    const res = await PostControllerService.listMyPostVoByPageUsingPost(
      queryRequest
    );
    if (res.code === 0) {
      myPosts.value = res.data.records;
    }
  } catch (error) {
    console.error("加载我的帖子失败", error);
  }
};

// 更新用户信息
const updateUserInfo = async () => {
  try {
    const res = await UserControllerService.updateMyUserUsingPost(form);
    if (res.code === 0) {
      message.success("更新成功");
      // 重新加载用户信息
      loadUserInfo();
    } else {
      message.error("更新失败：" + res.message);
    }
  } catch (error) {
    message.error("更新失败，请稍后重试");
    console.error(error);
  }
};

// 查看题目详情
const viewQuestion = (question) => {
  router.push(`/view/question/${question.id}`);
};

// 查看帖子详情
const viewPost = (post) => {
  if (post && post.id) {
    router.push(`/view/post/${post.id}`);
  } else {
    message.error("帖子ID无效");
  }
};

onMounted(async () => {
  await loadUserInfo();
  loadMyQuestions();
  loadMyPosts();
});
</script>

<style scoped>
#userProfileView {
  max-width: 1280px;
  margin: 0 auto;
  padding: 16px;
}
</style>
