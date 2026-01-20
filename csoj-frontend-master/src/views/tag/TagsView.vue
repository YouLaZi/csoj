<template>
  <div id="tagsView">
    <a-card title="标签管理" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="showAddModal">新增标签</a-button>
      </template>

      <!-- 标签搜索区域 -->
      <a-row style="margin-bottom: 16px">
        <a-col :span="8">
          <a-input-search
            v-model="searchText"
            placeholder="请输入标签名称"
            search-button
            @search="onSearch"
          />
        </a-col>
      </a-row>

      <!-- 标签列表 -->
      <a-table
        :loading="loading"
        :columns="columns"
        :data="tagList"
        :pagination="{
          showTotal: true,
          pageSize: pagination.pageSize,
          current: pagination.current,
          total: pagination.total,
          onChange: (page) => {
            pagination.current = page;
            fetchTags();
          },
        }"
      >
        <template #color="{ record }">
          <span :style="{ color: record.color }">{{ record.color }}</span>
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button type="text" @click="showEditModal(record)">
              编辑
            </a-button>
            <a-popconfirm
              content="确定要删除这个标签吗？"
              @ok="deleteTag(record)"
            >
              <a-button type="text" status="danger"> 删除 </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑标签的模态框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="isEdit ? '编辑标签' : '新增标签'"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="form" ref="formRef">
        <a-form-item
          field="name"
          label="标签名称"
          :rules="[{ required: true, message: '请输入标签名称' }]"
        >
          <a-input v-model="form.name" placeholder="请输入标签名称" />
        </a-form-item>
        <a-form-item field="type" label="标签分类">
          <a-select v-model="form.type" placeholder="请选择标签分类">
            <a-option
              v-for="option in typeOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="color" label="标签颜色">
          <a-input
            v-model="form.color"
            placeholder="请输入标签颜色，例如：#1890ff"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { TagControllerService } from "../../../generated/services/TagControllerService";
import type { Tag } from "../../../generated/models/Tag";
import type { BaseResponse_List_Tag_ } from "../../../generated/models/BaseResponse_List_Tag_";
import type { BaseResponse_long_ } from "../../../generated/models/BaseResponse_long_";
import type { BaseResponse_boolean_ } from "../../../generated/models/BaseResponse_boolean_";
import { OpenAPI } from "../../../generated/core/OpenAPI";
import { useUserStore } from "@/store/useUserStore";

// 标签列表
const tagList = ref<Tag[]>([]);
// 加载状态
const loading = ref(false);
// 搜索文本
const searchText = ref("");
// 模态框可见性
const modalVisible = ref(false);
// 是否为编辑模式
const isEdit = ref(false);
// 表单引用
const formRef = ref();
// 分页信息
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10,
});

// 表单数据
const form = reactive<Tag>({
  id: undefined,
  name: "",
  type: 0, // 0-题目, 1-帖子, 2-其他
  color: "#1890ff",
});

// 类型映射
const typeOptions = [
  { label: "题目", value: 0 },
  { label: "帖子", value: 1 },
  { label: "其他", value: 2 },
];

// 获取类型标签
const getTypeLabel = (type: number) => {
  const option = typeOptions.find((opt) => opt.value === type);
  return option ? option.label : "未知";
};

// 表格列定义
const columns = [
  {
    title: "ID",
    dataIndex: "id",
  },
  {
    title: "标签名称",
    dataIndex: "name",
    slotName: "name",
  },
  {
    title: "分类",
    dataIndex: "type",
    render: ({ record }: { record: Tag }) => getTypeLabel(record.type || 0),
  },
  {
    title: "颜色",
    dataIndex: "color",
    slotName: "color",
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

// 获取标签列表
const fetchTags = async () => {
  loading.value = true;
  try {
    // 设置用户token
    const { token } = useUserStore();
    if (token) {
      OpenAPI.TOKEN = token;
    }

    const res = await TagControllerService.getTagListUsingGet();
    console.log("标签列表响应:", res);

    if (res.code === 0 && res.data) {
      // 过滤搜索结果
      let filteredTags = res.data;
      if (searchText.value) {
        filteredTags = res.data.filter((tag) =>
          tag.name?.toLowerCase().includes(searchText.value.toLowerCase())
        );
      }

      // 简单分页处理
      const startIndex = (pagination.current - 1) * pagination.pageSize;
      const endIndex = startIndex + pagination.pageSize;
      tagList.value = filteredTags.slice(startIndex, endIndex);
      pagination.total = filteredTags.length;
    } else {
      Message.error("获取标签列表失败：" + (res.message || "未知错误"));
      tagList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error("获取标签列表失败", error);
    Message.error("获取标签列表失败");
    tagList.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索标签
const onSearch = () => {
  pagination.current = 1; // 重置到第一页
  fetchTags();
};

// 显示添加模态框
const showAddModal = () => {
  isEdit.value = false;
  resetForm();
  modalVisible.value = true;
};

// 显示编辑模态框
const showEditModal = (record: Tag) => {
  isEdit.value = true;
  form.id = record.id;
  form.name = record.name || "";
  form.type = record.type || 0;
  form.color = record.color || "#1890ff";
  modalVisible.value = true;
};

// 重置表单
const resetForm = () => {
  form.id = undefined;
  form.name = "";
  form.type = 0;
  form.color = "#1890ff";
};

// 处理模态框确认
const handleModalOk = async () => {
  if (!form.name) {
    Message.error("标签名称不能为空");
    return;
  }

  try {
    // 设置用户token
    const { token } = useUserStore();
    if (token) {
      OpenAPI.TOKEN = token;
    }

    let res;
    if (isEdit.value) {
      // 编辑标签
      res = await TagControllerService.updateTagUsingPost(form);
    } else {
      // 新增标签
      res = await TagControllerService.addTagUsingPost(form);
    }

    console.log("标签操作响应:", res);

    if (res.code === 0) {
      Message.success(isEdit.value ? "标签更新成功" : "标签添加成功");
      modalVisible.value = false;
      fetchTags(); // 刷新列表
    } else {
      Message.error(res.message || "操作失败");
    }
  } catch (error) {
    console.error("保存标签失败", error);
    Message.error("保存标签失败");
  }
};

// 处理模态框取消
const handleModalCancel = () => {
  modalVisible.value = false;
};

// 删除标签
const deleteTag = async (record: Tag) => {
  if (!record.id) {
    Message.error("标签ID不能为空");
    return;
  }

  try {
    // 设置用户token
    const { token } = useUserStore();
    if (token) {
      OpenAPI.TOKEN = token;
    }

    const res = await TagControllerService.deleteTagUsingPost(record.id);
    console.log("删除标签响应:", res);

    if (res.code === 0) {
      Message.success("标签删除成功");
      fetchTags(); // 刷新列表
    } else {
      Message.error(res.message || "删除失败");
    }
  } catch (error) {
    console.error("删除标签失败", error);
    Message.error("删除标签失败");
  }
};

// 页面加载时获取标签列表
onMounted(() => {
  fetchTags();
});
</script>

<style scoped>
#tagsView {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
</style>
