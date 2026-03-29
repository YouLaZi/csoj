<template>
  <div class="cat-panel">
    <!-- 猫咪展示区 -->
    <div class="cat-display">
      <div class="cat-avatar" :class="catMood">
        <span class="cat-emoji">{{ catEmoji }}</span>
      </div>
      <div class="cat-info">
        <div class="cat-name">{{ catData?.catName || "小橘" }}</div>
        <div class="cat-level">Lv.{{ catData?.level || 1 }}</div>
      </div>
    </div>

    <!-- 状态条 -->
    <div class="status-bars">
      <div class="status-item">
        <span class="status-label">🍔 饱食度</span>
        <div class="status-bar">
          <div class="status-fill hunger" :style="{ width: (catData?.hunger || 80) + '%' }"></div>
        </div>
        <span class="status-value">{{ catData?.hunger || 80 }}</span>
      </div>
      <div class="status-item">
        <span class="status-label">😊 快乐值</span>
        <div class="status-bar">
          <div class="status-fill happiness" :style="{ width: (catData?.happiness || 80) + '%' }"></div>
        </div>
        <span class="status-value">{{ catData?.happiness || 80 }}</span>
      </div>
      <div class="status-item">
        <span class="status-label">⚡ 精力值</span>
        <div class="status-bar">
          <div class="status-fill energy" :style="{ width: (catData?.energy || 100) + '%' }"></div>
        </div>
        <span class="status-value">{{ catData?.energy || 100 }}</span>
      </div>
    </div>

    <!-- 经验条 -->
    <div class="exp-bar">
      <div class="exp-fill" :style="{ width: expPercent + '%' }"></div>
      <span class="exp-text">EXP: {{ catData?.experience || 0 }} / {{ expNeeded }}</span>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <button class="action-btn" @click="handleFeed" :disabled="loading">
        <span class="btn-icon">🍖</span>
        <span class="btn-text">喂食</span>
      </button>
      <button class="action-btn" @click="handlePlay" :disabled="loading">
        <span class="btn-icon">🎾</span>
        <span class="btn-text">玩耍</span>
      </button>
      <button class="action-btn" @click="handlePet" :disabled="loading">
        <span class="btn-icon">🤚</span>
        <span class="btn-text">抚摸</span>
      </button>
      <button class="action-btn" @click="handleSleep" :disabled="loading">
        <span class="btn-icon">💤</span>
        <span class="btn-text">睡觉</span>
      </button>
    </div>

    <!-- 快捷商店入口 -->
    <div class="quick-shop" @click="showShop = true">
      <span>🛒 商店</span>
    </div>

    <!-- 商店弹窗 -->
    <Teleport to="body">
      <transition name="modal">
        <div v-if="showShop" class="shop-modal" @click.self="showShop = false">
          <div class="shop-content">
            <div class="shop-header">
              <h3>🛒 猫咪商店</h3>
              <button class="close-btn" @click="showShop = false">×</button>
            </div>
            <div class="shop-tabs">
              <button :class="{ active: shopTab === 'food' }" @click="shopTab = 'food'">食物</button>
              <button :class="{ active: shopTab === 'toy' }" @click="shopTab = 'toy'">玩具</button>
            </div>
            <div class="shop-items">
              <div v-for="item in filteredShopItems" :key="item.itemCode" class="shop-item">
                <span class="item-icon">{{ item.icon }}</span>
                <div class="item-info">
                  <div class="item-name">{{ item.itemName }}</div>
                  <div class="item-desc">{{ item.description }}</div>
                </div>
                <button class="buy-btn" @click="handleBuy(item)" :disabled="loading">
                  {{ item.price }} 积分
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import CatService from "@/services/CatService";
import { Message } from "@arco-design/web-vue";

interface CatData {
  catName: string;
  level: number;
  experience: number;
  hunger: number;
  happiness: number;
  energy: number;
  mood: string;
}

interface ShopItem {
  itemCode: string;
  itemName: string;
  itemType: string;
  description: string;
  price: number;
  icon: string;
}

const catData = ref<CatData | null>(null);
const shopItems = ref<ShopItem[]>([]);
const showShop = ref(false);
const shopTab = ref("food");
const loading = ref(false);

// 计算属性
const catMood = computed(() => catData.value?.mood || "happy");
const catEmoji = computed(() => {
  const mood = catData.value?.mood || "happy";
  const emojis: Record<string, string> = {
    happy: "😺",
    sad: "😿",
    thinking: "🤔",
    sleeping: "😴",
    excited: "🙀",
  };
  return emojis[mood] || "😺";
});

const expNeeded = computed(() => (catData.value?.level || 1) * 100);
const expPercent = computed(() => {
  const exp = catData.value?.experience || 0;
  return Math.min((exp / expNeeded.value) * 100, 100);
});

const filteredShopItems = computed(() =>
  shopItems.value.filter((item) => item.itemType === shopTab.value)
);

// 方法
const fetchCat = async () => {
  try {
    const res = await CatService.getCat();
    if (res.code === 0) {
      catData.value = res.data;
    }
  } catch (e) {
    console.error("获取猫咪失败", e);
  }
};

const fetchShopItems = async () => {
  try {
    const res = await CatService.getShopItems();
    if (res.code === 0) {
      shopItems.value = res.data;
    }
  } catch (e) {
    console.error("获取商店物品失败", e);
  }
};

const handleFeed = async () => {
  Message.info("请先在商店购买食物，然后在背包中使用");
  showShop.value = true;
  shopTab.value = "food";
};

const handlePlay = async () => {
  Message.info("请先在商店购买玩具，然后在背包中使用");
  showShop.value = true;
  shopTab.value = "toy";
};

const handlePet = async () => {
  loading.value = true;
  try {
    const res = await CatService.petCat();
    if (res.code === 0) {
      catData.value = res.data;
      Message.success("猫咪很开心~");
    } else {
      Message.error(res.message);
    }
  } catch (e) {
    Message.error("操作失败");
  } finally {
    loading.value = false;
  }
};

const handleSleep = async () => {
  loading.value = true;
  try {
    const res = await CatService.sleepCat();
    if (res.code === 0) {
      catData.value = res.data;
      Message.success("猫咪睡着了 Zzz...");
    } else {
      Message.error(res.message);
    }
  } catch (e) {
    Message.error("操作失败");
  } finally {
    loading.value = false;
  }
};

const handleBuy = async (item: ShopItem) => {
  loading.value = true;
  try {
    const res = await CatService.buyItem(item.itemCode, 1);
    if (res.code === 0) {
      Message.success(`购买 ${item.itemName} 成功！`);
    } else {
      Message.error(res.message);
    }
  } catch (e) {
    Message.error("购买失败");
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchCat();
  fetchShopItems();
});
</script>

<style scoped>
.cat-panel {
  background: linear-gradient(135deg, #fff5e6 0%, #ffe4c4 100%);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-card);
  position: relative;
  overflow: hidden;
  transition: transform var(--transition-base) var(--easing-smooth);
}

/* 猫咪面板装饰 */
.cat-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
}

.cat-panel:hover {
  transform: translateY(-2px);
}

.cat-display {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.cat-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(245, 158, 11, 0.4);
}

.cat-avatar.sleeping {
  animation: sleeping-sway 3s ease-in-out infinite;
}

.cat-emoji {
  font-size: 32px;
}

.cat-info {
  flex: 1;
}

.cat-name {
  font-size: 18px;
  font-weight: bold;
  color: #92400e;
}

.cat-level {
  font-size: 14px;
  color: #d97706;
}

.status-bars {
  margin-bottom: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.status-label {
  width: 70px;
  font-size: 12px;
  color: #78350f;
}

.status-bar {
  flex: 1;
  height: 8px;
  background: #fed7aa;
  border-radius: 4px;
  overflow: hidden;
}

.status-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.status-fill.hunger {
  background: linear-gradient(90deg, #f97316, #ea580c);
}

.status-fill.happiness {
  background: linear-gradient(90deg, #22c55e, #16a34a);
}

.status-fill.energy {
  background: linear-gradient(90deg, #3b82f6, #2563eb);
}

.status-value {
  width: 30px;
  font-size: 12px;
  color: #92400e;
  text-align: right;
}

.exp-bar {
  height: 12px;
  background: #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 16px;
  position: relative;
}

.exp-fill {
  height: 100%;
  background: linear-gradient(90deg, #a855f7, #7c3aed);
  border-radius: 6px;
  transition: width 0.3s ease;
}

.exp-text {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 10px;
  color: #4b5563;
  white-space: nowrap;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  background: white;
  border: 2px solid #fbbf24;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover:not(:disabled) {
  background: #fef3c7;
  transform: translateY(-2px);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 20px;
}

.btn-text {
  font-size: 12px;
  color: #92400e;
}

.quick-shop {
  text-align: center;
  padding: 8px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border-radius: 8px;
  cursor: pointer;
  color: white;
  font-weight: bold;
  transition: transform 0.2s ease;
}

.quick-shop:hover {
  transform: scale(1.02);
}

/* 商店弹窗 */
.shop-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.shop-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.shop-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: white;
  cursor: pointer;
}

.shop-tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
}

.shop-tabs button {
  flex: 1;
  padding: 12px;
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 500;
  color: #6b7280;
  transition: all 0.2s ease;
}

.shop-tabs button.active {
  color: #f59e0b;
  border-bottom: 2px solid #f59e0b;
}

.shop-items {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.shop-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
}

.item-icon {
  font-size: 32px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 500;
  color: #374151;
}

.item-desc {
  font-size: 12px;
  color: #6b7280;
}

.buy-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.buy-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

/* 动画 */
@keyframes sleeping-sway {
  0%,
  100% {
    transform: rotate(-3deg);
  }
  50% {
    transform: rotate(3deg);
  }
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

/* 深色模式 */
[data-theme="dark"] .cat-panel {
  background: linear-gradient(135deg, #78350f 0%, #92400e 100%);
}

[data-theme="dark"] .cat-name {
  color: #fef3c7;
}

[data-theme="dark"] .cat-level,
[data-theme="dark"] .status-label,
[data-theme="dark"] .status-value {
  color: #fcd34d;
}

[data-theme="dark"] .status-bar {
  background: #451a03;
}

[data-theme="dark"] .action-btn {
  background: #451a03;
  border-color: #d97706;
}

[data-theme="dark"] .action-btn:hover:not(:disabled) {
  background: #7c2d12;
}

[data-theme="dark"] .btn-text {
  color: #fcd34d;
}

[data-theme="dark"] .shop-content {
  background: #1f2937;
}

[data-theme="dark"] .shop-tabs button {
  color: #9ca3af;
}

[data-theme="dark"] .shop-tabs button.active {
  color: #fbbf24;
}

[data-theme="dark"] .shop-item {
  background: #374151;
}

[data-theme="dark"] .item-name {
  color: #e5e7eb;
}

[data-theme="dark"] .item-desc {
  color: #9ca3af;
}
</style>
