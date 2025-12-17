package com.harukeyua.wavedistortiondemo

val waveDistortionShader =
    """
    uniform shader uContent;
    uniform float2 uOrigin;
    uniform float uElapsedTime;
    uniform float uAmplitude;
    uniform float uAngularFrequency;
    uniform float uDecayRate;
    uniform float uWaveSpeed;
    uniform float uMaxRadius;
    uniform float uTintRatio;

    float2 lerp(float2 a, float2 b, float t) {
        return a + t * (b - a);
    }

    vec2 cubicBezierPoint(vec2 p0, vec2 p1, vec2 p2, vec2 p3, float t) {
        float u = 1.0 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;
        vec2 p = uuu * p0;
        p += 3.0 * uu * t * p1;
        p += 3.0 * u * tt * p2;
        p += ttt * p3;
        return p;
    }

    float bezierParamForX(float x, vec2 p0, vec2 p1, vec2 p2, vec2 p3) {
        float t = x; // Initial guess
        for (int i = 0; i < 5; i++) {
            vec2 bezierPoint = cubicBezierPoint(p0, p1, p2, p3, t);
            float x_t = bezierPoint.x;
            float dx_dt = -3.0 * (1.0 - t) * (1.0 - t) * p0.x + 3.0 * (1.0 - 2.0 * t) * (1.0 - t) * p1.x + 3.0 * t * (2.0 - 3.0 * t) * p2.x + 3.0 * t * t * p3.x;
            t -= (x_t - x) / dx_dt;
            t = clamp(t, 0.0, 1.0);
        }
        return t;
    }

    float bezierYForX(float x, vec2 p1, vec2 p2) {
        vec2 p0 = vec2(0.0, 0.0);
        vec2 p3 = vec2(1.0, 1.0);
        float t = bezierParamForX(x, p0, p1, p2, p3);
        vec2 bezierPoint = cubicBezierPoint(p0, p1, p2, p3, t);
        return bezierPoint.y;
    }

    float easeEmphasizedAccelerate(float x) {
        float2 p1 = float2(0.05, 0.7);
        float2 p2 = float2(0.1, 1.0);
        return bezierYForX(x, p1, p2);
    }

    half4 main(float2 fragCoord) {
        float distanceToOrigin = length(fragCoord - uOrigin);

        float localStartDelay = distanceToOrigin / uWaveSpeed;

        float localTime = max(0.0, uElapsedTime - localStartDelay);

        float rippleAmount = uAmplitude * sin(uAngularFrequency * localTime) * exp(-uDecayRate * localTime);

        float2 outwardNormal = normalize(fragCoord - uOrigin);

        float2 tangent = float2(-outwardNormal.y, outwardNormal.x);
        float swirlMix = 0.85 * sin(uAngularFrequency * localTime + distanceToOrigin * 0.04);
        float2 swirlDir = normalize(outwardNormal + swirlMix * tangent);

        float2 displacedCoord = fragCoord + rippleAmount * swirlDir;

        displacedCoord = lerp(fragCoord, displacedCoord, easeEmphasizedAccelerate(distanceToOrigin / uMaxRadius));

        half4 color = uContent.eval(displacedCoord);

        float ampSafe = max(abs(uAmplitude), 1e-5);
        float aberration = uTintRatio * abs(rippleAmount) / ampSafe;
        float2 abOffset = swirlDir * aberration;

        half4 base = color;
        half r = uContent.eval(displacedCoord + abOffset).r;
        half g = base.g;
        half b = uContent.eval(displacedCoord - abOffset).b;
        color = half4(r, g, b, base.a);

        return color;
    }
    """
        .trimIndent()
