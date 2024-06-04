package com.oladipo.facedetection

import android.app.Application
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ExecutionException

class CameraXViewModel(application: Application) : AndroidViewModel(application) {
    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null

    // Handle any errors (including cancellation) here.
    val processCameraProvider: LiveData<ProcessCameraProvider> get() {
        if (cameraProviderLiveData == null) {
            cameraProviderLiveData = MutableLiveData()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
            cameraProviderFuture.addListener(
                {
                    try {
                        cameraProviderLiveData!!.setValue(cameraProviderFuture.get())
                    } catch (e: ExecutionException) {
                        // Handle any errors (including cancellation) here.
                        Log.e(TAG, "Unhandled exception", e)
                    } catch (e: InterruptedException) {
                        Log.e(TAG, "Unhandled exception", e)
                    }
                },
                ContextCompat.getMainExecutor(getApplication())
            )
        }
        return cameraProviderLiveData!!
    }

    companion object {
        private const val TAG = "CameraXViewModel"
    }
}


enum class Gesture(val index: Int) {
//    SMILE(0),
    TURN_LEFT(0), TURN_RIGHT(1), HEAD_UP(2), HEAD_DOWN(3);

    companion object {
        fun getChosenGestures(): List<Gesture> {
            return values().let {
                it.shuffle()
                it.take(3)
            }
        }
    }
}

enum class GestureStatus {
    DONE, NOT_DONE
}