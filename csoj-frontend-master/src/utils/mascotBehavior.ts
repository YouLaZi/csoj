/**
 * 小猫行为控制器
 * 根据用户行为和上下文触发小猫的主动行为
 */

import { eventBus, FunEvents } from "./funEvents";

// 上下文类型
export interface MascotContext {
  // 页面信息
  currentPage: string;
  route: string;

  // 题目相关
  questionContext?: {
    id: string;
    difficulty: string;
    tags: string[];
    timeSpent: number; // 毫秒
    attemptCount: number;
  };

  // 用户状态
  userState: {
    isLoggedIn: boolean;
    todayCheckin: boolean;
    todaySolved: number;
    currentStreak: number;
    lastActiveTime: number;
  };

  // 时间信息
  timeContext: {
    hour: number;
    isLateNight: boolean;
    isWeekend: boolean;
  };

  // 选中文本
  selectedText?: {
    content: string;
    type: string;
  };
}

// 行为动作
export interface MascotAction {
  mood: "happy" | "sad" | "thinking" | "sleeping" | "excited" | "surprised";
  message: string;
  particles?: string;
  duration?: number;
}

// 行为规则
interface BehaviorRule {
  id: string;
  condition: (ctx: MascotContext) => boolean;
  action: MascotAction;
  cooldown: number; // 冷却时间（毫秒）
  priority: number; // 优先级，数字越大越优先
  lastTriggered?: number; // 上次触发时间
}

// 配置
interface BehaviorConfig {
  enabled: boolean;
  proactiveEnabled: boolean;
  selectionEnabled: boolean;
}

class MascotBehaviorController {
  private context: MascotContext;
  private rules: BehaviorRule[] = [];
  private config: BehaviorConfig = {
    enabled: true,
    proactiveEnabled: true,
    selectionEnabled: true,
  };

  private activityTimer: number | null = null;
  private questionTimer: number | null = null;
  private lastActivityTime = Date.now();

  // 回调函数
  private onActionCallback?: (action: MascotAction, ruleId: string) => void;

  constructor() {
    this.context = this.initContext();
    this.initRules();
    this.startActivityTracking();
  }

  /**
   * 初始化上下文
   */
  private initContext(): MascotContext {
    const now = new Date();
    const hour = now.getHours();

    return {
      currentPage: "",
      route: window.location.pathname,
      userState: {
        isLoggedIn: false,
        todayCheckin: false,
        todaySolved: 0,
        currentStreak: 0,
        lastActiveTime: Date.now(),
      },
      timeContext: {
        hour,
        isLateNight: hour >= 23 || hour < 6,
        isWeekend: now.getDay() === 0 || now.getDay() === 6,
      },
    };
  }

  /**
   * 初始化行为规则
   */
  private initRules(): void {
    this.rules = [
      // 欢迎回来 - 首次访问
      {
        id: "welcome_back",
        condition: (ctx) => this.isFirstVisitToday(),
        action: {
          mood: "excited",
          message: "喵~欢迎回来！今天要刷题吗？",
          particles: "💕",
        },
        cooldown: 86400000, // 24小时
        priority: 100,
      },

      // 深夜刷题提醒
      {
        id: "late_night_coding",
        condition: (ctx) => ctx.timeContext.isLateNight,
        action: {
          mood: "sleeping",
          message: "这么晚还在刷题？注意休息喵~",
        },
        cooldown: 3600000, // 1小时
        priority: 40,
      },

      // 题目卡住提示
      {
        id: "stuck_on_question",
        condition: (ctx) =>
          ctx.questionContext
            ? ctx.questionContext.timeSpent > 300000 &&
              ctx.questionContext.attemptCount >= 2
            : false,
        action: {
          mood: "thinking",
          message: "这道题有点难？需要我给点提示吗？",
        },
        cooldown: 180000, // 3分钟
        priority: 60,
      },

      // 长时间未操作
      {
        id: "idle_reminder",
        condition: () => Date.now() - this.lastActivityTime > 120000, // 2分钟
        action: {
          mood: "happy",
          message: "在思考吗？还是有其他问题喵？",
        },
        cooldown: 300000, // 5分钟
        priority: 20,
      },

      // 提交失败多次
      {
        id: "multiple_failures",
        condition: (ctx) =>
          ctx.questionContext
            ? ctx.questionContext.attemptCount >= 3 &&
              ctx.questionContext.attemptCount < 10
            : false,
        action: {
          mood: "sad",
          message: "别灰心！让我帮你分析一下错误吧~",
        },
        cooldown: 600000, // 10分钟
        priority: 70,
      },

      // 周末提醒
      {
        id: "weekend_greeting",
        condition: (ctx) =>
          ctx.timeContext.isWeekend &&
          ctx.timeContext.hour >= 9 &&
          ctx.timeContext.hour < 12,
        action: {
          mood: "happy",
          message: "周末愉快！今天有时间多刷几题喵~",
        },
        cooldown: 86400000, // 24小时
        priority: 30,
      },

      // 连续签到提醒
      {
        id: "checkin_reminder",
        condition: (ctx) =>
          ctx.userState.isLoggedIn && !ctx.userState.todayCheckin,
        action: {
          mood: "excited",
          message: "今天还没签到哦！来签到领积分喵~",
        },
        cooldown: 3600000, // 1小时
        priority: 50,
      },

      // 成就鼓励 - 完成多题
      {
        id: "achievement_encourage",
        condition: (ctx) =>
          ctx.userState.todaySolved > 0 && ctx.userState.todaySolved % 5 === 0,
        action: {
          mood: "excited",
          message: "哇！今天已经解决了好几道题！太棒了！",
          particles: "🎉",
        },
        cooldown: 3600000, // 1小时
        priority: 55,
      },

      // 连续打卡成就
      {
        id: "streak_achievement",
        condition: (ctx) =>
          ctx.userState.currentStreak > 0 &&
          ctx.userState.currentStreak % 7 === 0,
        action: {
          mood: "excited",
          message: "连续打卡一周啦！你太厉害了喵！",
          particles: "🌟",
        },
        cooldown: 86400000, // 24小时
        priority: 80,
      },

      // 选中文本后主动询问
      {
        id: "selection_help",
        condition: (ctx) =>
          ctx.selectedText !== undefined &&
          ctx.selectedText.content.length >= 10,
        action: {
          mood: "thinking",
          message: "需要我帮你解释这段内容吗？",
        },
        cooldown: 30000, // 30秒
        priority: 90,
      },
    ];

    // 按优先级排序
    this.rules.sort((a, b) => b.priority - a.priority);
  }

  /**
   * 开始活动追踪
   */
  private startActivityTracking(): void {
    const updateActivity = () => {
      this.lastActivityTime = Date.now();
      this.context.userState.lastActiveTime = this.lastActivityTime;
    };

    document.addEventListener("mousemove", updateActivity);
    document.addEventListener("keydown", updateActivity);
    document.addEventListener("click", updateActivity);
    document.addEventListener("scroll", updateActivity);

    // 定期检查行为触发
    this.activityTimer = window.setInterval(() => {
      this.evaluateRules();
    }, 10000); // 每10秒检查一次
  }

  /**
   * 设置回调
   */
  onAction(callback: (action: MascotAction, ruleId: string) => void): void {
    this.onActionCallback = callback;
  }

  /**
   * 更新上下文
   */
  updateContext(partial: Partial<MascotContext>): void {
    this.context = { ...this.context, ...partial };
    this.evaluateRules();
  }

  /**
   * 更新用户状态
   */
  updateUserState(partial: Partial<MascotContext["userState"]>): void {
    this.context.userState = { ...this.context.userState, ...partial };
  }

  /**
   * 更新题目上下文
   */
  updateQuestionContext(
    partial: Partial<NonNullable<MascotContext["questionContext"]>>
  ): void {
    if (this.context.questionContext) {
      this.context.questionContext = {
        ...this.context.questionContext,
        ...partial,
      };
    } else {
      this.context.questionContext =
        partial as MascotContext["questionContext"];
    }
  }

  /**
   * 设置选中文本
   */
  setSelectedText(text: MascotContext["selectedText"]): void {
    this.context.selectedText = text;
    this.evaluateRules();
  }

  /**
   * 清除选中文本
   */
  clearSelectedText(): void {
    this.context.selectedText = undefined;
  }

  /**
   * 开始计时题目
   */
  startQuestionTimer(
    questionId: string,
    difficulty: string,
    tags: string[]
  ): void {
    this.stopQuestionTimer();

    this.context.questionContext = {
      id: questionId,
      difficulty,
      tags,
      timeSpent: 0,
      attemptCount: 0,
    };

    const startTime = Date.now();
    this.questionTimer = window.setInterval(() => {
      if (this.context.questionContext) {
        this.context.questionContext.timeSpent = Date.now() - startTime;
      }
    }, 1000);
  }

  /**
   * 停止题目计时
   */
  stopQuestionTimer(): void {
    if (this.questionTimer) {
      clearInterval(this.questionTimer);
      this.questionTimer = null;
    }
  }

  /**
   * 增加尝试次数
   */
  incrementAttempt(): void {
    if (this.context.questionContext) {
      this.context.questionContext.attemptCount++;
    }
  }

  /**
   * 评估所有规则
   */
  private evaluateRules(): void {
    if (!this.config.enabled || !this.config.proactiveEnabled) return;

    const now = Date.now();

    for (const rule of this.rules) {
      // 检查冷却时间
      if (rule.lastTriggered && now - rule.lastTriggered < rule.cooldown) {
        continue;
      }

      // 检查条件
      try {
        if (rule.condition(this.context)) {
          this.triggerRule(rule);
          break; // 只触发优先级最高的一个
        }
      } catch (e) {
        console.warn(`[MascotBehavior] Rule ${rule.id} condition error:`, e);
      }
    }
  }

  /**
   * 触发规则
   */
  private triggerRule(rule: BehaviorRule): void {
    rule.lastTriggered = Date.now();
    console.log(`[MascotBehavior] Triggered: ${rule.id}`);

    if (this.onActionCallback) {
      this.onActionCallback(rule.action, rule.id);
    }
  }

  /**
   * 手动触发行为
   */
  trigger(
    mood: MascotAction["mood"],
    message: string,
    particles?: string
  ): void {
    if (this.onActionCallback) {
      this.onActionCallback({ mood, message, particles }, "manual");
    }
  }

  /**
   * 检查是否是今天首次访问
   */
  private isFirstVisitToday(): boolean {
    const lastVisit = localStorage.getItem("mascot_last_visit");
    const today = new Date().toDateString();

    if (lastVisit !== today) {
      localStorage.setItem("mascot_last_visit", today);
      return true;
    }

    return false;
  }

  /**
   * 启用/禁用
   */
  setEnabled(enabled: boolean): void {
    this.config.enabled = enabled;
  }

  /**
   * 设置主动行为开关
   */
  setProactiveEnabled(enabled: boolean): void {
    this.config.proactiveEnabled = enabled;
  }

  /**
   * 销毁
   */
  destroy(): void {
    if (this.activityTimer) {
      clearInterval(this.activityTimer);
    }
    this.stopQuestionTimer();
  }
}

// 导出单例
export const mascotBehavior = new MascotBehaviorController();
export default MascotBehaviorController;
