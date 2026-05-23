package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import kotlin.random.Random

@Composable
fun MatrixBackgroundCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "matrix_rain")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrix_y"
    )

    Canvas(modifier = modifier.fillMaxSize().background(Color(0xFF07090D))) {
        clipRect {
            val columns = size.width / 40.dp.toPx()
            val charHeight = 18.dp.toPx()
            
            for (i in 0 until columns.toInt()) {
                val speedFactor = (i % 3) + 1
                val xPos = i * 40.dp.toPx() + 10f
                val yStartOffset = (translationY * speedFactor) % size.height
                
                // Draw trailing columns
                for (j in 0..12) {
                    val yPos = yStartOffset - (j * charHeight)
                    if (yPos > 0 && yPos < size.height) {
                        val alpha = (1.0f - (j / 12.0f)).coerceIn(0f, 1f)
                        val sampleChar = if (Random.nextBoolean()) "0" else "1"
                        
                        // Fallback text drawing via line draws or safe representations inside Canvas wrapper
                        // Instead of complex native canvas static layouts that can crash previewing,
                        // we can draw micro indicators or falling cyber points for ultra aesthetics.
                        drawCircle(
                            color = TerminalGreen.copy(alpha = alpha * 0.4f),
                            radius = 1.5.dp.toPx() + (alpha * 1.5.dp.toPx()),
                            center = Offset(xPos, yPos)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusMeter(
    label: String,
    value: Float,
    maxValue: Float,
    unit: String = "",
    color: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    val progress = (value / maxValue).coerceIn(0f, 1f)
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "${value.toInt()} $unit (${(progress * 100).toInt()}%)",
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF10141A), shape = RoundedCornerShape(4.getDpColorOrShape()))
                .border(1.dp, color.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(color.copy(alpha = 0.7f), color)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

// Helper block to avoid syntax quirks with raw shape extensions if needed
private fun Int.getDpColorOrShape() = this.dp

@Composable
fun SimpleCircularChart(
    label: String,
    value: String,
    percentage: Float,
    color: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(90.dp)
            .background(TerminalBackground, shape = RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(70.dp)) {
            // Background arc track
            drawArc(
                color = Color.DarkGray.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
            )
            // Foreground value sweep arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = percentage * 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 6.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = color,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
