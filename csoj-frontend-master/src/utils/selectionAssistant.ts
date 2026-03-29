/**
 * 文本选择助手
 * 监听用户在页面上的文本选择，提供快速询问功能
 */

export type TextType = "code" | "error" | "math" | "description" | "general";

export interface SelectionInfo {
  text: string;
  type: TextType;
  position: { x: number; y: number };
  range: Range | null;
}

export interface SelectionAssistantOptions {
  minLength?: number;
  showDelay?: number;
  hideDelay?: number;
  onSelection?: (info: SelectionInfo) => void;
  onSelectionCleared?: () => void;
}

class SelectionAssistant {
  private minLength: number;
  private showDelay: number;
  private hideDelay: number;
  private onSelectionCallback?: (info: SelectionInfo) => void;
  private onSelectionClearedCallback?: () => void;

  private showTimer: number | null = null;
  private hideTimer: number | null = null;
  private isRunning = false;
  private lastSelection: SelectionInfo | null = null;

  constructor(options: SelectionAssistantOptions = {}) {
    this.minLength = options.minLength ?? 10;
    this.showDelay = options.showDelay ?? 300;
    this.hideDelay = options.hideDelay ?? 2000;
    this.onSelectionCallback = options.onSelection;
    this.onSelectionClearedCallback = options.onSelectionCleared;
  }

  /**
   * 开始监听选择事件
   */
  start(): void {
    if (this.isRunning) return;
    this.isRunning = true;

    document.addEventListener("selectionchange", this.handleSelectionChange);
    document.addEventListener("mousedown", this.handleMouseDown);
  }

  /**
   * 停止监听
   */
  stop(): void {
    if (!this.isRunning) return;
    this.isRunning = false;

    document.removeEventListener("selectionchange", this.handleSelectionChange);
    document.removeEventListener("mousedown", this.handleMouseDown);
    this.clearTimers();
  }

  /**
   * 获取当前选择信息
   */
  getCurrentSelection(): SelectionInfo | null {
    return this.lastSelection;
  }

  /**
   * 处理选择变化
   */
  private handleSelectionChange = (): void => {
    this.clearHideTimer();

    const selection = window.getSelection();
    if (!selection || selection.isCollapsed) {
      this.scheduleHide();
      return;
    }

    const text = selection.toString().trim();

    if (text.length < this.minLength) {
      this.scheduleHide();
      return;
    }

    // 延迟显示，避免快速选择时的闪烁
    this.showTimer = window.setTimeout(() => {
      this.processSelection(selection, text);
    }, this.showDelay);
  };

  /**
   * 处理鼠标按下（清除选择）
   */
  private handleMouseDown = (e: MouseEvent): void => {
    // 如果点击的不是询问气泡，则清除
    const target = e.target as HTMLElement;
    if (!target.closest(".ask-bubble") && !target.closest(".cute-mascot")) {
      this.scheduleHide();
    }
  };

  /**
   * 处理选择
   */
  private processSelection(selection: Selection, text: string): void {
    const range = selection.getRangeAt(0);
    const rects = range.getClientRects();

    if (rects.length === 0) return;

    // 使用最后一个矩形的位置（通常是选择结束的位置）
    const lastRect = rects[rects.length - 1];
    const position = {
      x: lastRect.right,
      y: lastRect.top + window.scrollY,
    };

    const type = this.detectTextType(text);

    this.lastSelection = {
      text,
      type,
      position,
      range: range.cloneRange(),
    };

    this.onSelectionCallback?.(this.lastSelection);
  }

  /**
   * 检测文本类型
   */
  private detectTextType(text: string): TextType {
    // 代码特征
    if (this.looksLikeCode(text)) return "code";

    // 错误信息特征
    if (this.looksLikeError(text)) return "error";

    // 数学公式特征
    if (this.looksLikeMath(text)) return "math";

    // 题目描述特征（包含常见关键词）
    if (this.looksLikeDescription(text)) return "description";

    return "general";
  }

  /**
   * 检测是否像代码
   */
  private looksLikeCode(text: string): boolean {
    const codePatterns = [
      /[{}()[\];]/, // 括号
      /(function|class|public|private|return|if|else|for|while)\s*\(/, // 关键字
      /(import|include|require|from)\s+/, // 导入语句
      /=&gt;|->/, // 箭头函数
      /\/\/|\/\*|\*\//, // 注释
      /console\.|print\(|System\.out/, // 输出语句
      /\w+\.\w+\(/, // 方法调用
    ];

    const lines = text.split("\n");
    let matchCount = 0;

    for (const pattern of codePatterns) {
      if (pattern.test(text)) {
        matchCount++;
      }
    }

    // 多行且有代码特征
    return (lines.length > 1 && matchCount >= 2) || matchCount >= 3;
  }

  /**
   * 检测是否像错误信息
   */
  private looksLikeError(text: string): boolean {
    const errorPatterns = [
      /error/i,
      /exception/i,
      /failed/i,
      /undefined/i,
      /null\s*pointer/i,
      /syntax\s*error/i,
      /timeout/i,
      /wrong\s*answer/i,
      /runtime\s*error/i,
      /compilation\s*error/i,
      /时间超限|内存超限|答案错误|运行错误|编译错误/,
    ];

    return errorPatterns.some((p) => p.test(text));
  }

  /**
   * 检测是否像数学公式
   */
  private looksLikeMath(text: string): boolean {
    const mathPatterns = [
      /\$.*\$/, // LaTeX 行内公式
      /\$\$.*\$\$/, // LaTeX 块公式
      /[∑∏∫∂√∞±×÷]/, // 数学符号
      /\d+\s*[+\-*/]\s*\d+/, // 简单算式
      /[a-z]\s*=\s*[^=]/, // 变量赋值
      /O\s*\(\s*n\s*\)/i, // 大O表示法
    ];

    return mathPatterns.some((p) => p.test(text));
  }

  /**
   * 检测是否像题目描述
   */
  private lookssLikeDescription(text: string): boolean {
    const descPatterns = [
      /给定|输入|输出|返回|要求|请你|请设计/,
      /given|input|output|return|required|design/i,
      /示例|样例|example/i,
      /数据范围|约束|constraint/i,
      /时间限制|空间限制|time limit|memory limit/i,
    ];

    return descPatterns.some((p) => p.test(text));
  }

  // 保留旧方法名的兼容
  private looksLikeDescription(text: string): boolean {
    return this.lookssLikeDescription(text);
  }

  /**
   * 安排隐藏
   */
  private scheduleHide(): void {
    this.clearShowTimer();
    this.hideTimer = window.setTimeout(() => {
      this.lastSelection = null;
      this.onSelectionClearedCallback?.();
    }, this.hideDelay);
  }

  /**
   * 清除显示定时器
   */
  private clearShowTimer(): void {
    if (this.showTimer) {
      clearTimeout(this.showTimer);
      this.showTimer = null;
    }
  }

  /**
   * 清除隐藏定时器
   */
  private clearHideTimer(): void {
    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
      this.hideTimer = null;
    }
  }

  /**
   * 清除所有定时器
   */
  private clearTimers(): void {
    this.clearShowTimer();
    this.clearHideTimer();
  }
}

// 导出单例
export const selectionAssistant = new SelectionAssistant();
export default SelectionAssistant;
