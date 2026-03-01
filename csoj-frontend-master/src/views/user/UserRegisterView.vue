<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 装饰背景 -->
      <div class="decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
      </div>

      <!-- 注册卡片 -->
      <div class="register-card">
        <!-- 头部 -->
        <div class="register-header">
          <h1 class="title">{{ $t("register.createAccount") }}</h1>
          <p class="subtitle">{{ $t("register.subtitle") }}</p>
        </div>

        <!-- 表单 -->
        <a-form
          layout="vertical"
          :model="form"
          :rules="rules"
          @submit="handleSubmit"
          class="register-form"
        >
          <a-form-item
            field="userAccount"
            :label="$t('form.userAccount')"
            hide-label
          >
            <a-input
              v-model="form.userAccount"
              :placeholder="$t('register.accountPlaceholder')"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-user />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item field="userName" :label="$t('form.username')" hide-label>
            <a-input
              v-model="form.userName"
              :placeholder="$t('form.pleaseEnterUsername')"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-idcard />
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
              :placeholder="$t('register.passwordPlaceholder')"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-lock />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item
            field="checkPassword"
            :label="$t('form.confirmPassword')"
            hide-label
          >
            <a-input-password
              v-model="form.checkPassword"
              :placeholder="$t('form.pleaseEnterAgainPassword')"
              size="large"
              allow-clear
            >
              <template #prefix>
                <icon-safe />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              long
              class="submit-btn"
            >
              {{ $t("user.register") }}
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 底部链接 -->
        <div class="register-footer">
          <span class="footer-text">{{ $t("form.hasAccount") }}</span>
          <a-button type="text" @click="toLogin" class="footer-link">
            {{ $t("register.loginNow") }}
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { UserControllerService, UserRegisterRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import {
  IconUser,
  IconIdcard,
  IconLock,
  IconSafe,
} from "@arco-design/web-vue/es/icon";

const { t } = useI18n();

const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  userName: "",
} as UserRegisterRequest & { checkPassword: string });

const rules = {
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
        return value === form.userPassword;
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

const toLogin = () => {
  router.push({ path: "/user/login" });
};

const handleSubmit = async () => {
  const registerRequest: UserRegisterRequest = {
    userAccount: form.userAccount,
    userPassword: form.userPassword,
    checkPassword: form.checkPassword,
    userName: form.userName,
  };
  const res = await UserControllerService.userRegisterUsingPost(
    registerRequest
  );
  if (res.code === 0) {
    message.success(t("user.registerSuccess"));
    router.push({ path: "/user/login" });
  } else {
    message.error(t("user.registerFailed") + "，" + res.message);
  }
};
</script>

<style scoped>
/* ========================================
   注册页面 - 简约优雅
   ======================================== */

.register-page {
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

.register-container {
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
  left: -100px;
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
  right: -50px;
}

/* ========================================
   注册卡片
   ======================================== */

.register-card {
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

.register-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-sm) 0;
  letter-spacing: 1px;
}

.subtitle {
  font-size: var(--font-size-base);
  color: var(--text-color-secondary);
  margin: 0;
}

/* ========================================
   表单
   ======================================== */

.register-form {
  margin-bottom: var(--spacing-lg);
}

.register-form :deep(.arco-form-item) {
  margin-bottom: var(--spacing-lg);
}

.register-form :deep(.arco-input-wrapper),
.register-form :deep(.arco-input-password) {
  background-color: var(--bg-color-tertiary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.register-form :deep(.arco-input-wrapper:hover),
.register-form :deep(.arco-input-password:hover) {
  border-color: var(--border-color);
}

.register-form :deep(.arco-input-wrapper:focus-within),
.register-form :deep(.arco-input-password:focus-within) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--focus-ring-color);
}

.register-form :deep(.arco-input) {
  background: transparent;
}

.register-form :deep(.arco-input::placeholder) {
  color: var(--text-color-placeholder);
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

.register-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border-color-light);
}

.footer-text {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.footer-link {
  color: var(--primary-color);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  padding: 0;
}

.footer-link:hover {
  background: transparent;
}

/* ========================================
   响应式设计
   ======================================== */

@media (max-width: 480px) {
  .register-page {
    padding: var(--spacing-md);
  }

  .register-card {
    padding: var(--spacing-xl) var(--spacing-lg);
  }

  .title {
    font-size: var(--font-size-2xl);
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] .register-page {
  background: linear-gradient(
    135deg,
    var(--bg-color) 0%,
    var(--bg-color-secondary) 100%
  );
}

[data-theme="dark"] .register-card {
  background: var(--bg-color-secondary);
  box-shadow: var(--shadow-xl);
}

[data-theme="dark"] .circle-1,
[data-theme="dark"] .circle-2 {
  background: radial-gradient(
    circle,
    var(--primary-light-color) 0%,
    transparent 70%
  );
  opacity: 0.2;
}

[data-theme="dark"] .register-form :deep(.arco-input-wrapper),
[data-theme="dark"] .register-form :deep(.arco-input-password) {
  background-color: var(--bg-color-tertiary);
}
</style>
