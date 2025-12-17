package com.harukeyua.wavedistortiondemo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import wavedistortiondemo.composeapp.generated.resources.Res
import wavedistortiondemo.composeapp.generated.resources.amplitude
import wavedistortiondemo.composeapp.generated.resources.amplitude_tooltip
import wavedistortiondemo.composeapp.generated.resources.angular_frequency
import wavedistortiondemo.composeapp.generated.resources.angular_frequency_tooltip
import wavedistortiondemo.composeapp.generated.resources.app_name
import wavedistortiondemo.composeapp.generated.resources.auto_waves
import wavedistortiondemo.composeapp.generated.resources.auto_waves_desc
import wavedistortiondemo.composeapp.generated.resources.decay_rate
import wavedistortiondemo.composeapp.generated.resources.decay_rate_tooltip
import wavedistortiondemo.composeapp.generated.resources.shader_controls
import wavedistortiondemo.composeapp.generated.resources.tint_ratio
import wavedistortiondemo.composeapp.generated.resources.tint_ratio_tooltip
import wavedistortiondemo.composeapp.generated.resources.wave_speed
import wavedistortiondemo.composeapp.generated.resources.wave_speed_tooltip
import wavedistortiondemo.composeapp.generated.resources.wave_speed_unit

private const val AmplitudeDefault = 24f
private const val AngularFrequencyDefault = 10f
private const val DecayRateDefault = 5f
private const val TintRatioDefault = 50f
private const val WaveSpeedMultiplierDefault = 0.6f

private const val MsPerTimeUnit = 500
private const val OvershootMultiplier = 1.3f
private const val VisibleThreshold = 0.01f
private val DecayTimeFactor = -ln(VisibleThreshold)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    AppTheme {
        val adaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)
        val isWide =
            adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
                WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
            )

        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current

        var rippleOrigin by remember { mutableStateOf(Offset.Zero) }
        val rippleTime = remember { Animatable(0f) }

        var amplitudeDp by remember { mutableStateOf(AmplitudeDefault) }
        var angularFrequency by remember { mutableStateOf(AngularFrequencyDefault) }
        var decayRate by remember { mutableStateOf(DecayRateDefault) }
        var tintRatio by remember { mutableStateOf(TintRatioDefault) }
        var waveSpeedMultiplier by remember { mutableStateOf(WaveSpeedMultiplierDefault) }
        var autoRippleEnabled by remember { mutableStateOf(false) }
        var sampleSize by remember { mutableStateOf(IntSize.Zero) }

        val triggerRipple: (Offset) -> Unit = { offset ->
            rippleOrigin = offset
            val targetTime = computeTargetTime(sampleSize, offset, waveSpeedMultiplier, decayRate)
            val durationMs = (targetTime * MsPerTimeUnit).toInt().coerceAtLeast(1)

            coroutineScope.launch {
                rippleTime.snapTo(0f)
                rippleTime.animateTo(
                    targetTime,
                    tween(durationMillis = durationMs, easing = LinearEasing),
                )
            }
        }

        LaunchedEffect(autoRippleEnabled, sampleSize, waveSpeedMultiplier, decayRate) {
            while (isActive && autoRippleEnabled && sampleSize != IntSize.Zero) {
                val x = Random.nextFloat() * sampleSize.width
                val y = Random.nextFloat() * sampleSize.height
                val origin = Offset(x, y)
                val targetTime =
                    computeTargetTime(sampleSize, origin, waveSpeedMultiplier, decayRate)
                val delayMs = (targetTime * MsPerTimeUnit).toLong()
                triggerRipple(origin)
                delay(delayMs)
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.app_name)) })
            }
        ) { paddingValues ->
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    WaveDistortionSample(
                        modifier = Modifier.weight(1f),
                        rippleOrigin = rippleOrigin,
                        rippleTime = rippleTime.value,
                        amplitudePx = with(density) { amplitudeDp.dp.toPx() },
                        angularFrequency = angularFrequency,
                        decayRate = decayRate,
                        tintRatio = tintRatio,
                        waveSpeedMultiplier = waveSpeedMultiplier,
                        onTap = triggerRipple,
                        onSizeChanged = { sampleSize = it },
                    )

                    ControlPanel(
                        modifier = Modifier.widthIn(max = 380.dp).fillMaxHeight(),
                        amplitudeDp = amplitudeDp,
                        onAmplitudeChange = { amplitudeDp = it },
                        angularFrequency = angularFrequency,
                        onAngularFrequencyChange = { angularFrequency = it },
                        decayRate = decayRate,
                        onDecayRateChange = { decayRate = it },
                        waveSpeedMultiplier = waveSpeedMultiplier,
                        onWaveSpeedMultiplierChange = { waveSpeedMultiplier = it },
                        tintRatio = tintRatio,
                        onTintRatioChange = { tintRatio = it },
                        autoRippleEnabled = autoRippleEnabled,
                        onAutoRippleEnabledChange = { autoRippleEnabled = it },
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    WaveDistortionSample(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        rippleOrigin = rippleOrigin,
                        rippleTime = rippleTime.value,
                        amplitudePx = with(density) { amplitudeDp.dp.toPx() },
                        angularFrequency = angularFrequency,
                        decayRate = decayRate,
                        tintRatio = tintRatio,
                        waveSpeedMultiplier = waveSpeedMultiplier,
                        onTap = triggerRipple,
                        onSizeChanged = { sampleSize = it },
                    )

                    ControlPanel(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        amplitudeDp = amplitudeDp,
                        onAmplitudeChange = { amplitudeDp = it },
                        angularFrequency = angularFrequency,
                        onAngularFrequencyChange = { angularFrequency = it },
                        decayRate = decayRate,
                        onDecayRateChange = { decayRate = it },
                        waveSpeedMultiplier = waveSpeedMultiplier,
                        onWaveSpeedMultiplierChange = { waveSpeedMultiplier = it },
                        tintRatio = tintRatio,
                        onTintRatioChange = { tintRatio = it },
                        autoRippleEnabled = autoRippleEnabled,
                        onAutoRippleEnabledChange = { autoRippleEnabled = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun WaveDistortionSample(
    rippleOrigin: Offset,
    rippleTime: Float,
    amplitudePx: Float,
    angularFrequency: Float,
    decayRate: Float,
    tintRatio: Float,
    waveSpeedMultiplier: Float,
    onTap: (Offset) -> Unit,
    onSizeChanged: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        AsyncImage(
            modifier =
                Modifier.pointerInput(Unit) { detectTapGestures { offset -> onTap(offset) } }
                    .runtimeShader(
                        shader = waveDistortionShader,
                        uniforms = { size ->
                            val radius = hypot(size.width, size.height) / 2f

                            uniform("uAmplitude", amplitudePx)
                            uniform("uAngularFrequency", angularFrequency)
                            uniform("uDecayRate", decayRate)
                            uniform("uTintRatio", tintRatio)

                            uniform("uOrigin", rippleOrigin.x, rippleOrigin.y)
                            uniform("uMaxRadius", radius)
                            uniform("uWaveSpeed", (radius * waveSpeedMultiplier).coerceAtLeast(1f))
                            uniform("uElapsedTime", rippleTime)
                        },
                    )
                    .onSizeChanged { onSizeChanged(it) }
                    .fillMaxSize(),
            model = "https://picsum.photos/id/28/2000",
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
    }
}

@Composable
private fun ControlPanel(
    amplitudeDp: Float,
    angularFrequency: Float,
    decayRate: Float,
    waveSpeedMultiplier: Float,
    tintRatio: Float,
    autoRippleEnabled: Boolean,
    onAmplitudeChange: (Float) -> Unit,
    onAngularFrequencyChange: (Float) -> Unit,
    onDecayRateChange: (Float) -> Unit,
    onWaveSpeedMultiplierChange: (Float) -> Unit,
    onTintRatioChange: (Float) -> Unit,
    onAutoRippleEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.shader_controls),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Checkbox(checked = autoRippleEnabled, onCheckedChange = onAutoRippleEnabledChange)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.auto_waves),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Text(
                        text = stringResource(Res.string.auto_waves_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SliderWithLabel(
                title = stringResource(Res.string.amplitude),
                value = amplitudeDp,
                range = 5f..64f,
                onValueChange = onAmplitudeChange,
                unit = "dp",
                tooltip = stringResource(Res.string.amplitude_tooltip),
            )

            SliderWithLabel(
                title = stringResource(Res.string.angular_frequency),
                value = angularFrequency,
                range = 0.5f..30f,
                onValueChange = onAngularFrequencyChange,
                tooltip = stringResource(Res.string.angular_frequency_tooltip),
            )

            SliderWithLabel(
                title = stringResource(Res.string.decay_rate),
                value = decayRate,
                range = 1f..10f,
                onValueChange = onDecayRateChange,
                tooltip = stringResource(Res.string.decay_rate_tooltip),
            )

            SliderWithLabel(
                title = stringResource(Res.string.wave_speed),
                value = waveSpeedMultiplier,
                range = 0.2f..1.5f,
                onValueChange = onWaveSpeedMultiplierChange,
                unit = stringResource(Res.string.wave_speed_unit),
                tooltip = stringResource(Res.string.wave_speed_tooltip),
            )

            SliderWithLabel(
                title = stringResource(Res.string.tint_ratio),
                value = tintRatio,
                range = 0f..150f,
                onValueChange = onTintRatioChange,
                tooltip = stringResource(Res.string.tint_ratio_tooltip),
            )
        }
    }
}

@Composable
private fun SliderWithLabel(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    unit: String? = null,
    tooltip: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )

            if (tooltip != null) {
                InfoTooltip(text = tooltip, modifier = Modifier.padding(end = 8.dp))
            }

            val formatedValue = remember(value) { value.format() }

            Text(
                text =
                    buildString {
                        append(formatedValue)
                        if (unit != null) {
                            append(" ")
                            append(unit)
                        }
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoTooltip(text: String, modifier: Modifier = Modifier) {
    val tooltipState = rememberTooltipState()
    val coroutineScope = rememberCoroutineScope()

    TooltipBox(
        state = tooltipState,
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(text = text) } },
    ) {
        Box(
            modifier =
                modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { coroutineScope.launch { tooltipState.show() } }
                    .wrapContentSize(Alignment.Center)
        ) {
            Text(
                text = "i",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun computeTargetTime(
    size: IntSize,
    origin: Offset,
    waveSpeedMultiplier: Float,
    decayRate: Float,
): Float {
    if (size == IntSize.Zero) return 1f

    val w = size.width.toFloat()
    val h = size.height.toFloat()

    val maxDx = max(origin.x, w - origin.x)
    val maxDy = max(origin.y, h - origin.y)
    val maxDistance = hypot(maxDx, maxDy)

    val halfDiagonal = hypot(w, h) / 2f

    val speed = (halfDiagonal * waveSpeedMultiplier).coerceAtLeast(1f)

    val travelTime = (maxDistance * OvershootMultiplier) / speed

    val decayTime = if (decayRate > 0) DecayTimeFactor / decayRate else 0f

    return travelTime + decayTime
}
