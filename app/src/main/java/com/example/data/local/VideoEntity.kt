package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_projects")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val prompt: String,
    val tagline: String,
    val totalDurationSec: Float,
    val aspectRatioName: String,
    val scenesJson: String,
    val thumbnailJson: String,
    val coderScript: String,
    val createdAt: Long,
    val modelUsed: String,
    val chaptersJson: String = "",
    val audioTrackJson: String = ""
)
