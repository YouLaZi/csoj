<template>
  <div class="page-container">
    <div id="userLoginView">
      <div class="login-header">
        <div class="logo-container">
          <img src="@/assets/logo.png" class="logo" />
          <h2>智码 CSOJ</h2>
        </div>
        <div class="login-decoration"></div>
      </div>
      <a-form
        label-align="left"
        auto-label-width
        :model="form"
        @submit="handleSubmit"
      >
        <a-form-item field="userAccount" label="账号">
          <a-input v-model="form.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item
          field="userPassword"
          tooltip="密码不少于 8 位"
          label="密码"
        >
          <a-input-password
            v-model="form.userPassword"
            placeholder="请输入密码"
          />
        </a-form-item>
        <a-form-item field="verificationCode" label="验证码">
          <div class="verification-container">
            <a-input
              v-model="form.verificationCode"
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
            <div class="additional-actions">
              <a-button @click="toForgotPassword" type="text"
                >忘记密码？</a-button
              >
              <a-button @click="toRegister" type="text"
                >没有账号？去注册</a-button
              >
            </div>
          </div>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { UserControllerService, UserLoginRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import VerificationCode from "@/components/VerificationCode.vue";

// 验证码组件引用
const verificationCodeRef = ref(null);

/**
 * 表单信息
 */
const form = reactive({
  userAccount: "",
  userPassword: "",
  verificationCode: "",
});

const router = useRouter();
const store = useStore();

/**
 * 跳转到忘记密码页
 */
const toForgotPassword = () => {
  router.push({
    path: "/user/forgot-password",
  });
};

/**
 * 跳转到注册页
 */
const toRegister = () => {
  router.push({
    path: "/user/register",
  });
};

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 验证码校验
  if (!form.verificationCode) {
    message.error("请输入验证码");
    return;
  }

  // 验证码校验
  if (
    verificationCodeRef.value &&
    verificationCodeRef.value.code &&
    verificationCodeRef.value.code.value &&
    form.verificationCode.toLowerCase() !==
      verificationCodeRef.value.code.value.toLowerCase()
  ) {
    message.error("验证码错误");
    verificationCodeRef.value.refreshCode();
    form.verificationCode = "";
    return;
  }

  const loginRequest = {
    userAccount: form.userAccount,
    userPassword: form.userPassword,
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

    // 登录成功，跳转到主页
    if (res.code === 0) {
      // 先设置token，再获取用户信息
      if (res.data.token) {
        await store.dispatch("user/setToken", res.data.token);
      }
      await store.dispatch("user/getLoginUser");
      router.push({
        path: "/",
        replace: true,
      });
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
      form.verificationCode = "";
    }
  }
};
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f5f9;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><rect x="0" y="0" width="100" height="100" fill="none" stroke="%23e1e8ed" stroke-width="0.5"/></svg>');
}

#userLoginView {
  max-width: 480px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 48px;
  background-color: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  border-top: 5px solid #34495e;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

#userLoginView:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.login-header {
  margin-bottom: 32px;
  text-align: center;
  position: relative;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.logo {
  width: 64px;
  height: 64px;
  margin-right: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.login-header h2 {
  font-size: 24px;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 0;
  letter-spacing: 1px;
}

.login-decoration {
  width: 60px;
  height: 3px;
  background-color: #34495e;
  margin: 0 auto;
  position: relative;
}

.login-decoration::before,
.login-decoration::after {
  content: "";
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #34495e;
  top: -2.5px;
}

.login-decoration::before {
  left: -10px;
}

.login-decoration::after {
  right: -10px;
}

.verification-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.verification-container .arco-input-wrapper {
  flex: 1;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
}

.additional-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.submit-btn {
  width: 130px;
  height: 40px;
  background-color: #34495e;
  border-color: #34495e;
  border-radius: 6px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background-color: #2c3e50;
  border-color: #2c3e50;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 73, 94, 0.3);
}

/* 书香风格装饰 */
#userLoginView::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200"><path d="M40,40 L160,40 L160,160 L40,160 Z" fill="none" stroke="%23e1e8ed" stroke-width="0.5" stroke-dasharray="2"/></svg>');
  opacity: 0.3;
  pointer-events: none;
}
</style>
