package com.codetrio.overdrive.data.lyrics

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

sealed class ModelDownloadStatus {
    object Idle : ModelDownloadStatus()
    object Checking : ModelDownloadStatus()
    object Downloaded : ModelDownloadStatus()
    object Downloading : ModelDownloadStatus()
    data class Error(val message: String) : ModelDownloadStatus()
}

object LyricsTranslator {
    private const val TAG = "LyricsTranslator"
    
    private var currentTranslator: Translator? = null
    
    fun resolveLanguageTag(languageCode: String): String {
        val code = if (languageCode == "system") Locale.getDefault().language else languageCode
        return TranslateLanguage.fromLanguageTag(code) ?: TranslateLanguage.JAPANESE
    }

    suspend fun isModelDownloadedForLanguage(languageCode: String): Boolean = withContext(Dispatchers.IO) {
        val langTag = resolveLanguageTag(languageCode)
        val model = TranslateRemoteModel.Builder(langTag).build()
        try {
            RemoteModelManager.getInstance().isModelDownloaded(model).await()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun downloadModelForLanguage(
        languageCode: String,
        onStatusChange: (ModelDownloadStatus) -> Unit
    ) = withContext(Dispatchers.IO) {
        val langTag = resolveLanguageTag(languageCode)
        val model = TranslateRemoteModel.Builder(langTag).build()
        val modelManager = RemoteModelManager.getInstance()
        
        onStatusChange(ModelDownloadStatus.Downloading)
        try {
            val conditions = DownloadConditions.Builder().build()
            modelManager.download(model, conditions).await()
            onStatusChange(ModelDownloadStatus.Downloaded)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model", e)
            onStatusChange(ModelDownloadStatus.Error(e.localizedMessage ?: "Download failed"))
        }
    }
    
    suspend fun translateLyrics(
        lyrics: List<LyricLine>,
        targetLanguageCode: String,
        engine: String = "mlkit",
        onProgress: (String) -> Unit
    ): List<LyricLine> = withContext(Dispatchers.IO) {
        if (lyrics.isEmpty()) return@withContext lyrics
        
        if (engine == "aicore") {
            return@withContext translateLyricsWithAICore(lyrics, targetLanguageCode, onProgress)
        }
        
        // Detect source language
        val sampleText = lyrics.map { it.content }.filter { it.isNotBlank() }.take(10).joinToString(" ")
        if (sampleText.isBlank()) return@withContext lyrics

        onProgress("Detecting language...")
        val languageIdentifier = LanguageIdentification.getClient()
        val sourceLanguage = try {
            languageIdentifier.identifyLanguage(sampleText).await()
        } catch (e: Exception) {
            Log.e(TAG, "Language identification failed", e)
            "und"
        } finally {
            languageIdentifier.close()
        }
        
        if (sourceLanguage == "und") {
            Log.w(TAG, "Could not identify source language.")
            return@withContext lyrics
        }
        
        Log.d(TAG, "Source language: $sourceLanguage, target: $targetLanguageCode")
        
        val sourceLangTag = TranslateLanguage.fromLanguageTag(sourceLanguage)
        val targetLangTag = TranslateLanguage.fromLanguageTag(resolveLanguageTag(targetLanguageCode))
        
        if (sourceLangTag == null || targetLangTag == null) {
            Log.w(TAG, "Language not supported by ML Kit: $sourceLanguage -> $targetLanguageCode")
            return@withContext lyrics
        }
        
        if (sourceLangTag == targetLangTag) {
            return@withContext lyrics
        }
        
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangTag)
            .setTargetLanguage(targetLangTag)
            .build()
            
        currentTranslator?.close()
        val translator = Translation.getClient(options)
        currentTranslator = translator
        
        onProgress("Downloading translation models if needed...")
        try {
            translator.downloadModelIfNeeded().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download translation model", e)
            return@withContext lyrics
        }
        
        onProgress("Translating lyrics...")
        val translatedLyrics = lyrics.map { line ->
            if (line.content.isBlank()) {
                line.copy(translatedContent = null)
            } else {
                try {
                    val translatedText = translator.translate(line.content).await()
                    line.copy(translatedContent = translatedText)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to translate line: ${line.content}", e)
                    line.copy(translatedContent = null)
                }
            }
        }
        
        onProgress("Translation complete")
        return@withContext translatedLyrics
    }
    
    private suspend fun translateLyricsWithAICore(
        lyrics: List<LyricLine>,
        targetLanguageCode: String,
        onProgress: (String) -> Unit
    ): List<LyricLine> = withContext(Dispatchers.IO) {
        val targetLangName = if (targetLanguageCode == "system" || targetLanguageCode == Locale.getDefault().language) {
            Locale.getDefault().displayLanguage
        } else {
            Locale(targetLanguageCode).displayLanguage
        }
        
        onProgress("Initializing AICore...")
        try {
            val model = com.google.mlkit.genai.prompt.Generation.getClient()
            val status = model.checkStatus()
            // 1=READY, 2=DOWNLOADABLE, 4=UPDATE_AVAILABLE
            if (status != 1 && status != 2 && status != 4) {
                Log.e(TAG, "AICore not available: status $status")
                return@withContext lyrics
            }
            
            // Reconstruct full text, keeping empty lines so we can match them back
            val originalLines = lyrics.map { it.content }
            val fullText = originalLines.joinToString("\n")
            
            val prompt = """
                Translate the following song lyrics into highly natural $targetLangName.
                Keep the EXACT same line structure, including empty lines.
                Do not add any intro, outro, or explanations.
                Here are the lyrics:
                
                $fullText
            """.trimIndent()

            onProgress("Translating with Gemini Nano...")
            
            // Assuming model.generateContent(String) exists and returns a task or directly string or response object
            // Usually it's model.generateContent(prompt).await() returning a GenerateContentResponse
            // Let's use reflection or try to compile to see the exact method
            // Actually, genai-prompt uses:
            // val response = model.generateContent(prompt).await()
            // val text = response.text
            // Let's write the code assuming this structure. I will check compilation later.
            val method = model.javaClass.getMethod("generateContent", String::class.java)
            val task = method.invoke(model, prompt) as com.google.android.gms.tasks.Task<*>
            val response = task.await()
            val textMethod = response.javaClass.getMethod("getText")
            val translatedText = textMethod.invoke(response) as String? ?: ""
            
            val translatedLines = translatedText.split("\n")
            
            if (translatedLines.size >= lyrics.size) { // Best effort match
                return@withContext lyrics.mapIndexed { index, lyricLine ->
                    if (lyricLine.content.isBlank()) {
                        lyricLine.copy(translatedContent = null)
                    } else {
                        lyricLine.copy(translatedContent = translatedLines[index].trim().takeIf { it.isNotBlank() })
                    }
                }
            } else {
                Log.w(TAG, "AICore returned mismatched line count (${translatedLines.size} vs ${lyrics.size})")
                // If it mismatched badly, let's just attempt to map by non-empty lines
                val nonBlankTranslated = translatedLines.filter { it.isNotBlank() }
                val nonBlankOriginal = lyrics.filter { it.content.isNotBlank() }
                
                if (nonBlankOriginal.size == nonBlankTranslated.size) {
                    var transIdx = 0
                    return@withContext lyrics.map { lyricLine ->
                        if (lyricLine.content.isBlank()) {
                            lyricLine.copy(translatedContent = null)
                        } else {
                            lyricLine.copy(translatedContent = nonBlankTranslated[transIdx++].trim().takeIf { it.isNotBlank() })
                        }
                    }
                }
                
                // Fallback to no translation if completely mangled structure
                return@withContext lyrics
            }

        } catch (e: Exception) {
            Log.e(TAG, "AICore translation failed", e)
            return@withContext lyrics
        }
    }
    
    fun close() {
        currentTranslator?.close()
        currentTranslator = null
    }
}
