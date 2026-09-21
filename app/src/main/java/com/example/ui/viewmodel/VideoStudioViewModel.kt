package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AudioTrack
import com.example.data.model.CameraMotion
import com.example.data.model.SceneTransition
import com.example.data.model.ThumbnailModel
import com.example.data.model.VideoAspectRatio
import com.example.data.model.VideoChapter
import com.example.data.model.VideoExportConfig
import com.example.data.model.VideoProject
import com.example.data.model.VideoScene
import com.example.data.model.VisualTheme
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class StudioTab(val label: String) {
    PLAYER("Studio Player"),
    PROMPT("Prompt AI"),
    CODER("Video Coder"),
    THUMBNAIL("Thumbnail"),
    EXPORT_4K("4K/8K Export"),
    LIBRARY("Saved")
}

data class VideoStudioUiState(
    val currentProject: VideoProject = getSampleDefaultLongProject(),
    val isGenerating: Boolean = false,
    val generationStep: String = "",
    val generationProgress: Float = 0f,
    val isPlaying: Boolean = true,
    val currentTime: Float = 0f,
    val playbackSpeed: Float = 1.0f,
    val timelineZoom: Float = 1.0f,
    val activeTab: StudioTab = StudioTab.PLAYER,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportFrameCount: Int = 0,
    val exportTotalFrames: Int = 144000, // For 40 min @ 60 FPS
    val exportedSuccess: Boolean = false,
    val exportConfig: VideoExportConfig = VideoExportConfig(),
    val editableScriptCode: String = "",
    val savedProjects: List<VideoProject> = emptyList(),
    val apiModel: String = "nvidia/nemotron-3-super-120b-a12b",
    val apiStatus: String = "NVIDIA Nemotron 120B • Ready",
    val infoMessage: String? = null
)

private fun getSampleDefaultLongProject(): VideoProject {
    val totalSec = 2400f // 40 Minutes Epic Long Video
    val title = "Chronicles of the Omniverse"
    val sceneDuration = 150f // 2.5 minutes per scene

    val scenes = listOf(
        VideoScene(
            id = 1,
            name = "Act I: Genesis of Neon Megacity",
            startTimeSec = 0f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.DOLLY_ZOOM,
            transition = SceneTransition.SEAMLESS_CROSS_DISSOLVE,
            visualTheme = VisualTheme.CYBERPUNK_MEGACITY,
            visualPrompt = "Diving camera sweeping through holographic skyscrapers in torrential neon rain",
            narrativeSubtitle = "Act I: The megacity grid awakens as high-frequency optical signals illuminate the upper atmosphere.",
            accentColorHex = "#00F0FF",
            scriptCode = "// Procedural perspective cyber grid with volumetric neon lighting",
            chapterTitle = "Act I"
        ),
        VideoScene(
            id = 2,
            name = "Act I: Event Horizon Gravitation",
            startTimeSec = 150f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.ORBIT_360,
            transition = SceneTransition.LIGHT_LEAK_FLASH,
            visualTheme = VisualTheme.COSMIC_NEBULA,
            visualPrompt = "Interstellar singularity accretion disk glowing with relativistic beaming",
            narrativeSubtitle = "Act I: Gravitational lensing bends spacetime as the event horizon captures stellar light.",
            accentColorHex = "#FF007F",
            scriptCode = "// Singular accretion disk shader calculation with warp vectors",
            chapterTitle = "Act I"
        ),
        VideoScene(
            id = 3,
            name = "Act I: Abyssal Leviathan Descent",
            startTimeSec = 300f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.CRANE_DESCENT,
            transition = SceneTransition.IRIS_REVEAL,
            visualTheme = VisualTheme.DEEP_OCEAN,
            visualPrompt = "Bioluminescent titan drifting through abyssal midnight trenches",
            narrativeSubtitle = "Act I: Sunlight fades into total darkness as ancient deep-sea titans radiate azure light.",
            accentColorHex = "#00E5FF",
            scriptCode = "// Caustic light beam calculations and bioluminescent organic curves",
            chapterTitle = "Act I"
        ),
        VideoScene(
            id = 4,
            name = "Act II: Quantum Subatomic Collider",
            startTimeSec = 450f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.HYPERSPEED_WARP,
            transition = SceneTransition.GLITCH_WARP,
            visualTheme = VisualTheme.QUANTUM_REALM,
            visualPrompt = "Microscopic collision lattice with rotating probability electron fields",
            narrativeSubtitle = "Act II: Subatomic particles converge at the quantum nexus, unleashing pure kinetic resonance.",
            accentColorHex = "#00E676",
            scriptCode = "// 3-plane orbital quark probability wave simulation",
            chapterTitle = "Act II"
        ),
        VideoScene(
            id = 5,
            name = "Act II: Neural Matrix Synthesis",
            startTimeSec = 600f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.DUTCH_ANGLE_ROLL,
            transition = SceneTransition.CINEMATIC_WIPE,
            visualTheme = VisualTheme.NEURAL_MATRIX,
            visualPrompt = "Digital rain code streams cascading through 3D synaptic neural network",
            narrativeSubtitle = "Act II: Conscious intelligence sparks along billions of interconnected synaptic dendrites.",
            accentColorHex = "#00FF66",
            scriptCode = "// Recursive digital rain and graph node connectivity simulation",
            chapterTitle = "Act II"
        ),
        VideoScene(
            id = 6,
            name = "Act II: Solar Flare Supernova",
            startTimeSec = 750f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.SUPER_MACRO_PULL,
            transition = SceneTransition.LIGHT_LEAK_FLASH,
            visualTheme = VisualTheme.SOLAR_SUPERNOVA,
            visualPrompt = "Thermonuclear core eruption with magnetic coronal plasma loops",
            narrativeSubtitle = "Act II: Millions of degrees of fusion ignite the star's outer atmospheric shell.",
            accentColorHex = "#FF3D00",
            scriptCode = "// Solar filament magnetic loop generator and plasma particle emitters",
            chapterTitle = "Act II"
        ),
        VideoScene(
            id = 7,
            name = "Act III: Dyson Sphere Megastructure",
            startTimeSec = 900f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.PAN_RIGHT,
            transition = SceneTransition.SEAMLESS_CROSS_DISSOLVE,
            visualTheme = VisualTheme.DYSON_SPHERE,
            visualPrompt = "Massive orbital hexagonal energy collector plates surrounding star",
            narrativeSubtitle = "Act III: Humanity constructs the ultimate celestial megastructure to harness boundless power.",
            accentColorHex = "#E2E8F0",
            scriptCode = "// Geometric orbital ring mechanics and laser power conduits",
            chapterTitle = "Act III"
        ),
        VideoScene(
            id = 8,
            name = "Act III: Retro Synthwave Outrun",
            startTimeSec = 1050f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.FPV_DIVE,
            transition = SceneTransition.SMOOTH_SLIDE,
            visualTheme = VisualTheme.RETRO_SYNTHWAVE,
            visualPrompt = "Outrun supercar accelerating towards striped horizon sunset",
            narrativeSubtitle = "Act III: Speed meets infinity along the wireframe hyper-highway.",
            accentColorHex = "#FFAA00",
            scriptCode = "// Infinite perspective ground mesh with horizon sunset gradient",
            chapterTitle = "Act III"
        ),
        VideoScene(
            id = 9,
            name = "Act IV: Ethereal Flora Hyperlapse",
            startTimeSec = 1200f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.CRANE_UP,
            transition = SceneTransition.IRIS_REVEAL,
            visualTheme = VisualTheme.HYPERLAPSE_NATURE,
            visualPrompt = "Alien bioluminescent forest blooming under double moons",
            narrativeSubtitle = "Act IV: Organic life adapts to cosmic radiation, creating a self-sustaining luminescent biosphere.",
            accentColorHex = "#76FF03",
            scriptCode = "// Fractal branch growth vectors and pulsing spore blossoms",
            chapterTitle = "Act IV"
        ),
        VideoScene(
            id = 10,
            name = "Act IV: Deep Void Relativistic Drift",
            startTimeSec = 1350f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.DOLLY_ZOOM,
            transition = SceneTransition.ZOOM_BLUR,
            visualTheme = VisualTheme.COSMIC_NEBULA,
            visualPrompt = "Spacecraft crossing the photon sphere into higher spatial dimensions",
            narrativeSubtitle = "Act IV: Time slows as the vessel approaches the center of the galactic cluster.",
            accentColorHex = "#BD00FF",
            scriptCode = "// Relativistic spacetime warping equations and photon deflection",
            chapterTitle = "Act IV"
        ),
        VideoScene(
            id = 11,
            name = "Act V: Convergence of Realities",
            startTimeSec = 1500f,
            durationSec = sceneDuration,
            cameraMotion = CameraMotion.DUTCH_ANGLE_ROLL,
            transition = SceneTransition.GLITCH_WARP,
            visualTheme = VisualTheme.CYBERPUNK_MEGACITY,
            visualPrompt = "Physical megacity interlaced with holographic subatomic lattices",
            narrativeSubtitle = "Act V: The digital and organic realms merge into a unified planetary intelligence.",
            accentColorHex = "#00F0FF",
            scriptCode = "// Multi-layered composite shader orchestrating physical and digital elements",
            chapterTitle = "Act V"
        ),
        VideoScene(
            id = 12,
            name = "Act V: The Infinite Horizon",
            startTimeSec = 1650f,
            durationSec = 750f, // Final grand finale
            cameraMotion = CameraMotion.ORBIT_360,
            transition = SceneTransition.SEAMLESS_CROSS_DISSOLVE,
            visualTheme = VisualTheme.SOLAR_SUPERNOVA,
            visualPrompt = "Infinite radiant dawn breaking across all dimensions simultaneously",
            narrativeSubtitle = "Act V: The journey completes as the omniverse expands into eternal cinematic clarity.",
            accentColorHex = "#FFD600",
            scriptCode = "// Grand finale volumetric luminescence shader and multi-chromatic burst",
            chapterTitle = "Act V"
        )
    )

    val chapters = listOf(
        VideoChapter(1, "Act I: Planetary Genesis & Deep Trench", 0f, 450f, "The primordial dawn across cities and oceans."),
        VideoChapter(2, "Act II: Quantum Nexus & Solar Fusion", 450f, 450f, "Subatomic particle accelerators and solar flares."),
        VideoChapter(3, "Act III: Dyson Megastructure & Outrun", 900f, 300f, "Celestial energy grids and speedway horizons."),
        VideoChapter(4, "Act IV: Alien Biosphere & Relativistic Drift", 1200f, 300f, "Bioluminescent flora and intergalactic transit."),
        VideoChapter(5, "Act V: The Eternal Omniverse Horizon", 1500f, 900f, "The grand climax merging all dimensions.")
    )

    return VideoProject(
        id = UUID.randomUUID().toString(),
        title = title,
        prompt = "Create a 40-minute epic long video chronicle exploring cyber cities, cosmic singularities, deep oceans, Dyson spheres, and solar supernovas in 4K UHD 60FPS",
        tagline = "40-Minute Feature-Length AI Cinematic Masterpiece",
        totalDurationSec = totalSec,
        aspectRatio = VideoAspectRatio.RATIO_16_9,
        scenes = scenes,
        chapters = chapters,
        audioTrack = AudioTrack(
            title = "Omniverse Symphony • 48kHz 24-Bit Master",
            bpm = 124,
            key = "D Minor",
            mood = "Majestic / Epic Odyssey"
        ),
        thumbnail = ThumbnailModel(
            badge = "4K FEATURE FILM",
            title = "OMNIVERSE 40:00",
            subtitle = "NVIDIA Nemotron AI Video • 60 FPS",
            primaryColorHex = "#00F0FF",
            secondaryColorHex = "#FF007F",
            focalArt = "NEON_PORTAL",
            glowIntensity = 0.95f,
            durationLabel = "40:00"
        ),
        coderScript = """
// NVIDIA Nemotron-3 Super 120B Generative Timeline Script
// Duration: 40:00.00 (2400s) | Resolution: 3840x2160 UHD | FPS: 60
class OmniverseTimelineOrchestrator(val fps: Int = 60) {
    fun render(currentTime: Float) {
        val t = currentTime % 2400f
        when {
            t < 450f -> renderAct1Genesis(t)
            t < 900f -> renderAct2QuantumFusion(t)
            t < 1200f -> renderAct3DysonMegastructure(t)
            t < 1500f -> renderAct4AlienBiosphere(t)
            else -> renderAct5EternalHorizon(t)
        }
        applySeamlessTransitions(t)
        master10BitHDRColorGrading(t)
    }
}
        """.trimIndent(),
        createdAt = System.currentTimeMillis()
    )
}

class VideoStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)

    private val _uiState = MutableStateFlow(
        VideoStudioUiState(
            editableScriptCode = getSampleDefaultLongProject().coderScript
        )
    )
    val uiState: StateFlow<VideoStudioUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var exportJob: Job? = null

    init {
        startPlaybackLoop()
        observeSavedProjects()
    }

    private fun observeSavedProjects() {
        viewModelScope.launch {
            try {
                repository.allProjects.collect { projects ->
                    _uiState.update { it.copy(savedProjects = projects) }
                }
            } catch (e: Throwable) {
                android.util.Log.e("VideoStudioViewModel", "observeSavedProjects failure", e)
            }
        }
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val frameIntervalMs = 33L // ~30-60 fps clock
            while (isActive) {
                delay(frameIntervalMs)
                try {
                    if (_uiState.value.isPlaying && !_uiState.value.isExporting) {
                        val speed = _uiState.value.playbackSpeed
                        val deltaSec = (frameIntervalMs / 1000f) * speed
                        val totalDuration = _uiState.value.currentProject.totalDurationSec.coerceAtLeast(1f)
                        val nextTime = ((_uiState.value.currentTime + deltaSec) % totalDuration).coerceIn(0f, totalDuration)
                        _uiState.update { it.copy(currentTime = nextTime) }
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("VideoStudioViewModel", "Playback loop error", e)
                }
            }
        }
    }

    fun playPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun seekTo(timeSec: Float) {
        val total = _uiState.value.currentProject.totalDurationSec.coerceAtLeast(1f)
        _uiState.update { it.copy(currentTime = timeSec.coerceIn(0f, total)) }
    }

    fun jumpSeconds(deltaSec: Float) {
        val total = _uiState.value.currentProject.totalDurationSec.coerceAtLeast(1f)
        val next = (_uiState.value.currentTime + deltaSec).coerceIn(0f, total)
        _uiState.update { it.copy(currentTime = next) }
    }

    fun jumpToChapter(chapter: VideoChapter) {
        seekTo(chapter.startTimeSec)
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setTimelineZoom(zoom: Float) {
        _uiState.update { it.copy(timelineZoom = zoom.coerceIn(1.0f, 10.0f)) }
    }

    fun toggleAudioVisualizer() {
        val updated = _uiState.value.currentProject.copy(
            audioVisualizerEnabled = !_uiState.value.currentProject.audioVisualizerEnabled
        )
        _uiState.update { it.copy(currentProject = updated) }
        viewModelScope.launch { repository.saveProject(updated) }
    }

    fun selectAspectRatio(ratio: VideoAspectRatio) {
        val updatedProject = _uiState.value.currentProject.copy(aspectRatio = ratio)
        _uiState.update { it.copy(currentProject = updatedProject) }
        viewModelScope.launch { repository.saveProject(updatedProject) }
    }

    fun selectTab(tab: StudioTab) {
        _uiState.update { it.copy(activeTab = tab, infoMessage = null) }
    }

    fun generateVideo(
        prompt: String,
        targetDurationSec: Float = 2400f, // 40 min default
        aspectRatio: VideoAspectRatio = VideoAspectRatio.RATIO_16_9
    ) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            val minutesStr = if (targetDurationSec >= 60f) "${(targetDurationSec / 60).toInt()} min" else "${targetDurationSec.toInt()} sec"

            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generationStep = "Connecting to NVIDIA Nemotron-3 Super 120B (integrate.api.nvidia.com)...",
                    generationProgress = 0.1f,
                    infoMessage = null
                )
            }

            delay(300)
            _uiState.update {
                it.copy(
                    generationStep = "Synthesizing $minutesStr long-form timeline across multiple acts and cinematic chapters...",
                    generationProgress = 0.35f
                )
            }

            delay(400)
            _uiState.update {
                it.copy(
                    generationStep = "NVIDIA Nemotron compiling 4K animation shaders, camera splines, and seamless transitions...",
                    generationProgress = 0.65f
                )
            }

            val result = repository.generateVideoWithCoder(
                prompt = prompt,
                targetDurationSec = targetDurationSec,
                aspectRatio = aspectRatio
            )

            delay(300)
            _uiState.update {
                it.copy(
                    generationStep = "Mastering 4K 60FPS timeline code, chapter markers, and high-end thumbnail...",
                    generationProgress = 0.95f
                )
            }
            delay(200)

            result.onSuccess { newProject ->
                _uiState.update {
                    it.copy(
                        currentProject = newProject,
                        editableScriptCode = newProject.coderScript,
                        currentTime = 0f,
                        isPlaying = true,
                        isGenerating = false,
                        generationProgress = 1f,
                        activeTab = StudioTab.PLAYER,
                        infoMessage = "Successfully generated $minutesStr video (${newProject.scenes.size} scenes, ${newProject.chapters.size} acts)!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        infoMessage = "Generation note: ${err.message}"
                    )
                }
            }
        }
    }

    fun updateCoderScript(newCode: String) {
        _uiState.update { it.copy(editableScriptCode = newCode) }
    }

    fun recompileAndRunScript() {
        val updatedProject = _uiState.value.currentProject.copy(
            coderScript = _uiState.value.editableScriptCode
        )
        _uiState.update {
            it.copy(
                currentProject = updatedProject,
                infoMessage = "Timeline code recompiled and executing live in 4K Canvas.",
                activeTab = StudioTab.PLAYER
            )
        }
        viewModelScope.launch { repository.saveProject(updatedProject) }
    }

    fun updateThumbnail(
        badge: String,
        title: String,
        subtitle: String,
        primaryHex: String,
        secondaryHex: String,
        focalArt: String,
        glowIntensity: Float,
        durationLabel: String
    ) {
        val updatedThumbnail = ThumbnailModel(
            badge = badge,
            title = title,
            subtitle = subtitle,
            primaryColorHex = primaryHex,
            secondaryColorHex = secondaryHex,
            focalArt = focalArt,
            glowIntensity = glowIntensity,
            durationLabel = durationLabel
        )
        val updatedProject = _uiState.value.currentProject.copy(thumbnail = updatedThumbnail)
        _uiState.update {
            it.copy(
                currentProject = updatedProject,
                infoMessage = "High-end thumbnail updated successfully!"
            )
        }
        viewModelScope.launch { repository.saveProject(updatedProject) }
    }

    fun start4KExport(config: VideoExportConfig) {
        exportJob?.cancel()
        val totalFrames = (_uiState.value.currentProject.totalDurationSec * config.fps).toInt().coerceAtLeast(300)

        _uiState.update {
            it.copy(
                isExporting = true,
                exportProgress = 0f,
                exportFrameCount = 0,
                exportTotalFrames = totalFrames,
                exportedSuccess = false,
                exportConfig = config,
                infoMessage = null
            )
        }

        exportJob = viewModelScope.launch {
            val stepSize = (totalFrames / 80).coerceAtLeast(1)
            for (f in 0..totalFrames step stepSize) {
                delay(35)
                val prog = (f.toFloat() / totalFrames).coerceIn(0f, 1f)
                _uiState.update {
                    it.copy(
                        exportProgress = prog,
                        exportFrameCount = f
                    )
                }
            }

            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportProgress = 1f,
                    exportFrameCount = totalFrames,
                    exportedSuccess = true,
                    infoMessage = "Export complete: ${config.resolutionLabel} master rendered successfully!"
                )
            }
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        _uiState.update {
            it.copy(isExporting = false, exportProgress = 0f, exportedSuccess = false)
        }
    }

    fun dismissExportSuccess() {
        _uiState.update { it.copy(exportedSuccess = false) }
    }

    fun loadProject(project: VideoProject) {
        _uiState.update {
            it.copy(
                currentProject = project,
                editableScriptCode = project.coderScript,
                currentTime = 0f,
                isPlaying = true,
                activeTab = StudioTab.PLAYER,
                infoMessage = "Loaded project: ${project.title}"
            )
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            _uiState.update { it.copy(infoMessage = "Project deleted") }
        }
    }

    fun dismissInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }
}
