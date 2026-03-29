/**
 * OAuth 第三方登录服务
 */
import request from '@/plugins/axios';

export interface OAuthPlatform {
  code: string;
  name: string;
  pathName: string;
  enabled: boolean;
}

export interface OAuthAuthorizeResult {
  authorizeUrl: string;
  state: string;
  platform: string;
}

export interface OAuthLoginResult {
  success: boolean;
  token?: string;
  userInfo?: any;
  platform: string;
  nickname?: string;
  avatar?: string;
  message?: string;
  needBind?: boolean;
}

export interface OAuthBinding {
  id: number;
  platform: string;
  platformName: string;
  nickname: string;
  avatar: string;
  bindTime: string;
  enabled: boolean;
}

export const OAuthService = {
  /**
   * 获取支持的OAuth平台列表
   */
  async getPlatforms(): Promise<OAuthPlatform[]> {
    const res = await request.get('/oauth/platforms');
    return res.data;
  },

  /**
   * 获取授权URL
   */
  async getAuthorizeUrl(platform: string): Promise<OAuthAuthorizeResult> {
    const res = await request.get('/oauth/authorize', {
      params: { platform }
    });
    return res.data;
  },

  /**
   * 发起第三方登录
   * 跳转到授权页面
   */
  async login(platform: string): Promise<void> {
    const result = await this.getAuthorizeUrl(platform);
    // 跳转到授权页面
    window.location.href = result.authorizeUrl;
  },

  /**
   * 获取已绑定的第三方账号
   */
  async getBindings(userId: number): Promise<OAuthBinding[]> {
    const res = await request.get('/oauth/bindings', {
      params: { userId }
    });
    return res.data;
  },

  /**
   * 解绑第三方账号
   */
  async unbind(platform: string, userId: number): Promise<boolean> {
    const res = await request.delete('/oauth/unbind', {
      params: { platform, userId }
    });
    return res.data;
  }
};

export default OAuthService;
