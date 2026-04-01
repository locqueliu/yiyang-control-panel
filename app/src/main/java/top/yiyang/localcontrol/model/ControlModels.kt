package top.yiyang.localcontrol.model

import kotlinx.serialization.Serializable

@Serializable
data class AlarmConfig(
    val index: Int? = null,
    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "闹钟",
    val repeatDays: Int = 0,
    val actionType: Int = 0,
    val ringtoneType: Int = 1,
    val repeatCount: Int = 3,
    val enabled: Boolean = true,
)

data class ActionOption(
    val id: Int,
    val label: String,
)

data class LampState(
    val action: String = "on",
    val speed: Int = 5,
    val brightness: Int = 80,
    val red: Int = 255,
    val green: Int = 255,
    val blue: Int = 255,
)

data class DeviceAction(
    val id: Int,
    val label: String,
    val options: List<ActionOption> = emptyList(),
)


