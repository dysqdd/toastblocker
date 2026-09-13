package io.github.h465855hgg.toastblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 图标隐藏后的恢复入口。
 *
 * 使用 adb 广播即可恢复图标：
 *   adb shell am broadcast -a io.github.h465855hgg.toastblocker.SHOW_ICON
 */
class ShowIconReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_SHOW_ICON) {
            LauncherIcon.setHidden(context, false)
        }
    }

    companion object {
        const val ACTION_SHOW_ICON = "io.github.h465855hgg.toastblocker.SHOW_ICON"
    }
}
