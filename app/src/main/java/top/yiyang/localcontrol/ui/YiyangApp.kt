package top.yiyang.localcontrol.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.SettingsRemote
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.yiyang.localcontrol.data.DeviceApiClient
import top.yiyang.localcontrol.data.LocalStorage
import top.yiyang.localcontrol.ui.screen.ControlScreen
import top.yiyang.localcontrol.ui.screen.DevicesScreen
import top.yiyang.localcontrol.ui.screen.FeaturesScreen
import top.yiyang.localcontrol.ui.screen.MusicConfigScreen

private data class AppTab(
    val label: String,
    val icon: ImageVector,
)

private const val DebugPageWarningBody =
    "调试页面包含设备微调、系统配置、唤醒词修改等深度配置操作，部分修改会立即生效，并可能影响设备表现与稳定性，请知悉。"

private const val DebugPageWakewordWarning =
    "注意，修改唤醒词时，请务必保证输入的是中文拼音，否则会导致设备无法唤醒。"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YiyangApp(
    viewModel: MainViewModel,
    apiClient: DeviceApiClient,
    storage: LocalStorage,
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showDebugPageWarning by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.consumeMessage()
    }

    val tabs = remember {
        listOf(
            AppTab("设备", Icons.Outlined.Devices),
            AppTab("控制", Icons.Outlined.SettingsRemote),
            AppTab("调试", Icons.Outlined.Tune),
            AppTab("音乐配置", Icons.Outlined.GraphicEq),
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = index == selectedTabIndex,
                        onClick = {
                            when {
                                index == selectedTabIndex -> Unit
                                index == 2 -> showDebugPageWarning = true
                                else -> selectedTabIndex = index
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> DevicesScreen(
                modifier = Modifier.padding(innerPadding),
                devices = devices,
                selectedDevice = selectedDevice,
                isScanning = isScanning,
                statusText = statusText,
                onScan = viewModel::scanDevices,
                onConnectIp = viewModel::connectToIp,
                onConnectMac = viewModel::connectToMac,
                onAddDebugDevice = viewModel::selectDebugDevice,
                onSelect = viewModel::selectDevice,
            )

            1 -> ControlScreen(
                modifier = Modifier.padding(innerPadding),
                selectedDevice = selectedDevice,
                apiClient = apiClient,
            )

            2 -> FeaturesScreen(
                modifier = Modifier.padding(innerPadding),
                selectedDevice = selectedDevice,
                apiClient = apiClient,
            )

            else -> MusicConfigScreen(
                modifier = Modifier.padding(innerPadding),
                selectedDevice = selectedDevice,
                apiClient = apiClient,
                storage = storage,
            )
        }
    }

    if (showDebugPageWarning) {
        AlertDialog(
            onDismissRequest = { showDebugPageWarning = false },
            title = { Text("调试模式提示") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = DebugPageWarningBody,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = DebugPageWakewordWarning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDebugPageWarning = false }) {
                    Text("取消")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDebugPageWarning = false
                        selectedTabIndex = 2
                    },
                ) {
                    Text("继续")
                }
            },
        )
    }
}


