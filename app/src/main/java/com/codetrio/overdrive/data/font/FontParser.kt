package com.codetrio.overdrive.data.font

import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * High-performance parser for TrueType (TTF) and OpenType (OTF) font files.
 * Extracts font name, family name, PostScript name, and OpenType Font Variations (fvar table) axes.
 */
object FontParser {
    private const val TAG = "FontParser"

    private const val TAG_NAME = 0x6E616D65 // "name"
    private const val TAG_FVAR = 0x66766172 // "fvar"

    data class ParsedFontInfo(
        val familyName: String,
        val fullName: String?,
        val postScriptName: String?,
        val isVariable: Boolean,
        val axes: List<VariableAxis>
    )

    fun parse(file: File): ParsedFontInfo? {
        if (!file.exists() || file.length() < 12) return null

        try {
            RandomAccessFile(file, "r").use { raf ->
                val buffer = ByteArray(12)
                raf.readFully(buffer)
                val header = ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN)

                val sfntVersion = header.int
                // Standard TrueType 0x00010000, OpenType 'OTTO', TrueType Collections 'ttcf', etc.
                val numTables = header.short.toInt() and 0xFFFF

                if (numTables <= 0 || numTables > 200) return null

                // Read Table Directory
                val tableDirSize = numTables * 16
                val tableDirBytes = ByteArray(tableDirSize)
                raf.readFully(tableDirBytes)
                val tableDir = ByteBuffer.wrap(tableDirBytes).order(ByteOrder.BIG_ENDIAN)

                var nameOffset = 0L
                var nameLength = 0L
                var fvarOffset = 0L
                var fvarLength = 0L

                for (i in 0 until numTables) {
                    val tag = tableDir.int
                    tableDir.int // skip checksum
                    val offset = tableDir.int.toLong() and 0xFFFFFFFFL
                    val length = tableDir.int.toLong() and 0xFFFFFFFFL

                    if (tag == TAG_NAME) {
                        nameOffset = offset
                        nameLength = length
                    } else if (tag == TAG_FVAR) {
                        fvarOffset = offset
                        fvarLength = length
                    }
                }

                val names = if (nameOffset > 0 && nameLength > 0 && nameOffset + nameLength <= file.length()) {
                    raf.seek(nameOffset)
                    val nameData = ByteArray(nameLength.toInt())
                    raf.readFully(nameData)
                    parseNameTable(nameData)
                } else null

                val axes = if (fvarOffset > 0 && fvarLength > 0 && fvarOffset + fvarLength <= file.length()) {
                    raf.seek(fvarOffset)
                    val fvarData = ByteArray(fvarLength.toInt())
                    raf.readFully(fvarData)
                    parseFvarTable(fvarData)
                } else emptyList()

                val fallbackName = file.nameWithoutExtension.replace("_", " ").replace("-", " ")
                val familyName = names?.get(16) ?: names?.get(1) ?: names?.get(4) ?: fallbackName
                val fullName = names?.get(4) ?: familyName
                val psName = names?.get(6)

                return ParsedFontInfo(
                    familyName = familyName.trim(),
                    fullName = fullName.trim(),
                    postScriptName = psName?.trim(),
                    isVariable = axes.isNotEmpty(),
                    axes = axes
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing font file: ${file.absolutePath}", e)
            val fallbackName = file.nameWithoutExtension.replace("_", " ").replace("-", " ")
            return ParsedFontInfo(
                familyName = fallbackName,
                fullName = fallbackName,
                postScriptName = null,
                isVariable = false,
                axes = emptyList()
            )
        }
    }

    private fun parseNameTable(data: ByteArray): Map<Int, String> {
        val names = mutableMapOf<Int, String>()
        if (data.size < 6) return names

        val buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
        val format = buffer.short.toInt() and 0xFFFF
        val count = buffer.short.toInt() and 0xFFFF
        val stringOffset = buffer.short.toInt() and 0xFFFF

        for (i in 0 until count) {
            if (buffer.position() + 12 > data.size) break
            val platformID = buffer.short.toInt() and 0xFFFF
            val encodingID = buffer.short.toInt() and 0xFFFF
            val languageID = buffer.short.toInt() and 0xFFFF
            val nameID = buffer.short.toInt() and 0xFFFF
            val length = buffer.short.toInt() and 0xFFFF
            val offset = buffer.short.toInt() and 0xFFFF

            val strStart = stringOffset + offset
            if (strStart + length <= data.size) {
                try {
                    val strBytes = ByteArray(length)
                    System.arraycopy(data, strStart, strBytes, 0, length)
                    val str = when (platformID) {
                        0 -> String(strBytes, StandardCharsets.UTF_16BE) // Unicode
                        3 -> String(strBytes, StandardCharsets.UTF_16BE) // Windows (Unicode)
                        1 -> {
                            if (encodingID == 1) { // Japanese (Shift_JIS)
                                try {
                                    String(strBytes, java.nio.charset.Charset.forName("Shift_JIS"))
                                } catch (_: Exception) {
                                    String(strBytes, StandardCharsets.ISO_8859_1)
                                }
                            } else {
                                String(strBytes, StandardCharsets.ISO_8859_1) // Macintosh Roman
                            }
                        }
                        else -> String(strBytes, StandardCharsets.UTF_8)
                    }.trim().replace("\u0000", "")

                    if (str.isNotBlank()) {
                        // Prioritize English (0x0409) or Japanese (0x0411) on Windows, or UTF-16 Unicode
                        val isPreferredLang = languageID == 0x0411 || languageID == 0x0409 || languageID == 0
                        if (!names.containsKey(nameID) || (platformID == 3 && isPreferredLang)) {
                            names[nameID] = str
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return names
    }

    private fun parseFvarTable(data: ByteArray): List<VariableAxis> {
        val axes = mutableListOf<VariableAxis>()
        if (data.size < 16) return axes

        val buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
        val majorVersion = buffer.short.toInt() and 0xFFFF
        val minorVersion = buffer.short.toInt() and 0xFFFF
        val axesArrayOffset = buffer.short.toInt() and 0xFFFF
        buffer.short // reserved
        val axisCount = buffer.short.toInt() and 0xFFFF
        val axisSize = buffer.short.toInt() and 0xFFFF

        if (axisCount <= 0 || axisSize < 20 || axesArrayOffset + (axisCount * axisSize) > data.size) {
            return axes
        }

        buffer.position(axesArrayOffset)
        for (i in 0 until axisCount) {
            val axisStart = axesArrayOffset + (i * axisSize)
            buffer.position(axisStart)

            val tagBytes = ByteArray(4)
            buffer.get(tagBytes)
            val tag = String(tagBytes, StandardCharsets.US_ASCII)

            val minVal = readFixed(buffer)
            val defVal = readFixed(buffer)
            val maxVal = readFixed(buffer)
            val flags = buffer.short.toInt() and 0xFFFF
            val nameID = buffer.short.toInt() and 0xFFFF

            val axisName = when (tag) {
                "wght" -> "Weight"
                "wdth" -> "Width"
                "slnt" -> "Slant"
                "ital" -> "Italic"
                "opsz" -> "Optical Size"
                "ROND" -> "Roundness"
                "GRAD" -> "Grade"
                "CASL" -> "Casual"
                "CRSV" -> "Cursive"
                "FILL" -> "Fill"
                else -> tag.uppercase()
            }

            axes.add(
                VariableAxis(
                    tag = tag,
                    name = axisName,
                    minValue = minVal,
                    maxValue = maxVal,
                    defaultValue = defVal
                )
            )
        }

        return axes
    }

    private fun readFixed(buffer: ByteBuffer): Float {
        val raw = buffer.int
        return raw.toFloat() / 65536.0f
    }
}
