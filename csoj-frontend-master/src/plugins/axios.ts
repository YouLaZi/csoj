// Add a request interceptor
import axios from "axios";

// 设置默认配置
axios.defaults.withCredentials = true;
// axios.defaults.timeout = 10000; // 10秒超时
axios.defaults.timeout = 100000; // 10秒超时
axios.defaults.headers.common["Content-Type"] = "application/json";
axios.defaults.headers.common["Accept"] = "application/json";

// 存储CSRF Token
let storedCsrfToken: string | null = null;

axios.interceptors.request.use(
  function (config) {
    // 请求前处理

    // 添加CSRF保护 - 优先使用存储的token，其次查找meta标签
    const csrfToken =
      storedCsrfToken ||
      document
        .querySelector('meta[name="csrf-token"]')
        ?.getAttribute("content");
    if (csrfToken) {
      config.headers["X-CSRF-Token"] = csrfToken;
    }

    return config;
  },
  function (error) {
    // 请求错误处理
    console.error("请求拦截器错误:", error);
    return Promise.reject(error);
  }
);

// Add a response interceptor
axios.interceptors.response.use(
  function (response) {
    // 从响应头获取并存储CSRF Token
    const csrfToken = response.headers["x-csrf-token"];
    if (csrfToken) {
      storedCsrfToken = csrfToken;
      // 同时更新meta标签（如果存在）
      const metaTag = document.querySelector('meta[name="csrf-token"]');
      if (metaTag) {
        metaTag.setAttribute("content", csrfToken);
      }
    }
    // 调试日志
    console.log("响应成功:", response);
    return response;
  },
  function (error) {
    // 错误处理已由 ErrorService.ts 中的拦截器统一处理
    // 此处仅需确保错误被传递下去
    // console.error("响应拦截器错误（axios.ts）:", error); // 保留此行用于调试，或在确认 ErrorService 工作正常后移除
    return Promise.reject(error);
  }
);

export default axios;
