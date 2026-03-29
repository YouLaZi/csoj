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
import TeamListView from "@/views/team/TeamListView.vue";
import TeamDetailView from "@/views/team/TeamDetailView.vue";
import CompetitionListView from "@/views/competition/CompetitionListView.vue";
import CompetitionDetailView from "@/views/competition/CompetitionDetailView.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/user",
    name: "nav.user",
    component: UserLayout,
    children: [
      {
        path: "login",
        name: "nav.login",
        component: UserLoginView,
      },
      {
        path: "register",
        name: "nav.register",
        component: UserRegisterView,
      },
      {
        path: "forgot-password",
        name: "nav.forgotPassword",
        component: ForgotPasswordView,
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
    component: UserProfileView,
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
    component: QuestionsView,
  },
  {
    path: "/question_submit",
    name: "nav.questionSubmit",
    component: QuestionSubmitView,
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/view/question/:id",
    name: "nav.doQuestion",
    component: ViewQuestionView,
    props: true,
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/add/question",
    name: "nav.createQuestion",
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/update/question",
    name: "nav.updateQuestion",
    component: AddQuestionView,
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
        component: ManageQuestionView,
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
    component: PostsView,
  },
  {
    path: "/view/post/:id",
    name: "nav.viewPost",
    component: ViewPostView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/tags",
    name: "nav.tags",
    component: TagsView,
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
    component: HomeView,
  },
  {
    path: "/hide",
    name: "nav.hidden",
    component: HomeView,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/checkin",
    name: "nav.checkin",
    component: CheckinView,
    meta: {
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/points/leaderboard",
    name: "nav.leaderboard",
    component: LeaderboardView,
  },
  {
    path: "/team",
    name: "nav.team",
    component: TeamListView,
  },
  {
    path: "/team/:id",
    name: "nav.teamDetail",
    component: TeamDetailView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/competition",
    name: "nav.competition",
    component: CompetitionListView,
  },
  {
    path: "/competition/:id",
    name: "nav.competitionDetail",
    component: CompetitionDetailView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/permission-example",
    name: "nav.permissionExample",
    component: PermissionExample,
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
    component: NoAuthView,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "nav.notFound",
    component: NotFoundView,
    meta: {
      hideInMenu: true,
    },
  },
];
