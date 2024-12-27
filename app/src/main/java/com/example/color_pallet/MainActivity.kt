package com.example.color_pallet

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "color_settings")

class MainActivity : AppCompatActivity() {

    // DataStore keys
    private val RED_KEY = intPreferencesKey("red_value")
    private val GREEN_KEY = intPreferencesKey("green_value")
    private val BLUE_KEY = intPreferencesKey("blue_value")

    // UI components
    private lateinit var colorView: View
    private lateinit var switchRed: SwitchCompat
    private lateinit var switchGreen: SwitchCompat
    private lateinit var switchBlue: SwitchCompat
    private lateinit var seekBarRed: SeekBar
    private lateinit var seekBarGreen: SeekBar
    private lateinit var seekBarBlue: SeekBar
    private lateinit var editTextRed: EditText
    private lateinit var editTextGreen: EditText
    private lateinit var editTextBlue: EditText
    private lateinit var resetButton: Button
    private lateinit var colorCodeTextView: TextView

    private val myViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        initializeComponents()

        // Load stored values from DataStore
        lifecycleScope.launch {
            loadColorValuesFromDataStore()
        }

        // Initialize listeners
        setupListeners()
    }

    private fun initializeComponents() {
        colorView = findViewById(R.id.colorView)
        switchRed = findViewById(R.id.switch1)
        switchGreen = findViewById(R.id.switch2)
        switchBlue = findViewById(R.id.switch3)
        seekBarRed = findViewById(R.id.seekBar4)
        seekBarGreen = findViewById(R.id.seekBar5)
        seekBarBlue = findViewById(R.id.seekBar6)
        editTextRed = findViewById(R.id.editTextRed)
        editTextGreen = findViewById(R.id.editTextGreen)
        editTextBlue = findViewById(R.id.editTextBlue)
        resetButton = findViewById(R.id.button)
        colorCodeTextView = findViewById(R.id.textView)
    }

    private suspend fun loadColorValuesFromDataStore() {
        val preferences = dataStore.data.first()
        val red = preferences[RED_KEY] ?: 0
        val green = preferences[GREEN_KEY] ?: 0
        val blue = preferences[BLUE_KEY] ?: 0

        myViewModel.setRed(red)
        myViewModel.setGreen(green)
        myViewModel.setBlue(blue)

        // Update UI elements with loaded values
        updateUIFromModel()
    }

    private fun saveColorValue(key: Preferences.Key<Int>, value: Int) {
        lifecycleScope.launch {
            dataStore.edit { settings ->
                settings[key] = value
            }
        }
    }

    private fun updateUIFromModel() {
        seekBarRed.progress = myViewModel.getRed()
        seekBarGreen.progress = myViewModel.getGreen()
        seekBarBlue.progress = myViewModel.getBlue()
        editTextRed.setText(String.format(Locale.US, "%.3f", myViewModel.getRed() / 255.0f))
        editTextGreen.setText(String.format(Locale.US, "%.3f", myViewModel.getGreen() / 255.0f))
        editTextBlue.setText(String.format(Locale.US, "%.3f", myViewModel.getBlue() / 255.0f))
        updateColor()
    }

    private fun setupListeners() {
        setupSwitchListener(switchRed, myViewModel::getRed, myViewModel::setRed, myViewModel::setRedBackup, seekBarRed, editTextRed, RED_KEY)
        setupSwitchListener(switchGreen, myViewModel::getGreen, myViewModel::setGreen, myViewModel::setGreenBackup, seekBarGreen, editTextGreen, GREEN_KEY)
        setupSwitchListener(switchBlue, myViewModel::getBlue, myViewModel::setBlue, myViewModel::setBlueBackup, seekBarBlue, editTextBlue, BLUE_KEY)

        setupSeekBarListener(seekBarRed, myViewModel::setRed, editTextRed, RED_KEY, myViewModel::getRed)
        setupSeekBarListener(seekBarGreen, myViewModel::setGreen, editTextGreen, GREEN_KEY, myViewModel::getGreen)
        setupSeekBarListener(seekBarBlue, myViewModel::setBlue, editTextBlue, BLUE_KEY, myViewModel::getBlue)

        setupEditTextListener(editTextRed, myViewModel::setRed, seekBarRed, RED_KEY, myViewModel::getRed)
        setupEditTextListener(editTextGreen, myViewModel::setGreen, seekBarGreen, GREEN_KEY, myViewModel::getGreen)
        setupEditTextListener(editTextBlue, myViewModel::setBlue, seekBarBlue, BLUE_KEY, myViewModel::getBlue)

        resetButton.setOnClickListener { resetValues() }
    }

    private fun setupSwitchListener(
        switch: SwitchCompat,
        getColor: () -> Int,
        setColor: (Int) -> Unit,
        setBackup: (Int) -> Unit,
        seekBar: SeekBar,
        editText: EditText,
        key: Preferences.Key<Int>
    ) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            handleSwitchChange(isChecked, getColor, setColor, setBackup, seekBar, editText, key)
        }
    }

    private fun setupSeekBarListener(
        seekBar: SeekBar,
        setColor: (Int) -> Unit,
        editText: EditText,
        key: Preferences.Key<Int>,
        getColor: () -> Int
    ) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setColor(progress)
                editText.setText(String.format(Locale.US, "%.3f", progress / 255.0f))
                updateColor()
                saveColorValue(key, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupEditTextListener(
        editText: EditText,
        setColor: (Int) -> Unit,
        seekBar: SeekBar,
        key: Preferences.Key<Int>,
        getColor: () -> Int
    ) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val input = editText.text.toString().toFloatOrNull()
                if (input != null && input in 0.0..1.0) {
                    val newValue = (input * 255).toInt()
                    setColor(newValue)
                    seekBar.progress = newValue
                    updateColor()
                    saveColorValue(key, newValue)
                } else {
                    editText.setText(String.format(Locale.US, "%.3f", getColor() / 255.0f))
                }
            }
        }
    }

    private fun handleSwitchChange(
        isChecked: Boolean,
        getColor: () -> Int,
        setColor: (Int) -> Unit,
        setBackup: (Int) -> Unit,
        seekBar: SeekBar,
        editText: EditText,
        key: Preferences.Key<Int>
    ) {
        if (isChecked) {
            val restoredColor = when (key) {
                RED_KEY -> myViewModel.getRedBackup()
                GREEN_KEY -> myViewModel.getGreenBackup()
                BLUE_KEY -> myViewModel.getBlueBackup()
                else -> 0
            }
            setColor(restoredColor)
            seekBar.progress = restoredColor
            editText.setText(String.format(Locale.US, "%.3f", restoredColor / 255.0f))
        } else {
            setBackup(getColor())
            setColor(0)
            seekBar.progress = 0
            editText.setText("0.000")
        }
        seekBar.isEnabled = isChecked
        editText.isEnabled = isChecked
        updateColor()
        saveColorValue(key, getColor())
    }

    private fun updateColor() {
        val color = Color.rgb(myViewModel.getRed(), myViewModel.getGreen(), myViewModel.getBlue())
        colorView.setBackgroundColor(color)
        val hexColor = String.format("#%02X%02X%02X", myViewModel.getRed(), myViewModel.getGreen(), myViewModel.getBlue())
        colorCodeTextView.text = hexColor
        colorCodeTextView.setBackgroundColor(color)
    }

    private fun resetValues() {
        myViewModel.setRed(0)
        myViewModel.setGreen(0)
        myViewModel.setBlue(0)

        seekBarRed.progress = 0
        seekBarGreen.progress = 0
        seekBarBlue.progress = 0

        editTextRed.setText("0.000")
        editTextGreen.setText("0.000")
        editTextBlue.setText("0.000")

        colorView.setBackgroundColor(Color.rgb(0, 0, 0))
        updateColor()

        saveColorValue(RED_KEY, 0)
        saveColorValue(GREEN_KEY, 0)
        saveColorValue(BLUE_KEY, 0)
    }
}
