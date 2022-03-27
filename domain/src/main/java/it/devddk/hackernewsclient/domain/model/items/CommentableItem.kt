package it.devddk.hackernewsclient.domain.model.items

interface CommentableItem {
    val kids : List<CommentItem>
}