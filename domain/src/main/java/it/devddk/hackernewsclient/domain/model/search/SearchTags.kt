package it.devddk.hackernewsclient.domain.model.search

import it.devddk.hackernewsclient.domain.model.utils.ItemId

sealed class SearchTags(open val string: String) {

    companion object {
        fun orOf(vararg tags: SearchTags): Or {
            return Or(listOf(*tags))
        }

        fun andOf(vararg tags: SearchTags): And {
            return And(listOf(*tags))
        }

        val AllTags = orOf(Story, Comment, Poll, ShowHN, AskHN, FrontPage, PollOpt, Poll)
        val NoPolls = orOf(Story, Comment, Poll, ShowHN, AskHN, FrontPage)

        private fun buildCommaList(operands : List<SearchTags>) : String {
            return if(operands.isEmpty()) {
                throw IllegalArgumentException("Operator could not be empty")
            } else {
                operands.drop(1).fold(operands[0].string) { str: String, new -> "$str,${new.string}" }
            }
        }
    }

    sealed class Literal(override val string: String) : SearchTags(string)

    object Story : Literal("story")
    object Comment : Literal("comment")
    object Poll : Literal("poll")
    object PollOpt : Literal("pollopt")
    object ShowHN : Literal("show_hn")
    object AskHN : Literal("ask_hn")
    object FrontPage : Literal("front_page")
    data class Author(val userName: String) : Literal("author_$userName")
    data class StoryById(val id: ItemId) : Literal("story_$id")

    sealed class Operator(override val string: String) : SearchTags(string)

    data class Or(val operands: List<SearchTags>) : Operator("(${buildCommaList(operands)})")

    data class And(val operands: List<SearchTags>) : Operator(buildCommaList(operands))

    override fun toString(): String {
        return string
    }
}