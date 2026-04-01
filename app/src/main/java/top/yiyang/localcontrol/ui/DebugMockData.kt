package top.yiyang.localcontrol.ui

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import top.yiyang.localcontrol.model.AdvancedDeviceConfig
import top.yiyang.localcontrol.model.AlarmConfig
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.model.ProtocolIds
import top.yiyang.localcontrol.model.WakewordConfig
import top.yiyang.localcontrol.model.WallpaperStatus

object DebugMockData {
    fun device(): DeviceSummary = DeviceSummary(
        ipAddress = "198.18.0.88",
        deviceName = "UI 调试设备",
        deviceSn = "DEBUG-UI-001",
        productModel = ProtocolIds.botProModel,
        firmwareVersion = "debug-ui",
        macAddress = "AA:55:CC:10:20:30",
        debugMock = true,
        online = true,
        note = "调试模拟设备",
    )

    fun alarms(): List<AlarmConfig> = listOf(
        AlarmConfig(
            index = 0,
            hour = 7,
            minute = 30,
            label = "上班提醒",
            repeatDays = 62,
            actionType = 1,
            ringtoneType = 0,
            repeatCount = 3,
            enabled = true,
        ),
        AlarmConfig(
            index = 1,
            hour = 22,
            minute = 0,
            label = "睡前模式",
            repeatDays = 127,
            actionType = 4,
            ringtoneType = 1,
            repeatCount = 2,
            enabled = false,
        ),
    )

    fun advancedConfig(): AdvancedDeviceConfig = AdvancedDeviceConfig(
        globalMusic = true,
        fanFunction = true,
        doubleClickMode = 2,
        batteryDetection = true,
        ledNum = 12,
        isCustomEmotion = true,
        theme = 3,
        wallpaperTimeColor = 0x2F6F62,
        wallpaperDateColor = 0x4B7D74,
        wallpaperWakeupColor = 0xEADFCF,
        pcbVersion = 2,
        displayType = 1,
        screenFlip = 0,
    )

    fun wakeword(): WakewordConfig = WakewordConfig(
        word = "xiaoqi",
        display = "小柒",
        threshold = 24,
    )

    fun servoValues(productModel: String): Map<String, Int> {
        return if (productModel == ProtocolIds.waliModel) {
            mapOf(
                "left_arm" to 8,
                "right_arm" to -6,
            )
        } else {
            mapOf(
                "left_front_leg" to 6,
                "right_front_leg" to -4,
                "left_hide_leg" to 3,
                "right_hide_leg" to -2,
            )
        }
    }

    fun wallpaperStatus(): WallpaperStatus = WallpaperStatus(
        hasCustomWallpaper = true,
        fileSize = 32768,
    )

    fun musicRemoteConfig() = buildJsonObject {
        put("type", JsonPrimitive("RANDOM"))
        put("version", JsonPrimitive("1.0.0-debug"))
        put("remark", JsonPrimitive("调试模式设备返回"))
        put("apiUrl", JsonPrimitive("https://debug.local/music"))
        put("fieldMap", buildJsonObject {
            put("songName", JsonPrimitive("title"))
            put("artist", JsonPrimitive("artist"))
            put("audioUrl", JsonPrimitive("url"))
        })
        put("requestParams", buildJsonObject {
            put("keyword", JsonPrimitive("msg"))
            put("index", JsonPrimitive("n"))
        })
    }
}

