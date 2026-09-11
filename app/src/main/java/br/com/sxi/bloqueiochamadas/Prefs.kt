package br.com.sxi.bloqueiochamadas

import android.content.Context

/** Guarda as configuracoes do app (URL do webhook). */
object Prefs {
    private const val FILE = "sxi_bloqueio_prefs"
    private const val KEY_WEBHOOK = "webhook_url"

    fun getWebhookUrl(ctx: Context): String? =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY_WEBHOOK, null)
            ?.takeIf { it.isNotBlank() }

    fun setWebhookUrl(ctx: Context, url: String) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WEBHOOK, url.trim())
            .apply()
    }
}
