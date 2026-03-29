<template>
  <div class="chat-message" :class="[`message-${message.role}`, { streaming: message.isStreaming }]">
    <div class="message-avatar">
      <div v-if="message.role === 'user'" class="avatar user-avatar">
        <icon-user />
      </div>
      <div v-else class="avatar bot-avatar">
        <icon-robot />
      </div>
    </div>
    <div class="message-body">
      <div class="message-content" ref="contentRef">
        <!-- Code block detection -->
        <template v-if="hasCodeBlock">
          <div v-for="(block, index) in parsedBlocks" :key="index">
            <div v-if="block.type === 'code'" class="code-block-wrapper">
              <div class="code-header">
                <span class="code-lang">{{ block.language || 'code' }}</span>
                <button class="copy-btn" @click="copyCode(block.content)">
                  <icon-copy />
                  <span>{{ copied ? 'Copied!' : 'Copy' }}</span>
                </button>
              </div>
              <pre class="code-content"><code>{{ block.content }}</code></pre>
            </div>
            <div v-else class="text-block" v-html="renderMarkdown(block.content)"></div>
          </div>
        </template>
        <template v-else>
          <div v-html="renderMarkdown(message.content)"></div>
        </template>
        <!-- Streaming cursor -->
        <span v-if="message.isStreaming" class="streaming-cursor"></span>
      </div>
      <div class="message-meta">
        <span class="message-time">{{ formatTime(message.timestamp) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { iconUser, iconRobot, iconCopy } from "./icons";
import type { ChatMessage } from "@/types/chat";

const props = defineProps<{
  message: ChatMessage;
}>();

const contentRef = ref<HTMLElement | null>(null);
const copied = ref(false);

interface ParsedBlock {
  type: "text" | "code";
  content: string;
  language?: string;
}

const hasCodeBlock = computed(() => {
  return /```[\s\S]*?```/.test(props.message.content);
});

const parsedBlocks = computed((): ParsedBlock[] => {
  const content = props.message.content;
  const blocks: ParsedBlock[] = [];
  const codeBlockRegex = /```(\w*)\n?([\s\S]*?)```/g;
  let lastIndex = 0;
  let match;

  while ((match = codeBlockRegex.exec(content)) !== null) {
    if (match.index > lastIndex) {
      const text = content.slice(lastIndex, match.index).trim();
      if (text) {
        blocks.push({ type: "text", content: text });
      }
    }
    blocks.push({
      type: "code",
      language: match[1] || "plaintext",
      content: match[2].trim(),
    });
    lastIndex = match.index + match[0].length;
  }

  if (lastIndex < content.length) {
    const text = content.slice(lastIndex).trim();
    if (text) {
      blocks.push({ type: "text", content: text });
    }
  }
  return blocks;
});

function renderMarkdown(text: string): string {
  if (!text) return "";
  let result = text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
  result = result.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
  result = result.replace(/\*([^*]+)\*/g, "<em>$1</em>");
  result = result.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');
  result = result.replace(
    /\[([^\]]+)\]\(([^)]+)\)/g,
    '<a href="$2" target="_blank" rel="noopener">$1</a>'
  );
  result = result.replace(/\n/g, "<br>");
  return result;
}

function formatTime(timestamp: number): string {
  const date = new Date(timestamp);
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

async function copyCode(code: string): Promise<void> {
  try {
    await navigator.clipboard.writeText(code);
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  } catch (err) {
    console.error("Failed to copy:", err);
  }
}

watch(
  () => props.message.content,
  () => {
    if (props.message.isStreaming && contentRef.value) {
      contentRef.value.scrollTop = contentRef.value.scrollHeight;
    }
  }
);
</script>

<style scoped>
.chat-message {
  display: flex;
  gap: 12px;
  padding: 16px;
  animation: fade-in 0.3s ease;
}
@keyframes fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.message-avatar { flex-shrink: 0; }
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.user-avatar {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
}
.bot-avatar {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  color: white;
}
.message-body { flex: 1; min-width: 0; }
.message-content {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  overflow-wrap: break-word;
}
.message-user .message-content {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border-bottom-right-radius: 4px;
}
.message-assistant .message-content {
  background: var(--color-fill-1);
  color: var(--color-text-1);
  border-bottom-left-radius: 4px;
}
.message-meta { margin-top: 4px; padding: 0 16px; }
.message-time { font-size: 11px; color: var(--color-text-3); }
.code-block-wrapper {
  margin: 8px 0;
  border-radius: 8px;
  overflow: hidden;
  background: #1e1e1e;
  border: 1px solid var(--color-border-2);
}
.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #2d2d2d;
  border-bottom: 1px solid #3d3d3d;
}
.code-lang { font-size: 12px; color: #888; font-family: monospace; }
.copy-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: transparent;
  border: 1px solid #444;
  border-radius: 4px;
  color: #aaa;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.copy-btn:hover { background: #3d3d3d; color: white; }
.code-content {
  margin: 0;
  padding: 12px;
  overflow-x: auto;
  font-family: "JetBrains Mono", "Fira Code", monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #d4d4d4;
  white-space: pre;
}
.code-content code { font-family: inherit; }
.inline-code {
  background: var(--color-fill-2);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: "JetBrains Mono", "Fira Code", monospace;
  font-size: 0.9em;
}
.message-user .inline-code { background: rgba(255, 255, 255, 0.2); }
.text-block { margin: 4px 0; }
.streaming-cursor {
  display: inline-block;
  width: 8px;
  height: 16px;
  background: currentColor;
  margin-left: 2px;
  animation: blink 1s step-end infinite;
}
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
.chat-message.streaming .message-content { position: relative; }
[data-theme="dark"] .message-assistant .message-content { background: var(--color-fill-2); }
[data-theme="dark"] .code-block-wrapper { border-color: #3d3d3d; }
</style>
