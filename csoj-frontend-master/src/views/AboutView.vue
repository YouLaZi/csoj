<template>
  <div id="aboutView">
    <a-card class="about-card">
      <template #title>
        <div class="card-title">
          <icon-info-circle />
          <span style="margin-left: 8px">关于CSOJ</span>
        </div>
      </template>
      <a-typography>
        <a-typography-title :heading="4">在线判题系统介绍</a-typography-title>
        <a-typography-paragraph>
          CSOJ 是一个功能完善的在线判题系统（Online
          Judge），为编程爱好者、算法学习者和竞赛参与者提供了一个便捷的编程练习平台。系统支持多种编程语言，提供丰富的题目库和实时的代码评测功能。
        </a-typography-paragraph>

        <a-typography-title :heading="4">主要功能</a-typography-title>
        <a-typography-paragraph>
          <a-space direction="vertical" style="width: 100%">
            <a-card class="feature-card">
              <template #title>题目练习</template>
              <p>
                提供丰富多样的编程题目，覆盖基础算法、数据结构、动态规划等多个领域，满足不同难度需求。
              </p>
            </a-card>

            <a-card class="feature-card">
              <template #title>在线编程</template>
              <p>
                内置代码编辑器，支持多种编程语言，提供语法高亮和自动补全功能，让编码更加高效。
              </p>
            </a-card>

            <a-card class="feature-card">
              <template #title>实时评测</template>
              <p>
                提交代码后系统会自动进行编译和测试，给出运行结果、执行时间和内存占用等详细信息。
              </p>
            </a-card>

            <a-card class="feature-card">
              <template #title>社区交流</template>
              <p>
                用户可以在社区中分享解题思路、讨论技术问题，促进学习交流和共同进步。
              </p>
            </a-card>
          </a-space>
        </a-typography-paragraph>

        <!-- <a-typography-title :heading="4">开发团队</a-typography-title>
        <a-typography-paragraph>
          CSOJ
          由程序员Pyrrhus、程序员鱼皮及其团队开发维护，致力于为编程学习者提供优质的练习平台。我们持续优化系统功能和用户体验，欢迎广大用户提供宝贵意见和建议。
        </a-typography-paragraph> -->

        <a-divider />

        <a-typography-paragraph>
          <a-space>
            <a-link href="https://github.com/YouLaZi" target="_blank"
              >官方网站</a-link
            >
            <a-divider direction="vertical" />
            <a-link href="https://github.com/YouLaZi" target="_blank"
              >GitHub 仓库</a-link
            >
            <a-divider direction="vertical" />
            <a-link href="https://github.com/YouLaZi">联系我们</a-link>
          </a-space>
        </a-typography-paragraph>
      </a-typography>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { IconInfoCircle } from "@arco-design/web-vue/es/icon";
</script>

<style scoped>
#aboutView {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.about-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 500;
}

.feature-card {
  margin-bottom: 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.feature-card:hover {
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
</style>
