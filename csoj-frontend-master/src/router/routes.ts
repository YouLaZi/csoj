import { RouteRecordRaw } from "vue-router";
import HomeView from "@/views/HomeView.vue";
import UserLayout from "@/layouts/UserLayout.vue";
import UserLoginView from "@/views/user/UserLoginView.vue";
import UserRegisterView from "@/views/user/UserRegisterView.vue";
import ForgotPasswordView from "@/views/user/ForgotPasswordView.vue";
import UserProfileView from "@/views/user/UserProfileView.vue";
import AdminView from "@/views/AdminView.vue";
import NoAuthView from "@/views/NoAuthView.vue";
import ACCESS_ENUM from "@/access/accessEnum";
import PermissionExample from "@/components/PermissionExample.vue";
import AddQuestionView from "@/views/question/AddQuestionView.vue";
import ManageQuestionView from "@/views/question/ManageQuestionView.vue";
import QuestionsView from "@/views/question/QuestionsView.vue";
import QuestionSubmitView from "@/views/question/QuestionSubmitView.vue";
import ViewQuestionView from "@/views/question/ViewQuestionView.vue";
import PostsView from "@/views/post/PostsView.vue";
import ViewPostView from "@/views/post/ViewPostView.vue";
import TagsView from "@/views/tag/TagsView.vue";
import CheckinView from "@/views/points/CheckinView.vue";
import LeaderboardView from "@/views/points/LeaderboardView.vue";
import NotFoundView from "@/views/error/NotFoundView.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/user",
    name: "用户",
    component: UserLayout,
    children: [
      {
        path: "login",
        name: "用户登录",
        component: UserLoginView,
      },
      {
        path: "register",
        name: "用户注册",
        component: UserRegisterView,
      },
      {
        path: "forgot-password",
        name: "忘记密码",
        component: ForgotPasswordView,
      },
      {
        path: "reset-password",
        name: "重置密码",
        component: () => import("@/views/user/ResetPasswordView.vue"),
      },
    ],
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/user/profile",
    name: "个人中心",
    component: UserProfileView,
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/user/preferences",
    name: "个性化设置",
    component: () => import("@/views/user/UserPreferencesView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/questions",
    name: "浏览题目",
    component: QuestionsView,
  },
  {
    path: "/question_submit",
    name: "浏览题目提交",
    component: QuestionSubmitView,
  },
  {
    path: "/view/question/:id",
    name: "在线做题",
    component: ViewQuestionView,
    props: true,
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/add/question",
    name: "创建题目",
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/update/question",
    name: "更新题目",
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/manage/question",
    name: "管理题目",
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
    redirect: "/manage/question/list",
    children: [
      {
        path: "list",
        name: "题目列表",
        component: ManageQuestionView,
        meta: {
          access: ACCESS_ENUM.ADMIN,
        },
      },
      {
        path: "batch-import",
        name: "批量导入题目",
        component: () =>
          import("@/views/admin/question/BatchImportQuestionsView.vue"),
        meta: {
          access: ACCESS_ENUM.ADMIN,
        },
      },
    ],
  },
  {
    path: "/posts",
    name: "浏览帖子",
    component: PostsView,
  },
  {
    path: "/view/post/:id",
    name: "查看帖子",
    component: ViewPostView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/tags",
    name: "标签管理",
    component: TagsView,
    props: true,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/posts",
    name: "帖子管理",
    component: () => import("@/views/admin/AdminPostsView.vue"),
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/add/post",
    name: "创建帖子",
    component: () => import("@/views/post/AddPostView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/update/post",
    name: "更新帖子",
    component: () => import("@/views/post/AddPostView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/",
    name: "主页",
    component: HomeView,
  },
  {
    path: "/hide",
    name: "隐藏页面",
    component: HomeView,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/checkin",
    name: "每日签到",
    component: CheckinView,
    meta: {
      access: ACCESS_ENUM.USER,
    },
  }, // 积分排行榜路由
  {
    path: "/points/leaderboard",
    name: "积分排行榜",
    component: LeaderboardView,
  },
  {
    path: "/permission-example",
    name: "权限示例",
    component: PermissionExample,
    meta: {
      access: ACCESS_ENUM.USER,
      title: "权限示例",
    },
  },

  {
    path: "/about",
    name: "关于",
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },
  // 保留旧路由以兼容性
  {
    path: "/leaderboard",
    redirect: "/points/leaderboard",
  },
  {
    path: "/noAuth",
    name: "无权限",
    component: NoAuthView,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: NotFoundView,
    meta: {
      hideInMenu: true,
    },
  },
];
