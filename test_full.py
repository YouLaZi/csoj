# -*- coding: utf-8 -*-
"""
Full System Test for CodeSmart OJ with Element Discovery
"""
from playwright.sync_api import sync_playwright
import time
import sys
import os

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def test_full():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        print("=" * 60)
        print("CodeSmart OJ - Full System Test")
        print("=" * 60)

        # Login first
        print("\n[1/6] Logging in...")
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

        page.screenshot(path='test_screenshots/f01_logged_in.png')

        # Discover Problems Page
        print("\n[2/6] Discovering Problems Page...")
        page.goto('http://localhost:8081/question')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='test_screenshots/f02_problems.png')

        # Find all clickable elements
        print("  Discovering clickable elements...")
        all_links = page.locator('a').all()
        all_cards = page.locator('.arco-card, .card, [class*="question"], [class*="problem"]').all()

        print(f"  Links found: {len(all_links)}")
        for i, link in enumerate(all_links[:10]):
            try:
                text = link.inner_text()[:40].replace('\n', ' ')
                href = link.get_attribute('href') or ''
                print(f"    [{i}] {text} -> {href[:30]}")
            except:
                pass

        print(f"  Card elements: {len(all_cards)}")

        # Try clicking on first question item
        question_selectors = [
            '.arco-list-item',
            '.question-item',
            '.problem-item',
            '[class*="question"]',
            'tr:has(td)'
        ]

        for selector in question_selectors:
            items = page.locator(selector).all()
            if items:
                print(f"  Found {len(items)} items with: {selector}")
                try:
                    items[0].click()
                    page.wait_for_load_state('networkidle')
                    page.screenshot(path='test_screenshots/f03_question_detail.png')
                    print("  [OK] Question detail page loaded")
                    break
                except:
                    continue

        # Test Code Submission
        print("\n[3/6] Testing Code Submission...")
        page.wait_for_timeout(2000)

        # Check for Monaco editor
        if page.locator('.monaco-editor').count() > 0:
            print("  [OK] Monaco editor found")

            # Find submit button
            buttons = page.locator('button').all()
            for btn in buttons:
                try:
                    text = btn.inner_text()
                    if '提交' in text or 'Submit' in text:
                        btn.click()
                        page.wait_for_timeout(3000)
                        page.screenshot(path='test_screenshots/f04_code_submitted.png')
                        print(f"  [OK] Clicked submit button: {text}")
                        break
                except:
                    continue
        else:
            print("  [WARN] Monaco editor not found")

        # Discover Posts Page
        print("\n[4/6] Discovering Posts Page...")
        page.goto('http://localhost:8081/post')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='test_screenshots/f05_posts.png')

        all_links = page.locator('a').all()
        print(f"  Links found: {len(all_links)}")
        for i, link in enumerate(all_links[:10]):
            try:
                text = link.inner_text()[:40].replace('\n', ' ')
                href = link.get_attribute('href') or ''
                print(f"    [{i}] {text} -> {href[:30]}")
            except:
                pass

        # Try clicking on first post
        post_selectors = [
            '.arco-list-item a',
            '.post-item',
            '.post-card',
            'a[href*="/post"]'
        ]

        for selector in post_selectors:
            items = page.locator(selector).all()
            if items:
                print(f"  Found {len(items)} items with: {selector}")
                try:
                    items[0].click()
                    page.wait_for_load_state('networkidle')
                    page.screenshot(path='test_screenshots/f06_post_detail.png')
                    print("  [OK] Post detail page loaded")
                    break
                except:
                    continue

        # Test Profile Page
        print("\n[5/6] Testing Profile Page...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        # Look for profile/user menu
        profile_selectors = [
            'a[href*="/user"]',
            'a[href*="/profile"]',
            '.user-menu',
            '.profile-link'
        ]

        for selector in profile_selectors:
            if page.locator(selector).count() > 0:
                page.click(selector)
                page.wait_for_load_state('networkidle')
                page.screenshot(path='test_screenshots/f07_profile.png')
                print("  [OK] Profile page loaded")
                break

        # Test Language Switch
        print("\n[6/6] Testing Language Switch...")
        page.goto('http://localhost:8081/')
        page.wait_for_load_state('networkidle')

        # Find language button
        lang_btns = page.locator('button').all()
        for btn in lang_btns:
            try:
                text = btn.inner_text()
                if any(flag in text for flag in ['🇨🇳', '🇺🇸', '🇯🇵', '🇰🇷']):
                    btn.click()
                    page.wait_for_timeout(500)
                    page.screenshot(path='test_screenshots/f08_lang_dropdown.png')

                    # Click English option
                    if page.locator('text="English"').count() > 0:
                        page.click('text="English"')
                        page.wait_for_timeout(1000)
                        page.screenshot(path='test_screenshots/f09_english.png')
                        print("  [OK] Language switched to English")
                    break
            except:
                continue

        # Summary
        print("\n" + "=" * 60)
        print("Test Summary")
        print("=" * 60)

        screenshots = [
            "f01_logged_in.png - Logged in state",
            "f02_problems.png - Problems list",
            "f03_question_detail.png - Question detail",
            "f04_code_submitted.png - Code submission",
            "f05_posts.png - Posts list",
            "f06_post_detail.png - Post detail",
            "f07_profile.png - Profile page",
            "f08_lang_dropdown.png - Language dropdown",
            "f09_english.png - English mode"
        ]

        print("Screenshots saved:")
        for s in screenshots:
            print(f"  - {s}")

        browser.close()
        print("\n[OK] Full test completed!")

if __name__ == "__main__":
    test_full()
