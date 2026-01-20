<template>
  <div class="page-container">
    <div id="forgotPasswordView">
      <div class="header">
        <div class="logo-container">
          <img src="@/assets/logo.png" class="logo" />
          <h2>智码 CSOJ</h2>
        </div>
        <div class="decoration"></div>
      </div>
      <a-form
        label-align="left"
        auto-label-width
        :model="form"
        @submit="handleSubmit"
      >
        <a-form-item field="email" label="注册邮箱">
          <a-input v-model="form.email" placeholder="请输入注册时使用的邮箱" />
        </a-form-item>
        <a-form-item>
          <div class="form-actions">
            <a-button type="primary" html-type="submit" class="submit-btn">
              发送重置链接
            </a-button>
            <a-button @click="toLogin" type="text">返回登录</a-button>
          </div>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { UserControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

const form = reactive({
  email: "",
});

const router = useRouter();

const toLogin = () => {
  router.push({
    path: "/user/login",
  });
};

const handleSubmit = async () => {
  if (!form.email) {
    message.error("请输入邮箱地址");
    return;
  }
  // 邮箱格式校验 (简单示例)
  const emailRegex = /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/;
  if (!emailRegex.test(form.email)) {
    message.error("请输入有效的邮箱地址");
    return;
  }

  try {
    const loadingMessage = message.loading({
      content: "正在发送邮件...",
      duration: 0,
    });
    // 调用后端API发送密码重置邮件
    const res = await UserControllerService.forgotPasswordUsingPost({
      email: form.email,
    });
    loadingMessage.close();

    if (res.code === 0) {
      message.success(
        "密码重置链接已发送至您的邮箱，请注意查收。如果没有收到，请检查垃圾邮件。"
      );
      // 可以选择跳转到登录页或提示用户检查邮箱
      // router.push("/user/login");
    } else {
      message.error("发送失败，" + res.message);
    }
  } catch (error) {
    console.error("发送密码重置邮件错误:", error);
    message.error("发送失败，请稍后重试或联系管理员");
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

#forgotPasswordView {
  max-width: 480px;
  width: 100%;
  margin: 0 auto;
  padding: 32px 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  border-top: 4px solid #34495e;
}

.header {
  margin-bottom: 32px;
  text-align: center;
  position: relative;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.logo {
  width: 48px;
  height: 48px;
  margin-right: 12px;
}

.header h2 {
  font-size: 24px;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 0;
  letter-spacing: 1px;
}

.decoration {
  width: 60px;
  height: 3px;
  background-color: #34495e;
  margin: 0 auto;
  position: relative;
  bottom: -4px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.submit-btn {
  flex-grow: 1;
  margin-right: 16px;
}
</style>
