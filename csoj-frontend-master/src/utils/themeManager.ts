/**
 * 主题管理器 - 用于管理深色/浅色模式切换
 */

// 主题类型枚举
export enum ThemeType {
  LIGHT = "light",
  DARK = "dark",
}

// 本地存储键名
const THEME_KEY = "oj_theme_preference";

/**
 * 主题管理类
 */
export class ThemeManager {
  // 当前主题
  private static currentTheme: ThemeType = ThemeType.LIGHT;

  /**
   * 初始化主题
   * 从本地存储读取用户偏好，如果没有则使用系统偏好
   */
  public static init(): void {
    // 尝试从本地存储获取主题设置
    const savedTheme = localStorage.getItem(THEME_KEY);

    if (
      savedTheme &&
      Object.values(ThemeType).includes(savedTheme as ThemeType)
    ) {
      this.currentTheme = savedTheme as ThemeType;
    } else {
      // 如果没有保存的主题，检查系统偏好
      const prefersDark = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;
      this.currentTheme = prefersDark ? ThemeType.DARK : ThemeType.LIGHT;
      this.saveTheme();
    }

    // 应用主题
    this.applyTheme();

    // 监听系统主题变化
    window
      .matchMedia("(prefers-color-scheme: dark)")
      .addEventListener("change", (e) => {
        if (!localStorage.getItem(THEME_KEY)) {
          this.currentTheme = e.matches ? ThemeType.DARK : ThemeType.LIGHT;
          this.applyTheme();
        }
      });
  }

  /**
   * 获取当前主题
   */
  public static getTheme(): ThemeType {
    return this.currentTheme;
  }

  /**
   * 切换主题
   */
  public static toggleTheme(): ThemeType {
    this.currentTheme =
      this.currentTheme === ThemeType.LIGHT ? ThemeType.DARK : ThemeType.LIGHT;
    this.saveTheme();
    this.applyTheme();
    return this.currentTheme;
  }

  /**
   * 设置特定主题
   */
  public static setTheme(theme: ThemeType): void {
    if (this.currentTheme !== theme) {
      this.currentTheme = theme;
      this.saveTheme();
      this.applyTheme();
    }
  }

  /**
   * 保存主题到本地存储
   */
  private static saveTheme(): void {
    localStorage.setItem(THEME_KEY, this.currentTheme);
  }

  /**
   * 应用主题到DOM
   */
  private static applyTheme(): void {
    // 移除 Arco Design 的深色主题类，以防万一
    document.body.classList.remove("arco-theme-dark");

    // 根据当前主题添加或移除 Arco Design 的深色主题类
    if (this.currentTheme === ThemeType.DARK) {
      document.body.classList.add("arco-theme-dark");
    } else {
      // 浅色模式不需要特定的类，Arco Design 默认是浅色
      // 如果之前有 'arco-theme-dark'，上面的 remove 已经处理了
    }

    // 更新HTML属性，用于自定义CSS变量选择器或非Arco组件的样式
    document.documentElement.setAttribute("data-theme", this.currentTheme);
    // 兼容旧的 body class 设置，以防有其他地方依赖它
    document.body.classList.remove(ThemeType.LIGHT, ThemeType.DARK); // 先移除旧的
    document.body.classList.add(this.currentTheme); // 再添加当前的
  }
}
