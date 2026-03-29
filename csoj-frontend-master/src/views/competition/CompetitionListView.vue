<template>
  <div id="competitionListView" class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">{{ $t("competition.title") }}</h1>
      <a-button type="primary" @click="showCreateModal = true">
        <template #icon><icon-plus /></template>
        {{ $t("competition.createCompetition") }}
      </a-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <a-form :model="searchForm" layout="inline">
        <a-form-item :label="$t('competition.searchName')">
          <a-input
            v-model="searchForm.name"
            :placeholder="$t('competition.searchPlaceholder')"
            allow-clear
            @press-enter="searchCompetitions"
          />
        </a-form-item>
        <a-form-item :label="$t('competition.type')">
          <a-select
            v-model="searchForm.type"
            :placeholder="$t('competition.selectType')"
            allow-clear
            style="width: 150px"
          >
            <a-option value="elimination">{{
              $t("competition.types.elimination")
            }}</a-option>
            <a-option value="round_robin">{{
              $t("competition.types.roundRobin")
            }}</a-option>
            <a-option value="team_ac">{{
              $t("competition.types.teamAc")
            }}</a-option>
            <a-option value="battle">{{
              $t("competition.types.battle")
            }}</a-option>
            <a-option value="relay">{{
              $t("competition.types.relay")
            }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('competition.status')">
          <a-select
            v-model="searchForm.status"
            :placeholder="$t('competition.selectStatus')"
            allow-clear
            style="width: 120px"
          >
            <a-option :value="0">{{
              $t("competition.statuses.upcoming")
            }}</a-option>
            <a-option :value="1">{{
              $t("competition.statuses.registration")
            }}</a-option>
            <a-option :value="2">{{
              $t("competition.statuses.ongoing")
            }}</a-option>
            <a-option :value="3">{{
              $t("competition.statuses.ended")
            }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="searchCompetitions">
            <template #icon><icon-search /></template>
            {{ $t("common.search") }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 比赛列表 -->
    <div class="competition-list">
      <a-row :gutter="[16, 16]">
        <a-col
          v-for="competition in competitionList"
          :key="competition.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <a-card
            class="competition-card"
            hoverable
            @click="viewCompetition(competition.id)"
          >
            <template #cover>
              <div class="competition-cover">
                <img
                  v-if="competition.cover"
                  :src="competition.cover"
                  alt="cover"
                />
                <icon-trophy v-else :size="48" />
              </div>
            </template>
            <a-card-meta
              :title="competition.name"
              :description="competition.description"
            />
            <div class="competition-info">
              <div class="info-row">
                <span>
                  <icon-user-group /> {{ competition.currentTeams }}/{{
                    competition.maxTeams || "∞"
                  }}
                </span>
                <a-tag :color="getStatusColor(competition.status)">
                  {{ competition.statusName }}
                </a-tag>
              </div>
              <div class="info-row">
                <span class="type-tag">
                  <icon-apps /> {{ competition.typeName }}
                </span>
              </div>
              <div class="info-row time-info">
                <span v-if="competition.startTime">
                  <icon-clock-circle />
                  {{ formatDate(competition.startTime) }}
                </span>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 空状态 -->
      <a-empty
        v-if="competitionList.length === 0 && !loading"
        :description="$t('competition.noCompetitions')"
      />

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <a-button @click="loadMore" :loading="loading">
          {{ $t("common.loadMore") }}
        </a-button>
      </div>
    </div>

    <!-- 创建比赛弹窗 -->
    <a-modal
      v-model:visible="showCreateModal"
      :title="$t('competition.createCompetition')"
      :ok-loading="createLoading"
      @ok="handleCreateCompetition"
      @cancel="resetCreateForm"
      width="600px"
    >
      <a-form :model="createForm" layout="vertical">
        <a-form-item :label="$t('competition.competitionName')" required>
          <a-input
            v-model="createForm.name"
            :placeholder="$t('competition.namePlaceholder')"
          />
        </a-form-item>
        <a-form-item :label="$t('competition.description')">
          <a-textarea
            v-model="createForm.description"
            :placeholder="$t('competition.descriptionPlaceholder')"
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="$t('competition.type')" required>
              <a-select v-model="createForm.type">
                <a-option value="elimination">{{
                  $t("competition.types.elimination")
                }}</a-option>
                <a-option value="round_robin">{{
                  $t("competition.types.roundRobin")
                }}</a-option>
                <a-option value="team_ac">{{
                  $t("competition.types.teamAc")
                }}</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="$t('competition.maxTeams')">
              <a-input-number
                v-model="createForm.maxTeams"
                :min="2"
                :max="64"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="$t('competition.minTeamSize')">
              <a-input-number
                v-model="createForm.minTeamSize"
                :min="1"
                :max="10"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="$t('competition.maxTeamSize')">
              <a-input-number
                v-model="createForm.maxTeamSize"
                :min="1"
                :max="10"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="$t('competition.registerStartTime')">
              <a-date-picker
                v-model="createForm.registerStartTime"
                show-time
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="$t('competition.registerEndTime')">
              <a-date-picker
                v-model="createForm.registerEndTime"
                show-time
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="$t('competition.startTime')" required>
              <a-date-picker
                v-model="createForm.startTime"
                show-time
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="$t('competition.endTime')">
              <a-date-picker
                v-model="createForm.endTime"
                show-time
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item :label="$t('competition.timeLimit')">
          <a-input-number
            v-model="createForm.timeLimit"
            :min="10"
            :max="180"
            :placeholder="$t('competition.timeLimitPlaceholder')"
            style="width: 200px"
          />
          <span class="ml-2">{{ $t("competition.minutes") }}</span>
        </a-form-item>
        <a-form-item :label="$t('competition.isPublic')">
          <a-switch
            v-model="createForm.isPublic"
            :checked-value="1"
            :unchecked-value="0"
          />
          <span class="ml-2">{{
            createForm.isPublic
              ? $t("competition.public")
              : $t("competition.private")
          }}</span>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconPlus,
  IconSearch,
  IconTrophy,
  IconUserGroup,
  IconApps,
  IconClockCircle,
} from "@arco-design/web-vue/es/icon";
import dayjs from "dayjs";

const router = useRouter();

// 搜索表单
const searchForm = ref({
  name: "",
  type: undefined as string | undefined,
  status: undefined as number | undefined,
});

// 比赛列表
const competitionList = ref<any[]>([]);
const loading = ref(false);
const hasMore = ref(true);
const current = ref(1);
const pageSize = 12;

// 创建比赛
const showCreateModal = ref(false);
const createLoading = ref(false);
const createForm = ref({
  name: "",
  description: "",
  type: "team_ac",
  maxTeams: 16,
  minTeamSize: 1,
  maxTeamSize: 5,
  registerStartTime: "",
  registerEndTime: "",
  startTime: "",
  endTime: "",
  timeLimit: 60,
  isPublic: 1,
});

// 搜索比赛
const searchCompetitions = async () => {
  current.value = 1;
  competitionList.value = [];
  await loadCompetitions();
};

// 加载比赛列表
const loadCompetitions = async () => {
  loading.value = true;
  try {
    // TODO: 调用实际的API
    // const res = await CompetitionControllerService.listCompetitionsUsingGet({
    //   name: searchForm.value.name,
    //   type: searchForm.value.type,
    //   status: searchForm.value.status,
    //   current: current.value,
    //   pageSize,
    // });

    // 模拟数据
    const mockData = [
      {
        id: 1,
        name: "Spring Coding Challenge 2026",
        description: "Annual spring programming competition",
        type: "team_ac",
        typeName: "团队AC赛",
        status: 1,
        statusName: "报名中",
        currentTeams: 8,
        maxTeams: 16,
        startTime: new Date("2026-03-15T10:00:00"),
      },
      {
        id: 2,
        name: "Algorithm Battle Tournament",
        description: "Compete in elimination-style matches",
        type: "elimination",
        typeName: "淘汰赛",
        status: 2,
        statusName: "进行中",
        currentTeams: 32,
        maxTeams: 32,
        startTime: new Date("2026-03-01T09:00:00"),
      },
    ];

    if (current.value === 1) {
      competitionList.value = mockData;
    } else {
      competitionList.value.push(...mockData);
    }
    hasMore.value = false;
  } catch (error) {
    console.error("加载比赛列表失败:", error);
  } finally {
    loading.value = false;
  }
};

// 加载更多
const loadMore = async () => {
  current.value++;
  await loadCompetitions();
};

// 查看比赛详情
const viewCompetition = (competitionId: number) => {
  router.push(`/competition/${competitionId}`);
};

// 创建比赛
const handleCreateCompetition = async () => {
  if (!createForm.value.name.trim()) {
    Message.warning("请输入比赛名称");
    return;
  }

  createLoading.value = true;
  try {
    // TODO: 调用实际的API
    // const res = await CompetitionControllerService.createCompetitionUsingPost(createForm.value);
    Message.success("创建成功");
    showCreateModal.value = false;
    resetCreateForm();
    loadCompetitions();
  } catch (error) {
    Message.error("创建失败");
  } finally {
    createLoading.value = false;
  }
};

// 重置创建表单
const resetCreateForm = () => {
  createForm.value = {
    name: "",
    description: "",
    type: "team_ac",
    maxTeams: 16,
    minTeamSize: 1,
    maxTeamSize: 5,
    registerStartTime: "",
    registerEndTime: "",
    startTime: "",
    endTime: "",
    timeLimit: 60,
    isPublic: 1,
  };
};

// 获取状态颜色
const getStatusColor = (status: number) => {
  switch (status) {
    case 0:
      return "gray";
    case 1:
      return "green";
    case 2:
      return "blue";
    case 3:
      return "orange";
    default:
      return "gray";
  }
};

// 格式化日期
const formatDate = (date: Date | string) => {
  return dayjs(date).format("YYYY-MM-DD HH:mm");
};

onMounted(() => {
  loadCompetitions();
});
</script>

<style scoped>
#competitionListView {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.search-area {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--bg-color-secondary);
  border-radius: 8px;
}

.competition-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.competition-card:hover {
  transform: translateY(-4px);
}

.competition-cover {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
  background: linear-gradient(
    135deg,
    var(--primary-light-color),
    var(--primary-lighter-color)
  );
  color: var(--primary-color);
}

.competition-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.competition-info {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color-light);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--text-color-secondary);
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.time-info {
  font-size: 12px;
}

.type-tag {
  padding: 2px 8px;
  background: var(--bg-color-tertiary);
  border-radius: 4px;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.ml-2 {
  margin-left: 8px;
}
</style>
