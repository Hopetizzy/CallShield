package com.callshield.app.engine

object NumberNormalizer {

    /**
     * Cleans raw telephone string of dashes, spaces, parentheses, etc.
     */
    fun sanitize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.replace("[\\s\\-\\(\\)\\.]".toRegex(), "")
    }

    /**
     * Produces normalized formats for matching.
     * In Nigeria (+234):
     * - "02012345678" -> "+2342012345678" and "02012345678"
     * - "+2342012345678" -> "+2342012345678" and "02012345678"
     * - "2342012345678" -> "+2342012345678" and "02012345678"
     */
    fun getNormalizedVariants(raw: String?): NormalizedNumberVariants {
        val clean = sanitize(raw)
        if (clean.isBlank()) {
            return NormalizedNumberVariants(raw = "", international = "", national = "", rawDigitsOnly = "")
        }

        val digitsOnly = clean.replace("+", "")

        val (international, national) = when {
            // Begins with +234
            clean.startsWith("+234") -> {
                val rest = clean.substring(4)
                val nat = "0$rest"
                Pair(clean, nat)
            }
            // Begins with 234 without +
            clean.startsWith("234") && clean.length > 3 -> {
                val intl = "+$clean"
                val rest = clean.substring(3)
                val nat = "0$rest"
                Pair(intl, nat)
            }
            // Begins with 0 (e.g. 0201..., 080..., 07000...)
            clean.startsWith("0") -> {
                val rest = clean.substring(1)
                val intl = "+234$rest"
                Pair(intl, clean)
            }
            // Fallback for other formats
            else -> {
                Pair(clean, clean)
            }
        }

        return NormalizedNumberVariants(
            raw = clean,
            international = international,
            national = national,
            rawDigitsOnly = digitsOnly
        )
    }
}

data class NormalizedNumberVariants(
    val raw: String,
    val international: String,
    val national: String,
    val rawDigitsOnly: String
)
