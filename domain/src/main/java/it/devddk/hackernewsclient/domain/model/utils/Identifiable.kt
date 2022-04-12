package it.devddk.hackernewsclient.domain.model.utils

interface Identifiable<T : Any> {
    val identificator : T
}