package com.sortedqueue.portfolio.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YoutubeStreamResolverTest {

    @Test
    fun resolveStreamUrl_whenDirectYoutubeSucceeds_returnsDirectUrl() = runBlocking {
        val mockHtml = """
            <html>
            <body>
            <script>
            var ytInitialPlayerResponse = {
                "streamingData": {
                    "formats": [
                        {
                            "itag": 18,
                            "url": "https://googlevideo.com/direct_stream_url_18",
                            "mimeType": "video/mp4; codecs=\"avc1.42001E, mp4a.40.2\""
                        }
                    ]
                }
            };
            </script>
            </body>
            </html>
        """.trimIndent()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestUrl = chain.request().url.toString()
                if (requestUrl.contains("youtube.com/watch")) {
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(mockHtml.toResponseBody("text/html".toMediaType()))
                        .build()
                } else {
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(404)
                        .message("Not Found")
                        .body("".toResponseBody("text/plain".toMediaType()))
                        .build()
                }
            }
            .build()

        val resolver = YoutubeStreamResolver(okHttpClient)
        val result = resolver.resolveStreamUrl("test_id")

        assertEquals("https://googlevideo.com/direct_stream_url_18", result)
    }

    @Test
    fun resolveStreamUrl_whenDirectYoutubeFails_fallsBackToPipedSucceeds() = runBlocking {
        val mockPipedResponse = """
            {
                "videoStreams": [
                    {
                        "url": "https://piped-stream-url.com/stream",
                        "mimeType": "video/mp4"
                    }
                ]
            }
        """.trimIndent()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestUrl = chain.request().url.toString()
                when {
                    requestUrl.contains("youtube.com/watch") -> {
                        // YouTube watch page fails
                        Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(404)
                            .message("Not Found")
                            .body("Error".toResponseBody("text/html".toMediaType()))
                            .build()
                    }
                    requestUrl.contains("pipedapi.kavin.rocks") -> {
                        // First instance fails with 502 Bad Gateway
                        Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(502)
                            .message("Bad Gateway")
                            .body("".toResponseBody("text/plain".toMediaType()))
                            .build()
                    }
                    requestUrl.contains("api.piped.yt") -> {
                        // Second instance succeeds
                        Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(mockPipedResponse.toResponseBody("application/json".toMediaType()))
                            .build()
                    }
                    else -> {
                        Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(404)
                            .message("Not Found")
                            .body("".toResponseBody("text/plain".toMediaType()))
                            .build()
                    }
                }
            }
            .build()

        val resolver = YoutubeStreamResolver(okHttpClient)
        val result = resolver.resolveStreamUrl("test_id")

        assertEquals("https://piped-stream-url.com/stream", result)
    }

    @Test
    fun resolveStreamUrl_whenAllFail_returnsNull() = runBlocking {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(500)
                    .message("Internal Server Error")
                    .body("".toResponseBody("text/plain".toMediaType()))
                    .build()
            }
            .build()

        val resolver = YoutubeStreamResolver(okHttpClient)
        val result = resolver.resolveStreamUrl("test_id")

        assertNull(result)
    }
}
