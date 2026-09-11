package br.com.sxi.bloqueiochamadas

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.com.sxi.bloqueiochamadas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val pedirContatos =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            atualizarStatus()
        }

    private val pedirRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            atualizarStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContatos.setOnClickListener {
            pedirContatos.launch(Manifest.permission.READ_CONTACTS)
        }

        binding.btnAtivar.setOnClickListener { solicitarPapelTriagem() }

        binding.btnConfig.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
            )
        }

        // ---- Webhook ----
        binding.etWebhook.setText(Prefs.getWebhookUrl(this) ?: "")

        binding.btnSalvarWebhook.setOnClickListener {
            val url = binding.etWebhook.text.toString().trim()
            when {
                url.isEmpty() -> {
                    Prefs.setWebhookUrl(this, "")
                    toast(getString(R.string.webhook_limpo))
                }
                Patterns.WEB_URL.matcher(url).matches() &&
                    (url.startsWith("http://") || url.startsWith("https://")) -> {
                    Prefs.setWebhookUrl(this, url)
                    toast(getString(R.string.webhook_salvo))
                }
                else -> toast(getString(R.string.webhook_invalido))
            }
        }

        binding.btnTestarWebhook.setOnClickListener {
            val url = binding.etWebhook.text.toString().trim()
            if (url.isEmpty()) {
                toast(getString(R.string.webhook_sem_url))
            } else {
                WebhookNotifier.notifyBlocked(url, "+5511999999999", "TESTE SXI")
                toast(getString(R.string.webhook_teste_enviado))
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onResume() {
        super.onResume()
        atualizarStatus()
    }

    private fun temContatos(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    private fun ehTriadorPadrao(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val rm = getSystemService(RoleManager::class.java) ?: return false
        return rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun solicitarPapelTriagem() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val rm = getSystemService(RoleManager::class.java) ?: return
        if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            val intent = rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            pedirRole.launch(intent)
        }
    }

    private fun atualizarStatus() {
        val contatosOk = temContatos()
        val triadorOk = ehTriadorPadrao()

        binding.txtStatusContatos.text =
            if (contatosOk) getString(R.string.contatos_ok) else getString(R.string.contatos_falta)
        binding.txtStatusTriador.text =
            if (triadorOk) getString(R.string.triador_ok) else getString(R.string.triador_falta)

        binding.btnContatos.isEnabled = !contatosOk
        binding.btnAtivar.isEnabled = !triadorOk

        val protegido = contatosOk && triadorOk
        binding.txtResumo.text =
            if (protegido) getString(R.string.protegido) else getString(R.string.desprotegido)
    }
}
