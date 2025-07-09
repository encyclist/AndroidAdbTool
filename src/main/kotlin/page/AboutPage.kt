package page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tool.ADBUtil
import tool.FileUtil
import tool.PlatformUtil
import java.io.File

/**
 * @auth 二宁
 * @date 2023/12/7
 */
@Composable
fun AboutPage(){
    Column(modifier = Modifier.padding(10.dp).fillMaxWidth().background(Color.White).padding(10.dp).verticalScroll(rememberScrollState())) {
        Row {
            Text(text = "ADB路径：")
            Text(modifier = Modifier.clickable { FileUtil.openFileInExplorer(File(ADBUtil.ADB_PATH)) },text = ADBUtil.ADB_PATH, color = Color.Blue)
        }
        Row(modifier = Modifier.padding(top=10.dp)) {
            Text(text = "下载ADB（中国官方）：")
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://googledownloads.cn/android/repository/platform-tools-latest-windows.zip") }, text = "Windows版本", color = Color.Blue)
            Text(text = "、")
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://googledownloads.cn/android/repository/platform-tools-latest-darwin.zip") }, text = "Mac版本", color = Color.Blue)
            Text(text = "、")
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://googledownloads.cn/android/repository/platform-tools-latest-linux.zip") }, text = "Linux版本", color = Color.Blue)
        }
        Row(modifier = Modifier.padding(top=10.dp)) {
            Text(text = "下载ADB（官方）：")
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://dl.google.com/android/repository/platform-tools-latest-windows.zip") }, text = "Windows版本", color = Color.Blue)
            Text(text = "、")
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://dl.google.com/android/repository/platform-tools-latest-darwin.zip") }, text = "Mac版本", color = Color.Blue)
            Text(text = "、")
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://dl.google.com/android/repository/platform-tools-latest-linux.zip") }, text = "Linux版本", color = Color.Blue)
        }
        Column(modifier = Modifier.padding(top=10.dp)) {
            Text(text = "谷歌全家桶：")
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://www.apkmirror.com/apk/google-inc/google-contacts-sync/") }, text = "Google Contacts Sync(com.google.android.syncadapters.contacts)", color = Color.Blue)
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://www.apkmirror.com/apk/google-inc/google-calendar-sync/") }, text = "Google Calendar Sync(com.google.android.syncadapters.calendar)", color = Color.Blue)
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://www.apkmirror.com/apk/google-inc/google-account-manager/") }, text = "Google Account Manager(com.google.android.gsf.login)", color = Color.Blue)
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://www.apkmirror.com/apk/google-inc/google-services-framework/") }, text = "Google Services Framework(com.google.android.gsf)", color = Color.Blue)
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://www.apkmirror.com/apk/google-inc/google-play-services/") }, text = "Google Play services(com.google.android.gms)", color = Color.Blue)
            Text(modifier = Modifier.clickable { PlatformUtil.openBrowser("https://www.apkmirror.com/apk/google-inc/google-play-store/") }, text = "Google Play Store(com.android.vending)", color = Color.Blue)
        }
    }
}