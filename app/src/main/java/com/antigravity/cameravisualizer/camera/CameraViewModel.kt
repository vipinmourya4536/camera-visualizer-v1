package com.antigravity.cameravisualizer.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cameravisualizer.capture.CaptureState
import com.antigravity.cameravisualizer.processing.ProcessingState
import com.antigravity.cameravisualizer.storage.MediaStoreRepository
import com.antigravity.cameravisualizer.styles.StyleLibrary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _processingState = MutableStateFlow(ProcessingState())
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private val _captureState = MutableStateFlow(CaptureState())
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    val cameraEngine = CameraEngine(application)
    private val mediaStoreRepository = MediaStoreRepository(application)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        viewModelScope.launch {
            try {
                cameraEngine.initialize()
                _cameraState.update { it.copy(isInitialized = true) }
            } catch (e: Exception) {
                // Handle initialization error
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _cameraState.update {
            it.copy(permissionState = if (granted) PermissionState.GRANTED else PermissionState.DENIED)
        }
    }

    fun toggleCamera() {
        _cameraState.update {
            val newFacing = if (it.cameraFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            it.copy(cameraFacing = newFacing)
        }
    }

    fun setZoom(ratio: Float) {
        _cameraState.update { it.copy(activeLens = ratio) }
        cameraEngine.setZoomRatio(ratio)
    }

    fun setPreset(presetId: String) {
        _processingState.update { it.copy(presetId = presetId) }
    }

    fun setIntensity(intensity: Float) {
        _processingState.update { it.copy(intensity = intensity) }
    }

    fun setToneHue(x: Float, y: Float) {
        _processingState.update { it.copy(padX = x, padY = y) }
    }

    fun capturePhoto() {
        if (_captureState.value.isCapturing) return

        _captureState.update { it.copy(isCapturing = true) }

        viewModelScope.launch {
            val imageProxy = cameraEngine.takePicture(cameraExecutor)
            if (imageProxy != null) {
                processAndSaveImage(imageProxy)
            } else {
                _captureState.update { it.copy(isCapturing = false) }
            }
        }
    }

    private fun processAndSaveImage(imageProxy: ImageProxy) {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        
        // Handle rotation
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            if (_cameraState.value.cameraFacing == CameraSelector.LENS_FACING_FRONT) {
                matrix.postScale(-1f, 1f) // Mirror front camera
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else if (_cameraState.value.cameraFacing == CameraSelector.LENS_FACING_FRONT) {
            val matrix = Matrix()
            matrix.postScale(-1f, 1f) // Mirror front camera
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        imageProxy.close()

        // Apply lightweight "style" - in a real app this would use GPU shaders
        // For the prototype, we just save the image with the style name.
        val currentStyle = StyleLibrary.styles.find { it.id == _processingState.value.presetId }?.name ?: "STANDARD"
        
        val uri = mediaStoreRepository.savePhoto(bitmap, currentStyle)
        
        _captureState.update { 
            it.copy(
                isCapturing = false,
                lastPhotoUri = uri,
                reviewOpen = true
            )
        }
    }

    fun closeReview() {
        _captureState.update { it.copy(reviewOpen = false) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}
