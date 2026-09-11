# SXI Bloqueio de Chamadas

App Android que **bloqueia automaticamente qualquer chamada recebida de um número que não esteja na sua agenda**. Números conhecidos (salvos nos contatos) tocam normalmente; desconhecidos e números ocultos são rejeitados e silenciados.

## Como funciona

Usa a API oficial `CallScreeningService` (Android 10+). A cada chamada recebida, o sistema chama `onScreenCall()`, o app consulta a agenda via `ContactsContract.PhoneLookup` e:

- **Na agenda** → deixa tocar normalmente.
- **Fora da agenda / oculto** → `disallowCall + rejectCall`, sem notificação (fica só no histórico).

## Requisitos no celular

1. Android 10 (API 29) ou superior.
2. Conceder permissão de **Contatos**.
3. Definir o app como **Triador de chamadas** (Call Screening) quando solicitado.

## Build (CI)

O `.github/workflows/build.yml` compila o APK no GitHub Actions (Gradle 8.7 + AGP 8.5.2) e faz deploy automático na **SXI Store**.

APK final: https://www.sxi.com.br/apk/SXI_BloqueioChamadas.apk
Página: https://store.sxi.com.br/

### Secrets necessários

| Secret | Descrição |
|--------|-----------|
| `FTP_HOST` | host FTP (sxi.com.br) |
| `FTP_USER` | usuário FTP |
| `FTP_PASS` | senha FTP |

## Estrutura

```
app/src/main/java/br/com/sxi/bloqueiochamadas/
  ├── MainActivity.kt         # UI: pede permissões e ativa a triagem
  ├── CallBlockerService.kt   # CallScreeningService: bloqueia fora da agenda
  └── ContactChecker.kt       # consulta se o número está nos contatos
```

---
SXI Tecnologia • store.sxi.com.br
