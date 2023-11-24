package tool

import UnicodeCharConvert
import bean.DeviceInfo
import bean.FileBean
import java.io.File
import java.net.URLEncoder
import java.util.*
import javax.swing.filechooser.FileSystemView
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author erning
 * @date 2022/7/8 14:37
 * https://blog.csdn.net/zx54633089/article/details/115346785
 */
object ADBUtil {
    var ADB_PATH = getAdbPath()

    /**
     * 检查有没有ADB
     */
    fun checkADB(): Boolean {
        val command = arrayOf("--version")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        val versionLine = getLineWithStart("Version",data)
        return versionLine != null
    }

    /**
     * 获取设备列表
     */
    fun getDevice(): ArrayList<DeviceInfo> {
        val command = arrayOf("devices","-l")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        val list = arrayListOf<DeviceInfo>()
        data.forEachIndexed { index, arr ->
            if (index != 0){
                val offline = arr.getOrNull(1).equals("offline",true)
                val id = arr.getOrNull(0)?.split(":")?.firstOrNull() ?: ""
                val model = arr.getOrNull(3)?.split(":")?.getOrNull(1) ?: ""
                val name = arr.getOrNull(4)?.split(":")?.getOrNull(1) ?: ""
                val device = DeviceInfo(name,model,id)
                device.offline = offline
                device.ip = getWlan0IP(id)
                if(!offline) {
                    list.add(device)
                }
            }
        }
        return list
    }

    /**
     * 是否有Root
     */
    fun hasRoot(deviceId: String): Boolean {
        val command = arrayOf("-s",deviceId,"shell","su","-c","ls","/")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        return result.trim().isNotEmpty()
    }

    /**
     * 重启
     */
    fun reboot(deviceId: String,type:RebootType = RebootType.SYSTEM){
        val command = arrayOf("-s", deviceId, "reboot",type.type)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 截图
     */
    fun screenshot(deviceId: String,file: String? = null): String {
        val mFile = file ?: "/sdcard/${System.currentTimeMillis()}.png"
        val command = arrayOf("-s", deviceId, "shell","screencap",mFile)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
        return mFile
    }

    // region 设备信息
    /**
     * 获取系统信息
     */
    fun getProp(deviceId: String,key:String = ""): HashMap<String, String> {
        val command = arrayOf("-s", deviceId, "shell","getprop" ,key)
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val map = hashMapOf<String,String>()
        if (key.isNotEmpty()){
            map[key] = result
        }else{
            result.split("\n").forEach {
                val data = it.removePrefix("[").removeSuffix("]").split("]: [")
                map[data.getOrNull(0)?:""] = data.getOrNull(1)?:""
            }
        }
        return map
    }

    /**
     * 获取分辨率
     */
    fun getPhysicalSize(deviceId: String): String {
        val command = arrayOf("-s", deviceId, "shell","wm","size")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.getOrNull(0)?.lastOrNull() ?: ""
    }

    /**
     * 获取dpi
     */
    fun getDensity(deviceId: String): String {
        val command = arrayOf("-s", deviceId, "shell","wm","density")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.getOrNull(0)?.lastOrNull() ?: ""
    }

    /**
     * 获取电池信息
     */
    fun getBatteryInfo(deviceId: String):BatteryInfo{
        val command = arrayOf("-s", deviceId, "shell","dumpsys","battery")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return BatteryInfo().apply {
            ac = getLineWithStart("AC",data, strict = false)?.lastOrNull()?.toBoolean() ?: false
            usb = getLineWithStart("USB",data, strict = false)?.lastOrNull()?.toBoolean() ?: false
            wireless = getLineWithStart("Wireless",data, strict = false)?.lastOrNull()?.toBoolean() ?: false
            present = getLineWithStart("present",data, strict = false)?.lastOrNull()?.toBoolean() ?: false
            status = getLineWithStart("status",data, strict = false)?.lastOrNull()?.toInt() ?: -1
            health = getLineWithStart("health",data, strict = false)?.lastOrNull()?.toInt() ?: -1
            level = getLineWithStart("level",data, strict = false)?.lastOrNull()?.toInt() ?: -1
            scale = getLineWithStart("scale",data, strict = false)?.lastOrNull()?.toInt() ?: -1
            voltage = getLineWithStart("voltage",data, strict = false)?.lastOrNull()?.toInt() ?: -1
            temperature = getLineWithStart("temperature",data, strict = false)?.lastOrNull()?.toInt() ?: -1
            technology = getLineWithStart("technology",data, strict = false)?.lastOrNull()
            counter = getLineWithStart("Charge",data, strict = false)?.lastOrNull()?.toInt() ?: -1
        }
    }

    /**
     * 获取AndroidId
     */
    fun getAndroidId(deviceId: String): String {
        val command = arrayOf("-s", deviceId, "shell","settings","get","secure","android_id")
        return CLUtil.execute(arrayOf(ADB_PATH, *command)).trim()
    }
    // endregion

    // region 文件管理
    /**
     * 导出文件
     */
    fun pull(deviceId: String, deviceFile: String, localFile: String, su:Boolean = false) {
        val command = arrayOf("-s", deviceId, "pull", deviceFile, UnicodeCharConvert.string2Unicode(localFile))
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        if (su && result.contains("Permission denied",true)){
            // 没有权限
            val deviceFileName = deviceFile.split(File.separator).last()
            copyFile(deviceId,deviceFile,"/data/local/tmp/${deviceFileName}",true)
            setPermission(deviceId,"777","/data/local/tmp/${deviceFileName}",true)
            val command2 = arrayOf("-s", deviceId, "pull", "/data/local/tmp/${deviceFileName}", UnicodeCharConvert.string2Unicode(localFile))
            CLUtil.execute(arrayOf(ADB_PATH, *command2))
            deleteFile(deviceId,"/data/local/tmp/${deviceFileName}",true)
        }
    }

    /**
     * 上传文件
     */
    fun push(deviceId: String, localFile: String, deviceDir: String, su: Boolean) {
        val fileName = localFile.split(File.separator).last()
        val newFileName = "${System.currentTimeMillis()}"
        val command = arrayOf("-s", deviceId, "push", localFile, deviceDir+newFileName)
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        moveFile(deviceId,deviceDir+newFileName,deviceDir+fileName,su)
        if (su && result.contains("Permission denied",true)){
            // 没有权限
            val newDeviceDir = "/data/local/tmp/"
            CLUtil.execute(arrayOf(ADB_PATH, "-s", deviceId, "push", localFile, newDeviceDir+newFileName))
            moveFile(deviceId,newDeviceDir+newFileName,deviceDir+fileName,true)
        }
    }

    /**
     * 复制文件
     */
    fun copyFile(deviceId: String,originalFile: String,targetFile: String,su:Boolean = false){
        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","cp", originalFile, targetFile)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 设置文件权限
     */
    fun setPermission(deviceId: String,permission:String,file: String,su:Boolean = false){
        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","chmod", "-R", permission,file)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 移动文件
     */
    fun moveFile(deviceId: String, oldFile: String, newFile: String, su: Boolean) {
        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","mv", oldFile,newFile)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 列出文件
     */
    fun fileList(deviceId: String,dir:String,su:Boolean = false): ArrayList<FileBean> {
        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","ls","-p","-s","-A","-L","-h", dir)
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        val list = arrayListOf<FileBean>()
        data.forEach {
            if (it.firstOrNull()?.startsWith("total",true) == false){
                val size = it.getOrNull(0) ?: "?"
                val fileName = (it.getOrNull(1) ?: "")
                val isDir = fileName.endsWith("/")
                val bean = FileBean(fileName.removeSuffix("/"), isDir,size)
                bean.parent = dir
                list.add(bean)
            }
        }
        return list
    }

    /**
     * 删除文件
     */
    fun deleteFile(deviceId: String,file:String,su:Boolean = false) {
        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","rm","-r", file)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }
    // endregion

    // region 网络相关
    /**
     * 获取wlan0的ip地址
     */
    fun getWlan0IP(deviceId:String,v4:Boolean = true): String? {
        val command = arrayOf("-s",deviceId,"shell","ip","addr","show","wlan0","|","grep","'inet'","|","cut","-d","'/'","-f","1")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return if (v4){
            data.getOrNull(0)?.lastOrNull()
        }else{
            data.getOrNull(1)?.lastOrNull()
        }
    }

    /**
     * 获取Mac地址
     */
    fun getMac(deviceId:String): String {
        val command = arrayOf("-s",deviceId,"shell","ip","addr","show","wlan0","|","grep","'link/ether'")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.getOrNull(0)?.getOrNull(1) ?: ""
    }

    /**
     * 打开tcpip 5555
     */
    fun openWIFIConnect(deviceId: String): Boolean {
        val command = arrayOf("-s",deviceId,"tcpip","5555")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.firstOrNull()?.firstOrNull()?.equals("restarting",true) ?: false
    }

    /**
     * 连接设备
     */
    fun connectDevice(deviceId: String): Boolean {
        val command = arrayOf("connect",deviceId)
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.firstOrNull()?.firstOrNull()?.equals("connected",true) ?: false
    }

    /**
     * 断开设备
     */
    fun disconnectDevice(deviceId: String): Boolean {
        val command = arrayOf("disconnect",deviceId)
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.firstOrNull()?.firstOrNull()?.equals("disconnected",true) ?: false
    }
    // endregion

    // region 应用相关
    /**
     * 停止应用
     */
    fun stopApplication(deviceId: String, pk: String) {
        val command = arrayOf("-s", deviceId, "shell", "am", "force-stop", pk)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 卸载
     */
    fun unInstall(deviceId: String, pk: String) {
        val command = arrayOf("-s", deviceId, "uninstall", pk)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 清空数据
     * FIXME: 似乎没有正常工作
     */
    fun cleanAppData(deviceId: String, pk: String) {
        stopApplication(deviceId, pk)
        val command = arrayOf("-s", deviceId, "shell","pm","clear", pk)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 安装apk，结果如下
     * Performing Streamed Install
     * Success
     * 或
     * INSTALL_FAILED_DEPRECATED_SDK_VERSION: App package must target at least SDK version 23, but found 7
     */
    fun install(deviceId:String,filePath:String){
        val command = arrayOf("-s",deviceId,"install",filePath)
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        if(data.getOrNull(1)?.firstOrNull()?.equals("success",true) != true){
            if (data.joinToString(" ").contains("INSTALL_FAILED_DEPRECATED_SDK_VERSION")){
                installLow(deviceId, filePath)
            }else{
                throw RuntimeException(data.joinToString(" \\n "){
                    it.joinToString(" ")
                })
            }
        }
    }

    /**
     * 安装用于目标版本小于6.0的应用
     */
    private fun installLow(deviceId:String,filePath:String){
        val command = arrayOf("-s",deviceId,"install","--bypass-low-target-sdk-block",filePath)
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        if(data.getOrNull(1)?.firstOrNull()?.equals("success",true) != true){
            throw RuntimeException(data.joinToString(" \\n "){
                it.joinToString(" ")
            })
        }
    }

    /**
     * 获取应用安装路径
     */
    fun getApkPath(deviceId: String, pk: String): String? {
        val command = arrayOf("-s", deviceId, "shell", "pm", "path", pk)
        return CLUtil.execute(arrayOf(ADB_PATH, *command)).split(":").lastOrNull()?.replace("\n","")?.replace("\r","")
    }

    /**
     * 获取当前页面
     */
    fun getCurrentActivity(deviceId: String): String {
        val command = arrayOf("-s", deviceId, "shell", "dumpsys", "window", "|", "grep", "mCurrentFocus")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        val windows = data.joinToString("\n") {
            (it.lastOrNull() ?: "").replace("}","")
        }
        return windows
    }
    // endregion

    // region 交互
    /**
     * 输入文本
     */
    fun inputText(deviceId: String, text: String){
        val command = arrayOf("-s", deviceId, "shell","input","text", text)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 输入按键
     * 3:HOME键 4:返回键 5:打开拨号应用 6:挂断电话 24:增加音量 25:降低音量 26:电源键 27:拍照（需要在相机应用里）
     * 64:打开浏览器 82:菜单键 85:播放/暂停 86:停止播放 87:播放下一首 88:播放上一首
     * 122:移动光标到行首或列表顶部 123:移动光标到行末或列表底部
     * 126:恢复播放 127:暂停播放 164:静音
     * 176:打开系统设置 187:切换应用 207:打开联系人 208:打开日历 209:打开音乐 210:打开计算器
     * 220:降低屏幕亮度 221:提高屏幕亮度 223:系统休眠 224:点亮屏幕 231:打开语音助手
     */
    fun inputKey(deviceId: String, event: String){
        val command = arrayOf("-s", deviceId, "shell","input","keyevent", event)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 输入滑动
     */
    fun inputSwipe(deviceId: String, startX: String, startY: String, endX: String, endY: String){
        val command = arrayOf("-s", deviceId, "shell","input","swipe", startX,startY,endX,endY)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }
    // endregion

    /**
     * 获取ADB路径
     */
    private fun getAdbPath(): String {
        val isWindows = PlatformUtil.isWindows()
        val adbDir = if (isWindows){
            File(FileUtil.getUserHomeFile(),"AppData${File.separator}Local${File.separator}AndroidAdbTool${File.separator}runtimeAdbFiles")
        }else{
            File(FileUtil.getSelfPath(),"runtimeAdbFiles")
        }
        val adbFile = if (isWindows) {
            File(adbDir,"adb.exe")
        } else {
            File(adbDir,"adb")
        }
        if (adbFile.exists()) {
            println("找到ADB文件：${adbFile.absolutePath}")
            return adbFile.absolutePath
        }
        println("ADB文件不存在，使用环境变量")
        return "adb"
    }

    /**
     * 找到已指定字符串开头的行
     * @param start 要查找的内容
     * @param data 所有数据
     * @param ignoreCase 是否忽略大小写
     * @param strict 是否严格模式(严格模式下要求第一个单词必须和给定的单词完全相同，否则只要以给定的单词开头即可)
     */
    private fun getLineWithStart(start:String,data:Array<Array<String>>,ignoreCase:Boolean = true,strict:Boolean = true): Array<String>? {
        data.forEach { line ->
            if(strict){
                if(line.firstOrNull()?.equals(start,ignoreCase) == true){
                    return line
                }
            }else{
                if(line.firstOrNull()?.startsWith(start,ignoreCase) == true){
                    return line
                }
            }
        }
        return null
    }

    private fun parseResult(result:String?): Array<Array<String>> {
        result ?: return emptyArray()

        val data = arrayListOf<Array<String>>()

        val lines = result.replace("\r","\n").split("\n")
        lines.forEach {
            val line = parseLine(it)
            if(line.isNotEmpty()) {
                data.add(line)
            }
        }

        return data.toTypedArray()
    }

    private fun parseLine(line:String?):Array<String>{
        if(line.isNullOrBlank()){
            return emptyArray()
        }
        val lines = line.replace("\t"," ").split(" ").filter { it.trim().isNotBlank() }
        return lines.toTypedArray()
    }

    enum class RebootType(val type:String){
        SYSTEM(""), RECOVER("recovery"), FASTBOOT("bootloader")
    }

    class BatteryInfo(){
        var ac:Boolean = false
        var usb:Boolean = false
        var wireless:Boolean = false
        var present:Boolean = false // 电池是否存在
        var status:Int = -1 // 电池状态(1:未知;2:充电;3:放电;4:不充电;5:满电)
        var health:Int = -1 // 电池健康状态(1:未知;2:良好;3:过热;4:死亡;5:过电压;6:未指定故障;7:过冷)
        var level:Int = -1 // 电池电量
        var scale:Int = -1 // 电池总电量
        var voltage:Int = -1 // 电压
        var temperature:Int = -1 // 电池温度(一般/10)
        var technology:String? = ""
        var counter:Int = -1 // 充电计数(一般/1000)

        fun getHealthStr(): String {
            return when(health){
                2 -> "良好"
                3 -> "过热"
                4 -> "死亡"
                5 -> "过电压"
                6 -> "未指定故障"
                7 -> "过冷"
                else -> "未知"
            }
        }

        fun getStatusStr(): String {
            return when(status){
                2 -> "充电"
                3 -> "放电"
                4 -> "不充电"
                5 -> "满电"
                else -> "未知"
            }
        }

        fun getChargingMethod(): String {
            return when{
                ac -> "充电器"
                usb -> "USB"
                wireless -> "无线"
                else -> "未知"
            }
        }
    }
}