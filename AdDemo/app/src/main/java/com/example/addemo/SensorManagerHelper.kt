package com.example.addemo

import android.content.ContentValues
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.IOException

class SensorManagerHelper(private val context: Context) {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var magnetometer: Sensor? = null

    private lateinit var accelerometerFileUri: Uri
    private lateinit var gyroscopeFileUri: Uri
    private lateinit var magnetometerFileUri: Uri

    init {

    }
    private fun initializeSensors() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // 创建数据文件
        accelerometerFileUri = createFileForSensor("accelerometer.txt")
        gyroscopeFileUri = createFileForSensor("gyroscope.txt")
        magnetometerFileUri = createFileForSensor("magnetometer.txt")
    }

    fun startSensorListener(sensorEventListener: SensorEventListener) {
        initializeSensors()
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)

    }

    fun stopSensorListener(sensorEventListener: SensorEventListener) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    fun createFileForSensor(fileName: String): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        return uri ?: throw IOException("Failed to create new MediaStore record.")
    }

    fun writeDataToFileAcc(data: String) {
        try {
            context.contentResolver.openOutputStream(accelerometerFileUri, "wa")?.use { outputStream ->
                outputStream.write(data.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun writeDataToFileGyro(data: String) {
        try {
            context.contentResolver.openOutputStream(gyroscopeFileUri, "wa")?.use { outputStream ->
                outputStream.write(data.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun writeDataToFileMag(data: String) {
        try {
            context.contentResolver.openOutputStream(magnetometerFileUri, "wa")?.use { outputStream ->
                outputStream.write(data.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}
