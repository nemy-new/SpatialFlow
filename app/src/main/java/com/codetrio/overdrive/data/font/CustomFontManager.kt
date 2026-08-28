package com.codetrio.overdrive.data.font

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.codetrio.overdrive.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton repository & manager for application custom fonts, variable fonts,
 * and per-target typography mappings.
 */
class CustomFontManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val fontsDirectory: File by lazy {
        File(context.filesDir, "custom_fonts").apply {
            if (!exists()) mkdirs()
        }
    }

    private val _availableFonts = MutableStateFlow<List<CustomFontItem>>(emptyList())
    val availableFonts: StateFlow<List<CustomFontItem>> = _availableFonts.asStateFlow()

    private val _selectedFontIds = MutableStateFlow<Map<FontTarget, String>>(emptyMap())
    val selectedFontIds: StateFlow<Map<FontTarget, String>> = _selectedFontIds.asStateFlow()

    private val _variationConfigs = MutableStateFlow<Map<FontTarget, FontVariationConfig>>(emptyMap())
    val variationConfigs: StateFlow<Map<FontTarget, FontVariationConfig>> = _variationConfigs.asStateFlow()

    private val _downloadingFontIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingFontIds: StateFlow<Set<String>> = _downloadingFontIds.asStateFlow()

    // Cache of instantiated FontFamilies for performance
    private val fontFamilyCache = ConcurrentHashMap<String, FontFamily>()

    // Global version ticker to trigger Compose recomposition on font setting changes
    private val _fontChangeTicker = MutableStateFlow(0L)
    val fontChangeTicker: StateFlow<Long> = _fontChangeTicker.asStateFlow()

    companion object {
        private const val TAG = "CustomFontManager"
        private const val PREFS_NAME = "CustomFontPrefs"

        const val BUILTIN_GOOGLE_SANS_FLEX = "builtin_google_sans_flex"
        const val BUILTIN_GOOGLE_SANS_FLEX_NON_ROUNDED = "builtin_google_sans_flex_non_rounded"
        const val BUILTIN_SYSTEM_DEFAULT = "builtin_system_default"

        val cloudJapaneseFonts: List<CloudFontItem> = listOf(
            CloudFontItem(
                id = "cloud_noto_sans_jp",
                name = "Noto Sans JP",
                category = "ゴシック体 (バリアブル)",
                description = "Google製・最高峰の視認性と美しさを持つ標準日本語ゴシック",
                sampleText = "あふれ出す想いと旋律が、心を満たしていく。",
                isVariable = true,
                downloadUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/notosansjp/NotoSansJP%5Bwght%5D.ttf",
                fileName = "NotoSansJP[wght].ttf",
                estimatedSizeMb = 5.2f
            ),
            CloudFontItem(
                id = "cloud_mplus_1",
                name = "M PLUS 1",
                category = "モダンゴシック (バリアブル)",
                description = "洗練された幾何学的デザインのモダン日本語バリアブルフォント",
                sampleText = "音楽と、生きていく。未来を奏でる響き。",
                isVariable = true,
                downloadUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/mplus1/MPLUS1%5Bwght%5D.ttf",
                fileName = "MPLUS1[wght].ttf",
                estimatedSizeMb = 1.9f
            ),
            CloudFontItem(
                id = "cloud_zen_maru_gothic",
                name = "Zen Maru Gothic (禅丸ゴシック)",
                category = "丸ゴシック体",
                description = "温かみと柔らかさを兼ね備えた親しみやすい丸ゴシック",
                sampleText = "やわらかな音色が空間に溶け込んでいく。",
                isVariable = false,
                downloadUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/zenmarugothic/ZenMaruGothic-Regular.ttf",
                fileName = "ZenMaruGothic-Regular.ttf",
                estimatedSizeMb = 4.8f
            ),
            CloudFontItem(
                id = "cloud_mplus_rounded_1c",
                name = "M PLUS Rounded 1c",
                category = "丸ゴシック体",
                description = "丸みを帯びたポップでクリアな人気丸ゴシック体",
                sampleText = "きらめくリズム、どこまでも広がる世界。",
                isVariable = false,
                downloadUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/mplusrounded1c/MPLUSRounded1c-Regular.ttf",
                fileName = "MPLUSRounded1c-Regular.ttf",
                estimatedSizeMb = 5.4f
            ),
            CloudFontItem(
                id = "cloud_zen_kaku_gothic",
                name = "Zen Kaku Gothic New",
                category = "角ゴシック体",
                description = "シャープで洗練された端正なプロポーションの角ゴシック",
                sampleText = "鮮明で力強い音像がリアルに広がる。",
                isVariable = false,
                downloadUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/zenkakugothicnew/ZenKakuGothicNew-Regular.ttf",
                fileName = "ZenKakuGothicNew-Regular.ttf",
                estimatedSizeMb = 4.7f
            ),
            CloudFontItem(
                id = "cloud_shippori_mincho",
                name = "Shippori Mincho (しっぽり明朝)",
                category = "明朝体",
                description = "情緒的でエレガントな伝統美を誇る本格派明朝体",
                sampleText = "静寂の中に響く、一筋の光と詩情。",
                isVariable = false,
                downloadUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/shipporimincho/ShipporiMincho-Regular.ttf",
                fileName = "ShipporiMincho-Regular.ttf",
                estimatedSizeMb = 4.9f
            ),
            CloudFontItem(
                id = "cloud_noto_serif_jp",
                name = "Noto Serif JP (源ノ明朝)",
                category = "明朝体 (バリアブル)",
                description = "格調高く気品あふれるGoogleの日本語バリアブル明朝体",
                sampleText = "深みのあるハーモニーが心を揺さぶる。",
                isVariable = true,
                downloadUrl = "https://raw.githubusercontent.com/google/fonts/main/ofl/notoserifjp/NotoSerifJP%5Bwght%5D.ttf",
                fileName = "NotoSerifJP[wght].ttf",
                estimatedSizeMb = 9.8f
            )
        )

        private var INSTANCE: CustomFontManager? = null

        fun getInstance(context: Context): CustomFontManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CustomFontManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        loadInstalledFonts()
        loadPreferences()
    }

    private fun loadPreferences() {
        val fontMap = mutableMapOf<FontTarget, String>()
        val varMap = mutableMapOf<FontTarget, FontVariationConfig>()

        FontTarget.entries.forEach { target ->
            val defaultFontId = when (target) {
                FontTarget.LYRICS -> BUILTIN_GOOGLE_SANS_FLEX_NON_ROUNDED
                else -> BUILTIN_GOOGLE_SANS_FLEX
            }
            val fontId = prefs.getString("font_id_${target.name}", defaultFontId) ?: defaultFontId
            fontMap[target] = fontId

            val configJson = prefs.getString("var_config_${target.name}", null)
            val config = if (configJson != null) {
                try {
                    val obj = JSONObject(configJson)
                    val customAxes = mutableMapOf<String, Float>()
                    if (obj.has("custom")) {
                        val custObj = obj.getJSONObject("custom")
                        custObj.keys().forEach { k ->
                            customAxes[k] = custObj.getDouble(k).toFloat()
                        }
                    }
                    FontVariationConfig(
                        weight = obj.optDouble("wght", 400.0).toFloat(),
                        width = obj.optDouble("wdth", 100.0).toFloat(),
                        slant = obj.optDouble("slnt", 0.0).toFloat(),
                        roundness = obj.optDouble("ROND", if (target == FontTarget.LYRICS) 0.0 else 100.0).toFloat(),
                        opticalSize = obj.optDouble("opsz", 14.0).toFloat(),
                        customAxes = customAxes
                    )
                } catch (e: Exception) {
                    getDefaultConfigForTarget(target)
                }
            } else {
                getDefaultConfigForTarget(target)
            }
            varMap[target] = config
        }

        _selectedFontIds.value = fontMap
        _variationConfigs.value = varMap
    }

    private fun getDefaultConfigForTarget(target: FontTarget): FontVariationConfig {
        return when (target) {
            FontTarget.LYRICS -> FontVariationConfig(weight = 600f, width = 100f, slant = 0f, roundness = 0f, opticalSize = 18f)
            FontTarget.PLAYER_TITLE -> FontVariationConfig(weight = 700f, width = 100f, slant = 0f, roundness = 100f, opticalSize = 22f)
            FontTarget.HEADINGS -> FontVariationConfig(weight = 700f, width = 100f, slant = 0f, roundness = 100f, opticalSize = 24f)
            FontTarget.GLOBAL -> FontVariationConfig(weight = 400f, width = 100f, slant = 0f, roundness = 100f, opticalSize = 14f)
        }
    }

    private fun loadInstalledFonts() {
        val list = mutableListOf<CustomFontItem>()

        // 1. Built-in: Google Sans Flex (Max Rounded)
        list.add(
            CustomFontItem(
                id = BUILTIN_GOOGLE_SANS_FLEX,
                name = "Google Sans Flex (丸み 100%)",
                postScriptName = "GoogleSansFlex-Regular",
                isBuiltIn = true,
                isVariable = true,
                resId = R.font.google_sans_flex,
                fileSize = 4153208L,
                supportedAxes = listOf(
                    VariableAxis("wght", "Weight", 100f, 1000f, 400f),
                    VariableAxis("wdth", "Width", 25f, 151f, 100f),
                    VariableAxis("slnt", "Slant", -10f, 0f, 0f),
                    VariableAxis("ROND", "Roundness", 0f, 100f, 100f),
                    VariableAxis("opsz", "Optical Size", 6f, 144f, 14f)
                )
            )
        )

        // 2. Built-in: Google Sans Flex (Non-Rounded)
        list.add(
            CustomFontItem(
                id = BUILTIN_GOOGLE_SANS_FLEX_NON_ROUNDED,
                name = "Google Sans Flex (標準・丸みなし)",
                postScriptName = "GoogleSansFlex-Regular",
                isBuiltIn = true,
                isVariable = true,
                resId = R.font.google_sans_flex,
                fileSize = 4153208L,
                supportedAxes = listOf(
                    VariableAxis("wght", "Weight", 100f, 1000f, 400f),
                    VariableAxis("wdth", "Width", 25f, 151f, 100f),
                    VariableAxis("slnt", "Slant", -10f, 0f, 0f),
                    VariableAxis("ROND", "Roundness", 0f, 100f, 0f),
                    VariableAxis("opsz", "Optical Size", 6f, 144f, 14f)
                )
            )
        )

        // 3. Built-in: System Default (Roboto / Device Sans)
        list.add(
            CustomFontItem(
                id = BUILTIN_SYSTEM_DEFAULT,
                name = "システムデフォルト (Roboto)",
                postScriptName = "Roboto",
                isBuiltIn = true,
                isVariable = false,
                fileSize = 0L,
                supportedAxes = emptyList()
            )
        )

        // 4. Custom Imported Fonts from internal storage
        val fontFiles = fontsDirectory.listFiles { file ->
            val ext = file.extension.lowercase()
            ext in listOf("ttf", "otf", "ttc", "woff2")
        } ?: emptyArray()

        for (file in fontFiles) {
            try {
                val parsed = FontParser.parse(file)
                val fontId = file.nameWithoutExtension
                val fontName = parsed?.familyName?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
                list.add(
                    CustomFontItem(
                        id = fontId,
                        name = fontName,
                        postScriptName = parsed?.postScriptName,
                        isBuiltIn = false,
                        isVariable = parsed?.isVariable ?: false,
                        filePath = file.absolutePath,
                        fileSize = file.length(),
                        supportedAxes = parsed?.axes ?: emptyList()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load custom font: ${file.name}", e)
            }
        }

        _availableFonts.value = list
    }

    /**
     * Import a font from a Storage Access Framework Uri.
     */
    suspend fun importFontFromUri(uri: Uri): Result<CustomFontItem> {
        return try {
            val fileName = getFileNameFromUri(uri) ?: "font_${System.currentTimeMillis()}.ttf"
            val fontId = "custom_${UUID.randomUUID().toString().take(8)}"
            val extension = if (fileName.contains(".")) fileName.substringAfterLast(".") else "ttf"
            val targetFile = File(fontsDirectory, "$fontId.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(Exception("Unable to open font stream from Uri"))

            val parsed = FontParser.parse(targetFile)
            val fontName = parsed?.familyName?.takeIf { it.isNotBlank() }
                ?: fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")

            val item = CustomFontItem(
                id = fontId,
                name = fontName,
                postScriptName = parsed?.postScriptName,
                isBuiltIn = false,
                isVariable = parsed?.isVariable ?: false,
                filePath = targetFile.absolutePath,
                fileSize = targetFile.length(),
                supportedAxes = parsed?.axes ?: emptyList()
            )

            loadInstalledFonts()
            invalidateCache()
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing font from uri: $uri", e)
            Result.failure(e)
        }
    }

    /**
     * Delete an imported custom font.
     */
    fun deleteFont(fontId: String): Boolean {
        val font = _availableFonts.value.find { it.id == fontId } ?: return false
        if (font.isBuiltIn) return false

        font.filePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }

        // Revert any targets using this font back to default
        FontTarget.entries.forEach { target ->
            if (_selectedFontIds.value[target] == fontId) {
                setFontForTarget(target, BUILTIN_GOOGLE_SANS_FLEX)
            }
        }

        loadInstalledFonts()
        invalidateCache()
        return true
    }

    /**
     * Set selected font for a specific target.
     */
    fun setFontForTarget(target: FontTarget, fontId: String) {
        val current = _selectedFontIds.value.toMutableMap()
        current[target] = fontId
        _selectedFontIds.value = current
        prefs.edit().putString("font_id_${target.name}", fontId).apply()
        invalidateCache()
    }

    /**
     * Update variable font variation settings for a target.
     */
    fun setVariationConfig(target: FontTarget, config: FontVariationConfig) {
        val current = _variationConfigs.value.toMutableMap()
        current[target] = config
        _variationConfigs.value = current

        val obj = JSONObject().apply {
            put("wght", config.weight.toDouble())
            put("wdth", config.width.toDouble())
            put("slnt", config.slant.toDouble())
            put("ROND", config.roundness.toDouble())
            put("opsz", config.opticalSize.toDouble())
            if (config.customAxes.isNotEmpty()) {
                val cust = JSONObject()
                config.customAxes.forEach { (k, v) -> cust.put(k, v.toDouble()) }
                put("custom", cust)
            }
        }
        prefs.edit().putString("var_config_${target.name}", obj.toString()).apply()
        invalidateCache()
    }

    /**
     * Reset variation config to default for a target.
     */
    fun resetVariationConfig(target: FontTarget) {
        setVariationConfig(target, getDefaultConfigForTarget(target))
    }

    /**
     * Obtains the Compose [FontFamily] configured for the specified [FontTarget].
     */
    @OptIn(ExperimentalTextApi::class)
    fun getFontFamily(target: FontTarget): FontFamily {
        val fontId = _selectedFontIds.value[target] ?: BUILTIN_GOOGLE_SANS_FLEX
        val config = _variationConfigs.value[target] ?: getDefaultConfigForTarget(target)
        val fontItem = _availableFonts.value.find { it.id == fontId }

        val cacheKey = "${target.name}_${fontId}_${config.weight}_${config.width}_${config.slant}_${config.roundness}_${config.opticalSize}"
        return fontFamilyCache.getOrPut(cacheKey) {
            createFontFamily(fontItem, config)
        }
    }

    /**
     * Generates a preview [FontFamily] for a specific font and variation settings.
     */
    @OptIn(ExperimentalTextApi::class)
    fun createPreviewFontFamily(font: CustomFontItem, config: FontVariationConfig): FontFamily {
        return createFontFamily(font, config)
    }

    @OptIn(ExperimentalTextApi::class)
    private fun createFontFamily(fontItem: CustomFontItem?, config: FontVariationConfig): FontFamily {
        if (fontItem == null || fontItem.id == BUILTIN_SYSTEM_DEFAULT) {
            return FontFamily.Default
        }

        try {
            val settingsList = mutableListOf<FontVariation.Setting>()
            if (fontItem.isVariable) {
                val hasAxis = { tag: String -> fontItem.supportedAxes.any { it.tag == tag } || fontItem.isBuiltIn }

                if (hasAxis("wght")) {
                    settingsList.add(FontVariation.Setting("wght", config.weight))
                }
                if (hasAxis("wdth")) {
                    settingsList.add(FontVariation.Setting("wdth", config.width))
                }
                if (hasAxis("slnt")) {
                    settingsList.add(FontVariation.Setting("slnt", config.slant))
                }
                if (hasAxis("ROND")) {
                    settingsList.add(FontVariation.Setting("ROND", config.roundness))
                }
                if (hasAxis("opsz") && config.opticalSize > 0f) {
                    settingsList.add(FontVariation.Setting("opsz", config.opticalSize))
                }
                config.customAxes.forEach { (tag, value) ->
                    settingsList.add(FontVariation.Setting(tag, value))
                }
            }

            val variationSettings = if (settingsList.isNotEmpty()) {
                FontVariation.Settings(*settingsList.toTypedArray())
            } else {
                FontVariation.Settings()
            }

            val targetWeight = FontWeight(config.weight.toInt().coerceIn(100, 1000))
            val targetStyle = if (config.slant < -0.1f) FontStyle.Italic else FontStyle.Normal

            return if (fontItem.isBuiltIn && fontItem.resId != null) {
                FontFamily(
                    Font(
                        resId = fontItem.resId,
                        weight = FontWeight.Normal,
                        style = FontStyle.Normal,
                        variationSettings = variationSettings
                    ),
                    Font(
                        resId = fontItem.resId,
                        weight = targetWeight,
                        style = targetStyle,
                        variationSettings = variationSettings
                    ),
                    Font(
                        resId = fontItem.resId,
                        weight = FontWeight.Bold,
                        style = FontStyle.Normal,
                        variationSettings = variationSettings
                    )
                )
            } else if (fontItem.filePath != null) {
                val file = File(fontItem.filePath)
                if (file.exists()) {
                    FontFamily(
                        Font(
                            file = file,
                            weight = FontWeight.Normal,
                            style = FontStyle.Normal,
                            variationSettings = variationSettings
                        ),
                        Font(
                            file = file,
                            weight = targetWeight,
                            style = targetStyle,
                            variationSettings = variationSettings
                        ),
                        Font(
                            file = file,
                            weight = FontWeight.Bold,
                            style = FontStyle.Normal,
                            variationSettings = variationSettings
                        )
                    )
                } else {
                    FontFamily.Default
                }
            } else {
                FontFamily.Default
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error constructing FontFamily for font: ${fontItem.name}", e)
            return FontFamily.Default
        }
    }

    private fun invalidateCache() {
        fontFamilyCache.clear()
        _fontChangeTicker.value = System.currentTimeMillis()
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) path.substring(cut + 1) else path
            }
        }
        return name
    }

    suspend fun downloadCloudFont(item: CloudFontItem): Result<CustomFontItem> = withContext(Dispatchers.IO) {
        if (_downloadingFontIds.value.contains(item.id)) {
            return@withContext Result.failure(IllegalStateException("Font is already downloading"))
        }

        _downloadingFontIds.update { it + item.id }

        try {
            val destFile = File(fontsDirectory, item.fileName)
            val tempFile = File(fontsDirectory, "${item.fileName}.tmp")

            val url = java.net.URL(item.downloadUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 OverDrive-Android")

            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.exists()) {
                destFile.delete()
            }
            if (!tempFile.renameTo(destFile)) {
                tempFile.copyTo(destFile, overwrite = true)
                tempFile.delete()
            }

            val parsed = FontParser.parse(destFile)
            val fontItem = CustomFontItem(
                id = "cloud_${destFile.nameWithoutExtension}",
                name = parsed?.familyName?.takeIf { it.isNotBlank() } ?: item.name,
                postScriptName = parsed?.postScriptName,
                isBuiltIn = false,
                isVariable = parsed?.isVariable ?: item.isVariable,
                filePath = destFile.absolutePath,
                fileSize = destFile.length(),
                supportedAxes = parsed?.axes ?: emptyList()
            )

            loadInstalledFonts()
            invalidateCache()

            Result.success(fontItem)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download cloud font: ${item.name}", e)
            Result.failure(e)
        } finally {
            _downloadingFontIds.update { it - item.id }
        }
    }

    fun isCloudFontDownloaded(cloudFontId: String): Boolean {
        val cloudFont = cloudJapaneseFonts.find { it.id == cloudFontId } ?: return false
        val destFile = File(fontsDirectory, cloudFont.fileName)
        return destFile.exists() && destFile.length() > 1024
    }

    fun getDownloadedFontItem(cloudFontId: String): CustomFontItem? {
        val cloudFont = cloudJapaneseFonts.find { it.id == cloudFontId } ?: return null
        val targetId = "cloud_${File(cloudFont.fileName).nameWithoutExtension}"
        return _availableFonts.value.find { it.id == targetId || it.filePath?.endsWith(cloudFont.fileName) == true }
    }
}
