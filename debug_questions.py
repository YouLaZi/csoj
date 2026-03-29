# -*- coding: utf-8 -*-
"""Debug Questions Page"""
from playwright.sync_api import sync_playwright
import sys
import os

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

os.makedirs("test_screenshots", exist_ok=True)

def debug_questions():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        # Capture console logs
        console_logs = []
        page.on("console", lambda msg: console_logs.append(f"[{msg.type}] {msg.text}"))

        # Capture network requests
        failed_requests = []
        def on_request_failed(request):
            failed_requests.append(f"{request.method} {request.url} - {request.failure}")
        page.on("requestfailed", on_request_failed)

        print("=" * 60)
        print("Debug Questions Page")
        print("=" * 60)

        # Navigate to questions page
        print("\n[1] Loading questions page...")
        page.goto('http://localhost:8081/question')
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(3000)  # Wait for API calls

        page.screenshot(path='test_screenshots/debug_questions.png')

        # Check page content
        print("\n[2] Page content analysis:")

        # Check if questions list exists
        questions_list = page.locator('.questions-list, .question-item, .arco-list-item').all()
        print(f"  Question items found: {len(questions_list)}")

        # Check for empty state
        empty_state = page.locator('.empty-state, .arco-empty').all()
        print(f"  Empty state elements: {len(empty_state)}")

        # Check for error messages
        error_msgs = page.locator('.arco-message-error, .error-message').all()
        print(f"  Error messages: {len(error_msgs)}")

        # Get page text content
        page_text = page.locator('body').inner_text()
        if "noData" in page_text or "暂无数据" in page_text or "empty" in page_text.lower():
            print("  [!] Page shows empty/no data state")

        # Print console errors
        print("\n[3] Console logs:")
        errors = [log for log in console_logs if "error" in log.lower()]
        if errors:
            for err in errors[:10]:
                print(f"  ERROR: {err[:150]}")
        else:
            print("  No console errors")

        # Print failed requests
        print("\n[4] Failed requests:")
        if failed_requests:
            for req in failed_requests[:10]:
                print(f"  FAILED: {req[:150]}")
        else:
            print("  No failed requests")

        # Check API response directly
        print("\n[5] Testing API directly...")
        api_response = page.evaluate('''async () => {
            try {
                const res = await fetch('http://localhost:8121/api/question/list/page/vo', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({current: 1, pageSize: 10})
                });
                const data = await res.json();
                return {status: res.status, code: data.code, total: data.data?.total, records: data.data?.records?.length};
            } catch(e) {
                return {error: e.message};
            }
        }''')
        print(f"  API Response: {api_response}")

        # Check if the frontend is calling the correct API
        print("\n[6] Checking frontend API calls...")
        # Listen for the next 10 seconds for any API calls
        api_calls = []
        def on_response(response):
            if '/api/' in response.url():
                api_calls.append(f"{response.status()} {response.url()}")
        page.on("response", on_response)

        # Reload and capture API calls
        page.reload()
        page.wait_for_load_state('networkidle')
        page.wait_for_timeout(3000)

        print(f"  API calls captured: {len(api_calls)}")
        for call in api_calls[:10]:
            print(f"    {call}")

        browser.close()
        print("\n[OK] Debug complete!")

if __name__ == "__main__":
    debug_questions()
