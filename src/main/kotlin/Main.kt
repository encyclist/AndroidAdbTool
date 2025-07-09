import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import bean.About
import bean.CurrentAppInfo
import bean.DeviceInfo
import bean.DeviceRecord
import bean.FileManage
import bean.Install
import bean.MainNav
import bean.PhoneInfo
import bean.ProguardGenerator
import bean.QuickFun
import config.DeviceRecordUtil
import page.AboutPage
import page.CurrentAppInfoPage
import page.DeviceRecordPage
import page.FileManager
import page.InstallPage
import page.MainNav
import page.PhoneInfoPage
import page.ProguardGeneratorPage
import page.QuickPage
import res.defaultBgColor
import tool.ADBUtil
import tool.FileUtil
import java.awt.Dimension
import javax.swing.UIManager

fun main() = application {
    FileUtil.releaseAdb()
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    DeviceRecordUtil.readDeviceList()
    Window(
        onCloseRequest = {
            FileUtil.cleanCache()
            exitApplication()
        },
        title = "AndroidAdbTool(${System.getProperty("jpackage.app-version")})",
        visible = true,
        state = WindowState(
            size = DpSize(width = 1200.dp, height = 900.dp),
            position = WindowPosition(Alignment.Center)
        )
    ) {
        window.minimumSize = Dimension(680, 400)
        App()
    }
}

@Composable
@Preview
fun App() {
    val connectedDevicesList = remember { mutableStateListOf<DeviceInfo>() }
    val refreshConnectedDevicesList:()->Unit = {
        connectedDevicesList.clear()
        connectedDevicesList.addAll(ADBUtil.getDevice())
        DeviceRecordUtil.saveAll(connectedDevicesList)
    }

    var selectMainNav by remember { mutableStateOf<MainNav?>(null) }
    var device by remember { mutableStateOf<DeviceInfo?>(null) }

    MaterialTheme {
        Scaffold(bottomBar = {
            MainNav(
                modifier = Modifier.width(200.dp),
                refreshConnectedDevicesList,
                connectedDevicesList,
                { selectMainNav = it }) {
                if (it != null) {
                    device = it
                }
            }
        }) {
            Box(modifier = Modifier.background(defaultBgColor).fillMaxHeight().fillMaxWidth().padding(start = 200.dp)) {
                if (device != null) {
                    when (selectMainNav) {
                        is CurrentAppInfo -> CurrentAppInfoPage(device!!)
                        is PhoneInfo -> PhoneInfoPage(device!!)
                        is QuickFun -> QuickPage(device!!)
                        is FileManage -> FileManager(device!!, ADBUtil.hasRoot(device!!.device))
                        is Install -> InstallPage(device!!)
                    }
                }
                when (selectMainNav) {
                    is DeviceRecord -> DeviceRecordPage(refreshConnectedDevicesList, connectedDevicesList)
                    is ProguardGenerator -> ProguardGeneratorPage()
                    is About -> AboutPage()
                }
            }
        }
    }
}