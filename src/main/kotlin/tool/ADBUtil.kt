package tool

import UnicodeCharConvert
import bean.DeviceInfo
import bean.FileBean
import java.io.File
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
    fun hasRoot(deviceId: String?): Boolean {
        deviceId ?: return false

        val command = arrayOf("-s",deviceId,"shell","su","-c","ls","/")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        return result.trim().isNotEmpty()
    }

    /**
     * 重启
     */
    fun reboot(deviceId: String?,type:RebootType = RebootType.SYSTEM){
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "reboot",type.type)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 截图
     */
    fun screenshot(deviceId: String?,file: String? = null): String {
        deviceId ?: return ""

        val mFile = file ?: "/sdcard/${System.currentTimeMillis()}.png"
        val command = arrayOf("-s", deviceId, "shell","screencap",mFile)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
        return mFile
    }

    // region 设备信息
    /**
     * 获取系统信息
     */
    fun getProp(deviceId: String?,key:String = ""): HashMap<String, String> {
        deviceId ?: return hashMapOf()

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
    fun getPhysicalSize(deviceId: String?): String {
        deviceId ?: return ""

        val command = arrayOf("-s", deviceId, "shell","wm","size")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.getOrNull(0)?.lastOrNull() ?: ""
    }

    /**
     * 获取dpi
     */
    fun getDensity(deviceId: String?): String {
        deviceId ?: return ""

        val command = arrayOf("-s", deviceId, "shell","wm","density")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.getOrNull(0)?.lastOrNull() ?: ""
    }

    /**
     * 获取电池信息
     */
    fun getBatteryInfo(deviceId: String?):BatteryInfo{
        deviceId ?: return BatteryInfo()

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
    fun getAndroidId(deviceId: String?): String {
        deviceId ?: return ""

        val command = arrayOf("-s", deviceId, "shell","settings","get","secure","android_id")
        return CLUtil.execute(arrayOf(ADB_PATH, *command)).trim()
    }

    fun getMemoryInfo(deviceId: String?):MemoryInfo{
        deviceId ?: return MemoryInfo()

        val command = arrayOf("-s", deviceId, "shell","cat","/proc/meminfo")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return MemoryInfo().apply {
            memTotal = getLineWithStart("MemTotal",data,true,false)?.getOrNull(1)
            memFree = getLineWithStart("MemFree",data,true,false)?.getOrNull(1)
            memAvailable = getLineWithStart("MemAvailable",data,true,false)?.getOrNull(1)
            buffers = getLineWithStart("Buffers",data,true,false)?.getOrNull(1)
            cached = getLineWithStart("Cached",data,true,false)?.getOrNull(1)
            swapCached = getLineWithStart("SwapCached",data,true,false)?.getOrNull(1)
            active = getLineWithStart("Active",data,true,false)?.getOrNull(1)
            inactive = getLineWithStart("Inactive",data,true,false)?.getOrNull(1)
            swapTotal = getLineWithStart("SwapTotal",data,true,false)?.getOrNull(1)
            swapFree = getLineWithStart("SwapFree",data,true,false)?.getOrNull(1)
        }
    }
    // endregion

    // region 文件管理
    /**
     * 导出文件
     */
    fun pull(deviceId: String?, deviceFile: String, localFile: String, su:Boolean = false) {
        deviceId ?: return

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
    fun push(deviceId: String?, localFile: String, deviceDir: String, su: Boolean) {
        deviceId ?: return

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
    fun copyFile(deviceId: String?,originalFile: String,targetFile: String,su:Boolean = false){
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","cp", originalFile, targetFile)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 设置文件权限
     */
    fun setPermission(deviceId: String?,permission:String,file: String,su:Boolean = false){
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","chmod", "-R", permission,file)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 移动文件
     */
    fun moveFile(deviceId: String?, oldFile: String, newFile: String, su: Boolean) {
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","mv", oldFile,newFile)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 列出文件
     */
    fun fileList(deviceId: String?,dir:String,su:Boolean = false): ArrayList<FileBean> {
        deviceId ?: return arrayListOf()

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
    fun deleteFile(deviceId: String?,file:String,su:Boolean = false) {
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell",if (su) "su -c" else "","rm","-r", file)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }
    // endregion

    // region 网络相关
    /**
     * 获取wlan0的ip地址
     */
    fun getWlan0IP(deviceId:String?,v4:Boolean = true): String? {
        deviceId ?: return null

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
    fun getMac(deviceId:String?): String {
        deviceId ?: return ""

        val command = arrayOf("-s",deviceId,"shell","ip","addr","show","wlan0","|","grep","'link/ether'")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.getOrNull(0)?.getOrNull(1) ?: ""
    }

    /**
     * 打开tcpip 5555
     */
    fun openWIFIConnect(deviceId: String?): Boolean {
        deviceId ?: return false

        val command = arrayOf("-s",deviceId,"tcpip","5555")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.firstOrNull()?.firstOrNull()?.equals("restarting",true) ?: false
    }

    /**
     * 连接设备
     */
    fun connectDevice(deviceId: String?): Boolean {
        deviceId ?: return false

        val command = arrayOf("connect",deviceId)
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.firstOrNull()?.firstOrNull()?.equals("connected",true) ?: false
    }

    /**
     * 断开设备
     */
    fun disconnectDevice(deviceId: String?): Boolean {
        deviceId ?: return false
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
    fun stopApplication(deviceId: String?, pk: String) {
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell", "am", "force-stop", pk)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 卸载
     */
    fun unInstall(deviceId: String?, pk: String) {
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "uninstall", pk)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 清空数据
     * FIXME: 似乎没有正常工作
     */
    fun cleanAppData(deviceId: String?, pk: String) {
        deviceId ?: return

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
    fun install(deviceId:String?,filePath:String){
        deviceId ?: return

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

    fun installXapk(deviceId:String?,dirPath:String,obbDir:String) {
        deviceId ?: return

        val command = arrayListOf("-s",deviceId,"install-multiple")
        File(dirPath).listFiles()?.forEach {
            if (it.name.endsWith(".apk",true)){
                command.add(it.absolutePath)
            }
        }
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command.toTypedArray()))
        if(result.contains("success",true)){
            val obb = File(obbDir)
            if (obb.exists()) {
                obb.listFiles()?.forEach {
                    push(deviceId,it.absolutePath,"/sdcard/Android/obb/",false)
                }
            }
        }
    }

    /**
     * 安装用于目标版本小于6.0的应用
     */
    private fun installLow(deviceId:String?,filePath:String){
        deviceId ?: return

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
    fun getApkPath(deviceId: String?, pk: String): String? {
        deviceId ?: return null

        val command = arrayOf("-s", deviceId, "shell", "pm", "path", pk)
        return CLUtil.execute(arrayOf(ADB_PATH, *command)).split(":").lastOrNull()?.replace("\n","")?.replace("\r","")
    }

    /**
     * 获取当前页面
     */
    fun getCurrentActivity(deviceId: String?): String {
        deviceId ?: return ""

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
    fun inputText(deviceId: String?, text: String){
        deviceId ?: return

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
    fun inputKey(deviceId: String?, event: String){
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell","input","keyevent", event)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }

    /**
     * 输入滑动
     */
    fun inputSwipe(deviceId: String?, startX: String, startY: String, endX: String, endY: String){
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell","input","swipe", startX,startY,endX,endY)
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }
    // endregion

    // region 开发
    fun enableAdbWifi(deviceId: String?) {
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "tcpip","5555")
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }
    fun showBorder(deviceId: String?,show: Boolean) {
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell", "setprop","debug.layout","$show;","service","call","activity","1599295570")
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }
    fun isShowBorder(deviceId: String?): Boolean {
        deviceId ?: return false

        val command = arrayOf("-s", deviceId, "shell", "getprop","debug.layout")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.getOrNull(0)?.getOrNull(0).toBoolean()
    }
    fun isShowTouchLocations(deviceId: String?): Boolean {
        deviceId ?: return false

        val command = arrayOf("-s", deviceId, "shell", "settings","get","system","pointer_location")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        return data.getOrNull(0)?.getOrNull(0) == "1"
    }
    fun showTouchLocations(deviceId: String?,show: Boolean) {
        deviceId ?: return

        val command = arrayOf("-s", deviceId, "shell", "settings","put","system","pointer_location",if(show) "1" else "0")
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }
    fun isShowHWUIProfile(deviceId: String?): Boolean {
        deviceId ?: return false

        val command = arrayOf("-s", deviceId, "shell", "getprop","debug.hwui.profile")
        val result = CLUtil.execute(arrayOf(ADB_PATH, *command))
        val data = parseResult(result)
        val r = data.getOrNull(0)?.getOrNull(0)
        return r == "true" || r == "visual_bars"
    }
    fun showHWUIProfile(deviceId: String?,show: Boolean) {
        deviceId ?: return

        var command = arrayOf("-s", deviceId, "shell", "setprop","debug.hwui.profile",if(show) "visual_bars" else "false")
        CLUtil.execute(arrayOf(ADB_PATH, *command))
        command = arrayOf("-s", deviceId, "shell", "service","call","activity","1599295570")
        CLUtil.execute(arrayOf(ADB_PATH, *command))
    }
    // endregion

    /**
     * 获取ADB路径
     */
    private fun getAdbPath(): String {
        val adbDir = File(FileUtil.getUserHomeFile(),".AndroidAdbTool${File.separator}runtimeAdbFiles")
        val adbFile = if (PlatformUtil.isWindows()) {
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
        SYSTEM(""), RECOVER("recovery"), FASTBOOT("fastboot"), BOOTLOADER("bootloader")
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

    class MemoryInfo(){
        var memTotal: String? = null // 所有可用RAM大小（即物理内存减去一些预留位和内核的二进制代码大小）（HighTotal + LowTotal）,系统从加电开始到引导完成，BIOS等要保留一些内存，内核要保留一些内存，最后剩下可供系统支配的内存就是MemTotal。这个值在系统运行期间一般是固定不变的
        var memFree: String? = null // LowFree与HighFree的总和，被系统留着未使用的内存,MemFree是说的系统层面
        var memAvailable: String? = null // 应用程序可用内存数。系统中有些内存虽然已被使用但是可以回收的，比如cache/buffer、slab都有一部分可以回收，所以MemFree不能代表全部可用的内存，这部分可回收的内存加上MemFree才是系统可用的内存，即：MemAvailable≈MemFree+Buffers+Cached，它是内核使用特定的算法计算出来的，是一个估计,MemAvailable是说的应用程序层面
        var buffers: String? = null // 用来给文件做缓冲大小
        var cached: String? = null // 用于从磁盘读取的文件在内存中的缓存。不包括 SwapCached
        var swapCached: String? = null // 被swap到磁盘上的内存，如果已经被swap回来，但是仍在swapfile中也保留了一份。（在内存压力大的时候这样可以节省I/O，因为很可能这些内存马上又要被换出去）
        var active: String? = null // 更近和更频繁使用的内存，如果非必要不会被使用。
        var inactive: String? = null // 更没有被频繁使用的内存，更有可能被用于其他用途。
        var swapTotal: String? = null // 总的可用SWAP空间。
        var swapFree: String? = null // 未使用的SWAP空间。
    }
}