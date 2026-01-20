<template>
  <div class="checkin-container">
    <a-card class="checkin-card" title="每日签到" :bordered="false">
      <div class="checkin-content">
        <div class="checkin-status">
          <div v-if="checkedInToday" class="checked-in">
            <a-result
              status="success"
              title="今日已签到"
              sub-title="明天再来获取更多积分吧！"
            >
              <template #extra>
                <div class="points-info">
                  <a-statistic
                    title="今日获得积分"
                    :value="15"
                    :value-style="{
                      color:
                        typeof document !== 'undefined' &&
                        document.documentElement
                          ? getComputedStyle(document.documentElement)
                              .getPropertyValue('--success-color')
                              .trim() || '#3f8600'
                          : '#3f8600',
                    }"
                  >
                    <template #prefix>
                      <icon-red-envelope />
                    </template>
                  </a-statistic>
                </div>
              </template>
            </a-result>
          </div>
          <div v-else class="not-checked-in">
            <a-result title="每日签到" sub-title="签到即可获得15积分">
              <template #icon>
                <icon-calendar />
              </template>
              <template #extra>
                <a-button type="primary" size="large" @click="handleCheckin">
                  立即签到
                </a-button>
              </template>
            </a-result>
          </div>
        </div>

        <div class="checkin-calendar">
          <a-typography-title :heading="5">本月签到记录</a-typography-title>
          <a-calendar
            :panel-mode="'month'"
            :default-value="new Date()"
            :disabled-date="disabledDate"
            :disabled-cell="() => false"
            :cell-render="cellRender"
          />
        </div>
      </div>
    </a-card>

    <a-card class="points-card" title="我的积分" :bordered="false">
      <div class="points-header">
        <a-statistic
          title="当前积分"
          :value="userPoints"
          :value-style="{
            color:
              typeof document !== 'undefined' && document.documentElement
                ? getComputedStyle(document.documentElement)
                    .getPropertyValue('--primary-color')
                    .trim() || '#722ed1'
                : '#722ed1',
          }"
        >
          <template #prefix>
            <icon-gift />
          </template>
        </a-statistic>
        <a-button type="primary" @click="loadPointsRecords">刷新</a-button>
      </div>

      <a-divider />

      <a-table
        :columns="columns"
        :data="pointsRecords"
        :pagination="{
          showTotal: true,
          pageSize: 5,
        }"
      >
        <template #points="{ record }">
          <span
            :style="{
              color:
                record.points > 0
                  ? typeof document !== 'undefined' && document.documentElement
                    ? getComputedStyle(document.documentElement)
                        .getPropertyValue('--success-color')
                        .trim() || '#3f8600'
                    : '#3f8600'
                  : typeof document !== 'undefined' && document.documentElement
                  ? getComputedStyle(document.documentElement)
                      .getPropertyValue('--danger-color')
                      .trim() || '#cf1322'
                  : '#cf1322',
            }"
          >
            {{ record.points > 0 ? "+" : "" }}{{ record.points }}
          </span>
        </template>
        <template #createTime="{ record }">
          {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, h } from "vue";
import { Message } from "@arco-design/web-vue";
import { useStore } from "vuex";
import moment from "moment";
import {
  IconCalendar,
  IconRedEnvelope,
  IconGift,
  IconCheckCircle,
} from "@arco-design/web-vue/es/icon";

// 从后端生成的API服务导入
import CheckinService from "@/services/CheckinService";
import { CheckinRecord } from "../../../generated/services/CheckinControllerService";
import {
  PointsControllerService,
  PointsRecord,
} from "../../../generated/services/PointsControllerService";

// 用户信息

const store = useStore();

// 用户信息
const user = ref(store.state.user.loginUser || {});

// 用户积分
const userPoints = ref(0);

// 今日是否已签到
const checkedInToday = ref(false);

// 签到记录（用于日历展示）
const checkinRecords = ref([]);

// 积分记录
const pointsRecords = ref([]);

// 表格列定义
const columns = [
  {
    title: "积分变动",
    dataIndex: "points",
    slotName: "points",
  },
  {
    title: "描述",
    dataIndex: "description",
  },
  {
    title: "类型",
    dataIndex: "type",
  },
  {
    title: "时间",
    dataIndex: "createTime",
    slotName: "createTime",
  },
];

// 禁用未来日期
const disabledDate = (date: Date) => {
  return date > new Date();
};

// 自定义日历单元格渲染
const cellRender = (current: Date) => {
  // 检查当前日期是否在签到记录中
  const currentDateStr = moment(current).format("YYYY-MM-DD");
  const isCheckedIn = checkinRecords.value.some(
    (record) => record.checkinDate === currentDateStr
  );

  if (isCheckedIn) {
    return h(
      "div",
      {
        class: "calendar-cell checked-in-date",
        style: {
          position: "relative",
          height: "100%",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
        },
      },
      [
        h("span", current.getDate().toString()),
        h(IconCheckCircle, {
          style: {
            position: "absolute",
            bottom: "2px",
            right: "2px",
            color: "#52c41a",
            fontSize: "12px",
          },
        }),
      ]
    );
  }
  return null;
};

// 加载签到记录
const loadCheckinRecords = async () => {
  try {
    const res = await CheckinService.getUserCheckinRecords();
    console.log("签到记录响应:", res); // 调试日志

    // 处理API响应数据，根据实际返回结构
    if (res && res.code === 0) {
      if (res.data && res.data.records && Array.isArray(res.data.records)) {
        // 处理签到记录数据
        const checkinData = res.data.records;

        // 转换为日历组件需要的格式
        checkinRecords.value = checkinData.map((record: any) => ({
          checkinDate: moment(record.checkinDate).format("YYYY-MM-DD"), // 转换为YYYY-MM-DD格式
          points: 15, // 每次签到固定15积分
          createTime: record.createTime,
        }));

        // 检查今日是否已签到
        const today = moment().format("YYYY-MM-DD");
        checkedInToday.value = checkinData.some(
          (record: any) =>
            moment(record.checkinDate).format("YYYY-MM-DD") === today
        );

        console.log("处理后的签到记录:", checkinRecords.value);
        console.log("今日是否已签到:", checkedInToday.value);
      } else {
        checkinRecords.value = [];
        checkedInToday.value = false;
      }
    } else {
      console.warn("API返回错误或无数据:", res);
      checkinRecords.value = [];
      checkedInToday.value = false;
    }
  } catch (error) {
    console.error("获取签到记录失败:", error);
    // 不显示错误消息，避免干扰用户体验
    checkinRecords.value = [];
    checkedInToday.value = false;
  }
};

// 加载积分记录
const loadPointsRecords = async () => {
  try {
    // 使用与签到记录相同的API接口，避免重复调用
    const res = await CheckinService.getUserCheckinRecords();
    console.log("积分记录响应:", res); // 调试日志

    if (
      res &&
      res.code === 0 &&
      res.data &&
      res.data.records &&
      Array.isArray(res.data.records)
    ) {
      // 处理签到记录数据，转换为积分记录格式
      pointsRecords.value = res.data.records.map((record: any) => ({
        id: record.id,
        points: 15, // 每次签到固定15积分
        description: "每日签到",
        type: "签到",
        createTime: new Date(record.createTime),
      }));

      // 计算总积分
      userPoints.value = res.data.records.length * 15;

      console.log("处理后的积分记录:", pointsRecords.value);
      console.log("总积分:", userPoints.value);
    } else {
      console.warn("获取积分记录失败:", res);
      pointsRecords.value = [];
      userPoints.value = 0;
    }
  } catch (error) {
    console.error("获取积分记录失败:", error);
    // 不显示错误消息，避免干扰用户体验
    pointsRecords.value = [];
    userPoints.value = 0;
  }
};

// 签到处理
const handleCheckin = async () => {
  if (checkedInToday.value) {
    Message.warning("今日已签到");
    return;
  }

  try {
    const res = await CheckinService.checkin();
    console.log("签到响应:", res); // 调试日志
    if (res.code === 0) {
      Message.success("签到成功，获得15积分");
      // 重新加载签到记录和积分记录
      await loadCheckinRecords();
      await loadPointsRecords();
    } else {
      Message.error("签到失败：" + (res.message || "未知错误"));
    }
  } catch (error) {
    console.error("签到失败:", error);
    Message.error("签到失败，请稍后重试");
  }
};

// 处理ResizeObserver循环错误
const resizeObserverErrorHandler = (e: ErrorEvent) => {
  if (
    e.message ===
    "ResizeObserver loop completed with undelivered notifications."
  ) {
    e.stopImmediatePropagation();
    return false;
  }
};

onMounted(() => {
  loadCheckinRecords();
  loadPointsRecords();

  // 添加错误处理监听器
  window.addEventListener("error", resizeObserverErrorHandler);
});

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener("error", resizeObserverErrorHandler);
});
</script>

<style scoped>
.checkin-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.checkin-card,
.points-card {
  margin-bottom: 20px;
}

.checkin-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.points-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.calendar-cell {
  position: relative;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.2s ease;
}

.checked-in-date {
  background-color: rgba(82, 196, 26, 0.1);
  border-radius: 4px;
  border: 1px solid rgba(82, 196, 26, 0.3);
  color: #52c41a;
  font-weight: bold;
}

@media (max-width: 768px) {
  .checkin-content {
    flex-direction: column;
  }
}
</style>
