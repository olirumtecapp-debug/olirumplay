package com.olirum.player

import com.olirum.player.data.remote.youtube.YouTubeDurationParser
import org.junit.Assert.assertEquals
import org.junit.Test

class YouTubeDurationParserTest {

    @Test
    fun testParseStandardDuration() {
        assertEquals("03:45", YouTubeDurationParser.parse("PT3M45S"))
        assertEquals("00:59", YouTubeDurationParser.parse("PT59S"))
        assertEquals("12:00", YouTubeDurationParser.parse("PT12M"))
        assertEquals("1:02:03", YouTubeDurationParser.parse("PT1H2M3S"))
        assertEquals("2:00:00", YouTubeDurationParser.parse("PT2H"))
    }

    @Test
    fun testParseToSeconds() {
        assertEquals(225L, YouTubeDurationParser.parseToSeconds("PT3M45S"))
        assertEquals(59L, YouTubeDurationParser.parseToSeconds("PT59S"))
        assertEquals(3723L, YouTubeDurationParser.parseToSeconds("PT1H2M3S"))
    }

    @Test
    fun testCleanHtmlEntities() {
        assertEquals("Rock & Roll", YouTubeDurationParser.cleanHtmlEntities("Rock &amp; Roll"))
        assertEquals("Coldplay \"Yellow\"", YouTubeDurationParser.cleanHtmlEntities("Coldplay &quot;Yellow&quot;"))
        assertEquals("Guns N' Roses", YouTubeDurationParser.cleanHtmlEntities("Guns N&#39; Roses"))
    }
}
