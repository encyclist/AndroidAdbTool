package config

import bean.DeviceInfo
import com.alibaba.fastjson.JSON
import tool.FileUtil

/**
 * @auth 二宁
 * @date 2023/11/24
 */
object DeviceRecordUtil {
    private const val CONFIG_DEVICE_RECORD = "devicesRecord"
    private val deviceList = arrayListOf<DeviceInfo>()

    fun readDeviceList(){
        val configFile = FileUtil.getConfigFile(CONFIG_DEVICE_RECORD)
        deviceList.clear()
        try {
            deviceList.addAll(JSON.parseArray(configFile.readText(),DeviceInfo::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        deviceList.forEach {
            it.connected = false
        }
    }

    fun getDeviceList(): List<DeviceInfo> {
        return deviceList
    }

    fun saveAll(list: List<DeviceInfo>) {
        list.forEach { source ->
            val targetDevice = deviceList.find { it.device == source.device }
            if(targetDevice != null){
                deviceList.remove(targetDevice)
            }
        }
        deviceList.addAll(list)
        val configFile = FileUtil.getConfigFile(CONFIG_DEVICE_RECORD)
        configFile.writeText(JSON.toJSONString(JSON.toJSON(deviceList)))
    }

    fun saveDevice(device: DeviceInfo){
        val targetDevice = deviceList.find { it.device == device.device }
        if(targetDevice != null){
            deviceList.remove(targetDevice)
        }
        deviceList.add(device)
        val configFile = FileUtil.getConfigFile(CONFIG_DEVICE_RECORD)
        configFile.writeText(JSON.toJSONString(JSON.toJSON(deviceList)))
    }

    fun deleteDevice(device: DeviceInfo){
        val targetDevice = deviceList.find { it.device == device.device }
        if(targetDevice != null){
            deviceList.remove(targetDevice)
            val configFile = FileUtil.getConfigFile(CONFIG_DEVICE_RECORD)
            configFile.writeText(JSON.toJSONString(JSON.toJSON(deviceList)))
        }
    }
}