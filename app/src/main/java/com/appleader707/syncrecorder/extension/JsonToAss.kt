package com.appleader707.syncrecorder.extension

import com.appleader707.syncrecorder.domain.SensorSnapshot
import com.google.gson.Gson
import java.io.File

fun convertJsonToAss(jsonlFile: File, outputAssFile: File) {
    val assHeader = """
        [Script Info]
        Title: Sensor Data
        ScriptType: v4.00+
        Collisions: Normal
        PlayResX: 384
        PlayResY: 288
        [V4+ Styles]
        Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding
        Style: Default,Arial,1,&H00FFFFFF,&H000000FF,&H00000000,&H64000000,0,0,0,0,100,100,0,0,1,1,0,2,10,10,10,1
        [Events]
        Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
    """.trimIndent()

    val gson = Gson()
    val lines = mutableListOf<String>()

    jsonlFile.forEachLine { line ->
        val snapshot = gson.fromJson(line, SensorSnapshot::class.java)
        val startMs = snapshot.timestampNanos / 1_000_000
        val endMs = startMs + 200
        val start = millisToAssTime(startMs)
        val end = millisToAssTime(endMs)
        lines.add("Dialogue: 0,$start,$end,Default,,0,0,0,,${snapshot.type}:${snapshot.values.joinToString()}")
    }

    outputAssFile.writeText(assHeader + "\n" + lines.joinToString("\n"))
}

fun millisToAssTime(ms: Long): String {
    val hours = ms / 3600000
    val minutes = (ms / 60000) % 60
    val seconds = (ms / 1000) % 60
    val centiseconds = (ms % 1000) / 10
    return String.format("%01d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds)
}