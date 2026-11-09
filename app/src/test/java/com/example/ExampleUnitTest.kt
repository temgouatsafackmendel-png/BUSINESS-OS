package com.example

import com.example.data.ai.AiConfigState
import com.example.data.ai.AiProviderType
import com.example.data.ai.PluggableAiEngine
import com.example.ui.viewmodel.BusinessOsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun financialFormatting_xafAndEur_isAccurate() {
        val xafFormatted = BusinessOsViewModel.formatMoney(14_500_000_00L, "XAF")
        assertTrue(xafFormatted.contains("XAF"))
        assertTrue(xafFormatted.contains("14"))

        val eurFormatted = BusinessOsViewModel.formatMoney(42_000_50L, "EUR")
        assertTrue(eurFormatted.contains("EUR"))
        assertTrue(eurFormatted.contains("50"))
    }

    @Test
    fun aiProvider_whenDisabled_returnsAutonomousGuardrail() = runTest {
        val engine = PluggableAiEngine()
        val disabledConfig = AiConfigState(
            isEnabled = false,
            providerType = AiProviderType.DISABLED
        )
        val resp = engine.analyzeAndRespond(
            userPrompt = "Analyse la trésorerie",
            erpLiveContext = "Trésorerie: 10 000 000 XAF",
            config = disabledConfig
        )
        assertEquals("SYSTEM_GUARDRAIL", resp.role)
        assertTrue(resp.content.contains("désactivé"))
    }

    @Test
    fun aiProvider_sensitiveFinancialOperation_enforcesHumanConfirmation() = runTest {
        val engine = PluggableAiEngine()
        val localConfig = AiConfigState(
            isEnabled = true,
            providerType = AiProviderType.LOCAL_DETERMINISTIC
        )
        val resp = engine.analyzeAndRespond(
            userPrompt = "Exécuter un virement bancaire de 5 000 000 XAF",
            erpLiveContext = "Banque: 28 450 000 XAF",
            config = localConfig
        )
        assertNotNull(resp.requiresHumanApprovalAction)
        assertTrue(resp.content.contains("PROTECTION FINANCIÈRE ACTIVE"))
    }
}
