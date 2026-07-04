package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

data class BrainNode(
    val name: String,
    val baseValue: Float, // percentage 0 to 1
    val xPercent: Float, // location in canvas 0 to 1
    val yPercent: Float,
    val color: Color,
    val description: String,
    val tips: String
)

@Composable
fun BrainAnimation(
    testCount: Int,
    averageAccuracy: Float,
    averageScorePercent: Float,
    modifier: Modifier = Modifier
) {
    // Dynamically calculate metrics based on testCount and scores to show realistic learning improvements
    val logicMultiplier = min(1f, 0.45f + (testCount * 0.12f) + (averageScorePercent * 0.4f))
    val speedMultiplier = min(1f, 0.50f + (testCount * 0.08f) + (averageAccuracy * 0.3f))
    val retentionMultiplier = min(1f, 0.40f + (testCount * 0.15f))
    val accuracyMultiplier = min(1f, 0.30f + (averageAccuracy * 0.7f))
    val depthMultiplier = min(1f, 0.35f + (testCount * 0.1f) + (averageScorePercent * 0.5f))

    val nodes = remember(testCount, averageAccuracy, averageScorePercent) {
        listOf(
            BrainNode(
                name = "Mathematical Logic",
                baseValue = logicMultiplier,
                xPercent = 0.50f,
                yPercent = 0.25f,
                color = Color(0xFF64B5F6), // Sky Blue
                description = "Frontal Lobe: Logical reasoning, equation processing, & complex deduction capabilities.",
                tips = "Improve logic by attempting challenging JEE Advanced single & multi-correct questions."
            ),
            BrainNode(
                name = "Calculation Speed",
                baseValue = speedMultiplier,
                xPercent = 0.72f,
                yPercent = 0.38f,
                color = Color(0xFFFFB74D), // Golden Orange
                description = "Parietal Lobe: Rapid algebraic evaluations, arithmetic efficiency & numeric estimation.",
                tips = "Improve speed by practicing JEE Mains Section B numerical integer tests under time limits."
            ),
            BrainNode(
                name = "Memory Retention",
                baseValue = retentionMultiplier,
                xPercent = 0.28f,
                yPercent = 0.45f,
                color = Color(0xFF81C784), // Pale Green
                description = "Temporal Lobe: Formula memory, chemical reactions, physical constants & mathematical identities.",
                tips = "Improve retention by solving comprehensive inorganic chemistry & direct physics formula questions."
            ),
            BrainNode(
                name = "Accuracy Index",
                baseValue = accuracyMultiplier,
                xPercent = 0.48f,
                yPercent = 0.68f,
                color = Color(0xFFE57373), // Red Coral
                description = "Occipital Lobe: Decreased negative markings, precise OMR bubbling, and elimination of silly mistakes.",
                tips = "Improve accuracy by double-checking multiple choice calculations before bubbling."
            ),
            BrainNode(
                name = "Conceptual Depth",
                baseValue = depthMultiplier,
                xPercent = 0.76f,
                yPercent = 0.72f,
                color = Color(0xFFBA68C8), // Bright Purple
                description = "Cerebellum: Conceptual understanding, matching column matrices, and comprehension passages.",
                tips = "Improve depth by carefully reading Advanced passage paragraphs and solving matching list matrices."
            )
        )
    }

    var selectedNode by remember { mutableStateOf<BrainNode?>(null) }

    // Pulsing animations for glowing effect
    val infiniteTransition = rememberInfiniteTransition(label = "brain_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val electronFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "electron"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Neural Brain",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-time Neural Growth Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A Programmatic visualization showing cognitive learning gains from test attempts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    // Neural Network Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(nodes) {
                                detectTapGestures { offset ->
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height

                                    // Check which node was clicked
                                    var clicked: BrainNode? = null
                                    for (node in nodes) {
                                        val nodeX = node.xPercent * canvasWidth
                                        val nodeY = node.yPercent * canvasHeight
                                        val dist = sqrt(
                                            (offset.x - nodeX).pow(2) + (offset.y - nodeY).pow(2)
                                        )
                                        // Touch target of 32dp radius
                                        if (dist < 50f) {
                                            clicked = node
                                            break
                                        }
                                    }
                                    selectedNode = clicked
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height

                        // 1. Draw Synaptic Pathways (connecting edges)
                        val edges = listOf(
                            Pair(0, 1), Pair(0, 2), Pair(1, 3), Pair(2, 3),
                            Pair(1, 4), Pair(3, 4), Pair(0, 3), Pair(2, 4)
                        )

                        for ((start, end) in edges) {
                            val nStart = nodes[start]
                            val nEnd = nodes[end]

                            val startPt = Offset(nStart.xPercent * w, nStart.yPercent * h)
                            val endPt = Offset(nEnd.xPercent * w, nEnd.yPercent * h)

                            // Draw gradient connection lines representing digital synapse pathways
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        nStart.color.copy(alpha = 0.5f),
                                        nEnd.color.copy(alpha = 0.5f)
                                    ),
                                    start = startPt,
                                    end = endPt
                                ),
                                start = startPt,
                                end = endPt,
                                strokeWidth = 3f
                            )

                            // 2. Draw animated synaptic electric pulses flowing between nodes
                            val currentPos = startPt + (endPt - startPt) * electronFlow
                            drawCircle(
                                color = Color.White,
                                radius = 5f,
                                center = currentPos
                            )
                            drawCircle(
                                color = nStart.color.copy(alpha = 0.6f),
                                radius = 12f * pulseScale,
                                center = currentPos
                            )
                        }

                        // 3. Draw Hubs / Nodes
                        nodes.forEach { node ->
                            val cx = node.xPercent * w
                            val cy = node.yPercent * h
                            val baseRadius = 14f + (node.baseValue * 12f)

                            // Outer Pulse Glow
                            drawCircle(
                                color = node.color.copy(alpha = 0.15f * pulseScale),
                                radius = baseRadius * 2f,
                                center = Offset(cx, cy)
                            )

                            // Inner Glow
                            drawCircle(
                                color = node.color.copy(alpha = 0.4f),
                                radius = baseRadius * 1.3f,
                                center = Offset(cx, cy)
                            )

                            // Primary Node Solid
                            drawCircle(
                                color = node.color,
                                radius = baseRadius,
                                center = Offset(cx, cy)
                            )

                            // Center Core Core
                            drawCircle(
                                color = Color.White,
                                radius = baseRadius * 0.35f,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // Floating text label markers for nodes
                    nodes.forEach { node ->
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (node.xPercent * 100).dp / 100 * 280, // approximation to keep on screen
                                    y = (node.yPercent * 100).dp / 100 * 180
                                )
                        ) {
                            // Quick text next to node
                        }
                    }

                    // Instruction overlay inside the box
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "💡 Tap on any neural node to inspect brain improvement metrics",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 4. Detailed Node Metric Card
                val nodeToDisplay = selectedNode ?: nodes.first()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = nodeToDisplay.color.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, nodeToDisplay.color.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = nodeToDisplay.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = nodeToDisplay.color
                            )

                            Text(
                                text = "${(nodeToDisplay.baseValue * 100).toInt()}% Eff",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = nodeToDisplay.color
                            )
                        }

                        // Progress Bar showing percentage
                        LinearProgressIndicator(
                            progress = { nodeToDisplay.baseValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .padding(vertical = 4.dp),
                            color = nodeToDisplay.color,
                            trackColor = nodeToDisplay.color.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = nodeToDisplay.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Tip",
                                tint = nodeToDisplay.color,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Preparation Advice:\n${nodeToDisplay.tips}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
