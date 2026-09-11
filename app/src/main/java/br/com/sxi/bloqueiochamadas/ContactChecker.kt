package br.com.sxi.bloqueiochamadas

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log

/**
 * Responsavel por verificar se um numero de telefone pertence a um contato salvo na agenda.
 * Usa o PhoneLookup do ContactsContract, que ja normaliza formatos (com/sem DDD, +55, etc.).
 */
object ContactChecker {

    private const val TAG = "SXIBloqueio"

    fun isInContacts(context: Context, rawNumber: String?): Boolean {
        if (rawNumber.isNullOrBlank()) {
            // Numero privado / oculto -> tratamos como NAO esta na agenda (sera bloqueado)
            Log.d(TAG, "Numero vazio/privado -> nao esta na agenda")
            return false
        }

        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(rawNumber)
        )

        return try {
            context.contentResolver.query(
                lookupUri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null
            )?.use { cursor ->
                val found = cursor.count > 0
                Log.d(TAG, "Numero $rawNumber -> na agenda: $found")
                found
            } ?: false
        } catch (e: SecurityException) {
            // Sem permissao READ_CONTACTS: por seguranca NAO bloqueia (evita bloquear tudo)
            Log.w(TAG, "Sem permissao READ_CONTACTS, permitindo chamada", e)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao consultar agenda, permitindo chamada", e)
            true
        }
    }
}
