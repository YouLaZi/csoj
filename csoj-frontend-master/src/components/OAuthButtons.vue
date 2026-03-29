<template>
  <div class="oauth-buttons">
    <a-divider>{{ $t('oauth.orThirdParty') }}</a-divider>
    <div class="oauth-icons">
      <a-tooltip v-for="platform in enabledPlatforms" :key="platform.code" :content="platform.name">
        <a-button
          class="oauth-btn"
          :class="platform.code.toLowerCase()"
          @click="handleOAuthLogin(platform.code)"
          :loading="loading === platform.code"
        >
          <template #icon>
            <component :is="getIconComponent(platform.code)" />
          </template>
        </a-button>
      </a-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { useI18n } from 'vue-i18n';
import OAuthService from '@/services/OAuthService';
import { IconGithub, IconTikTokColor } from '@arco-design/web-vue/es/icon';

const { t } = useI18n();

const platforms = ref<any[]>([]);
const loading = ref<string | null>(null);

const enabledPlatforms = computed(() => {
  return platforms.value.filter(p => p.enabled);
});

const emit = defineEmits<{
  (e: 'login', platform: string): void;
}>();

const getIconComponent = (code: string) => {
  switch (code.toUpperCase()) {
    case 'GITHUB':
      return IconGithub;
    case 'GITEE':
      return 'icon-gitee';
    case 'QQ':
      return IconTikTokColor;
    case 'WECHAT':
      return 'icon-wechat';
    default:
      return IconGithub;
  }
};

const handleOAuthLogin = async (platform: string) => {
  loading.value = platform;
  try {
    emit('login', platform);
    await OAuthService.login(platform);
  } catch (error: any) {
    Message.error(t('oauth.loginFailed') + ': ' + (error.message || 'Unknown error'));
  } finally {
    loading.value = null;
  }
};

const loadPlatforms = async () => {
  try {
    platforms.value = await OAuthService.getPlatforms();
  } catch (error) {
    console.error('Failed to load OAuth platforms:', error);
  }
};

onMounted(() => {
  loadPlatforms();
});
</script>

<style scoped>
.oauth-buttons {
  margin-top: 20px;
  width: 100%;
}

.oauth-icons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
}

.oauth-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid var(--color-border-2);
  background: var(--color-bg-2);
  transition: all 0.3s ease;
}

.oauth-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.oauth-btn.github:hover {
  background: #24292e;
  border-color: #24292e;
  color: #fff;
}

.oauth-btn.gitee:hover {
  background: #c71d23;
  border-color: #c71d23;
  color: #fff;
}

.oauth-btn.qq:hover {
  background: #12b7f5;
  border-color: #12b7f5;
  color: #fff;
}

.oauth-btn.wechat:hover {
  background: #07c160;
  border-color: #07c160;
  color: #fff;
}
</style>
