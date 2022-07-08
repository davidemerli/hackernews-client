package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.utils.Stack
import it.devddk.hackernewsclient.domain.utils.pop
import it.devddk.hackernewsclient.domain.utils.push
import java.util.*


data class ItemTree(
    val rootItemId: ItemId,
    val item: Item,
    val comments: List<ItemTree>,
    val options: List<ItemTree>
) {

   fun dfsWalkComments() : List<Item> {
       val dfsList = mutableListOf<Item>()
       val stack : Stack<ItemTree> = LinkedList<ItemTree>()
       stack.push(this)
       while(stack.isNotEmpty()) {
           val currNode = stack.pop()!!
           dfsList.add(currNode.item)
           currNode.comments.reversed().forEach {
               stack.push(it)
           }
       }
       return dfsList
   }
}