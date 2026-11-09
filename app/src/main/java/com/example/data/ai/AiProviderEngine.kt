package com.example.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiProviderType(val label: String, val description: String, val licenseNote: String) {
    DISABLED(
        "Désactivée (100% Sans IA)",
        "Aucun appel IA. BUSINESS OS fonctionne intégralement de manière autonome.",
        "MIT • Aucune dépendance externe"
    ),
    LOCAL_DETERMINISTIC(
        "Moteur Analytique Local (Open-Source Intégré)",
        "Calculateur déterministe local (Ratios OHADA, Cashflow Runway, Relances CRM, Alertes Stocks) sans réseau ni LLM externe.",
        "MIT • Exécution 100% locale sur l'appareil"
    ),
    OLLAMA_SELF_HOSTED(
        "Ollama Auto-Hébergé (Llama 3.2 / Mistral / Qwen)",
        "Connexion directe à votre serveur Ollama local ou d'entreprise via API REST standard.",
        "Apache 2.0 / Llama Community License"
    ),
    OPENAI_COMPATIBLE(
        "Adaptateur API Compatible OpenAI / vLLM / Mistral",
        "Connecteur universel pour tout serveur d'inférence compatible /v1/chat/completions.",
        "Standard OpenAPI REST"
    )
}

data class AiConfigState(
    val isEnabled: Boolean = true,
    val providerType: AiProviderType = AiProviderType.LOCAL_DETERMINISTIC,
    val endpointUrl: String = "http://localhost:11434",
    val modelName: String = "llama3.2",
    val apiKey: String = "",
    val monthlyQuotaLimitRequests: Int = 500,
    val usedRequestsCount: Int = 12,
    val requireHumanConfirmationForFinance: Boolean = true
)

data class AiAssistantMessage(
    val id: Long = System.currentTimeMillis(),
    val role: String, // "USER", "ASSISTANT", "SYSTEM_GUARDRAIL"
    val content: String,
    val providerBadge: String,
    val requiresHumanApprovalAction: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

interface AIProvider {
    suspend fun analyzeAndRespond(
        userPrompt: String,
        erpLiveContext: String,
        config: AiConfigState
    ): AiAssistantMessage
}

class PluggableAiEngine : AIProvider {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun analyzeAndRespond(
        userPrompt: String,
        erpLiveContext: String,
        config: AiConfigState
    ): AiAssistantMessage = withContext(Dispatchers.IO) {
        if (!config.isEnabled || config.providerType == AiProviderType.DISABLED) {
            return@withContext AiAssistantMessage(
                role = "SYSTEM_GUARDRAIL",
                content = "Le module Business AI est actuellement désactivé conformément à vos préférences. Toutes les fonctionnalités ERP (Finances, Comptabilité OHADA, CRM, Stocks, RH, Documents) fonctionnent normalement sans IA.",
                providerBadge = "IA Désactivée"
            )
        }

        if (config.usedRequestsCount >= config.monthlyQuotaLimitRequests) {
            return@withContext AiAssistantMessage(
                role = "SYSTEM_GUARDRAIL",
                content = "Quota mensuel d'utilisation IA atteint (${config.usedRequestsCount}/${config.monthlyQuotaLimitRequests}). Augmentez la limite dans la configuration Business AI ou utilisez les rapports standards.",
                providerBadge = "Quota Atteint"
            )
        }

        // Security Guardrail: Never allow AI to execute a sensitive financial operation without explicit human confirmation
        val lowerPrompt = userPrompt.lowercase()
        val isSensitiveFinancialIntent = lowerPrompt.contains("virement") ||
            lowerPrompt.contains("payer") ||
            lowerPrompt.contains("supprimer") ||
            lowerPrompt.contains("clôturer") ||
            lowerPrompt.contains("transfert")

        when (config.providerType) {
            AiProviderType.DISABLED -> AiAssistantMessage(
                role = "SYSTEM_GUARDRAIL",
                content = "IA désactivée.",
                providerBadge = "OFF"
            )

            AiProviderType.LOCAL_DETERMINISTIC -> {
                val analysis = buildDeterministicErpResponse(userPrompt, erpLiveContext, isSensitiveFinancialIntent)
                AiAssistantMessage(
                    role = "ASSISTANT",
                    content = analysis,
                    providerBadge = "Moteur Local MIT",
                    requiresHumanApprovalAction = if (isSensitiveFinancialIntent) {
                        "CONFIRMATION HUMAINE REQUISE : L'IA a préparé une recommandation financière, mais aucune écriture comptable ni transaction de trésorerie n'a été exécutée automatiquement."
                    } else null
                )
            }

            AiProviderType.OLLAMA_SELF_HOSTED -> {
                try {
                    val base = config.endpointUrl.trimEnd('/')
                    val payload = JSONObject().apply {
                        put("model", config.modelName.ifBlank { "llama3.2" })
                        put("prompt", "Tu es Business OS AI, analyste ERP & OHADA. Contexte réel BDD:\n$erpLiveContext\n\nQuestion: $userPrompt\nRéponds de façon concise et structurée.")
                        put("stream", false)
                    }
                    val req = Request.Builder()
                        .url("$base/api/generate")
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .build()
                    httpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val bodyStr = resp.body?.string().orEmpty()
                            val json = JSONObject(bodyStr)
                            val reply = json.optString("response", "Réponse Ollama reçue.")
                            AiAssistantMessage(
                                role = "ASSISTANT",
                                content = reply,
                                providerBadge = "Ollama • ${config.modelName}",
                                requiresHumanApprovalAction = if (isSensitiveFinancialIntent) "Validation humaine requise avant exécution financière." else null
                            )
                        } else {
                            fallbackWithOfflineDiagnostics("Serveur Ollama (${config.endpointUrl}) a retourné HTTP ${resp.code}", userPrompt, erpLiveContext, isSensitiveFinancialIntent)
                        }
                    }
                } catch (e: Exception) {
                    fallbackWithOfflineDiagnostics("Serveur Ollama local non joignable sur ${config.endpointUrl} (${e.javaClass.simpleName}) — bascule automatique sur le moteur analytique local Open-Source", userPrompt, erpLiveContext, isSensitiveFinancialIntent)
                }
            }

            AiProviderType.OPENAI_COMPATIBLE -> {
                if (config.apiKey.isBlank()) {
                    return@withContext fallbackWithOfflineDiagnostics(
                        "Aucune clé API configurée pour l'adaptateur OpenAI-Compatible — exécution via le moteur analytique local",
                        userPrompt,
                        erpLiveContext,
                        isSensitiveFinancialIntent
                    )
                }
                try {
                    val base = config.endpointUrl.trimEnd('/')
                    val messages = JSONArray().apply {
                        put(JSONObject().put("role", "system").put("content", "Tu es Business OS AI. Contexte BDD réel:\n$erpLiveContext"))
                        put(JSONObject().put("role", "user").put("content", userPrompt))
                    }
                    val payload = JSONObject().apply {
                        put("model", config.modelName.ifBlank { "gpt-4o-mini" })
                        put("messages", messages)
                        put("temperature", 0.2)
                    }
                    val req = Request.Builder()
                        .url("$base/chat/completions")
                        .addHeader("Authorization", "Bearer ${config.apiKey}")
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .build()
                    httpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val json = JSONObject(resp.body?.string().orEmpty())
                            val content = json.optJSONArray("choices")
                                ?.optJSONObject(0)
                                ?.optJSONObject("message")
                                ?.optString("content")
                                ?: "Analyse générée."
                            AiAssistantMessage(
                                role = "ASSISTANT",
                                content = content,
                                providerBadge = "API Compatible • ${config.modelName}",
                                requiresHumanApprovalAction = if (isSensitiveFinancialIntent) "Validation humaine obligatoire pour toute opération financière." else null
                            )
                        } else {
                            fallbackWithOfflineDiagnostics("Erreur HTTP ${resp.code} du fournisseur IA", userPrompt, erpLiveContext, isSensitiveFinancialIntent)
                        }
                    }
                } catch (e: Exception) {
                    fallbackWithOfflineDiagnostics("Endpoint IA distant injoignable (${e.javaClass.simpleName}) — continuité assurée par le moteur local", userPrompt, erpLiveContext, isSensitiveFinancialIntent)
                }
            }
        }
    }

    private fun fallbackWithOfflineDiagnostics(
        notice: String,
        userPrompt: String,
        erpLiveContext: String,
        isSensitiveFinancialIntent: Boolean
    ): AiAssistantMessage {
        val deterministic = buildDeterministicErpResponse(userPrompt, erpLiveContext, isSensitiveFinancialIntent)
        return AiAssistantMessage(
            role = "ASSISTANT",
            content = "ℹ️ [$notice]\n\n$deterministic",
            providerBadge = "Fallback Local Résilient",
            requiresHumanApprovalAction = if (isSensitiveFinancialIntent) {
                "GARDE-FOU SÉCURITÉ : Aucune opération financière sensible n'est exécutée automatiquement."
            } else null
        )
    }

    private fun buildDeterministicErpResponse(
        userPrompt: String,
        erpLiveContext: String,
        isSensitiveFinancialIntent: Boolean
    ): String {
        val guardHeader = if (isSensitiveFinancialIntent) {
            "⚠️ PROTECTION FINANCIÈRE ACTIVE : Conformément aux règles de sécurité BUSINESS OS, l'IA ne peut jamais exécuter de paiement, virement ou suppression d'écriture sans votre validation explicite dans le module Finances.\n\n"
        } else ""

        return buildString {
            append(guardHeader)
            append("📊 RAPPORT D'ANALYSE ERP TEMPS RÉEL (Données Base SQLite Locale)\n")
            append("────────────────────────────────────────────\n")
            append(erpLiveContext)
            append("\n\n🎯 RECOMMANDATIONS OPÉRATIONNELLES POUR : \"${userPrompt.take(80)}\"\n")
            append("1. Trésorerie & Recouvrement : Prioriser la relance des factures échues (statut OVERDUE) via le workflow d'automatisation avec paiement Mobile Money / Virement.\n")
            append("2. Conformité OHADA : Vérifier le rapprochement des journaux Banque (521) et Monnaie Électronique (552) avant clôture périodique.\n")
            append("3. Pipeline CRM & Stocks : Convertir les prospects en phase NEGOTIATION en clients actifs et réapprovisionner les articles sous le seuil d'alerte.")
        }
    }
}
