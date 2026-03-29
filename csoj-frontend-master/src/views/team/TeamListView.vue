<template>
  <div id="teamListView" class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">{{ $t("team.title") }}</h1>
      <a-button type="primary" @click="showCreateModal = true">
        <template #icon><icon-plus /></template>
        {{ $t("team.createTeam") }}
      </a-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <a-form :model="searchForm" layout="inline">
        <a-form-item :label="$t('team.searchName')">
          <a-input
            v-model="searchForm.name"
            :placeholder="$t('team.searchPlaceholder')"
            allow-clear
            @press-enter="searchTeams"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="searchTeams">
            <template #icon><icon-search /></template>
            {{ $t("common.search") }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 团队列表 -->
    <div class="team-list">
      <a-row :gutter="[16, 16]">
        <a-col
          v-for="team in teamList"
          :key="team.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <a-card class="team-card" hoverable @click="viewTeam(team.id)">
            <template #cover>
              <div class="team-avatar">
                <a-avatar :size="64" :image-url="team.avatar">
                  <icon-user-group v-if="!team.avatar" />
                </a-avatar>
              </div>
            </template>
            <a-card-meta :title="team.name" :description="team.description">
              <template #avatar>
                <a-avatar :size="24" :image-url="team.leader?.userAvatar">
                  {{ team.leader?.userName?.charAt(0) }}
                </a-avatar>
              </template>
            </a-card-meta>
            <div class="team-stats">
              <span>
                <icon-user /> {{ team.currentMembers }}/{{ team.maxMembers }}
              </span>
              <span>
                <icon-trophy /> {{ $t("team.rating") }}: {{ team.rating }}
              </span>
            </div>
            <div class="team-rank">
              <a-tag :color="getRankColor(team.rank)">#{{ team.rank }}</a-tag>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 空状态 -->
      <a-empty
        v-if="teamList.length === 0 && !loading"
        :description="$t('team.noTeams')"
      />

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <a-button @click="loadMore" :loading="loading">
          {{ $t("common.loadMore") }}
        </a-button>
      </div>
    </div>

    <!-- 创建团队弹窗 -->
    <a-modal
      v-model:visible="showCreateModal"
      :title="$t('team.createTeam')"
      :ok-loading="createLoading"
      @ok="handleCreateTeam"
      @cancel="resetCreateForm"
    >
      <a-form :model="createForm" layout="vertical">
        <a-form-item :label="$t('team.teamName')" required>
          <a-input
            v-model="createForm.name"
            :placeholder="$t('team.teamNamePlaceholder')"
          />
        </a-form-item>
        <a-form-item :label="$t('team.description')">
          <a-textarea
            v-model="createForm.description"
            :placeholder="$t('team.descriptionPlaceholder')"
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>
        <a-form-item :label="$t('team.maxMembers')">
          <a-input-number v-model="createForm.maxMembers" :min="2" :max="10" />
        </a-form-item>
        <a-form-item :label="$t('team.isPublic')">
          <a-switch
            v-model="createForm.isPublic"
            :checked-value="1"
            :unchecked-value="0"
          />
          <span class="ml-2">{{
            createForm.isPublic ? $t("team.public") : $t("team.private")
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
  IconUserGroup,
  IconUser,
  IconTrophy,
} from "@arco-design/web-vue/es/icon";
import { TeamControllerService } from "../../../generated";

const router = useRouter();

// 搜索表单
const searchForm = ref({
  name: "",
});

// 团队列表
const teamList = ref<any[]>([]);
const loading = ref(false);
const hasMore = ref(true);
const current = ref(1);
const pageSize = 12;

// 创建团队
const showCreateModal = ref(false);
const createLoading = ref(false);
const createForm = ref({
  name: "",
  description: "",
  maxMembers: 5,
  isPublic: 1,
});

// 搜索团队
const searchTeams = async () => {
  current.value = 1;
  teamList.value = [];
  await loadTeams();
};

// 加载团队列表
const loadTeams = async () => {
  loading.value = true;
  try {
    const res = await TeamControllerService.listTeamsUsingGet({
      name: searchForm.value.name,
      current: current.value,
      pageSize,
    });
    if (res.code === 0 && res.data) {
      if (current.value === 1) {
        teamList.value = res.data.records || [];
      } else {
        teamList.value.push(...(res.data.records || []));
      }
      hasMore.value = teamList.value.length < (res.data.total || 0);
    }
  } catch (error) {
    console.error("加载团队列表失败:", error);
  } finally {
    loading.value = false;
  }
};

// 加载更多
const loadMore = async () => {
  current.value++;
  await loadTeams();
};

// 查看团队详情
const viewTeam = (teamId: number) => {
  router.push(`/team/${teamId}`);
};

// 创建团队
const handleCreateTeam = async () => {
  if (!createForm.value.name.trim()) {
    Message.warning("请输入团队名称");
    return;
  }

  createLoading.value = true;
  try {
    const res = await TeamControllerService.createTeamUsingPost(
      createForm.value
    );
    if (res.code === 0) {
      Message.success("创建成功");
      showCreateModal.value = false;
      resetCreateForm();
      router.push(`/team/${res.data}`);
    } else {
      Message.error(res.message || "创建失败");
    }
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
    maxMembers: 5,
    isPublic: 1,
  };
};

// 获取排名颜色
const getRankColor = (rank: number) => {
  if (rank === 1) return "gold";
  if (rank === 2) return "silver";
  if (rank === 3) return "bronze";
  return "blue";
};

onMounted(() => {
  loadTeams();
});
</script>

<style scoped>
#teamListView {
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

.team-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.team-card:hover {
  transform: translateY(-4px);
}

.team-avatar {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  background: linear-gradient(
    135deg,
    var(--primary-light-color),
    var(--primary-lighter-color)
  );
}

.team-stats {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color-light);
  font-size: 13px;
  color: var(--text-color-secondary);
}

.team-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.team-rank {
  position: absolute;
  top: 8px;
  right: 8px;
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
