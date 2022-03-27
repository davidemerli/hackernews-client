package it.devddk.hackernewsclient.domain.model.items

interface MainItem : BaseItem {
    val title : String
    val score : Int
    val descendants : Int
}