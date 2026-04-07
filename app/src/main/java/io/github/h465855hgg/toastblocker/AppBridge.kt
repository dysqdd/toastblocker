package io.github.h465855hgg.toastblocker

import androidx.annotation.Keep

object AppBridge {

    /**
     * 模块激活状态 - 会被 Xposed 直接修改字段值
     * 注意：必须是 @JvmField 才能被反射修改
     */
    @JvmField
    @Keep
    var isModuleActive: Boolean = false
}