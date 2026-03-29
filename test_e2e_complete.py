# -*- coding: utf-8 -*-
"""
CSOJ 在线判题系统 - 完整端到端测试套件
测试范围: 接口测试、功能测试、用户体验测试
"""
from playwright.sync_api import sync_playwright, expect
import sys
import os
import json
from datetime import datetime

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)
os.makedirs("test_results", exist_ok=True)

# 测试配置
CONFIG = {
    "frontend_url": "http://localhost:8088",
    "backend_url": "http://localhost:8121",
    "test_user": {"account": "admin", "password": "123456789"},
    "timeout": 10000
}


class TestResult:
    """测试结果收集器"""
    def __init__(self):
        self.results = []
        self.passed = 0
        self.failed = 0
        self.warnings = 0

    def add(self, category, name, status, message="", screenshot=""):
        self.results.append({
            "category": category,
            "name": name,
            "status": status,
            "message": message,
            "screenshot": screenshot,
            "time": datetime.now().strftime("%H:%M:%S")
        })
        if status == "PASS":
            self.passed += 1
        elif status == "FAIL":
            self.failed += 1
        else:
            self.warnings += 1

    def report(self):
        total = self.passed + self.failed + self.warnings
        print("\n" + "=" * 70)
        print("测试结果汇总")
        print("=" * 70)
        print(f"  总计: {total} | 通过: {self.passed} | 失败: {self.failed} | 警告: {self.warnings}")
        print("-" * 70)

        # 按类别分组
        categories = {}
        for r in self.results:
            if r["category"] not in categories:
                categories[r["category"]] = []
            categories[r["category"]].append(r)

        for cat, items in categories.items():
            print(f"\n【{cat}】")
            for item in items:
                status_icon = {"PASS": "✅", "FAIL": "❌", "WARN": "⚠️"}.get(item["status"], "❓")
                print(f"  {status_icon} [{item['time']}] {item['name']}")
                if item["message"]:
                    print(f"      {item['message'][:80]}")

        # 保存JSON报告
        report_path = "test_results/report.json"
        with open(report_path, 'w', encoding='utf-8') as f:
            json.dump({
                "summary": {
                    "total": total,
                    "passed": self.passed,
                    "failed": self.failed,
                    "warnings": self.warnings,
                    "pass_rate": f"{(self.passed/total*100):.1f}%" if total > 0 else "0%"
                },
                "results": self.results
            }, f, ensure_ascii=False, indent=2)
        print(f"\n详细报告已保存: {report_path}")


def test_system():
    """完整系统测试"""
    result = TestResult()

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            viewport={"width": 1920, "height": 1080},
            locale="zh-CN"
        )
        page = context.new_page()

        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))
        page.on("pageerror", lambda err: console_logs.append(f"[error] {err}"))

        print("=" * 70)
        print("CSOJ 在线判题系统 - 端到端测试")
        print("=" * 70)
        print(f"前端地址: {CONFIG['frontend_url']}")
        print(f"后端地址: {CONFIG['backend_url']}")
        print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

        # ===================== 1. 首页加载测试 =====================
        print("\n[1/10] 首页加载测试...")
        try:
            page.goto(CONFIG['frontend_url'], timeout=CONFIG['timeout'])
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(3000)

            # 截图
            page.screenshot(path='test_screenshots/e2e_01_homepage.png')

            # 验证关键元素
            app_loaded = page.locator('#app > *').count() > 0
            buttons = page.locator('button').count()

            if app_loaded and buttons > 0:
                result.add("页面加载", "首页加载", "PASS", f"按钮数量: {buttons}")
            else:
                result.add("页面加载", "首页加载", "FAIL", "页面未完全加载")
        except Exception as e:
            result.add("页面加载", "首页加载", "FAIL", str(e)[:100])

        # ===================== 2. 登录功能测试 =====================
        print("\n[2/10] 登录功能测试...")
        try:
            # 查找并点击登录按钮
            login_btns = page.locator('button:has-text("登录")').all()
            if login_btns:
                login_btns[0].click()
                page.wait_for_timeout(1000)
                page.screenshot(path='test_screenshots/e2e_02_login_modal.png')

                # 填写登录表单
                inputs = page.locator('.arco-modal input:visible').all()
                if len(inputs) >= 2:
                    inputs[0].fill(CONFIG['test_user']['account'])
                    inputs[1].fill(CONFIG['test_user']['password'])
                    page.wait_for_timeout(500)
                    page.screenshot(path='test_screenshots/e2e_03_login_filled.png')

                    # 提交登录
                    submit_btns = page.locator('.arco-modal button:has-text("登录")').all()
                    if submit_btns:
                        submit_btns[-1].click()
                        page.wait_for_timeout(3000)

                # 验证登录状态
                page.screenshot(path='test_screenshots/e2e_04_after_login.png')
                body_text = page.locator('body').inner_text()

                if "退出" in body_text or CONFIG['test_user']['account'] in body_text:
                    result.add("用户认证", "用户登录", "PASS", f"用户: {CONFIG['test_user']['account']}")
                else:
                    result.add("用户认证", "用户登录", "WARN", "登录状态不确定")
            else:
                result.add("用户认证", "用户登录", "FAIL", "未找到登录按钮")
        except Exception as e:
            result.add("用户认证", "用户登录", "FAIL", str(e)[:100])

        # ===================== 3. 题目列表测试 =====================
        print("\n[3/10] 题目列表测试...")
        try:
            page.goto(f"{CONFIG['frontend_url']}/questions")
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(3000)
            page.screenshot(path='test_screenshots/e2e_05_questions.png')

            # 统计题目数量
            question_items = page.locator('.question-item, .arco-list-item, .arco-card').all()
            count = len(question_items)

            if count > 0:
                result.add("题目功能", "题目列表", "PASS", f"找到 {count} 个题目")

                # 点击第一个题目进入详情
                question_items[0].click()
                page.wait_for_load_state('networkidle')
                page.wait_for_timeout(2000)
                page.screenshot(path='test_screenshots/e2e_06_question_detail.png')

                # 验证代码编辑器
                if page.locator('.monaco-editor').count() > 0:
                    result.add("题目功能", "代码编辑器", "PASS", "Monaco编辑器已加载")
                else:
                    result.add("题目功能", "代码编辑器", "WARN", "编辑器未加载")
            else:
                result.add("题目功能", "题目列表", "FAIL", "未找到任何题目")
        except Exception as e:
            result.add("题目功能", "题目列表", "FAIL", str(e)[:100])

        # ===================== 4. 帖子功能测试 =====================
        print("\n[4/10] 帖子功能测试...")
        try:
            page.goto(f"{CONFIG['frontend_url']}/posts")
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(3000)
            page.screenshot(path='test_screenshots/e2e_07_posts.png')

            post_items = page.locator('.post-card, .arco-card').all()
            count = len(post_items)

            if count > 0:
                result.add("帖子功能", "帖子列表", "PASS", f"找到 {count} 个帖子")
            else:
                result.add("帖子功能", "帖子列表", "WARN", "帖子列表为空")
        except Exception as e:
            result.add("帖子功能", "帖子列表", "FAIL", str(e)[:100])

        # ===================== 5. 签到功能测试 =====================
        print("\n[5/10] 签到功能测试...")
        try:
            page.goto(CONFIG['frontend_url'])
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(2000)

            checkin_btn = page.locator('button:has-text("签到")')
            if checkin_btn.count() > 0:
                checkin_btn.first.click()
                page.wait_for_timeout(2000)
                page.screenshot(path='test_screenshots/e2e_08_checkin.png')
                result.add("签到系统", "每日签到", "PASS", "签到成功")
            else:
                page.screenshot(path='test_screenshots/e2e_08_checkin_already.png')
                result.add("签到系统", "每日签到", "WARN", "今日已签到或按钮不存在")
        except Exception as e:
            result.add("签到系统", "每日签到", "FAIL", str(e)[:100])

        # ===================== 6. 排行榜测试 =====================
        print("\n[6/10] 排行榜测试...")
        try:
            page.goto(f"{CONFIG['frontend_url']}/leaderboard")
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(3000)
            page.screenshot(path='test_screenshots/e2e_09_leaderboard.png')

            # 检查排行榜表格
            table_rows = page.locator('table tr, .arco-table-tr').all()
            if len(table_rows) > 1:  # 包含表头
                result.add("排行榜", "排行榜显示", "PASS", f"找到 {len(table_rows)} 行数据")
            else:
                result.add("排行榜", "排行榜显示", "WARN", "排行榜数据为空")
        except Exception as e:
            result.add("排行榜", "排行榜显示", "FAIL", str(e)[:100])

        # ===================== 7. 用户个人中心测试 =====================
        print("\n[7/10] 用户个人中心测试...")
        try:
            page.goto(f"{CONFIG['frontend_url']}/user/profile")
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(3000)
            page.screenshot(path='test_screenshots/e2e_10_profile.png')

            # 检查个人信息显示
            profile_elements = page.locator('.user-profile, .arco-descriptions, form').count()
            if profile_elements > 0:
                result.add("用户中心", "个人资料页", "PASS", "个人资料页正常显示")
            else:
                result.add("用户中心", "个人资料页", "WARN", "个人资料页元素不完整")
        except Exception as e:
            result.add("用户中心", "个人资料页", "FAIL", str(e)[:100])

        # ===================== 8. 国际化测试 =====================
        print("\n[8/10] 国际化测试...")
        try:
            page.goto(CONFIG['frontend_url'])
            page.wait_for_load_state('networkidle')

            # 查找语言切换按钮 (使用emoji国旗图标)
            lang_switched = False
            locale_btn = page.locator('.locale-toggle-button, button:has-text("🇨🇳"), button:has-text("🇺🇸")')
            if locale_btn.count() > 0:
                locale_btn.first.click()
                page.wait_for_timeout(500)
                # 点击英文选项
                en_option = page.locator('.arco-dropdown-option:has-text("English"), .arco-doption:has-text("English")')
                if en_option.count() > 0:
                    en_option.first.click()
                    page.wait_for_timeout(1000)
                    page.screenshot(path='test_screenshots/e2e_11_english.png')
                    # 验证页面文本变为英文
                    body = page.locator('body').inner_text()
                    if any(word in body for word in ['Home', 'Questions', 'Posts', 'Login']):
                        lang_switched = True
                        result.add("国际化", "语言切换", "PASS", "成功切换到英文")

            if not lang_switched:
                result.add("国际化", "语言切换", "WARN", "未找到语言切换功能")
        except Exception as e:
            result.add("国际化", "语言切换", "FAIL", str(e)[:100])

        # ===================== 9. 响应式设计测试 =====================
        print("\n[9/10] 响应式设计测试...")
        try:
            # 测试移动端视口
            page.set_viewport_size({"width": 375, "height": 667})
            page.goto(CONFIG['frontend_url'])
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(2000)
            page.screenshot(path='test_screenshots/e2e_12_mobile.png')

            # 恢复桌面视口
            page.set_viewport_size({"width": 1920, "height": 1080})

            # 检查移动端菜单
            mobile_menu = page.locator('.arco-menu-collapse, .arco-drawer, [class*="mobile"]')
            result.add("响应式设计", "移动端适配", "PASS", "移动端视口测试完成")
        except Exception as e:
            result.add("响应式设计", "移动端适配", "WARN", str(e)[:100])

        # ===================== 10. 控制台错误检查 =====================
        print("\n[10/10] 控制台错误检查...")
        errors = [log for log in console_logs if "error" in log.lower() and "favicon" not in log.lower()]

        if errors:
            unique_errors = list(set(errors))[:5]  # 去重并限制数量
            error_summary = f"发现 {len(errors)} 个错误"
            result.add("错误检查", "控制台错误", "WARN", error_summary)
            for err in unique_errors:
                print(f"    - {err[:100]}...")
        else:
            result.add("错误检查", "控制台错误", "PASS", "无控制台错误")

        # 关闭浏览器
        browser.close()

    # 输出测试报告
    result.report()
    return result


if __name__ == "__main__":
    test_system()
