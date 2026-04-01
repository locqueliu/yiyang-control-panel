package top.yiyang.localcontrol.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AddAlarm
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.math.roundToInt
import top.yiyang.localcontrol.data.DeviceApiClient
import top.yiyang.localcontrol.model.ActionOption
import top.yiyang.localcontrol.model.AlarmConfig
import top.yiyang.localcontrol.model.DeviceAction
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.model.LampState
import top.yiyang.localcontrol.ui.displayTitle
import top.yiyang.localcontrol.ui.DebugMockData

private val actionCatalog = listOf(
    DeviceAction(1, "立正"),
    DeviceAction(2, "坐下"),
    DeviceAction(3, "趴下", listOf(ActionOption(1, "后腿朝前"), ActionOption(2, "前腿朝前"))),
    DeviceAction(4, "睡觉"),
    DeviceAction(9, "摇尾巴"),
    DeviceAction(10, "尾巴控制", listOf(ActionOption(1, "抬起"), ActionOption(2, "放下"))),
    DeviceAction(11, "摇摆", listOf(ActionOption(1, "左右摇摆"), ActionOption(2, "前后摇摆"))),
    DeviceAction(12, "挠痒"),
    DeviceAction(13, "挥手"),
    DeviceAction(15, "伸懒腰"),
    DeviceAction(16, "后退拉伸"),
    DeviceAction(17, "展示部位", listOf(ActionOption(1, "左爪"), ActionOption(2, "右爪"), ActionOption(3, "左腿"), ActionOption(4, "右腿"))),
    DeviceAction(18, "跪拜"),
)

private data class ActionSection(
    val title: String,
    val description: String,
    val actionIds: List<Int>,
)

private data class LampModeOption(
    val value: String,
    val label: String,
    val subtitle: String,
)

private data class LampPresetColor(
    val label: String,
    val red: Int,
    val green: Int,
    val blue: Int,
)

private val actionSections = listOf(
    ActionSection(
        title = "姿态动作",
        description = "常用姿态切换，适合快速预设动作。",
        actionIds = listOf(1, 2, 3, 4),
    ),
    ActionSection(
        title = "尾部与律动",
        description = "尾巴、摆动和节奏型动作。",
        actionIds = listOf(9, 10, 11),
    ),
    ActionSection(
        title = "互动展示",
        description = "互动表演和部位展示动作。",
        actionIds = listOf(12, 13, 15, 16, 17, 18),
    ),
)

private val lampModeCatalog = listOf(
    LampModeOption("on", "常亮", "稳定常亮"),
    LampModeOption("breathe", "呼吸灯", "缓慢明暗"),
    LampModeOption("rainbow", "彩虹灯", "多彩渐变"),
    LampModeOption("flow", "流水灯", "连续流动"),
    LampModeOption("flash", "闪光灯", "快速闪烁"),
    LampModeOption("custom", "自定义颜色", "自选主色"),
    LampModeOption("audio", "音频响应", "跟随声音"),
)

private val lampPresetCatalog = listOf(
    LampPresetColor("\u6696\u674f", 255, 214, 170),
    LampPresetColor("\u6d77\u84dd", 96, 194, 255),
    LampPresetColor("\u8584\u8377", 79, 211, 196),
    LampPresetColor("\u8349\u7eff", 130, 220, 120),
    LampPresetColor("\u6a59\u5149", 255, 166, 76),
    LampPresetColor("\u73ab\u7c89", 255, 105, 143),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlScreen(
    modifier: Modifier = Modifier,
    selectedDevice: DeviceSummary?,
    apiClient: DeviceApiClient,
) {
    if (selectedDevice == null) {
        LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp)) {
            item { EmptyStateCard("还没选设备", "先去“设备”页选一台在线桌宠，再回来控制。") }
        }
        return
    }

    val scope = rememberCoroutineScope()
    val contentListState = rememberLazyListState()
    val isDebugDevice = selectedDevice.debugMock
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var headerExpanded by rememberSaveable(selectedDevice.ipAddress) { mutableStateOf(true) }
    var speed by rememberSaveable { mutableFloatStateOf(50f) }
    var activeDirection by remember { mutableStateOf<Int?>(null) }
    var expandedActionId by rememberSaveable { mutableIntStateOf(-1) }
    var feedback by remember {
        mutableStateOf(
            if (isDebugDevice) "调试模拟设备：当前页面使用本地假数据。"
            else "当前设备：${selectedDevice.displayTitle()}",
        )
    }
    var lampEnabled by rememberSaveable { mutableStateOf(false) }
    var lampMode by rememberSaveable { mutableStateOf("on") }
    var lampBrightness by rememberSaveable { mutableFloatStateOf(80f) }
    var lampSpeed by rememberSaveable { mutableFloatStateOf(5f) }
    var lampRed by rememberSaveable { mutableIntStateOf(255) }
    var lampGreen by rememberSaveable { mutableIntStateOf(214) }
    var lampBlue by rememberSaveable { mutableIntStateOf(170) }
    var volume by remember { mutableFloatStateOf(70f) }
    var alarmsLoading by remember { mutableStateOf(false) }
    val alarms = remember { mutableStateListOf<AlarmConfig>() }
    val actionOptions = remember { mutableStateMapOf(3 to 1, 10 to 1, 11 to 1, 17 to 1) }
    var editingAlarm by remember { mutableStateOf<AlarmConfig?>(null) }

    suspend fun refreshUtilities() {
        alarmsLoading = true
        if (isDebugDevice) {
            volume = 46f
            alarms.clear()
            alarms.addAll(DebugMockData.alarms())
            feedback = "调试模式已载入音量和闹钟假数据"
            alarmsLoading = false
            return
        }
        runCatching {
            volume = apiClient.getVolume(selectedDevice.ipAddress).toFloat()
            alarms.clear()
            alarms.addAll(apiClient.getAlarms(selectedDevice.ipAddress))
            feedback = "已同步音量和闹钟列表"
        }.onFailure { feedback = it.message ?: "读取设备数据失败" }
        alarmsLoading = false
    }

    suspend fun rebootCurrentDevice() {
        if (isDebugDevice) {
            feedback = "调试模式：模拟发送重启命令"
            return
        }
        runCatching { apiClient.rebootDevice(selectedDevice.ipAddress) }
            .onSuccess { feedback = "重启命令已发送，设备约 30 秒后恢复" }
            .onFailure { feedback = it.message ?: "重启失败" }
    }

    suspend fun enterWifiConfigMode() {
        if (isDebugDevice) {
            feedback = "调试模式：模拟进入配网模式"
            return
        }
        runCatching { apiClient.enterWifiConfig(selectedDevice.ipAddress) }
            .onSuccess { feedback = "设备将进入配网模式，请准备连接热点" }
            .onFailure { feedback = it.message ?: "进入配网模式失败" }
    }

    LaunchedEffect(selectedDevice.ipAddress) {
        refreshUtilities()
    }

    LaunchedEffect(contentListState, tabIndex, selectedDevice.ipAddress) {
        snapshotFlow { contentListState.firstVisibleItemIndex to contentListState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (headerExpanded && (index > 0 || offset >= 56)) {
                    headerExpanded = false
                }
            }
    }

    fun actionSpeed() = (350 - ((speed - 1f) * 250f / 99f)).toInt().coerceIn(100, 350)
    fun lightSpeed() = (1 + ((speed - 1f) * 4f / 99f)).toInt().coerceIn(1, 5)
    fun tailSpeed() = (2 + ((speed - 1f) * 6f / 99f)).toInt().coerceIn(2, 8)
    val isCustomLampMode = lampMode == "custom"
    val lampHexLabel = lampColorHex(lampRed, lampGreen, lampBlue)
    val lampPreview = lampPreviewColor(lampRed, lampGreen, lampBlue)
    val selectedLampPreset = lampPresetCatalog.firstOrNull {
        it.red == lampRed && it.green == lampGreen && it.blue == lampBlue
    }

    fun buildActionPayload(action: DeviceAction) = buildJsonObject {
        when (action.id) {
            3 -> put("direction", JsonPrimitive(actionOptions[action.id] ?: 1))
            9 -> {
                put("speed", JsonPrimitive(tailSpeed()))
                put("part_id", JsonPrimitive(1))
            }
            10 -> put("part_id", JsonPrimitive(actionOptions[action.id] ?: 1))
            11 -> {
                put("speed", JsonPrimitive(lightSpeed()))
                put("direction", JsonPrimitive(actionOptions[action.id] ?: 1))
            }
            12 -> put("speed", JsonPrimitive(tailSpeed()))
            13 -> {
                put("steps", JsonPrimitive(5))
                put("speed", JsonPrimitive(tailSpeed()))
            }
            17 -> {
                put("speed", JsonPrimitive(tailSpeed()))
                put("part_id", JsonPrimitive(actionOptions[action.id] ?: 1))
            }
        }
    }

    fun buildLampState(targetEnabled: Boolean): LampState = LampState(
        action = if (targetEnabled) lampMode else "off",
        speed = lampSpeed.toInt().coerceIn(1, 10),
        brightness = lampBrightness.toInt().coerceIn(5, 100),
        red = lampRed.coerceIn(0, 255),
        green = lampGreen.coerceIn(0, 255),
        blue = lampBlue.coerceIn(0, 255),
    )

    fun lampFeedbackMessage(targetEnabled: Boolean): String {
        val prefix = if (isDebugDevice) "\u8c03\u8bd5\u6a21\u5f0f\uff1a" else ""
        return when {
            !targetEnabled -> "${prefix}\u706f\u5149\u5df2\u5173\u95ed"
            lampMode == "custom" -> "${prefix}\u5df2\u5e94\u7528\u81ea\u5b9a\u4e49\u989c\u8272 $lampHexLabel"
            else -> "${prefix}\u5df2\u5e94\u7528${lampModeLabel(lampMode)}\u706f\u6548"
        }
    }

    suspend fun applyLampState(targetEnabled: Boolean) {
        val nextState = buildLampState(targetEnabled)
        if (isDebugDevice) {
            lampEnabled = targetEnabled
            feedback = lampFeedbackMessage(targetEnabled)
            return
        }

        runCatching {
            apiClient.setLampState(selectedDevice.ipAddress, nextState)
        }.onSuccess {
            lampEnabled = targetEnabled
            feedback = lampFeedbackMessage(targetEnabled)
        }.onFailure {
            feedback = it.message ?: "灯光控制失败"
        }
    }

    fun syncLampIfEnabled() {
        if (!lampEnabled) return
        scope.launch { applyLampState(true) }
    }

    suspend fun executeAction(action: DeviceAction) {
        expandedActionId = -1
        val optionSummary = currentActionOptionLabel(action, actionOptions)
            ?.let { " · $it" }
            .orEmpty()

        if (isDebugDevice) {
            feedback = "调试模式：动作“${action.label}$optionSummary”已执行"
            return
        }

        val payload = buildActionPayload(action)
        runCatching {
            apiClient.triggerAction(selectedDevice.ipAddress, action.id, payload)
            feedback = "动作“${action.label}$optionSummary”已执行"
        }.onFailure { feedback = it.message ?: "动作失败" }
    }

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
                emphasized = true,
                collapsed = !headerExpanded,
                onToggle = { headerExpanded = !headerExpanded },
                actions = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "系统快捷",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            HeaderQuickActionButton(
                                modifier = Modifier.weight(1f),
                                text = "重启设备",
                                icon = Icons.Outlined.Refresh,
                                onClick = { scope.launch { rebootCurrentDevice() } },
                                primary = true,
                            )
                            HeaderQuickActionButton(
                                modifier = Modifier.weight(1f),
                                text = "配网模式",
                                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                                onClick = { scope.launch { enterWifiConfigMode() } },
                            )
                        }
                    }
                },
            )
            ToolTabRow(
                selectedTabIndex = tabIndex,
                titles = listOf("控制功能", "实用工具"),
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
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("速度设置 ${speed.toInt()}%", fontWeight = FontWeight.SemiBold)
                        Slider(value = speed, onValueChange = { speed = it }, valueRange = 1f..100f)
                    }
                }
            }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("方向控制", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "按住方向键持续发送，松开立即停止。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        DirectionPad(
                            activeDirection = activeDirection,
                            onPress = { actionId ->
                                scope.launch {
                                    if (isDebugDevice) {
                                        activeDirection = actionId
                                        feedback = "调试模式：方向动作 ${directionLabel(actionId)} 已触发"
                                    } else {
                                        runCatching {
                                            activeDirection = actionId
                                            apiClient.startDirectionalAction(selectedDevice.ipAddress, actionId, actionSpeed())
                                            feedback = "方向动作已发送"
                                        }.onFailure {
                                            activeDirection = null
                                            feedback = it.message ?: "方向控制失败"
                                        }
                                    }
                                }
                            },
                            onRelease = {
                                scope.launch {
                                    if (!isDebugDevice) {
                                        runCatching { apiClient.stopDirectionalAction(selectedDevice.ipAddress) }
                                    } else {
                                        feedback = "调试模式：方向动作已停止"
                                    }
                                    activeDirection = null
                                }
                            },
                        )
                    }
                }
            }
            item {
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("动作控制", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "常用动作按分组排列。带选项的动作点击后会在按钮旁展开，选择后立即执行。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        actionSections.forEach { section ->
                            val actions = section.actionIds.mapNotNull { actionId ->
                                actionCatalog.firstOrNull { it.id == actionId }
                            }
                            ActionSectionLayout(
                                title = section.title,
                                description = section.description,
                                actions = actions,
                                expandedActionId = expandedActionId,
                                actionOptions = actionOptions,
                                onActionClick = { action ->
                                    if (action.options.isNotEmpty()) {
                                        expandedActionId = if (expandedActionId == action.id) -1 else action.id
                                    } else {
                                        scope.launch {
                                            executeAction(action)
                                        }
                                    }
                                },
                                onOptionClick = { action, option ->
                                    actionOptions[action.id] = option.id
                                    scope.launch { executeAction(action) }
                                },
                                onDismissOptions = { expandedActionId = -1 },
                            )
                        }
                    }
                }
            }
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .padding(18.dp)
                                .animateContentSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text("灯光控制", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "灯光开关负责启停；开启后模式、颜色和参数会立即生效。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                "灯光总开关",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                if (lampEnabled) "当前已开启" else "当前已关闭",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        Switch(
                                            checked = lampEnabled,
                                            onCheckedChange = { enabled ->
                                                scope.launch {
                                                    applyLampState(enabled)
                                                }
                                            },
                                        )
                                    }
                                    Text(
                                        if (lampEnabled) {
                                            "当前模式 ${lampModeLabel(lampMode)}，其余设置点击后会立即响应。"
                                        } else {
                                            "可先预设模式和参数，打开后立即按当前预设生效。"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("灯效模式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (lampEnabled) "点击后立即切换当前灯效。" else "可先选择模式，开启灯光后立即生效。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                lampModeCatalog.chunked(3).forEach { modeRow ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        modeRow.forEach { mode ->
                                            LampModeTile(
                                                modifier = Modifier.weight(1f),
                                                mode = mode,
                                                selected = lampMode == mode.value,
                                                onClick = {
                                                    lampMode = mode.value
                                                    syncLampIfEnabled()
                                                },
                                            )
                                        }
                                        repeat(3 - modeRow.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            if (isCustomLampMode) {
                                ElevatedCard(
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        Text(
                                            "\u81ea\u5b9a\u4e49\u989c\u8272",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            if (lampEnabled) {
                                                "预设色和 RGB 微调会立即同步到设备。"
                                            } else {
                                                "可先预设颜色，开启灯光后按当前颜色生效。"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .background(lampPreview, RoundedCornerShape(18.dp)),
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    lampHexLabel,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                Text(
                                                    selectedLampPreset?.label ?: "\u81ea\u5b9a\u4e49\u6df7\u8272",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Text(
                                                    "RGB $lampRed / $lampGreen / $lampBlue",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                        lampPresetCatalog.chunked(3).forEach { presetRow ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            ) {
                                                presetRow.forEach { preset ->
                                                    LampPresetTile(
                                                        modifier = Modifier.weight(1f),
                                                        preset = preset,
                                                        selected = preset.red == lampRed &&
                                                            preset.green == lampGreen &&
                                                            preset.blue == lampBlue,
                                                        onClick = {
                                                            lampRed = preset.red
                                                            lampGreen = preset.green
                                                            lampBlue = preset.blue
                                                            syncLampIfEnabled()
                                                        },
                                                    )
                                                }
                                                repeat(3 - presetRow.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                        Text("\u7ea2\u8272 $lampRed", fontWeight = FontWeight.SemiBold)
                                        Slider(
                                            value = lampRed.toFloat(),
                                            onValueChange = { lampRed = it.roundToInt().coerceIn(0, 255) },
                                            onValueChangeFinished = { syncLampIfEnabled() },
                                            valueRange = 0f..255f,
                                        )
                                        TimeScaleLabels(start = "0", middle = "128", end = "255")
                                        Text("\u7eff\u8272 $lampGreen", fontWeight = FontWeight.SemiBold)
                                        Slider(
                                            value = lampGreen.toFloat(),
                                            onValueChange = { lampGreen = it.roundToInt().coerceIn(0, 255) },
                                            onValueChangeFinished = { syncLampIfEnabled() },
                                            valueRange = 0f..255f,
                                        )
                                        TimeScaleLabels(start = "0", middle = "128", end = "255")
                                        Text("\u84dd\u8272 $lampBlue", fontWeight = FontWeight.SemiBold)
                                        Slider(
                                            value = lampBlue.toFloat(),
                                            onValueChange = { lampBlue = it.roundToInt().coerceIn(0, 255) },
                                            onValueChangeFinished = { syncLampIfEnabled() },
                                            valueRange = 0f..255f,
                                        )
                                        TimeScaleLabels(start = "0", middle = "128", end = "255")
                                    }
                                }
                            }

                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        "灯效参数",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        "速度 ${lampSpeed.toInt()} · 亮度 ${lampBrightness.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text("灯效速度 ${lampSpeed.toInt()}", fontWeight = FontWeight.SemiBold)
                                    Slider(
                                        value = lampSpeed,
                                        onValueChange = { lampSpeed = it },
                                        onValueChangeFinished = { syncLampIfEnabled() },
                                        valueRange = 1f..10f,
                                    )
                                    Text("灯光亮度 ${lampBrightness.toInt()}%", fontWeight = FontWeight.SemiBold)
                                    Slider(
                                        value = lampBrightness,
                                        onValueChange = { lampBrightness = it },
                                        onValueChangeFinished = { syncLampIfEnabled() },
                                        valueRange = 5f..100f,
                                    )
                                    Text(
                                        if (lampEnabled) {
                                            "当前效果：${lampModeLabel(lampMode)}${if (isCustomLampMode) " · $lampHexLabel" else ""}，调整后会立即生效。"
                                        } else {
                                            "当前预设：${lampModeLabel(lampMode)}${if (isCustomLampMode) " · $lampHexLabel" else ""}。"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (tabIndex == 1) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text("音量控制", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "快速调整当前设备音量。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("当前音量 ${volume.toInt()}")
                        Slider(value = volume, onValueChange = { volume = it }, valueRange = 0f..100f, onValueChangeFinished = {
                            scope.launch {
                                if (isDebugDevice) {
                                    feedback = "调试模式：音量已调整到 ${volume.toInt()}"
                                } else {
                                    runCatching { apiClient.setVolume(selectedDevice.ipAddress, volume.toInt()) }
                                        .onSuccess { feedback = "音量已更新" }
                                        .onFailure { feedback = it.message ?: "音量设置失败" }
                                }
                            }
                        })
                    }
                }
            }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text("闹钟管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "新增、编辑和管理设备闹钟。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { editingAlarm = AlarmConfig() },
                            ) { Icon(Icons.Outlined.AddAlarm, null); Text("添加闹钟") }
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { scope.launch { refreshUtilities() } },
                                enabled = !alarmsLoading,
                            ) {
                                Text(if (alarmsLoading) "刷新中..." else "刷新列表")
                            }
                        }
                        if (alarms.isEmpty()) {
                            Text("当前设备还没有闹钟。")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                alarms.forEach { alarm ->
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                        ),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            Text(
                                                "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')} · ${alarm.label}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                "重复：${repeatDaysLabel(alarm.repeatDays)} · 动作：${alarm.actionType} · 铃声：${alarm.ringtoneType}",
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Switch(checked = alarm.enabled, onCheckedChange = { enabled ->
                                                    scope.launch {
                                                        if (isDebugDevice) {
                                                            val index = alarms.indexOf(alarm)
                                                            if (index >= 0) alarms[index] = alarm.copy(enabled = enabled)
                                                            feedback = if (enabled) "调试模式：闹钟已启用" else "调试模式：闹钟已停用"
                                                        } else {
                                                            runCatching { apiClient.toggleAlarm(selectedDevice.ipAddress, alarm.index ?: return@launch, enabled) }
                                                                .onSuccess { feedback = if (enabled) "闹钟已启用" else "闹钟已停用"; refreshUtilities() }
                                                                .onFailure { feedback = it.message ?: "闹钟切换失败" }
                                                        }
                                                    }
                                                })
                                                OutlinedButton(
                                                    modifier = Modifier.weight(1f),
                                                    onClick = { editingAlarm = alarm },
                                                ) { Text("编辑") }
                                                OutlinedButton(
                                                    modifier = Modifier.weight(1f),
                                                    onClick = {
                                                        scope.launch {
                                                            if (isDebugDevice) {
                                                                alarms.remove(alarm)
                                                                feedback = "调试模式：闹钟已删除"
                                                            } else {
                                                                runCatching { apiClient.deleteAlarm(selectedDevice.ipAddress, alarm.index ?: return@launch) }
                                                                    .onSuccess { feedback = "闹钟已删除"; refreshUtilities() }
                                                                    .onFailure { feedback = it.message ?: "删除失败" }
                                                            }
                                                        }
                                                    },
                                                ) { Text("删除") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    editingAlarm?.let { original ->
        AlarmEditorDialog(
            alarm = original,
            onDismiss = { editingAlarm = null },
            onConfirm = { updated ->
                scope.launch {
                    if (isDebugDevice) {
                        val normalized = updated.copy(
                            index = updated.index ?: ((alarms.maxOfOrNull { it.index ?: -1 } ?: -1) + 1),
                        )
                        val existingIndex = alarms.indexOfFirst { it.index == normalized.index }
                        if (existingIndex >= 0) alarms[existingIndex] = normalized else alarms.add(normalized)
                        feedback = "调试模式：闹钟已保存"
                        editingAlarm = null
                    } else {
                        runCatching { apiClient.saveAlarm(selectedDevice.ipAddress, updated) }
                            .onSuccess { feedback = "闹钟已保存"; editingAlarm = null; refreshUtilities() }
                            .onFailure { feedback = it.message ?: "保存闹钟失败" }
                    }
                }
            },
        )
    }
}

@Composable
private fun DirectionPad(
    activeDirection: Int?,
    onPress: (Int) -> Unit,
    onRelease: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val padSize = maxWidth.coerceAtMost(296.dp)
        val buttonSize = when {
            padSize < 250.dp -> 74.dp
            padSize < 286.dp -> 84.dp
            else -> 92.dp
        }
        val centerSize = when {
            padSize < 250.dp -> 78.dp
            padSize < 286.dp -> 88.dp
            else -> 96.dp
        }

        Box(
            modifier = Modifier.size(padSize),
            contentAlignment = Alignment.Center,
        ) {
            DirectionButton(
                modifier = Modifier.align(Alignment.TopCenter),
                label = "前进",
                icon = Icons.Outlined.ArrowUpward,
                active = activeDirection == 5,
                actionId = 5,
                buttonSize = buttonSize,
                onPress = onPress,
                onRelease = onRelease,
            )
            DirectionButton(
                modifier = Modifier.align(Alignment.CenterStart),
                label = "左转",
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                active = activeDirection == 7,
                actionId = 7,
                buttonSize = buttonSize,
                onPress = onPress,
                onRelease = onRelease,
            )
            DirectionCenterBadge(
                modifier = Modifier.align(Alignment.Center),
                activeDirection = activeDirection,
                badgeSize = centerSize,
            )
            DirectionButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                label = "右转",
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                active = activeDirection == 8,
                actionId = 8,
                buttonSize = buttonSize,
                onPress = onPress,
                onRelease = onRelease,
            )
            DirectionButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                label = "后退",
                icon = Icons.Outlined.ArrowDownward,
                active = activeDirection == 6,
                actionId = 6,
                buttonSize = buttonSize,
                onPress = onPress,
                onRelease = onRelease,
            )
        }
    }
}

@Composable
private fun DirectionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    actionId: Int,
    buttonSize: androidx.compose.ui.unit.Dp,
    onPress: (Int) -> Unit,
    onRelease: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .size(buttonSize)
            .pointerInput(actionId) {
            detectTapGestures(
                onPress = {
                    onPress(actionId)
                    tryAwaitRelease()
                    onRelease()
                },
            )
        },
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DirectionCenterBadge(
    modifier: Modifier = Modifier,
    activeDirection: Int?,
    badgeSize: androidx.compose.ui.unit.Dp,
) {
    val title = when (activeDirection) {
        5 -> "前进中"
        6 -> "后退中"
        7 -> "左转中"
        8 -> "右转中"
        else -> "待命"
    }
    val subtitle = if (activeDirection == null) "按住发送" else "松开停止"

    ElevatedCard(
        modifier = modifier.size(badgeSize),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (activeDirection == null) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActionSectionLayout(
    title: String,
    description: String,
    actions: List<DeviceAction>,
    expandedActionId: Int,
    actionOptions: Map<Int, Int>,
    onActionClick: (DeviceAction) -> Unit,
    onOptionClick: (DeviceAction, ActionOption) -> Unit,
    onDismissOptions: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        actions.chunked(3).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowActions.forEach { action ->
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        action = action,
                        optionSummary = currentActionOptionLabel(action, actionOptions),
                        selectedOptionId = actionOptions[action.id],
                        expanded = expandedActionId == action.id,
                        onClick = { onActionClick(action) },
                        onOptionClick = { option -> onOptionClick(action, option) },
                        onDismissOptions = onDismissOptions,
                    )
                }
                repeat(3 - rowActions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ActionTile(
    modifier: Modifier = Modifier,
    action: DeviceAction,
    optionSummary: String?,
    selectedOptionId: Int?,
    expanded: Boolean,
    onClick: () -> Unit,
    onOptionClick: (ActionOption) -> Unit,
    onDismissOptions: () -> Unit,
) {
    Box(modifier = modifier) {
        SelectionTile(
            modifier = Modifier.fillMaxWidth(),
            title = action.label,
            subtitle = when {
                action.options.isEmpty() -> "点击即执行"
                expanded -> "选择后立即执行"
                optionSummary.isNullOrBlank() -> "点击展开选项"
                else -> "当前：$optionSummary"
            },
            selected = expanded,
            onClick = onClick,
        )
        if (action.options.isNotEmpty()) {
            ActionOptionDropdownMenu(
                action = action,
                selectedOptionId = selectedOptionId,
                expanded = expanded,
                onDismissRequest = onDismissOptions,
                onOptionClick = onOptionClick,
            )
        }
    }
}

@Composable
private fun ActionOptionDropdownMenu(
    action: DeviceAction,
    selectedOptionId: Int?,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onOptionClick: (ActionOption) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.widthIn(min = 228.dp, max = 260.dp),
        offset = DpOffset(0.dp, 8.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 14.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    action.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "选择后立即执行",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            action.options.forEach { option ->
                ActionOptionMenuButton(
                    option = option,
                    selected = selectedOptionId == option.id,
                    onClick = { onOptionClick(option) },
                )
            }
        }
    }
}

@Composable
private fun ActionOptionMenuButton(
    option: ActionOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (selected) "当前默认选项" else "点击立即执行",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            if (selected) {
                Text(
                    "当前",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
    if (selected) {
        Button(
            modifier = buttonModifier,
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        ) {
            content()
        }
    } else {
        OutlinedButton(
            modifier = buttonModifier,
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun LampModeTile(
    modifier: Modifier = Modifier,
    mode: LampModeOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    SelectionTile(
        modifier = modifier,
        title = mode.label,
        subtitle = if (selected) "当前使用" else mode.subtitle,
        selected = selected,
        onClick = onClick,
    )
}

@Composable
private fun LampPresetTile(
    modifier: Modifier = Modifier,
    preset: LampPresetColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val buttonModifier = modifier.heightIn(min = 78.dp)
    val swatchColor = lampPreviewColor(preset.red, preset.green, preset.blue)
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(swatchColor, RoundedCornerShape(9.dp)),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    preset.label,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    lampColorHex(preset.red, preset.green, preset.blue),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
    if (selected) {
        Button(
            modifier = buttonModifier,
            onClick = onClick,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            content()
        }
    } else {
        OutlinedButton(
            modifier = buttonModifier,
            onClick = onClick,
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SelectionTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val buttonModifier = modifier.heightIn(min = 76.dp)
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Start,
            )
        }
    }
    if (selected) {
        Button(
            modifier = buttonModifier,
            onClick = onClick,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            content()
        }
    } else {
        OutlinedButton(
            modifier = buttonModifier,
            onClick = onClick,
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun AlarmEditorDialog(
    alarm: AlarmConfig,
    onDismiss: () -> Unit,
    onConfirm: (AlarmConfig) -> Unit,
) {
    var hour by remember(alarm) { mutableIntStateOf(alarm.hour) }
    var minute by remember(alarm) { mutableIntStateOf(alarm.minute) }
    var label by remember(alarm) { mutableStateOf(alarm.label) }
    var repeatDays by remember(alarm) { mutableIntStateOf(alarm.repeatDays) }
    var actionType by remember(alarm) { mutableIntStateOf(alarm.actionType) }
    var ringtoneType by remember(alarm) { mutableIntStateOf(alarm.ringtoneType) }

    AlertDialog(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    alarm.copy(
                        hour = hour.coerceIn(0, 23),
                        minute = minute.coerceIn(0, 59),
                        label = label.ifBlank { "闹钟" },
                        repeatDays = repeatDays,
                        actionType = actionType,
                        ringtoneType = ringtoneType,
                    ),
                )
            }) { Text("保存") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } },
        title = { Text(if (alarm.index == null) "添加闹钟" else "编辑闹钟") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            formatAlarmTime(hour, minute),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "拖动滑块设置闹钟时间",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("小时 ${hour.toString().padStart(2, '0')}", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = hour.toFloat(),
                        onValueChange = { hour = it.roundToInt().coerceIn(0, 23) },
                        valueRange = 0f..23f,
                        steps = 22,
                    )
                    TimeScaleLabels(start = "00", middle = "12", end = "23")
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("分钟 ${minute.toString().padStart(2, '0')}", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = minute.toFloat(),
                        onValueChange = { minute = it.roundToInt().coerceIn(0, 59) },
                        valueRange = 0f..59f,
                        steps = 58,
                    )
                    TimeScaleLabels(start = "00", middle = "30", end = "59")
                }
                OutlinedTextField(label, { label = it }, label = { Text("标签") }, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "一次性", 127 to "每天", 62 to "工作日", 65 to "周末").forEach { (value, text) ->
                        FilterChip(selected = repeatDays == value, onClick = { repeatDays = value }, label = { Text(text) })
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "无动作", 1 to "立正", 2 to "坐下", 3 to "趴下", 4 to "睡觉", 9 to "摇尾巴").forEach { (value, text) ->
                        FilterChip(selected = actionType == value, onClick = { actionType = value }, label = { Text(text) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = ringtoneType == 0, onClick = { ringtoneType = 0 }, label = { Text("铃声 1") })
                    FilterChip(selected = ringtoneType == 1, onClick = { ringtoneType = 1 }, label = { Text("铃声 2") })
                }
            }
        },
    )
}

@Composable
private fun TimeScaleLabels(
    start: String,
    middle: String,
    end: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(start, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.weight(1f))
        Text(middle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.weight(1f))
        Text(end, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun repeatDaysLabel(value: Int): String = when (value) {
    0 -> "一次性"
    127 -> "每天"
    62 -> "工作日"
    65 -> "周末"
    else -> "自定义($value)"
}

private fun lampModeLabel(value: String): String =
    lampModeCatalog.firstOrNull { it.value == value }?.label ?: value

private fun lampPreviewColor(red: Int, green: Int, blue: Int): Color =
    Color(
        red = red.coerceIn(0, 255) / 255f,
        green = green.coerceIn(0, 255) / 255f,
        blue = blue.coerceIn(0, 255) / 255f,
        alpha = 1f,
    )

private fun lampColorHex(red: Int, green: Int, blue: Int): String =
    "#%02X%02X%02X".format(
        red.coerceIn(0, 255),
        green.coerceIn(0, 255),
        blue.coerceIn(0, 255),
    )

private fun directionLabel(actionId: Int): String = when (actionId) {
    5 -> "前进"
    6 -> "后退"
    7 -> "左转"
    8 -> "右转"
    else -> "未知方向"
}

private fun currentActionOptionLabel(
    action: DeviceAction,
    actionOptions: Map<Int, Int>,
): String? {
    if (action.options.isEmpty()) return null
    val selectedId = actionOptions[action.id] ?: action.options.first().id
    return action.options.firstOrNull { it.id == selectedId }?.label
}

private fun formatAlarmTime(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

