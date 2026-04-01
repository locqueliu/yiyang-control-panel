package top.yiyang.localcontrol.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yiyang.localcontrol.R
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.ui.asYiyangBrandText
import top.yiyang.localcontrol.ui.displayProductModel
import top.yiyang.localcontrol.ui.displayTitle

@Composable
fun DevicesScreen(
    modifier: Modifier = Modifier,
    devices: List<DeviceSummary>,
    selectedDevice: DeviceSummary?,
    isScanning: Boolean,
    statusText: String,
    onScan: () -> Unit,
    onConnectIp: (String) -> Unit,
    onConnectMac: (String) -> Unit,
    onAddDebugDevice: () -> Unit,
    onSelect: (DeviceSummary) -> Unit,
) {
    var ipInput by rememberSaveable { mutableStateOf("") }
    var macInput by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    WorkbenchBrandHeader()
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ToolMetaChip(
                            label = "扫描状态",
                            value = if (isScanning) "扫描中" else "待命",
                            emphasized = isScanning,
                        )
                        ToolMetaChip("已记录", "${devices.size} 台")
                        ToolMetaChip(
                            label = "当前设备",
                            value = selectedDevice?.displayTitle() ?: "未选择",
                            emphasized = selectedDevice != null,
                        )
                    }
                    ToolFeedbackBanner(
                        text = statusText,
                        emphasized = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onScan,
                            enabled = !isScanning,
                        ) {
                            androidx.compose.material3.Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isScanning) "扫描中..." else "重新扫描")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onAddDebugDevice,
                            enabled = !isScanning,
                        ) {
                            Text("添加调试设备")
                        }
                    }
                }
            }
        }

        item {
            ToolSectionCard(
                title = "快捷连接",
                subtitle = "扫不到设备时，可以直接通过 IP 或 MAC 继续测试。",
                trailing = {
                    ToolStatusBadge(
                        text = if (selectedDevice == null) "未连接" else "可操作",
                        tone = if (selectedDevice == null) ToolStatusTone.Neutral else ToolStatusTone.Positive,
                    )
                },
            ) {
                QuickConnectBlock(
                    title = "IP 直连",
                    value = ipInput,
                    label = "IP 地址",
                    placeholder = "例如 192.168.1.88",
                    primaryText = "连接 IP",
                    onValueChange = { ipInput = it },
                    onClear = { ipInput = "" },
                    onConnect = { onConnectIp(ipInput.trim()) },
                    enabled = !isScanning,
                )
                QuickConnectDivider()
                QuickConnectBlock(
                    title = "MAC 查找连接",
                    value = macInput,
                    label = "MAC 地址",
                    placeholder = "例如 AA:BB:CC:DD:EE:FF",
                    primaryText = "查找并连接",
                    onValueChange = { macInput = it },
                    onClear = { macInput = "" },
                    onConnect = { onConnectMac(macInput.trim()) },
                    enabled = !isScanning,
                )
            }
        }

        if (devices.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "还没找到设备",
                    description = "确认手机与桌宠处于同一 Wi-Fi 或热点网络，然后重新扫描，或用上面的快捷连接继续测试。",
                )
            }
        } else {
            item {
                ToolSectionCard(
                    title = "设备列表",
                subtitle = "点选任意设备后，控制、调试和音乐配置页都会切换到该设备。",
                    trailing = { ToolStatusBadge("${devices.size} 台", ToolStatusTone.Accent) },
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ToolMetaChip("在线", "${devices.count { it.online }} 台")
                        if (devices.any { it.debugMock }) {
                            ToolMetaChip("调试", "${devices.count { it.debugMock }} 台")
                        }
                    }
                }
            }

            items(devices, key = { "${it.ipAddress}-${it.deviceSn}-${it.debugMock}" }) { device ->
                DeviceWorkbenchCard(
                    device = device,
                    isCurrent = selectedDevice?.ipAddress == device.ipAddress,
                    onSelect = { onSelect(device) },
                )
            }
        }
    }
}

@Composable
private fun WorkbenchBrandHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.yiyang_brand_logo),
            contentDescription = "YIYIANG control panel logo",
            modifier = Modifier
                .size(60.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(22.dp)),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = "YIYIANG",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both,
                    ),
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "control panel",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both,
                    ),
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "毅扬智能小狗总控台",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "自动扫描同一网段的机器狗并连接，请确保和小狗处于同一WiFi，若您知道小狗ip地址也可手动添加",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.86f),
            )
        }
    }
}

@Composable
private fun QuickConnectBlock(
    title: String,
    value: String,
    label: String,
    placeholder: String,
    primaryText: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onConnect: () -> Unit,
    enabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onClear,
                enabled = enabled && value.isNotBlank(),
            ) {
                Text("清空")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onConnect,
                enabled = enabled && value.isNotBlank(),
            ) {
                Text(primaryText)
            }
        }
    }
}

@Composable
private fun QuickConnectDivider() {
    androidx.compose.material3.HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
    )
}

@Composable
private fun DeviceWorkbenchCard(
    device: DeviceSummary,
    isCurrent: Boolean,
    onSelect: () -> Unit,
) {
    val subtitle = buildString {
        append(device.displayProductModel().ifBlank { "未知型号" })
        append(" · 固件 ")
        append(device.firmwareVersion.ifBlank { "-" })
    }

    ToolSectionCard(
        title = device.displayTitle(),
        subtitle = subtitle,
        trailing = {
            when {
                isCurrent -> ToolStatusBadge("当前设备", ToolStatusTone.Accent)
                device.debugMock -> ToolStatusBadge("调试设备", ToolStatusTone.Warning)
                else -> ToolStatusBadge("在线", ToolStatusTone.Positive)
            }
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ToolMetaChip("IP", device.ipAddress, emphasized = isCurrent)
            if (device.macAddress.isNotBlank()) {
                ToolMetaChip("MAC", device.macAddress)
            }
            ToolMetaChip("SN", device.deviceSn.ifBlank { "-" })
        }
        if (device.note.isNotBlank()) {
            ToolFeedbackBanner(
                text = device.note.asYiyangBrandText(),
                emphasized = isCurrent,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onSelect,
            ) {
                Text(if (isCurrent) "已设为当前设备" else "设为当前设备")
            }
            if (device.debugMock) {
                ToolStatusBadge("本地假数据", ToolStatusTone.Warning)
            }
        }
    }
}

