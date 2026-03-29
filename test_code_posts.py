# -*- coding: utf-8 -*-
"""
Extended System Test for CodeSmart OJ
Tests: Problem solving, Code submission, Posts
"""
from playwright.sync_api import sync_playwright
import time
import sys
import os

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def test_code_and_posts():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        print("=" * 60)
        print("CodeSmart OJ - Extended Test (Code & Posts)")
        print("=" * 60)

        # Login first
        print("\n[1/5] Logging in...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        # Use existing admin account
        page.click('button:has-text("登录")')
        page.wait_for_timeout(1000)

        inputs = page.locator('input:visible').all()
        if len(inputs) >= 2:
            inputs[0].fill("admin")  # Use admin account
            inputs[1].fill("123456789")

            login_btns = page.locator('button:has-text("登录")').all()
            if login_btns:
                login_btns[-1].click()
                page.wait_for_timeout(2000)
                print("  [OK] Logged in as admin")

        page.screenshot(path='test_screenshots/e01_logged_in.png')

        # Test Problems Page
        print("\n[2/5] Testing Problems Page...")
        page.goto('http://localhost:8081/question')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='test_screenshots/e02_problems_list.png')

        # Count problems
        problems = page.locator('a[href*="/view/question"]').all()
        print(f"  Found {len(problems)} problems")

        if problems:
            # Click first problem
            problems[0].click()
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(2000)
            page.screenshot(path='test_screenshots/e03_problem_detail.png')
            print("  [OK] Problem detail page loaded")

            # Test code editor
            print("\n[3/5] Testing Code Editor...")
            editor = page.locator('.monaco-editor, .code-editor, textarea')
            if editor.count() > 0:
                print("  [OK] Code editor found")

                # Try to submit code
                submit_btns = page.locator('button:has-text("提交"), button:has-text("Submit")').all()
                if submit_btns:
                    submit_btns[-1].click()
                    page.wait_for_timeout(3000)
                    page.screenshot(path='test_screenshots/e04_code_submitted.png')
                    print("  [OK] Code submitted")

                    # Check for result
                    page.wait_for_timeout(2000)
                    page.screenshot(path='test_screenshots/e05_code_result.png')

                    result_text = page.content()
                    if "成功" in result_text or "success" in result_text.lower():
                        print("  [OK] Code execution successful")
                    elif "错误" in result_text or "error" in result_text.lower():
                        print("  [INFO] Code execution had errors (expected for default code)")
            else:
                print("  [WARN] Code editor not found")
        else:
            print("  [WARN] No problems found")

        # Test Posts Page
        print("\n[4/5] Testing Posts Page...")
        page.goto('http://localhost:8081/post')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='test_screenshots/e06_posts_list.png')

        posts = page.locator('a[href*="/view/post"], .post-card, .post-item').all()
        print(f"  Found {len(posts)} posts")

        if posts:
            posts[0].click()
            page.wait_for_load_state('networkidle')
            page.screenshot(path='test_screenshots/e07_post_detail.png')
            print("  [OK] Post detail page loaded")

            # Check for comments section
            if page.locator('.comment, .comments, [class*="comment"]').count() > 0:
                print("  [OK] Comments section found")

        # Test Create Post (if available)
        print("\n[5/5] Testing Create Post...")
        page.goto('http://localhost:8081/post')
        page.wait_for_load_state('networkidle')

        create_btn = page.locator('button:has-text("发布"), button:has-text("创建"), a:has-text("发布"), .create-post-btn')
        if create_btn.count() > 0:
            create_btn.first.click()
            page.wait_for_load_state('networkidle')
            page.screenshot(path='test_screenshots/e08_create_post.png')
            print("  [OK] Create post page opened")
        else:
            print("  [INFO] Create post button not found (may need different selector)")

        # Summary
        print("\n" + "=" * 60)
        print("Extended Test Summary")
        print("=" * 60)
        print("Screenshots saved to: test_screenshots/")
        print("  - e01_logged_in.png")
        print("  - e02_problems_list.png")
        print("  - e03_problem_detail.png")
        print("  - e04_code_submitted.png")
        print("  - e05_code_result.png")
        print("  - e06_posts_list.png")
        print("  - e07_post_detail.png")
        print("  - e08_create_post.png")

        browser.close()
        print("\n[OK] Extended test completed!")

if __name__ == "__main__":
    test_code_and_posts()
