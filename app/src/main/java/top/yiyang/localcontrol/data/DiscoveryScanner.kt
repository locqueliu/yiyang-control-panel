package top.yiyang.localcontrol.data

import android.content.Context
import android.net.ConnectivityManager
import java.io.File
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import top.yiyang.localcontrol.model.DeviceSummary

data class DiscoveryScanReport(
    val devices: List<DeviceSummary>,
    val rangeSummary: String,
    val totalHosts: Int,
    val discoveryTargets: Int,
    val arpMatchedHosts: Int,
)

class DiscoveryScanner(
    private val context: Context,
    private val apiClient: DeviceApiClient,
) {
    suspend fun scanCurrentSubnet(): DiscoveryScanReport = scanLocalNetwork()

    suspend fun scanLocalNetwork(): DiscoveryScanReport = coroutineScope {
        val ranges = collectScanRanges()
        if (ranges.isEmpty()) {
            return@coroutineScope DiscoveryScanReport(
                devices = emptyList(),
                rangeSummary = "未识别到可扫描的局域网网段",
                totalHosts = 0,
                discoveryTargets = 0,
                arpMatchedHosts = 0,
            )
        }

        val localAddresses = ranges.flatMapTo(mutableSetOf()) { it.localAddresses }
        val allTargets = ranges
            .flatMap { range -> range.hostAddresses(localAddresses).map(::longToIpv4).toList() }
            .distinct()

        allTargets.chunked(PROBE_BATCH_SIZE).forEach { chunk ->
            chunk.map { ipAddress ->
                async { probeHost(ipAddress) }
            }.awaitAll()
        }

        val targetSet = allTargets.toHashSet()
        val arpTable = readArpTable().filterKeys(targetSet::contains)
        val discoveryTargets = chooseDiscoveryTargets(
            allTargets = allTargets,
            arpMatches = arpTable.keys.sortedBy(::ipSortKey),
        )

        val discovered = linkedMapOf<String, DeviceSummary>()
        discoveryTargets.chunked(DISCOVERY_BATCH_SIZE).forEach { chunk ->
            chunk.map { ipAddress ->
                async {
                    ipAddress to tryDiscoverDevice(ipAddress)
                }
            }.awaitAll().forEach { (ipAddress, device) ->
                val resolved = device?.withMac(arpTable[ipAddress].orEmpty()) ?: return@forEach
                val merged = discovered[ipAddress]?.mergeWith(resolved) ?: resolved
                discovered[ipAddress] = merged
            }
        }

        DiscoveryScanReport(
            devices = discovered.values.sortedBy(::deviceSortKey),
            rangeSummary = ranges.joinToString(", ") { it.cidrLabel },
            totalHosts = allTargets.size,
            discoveryTargets = discoveryTargets.size,
            arpMatchedHosts = arpTable.size,
        )
    }

    suspend fun connectByIp(
        rawIpAddress: String,
        allowPlaceholder: Boolean = true,
    ): DeviceSummary {
        val ipAddress = normalizeIp(rawIpAddress)
            ?: throw IllegalArgumentException("IP 地址格式错误，请输入类似 192.168.1.88 的地址")

        probeHost(ipAddress)
        val discovered = tryDiscoverDevice(ipAddress)
        if (discovered != null) {
            return discovered.withMac(readArpTable()[ipAddress].orEmpty())
        }

        if (!allowPlaceholder) {
            throw IllegalStateException("目标地址未返回设备信息，请确认设备接口可访问")
        }

        return DeviceSummary(
            ipAddress = ipAddress,
            deviceName = "手动连接设备",
            macAddress = readArpTable()[ipAddress].orEmpty(),
            online = true,
        )
    }

    suspend fun connectByMac(rawMacAddress: String): DeviceSummary {
        val macAddress = normalizeMac(rawMacAddress)
            ?: throw IllegalArgumentException("MAC 地址格式错误，请输入类似 AA:BB:CC:DD:EE:FF 的地址")

        resolveIpByMac(macAddress)?.let { return connectByIp(it) }

        val report = scanLocalNetwork()
        report.devices.firstOrNull { normalizeMac(it.macAddress) == macAddress }?.let { return it }

        resolveIpByMac(macAddress)?.let { return connectByIp(it) }

        throw IllegalStateException(
            "未在当前局域网 ARP 记录中找到对应设备，请先确认设备已联网并与手机处于同一网络",
        )
    }

    private fun collectScanRanges(): List<ScanRange> {
        val fromActiveNetwork = collectActiveNetworkRanges()
        if (fromActiveNetwork.isNotEmpty()) return fromActiveNetwork

        val fromRouteTable = collectDefaultRouteRanges()
        if (fromRouteTable.isNotEmpty()) return fromRouteTable

        return collectInterfaceRanges()
    }

    private fun collectActiveNetworkRanges(): List<ScanRange> {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return emptyList()
        val activeNetwork = manager.activeNetwork ?: return emptyList()
        val linkProperties = manager.getLinkProperties(activeNetwork) ?: return emptyList()
        val interfaceName = linkProperties.interfaceName

        return linkProperties.linkAddresses.mapNotNull { linkAddress ->
            val address = linkAddress.address
            if (address !is Inet4Address || !address.isSiteLocalAddress) return@mapNotNull null
            buildScanRange(
                networkAddress = ipv4ToLong(address) and prefixToMask(linkAddress.prefixLength),
                prefixLength = linkAddress.prefixLength,
                interfaceName = interfaceName,
                localAddresses = setOf(ipv4ToLong(address)),
            )
        }.distinctBy { it.cidrLabel }
    }

    private fun collectDefaultRouteRanges(): List<ScanRange> {
        val routeFile = File("/proc/net/route")
        if (!routeFile.exists()) return emptyList()

        val defaultInterfaces = runCatching {
            routeFile.useLines { lines ->
                lines.drop(1).mapNotNull { line ->
                    val columns = line.trim().split(ROUTE_SPLIT_REGEX)
                    if (columns.size < 4) return@mapNotNull null
                    val destination = columns[1]
                    val flags = columns[3].toIntOrNull(16) ?: 0
                    if (destination != "00000000" || flags and ROUTE_FLAG_UP == 0) return@mapNotNull null
                    columns[0]
                }.distinct().toList()
            }
        }.getOrDefault(emptyList())

        if (defaultInterfaces.isEmpty()) return emptyList()

        val ranges = linkedMapOf<String, ScanRange>()
        for (interfaceName in defaultInterfaces) {
            val networkInterface = runCatching { NetworkInterface.getByName(interfaceName) }.getOrNull() ?: continue
            buildScanRangesFromInterface(networkInterface).forEach { range ->
                ranges.putIfAbsent(range.cidrLabel, range)
            }
        }
        return ranges.values.sortedBy { it.startAddress }
    }

    private fun collectInterfaceRanges(): List<ScanRange> {
        val interfaces = runCatching { Collections.list(NetworkInterface.getNetworkInterfaces()) }
            .getOrDefault(emptyList())
        val ranges = linkedMapOf<String, ScanRange>()

        interfaces.forEach { networkInterface ->
            buildScanRangesFromInterface(networkInterface).forEach { range ->
                ranges.putIfAbsent(range.cidrLabel, range)
            }
        }

        return ranges.values.sortedBy { it.startAddress }
    }

    private fun buildScanRangesFromInterface(networkInterface: NetworkInterface): List<ScanRange> {
        if (!networkInterface.isUp || networkInterface.isLoopback || networkInterface.isVirtual) {
            return emptyList()
        }

        return networkInterface.interfaceAddresses.mapNotNull { interfaceAddress ->
            val address = interfaceAddress.address
            if (address !is Inet4Address || !address.isSiteLocalAddress) return@mapNotNull null
            val prefixLength = interfaceAddress.networkPrefixLength.toInt().takeIf { it in 1..30 } ?: return@mapNotNull null
            buildScanRange(
                networkAddress = ipv4ToLong(address) and prefixToMask(prefixLength),
                prefixLength = prefixLength,
                interfaceName = networkInterface.name,
                localAddresses = setOf(ipv4ToLong(address)),
            )
        }
    }

    private fun buildScanRange(
        networkAddress: Long,
        prefixLength: Int,
        interfaceName: String?,
        localAddresses: Set<Long>,
    ): ScanRange? {
        val clampedPrefix = prefixLength.coerceIn(1, 30)
        val mask = prefixToMask(clampedPrefix)
        val normalizedNetwork = networkAddress and mask
        val broadcast = normalizedNetwork or mask.inv().and(IPV4_MASK)
        if (broadcast - normalizedNetwork <= 1) return null

        return ScanRange(
            networkAddress = normalizedNetwork,
            startAddress = normalizedNetwork + 1,
            endAddress = broadcast - 1,
            prefixLength = clampedPrefix,
            interfaceName = interfaceName.orEmpty(),
            localAddresses = localAddresses,
        )
    }

    private fun chooseDiscoveryTargets(
        allTargets: List<String>,
        arpMatches: List<String>,
    ): List<String> {
        return when {
            allTargets.size <= MAX_FULL_DISCOVERY_HOSTS -> allTargets
            arpMatches.isNotEmpty() -> arpMatches
            else -> allTargets
        }
    }

    private suspend fun probeHost(ipAddress: String) = withContext(Dispatchers.IO) {
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ipAddress, HTTP_PORT), PROBE_TIMEOUT_MS)
            }
        }
    }

    private suspend fun tryDiscoverDevice(ipAddress: String): DeviceSummary? =
        runCatching { apiClient.discoverDevice(ipAddress) }.getOrNull()

    private fun readArpTable(): Map<String, String> {
        val arpFile = File("/proc/net/arp")
        if (!arpFile.exists()) return emptyMap()

        return runCatching {
            arpFile.useLines { lines ->
                lines.drop(1).mapNotNull { line ->
                    val columns = line.trim().split(ARP_SPLIT_REGEX)
                    if (columns.size < 4) return@mapNotNull null

                    val ipAddress = normalizeIp(columns[0]) ?: return@mapNotNull null
                    val macAddress = normalizeMac(columns[3]) ?: return@mapNotNull null
                    if (macAddress == EMPTY_MAC) return@mapNotNull null
                    ipAddress to macAddress
                }.toMap()
            }
        }.getOrDefault(emptyMap())
    }

    private fun resolveIpByMac(macAddress: String): String? =
        readArpTable().entries.firstOrNull { it.value == macAddress }?.key

    private fun deviceSortKey(device: DeviceSummary): Long = ipSortKey(device.ipAddress)

    private fun ipSortKey(ipAddress: String): Long = normalizeIp(ipAddress)?.let(::ipv4ToLong) ?: Long.MAX_VALUE

    private fun normalizeIp(rawIpAddress: String): String? {
        val parts = rawIpAddress.trim().split(".")
        if (parts.size != 4) return null

        val normalizedParts = mutableListOf<String>()
        for (part in parts) {
            val value = part.toIntOrNull() ?: return null
            if (value !in 0..255) return null
            normalizedParts += value.toString()
        }
        return normalizedParts.joinToString(".")
    }

    private fun normalizeMac(rawMacAddress: String): String? {
        val compact = rawMacAddress
            .trim()
            .uppercase(Locale.US)
            .filter { it.isDigit() || it in 'A'..'F' }
        if (compact.length != 12) return null
        return compact.chunked(2).joinToString(":")
    }

    private fun ipv4ToLong(address: Inet4Address): Long = address.address.fold(0L) { acc, byte ->
        (acc shl 8) or (byte.toInt() and 0xFF).toLong()
    }

    private fun ipv4ToLong(ipAddress: String): Long = ipAddress.split(".").fold(0L) { acc, part ->
        (acc shl 8) or part.toLong()
    }

    private fun prefixToMask(prefixLength: Int): Long {
        if (prefixLength <= 0) return 0L
        return (IPV4_MASK shl (32 - prefixLength)) and IPV4_MASK
    }

    private fun DeviceSummary.withMac(macAddress: String): DeviceSummary = copy(
        macAddress = this.macAddress.ifBlank { macAddress },
    )

    private fun DeviceSummary.mergeWith(other: DeviceSummary): DeviceSummary = copy(
        deviceName = other.deviceName.ifBlank { deviceName },
        deviceSn = other.deviceSn.ifBlank { deviceSn },
        productModel = other.productModel.ifBlank { productModel },
        firmwareVersion = other.firmwareVersion.ifBlank { firmwareVersion },
        macAddress = other.macAddress.ifBlank { macAddress },
        debugMock = debugMock || other.debugMock,
        online = other.online || online,
        note = other.note.ifBlank { note },
        lastSeenAt = maxOf(lastSeenAt, other.lastSeenAt),
    )

    private data class ScanRange(
        val networkAddress: Long,
        val startAddress: Long,
        val endAddress: Long,
        val prefixLength: Int,
        val interfaceName: String,
        val localAddresses: Set<Long>,
    ) {
        val cidrLabel: String
            get() = "${Companion.longToIpv4(networkAddress)}/$prefixLength"

        fun hostAddresses(excluded: Set<Long>): Sequence<Long> =
            (startAddress..endAddress).asSequence().filterNot { it in excluded }
    }

    private companion object {
        const val HTTP_PORT = 80
        const val PROBE_TIMEOUT_MS = 160
        const val PROBE_BATCH_SIZE = 96
        const val DISCOVERY_BATCH_SIZE = 48
        const val MAX_FULL_DISCOVERY_HOSTS = 512
        const val IPV4_MASK = 0xFFFFFFFFL
        const val EMPTY_MAC = "00:00:00:00:00:00"
        const val ROUTE_FLAG_UP = 0x1
        val ARP_SPLIT_REGEX = Regex("\\s+")
        val ROUTE_SPLIT_REGEX = Regex("\\s+")

        fun longToIpv4(value: Long): String = listOf(
            (value shr 24) and 0xFF,
            (value shr 16) and 0xFF,
            (value shr 8) and 0xFF,
            value and 0xFF,
        ).joinToString(".")
    }
}

