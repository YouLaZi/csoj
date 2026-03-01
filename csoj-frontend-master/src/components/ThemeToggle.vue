<template>
  <div class="theme-toggle">
    <a-tooltip :content="themeTooltip">
      <a-button class="theme-toggle-button" shape="circle" @click="toggleTheme">
        <template #icon>
          <icon-moon-fill v-if="isDarkMode" />
          <icon-sun-fill v-else />
        </template>
      </a-button>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { ThemeManager, ThemeType } from "@/utils/themeManager";
import { IconMoonFill, IconSunFill } from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";

const { t } = useI18n();

// 当前主题状态
const currentTheme = ref<ThemeType>(ThemeType.LIGHT);

// 计算是否为深色模式
const isDarkMode = computed(() => {
  return currentTheme.value === ThemeType.DARK;
});

// 计算提示文本
const themeTooltip = computed(() => {
  return isDarkMode.value ? t("theme.toggleLight") : t("theme.toggleDark");
});

// 初始化主题
onMounted(() => {
  // ThemeManager.init() 已在 App.vue 中调用，这里只获取当前主题
  currentTheme.value = ThemeManager.getTheme();

  // 监听主题变化事件
  window.addEventListener("themeChange", (event: CustomEvent) => {
    currentTheme.value = event.detail.theme;
  });
});

// 切换主题
const toggleTheme = () => {
  try {
    currentTheme.value = ThemeManager.toggleTheme();
    // 触发自定义事件，通知其他组件主题已更改
    const themeChangeEvent = new CustomEvent("themeChange", {
      detail: { theme: currentTheme.value },
    });
    window.dispatchEvent(themeChangeEvent);

    // 保存到用户偏好设置
    const userPreferences = localStorage.getItem("user_preferences");
    if (userPreferences) {
      const preferences = JSON.parse(userPreferences);
      preferences.theme = currentTheme.value;
      localStorage.setItem("user_preferences", JSON.stringify(preferences));
    }
  } catch (error) {
    console.error("切换主题失败", error);
    Message.error("切换主题失败");
  }
};

// 导出方法供其他组件调用
defineExpose({
  setTheme: (theme: ThemeType) => {
    ThemeManager.setTheme(theme);
    currentTheme.value = theme;
  },
});
</script>

<style scoped>
.theme-toggle {
  display: inline-flex;
  align-items: center;
}

.theme-toggle-button {
  background-color: transparent;
  color: var(--text-color-primary);
  border: 1px solid var(--border-color);
  transition: all 0.3s;
}

.theme-toggle-button:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
}
</style>
