package top.yiyang.localcontrol.ui

import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.model.ProtocolIds

internal fun String.asYiyangBrandText(): String {
    return this
        .replace(ProtocolIds.legacyBrandUpper, "YIYANG")
        .replace(ProtocolIds.legacyBrandTitle, "Yiyang")
        .replace(ProtocolIds.legacyBrandLower, "yiyang")
}

internal fun DeviceSummary.displayTitle(): String = title.asYiyangBrandText()

internal fun DeviceSummary.displayProductModel(): String =
    productModel.ifBlank { "未知" }.asYiyangBrandText()

