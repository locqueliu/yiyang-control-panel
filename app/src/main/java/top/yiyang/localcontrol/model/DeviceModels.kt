package top.yiyang.localcontrol.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceSummary(
    val ipAddress: String,
    val deviceName: String = "",
    val deviceSn: String = "",
    val productModel: String = "",
    val firmwareVersion: String = "",
    val macAddress: String = "",
    val debugMock: Boolean = false,
    val online: Boolean = true,
    val note: String = "",
    val lastSeenAt: Long = System.currentTimeMillis(),
) {
    val title: String
        get() = deviceName
            .ifBlank { deviceSn.ifBlank { ipAddress } }
            .replace(ProtocolIds.legacyBrandUpper, "YIYANG")
            .replace(ProtocolIds.legacyBrandTitle, "Yiyang")
            .replace(ProtocolIds.legacyBrandLower, "yiyang")
}

@Serializable
data class AdvancedDeviceConfig(
    val globalMusic: Boolean = true,
    val fanFunction: Boolean = false,
    val doubleClickMode: Int = 0,
    val batteryDetection: Boolean = true,
    val ledNum: Int = 4,
    val isCustomEmotion: Boolean = false,
    val theme: Int = 0,
    val wallpaperTimeColor: Int = 0x258026,
    val wallpaperDateColor: Int = 0x258026,
    val wallpaperWakeupColor: Int = 0xFDF5E6,
    val pcbVersion: Int = 2,
    val displayType: Int = 0,
    val screenFlip: Int = 0,
)

@Serializable
data class WakewordConfig(
    val word: String = "",
    val display: String = "",
    val threshold: Int = 20,
)

@Serializable
data class WallpaperStatus(
    val hasCustomWallpaper: Boolean = false,
    val fileSize: Long = 0,
)

