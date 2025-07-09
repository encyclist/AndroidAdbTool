package page

import UZipFile
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import bean.DeviceInfo
import tool.ADBUtil
import tool.BundleUtil
import tool.FileUtil
import java.awt.FileDialog
import java.io.File

/**
 * @auth 二宁
 * @date 2023/12/4
 */
@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun InstallPage(device: DeviceInfo){
    var apkFile by remember { mutableStateOf<String?>(null) }
    var jksPath by remember { mutableStateOf<String?>(null) }
    var jksPass by remember { mutableStateOf<String?>(null) }
    var jksAlias by remember { mutableStateOf<String?>(null) }
    var jksAliasPass by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(10.dp).fillMaxWidth().padding(10.dp)) {
        Row(modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                val fileDialog = FileDialog(ComposeWindow(),"选择安装包", FileDialog.LOAD)
                fileDialog.isMultipleMode = false
                fileDialog.isVisible = true
                val directory = fileDialog.directory?.replace("\n","")?.replace("\r","")
                val file = fileDialog.file?.replace("\n","")?.replace("\r","")
                if(directory.isNullOrBlank() || file.isNullOrBlank()){
                    return@Button
                }
                apkFile = File(directory,file).absolutePath
            }, modifier = Modifier.width(200.dp).padding(end=10.dp)) {
                Text("选择安装包")
            }
            TextField(
                colors = textFieldColors(
                    backgroundColor = Color.White
                ),
                shape = RectangleShape,
                modifier = Modifier.dragAndDropTarget({true},object :DragAndDropTarget{
                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        if(event.dragData() is DragData.FilesList){
                            val files = (event.dragData() as DragData.FilesList).readFiles()
                            if(files.size != 1){
                                return false
                            }
                            val file = files.first()
                            apkFile = file.removePrefix("file:")
                            return true
                        }
                        return false
                    }
                }),
                value = apkFile ?: "" ,
                onValueChange = {apkFile=it},
                singleLine = true,
                placeholder = { Text("支持apk、apks、xapk、aab，支持拖拽") },
                isError = apkFile.isNullOrBlank() || (!apkFile!!.endsWith(".apk",true) && !apkFile!!.endsWith(".apks",true) || !apkFile!!.endsWith(".xapk",true) || !apkFile!!.endsWith(".aab",true))
            )
        }
        Row(modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                val fileDialog = FileDialog(ComposeWindow(),"选择密钥文件", FileDialog.LOAD)
                fileDialog.isMultipleMode = false
                fileDialog.isVisible = true
                val directory = fileDialog.directory?.replace("\n","")?.replace("\r","")
                val file = fileDialog.file?.replace("\n","")?.replace("\r","")
                if(directory.isNullOrBlank() || file.isNullOrBlank()){
                    return@Button
                }
                jksPath = File(directory,file).absolutePath
            }, modifier = Modifier.width(200.dp).padding(end=10.dp)) {
                Text("选择密钥文件")
            }
            TextField(
                colors = textFieldColors(
                    backgroundColor = Color.White
                ),
                shape = RectangleShape,
                modifier = Modifier.dragAndDropTarget({true},object :DragAndDropTarget{
                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        if(event.dragData() is DragData.FilesList){
                            val files = (event.dragData() as DragData.FilesList).readFiles()
                            if(files.size != 1){
                                return false
                            }
                            val file = files.first()
                            jksPath = file.removePrefix("file:")
                            return true
                        }
                        return false
                    }
                }),
                value = jksPath ?: "" ,
                onValueChange = {jksPath=it},
                singleLine = true,
                placeholder = { Text("签名文件路径，支持拖拽") },
                isError = jksPath.isNullOrBlank()
            )
        }
        Row(modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.width(200.dp),text = "JksPass: ")
            TextField(
                colors = textFieldColors(
                    backgroundColor = Color.White
                ),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                value = jksPass ?: "" ,
                onValueChange = {jksPass=it},
                singleLine = true,
                placeholder = { Text("签名文件密码") },
                isError = jksPass.isNullOrBlank()
            )
        }
        Row(modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.width(200.dp),text = "JksAlias: ")
            TextField(
                colors = textFieldColors(
                    backgroundColor = Color.White
                ),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                value = jksAlias ?: "" ,
                onValueChange = {jksAlias=it},
                singleLine = true,
                placeholder = { Text("密钥别名") },
                isError = jksAlias.isNullOrBlank()
            )
        }
        Row(modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.width(200.dp), text = "JksAliasPass: ")
            TextField(
                colors = textFieldColors(
                    backgroundColor = Color.White
                ),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                value = jksAliasPass ?: "" ,
                onValueChange = {jksAliasPass=it},
                singleLine = true,
                placeholder = { Text("密钥密码") },
                isError = jksAliasPass.isNullOrBlank()
            )
        }
        Button(onClick = {
            installApp(device,File(apkFile ?: ""),jksPath,jksPass,jksAlias,jksAliasPass)
        }, modifier = Modifier.fillMaxWidth().padding(5.dp)) {
            Text("安装")
        }
        Text(modifier = Modifier.padding(5.dp),text = "如未安装成功请自己找原因")
    }
}

private fun installApp(device:DeviceInfo,file: File?,jksPath:String?,jksPass:String?,jksAlias:String?,jksAliasPass:String?){
    println(file?.absolutePath)
    file ?: return
    if(!file.exists()){
        return
    }

    if(file.name.endsWith(".apk",true)){
        ADBUtil.install(device.device,file.absolutePath)
    }else if(file.name.endsWith(".aab",true)){
        jksPath ?: return
        jksPass ?: return
        jksAlias ?: return
        jksAliasPass ?: return
        if(!File(jksPath).exists()){
            return
        }
        val apksPath = File(FileUtil.getCacheDir(),"${System.currentTimeMillis()}.apks").absolutePath
        try {
            BundleUtil.aab2apks(file.absolutePath,apksPath,jksPath,jksPass,jksAlias,jksAliasPass)
            BundleUtil.installApks(device,apksPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }else if(file.name.endsWith(".apks",true)){
        try {
            BundleUtil.installApks(device,file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }else if(file.name.endsWith(".xapk",true)){
        val path = File(FileUtil.getCacheDir(),"xapk")
        if(path.exists()){
            path.deleteRecursively()
        }
        path.mkdirs()
        UZipFile.unZipFiles(file, path.absolutePath)
        ADBUtil.installXapk(device.device,path.absolutePath,File(path, "Android/obb/").absolutePath)
    }
}