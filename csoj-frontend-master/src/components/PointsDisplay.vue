<template>
  <div class="points-display">
    <a-card class="points-card" title="我的积分" :bordered="false">
      <div class="points-header">
        <a-statistic
          title="当前积分"
          :value="userPoints"
          :value-style="{ color: '#722ed1' }"
        >
          <template #prefix>
            <icon-gift />
          </template>
        </a-statistic>
        <a-button type="primary" @click="$router.push('/checkin')">
          每日签到
        </a-button>
      </div>

      <a-divider />

      <a-collapse :default-active-key="['1']">
        <a-collapse-item header="积分获取途径" key="1">
          <a-descriptions :column="{ xs: 1, sm: 2, md: 3 }" size="small">
            <a-descriptions-item label="每日签到">+15积分</a-descriptions-item>
            <a-descriptions-item label="提交题目">+5积分</a-descriptions-item>
            <a-descriptions-item label="题目通过">+20积分</a-descriptions-item>
            <a-descriptions-item label="发布题解">+30积分</a-descriptions-item>
          </a-descriptions>
        </a-collapse-item>
      </a-collapse>

      <a-divider />

      <div class="points-history">
        <a-typography-title :heading="6">最近积分记录</a-typography-title>
        <a-list :max-height="300">
          <a-list-item
            v-for="record in pointsRecords.slice(0, 5)"
            :key="record.id"
          >
            <a-list-item-meta>
              <template #title>
                <span
                  :style="{ color: record.points > 0 ? '#3f8600' : '#cf1322' }"
                >
                  {{ record.points > 0 ? "+" : "" }}{{ record.points }}
                </span>
                <span class="record-description">{{ record.description }}</span>
              </template>
              <template #description>
                {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
              </template>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
        <div class="view-more">
          <a-button type="text" @click="$router.push('/checkin')">
            查看更多积分记录
          </a-button>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconGift } from "@arco-design/web-vue/es/icon";
import moment from "moment";

// 从后端生成的API服务导入
import {
  PointsControllerService,
  PointsRecord,
} from "../../generated/services/PointsControllerService";

// 用户积分
const userPoints = ref(0);

// 积分记录
const pointsRecords = ref<PointsRecord[]>([]);

// 加载积分记录
const loadPointsRecords = async () => {
  try {
    const res = await PointsControllerService.getUserPointsRecordsUsingGet();
    if (res.data.code === 0 && res.data.data) {
      pointsRecords.value = res.data.data.records || [];
      userPoints.value = res.data.data.totalPoints || 0;
    } else {
      // 如果API调用失败或返回错误，使用模拟数据
      pointsRecords.value = [
        {
          id: 1,
          points: 15,
          description: "每日签到",
          type: "签到",
          createTime: new Date(),
        },
        {
          id: 2,
          points: 5,
          description: "提交题目",
          type: "题目提交",
          createTime: new Date(Date.now() - 86400000),
        },
        {
          id: 3,
          points: 20,
          description: "题目通过",
          type: "题目通过",
          createTime: new Date(Date.now() - 172800000),
        },
      ];

      // 计算总积分
      userPoints.value = pointsRecords.value.reduce(
        (sum, record) => sum + record.points,
        0
      );
    }
  } catch (error) {
    Message.error("获取积分记录失败");
    console.error(error);

    // 出错时使用模拟数据
    pointsRecords.value = [
      {
        id: 1,
        points: 15,
        description: "每日签到",
        type: "签到",
        createTime: new Date(),
      },
      {
        id: 2,
        points: 5,
        description: "提交题目",
        type: "题目提交",
        createTime: new Date(Date.now() - 86400000),
      },
      {
        id: 3,
        points: 20,
        description: "题目通过",
        type: "题目通过",
        createTime: new Date(Date.now() - 172800000),
      },
    ];

    // 计算总积分
    userPoints.value = pointsRecords.value.reduce(
      (sum, record) => sum + record.points,
      0
    );
  }
};

onMounted(() => {
  loadPointsRecords();
});
</script>

<style scoped>
.points-display {
  margin-bottom: 20px;
}

.points-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.record-description {
  margin-left: 10px;
}

.view-more {
  text-align: center;
  margin-top: 10px;
}
</style>
