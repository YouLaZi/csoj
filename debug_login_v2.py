# -*- coding: utf-8 -*-
"""Debug Login - Wait for page fully loaded"""
from playwright.sync_api import sync_playwright
import sys
import os
import time

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def debug_login():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

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
        print("Debug Login - Full Page Load")
        print("=" * 60)

        # Load page and wait for Vue to render
        print("\n[1] Loading homepage...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(5000)  # Wait for Vue to initialize

        page.screenshot(path='test_screenshots/debug_homepage.png')

        # Find all buttons
        all_buttons = page.locator('button').all()
        print(f"\n[2] All buttons found: {len(all_buttons)}")
        for i, btn in enumerate(all_buttons[:15]):
            try:
                text = btn.inner_text()[:50]
                print(f"  [{i}] {text}")
            except:
                pass

        # Find login buttons specifically
        print("\n[3] Looking for login buttons...")
        login_selectors = [
            'button:has-text("登录")',
            'button:has-text("Login")',
            '.arco-btn:has-text("登录")',
            'button[class*="login"]'
        ]

        for selector in login_selectors:
            count = page.locator(selector).count()
            print(f"  {selector}: {count} found")

        # Check page content
        print("\n[4] Page content check...")
        body_text = page.locator('body').inner_text()[:300]
        print(f"  Body preview: {body_text[:200]}...")

        # Print console errors
        print("\n[5] Console logs:")
        for log in console_logs[:10]:
            print(f"  {log[:150]}")

        browser.close()
        print("\n[OK] Debug complete!")

if __name__ == "__main__":
    debug_login()
