package com.hdi.pda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var statusText: TextView
    private lateinit var cameraExecutor: ExecutorService
    
    private var isScanning = true
    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        viewFinder = findViewById(R.id.viewFinder)
        statusText = findViewById(R.id.statusText)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 시작
        startCamera()

        // 닫기 버튼
        findViewById<View>(R.id.closeButton).setOnClickListener {
            finish()
        }

        // 플래시 토글 버튼 (선택사항)
        findViewById<View>(R.id.flashButton).setOnClickListener {
            toggleFlash()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.surfaceProvider)
                    }

                // ImageAnalysis
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
                    }

                // 후면 카메라 선택
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // 기존 바인딩 해제
                cameraProvider.unbindAll()

                // 카메라 바인딩
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

                statusText.text = "바코드를 화면 중앙에 위치시키세요"

            } catch (exc: Exception) {
                Log.e(TAG, "카메라 시작 실패", exc)
                Toast.makeText(
                    this,
                    "카메라를 시작할 수 없습니다: ${exc.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 바코드 분석기
     */
    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient()

        @androidx.annotation.OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            // 스캔 중지 상태면 이미지 닫고 리턴
            if (!isScanning) {
                imageProxy.close()
                return
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            processBarcodeResult(barcode)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "바코드 스캔 실패", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    /**
     * 바코드 결과 처리
     */
    private fun processBarcodeResult(barcode: Barcode) {
        val rawValue = barcode.rawValue ?: return

        // GUID 형식인지 확인
        if (isValidGuid(rawValue)) {
            onBarcodeDetected(rawValue)
        }
    }

    /**
     * 유효한 GUID인지 확인
     */
    private fun isValidGuid(value: String): Boolean {
        // 중괄호 제거
        val cleanValue = value.trim().removeSurrounding("{", "}")
        
        // GUID 패턴: 8-4-4-4-12
        val guidPattern = Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        )
        
        return guidPattern.matches(cleanValue)
    }

    /**
     * 바코드 감지 성공
     */
    private fun onBarcodeDetected(barcodeValue: String) {
        if (!isScanning) return

        // 중복 스캔 방지
        isScanning = false

        runOnUiThread {
            // 진동 피드백
            vibrate()

            // 상태 텍스트 업데이트
            statusText.text = "✓ 스캔 완료!"

            // 중괄호 제거
            val cleanValue = barcodeValue.trim().removeSurrounding("{", "}")

            // 결과 전달
            val resultIntent = Intent().apply {
                putExtra("SCAN_RESULT", cleanValue)
            }
            setResult(RESULT_OK, resultIntent)

            // Activity 종료
            finish()
        }
    }

    /**
     * 진동 피드백
     */
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                it.vibrate(
                    VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }
    }

    /**
     * 플래시 토글
     */
    private fun toggleFlash() {
        camera?.let {
            val currentFlashMode = it.cameraInfo.torchState.value
            val newFlashMode = currentFlashMode != TorchState.ON
            it.cameraControl.enableTorch(newFlashMode)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "ScannerActivity"
    }
}
