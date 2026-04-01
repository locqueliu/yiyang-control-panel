package top.yiyang.localcontrol.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp as lerpTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import top.yiyang.localcontrol.model.DeviceSummary
import top.yiyang.localcontrol.ui.displayProductModel
import top.yiyang.localcontrol.ui.displayTitle

private const val HeaderMotionDurationMillis = 320

@Composable
internal fun DeviceStatusHeaderCard(
    device: DeviceSummary,
    feedback: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    collapsed: Boolean = false,
    onToggle: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    val headerFadeSpec = tween<Float>(
        durationMillis = HeaderMotionDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val headerSizeSpec = tween<IntSize>(
        durationMillis = HeaderMotionDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val collapseProgress by animateFloatAsState(
        targetValue = if (collapsed) 1f else 0f,
        animationSpec = tween(
            durationMillis = HeaderMotionDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "headerCollapseProgress",
    )
    val containerSpacing = lerpDp(12.dp, 10.dp, collapseProgress)
    val titleSpacing = lerpDp(6.dp, 2.dp, collapseProgress)
    val titleStyle = lerpTextStyle(
        MaterialTheme.typography.titleLarge,
        MaterialTheme.typography.titleMedium,
        collapseProgress,
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (emphasized) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize(animationSpec = headerSizeSpec),
            verticalArrangement = Arrangement.spacedBy(containerSpacing),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { current ->
                        if (onToggle != null) current.clickable(onClick = onToggle) else current
                    },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(titleSpacing),
                ) {
                    Text(
                        text = device.displayTitle(),
                        style = titleStyle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    AnimatedVisibility(
                        visible = !collapsed,
                        enter = fadeIn(animationSpec = headerFadeSpec) + expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = headerSizeSpec,
                        ),
                        exit = fadeOut(animationSpec = headerFadeSpec) + shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = headerSizeSpec,
                        ),
                    ) {
                        Text(
                            text = if (device.debugMock) {
                                "本地调试设备已连接，可直接验证控制与系统操作。"
                            } else {
                                "当前设备已就绪，可直接执行控制和维护操作。"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (emphasized) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
                HeaderCollapseBadge(
                    collapsed = collapsed,
                    emphasized = emphasized,
                )
            }

            AnimatedVisibility(
                visible = !collapsed,
                enter = fadeIn(animationSpec = headerFadeSpec) + expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = headerSizeSpec,
                ),
                exit = fadeOut(animationSpec = headerFadeSpec) + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = headerSizeSpec,
                ),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    actions?.invoke()
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ToolMetaChip("型号", device.displayProductModel())
                AnimatedVisibility(
                    visible = !collapsed,
                    enter = fadeIn(animationSpec = headerFadeSpec) + expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = headerSizeSpec,
                    ),
                    exit = fadeOut(animationSpec = headerFadeSpec) + shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = headerSizeSpec,
                    ),
                ) {
                    ToolMetaChip("固件", device.firmwareVersion.ifBlank { "-" })
                }
                ToolMetaChip("IP", device.ipAddress)
                if (device.debugMock) {
                    ToolStatusBadge("调试模式", ToolStatusTone.Warning)
                }
            }

            AnimatedVisibility(
                visible = !collapsed,
                enter = fadeIn(animationSpec = headerFadeSpec) + expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = headerSizeSpec,
                ),
                exit = fadeOut(animationSpec = headerFadeSpec) + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = headerSizeSpec,
                ),
            ) {
                ToolFeedbackBanner(
                    text = feedback,
                    emphasized = emphasized,
                )
            }
        }
    }
}

@Composable
private fun HeaderCollapseBadge(
    collapsed: Boolean,
    emphasized: Boolean,
) {
    val badgeRotation by animateFloatAsState(
        targetValue = if (collapsed) 0f else 180f,
        animationSpec = tween(
            durationMillis = HeaderMotionDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "headerBadgeRotation",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolStatusBadge(
            text = if (collapsed) "展开" else "收起",
            tone = if (emphasized) ToolStatusTone.Positive else ToolStatusTone.Neutral,
        )
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.graphicsLayer { rotationZ = badgeRotation },
            tint = if (emphasized) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
internal fun HeaderQuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    val content: @Composable RowScope.() -> Unit = {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    if (primary) {
        Button(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            content()
        }
    } else {
        OutlinedButton(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            content()
        }
    }
}
