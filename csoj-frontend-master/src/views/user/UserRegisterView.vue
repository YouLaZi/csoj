<template>
  <div class="page-container">
    <div id="userRegisterView">
      <div class="register-header">
        <h2>用户注册</h2>
        <div class="register-decoration"></div>
      </div>
      <a-form
        label-align="left"
        auto-label-width
        :model="form"
        :rules="rules"
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
        <a-form-item
          field="checkPassword"
          tooltip="请再次输入密码"
          label="确认密码"
        >
          <a-input-password
            v-model="form.checkPassword"
            placeholder="请再次输入密码"
          />
        </a-form-item>
        <a-form-item field="userName" label="用户名">
          <a-input v-model="form.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item>
          <div class="form-actions">
            <a-button type="primary" html-type="submit" class="submit-btn">
              注册
            </a-button>
            <a-button @click="toLogin" type="text">已有账号？去登录</a-button>
          </div>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { UserControllerService, UserRegisterRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

/**
 * 表单信息
 */
const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  userName: "",
} as UserRegisterRequest & { checkPassword: string });

/**
 * 表单校验规则
 */
const rules = {
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
        return value === form.userPassword;
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

/**
 * 跳转到登录页
 */
const toLogin = () => {
  router.push({
    path: "/user/login",
  });
};

/**
 * 提交表单
 */
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
    message.success("注册成功");
    // 跳转到登录页
    router.push({
      path: "/user/login",
    });
  } else {
    message.error("注册失败，" + res.message);
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

#userRegisterView {
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

.register-header {
  margin-bottom: 32px;
  text-align: center;
  position: relative;
}

.register-header h2 {
  font-size: 24px;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.register-decoration {
  width: 60px;
  height: 3px;
  background-color: #34495e;
  margin: 0 auto;
  position: relative;
}

.register-decoration::before,
.register-decoration::after {
  content: "";
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #34495e;
  top: -2.5px;
}

.register-decoration::before {
  left: -10px;
}

.register-decoration::after {
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
  align-items: center;
  margin-top: 8px;
}

.submit-btn {
  width: 120px;
  background-color: #34495e;
  border-color: #34495e;
}

.submit-btn:hover {
  background-color: #2c3e50;
  border-color: #2c3e50;
}

/* 书香风格装饰 */
#userRegisterView::before {
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
