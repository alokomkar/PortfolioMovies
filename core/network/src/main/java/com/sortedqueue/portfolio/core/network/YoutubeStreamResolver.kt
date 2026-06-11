package com.sortedqueue.portfolio.core.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeStreamResolver(
    private val client: OkHttpClient
) {
    @Inject
    constructor() : this(OkHttpClient())

    private val gson = Gson()

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    suspend fun resolveStreamUrl(youtubeId: String): String? = withContext(Dispatchers.IO) {
        // 1. Primary Strategy: Try direct YouTube watch page extraction
        val directUrl = resolveDirectStreamUrl(youtubeId)
        if (directUrl != null) {
            return@withContext directUrl
        }

        // 2. Secondary Strategy: Fallback to public Piped API instances
        val instances = listOf(
            "pipedapi.kavin.rocks",
            "api.piped.yt",
            "pipedapi.colt.one"
        )
        
        for (instance in instances) {
            val url = "https://$instance/streams/$youtubeId"
            val request = Request.Builder().url(url).build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@use null
                        val pipedResponse = gson.fromJson(body, PipedStreamResponse::class.java)
                        
                        // Find a mp4 video stream with valid url
                        val stream = pipedResponse.videoStreams
                            ?.firstOrNull { it.mimeType?.contains("video/mp4") == true || it.format == "MPEG_4" }
                            ?: pipedResponse.videoStreams?.firstOrNull()
                        
                        if (stream?.url != null) {
                            return@withContext stream.url
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore and try next instance
            }
        }
        null
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "ReturnCount", "MagicNumber")
    private fun resolveDirectStreamUrl(youtubeId: String): String? {
        val watchUrl = "https://www.youtube.com/watch?v=$youtubeId"
        val request = Request.Builder()
            .url(watchUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val html = response.body?.string() ?: return null
                val jsonString = extractJson(html, "ytInitialPlayerResponse") ?: return null
                val playerResponse = gson.fromJson(jsonString, YoutubePlayerResponse::class.java)
                
                // Prioritize format 18 (combined 360p mp4), then any mp4 format, then any direct url format
                return playerResponse.streamingData?.formats
                    ?.firstOrNull { it.itag == 18 && !it.url.isNullOrEmpty() }?.url
                    ?: playerResponse.streamingData?.formats
                        ?.firstOrNull { !it.url.isNullOrEmpty() && it.mimeType?.contains("video/mp4") == true }?.url
                    ?: playerResponse.streamingData?.formats
                        ?.firstOrNull { !it.url.isNullOrEmpty() }?.url
                    ?: playerResponse.streamingData?.adaptiveFormats
                        ?.firstOrNull { !it.url.isNullOrEmpty() && it.mimeType?.contains("video/mp4") == true }?.url
                    ?: playerResponse.streamingData?.adaptiveFormats
                        ?.firstOrNull { !it.url.isNullOrEmpty() }?.url
            }
        } catch (e: Exception) {
            // Log or ignore, fallback to Piped
            return null
        }
    }

    @Suppress("ReturnCount", "CyclomaticComplexMethod", "NestedBlockDepth", "LoopWithTooManyJumpStatements")
    private fun extractJson(html: String, key: String): String? {
        val index = html.indexOf(key)
        if (index == -1) return null
        var startIndex = index + key.length
        // Skip optional whitespace/equals sign/var
        while (startIndex < html.length && (html[startIndex].isWhitespace() || html[startIndex] == '=')) {
            startIndex++
        }
        if (startIndex >= html.length || html[startIndex] != '{') return null
        
        var braceCount = 0
        var inString = false
        var escape = false
        for (i in startIndex until html.length) {
            val char = html[i]
            if (escape) {
                escape = false
                continue
            }
            if (char == '\\') {
                escape = true
                continue
            }
            if (char == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (char == '{') {
                    braceCount++
                } else if (char == '}') {
                    braceCount--
                    if (braceCount == 0) {
                        return html.substring(startIndex, i + 1)
                    }
                }
            }
        }
        return null
    }
}

data class YoutubePlayerResponse(
    val streamingData: YoutubeStreamingData?
)

data class YoutubeStreamingData(
    val formats: List<YoutubeFormat>?,
    val adaptiveFormats: List<YoutubeFormat>?
)

data class YoutubeFormat(
    val itag: Int?,
    val url: String?,
    val mimeType: String?,
    val quality: String?,
    val qualityLabel: String?
)

data class PipedStreamResponse(
    val videoStreams: List<PipedVideoStream>?
)

data class PipedVideoStream(
    val url: String?,
    val format: String?,
    val quality: String?,
    val mimeType: String?
)
