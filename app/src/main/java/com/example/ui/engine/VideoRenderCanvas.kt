package com.example.ui.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import com.example.data.model.CameraMotion
import com.example.data.model.SceneTransition
import com.example.data.model.VideoProject
import com.example.data.model.VideoScene
import com.example.data.model.VisualTheme
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VideoRenderCanvas(
    currentTime: Float,
    project: VideoProject,
    modifier: Modifier = Modifier,
    showHud: Boolean = true
) {
    Box(modifier = modifier.background(Color(0xFF04060A))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width < 10f || size.height < 10f) return@Canvas

            try {
                if (project.scenes.isEmpty()) {
                    drawEmptyCanvas(size)
                    return@Canvas
                }

                // Determine active scene and transition state
                val totalDuration = project.totalDurationSec.coerceAtLeast(1f)
                val loopedTime = (currentTime % totalDuration).coerceIn(0f, totalDuration)
                var activeSceneIndex = project.scenes.indexOfFirst {
                    loopedTime >= it.startTimeSec && loopedTime < (it.startTimeSec + it.durationSec)
                }
                if (activeSceneIndex == -1) {
                    activeSceneIndex = (project.scenes.size - 1).coerceAtLeast(0)
                }

                val currentScene = project.scenes.getOrNull(activeSceneIndex)
                    ?: project.scenes.firstOrNull()
                    ?: return@Canvas

                val sceneElapsed = (loopedTime - currentScene.startTimeSec).coerceAtLeast(0f)
                val sceneProgress = (sceneElapsed / currentScene.durationSec.coerceAtLeast(0.1f)).coerceIn(0f, 1f)

                // Check if we are near the end of current scene (transition phase)
                val transitionDuration = currentScene.transition.durationSec.coerceAtMost(currentScene.durationSec * 0.4f)
                val timeRemainingInScene = currentScene.durationSec - sceneElapsed
                val isTransitioning = timeRemainingInScene <= transitionDuration && activeSceneIndex < project.scenes.size - 1

                if (isTransitioning) {
                    val nextScene = project.scenes.getOrNull(activeSceneIndex + 1)
                    if (nextScene != null) {
                        val transitionProgress = 1f - (timeRemainingInScene / transitionDuration.coerceAtLeast(0.1f)).coerceIn(0f, 1f)
                        renderSceneWithTransition(
                            fromScene = currentScene,
                            toScene = nextScene,
                            transitionType = currentScene.transition,
                            transitionProgress = transitionProgress,
                            time = loopedTime,
                            size = size
                        )
                    } else {
                        renderSingleScene(
                            scene = currentScene,
                            sceneProgress = sceneProgress,
                            time = loopedTime,
                            size = size
                        )
                    }
                } else {
                    renderSingleScene(
                        scene = currentScene,
                        sceneProgress = sceneProgress,
                        time = loopedTime,
                        size = size
                    )
                }

                // Real-time Audio Reactive Spectrum Overlay
                if (project.audioVisualizerEnabled) {
                    drawAudioSpectrumVisualizer(loopedTime, size)
                }

                // Cinematic 4K Film Vignette & Anamorphic Grain Atmosphere
                drawCinematicVignette(size)

                // Professional Studio Telemetry HUD
                if (showHud) {
                    drawProfessionalStudioHud(
                        time = loopedTime,
                        totalDuration = totalDuration,
                        scene = currentScene,
                        project = project,
                        size = size
                    )
                }
            } catch (e: Throwable) {
                drawEmptyCanvas(size)
            }
        }
    }
}

private fun DrawScope.renderSingleScene(
    scene: VideoScene,
    sceneProgress: Float,
    time: Float,
    size: Size
) {
    applyCameraTransform(scene.cameraMotion, sceneProgress, time, size) {
        when (scene.visualTheme) {
            VisualTheme.CYBERPUNK_MEGACITY -> drawCyberpunkMegacity(time, sceneProgress, size)
            VisualTheme.COSMIC_NEBULA -> drawCosmicNebula(time, sceneProgress, size)
            VisualTheme.DEEP_OCEAN -> drawDeepOcean(time, sceneProgress, size)
            VisualTheme.QUANTUM_REALM -> drawQuantumRealm(time, sceneProgress, size)
            VisualTheme.RETRO_SYNTHWAVE -> drawRetroSynthwave(time, sceneProgress, size)
            VisualTheme.HYPERLAPSE_NATURE -> drawHyperlapseNature(time, sceneProgress, size)
            VisualTheme.NEURAL_MATRIX -> drawNeuralMatrix(time, sceneProgress, size)
            VisualTheme.SOLAR_SUPERNOVA -> drawSolarSupernova(time, sceneProgress, size)
            VisualTheme.DYSON_SPHERE -> drawDysonSphere(time, sceneProgress, size)
        }
    }
}

private fun DrawScope.renderSceneWithTransition(
    fromScene: VideoScene,
    toScene: VideoScene,
    transitionType: SceneTransition,
    transitionProgress: Float,
    time: Float,
    size: Size
) {
    when (transitionType) {
        SceneTransition.SEAMLESS_CROSS_DISSOLVE -> {
            renderSingleScene(fromScene, 1f, time, size)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00F0FF).copy(alpha = transitionProgress * 0.35f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                ),
                blendMode = BlendMode.Screen
            )
            renderSingleScene(toScene, transitionProgress * 0.3f, time, size)
        }
        SceneTransition.ZOOM_BLUR -> {
            val zoomScale = 1f + transitionProgress * 0.7f
            scale(zoomScale, pivot = Offset(size.width * 0.5f, size.height * 0.5f)) {
                renderSingleScene(fromScene, 1f, time, size)
            }
            if (transitionProgress > 0.45f) {
                renderSingleScene(toScene, transitionProgress, time, size)
            }
            val numRays = 20
            for (i in 0 until numRays) {
                val angle = (i * (360f / numRays) + time * 60f) * (PI / 180f).toFloat()
                val r1 = size.minDimension * 0.12f
                val r2 = size.maxDimension * 0.85f
                drawLine(
                    color = Color.White.copy(alpha = transitionProgress * 0.45f),
                    start = Offset(size.width / 2 + cos(angle) * r1, size.height / 2 + sin(angle) * r1),
                    end = Offset(size.width / 2 + cos(angle) * r2, size.height / 2 + sin(angle) * r2),
                    strokeWidth = 3.5f
                )
            }
        }
        SceneTransition.LIGHT_LEAK_FLASH -> {
            renderSingleScene(if (transitionProgress < 0.5f) fromScene else toScene, transitionProgress, time, size)
            val flash = 1f - abs(transitionProgress - 0.5f) * 2f
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFB800).copy(alpha = flash * 0.75f),
                        Color(0xFF00F0FF).copy(alpha = flash * 0.85f),
                        Color.White.copy(alpha = flash * 0.95f)
                    ),
                    start = Offset(size.width * (1f - transitionProgress), 0f),
                    end = Offset(size.width * transitionProgress, size.height)
                ),
                blendMode = BlendMode.Screen
            )
        }
        SceneTransition.GLITCH_WARP -> {
            if (transitionProgress < 0.5f) {
                renderSingleScene(fromScene, 1f, time, size)
            } else {
                renderSingleScene(toScene, transitionProgress, time, size)
            }
            val sliceCount = 10
            for (i in 0 until sliceCount) {
                val y = (i * size.height / sliceCount)
                val h = size.height / (sliceCount * 2)
                val xOffset = sin(time * 35f + i) * 35f * (1f - abs(transitionProgress - 0.5f) * 2f)
                drawRect(
                    color = if (i % 2 == 0) Color(0xFF00F0FF).copy(alpha = 0.4f) else Color(0xFFFF007F).copy(alpha = 0.4f),
                    topLeft = Offset(xOffset, y),
                    size = Size(size.width, h),
                    blendMode = BlendMode.Plus
                )
            }
        }
        SceneTransition.CINEMATIC_WIPE -> {
            renderSingleScene(fromScene, 1f, time, size)
            val wipeX = size.width * transitionProgress
            clipRect(left = 0f, top = 0f, right = wipeX, bottom = size.height) {
                renderSingleScene(toScene, transitionProgress, time, size)
            }
            drawLine(
                color = Color(0xFF00F0FF),
                start = Offset(wipeX, 0f),
                end = Offset(wipeX, size.height),
                strokeWidth = 4f
            )
        }
        SceneTransition.SMOOTH_SLIDE -> {
            val slideX = size.width * (1f - transitionProgress)
            translate(left = -size.width * transitionProgress) {
                renderSingleScene(fromScene, 1f, time, size)
            }
            translate(left = slideX) {
                renderSingleScene(toScene, transitionProgress, time, size)
            }
        }
        SceneTransition.IRIS_REVEAL -> {
            renderSingleScene(fromScene, 1f, time, size)
            val maxR = size.maxDimension * 0.75f
            val currentR = maxR * transitionProgress
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            val circlePath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(center, currentR))
            }
            clipPath(circlePath) {
                renderSingleScene(toScene, transitionProgress, time, size)
            }
            drawCircle(
                color = Color(0xFF00F0FF),
                center = center,
                radius = currentR,
                style = Stroke(width = 4f)
            )
        }
    }
}

private inline fun DrawScope.applyCameraTransform(
    motion: CameraMotion,
    progress: Float,
    time: Float,
    size: Size,
    crossinline drawBlock: DrawScope.() -> Unit
) {
    when (motion) {
        CameraMotion.PAN_RIGHT -> {
            val maxPan = size.width * 0.12f
            val panX = -maxPan * progress
            translate(left = panX) { drawBlock() }
        }
        CameraMotion.DOLLY_ZOOM -> {
            val scaleFactor = 1.0f + 0.28f * sin(progress * PI.toFloat())
            scale(scaleFactor, pivot = Offset(size.width * 0.5f, size.height * 0.5f)) { drawBlock() }
        }
        CameraMotion.ORBIT_360 -> {
            val angle = progress * 16f - 8f
            rotate(angle, pivot = Offset(size.width * 0.5f, size.height * 0.5f)) { drawBlock() }
        }
        CameraMotion.CRANE_UP -> {
            val craneY = (1f - progress) * (size.height * 0.14f)
            translate(top = craneY) { drawBlock() }
        }
        CameraMotion.CRANE_DESCENT -> {
            val craneY = progress * (size.height * 0.14f)
            translate(top = -craneY) { drawBlock() }
        }
        CameraMotion.HYPERSPEED_WARP -> {
            val warpScale = 1.0f + progress * 0.85f
            scale(warpScale, pivot = Offset(size.width * 0.5f, size.height * 0.5f)) { drawBlock() }
        }
        CameraMotion.FPV_DIVE -> {
            val tilt = sin(progress * PI.toFloat() * 2) * 5f
            val diveY = progress * (size.height * 0.1f)
            rotate(tilt, pivot = Offset(size.width * 0.5f, size.height * 0.5f)) {
                translate(top = diveY) { drawBlock() }
            }
        }
        CameraMotion.DUTCH_ANGLE_ROLL -> {
            val roll = sin(time * 0.8f) * 7f
            rotate(roll, pivot = Offset(size.width * 0.5f, size.height * 0.5f)) { drawBlock() }
        }
        CameraMotion.SUPER_MACRO_PULL -> {
            val pullScale = 1.45f - progress * 0.45f
            scale(pullScale, pivot = Offset(size.width * 0.5f, size.height * 0.5f)) { drawBlock() }
        }
    }
}

// ---------------- THEME 1: CYBERPUNK MEGACITY ----------------
private fun DrawScope.drawCyberpunkMegacity(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF030508), Color(0xFF0B111B), Color(0xFF1B072B)),
            startY = 0f,
            endY = size.height
        )
    )
    val numBuildings = 14
    val buildingWidth = size.width / (numBuildings - 3)
    for (i in 0 until numBuildings) {
        val bHeight = (0.35f + ((i * 37) % 100) / 180f) * size.height
        val bx = (i - 1) * buildingWidth + (sin(time * 0.1f + i) * 6f)
        val by = size.height - bHeight
        drawRect(
            color = Color(0xFF080D15),
            topLeft = Offset(bx, by),
            size = Size(buildingWidth - 4f, bHeight)
        )
        drawLine(
            color = if (i % 2 == 0) Color(0xFF00F0FF).copy(alpha = 0.6f) else Color(0xFFFF007F).copy(alpha = 0.6f),
            start = Offset(bx, by),
            end = Offset(bx + buildingWidth - 4f, by),
            strokeWidth = 3f
        )
        for (w in 0..4) {
            val wx = bx + 6f + w * 8f
            if (wx < bx + buildingWidth - 8f) {
                for (wy in 0..8) {
                    val windowY = by + 12f + wy * 14f
                    if (windowY < size.height - 16f && (i + w + wy) % 3 != 0) {
                        drawRect(
                            color = if ((w + i) % 2 == 0) Color(0xFF00F0FF).copy(alpha = 0.5f) else Color(0xFFFFB800).copy(alpha = 0.5f),
                            topLeft = Offset(wx, windowY),
                            size = Size(4f, 7f)
                        )
                    }
                }
            }
        }
    }
    // Flying Cyber Speeders
    for (s in 0..3) {
        val speed = 260f + s * 90f
        val sx = ((time * speed + s * 240f) % (size.width + 300f)) - 150f
        val sy = size.height * (0.3f + s * 0.12f) + sin(time * 3f + s) * 8f
        drawCircle(
            color = if (s % 2 == 0) Color(0xFF00F0FF) else Color(0xFFFF007F),
            center = Offset(sx, sy),
            radius = 4.5f
        )
        drawLine(
            color = if (s % 2 == 0) Color(0xFF00F0FF).copy(alpha = 0.6f) else Color(0xFFFF007F).copy(alpha = 0.6f),
            start = Offset(sx - 40f, sy),
            end = Offset(sx, sy),
            strokeWidth = 2.5f
        )
    }
    // Volumetric Neon Rain
    for (r in 0..45) {
        val rx = ((r * 89 + time * 120f) % size.width)
        val ry = ((r * 137 + time * 550f) % size.height)
        drawLine(
            color = Color(0xFF00F0FF).copy(alpha = 0.22f),
            start = Offset(rx, ry),
            end = Offset(rx - 3f, ry + 16f),
            strokeWidth = 1.5f
        )
    }
}

// ---------------- THEME 2: COSMIC SINGULARITY NEBULA ----------------
private fun DrawScope.drawCosmicNebula(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1E0A3C), Color(0xFF080314), Color(0xFF020108)),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.maxDimension * 0.8f
        )
    )
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val bhRadius = size.minDimension * 0.22f
    // Accretion disk rings
    for (i in 0..5) {
        val rot = time * (18f + i * 4f)
        rotate(rot, pivot = Offset(cx, cy)) {
            val rx = bhRadius * (1.6f + i * 0.25f)
            val ry = bhRadius * (0.42f + i * 0.08f)
            drawOval(
                brush = Brush.sweepGradient(
                    listOf(Color(0xFFFF007F), Color(0xFFFFB800), Color(0xFF00F0FF), Color(0xFFFF007F)),
                    center = Offset(cx, cy)
                ),
                topLeft = Offset(cx - rx, cy - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(width = 4.5f)
            )
        }
    }
    // Event horizon core
    drawCircle(
        color = Color(0xFF010204),
        center = Offset(cx, cy),
        radius = bhRadius
    )
    drawCircle(
        color = Color(0xFF00F0FF).copy(alpha = 0.85f),
        center = Offset(cx, cy),
        radius = bhRadius + 2.5f,
        style = Stroke(width = 3.5f)
    )
    // Relativistic photon jets
    drawLine(
        brush = Brush.linearGradient(listOf(Color(0xFF00F0FF), Color.Transparent)),
        start = Offset(cx, cy - bhRadius),
        end = Offset(cx, 0f),
        strokeWidth = 6f
    )
    drawLine(
        brush = Brush.linearGradient(listOf(Color(0xFF00F0FF), Color.Transparent)),
        start = Offset(cx, cy + bhRadius),
        end = Offset(cx, size.height),
        strokeWidth = 6f
    )
}

// ---------------- THEME 3: DEEP OCEAN BIOLUMINESCENCE ----------------
private fun DrawScope.drawDeepOcean(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF020C17), Color(0xFF041624), Color(0xFF010810)),
            startY = 0f,
            endY = size.height
        )
    )
    // Sunlight caustics
    for (c in 0..4) {
        val angle = (c * 24f - 18f) * (PI / 180f).toFloat()
        val startX = size.width * (0.2f + c * 0.16f)
        val endX = startX + sin(time * 0.8f + c) * 70f
        drawLine(
            brush = Brush.verticalGradient(listOf(Color(0xFF00F0FF).copy(alpha = 0.18f), Color.Transparent)),
            start = Offset(startX, 0f),
            end = Offset(endX, size.height),
            strokeWidth = size.width * 0.12f
        )
    }
    // Bioluminescent Leviathan Creature
    val lx = size.width * 0.5f + sin(time * 0.6f) * 60f
    val ly = size.height * 0.55f + cos(time * 0.8f) * 25f
    for (seg in 0..8) {
        val segX = lx - seg * 24f + sin(time * 1.6f - seg * 0.4f) * 16f
        val segY = ly + cos(time * 1.6f - seg * 0.4f) * 10f
        val rad = (18f - seg * 1.6f).coerceAtLeast(4f)
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.75f - seg * 0.06f),
            center = Offset(segX, segY),
            radius = rad
        )
    }
    // Rising bubbles
    for (b in 0..30) {
        val bx = ((b * 67 + sin(time * 1.5f + b) * 20f) % size.width)
        val by = ((b * 93 - time * 65f) % size.height + size.height) % size.height
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.35f),
            center = Offset(bx, by),
            radius = (b % 4 + 2).toFloat(),
            style = Stroke(width = 1.5f)
        )
    }
}

// ---------------- THEME 4: QUANTUM SUBATOMIC REALM ----------------
private fun DrawScope.drawQuantumRealm(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF041810), Color(0xFF020C08), Color(0xFF010403)),
            center = Offset(size.width * 0.5f, size.height * 0.5f)
        )
    )
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val qRadius = size.minDimension * 0.32f
    // Multi-plane orbital electron probability clouds
    for (i in 0..3) {
        val rot = time * (30f + i * 20f)
        rotate(rot, pivot = Offset(cx, cy)) {
            val rx = qRadius
            val ry = qRadius * 0.35f
            drawOval(
                color = if (i % 2 == 0) Color(0xFF00E676).copy(alpha = 0.7f) else Color(0xFF00F0FF).copy(alpha = 0.7f),
                topLeft = Offset(cx - rx, cy - ry),
                size = Size(rx * 2, ry * 2),
                style = Stroke(width = 3f)
            )
            val eAngle = (time * (2.5f + i) + i) % (2 * PI.toFloat())
            val ex = cx + cos(eAngle) * rx
            val ey = cy + sin(eAngle) * ry
            drawCircle(Color.White, center = Offset(ex, ey), radius = 5.5f)
            drawCircle(Color(0xFF00E676), center = Offset(ex, ey), radius = 10f, style = Stroke(width = 2f))
        }
    }
    // Quantum Core Nexus
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White, Color(0xFF00E676), Color.Transparent)),
        center = Offset(cx, cy),
        radius = qRadius * 0.25f
    )
}

// ---------------- THEME 5: RETRO SYNTHWAVE SUNSET ----------------
private fun DrawScope.drawRetroSynthwave(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF140224), Color(0xFF38084A), Color(0xFF6B115A)),
            startY = 0f,
            endY = size.height * 0.55f
        )
    )
    // Striped Outrun Sun
    val sunRadius = size.minDimension * 0.24f
    val sunCenter = Offset(size.width * 0.5f, size.height * 0.52f)
    drawCircle(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFAA00), Color(0xFFFF007F))),
        center = sunCenter,
        radius = sunRadius
    )
    for (s in 1..6) {
        val stripeY = sunCenter.y + (s * 10f)
        drawRect(
            color = Color(0xFF38084A),
            topLeft = Offset(sunCenter.x - sunRadius, stripeY),
            size = Size(sunRadius * 2, s * 2.2f)
        )
    }
    // Infinite Perspective Neon Wireframe Grid
    val horizonY = size.height * 0.55f
    drawRect(
        color = Color(0xFF090111),
        topLeft = Offset(0f, horizonY),
        size = Size(size.width, size.height - horizonY)
    )
    val numLines = 14
    for (i in 0..numLines) {
        val bottomX = (i / numLines.toFloat()) * size.width
        drawLine(
            color = Color(0xFFFF007F).copy(alpha = 0.65f),
            start = Offset(size.width * 0.5f, horizonY),
            end = Offset(bottomX, size.height),
            strokeWidth = 2f
        )
    }
    for (h in 1..8) {
        val prog = (h / 8f) * (h / 8f)
        val y = horizonY + prog * (size.height - horizonY) + (time * 25f % (size.height * 0.05f))
        if (y < size.height) {
            drawLine(
                color = Color(0xFF00F0FF).copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )
        }
    }
}

// ---------------- THEME 6: HYPERLAPSE NATURE FLORA ----------------
private fun DrawScope.drawHyperlapseNature(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF021207), Color(0xFF06240E), Color(0xFF09140D)),
            startY = 0f,
            endY = size.height
        )
    )
    val branchCount = 7
    for (b in 0 until branchCount) {
        val bx = size.width * (0.15f + b * 0.12f)
        val path = Path().apply {
            moveTo(bx, size.height)
            quadraticTo(
                bx + sin(time * 0.8f + b) * 35f,
                size.height * 0.6f,
                bx + sin(time * 1.2f + b) * 50f,
                size.height * 0.35f
            )
        }
        drawPath(
            path = path,
            color = Color(0xFF76FF03).copy(alpha = 0.65f),
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )
        // Pulsing blossom spores
        val sporeX = bx + sin(time * 1.2f + b) * 50f
        val sporeY = size.height * 0.35f
        drawCircle(
            color = Color(0xFF00E5FF),
            center = Offset(sporeX, sporeY),
            radius = 8f + sin(time * 3f + b) * 3f
        )
    }
}

// ---------------- THEME 7: NEURAL MATRIX DATA STREAM ----------------
private fun DrawScope.drawNeuralMatrix(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF020904), Color(0xFF041209), Color(0xFF010502))
        )
    )
    // Digital rain stream columns
    val cols = 18
    val colWidth = size.width / cols
    for (c in 0 until cols) {
        val streamSpeed = 160f + (c * 31 % 100)
        val cy = (time * streamSpeed + c * 80f) % (size.height + 200f) - 100f
        val cx = c * colWidth + colWidth * 0.5f
        for (glyph in 0..7) {
            val gy = cy - glyph * 16f
            if (gy in 0f..size.height) {
                val alpha = (1f - glyph / 8f).coerceIn(0.1f, 1f)
                drawCircle(
                    color = if (glyph == 0) Color.White else Color(0xFF00FF66).copy(alpha = alpha),
                    center = Offset(cx, gy),
                    radius = if (glyph == 0) 3.5f else 2.5f
                )
            }
        }
    }
    // 3D Synaptic neural nodes
    val nodeCount = 8
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    for (n in 0 until nodeCount) {
        val angle = (n * (360f / nodeCount) + time * 20f) * (PI / 180f).toFloat()
        val r = size.minDimension * 0.3f
        val nx = cx + cos(angle) * r
        val ny = cy + sin(angle) * r
        // Connect synapse to center
        drawLine(
            color = Color(0xFF00E5FF).copy(alpha = 0.4f),
            start = Offset(cx, cy),
            end = Offset(nx, ny),
            strokeWidth = 2f
        )
        drawCircle(Color(0xFF00FF66), center = Offset(nx, ny), radius = 6f)
    }
    drawCircle(
        color = Color(0xFF00E5FF),
        center = Offset(cx, cy),
        radius = 12f + sin(time * 4f) * 3f
    )
}

// ---------------- THEME 8: SOLAR FLARE & SUPERNOVA ----------------
private fun DrawScope.drawSolarSupernova(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF330800), Color(0xFF140300), Color(0xFF060100)),
            center = Offset(size.width * 0.5f, size.height * 0.5f)
        )
    )
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val sunRadius = size.minDimension * 0.28f

    // Solar flare loops
    for (f in 0..4) {
        val flareAngle = (f * 72f + time * 15f) * (PI / 180f).toFloat()
        val flareR = sunRadius * (1.3f + sin(time * 2f + f) * 0.25f)
        val fx = cx + cos(flareAngle) * flareR
        val fy = cy + sin(flareAngle) * flareR
        drawLine(
            color = Color(0xFFFFD600).copy(alpha = 0.7f),
            start = Offset(cx + cos(flareAngle) * sunRadius, cy + sin(flareAngle) * sunRadius),
            end = Offset(fx, fy),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }

    // Thermonuclear Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFFFD600), Color(0xFFFF3D00)),
            center = Offset(cx, cy),
            radius = sunRadius
        ),
        center = Offset(cx, cy),
        radius = sunRadius
    )
}

// ---------------- THEME 9: DYSON SPHERE MEGASTRUCTURE ----------------
private fun DrawScope.drawDysonSphere(time: Float, progress: Float, size: Size) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF080D1A), Color(0xFF020408)),
            center = Offset(size.width * 0.5f, size.height * 0.5f)
        )
    )
    val cx = size.width * 0.5f
    val cy = size.height * 0.5f
    val coreRadius = size.minDimension * 0.16f
    // Central captured star
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White, Color(0xFF00E5FF), Color.Transparent)),
        center = Offset(cx, cy),
        radius = coreRadius * 1.5f
    )
    // Rotating Dyson orbital panels
    for (ring in 1..3) {
        val rot = time * (12f * ring)
        rotate(rot, pivot = Offset(cx, cy)) {
            val r = coreRadius * (1.8f * ring)
            val segments = 8
            for (s in 0 until segments) {
                val sAngle = (s * (360f / segments)) * (PI / 180f).toFloat()
                val px = cx + cos(sAngle) * r
                val py = cy + sin(sAngle) * r
                drawRoundRect(
                    color = Color(0xFFE2E8F0).copy(alpha = 0.85f),
                    topLeft = Offset(px - 14f, py - 8f),
                    size = Size(28f, 16f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 2.5f)
                )
            }
        }
    }
}

// ---------------- REAL-TIME AUDIO SPECTRUM VISUALIZER ----------------
private fun DrawScope.drawAudioSpectrumVisualizer(time: Float, size: Size) {
    val numBars = 32
    val barWidth = (size.width * 0.5f) / numBars
    val startX = size.width * 0.25f
    val baseY = size.height - 24f

    for (b in 0 until numBars) {
        val freq = b * 0.4f
        val amp = abs(sin(time * 6f + freq) * cos(time * 3f + b * 0.2f))
        val barH = (amp * (size.height * 0.15f)).coerceAtLeast(4f)
        val x = startX + b * barWidth

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF00F0FF), Color(0xFFFF007F).copy(alpha = 0.6f)),
                startY = baseY - barH,
                endY = baseY
            ),
            topLeft = Offset(x, baseY - barH),
            size = Size(barWidth - 2.5f, barH),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }

    // Pulsing Sub-Bass Center Ring
    val bassPulse = abs(sin(time * 4f))
    drawCircle(
        color = Color(0xFF00F0FF).copy(alpha = bassPulse * 0.25f),
        center = Offset(size.width * 0.5f, size.height * 0.5f),
        radius = size.minDimension * 0.38f + bassPulse * 15f,
        style = Stroke(width = 2f)
    )
}

// ---------------- PROFESSIONAL STUDIO HUD OVERLAY ----------------
private fun DrawScope.drawProfessionalStudioHud(
    time: Float,
    totalDuration: Float,
    scene: VideoScene,
    project: VideoProject,
    size: Size
) {
    try {
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas

            // REC Indicator
            val isBlinkOn = ((time * 2f).toInt() % 2 == 0)
            if (isBlinkOn) {
                val recDotPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                    isAntiAlias = true
                }
                native.drawCircle(32f, 32f, 8f, recDotPaint)
            }
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 22f
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            }
            native.drawText("REC [4K 60FPS]", 48f, 38f, textPaint)

            // Timecode with frames (HH:MM:SS:FF)
            val timecode = formatFullTimecode(time)
            val totalTc = formatFullTimecode(totalDuration)
            val tcPaint = android.graphics.Paint().apply {
                color = 0xFF00F0FF.toInt()
                textSize = 24f
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            }
            native.drawText("TC $timecode / $totalTc", 32f, 72f, tcPaint)

            // Chapter & Scene Metadata (Top-Right)
            val rightPaint = android.graphics.Paint().apply {
                color = 0xFFE2E8F0.toInt()
                textSize = 20f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.RIGHT
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            }
            val chapterLabel = if (scene.chapterTitle.isNotBlank()) "${scene.chapterTitle.uppercase()} • " else ""
            native.drawText("$chapterLabel${scene.name.take(28)}", size.width - 28f, 38f, rightPaint)

            val lensPaint = android.graphics.Paint().apply {
                color = 0xFF94A3B8.toInt()
                textSize = 18f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.RIGHT
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
            }
            native.drawText("35mm T1.5 CINE • ISO 800 • BT.2020 HDR", size.width - 28f, 68f, lensPaint)
        }
    } catch (e: Throwable) {
        android.util.Log.e("VideoRenderCanvas", "drawProfessionalStudioHud error", e)
    }
}

private fun DrawScope.drawCinematicVignette(size: Size) {
    val vignetteRad = (size.maxDimension * 0.72f).coerceAtLeast(1f)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = vignetteRad
        )
    )
}

private fun DrawScope.drawEmptyCanvas(size: Size) {
    drawRect(color = Color(0xFF0A0D14))
    drawLine(
        color = Color(0xFF00F0FF).copy(alpha = 0.2f),
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )
}

private fun formatFullTimecode(seconds: Float): String {
    val totalSec = seconds.toInt()
    val hours = totalSec / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60
    val frames = ((seconds - totalSec) * 60).toInt()
    return String.format(Locale.US, "%02d:%02d:%02d:%02d", hours, mins, secs, frames)
}

private inline fun DrawScope.drawIntoCanvas(block: (androidx.compose.ui.graphics.Canvas) -> Unit) {
    drawContext.canvas.let(block)
}
