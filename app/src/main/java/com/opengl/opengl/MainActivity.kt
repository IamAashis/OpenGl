package com.opengl.opengl

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    var glView: FrameLayout? = null
    private var renderer: GLRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glView = findViewById(R.id.fltOpenGl);
        setupGL()
    }

    private fun setupGL() {
        renderer = GLRenderer()
        val glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(renderer)
        glView?.addView(glSurfaceView)
        produceFrame()
    }

    private fun produceFrame() {
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.sample)
        renderer?.setBitmap(bmp)
    }
}