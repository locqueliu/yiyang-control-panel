package top.yiyang.localcontrol.data

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import top.yiyang.localcontrol.model.AdvancedDeviceConfig
import top.yiyang.localcontrol.model.AlarmConfig
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.model.LampState
import top.yiyang.localcontrol.model.WakewordConfig
import top.yiyang.localcontrol.model.WallpaperStatus

class DeviceApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(1200, TimeUnit.MILLISECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun discoverDevice(ipAddress: String): DeviceSummary? = withContext(Dispatchers.IO) {
        val response = requestJsonObject(
            "http://$ipAddress/api/discover",
            "GET",
            timeoutSeconds = 1L,
        ) ?: return@withContext null

        val success = response.booleanOrDefault(true, "success")
        if (!success && response.isEmpty()) return@withContext null

        DeviceSummary(
            ipAddress = ipAddress,
            deviceName = response.stringOrEmpty("deviceName", "name"),
            deviceSn = response.stringOrEmpty("deviceSn", "sn", "serialNumber"),
            productModel = response.stringOrEmpty("productModel", "model"),
            firmwareVersion = response.stringOrEmpty("firmwareVersion", "version"),
            macAddress = response.stringOrEmpty("macAddress", "mac", "deviceMac", "mac_addr"),
            online = true,
        )
    }

    suspend fun startDirectionalAction(ipAddress: String, actionType: Int, speed: Int) {
        postJson(
            url = "http://$ipAddress/api/pet/action/start",
            body = buildJsonObject {
                put("action_type", JsonPrimitive(actionType))
                put("speed", JsonPrimitive(speed))
            },
        )
    }

    suspend fun stopDirectionalAction(ipAddress: String) {
        postJson(url = "http://$ipAddress/api/pet/action/stop", body = buildJsonObject { })
    }

    suspend fun triggerAction(ipAddress: String, actionType: Int, params: JsonObject = buildJsonObject { }) {
        val payload = buildJsonObject {
            put("action_type", JsonPrimitive(actionType))
            params.forEach { (key, value) -> put(key, value) }
        }
        postJson("http://$ipAddress/api/pet/action", payload)
    }

    suspend fun setLampState(ipAddress: String, lampState: LampState) {
        postJson(
            "http://$ipAddress/api/lamp/control",
            buildJsonObject {
                put("action", JsonPrimitive(lampState.action))
                put("speed", JsonPrimitive(lampState.speed))
                put("brightness", JsonPrimitive(lampState.brightness))
                put("r", JsonPrimitive(lampState.red))
                put("g", JsonPrimitive(lampState.green))
                put("b", JsonPrimitive(lampState.blue))
            },
        )
    }

    suspend fun rebootDevice(ipAddress: String) {
        postJson("http://$ipAddress/api/system/restart", buildJsonObject { })
    }

    suspend fun enterWifiConfig(ipAddress: String) {
        postJson("http://$ipAddress/api/system/wifi-config", buildJsonObject { })
    }

    suspend fun getVolume(ipAddress: String): Int {
        val response = requestJsonObject("http://$ipAddress/api/volume", "GET")
            ?: throw IllegalStateException("设备未返回音量信息")
        return response["data"]?.jsonObject?.intOrDefault(70, "volume")
            ?: response.intOrDefault(70, "volume")
    }

    suspend fun setVolume(ipAddress: String, volume: Int) {
        postJson(
            "http://$ipAddress/api/volume",
            buildJsonObject { put("volume", JsonPrimitive(volume)) },
        )
    }

    suspend fun getAlarms(ipAddress: String): List<AlarmConfig> {
        val response = requestJsonObject("http://$ipAddress/api/alarm/list", "GET")
            ?: return emptyList()
        val payload = response["data"] ?: response["alarms"] ?: buildJsonArray { }
        return payload.toAlarmList()
    }

    suspend fun saveAlarm(ipAddress: String, alarm: AlarmConfig) {
        val payload = buildJsonObject {
            put("hour", JsonPrimitive(alarm.hour))
            put("minute", JsonPrimitive(alarm.minute))
            put("label", JsonPrimitive(alarm.label))
            put("repeat_days", JsonPrimitive(alarm.repeatDays))
            put("action_type", JsonPrimitive(alarm.actionType))
            put("ringtone_type", JsonPrimitive(alarm.ringtoneType))
            put("repeat_count", JsonPrimitive(alarm.repeatCount))
        }
        if (alarm.index == null) {
            postJson("http://$ipAddress/api/alarm/add", payload)
        } else {
            requestJsonObject(
                "http://$ipAddress/api/alarm/update/${alarm.index}",
                "PUT",
                payload,
            ) ?: throw IllegalStateException("更新闹钟失败")
        }
    }

    suspend fun deleteAlarm(ipAddress: String, index: Int) {
        requestJsonObject("http://$ipAddress/api/alarm/delete/$index", "DELETE")
            ?: throw IllegalStateException("删除闹钟失败")
    }

    suspend fun toggleAlarm(ipAddress: String, index: Int, enabled: Boolean) {
        postJson(
            "http://$ipAddress/api/alarm/toggle/$index",
            buildJsonObject { put("enabled", JsonPrimitive(enabled)) },
        )
    }

    suspend fun getServoTrims(ipAddress: String): Map<String, Int> {
        val response = requestJsonObject("http://$ipAddress/api/pet/servo/trim", "GET")
            ?: return emptyMap()
        val trimObject = response["trims"]?.jsonObject ?: response["data"]?.jsonObject ?: response
        return trimObject.mapValues { (_, value) -> value.jsonPrimitive.intOrNull ?: 0 }
    }

    suspend fun saveServoTrim(ipAddress: String, servoType: String, trimValue: Int) {
        postJson(
            "http://$ipAddress/api/pet/servo/trim",
            buildJsonObject {
                put("servo_type", JsonPrimitive(servoType))
                put("trim_value", JsonPrimitive(trimValue))
            },
        )
    }

    suspend fun getWakewordConfig(ipAddress: String): WakewordConfig {
        val response = requestJsonObject("http://$ipAddress/api/wakeword", "GET")
            ?: return WakewordConfig()
        val config = response["data"]?.jsonObject ?: response
        return WakewordConfig(
            word = config.stringOrEmpty("word"),
            display = config.stringOrEmpty("display"),
            threshold = config.intOrDefault(20, "threshold"),
        )
    }

    suspend fun saveWakewordConfig(ipAddress: String, config: WakewordConfig) {
        postJson(
            "http://$ipAddress/api/wakeword",
            buildJsonObject {
                put("word", JsonPrimitive(config.word))
                put("display", JsonPrimitive(config.display))
                put("threshold", JsonPrimitive(config.threshold))
            },
        )
    }

    suspend fun getAdvancedConfig(ipAddress: String): AdvancedDeviceConfig {
        val response = requestJsonObject("http://$ipAddress/api/config", "GET")
            ?: return AdvancedDeviceConfig()
        val config = response["data"]?.jsonObject ?: response
        return AdvancedDeviceConfig(
            globalMusic = config.booleanOrDefault(true, "global_music"),
            fanFunction = config.booleanOrDefault(false, "fan_function"),
            doubleClickMode = config.intOrDefault(0, "double_click_mode"),
            batteryDetection = config.booleanOrDefault(true, "battery_detection"),
            ledNum = config.intOrDefault(4, "led_num"),
            isCustomEmotion = config.booleanOrDefault(false, "is_custom_emotion"),
            theme = config.intOrDefault(0, "theme"),
            wallpaperTimeColor = config.intOrDefault(0x258026, "wallpaper_time_color"),
            wallpaperDateColor = config.intOrDefault(0x258026, "wallpaper_date_color"),
            wallpaperWakeupColor = config.intOrDefault(0xFDF5E6, "wallpaper_wakeup_color"),
            pcbVersion = config.intOrDefault(2, "pcb_version"),
            displayType = config.intOrDefault(0, "display_type"),
            screenFlip = config.intOrDefault(0, "screen_flip"),
        )
    }

    suspend fun saveAdvancedConfig(ipAddress: String, config: AdvancedDeviceConfig) {
        postJson(
            "http://$ipAddress/api/config",
            buildJsonObject {
                put("global_music", JsonPrimitive(config.globalMusic))
                put("fan_function", JsonPrimitive(config.fanFunction))
                put("double_click_mode", JsonPrimitive(config.doubleClickMode))
                put("battery_detection", JsonPrimitive(config.batteryDetection))
                put("led_num", JsonPrimitive(config.ledNum))
                put("is_custom_emotion", JsonPrimitive(config.isCustomEmotion))
                put("theme", JsonPrimitive(config.theme))
                put("wallpaper_time_color", JsonPrimitive(config.wallpaperTimeColor))
                put("wallpaper_date_color", JsonPrimitive(config.wallpaperDateColor))
                put("wallpaper_wakeup_color", JsonPrimitive(config.wallpaperWakeupColor))
                put("pcb_version", JsonPrimitive(config.pcbVersion))
                put("display_type", JsonPrimitive(config.displayType))
                put("screen_flip", JsonPrimitive(config.screenFlip))
            },
        )
    }

    suspend fun getWallpaperStatus(ipAddress: String): WallpaperStatus {
        val response = requestJsonObject("http://$ipAddress/api/wallpaper/config", "GET")
            ?: return WallpaperStatus()
        val config = response["data"]?.jsonObject ?: response
        return WallpaperStatus(
            hasCustomWallpaper = config.booleanOrDefault(false, "has_custom_wallpaper"),
            fileSize = config["file_size"]?.jsonPrimitive?.longOrNull ?: 0L,
        )
    }

    fun wallpaperPreviewUrl(ipAddress: String): String = "http://$ipAddress/api/wallpaper/thumbnail"

    suspend fun uploadWallpaper(
        ipAddress: String,
        fileName: String,
        bytes: ByteArray,
        mimeType: String,
    ) {
        withContext(Dispatchers.IO) {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    bytes.toRequestBody(mimeType.toMediaType()),
                )
                .build()
            executeRequest(
                Request.Builder()
                    .url("http://$ipAddress/api/wallpaper/upload")
                    .post(requestBody)
                    .build(),
            )
        }
    }

    suspend fun resetWallpaper(ipAddress: String) {
        postJson("http://$ipAddress/api/wallpaper/reset", buildJsonObject { })
    }

    suspend fun getMusicConfig(ipAddress: String): JsonObject? =
        requestJsonObject("http://$ipAddress/api/music/config", "GET")

    suspend fun syncMusicConfig(ipAddress: String, payload: JsonObject) {
        postJson("http://$ipAddress/api/music/config", payload)
    }

    private suspend fun postJson(url: String, body: JsonObject) {
        requestJsonObject(url, "POST", body) ?: throw IllegalStateException("设备无响应")
    }

    private suspend fun requestJsonObject(
        url: String,
        method: String,
        body: JsonObject? = null,
        timeoutSeconds: Long = 4L,
    ): JsonObject? = withContext(Dispatchers.IO) {
        val jsonClient = client.newBuilder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .build()

        val requestBody = body?.let {
            AppJson.encodeToString(JsonObject.serializer(), it)
                .toRequestBody("application/json".toMediaType())
        }

        val request = Request.Builder()
            .url(url)
            .method(method, when (method) {
                "POST", "PUT" -> requestBody ?: ByteArray(0).toRequestBody()
                "DELETE" -> null
                else -> null
            })
            .build()

        jsonClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}")
            }
            val raw = response.body?.string().orEmpty()
            if (raw.isBlank()) return@withContext buildJsonObject { }
            parseJsonObject(raw)
        }
    }

    private fun executeRequest(request: Request) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}")
        }
    }
}

private fun JsonElement.toAlarmList(): List<AlarmConfig> {
    val array = when (this) {
        is JsonArray -> this
        is JsonObject -> buildJsonArray { add(this@toAlarmList) }
        else -> buildJsonArray { }
    }
    return array.mapNotNull { element ->
        val item = runCatching { element.jsonObject }.getOrNull() ?: return@mapNotNull null
        AlarmConfig(
            index = item["index"]?.jsonPrimitive?.intOrNull,
            hour = item.intOrDefault(7, "hour"),
            minute = item.intOrDefault(0, "minute"),
            label = item.stringOrEmpty("label"),
            repeatDays = item.intOrDefault(0, "repeat_days"),
            actionType = item.intOrDefault(0, "action_type"),
            ringtoneType = item.intOrDefault(1, "ringtone_type"),
            repeatCount = item.intOrDefault(3, "repeat_count"),
            enabled = item.booleanOrDefault(true, "enabled"),
        )
    }
}

