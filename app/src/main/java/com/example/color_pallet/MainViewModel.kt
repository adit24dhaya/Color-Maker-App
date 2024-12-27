package com.example.color_pallet

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private var red = 0
    private var green = 0
    private var blue = 0

    private var redBackup = 0
    private var greenBackup = 0
    private var blueBackup = 0

    // Functions for Red
    fun getRed(): Int {
        return red
    }

    fun setRed(newCount: Int) {
        red = newCount
    }

    fun getRedBackup(): Int {
        return redBackup
    }

    fun setRedBackup(value: Int) {
        redBackup = value
    }

    // Functions for Green
    fun getGreen(): Int {
        return green
    }

    fun setGreen(newCount: Int) {
        green = newCount
    }

    fun getGreenBackup(): Int {
        return greenBackup
    }

    fun setGreenBackup(value: Int) {
        greenBackup = value
    }

    // Functions for Blue
    fun getBlue(): Int {
        return blue
    }

    fun setBlue(newCount: Int) {
        blue = newCount
    }

    fun getBlueBackup(): Int {
        return blueBackup
    }

    fun setBlueBackup(value: Int) {
        blueBackup = value
    }
}
