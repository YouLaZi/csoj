<template>
  <div id="teamDetailView" class="page-container">
    <a-spin :loading="loading" style="width: 100%">
      <!-- 团队信息卡片 -->
      <div v-if="team" class="team-header">
        <div class="team-info">
          <a-avatar :size="80" :image-url="team.avatar">
            <icon-user-group v-if="!team.avatar" :size="40" />
          </a-avatar>
          <div class="team-meta">
            <h1 class="team-name">{{ team.name }}</h1>
            <div class="team-badges">
              <a-tag :color="getRankColor(team.rank)"
                >#{{ team.rank }} {{ $t("team.ranking") }}</a-tag
              >
              <a-tag color="blue"
                >{{ $t("team.rating") }}: {{ team.rating }}</a-tag
              >
              <a-tag v-if="team.isPublic" color="green">{{
                $t("team.public")
              }}</a-tag>
              <a-tag v-else color="gray">{{ $t("team.private") }}</a-tag>
            </div>
            <p class="team-description">
              {{ team.description || $t("team.noDescription") }}
            </p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="team-actions">
          <template v-if="team.hasJoined">
            <a-button
              v-if="team.userRole === 'leader'"
              type="primary"
              @click="showInviteModal = true"
            >
              <template #icon><icon-share-alt /></template>
              {{ $t("team.inviteMembers") }}
            </a-button>
            <a-button
              v-if="team.userRole === 'leader'"
              @click="showEditModal = true"
            >
              <template #icon><icon-edit /></template>
              {{ $t("common.edit") }}
            </a-button>
            <a-popconfirm
              v-if="team.userRole !== 'leader'"
              :content="$t('team.quitConfirm')"
              @ok="handleQuit"
            >
              <a-button status="danger">
                <template #icon><icon-export /></template>
                {{ $t("team.quit") }}
              </a-button>
            </a-popconfirm>
          </template>
          <template v-else>
            <a-button type="primary" @click="handleJoin">
              <template #icon><icon-plus /></template>
              {{ $t("team.joinTeam") }}
            </a-button>
          </template>
        </div>
      </div>

      <!-- 统计信息 -->
      <div v-if="team" class="team-stats-row">
        <div class="stat-item">
          <span class="stat-value">{{ team.winCount }}</span>
          <span class="stat-label">{{ $t("team.wins") }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ team.loseCount }}</span>
          <span class="stat-label">{{ $t("team.losses") }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ team.drawCount }}</span>
          <span class="stat-label">{{ $t("team.draws") }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ team.totalScore }}</span>
          <span class="stat-label">{{ $t("team.totalScore") }}</span>
        </div>
      </div>

      <!-- Tab 切换 -->
      <a-tabs v-model:active-key="activeTab" class="team-tabs">
        <a-tab-pane key="members" :title="$t('team.members')">
          <div class="member-list">
            <a-list :bordered="false">
              <a-list-item v-for="member in members" :key="member.userId">
                <a-list-item-meta
                  :title="member.userName"
                  :description="getRoleDescription(member.role)"
                >
                  <template #avatar>
                    <a-avatar :image-url="member.userAvatar">
                      {{ member.userName?.charAt(0) }}
                    </a-avatar>
                  </template>
                </a-list-item-meta>
                <template #extra>
                  <div class="member-actions">
                    <span class="member-stats">
                      {{ $t("team.contribution") }}:
                      {{ member.contributionScore }}
                    </span>
                    <a-button
                      v-if="
                        team?.userRole === 'leader' && member.role !== 'leader'
                      "
                      size="small"
                      @click="handleTransferLeader(member.userId)"
                    >
                      {{ $t("team.transferLeader") }}
                    </a-button>
                    <a-popconfirm
                      v-if="
                        team?.userRole === 'leader' && member.role !== 'leader'
                      "
                      :content="$t('team.kickConfirm')"
                      @ok="handleKick(member.userId)"
                    >
                      <a-button size="small" status="danger">
                        {{ $t("team.kick") }}
                      </a-button>
                    </a-popconfirm>
                  </div>
                </template>
              </a-list-item>
            </a-list>
          </div>
        </a-tab-pane>

        <a-tab-pane key="history" :title="$t('team.matchHistory')">
          <a-empty :description="$t('team.noMatches')" />
        </a-tab-pane>
      </a-tabs>
    </a-spin>

    <!-- 邀请成员弹窗 -->
    <a-modal
      v-model:visible="showInviteModal"
      :title="$t('team.inviteMembers')"
      :footer="false"
    >
      <div class="invite-content">
        <p>{{ $t("team.inviteCodeLabel") }}</p>
        <div class="invite-code">
          <span class="code">{{ inviteCode }}</span>
          <a-button size="small" @click="copyInviteCode">
            <template #icon><icon-copy /></template>
            {{ $t("common.copy") }}
          </a-button>
        </div>
        <a-button
          type="primary"
          long
          @click="refreshInviteCode"
          :loading="refreshingCode"
        >
          {{ $t("team.refreshCode") }}
        </a-button>
      </div>
    </a-modal>

    <!-- 编辑团队弹窗 -->
    <a-modal
      v-model:visible="showEditModal"
      :title="$t('team.editTeam')"
      :ok-loading="editLoading"
      @ok="handleEditTeam"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item :label="$t('team.teamName')" required>
          <a-input v-model="editForm.name" />
        </a-form-item>
        <a-form-item :label="$t('team.description')">
          <a-textarea
            v-model="editForm.description"
            :auto-size="{ minRows: 3 }"
          />
        </a-form-item>
        <a-form-item :label="$t('team.maxMembers')">
          <a-input-number v-model="editForm.maxMembers" :min="2" :max="10" />
        </a-form-item>
        <a-form-item :label="$t('team.isPublic')">
          <a-switch
            v-model="editForm.isPublic"
            :checked-value="1"
            :unchecked-value="0"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconUserGroup,
  IconShareAlt,
  IconEdit,
  IconExport,
  IconPlus,
  IconCopy,
} from "@arco-design/web-vue/es/icon";
import { TeamControllerService } from "../../../generated";

const route = useRoute();
const router = useRouter();

const teamId = computed(() => Number(route.params.id));

const loading = ref(false);
const team = ref<any>(null);
const members = ref<any[]>([]);
const activeTab = ref("members");

// 邀请相关
const showInviteModal = ref(false);
const inviteCode = ref("");
const refreshingCode = ref(false);

// 编辑相关
const showEditModal = ref(false);
const editLoading = ref(false);
const editForm = ref({
  id: 0,
  name: "",
  description: "",
  maxMembers: 5,
  isPublic: 1,
});

// 加载团队详情
const loadTeam = async () => {
  loading.value = true;
  try {
    const res = await TeamControllerService.getTeamUsingGet(teamId.value);
    if (res.code === 0 && res.data) {
      team.value = res.data;
      members.value = res.data.members || [];
      inviteCode.value = res.data.inviteCode || "";

      editForm.value = {
        id: res.data.id,
        name: res.data.name,
        description: res.data.description || "",
        maxMembers: res.data.maxMembers,
        isPublic: res.data.isPublic,
      };
    } else {
      Message.error(res.message || "加载失败");
    }
  } catch (error) {
    Message.error("加载团队详情失败");
  } finally {
    loading.value = false;
  }
};

// 加入团队
const handleJoin = async () => {
  try {
    const res = await TeamControllerService.joinTeamUsingPost({
      teamId: teamId.value,
    });
    if (res.code === 0) {
      Message.success("加入成功");
      loadTeam();
    } else {
      Message.error(res.message || "加入失败");
    }
  } catch (error) {
    Message.error("加入失败");
  }
};

// 退出团队
const handleQuit = async () => {
  try {
    const res = await TeamControllerService.quitTeamUsingPost(teamId.value);
    if (res.code === 0) {
      Message.success("已退出团队");
      router.push("/team");
    } else {
      Message.error(res.message || "退出失败");
    }
  } catch (error) {
    Message.error("退出失败");
  }
};

// 移除成员
const handleKick = async (userId: number) => {
  try {
    const res = await TeamControllerService.kickMemberUsingPost(
      teamId.value,
      userId
    );
    if (res.code === 0) {
      Message.success("已移除成员");
      loadTeam();
    } else {
      Message.error(res.message || "移除失败");
    }
  } catch (error) {
    Message.error("移除失败");
  }
};

// 转让队长
const handleTransferLeader = async (newLeaderId: number) => {
  try {
    const res = await TeamControllerService.transferLeaderUsingPost(
      teamId.value,
      newLeaderId
    );
    if (res.code === 0) {
      Message.success("已转让队长");
      loadTeam();
    } else {
      Message.error(res.message || "转让失败");
    }
  } catch (error) {
    Message.error("转让失败");
  }
};

// 复制邀请码
const copyInviteCode = () => {
  navigator.clipboard.writeText(inviteCode.value);
  Message.success("已复制邀请码");
};

// 刷新邀请码
const refreshInviteCode = async () => {
  refreshingCode.value = true;
  try {
    const res = await TeamControllerService.generateInviteCodeUsingPost(
      teamId.value
    );
    if (res.code === 0) {
      inviteCode.value = res.data;
      Message.success("已生成新邀请码");
    } else {
      Message.error(res.message || "生成失败");
    }
  } catch (error) {
    Message.error("生成失败");
  } finally {
    refreshingCode.value = false;
  }
};

// 编辑团队
const handleEditTeam = async () => {
  editLoading.value = true;
  try {
    const res = await TeamControllerService.updateTeamUsingPut(editForm.value);
    if (res.code === 0) {
      Message.success("保存成功");
      showEditModal.value = false;
      loadTeam();
    } else {
      Message.error(res.message || "保存失败");
    }
  } catch (error) {
    Message.error("保存失败");
  } finally {
    editLoading.value = false;
  }
};

// 获取角色描述
const getRoleDescription = (role: string) => {
  const roleMap: Record<string, string> = {
    leader: "队长",
    vice_leader: "副队长",
    member: "队员",
  };
  return roleMap[role] || "队员";
};

// 获取排名颜色
const getRankColor = (rank: number) => {
  if (rank === 1) return "gold";
  if (rank === 2) return "silver";
  if (rank === 3) return "bronze";
  return "blue";
};

onMounted(() => {
  loadTeam();
});
</script>

<style scoped>
#teamDetailView {
  max-width: 900px;
  margin: 0 auto;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  background: var(--bg-color-secondary);
  border-radius: 12px;
  margin-bottom: 24px;
}

.team-info {
  display: flex;
  gap: 20px;
}

.team-meta {
  flex: 1;
}

.team-name {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
}

.team-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.team-description {
  margin: 0;
  color: var(--text-color-secondary);
  font-size: 14px;
}

.team-actions {
  display: flex;
  gap: 8px;
}

.team-stats-row {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background: var(--bg-color-secondary);
  border-radius: 8px;
  margin-bottom: 24px;
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

.team-tabs {
  background: var(--bg-color-secondary);
  border-radius: 8px;
  padding: 16px;
}

.member-list {
  min-height: 200px;
}

.member-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.member-stats {
  font-size: 13px;
  color: var(--text-color-secondary);
  margin-right: 8px;
}

.invite-content {
  text-align: center;
}

.invite-code {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
  padding: 16px;
  background: var(--bg-color-tertiary);
  border-radius: 8px;
}

.invite-code .code {
  font-size: 24px;
  font-weight: 600;
  font-family: monospace;
  letter-spacing: 4px;
}

@media (max-width: 768px) {
  .team-header {
    flex-direction: column;
    gap: 16px;
  }

  .team-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .team-badges {
    justify-content: center;
  }

  .team-actions {
    width: 100%;
    justify-content: center;
  }
}
</style>
