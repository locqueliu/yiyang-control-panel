package top.yiyang.localcontrol.model

internal object ProtocolIds {
    val legacyBrandUpper: String = charArrayOf('Z', 'Z', 'P', 'E', 'T').concatToString()
    val legacyBrandTitle: String = charArrayOf('Z', 'z', 'P', 'e', 't').concatToString()
    val legacyBrandLower: String = legacyBrandUpper.lowercase()

    val botModel: String = "$legacyBrandUpper-BOT"
    val botProModel: String = "$legacyBrandUpper-BOT-PRO"
    val waliModel: String = "$legacyBrandUpper-WALI"
}
