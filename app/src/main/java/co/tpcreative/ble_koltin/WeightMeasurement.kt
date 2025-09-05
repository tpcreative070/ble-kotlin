package co.tpcreative.ble_koltin

import co.tpcreative.ble.kotlin.core.BluetoothBytesParser
import co.tpcreative.ble_koltin.ObservationUnit.Kilograms
import co.tpcreative.ble_koltin.ObservationUnit.Pounds
import java.nio.ByteOrder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.round

data class WeightMeasurement(
    val weight: Double,
    val unit: ObservationUnit,
    val timestamp: Date?,
    val userID: UInt?,
    val bmi: Double?,
    val heightInMetersOrInches: Double?,
    val createdAt: Date = Calendar.getInstance().time
) {
    override fun toString(): String {
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH)
        return "${"%.1f".format(weight)} ${unit.notation} \nat ${dateFormat.format(timestamp ?: createdAt)} "
    }

    companion object {
        fun fromBytes(value: ByteArray): WeightMeasurement? {
            val parser = BluetoothBytesParser(value, 0, ByteOrder.LITTLE_ENDIAN)

            try {
                val flags = parser.getUInt8()
                val unit = if (flags and 0x01u > 0u) Pounds else Kilograms
                val timestampPresent = flags and 0x02u > 0u
                val userIDPresent = flags and 0x04u > 0u
                val bmiAndHeightPresent = flags and 0x08u > 0u

                val weightMultiplier = if (unit == Kilograms) 0.005f else 0.01f
                val weight = parser.getUInt16().toDouble() * weightMultiplier
                val timestamp = if (timestampPresent) parser.getDateTime() else null
                val userID = if (userIDPresent) parser.getUInt8() else null
                val bmi = if (bmiAndHeightPresent) parser.getUInt16().toDouble() * 0.1f else null
                val heightMultiplier = if (unit == Kilograms) 0.001f else 0.1f
                val height = if (bmiAndHeightPresent) parser.getUInt16().toDouble() * heightMultiplier else null

                return WeightMeasurement(
                    weight = round(weight * 100) / 100,
                    unit = unit,
                    timestamp = timestamp,
                    userID = userID,
                    bmi = bmi,
                    heightInMetersOrInches = height
                )
            } catch (_: Exception) {
                return null
            }
        }
    }
}