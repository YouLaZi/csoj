/**
 * 全局事件总线 - 用于组件间通信
 * 支持触发庆祝效果、成就解锁等有趣的功能
 */

type EventCallback = (...args: any[]) => void;

class EventBus {
  private events: Map<string, EventCallback[]> = new Map();

  /**
   * 监听事件
   */
  on(event: string, callback: EventCallback) {
    if (!this.events.has(event)) {
      this.events.set(event, []);
    }
    this.events.get(event)!.push(callback);
  }

  /**
   * 取消监听
   */
  off(event: string, callback: EventCallback) {
    if (!this.events.has(event)) return;
    const callbacks = this.events.get(event)!;
    const index = callbacks.indexOf(callback);
    if (index > -1) {
      callbacks.splice(index, 1);
    }
  }

  /**
   * 触发事件
   */
  emit(event: string, ...args: any[]) {
    if (!this.events.has(event)) return;
    this.events.get(event)!.forEach((callback) => {
      callback(...args);
    });
  }

  /**
   * 只监听一次
   */
  once(event: string, callback: EventCallback) {
    const onceCallback = (...args: any[]) => {
      callback(...args);
      this.off(event, onceCallback);
    };
    this.on(event, onceCallback);
  }
}

// 创建全局事件总线实例
export const eventBus = new EventBus();

// 预定义事件类型
export const FunEvents = {
  // 代码提交成功
  SUBMISSION_SUCCESS: "fun:submission_success",
  // 代码通过所有测试
  SUBMISSION_ACCEPTED: "fun:submission_accepted",
  // 签到成功
  CHECKIN_SUCCESS: "fun:checkin_success",
  // 连续签到里程碑
  CHECKIN_STREAK: "fun:checkin_streak",
  // 成就解锁
  ACHIEVEMENT_UNLOCKED: "fun:achievement_unlocked",
  // 等级提升
  LEVEL_UP: "fun:level_up",
  // 获得积分
  POINTS_EARNED: "fun:points_earned",
  // 解决问题
  PROBLEM_SOLVED: "fun:problem_solved",

  // 猫咪系统事件
  CAT_FED: "fun:cat_fed",
  CAT_PLAYED: "fun:cat_played",
  CAT_LEVEL_UP: "fun:cat_level_up",
  CAT_MOOD_CHANGE: "fun:cat_mood_change",

  // 成就系统事件
  ACHIEVEMENT_PROGRESS: "fun:achievement_progress",

  // 每日挑战事件
  CHALLENGE_COMPLETED: "fun:challenge_completed",
  CHALLENGE_STREAK: "fun:challenge_streak",
  DOUBLE_POINTS: "fun:double_points",

  // 数据可视化事件
  STATS_UPDATED: "fun:stats_updated",
  SKILL_LEVEL_UP: "fun:skill_level_up",
} as const;

// 便捷方法：触发庆祝效果
export const triggerCelebration = (
  type:
    | "success"
    | "level-up"
    | "achievement"
    | "streak"
    | "custom" = "success",
  title?: string,
  subtitle?: string
) => {
  eventBus.emit("fun:celebration", { type, title, subtitle });
};

// 便捷方法：触发成就解锁
export const triggerAchievement = (achievementId: string) => {
  eventBus.emit(FunEvents.ACHIEVEMENT_UNLOCKED, achievementId);
};

// 便捷方法：触发提交成功
export const triggerSubmissionSuccess = (accepted: boolean, score?: number) => {
  if (accepted) {
    eventBus.emit(FunEvents.SUBMISSION_ACCEPTED, { score });
  } else {
    eventBus.emit(FunEvents.SUBMISSION_SUCCESS, { score });
  }
};

export default eventBus;
