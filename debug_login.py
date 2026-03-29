# -*- coding: utf-8 -*-
"""Debug Login API Integration"""
from playwright.sync_api import sync_playwright
import sys
import os

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def debug_login():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        # Capture console logs
        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

        # Capture network requests
        network_logs = []
        def on_response(response):
            if '/api/' in response.url:
                try:
                    status = response.status
                    network_logs.append(f"[RESPONSE] {status} {response.url}")
                except:
                    pass
        page.on("response", on_response)

        print("=" * 60)
        print("Debug Login API Integration")
        print("=" * 60)

        # Test backend API directly
        print("\n[1] Testing Backend Login API directly...")
        import requests
        try:
            res = requests.post('http://localhost:8121/api/user/login',
                json={"userAccount": "admin", "userPassword": "123456789"},
                headers={"Content-Type": "application/json"})
            print(f"  Status: {res.status_code}")
            print(f"  Response: {res.text[:500]}")
        except Exception as e:
            print(f"  Error: {e}")

        # Test frontend login flow
        print("\n[2] Testing Frontend Login Flow...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        # Click login button
        login_btns = page.locator('button:has-text("登录")').all()
        print(f"  Login buttons found: {len(login_btns)}")

        if login_btns:
            login_btns[0].click()
            page.wait_for_timeout(1000)
            page.screenshot(path='test_screenshots/debug_login_modal.png')

            # Fill form
            inputs = page.locator('input:visible').all()
            print(f"  Input fields found: {len(inputs)}")

            if len(inputs) >= 2:
                inputs[0].fill("admin")
                inputs[1].fill("123456789")
                print("  Filled login form")

                page.screenshot(path='test_screenshots/debug_login_filled.png')

                # Click submit
                submit_btns = page.locator('button:has-text("登录")').all()
                if submit_btns:
                    print(f"  Submit buttons found: {len(submit_btns)}")
                    submit_btns[-1].click()
                    page.wait_for_timeout(3000)
                    page.screenshot(path='test_screenshots/debug_login_result.png')

        # Print network logs
        print("\n[3] Network Requests:")
        for log in network_logs[-20:]:
            print(f"  {log}")

        # Print console logs
        print("\n[4] Console Logs:")
        errors = [log for log in console_logs if "error" in log.lower()]
        if errors:
            for err in errors[:10]:
                print(f"  ERROR: {err[:200]}")
        else:
            print("  No errors")

        # Check login state
        print("\n[5] Login State Check:")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        # Check if logged in
        if page.locator('text="退出"').count() > 0 or page.locator('text="Logout"').count() > 0:
            print("  [OK] User is logged in")
        elif page.locator('text="登录"').count() > 0:
            print("  [WARN] User is NOT logged in (login button still visible)")
        else:
            print("  [INFO] Login state unclear")

        page.screenshot(path='test_screenshots/debug_login_final.png')

        browser.close()
        print("\n[OK] Debug complete!")

if __name__ == "__main__":
    debug_login()
