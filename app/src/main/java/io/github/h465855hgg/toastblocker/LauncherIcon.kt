package io.github.h465855hgg.toastblocker

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * 控制桌面图标的显示与隐藏。
 *
 * 原理：真正的启动入口是 AndroidManifest 中名为 MainActivityLauncher 的 activity-alias，
 * 通过 setComponentEnabledSetting 禁用该 alias 即可让图标从桌面消失，启用即可恢复。
 */
object LauncherIcon {

    private const val PREFS_NAME = "settings"
    private const val KEY_HIDDEN = "hide_launcher_icon"

    // 必须与 AndroidManifest.xml 中的 activity-alias 完全一致
    private const val ALIAS = "io.github.h465855hgg.toastblocker.MainActivityLauncher"

    /** 读取当前是否隐藏图标 */
    fun isHidden(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HIDDEN, false)

    /** 设置隐藏状态，并持久化 */
    fun setHidden(context: Context, hidden: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HIDDEN, hidden)
            .apply()
        apply(context, hidden)
    }

    /** 将持久化的偏好同步到系统组件状态（应用启动时调用，保证状态一致） */
    fun sync(context: Context) {
        apply(context, isHidden(context))
    }

    private fun apply(context: Context, hidden: Boolean) {
        val pm = context.packageManager
        val component = ComponentName(context.packageName, ALIAS)
        val state = if (hidden) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        pm.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP)
    }
}
