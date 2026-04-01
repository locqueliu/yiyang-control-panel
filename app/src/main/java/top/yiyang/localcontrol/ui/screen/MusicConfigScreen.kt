package top.yiyang.localcontrol.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.net.URI
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.contentOrNull
import top.yiyang.localcontrol.data.AppJson
import top.yiyang.localcontrol.data.DeviceApiClient
import top.yiyang.localcontrol.data.LocalStorage
import top.yiyang.localcontrol.data.parseJsonMap
import top.yiyang.localcontrol.data.parseJsonObject
import top.yiyang.localcontrol.data.stringOrEmpty
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.model.MusicApiConfig
import top.yiyang.localcontrol.ui.asYiyangBrandText
import top.yiyang.localcontrol.ui.DebugMockData
import top.yiyang.localcontrol.ui.displayTitle

private const val MusicConfigLegalNotice =
    "本页面仅提供 API 配置与调试能力，所有音乐 / 音频 API 由用户自行提供并确保合法合规，包括但不限于著作权、信息网络传播权、表演权等相关权益的取得与许可。本应用不存储、不托管、不审核任何具体 API 或音频内容，也不对由此产生的任何风险或纠纷承担责任，使用本应用即视为用户自行承担全部法律与合规责任。"

@Composable
fun MusicConfigScreen(
    modifier: Modifier = Modifier,
    selectedDevice: DeviceSummary?,
    apiClient: DeviceApiClient,
    storage: LocalStorage,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDebugDevice = selectedDevice?.debugMock == true
    val configs = remember { mutableStateListOf<MusicApiConfig>().apply { addAll(storage.loadMusicConfigs()) } }
    var current by remember { mutableStateOf(configs.firstOrNull() ?: MusicApiConfig(name = "默认配置")) }
    var feedback by remember { mutableStateOf("等待导入配置文件。") }
    var sampleJson by rememberSaveable { mutableStateOf("") }
    var remotePreview by rememberSaveable { mutableStateOf("") }
    var editorExpanded by remember(selectedDevice?.ipAddress) { mutableStateOf(false) }

    fun persistConfigs() {
        val existing = configs.indexOfFirst { it.id == current.id }
        if (existing >= 0) configs[existing] = current else configs.add(0, current)
        storage.saveMusicConfigs(configs.toList())
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = context.readText(uri) ?: return@rememberLauncherForActivityResult
        runCatching { importMusicConfig(text) }
            .onSuccess {
                current = it
                persistConfigs()
                feedback = "配置文件已导入并保存到本地"
            }
            .onFailure { feedback = "导入失败：${it.message}" }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        context.writeText(uri, AppJson.encodeToString(current))
        feedback = "配置已导出"
    }

    val previewJson = remember(current) { AppJson.encodeToString(buildMusicPayload(current)) }
    val currentConfigName = current.name.ifBlank { "未命名配置" }

    LaunchedEffect(selectedDevice?.ipAddress) {
        feedback = when {
            selectedDevice == null -> "未选择设备，当前仅可导入和编辑配置。"
            selectedDevice.debugMock -> "已切换到调试模拟设备，可直接验证导入与同步流程。"
            else -> "已切换同步目标，可以继续同步当前配置。"
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ToolSectionCard(
                title = "音乐配置",
                subtitle = "导入配置文件后，可直接同步到当前设备。",
                highlighted = true,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ToolMetaChip(
                        "同步目标",
                        selectedDevice?.displayTitle() ?: "未选择",
                        emphasized = selectedDevice != null,
                    )
                    ToolMetaChip("当前配置", currentConfigName.asYiyangBrandText())
                    ToolMetaChip("当前类型", current.type)
                }
                ToolFeedbackBanner(
                    text = selectedDevice?.let {
                        "目标设备 ${it.displayTitle()} · ${it.ipAddress}" + if (it.debugMock) " · 调试模式" else ""
                    } ?: "未选择设备时无法同步到设备。",
                    emphasized = true,
                )
                ToolFeedbackBanner(text = feedback)
            }
        }

        item {
            ToolSectionCard(
                title = "快速导入与同步",
                subtitle = "导入配置文件后，直接同步到设备。",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                    ) { Text("导入文件") }
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = selectedDevice != null,
                        onClick = {
                            if (selectedDevice == null) {
                                feedback = "先选择设备再同步"
                                return@Button
                            }
                            scope.launch {
                                if (isDebugDevice) {
                                    persistConfigs()
                                    remotePreview = AppJson.encodeToString(
                                        JsonObject.serializer(),
                                        buildMusicPayload(current),
                                    )
                                    feedback = "调试模式：已模拟同步到设备"
                                } else {
                                    runCatching {
                                        apiClient.syncMusicConfig(
                                            selectedDevice.ipAddress,
                                            buildMusicPayload(current),
                                        )
                                    }
                                        .onSuccess {
                                            feedback = "已同步到设备"
                                            persistConfigs()
                                        }
                                        .onFailure { feedback = it.message ?: "同步配置失败" }
                                }
                            }
                        },
                    ) { Text("同步到设备") }
                }
            }
        }

        item {
            CollapsibleToolSectionCard(
                title = "自定义",
                subtitle = "管理本地配置、接口设置和预览。",
                expanded = editorExpanded,
                onExpandedChange = { editorExpanded = it },
                showStateBadge = false,
                summary = {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ToolMetaChip("当前类型", current.type)
                        ToolMetaChip("配置名称", currentConfigName.asYiyangBrandText())
                        ToolMetaChip("本地配置", "${configs.size} 份")
                    }
                },
            ) {
                MusicEditorBlock(
                    title = "配置管理",
                    subtitle = "选择、保存或读取配置。",
                ) {
                    if (configs.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            configs.forEach { config ->
                                FilterChip(
                                    selected = current.id == config.id,
                                    onClick = { current = config },
                                    label = { Text(config.name.ifBlank { "未命名配置" }.asYiyangBrandText()) },
                                )
                            }
                        }
                    } else {
                        ToolFeedbackBanner("暂无本地配置。")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { current = MusicApiConfig(name = "新配置") },
                        ) { Text("新建空白") }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                persistConfigs()
                                feedback = "已保存到本地"
                            },
                        ) { Text("保存本地") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { exportLauncher.launch("${current.name.ifBlank { "music-config" }}.json") },
                        ) { Text("导出配置") }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = selectedDevice != null,
                            onClick = {
                                if (selectedDevice == null) {
                                    feedback = "先选择设备再读取配置"
                                    return@OutlinedButton
                                }
                                scope.launch {
                                    if (isDebugDevice) {
                                        remotePreview = AppJson.encodeToString(
                                            JsonObject.serializer(),
                                            DebugMockData.musicRemoteConfig(),
                                        )
                                        feedback = "调试模式：已读取设备配置"
                                    } else {
                                        runCatching { apiClient.getMusicConfig(selectedDevice.ipAddress) }
                                            .onSuccess {
                                                remotePreview = AppJson.encodeToString(
                                                    JsonObject.serializer(),
                                                    it ?: buildJsonObject { },
                                                )
                                                feedback = "已读取设备配置"
                                            }
                                            .onFailure { feedback = it.message ?: "读取设备配置失败" }
                                    }
                                }
                            },
                        ) { Text("读取设备") }
                    }
                }

                HorizontalDivider()

                MusicEditorBlock(
                    title = "接口设置",
                    subtitle = "设置接口类型、地址和响应路径。",
                ) {
                    OutlinedTextField(
                        value = current.name,
                        onValueChange = { current = current.copy(name = it) },
                        label = { Text("配置名称") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = current.remark,
                        onValueChange = { current = current.copy(remark = it) },
                        label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "接口类型",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("RANDOM", "COMBINED", "SEPARATED").forEach { type ->
                            FilterChip(
                                selected = current.type == type,
                                onClick = { current = current.copy(type = type) },
                                label = { Text(type) },
                            )
                        }
                    }
                    if (current.type == "SEPARATED") {
                        OutlinedTextField(
                            value = current.searchApiUrl,
                            onValueChange = { current = current.copy(searchApiUrl = it) },
                            label = { Text("搜索接口地址") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = current.parseApiUrl,
                            onValueChange = { current = current.copy(parseApiUrl = it) },
                            label = { Text("解析接口地址") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        OutlinedTextField(
                            value = current.apiUrl,
                            onValueChange = { current = current.copy(apiUrl = it) },
                            label = { Text("接口地址") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    OutlinedTextField(
                        value = current.dataPath,
                        onValueChange = { current = current.copy(dataPath = it) },
                        label = { Text("响应路径（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                HorizontalDivider()

                MusicEditorBlock(
                    title = "字段映射",
                    subtitle = "配置字段映射并识别示例数据。",
                ) {
                    if (current.type == "SEPARATED") {
                        OutlinedTextField(
                            value = current.searchFieldMapJson,
                            onValueChange = { current = current.copy(searchFieldMapJson = it) },
                            label = { Text("搜索字段映射 JSON") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                        )
                        OutlinedTextField(
                            value = current.parseFieldMapJson,
                            onValueChange = { current = current.copy(parseFieldMapJson = it) },
                            label = { Text("解析字段映射 JSON") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                        )
                        OutlinedTextField(
                            value = current.searchRequestParamsJson,
                            onValueChange = { current = current.copy(searchRequestParamsJson = it) },
                            label = { Text("搜索请求参数映射 JSON") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                        )
                        OutlinedTextField(
                            value = current.parseRequestParamsJson,
                            onValueChange = { current = current.copy(parseRequestParamsJson = it) },
                            label = { Text("解析请求参数映射 JSON") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                        )
                        OutlinedTextField(
                            value = current.extraSearchRequestParamsJson,
                            onValueChange = { current = current.copy(extraSearchRequestParamsJson = it) },
                            label = { Text("搜索附加参数 JSON（可选）") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                        )
                        OutlinedTextField(
                            value = current.extraParseRequestParamsJson,
                            onValueChange = { current = current.copy(extraParseRequestParamsJson = it) },
                            label = { Text("解析附加参数 JSON（可选）") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                        )
                    } else {
                        OutlinedTextField(
                            value = current.fieldMapJson,
                            onValueChange = { current = current.copy(fieldMapJson = it) },
                            label = { Text("字段映射 JSON") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                        )
                        OutlinedTextField(
                            value = current.requestParamsJson,
                            onValueChange = { current = current.copy(requestParamsJson = it) },
                            label = { Text("请求参数映射 JSON") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                        )
                        OutlinedTextField(
                            value = current.extraRequestParamsJson,
                            onValueChange = { current = current.copy(extraRequestParamsJson = it) },
                            label = { Text("附加固定参数 JSON（可选）") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                        )
                    }
                    OutlinedTextField(
                        value = sampleJson,
                        onValueChange = { sampleJson = it },
                        label = { Text("示例 JSON") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                    )
                    OutlinedButton(
                        onClick = {
                            applySuggestedMapping(sampleJson, current)?.let {
                                current = it
                                feedback = "已生成字段映射草稿"
                            } ?: run {
                                feedback = "未识别到可用字段"
                            }
                        },
                    ) { Text("自动识别") }
                }

                HorizontalDivider()

                MusicEditorBlock(
                    title = "预览",
                    subtitle = "查看即将同步到设备的配置。",
                ) {
                    OutlinedTextField(
                        value = previewJson,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        minLines = 10,
                        label = { Text("准备同步的 JSON") },
                    )
                    if (remotePreview.isNotBlank()) {
                        OutlinedTextField(
                            value = remotePreview,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            minLines = 8,
                            label = { Text("设备当前配置") },
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = MusicConfigLegalNotice,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MusicEditorBlock(
    title: String,
    subtitle: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}

private fun buildMusicPayload(config: MusicApiConfig): JsonObject = buildJsonObject {
    put("type", JsonPrimitive(config.type))
    put("version", JsonPrimitive(config.version))
    put("remark", JsonPrimitive(config.remark))
    put("dataPath", JsonPrimitive(config.dataPath))
    when (config.type) {
        "SEPARATED" -> {
            put("searchApiUrl", JsonPrimitive(config.searchApiUrl))
            put("parseApiUrl", JsonPrimitive(config.parseApiUrl))
            put("searchFieldMap", parseJsonMap(config.searchFieldMapJson))
            put("parseFieldMap", parseJsonMap(config.parseFieldMapJson))
            put("searchRequestParams", parseJsonMap(config.searchRequestParamsJson))
            put("parseRequestParams", parseJsonMap(config.parseRequestParamsJson))
            putJsonMapIfNotBlank("extraSearchRequestParams", config.extraSearchRequestParamsJson)
            putJsonMapIfNotBlank("extraParseRequestParams", config.extraParseRequestParamsJson)
        }

        else -> {
            put("apiUrl", JsonPrimitive(config.apiUrl))
            put("fieldMap", parseJsonMap(config.fieldMapJson))
            put("requestParams", parseJsonMap(config.requestParamsJson))
            putJsonMapIfNotBlank("extraRequestParams", config.extraRequestParamsJson)
        }
    }
}

private fun importMusicConfig(raw: String): MusicApiConfig {
    val root = parseJsonObject(raw) ?: throw IllegalArgumentException("文件不是有效的 JSON 对象")
    if (!looksLikeMusicConfig(root)) {
        throw IllegalArgumentException("文件不是支持的音乐配置格式")
    }

    val defaults = MusicApiConfig(id = "", name = "")
    val importedId = root.stringOrEmpty("id").ifBlank { UUID.randomUUID().toString() }
    val importedType = normalizeMusicConfigType(root.stringOrEmpty("type"))

    return MusicApiConfig(
        id = importedId,
        name = root.stringOrEmpty("name").ifBlank { guessMusicConfigName(root) },
        remark = root.stringOrEmpty("remark"),
        type = importedType,
        apiUrl = root.stringOrEmpty("apiUrl"),
        searchApiUrl = root.stringOrEmpty("searchApiUrl"),
        parseApiUrl = root.stringOrEmpty("parseApiUrl"),
        dataPath = root.stringOrEmpty("dataPath"),
        fieldMapJson = readJsonEditorValue(root, defaults.fieldMapJson, "fieldMapJson", "fieldMap"),
        searchFieldMapJson = readJsonEditorValue(root, defaults.searchFieldMapJson, "searchFieldMapJson", "searchFieldMap"),
        parseFieldMapJson = readJsonEditorValue(root, defaults.parseFieldMapJson, "parseFieldMapJson", "parseFieldMap"),
        requestParamsJson = readJsonEditorValue(root, defaults.requestParamsJson, "requestParamsJson", "requestParams"),
        searchRequestParamsJson = readJsonEditorValue(root, defaults.searchRequestParamsJson, "searchRequestParamsJson", "searchRequestParams"),
        parseRequestParamsJson = readJsonEditorValue(root, defaults.parseRequestParamsJson, "parseRequestParamsJson", "parseRequestParams"),
        extraRequestParamsJson = readJsonEditorValue(root, "", "extraRequestParamsJson", "extraRequestParams"),
        extraSearchRequestParamsJson = readJsonEditorValue(root, "", "extraSearchRequestParamsJson", "extraSearchRequestParams"),
        extraParseRequestParamsJson = readJsonEditorValue(root, "", "extraParseRequestParamsJson", "extraParseRequestParams"),
        version = readScalarValue(root, defaults.version, "version"),
    )
}

private fun applySuggestedMapping(sample: String, config: MusicApiConfig): MusicApiConfig? {
    val root = parseJsonObject(sample) ?: return null
    val candidate = sampleObject(root, config.dataPath) ?: root
    val mapped = when (config.type) {
        "SEPARATED" -> config.copy(
            searchFieldMapJson = AppJson.encodeToString(suggestSearchFieldMap(candidate)),
            parseFieldMapJson = AppJson.encodeToString(suggestParseFieldMap(candidate)),
        )
        else -> config.copy(
            fieldMapJson = AppJson.encodeToString(suggestFieldMap(candidate)),
        )
    }
    return mapped
}

private fun sampleObject(root: JsonObject, dataPath: String): JsonObject? {
    if (dataPath.isBlank()) return root
    var current: JsonElement = root
    dataPath.split(".").forEach { key ->
        current = when (current) {
            is JsonObject -> current.jsonObject[key] ?: return null
            else -> return null
        }
    }
    return when (current) {
        is JsonObject -> current
        is JsonArray -> current.firstOrNull()?.jsonObject
        else -> null
    }
}

private fun suggestFieldMap(sample: JsonObject): JsonObject = buildJsonObject {
    guessKey(sample, "title", "name", "songName")?.let { put("songName", JsonPrimitive(it)) }
    guessKey(sample, "artist", "author", "singer")?.let { put("artist", JsonPrimitive(it)) }
    guessKey(sample, "album")?.let { put("album", JsonPrimitive(it)) }
    guessKey(sample, "duration", "length")?.let { put("duration", JsonPrimitive(it)) }
    guessKey(sample, "url", "audioUrl", "playUrl")?.let { put("audioUrl", JsonPrimitive(it)) }
    guessKey(sample, "lrc", "lyric", "lyricUrl")?.let { put("lyricUrl", JsonPrimitive(it)) }
}

private fun suggestSearchFieldMap(sample: JsonObject): JsonObject = buildJsonObject {
    guessKey(sample, "id", "songId")?.let { put("id", JsonPrimitive(it)) }
    guessKey(sample, "title", "name", "songName")?.let { put("songName", JsonPrimitive(it)) }
    guessKey(sample, "artist", "author", "singer")?.let { put("artist", JsonPrimitive(it)) }
}

private fun suggestParseFieldMap(sample: JsonObject): JsonObject = buildJsonObject {
    guessKey(sample, "url", "audioUrl", "playUrl")?.let { put("audioUrl", JsonPrimitive(it)) }
    guessKey(sample, "lrc", "lyric", "lyricUrl")?.let { put("lyricUrl", JsonPrimitive(it)) }
}

private fun guessKey(sample: JsonObject, vararg candidates: String): String? {
    val keys = sample.keys
    return candidates.firstOrNull { expected ->
        keys.any { it.equals(expected, ignoreCase = true) }
    }?.let { expected ->
        keys.firstOrNull { it.equals(expected, ignoreCase = true) }
    }
}

private fun looksLikeMusicConfig(root: JsonObject): Boolean {
    val keys = setOf(
        "id",
        "name",
        "type",
        "apiUrl",
        "searchApiUrl",
        "parseApiUrl",
        "fieldMap",
        "fieldMapJson",
        "requestParams",
        "requestParamsJson",
        "searchFieldMap",
        "searchFieldMapJson",
        "parseFieldMap",
        "parseFieldMapJson",
    )
    return root.keys.any { it in keys }
}

private fun normalizeMusicConfigType(raw: String): String {
    return when (raw.trim().uppercase()) {
        "COMBINED", "SEPARATED", "RANDOM" -> raw.trim().uppercase()
        else -> "RANDOM"
    }
}

private fun readScalarValue(
    root: JsonObject,
    defaultValue: String,
    vararg keys: String,
): String {
    keys.forEach { key ->
        val value = root[key] ?: return@forEach
        return when (value) {
            JsonNull -> ""
            is JsonPrimitive -> value.contentOrNull.orEmpty()
            else -> AppJson.encodeToString(JsonElement.serializer(), value)
        }
    }
    return defaultValue
}

private fun readJsonEditorValue(
    root: JsonObject,
    defaultValue: String,
    vararg keys: String,
): String {
    keys.forEach { key ->
        val value = root[key] ?: return@forEach
        return when (value) {
            JsonNull -> ""
            is JsonObject -> AppJson.encodeToString(JsonObject.serializer(), value)
            is JsonArray -> AppJson.encodeToString(JsonArray.serializer(), value)
            is JsonPrimitive -> normalizeJsonEditorText(value.contentOrNull.orEmpty())
        }
    }
    return defaultValue
}

private fun normalizeJsonEditorText(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank() || trimmed == "null") return ""
    val parsed = runCatching { AppJson.parseToJsonElement(trimmed) }.getOrNull()
    return when (parsed) {
        is JsonObject -> AppJson.encodeToString(JsonObject.serializer(), parsed)
        is JsonArray -> AppJson.encodeToString(JsonArray.serializer(), parsed)
        else -> raw
    }
}

private fun guessMusicConfigName(root: JsonObject): String {
    root.stringOrEmpty("name").takeIf { it.isNotBlank() }?.let { return it }

    val apiCandidates = listOf(
        root.stringOrEmpty("apiUrl"),
        root.stringOrEmpty("searchApiUrl"),
        root.stringOrEmpty("parseApiUrl"),
    ).filter { it.isNotBlank() }

    apiCandidates.forEach { url ->
        val parsed = runCatching { URI(url) }.getOrNull() ?: return@forEach
        val pathName = parsed.path
            ?.trim('/')
            ?.split('/')
            ?.lastOrNull()
            .orEmpty()
        if (pathName.isNotBlank()) return pathName

        val hostName = parsed.host
            ?.removePrefix("api.")
            ?.substringBefore('.')
            .orEmpty()
        if (hostName.isNotBlank()) return hostName
    }

    return when (normalizeMusicConfigType(root.stringOrEmpty("type"))) {
        "COMBINED" -> "组合接口"
        "SEPARATED" -> "分离接口"
        else -> "随机接口"
    }
}

private fun Context.readText(uri: Uri): String? {
    return contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.putJsonMapIfNotBlank(key: String, raw: String) {
    if (raw.isBlank()) return
    put(key, parseJsonMap(raw))
}

private fun Context.writeText(uri: Uri, value: String) {
    contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(value) }
}

