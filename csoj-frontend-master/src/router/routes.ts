import { RouteRecordRaw } from "vue-router";
import UserLayout from "@/layouts/UserLayout.vue";
import ACCESS_ENUM from "@/access/accessEnum";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/user",
    name: "nav.user",
    component: UserLayout,
    children: [
      {
        path: "login",
        name: "nav.login",
        component: () => import("@/views/user/UserLoginView.vue"),
      },
      {
        path: "register",
        name: "nav.register",
        component: () => import("@/views/user/UserRegisterView.vue"),
      },
      {
        path: "forgot-password",
        name: "nav.forgotPassword",
        component: () => import("@/views/user/ForgotPasswordView.vue"),
      },
      {
        path: "reset-password",
        name: "nav.resetPassword",
        component: () => import("@/views/user/ResetPasswordView.vue"),
      },
    ],
    meta: {
      hideInMenu: true,
    },
  },
  // OAuth 第三方登录回调
  {
    path: "/oauth/callback/:platform",
    name: "oauthCallback",
    component: () => import("@/views/user/OAuthCallbackView.vue"),
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/user/profile",
    name: "nav.profile",
    component: () => import("@/views/user/UserProfileView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/user/preferences",
    name: "nav.preferences",
    component: () => import("@/views/user/UserPreferencesView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/questions",
    name: "nav.questions",
    component: () => import("@/views/question/QuestionsView.vue"),
  },
  {
    path: "/question_submit",
    name: "nav.questionSubmit",
    component: () => import("@/views/question/QuestionSubmitView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/view/question/:id",
    name: "nav.doQuestion",
    component: () => import("@/views/question/ViewQuestionView.vue"),
    props: true,
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/add/question",
    name: "nav.createQuestion",
    component: () => import("@/views/question/AddQuestionView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/update/question",
    name: "nav.updateQuestion",
    component: () => import("@/views/question/AddQuestionView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/manage/question",
    name: "nav.manageQuestion",
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
    redirect: "/manage/question/list",
    children: [
      {
        path: "list",
        name: "nav.questionList",
        component: () => import("@/views/question/ManageQuestionView.vue"),
        meta: {
          access: ACCESS_ENUM.ADMIN,
        },
      },
      {
        path: "batch-import",
        name: "nav.batchImport",
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
    name: "nav.posts",
    component: () => import("@/views/post/PostsView.vue"),
  },
  {
    path: "/view/post/:id",
    name: "nav.viewPost",
    component: () => import("@/views/post/ViewPostView.vue"),
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/tags",
    name: "nav.tags",
    component: () => import("@/views/tag/TagsView.vue"),
    props: true,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/posts",
    name: "nav.postManage",
    component: () => import("@/views/admin/AdminPostsView.vue"),
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/add/post",
    name: "nav.createPost",
    component: () => import("@/views/post/AddPostView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/update/post",
    name: "nav.updatePost",
    component: () => import("@/views/post/AddPostView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/",
    name: "nav.home",
    component: () => import("@/views/HomeView.vue"),
  },
  {
    path: "/hide",
    name: "nav.hidden",
    component: () => import("@/views/HomeView.vue"),
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/checkin",
    name: "nav.checkin",
    component: () => import("@/views/points/CheckinView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/points/leaderboard",
    name: "nav.leaderboard",
    component: () => import("@/views/points/LeaderboardView.vue"),
  },
  {
    path: "/team",
    name: "nav.team",
    component: () => import("@/views/team/TeamListView.vue"),
  },
  {
    path: "/team/:id",
    name: "nav.teamDetail",
    component: () => import("@/views/team/TeamDetailView.vue"),
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/competition",
    name: "nav.competition",
    component: () => import("@/views/competition/CompetitionListView.vue"),
  },
  {
    path: "/competition/:id",
    name: "nav.competitionDetail",
    component: () => import("@/views/competition/CompetitionDetailView.vue"),
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/permission-example",
    name: "nav.permissionExample",
    component: () => import("@/components/PermissionExample.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      title: "nav.permissionExample",
    },
  },
  {
    path: "/about",
    name: "nav.about",
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },
  {
    path: "/leaderboard",
    redirect: "/points/leaderboard",
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/noAuth",
    name: "nav.noAuth",
    component: () => import("@/views/NoAuthView.vue"),
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "nav.notFound",
    component: () => import("@/views/error/NotFoundView.vue"),
    meta: {
      hideInMenu: true,
    },
  },
];
