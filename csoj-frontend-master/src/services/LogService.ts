import axios from "axios";

/**
 * 日志级别枚举
 */
export enum LogLevel {
  DEBUG = "debug",
  INFO = "info",
  WARN = "warn",
  ERROR = "error",
}

/**
 * 日志条目接口
 */
interface LogEntry {
  timestamp: number;
  level: LogLevel;
  message: string;
  data?: any;
  context?: Record<string, any>;
}

/**
 * 日志服务配置
 */
interface LogServiceConfig {
  // 是否启用控制台输出
  enableConsole: boolean;
  // 是否启用远程日志
  enableRemoteLogging: boolean;
  // 远程日志API地址
  remoteLogEndpoint?: string;
  // 最小日志级别
  minLevel: LogLevel;
  // 最大缓存日志数量
  maxBufferSize: number;
  // 自动发送日志的时间间隔（毫秒）
  flushInterval: number;
}

/**
 * 日志服务
 * 提供统一的日志记录机制，支持不同级别的日志，并可配置远程日志上报
 */
export class LogService {
  // 默认配置
  private static config: LogServiceConfig = {
    enableConsole: true,
    enableRemoteLogging: false,
    minLevel:
      process.env.NODE_ENV === "production" ? LogLevel.INFO : LogLevel.DEBUG,
    maxBufferSize: 100,
    flushInterval: 30000, // 30秒
  };

  // 日志缓冲区
  private static buffer: LogEntry[] = [];

  // 定时发送计时器
  private static flushTimer: number | null = null;

  // 日志级别优先级映射
  private static readonly levelPriority: Record<LogLevel, number> = {
    [LogLevel.DEBUG]: 0,
    [LogLevel.INFO]: 1,
    [LogLevel.WARN]: 2,
    [LogLevel.ERROR]: 3,
  };

  /**
   * 初始化日志服务
   */
  public static init(config?: Partial<LogServiceConfig>): void {
    // 合并配置
    if (config) {
      this.config = { ...this.config, ...config };
    }

    // 在生产环境中禁用控制台调试日志
    if (process.env.NODE_ENV === "production") {
      this.config.minLevel = LogLevel.INFO;

      // 重写控制台方法，防止开发日志泄露到生产环境
      if (!this.config.enableConsole) {
        console.debug = () => {
          // 空函数，不输出任何内容
        };
      }
    }

    // 设置定时发送
    if (this.config.enableRemoteLogging && this.config.remoteLogEndpoint) {
      this.startFlushTimer();

      // 页面卸载前尝试发送剩余日志
      window.addEventListener("beforeunload", () => {
        this.flush();
      });
    }
  }

  /**
   * 记录调试日志
   */
  public static debug(
    message: string,
    data?: any,
    context?: Record<string, any>
  ): void {
    this.log(LogLevel.DEBUG, message, data, context);
  }

  /**
   * 记录信息日志
   */
  public static info(
    message: string,
    data?: any,
    context?: Record<string, any>
  ): void {
    this.log(LogLevel.INFO, message, data, context);
  }

  /**
   * 记录警告日志
   */
  public static warn(
    message: string,
    data?: any,
    context?: Record<string, any>
  ): void {
    this.log(LogLevel.WARN, message, data, context);
  }

  /**
   * 记录错误日志
   */
  public static error(
    message: string,
    data?: any,
    context?: Record<string, any>
  ): void {
    this.log(LogLevel.ERROR, message, data, context);
  }

  /**
   * 记录日志
   */
  private static log(
    level: LogLevel,
    message: string,
    data?: any,
    context?: Record<string, any>
  ): void {
    // 检查日志级别
    if (this.levelPriority[level] < this.levelPriority[this.config.minLevel]) {
      return;
    }

    const entry: LogEntry = {
      timestamp: Date.now(),
      level,
      message,
      data,
      context: {
        url: window.location.href,
        userAgent: navigator.userAgent,
        ...context,
      },
    };

    // 输出到控制台
    if (this.config.enableConsole) {
      this.consoleLog(entry);
    }

    // 添加到缓冲区
    if (this.config.enableRemoteLogging && this.config.remoteLogEndpoint) {
      this.buffer.push(entry);

      // 如果缓冲区已满，立即发送
      if (this.buffer.length >= this.config.maxBufferSize) {
        this.flush();
      }
    }
  }

  /**
   * 输出到控制台
   */
  private static consoleLog(entry: LogEntry): void {
    const { level, message, data } = entry;
    const timestamp = new Date(entry.timestamp).toISOString();
    const prefix = `[${timestamp}] [${level.toUpperCase()}]`;

    switch (level) {
      case LogLevel.DEBUG:
        console.debug(prefix, message, data || "");
        break;
      case LogLevel.INFO:
        console.info(prefix, message, data || "");
        break;
      case LogLevel.WARN:
        console.warn(prefix, message, data || "");
        break;
      case LogLevel.ERROR:
        console.error(prefix, message, data || "");
        break;
    }
  }

  /**
   * 发送日志到远程服务器
   */
  public static async flush(): Promise<void> {
    if (
      !this.config.enableRemoteLogging ||
      !this.config.remoteLogEndpoint ||
      this.buffer.length === 0
    ) {
      return;
    }

    const logs = [...this.buffer];
    this.buffer = [];

    try {
      await axios.post(this.config.remoteLogEndpoint, { logs });
    } catch (error) {
      // 发送失败，将日志放回缓冲区
      this.buffer = [...logs, ...this.buffer];
      // 限制缓冲区大小，防止内存泄漏
      if (this.buffer.length > this.config.maxBufferSize) {
        this.buffer = this.buffer.slice(-this.config.maxBufferSize);
      }
    }
  }

  /**
   * 启动定时发送计时器
   */
  private static startFlushTimer(): void {
    if (this.flushTimer !== null) {
      clearInterval(this.flushTimer);
    }

    this.flushTimer = window.setInterval(() => {
      this.flush();
    }, this.config.flushInterval);
  }

  /**
   * 停止定时发送计时器
   */
  public static stopFlushTimer(): void {
    if (this.flushTimer !== null) {
      clearInterval(this.flushTimer);
      this.flushTimer = null;
    }
  }

  /**
   * 设置配置
   */
  public static setConfig(config: Partial<LogServiceConfig>): void {
    this.config = { ...this.config, ...config };

    // 如果更改了发送间隔，重新启动计时器
    if (config.flushInterval && this.flushTimer !== null) {
      this.startFlushTimer();
    }
  }

  /**
   * 获取当前配置
   */
  public static getConfig(): LogServiceConfig {
    return { ...this.config };
  }
}

export default LogService;
