package co.tpcreative.ble_koltin

import co.tpcreative.ble.kotlin.core.BluetoothBytesParser
import java.nio.ByteOrder
import java.util.Calendar
import java.util.Date

data class HeartRateMeasurement(
    val pulse: UShort,
    val energyExpended: UShort?,
    val rrIntervals: IntArray,
    val sensorContactStatus: SensorContactFeature,
    val createdAt: Date = Calendar.getInstance().time
) {
    override fun toString(): String {
        return "$pulse bpm"
    }

    companion object {
        fun fromBytes(value: ByteArray): HeartRateMeasurement? {
            val parser = BluetoothBytesParser(value, 0, ByteOrder.LITTLE_ENDIAN)

            try {
                val flags = parser.getUInt8()
                val pulse = if (flags and 0x01u == 0u) parser.getUInt8().toUShort() else parser.getUInt16()
                val sensorContactStatusFlag = flags and 0x06u shr 1
                val energyExpenditurePresent = flags and 0x08u > 0u
                val rrIntervalPresent = flags and 0x10u > 0u

                val sensorContactStatus = when (sensorContactStatusFlag) {
                    0u, 1u -> SensorContactFeature.NotSupported
                    2u -> SensorContactFeature.SupportedNoContact
                    3u -> SensorContactFeature.SupportedAndContact
                    else -> SensorContactFeature.NotSupported
                }

                val energyExpended = if (energyExpenditurePresent) parser.getUInt16() else null

                val rrArray = ArrayList<Int>()
                if (rrIntervalPresent) {
                    while (parser.offset < value.size) {
                        val rrInterval = parser.getUInt16()
                        rrArray.add((rrInterval.toDouble() / 1024.0 * 1000.0).toInt())
                    }
                }

                return HeartRateMeasurement(
                    pulse = pulse,
                    energyExpended = energyExpended,
                    sensorContactStatus = sensorContactStatus,
                    rrIntervals = rrArray.toIntArray()
                )
            } catch (_: Exception) {
                return null
            }
        }
    }
}