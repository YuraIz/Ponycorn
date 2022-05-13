package com.yuraiz.ponycorn

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.yuraiz.ponycorn.databinding.ActivityMainBinding
import com.yuraiz.ponycorn.fractal.VariableFractal
import com.yuraiz.ponycorn.render.MultiFractalRenderer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonNext.setOnClickListener { binding.myGLSurfaceView.next() }
        binding.buttonPrev.setOnClickListener { binding.myGLSurfaceView.prev() }

        binding.fractalControls.visibility = View.INVISIBLE

        MultiFractalRenderer.slider_visibiliry = {
            binding.fractalControls.visibility = it
        }

        binding.fractalControls.addOnChangeListener { _, value, _ ->
            VariableFractal.value = value
        }
    }
}