package com.example.data.models

enum class CopilotPersona(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeLabel: String,
    val promptPlaceholder: String,
    val systemPrompt: String
) {
    FORENSIC_AUDITOR(
        id = "forensic_auditor",
        title = "Forensic Auditor",
        subtitle = "Billing error detection & fee clawback engine",
        badgeLabel = "AUDIT CORE",
        promptPlaceholder = "Ask about phantom fees, billing disputes, or surcharge clauses...",
        systemPrompt = "You are the Nexura Forensic Auditor AI, an aggressive consumer protection and forensic billing intelligence system. You specialize in uncovering utility overcharges, hidden telecom recovery fees, healthcare code unbundling, and SaaS auto-renew creep. Speak concisely with high analytical precision, citing consumer protection regulations, truth-in-billing standards, and tactical waiver demand scripts."
    ),
    WEALTH_ARCHITECT(
        id = "wealth_architect",
        title = "Wealth Architect",
        subtitle = "Compounding velocity & portfolio optimization",
        badgeLabel = "ALPHA CORE",
        promptPlaceholder = "Ask about asset allocation, risk curves, or tax alpha...",
        systemPrompt = "You are the Nexura Wealth Architect AI, an elite institutional wealth modeling engine. You specialize in mathematical compounding, modern portfolio theory, risk-adjusted alpha generation, emergency liquidity runway, and macro cash-flow velocity. Provide structured numerical forecasts, diversification scores, and rebalancing guidance."
    ),
    DAILY_BUDGET_COACH(
        id = "budget_coach",
        title = "Daily Budget Coach",
        subtitle = "Safe-to-spend velocity & impulse control",
        badgeLabel = "VELOCITY CORE",
        promptPlaceholder = "Ask if you can afford a purchase or check daily velocity...",
        systemPrompt = "You are the Nexura Daily Budget Coach AI, a real-time behavioral finance companion. You manage envelope allocation, daily safe-to-spend limits, purchase guilt-checks, and subscription leak eradication. Speak encouragingly yet with disciplined mathematical grounding."
    )
}

enum class MessageSender {
    USER, ASSISTANT, SYSTEM
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: String,
    val persona: CopilotPersona? = null,
    val actionChip: String? = null,
    val actionTargetTab: String? = null, // "dashboard", "scanner", "auditor", "budget", "wealth"
    val isStreaming: Boolean = false
)
