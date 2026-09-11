package br.com.sxi.bloqueiochamadas

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dispara um POST (JSON) para a URL de webhook cadastrada pelo usuario,
 * toda vez que uma chamada e bloqueada.
 *
 * Roda em uma thread separada porque o CallScreeningService executa na main thread
 * e nao pode fazer rede de forma sincrona.
 */
object WebhookNotifier {

    private const val TAG = "SXIBloqueio"

    fun notifyBlocked(url: String?, numero: String?, nomeContato: String?) {
        if (url.isNullOrBlank()) return  // sem webhook cadastrado -> nao faz nada

        Thread {
            var conn: HttpURLConnection? = null
            try {
                val payload = JSONObject().apply {
                    put("evento", "chamada_bloqueada")
                    put("numero", numero ?: "oculto")
                    put("acao", "bloqueada")
                    put("nome_contato", nomeContato ?: JSONObject.NULL)
                    put("data", isoAgora())
                    put("app", "SXI Bloqueio de Chamadas")
                }.toString()

                conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "SXI-Bloqueio-App")
                }

                conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                // consome a resposta para liberar a conexao
                (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.use(BufferedReader::readText)

                Log.i(TAG, "Webhook POST -> HTTP $code (numero=$numero)")
            } catch (e: Exception) {
                Log.w(TAG, "Falha ao disparar webhook", e)
            } finally {
                conn?.disconnect()
            }
        }.start()
    }

    private fun isoAgora(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date())
}
