package it.devddk.hackernewsclient.domain.model.items

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ItemTreeDfsTest {
    @Test
    fun dfs_isCorrect() {
        val item1 = Item(10, ItemType.STORY, time = LocalDateTime.now(), by = "giovanni")
        val item2 = Item(11, ItemType.STORY, time = LocalDateTime.now(), by = "giovanni")
        val item3 = Item(12, ItemType.STORY, time = LocalDateTime.now(), by = "giovanni")
        val item4 = Item(13, ItemType.STORY, time = LocalDateTime.now(), by = "giovanni")
        val item5 = Item(14, ItemType.STORY, time = LocalDateTime.now(), by = "giovanni")
        val item6 = Item(15, ItemType.STORY, time = LocalDateTime.now(), by = "giovanni")


        val dut1 =
            ItemTree(
                10,
                Item(10, ItemType.STORY, time = LocalDateTime.now(), by = "giovanni"),
                emptyList(),
                emptyList()
            )
        assertEquals(dut1.dfsWalkComments().size, 1)
        assertEquals(dut1.dfsWalkComments().map { it.id }, listOf(item1.id))

        val dut2 =
            ItemTree(
                10,
                item1,
                listOf(
                    ItemTree(
                        11,
                        item2,
                        listOf(ItemTree(12, item3, emptyList(), emptyList())),
                        listOf(ItemTree(13, item4, emptyList(), emptyList()))
                    ),
                    ItemTree(14, item5, listOf(
                        ItemTree(15, item6, emptyList(), emptyList())
                    ), emptyList())
                ),
                emptyList()
            )

        assertEquals(dut1.dfsWalkComments().size, 1)
        assertEquals(dut2.dfsWalkComments().map { it.id }, listOf(item1.id, item2.id, item3.id, item5.id, item6.id))
    }
}