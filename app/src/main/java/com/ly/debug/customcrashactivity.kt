package com.ly.debug

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ly.ui.theme.LyTheme

//显示崩溃信息的Activity
class customcrashactivity: ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stackTrace = intent.getStringExtra("stackTrace")
        enableEdgeToEdge()
        setContent {
            LyTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    TopAppBar(modifier = Modifier.padding(16.dp), title = { Text(text = "Crash Info") })
                }) { padding ->
                    Column(modifier = Modifier.padding(padding).fillMaxSize().padding(top = 56.dp)) {
                        stackTrace?.let { Text(text = it, Modifier.fillMaxSize().padding(top = 56.dp)) }
                    }
                }
            }
        }
    }
}