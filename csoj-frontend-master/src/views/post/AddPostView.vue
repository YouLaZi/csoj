<template>
  <div id="addPostView">
    <h2>{{ isEdit ? "编辑帖子" : "创建帖子" }}</h2>
    <a-form :model="form" label-align="left">
      <a-form-item field="title" label="标题">
        <a-input v-model="form.title" placeholder="请输入标题" />
      </a-form-item>
      <a-form-item field="content" label="内容">
        <MdEditor :value="form.content" :handle-change="onContentChange" />
      </a-form-item>
      <a-form-item field="tags" label="标签">
        <TagSelect
          v-model="form.tags"
          category="帖子"
          placeholder="请选择标签"
        />
      </a-form-item>
      <div style="margin-top: 16px" />
      <a-form-item>
        <a-button type="primary" style="min-width: 200px" @click="doSubmit"
          >提交
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from "vue";
import {
  PostControllerService,
  PostAddRequest,
  PostUpdateRequest,
  PostEditRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter, useRoute } from "vue-router";
import MdEditor from "@/components/MdEditor.vue";
import TagSelect from "@/components/TagSelect.vue";

const router = useRouter();
const route = useRoute();

// 是否为编辑模式
const isEdit = ref(route.path.includes("/update"));

// 表单数据
const form = reactive<PostEditRequest>({
  title: "",
  content: "",
  tags: [],
});

// 如果是编辑模式，加载帖子数据
const loadData = async () => {
  if (isEdit.value && route.query.id) {
    const res = await PostControllerService.getPostVoByIdUsingGet(
      route.query.id as any
    );
    if (res.code === 0) {
      const { title, content, tags, id } = res.data;
      form.title = title;
      form.content = content;
      form.tags = tags || [];
      form.id = id;
    } else {
      message.error("加载失败，" + res.message);
    }
  }
};

// 内容变化时的处理函数
const onContentChange = (value: string) => {
  form.content = value;
};

// 提交表单
const doSubmit = async () => {
  if (!form.title) {
    message.error("标题不能为空");
    return;
  }
  if (!form.content) {
    message.error("内容不能为空");
    return;
  }

  try {
    // 根据是否为编辑模式，调用不同的接口
    let res;
    if (isEdit.value) {
      const postUpdateRequest: PostUpdateRequest = {
        id: form.id,
        title: form.title,
        content: form.content,
        tags: form.tags,
      };
      res = await PostControllerService.updatePostUsingPost(postUpdateRequest);
    } else {
      const postAddRequest: PostAddRequest = {
        title: form.title,
        content: form.content,
        tags: form.tags,
      };
      res = await PostControllerService.addPostUsingPost(postAddRequest);
    }

    if (res.code === 0) {
      message.success(isEdit.value ? "更新成功" : "创建成功");
      // 跳转到帖子列表页
      router.push("/posts");
    } else {
      message.error("操作失败，" + res.message);
    }
  } catch (error) {
    message.error("操作失败，请稍后重试");
    console.error(error);
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
#addPostView {
  max-width: 1280px;
  margin: 0 auto;
  padding: 16px;
}
</style>
