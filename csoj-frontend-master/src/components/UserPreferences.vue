<template>
  <div class="user-preferences">
    <a-card class="preferences-card" :bordered="false">
      <template #title>
        <div class="card-title"><icon-settings /> 个性化设置</div>
      </template>

      <a-form :model="preferences" layout="vertical">
        <!-- 主题设置 -->
        <a-form-item field="theme" label="界面主题">
          <a-radio-group v-model="preferences.theme" type="button">
            <a-radio value="light">浅色模式</a-radio>
            <a-radio value="dark">深色模式</a-radio>
            <a-radio value="system">跟随系统</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 代码编辑器设置 -->
        <a-form-item field="codeEditorTheme" label="代码编辑器主题">
          <a-select v-model="preferences.codeEditorTheme">
            <a-option value="vs">Visual Studio</a-option>
            <a-option value="vs-dark">Visual Studio Dark</a-option>
            <a-option value="hc-black">High Contrast Black</a-option>
          </a-select>
        </a-form-item>

        <a-form-item field="codeEditorFontSize" label="代码编辑器字体大小">
          <a-input-number
            v-model="preferences.codeEditorFontSize"
            :min="12"
            :max="24"
          />
        </a-form-item>

        <!-- 语言偏好 -->
        <a-form-item field="preferredLanguage" label="默认编程语言">
          <a-select v-model="preferences.preferredLanguage">
            <a-option value="java">Java</a-option>
            <a-option value="cpp">C++</a-option>
            <a-option value="python">Python</a-option>
            <a-option value="javascript">JavaScript</a-option>
            <a-option value="csharp">C#</a-option>
            <a-option value="ruby">Ruby</a-option>
            <a-option value="swift">Swift</a-option>
            <a-option value="go">Go</a-option>
            <a-option value="ruby">Ruby</a-option>
          </a-select>
        </a-form-item>

        <!-- 界面布局 -->
        <a-form-item field="layout" label="界面布局">
          <a-radio-group v-model="preferences.layout" type="button">
            <a-radio value="default">默认</a-radio>
            <a-radio value="compact">紧凑</a-radio>
            <a-radio value="comfortable">舒适</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 通知设置 -->
        <a-form-item field="notifications" label="通知设置">
          <a-space direction="vertical">
            <a-checkbox v-model="preferences.notifications.newQuestions"
              >新题目通知</a-checkbox
            >
            <a-checkbox v-model="preferences.notifications.submissionResults"
              >提交结果通知</a-checkbox
            >
            <a-checkbox v-model="preferences.notifications.systemMessages"
              >系统消息通知</a-checkbox
            >
          </a-space>
        </a-form-item>

        <!-- 保存按钮 -->
        <a-form-item>
          <a-button type="primary" @click="savePreferences" :loading="saving">
            保存设置
          </a-button>
          <a-button style="margin-left: 8px" @click="resetPreferences">
            恢复默认
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from "vue";
import { IconSettings } from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";
import { ThemeManager, ThemeType } from "@/utils/themeManager";

// 保存状态
const saving = ref(false);

// 用户偏好设置
const preferences = reactive({
  theme: "light",
  codeEditorTheme: "vs",
  codeEditorFontSize: 14,
  preferredLanguage: "java",
  layout: "default",
  notifications: {
    newQuestions: true,
    submissionResults: true,
    systemMessages: true,
  },
});

// 默认偏好设置
const defaultPreferences = {
  theme: "system",
  codeEditorTheme: "vs",
  codeEditorFontSize: 14,
  preferredLanguage: "java",
  layout: "default",
  notifications: {
    newQuestions: true,
    submissionResults: true,
    systemMessages: true,
  },
};

// 加载用户偏好设置
const loadPreferences = () => {
  const savedPreferences = localStorage.getItem("user_preferences");
  if (savedPreferences) {
    try {
      const parsedPreferences = JSON.parse(savedPreferences);
      Object.assign(preferences, parsedPreferences);
    } catch (error) {
      console.error("解析用户偏好设置失败", error);
    }
  }
};

// 保存用户偏好设置
const savePreferences = async () => {
  saving.value = true;
  try {
    // 保存到本地存储
    localStorage.setItem("user_preferences", JSON.stringify(preferences));

    // 应用主题设置
    if (preferences.theme === "system") {
      // 跟随系统
      const prefersDark = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;
      ThemeManager.setTheme(prefersDark ? ThemeType.DARK : ThemeType.LIGHT);
    } else {
      // 手动设置
      ThemeManager.setTheme(preferences.theme as ThemeType);
    }

    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 500));

    Message.success("偏好设置已保存");
  } catch (error) {
    console.error("保存用户偏好设置失败", error);
    Message.error("保存偏好设置失败");
  } finally {
    saving.value = false;
  }
};

// 重置为默认设置
const resetPreferences = () => {
  Object.assign(preferences, defaultPreferences);
  savePreferences();
  Message.info("已恢复默认设置");
};

// 监听主题变化
watch(
  () => preferences.theme,
  (newTheme) => {
    if (newTheme === "system") {
      const prefersDark = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;
      ThemeManager.setTheme(prefersDark ? ThemeType.DARK : ThemeType.LIGHT);
    } else {
      ThemeManager.setTheme(newTheme as ThemeType);
    }

    // 触发主题变化事件，通知其他组件
    const themeChangeEvent = new CustomEvent("themeChange", {
      detail: { theme: newTheme },
    });
    window.dispatchEvent(themeChangeEvent);
  }
);

onMounted(() => {
  loadPreferences();

  // 如果设置为跟随系统，则监听系统主题变化
  if (preferences.theme === "system") {
    window
      .matchMedia("(prefers-color-scheme: dark)")
      .addEventListener("change", (e) => {
        const newTheme = e.matches ? ThemeType.DARK : ThemeType.LIGHT;
        ThemeManager.setTheme(newTheme);
      });
  }

  // 监听来自ThemeToggle的主题变化事件
  window.addEventListener("themeChange", (event: CustomEvent) => {
    if (event.detail.theme && event.detail.theme !== preferences.theme) {
      preferences.theme = event.detail.theme;
    }
  });
});
</script>

<style scoped>
.user-preferences {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto 24px;
  padding: 0 16px;
}

.preferences-card {
  background-color: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--box-shadow);
  transition: box-shadow 0.3s;
  width: 100%;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color-primary);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .arco-form-item {
    margin-bottom: 16px;
  }
}
</style>
