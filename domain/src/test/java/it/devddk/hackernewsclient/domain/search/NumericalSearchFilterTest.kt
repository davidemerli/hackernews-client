package it.devddk.hackernewsclient.domain.search

import it.devddk.hackernewsclient.domain.model.search.NumericalSearchFilters
import it.devddk.hackernewsclient.domain.model.search.SearchFilter
import junit.framework.Assert.assertEquals
import org.junit.Test

class NumericalSearchFilterTest {

    @Test
    fun emptyFilter_isEmptyString() {
        val dut = NumericalSearchFilters(emptyList())
        assertEquals("Empty list should produce empty string","", dut.toString())
    }

    @Test
    fun filter_Operators() {

        val num = 5

        val dut = NumericalSearchFilters(listOf(
            SearchFilter.CREATED_AT_INSTANT lessThan num,
            SearchFilter.NUM_COMMENTS greaterThan num,
            SearchFilter.POINTS equal num,
            SearchFilter.POINTS greaterEqualThan num,
            SearchFilter.POINTS lessEqualThan num
        ))

        val expected = "created_at_i<$num," +
                    "num_comments>$num," +
                    "points=$num," +
                    "points>=$num," +
                    "points<=$num"

        assertEquals("Filter toString() should match expected",expected, dut.toString())

        val dut2 = dut + dut
        val expected2 = "$expected,$expected"

        assertEquals("Sum of duts",expected2, dut2.toString())

        val dut3 = dut + (SearchFilter.POINTS equal 10)
        val expected3 = "$expected,points=10"

        assertEquals("Sum of duts",expected3, dut3.toString())

    }



}