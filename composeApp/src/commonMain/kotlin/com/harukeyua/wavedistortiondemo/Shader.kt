package com.harukeyua.wavedistortiondemo

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size

expect fun Modifier.runtimeShader(
    shader: String,
    shaderUniformName: String = "uContent",
    uniforms: (ShaderUniformProvider.(size: Size) -> Unit)? = null,
): Modifier

interface ShaderUniformProvider {
    fun uniform(name: String, value: Int)

    fun uniform(name: String, value: Float)

    fun uniform(name: String, value1: Float, value2: Float)
}
