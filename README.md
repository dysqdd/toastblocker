# ToastBlocker

一个基于 [LSPosed](https://github.com/LSPosed/LSPosed) / Xposed 框架的 Android 模块，用于拦截并屏蔽系统及第三方应用弹出的 **Toast 轻提示**，让界面更清爽。

## ✨ 功能特性

- 🚫 **全局拦截 Toast**：Hook `Toast.show()` 与 `Toast.makeText()`，屏蔽所有应用的 Toast 弹窗
- 🎯 **可选作用域**：默认拦截所有应用，可配置为只拦截指定包名
- 📱 **简洁的 Compose 界面**：实时显示模块激活状态
- 👻 **隐藏桌面图标**：可在应用内一键隐藏图标，通过 adb 广播或 LSPosed 管理器恢复

## 📋 使用要求

- 已安装并激活 **LSPosed**（或 EdXposed）框架
- Android 7.0（API 24）及以上

## 📦 安装与激活

1. 从 [Releases](../../releases) 下载最新 APK 并安装
2. 打开 LSPosed 管理器，在「模块」中启用 ToastBlocker
3. 在作用域中勾选需要拦截的应用（建议勾选「系统框架」以覆盖系统 Toast）
4. 重启设备或重启目标应用进程使其生效

## 🎯 自定义拦截范围

默认 `TARGET_PACKAGES` 为空集合，即拦截**所有**应用的 Toast。

如需只拦截特定应用，编辑 `app/src/main/java/io/github/h465855hgg/toastblocker/HookMain.kt`：

```kotlin
val TARGET_PACKAGES = setOf("com.example.app1", "com.example.app2")
```

## 👻 隐藏桌面图标

在应用内「设置 → 隐藏桌面图标」打开开关即可让图标从桌面消失。

图标隐藏后的恢复方式（任选其一）：

- 通过 LSPosed 管理器重新打开本应用
- 使用 adb 广播：

```bash
adb shell am broadcast -a io.github.h465855hgg.toastblocker.SHOW_ICON
```

## 🔨 构建

1. 用 [Android Studio](https://developer.android.com/studio) 打开项目
2. 等待 Gradle 同步完成（Xposed API 为 `compileOnly`，无需框架即可编译）
3. 执行 `Build > Build App Bundle(s) / APK(s) > Build APK(s)`

或使用命令行：

```bash
./gradlew assembleRelease
```

## 🧩 实现原理

- 通过 Xposed 框架 Hook Android 框架层的 `android.widget.Toast` 类
- 拦截 `Toast.show()` 与两个重载的 `Toast.makeText()`，返回一个无操作的假 Toast，避免调用方因空值崩溃
- 模块自 Hook 自身以标记激活状态，供 Compose 界面实时展示
- 桌面图标通过 `activity-alias` 实现，运行时用 `PackageManager` 动态启用/禁用

## 📄 许可

[MIT License](LICENSE)
