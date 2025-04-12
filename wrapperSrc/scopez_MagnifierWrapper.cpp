#include <windows.h>
#include <magnification.h>
#include <jni.h>
#include "scopez_MagnifierWrapper.h"
#include <string.h>

#define WM_RECREATE_WINDOW_SYNC (WM_USER + 101)

static HWND hwndMagnifier = NULL;
static HWND hwndMag = NULL;
static DWORD uiThreadId = 0;
static HANDLE hUIThread = NULL;

static int windowWidth = 300;
static int windowHeight = 300;
static bool circularMode = false;
static double currentZoom = 2.0;
static int currentRefreshRate = 60;
static int offSetX = 0;
static int offSetY = 0;

static bool isRecreatingWindow = false;

LRESULT CALLBACK MagnifierWndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);

void UpdateMagnifierContent()
{
    if (hwndMagnifier && hwndMag)
    {
        int screenWidth = GetSystemMetrics(SM_CXSCREEN);
        int screenHeight = GetSystemMetrics(SM_CYSCREEN);
        POINT center = { (screenWidth / 2) + (offSetX / 2), (screenHeight / 2) + (offSetY / 2) };

        int scaledWidth = (int)(windowWidth / currentZoom);
        int scaledHeight = (int)(windowHeight / currentZoom);

        RECT sourceRect;
        sourceRect.left = (center.x - scaledWidth / 2) + (offSetX / 2);
        sourceRect.top = (center.y - scaledHeight / 2) + (offSetY / 2);
        sourceRect.right = sourceRect.left + scaledWidth;
        sourceRect.bottom = sourceRect.top + scaledHeight;

        MagSetWindowSource(hwndMag, sourceRect);
        InvalidateRect(hwndMagnifier, NULL, TRUE);
        UpdateWindow(hwndMagnifier);
    }
}

void RecreateWindow()
{
    HINSTANCE hInst = GetModuleHandleW(NULL);

    isRecreatingWindow = true;
    if (hwndMagnifier)
    {
        KillTimer(hwndMagnifier, 1);
        DestroyWindow(hwndMagnifier);
        hwndMagnifier = NULL;
        hwndMag = NULL;
    }

    hwndMagnifier = CreateWindowExW(
        WS_EX_TOPMOST | WS_EX_LAYERED | WS_EX_TRANSPARENT,
        L"MagnifierWindowClassJNI",
        L"Magnifier JNI",
        WS_POPUP,
        ((GetSystemMetrics(SM_CXSCREEN) - windowWidth) / 2) + offSetX,
        ((GetSystemMetrics(SM_CYSCREEN) - windowHeight) / 2) + offSetY,
        windowWidth, windowHeight,
        NULL, NULL, hInst, NULL
    );
    if (!hwndMagnifier)
    {
        isRecreatingWindow = false;
        return;
    }

    if (circularMode)
    {
        HRGN hRgn = CreateEllipticRgn(0, 0, windowWidth, windowHeight);
        SetWindowRgn(hwndMagnifier, hRgn, TRUE);
    }

    hwndMag = CreateWindowW(
        WC_MAGNIFIER,
        NULL,
        WS_CHILD | WS_VISIBLE,
        0, 0, windowWidth, windowHeight,
        hwndMagnifier, NULL, hInst, NULL
    );
    if (!hwndMag)
    {
        isRecreatingWindow = false;
        return;
    }

    UINT interval = (UINT)(1000 / currentRefreshRate);
    SetTimer(hwndMagnifier, 1, interval, NULL);

    ShowWindow(hwndMagnifier, SW_SHOW);
    UpdateWindow(hwndMagnifier);

    isRecreatingWindow = false;
}

LRESULT CALLBACK MagnifierWndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
    switch (uMsg)
    {
    case WM_TIMER:
        UpdateMagnifierContent();
        break;
    case WM_RECREATE_WINDOW_SYNC:
        RecreateWindow();
        break;
    case WM_DESTROY:
        if (!isRecreatingWindow)
            PostQuitMessage(0);
        break;
    default:
        return DefWindowProcW(hwnd, uMsg, wParam, lParam);
    }
    return 0;
}

DWORD WINAPI UIThreadProc(LPVOID lpParam)
{
    if (!MagInitialize())
        return 1;

    HINSTANCE hInst = GetModuleHandleW(NULL);
    WNDCLASSW wc = { 0 };
    wc.lpfnWndProc = MagnifierWndProc;
    wc.hInstance = hInst;
    wc.lpszClassName = L"MagnifierWindowClassJNI";
    if (!RegisterClassW(&wc))
    {
        MagUninitialize();
        return 1;
    }

    hwndMagnifier = CreateWindowExW(
        WS_EX_TOPMOST | WS_EX_LAYERED | WS_EX_TRANSPARENT,
        L"MagnifierWindowClassJNI",
        L"Magnifier JNI",
        WS_POPUP,
        ((GetSystemMetrics(SM_CXSCREEN) - windowWidth) / 2) + offSetX,
        ((GetSystemMetrics(SM_CYSCREEN) - windowHeight) / 2) + offSetY,
        windowWidth, windowHeight,
        NULL, NULL, hInst, NULL
    );
    if (!hwndMagnifier)
    {
        MagUninitialize();
        return 1;
    }

    if (circularMode)
    {
        HRGN hRgn = CreateEllipticRgn(0, 0, windowWidth, windowHeight);
        SetWindowRgn(hwndMagnifier, hRgn, TRUE);
    }

    hwndMag = CreateWindowW(
        WC_MAGNIFIER,
        NULL,
        WS_CHILD | WS_VISIBLE,
        0, 0, windowWidth, windowHeight,
        hwndMagnifier, NULL, hInst, NULL
    );
    if (!hwndMag)
    {
        DestroyWindow(hwndMagnifier);
        MagUninitialize();
        return 1;
    }

    UINT interval = (UINT)(1000 / currentRefreshRate);
    SetTimer(hwndMagnifier, 1, interval, NULL);

    ShowWindow(hwndMagnifier, SW_SHOW);
    UpdateWindow(hwndMagnifier);

    uiThreadId = GetCurrentThreadId();

    MSG msg;
    while (GetMessageW(&msg, NULL, 0, 0))
    {
        TranslateMessage(&msg);
        DispatchMessageW(&msg);
    }

    MagUninitialize();
    return 0;
}

JNIEXPORT jboolean JNICALL Java_scopez_MagnifierWrapper_nativeInit(JNIEnv* env, jobject obj)
{
    hUIThread = CreateThread(NULL, 0, UIThreadProc, NULL, 0, NULL);
    if (hUIThread == NULL)
        return JNI_FALSE;

    Sleep(200);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_scopez_MagnifierWrapper_nativeSetResolution(JNIEnv* env, jobject obj, jint width, jint height)
{
    windowWidth = (int)width;
    windowHeight = (int)height;
    if (hwndMagnifier && uiThreadId != 0)
        SendMessage(hwndMagnifier, WM_RECREATE_WINDOW_SYNC, 0, 0);
}

JNIEXPORT void JNICALL Java_scopez_MagnifierWrapper_nativeSetWindowShape(JNIEnv* env, jobject obj, jboolean circular)
{
    circularMode = (circular == JNI_TRUE);
    if (hwndMagnifier)
    {
        if (circularMode)
        {
            HRGN hRgn = CreateEllipticRgn(0, 0, windowWidth, windowHeight);
            SetWindowRgn(hwndMagnifier, hRgn, TRUE);
        }
        else
        {
            SetWindowRgn(hwndMagnifier, NULL, TRUE);
        }
    }
}

JNIEXPORT void JNICALL Java_scopez_MagnifierWrapper_nativeSetRefreshRate(JNIEnv* env, jobject obj, jint rate)
{
    currentRefreshRate = (int)rate;
    if (hwndMagnifier)
    {
        UINT interval = (UINT)(1000 / currentRefreshRate);
        KillTimer(hwndMagnifier, 1);
        SetTimer(hwndMagnifier, 1, interval, NULL);
    }
}

JNIEXPORT void JNICALL Java_scopez_MagnifierWrapper_nativeSetZoom(JNIEnv* env, jobject obj, jdouble zoom)
{
    currentZoom = (double)zoom;
    if (hwndMag)
    {
        MAGTRANSFORM matrix;
        float temp[9] = {
            (float)currentZoom, 0.0f, 0.0f,
            0.0f, (float)currentZoom, 0.0f,
            0.0f, 0.0f, 1.0f
        };
        memcpy(matrix.v, temp, sizeof(temp));
        MagSetWindowTransform(hwndMag, &matrix);
    }
}

JNIEXPORT void JNICALL Java_scopez_MagnifierWrapper_nativeShowWindow(JNIEnv* env, jobject obj)
{
    if (hwndMagnifier)
    {
        ShowWindow(hwndMagnifier, SW_SHOW);
        UpdateWindow(hwndMagnifier);
    }
}

JNIEXPORT void JNICALL Java_scopez_MagnifierWrapper_nativeHideWindow(JNIEnv* env, jobject obj)
{
    if (hwndMagnifier)
        ShowWindow(hwndMagnifier, SW_HIDE);
}

JNIEXPORT void JNICALL Java_scopez_MagnifierWrapper_nativeMoveWindow(JNIEnv* env, jobject obj, jint offsetX, jint offsetY)
{
    offSetX = (int)offsetX;
    offSetY = (int)offsetY;
    if (hwndMagnifier && uiThreadId != 0)
        SendMessage(hwndMagnifier, WM_RECREATE_WINDOW_SYNC, 0, 0);
}

JNIEXPORT void JNICALL Java_scopez_MagnifierWrapper_nativeDispose(JNIEnv* env, jobject obj)
{
    if (hwndMagnifier)
    {
        DestroyWindow(hwndMagnifier);
        hwndMagnifier = NULL;
        hwndMag = NULL;
    }
    if (hUIThread)
    {
        PostThreadMessage(uiThreadId, WM_QUIT, 0, 0);
        hUIThread = NULL;
    }
}
