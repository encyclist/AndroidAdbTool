package page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import bean.DeviceInfo
import dialog.MessageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.ADBUtil
import java.awt.FileDialog

@Composable
fun CurrentAppInfoPage(device: DeviceInfo) {
    val scrollState = rememberScrollState()
    var refreshCount by remember { mutableStateOf(0) }
    var showUnInstallDialog by remember { mutableStateOf(false) }
    var showCleanDataDialog by remember { mutableStateOf(false) }

    var currentActivity by remember { mutableStateOf("") }
    var currentPackageName by remember { mutableStateOf("") }
    var currentApkPath by remember { mutableStateOf("") }

    LaunchedEffect(refreshCount, device.device) {
        withContext(Dispatchers.IO) {
            val mCurrentActivity = ADBUtil.getCurrentActivity(device.device)
            val mCurrentPackageName = mCurrentActivity.split("/").getOrNull(0) ?: ""
            val mCurrentApkPath = ADBUtil.getApkPath(device.device, mCurrentPackageName) ?: ""
            withContext(Dispatchers.Default) {
                currentActivity = mCurrentActivity
                currentPackageName = mCurrentPackageName
                currentApkPath = mCurrentApkPath
            }
        }
    }
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        Surface(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
            SelectionContainer {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row {
                        Text("当前应用包名：")
                        Text(currentPackageName)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Text("当前页面：")
                        Text(currentActivity)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Text("当前应用安装路径：")
                        Text(currentApkPath)
                    }
                }
            }
        }
        Row {
            Button(onClick = {
                ADBUtil.stopApplication(device.device, currentPackageName)
            }, modifier = Modifier.padding(10.dp)) {
                Text("停止运行")
            }
            Button(onClick = {
                showUnInstallDialog = true
            }, modifier = Modifier.padding(10.dp)) {
                Text("卸载应用")
            }
            Button(onClick = {
                showCleanDataDialog = true
            }, modifier = Modifier.padding(10.dp)) {
                Text("清空数据")
            }
            Button(onClick = {
                val fileName = "${currentPackageName}.apk"
                val fileDialog = FileDialog(ComposeWindow(), "导出安装包", FileDialog.SAVE)
                fileDialog.isMultipleMode = false
                fileDialog.file = fileName
                fileDialog.isVisible = true
                val directory = fileDialog.directory?.replace("\n", "")?.replace("\r", "")
                var file = fileDialog.file?.replace("\n", "")?.replace("\r", "")
                if (file.isNullOrBlank()) {
                    file = fileName
                }
                if (directory != null) {
                    val path = "$directory$file"
                    println(path)
                    ADBUtil.pull(device.device, currentApkPath, path)
                }
            }, modifier = Modifier.padding(10.dp)) {
                Text("导出安装包")
            }
        }
        Button(onClick = {
            refreshCount++
        }, modifier = Modifier.padding(10.dp).fillMaxWidth()) {
            Text("刷新数据")
        }
    }

    if (showUnInstallDialog) {
        MessageDialog("注意", "确定要卸载吗？", {
            showUnInstallDialog = false
        }, {
            showUnInstallDialog = false
            ADBUtil.unInstall(device.device, currentPackageName)
            refreshCount++
        })
    }
    if (showCleanDataDialog) {
        MessageDialog("注意", "确定要清空数据吗？", {
            showCleanDataDialog = false
        }, {
            showCleanDataDialog = false
            ADBUtil.cleanAppData(device.device, currentPackageName)
            refreshCount++
        })
    }
}