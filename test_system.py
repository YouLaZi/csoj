# -*- coding: utf-8 -*-
"""
System Integration Test for CodeSmart OJ - Discovery Mode
First discovers available elements, then tests functionality
"""
from playwright.sync_api import sync_playwright
import time
import sys
import os

# Force UTF-8 output
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def test_system():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

        print("=" * 60)
        print("CodeSmart OJ System Integration Test")
        print("=" * 60)

        # 1. Homepage Discovery
        print("\n[1/6] Homepage Discovery...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='test_screenshots/01_homepage.png')

        # Discover buttons
        buttons = page.locator('button').all()
        print(f"  Found {len(buttons)} buttons:")
        for i, btn in enumerate(buttons[:10]):
            try:
                text = btn.inner_text()[:30] if btn.inner_text() else "(no text)"
                print(f"    [{i}] {text}")
            except:
                pass

        # Discover links
        links = page.locator('a').all()
        print(f"  Found {len(links)} links:")
        for i, link in enumerate(links[:10]):
            try:
                text = link.inner_text()[:30] if link.inner_text() else "(no text)"
                print(f"    [{i}] {text}")
            except:
                pass

        # 2. Test Registration
        print("\n[2/6] Testing User Registration...")

        # Try to find register button in multiple ways
        register_found = False
        register_selectors = [
            'button:has-text("Register")',
            'button:has-text("register")',
            'button:has-text("注册")',
            '.arco-btn:has-text("Register")',
            '.arco-btn:has-text("注册")'
        ]

        for selector in register_selectors:
            try:
                if page.locator(selector).count() > 0:
                    page.click(selector)
                    register_found = True
                    print(f"  Found register button with: {selector}")
                    break
            except:
                continue

        if not register_found:
            # Look for any button containing "reg" or "注"
            for btn in buttons:
                try:
                    text = btn.inner_text().lower()
                    if 'register' in text or 'reg' in text or '注' in text:
                        btn.click()
                        register_found = True
                        print(f"  Found register button: {btn.inner_text()}")
                        break
                except:
                    continue

        if register_found:
            page.wait_for_timeout(1000)
            page.screenshot(path='test_screenshots/02_registration_modal.png')

            # Fill registration form
            timestamp = int(time.time())
            test_user = f"testuser_{timestamp}"
            test_password = "Test@123456"

            # Find all VISIBLE input fields in the modal
            inputs = page.locator('input:visible').all()
            print(f"  Found {len(inputs)} visible input fields")

            for i, inp in enumerate(inputs):
                try:
                    inp_type = inp.get_attribute('type') or 'text'
                    placeholder = inp.get_attribute('placeholder') or ''
                    print(f"    [{i}] type={inp_type}, placeholder={placeholder[:30]}")
                except:
                    pass

            if len(inputs) >= 4:
                inputs[0].fill(test_user)  # Account
                inputs[1].fill(test_password)  # Password
                inputs[2].fill(test_password)  # Confirm Password
                inputs[3].fill(f"TestUser{timestamp}")  # Username
                print(f"  [OK] Form filled (user: {test_user})")

                page.screenshot(path='test_screenshots/03_form_filled.png')

                # Find submit button
                submit_btns = page.locator('button[type="submit"], button:has-text("Register"), button:has-text("注册")').all()
                if submit_btns:
                    submit_btns[-1].click()
                    page.wait_for_timeout(2000)
                    page.screenshot(path='test_screenshots/04_after_register.png')
                    print("  [OK] Registration submitted")
        else:
            print("  [WARN] Register button not found")

        # 3. Test Login
        print("\n[3/6] Testing User Login...")

        # Close modal if open
        try:
            page.keyboard.press('Escape')
            page.wait_for_timeout(500)
        except:
            pass

        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        # Find login button
        login_found = False
        login_selectors = [
            'button:has-text("Login")',
            'button:has-text("login")',
            'button:has-text("登录")',
            '.arco-btn:has-text("Login")'
        ]

        for selector in login_selectors:
            try:
                if page.locator(selector).count() > 0:
                    page.click(selector)
                    login_found = True
                    print(f"  Found login button with: {selector}")
                    break
            except:
                continue

        if login_found:
            page.wait_for_timeout(1000)
            page.screenshot(path='test_screenshots/05_login_modal.png')

            inputs = page.locator('input:visible').all()
            if len(inputs) >= 2:
                inputs[0].fill(test_user)
                inputs[1].fill(test_password)
                print(f"  [OK] Login form filled")

                page.screenshot(path='test_screenshots/06_login_filled.png')

                # Submit login
                submit_btns = page.locator('button[type="submit"], button:has-text("Login"), button:has-text("登录")').all()
                if submit_btns:
                    submit_btns[-1].click()
                    page.wait_for_timeout(2000)
                    page.screenshot(path='test_screenshots/07_after_login.png')
                    print("  [OK] Login submitted")

        # 4. Test Check-in
        print("\n[4/6] Testing Daily Check-in...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        # Find check-in button
        checkin_selectors = [
            'button:has-text("Check-in")',
            'button:has-text("签到")',
            '.checkin-calendar button:not([disabled])'
        ]

        for selector in checkin_selectors:
            try:
                if page.locator(selector).count() > 0:
                    page.click(selector)
                    page.wait_for_timeout(2000)
                    page.screenshot(path='test_screenshots/08_checkin.png')
                    print(f"  [OK] Check-in clicked")
                    break
            except:
                continue

        # 5. Test Problem Navigation
        print("\n[5/6] Testing Problem Navigation...")
        page.goto('http://localhost:8081/question')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='test_screenshots/09_problems.png')

        # Click first problem
        problem_links = page.locator('a[href*="/view/question"]').all()
        if problem_links:
            problem_links[0].click()
            page.wait_for_load_state('networkidle')
            page.screenshot(path='test_screenshots/10_problem_detail.png')
            print("  [OK] Problem page loaded")

            # Try to submit code
            page.wait_for_timeout(2000)
            submit_selectors = [
                'button:has-text("Submit")',
                'button:has-text("提交")',
                'button:has-text("提交代码")'
            ]

            for selector in submit_selectors:
                try:
                    if page.locator(selector).count() > 0:
                        page.click(selector)
                        page.wait_for_timeout(3000)
                        page.screenshot(path='test_screenshots/11_code_submitted.png')
                        print("  [OK] Code submitted")
                        break
                except:
                    continue

        # 6. Test Language Switch
        print("\n[6/6] Testing Language Switch...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        # Find language toggle
        lang_btns = page.locator('button').all()
        for btn in lang_btns:
            try:
                text = btn.inner_text()
                if any(flag in text for flag in ['CN', 'US', 'EN', 'JP', 'KR', 'FR', 'ES']):
                    btn.click()
                    page.wait_for_timeout(500)
                    page.screenshot(path='test_screenshots/12_lang_dropdown.png')

                    # Select English
                    if page.locator('text="English"').count() > 0:
                        page.click('text="English"')
                        page.wait_for_timeout(1000)
                        page.screenshot(path='test_screenshots/13_english_mode.png')
                        print("  [OK] Language switched to English")
                    break
            except:
                continue

        # Summary
        print("\n" + "=" * 60)
        print("Test Summary")
        print("=" * 60)
        print("Screenshots saved to: test_screenshots/")

        errors = [log for log in console_logs if "error" in log.lower()]
        if errors:
            print(f"\nConsole errors: {len(errors)}")
            for err in errors[:3]:
                print(f"  - {err[:80]}...")

        browser.close()
        print("\n[OK] Test completed!")

if __name__ == "__main__":
    test_system()
