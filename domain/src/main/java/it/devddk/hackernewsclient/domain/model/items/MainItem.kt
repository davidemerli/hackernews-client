package it.devddk.hackernewsclient.domain.model.items

interface MainItem {
    val title : String
    val score : Int
    val descendants : Int
}