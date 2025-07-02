package bean

import kotlinx.serialization.Serializable

@Serializable
class DeviceInfo:java.io.Serializable{
    var deviceName: String? = null
    var deviceModel: String? = null
    var device: String? = null
    var offline = false
    var ip:String? = null
    var connected = true

    constructor(){
    }
    constructor(deviceName: String, deviceModel: String, device: String):this(){
        this.deviceName = deviceName
        this.deviceModel = deviceModel
        this.device = device
    }

    fun isWifiConnect(): Boolean {
        return device == ip
    }
}