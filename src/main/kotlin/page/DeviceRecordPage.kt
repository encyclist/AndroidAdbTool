package page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bean.DeviceInfo
import config.DeviceRecordUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.ADBUtil

/**
 * @auth 二宁
 * @date 2023/11/24
 */
@Composable
fun DeviceRecordPage(refreshConnectedDevicesList: () -> Unit, connectedDevicesList: SnapshotStateList<DeviceInfo>) {
    var refresh by remember { mutableStateOf(0) }
    val deviceList = remember { mutableStateListOf<DeviceInfo>() }

    LaunchedEffect(refresh) {
        withContext(Dispatchers.IO){
            var savedList = DeviceRecordUtil.getDeviceList()
            savedList.forEach { saved ->
                val connected = connectedDevicesList.find { it.device == saved.device }
                saved.connected = connected != null
            }
            savedList = savedList.sortedBy { it.device }
            withContext(Dispatchers.Default) {
                deviceList.clear()
                deviceList.addAll(savedList)
            }
        }
    }
    LazyColumn(modifier = Modifier.padding(10.dp).fillMaxHeight()) {
        items(deviceList.size) {
            val item = deviceList[it]
            Column(modifier = Modifier.background(Color.White)) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    text = "设备ID：${item.device}    设备名称：${item.deviceName}    设备型号：${item.deviceModel}    设备IP：${item.ip}"
                )
                Row {
                    if (item.connected && item.isWifiConnect()){
                        Button(onClick = {
                            ADBUtil.disconnectDevice(item.device)
                            refreshConnectedDevicesList()
                            refresh++
                        }, modifier = Modifier.padding(horizontal = 10.dp)) {
                            Text("WIFI断开")
                        }
                    }else{
                        Button(onClick = {
                            if(!item.isWifiConnect()){
                                ADBUtil.openWIFIConnect(item.device)
                            }
                            ADBUtil.connectDevice(item.device)
                            refreshConnectedDevicesList()
                            refresh++
                        }, modifier = Modifier.padding(horizontal = 10.dp)) {
                            Text("WIFI连接")
                        }
                    }
                    Button(onClick = {
                        DeviceRecordUtil.deleteDevice(item)
                        refreshConnectedDevicesList()
                        refresh++
                    }, modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text("删除")
                    }
                }
            }
            Box(modifier = Modifier.height(10.dp))
        }
    }
}