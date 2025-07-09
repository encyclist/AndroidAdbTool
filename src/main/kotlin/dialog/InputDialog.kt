package dialog

import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * @auth 二宁
 * @date 2023/11/3
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InputDialog(title: String, content: String, clickCancel: () -> Unit, clickConfirm: (text:String?) -> Unit) {
    val text = remember { mutableStateOf(content) }
    val hasFocus = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(modifier = Modifier.width(400.dp),
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = {
                    if (hasFocus.value) {
                        focusRequester.freeFocus()
                    }
                    clickConfirm.invoke(text.value)
                },
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if(hasFocus.value) {
                        focusRequester.freeFocus()
                    }
                    clickCancel()
                },
            ) {
                Text("取消")
            }
        },
        title = {
            Text(title)
        }, text = {
            TextField(
                modifier = Modifier.focusRequester(focusRequester).onFocusChanged { hasFocus.value = it.isFocused },
                value = text.value,
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White,textColor=Color.Black),
                onValueChange = {
                    text.value = it
                }
            )
        })

    focusRequester.requestFocus()
}