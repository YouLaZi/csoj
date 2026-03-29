<template>
  <div id="competitionDetailView" class="page-container">
    <a-spin :loading="loading" style="width: 100%">
      <!-- 比赛信息卡片 -->
      <div v-if="competition" class="competition-header">
        <div class="competition-info">
          <div class="competition-cover">
            <img
              v-if="competition.cover"
              :src="competition.cover"
              alt="cover"
            />
            <icon-trophy v-else :size="48" />
          </div>
          <div class="competition-meta">
            <h1 class="competition-name">{{ competition.name }}</h1>
            <div class="competition-badges">
              <a-tag :color="getStatusColor(competition.status)">
                {{ competition.statusName }}
              </a-tag>
              <a-tag color="blue">{{ competition.typeName }}</a-tag>
              <a-tag v-if="competition.isPublic" color="green">{{
                $t("competition.public")
              }}</a-tag>
              <a-tag v-else color="gray">{{ $t("competition.private") }}</a-tag>
            </div>
            <p class="competition-description">
              {{ competition.description || $t("competition.noDescription") }}
            </p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="competition-actions">
          <template v-if="competition.status === 1">
            <!-- 报名阶段 -->
            <template v-if="!competition.hasRegistered">
              <a-button type="primary" @click="showRegisterModal = true">
                <template #icon><icon-plus /></template>
                {{ $t("competition.register") }}
              </a-button>
            </template>
            <template v-else>
              <a-button disabled>
                {{ $t("competition.alreadyRegistered") }}
              </a-button>
              <a-popconfirm
                :content="$t('competition.cancelRegisterConfirm')"
                @ok="handleCancelRegister"
              >
                <a-button status="danger">
                  {{ $t("competition.cancelRegister") }}
                </a-button>
              </a-popconfirm>
            </template>
          </template>
          <template v-if="competition.status === 2">
            <!-- 进行中 -->
            <a-button type="primary" @click="activeTab = 'matches'">
              <template #icon><icon-eye /></template>
              {{ $t("competition.viewMatches") }}
            </a-button>
          </template>
          <!-- 管理员操作 -->
          <template v-if="isCreator">
            <a-button
              v-if="competition.status === 1"
              type="primary"
              @click="handleStartCompetition"
            >
              {{ $t("competition.startCompetition") }}
            </a-button>
            <a-button
              v-if="competition.status === 1"
              @click="handleGenerateMatches"
            >
              {{ $t("competition.generateMatches") }}
            </a-button>
            <a-button
              v-if="competition.status === 2"
              status="warning"
              @click="handleEndCompetition"
            >
              {{ $t("competition.endCompetition") }}
            </a-button>
          </template>
        </div>
      </div>

      <!-- 统计信息 -->
      <div v-if="competition" class="competition-stats-row">
        <div class="stat-item">
          <span class="stat-value">{{ competition.currentTeams }}</span>
          <span class="stat-label">{{
            $t("competition.registeredTeams")
          }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ competition.maxTeams || "∞" }}</span>
          <span class="stat-label">{{ $t("competition.maxTeams") }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-value"
            >{{ competition.minTeamSize }}-{{ competition.maxTeamSize }}</span
          >
          <span class="stat-label">{{ $t("competition.teamSize") }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ competition.timeLimit || "∞" }}</span>
          <span class="stat-label">{{ $t("competition.timeLimit") }}</span>
        </div>
      </div>

      <!-- 时间信息 -->
      <div v-if="competition" class="time-info-card">
        <div class="time-row">
          <div class="time-item">
            <icon-calendar />
            <span class="time-label"
              >{{ $t("competition.registerTime") }}:</span
            >
            <span class="time-value">
              {{ formatDate(competition.registerStartTime) }} ~
              {{ formatDate(competition.registerEndTime) }}
            </span>
          </div>
        </div>
        <div class="time-row">
          <div class="time-item">
            <icon-clock-circle />
            <span class="time-label"
              >{{ $t("competition.competitionTime") }}:</span
            >
            <span class="time-value">
              {{ formatDate(competition.startTime) }} ~
              {{ formatDate(competition.endTime) }}
            </span>
          </div>
        </div>
      </div>

      <!-- Tab 切换 -->
      <a-tabs v-model:active-key="activeTab" class="competition-tabs">
        <a-tab-pane
          key="registrations"
          :title="$t('competition.registrations')"
        >
          <div class="registration-list">
            <a-list :bordered="false">
              <a-list-item v-for="team in registrations" :key="team.id">
                <a-list-item-meta
                  :title="team.name"
                  :description="team.description"
                >
                  <template #avatar>
                    <a-avatar :image-url="team.avatar">
                      <icon-user-group v-if="!team.avatar" />
                    </a-avatar>
                  </template>
                </a-list-item-meta>
                <template #extra>
                  <span class="team-members">
                    <icon-user /> {{ team.currentMembers }}/{{
                      team.maxMembers
                    }}
                  </span>
                </template>
              </a-list-item>
            </a-list>
            <a-empty
              v-if="registrations.length === 0"
              :description="$t('competition.noRegistrations')"
            />
          </div>
        </a-tab-pane>

        <a-tab-pane key="matches" :title="$t('competition.matches')">
          <div class="match-list">
            <div v-for="match in matches" :key="match.id" class="match-card">
              <div class="match-header">
                <span class="match-round"
                  >{{ $t("competition.round") }} {{ match.round }}</span
                >
                <a-tag :color="getMatchStatusColor(match.status)">
                  {{ getMatchStatusName(match.status) }}
                </a-tag>
              </div>
              <div class="match-teams">
                <div class="team team-a">
                  <span class="team-name">{{
                    match.teamA?.name || "TBD"
                  }}</span>
                  <span class="team-score">{{ match.teamAScore || 0 }}</span>
                </div>
                <span class="vs">VS</span>
                <div class="team team-b">
                  <span class="team-name">{{
                    match.teamB?.name || "TBD"
                  }}</span>
                  <span class="team-score">{{ match.teamBScore || 0 }}</span>
                </div>
              </div>
            </div>
            <a-empty
              v-if="matches.length === 0"
              :description="$t('competition.noMatches')"
            />
          </div>
        </a-tab-pane>

        <a-tab-pane key="rules" :title="$t('competition.rules')">
          <div class="rules-content">
            <h3>{{ $t("competition.competitionRules") }}</h3>
            <p>{{ $t("competition.rulesDescription") }}</p>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-spin>

    <!-- 报名弹窗 -->
    <a-modal
      v-model:visible="showRegisterModal"
      :title="$t('competition.register')"
      :ok-loading="registerLoading"
      @ok="handleRegister"
    >
      <a-form layout="vertical">
        <a-form-item :label="$t('competition.selectTeam')" required>
          <a-select
            v-model="selectedTeamId"
            :placeholder="$t('competition.selectTeamPlaceholder')"
          >
            <a-option v-for="team in myTeams" :key="team.id" :value="team.id">
              {{ team.name }} ({{ team.currentMembers
              }}{{ $t("competition.members") }})
            </a-option>
          </a-select>
        </a-form-item>
      </a-form>
      <a-empty
        v-if="myTeams.length === 0"
        :description="$t('competition.noTeams')"
      >
        <a-button type="primary" @click="router.push('/team')">
          {{ $t("competition.createTeamFirst") }}
        </a-button>
      </a-empty>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconTrophy,
  IconPlus,
  IconEye,
  IconUserGroup,
  IconUser,
  IconCalendar,
  IconClockCircle,
} from "@arco-design/web-vue/es/icon";
import dayjs from "dayjs";

const route = useRoute();
const router = useRouter();

const competitionId = computed(() => Number(route.params.id));

const loading = ref(false);
const competition = ref<any>(null);
const registrations = ref<any[]>([]);
const matches = ref<any[]>([]);
const activeTab = ref("registrations");

// 报名相关
const showRegisterModal = ref(false);
const registerLoading = ref(false);
const selectedTeamId = ref<number | undefined>();
const myTeams = ref<any[]>([]);

// 是否为创建者
const isCreator = computed(() => {
  // TODO: 检查当前用户是否为比赛创建者
  return false;
});

// 加载比赛详情
const loadCompetition = async () => {
  loading.value = true;
  try {
    // TODO: 调用实际的API
    // const res = await CompetitionControllerService.getCompetitionUsingGet(competitionId.value);

    // 模拟数据
    competition.value = {
      id: competitionId.value,
      name: "Spring Coding Challenge 2026",
      description: "Annual spring programming competition for teams",
      type: "team_ac",
      typeName: "团队AC赛",
      status: 1,
      statusName: "报名中",
      currentTeams: 8,
      maxTeams: 16,
      minTeamSize: 1,
      maxTeamSize: 5,
      timeLimit: 60,
      isPublic: 1,
      hasRegistered: false,
      registerStartTime: new Date("2026-03-01T00:00:00"),
      registerEndTime: new Date("2026-03-14T23:59:59"),
      startTime: new Date("2026-03-15T10:00:00"),
      endTime: new Date("2026-03-15T15:00:00"),
    };

    // 加载报名列表
    registrations.value = [
      {
        id: 1,
        name: "Code Warriors",
        description: "Team from Beijing",
        currentMembers: 4,
        maxMembers: 5,
      },
      {
        id: 2,
        name: "Algorithm Masters",
        description: "Team from Shanghai",
        currentMembers: 3,
        maxMembers: 5,
      },
    ];

    // 加载我的团队
    myTeams.value = [
      { id: 1, name: "My Team", currentMembers: 3, maxMembers: 5 },
    ];
  } catch (error) {
    Message.error("加载比赛详情失败");
  } finally {
    loading.value = false;
  }
};

// 报名比赛
const handleRegister = async () => {
  if (!selectedTeamId.value) {
    Message.warning("请选择参赛团队");
    return;
  }

  registerLoading.value = true;
  try {
    // TODO: 调用实际的API
    Message.success("报名成功");
    showRegisterModal.value = false;
    loadCompetition();
  } catch (error) {
    Message.error("报名失败");
  } finally {
    registerLoading.value = false;
  }
};

// 取消报名
const handleCancelRegister = async () => {
  try {
    // TODO: 调用实际的API
    Message.success("已取消报名");
    loadCompetition();
  } catch (error) {
    Message.error("取消失败");
  }
};

// 开始比赛
const handleStartCompetition = async () => {
  try {
    // TODO: 调用实际的API
    Message.success("比赛已开始");
    loadCompetition();
  } catch (error) {
    Message.error("操作失败");
  }
};

// 结束比赛
const handleEndCompetition = async () => {
  try {
    // TODO: 调用实际的API
    Message.success("比赛已结束");
    loadCompetition();
  } catch (error) {
    Message.error("操作失败");
  }
};

// 生成对阵
const handleGenerateMatches = async () => {
  try {
    // TODO: 调用实际的API
    Message.success("对阵已生成");
    loadCompetition();
  } catch (error) {
    Message.error("操作失败");
  }
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

// 获取对阵状态颜色
const getMatchStatusColor = (status: number) => {
  switch (status) {
    case 0:
      return "gray";
    case 1:
      return "blue";
    case 2:
      return "green";
    default:
      return "gray";
  }
};

// 获取对阵状态名称
const getMatchStatusName = (status: number) => {
  switch (status) {
    case 0:
      return "未开始";
    case 1:
      return "进行中";
    case 2:
      return "已结束";
    default:
      return "未知";
  }
};

// 格式化日期
const formatDate = (date: Date | string) => {
  if (!date) return "TBD";
  return dayjs(date).format("YYYY-MM-DD HH:mm");
};

onMounted(() => {
  loadCompetition();
});
</script>

<style scoped>
#competitionDetailView {
  max-width: 900px;
  margin: 0 auto;
}

.competition-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  background: var(--bg-color-secondary);
  border-radius: 12px;
  margin-bottom: 24px;
}

.competition-info {
  display: flex;
  gap: 20px;
}

.competition-cover {
  width: 120px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(
    135deg,
    var(--primary-light-color),
    var(--primary-lighter-color)
  );
  border-radius: 12px;
  color: var(--primary-color);
  flex-shrink: 0;
}

.competition-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

.competition-meta {
  flex: 1;
}

.competition-name {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
}

.competition-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.competition-description {
  margin: 0;
  color: var(--text-color-secondary);
  font-size: 14px;
}

.competition-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.competition-stats-row {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background: var(--bg-color-secondary);
  border-radius: 8px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--primary-color);
}

.stat-label {
  font-size: 13px;
  color: var(--text-color-secondary);
}

.time-info-card {
  padding: 16px;
  background: var(--bg-color-secondary);
  border-radius: 8px;
  margin-bottom: 24px;
}

.time-row {
  margin-bottom: 8px;
}

.time-row:last-child {
  margin-bottom: 0;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.time-label {
  color: var(--text-color-secondary);
}

.time-value {
  color: var(--text-color);
}

.competition-tabs {
  background: var(--bg-color-secondary);
  border-radius: 8px;
  padding: 16px;
}

.registration-list,
.match-list {
  min-height: 200px;
}

.team-members {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-color-secondary);
}

.match-card {
  padding: 16px;
  background: var(--bg-color-tertiary);
  border-radius: 8px;
  margin-bottom: 12px;
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.match-round {
  font-weight: 500;
}

.match-teams {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team {
  flex: 1;
  text-align: center;
}

.team-name {
  display: block;
  font-size: 14px;
  margin-bottom: 4px;
}

.team-score {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
}

.vs {
  padding: 0 20px;
  font-weight: 600;
  color: var(--text-color-secondary);
}

.rules-content {
  padding: 16px;
}

.rules-content h3 {
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .competition-header {
    flex-direction: column;
    gap: 16px;
  }

  .competition-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .competition-badges {
    justify-content: center;
  }

  .competition-actions {
    width: 100%;
    justify-content: center;
  }
}
</style>
