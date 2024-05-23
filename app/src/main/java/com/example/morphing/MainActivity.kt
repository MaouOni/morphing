package com.example.morphing

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var pixelDissolveView: PixelDissolveView
    private var image1: Bitmap? = null
    private var image2: Bitmap? = null

    private val selectImage1Launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                val inputStream = contentResolver.openInputStream(it)
                image1 = BitmapFactory.decodeStream(inputStream)
                cropAndSetImages()
            }
        }
    }

    private val selectImage2Launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                val inputStream = contentResolver.openInputStream(it)
                image2 = BitmapFactory.decodeStream(inputStream)
                cropAndSetImages()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pixelDissolveView = findViewById(R.id.pixelDissolveView)

        findViewById<Button>(R.id.selectImage1).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectImage1Launcher.launch(intent)
        }

        findViewById<Button>(R.id.selectImage2).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selectImage2Launcher.launch(intent)
        }

        findViewById<Button>(R.id.startTransition).setOnClickListener {
            startPixelDissolveTransition()
        }
    }

    private fun cropAndSetImages() {
        image1?.let { img1 ->
            image2?.let { img2 ->
                val croppedImg1 = cropToSquare(img1)
                val croppedImg2 = cropToSquare(img2)
                pixelDissolveView.setImages(croppedImg1, croppedImg2)
            }
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val dimension = Math.min(bitmap.width, bitmap.height)
        return Bitmap.createBitmap(bitmap, 0, 0, dimension, dimension)
    }

    private fun startPixelDissolveTransition() {
        thread {
            var progress = 0f
            val duration = 2000L // 2 seconds
            val frameRate = 16L // Approx 60 FPS
            val increment = frameRate.toFloat() / duration

            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    if (progress <= 1f) {
                        pixelDissolveView.setProgress(progress)
                        progress += increment
                        handler.postDelayed(this, frameRate)
                    }
                }
            }
            handler.post(runnable)
        }
    }
}
