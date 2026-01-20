<template>
  <a-modal
    v-model:visible="visible"
    :title="isLogin ? '用户登录' : '用户注册'"
    :footer="false"
    :mask-closable="false"
    width="480px"
    class="auth-modal"
    :unmount-on-close="false"
  >
    <!-- 登录表单 -->
    <a-form
      v-if="isLogin"
      label-align="left"
      auto-label-width
      :model="loginForm"
      @submit="handleLoginSubmit"
    >
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="loginForm.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于 8 位" label="密码">
        <a-input-password
          v-model="loginForm.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item field="verificationCode" label="验证码">
        <div class="verification-container">
          <a-input
            v-model="loginForm.verificationCode"
            placeholder="请输入验证码"
          />
          <VerificationCode ref="verificationCodeRef" />
        </div>
      </a-form-item>
      <a-form-item>
        <div class="form-actions">
          <a-button type="primary" html-type="submit" class="submit-btn">
            登录
          </a-button>
        </div>
      </a-form-item>
      <div class="additional-actions">
        <a-button @click="switchMode" type="text">没有账号？去注册</a-button>
        <a-button @click="showForgotPassword" type="text">忘记密码？</a-button>
      </div>
    </a-form>

    <!-- 注册表单 -->
    <a-form
      v-else
      label-align="left"
      auto-label-width
      :model="registerForm"
      :rules="registerRules"
      @submit="handleRegisterSubmit"
    >
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="registerForm.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于 8 位" label="密码">
        <a-input-password
          v-model="registerForm.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item
        field="checkPassword"
        tooltip="请再次输入密码"
        label="确认密码"
      >
        <a-input-password
          v-model="registerForm.checkPassword"
          placeholder="请再次输入密码"
        />
      </a-form-item>
      <a-form-item field="userName" label="用户名">
        <a-input v-model="registerForm.userName" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item>
        <div class="form-actions">
          <a-button type="primary" html-type="submit" class="submit-btn">
            注册
          </a-button>
        </div>
      </a-form-item>
      <div class="additional-actions">
        <a-button @click="switchMode" type="text">已有账号？去登录</a-button>
      </div>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref, defineExpose } from "vue";
import {
  UserControllerService,
  UserLoginRequest,
  UserRegisterRequest,
} from "../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import VerificationCode from "@/components/VerificationCode.vue";

// 控制模态框显示
const visible = ref(false);

// 控制显示登录还是注册表单
const isLogin = ref(true);

// 验证码组件引用
const verificationCodeRef = ref(null);

// 登录表单信息
const loginForm = reactive({
  userAccount: "",
  userPassword: "",
  verificationCode: "",
});

// 注册表单信息
const registerForm = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  userName: "",
} as UserRegisterRequest & { checkPassword: string });

// 注册表单校验规则
const registerRules = {
  userAccount: [
    { required: true, message: "请输入账号" },
    { minLength: 4, message: "账号长度不能小于 4 位" },
  ],
  userPassword: [
    { required: true, message: "请输入密码" },
    { minLength: 8, message: "密码长度不能小于 8 位" },
  ],
  checkPassword: [
    { required: true, message: "请再次输入密码" },
    {
      validator: (value: string) => {
        return value === registerForm.userPassword;
      },
      message: "两次输入的密码不一致",
    },
  ],
  userName: [
    { required: true, message: "请输入用户名" },
    { minLength: 2, message: "用户名长度不能小于 2 位" },
  ],
};

const router = useRouter();
const store = useStore();

// 切换登录/注册模式
const switchMode = () => {
  isLogin.value = !isLogin.value;
  // 切换时重置表单
  if (isLogin.value) {
    loginForm.userAccount = "";
    loginForm.userPassword = "";
    loginForm.verificationCode = "";
    if (verificationCodeRef.value) {
      verificationCodeRef.value.refreshCode();
    }
  } else {
    registerForm.userAccount = "";
    registerForm.userPassword = "";
    registerForm.checkPassword = "";
    registerForm.userName = "";
  }
};

// 打开模态框
const open = (mode: "login" | "register" = "login") => {
  isLogin.value = mode === "login";
  visible.value = true;
  // 如果是登录模式，刷新验证码
  if (isLogin.value && verificationCodeRef.value) {
    verificationCodeRef.value.refreshCode();
  }
};

// 关闭模态框
const close = () => {
  visible.value = false;
};

// 显示忘记密码
const showForgotPassword = () => {
  // 跳转到忘记密码页面
  router.push("/user/forgot-password");
  close();
};

// 登录提交
const handleLoginSubmit = async () => {
  // 验证码校验
  if (!loginForm.verificationCode) {
    message.error("请输入验证码");
    return;
  }

  // 验证码校验
  if (
    verificationCodeRef.value &&
    verificationCodeRef.value.code &&
    verificationCodeRef.value.code.value &&
    loginForm.verificationCode.toLowerCase() !==
      verificationCodeRef.value.code.value.toLowerCase()
  ) {
    message.error("验证码错误");
    verificationCodeRef.value.refreshCode();
    loginForm.verificationCode = "";
    return;
  }

  const loginRequest = {
    userAccount: loginForm.userAccount,
    userPassword: loginForm.userPassword,
  } as UserLoginRequest;

  try {
    // 显示加载中提示
    const loadingMessage = message.loading({
      content: "登录中...",
      duration: 0,
    });

    const res = await UserControllerService.userLoginUsingPost(loginRequest);
    // 关闭加载提示
    loadingMessage.close();

    // 登录成功，关闭模态框
    if (res.code === 0) {
      // 先设置token，再获取用户信息
      if (res.data.token) {
        await store.dispatch("user/setToken", res.data.token);
      }
      await store.dispatch("user/getLoginUser");
      close();
      message.success("登录成功");
    } else {
      message.error("登录失败，" + res.message);
      // 刷新验证码
      if (verificationCodeRef.value) {
        verificationCodeRef.value.refreshCode();
        loginForm.verificationCode = "";
      }
    }
  } catch (error) {
    // 处理网络错误
    console.error("登录请求错误:", error);

    // 显示友好的错误信息
    if (error.message === "Network Error") {
      message.error("网络连接错误，请检查网络连接或确认后端服务是否启动");
    } else {
      message.error("登录失败，" + (error.message || "未知错误"));
    }

    // 刷新验证码
    if (verificationCodeRef.value) {
      verificationCodeRef.value.refreshCode();
      loginForm.verificationCode = "";
    }
  }
};

// 注册提交
const handleRegisterSubmit = async () => {
  const registerRequest: UserRegisterRequest = {
    userAccount: registerForm.userAccount,
    userPassword: registerForm.userPassword,
    checkPassword: registerForm.checkPassword,
    userName: registerForm.userName,
  };
  const res = await UserControllerService.userRegisterUsingPost(
    registerRequest
  );
  if (res.code === 0) {
    message.success("注册成功");
    // 切换到登录模式
    isLogin.value = true;
    // 清空登录表单
    loginForm.userAccount = registerForm.userAccount;
    loginForm.userPassword = "";
    loginForm.verificationCode = "";
    if (verificationCodeRef.value) {
      verificationCodeRef.value.refreshCode();
    }
  } else {
    message.error("注册失败，" + res.message);
  }
};

// 暴露方法给父组件
defineExpose({
  open,
  close,
});
</script>

<style scoped>
/* 模态框样式优化 */
.auth-modal :deep(.arco-modal) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.auth-modal :deep(.arco-modal-header) {
  border-bottom: none;
  padding: 24px 24px 0;
}

.auth-modal :deep(.arco-modal-title) {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-1);
  text-align: center;
}

.auth-modal :deep(.arco-modal-body) {
  padding: 20px 32px 32px;
}

/* 表单样式优化 */
.auth-modal :deep(.arco-form) {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
}

.auth-modal :deep(.arco-form-item) {
  width: 100%;
  max-width: 400px;
  margin: 0 auto 20px;
  display: flex;
  justify-content: center;
}

.auth-modal :deep(.arco-form-item:last-child) {
  display: flex !important;
  justify-content: center !important;
  margin-bottom: 0 !important;
}

.auth-modal :deep(.arco-form-item-control) {
  display: flex !important;
  justify-content: center !important;
}

.auth-modal
  :deep(.arco-form-item[field="verificationCode"] .arco-form-item-control) {
  justify-content: flex-start; /* 验证码输入框保持左对齐 */
  width: 100%;
}

.auth-modal :deep(.arco-form-item[field="verificationCode"]) {
  margin-bottom: 24px !important;
}

.auth-modal :deep(.arco-form-item-label-col) {
  justify-content: flex-start; /* 标签文字左对齐 */
  padding-right: 0; /* 移除默认右间距 */
  font-weight: 500;
  color: var(--color-text-2);
}

.auth-modal :deep(.arco-form-item-control-wrapper) {
  display: flex;
  justify-content: center;
}

/* 输入框样式优化 */
.auth-modal :deep(.arco-input),
.auth-modal :deep(.arco-input-password) {
  width: 100%;
  max-width: 100%; /* 控制输入框最大宽度 */
  height: 42px;
  border-radius: 6px;
  transition: all 0.3s;
  border: 1px solid var(--color-border-2);
}

.auth-modal :deep(.arco-input:focus),
.auth-modal :deep(.arco-input-password:focus) {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.verification-container {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

/* 按钮样式优化 */
.form-actions {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-right: 50px;
}

.additional-actions {
  display: flex;
  justify-content: center;
  gap: 16px; /* 添加间距 */
  color: var(--color-text-3);
}

.submit-btn {
  display: block;
  margin: 0 auto;
  width: 200px; /* 固定宽度 */
  height: 42px;
  font-size: 16px;
  border-radius: 6px;
  background-color: #1890ff;
  border-color: #1890ff;
  transition: all 0.3s;
}

.submit-btn:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

:deep(.arco-btn-text) {
  color: #1890ff;
  font-size: 14px;
  padding: 0 8px;
  height: 32px;
  transition: all 0.3s;
}

:deep(.arco-btn-text:hover) {
  color: #40a9ff;
  background-color: rgba(24, 144, 255, 0.05);
}
</style>
