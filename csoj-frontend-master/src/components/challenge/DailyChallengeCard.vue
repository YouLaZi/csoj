<template>
  <div class="daily-challenge-card" :class="{ completed: isCompleted }">
    <div class="challenge-header">
      <div class="challenge-icon">🎯</div>
      <div class="challenge-title">今日挑战</div>
      <div v-if="streak > 0" class="streak-badge">
        🔥 {{ streak }}天连胜
      </div>
    </div>

    <div v-if="challenge" class="challenge-content">
      <div class="question-title">{{ challenge.description || "今日挑战题目" }}</div>
      <div class="challenge-meta">
        <span class="difficulty" :class="challenge.difficulty">
          {{ difficultyText }}
        </span>
        <span class="bonus">🎁 {{ challenge.bonusPoints }}+ 积分</span>
      </div>

      <div v-if="isCompleted" class="completed-info">
        <span class="completed-icon">✅</span>
        <span>今日已完成！获得 {{ userChallenge?.pointsEarned || 0 }} 积分</span>
      </div>

      <button
        v-else
        class="start-btn"
        @click="startChallenge"
      >
        开始挑战 →
      </button>
    </div>

    <div v-else class="no-challenge">
      <span>暂无今日挑战</span>
    </div>

    <!-- 连胜奖励提示 -->
    <div v-if="streak > 0" class="streak-bonus">
      <span>连胜奖励: +{{ streak * 5 }} 积分</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import DailyChallengeService from "@/services/DailyChallengeService";
import { Message } from "@arco-design/web-vue";

interface Challenge {
  id: number;
  questionId: number;
  description: string;
  difficulty: string;
  bonusPoints: number;
}

interface UserChallenge {
  isCompleted: number;
  pointsEarned: number;
}

const router = useRouter();
const challenge = ref<Challenge | null>(null);
const userChallenge = ref<UserChallenge | null>(null);
const streak = ref(0);

const isCompleted = computed(() => userChallenge.value?.isCompleted === 1);
const difficultyText = computed(() => {
  const map: Record<string, string> = {
    easy: "简单",
    medium: "中等",
    hard: "困难",
  };
  return map[challenge.value?.difficulty || ""] || challenge.value?.difficulty;
});

const fetchData = async () => {
  try {
    const [challengeRes, streakRes] = await Promise.all([
      DailyChallengeService.getTodayChallenge(),
      DailyChallengeService.getUserStreak(),
    ]);

    if (challengeRes.code === 0) {
      challenge.value = challengeRes.data;
      if (challenge.value) {
        const statusRes = await DailyChallengeService.getUserChallengeStatus(
          challenge.value.id
        );
        if (statusRes.code === 0) {
          userChallenge.value = statusRes.data;
        }
      }
    }

    if (streakRes.code === 0) {
      streak.value = streakRes.data?.currentStreak || 0;
    }
  } catch (e) {
    console.error("获取每日挑战失败", e);
  }
};

const startChallenge = () => {
  if (challenge.value) {
    router.push(`/question/${challenge.value.questionId}`);
    Message.success("开始今日挑战！");
  }
};

onMounted(fetchData);
</script>

<style scoped>
.daily-challenge-card {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(251, 191, 36, 0.3);
  transition: transform 0.3s ease;
}

.daily-challenge-card:hover {
  transform: translateY(-2px);
}

.daily-challenge-card.completed {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.3);
}

.challenge-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.challenge-icon {
  font-size: 24px;
}

.challenge-title {
  font-size: 16px;
  font-weight: bold;
  color: #92400e;
}

.streak-badge {
  margin-left: auto;
  padding: 4px 12px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  animation: fire-pulse 1s ease infinite;
}

@keyframes fire-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.challenge-content {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 16px;
}

.question-title {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 12px;
}

.challenge-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.difficulty {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.difficulty.easy {
  background: #d1fae5;
  color: #065f46;
}

.difficulty.medium {
  background: #fef3c7;
  color: #92400e;
}

.difficulty.hard {
  background: #fee2e2;
  color: #991b1b;
}

.bonus {
  font-size: 12px;
  color: #d97706;
}

.completed-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #065f46;
  font-weight: 500;
}

.completed-icon {
  font-size: 20px;
}

.start-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.start-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
}

.no-challenge {
  text-align: center;
  padding: 20px;
  color: #6b7280;
}

.streak-bonus {
  margin-top: 12px;
  text-align: center;
  font-size: 12px;
  color: #d97706;
}

/* 深色模式 */
[data-theme="dark"] .daily-challenge-card {
  background: linear-gradient(135deg, #78350f 0%, #92400e 100%);
}

[data-theme="dark"] .daily-challenge-card.completed {
  background: linear-gradient(135deg, #064e3b 0%, #065f46 100%);
}

[data-theme="dark"] .challenge-title {
  color: #fef3c7;
}

[data-theme="dark"] .challenge-content {
  background: rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .question-title {
  color: #f3f4f6;
}
</style>
