<template>
  <div class="system-announcement">
    <a-card title="系统公告" :bordered="false">
      <template #extra>
        <a-tag color="red" v-if="hasNewAnnouncement">新</a-tag>
      </template>

      <a-empty
        v-if="!announcements || announcements.length === 0"
        description="暂无公告"
      />

      <a-list v-else :max-height="300">
        <a-list-item v-for="(item, index) in announcements" :key="index">
          <a-list-item-meta>
            <template #title>
              <div class="announcement-title">
                <span>{{ item.title }}</span>
                <a-tag v-if="item.isNew" color="red" size="small">新</a-tag>
              </div>
            </template>
            <template #description>
              <div class="announcement-time">
                {{ formatTime(item.createTime) }}
              </div>
            </template>
          </a-list-item-meta>
          <div class="announcement-content">{{ item.content }}</div>
        </a-list-item>
      </a-list>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import moment from "moment";
import AnnouncementService from "@/services/AnnouncementService";

// 公告数据
const announcements = ref<any[]>([]);

// 判断是否有新公告
const hasNewAnnouncement = computed(() => {
  return announcements.value.some((item) => item.isNew);
});

// 格式化时间
const formatTime = (time: string | number | Date) => {
  if (!time) return "";
  return moment(time).format("YYYY-MM-DD HH:mm");
};

// 加载公告数据
const loadAnnouncements = async () => {
  try {
    // 调用后端API获取公告数据
    const res = await AnnouncementService.getAnnouncements();
    if (res.code === 0) {
      announcements.value = res.data || [];
    } else {
      Message.error(res.message || "获取公告数据失败");
    }
  } catch (error) {
    Message.error("加载公告数据失败：" + error.message);
  }
};

onMounted(() => {
  loadAnnouncements();
});
</script>

<style scoped>
.system-announcement {
  margin-bottom: 16px;
}

.announcement-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.announcement-time {
  font-size: 12px;
  color: #86909c;
}

.announcement-content {
  margin-top: 8px;
  color: #4e5969;
  font-size: 14px;
  line-height: 1.5;
}
</style>
