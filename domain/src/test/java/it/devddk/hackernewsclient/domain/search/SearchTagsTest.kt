package it.devddk.hackernewsclient.domain.search

import it.devddk.hackernewsclient.domain.model.search.SearchTags
import it.devddk.hackernewsclient.domain.model.search.SearchTags.Companion.AllTags
import it.devddk.hackernewsclient.domain.model.search.SearchTags.Companion.NoPollOpts
import it.devddk.hackernewsclient.domain.model.search.SearchTags.Companion.andOf
import it.devddk.hackernewsclient.domain.model.search.SearchTags.Companion.orOf
import junit.framework.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.lang.IllegalArgumentException

class SearchTagsTest {
    @Test
    fun allTags_containsAll() {
        val expected = "(story,comment,show_hn,ask_hn,front_page,poll,pollopt)"
        assertEquals(expected, AllTags.string)
    }

    @Test
    fun noPolloOpts_containsAll() {
        val expected = "(story,comment,show_hn,ask_hn,front_page,poll)"
        assertEquals(expected, NoPollOpts.string)
    }

    @Test
    fun tagsWithParams_isCorrect() {
        val dut1 = SearchTags.Author("giovanni")
        assertEquals("author_giovanni", dut1.string)

        assertThrows(IllegalArgumentException::class.java) {
            SearchTags.Author("")
        }

        val dut2 = SearchTags.StoryById(10)
        assertEquals("story_10", dut2.string)
    }

    @Test
    fun operators_generateCorrectStrings() {
        val dut1 = andOf(orOf(SearchTags.Story, SearchTags.Comment),SearchTags.StoryById(10),orOf(SearchTags.AskHN))
        val expected1 = "(story,comment),story_10,(ask_hn)"
        assertEquals(expected1, dut1.string)
    }
}