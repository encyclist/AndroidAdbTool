package page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bean.DeviceInfo
import bean.FileBean
import dialog.InputDialog
import dialog.MessageDialog
import tool.ADBUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.FileUtil
import java.awt.FileDialog
import java.io.File

private const val ROOT = "/"
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileManager(device: DeviceInfo,root:Boolean = false) {
    var showDeleteDialog by remember { mutableStateOf<FileBean?>(null) }
    var showRenameDialog by remember { mutableStateOf<FileBean?>(null) }
    var foldName by remember { mutableStateOf(ROOT) }
    var refresh by remember { mutableStateOf(0) }
    val fileList = remember { mutableStateListOf<FileBean>() }

    // 获取本机文件及文件夹
    LaunchedEffect(foldName,device.device,refresh) {
        withContext(Dispatchers.IO) {
            val list = ADBUtil.fileList(device.device,foldName,root)
            list.sortBy { !it.fold }
            withContext(Dispatchers.Default) {
                fileList.clear()
                fileList.addAll(list)
            }
        }
    }
    LazyColumn(modifier = Modifier.padding(10.dp).fillMaxHeight().background(Color.White)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp)
                    .combinedClickable(onClick = {
                        if (foldName != ROOT) {
                            foldName = foldName.removeSuffix("/")
                            foldName = foldName.substring(0,foldName.lastIndexOf('/')+1)
                        }
                    }, onDoubleClick = {
                    }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(foldName != ROOT) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
                Spacer(modifier = Modifier.width(18.dp))
                Text(foldName, fontSize = 16.sp, modifier = Modifier.padding(vertical = 6.dp))
                Spacer(modifier = Modifier.weight(1F))
                Icon(painterResource("ic_upload.svg"), contentDescription = "上传", modifier = Modifier.size(18.dp).clickable {
                    pushFile(device.device,foldName,root)
                    refresh++
                })
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Refresh, contentDescription = "刷新", modifier = Modifier.clickable {
                    refresh++
                })
            }
        }
        items(fileList.size) {
            val item = fileList[it]
            ContextMenuArea(
                items = {
                    val list = arrayListOf<ContextMenuItem>()
                    if(item.fold){
                        list.add(ContextMenuItem("上传到此处") {
                            pushFile(device.device,item.name,root)
                            refresh++
                        })
                    }else{
                        list.add(ContextMenuItem("默认方式打开") { pullFileToCache(device.device,item,true,root) })
                    }
                    list.add(ContextMenuItem("导出到电脑") { pullFile(device.device,item,false,root) })
                    list.add(ContextMenuItem("重命名") {
                        showRenameDialog = item
                    })
                    list.add(ContextMenuItem("删除") { showDeleteDialog = item })
                    list
                }
            ){
                Row(
                    modifier = Modifier.combinedClickable(onClick = {
                        if (item.fold) {
                            // 去当前文件夹
                            fileList.clear()
                            foldName = "$foldName${item.name}/"
                        }
                    }, onDoubleClick = {
                        if (item.fold) {
                            // 去当前文件夹
                            fileList.clear()
                            foldName = "$foldName${item.name}/"
                        } else {
                            pullFileToCache(device.device,item,true,root)
                        }
                    })
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(if (item.fold) "ic_folder.svg" else "ic_file.svg"),
                        "文件夹：${item.fold}",
                        modifier = Modifier.width(24.dp).height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(item.name, fontSize = 16.sp)
                    if (!item.fold){
                        Text(modifier = Modifier.fillMaxWidth(), text = item.size, fontSize = 16.sp, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }

    if(showDeleteDialog != null) {
        val file = "${showDeleteDialog?.parent}${showDeleteDialog?.name}"
        MessageDialog("注意", "确定要删除${file}吗？", {
            showDeleteDialog = null
        }, {
            ADBUtil.deleteFile(device.device,file,root)
            fileList.remove(showDeleteDialog)
            showDeleteDialog = null
        })
    }
    if(showRenameDialog != null){
        val file = "${showRenameDialog?.parent}${showRenameDialog?.name}"
        InputDialog("重命名",showRenameDialog?.name?:"",{
            showRenameDialog = null
        },{
            if (it.isNullOrBlank()){
                showRenameDialog = null
                return@InputDialog
            }
            val newFile = "${showRenameDialog?.parent}${it}"
            println("${file}重命名为${newFile}")
            ADBUtil.moveFile(device.device,file,newFile,root)
            showRenameDialog = null
            refresh++
        })
    }
}

private fun pullFile(deviceId: String?,source:FileBean,open:Boolean,root:Boolean){
    val fileDialog = FileDialog(ComposeWindow(),"导出文件", FileDialog.SAVE)
    fileDialog.isMultipleMode = false
    fileDialog.file = source.name
    fileDialog.isVisible = true
    val directory = fileDialog.directory?.replace("\n","")?.replace("\r","")
    var file = fileDialog.file?.replace("\n","")?.replace("\r","")
    if(file.isNullOrBlank()){
        file = source.name
    }
    if (directory != null) {
        val path = "$directory${file}"
        ADBUtil.pull(deviceId,"${source.parent}${source.name}",path,root)
        if (open){
            FileUtil.openFileWithDefault(File(path))
        }
    }
}

private fun pullFileToCache(deviceId: String?,source:FileBean,open:Boolean,root:Boolean){
    val cacheDir = FileUtil.getCacheDir()
    val file = File(cacheDir,source.name)
    ADBUtil.pull(deviceId,"${source.parent}${source.name}",file.absolutePath,root)
    if (open){
        FileUtil.openFileWithDefault(file)
    }
}

private fun pushFile(deviceId: String?,deviceDir:String,root:Boolean){
    val fileDialog = FileDialog(ComposeWindow(),"上传文件",FileDialog.LOAD)
    fileDialog.isMultipleMode = false
    fileDialog.isVisible = true
    val directory = fileDialog.directory?.replace("\n","")?.replace("\r","")
    val file = fileDialog.file?.replace("\n","")?.replace("\r","")
    if(directory.isNullOrBlank() || file.isNullOrBlank()){
        return
    }
    println("上传文件${directory}${file}到${deviceDir}/")
    ADBUtil.push(deviceId,directory+file, "$deviceDir",root)
}