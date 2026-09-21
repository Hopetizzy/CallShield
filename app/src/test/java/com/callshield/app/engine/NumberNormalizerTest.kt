package com.callshield.app.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberNormalizerTest {

    @Test
    fun testNigerianNumberNormalization() {
        // Test 0201 Lagos/Ibadan VoIP trunk number
        val v1 = NumberNormalizer.getNormalizedVariants("02012345678")
        assertEquals("+2342012345678", v1.international)
        assertEquals("02012345678", v1.national)
        assertEquals("02012345678", v1.rawDigitsOnly)

        // Test +234 international format
        val v2 = NumberNormalizer.getNormalizedVariants("+2342018889999")
        assertEquals("+2342018889999", v2.international)
        assertEquals("02018889999", v2.national)

        // Test with dashes and spaces
        val v3 = NumberNormalizer.getNormalizedVariants("+234 (201) 888-9999")
        assertEquals("+2342018889999", v3.international)
        assertEquals("02018889999", v3.national)

        // Test 234 without plus
        val v4 = NumberNormalizer.getNormalizedVariants("2347000001234")
        assertEquals("+2347000001234", v4.international)
        assertEquals("07000001234", v4.national)
    }
}
