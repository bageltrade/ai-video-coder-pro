package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

enum class VideoAspectRatio(val label: String, val ratio: Float, val widthPx: Int, val heightPx: Int) {
    RATIO_16_9("16:9 Landscape (4K 3840x2160)", 16f / 9f, 3840, 2160),
    RATIO_9_16("9:16 Vertical (4K 2160x3840)", 9f / 16f, 2160, 3840),
    RATIO_1_1("1:1 Square (4K 2160x2160)", 1f, 2160, 2160),
    RATIO_21_9("21:9 CinemaScope (4K 5040x2160)", 21f / 9f, 5040, 2160),
    RATIO_4_3("4:3 IMAX Classic (4K 2880x2160)", 4f / 3f, 2880, 2160)
}

enum class CameraMotion(val displayName: String, val speedFactor: Float) {
    PAN_RIGHT("Cinematic Pan Right", 1.0f),
    DOLLY_ZOOM("Vertigo Dolly Zoom", 1.4f),
    ORBIT_360("Orbital 360 Sweep", 1.2f),
    CRANE_UP("Atmospheric Crane Up", 0.9f),
    CRANE_DESCENT("Slow Atmospheric Descent", 0.8f),
    HYPERSPEED_WARP("Hyperspeed Warp Tunnel", 2.2f),
    FPV_DIVE("FPV Drone High-Speed Dive", 1.8f),
    DUTCH_ANGLE_ROLL("Dutch Angle Orbital Roll", 1.3f),
    SUPER_MACRO_PULL("Super Macro Lens Pullback", 1.5f)
}

enum class SceneTransition(val displayName: String, val durationSec: Float) {
    SEAMLESS_CROSS_DISSOLVE("Seamless Cross-Dissolve", 1.2f),
    ZOOM_BLUR("Hyper Zoom Blur", 0.9f),
    LIGHT_LEAK_FLASH("Anamorphic Light Leak Flash", 0.8f),
    GLITCH_WARP("Digital Glitch Warp", 0.7f),
    CINEMATIC_WIPE("Soft Directional Wipe", 1.0f),
    SMOOTH_SLIDE("Kinetic Camera Slide", 1.1f),
    IRIS_REVEAL("Cinematic Iris Reveal", 1.0f)
}

enum class VisualTheme(val themeName: String, val defaultColors: List<String>) {
    CYBERPUNK_MEGACITY("Cyberpunk Megacity", listOf("#00F0FF", "#BD00FF", "#0A0D14")),
    COSMIC_NEBULA("Cosmic Singularity Nebula", listOf("#FF007F", "#7928CA", "#050510")),
    DEEP_OCEAN("Abyssal Bioluminescence", listOf("#00E5FF", "#00B0FF", "#001020")),
    QUANTUM_REALM("Quantum Subatomic Collision", listOf("#00E676", "#00F0FF", "#0A140E")),
    RETRO_SYNTHWAVE("Outrun Synthwave Sunset", listOf("#FF007F", "#FFAA00", "#180026")),
    HYPERLAPSE_NATURE("Bioluminescent Flora Hyperlapse", listOf("#76FF03", "#00E5FF", "#081C0E")),
    NEURAL_MATRIX("Neural Matrix Data Stream", listOf("#00FF66", "#00E5FF", "#03080A")),
    SOLAR_SUPERNOVA("Solar Flare & Supernova", listOf("#FF3D00", "#FFD600", "#180500")),
    DYSON_SPHERE("Dyson Sphere Megastructure", listOf("#E0E0E0", "#00E5FF", "#0A0E17"))
}

@JsonClass(generateAdapter = true)
data class ThumbnailModel(
    val badge: String = "4K ULTRA HD",
    val title: String = "CYBERNETIC HORIZON",
    val subtitle: String = "AI Generative Masterpiece",
    val primaryColorHex: String = "#00F0FF",
    val secondaryColorHex: String = "#FF007F",
    val focalArt: String = "NEON_PORTAL",
    val glowIntensity: Float = 0.9f,
    val durationLabel: String = "40:00"
)

@JsonClass(generateAdapter = true)
data class VideoScene(
    val id: Int = 1,
    val name: String = "Scene 1: Initial Inception",
    val startTimeSec: Float = 0f,
    val durationSec: Float = 6f,
    val cameraMotion: CameraMotion = CameraMotion.DOLLY_ZOOM,
    val transition: SceneTransition = SceneTransition.SEAMLESS_CROSS_DISSOLVE,
    val visualTheme: VisualTheme = VisualTheme.CYBERPUNK_MEGACITY,
    val visualPrompt: String = "",
    val scriptCode: String = "",
    val narrativeSubtitle: String = "",
    val accentColorHex: String = "#00F0FF",
    val chapterTitle: String = "Act I"
)

@JsonClass(generateAdapter = true)
data class VideoChapter(
    val id: Int = 1,
    val title: String = "Act I: Inception",
    val startTimeSec: Float = 0f,
    val durationSec: Float = 300f,
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class AudioTrack(
    val title: String = "Cinematic Synthwave & Orchestral Sub-Bass",
    val bpm: Int = 120,
    val key: String = "F Minor",
    val mood: String = "Epic / Sci-Fi Odyssey",
    val waveformLevels: List<Float> = listOf(0.3f, 0.5f, 0.8f, 0.6f, 0.9f, 0.7f, 0.4f, 0.6f, 0.85f, 0.5f)
)

@JsonClass(generateAdapter = true)
data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Video Project",
    val prompt: String = "",
    val tagline: String = "",
    val totalDurationSec: Float = 2400f, // Default 40 minutes for long videos
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.RATIO_16_9,
    val scenes: List<VideoScene> = emptyList(),
    val chapters: List<VideoChapter> = emptyList(),
    val audioTrack: AudioTrack = AudioTrack(),
    val thumbnail: ThumbnailModel = ThumbnailModel(),
    val coderScript: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val modelUsed: String = "nvidia/nemotron-3-super-120b-a12b",
    val audioVisualizerEnabled: Boolean = true
)

data class VideoExportConfig(
    val resolutionLabel: String = "4K UHD (3840x2160)",
    val width: Int = 3840,
    val height: Int = 2160,
    val fps: Int = 60,
    val codec: String = "H.265 / HEVC High Tier",
    val bitrateMbps: Int = 85,
    val colorDepth: String = "10-bit HDR (BT.2020)",
    val exportPreset: String = "Master Studio 4K UHD",
    val audioQuality: String = "48kHz 24-bit PCM Uncompressed",
    val isLongVideoOptimizationEnabled: Boolean = true
)

// NVIDIA OpenAI-compatible chat API models
@JsonClass(generateAdapter = true)
data class NvidiaChatRequest(
    val model: String = "nvidia/nemotron-3-super-120b-a12b",
    val messages: List<NvidiaMessage>,
    val temperature: Double = 0.6,
    val max_tokens: Int = 4096,
    val top_p: Double = 0.9
)

@JsonClass(generateAdapter = true)
data class NvidiaMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class NvidiaChatResponse(
    val id: String? = null,
    val choices: List<NvidiaChoice>? = null,
    val usage: NvidiaUsage? = null
)

@JsonClass(generateAdapter = true)
data class NvidiaChoice(
    val index: Int? = null,
    val message: NvidiaMessage,
    val finish_reason: String? = null
)

@JsonClass(generateAdapter = true)
data class NvidiaUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)
