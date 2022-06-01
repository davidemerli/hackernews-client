package it.devddk.hackernewsclient.domain.model.items


data class ItemTree(
    val item: Item,
    val comments: List<ItemTree>,
    val options: List<ItemTree>
)