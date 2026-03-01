<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 装饰背景 -->
      <div class="decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>

      <!-- 登录卡片 -->
      <div class="login-card">
        <!-- 头部 -->
        <div class="login-header">
          <div class="logo-wrapper">
            <img src="@/assets/logo.png" class="logo" alt="Logo" />
          </div>
          <h1 class="title">{{ $t("app.name") }}</h1>
          <p class="subtitle">{{ $t("app.title") }}</p>
        </div>

        <!-- 表单 -->
        <a-form
          layout="vertical"
          :model="form"
          @submit="handleSubmit"
          class="login-form"
        >
          <a-form-item
            field="userAccount"
            :label="$t('form.userAccount')"
            hide-label
          >
            <a-input
              v-model="form.userAccount"
              :placeholder="$t('form.pleaseEnterAccount')"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-user />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item
            field="userPassword"
            :label="$t('form.password')"
            hide-label
          >
            <a-input-password
              v-model="form.userPassword"
              :placeholder="$t('form.pleaseEnterPassword')"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item
            field="verificationCode"
            :label="$t('form.verificationCode')"
            hide-label
          >
            <div class="verification-row">
              <a-input
                v-model="form.verificationCode"
                :placeholder="$t('form.pleaseEnterCode')"
                size="large"
                allow-clear
              >
                <template #prefix>
                  <icon-safe />
                </template>
              </a-input>
              <VerificationCode
                ref="verificationCodeRef"
                class="verification-code"
              />
            </div>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              long
              class="submit-btn"
            >
              {{ $t("user.login") }}
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 底部链接 -->
        <div class="login-footer">
          <a-button type="text" @click="toForgotPassword" class="footer-link">
            {{ $t("form.forgotPassword") }}
          </a-button>
          <span class="divider">|</span>
          <a-button type="text" @click="toRegister" class="footer-link">
            {{ $t("form.noAccount") }}{{ $t("form.goRegister") }}
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { UserControllerService, UserLoginRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { useI18n } from "vue-i18n";
import VerificationCode from "@/components/VerificationCode.vue";
import { IconUser, IconLock, IconSafe } from "@arco-design/web-vue/es/icon";

const { t } = useI18n();

const verificationCodeRef = ref(null);

const form = reactive({
  userAccount: "",
  userPassword: "",
  verificationCode: "",
});

const router = useRouter();
const store = useStore();

const toForgotPassword = () => {
  router.push({ path: "/user/forgot-password" });
};

const toRegister = () => {
  router.push({ path: "/user/register" });
};

const handleSubmit = async () => {
  if (!form.verificationCode) {
    message.error(t("form.pleaseEnterCode"));
    return;
  }

  if (
    verificationCodeRef.value &&
    verificationCodeRef.value.code &&
    verificationCodeRef.value.code.value &&
    form.verificationCode.toLowerCase() !==
      verificationCodeRef.value.code.value.toLowerCase()
  ) {
    message.error(t("form.codeError"));
    verificationCodeRef.value.refreshCode();
    form.verificationCode = "";
    return;
  }

  const loginRequest = {
    userAccount: form.userAccount,
    userPassword: form.userPassword,
  } as UserLoginRequest;

  try {
    const loadingMessage = message.loading({
      content: t("form.logining"),
      duration: 0,
    });

    const res = await UserControllerService.userLoginUsingPost(loginRequest);
    loadingMessage.close();

    if (res.code === 0) {
      if (res.data.token) {
        await store.dispatch("user/setToken", res.data.token);
      }
      await store.dispatch("user/getLoginUser");
      router.push({ path: "/", replace: true });
    }
  } catch (error) {
    console.error("登录请求错误:", error);
    if (error.message === "Network Error") {
      message.error(t("error.networkError"));
    } else {
      message.error(
        t("user.loginFailed") + "，" + (error.message || t("message.failed"))
      );
    }
    if (verificationCodeRef.value) {
      verificationCodeRef.value.refreshCode();
      form.verificationCode = "";
    }
  }
};
</script>

<style scoped>
/* ========================================
   登录页面 - 简约优雅
   ======================================== */

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    135deg,
    var(--bg-color) 0%,
    var(--bg-color-tertiary) 100%
  );
  padding: var(--spacing-xl);
  position: relative;
  overflow: hidden;
}

.login-container {
  width: 100%;
  max-width: 420px;
  position: relative;
}

/* ========================================
   装饰元素
   ======================================== */

.decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.4;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(
    circle,
    var(--primary-lighter-color) 0%,
    transparent 70%
  );
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(
    circle,
    var(--primary-lighter-color) 0%,
    transparent 70%
  );
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(
    circle,
    var(--primary-lighter-color) 0%,
    transparent 70%
  );
  top: 50%;
  left: 10%;
}

/* ========================================
   登录卡片
   ======================================== */

.login-card {
  background: var(--bg-color-secondary);
  border-radius: var(--radius-xl);
  padding: var(--spacing-3xl) var(--spacing-2xl);
  box-shadow: var(--shadow-xl);
  position: relative;
  z-index: 1;
}

/* ========================================
   头部
   ======================================== */

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.logo-wrapper {
  margin-bottom: var(--spacing-md);
}

.logo {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  transition: transform var(--transition-base);
}

.logo:hover {
  transform: scale(1.05);
}

.title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-xs) 0;
  letter-spacing: 2px;
}

.subtitle {
  font-size: var(--font-size-base);
  color: var(--text-color-secondary);
  margin: 0;
}

/* ========================================
   表单
   ======================================== */

.login-form {
  margin-bottom: var(--spacing-lg);
}

.login-form :deep(.arco-form-item) {
  margin-bottom: var(--spacing-lg);
}

.login-form :deep(.arco-input-wrapper),
.login-form :deep(.arco-input-password) {
  background-color: var(--bg-color-tertiary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.login-form :deep(.arco-input-wrapper:hover),
.login-form :deep(.arco-input-password:hover) {
  border-color: var(--border-color);
}

.login-form :deep(.arco-input-wrapper:focus-within),
.login-form :deep(.arco-input-password:focus-within) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--focus-ring-color);
}

.login-form :deep(.arco-input) {
  background: transparent;
}

.login-form :deep(.arco-input::placeholder) {
  color: var(--text-color-placeholder);
}

/* 验证码行 */
.verification-row {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.verification-row :deep(.arco-input-wrapper) {
  flex: 1;
}

.verification-code {
  flex-shrink: 0;
  cursor: pointer;
  border-radius: var(--radius-md);
  overflow: hidden;
}

/* 提交按钮 */
.submit-btn {
  height: 48px;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  border-radius: var(--radius-md);
  background: linear-gradient(
    135deg,
    var(--primary-color),
    var(--primary-hover-color)
  );
  border: none;
  transition: all var(--transition-base);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.submit-btn:active {
  transform: translateY(0);
}

/* ========================================
   底部链接
   ======================================== */

.login-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border-color-light);
}

.footer-link {
  color: var(--text-color-secondary);
  font-size: var(--font-size-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
}

.footer-link:hover {
  color: var(--primary-color);
  background: transparent;
}

.divider {
  color: var(--border-color);
  font-size: var(--font-size-sm);
}

/* ========================================
   响应式设计
   ======================================== */

@media (max-width: 480px) {
  .login-page {
    padding: var(--spacing-md);
  }

  .login-card {
    padding: var(--spacing-xl) var(--spacing-lg);
  }

  .title {
    font-size: var(--font-size-2xl);
  }

  .logo {
    width: 56px;
    height: 56px;
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] .login-page {
  background: linear-gradient(
    135deg,
    var(--bg-color) 0%,
    var(--bg-color-secondary) 100%
  );
}

[data-theme="dark"] .login-card {
  background: var(--bg-color-secondary);
  box-shadow: var(--shadow-xl);
}

[data-theme="dark"] .circle-1,
[data-theme="dark"] .circle-2,
[data-theme="dark"] .circle-3 {
  background: radial-gradient(
    circle,
    var(--primary-light-color) 0%,
    transparent 70%
  );
  opacity: 0.2;
}

[data-theme="dark"] .login-form :deep(.arco-input-wrapper),
[data-theme="dark"] .login-form :deep(.arco-input-password) {
  background-color: var(--bg-color-tertiary);
}

[data-theme="dark"] .submit-btn {
  background: linear-gradient(
    135deg,
    var(--primary-color),
    var(--primary-hover-color)
  );
}
</style>
