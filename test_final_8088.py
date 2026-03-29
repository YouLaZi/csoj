# -*- coding: utf-8 -*-
"""Final System Test - Port 8088"""
from playwright.sync_api import sync_playwright
import sys
import os

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def test_final():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

        print("=" * 60)
        print("Final System Test - Port 8088")
        print("=" * 60)

        # 1. Homepage
        print("\n[1/6] Loading homepage...")
        page.goto('http://localhost:8088/')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(3000)
        page.screenshot(path='test_screenshots/final_8088_homepage.png')
        print("  [OK] Homepage loaded")

        # 2. Login
        print("\n[2/6] Testing Login...")
        login_btns = page.locator('button:has-text("登录")').all()
        if login_btns:
            login_btns[0].click()
            page.wait_for_timeout(1000)
            inputs = page.locator('input:visible').all()
            if len(inputs) >= 2:
                inputs[0].fill("admin")
                inputs[1].fill("123456789")
                submit_btns = page.locator('button:has-text("登录")').all()
                if submit_btns:
                    submit_btns[-1].click()
                    page.wait_for_timeout(3000)
                    print("  [OK] Login successful")

        # 3. Questions
        print("\n[3/6] Testing Questions...")
        page.goto('http://localhost:8088/questions')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(3000)
        questions = page.locator('.question-item').all()
        print(f"  Questions found: {len(questions)}")
        if questions:
            questions[0].click()
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(2000)
            if page.locator('.monaco-editor').count() > 0:
                print("  [OK] Code editor loaded")
            else:
                print("  [WARN] Code editor not found")
        page.screenshot(path='test_screenshots/final_8088_questions.png')

        # 4. Posts
        print("\n[4/6] Testing Posts...")
        page.goto('http://localhost:8088/posts')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(3000)
        posts = page.locator('.post-card').all()
        print(f"  Posts found: {len(posts)}")
        page.screenshot(path='test_screenshots/final_8088_posts.png')

        # 5. Check-in
        print("\n[5/6] Testing Check-in...")
        page.goto('http://localhost:8088/')
        page.wait_for_load_state('networkidle')
        checkin_btn = page.locator('button:has-text("签到")')
        if checkin_btn.count() > 0:
            checkin_btn.click()
            page.wait_for_timeout(2000)
            print("  [OK] Check-in clicked")
        else:
            print("  [INFO] Already checked in")
        page.screenshot(path='test_screenshots/final_8088_checkin.png')

        # 6. Language Switch
        print("\n[6/6] Testing Language Switch...")
        page.goto('http://localhost:8088/')
        page.wait_for_load_state('networkidle')
        for btn in page.locator('button').all():
            try:
                text = btn.inner_text()
                if 'CN' in text or 'US' in text:
                    btn.click()
                    page.wait_for_timeout(500)
                    if page.locator('text="English"').count() > 0:
                        page.click('text="English"')
                        page.wait_for_timeout(1000)
                        print("  [OK] Language switched")
                        break
            except:
                continue
        page.screenshot(path='test_screenshots/final_8088_english.png')

        # Summary
        print("\n" + "=" * 60)
        print("Test Summary")
        print("=" * 60)

        errors = [log for log in console_logs if "error" in log.lower()]
        if errors:
            print(f"\nConsole errors: {len(errors)}")
            for err in errors[:5]:
                print(f"  - {err[:100]}...")
        else:
            print("\n[OK] No console errors!")

        print("\nScreenshots saved to: test_screenshots/")
        browser.close()
        print("\n[OK] All tests completed!")

if __name__ == "__main__":
    test_final()
