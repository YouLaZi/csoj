# -*- coding: utf-8 -*-
"""Test Login on port 8088"""
from playwright.sync_api import sync_playwright
import sys
import os

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def test_login():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

        print("=" * 60)
        print("Test Login - Port 8088")
        print("=" * 60)

        # Load page
        print("\n[1] Loading homepage...")
        page.goto('http://localhost:8088/')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(5000)

        page.screenshot(path='test_screenshots/test_8088_homepage.png')

        # Check page loaded
        buttons = page.locator('button').all()
        print(f"  Buttons found: {len(buttons)}")

        for btn in buttons[:10]:
            try:
                text = btn.inner_text()[:30]
                print(f"    - {text}")
            except:
                pass

        # Find login button
        print("\n[2] Looking for login button...")
        login_btns = page.locator('button:has-text("登录")').all()
        print(f"  Login buttons: {len(login_btns)}")

        if login_btns:
            login_btns[0].click()
            page.wait_for_timeout(1000)
            page.screenshot(path='test_screenshots/test_8088_login_modal.png')

            # Fill login form
            inputs = page.locator('input:visible').all()
            print(f"  Input fields: {len(inputs)}")

            if len(inputs) >= 2:
                inputs[0].fill("admin")
                inputs[1].fill("123456789")
                print("  Filled login form")

                page.screenshot(path='test_screenshots/test_8088_login_filled.png')

                # Submit
                submit_btns = page.locator('button:has-text("登录")').all()
                if submit_btns:
                    submit_btns[-1].click()
                    page.wait_for_timeout(3000)
                    page.screenshot(path='test_screenshots/test_8088_login_result.png')
                    print("  Submitted login")

        # Check result
        print("\n[3] Checking login state...")
        page.wait_for_timeout(2000)

        body_text = page.locator('body').inner_text()[:500]
        if "退出" in body_text or "admin" in body_text:
            print("  [OK] Login successful!")
        else:
            print("  [INFO] Login state unclear")

        # Print console errors
        print("\n[4] Console errors:")
        errors = [log for log in console_logs if "error" in log.lower()]
        if errors:
            for err in errors[:5]:
                print(f"  {err[:150]}")
        else:
            print("  No errors")

        browser.close()
        print("\n[OK] Test complete!")

if __name__ == "__main__":
    test_login()
