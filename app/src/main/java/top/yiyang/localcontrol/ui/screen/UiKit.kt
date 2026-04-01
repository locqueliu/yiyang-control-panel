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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

internal enum class ToolStatusTone {
    Neutral,
    Accent,
    Positive,
    Warning,
}

@Composable
internal fun ToolTabRow(
    selectedTabIndex: Int,
    titles: List<String>,
    onSelect: (Int) -> Unit,
) {
    SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
        titles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onSelect(index) },
                text = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

private const val ToolSectionMotionDurationMillis = 320

@Composable
internal fun ToolSectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (highlighted) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
                trailing?.invoke()
            }
            content()
        }
    }
}

@Composable
internal fun CollapsibleToolSectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    showStateBadge: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    summary: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sizeSpec = tween<IntSize>(
        durationMillis = ToolSectionMotionDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val fadeSpec = tween<Float>(
        durationMillis = ToolSectionMotionDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(
            durationMillis = ToolSectionMotionDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "collapsibleSectionRotation",
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(animationSpec = sizeSpec)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (highlighted) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showStateBadge) {
                        ToolStatusBadge(
                            text = if (expanded) "收起" else "展开",
                            tone = if (highlighted) ToolStatusTone.Positive else ToolStatusTone.Neutral,
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer { rotationZ = iconRotation },
                        tint = if (highlighted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            summary?.invoke(this)

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = fadeSpec) + expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = sizeSpec,
                ),
                exit = fadeOut(animationSpec = fadeSpec) + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = sizeSpec,
                ),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun ToolMetaChip(
    label: String,
    value: String,
    emphasized: Boolean = false,
) {
    Row(
        modifier = Modifier
            .background(
                color = if (emphasized) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (emphasized) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (emphasized) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
internal fun ToolStatusBadge(
    text: String,
    tone: ToolStatusTone = ToolStatusTone.Neutral,
) {
    val background = when (tone) {
        ToolStatusTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant
        ToolStatusTone.Accent -> MaterialTheme.colorScheme.primaryContainer
        ToolStatusTone.Positive -> MaterialTheme.colorScheme.secondaryContainer
        ToolStatusTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val foreground = when (tone) {
        ToolStatusTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        ToolStatusTone.Accent -> MaterialTheme.colorScheme.onPrimaryContainer
        ToolStatusTone.Positive -> MaterialTheme.colorScheme.onSecondaryContainer
        ToolStatusTone.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
        )
    }
}

@Composable
internal fun ToolFeedbackBanner(
    text: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (emphasized) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = if (emphasized) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
) {
    ToolSectionCard(
        title = title,
        subtitle = description,
        highlighted = false,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ToolMetaChip("状态", "等待操作")
        }
    }
}

