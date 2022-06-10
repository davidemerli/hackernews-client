package it.devddk.hackernewsclient.domain.model.items

enum class ItemCollectionTag(val availableOffline : Boolean, val itemFilter : (Item) -> Boolean) {
    FAVORITE(true, { true }),
    READ_LATER(true, { true }),
    APPLY_LATER(true, { it.type == ItemType.JOB })
}