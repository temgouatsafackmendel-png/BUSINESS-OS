package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ErpKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = ElectricGreen,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.4).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ErpStatusBadge(
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                ),
                color = color
            )
        }
    }
}

@Composable
fun ErpSubTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    tagPrefix: String = "subtab"
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                val bgColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    label = "tabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tabText"
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = bgColor,
                    border = if (selected) BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)) else null,
                    modifier = Modifier
                        .clickable { onSelect(index) }
                        .testTag("${tagPrefix}_$index")
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErpSectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InteractiveFinancialChart(
    revenuePoints: List<Float>,
    expensePoints: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = SecondaryEmerald
    val dangerColor = StatusDanger
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            val w = size.width
            val h = size.height
            val maxVal = (revenuePoints + expensePoints).maxOrNull()?.coerceAtLeast(1f) ?: 1f

            // Subtle architectural dashed/solid grid lines
            for (i in 0..3) {
                val y = h * (i / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val count = revenuePoints.size.coerceAtLeast(1)
            val slotWidth = w / count
            val barWidth = (slotWidth * 0.22f).coerceAtLeast(6.dp.toPx())

            val linePath = Path()
            val areaPath = Path()

            revenuePoints.forEachIndexed { idx, rev ->
                val exp = expensePoints.getOrElse(idx) { 0f }
                val centerX = idx * slotWidth + slotWidth / 2f

                val revHeight = (rev / maxVal) * (h * 0.86f)
                val expHeight = (exp / maxVal) * (h * 0.86f)
                val revTopY = h - revHeight

                // Revenue pill bar
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(primaryColor, secondaryColor.copy(alpha = 0.75f))
                    ),
                    topLeft = Offset(centerX - barWidth - 3.dp.toPx(), revTopY),
                    size = Size(barWidth, revHeight),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )

                // Expense pill bar
                drawRoundRect(
                    color = dangerColor.copy(alpha = 0.65f),
                    topLeft = Offset(centerX + 3.dp.toPx(), h - expHeight),
                    size = Size(barWidth, expHeight),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )

                val netY = h - ((rev - exp * 0.35f).coerceAtLeast(0f) / maxVal) * (h * 0.9f)
                if (idx == 0) {
                    linePath.moveTo(centerX, netY)
                    areaPath.moveTo(centerX, h)
                    areaPath.lineTo(centerX, netY)
                } else {
                    linePath.lineTo(centerX, netY)
                    areaPath.lineTo(centerX, netY)
                }

                if (idx == revenuePoints.lastIndex) {
                    areaPath.lineTo(centerX, h)
                    areaPath.close()
                }
            }

            // Soft glowing gradient area under the trajectory line
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    listOf(primaryColor.copy(alpha = 0.16f), Color.Transparent)
                )
            )

            // Trajectory line
            drawPath(
                path = linePath,
                brush = Brush.horizontalGradient(listOf(ElectricGreen, SecondaryEmerald)),
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            labels.forEach { lbl ->
                Text(
                    text = lbl,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primaryColor))
                Text("Revenus Encaissés", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dangerColor.copy(alpha = 0.8f)))
                Text("Dépenses & Charges", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(12.dp, 2.5.dp).clip(RoundedCornerShape(2.dp)).background(SecondaryEmerald))
                Text("Tendance Marge", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
