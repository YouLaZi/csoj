<template>
  <div :class="editorThemeClass">
    <Editor
      :value="value"
      :mode="mode"
      :plugins="plugins"
      @change="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import gfm from "@bytemd/plugin-gfm";
import highlight from "@bytemd/plugin-highlight";
import { Editor } from "@bytemd/vue-next"; // Viewer removed
import {
  ref,
  withDefaults,
  defineProps,
  onMounted, // Added
  onUnmounted, // Added
  computed, // Added
} from "vue";
import { ThemeManager, ThemeType } from "@/utils/themeManager"; // Added

// Theme management
const currentTheme = ref<ThemeType>(ThemeManager.getTheme());
const editorThemeClass = computed(() => {
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

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
  mode?: string;
  handleChange: (v: string) => void;
}

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
  mode: () => "split",
  handleChange: (v: string) => {
    console.log(v);
  },
});
</script>

<style>
.bytemd-toolbar-icon.bytemd-tippy.bytemd-tippy-right:last-child {
  display: none;
}
</style>
