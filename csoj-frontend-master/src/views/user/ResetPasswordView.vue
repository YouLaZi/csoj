<template>
  <div class="reset-password-container">
    <div class="reset-form-wrapper">
      <h2>重置密码</h2>
      <form
        ref="passwordForm"
        @submit.prevent="handleSubmit"
        class="reset-form"
      >
        <div class="form-item">
          <label for="newPassword">新密码</label>
          <a-input-password
            id="newPassword"
            name="newPassword"
            placeholder="请输入新密码（至少8位）"
            @change="logFormState"
          />
        </div>

        <div class="form-item">
          <label for="confirmPassword">确认密码</label>
          <a-input-password
            id="confirmPassword"
            name="confirmPassword"
            placeholder="请再次输入新密码"
            @change="logFormState"
          />
        </div>

        <div class="form-item">
          <a-button
            type="primary"
            html-type="submit"
            class="reset-button"
            :loading="loading"
          >
            重置密码
          </a-button>
        </div>

        <div class="additional-actions">
          <router-link to="/user/login">返回登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { useRoute, useRouter } from "vue-router";
import {
  UserControllerService,
  ResetPasswordRequest,
} from "../../../generated";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const token = ref("");
const passwordForm = ref<HTMLFormElement | null>(null);

// 添加日志记录表单状态变化
const logFormState = () => {
  if (!passwordForm.value) return;

  const formData = new FormData(passwordForm.value);
  console.log("表单状态变化", {
    newPassword: formData.get("newPassword"),
    confirmPassword: formData.get("confirmPassword"),
  });
};

onMounted(() => {
  token.value = route.query.token as string;
  if (!token.value) {
    Message.error("无效的密码重置链接");
    router.push("/user/login");
  }
});

const handleSubmit = async (event: Event) => {
  event.preventDefault();

  if (!passwordForm.value) {
    console.error("表单元素未找到");
    Message.error("系统错误，请刷新页面重试");
    return;
  }

  try {
    const formData = new FormData(passwordForm.value);
    const newPassword = formData.get("newPassword") as string;
    const confirmPassword = formData.get("confirmPassword") as string;

    console.log("表单提交数据", {
      newPassword,
      confirmPassword,
    });

    // 基本验证
    if (!newPassword) {
      Message.error("请输入新密码");
      return;
    }

    if (newPassword.length < 8) {
      Message.error("密码长度不能少于8位");
      return;
    }

    if (!confirmPassword) {
      Message.error("请确认新密码");
      return;
    }

    if (newPassword !== confirmPassword) {
      Message.error("两次输入的密码不一致");
      return;
    }

    if (!token.value) {
      Message.error("无效的密码重置链接");
      return;
    }

    console.log("准备发送请求", {
      token: token.value,
      newPassword,
    });

    loading.value = true;

    const resetPasswordRequest: ResetPasswordRequest = {
      token: token.value,
      newPassword: newPassword,
    };

    const response = await UserControllerService.resetPasswordUsingPost(
      resetPasswordRequest
    );

    console.log("API 响应:", response);

    if (response.code === 0) {
      Message.success("密码重置成功，请使用新密码登录");
      router.push("/user/login");
    } else {
      Message.error(response.message || "密码重置失败");
    }
  } catch (error: any) {
    console.error("处理提交时出错:", error);
    Message.error(error.body?.message || "密码重置失败，请稍后再试");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.reset-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--color-fill-1);
}

.reset-form-wrapper {
  width: 400px;
  padding: 40px;
  background: var(--color-bg-1);
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: rgba(0, 0, 0, 0.85);
}

.reset-form {
  display: flex;
  flex-direction: column;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  color: rgba(0, 0, 0, 0.85);
}

.reset-button {
  width: 100%;
}

.additional-actions {
  margin-top: 16px;
  text-align: center;
}
</style>
