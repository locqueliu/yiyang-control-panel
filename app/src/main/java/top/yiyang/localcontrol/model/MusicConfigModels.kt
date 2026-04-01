package top.yiyang.localcontrol.model

import java.util.UUID
import kotlinx.serialization.Serializable

enum class MusicApiType {
    RANDOM,
    COMBINED,
    SEPARATED,
}

@Serializable
data class MusicApiConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val remark: String = "",
    val type: String = MusicApiType.RANDOM.name,
    val apiUrl: String = "",
    val searchApiUrl: String = "",
    val parseApiUrl: String = "",
    val dataPath: String = "",
    val fieldMapJson: String = "{\n  \"songName\": \"title\",\n  \"artist\": \"artist\",\n  \"audioUrl\": \"url\"\n}",
    val searchFieldMapJson: String = "{\n  \"id\": \"id\",\n  \"songName\": \"title\",\n  \"artist\": \"artist\"\n}",
    val parseFieldMapJson: String = "{\n  \"audioUrl\": \"url\",\n  \"lyricUrl\": \"lrc\"\n}",
    val requestParamsJson: String = "{\n  \"keyword\": \"msg\",\n  \"index\": \"n\"\n}",
    val searchRequestParamsJson: String = "{\n  \"keyword\": \"name\"\n}",
    val parseRequestParamsJson: String = "{\n  \"id\": \"id\"\n}",
    val extraRequestParamsJson: String = "",
    val extraSearchRequestParamsJson: String = "",
    val extraParseRequestParamsJson: String = "",
    val version: String = "1.0.0",
)

