package com.harukeyua.wavedistortiondemo

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

actual fun Modifier.runtimeShader(
    shader: String,
    shaderUniformName: String,
    uniforms: (ShaderUniformProvider.(size: Size) -> Unit)?,
): Modifier = graphicsLayer {
    val runtimeShaderBuilder = RuntimeShaderBuilder(effect = RuntimeEffect.makeForShader(shader))
    val shaderUniformProvider = ShaderUniformProviderImpl(runtimeShaderBuilder)
    val contentSize = Size(width = size.width.toFloat(), height = size.height.toFloat())
    renderEffect =
        ImageFilter.makeRuntimeShader(
                runtimeShaderBuilder =
                    runtimeShaderBuilder.apply {
                        uniforms?.invoke(shaderUniformProvider, contentSize)
                    },
                shaderName = shaderUniformName,
                input = null,
            )
            .asComposeRenderEffect()
}

private class ShaderUniformProviderImpl(private val runtimeShaderBuilder: RuntimeShaderBuilder) :
    ShaderUniformProvider {

    override fun uniform(name: String, value: Int) {
        runtimeShaderBuilder.uniform(name, value)
    }

    override fun uniform(name: String, value: Float) {
        runtimeShaderBuilder.uniform(name, value)
    }

    override fun uniform(name: String, value1: Float, value2: Float) {
        runtimeShaderBuilder.uniform(name, value1, value2)
    }
}
