package com.example.adv2.function

import android.content.ContentValues
import android.content.Context
import android.hardware.Sensor
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
    private lateinit var fileUri: Uri
    private var addHeaderAcc: Boolean = true
    private var addHeaderGyro: Boolean = true
    private var addHeaderMag: Boolean = true
    private var addHeader: Boolean = true


    init {
    }

    private fun initializeSensors() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // 创建数据文件
        //得到时间戳

//        accelerometerFileUri = createFileForSensor("accelerometer.csv")
//        gyroscopeFileUri = createFileForSensor("gyroscope.csv")
//        magnetometerFileUri = createFileForSensor("magnetometer.csv")
        fileUri = createFileForSensor("file.csv")
        addHeaderForFile(fileUri)

        addHeaderAcc = true
        addHeaderGyro = true
        addHeaderMag = true
        addHeader = true

    }



    fun startSensorListener(sensorEventListener: SensorEventListener):Uri {
        initializeSensors()
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
//        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        return fileUri
    }

    fun stopSensorListener(sensorEventListener: SensorEventListener) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    fun createFileForSensor(fileName: String): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        return uri ?: throw IOException("Failed to create new MediaStore record.")
    }

    fun addHeaderForFile(file_uri: Uri){
        try {
            context.contentResolver.openOutputStream(file_uri, "wa")?.use { outputStream ->
                if (addHeader) {
                    // 只在首次写入时添加标题行
                    val header = "Time,angle-z,angle-x,angle-y\n"
                    outputStream.write(header.toByteArray())
                    addHeader = false
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun writeDataToFile(data: String) {
        try {
            context.contentResolver.openOutputStream(fileUri, "wa")?.use { outputStream ->
//                 假设data字符串是以逗号分隔的x,y,z值
                outputStream.write(data.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun writeDataToFileAcc(data: String) {
        try {
            context.contentResolver.openOutputStream(accelerometerFileUri, "wa")?.use { outputStream ->
                if (addHeaderAcc) {
                    // 只在首次写入时添加标题行
                    val header = "Time,X-axis,Y-axis,Z-axis\n"
                    outputStream.write(header.toByteArray())
                    addHeaderAcc = false
                }
                // 假设data字符串是以逗号分隔的x,y,z值
//                val csvFormattedString = "$data" // 在每条记录后添加换行符
                outputStream.write(data.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun writeDataToFileGyro(data: String) {
        try {
            context.contentResolver.openOutputStream(gyroscopeFileUri, "wa")?.use { outputStream ->
                if (addHeaderGyro) {
                    // 只在首次写入时添加标题行
                    val header = "Time,X-axis,Y-axis,Z-axis\n"
                    outputStream.write(header.toByteArray())
                    addHeaderGyro = false
                }
                // 假设data字符串是以逗号分隔的x,y,z值
//                val csvFormattedString = "$data\n" // 在每条记录后添加换行符
                outputStream.write(data.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun writeDataToFileMag(data: String) {
        try {
            context.contentResolver.openOutputStream(magnetometerFileUri, "wa")?.use { outputStream ->
                if (addHeaderMag) {
                    // 只在首次写入时添加标题行
                    val header = "Time,X-axis,Y-axis,Z-axis,X-axis,Y-axis,Z-axis,X-axis,Y-axis,Z-axis\n"
                    outputStream.write(header.toByteArray())
                    addHeaderMag = false
                }
                // 假设data字符串是以逗号分隔的x,y,z值
//                val csvFormattedString = "$data\n" // 在每条记录后添加换行符
                outputStream.write(data.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
