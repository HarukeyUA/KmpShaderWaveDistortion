package com.harukeyua.wavedistortiondemo

import java.text.DecimalFormat

actual fun Float.format(): String =
    DecimalFormat.getInstance()
        .apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
        .format(this)
