package io.github.mrasterisco.rxfireauth.handlers.apple

import android.webkit.JavascriptInterface
import com.auth0.android.jwt.JWT
import org.json.JSONObject
import java.net.URLDecoder

@Suppress("unused")
internal class JavaScriptInterface {

    internal var code: String = ""
        private set
    internal var idToken: String = ""
        private set
    internal var state: String = ""
        private set
    internal var email: String = ""
        private set
    internal var name: String = ""
        private set

    internal var completionHandler: (() -> Unit)? = null

    @JavascriptInterface
    fun read(body: String) {
        val map = convertToMap(body)

        code = map["code"] ?: ""
        idToken = map["id_token"] ?: ""
        state = map["state"] ?: ""

        try {
            val jwt = JWT(idToken)
            email = jwt.getClaim("email").asString() ?: ""

            map["user"]?.let {
                name = extractName(it)
            }
        } catch (exc: Throwable) { /* ignored */ }

        completionHandler?.invoke()
    }

    private fun convertToMap(body: String): Map<String, String> {
        val parameters = body.split("&")
        val map = mutableMapOf<String, String>()

        parameters.forEach {
            val split = it.split("=")
            if (split.count() == 2) {
                map[split[0]] = URLDecoder.decode(split[1], "UTF-8")
            }
        }

        return map
    }

    private fun extractName(userJSONString: String): String {
        val jsonObj = JSONObject(userJSONString.substring(userJSONString.indexOf("{"), userJSONString.lastIndexOf("}") + 1))
        val jsonName = jsonObj.getJSONObject("name")

        return (jsonName.getString("firstName") + " " + jsonName.getString("lastName")).trim()
    }

}