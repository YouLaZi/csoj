/**
 * 语言管理器 - 用于管理多语言切换
 */

import { getLocale, setLocale, getLocaleKey, LocaleType } from "@/locales";

// 语言配置
export const LOCALES: { value: LocaleType; label: string; icon: string }[] = [
  { value: "zh-CN", label: "中文", icon: "🇨🇳" },
  { value: "en-US", label: "English", icon: "🇺🇸" },
  { value: "ja-JP", label: "日本語", icon: "🇯🇵" },
  { value: "ko-KR", label: "한국어", icon: "🇰🇷" },
  { value: "es-ES", label: "Español", icon: "🇪🇸" },
  { value: "fr-FR", label: "Français", icon: "🇫🇷" },
];

/**
 * 语言管理类
 */
export class LocaleManager {
  // 当前语言
  private static currentLocale: LocaleType = "zh-CN";

  /**
   * 初始化语言
   * 从本地存储或浏览器语言检测
   */
  public static init(): void {
    this.currentLocale = getLocale();

    // 更新 HTML lang 属性
    document.documentElement.setAttribute("lang", this.currentLocale);

    // 处理 URL 参数
    const urlParams = new URLSearchParams(window.location.search);
    const urlLocale = urlParams.get("locale");
    if (urlLocale === "en-US" || urlLocale === "zh-CN") {
      this.setLocale(urlLocale);
    }
  }

  /**
   * 获取当前语言
   */
  public static getLocale(): LocaleType {
    return this.currentLocale;
  }

  /**
   * 设置语言
   */
  public static setLocale(locale: LocaleType): void {
    if (this.currentLocale !== locale) {
      this.currentLocale = locale;
      setLocale(locale);

      // 触发自定义事件，通知其他组件语言已更改
      const localeChangeEvent = new CustomEvent("localeChange", {
        detail: { locale },
      });
      window.dispatchEvent(localeChangeEvent);
    }
  }

  /**
   * 切换语言（中英文切换）
   */
  public static toggleLocale(): LocaleType {
    const newLocale = this.currentLocale === "zh-CN" ? "en-US" : "zh-CN";
    this.setLocale(newLocale);
    return newLocale;
  }

  /**
   * 获取语言配置列表
   */
  public static getLocales(): typeof LOCALES {
    return LOCALES;
  }

  /**
   * 获取当前语言的标签
   */
  public static getLocaleLabel(): string {
    const config = LOCALES.find((l) => l.value === this.currentLocale);
    return config ? config.label : "中文";
  }

  /**
   * 获取当前语言的图标
   */
  public static getLocaleIcon(): string {
    const config = LOCALES.find((l) => l.value === this.currentLocale);
    return config ? config.icon : "🇨🇳";
  }

  /**
   * 判断是否为中文
   */
  public static isChinese(): boolean {
    return this.currentLocale === "zh-CN";
  }

  /**
   * 判断是否为英文
   */
  public static isEnglish(): boolean {
    return this.currentLocale === "en-US";
  }
}
