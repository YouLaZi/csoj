<template>
  <div class="oauth-callback">
    <a-spin :loading="loading" tip="正在登录...">
      <div class="callback-content">
        <a-result
          v-if="!loading && success"
          status="success"
          :title="$t('oauth.loginSuccess')"
          :subtitle="message"
        >
          <template #extra>
            <a-button type="primary" @click="goHome">
              {{ $t('common.backHome') }}
            </a-button>
          </template>
        </a-result>
        <a-result
          v-if="!loading && !success"
          status="error"
          :title="$t('oauth.loginFailed')"
          :subtitle="message"
        >
          <template #extra>
            <a-space>
              <a-button @click="retry">{{ $t('common.retry') }}</a-button>
              <a-button type="primary" @click="goLogin">{{ $t('user.login') }}</a-button>
            </a-space>
          </template>
        </a-result>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';
import { Message } from '@arco-design/web-vue';
import request from '@/plugins/axios';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const store = useStore();

const loading = ref(true);
const success = ref(false);
const message = ref('');

const handleCallback = async () => {
  const platform = route.params.platform as string;
  const code = route.query.code as string;
  const state = route.query.state as string;

  if (!code) {
    loading.value = false;
    success.value = false;
    message.value = t('oauth.noAuthCode');
    return;
  }

  try {
    const res = await request.get(`/oauth/callback/${platform}`, {
      params: { code, state }
    });

    if (res.code === 0 && res.data?.success) {
      // 设置 Token
      if (res.data.token) {
        await store.dispatch('user/setToken', res.data.token);
      }
      await store.dispatch('user/getLoginUser');

      success.value = true;
      message.value = t('oauth.welcome', { name: res.data.nickname || 'User' });

      Message.success(t('oauth.loginSuccess'));

      // 延迟跳转到首页
      setTimeout(() => {
        router.replace('/');
      }, 1500);
    } else {
      success.value = false;
      message.value = res.data?.message || res.message || t('oauth.loginFailed');
    }
  } catch (error: any) {
    console.error('OAuth callback error:', error);
    success.value = false;
    message.value = error.message || t('oauth.loginFailed');
  } finally {
    loading.value = false;
  }
};

const goHome = () => {
  router.replace('/');
};

const goLogin = () => {
  router.replace('/?showLogin=true');
};

const retry = () => {
  loading.value = true;
  handleCallback();
};

onMounted(() => {
  handleCallback();
});
</script>

<style scoped>
.oauth-callback {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--color-bg-1);
}

.callback-content {
  background: var(--color-bg-2);
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}
</style>
