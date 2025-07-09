package tool

import androidx.compose.ui.res.useResource
import java.awt.Desktop
import java.io.File
import javax.swing.filechooser.FileSystemView

/**
 * @auth 二宁
 * @date 2023/11/3
 */
object FileUtil {
    private val mediaFileType = arrayOf(
        ".mp3",".aac",".m4a",".flac",".m4a",".wav",".aiff",".aif",".wma",".ogg",".oga",".opus",".m4a",".ac3",".dts",".mid",".midi",".au",".amr",".ra",".rm",".ape",".mka",
        ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".webm", ".mpeg", ".mpg", ".vob", ".ts", ".m2ts", ".m4v", ".3gp", ".3g2", ".ogv", ".rm", ".rmvb", ".asf", ".divx", ".f4v", ".mts", ".m2ts",".vtt",".m3u", ".m3u8",
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif", ".webp", ".heif", ".hei", ".svg", ".psd", ".ai", ".eps", ".cr2", ".nef", ".ico", ".cur", ".pcx", ".tga", ".ppm", ".pgm", ".xcf", ".hdr", ".exr",".psd",
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf", ".odt", ".ods", ".odp", ".epub", ".mobi", ".azw", ".azw3", ".fb2", ".chm",
    )

    fun getSelfPath(): String {
        // 方法一：获取当前可执行jar包所在目录
        var filePath = System.getProperty("java.class.path")
        // 得到当前操作系统的分隔符，windows下是";",linux下是":"
        val pathSplit = System.getProperty("path.separator")
        // 若没有其他依赖，则filePath的结果应当是该可运行jar包的绝对路径，此时我们只需要经过字符串解析，便可得到jar所在目录
        if (filePath.contains(pathSplit)) {
            val paths = filePath.split(pathSplit)
            for (i in paths.indices) {
                val f = File(paths[i])
                if (f.exists()){
                    // 只要目录，不精确到文件
                    filePath = if(f.isFile) f.parentFile.absolutePath else f.absolutePath
                    break
                }
            }
        } else if (filePath.endsWith(".jar")) {
            // 截取路径中的jar包名,可执行jar包运行的结果里包含".jar"
            // 有时候会直接返回文件名，过滤之
            filePath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1)
        }
        if (filePath.isBlank()){
            filePath = System.getProperty("user.dir")
        }
        println("jar包所在目录：$filePath")
        return filePath
    }

    fun releaseAdb(){
        val adbDir = File(getUserHomeFile(),".AndroidAdbTool${File.separator}runtimeAdbFiles")
        if(adbDir.isFile){
            adbDir.delete()
        }
        if (!adbDir.exists()){
            adbDir.mkdirs()
            adbDir.setWritable(true,false)
        }
        if (PlatformUtil.isWindows()){
            releaseFile(adbDir,"adb.exe","bin/windows/adb.exe")
            releaseFile(adbDir,"AdbWinApi.dll","bin/windows/AdbWinApi.dll")
            releaseFile(adbDir,"AdbWinUsbApi.dll","bin/windows/AdbWinUsbApi.dll")
        }else if(PlatformUtil.isMac()){
            releaseFile(adbDir,"adb","bin/mac/adb")
        }else{
            releaseFile(adbDir,"adb","bin/linux/adb")
        }
    }

    private fun releaseFile(dir:File,fileName:String,packageFile:String){
        try {
            println("释放文件：${packageFile}到${dir}${File.separator}${fileName}")
            val file = File(dir,fileName)
            if (!file.exists()){
                useResource(packageFile){
                    file.createNewFile()
                    file.setWritable(true,false)
                    file.writeBytes(it.readAllBytes())
                }
                file.setExecutable(true,false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCacheDir(): File {
        val cacheDir = File(getUserHomeFile(),".AndroidAdbTool${File.separator}runtimeCache")
        if(cacheDir.isFile){
            cacheDir.delete()
        }
        if (!cacheDir.exists()){
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    fun cleanCache(){
        getCacheDir().deleteRecursively()
    }

    fun openFileWithDefault(file:File?){
        file ?: return
        if(!file.exists()){
            return
        }
        try {
            Desktop.getDesktop().open(file)
        }catch (e:Exception){
            e.printStackTrace()
            openFileInExplorer(file)
        }
    }

    fun openFileInExplorer(file:File?){
        file ?: return
        if(!file.exists()){
            return
        }
        try {
            Desktop.getDesktop().browseFileDirectory(file)
        }catch (ignore:Exception){ }
    }

    fun getConfigFile(fileName:String): File {
        val dir = File(getUserHomeFile(),".AndroidAdbTool${File.separator}config")
        if(dir.isFile){
            dir.delete()
        }
        if (!dir.exists()){
            dir.mkdirs()
            dir.setWritable(true,false)
        }
        val configFile = File(dir,fileName)
        if (configFile.isDirectory){
            configFile.deleteRecursively()
        }
        configFile.createNewFile()
        return configFile
    }

    fun getDesktopFile(): File {
        return File(getUserHomeFile(),"Desktop")
    }
    fun getUserHomeFile(): File? {
        val home = FileSystemView.getFileSystemView().homeDirectory
        return if(PlatformUtil.isWindows() && home.name.equals("Desktop",true)){
            File(home.absolutePath.removeSuffix(File.separator+"Desktop"))
        }else{
            home
        }
    }

    fun isMediaFile(file: String): Boolean {
        return mediaFileType.any { file.endsWith(it,true) }
    }
}