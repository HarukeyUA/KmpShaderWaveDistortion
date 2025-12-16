package com.harukeyua.wavedistortiondemo

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

actual fun Modifier.runtimeShader(
    shader: String,
    shaderUniformName: String,
    uniforms: (ShaderUniformProvider.(size: Size) -> Unit)?,
): Modifier = graphicsLayer {
    val runtimeShader = RuntimeShader(shader)
    val shaderUniformProvider = ShaderUniformProviderImpl(runtimeShader)
    val contentSize = Size(width = size.width.toFloat(), height = size.height.toFloat())
    renderEffect =
        RenderEffect.createRuntimeShaderEffect(
                runtimeShader.apply { uniforms?.invoke(shaderUniformProvider, contentSize) },
                shaderUniformName,
            )
            .asComposeRenderEffect()
}

private class ShaderUniformProviderImpl(private val runtimeShader: RuntimeShader) :
    ShaderUniformProvider {

    override fun uniform(name: String, value: Int) {
        runtimeShader.setIntUniform(name, value)
    }

    override fun uniform(name: String, value: Float) {
        runtimeShader.setFloatUniform(name, value)
    }

    override fun uniform(name: String, value1: Float, value2: Float) {
        runtimeShader.setFloatUniform(name, value1, value2)
    }
}
