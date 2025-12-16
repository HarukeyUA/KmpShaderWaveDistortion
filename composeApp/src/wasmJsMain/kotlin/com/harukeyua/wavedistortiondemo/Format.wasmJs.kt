package com.harukeyua.wavedistortiondemo

@JsFun(
    "value => new Intl.NumberFormat(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 }).format(value)"
)
private external fun formatNumberJs(value: Double): String

actual fun Float.format(): String = formatNumberJs(this.toDouble())
