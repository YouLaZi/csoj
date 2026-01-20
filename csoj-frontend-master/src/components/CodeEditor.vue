<template>
  <div
    id="code-editor"
    ref="codeEditorRef"
    style="min-height: 400px; height: 60vh"
  />
  <!--  <a-button @click="fillValue">填充值</a-button>-->
</template>

<script setup lang="ts">
import * as monaco from "monaco-editor";
import {
  onMounted,
  ref,
  toRaw,
  withDefaults,
  defineProps,
  watch,
  onUnmounted,
} from "vue";
import { ThemeManager, ThemeType } from "@/utils/themeManager";

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
  language?: string;
  handleChange: (v: string) => void;
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
  // php: `<?php
  // // 在这里编写你的 PHP 代码
  // echo "Hello, PHP!";
  // ?>`,
  // swift: `// 在这里编写你的 Swift 代码
  // print("Hello, Swift!")`,
  // kotlin: `// 在这里编写你的 Kotlin 代码
  // fun main() {
  //     println("Hello, Kotlin!")
  // }`,
  // ruby: `# 在这里编写你的 Ruby 代码
  // puts "Hello, Ruby!"`,
  // rust: `// 在这里编写你的 Rust 代码
  // fn main() {
  //     println!("Hello, Rust!");
  // }`,
  // scala: `// 在这里编写你的 Scala 代码
  // object Main extends App {
  //   println("Hello, Scala!")
  // }`,
  // objective-c: `// 在这里编写你的 Objective-C 代码
  // #import <Foundation/Foundation.h>
  // int main(int argc, const char * argv[]) {
  //   @autoreleasepool {
  //       NSLog(@"Hello, Objective-C!");
  //   }
  //   return 0;
  // }`,
  // dart: `// 在这里编写你的 Dart 代码
  // void main() {
  //   print('Hello, Dart!');
  // }`,
  // r: `# 在这里编写你的 R 代码
  // print("Hello, R!")`,
  // perl: `#!/usr/bin/perl
  // # 在这里编写你的 Perl 代码
  // print "Hello, Perl!\n";`,
  // lua: `-- 在这里编写你的 Lua 代码
  // print("Hello, Lua!")`,
  // sql: `-- 在这里编写你的 SQL 代码
  // SELECT 'Hello, SQL!';`,
  // shell: `#!/bin/bash
  // # 在这里编写你的 Shell 代码
  // echo "Hello, Shell!"`,
  // html: `<!-- 在这里编写你的 HTML 代码 -->
  // <!DOCTYPE html>
  // <html>
  // <head>
  //   <title>Hello, HTML!</title>
  // </head>
  // <body>
  //   <h1>Hello, HTML!</h1>
  // </body>
  // </html>`,
  // css: `/* 在这里编写你的 CSS 代码 */
  // body {
  //   font-family: sans-serif;
  // }`,
  // less: `// 在这里编写你的 Less 代码
  // @color: blue;
  // .header {
  //   color: @color;
  // }`,
  // scss: `// 在这里编写你的 SCSS 代码
  // $color: blue;
  // .header {
  //   color: $color;
  // }`,
  // json: `{
  //   "message": "Hello, JSON!"
  // }`,
  // xml: `<!-- 在这里编写你的 XML 代码 -->
  // <message>Hello, XML!</message>`,
  // yaml: `# 在这里编写你的 YAML 代码
  // message: Hello, YAML!`,
  // markdown: `# 在这里编写你的 Markdown 代码
  // Hello, Markdown!`,
  // plaintext: `在这里编写你的纯文本内容`,
};

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

// const fillValue = () => {
//   if (!codeEditor.value) {
//     return;
//   }
//   // 改变值
//   toRaw(codeEditor.value).setValue("新的值");
// };

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
  // Hover on each property to see its docs!
  codeEditor.value = monaco.editor.create(codeEditorRef.value, {
    value: props.value,
    language: props.language,
    automaticLayout: true,
    colorDecorators: true,
    minimap: {
      enabled: true,
    },
    readOnly: false,
    theme: ThemeManager.getTheme() === ThemeType.DARK ? "vs-dark" : "vs", // Initial theme
    lineNumbers: "on", // 显示行号
    roundedSelection: false,
    scrollBeyondLastLine: false,
  });

  // 初始化时，如果编辑器内容为空且语言有模板，则填充模板
  if (!props.value.trim() && props.language && codeTemplates[props.language]) {
    toRaw(codeEditor.value).setValue(codeTemplates[props.language]);
  }

  // 编辑 监听内容变化
  codeEditor.value.onDidChangeModelContent(() => {
    props.handleChange(toRaw(codeEditor.value).getValue());
  });

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
</script>

<style scoped></style>
