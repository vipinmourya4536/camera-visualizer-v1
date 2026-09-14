package com.antigravity.cameravisualizer.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.camera.core.ImageProxy

class CameraEngine(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    var imageCapture: ImageCapture? = null
    var preview: Preview? = null
    var camera: Camera? = null

    suspend fun initialize(): ProcessCameraProvider = suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                continuation.resume(cameraProvider!!)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        lensFacing: Int
    ) {
        val provider = cameraProvider ?: return

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            Log.e("CameraEngine", "Use case binding failed", exc)
        }
    }

    suspend fun takePicture(executor: Executor): ImageProxy? = suspendCancellableCoroutine { continuation ->
        val capture = imageCapture ?: run {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                continuation.resume(image)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraEngine", "Photo capture failed: ${exception.message}", exception)
                continuation.resume(null)
            }
        })
    }

    fun setZoomRatio(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }
}
