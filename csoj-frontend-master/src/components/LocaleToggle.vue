<template>
  <div class="locale-toggle">
    <a-dropdown trigger="click" @select="handleLocaleChange">
      <a-button class="locale-toggle-button" shape="circle">
        <template #icon>
          <span class="locale-icon">{{ currentLocaleIcon }}</span>
        </template>
      </a-button>
      <template #content>
        <a-doption
          v-for="locale in locales"
          :key="locale.value"
          :value="locale.value"
          :class="{ active: currentLocale === locale.value }"
        >
          <span class="locale-option">
            <span class="locale-option-icon">{{ locale.icon }}</span>
            <span class="locale-option-label">{{ locale.label }}</span>
          </span>
        </a-doption>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { LocaleManager, LOCALES, LocaleType } from "@/utils/localeManager";

// 使用 i18n
const { locale } = useI18n();

// 当前语言
const currentLocale = ref<LocaleType>("zh-CN");

// 语言列表
const locales = LOCALES;

// 当前语言图标
const currentLocaleIcon = computed(() => {
  const config = LOCALES.find((l) => l.value === currentLocale.value);
  return config ? config.icon : "🇨🇳";
});

// 初始化
onMounted(() => {
  // LocaleManager.init() 已在 main.ts 中调用，这里只获取当前语言
  currentLocale.value = LocaleManager.getLocale();

  // 监听语言变化事件
  window.addEventListener("localeChange", (event: CustomEvent) => {
    currentLocale.value = event.detail.locale;
    locale.value = event.detail.locale;
  });
});

// 切换语言
const handleLocaleChange = (
  value: string | number | boolean | Record<string, any> | undefined
) => {
  if (typeof value === "string") {
    LocaleManager.setLocale(value as LocaleType);
    locale.value = value;
    currentLocale.value = value as LocaleType;

    // 保存到用户偏好设置
    const userPreferences = localStorage.getItem("user_preferences");
    if (userPreferences) {
      const preferences = JSON.parse(userPreferences);
      preferences.locale = value;
      localStorage.setItem("user_preferences", JSON.stringify(preferences));
    }
  }
};
</script>

<style scoped>
.locale-toggle {
  display: inline-flex;
  align-items: center;
}

.locale-toggle-button {
  background-color: transparent;
  color: var(--text-color-primary);
  border: 1px solid var(--border-color);
  transition: all 0.3s;
}

.locale-toggle-button:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.locale-icon {
  font-size: 16px;
}

.locale-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.locale-option-icon {
  font-size: 16px;
}

.locale-option-label {
  font-size: 14px;
}

:deep(.arco-dropdown-option.active) {
  background-color: var(--color-fill-2);
  color: var(--primary-color);
}
</style>
