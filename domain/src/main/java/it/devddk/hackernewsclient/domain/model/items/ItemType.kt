package it.devddk.hackernewsclient.domain.model.items

enum class ItemType(val isRoot : Boolean) {
    JOB(true),
    STORY(true),
    COMMENT(false),
    POLL(true),
    POLL_OPT(false)
}