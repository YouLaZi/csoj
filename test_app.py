from playwright.sync_api import sync_playwright
import os

# Create screenshots directory
os.makedirs('f:/CODE/JAVA/oj/test_screenshots', exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={'width': 1920, 'height': 1080}
    )
    page = context.new_page()

    # Capture console logs
    console_messages = []
    page.on('console', lambda msg: console_messages.append(f'{msg.type}: {msg.text}'))

    print("=" * 60)
    print("CSOJ Application Test Report")
    print("=" * 60)

    # Test 1: Homepage with longer wait
    print("\n[1] Testing Homepage...")
    try:
        page.goto('http://localhost:8088/', timeout=60000)
        page.wait_for_selector('#app', timeout=10000)
        page.wait_for_timeout(10000)  # Longer wait for Vue to render
        page.screenshot(path='f:/CODE/JAVA/oj/test_screenshots/01_homepage.png', full_page=True)
        title = page.title()
        print(f"    Page title: {title}")

        # Check if app content is visible
        app_content = page.locator('#app').inner_html()
        if len(app_content) > 100:
            print(f"    Vue app rendered ({len(app_content)} chars)")
        else:
            print("    Warning: Vue app may not have rendered")

        # Check for specific elements
        try:
            if page.locator('.welcome-section').is_visible():
                print("    Welcome section visible")
            if page.locator('nav').is_visible():
                print("    Navigation visible")
        except:
            pass

        print("    Screenshot saved: 01_homepage.png")
    except Exception as e:
        print(f"    Error: {e}")

    # Test 2: Questions page
    print("\n[2] Testing Questions page...")
    try:
        page.goto('http://localhost:8088/questions', timeout=60000)
        page.wait_for_selector('#app', timeout=10000)
        page.wait_for_timeout(10000)
        page.screenshot(path='f:/CODE/JAVA/oj/test_screenshots/02_questions.png', full_page=True)
        print("    Screenshot saved: 02_questions.png")
    except Exception as e:
        print(f"    Error: {e}")

    # Test 3: Login page (note: this redirects to /?showLogin=true)
    print("\n[3] Testing Login redirect...")
    try:
        page.goto('http://localhost:8088/user/login', timeout=60000)
        page.wait_for_selector('#app', timeout=10000)
        page.wait_for_timeout(10000)
        current_url = page.url
        print(f"    Redirected to: {current_url}")
        page.screenshot(path='f:/CODE/JAVA/oj/test_screenshots/03_login.png', full_page=True)
        print("    Screenshot saved: 03_login.png")
    except Exception as e:
        print(f"    Error: {e}")

    # Test 4: Register page
    print("\n[4] Testing Register redirect...")
    try:
        page.goto('http://localhost:8088/user/register', timeout=60000)
        page.wait_for_selector('#app', timeout=10000)
        page.wait_for_timeout(10000)
        current_url = page.url
        print(f"    Redirected to: {current_url}")
        page.screenshot(path='f:/CODE/JAVA/oj/test_screenshots/04_register.png', full_page=True)
        print("    Screenshot saved: 04_register.png")
    except Exception as e:
        print(f"    Error: {e}")

    # Test 5: Forgot password page (should NOT redirect)
    print("\n[5] Testing Forgot Password page...")
    try:
        page.goto('http://localhost:8088/user/forgot-password', timeout=60000)
        page.wait_for_selector('#app', timeout=10000)
        page.wait_for_timeout(10000)
        current_url = page.url
        print(f"    URL: {current_url}")
        page.screenshot(path='f:/CODE/JAVA/oj/test_screenshots/05_forgot_password.png', full_page=True)
        print("    Screenshot saved: 05_forgot_password.png")
    except Exception as e:
        print(f"    Error: {e}")

    # Test 6: Backend API check
    print("\n[6] Testing Backend API...")
    try:
        api_response = page.request.get('http://localhost:8121/api/')
        print(f"    Backend API status: {api_response.status()}")
    except Exception as e:
        print(f"    Backend API check: {e}")

    # Print console errors if any
    errors = [msg for msg in console_messages if 'error' in msg.lower()]
    warnings = [msg for msg in console_messages if 'warning' in msg.lower() or 'warn' in msg.lower()]
    if errors:
        print("\n[Console Errors]")
        for err in errors[:5]:
            print(f"    {err}")
    if warnings:
        print("\n[Console Warnings]")
        for warn in warnings[:3]:
            print(f"    {warn}")

    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)
    print("Screenshots saved to: f:/CODE/JAVA/oj/test_screenshots/")
    print("Frontend URL: http://localhost:8088/")
    print("Backend URL: http://localhost:8121/api/")

    browser.close()
    print("\nTests completed!")
