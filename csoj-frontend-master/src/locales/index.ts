import { createI18n } from "vue-i18n";
import zhCN from "./zh-CN.json";
import enUS from "./en-US.json";
import jaJP from "./ja-JP.json";
import koKR from "./ko-KR.json";
import esES from "./es-ES.json";
import frFR from "./fr-FR.json";

// 语言类型
export type LocaleType =
  | "zh-CN"
  | "en-US"
  | "ja-JP"
  | "ko-KR"
  | "es-ES"
  | "fr-FR";

// 语言包
const messages = {
  "zh-CN": zhCN,
  "en-US": enUS,
  "ja-JP": jaJP,
  "ko-KR": koKR,
  "es-ES": esES,
  "fr-FR": frFR,
};

// 本地存储键名
const LOCALE_KEY = "oj_locale_preference";

/**
 * 获取默认语言
 * 优先级：URL参数 > localStorage > 浏览器语言 > 默认中文
 */
function getDefaultLocale(): LocaleType {
  // 1. 检查 URL 参数
  const urlParams = new URLSearchParams(window.location.search);
  const urlLocale = urlParams.get("locale");
  if (urlLocale === "en-US" || urlLocale === "zh-CN") {
    return urlLocale;
  }

  // 2. 检查 localStorage
  const savedLocale = localStorage.getItem(LOCALE_KEY);
  if (savedLocale === "en-US" || savedLocale === "zh-CN") {
    return savedLocale;
  }

  // 3. 检查浏览器语言
  const browserLang = navigator.language;
  if (browserLang.startsWith("zh")) {
    return "zh-CN";
  }

  // 4. 默认中文
  return "zh-CN";
}

// 创建 i18n 实例
const i18n = createI18n({
  locale: getDefaultLocale(),
  fallbackLocale: "zh-CN",
  legacy: false,
  globalInjection: true,
  messages,
});

/**
 * 切换语言
 */
export function setLocale(locale: LocaleType): void {
  if (i18n.mode === "legacy") {
    (i18n.global as any).locale = locale;
  } else {
    (i18n.global.locale as any).value = locale;
  }
  localStorage.setItem(LOCALE_KEY, locale);

  // 更新 HTML lang 属性
  document.documentElement.setAttribute("lang", locale);
}

/**
 * 获取当前语言
 */
export function getLocale(): LocaleType {
  if (i18n.mode === "legacy") {
    return (i18n.global as any).locale as LocaleType;
  } else {
    return (i18n.global.locale as any).value as LocaleType;
  }
}

/**
 * 获取存储的语言偏好键名
 */
export function getLocaleKey(): string {
  return LOCALE_KEY;
}

export default i18n;
