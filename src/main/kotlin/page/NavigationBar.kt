package page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import bean.DeviceInfo
import bean.MainNav

/**
 * @auth 二宁
 * @date 2023/11/27
 */
@Composable
fun MainNav(modifier: Modifier, refreshConnectedDevicesList:()->Unit, connectedDevicesList:MutableList<DeviceInfo>, onSelectItem: (MainNav) -> Unit, device: (DeviceInfo?) -> Unit) {
    val allNav = MainNav.createMainNavData()
    var selectedNav by remember { mutableStateOf(allNav.first()) }
    NavigationRail(modifier, elevation = 0.dp, header = {
        Spacer(modifier = Modifier.height(12.dp))
        ConnectDevices(refreshConnectedDevicesList,connectedDevicesList,device)
    }) {
        allNav.forEach { item ->
            MainNavItem(selectedNav,item) {
                selectedNav = it
                onSelectItem.invoke(selectedNav)
            }
        }
    }
}

@Composable
private fun MainNavItem(selectedNav:MainNav, mainNavBean:MainNav, onClick: (MainNav) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick.invoke(mainNavBean) }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.width(24.dp).height(24.dp),
            painter = painterResource(mainNavBean.svgName),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = mainNavBean.labelText,
            color = if (selectedNav == mainNavBean) Color(139, 195, 74) else Color.Gray
        )
    }
}

/**
 * 连接设备widget
 */
@Composable
private fun ConnectDevices(refreshConnectedDevicesList:()->Unit,connectedDevicesList:MutableList<DeviceInfo>,deviceCallback: (DeviceInfo?) -> Unit) {
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(refresh) {
        refreshConnectedDevicesList()
    }

    var showDeviceItem by remember { mutableStateOf(false) }
    val size by animateDpAsState(targetValue = if (showDeviceItem) 120.dp else 0.dp)
    // 箭头旋转动画
    val arrowAnim by animateFloatAsState(if (showDeviceItem) -180f else 0f)
    var selectIndexDevice by remember { mutableStateOf(0) }
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        val firstDevice = connectedDevicesList.getOrNull(selectIndexDevice) ?: connectedDevicesList.firstOrNull()
        deviceCallback.invoke(firstDevice)
        val deviceName = if(firstDevice != null) "${firstDevice.deviceName}\n${firstDevice.deviceModel}" else "未连接设备"

        TextButton(
            { showDeviceItem = !showDeviceItem },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(19.dp),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text(deviceName, color = Color.Black)
            Icon(
                modifier = Modifier.graphicsLayer { rotationX = arrowAnim },
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "",
                tint = Color.Black,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AnimatedVisibility(size != 0.dp) {
            LazyColumn {
                items(connectedDevicesList.size) {
                    val device = connectedDevicesList[it]
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectIndexDevice = it
                            showDeviceItem = false
                            deviceCallback.invoke(device)
                        }.padding(vertical = 6.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${device.deviceName}\n${device.deviceModel}",
                            color = if (selectIndexDevice == it) Color(139, 195, 74) else Color.Black
                        )
                        if (deviceName.contains(device.deviceName ?: "") || deviceName.contains(device.deviceModel ?: "")) {
                            Icon(Icons.Default.Check, contentDescription = "", tint = Color(139, 195, 74))
                        }
                    }
                }
            }
        }

        Button(onClick = {
            selectIndexDevice = 0
            refresh++
        }, modifier = Modifier.fillMaxWidth()) {
            Text("刷新Adb Device")
        }
    }
}