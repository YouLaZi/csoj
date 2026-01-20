import { Message } from "@arco-design/web-vue";
import axios, { AxiosError } from "axios";
import LogService, { LogLevel } from "./LogService";

/**
 * 错误类型枚举
 */
export enum ErrorType {
  // 网络错误
  NETWORK = "network",
  // API错误
  API = "api",
  // 权限错误
  PERMISSION = "permission",
  // 资源不存在
  NOT_FOUND = "not_found",
  // 服务器错误
  SERVER = "server",
  // ResizeObserver错误
  RESIZE_OBSERVER = "resize_observer",
  // 未知错误
  UNKNOWN = "unknown",
}

/**
 * 错误处理服务
 * 提供统一的错误处理机制，包括错误分类、日志记录和用户提示
 */
export class ErrorService {
  // 是否为生产环境
  private static isProduction = process.env.NODE_ENV === "production";

  // 是否已初始化全局错误处理
  private static initialized = false;

  /**
   * 初始化全局错误处理
   */
  public static init(): void {
    if (this.initialized) {
      return;
    }

    // 处理未捕获的Promise错误
    window.addEventListener("unhandledrejection", (event) => {
      this.handleError(event.reason);
    });

    // 处理全局错误
    window.addEventListener("error", (event) => {
      // 阻止默认行为，避免控制台出现重复错误
      if (this.isResizeObserverError(event.error || event.message)) {
        event.preventDefault();
      }
      this.handleError(event.error || event.message);
    });

    // 设置Axios全局错误处理
    this.setupAxiosErrorHandling();

    this.initialized = true;
  }

  /**
   * 设置Axios错误处理
   */
  private static setupAxiosErrorHandling(): void {
    axios.interceptors.response.use(
      (response) => response,
      (error) => {
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  /**
   * 处理API错误
   */
  private static handleApiError(error: AxiosError): void {
    if (error.code === "ECONNABORTED" && error.message.includes("timeout")) {
      this.showErrorMessage("请求超时，请检查网络连接或稍后重试");
      this.logError(ErrorType.NETWORK, "请求超时", error);
      return;
    }

    if (error.message === "Network Error") {
      this.showErrorMessage("网络连接错误，请检查网络或后端服务是否正常");
      this.logError(ErrorType.NETWORK, "网络连接错误", error);
      return;
    }

    if (error.response) {
      const status = error.response.status;

      if (status === 404) {
        this.showErrorMessage("请求的资源不存在");
        this.logError(ErrorType.NOT_FOUND, "资源不存在", error);
        return;
      }

      if (status === 401 || status === 403) {
        this.showErrorMessage("没有权限执行此操作");
        this.logError(ErrorType.PERMISSION, "权限错误", error);
        return;
      }

      if (status >= 500) {
        this.showErrorMessage("服务器错误，请稍后重试");
        this.logError(ErrorType.SERVER, "服务器错误", error);
        return;
      }

      // 处理业务错误
      const data = error.response.data as any;
      if (data && data.message) {
        this.showErrorMessage(data.message);
        this.logError(ErrorType.API, data.message, error);
        return;
      }
    }

    // 未分类的API错误
    this.showErrorMessage("请求失败，请稍后重试");
    this.logError(ErrorType.API, "未分类API错误", error);
  }

  /**
   * 处理错误
   */
  public static handleError(error: any): void {
    // 处理ResizeObserver错误
    if (this.isResizeObserverError(error)) {
      // ResizeObserver错误通常不影响功能，只记录日志不显示给用户
      this.logError(ErrorType.RESIZE_OBSERVER, "ResizeObserver循环错误", error);
      return;
    }

    // 处理Axios错误
    if (axios.isAxiosError(error)) {
      this.handleApiError(error);
      return;
    }

    // 处理其他错误
    const errorMessage = error?.message || "未知错误";
    this.showErrorMessage("操作失败: " + errorMessage);
    this.logError(ErrorType.UNKNOWN, errorMessage, error);
  }

  /**
   * 判断是否为ResizeObserver错误
   */
  public static isResizeObserverError(error: any): boolean {
    if (!error) return false;

    const errorMessage = typeof error === "string" ? error : error.message;
    return (
      !!errorMessage &&
      (errorMessage.includes("ResizeObserver loop") ||
        errorMessage ===
          "ResizeObserver loop completed with undelivered notifications.")
    );
  }

  /**
   * 显示错误消息
   */
  public static showErrorMessage(message: string): void {
    Message.error({
      content: message,
      duration: 3000,
    });
  }

  /**
   * 记录错误日志
   */
  private static logError(type: ErrorType, message: string, error: any): void {
    const context = {
      errorType: type,
      timestamp: new Date().toISOString(),
    };

    // 使用LogService记录错误
    LogService.error(message, error, context);
  }

  /**
   * 创建ResizeObserver错误处理器
   * 返回一个清理函数，用于在组件卸载时移除事件监听
   */
  public static createResizeObserverErrorHandler(): () => void {
    const errorHandler = (event: ErrorEvent) => {
      if (this.isResizeObserverError(event.error || event.message)) {
        event.preventDefault();
        event.stopPropagation();
        this.logError(
          ErrorType.RESIZE_OBSERVER,
          "ResizeObserver循环错误",
          event.error || event.message
        );
      }
    };

    window.addEventListener("error", errorHandler, true);

    // 返回清理函数
    return () => {
      window.removeEventListener("error", errorHandler, true);
    };
  }
}

export default ErrorService;
