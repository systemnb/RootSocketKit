package com.ly

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.ly.ui.theme.LyTheme
import androidx.core.net.toUri
import com.ly.service.Native
import java.io.DataOutputStream
import java.io.File

class MainActivity : ComponentActivity() {
    private val REQUEST_CODE = 1000
    private var showPermissionDialog by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LyTheme {
                var showDialog by remember { mutableStateOf(true) }
                var initializationText by remember { mutableStateOf("正在初始化...") }
                var currentPageIndex by remember { mutableStateOf(2) }
                if (showPermissionDialog) {
                    PermissionDialog(
                        onDismiss = { showPermissionDialog = false },
                        onConfirm = {
                            showPermissionDialog = false
                            val intent = Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:$packageName".toUri()
                            )
                            startActivity(intent)
                        }
                    )
                }
                LaunchedEffect(Unit) {
                    stopService()
                    initializationText = "正在申请权限..."
                    permissionRequest()
                    initializationText = "正在加载资源..."
                    CopyFile(this@MainActivity)
                    initializationText = "正在启动服务..."
                    LoadService()
                    initializationText = "加载完成"
                    showDialog = false // 关闭弹窗
                }
                if (showDialog) {
                    DialogLoading(initializationText)
                }
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    // 底部导航栏： 功能 授权 主页 日记 我的
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentPageIndex == 0,
                            onClick = { currentPageIndex = 0 },
                            icon = { Icon(Icons.Default.Menu, contentDescription = "功能") },
                            label = { Text("功能") },
                            alwaysShowLabel = false
                        )
                        NavigationBarItem(
                            selected = currentPageIndex == 1,
                            onClick = { currentPageIndex = 1 },
                            icon = {Icon(painter = painterResource(id = R.drawable.baseline_security_24), contentDescription = "授权")},
                            label = { Text("授权") },
                            alwaysShowLabel = false
                        )
                        NavigationBarItem(
                            selected = currentPageIndex == 2,
                            onClick = { currentPageIndex = 2 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "主页") },
                            label = { Text("主页") },
                            alwaysShowLabel = false
                        )
                        NavigationBarItem(
                            selected = currentPageIndex == 3,
                            onClick = { currentPageIndex = 3 },
                            icon = { Icon(painter = painterResource(R.drawable.baseline_assignment_24), contentDescription = "日记") },
                            label = { Text("日记") },
                            alwaysShowLabel = false
                        )
                        NavigationBarItem(
                            selected = currentPageIndex == 4,
                            onClick = { currentPageIndex = 4 },
                            icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                            label = { Text("我的") },
                            alwaysShowLabel = false
                        )
                    }
                }) { innerPadding ->
                    when (currentPageIndex) {
                        0 -> FunctionPage(Modifier.padding(innerPadding))
                        1 -> AuthorizationPage(Modifier.padding(innerPadding))
                        2 -> HomePage(Modifier.padding(innerPadding))
                        3 -> LoggingPage(Modifier.padding(innerPadding))
                        4 -> AccountPage(Modifier.padding(innerPadding))
                        else -> HomePage(Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        if (requestCode == REQUEST_CODE) {
            var isAllGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false
                    break
                }
            }
            if (!isAllGranted)
                showPermissionDialog = true
        }
    }

    fun permissionRequest() {
        val permissions: MutableList<String> = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else { // Android 12及以下
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissions(this, permissions.toTypedArray(), REQUEST_CODE)
        }
    }

    @Composable
    fun PermissionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("权限申请失败") },
            text = { Text("请授予相关权限，否则部分功能可能无法正常使用") },
            confirmButton = {
                TextButton(onClick = onConfirm) { Text("设置") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        )
    }

    @Composable
    fun DialogLoading(message: String = "正在加载...") {
        Dialog(onDismissRequest = { }) {
            Card(
                modifier = Modifier
                    .height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }

    fun CopyFile(context: Context) {
        try {
            //将ly_service和driver.ko文件复制到/data/data/com.ly/files目录下
            var file = context.assets.open("ly_service")
            var out = context.openFileOutput("ly_service", Context.MODE_PRIVATE)
            val buffer = ByteArray(1024)
            var len: Int
            while (file.read(buffer).also { len = it } > 0) {
                out.write(buffer, 0, len)
            }
            file.close()
            out.close()
            file = context.assets.open("driver.ko")
            out = context.openFileOutput("driver.ko", Context.MODE_PRIVATE)
            while (file.read(buffer).also { len = it } > 0) {
                out.write(buffer, 0, len)
            }
            file.close()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //使用root权限将ly_service和driver.ko文件复制到/data/ly目录下
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("mkdir -p /data/ly\n")
            os.writeBytes("chmod 770 /data/ly\n")
            os.writeBytes("cp -frL /data/data/com.ly/files/ly_service /data/ly/ly_service\n")
            os.writeBytes("cp -frL /data/data/com.ly/files/driver.ko /data/ly/driver.ko\n")
            os.writeBytes("chmod 770 /data/ly/*\n")
            os.writeBytes("exit\n")
            os.flush()
            os.close()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun LoadService() {
        try {
            Runtime.getRuntime().exec("su -c insmod /data/ly/driver.ko").waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isServiceRunning()) {
            return
        }

        Thread {
            try {
                Runtime.getRuntime().exec("su -c /data/ly/ly_service").waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    fun isServiceRunning(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c 'pgrep ly_service'")
            process.waitFor()
            process.inputStream.bufferedReader().readLine() != null
        } catch (e: Exception) {
            false
        }
    }

    fun stopService() {
        Native.Exit()
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("pkill ly_service\n")
            os.writeBytes("pkill /data/ly/ly_service\n")
            os.writeBytes("rmmod driver.ko\n")
            os.writeBytes("exit\n")
            os.flush()
            os.close()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

