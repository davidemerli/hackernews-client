package it.devddk.hackernewsclient.domain.model.items

interface ParentItem {
    val kids : List<ChildItem>
}