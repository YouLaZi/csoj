# -*- coding: utf-8 -*-
"""Debug - Wait for Vue to render"""
from playwright.sync_api import sync_playwright
import sys
import os
import time

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def debug_vue():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

        print("=" * 60)
        print("Debug - Wait for Vue Render")
        print("=" * 60)

        # Load page
        print("\n[1] Loading homepage and waiting for Vue...")
        page.goto('http://localhost:8081/', wait_until='domcontentloaded')

        # Wait for Vue app to mount (look for specific Vue-rendered elements)
        try:
            # Wait for navigation or any Vue-rendered element
            page.wait_for_selector('#app > *', timeout=15000)
            print("  Vue app mounted!")
        except Exception as e:
            print(f"  Wait error: {e}")

        # Additional wait for async data
        page.wait_for_timeout(5000)

        page.screenshot(path='test_screenshots/debug_vue_loaded.png')

        # Check app element
        app_content = page.locator('#app').inner_html()[:500]
        print(f"\n[2] #app content preview:\n  {app_content[:300]}...")

        # Check for navigation
        nav_items = page.locator('nav, .arco-menu, [class*="nav"]').all()
        print(f"\n[3] Navigation elements: {len(nav_items)}")

        # Check for buttons
        buttons = page.locator('button').all()
        print(f"  Buttons: {len(buttons)}")
        for btn in buttons[:5]:
            try:
                print(f"    - {btn.inner_text()[:30]}")
            except:
                pass

        # Print console errors
        print("\n[4] Console errors:")
        errors = [log for log in console_logs if "error" in log.lower()]
        if errors:
            for err in errors[:10]:
                print(f"  {err[:200]}")
        else:
            print("  No errors")

        # Check body text
        print("\n[5] Page body text (first 500 chars):")
        body = page.locator('body').inner_text()[:500]
        print(f"  {body}")

        browser.close()
        print("\n[OK] Debug complete!")

if __name__ == "__main__":
    debug_vue()
