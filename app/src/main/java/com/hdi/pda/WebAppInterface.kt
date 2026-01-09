package com.hdi.pda

import android.webkit.JavascriptInterface

/**
 * JavaScript Bridge Interface
 * 웹페이지에서 window.Native.openScanner() 형태로 호출
 */
class WebAppInterface(private val activity: MainActivity) {

    /**
     * 바코드 스캐너 열기
     * 웹페이지에서 호출: window.Native.openScanner()
     */
    @JavascriptInterface
    fun openScanner() {
        activity.runOnUiThread {
            activity.startScanner()
        }
    }

    /**
     * 앱 정보 반환 (선택사항)
     */
    @JavascriptInterface
    fun getAppVersion(): String {
        return "1.0.0"
    }

    /**
     * 네이티브 앱 확인 (선택사항)
     */
    @JavascriptInterface
    fun isNativeApp(): Boolean {
        return true
    }
}
