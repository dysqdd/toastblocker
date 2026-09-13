package io.github.h465855hgg.toastblocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.h465855hgg.toastblocker.ui.theme.ToastBlockerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 恢复/同步隐藏图标偏好，保证组件状态与设置一致
        LauncherIcon.sync(this)
        enableEdgeToEdge()
        setContent {
            ToastBlockerTheme {
                App()
            }
        }
    }
}
