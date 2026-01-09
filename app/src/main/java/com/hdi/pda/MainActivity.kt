package com.hdi.pda

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val startUrl = "http://erp.hdi21.co.kr/mobile/"

    // ActivityResult API - 스캐너 결과 수신
    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("SCAN_RESULT")?.let { barcode ->
                injectScanResult(barcode)
            }
        }
    }

    // 카메라 권한 요청
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startScanner()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        setupWebView()

        // WebView 상태 복원 또는 초기 로드
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(startUrl)
        }
    }

    private fun setupWebView() {
        webView.apply {
            // WebView 설정
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                
                // 성능 최적화
                setRenderPriority(WebSettings.RenderPriority.HIGH)
                
                // Zoom 설정
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                
                // 파일 접근
                allowFileAccess = true
                allowContentAccess = true
                
                // 기타 설정
                javaScriptCanOpenWindowsAutomatically = true
                loadsImagesAutomatically = true
                blockNetworkImage = false
            }

            // JavaScript Interface 등록
            addJavascriptInterface(WebAppInterface(this@MainActivity), "Native")

            // WebViewClient 설정
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "페이지 로드 오류: $description",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    url: String?
                ): Boolean {
                    // 동일 도메인은 WebView에서 처리
                    if (url != null && url.contains("erp.hdi21.co.kr")) {
                        return false
                    }
                    return false
                }
            }

            // WebChromeClient 설정 (파일 업로드, alert 등)
            webChromeClient = WebChromeClient()
        }
    }

    /**
     * 스캔 결과를 WebView에 주입
     * scan_bar 텍스트박스에 값을 넣고 필요한 이벤트 발생
     */
    private fun injectScanResult(barcode: String) {
        // JavaScript로 문자열 전달 시 escape 처리
        val escaped = barcode
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\"", "\\\"")

        val javascript = """
            (function() {
                try {
                    var input = document.getElementById('scan_bar');
                    if (input) {
                        // 값 설정
                        input.value = '$escaped';
                        
                        // input 이벤트 발생
                        var inputEvent = new Event('input', { bubbles: true });
                        input.dispatchEvent(inputEvent);
                        
                        // change 이벤트 발생
                        var changeEvent = new Event('change', { bubbles: true });
                        input.dispatchEvent(changeEvent);
                        
                        // keyup 이벤트 발생 (기존 로직이 keyup으로 처리)
                        var keyupEvent = new KeyboardEvent('keyup', {
                            key: 'Enter',
                            code: 'Enter',
                            keyCode: 13,
                            which: 13,
                            bubbles: true,
                            cancelable: true
                        });
                        input.dispatchEvent(keyupEvent);
                        
                        // 포커스
                        input.focus();
                        
                        console.log('Barcode injected successfully: $escaped');
                    } else {
                        console.error('scan_bar element not found');
                    }
                } catch (e) {
                    console.error('Error injecting barcode:', e);
                }
            })();
        """.trimIndent()

        runOnUiThread {
            webView.evaluateJavascript(javascript) { result ->
                // 콜백 결과 확인 (선택사항)
            }
        }
    }

    /**
     * 스캐너 시작 (WebAppInterface에서 호출)
     */
    fun startScanner() {
        // 카메라 권한 확인
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 있으면 스캐너 시작
                val intent = Intent(this, ScannerActivity::class.java)
                scannerLauncher.launch(intent)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // 권한 설명 표시
                AlertDialog.Builder(this)
                    .setTitle("카메라 권한 필요")
                    .setMessage("바코드를 스캔하려면 카메라 권한이 필요합니다.")
                    .setPositiveButton("확인") { _, _ ->
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
            else -> {
                // 권한 요청
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한 거부됨")
            .setMessage("바코드 스캔을 사용하려면 설정에서 카메라 권한을 허용해주세요.")
            .setPositiveButton("확인", null)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // WebView 상태 저장 (중요!)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // WebView 상태 복원은 onCreate에서 처리
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // WebView 정리
        webView.destroy()
    }
}
