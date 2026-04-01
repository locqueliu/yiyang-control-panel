package top.yiyang.localcontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.yiyang.localcontrol.data.DiscoveryScanner
import top.yiyang.localcontrol.data.LocalStorage
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.ui.DebugMockData
import top.yiyang.localcontrol.ui.asYiyangBrandText

class MainViewModel(
    private val storage: LocalStorage,
    private val scanner: DiscoveryScanner,
) : ViewModel() {
    private val _devices = MutableStateFlow(storage.loadDevices().sortedBy(::deviceSortKey))
    val devices: StateFlow<List<DeviceSummary>> = _devices.asStateFlow()

    private val _selectedDevice = MutableStateFlow(
        _devices.value.firstOrNull { it.ipAddress == storage.loadSelectedDeviceIp() },
    )
    val selectedDevice: StateFlow<DeviceSummary?> = _selectedDevice.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _statusText = MutableStateFlow(
        if (_devices.value.isEmpty()) {
            "测试版支持全网段扫描、手动 IP 连接和 MAC 反查连接。"
        } else {
            "已缓存 ${_devices.value.size} 台设备，可重新扫描或直接继续连接。"
        },
    )
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        if (_devices.value.isEmpty()) {
            scanDevices()
        }
    }

    fun scanDevices() {
        launchBusy("正在扫描全部局域网地址，请稍候...") {
            val report = scanner.scanLocalNetwork()
            val scanned = report.devices
            if (scanned.isNotEmpty()) {
                _devices.value = mergeDevices(scanned + _devices.value)
                storage.saveDevices(_devices.value)
                restoreSelectedDevice()
                _statusText.value = "已扫描 ${report.rangeSummary}，共 ${report.totalHosts} 个地址，发现 ${scanned.size} 台设备。"
                _message.value = "扫描到 ${scanned.size} 台设备"
            } else {
                _statusText.value =
                    "已扫描 ${report.rangeSummary}，共 ${report.totalHosts} 个地址，ARP 命中 ${report.arpMatchedHosts} 个，可尝试手动输入 IP 或 MAC 地址继续连接。"
                _message.value = "未扫描到设备，请尝试手动输入 IP 或 MAC 地址"
            }
        }
    }

    fun connectToIp(ipAddress: String) {
        launchBusy("正在尝试连接 IP 地址 $ipAddress ...") {
            val device = scanner.connectByIp(ipAddress)
            val saved = upsertDevice(device)
            selectDevice(saved)
            _statusText.value = if (saved.deviceName == "手动连接设备") {
                "已按手动地址加入设备列表，可继续测试控制与配置接口。"
            } else {
                "已通过 IP 地址连接到 ${saved.title.asYiyangBrandText()}。"
            }
            _message.value = "已连接 ${saved.title.asYiyangBrandText()}（${saved.ipAddress}）"
        }
    }

    fun connectToMac(macAddress: String) {
        launchBusy("正在通过 MAC 地址查找设备，请稍候...") {
            val device = scanner.connectByMac(macAddress)
            val saved = upsertDevice(device)
            selectDevice(saved)
            _statusText.value = "已根据 MAC 地址定位到 ${saved.ipAddress} 并完成连接。"
            _message.value = "MAC 查找成功，已连接 ${saved.title.asYiyangBrandText()}"
        }
    }

    fun selectDebugDevice() {
        val device = upsertDevice(DebugMockData.device())
        _selectedDevice.value = device
        storage.saveSelectedDeviceIp(device.ipAddress)
        _statusText.value = "已进入调试模拟设备模式，控制页和调试页将使用本地假数据。"
        _message.value = "已连接调试模拟设备"
    }

    fun selectDevice(device: DeviceSummary) {
        _selectedDevice.value = device
        storage.saveSelectedDeviceIp(device.ipAddress)
        _message.value = "已选择 ${device.title.asYiyangBrandText()}"
    }

    fun updateSelectedDevice(device: DeviceSummary) {
        val saved = upsertDevice(device)
        _selectedDevice.value = saved
        storage.saveSelectedDeviceIp(saved.ipAddress)
    }

    fun consumeMessage() {
        _message.value = null
    }

    private fun launchBusy(
        busyText: String,
        block: suspend () -> Unit,
    ) {
        if (_isScanning.value) return

        viewModelScope.launch {
            _isScanning.value = true
            _statusText.value = busyText
            runCatching { block() }
                .onFailure { error ->
                    _statusText.value = "这次连接没有成功，请换一个入口再试。"
                    _message.value = error.message ?: "操作失败"
                }
            _isScanning.value = false
        }
    }

    private fun upsertDevice(device: DeviceSummary): DeviceSummary {
        _devices.value = mergeDevices(listOf(device) + _devices.value)
        storage.saveDevices(_devices.value)
        return _devices.value.first { it.ipAddress == device.ipAddress }
    }

    private fun restoreSelectedDevice() {
        _selectedDevice.value = _devices.value.firstOrNull { it.ipAddress == storage.loadSelectedDeviceIp() }
    }

    private fun mergeDevices(devices: List<DeviceSummary>): List<DeviceSummary> {
        val merged = linkedMapOf<String, DeviceSummary>()
        for (device in devices) {
            val current = merged[device.ipAddress]
            merged[device.ipAddress] = current?.mergeWith(device) ?: device
        }
        return merged.values.sortedBy(::deviceSortKey)
    }

    private fun deviceSortKey(device: DeviceSummary): Long =
        device.ipAddress.split(".").fold(0L) { acc, part -> (acc shl 8) + (part.toLongOrNull() ?: 0L) }

    private fun DeviceSummary.mergeWith(other: DeviceSummary): DeviceSummary = copy(
        deviceName = deviceName.ifBlank { other.deviceName },
        deviceSn = deviceSn.ifBlank { other.deviceSn },
        productModel = productModel.ifBlank { other.productModel },
        firmwareVersion = firmwareVersion.ifBlank { other.firmwareVersion },
        macAddress = macAddress.ifBlank { other.macAddress },
        debugMock = debugMock || other.debugMock,
        online = online || other.online,
        note = note.ifBlank { other.note },
        lastSeenAt = maxOf(lastSeenAt, other.lastSeenAt),
    )
}

