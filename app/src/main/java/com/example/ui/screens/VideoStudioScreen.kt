package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CameraMotion
import com.example.data.model.SceneTransition
import com.example.data.model.VideoAspectRatio
import com.example.data.model.VideoChapter
import com.example.data.model.VideoExportConfig
import com.example.data.model.VideoProject
import com.example.data.model.VideoScene
import com.example.ui.engine.ThumbnailCanvas
import com.example.ui.engine.VideoRenderCanvas
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioMagenta
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceBorder
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.StudioTab
import com.example.ui.viewmodel.VideoStudioUiState
import com.example.ui.viewmodel.VideoStudioViewModel
import java.util.Locale

@Composable
fun VideoStudioScreen(
    viewModel: VideoStudioViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioBackground,
        topBar = {
            StudioHeader(
                apiModel = uiState.apiModel,
                totalDuration = uiState.currentProject.totalDurationSec,
                currentAspectRatio = uiState.currentProject.aspectRatio,
                isVisualizerActive = uiState.currentProject.audioVisualizerEnabled,
                onToggleVisualizer = { viewModel.toggleAudioVisualizer() },
                onAspectRatioSelected = { viewModel.selectAspectRatio(it) }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 840.dp

            if (isWideScreen) {
                // Workstation Split Mode (Tablets / Landscape / Foldables)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Pane: 4K Master Viewport & Long Video Scrubber
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = StudioSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            VideoViewportSection(
                                uiState = uiState,
                                onPlayPause = { viewModel.playPause() },
                                onSeek = { viewModel.seekTo(it) },
                                onJump = { viewModel.jumpSeconds(it) },
                                onSpeedChange = { viewModel.setPlaybackSpeed(it) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LongVideoChapterLane(
                                chapters = uiState.currentProject.chapters,
                                currentTime = uiState.currentTime,
                                onChapterSelected = { viewModel.jumpToChapter(it) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TimelineTracksSection(
                                project = uiState.currentProject,
                                currentTime = uiState.currentTime,
                                onSeek = { viewModel.seekTo(it) }
                            )
                        }
                    }

                    // Right Pane: Studio Navigation Tabs & Tools
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = StudioSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            StudioTabsBar(
                                activeTab = uiState.activeTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                StudioTabContent(
                                    tab = uiState.activeTab,
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    context = context
                                )
                            }
                        }
                    }
                }
            } else {
                // Mobile Handheld Vertical Layout
                Column(modifier = Modifier.fillMaxSize()) {
                    VideoViewportSection(
                        uiState = uiState,
                        onPlayPause = { viewModel.playPause() },
                        onSeek = { viewModel.seekTo(it) },
                        onJump = { viewModel.jumpSeconds(it) },
                        onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )

                    // Long-Video Act / Chapter Navigation Bar
                    LongVideoChapterLane(
                        chapters = uiState.currentProject.chapters,
                        currentTime = uiState.currentTime,
                        onChapterSelected = { viewModel.jumpToChapter(it) },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )

                    StudioTabsBar(
                        activeTab = uiState.activeTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        StudioTabContent(
                            tab = uiState.activeTab,
                            uiState = uiState,
                            viewModel = viewModel,
                            context = context
                        )
                    }
                }
            }
        }
    }

    // Modal Progress Dialogs
    if (uiState.isGenerating) {
        GenerationProgressDialog(
            step = uiState.generationStep,
            progress = uiState.generationProgress
        )
    }

    if (uiState.isExporting) {
        ExportProgressDialog(
            progress = uiState.exportProgress,
            frameCount = uiState.exportFrameCount,
            totalFrames = uiState.exportTotalFrames,
            config = uiState.exportConfig,
            durationSec = uiState.currentProject.totalDurationSec,
            onCancel = { viewModel.cancelExport() }
        )
    }

    if (uiState.exportedSuccess) {
        ExportSuccessDialog(
            config = uiState.exportConfig,
            durationSec = uiState.currentProject.totalDurationSec,
            title = uiState.currentProject.title,
            onDismiss = { viewModel.dismissExportSuccess() },
            onShare = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Generated ${uiState.currentProject.title} (${(uiState.currentProject.totalDurationSec / 60).toInt()} min 4K UHD Film) with NVIDIA Nemotron-3 Super 120B!"
                    )
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share 4K Master Video"))
                viewModel.dismissExportSuccess()
            }
        )
    }
}

@Composable
private fun StudioHeader(
    apiModel: String,
    totalDuration: Float,
    currentAspectRatio: VideoAspectRatio,
    isVisualizerActive: Boolean,
    onToggleVisualizer: () -> Unit,
    onAspectRatioSelected: (VideoAspectRatio) -> Unit
) {
    Surface(
        color = StudioSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Brand
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(StudioCyan, StudioMagenta))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Logo",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Video Coder Pro",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        val durationMinutes = (totalDuration / 60).toInt()
                        Text(
                            text = "NVIDIA Nemotron-3 • $durationMinutes-Min 4K Engine",
                            color = StudioCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Controls: Audio Visualizer & 120B Model Pill
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Audio Visualizer Toggle Button
                    IconButton(
                        onClick = onToggleVisualizer,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isVisualizerActive) StudioCyan.copy(alpha = 0.25f) else StudioSurfaceVariant)
                            .border(1.dp, if (isVisualizerActive) StudioCyan else StudioSurfaceBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Spectrum",
                            tint = if (isVisualizerActive) StudioCyan else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Model Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(StudioSurfaceVariant)
                            .border(1.dp, StudioCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val transition = rememberInfiniteTransition(label = "pulse")
                        val alpha by transition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(StudioEmerald.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "120B ONLINE",
                            color = StudioEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Aspect Ratio Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aspect Ratio:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                VideoAspectRatio.entries.forEach { ratio ->
                    val isSelected = ratio == currentAspectRatio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) StudioCyan.copy(alpha = 0.22f) else StudioSurfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) StudioCyan else StudioSurfaceBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onAspectRatioSelected(ratio) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = ratio.label.split(" ").first(),
                            color = if (isSelected) StudioCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioTabsBar(
    activeTab: StudioTab,
    onTabSelected: (StudioTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = activeTab.ordinal,
        containerColor = StudioSurface,
        contentColor = StudioCyan,
        edgePadding = 8.dp,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                color = StudioCyan,
                height = 3.dp
            )
        },
        divider = { HorizontalDivider(color = StudioSurfaceBorder) }
    ) {
        StudioTab.entries.forEach { tab ->
            val isSelected = activeTab == tab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (tab) {
                            StudioTab.PLAYER -> Icons.Default.PlayArrow
                            StudioTab.PROMPT -> Icons.Default.AutoAwesome
                            StudioTab.CODER -> Icons.Default.Code
                            StudioTab.THUMBNAIL -> Icons.Default.Image
                            StudioTab.EXPORT_4K -> Icons.Default.Download
                            StudioTab.LIBRARY -> Icons.Default.FolderOpen
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (isSelected) StudioCyan else TextMuted
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = tab.label,
                            color = if (isSelected) StudioCyan else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun VideoViewportSection(
    uiState: VideoStudioUiState,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onJump: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Viewport Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFF04060A))
            ) {
                VideoRenderCanvas(
                    currentTime = uiState.currentTime,
                    project = uiState.currentProject,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtitle Overlay
                val activeScene = uiState.currentProject.scenes.find {
                    uiState.currentTime >= it.startTimeSec && uiState.currentTime < (it.startTimeSec + it.durationSec)
                }
                if (activeScene != null && activeScene.narrativeSubtitle.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.78f))
                            .border(1.dp, StudioCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeScene.narrativeSubtitle,
                            color = Color(0xFFF1F5F9),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Transport Control Console
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Timecode & Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatLongTimecode(uiState.currentTime),
                        color = StudioCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Slider(
                        value = uiState.currentTime.coerceIn(0f, uiState.currentProject.totalDurationSec.coerceAtLeast(1f)),
                        onValueChange = onSeek,
                        valueRange = 0f..uiState.currentProject.totalDurationSec.coerceAtLeast(1f),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .testTag("timeline_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = StudioCyan,
                            activeTrackColor = StudioCyan,
                            inactiveTrackColor = StudioSurfaceBorder
                        )
                    )

                    Text(
                        text = formatLongTimecode(uiState.currentProject.totalDurationSec),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Long Video Navigation Buttons (-60s, -10s, Play/Pause, +10s, +60s)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // -60s button
                        IconButton(onClick = { onJump(-60f) }, modifier = Modifier.size(34.dp)) {
                            Text("-1m", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // -10s button
                        IconButton(onClick = { onJump(-10f) }, modifier = Modifier.size(34.dp)) {
                            Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        // Play/Pause Master
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StudioCyan)
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        // +10s button
                        IconButton(onClick = { onJump(10f) }, modifier = Modifier.size(34.dp)) {
                            Icon(imageVector = Icons.Default.FastForward, contentDescription = "Forward 10s", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        // +60s button
                        IconButton(onClick = { onJump(60f) }, modifier = Modifier.size(34.dp)) {
                            Text("+1m", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Fast Scrubbing Speed Chips (0.5x, 1x, 2x, 5x, 10x, 20x)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0.5f, 1.0f, 2.0f, 5.0f, 10.0f, 20.0f).forEach { sp ->
                            val isSelected = uiState.playbackSpeed == sp
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) StudioCyan.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { onSpeedChange(sp) }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (sp >= 1f) "${sp.toInt()}x" else "${sp}x",
                                    color = if (isSelected) StudioCyan else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LongVideoChapterLane(
    chapters: List<VideoChapter>,
    currentTime: Float,
    onChapterSelected: (VideoChapter) -> Unit,
    modifier: Modifier = Modifier
) {
    if (chapters.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHAPTER ACTS (LONG FORM)",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "${chapters.size} Narrative Acts",
                color = StudioCyan,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(chapters) { ch ->
                val isActive = currentTime >= ch.startTimeSec && currentTime < (ch.startTimeSec + ch.durationSec)
                val startMin = (ch.startTimeSec / 60).toInt()
                val startSec = (ch.startTimeSec % 60).toInt()

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) StudioCyan.copy(alpha = 0.25f) else StudioSurfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (isActive) StudioCyan else StudioSurfaceBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onChapterSelected(ch) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Column {
                        Text(
                            text = ch.title,
                            color = if (isActive) StudioCyan else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", startMin, startSec),
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineTracksSection(
    project: VideoProject,
    currentTime: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SCENE TRACKS & TRANSITIONS (${project.scenes.size})",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Total: ${(project.totalDurationSec / 60).toInt()} min",
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(project.scenes) { scene ->
                val isActive = currentTime >= scene.startTimeSec && currentTime < (scene.startTimeSec + scene.durationSec)
                val sceneColor = parseColorSafely(scene.accentColorHex, StudioCyan)
                val durMinutes = (scene.durationSec / 60).toInt()
                val durSeconds = (scene.durationSec % 60).toInt()

                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .clickable { onSeek(scene.startTimeSec) }
                        .testTag("scene_card_${scene.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) StudioSurfaceVariant else StudioSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) sceneColor else StudioSurfaceBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCENE ${scene.id}",
                                color = sceneColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", durMinutes, durSeconds),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = scene.name,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StudioBackground)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = scene.transition.displayName,
                                color = StudioMagenta,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${scene.visualTheme.themeName.split(" ").first()} • ${scene.cameraMotion.displayName}",
                            color = TextMuted,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioTabContent(
    tab: StudioTab,
    uiState: VideoStudioUiState,
    viewModel: VideoStudioViewModel,
    context: Context
) {
    when (tab) {
        StudioTab.PLAYER -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                TimelineTracksSection(
                    project = uiState.currentProject,
                    currentTime = uiState.currentTime,
                    onSeek = { viewModel.seekTo(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Active Scene Detail Inspector Card
                val activeScene = uiState.currentProject.scenes.find {
                    uiState.currentTime >= it.startTimeSec && uiState.currentTime < (it.startTimeSec + it.durationSec)
                } ?: uiState.currentProject.scenes.firstOrNull()

                if (activeScene != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Active Scene: ${activeScene.name}",
                                    color = StudioCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${(activeScene.durationSec / 60).toInt()}m ${(activeScene.durationSec % 60).toInt()}s",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Visual Theme: ${activeScene.visualTheme.themeName}",
                                color = StudioEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Prompt: ${activeScene.visualPrompt}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Seamless Transition: ${activeScene.transition.displayName}",
                                color = StudioMagenta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Camera Vector: ${activeScene.cameraMotion.displayName}",
                                color = StudioAmber,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        StudioTab.PROMPT -> {
            PromptConsoleView(
                onGenerate = { prompt, duration, aspect ->
                    viewModel.generateVideo(prompt, duration, aspect)
                },
                currentAspect = uiState.currentProject.aspectRatio
            )
        }

        StudioTab.CODER -> {
            VideoCoderView(
                scriptCode = uiState.editableScriptCode,
                onCodeChange = { viewModel.updateCoderScript(it) },
                onRecompile = { viewModel.recompileAndRunScript() },
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("VideoCoderScript", uiState.editableScriptCode))
                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        StudioTab.THUMBNAIL -> {
            ThumbnailStudioView(
                thumbnail = uiState.currentProject.thumbnail,
                onUpdate = { b, t, s, p, sc, f, g, d ->
                    viewModel.updateThumbnail(b, t, s, p, sc, f, g, d)
                }
            )
        }

        StudioTab.EXPORT_4K -> {
            ExportStudioView(
                project = uiState.currentProject,
                onStartExport = { viewModel.start4KExport(it) }
            )
        }

        StudioTab.LIBRARY -> {
            SavedProjectsView(
                projects = uiState.savedProjects,
                onSelectProject = { viewModel.loadProject(it) },
                onDeleteProject = { viewModel.deleteProject(it.id) }
            )
        }
    }
}

@Composable
private fun PromptConsoleView(
    onGenerate: (String, Float, VideoAspectRatio) -> Unit,
    currentAspect: VideoAspectRatio
) {
    var promptText by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableFloatStateOf(2400f) } // Default 40 Minutes
    var selectedAspect by remember { mutableStateOf(currentAspect) }

    val promptPresets = listOf(
        "40-minute epic space odyssey from Earth's orbit through deep void into supermassive black hole singularity",
        "40-minute deep oceanic abyssal exploration with colossal bioluminescent leviathans and caustic light beams",
        "30-minute cyberpunk Neo-Tokyo 2099 saga with high-speed hover chases and holographic neon rain",
        "40-minute subatomic quantum particle collision chronicle and sentient neural matrix awakening",
        "30-minute retro synthwave hyper-highway journey toward the infinite striped horizon sunset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = "AI PROMPT-TO-VIDEO LONG FILM CODER",
            color = StudioCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "NVIDIA Nemotron-3 Super 120B (https://integrate.api.nvidia.com/v1)",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            placeholder = { Text("Enter prompt for long-form video (e.g. 40-minute cosmic odyssey in 4K UHD)...", color = TextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .testTag("prompt_input_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = StudioSurfaceVariant,
                unfocusedContainerColor = StudioSurface,
                focusedBorderColor = StudioCyan,
                unfocusedBorderColor = StudioSurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Epic Long-Form Presets:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(promptPresets) { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(StudioSurfaceVariant)
                        .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(16.dp))
                        .clickable { promptText = preset }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.take(38) + "…",
                        color = StudioCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Video Length Selector (Supporting 30s up to 60 Minutes!)
        Text("Target Video Length:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        val durationOptions = listOf(
            30f to "30s",
            120f to "2m",
            300f to "5m",
            900f to "15m",
            1800f to "30m (Long)",
            2400f to "40m (Epic)",
            3600f to "60m (Cinema)"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durationOptions.forEach { (dur, label) ->
                val isSel = selectedDuration == dur
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) StudioCyan.copy(alpha = 0.22f) else StudioSurfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (isSel) StudioCyan else StudioSurfaceBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedDuration = dur }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSel) StudioCyan else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Generate Button
        Button(
            onClick = {
                val p = promptText.ifBlank { "40-minute cosmic odyssey through black holes and Dyson spheres" }
                onGenerate(p, selectedDuration, selectedAspect)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_video_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(StudioCyan, StudioMagenta)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    val minLabel = if (selectedDuration >= 60f) "${(selectedDuration / 60).toInt()} Min" else "${selectedDuration.toInt()}s"
                    Text(
                        text = "Generate $minLabel 4K Video with Nemotron",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoCoderView(
    scriptCode: String,
    onCodeChange: (String) -> Unit,
    onRecompile: () -> Unit,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GENERATIVE TIMELINE CODER SCRIPT",
                    color = StudioCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text("Inspect and execute live timeline shader code", color = TextMuted, fontSize = 10.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyan)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 11.sp)
                }

                Button(
                    onClick = onRecompile,
                    modifier = Modifier.height(34.dp).testTag("recompile_script_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCyan)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recompile & Run", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code Editor Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF05070C)),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            OutlinedTextField(
                value = scriptCode,
                onValueChange = onCodeChange,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("code_editor_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color(0xFF70F5FF),
                    unfocusedTextColor = Color(0xFF70F5FF)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
private fun ThumbnailStudioView(
    thumbnail: com.example.data.model.ThumbnailModel,
    onUpdate: (String, String, String, String, String, String, Float, String) -> Unit
) {
    var badge by remember(thumbnail) { mutableStateOf(thumbnail.badge) }
    var title by remember(thumbnail) { mutableStateOf(thumbnail.title) }
    var subtitle by remember(thumbnail) { mutableStateOf(thumbnail.subtitle) }
    var primaryHex by remember(thumbnail) { mutableStateOf(thumbnail.primaryColorHex) }
    var secondaryHex by remember(thumbnail) { mutableStateOf(thumbnail.secondaryColorHex) }
    var focalArt by remember(thumbnail) { mutableStateOf(thumbnail.focalArt) }
    var glow by remember(thumbnail) { mutableFloatStateOf(thumbnail.glowIntensity) }
    var durationLabel by remember(thumbnail) { mutableStateOf(thumbnail.durationLabel) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = "HIGH-END 4K THUMBNAIL STUDIO",
            color = StudioCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text("High-impact cinematic thumbnail preview with neon badges and focal art", color = TextMuted, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(10.dp))

        // Thumbnail Canvas Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyan.copy(alpha = 0.5f))
        ) {
            ThumbnailCanvas(
                thumbnail = com.example.data.model.ThumbnailModel(
                    badge = badge,
                    title = title,
                    subtitle = subtitle,
                    primaryColorHex = primaryHex,
                    secondaryColorHex = secondaryHex,
                    focalArt = focalArt,
                    glowIntensity = glow,
                    durationLabel = durationLabel
                ),
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Main Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = subtitle,
            onValueChange = { subtitle = it },
            label = { Text("Subtitle Hook") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = badge,
                onValueChange = { badge = it },
                label = { Text("Badge Label") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = durationLabel,
                onValueChange = { durationLabel = it },
                label = { Text("Duration Pill") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Focal Art:", color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("NEON_PORTAL", "SINGULARITY", "QUANTUM_SPHERE", "DEEP_LEVIATHAN").forEach { art ->
                val isSel = focalArt.equals(art, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) StudioCyan.copy(alpha = 0.2f) else StudioSurfaceVariant)
                        .border(1.dp, if (isSel) StudioCyan else StudioSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { focalArt = art }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = art.split("_").first(),
                        color = if (isSel) StudioCyan else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onUpdate(badge, title, subtitle, primaryHex, secondaryHex, focalArt, glow, durationLabel) },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan)
        ) {
            Text("Save & Apply Thumbnail", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ExportStudioView(
    project: VideoProject,
    onStartExport: (VideoExportConfig) -> Unit
) {
    var selectedRes by remember { mutableStateOf("4K UHD (3840x2160)") }
    var selectedFps by remember { mutableStateOf(60) }
    var selectedCodec by remember { mutableStateOf("H.265 / HEVC 10-bit HDR") }
    var selectedBitrate by remember { mutableStateOf(85) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = "4K & 8K PROFESSIONAL EXPORT PIPELINE",
            color = StudioCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Long-form render suite with seamless multi-act transitions and 10-bit HDR mastering",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Export Presets:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        val resolutions = listOf(
            "4K UHD (3840x2160)" to "Master Ultra-HD Broadcast Standard (16:9)",
            "8K Super Resolution (7680x4320)" to "Next-Gen 8K Master Cinema Pipeline",
            "4K CinemaScope (5040x2160)" to "Anamorphic Ultra-Widescreen (21:9)",
            "4K Vertical Reel (2160x3840)" to "Full 4K Mobile Shorts & Long Reels (9:16)",
            "1080p FHD (1920x1080)" to "High-Speed Web Delivery Cut"
        )

        resolutions.forEach { (res, desc) ->
            val isSel = selectedRes == res
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { selectedRes = res },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSel) StudioSurfaceVariant else StudioSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSel) 1.5.dp else 1.dp,
                    color = if (isSel) StudioCyan else StudioSurfaceBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isSel) StudioCyan else TextMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSel) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StudioCyan)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = res, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = desc, color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Framerate:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(30, 60, 120).forEach { fps ->
                        val isSel = selectedFps == fps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) StudioCyan.copy(alpha = 0.25f) else StudioSurfaceVariant)
                                .border(1.dp, if (isSel) StudioCyan else StudioSurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedFps = fps }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${fps}fps", color = if (isSel) StudioCyan else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Master Bitrate:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(45, 85, 120).forEach { br ->
                        val isSel = selectedBitrate == br
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) StudioMagenta.copy(alpha = 0.25f) else StudioSurfaceVariant)
                                .border(1.dp, if (isSel) StudioMagenta else StudioSurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedBitrate = br }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${br}M", color = if (isSel) StudioMagenta else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Long Video Stats Box
        val estGb = (project.totalDurationSec * (selectedBitrate / 8f) / 1024f)
        val totalFrames = (project.totalDurationSec * selectedFps).toInt()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Frames", color = TextMuted, fontSize = 10.sp)
                    Text("$totalFrames", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Est. File Size", color = TextMuted, fontSize = 10.sp)
                    Text(String.format(Locale.US, "%.1f GB", estGb), color = StudioCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Audio Depth", color = TextMuted, fontSize = 10.sp)
                    Text("48kHz 24-Bit", color = StudioAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                onStartExport(
                    VideoExportConfig(
                        resolutionLabel = selectedRes,
                        fps = selectedFps,
                        codec = selectedCodec,
                        bitrateMbps = selectedBitrate
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_4k_export_button"),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan)
        ) {
            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            val durMin = (project.totalDurationSec / 60).toInt()
            Text("Render & Export $durMin-Min Film", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SavedProjectsView(
    projects: List<VideoProject>,
    onSelectProject: (VideoProject) -> Unit,
    onDeleteProject: (VideoProject) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "SAVED VIDEO PROJECTS (${projects.size})",
            color = StudioCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved projects yet. Generate your first video with Nemotron!", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(projects) { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectProject(p) },
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val mins = (p.totalDurationSec / 60).toInt()
                                val secs = (p.totalDurationSec % 60).toInt()
                                Text(
                                    text = "${p.scenes.size} scenes • ${mins}m ${secs}s • ${p.aspectRatio.label.split(" ").first()}",
                                    color = StudioCyan,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = p.prompt,
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(onClick = { onDeleteProject(p) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF5252)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerationProgressDialog(step: String, progress: Float) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = StudioCyan,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Nemotron Video Coder", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(text = step, color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = StudioCyan,
                    trackColor = StudioSurfaceBorder
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(progress * 100).toInt()}% synthesized",
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        },
        containerColor = StudioSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ExportProgressDialog(
    progress: Float,
    frameCount: Int,
    totalFrames: Int,
    config: VideoExportConfig,
    durationSec: Float,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Color(0xFFFF5252))
            }
        },
        title = {
            Text("Rendering 4K UHD Master", color = StudioCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column {
                val durMin = (durationSec / 60).toInt()
                Text(
                    text = "Rendering $durMin-min film in ${config.resolutionLabel} @ ${config.fps} FPS",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = StudioMagenta,
                    trackColor = StudioSurfaceBorder
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Frame $frameCount / $totalFrames", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "${(progress * 100).toInt()}%", color = StudioCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        },
        containerColor = StudioSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ExportSuccessDialog(
    config: VideoExportConfig,
    durationSec: Float,
    title: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCyan)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Video", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = TextSecondary)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StudioEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Master Video Render Complete", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column {
                val durMin = (durationSec / 60).toInt()
                Text(
                    text = "\"$title\" ($durMin-minute long film) rendered successfully with seamless multi-act transitions.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Resolution: ${config.resolutionLabel}", color = StudioCyan, fontSize = 11.sp)
                Text("• Runtime: $durMin minutes @ ${config.fps} FPS", color = StudioCyan, fontSize = 11.sp)
                Text("• Codec: ${config.codec}", color = StudioCyan, fontSize = 11.sp)
                Text("• Bitrate: ${config.bitrateMbps} Mbps (10-bit HDR)", color = StudioCyan, fontSize = 11.sp)
            }
        },
        containerColor = StudioSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

private fun formatLongTimecode(seconds: Float): String {
    val totalSec = seconds.toInt()
    val hours = totalSec / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }
}

private fun parseColorSafely(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = android.graphics.Color.parseColor("#$clean")
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}
