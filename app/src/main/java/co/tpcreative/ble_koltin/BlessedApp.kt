package co.tpcreative.ble_koltin

import android.app.Application

class BlessedApp: Application() {
    override fun onCreate() {
        super.onCreate()
        BluetoothHandler.initialize(this.applicationContext)
    }
}