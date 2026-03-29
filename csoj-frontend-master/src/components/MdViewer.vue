<template>
  <div :class="viewerThemeClass">
    <Viewer :value="value" :plugins="plugins" />
  </div>
</template>

<script setup lang="ts">
import gfm from "@bytemd/plugin-gfm";
import highlight from "@bytemd/plugin-highlight";
import { Viewer } from "@bytemd/vue-next";
import {
  withDefaults,
  ref, // Added
  computed, // Added
  onMounted, // Added
  onUnmounted, // Added
} from "vue";
import { ThemeManager, ThemeType } from "@/utils/themeManager"; // Added

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
}

// Theme management
const currentTheme = ref<ThemeType>(ThemeManager.getTheme());
const viewerThemeClass = computed(() => {
  // This class 'bytemd-dark-mode' is an example.
  // Actual class and CSS might need to be specific to ByteMD's theming for dark mode.
  return currentTheme.value === ThemeType.DARK ? "bytemd-dark-mode" : "";
});

const handleThemeChange = (event: CustomEvent) => {
  if (event.detail && typeof event.detail.theme !== "undefined") {
    currentTheme.value = event.detail.theme;
  }
};

onMounted(() => {
  currentTheme.value = ThemeManager.getTheme(); // Ensure it's up-to-date on mount
  window.addEventListener("themeChange", handleThemeChange);
});

onUnmounted(() => {
  window.removeEventListener("themeChange", handleThemeChange);
});

const plugins = [
  gfm(),
  highlight(),
  // Add more plugins here
];

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  value: () => "",
});
</script>

<style></style>
