# -*- coding: utf-8 -*-
"""Final System Test with Correct Routes"""
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
        print("CodeSmart OJ - Final System Test")
        print("=" * 60)

        # 1. Login
        print("\n[1/7] Testing Login...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        page.click('button:has-text("登录")')
        page.wait_for_timeout(1000)

        inputs = page.locator('input:visible').all()
        if len(inputs) >= 2:
            inputs[0].fill("admin")
            inputs[1].fill("123456789")
            login_btns = page.locator('button:has-text("登录")').all()
            if login_btns:
                login_btns[-1].click()
                page.wait_for_timeout(2000)
                print("  [OK] Logged in as admin")

        page.screenshot(path='test_screenshots/final_01_login.png')

        # 2. Questions Page - CORRECT URL
        print("\n[2/7] Testing Questions Page (/questions)...")
        page.goto('http://localhost:8081/questions')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(3000)
        page.screenshot(path='test_screenshots/final_02_questions.png')

        questions = page.locator('.question-item').all()
        print(f"  Question items found: {len(questions)}")

        if questions:
            # Click first question
            questions[0].click()
            page.wait_for_load_state('networkidle')
            page.wait_for_timeout(2000)
            page.screenshot(path='test_screenshots/final_03_question_detail.png')
            print("  [OK] Question detail page loaded")

            # Check for code editor
            if page.locator('.monaco-editor').count() > 0:
                print("  [OK] Monaco editor found")
            else:
                print("  [WARN] Monaco editor not found")

            # Test submit button
            submit_btns = page.locator('button:has-text("提交"), button:has-text("Submit")').all()
            if submit_btns:
                print(f"  [OK] Submit button found: {submit_btns[0].inner_text()}")
        else:
            print("  [WARN] No questions found on page")

        # 3. Posts Page - CORRECT URL
        print("\n[3/7] Testing Posts Page (/posts)...")
        page.goto('http://localhost:8081/posts')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(3000)
        page.screenshot(path='test_screenshots/final_04_posts.png')

        posts = page.locator('.post-card').all()
        print(f"  Post items found: {len(posts)}")

        if posts:
            posts[0].click()
            page.wait_for_load_state('networkidle')
            page.screenshot(path='test_screenshots/final_05_post_detail.png')
            print("  [OK] Post detail page loaded")

        # 4. Check-in
        print("\n[4/7] Testing Check-in...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        checkin_btn = page.locator('button:has-text("签到"), button:has-text("Check-in")')
        if checkin_btn.count() > 0:
            checkin_btn.click()
            page.wait_for_timeout(2000)
            page.screenshot(path='test_screenshots/final_06_checkin.png')
            print("  [OK] Check-in clicked")
        else:
            print("  [INFO] Check-in button not found (may already checked in)")

        # 5. Leaderboard
        print("\n[5/7] Testing Leaderboard...")
        page.goto('http://localhost:8081/points/leaderboard')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='test_screenshots/final_07_leaderboard.png')
        print("  [OK] Leaderboard page loaded")

        # 6. Profile
        print("\n[6/7] Testing Profile...")
        page.goto('http://localhost:8081/user/profile')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='test_screenshots/final_08_profile.png')
        print("  [OK] Profile page loaded")

        # 7. Language Switch
        print("\n[7/7] Testing Language Switch...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        lang_btns = page.locator('button').all()
        for btn in lang_btns:
            try:
                text = btn.inner_text()
                if any(flag in text for flag in ['CN', 'US', 'EN']):
                    btn.click()
                    page.wait_for_timeout(500)
                    if page.locator('text="English"').count() > 0:
                        page.click('text="English"')
                        page.wait_for_timeout(1000)
                        page.screenshot(path='test_screenshots/final_09_english.png')
                        print("  [OK] Language switched")
                    break
            except:
                continue

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
            print("\nNo console errors!")

        print("\nScreenshots saved to: test_screenshots/")
        browser.close()
        print("\n[OK] All tests completed!")

if __name__ == "__main__":
    test_final()
