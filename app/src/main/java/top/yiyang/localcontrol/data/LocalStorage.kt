package top.yiyang.localcontrol.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.model.MusicApiConfig

class LocalStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("yiyang_local_control", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun loadDevices(): List<DeviceSummary> = decodeList("devices", DeviceSummary.serializer())

    fun saveDevices(devices: List<DeviceSummary>) {
        prefs.edit().putString(
            "devices",
            json.encodeToString(ListSerializer(DeviceSummary.serializer()), devices),
        ).apply()
    }

    fun loadSelectedDeviceIp(): String? = prefs.getString("selected_device_ip", null)

    fun saveSelectedDeviceIp(ipAddress: String?) {
        prefs.edit().putString("selected_device_ip", ipAddress).apply()
    }

    fun loadMusicConfigs(): List<MusicApiConfig> = decodeList("music_configs", MusicApiConfig.serializer())

    fun saveMusicConfigs(configs: List<MusicApiConfig>) {
        prefs.edit().putString(
            "music_configs",
            json.encodeToString(ListSerializer(MusicApiConfig.serializer()), configs),
        ).apply()
    }

    private fun <T> decodeList(
        key: String,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): List<T> {
        val raw = prefs.getString(key, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(serializer), raw)
        }.getOrDefault(emptyList())
    }
}


