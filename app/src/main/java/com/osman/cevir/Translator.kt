package com.osman.cevir

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object Translator {
    fun translate(key: String, text: String, source: String, target: String): String {
        val url = URL("https://translation.googleapis.com/language/translate/v2?key=" + URLEncoder.encode(key, "UTF-8"))
        val c = url.openConnection() as HttpURLConnection
        c.requestMethod = "POST"
        c.doOutput = true
        c.connectTimeout = 10000
        c.readTimeout = 15000
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        val body = "q=" + URLEncoder.encode(text, "UTF-8") + "&source=$source&target=$target&format=text"
        c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = c.responseCode
        val stream = if (code in 200..299) c.inputStream else c.errorStream
        val s = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) {
            val msg = try { JSONObject(s).getJSONObject("error").getString("message") } catch (e: Exception) { "HTTP $code" }
            throw Exception(msg)
        }
        return JSONObject(s).getJSONObject("data").getJSONArray("translations")
            .getJSONObject(0).getString("translatedText")
    }
}
