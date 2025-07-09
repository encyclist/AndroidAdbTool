package page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bean.DeviceInfo
import dialog.InputDialog
import res.randomColor
import tool.ADBUtil
import tool.FileUtil
import java.io.File

/**
 * 快捷功能
 */
@Composable
fun QuickPage(device: DeviceInfo) {
    LazyColumn(modifier = Modifier.fillMaxHeight().padding(horizontal = 10.dp, vertical = 10.dp).fillMaxWidth()) {
        // 前后加个间距
        item {
            CommonFunction(device)
            Spacer(modifier = Modifier.height(10.dp))
            AboutSystem(device)
            Spacer(modifier = Modifier.height(10.dp))
            AboutKeyBoard(device)
        }
    }
}

/**
 * 常用功能
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommonFunction(device: DeviceInfo) {
    var showInputTextDialog by remember { mutableStateOf(false) }
    var showBorder by remember { mutableStateOf(ADBUtil.isShowBorder(device.device)) }
    var showTouchLocations by remember { mutableStateOf(ADBUtil.isShowTouchLocations(device.device)) }
    var showHWUIProfile by remember { mutableStateOf(ADBUtil.isShowHWUIProfile(device.device)) }

    BaseQuick("常用功能", color = Color(255, 152, 0)) {
        FlowRow {
            QuickItem("icon/ic_input.svg", "输入文本", modifier = Modifier.clickable {
                showInputTextDialog = true
            })
            QuickItem("icon/ic_cut_screen.svg", "截图保存到桌面", modifier = Modifier.clickable {
                val deviceFile = ADBUtil.screenshot(device.device)
                val localFile = File(FileUtil.getDesktopFile(),deviceFile.split("/").last()).absolutePath
                ADBUtil.pull(device.device,deviceFile,localFile)
                ADBUtil.deleteFile(device.device,deviceFile)
            })
            QuickItem("ic_app_info.svg", "启用ADB Wi-Fi", modifier = Modifier.clickable {
                ADBUtil.enableAdbWifi(device.device)
            })
            QuickItem("ic_app_info.svg", if(showBorder) "隐藏布局边界" else "显示布局边界", modifier = Modifier.clickable {
                ADBUtil.showBorder(device.device,!showBorder)
                showBorder = ADBUtil.isShowBorder(device.device)
            })
            QuickItem("ic_app_info.svg", if(showTouchLocations) "隐藏指针位置" else "显示指针位置", modifier = Modifier.clickable {
                ADBUtil.showTouchLocations(device.device,!showTouchLocations)
                showTouchLocations = ADBUtil.isShowTouchLocations(device.device)
            })
            QuickItem("ic_app_info.svg", if(showHWUIProfile) "隐藏GPU呈现模式" else "显示GPU呈现模式", modifier = Modifier.clickable {
                ADBUtil.showHWUIProfile(device.device,!showHWUIProfile)
                showHWUIProfile = ADBUtil.isShowHWUIProfile(device.device)
            })
        }
    }

    if(showInputTextDialog) {
        InputDialog("请输入", "", {
            showInputTextDialog = false
        }, {
            showInputTextDialog = false
            it?.let {
                ADBUtil.inputText(device.device,it)
            }
        })
    }
}

/**
 * 系统相关
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutSystem(device: DeviceInfo) {
    BaseQuick("系统相关", color = Color(255, 193, 7)) {
        FlowRow {
            QuickItem("icon/ic_reboot.svg", "重启手机", modifier = Modifier.clickable { ADBUtil.reboot(device.device,ADBUtil.RebootType.SYSTEM) })
            QuickItem("icon/ic_recover.svg", "重启到Recover", modifier = Modifier.clickable { ADBUtil.reboot(device.device,ADBUtil.RebootType.RECOVER) })
            QuickItem("icon/ic_fastboot.svg", "重启到Fastboot", modifier = Modifier.clickable { ADBUtil.reboot(device.device,ADBUtil.RebootType.FASTBOOT) })
            QuickItem("icon/ic_fastboot.svg", "重启到Bootloader", modifier = Modifier.clickable { ADBUtil.reboot(device.device,ADBUtil.RebootType.BOOTLOADER) })
        }
    }
}

/**
 * 按键相关
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutKeyBoard(device: DeviceInfo) {
    BaseQuick("按键相关", color = Color(158, 176, 184)) {
        FlowRow {
            QuickItem("icon/ic_home.svg", "Home键", modifier = Modifier.clickable { ADBUtil.inputKey(device.device,"3") })
            QuickItem("icon/ic_back.svg", "Back键", modifier = Modifier.clickable { ADBUtil.inputKey(device.device,"4") })
            QuickItem("icon/ic_menu.svg", "Menu键", modifier = Modifier.clickable { ADBUtil.inputKey(device.device,"82") })
            QuickItem("icon/ic_power.svg", "Power键", modifier = Modifier.clickable { ADBUtil.inputKey(device.device,"26") })
            QuickItem("icon/ic_volume_add.svg", "增加音量", modifier = Modifier.clickable { ADBUtil.inputKey(device.device,"24") })
            QuickItem("icon/ic_volume_sub.svg", "降低音量", modifier = Modifier.clickable { ADBUtil.inputKey(device.device,"25") })
            QuickItem("icon/ic_volume_close.svg", "静音", modifier = Modifier.clickable { ADBUtil.inputKey(device.device,"164") })
            QuickItem("icon/ic_switch.svg", "切换应用", modifier = Modifier.clickable { ADBUtil.inputKey(device.device,"187") })
            QuickItem("icon/ic_top.svg", "向上滑动", modifier = Modifier.clickable { ADBUtil.inputSwipe(device.device,"300","800","300","200") })
            QuickItem("icon/ic_down.svg", "向下滑动", modifier = Modifier.clickable { ADBUtil.inputSwipe(device.device,"300","200","300","800") })
            QuickItem("icon/ic_left.svg", "向左滑动", modifier = Modifier.clickable { ADBUtil.inputSwipe(device.device,"800","300","200","300") })
            QuickItem("icon/ic_right.svg", "向右滑动", modifier = Modifier.clickable { ADBUtil.inputSwipe(device.device,"200","300","800","300") })
        }
    }
}

@Composable
private fun BaseQuick(title: String, color: Color = Color.Transparent, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.background(Color.White).fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp).height(16.dp).clip(RoundedCornerShape(8.dp)).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title)
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun QuickItem(ttf: String? = null, title: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(10.dp).width(130.dp).height(120.dp).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (ttf == null || title == null) return@Column
        Box(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(randomColor.random())) {
            Image(
                painter = painterResource(ttf),
                contentDescription = title,
                modifier = Modifier.padding(16.dp).size(38.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 12.sp)
    }
}