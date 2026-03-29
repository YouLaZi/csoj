# -*- coding: utf-8 -*-
"""
CSOJ 后端 API 测试脚本
测试所有主要API端点
"""
import requests
import json
import sys
import os
from datetime import datetime

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

# 测试配置
BASE_URL = "http://localhost:8121/api"
TEST_USER = {"account": "admin", "password": "123456789"}


class APITester:
    def __init__(self):
        self.session = requests.Session()
        self.csrf_token = None
        self.results = []
        self.passed = 0
        self.failed = 0
        self.logged_in = False

        self.log_test("INIT", "INFO", f"测试地址: {BASE_URL}")

        self.log_test("INIT", "INFO", f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    def log_test(self, name, status, message="", response=None):
        """记录测试结果"""
        result = {
            "name": name,
            "status": status,
            "message": message,
            "time": datetime.now().strftime("%H:%M:%S")
        }
        self.results.append(result)
        if status == "PASS":
            self.passed += 1
            print(f"  ✅ {name}: {message}")
        elif status == "FAIL":
            self.failed += 1
            print(f"  ❌ {name}: {message}")
            if response:
                print(f"      响应: {response.text[:200]}")
        else:
            print(f"  ⚠️  {name}: {message}")
    def get_headers(self, with_csrf=True):
        """获取请求头"""
        headers = {"Content-Type": "application/json"}
        if with_csrf and self.csrf_token:
            headers["X-CSRF-Token"] = self.csrf_token
        return headers
    def get_csrf_token(self):
        """获取CSRF Token"""
        try:
            resp = self.session.get(f"{BASE_URL}/user/get/login", timeout=10)
            self.csrf_token = resp.headers.get('x-csrf-token', '')
            if self.csrf_token:
                print(f"  📌 CSRF Token: {self.csrf_token[:20]}...")
                return True
            return False
        except Exception as e:
            print(f"  ⚠️  获取CSRF Token失败: {e}")
            return False
    def login(self):
        """登录并保持会话"""
        try:
            resp = self.session.post(
                f"{BASE_URL}/user/login",
                json={"userAccount": TEST_USER["account"], "userPassword": TEST_USER["password"]},
                headers=self.get_headers(),
                timeout=10
            )
            data = resp.json()
            if data.get("code") == 0:
                self.logged_in = True
                new_token = resp.headers.get('x-csrf-token')
                if new_token:
                    self.csrf_token = new_token
                return True, "登录成功"
            else:
                return False, data.get("message", "未知错误")
        except Exception as e:
            return False, str(e)
    def logout(self):
        """登出"""
        try:
            resp = self.session.post(
                f"{BASE_URL}/user/logout",
                headers=self.get_headers(),
                timeout=10
            )
            data = resp.json()
            self.logged_in = False
            return data.get("code") == 0
        except:
            return False
    def test_health(self):
        """测试服务健康状态"""
        print("\n[1] 服务健康检查")
        try:
            resp = self.session.get(f"{BASE_URL}/user/get/login", timeout=10)
            if resp.status_code in [200, 401]:
                self.log_test("服务状态", "PASS", f"HTTP {resp.status_code}")
                return True
            else:
                self.log_test("服务状态", "FAIL", f"HTTP {resp.status_code}")
                return False
        except Exception as e:
            self.log_test("服务状态", "FAIL", str(e))
            return False
    def test_csrf_protection(self):
        """测试CSRF保护"""
        print("\n[2] CSRF保护测试")
        try:
            # 不带CSRF Token的POST请求
            resp = self.session.post(
                f"{BASE_URL}/user/login",
                json=TEST_USER,
                timeout=10
            )
            if resp.status_code == 403 or "CSRF" in resp.text:
                self.log_test("CSRF保护", "PASS", "正确拒绝无Token请求")
            else:
                self.log_test("CSRF保护", "WARN", f"响应: {resp.status_code}")
        except Exception as e:
            self.log_test("CSRF保护", "FAIL", str(e))
    def test_user_apis(self):
        """测试用户相关API"""
        print("\n[3] 用户API测试")
        # 3.1 登录
        success, msg = self.login()
        if success:
            self.log_test("用户登录", "PASS", f"用户: {TEST_USER['account']}")
        else:
            self.log_test("用户登录", "FAIL", msg)
            return
        # 3.2 获取当前用户
        try:
            resp = self.session.get(f"{BASE_URL}/user/get/login", timeout=10)
            data = resp.json()
            if data.get("code") == 0 and data.get("data"):
                self.log_test("获取当前用户", "PASS", f"用户名: {data['data'].get('userName', 'N/A')}")
            else:
                self.log_test("获取当前用户", "FAIL", data.get("message", "无数据"))
        except Exception as e:
            self.log_test("获取当前用户", "FAIL", str(e))
    def test_question_apis(self):
        """测试题目相关API"""
        print("\n[4] 题目API测试")
        # 4.1 获取题目列表 (使用公开API /vo)
        try:
            resp = self.session.post(
                f"{BASE_URL}/question/list/page/vo",
                json={"current": 1, "pageSize": 10, "sortField": "createTime", "sortOrder": "descend"},
                headers=self.get_headers(),
                timeout=10
            )
            data = resp.json()
            if data.get("code") == 0:
                total = data.get("data", {}).get("total", 0)
                records = len(data.get("data", {}).get("records", []))
                self.log_test("题目列表", "PASS", f"总数: {total}, 当前页: {records}")
            else:
                self.log_test("题目列表", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("题目列表", "FAIL", str(e))
        # 4.2 获取题目详情 (使用公开API /vo)
        try:
            resp = self.session.get(f"{BASE_URL}/question/get/vo?id=1", timeout=10)
            data = resp.json()
            if data.get("code") == 0 and data.get("data"):
                self.log_test("题目详情", "PASS", f"标题: {data['data'].get('title', 'N/A')[:30]}")
            elif data.get("code") == 40400 or "不存在" in data.get("message", "") or data.get("code") == 404:
                # 题目不存在是正常情况
                self.log_test("题目详情", "PASS", "题目ID=1不存在（正常）")
            else:
                self.log_test("题目详情", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("题目详情", "FAIL", str(e))
    def test_post_apis(self):
        """测试帖子相关API"""
        print("\n[5] 帖子API测试")
        # 5.1 获取帖子列表 (使用公开API /vo)
        try:
            resp = self.session.post(
                f"{BASE_URL}/post/list/page/vo",
                json={"current": 1, "pageSize": 10, "sortField": "createTime", "sortOrder": "descend"},
                headers=self.get_headers(),
                timeout=10
            )
            data = resp.json()
            if data.get("code") == 0:
                total = data.get("data", {}).get("total", 0)
                records = len(data.get("data", {}).get("records", []))
                self.log_test("帖子列表", "PASS", f"总数: {total}, 当前页: {records}")
            else:
                self.log_test("帖子列表", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("帖子列表", "FAIL", str(e))
        # 5.2 获取帖子详情 (使用公开API /vo)
        try:
            resp = self.session.get(f"{BASE_URL}/post/get/vo?id=1", timeout=10)
            data = resp.json()
            if data.get("code") == 0 and data.get("data"):
                self.log_test("帖子详情", "PASS", f"标题: {data['data'].get('title', 'N/A')[:30]}")
            elif data.get("code") == 40400 or "不存在" in data.get("message", "") or data.get("code") == 404:
                self.log_test("帖子详情", "PASS", "帖子ID=1不存在（正常）")
            else:
                self.log_test("帖子详情", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("帖子详情", "FAIL", str(e))
    def test_checkin_apis(self):
        """测试签到相关API"""
        print("\n[6] 签到API测试")
        # 首先重新获取CSRF Token（登录后可能已更新）
        try:
            resp = self.session.get(f"{BASE_URL}/user/get/login", timeout=10)
            new_token = resp.headers.get('x-csrf-token')
            if new_token:
                self.csrf_token = new_token
                print(f"  📌 更新CSRF Token: {self.csrf_token[:20]}...")
        except:
            pass
        # 6.1 签到
        try:
            resp = self.session.post(
                f"{BASE_URL}/checkin",
                headers=self.get_headers(),
                timeout=10
            )
            data = resp.json()
            if data.get("code") == 0:
                self.log_test("每日签到", "PASS", "签到成功")
            elif "已签到" in data.get("message", ""):
                self.log_test("每日签到", "PASS", "今日已签到")
            else:
                self.log_test("每日签到", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("每日签到", "FAIL", str(e))
        # 6.2 获取签到状态
        try:
            resp = self.session.get(f"{BASE_URL}/checkin/status", timeout=10)
            data = resp.json()
            if data.get("code") == 0:
                status_data = data.get("data", {})
                checked = status_data.get("checked", False) if isinstance(status_data, dict) else False
                self.log_test("签到状态", "PASS", f"已签到: {checked}")
            else:
                self.log_test("签到状态", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("签到状态", "FAIL", str(e))
    def test_leaderboard_apis(self):
        """测试排行榜API"""
        print("\n[7] 排行榜API测试")
        try:
            resp = self.session.get(
                f"{BASE_URL}/leaderboard/points",
                params={"timeRange": "all", "count": 10},
                timeout=10
            )
            data = resp.json()
            if data.get("code") == 0:
                count = len(data.get("data", []))
                self.log_test("积分排行榜", "PASS", f"返回 {count} 条记录")
            else:
                self.log_test("积分排行榜", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("积分排行榜", "FAIL", str(e))
    def test_announcement_apis(self):
        """测试公告API"""
        print("\n[8] 公告API测试")
        try:
            resp = self.session.get(
                f"{BASE_URL}/announcement/list",
                params={"page": 1, "pageSize": 10},
                timeout=10
            )
            data = resp.json()
            if data.get("code") == 0:
                total = data.get("data", {}).get("total", 0)
                self.log_test("公告列表", "PASS", f"总数: {total}")
            else:
                self.log_test("公告列表", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("公告列表", "FAIL", str(e))
        try:
            resp = self.session.get(
                f"{BASE_URL}/announcement/latest",
                params={"count": 5},
                timeout=10
            )
            data = resp.json()
            if data.get("code") == 0:
                count = len(data.get("data", []))
                self.log_test("最新公告", "PASS", f"返回 {count} 条公告")
            else:
                self.log_test("最新公告", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("最新公告", "FAIL", str(e))
    def test_tag_apis(self):
        """测试标签API"""
        print("\n[9] 标签API测试")
        try:
            resp = self.session.get(f"{BASE_URL}/tag/list", timeout=10)
            data = resp.json()
            if data.get("code") == 0:
                count = len(data.get("data", []))
                self.log_test("标签列表", "PASS", f"返回 {count} 个标签")
            else:
                self.log_test("标签列表", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("标签列表", "FAIL", str(e))
    def test_logout(self):
        """测试登出"""
        print("\n[10] 登出测试")
        try:
            resp = self.session.post(
                f"{BASE_URL}/user/logout",
                headers=self.get_headers(),
                timeout=10
            )
            data = resp.json()
            if data.get("code") == 0:
                self.log_test("用户登出", "PASS", "登出成功")
            else:
                self.log_test("用户登出", "FAIL", data.get("message", "未知错误"))
        except Exception as e:
            self.log_test("用户登出", "FAIL", str(e))
    def run_all_tests(self):
        """运行所有测试"""
        print("=" * 60)
        print("CSOJ 后端 API 测试")
        print("=" * 60)
        # 获取CSRF Token
        print("\n[0] 初始化")
        self.get_csrf_token()
        # 运行测试 - 按照依赖关系顺序执行
        self.test_health()
        self.test_csrf_protection()
        self.test_user_apis()  # 登录
        self.test_question_apis()  # 需要登录
        self.test_post_apis()  # 需要登录
        self.test_checkin_apis()  # 需要登录
        self.test_leaderboard_apis()
        self.test_announcement_apis()
        self.test_tag_apis()
        self.test_logout()  # 最后登出
        # 输出总结
        print("\n" + "=" * 60)
        print("测试结果汇总")
        print("=" * 60)
        total = self.passed + self.failed
        print(f"  总计: {total} | 通过: {self.passed} | 失败: {self.failed}")
        if total > 0:
            print(f"  通过率: {(self.passed/total*100):.1f}%")
        # 保存报告
        os.makedirs("test_results", exist_ok=True)
        report = {
            "summary": {
                "total": total,
                "passed": self.passed,
                "failed": self.failed,
                "pass_rate": f"{(self.passed/total*100):.1f}%" if total > 0 else "0%"
            },
            "results": self.results,
            "timestamp": datetime.now().isoformat()
        }
        with open("test_results/api_test_report.json", "w", encoding="utf-8") as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        print(f"\n报告已保存: test_results/api_test_report.json")
        return self.failed == 0
if __name__ == "__main__":
    tester = APITester()
    success = tester.run_all_tests()
    sys.exit(0 if success else 1)
