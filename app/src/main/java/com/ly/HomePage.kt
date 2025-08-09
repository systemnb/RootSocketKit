package com.ly

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ly.service.Native
import com.ly.ui.theme.LyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    var context = LocalContext.current

    LyTheme {
        var pid by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var size by remember { mutableStateOf("") }
        Scaffold(modifier = modifier.fillMaxSize()) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                //一个输入框
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = pid,
                    onValueChange = { pid = it },
                    label = { Text("pid") },
                    placeholder = { Text("请输入pid") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (pid.isNotEmpty()) {
                            IconButton(onClick = { pid = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = null)
                            }
                        }
                    },
                    supportingText = { Text("支持输入数字") }
                )
                //一个按钮
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (pid.isEmpty()) {
                            Toast.makeText(context, "请输入pid", Toast.LENGTH_SHORT).show()
                        } else {
                            val result = Native.OpenProcess(pid.toInt())
                            if (result.status == 0.toLong()) {
                                Toast.makeText(
                                    context,
                                    "获取句柄成功, HANDLE=${result.data}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "获取句柄失败, STATUS=${result.status}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                ) {
                    Text("获取句柄")
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("地址") },
                    placeholder = { Text("请输入地址") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Filled.Clear, contentDescription = null) },
                    supportingText = { Text("支持输入数字") }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("大小") },
                    placeholder = { Text("请输入大小") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Filled.Clear, contentDescription = null) },
                    supportingText = { Text("支持输入数字") }
                )
                var isLoading by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (address.isEmpty() || size.isEmpty()) {
                            Toast.makeText(context, "请输入地址或大小", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val openResult = Native.OpenProcess(pid.toInt())
                                if (openResult.status == 0L) {
                                    val result = Native.ReadMemory(
                                        openResult.data,
                                        address.toLong(16),
                                        size.toLong(),
                                        true
                                    )

                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        if (result.status == 0L) {
                                            Toast.makeText(
                                                context,
                                                "读取内存成功",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.d("ly", "读取内存成功, DATA=${result.data_buf?.contentToString()}")
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "读取内存失败, STATUS=${result.status}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "打开进程失败, STATUS=${openResult.status}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("读取内存")
                }

                if (isLoading) {
                    Dialog(onDismissRequest = {}) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    HomePage()
}