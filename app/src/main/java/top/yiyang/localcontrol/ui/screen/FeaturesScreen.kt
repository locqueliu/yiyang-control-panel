package top.yiyang.localcontrol.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import top.yiyang.localcontrol.data.DeviceApiClient
import top.yiyang.localcontrol.model.AdvancedDeviceConfig
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.model.ProtocolIds
import top.yiyang.localcontrol.model.WakewordConfig
import top.yiyang.localcontrol.model.WallpaperStatus
import top.yiyang.localcontrol.ui.displayProductModel
import top.yiyang.localcontrol.ui.displayTitle
import top.yiyang.localcontrol.ui.DebugMockData

private data class ConfigHelpContent(
    val title: String,
    val description: String,
    val recommendation: String,
)

private data class ConfigPresetOption(
    val value: Int,
    val label: String,
)

private val globalMusicHelp = ConfigHelpContent(
    title = "全局音乐功能",
    description = "控制设备是否启用音乐相关能力，包括本地播放联动、控制页音乐操作以及音乐接口同步能力。",
    recommendation = "不使用音乐功能或设备未接音频模块时可以关闭，避免无效调用。",
)

private val fanFunctionHelp = ConfigHelpContent(
    title = "风扇功能",
    description = "控制风扇扩展模块是否参与设备运行。启用后，固件会按风扇能力执行相关逻辑。",
    recommendation = "未安装风扇模块时建议关闭，避免设备进入不匹配的控制路径。",
)

private val batteryDetectionHelp = ConfigHelpContent(
    title = "电量检测",
    description = "用于读取电池状态、低电量表现和部分续航相关提示，是移动供电机型的重要状态开关。",
    recommendation = "固定 USB 供电或无电池版本可按需要关闭；电池机型建议保持开启。",
)

private val doubleClickModeHelp = ConfigHelpContent(
    title = "双击模式",
    description = "设置机身双击后的响应模式或动作编号，不同固件中同一编号可能对应不同交互行为。",
    recommendation = "若不确定编号含义，先记录原值再修改；调试时建议小步验证。",
)

private val ledNumHelp = ConfigHelpContent(
    title = "LED 灯数量",
    description = "定义设备当前参与灯效计算的 LED 数量，会直接影响灯效长度、节奏和渲染范围。",
    recommendation = "请与实际硬件灯珠数量保持一致，数量错误会导致灯效显示异常。",
)

private val customEmotionHelp = ConfigHelpContent(
    title = "自定义表情",
    description = "控制设备是否允许加载和显示自定义表情资源，通常与高阶主题或个性化资源联动。",
    recommendation = "不使用自定义表情资源时可以关闭，减少误触发和资源依赖。",
)

private val themeHelp = ConfigHelpContent(
    title = "主题",
    description = "设置设备界面主题编号，不同编号通常对应不同表盘、颜色或视觉风格。",
    recommendation = "若没有主题编号对照表，建议按当前可用主题逐个验证，不要一次跨大范围改动。",
)

private val wallpaperTimeColorHelp = ConfigHelpContent(
    title = "壁纸时间颜色",
    description = "设置壁纸界面中时间文本的颜色，使用十六进制 RGB 值保存。",
    recommendation = "建议使用 6 位十六进制颜色，如 2F6F62，并与背景保持足够对比度。",
)

private val wallpaperDateColorHelp = ConfigHelpContent(
    title = "壁纸日期颜色",
    description = "设置壁纸界面中日期文本的颜色，使用十六进制 RGB 值保存。",
    recommendation = "尽量与时间颜色同风格但略有层次，保证日期信息能快速辨认。",
)

private val wallpaperWakeupColorHelp = ConfigHelpContent(
    title = "壁纸唤醒颜色",
    description = "设置壁纸界面中唤醒词或唤醒状态文字的颜色，使用十六进制 RGB 值保存。",
    recommendation = "建议选择较亮或强调色，用来区分唤醒状态与普通显示内容。",
)

private val pcbVersionHelp = ConfigHelpContent(
    title = "PCB 版本号",
    description = "用于标识当前设备主板硬件版本，固件会据此适配部分硬件行为和兼容逻辑。",
    recommendation = "除非明确知道主板版本，否则不要随意改动，错误版本可能导致兼容问题。",
)

private val displayTypeHelp = ConfigHelpContent(
    title = "显示类型",
    description = "设置屏幕或显示模组类型编号，用于匹配对应的显示驱动和布局参数。",
    recommendation = "仅在更换屏幕模组或确认固件文档时修改，未知情况下保持原值最安全。",
)

private val screenFlipHelp = ConfigHelpContent(
    title = "屏幕翻转",
    description = "控制屏幕方向是否翻转，适合硬件安装方向与默认显示方向不一致的场景。",
    recommendation = "常见值通常为 0 或 1；修改后请立即确认界面方向是否符合设备安装方式。",
)

private val doubleClickModeOptions = listOf(
    ConfigPresetOption(0, "模式 0"),
    ConfigPresetOption(1, "模式 1"),
    ConfigPresetOption(2, "模式 2"),
    ConfigPresetOption(3, "模式 3"),
)

private val ledCountOptions = listOf(
    ConfigPresetOption(4, "4 灯"),
    ConfigPresetOption(8, "8 灯"),
    ConfigPresetOption(12, "12 灯"),
    ConfigPresetOption(16, "16 灯"),
)

private val themeOptions = listOf(
    ConfigPresetOption(0, "主题 0"),
    ConfigPresetOption(1, "主题 1"),
    ConfigPresetOption(2, "主题 2"),
    ConfigPresetOption(3, "主题 3"),
    ConfigPresetOption(4, "主题 4"),
)

private val pcbVersionOptions = listOf(
    ConfigPresetOption(1, "V1"),
    ConfigPresetOption(2, "V2"),
    ConfigPresetOption(3, "V3"),
)

private val displayTypeOptions = listOf(
    ConfigPresetOption(0, "类型 0"),
    ConfigPresetOption(1, "类型 1"),
)

private val screenFlipOptions = listOf(
    ConfigPresetOption(0, "正常"),
    ConfigPresetOption(1, "翻转"),
)

private val wallpaperColorPresets = listOf(
    0x2F6F62,
    0x4B7D74,
    0xEADFCF,
    0xDFAF7C,
    0x6886C5,
    0xF3D7A5,
)

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun FeaturesScreen(
    modifier: Modifier = Modifier,
    selectedDevice: DeviceSummary?,
    apiClient: DeviceApiClient,
) {
    if (selectedDevice == null) {
        LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp)) {
            item { EmptyStateCard("还没选设备", "先去“设备”页选一台在线设备，再回来改配置。") }
        }
        return
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDebugDevice = selectedDevice.debugMock
    val isProModel = selectedDevice.productModel.contains("PRO")
    val isBotModel = selectedDevice.productModel == ProtocolIds.botModel
    val contentListState = rememberLazyListState()
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var headerExpanded by rememberSaveable(selectedDevice.ipAddress) { mutableStateOf(true) }
    var feedback by remember {
        mutableStateOf(
            if (isDebugDevice) "调试模拟设备：当前页面使用本地假数据。"
            else "当前设备：${selectedDevice.displayTitle()}",
        )
    }
    var activeConfigHelp by remember { mutableStateOf<ConfigHelpContent?>(null) }
    var advancedConfig by remember { mutableStateOf(AdvancedDeviceConfig()) }
    var wakeword by remember { mutableStateOf(WakewordConfig()) }
    val servoValues = remember { mutableStateMapOf<String, Int>() }
    var wallpaperStatus by remember { mutableStateOf(WallpaperStatus()) }
    var wallpaperPreviewSeed by remember { mutableIntStateOf(0) }
    var selectedWallpaperUri by remember { mutableStateOf<Uri?>(null) }

    suspend fun refreshAdvanced() {
        if (isDebugDevice) {
            advancedConfig = DebugMockData.advancedConfig()
            feedback = "调试模式已载入设备高级配置"
            return
        }
        runCatching { advancedConfig = apiClient.getAdvancedConfig(selectedDevice.ipAddress) }
            .onSuccess { feedback = "设备高级配置已刷新" }
            .onFailure { feedback = it.message ?: "读取设备配置失败" }
    }

    suspend fun saveAdvanced() {
        if (isDebugDevice) {
            feedback = "调试模式：设备高级配置已保存到本地预览"
            return
        }
        runCatching { apiClient.saveAdvancedConfig(selectedDevice.ipAddress, advancedConfig) }
            .onSuccess { feedback = "设备高级配置已保存" }
            .onFailure { feedback = it.message ?: "保存配置失败" }
    }

    suspend fun refreshWakeword() {
        if (isDebugDevice) {
            wakeword = DebugMockData.wakeword()
            feedback = "调试模式已载入唤醒词配置"
            return
        }
        runCatching { wakeword = apiClient.getWakewordConfig(selectedDevice.ipAddress) }
            .onSuccess { feedback = "唤醒词配置已刷新" }
            .onFailure { feedback = it.message ?: "读取唤醒词失败" }
    }

    suspend fun refreshServo() {
        if (isDebugDevice) {
            servoValues.clear()
            servoValues.putAll(DebugMockData.servoValues(selectedDevice.productModel))
            feedback = "调试模式已载入舵机微调数据"
            return
        }
        runCatching {
            servoValues.clear()
            servoValues.putAll(apiClient.getServoTrims(selectedDevice.ipAddress))
        }
            .onSuccess { feedback = "舵机微调值已刷新" }
            .onFailure { feedback = it.message ?: "读取舵机微调失败" }
    }

    suspend fun refreshWallpaper() {
        if (isDebugDevice) {
            wallpaperStatus = DebugMockData.wallpaperStatus()
            wallpaperPreviewSeed += 1
            feedback = "调试模式已载入壁纸状态"
            return
        }
        runCatching { wallpaperStatus = apiClient.getWallpaperStatus(selectedDevice.ipAddress) }
            .onSuccess { wallpaperPreviewSeed += 1 }
            .onFailure { feedback = it.message ?: "读取壁纸状态失败" }
    }

    LaunchedEffect(selectedDevice.ipAddress) {
        refreshAdvanced()
        refreshWakeword()
        refreshServo()
        refreshWallpaper()
    }

    LaunchedEffect(contentListState, tabIndex, selectedDevice.ipAddress) {
        snapshotFlow { contentListState.firstVisibleItemIndex to contentListState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (headerExpanded && (index > 0 || offset >= 56)) {
                    headerExpanded = false
                }
            }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedWallpaperUri = uri
        if (uri != null) feedback = "已选择壁纸文件，点上传即可同步到设备"
    }

    val configSectionCount = 2 + if (isProModel) 1 else 0 + if (isBotModel) 1 else 0
    val configItemCount = 5 + if (isProModel) 5 else 0 + if (isBotModel) 3 else 0

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DeviceStatusHeaderCard(
                device = selectedDevice,
                feedback = feedback,
                emphasized = isDebugDevice,
                collapsed = !headerExpanded,
                onToggle = { headerExpanded = !headerExpanded },
            )
            ToolTabRow(
                selectedTabIndex = tabIndex,
                titles = listOf("微调", "配置", "唤醒", "壁纸"),
                onSelect = { tabIndex = it },
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = contentListState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (tabIndex == 0) {
            val servoKeys = if (selectedDevice.productModel == ProtocolIds.waliModel) {
                listOf("left_arm" to "左手臂", "right_arm" to "右手臂")
            } else {
                listOf(
                    "left_front_leg" to "左前腿",
                    "right_front_leg" to "右前腿",
                    "left_hide_leg" to "左后腿",
                    "right_hide_leg" to "右后腿",
                )
            }
            item {
                OutlinedButton(onClick = { scope.launch { refreshServo() } }) { Text("刷新微调值") }
            }
            items(servoKeys) { (key, label) ->
                val value = servoValues[key] ?: 0
                ElevatedCard {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("$label ($key)", fontWeight = FontWeight.SemiBold)
                        Text("当前值：$value")
                        Slider(
                            value = value.toFloat(),
                            onValueChange = { servoValues[key] = it.toInt() },
                            valueRange = -50f..50f,
                        )
                        Button(onClick = {
                            scope.launch {
                                runCatching { apiClient.saveServoTrim(selectedDevice.ipAddress, key, servoValues[key] ?: 0) }
                                    .onSuccess { feedback = "$label 微调值已保存" }
                                    .onFailure { feedback = it.message ?: "保存微调值失败" }
                            }
                        }) { Text("保存") }
                    }
                }
            }
        }

            if (tabIndex == 1) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdvancedConfigOverviewCard(
                        productModel = selectedDevice.displayProductModel(),
                        sectionCount = configSectionCount,
                        itemCount = configItemCount,
                        onRefresh = { scope.launch { refreshAdvanced() } },
                        onSave = { scope.launch { saveAdvanced() } },
                    )

                    ConfigSectionCard(title = "系统开关", itemCount = 3) {
                        ConfigSwitchItem(
                            title = "全局音乐功能",
                            checked = advancedConfig.globalMusic,
                            help = globalMusicHelp,
                            onHelpClick = { activeConfigHelp = it },
                            onCheckedChange = { advancedConfig = advancedConfig.copy(globalMusic = it) },
                        )
                        ConfigDivider()
                        ConfigSwitchItem(
                            title = "风扇功能",
                            checked = advancedConfig.fanFunction,
                            help = fanFunctionHelp,
                            onHelpClick = { activeConfigHelp = it },
                            onCheckedChange = { advancedConfig = advancedConfig.copy(fanFunction = it) },
                        )
                        ConfigDivider()
                        ConfigSwitchItem(
                            title = "电量检测",
                            checked = advancedConfig.batteryDetection,
                            help = batteryDetectionHelp,
                            onHelpClick = { activeConfigHelp = it },
                            onCheckedChange = { advancedConfig = advancedConfig.copy(batteryDetection = it) },
                        )
                    }

                    ConfigSectionCard(title = "交互与灯效", itemCount = 2) {
                        ConfigPresetNumberItem(
                            title = "双击模式",
                            value = advancedConfig.doubleClickMode,
                            help = doubleClickModeHelp,
                            presets = doubleClickModeOptions,
                            onHelpClick = { activeConfigHelp = it },
                            onValueChange = { advancedConfig = advancedConfig.copy(doubleClickMode = it) },
                        )
                        ConfigDivider()
                        ConfigPresetNumberItem(
                            title = "LED 灯数量",
                            value = advancedConfig.ledNum,
                            help = ledNumHelp,
                            presets = ledCountOptions,
                            onHelpClick = { activeConfigHelp = it },
                            onValueChange = { advancedConfig = advancedConfig.copy(ledNum = it) },
                        )
                    }

                    if (isProModel) {
                        ConfigSectionCard(title = "主题与壁纸", itemCount = 5) {
                            ConfigSwitchItem(
                                title = "自定义表情",
                                checked = advancedConfig.isCustomEmotion,
                                help = customEmotionHelp,
                                onHelpClick = { activeConfigHelp = it },
                                onCheckedChange = { advancedConfig = advancedConfig.copy(isCustomEmotion = it) },
                            )
                            ConfigDivider()
                            ConfigPresetNumberItem(
                                title = "主题",
                                value = advancedConfig.theme,
                                help = themeHelp,
                                presets = themeOptions,
                                onHelpClick = { activeConfigHelp = it },
                                onValueChange = { advancedConfig = advancedConfig.copy(theme = it) },
                            )
                            ConfigDivider()
                            ConfigHexInputItem(
                                title = "壁纸时间颜色",
                                value = advancedConfig.wallpaperTimeColor,
                                help = wallpaperTimeColorHelp,
                                presets = wallpaperColorPresets,
                                onHelpClick = { activeConfigHelp = it },
                                onValueChange = { advancedConfig = advancedConfig.copy(wallpaperTimeColor = it) },
                            )
                            ConfigDivider()
                            ConfigHexInputItem(
                                title = "壁纸日期颜色",
                                value = advancedConfig.wallpaperDateColor,
                                help = wallpaperDateColorHelp,
                                presets = wallpaperColorPresets,
                                onHelpClick = { activeConfigHelp = it },
                                onValueChange = { advancedConfig = advancedConfig.copy(wallpaperDateColor = it) },
                            )
                            ConfigDivider()
                            ConfigHexInputItem(
                                title = "壁纸唤醒颜色",
                                value = advancedConfig.wallpaperWakeupColor,
                                help = wallpaperWakeupColorHelp,
                                presets = wallpaperColorPresets,
                                onHelpClick = { activeConfigHelp = it },
                                onValueChange = { advancedConfig = advancedConfig.copy(wallpaperWakeupColor = it) },
                            )
                        }
                    }

                    if (isBotModel) {
                        ConfigSectionCard(title = "硬件兼容", itemCount = 3) {
                            ConfigChoiceItem(
                                title = "PCB 版本号",
                                value = advancedConfig.pcbVersion,
                                help = pcbVersionHelp,
                                options = pcbVersionOptions,
                                onHelpClick = { activeConfigHelp = it },
                                onValueChange = { advancedConfig = advancedConfig.copy(pcbVersion = it) },
                            )
                            ConfigDivider()
                            ConfigChoiceItem(
                                title = "显示类型",
                                value = advancedConfig.displayType,
                                help = displayTypeHelp,
                                options = displayTypeOptions,
                                onHelpClick = { activeConfigHelp = it },
                                onValueChange = { advancedConfig = advancedConfig.copy(displayType = it) },
                            )
                            ConfigDivider()
                            ConfigChoiceItem(
                                title = "屏幕翻转",
                                value = advancedConfig.screenFlip,
                                help = screenFlipHelp,
                                options = screenFlipOptions,
                                onHelpClick = { activeConfigHelp = it },
                                onValueChange = { advancedConfig = advancedConfig.copy(screenFlip = it) },
                            )
                        }
                    }
                }
            }
        }

            if (tabIndex == 2) {
            item {
                ElevatedCard {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { scope.launch { refreshWakeword() } }) { Text("刷新配置") }
                            Button(onClick = {
                                scope.launch {
                                    if (isDebugDevice) {
                                        feedback = "调试模式：唤醒词配置已保存到本地预览"
                                    } else {
                                        runCatching { apiClient.saveWakewordConfig(selectedDevice.ipAddress, wakeword) }
                                            .onSuccess { feedback = "唤醒词配置已保存，部分机型需重启后完全生效" }
                                            .onFailure { feedback = it.message ?: "保存唤醒词失败" }
                                    }
                                }
                            }) { Text("保存配置") }
                        }
                        OutlinedTextField(
                            value = wakeword.word,
                            onValueChange = { wakeword = wakeword.copy(word = it.lowercase()) },
                            label = { Text("唤醒词拼音") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = wakeword.display,
                            onValueChange = { wakeword = wakeword.copy(display = it) },
                            label = { Text("显示名称") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("识别阈值 ${wakeword.threshold}")
                        Slider(
                            value = wakeword.threshold.toFloat(),
                            onValueChange = { wakeword = wakeword.copy(threshold = it.toInt()) },
                            valueRange = 0f..100f,
                        )
                        Text("建议阈值 20-30。数值越低越灵敏。")
                    }
                }
            }
        }

            if (tabIndex == 3) {
            item {
                ElevatedCard {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("当前壁纸", fontWeight = FontWeight.SemiBold)
                        if (wallpaperStatus.hasCustomWallpaper) {
                            if (isDebugDevice) {
                                Text("调试模式：这里预留真实壁纸预览区域。")
                            } else {
                                AsyncImage(
                                    model = apiClient.wallpaperPreviewUrl(selectedDevice.ipAddress) + "?t=$wallpaperPreviewSeed",
                                    contentDescription = "当前壁纸",
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Text("已设置自定义壁纸，文件大小 ${wallpaperStatus.fileSize} bytes")
                        } else {
                            Text("当前没有自定义壁纸。")
                        }
                        if (selectedWallpaperUri != null) {
                            AsyncImage(model = selectedWallpaperUri, contentDescription = "待上传壁纸", modifier = Modifier.fillMaxWidth())
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) { Text("选择图片") }
                            Button(onClick = {
                                val uri = selectedWallpaperUri ?: return@Button
                                scope.launch {
                                    if (isDebugDevice) {
                                        wallpaperStatus = wallpaperStatus.copy(hasCustomWallpaper = true, fileSize = 40960)
                                        feedback = "调试模式：已模拟壁纸上传"
                                    } else {
                                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                                        runCatching {
                                            apiClient.uploadWallpaper(selectedDevice.ipAddress, "wallpaper-upload", bytes, context.mimeType(uri))
                                            refreshWallpaper()
                                        }.onSuccess { feedback = "壁纸已上传" }
                                            .onFailure { feedback = it.message ?: "壁纸上传失败" }
                                    }
                                }
                            }, enabled = selectedWallpaperUri != null) { Text("上传壁纸") }
                        }
                        OutlinedButton(onClick = {
                            scope.launch {
                                if (isDebugDevice) {
                                    wallpaperStatus = WallpaperStatus()
                                    selectedWallpaperUri = null
                                    feedback = "调试模式：已恢复默认壁纸"
                                } else {
                                    runCatching { apiClient.resetWallpaper(selectedDevice.ipAddress); refreshWallpaper() }
                                        .onSuccess { feedback = "已恢复默认壁纸" }
                                        .onFailure { feedback = it.message ?: "重置壁纸失败" }
                                }
                            }
                        }) { Text("重置默认") }
                        Text("建议上传 240x240 的 PNG/GIF/JPG/WEBP。首版先走直接上传，设备侧不接受的文件会返回错误。")
                    }
                }
            }
        }
    }

    activeConfigHelp?.let { help ->
        ConfigHelpDialog(
            content = help,
            onDismiss = { activeConfigHelp = null },
        )
    }
    }
}

@Composable
private fun AdvancedConfigOverviewCard(
    productModel: String,
    sectionCount: Int,
    itemCount: Int,
    onRefresh: () -> Unit,
    onSave: () -> Unit,
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("设备配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "按功能分组管理系统开关、灯效参数和硬件兼容项，点右侧 ? 可查看详情说明。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ConfigStatChip("机型", productModel)
                ConfigStatChip("分组", "$sectionCount 组")
                ConfigStatChip("配置项", "$itemCount 项")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onRefresh) { Text("刷新配置") }
                Button(modifier = Modifier.weight(1f), onClick = onSave) { Text("保存配置") }
            }
        }
    }
}

@Composable
private fun ConfigStatChip(title: String, value: String) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    itemCount: Int,
    content: @Composable () -> Unit,
) {
    ElevatedCard {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "$itemCount 项",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            content()
        }
    }
}

@Composable
private fun ConfigSwitchItem(
    title: String,
    checked: Boolean,
    help: ConfigHelpContent,
    onHelpClick: (ConfigHelpContent) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                if (checked) "已开启" else "已关闭",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ConfigInfoButton(onClick = { onHelpClick(help) })
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ConfigPresetNumberItem(
    title: String,
    value: Int,
    help: ConfigHelpContent,
    presets: List<ConfigPresetOption>,
    onHelpClick: (ConfigHelpContent) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ConfigFieldHeader(
            title = title,
            valueSummary = "当前值 $value",
            help = help,
            onHelpClick = onHelpClick,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.forEach { option ->
                FilterChip(
                    selected = value == option.value,
                    onClick = {
                        text = option.value.toString()
                        onValueChange(option.value)
                    },
                    label = { Text(option.label) },
                )
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }
                text = filtered
                filtered.toIntOrNull()?.let(onValueChange)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("手动输入") },
            placeholder = { Text("输入数字") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
private fun ConfigChoiceItem(
    title: String,
    value: Int,
    help: ConfigHelpContent,
    options: List<ConfigPresetOption>,
    onHelpClick: (ConfigHelpContent) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ConfigFieldHeader(
            title = title,
            valueSummary = "当前值 ${options.firstOrNull { it.value == value }?.label ?: value}",
            help = help,
            onHelpClick = onHelpClick,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = value == option.value,
                    onClick = { onValueChange(option.value) },
                    label = { Text(option.label) },
                )
            }
        }
    }
}

@Composable
private fun ConfigHexInputItem(
    title: String,
    value: Int,
    help: ConfigHelpContent,
    presets: List<Int>,
    onHelpClick: (ConfigHelpContent) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toHexDisplay()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ConfigFieldHeader(
            title = title,
            valueSummary = "当前值 #${value.toHexDisplay()}",
            help = help,
            onHelpClick = onHelpClick,
            trailingContent = {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(configColorPreview(value), RoundedCornerShape(6.dp)),
                )
            },
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.forEach { preset ->
                FilterChip(
                    selected = value == preset,
                    onClick = {
                        text = preset.toHexDisplay()
                        onValueChange(preset)
                    },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(configColorPreview(preset), CircleShape),
                            )
                            Text("#${preset.toHexDisplay()}")
                        }
                    },
                )
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val filtered = input
                    .removePrefix("#")
                    .uppercase()
                    .filter { it.isDigit() || it in 'A'..'F' }
                    .take(6)
                text = filtered
                filtered.toIntOrNull(16)?.let(onValueChange)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("手动输入") },
            prefix = { Text("#") },
            placeholder = { Text("RRGGBB") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        )
    }
}

@Composable
private fun ConfigFieldHeader(
    title: String,
    valueSummary: String,
    help: ConfigHelpContent,
    onHelpClick: (ConfigHelpContent) -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                valueSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            trailingContent?.invoke()
            ConfigInfoButton(onClick = { onHelpClick(help) })
        }
    }
}

@Composable
private fun ConfigInfoButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "?",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConfigHelpDialog(
    content: ConfigHelpContent,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了") } },
        title = { Text(content.title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(content.description)
                Text("建议", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    content.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun ConfigDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
    )
}

private fun Int.toHexDisplay(): String = toString(16).uppercase().padStart(6, '0')

private fun configColorPreview(value: Int): Color =
    Color(
        red = ((value shr 16) and 0xFF) / 255f,
        green = ((value shr 8) and 0xFF) / 255f,
        blue = (value and 0xFF) / 255f,
        alpha = 1f,
    )

private fun Context.mimeType(uri: Uri): String {
    return contentResolver.getType(uri) ?: "application/octet-stream"
}

