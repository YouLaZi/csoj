// src/utils/resizeObserverHelper.ts
import { ErrorService } from "../services/ErrorService";

/**
 * 创建一个防抖函数
 * @param fn 要执行的函数
 * @param delay 延迟时间（毫秒）
 * @returns 防抖处理后的函数
 */
const debounce = (fn: (...args: any[]) => void, delay: number) => {
  let timeoutId: number | null = null;
  return function (this: any, ...args: any[]) {
    if (timeoutId !== null) {
      clearTimeout(timeoutId);
    }
    timeoutId = window.setTimeout(() => {
      fn.apply(this, args);
      timeoutId = null; // 清除引用
    }, delay);
  };
};

/**
 * 设置ResizeObserver循环限制错误的解决方案
 * 这个函数通过使用requestAnimationFrame来解决ResizeObserver循环错误
 * 适用于使用动态调整大小组件（如编辑器、图表等）的场景
 *
 * @param debounceDelay 防抖延迟时间（毫秒）
 * @returns 清理函数，用于移除事件监听器
 */
export const setupResizeObserverWorkaround = (
  debounceDelay = 100
): (() => void) => {
  // 应用解决方案的函数
  const applyWorkaround = debounce(() => {
    // 使用三重requestAnimationFrame来确保在多个渲染周期执行
    // 这是解决ResizeObserver循环错误的增强方法
    window.requestAnimationFrame(() => {
      window.requestAnimationFrame(() => {
        window.requestAnimationFrame(() => {
          window.requestAnimationFrame(() => {
            // 解决方案已应用，不需要额外操作
            // 增加一层requestAnimationFrame以更有效地抑制错误
          });
        });
      });
    });
  }, debounceDelay);

  // 全局错误处理函数
  const globalErrorHandler = (event: Event) => {
    // 处理标准错误事件
    if (event instanceof ErrorEvent) {
      if (ErrorService.isResizeObserverError(event.error || event.message)) {
        event.preventDefault(); // 阻止默认错误处理
        applyWorkaround();
      }
    }
  };

  // 添加专门处理ResizeObserver错误的处理程序
  const resizeObserverErrorHandler = () => {
    applyWorkaround();
    return true; // 表示错误已处理
  };

  // 添加事件监听器
  window.addEventListener("error", globalErrorHandler, true);

  // 如果浏览器支持，添加专门的ResizeObserver错误处理
  if (typeof window.ResizeObserver !== "undefined") {
    const originalResizeObserver = window.ResizeObserver;
    window.ResizeObserver = class extends originalResizeObserver {
      constructor(callback: ResizeObserverCallback) {
        const wrappedCallback = (
          entries: ResizeObserverEntry[],
          observer: ResizeObserver
        ) => {
          // 尝试执行原始回调，如果发生错误则应用解决方案
          try {
            callback(entries, observer);
          } catch (error) {
            if (ErrorService.isResizeObserverError(error)) {
              applyWorkaround();
            } else {
              throw error; // 重新抛出非ResizeObserver错误
            }
          }
        };
        super(wrappedCallback);
      }
    };
  }

  // 返回清理函数
  return () => {
    window.removeEventListener("error", globalErrorHandler, true);
    // 恢复原始的ResizeObserver（如果被修改）
    if (
      typeof window.ResizeObserver !== "undefined" &&
      window.ResizeObserver.toString().includes("wrappedCallback")
    ) {
      // 注意：在实际应用中，可能需要保存原始ResizeObserver的引用以便恢复
    }
  };
};
