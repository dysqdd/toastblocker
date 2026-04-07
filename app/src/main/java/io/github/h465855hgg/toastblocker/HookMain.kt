package io.github.h465855hgg.toastblocker

import android.content.Context
import android.util.Log
import android.widget.Toast
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookMain : IXposedHookLoadPackage {

    companion object {
        const val TAG = "ToastBlocker"

        // 目标应用包名，空集合表示拦截所有应用
        val TARGET_PACKAGES = setOf<String>()
    }
    private fun hookSelf(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // 直接通过反射修改字段值（不是调用方法！）
            val bridgeClass = Class.forName(
                "io.github.h465855hgg.toastblocker.AppBridge",
                true,
                lpparam.classLoader
            )
            val field = bridgeClass.getDeclaredField("isModuleActive")
            field.isAccessible = true
            field.setBoolean(null, true)  // static 字段，obj 传 null

            XposedBridge.log("[$TAG] ✅ 模块激活状态已设置")
        } catch (e: Throwable) {
            XposedBridge.log("[$TAG] ❌ 设置激活状态失败: ${e.message}")
        }
    }
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // ========== 关键：自 Hook，标记模块已激活 ==========
        if (lpparam.packageName == "io.github.h465855hgg.toastblocker") {
            hookSelf(lpparam)
            XposedBridge.log("[$TAG] 模块自身已激活")
        }

        // 检查是否需要处理此应用
        if (TARGET_PACKAGES.isNotEmpty() && lpparam.packageName !in TARGET_PACKAGES) {
            return
        }

        log("========================================")
        log("开始 Hook 应用: ${lpparam.packageName}")
        log("进程名: ${lpparam.processName}")
        log("========================================")

        try {
            hookToastShow(lpparam)
            hookToastMakeText(lpparam)
            hookToastMakeTextWithResId(lpparam)
            log("所有 Hook 点设置完成")
        } catch (e: Exception) {
            logE("Hook 设置失败", e)
        }
    }

    /**
     * Hook Toast.show()
     */
    private fun hookToastShow(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                Toast::class.java,
                "show",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val toast = param.thisObject as Toast
                        val text = getToastTextSafely(toast)

                        log("🚫 拦截 Toast.show() | 内容: $text | 应用: ${lpparam.packageName}")

                        // 安全拦截
                        param.result = null
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        // 确保没有异常
                        if (param.hasThrowable()) {
                            logE("show() 抛出异常", param.throwable)
                            param.throwable = null
                        }
                    }
                }
            )
            log("✅ Toast.show() Hook 成功")
        } catch (e: Exception) {
            logE("❌ Toast.show() Hook 失败", e)
        }
    }

    /**
     * Hook Toast.makeText(Context, CharSequence, int)
     */
    private fun hookToastMakeText(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                Toast::class.java,
                "makeText",
                Context::class.java,
                CharSequence::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val text = param.args[1]?.toString() ?: "[null]"
                        log("🚫 拦截 Toast.makeText() | 内容: $text | 应用: ${lpparam.packageName}")

                        // 返回假 Toast 防止崩溃
                        val fakeToast = createFakeToast(param.args[0] as Context)
                        param.result = fakeToast
                    }
                }
            )
            log("✅ Toast.makeText(CharSequence) Hook 成功")
        } catch (e: Exception) {
            logE("❌ Toast.makeText(CharSequence) Hook 失败", e)
        }
    }

    /**
     * Hook Toast.makeText(Context, int, int)
     */
    private fun hookToastMakeTextWithResId(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                Toast::class.java,
                "makeText",
                Context::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val resId = param.args[1] as Int
                        log("🚫 拦截 Toast.makeText() | 资源ID: $resId | 应用: ${lpparam.packageName}")

                        val fakeToast = createFakeToast(param.args[0] as Context)
                        param.result = fakeToast
                    }
                }
            )
            log("✅ Toast.makeText(resId) Hook 成功")
        } catch (e: Exception) {
            logE("❌ Toast.makeText(resId) Hook 失败", e)
        }
    }

    /**
     * 创建假 Toast
     */
    private fun createFakeToast(context: Context): Toast {
        return object : Toast(context) {
            override fun show() {
                log("FakeToast.show() 被调用，已忽略")
            }

            override fun setText(resId: Int) {
                log("FakeToast.setText(resId: $resId) 被忽略")
            }

            override fun setText(s: CharSequence?) {
                log("FakeToast.setText(s: $s) 被忽略")
            }
        }
    }

    /**
     * 安全获取 Toast 文本
     */
    private fun getToastTextSafely(toast: Toast): String {
        return try {
            val mText = XposedHelpers.getObjectField(toast, "mText")
            if (mText != null) {
                return mText.toString()
            }

            val mNextView = XposedHelpers.getObjectField(toast, "mNextView")
            if (mNextView != null) {
                val view = mNextView as android.view.View
                if (view is android.widget.TextView) {
                    view.text?.toString() ?: "[空TextView]"
                } else {
                    "[非TextView: ${view.javaClass.simpleName}]"
                }
            } else {
                "[无视图]"
            }
        } catch (e: Exception) {
            "[获取失败: ${e.message}]"
        }
    }

    /**
     * 输出到 LSPosed 日志
     */
    private fun log(message: String) {
        // 使用 XposedBridge.log 输出到 LSPosed 日志
        XposedBridge.log("[$TAG] $message")

        // 同时输出到系统日志，方便 adb logcat 查看
        Log.i(TAG, message)
    }

    /**
     * 错误日志到 LSPosed
     */
    private fun logE(message: String, e: Throwable? = null) {
        XposedBridge.log("[$TAG] ❌ ERROR: $message")
        e?.let {
            XposedBridge.log(it)
            Log.e(TAG, message, it)
        }
    }
}