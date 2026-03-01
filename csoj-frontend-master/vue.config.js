const { defineConfig } = require("@vue/cli-service");
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");

module.exports = defineConfig({
  transpileDependencies: true,
  // 禁用生产环境 source map 以减少内存使用
  productionSourceMap: false,
  // 禁用并行处理以减少内存使用
  parallel: false,
  chainWebpack(config) {
    config.plugin("monaco").use(new MonacoWebpackPlugin());
    // 禁用预加载和预获取以减少内存使用
    config.plugins.delete("prefetch");
    config.plugins.delete("preload");
  },
  // 设置页面标题
  pages: {
    index: {
      entry: "src/main.ts",
      title: "CodeSmart",
    },
  },
  // 配置开发服务器代理
  devServer: {
    proxy: {
      "/api": {
        target: "http://localhost:8121",
        changeOrigin: true,
        // 不重写路径，因为后端已经配置了context-path为/api
        // pathRewrite: {'^/api': ''}
      },
    },
    client: {
      overlay: false,
    },
  },
});
