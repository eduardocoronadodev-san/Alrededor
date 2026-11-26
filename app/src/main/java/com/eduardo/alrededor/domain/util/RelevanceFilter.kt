package com.eduardo.alrededor.domain.util

object RelevanceFilter {
    private val IRRELEVANT_KEYWORDS = listOf(
        "calle", "avenida", "colonia", "barrio", "localidad", "estación de metro",
        "parada de autobús", "carretera", "autopista", "municipio", "poblado"
    )

    fun isRelevant(description: String?): Boolean {
        if (description.isNullOrBlank()) return true
        val descLower = description.lowercase()
        return IRRELEVANT_KEYWORDS.none { descLower.contains(it) }
    }
}
