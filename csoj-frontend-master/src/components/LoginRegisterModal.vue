<template>
  <a-modal
    v-model:visible="visible"
    :title="isLogin ? $t('user.login') : $t('user.register')"
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
      <a-form-item field="userAccount" :label="$t('form.userAccount')">
        <a-input
          v-model="loginForm.userAccount"
          :placeholder="$t('form.pleaseEnterAccount')"
        />
      </a-form-item>
      <a-form-item
        field="userPassword"
        :tooltip="$t('form.passwordMinLength')"
        :label="$t('form.password')"
      >
        <a-input-password
          v-model="loginForm.userPassword"
          :placeholder="$t('form.pleaseEnterPassword')"
        />
      </a-form-item>
      <a-form-item
        field="verificationCode"
        :label="$t('form.verificationCode')"
      >
        <div class="verification-container">
          <a-input
            v-model="loginForm.verificationCode"
            :placeholder="$t('form.pleaseEnterCode')"
          />
          <VerificationCode ref="verificationCodeRef" />
        </div>
      </a-form-item>
      <a-form-item>
        <div class="form-actions">
          <a-button type="primary" html-type="submit" class="submit-btn">
            {{ $t("user.login") }}
          </a-button>
        </div>
      </a-form-item>
      <div class="additional-actions">
        <a-button @click="switchMode" type="text"
          >{{ $t("form.noAccount") }}{{ $t("form.goRegister") }}</a-button
        >
        <a-button @click="showForgotPassword" type="text">{{
          $t("form.forgotPassword")
        }}</a-button>
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
      <a-form-item field="userAccount" :label="$t('form.userAccount')">
        <a-input
          v-model="registerForm.userAccount"
          :placeholder="$t('form.pleaseEnterAccount')"
        />
      </a-form-item>
      <a-form-item
        field="userPassword"
        :tooltip="$t('form.passwordMinLength')"
        :label="$t('form.password')"
      >
        <a-input-password
          v-model="registerForm.userPassword"
          :placeholder="$t('form.pleaseEnterPassword')"
        />
      </a-form-item>
      <a-form-item
        field="checkPassword"
        :tooltip="$t('form.pleaseEnterAgainPassword')"
        :label="$t('form.confirmPassword')"
      >
        <a-input-password
          v-model="registerForm.checkPassword"
          :placeholder="$t('form.pleaseEnterAgainPassword')"
        />
      </a-form-item>
      <a-form-item field="userName" :label="$t('form.username')">
        <a-input
          v-model="registerForm.userName"
          :placeholder="$t('form.pleaseEnterUsername')"
        />
      </a-form-item>
      <a-form-item>
        <div class="form-actions">
          <a-button type="primary" html-type="submit" class="submit-btn">
            {{ $t("user.register") }}
          </a-button>
        </div>
      </a-form-item>
      <div class="additional-actions">
        <a-button @click="switchMode" type="text"
          >{{ $t("form.hasAccount") }}{{ $t("form.goLogin") }}</a-button
        >
      </div>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import {
  UserControllerService,
  UserLoginRequest,
  UserRegisterRequest,
} from "../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { useI18n } from "vue-i18n";
import VerificationCode from "@/components/VerificationCode.vue";

const { t } = useI18n();

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
    { required: true, message: t("form.pleaseEnterAccount") },
    { minLength: 4, message: t("form.accountMinLength") },
  ],
  userPassword: [
    { required: true, message: t("form.pleaseEnterPassword") },
    { minLength: 8, message: t("form.passwordLengthMin") },
  ],
  checkPassword: [
    { required: true, message: t("form.pleaseEnterAgainPassword") },
    {
      validator: (value: string) => {
        return value === registerForm.userPassword;
      },
      message: t("form.passwordNotMatch"),
    },
  ],
  userName: [
    { required: true, message: t("form.pleaseEnterUsername") },
    { minLength: 2, message: t("form.usernameMinLength") },
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
    message.error(t("form.pleaseEnterCode"));
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
    message.error(t("form.codeError"));
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
      content: t("form.logining"),
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
      message.success(t("user.loginSuccess"));
    } else {
      message.error(t("user.loginFailed") + "，" + res.message);
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
      message.error(t("error.networkError"));
    } else {
      message.error(
        t("user.loginFailed") + "，" + (error.message || t("message.failed"))
      );
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
    message.success(t("user.registerSuccess"));
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
    message.error(t("user.registerFailed") + "，" + res.message);
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
