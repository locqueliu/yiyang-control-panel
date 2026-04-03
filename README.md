# Yiyang Control Panel

Yiyang Control Panel 是一个面向 ZZPET / ESP32 桌宠设备的 Android 本地控制 App，目标是把原本分散在网页端的设备发现、控制、调试与音乐配置能力整合成一个适合手机直接使用的原生工具。

## 项目定位

- 面向局域网内的设备发现、连接、控制与配置
- 不依赖账号体系，不依赖云端后台
- 以设备直连为主，不包含远程云控能力
- 当前版本更接近可运行的首版工程，而不是完全稳定的商用发布版

## 已实现内容

- 局域网扫描设备，并缓存最近发现和最近选择的设备
- 支持通过 IP 地址或 MAC 地址补充连接
- 提供设备页、控制页、调试页、音乐配置页四个主入口
- 支持方向控制、动作控制、灯光控制、系统控制和音量调节
- 提供设备调试与高级配置相关界面
- 提供音乐接口配置的本地保存、导入导出和同步入口

## 技术栈

- Kotlin 2.2.0
- Jetpack Compose
- Android Gradle Plugin 8.9.2
- Gradle 8.11.1
- Android Min SDK 26
- Android Compile SDK 36
- Android Target SDK 35
- OkHttp
- Kotlin Serialization

## 运行环境

- Android Studio 最新稳定版
- JDK 17
- Android SDK
- 支持 Gradle Wrapper 的本地开发环境

## 快速开始

### 1. 获取项目

如果你是从 GitHub 拉取项目：

```powershell
git clone https://github.com/locqueliu/yiyang-control-panel.git
cd yiyang-control-panel
```

如果你当前已经在本机目录中，可以直接进入：

```powershell
cd C:\Users\Locse\Desktop\yiyang-control-panel
```

### 2. 配置 Android SDK

如果本机没有配置全局 Android SDK 环境变量，请在项目根目录创建 `local.properties`：

```properties
sdk.dir=C:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

### 3. 构建调试包

```powershell
.\gradlew.bat assembleDebug
```

### 4. 在 Android Studio 中运行

- 直接使用 Android Studio 打开项目根目录 `C:\Users\Locse\Desktop\yiyang-control-panel`
- 等待 Gradle Sync 完成
- 连接模拟器或真机后运行 `app` 模块

## 项目结构

```text
yiyang-control-panel/
├── app/                         Android 应用源码
├── gradle/                      Gradle Wrapper 配置
├── Product-Spec.md              产品需求文档
├── Product-Spec-CHANGELOG.md    需求变更记录
├── CHANGELOG.md                 项目变更日志
├── CONTRIBUTING.md              贡献说明
├── SECURITY.md                  安全说明
├── CODE_OF_CONDUCT.md           社区行为准则
└── .github/                     Issue / PR 模板
```

## 开发说明

- 应用入口位于 `app/src/main/java/top/yiyang/localcontrol/MainActivity.kt`
- UI 主要基于 Jetpack Compose 构建
- 当前工程以本地控制与调试流程打通为优先目标
- 项目中已经整理了产品文档，适合继续按需求文档推进迭代

## 当前限制

- 当前仓库未包含自动化测试
- 设备能力与接口兼容性依赖具体固件版本
- 调试页包含高风险配置入口，使用前应确认目标设备与固件能力
- 目前默认面向局域网使用场景，不提供公网远程控制能力

## 文档导航

- [Product-Spec.md](./Product-Spec.md)
- [Product-Spec-CHANGELOG.md](./Product-Spec-CHANGELOG.md)
- [CHANGELOG.md](./CHANGELOG.md)
- [CONTRIBUTING.md](./CONTRIBUTING.md)
- [SECURITY.md](./SECURITY.md)
- [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)

## 贡献方式

欢迎提交 Issue 和 Pull Request。在发起较大改动前，建议先阅读 [CONTRIBUTING.md](./CONTRIBUTING.md)。

## 安全与责任说明

- 请不要在公开 Issue 中直接贴出设备局域网 IP、密钥、私有接口地址或账号信息
- 如果你发现安全问题，请按 [SECURITY.md](./SECURITY.md) 中的方式处理
- 本项目当前不承诺适配所有 ZZPET / ESP32 设备分支与固件变种，接入前请自行验证

## License

本项目使用 [MIT License](./LICENSE)。
