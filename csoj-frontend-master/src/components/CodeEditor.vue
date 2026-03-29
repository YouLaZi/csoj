<template>
  <div class="code-editor-wrapper">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <a-tooltip content="格式化代码 (Shift+Alt+F)">
          <a-button size="small" @click="formatCode">
            <template #icon><icon-code /></template>
          </a-button>
        </a-tooltip>
        <a-tooltip content="重置为模板">
          <a-button size="small" @click="resetToTemplate">
            <template #icon><icon-refresh /></template>
          </a-button>
        </a-tooltip>
        <a-dropdown trigger="click">
          <a-button size="small">
            <template #icon><icon-insert-snippet /></template>
            代码片段
          </a-button>
          <template #content>
            <a-doption
              v-for="snippet in currentSnippets"
              :key="snippet.label"
              @click="insertSnippet(snippet)"
            >
              <div class="snippet-option">
                <span class="snippet-label">{{ snippet.label }}</span>
                <span class="snippet-desc">{{ snippet.description }}</span>
              </div>
            </a-doption>
            <a-divider v-if="currentSnippets.length === 0" />
            <a-doption v-if="currentSnippets.length === 0" disabled>
              暂无可用片段
            </a-doption>
          </template>
        </a-dropdown>
      </div>
      <div class="toolbar-right">
        <a-tooltip content="编辑器设置">
          <a-button size="small" @click="showSettings = true">
            <template #icon><icon-settings /></template>
          </a-button>
        </a-tooltip>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div
      id="code-editor"
      ref="codeEditorRef"
      class="monaco-container"
      :style="{ height: editorHeight }"
    />

    <!-- 设置抽屉 -->
    <a-drawer
      v-model:visible="showSettings"
      title="编辑器设置"
      placement="right"
      :width="320"
    >
      <div class="settings-form">
        <a-form-item label="字体大小">
          <a-input-number
            v-model="editorSettings.fontSize"
            :min="10"
            :max="30"
            @change="applySettings"
          />
        </a-form-item>
        <a-form-item label="Tab 大小">
          <a-input-number
            v-model="editorSettings.tabSize"
            :min="2"
            :max="8"
            @change="applySettings"
          />
        </a-form-item>
        <a-form-item label="自动换行">
          <a-switch v-model="editorSettings.wordWrap" @change="applySettings" />
        </a-form-item>
        <a-form-item label="显示小地图">
          <a-switch v-model="editorSettings.minimap" @change="applySettings" />
        </a-form-item>
        <a-form-item label="显示行号">
          <a-switch
            v-model="editorSettings.lineNumbers"
            @change="applySettings"
          />
        </a-form-item>
        <a-form-item label="编辑器高度">
          <a-select v-model="editorSettings.height" @change="applySettings">
            <a-option value="400px">小 (400px)</a-option>
            <a-option value="60vh">中 (60vh)</a-option>
            <a-option value="80vh">大 (80vh)</a-option>
          </a-select>
        </a-form-item>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import * as monaco from "monaco-editor";
import {
  onMounted,
  ref,
  toRaw,
  withDefaults,
  watch,
  onUnmounted,
  computed,
} from "vue";
import { ThemeManager, ThemeType } from "@/utils/themeManager";
import {
  IconCode,
  IconRefresh,
  IconSettings,
  IconInsertSnippet,
} from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
  language?: string;
  handleChange: (v: string) => void;
}

/**
 * 代码片段接口
 */
interface CodeSnippet {
  label: string;
  description: string;
  code: string;
  languages?: string[]; // 支持的语言，不设置则所有语言可用
}

/**
 * 编辑器设置接口
 */
interface EditorSettings {
  fontSize: number;
  tabSize: number;
  wordWrap: boolean;
  minimap: boolean;
  lineNumbers: boolean;
  height: string;
}

/**
 * 代码模板
 */
const codeTemplates: Record<string, string> = {
  java: `public class Main {
    public static void main(String[] args) {
        // 在这里编写你的 Java 代码
    }
}`,
  c: `#include <stdio.h>

int main() {
    // 在这里编写你的 C 代码
    return 0;
}`,
  cpp: `#include <iostream>

int main() {
    // 在这里编写你的 C++ 代码
    return 0;
}`,
  csharp: `using System;

public class Program {
    public static void Main(string[] args) {
        // 在这里编写你的 C# 代码
    }
}`,
  go: `package main

import "fmt"

func main() {
    // 在这里编写你的 Go 代码
}`,
  javascript: `// 在这里编写你的 JavaScript 代码
console.log("Hello, JavaScript!");`,
  python: `# 在这里编写你的 Python 代码
print("Hello, Python!")`,
  typescript: `// 在这里编写你的 TypeScript 代码
console.log("Hello, TypeScript!");`,
};

/**
 * 代码片段库
 */
const codeSnippets: CodeSnippet[] = [
  // Java 片段
  {
    label: "for循环",
    description: "标准for循环",
    code: "for (int i = 0; i < n; i++) {\n    \n}",
    languages: ["java"],
  },
  {
    label: "读取输入",
    description: "Scanner读取标准输入",
    code: `Scanner scanner = new Scanner(System.in);
int n = scanner.nextInt();`,
    languages: ["java"],
  },
  {
    label: "数组排序",
    description: "数组排序",
    code: `Arrays.sort(arr);`,
    languages: ["java"],
  },
  {
    label: "HashMap",
    description: "创建HashMap",
    code: `Map<String, Integer> map = new HashMap<>();`,
    languages: ["java"],
  },
  {
    label: "ArrayList",
    description: "创建动态数组",
    code: `List<Integer> list = new ArrayList<>();`,
    languages: ["java"],
  },
  // C 片段
  {
    label: "for循环",
    description: "标准for循环",
    code: "for (int i = 0; i < n; i++) {\n    \n}",
    languages: ["c"],
  },
  {
    label: "读取输入",
    description: "scanf读取输入",
    code: `int n;
scanf("%d", &n);`,
    languages: ["c"],
  },
  {
    label: "动态数组",
    description: "malloc动态分配",
    code: `int *arr = (int*)malloc(n * sizeof(int));`,
    languages: ["c"],
  },
  // C++ 片段
  {
    label: "for循环",
    description: "范围for循环",
    code: "for (int i = 0; i < n; i++) {\n    \n}",
    languages: ["cpp"],
  },
  {
    label: "读取输入",
    description: "cin读取输入",
    code: `int n;
cin >> n;`,
    languages: ["cpp"],
  },
  {
    label: "vector",
    description: "创建动态数组",
    code: `vector<int> v;`,
    languages: ["cpp"],
  },
  {
    label: "sort排序",
    description: "vector排序",
    code: `sort(v.begin(), v.end());`,
    languages: ["cpp"],
  },
  {
    label: "map",
    description: "创建映射",
    code: `map<string, int> m;`,
    languages: ["cpp"],
  },
  // Python 片段
  {
    label: "for循环",
    description: "范围循环",
    code: `for i in range(n):
    `,
    languages: ["python"],
  },
  {
    label: "读取输入",
    description: "读取标准输入",
    code: `n = int(input())`,
    languages: ["python"],
  },
  {
    label: "列表推导",
    description: "列表推导式",
    code: `[x for x in range(n)]`,
    languages: ["python"],
  },
  {
    label: "字典",
    description: "创建字典",
    code: `d = {}`,
    languages: ["python"],
  },
  // Go 片段
  {
    label: "for循环",
    description: "标准for循环",
    code: `for i := 0; i < n; i++ {
    `,
    languages: ["go"],
  },
  {
    label: "读取输入",
    description: "fmt读取输入",
    code: `var n int
fmt.Scan(&n)`,
    languages: ["go"],
  },
  {
    label: "slice",
    description: "创建切片",
    code: `s := make([]int, 0)`,
    languages: ["go"],
  },
  {
    label: "map",
    description: "创建映射",
    code: `m := make(map[string]int)`,
    languages: ["go"],
  },
  // JavaScript 片段
  {
    label: "for循环",
    description: "标准for循环",
    code: `for (let i = 0; i < n; i++) {
    `,
    languages: ["javascript", "typescript"],
  },
  {
    label: "读取输入",
    description: "readline读取",
    code: `const readline = require('readline');
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});
rl.on('line', (line) => {
    const n = parseInt(line);
});`,
    languages: ["javascript"],
  },
  {
    label: "Map",
    description: "创建Map",
    code: `const map = new Map();`,
    languages: ["javascript", "typescript"],
  },
  {
    label: "数组方法",
    description: "常用数组方法",
    code: `arr.map(x => x * 2).filter(x => x > 0);`,
    languages: ["javascript", "typescript"],
  },
  // 通用片段
  {
    label: "if条件",
    description: "条件判断",
    code: `if (condition) {
    } else {
    }`,
  },
  {
    label: "while循环",
    description: "while循环",
    code: `while (condition) {
    }`,
  },
  {
    label: "函数定义",
    description: "定义函数",
    code: `function name(params) {
    }`,
  },
];

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  value: () => "",
  language: () => "java",
  handleChange: (v: string) => {
    console.log(v);
  },
});

const codeEditorRef = ref();
const codeEditor = ref();
const showSettings = ref(false);

// 编辑器设置
const editorSettings = ref<EditorSettings>({
  fontSize: 14,
  tabSize: 4,
  wordWrap: false,
  minimap: true,
  lineNumbers: true,
  height: "60vh",
});

// 从本地存储加载设置
const SETTINGS_KEY = "code_editor_settings";
const loadSettings = () => {
  const saved = localStorage.getItem(SETTINGS_KEY);
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      editorSettings.value = { ...editorSettings.value, ...parsed };
    } catch (e) {
      console.error("Failed to load editor settings:", e);
    }
  }
};

// 保存设置到本地存储
const saveSettings = () => {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify(editorSettings.value));
};

// 计算编辑器高度
const editorHeight = computed(() => editorSettings.value.height);

// 获取当前语言可用的代码片段
const currentSnippets = computed(() => {
  return codeSnippets.filter(
    (s) => !s.languages || s.languages.includes(props.language)
  );
});

// 插入代码片段
const insertSnippet = (snippet: CodeSnippet) => {
  if (!codeEditor.value) return;

  const editor = toRaw(codeEditor.value);
  const position = editor.getPosition();
  editor.executeEdits("", [
    {
      range: new monaco.Range(
        position.lineNumber,
        position.column,
        position.lineNumber,
        position.column
      ),
      text: snippet.code,
    },
  ]);
  editor.focus();
};

// 格式化代码
const formatCode = async () => {
  if (!codeEditor.value) return;

  try {
    const editor = toRaw(codeEditor.value);
    await editor.getAction("editor.action.formatDocument").run();
    Message.success("代码格式化完成");
  } catch (error) {
    console.error("Format error:", error);
    Message.warning("当前语言暂不支持格式化");
  }
};

// 重置为模板
const resetToTemplate = () => {
  if (!codeEditor.value) return;

  const template = codeTemplates[props.language];
  if (template) {
    toRaw(codeEditor.value).setValue(template);
    Message.success("已重置为模板");
  } else {
    Message.warning("当前语言没有默认模板");
  }
};

// 应用设置到编辑器
const applySettings = () => {
  if (!codeEditor.value) return;

  const editor = toRaw(codeEditor.value);
  editor.updateOptions({
    fontSize: editorSettings.value.fontSize,
    tabSize: editorSettings.value.tabSize,
    wordWrap: editorSettings.value.wordWrap ? "on" : "off",
    minimap: { enabled: editorSettings.value.minimap },
    lineNumbers: editorSettings.value.lineNumbers ? "on" : "off",
  });

  saveSettings();
};

// 注册快捷键
const registerShortcuts = (editor: monaco.editor.IStandaloneCodeEditor) => {
  // 格式化快捷键 Shift+Alt+F
  editor.addCommand(
    monaco.KeyMod.Shift | monaco.KeyMod.Alt | monaco.KeyCode.KeyF,
    () => {
      formatCode();
    }
  );

  // 保存快捷键 Ctrl+S (防止浏览器默认行为)
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
    // 可以触发保存操作
    Message.info("代码已自动保存");
  });
};

watch(
  () => props.language,
  () => {
    if (codeEditor.value) {
      monaco.editor.setModelLanguage(
        toRaw(codeEditor.value).getModel(),
        props.language
      );
      // 切换语言时，如果编辑器内容为空，则填充模板
      const currentValue = toRaw(codeEditor.value).getValue();
      if (
        !currentValue.trim() &&
        props.language &&
        codeTemplates[props.language]
      ) {
        toRaw(codeEditor.value).setValue(codeTemplates[props.language]);
      }
    }
  }
);

onMounted(() => {
  if (!codeEditorRef.value) {
    return;
  }

  // 加载保存的设置
  loadSettings();

  // 创建编辑器
  codeEditor.value = monaco.editor.create(codeEditorRef.value, {
    value: props.value,
    language: props.language,
    automaticLayout: true,
    colorDecorators: true,
    minimap: {
      enabled: editorSettings.value.minimap,
    },
    readOnly: false,
    theme: ThemeManager.getTheme() === ThemeType.DARK ? "vs-dark" : "vs",
    lineNumbers: editorSettings.value.lineNumbers ? "on" : "off",
    roundedSelection: false,
    scrollBeyondLastLine: false,
    fontSize: editorSettings.value.fontSize,
    tabSize: editorSettings.value.tabSize,
    wordWrap: editorSettings.value.wordWrap ? "on" : "off",
    // 增强功能
    formatOnPaste: true,
    formatOnType: true,
    suggestOnTriggerCharacters: true,
    acceptSuggestionOnEnter: "on",
    quickSuggestions: true,
    snippetSuggestions: "top",
  });

  // 初始化时，如果编辑器内容为空且语言有模板，则填充模板
  if (!props.value.trim() && props.language && codeTemplates[props.language]) {
    toRaw(codeEditor.value).setValue(codeTemplates[props.language]);
  }

  // 编辑 监听内容变化
  codeEditor.value.onDidChangeModelContent(() => {
    props.handleChange(toRaw(codeEditor.value).getValue());
  });

  // 注册快捷键
  registerShortcuts(toRaw(codeEditor.value));

  // 监听主题变化
  const handleThemeChange = (event: CustomEvent) => {
    const newTheme = event.detail.theme;
    monaco.editor.setTheme(newTheme === ThemeType.DARK ? "vs-dark" : "vs");
  };

  window.addEventListener("themeChange", handleThemeChange);

  // 组件卸载时移除监听器
  onUnmounted(() => {
    window.removeEventListener("themeChange", handleThemeChange);
    if (codeEditor.value) {
      toRaw(codeEditor.value).dispose();
    }
  });
});

// 暴露方法供父组件调用
defineExpose({
  formatCode,
  resetToTemplate,
  getValue: () => toRaw(codeEditor.value)?.getValue(),
  setValue: (value: string) => toRaw(codeEditor.value)?.setValue(value),
});
</script>

<style scoped>
.code-editor-wrapper {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: var(--bg-color-secondary);
  border-bottom: 1px solid var(--border-color);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
}

.monaco-container {
  min-height: 400px;
}

.snippet-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.snippet-label {
  font-weight: 500;
}

.snippet-desc {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

:deep(.arco-form-item) {
  margin-bottom: 0;
}

/* 暗色主题适配 */
:deep(.arco-drawer) {
  background-color: var(--bg-color-secondary);
}

:deep(.arco-drawer-title) {
  color: var(--text-color-primary);
}
</style>
