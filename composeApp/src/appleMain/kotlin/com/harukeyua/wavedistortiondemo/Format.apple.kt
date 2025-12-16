package com.harukeyua.wavedistortiondemo

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun Float.format(): String {
    val formatter =
        NSNumberFormatter().apply {
            this.minimumFractionDigits = 0.toULong()
            this.maximumFractionDigits = 2.toULong()
            this.numberStyle = NSNumberFormatterDecimalStyle
        }

    return formatter.stringFromNumber(NSNumber(this)).orEmpty()
}
