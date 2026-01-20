const { defineConfig } = require("@vue/cli-service");
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");

module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack(config) {
    config.plugin("monaco").use(new MonacoWebpackPlugin());
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
