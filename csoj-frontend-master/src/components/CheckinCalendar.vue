<template>
  <div class="checkin-calendar">
    <a-card title="每日签到" :bordered="false">
      <template #extra>
        <a-button
          type="primary"
          size="small"
          @click="handleCheckin"
          :disabled="hasCheckedToday"
        >
          {{ hasCheckedToday ? "今日已签到" : "立即签到" }}
        </a-button>
      </template>

      <div class="calendar-container">
        <a-calendar v-model="currentDate" :panel="true" :fullscreen="false">
          <template #cell="{ date }">
            <div
              class="calendar-cell"
              :class="{ 'checked-day': isCheckedDay(date) }"
            >
              {{ date.getDate() }}
              <div v-if="isCheckedDay(date)" class="check-mark">✓</div>
            </div>
          </template>
        </a-calendar>
      </div>

      <div class="checkin-info">
        <p>
          已连续签到: <span class="highlight">{{ consecutiveDays }}</span> 天
        </p>
        <p>
          本月签到: <span class="highlight">{{ monthlyCheckins }}</span> 天
        </p>
        <p>
          总积分: <span class="highlight">{{ totalPoints }}</span>
        </p>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import CheckinService from "@/services/CheckinService";
import { useUserStore } from "@/store/useUserStore";

// 当前日期
const currentDate = ref(new Date());

//签到数据
const checkinDays = ref<Date[]>([]);
const consecutiveDays = ref(0);
const monthlyCheckins = ref(0);
const totalPoints = ref(0);

// 判断是否已经签到
const hasCheckedToday = computed(() => {
  try {
    if (!Array.isArray(checkinDays.value)) {
      return false;
    }
    const today = new Date();
    const todayNormalized = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate()
    );

    return checkinDays.value.some((date) => {
      if (!(date instanceof Date)) return false;
      const dateNormalized = new Date(
        date.getFullYear(),
        date.getMonth(),
        date.getDate()
      );
      return dateNormalized.getTime() === todayNormalized.getTime();
    });
  } catch (error) {
    console.error("检查今日签到状态时出错:", error);
    return false;
  }
});

// 签到操作
const handleCheckin = async () => {
  if (hasCheckedToday.value) {
    Message.warning("今日已签到");
    return;
  }

  try {
    // 调用签到服务
    const res = await CheckinService.checkin();
    console.log("签到响应:", res);
    console.log("签到响应码:", res.code);
    console.log("签到响应数据:", res.data);
    console.log("签到响应值:", res.data.success);

    if (res.success) {
      // 签到成功，重新加载签到数据
      await loadCheckinData();
      Message.success(`签到成功，获得${res.data.points}积分！`);
    } else {
      console.error("签到失败:", res.message);
      Message.error(res.message || "签到失败");
    }
  } catch (error) {
    console.error("签到错误:", error);
    const errorMessage = error instanceof Error ? error.message : "未知错误";
    Message.error("签到失败：" + errorMessage);
  }
};

// 加载签到数据
const loadCheckinData = async () => {
  try {
    const res = await CheckinService.getUserCheckinRecords();
    console.log("签到数据响应:", res);

    // 处理 API 响应数据
    if (res && res.code === 0) {
      if (res.data && res.data.records && Array.isArray(res.data.records)) {
        // 处理签到记录数据，从checkinDate字段提取签到日期
        const checkinRecords = res.data.records;

        // 确保日期格式正确处理
        checkinDays.value = checkinRecords.map((record: any) => {
          const date = new Date(record.checkinDate);
          // 重置时间为当天的00:00:00，避免时区问题
          return new Date(date.getFullYear(), date.getMonth(), date.getDate());
        });

        console.log("处理后的签到日期:", checkinDays.value);
        console.log("原始签到记录:", checkinRecords);
      } else {
        checkinDays.value = [];
      }

      // 计算统计数据
      consecutiveDays.value = calculateConsecutiveDays();
      monthlyCheckins.value = getCurrentMonthCheckins();

      // 计算总积分（每次签到15积分）
      if (res.data && res.data.records && Array.isArray(res.data.records)) {
        totalPoints.value = res.data.records.length * 15;
      }
    } else {
      console.warn("获取签到数据失败:", res);
      // 不显示错误消息，只是设置空数据
      checkinDays.value = [];
      consecutiveDays.value = 0;
      monthlyCheckins.value = 0;
      totalPoints.value = 0;
    }
  } catch (error) {
    console.error("签到数据加载出错:", error);
    // 设置默认值而不是显示错误
    checkinDays.value = [];
    consecutiveDays.value = 0;
    monthlyCheckins.value = 0;
    totalPoints.value = 0;
  }
};

// 计算属性：今天是否已签到
const isCheckedDay = (date: Date) => {
  try {
    if (!date || !(date instanceof Date) || !Array.isArray(checkinDays.value)) {
      return false;
    }
    const targetDate = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate()
    );

    return checkinDays.value.some((d) => {
      if (!(d instanceof Date)) return false;
      const checkinDate = new Date(d.getFullYear(), d.getMonth(), d.getDate());
      return checkinDate.getTime() === targetDate.getTime();
    });
  } catch (error) {
    console.error("检查签到日期时出错:", error);
    return false;
  }
};

// 计算连续签到天数
const calculateConsecutiveDays = () => {
  try {
    if (!Array.isArray(checkinDays.value) || checkinDays.value.length === 0) {
      return 0;
    }

    // 对签到日期进行排序（从最新到最旧）
    const sortedDates = [...checkinDays.value]
      .map(
        (date) => new Date(date.getFullYear(), date.getMonth(), date.getDate())
      )
      .sort((a, b) => b.getTime() - a.getTime());

    const today = new Date();
    const todayNormalized = new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate()
    );

    let count = 0;
    let expectedDate = new Date(todayNormalized);

    // 从今天开始往前推，检查连续签到
    for (const checkinDate of sortedDates) {
      if (checkinDate.getTime() === expectedDate.getTime()) {
        count++;
        expectedDate.setDate(expectedDate.getDate() - 1);
      } else if (checkinDate.getTime() < expectedDate.getTime()) {
        // 如果签到日期早于期望日期，说明连续性中断
        break;
      }

      // 防止无限循环
      if (count > 365) {
        console.warn("连续签到天数超过365天，可能存在计算错误");
        break;
      }
    }

    return count;
  } catch (error) {
    console.error("计算连续签到天数时出错:", error);
    return 0;
  }
};

// 获取当月签到天数
const getCurrentMonthCheckins = () => {
  const now = new Date();
  const currentMonth = now.getMonth();
  const currentYear = now.getFullYear();

  return checkinDays.value.filter((date) => {
    return (
      date.getMonth() === currentMonth && date.getFullYear() === currentYear
    );
  }).length;
};

onMounted(() => {
  loadCheckinData();
});
</script>

<style scoped>
.checkin-calendar {
  margin-bottom: 16px;
}

.calendar-container {
  margin-bottom: 16px;
  overflow: hidden;
}

.calendar-cell {
  position: relative;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.2s ease;
}

.checked-day {
  color: #52c41a;
  font-weight: bold;
  background-color: rgba(82, 196, 26, 0.1);
  border-radius: 4px;
  border: 1px solid rgba(82, 196, 26, 0.3);
}

.check-mark {
  position: absolute;
  bottom: 2px;
  right: 2px;
  font-size: 10px;
  color: #165dff;
}

.checkin-info {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.checkin-info p {
  margin: 4px 0;
  flex: 1 1 auto;
}

.highlight {
  color: #165dff;
  font-weight: bold;
  margin: 0 4px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .calendar-container :deep(.arco-picker-cell) {
    padding: 0;
  }

  .calendar-container :deep(.arco-calendar-cell) {
    height: 32px;
  }

  .checkin-info {
    flex-direction: column;
  }
}
</style>
