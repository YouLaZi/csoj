<template>
  <div class="architecture-diagram">
    <a-card title="系统架构图" :bordered="false">
      <template #extra>
        <a-button type="text" @click="refreshDiagram">
          <template #icon><icon-refresh /></template>
          刷新
        </a-button>
      </template>

      <!-- Mermaid 图表容器 - 添加 key 绑定 -->
      <div ref="mermaidContainer" class="mermaid-container">
        <div v-if="!mermaidLoaded" class="loading-placeholder">
          <a-spin size="large" />
          <p>正在加载架构图...</p>
        </div>
        <div v-else-if="error" class="error-placeholder">
          <a-result status="error" title="图表加载失败" :sub-title="error">
            <template #extra>
              <a-button type="primary" @click="refreshDiagram"
                >重新加载</a-button
              >
            </template>
          </a-result>
        </div>
        <!-- 添加 key 绑定确保每次刷新都创建新节点 -->
        <div
          v-else
          :key="diagramKey"
          ref="diagramRef"
          class="diagram-content"
        ></div>
      </div>

      <!-- 图表说明 -->
      <a-divider />
      <a-alert type="info" show-icon>
        <template #title>系统架构说明</template>
        <!-- 使用 #default 插槽替换 #content -->
        <template #default>
          <p>
            此图展示了CodeSmart在线判题系统的整体架构，包括前端模块、用户模块、题目模块、判题模块、社区模块、AI辅助模块、积分系统和后端服务等核心组件。
          </p>
          <p>
            <strong>注意：</strong>
            如需查看完整的交互式架构图，请确保已安装mermaid依赖。
          </p>
        </template>
      </a-alert>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import { IconRefresh } from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";
import mermaid from "mermaid";

// 添加 diagramKey 作为响应式变量
const diagramKey = ref(0);
const mermaidContainer = ref<HTMLElement>();
const diagramRef = ref<HTMLElement>();
const mermaidLoaded = ref(false);
const error = ref<string>("");

// Mermaid 图表定义
const mermaidDiagram = `
graph TD
    %% 主要模块定义
    subgraph 前端模块
        UI[用户界面]
        Auth[认证授权]
        PermCtrl[权限控制]
    end
    
    subgraph 用户模块
        UserMgmt[用户管理]
        UserProfile[用户资料]
        PermissionMgmt[权限管理]
    end
    
    subgraph 题目模块
        QuestionBank[题库管理]
        QuestionEdit[题目编辑]
        QuestionSearch[题目搜索]
    end
    
    subgraph 判题模块
        CodeSubmit[代码提交]
        JudgeEngine[判题引擎]
        Sandbox[代码沙箱]
        TestCase[测试用例]
    end
    
    subgraph 社区模块
        PostMgmt[帖子管理]
        Comment[评论系统]
        Notification[通知系统]
    end
    
    subgraph AI辅助模块
        AIChat[AI对话]
        CodeExplain[代码解释]
        LearningGuide[学习引导]
    end
    
    subgraph 积分系统
        PointsCalc[积分计算]
        Ranking[排行榜]
        Achievement[成就系统]
    end
    
    subgraph 后端服务
        API[RESTful API]
        Cache[缓存服务]
        DB[数据库]
        Search[搜索引擎]
        MQ[消息队列]
    end
    
    %% 模块间交互关系
    UI --> Auth
    UI --> PermCtrl
    Auth --> UserMgmt
    PermCtrl --> PermissionMgmt
    
    %% 前端与后端交互
    UI --> API
    
    %% 用户模块交互
    UserMgmt --> DB
    UserProfile --> DB
    PermissionMgmt --> Cache
    PermissionMgmt --> DB
    
    %% 题目模块交互
    QuestionBank --> API
    QuestionEdit --> API
    QuestionSearch --> Search
    
    %% 判题模块交互
    CodeSubmit --> API
    CodeSubmit --> MQ
    MQ --> JudgeEngine
    JudgeEngine --> Sandbox
    JudgeEngine --> TestCase
    JudgeEngine --> DB
    
    %% 社区模块交互
    PostMgmt --> API
    PostMgmt --> Search
    Comment --> API
    Notification --> MQ
    
    %% AI辅助模块交互
    AIChat --> API
    CodeExplain --> API
    LearningGuide --> API
    
    %% 积分系统交互
    PointsCalc --> DB
    Ranking --> Cache
    Achievement --> DB
    
    %% 数据流向
    DB --> Cache
    API --> Cache
    API --> DB
    API --> Search
    API --> MQ
    
    %% 样式设置
    classDef frontendModule fill:#f9d5e5,stroke:#333,stroke-width:1px
    classDef userModule fill:#eeac99,stroke:#333,stroke-width:1px
    classDef questionModule fill:#e06377,stroke:#333,stroke-width:1px
    classDef judgeModule fill:#c83349,stroke:#333,stroke-width:1px
    classDef communityModule fill:#5b9aa0,stroke:#333,stroke-width:1px
    classDef aiModule fill:#d6e5fa,stroke:#333,stroke-width:1px
    classDef pointsModule fill:#a8d8ea,stroke:#333,stroke-width:1px
    classDef backendModule fill:#aa96da,stroke:#333,stroke-width:1px
    
    class UI,Auth,PermCtrl frontendModule
    class UserMgmt,UserProfile,PermissionMgmt userModule
    class QuestionBank,QuestionEdit,QuestionSearch questionModule
    class CodeSubmit,JudgeEngine,Sandbox,TestCase judgeModule
    class PostMgmt,Comment,Notification communityModule
    class AIChat,CodeExplain,LearningGuide aiModule
    class PointsCalc,Ranking,Achievement pointsModule
    class API,Cache,DB,Search,MQ backendModule
`;

// 渲染图表
const renderDiagram = async () => {
  try {
    error.value = "";
    mermaidLoaded.value = false;

    // 每次渲染增加 key 值确保创建新节点
    diagramKey.value++;

    // 等待 DOM 更新
    await nextTick();

    if (!diagramRef.value) return;

    // 初始化 Mermaid
    mermaid.initialize({
      startOnLoad: false,
      theme: "default",
      securityLevel: "loose",
      flowchart: {
        useMaxWidth: true,
        htmlLabels: true,
        curve: "basis",
      },
      fontFamily: "inherit",
    });

    // 清空容器
    diagramRef.value.innerHTML = "";

    // 创建临时容器用于渲染
    const tempContainer = document.createElement("div");
    tempContainer.style.display = "none";
    document.body.appendChild(tempContainer);

    try {
      // 渲染到临时容器
      const { svg } = await mermaid.render(
        `diagram-${diagramKey.value}`,
        mermaidDiagram
      );

      // 将 SVG 插入到实际容器
      diagramRef.value.innerHTML = svg;
      mermaidLoaded.value = true;
      Message.success("架构图加载成功");
    } catch (err: any) {
      console.error("Mermaid 渲染错误:", err);
      error.value = err.message || "图表渲染失败";
      mermaidLoaded.value = true;
    } finally {
      // 清理临时容器
      document.body.removeChild(tempContainer);
    }
  } catch (err: any) {
    console.error("图表渲染失败:", err);
    error.value = err.message || "未知错误";
    mermaidLoaded.value = true;
  }
};

// 刷新图表
const refreshDiagram = () => {
  renderDiagram();
};

// 组件挂载时渲染图表
onMounted(() => {
  renderDiagram();
});
</script>

<style scoped>
.architecture-diagram {
  width: 100%;
}

.mermaid-container {
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-bg-2);
  border-radius: 4px;
  border: 1px solid var(--color-border);
  position: relative;
}

.diagram-content {
  width: 100%;
  padding: 20px;
  overflow: auto;
  text-align: center;
  min-height: 450px;
}

.loading-placeholder,
.error-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  z-index: 10;
}

.loading-placeholder p {
  margin-top: 16px;
  color: var(--color-text-2);
}

/* 添加响应式处理 */
@media (max-width: 768px) {
  .mermaid-container {
    min-height: 300px;
  }

  .diagram-content {
    transform: scale(0.8);
    transform-origin: top center;
  }
}
</style>
