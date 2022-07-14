package it.devddk.hackernewsclient.domain.utils

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StackTest {

    private lateinit var dut : Stack<Int>

    @Before
    fun initStack() {
        dut = mutableListOf()
    }

    @Test
    fun popPush_LIFO_work() {
        dut.push(1)
        dut.push(2)
        dut.push(3)
        assertEquals(3, dut.peek())
        assertEquals(3, dut.peek())
        assertEquals(3, dut.pop())
        assertEquals(2, dut.pop())
        dut.push(4)
        assertEquals(4, dut.pop())
        assertEquals(1, dut.peek())
        assertEquals(1, dut.pop())

    }

    @Test
    fun edgeCases_handled() {
        assertNull(dut.pop())
        assertNull(dut.peek())
        dut.push(1)
        dut.pop()
        assertNull(dut.pop())
        assertNull(dut.peek())
    }

}