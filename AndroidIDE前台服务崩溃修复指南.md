# AndroidIDE 前台服务 (FGS) 崩溃修复指南

## 问题现象

当你的应用在 Android 14 (SDK 34) 或更高版本的设备上运行时，可能会在启动或发生其他异常时意外崩溃。查看崩溃日志，你会发现一个关键的 `SecurityException`，内容如下：

```java
java.lang.SecurityException: Starting FGS with type dataSync ... requires permissions: ... [android.permission.FOREGROUND_SERVICE_DATA_SYNC]
```

这个错误通常与 `com.itsaky.androidide.logsender.LogSenderService` 服务有关。

## 问题原因

这个崩溃**不是你的应用代码的错**，而是由 **AndroidIDE 的一个内置功能**引起的。

AndroidIDE 包含一个名为 `LogSenderService` 的服务，它的作用是在你的应用崩溃时，自动收集日志并将其发送回 AndroidIDE，方便你进行调试。

从 Android 14 (API 级别 34) 开始，Google 对前台服务（Foreground Services, FGS）的管理变得更加严格。现在，如果应用要启动一个前台服务，必须在 `AndroidManifest.xml` 文件中明确声明该服务的**类型**，并申请相应的权限。

`LogSenderService` 作为一个 `dataSync`（数据同步）类型的服务，在启动时需要 `FOREGROUND_SERVICE_DATA_SYNC` 权限。然而，AndroidIDE 在默认生成的项目中没有自动添加这些声明，导致当这个服务尝试启动时，系统因缺少权限和类型声明而抛出 `SecurityException`，最终导致应用崩溃。

## 解决方案

解决方案非常简单，只需要在你的 `app/src/main/AndroidManifest.xml` 文件中手动添加所需的权限和服务声明即可。

1.  **声明前台服务权限**：
    在 `<manifest>` 标签内，`<application>` 标签外，添加以下权限声明：

    ```xml
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
    ```

2.  **声明服务类型**：
    在 `<application>` 标签内，添加 `LogSenderService` 的服务声明，并指定其 `foregroundServiceType`：

    ```xml
    <service
        android:name="com.itsaky.androidide.logsender.LogSenderService"
        android:foregroundServiceType="dataSync" />
    ```

### 完整示例

修改后的 `AndroidManifest.xml` 文件看起来应该像这样：

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- 1. 添加权限 -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

    <application
        android:name=".MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <activity ... >
            ...
        </activity>

        <!-- 2. 添加服务声明和类型 -->
        <service
            android:name="com.itsaky.androidide.logsender.LogSenderService"
            android:foregroundServiceType="dataSync" />
            
    </application>

</manifest>
```

## 总结

这个崩溃是由于 AndroidIDE 的项目模板与 Android 14+ 的系统要求不兼容导致的。通过在 `AndroidManifest.xml` 中手动添加权限和服务声明，就可以轻松解决这个问题，让应用正常运行。
