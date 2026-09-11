package br.com.sxi.bloqueiochamadas

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

/**
 * Servico de triagem de chamadas.
 *
 * O sistema Android chama onScreenCall() para CADA chamada recebida (desde que o app
 * esteja definido como "app de triagem de chamadas" pelo usuario - ROLE_CALL_SCREENING).
 *
 * Regra: se o numero NAO estiver na agenda do celular, a chamada e rejeitada e silenciada.
 */
class CallBlockerService : CallScreeningService() {

    private val TAG = "SXIBloqueio"

    override fun onScreenCall(callDetails: Call.Details) {
        // So processamos chamadas RECEBIDAS. As demais (saindo) sao sempre permitidas.
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val number = callDetails.handle?.schemeSpecificPart
        val naAgenda = ContactChecker.isInContacts(this, number)

        val response = CallResponse.Builder()

        if (naAgenda) {
            // Contato conhecido -> deixa tocar normalmente
            Log.i(TAG, "PERMITIDA: $number (esta na agenda)")
        } else {
            // Desconhecido / oculto -> bloqueia, rejeita e nao notifica
            Log.i(TAG, "BLOQUEADA: $number (fora da agenda)")
            response.setDisallowCall(true)   // nao deixa a chamada chegar ao usuario
            response.setRejectCall(true)      // encerra a chamada (como recusar)
            response.setSkipCallLog(false)    // mantem no historico de chamadas
            response.setSkipNotification(true) // nao mostra notificacao de chamada perdida
        }

        respondToCall(callDetails, response.build())
    }
}
