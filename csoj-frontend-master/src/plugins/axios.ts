// Add a request interceptor
import axios from "axios";

// 设置默认配置
axios.defaults.withCredentials = true;
// axios.defaults.timeout = 10000; // 10秒超时
axios.defaults.timeout = 100000; // 10秒超时
axios.defaults.headers.common["Content-Type"] = "application/json";
axios.defaults.headers.common["Accept"] = "application/json";

axios.interceptors.request.use(
  function (config) {
    // 请求前处理

    // 添加CSRF保护
    const csrfToken = document
      .querySelector('meta[name="csrf-token"]')
      ?.getAttribute("content");
    if (csrfToken) {
      config.headers["X-CSRF-TOKEN"] = csrfToken;
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
