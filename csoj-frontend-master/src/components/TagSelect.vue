<template>
  <div class="tag-select-container">
    <a-input-tag
      v-model="selectedTags"
      :placeholder="placeholder"
      allow-clear
      :readonly="readonly"
      @change="handleChange"
    >
      <template #tag="{ value }">
        <a-tag
          :style="{
            backgroundColor: getTagColor(value),
            color: 'var(--color-white)',
          }"
          :closable="!readonly"
        >
          {{ value }}
        </a-tag>
      </template>
    </a-input-tag>
    <div v-if="showSuggestions" class="tag-suggestions">
      <p class="suggestion-title">推荐标签：</p>
      <div class="suggestion-tags">
        <a-tag
          v-for="tag in filteredSuggestions"
          :key="tag.id"
          :style="{
            backgroundColor: tag.color,
            color: 'var(--color-white)',
            cursor: 'pointer',
          }"
          @click="addTag(tag.name)"
        >
          {{ tag.name }}
        </a-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { TagControllerService } from "../../generated/services/TagControllerService";
import type { Tag } from "../../generated/models/Tag";
import { OpenAPI } from "../../generated/core/OpenAPI";
import { useUserStore } from "@/store/useUserStore";

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
  placeholder: {
    type: String,
    default: "请选择标签",
  },
  category: {
    type: String,
    default: undefined,
  },
  readonly: {
    type: Boolean,
    default: false,
  },
  showSuggestions: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["update:modelValue", "change"]);

// 选中的标签
const selectedTags = computed({
  get: () => props.modelValue,
  set: (val) => {
    emit("update:modelValue", val);
  },
});

// 标签建议列表
const tagSuggestions = ref<Tag[]>([]);

// 过滤后的建议标签（排除已选择的）
const filteredSuggestions = computed(() => {
  return tagSuggestions.value.filter(
    (tag) => !selectedTags.value.includes(tag.name)
  );
});

// 获取标签颜色
const getTagColor = (tagName: string) => {
  const tag = tagSuggestions.value.find((t) => t.name === tagName);
  return tag ? tag.color : "#1890ff"; // 默认颜色
};

// 添加标签
const addTag = (tagName: string) => {
  if (!selectedTags.value.includes(tagName)) {
    const newTags = [...selectedTags.value, tagName];
    emit("update:modelValue", newTags);
    emit("change", newTags);
  }
};

// 处理标签变化
const handleChange = (tags: string[]) => {
  emit("change", tags);
};

// 获取标签建议
const fetchTagSuggestions = async () => {
  try {
    // 设置用户token
    const { token } = useUserStore();
    if (token) {
      OpenAPI.TOKEN = token;
    }

    const res = await TagControllerService.getTagListUsingGet();
    if (res.code === 0 && res.data) {
      // 根据分类过滤标签
      let filteredTags = res.data;
      if (props.category) {
        const categoryMap: { [key: string]: number } = {
          题目: 0,
          帖子: 1,
          其他: 2,
        };
        const categoryType = categoryMap[props.category];
        if (categoryType !== undefined) {
          filteredTags = res.data.filter((tag) => tag.type === categoryType);
        }
      }
      tagSuggestions.value = filteredTags;
    }
  } catch (error) {
    console.error("获取标签建议失败", error);
  }
};

// 监听分类变化，重新获取标签建议
watch(
  () => props.category,
  () => {
    fetchTagSuggestions();
  }
);

// 组件挂载时获取标签建议
onMounted(() => {
  fetchTagSuggestions();
});
</script>

<style scoped>
.tag-select-container {
  width: 100%;
}

.tag-suggestions {
  margin-top: 8px;
}

.suggestion-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.suggestion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
