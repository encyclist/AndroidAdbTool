package dialog

import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @auth 二宁
 * @date 2023/11/3
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessageDialog(title: String, content: String, clickCancel: () -> Unit, clickConfirm: () -> Unit) {
    AlertDialog(modifier = Modifier.width(300.dp),
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = clickConfirm,
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = clickCancel,
            ) {
                Text("取消")
            }
        },
        title = {
            Text(title)
        }, text = {
            Text(content)
        })
}