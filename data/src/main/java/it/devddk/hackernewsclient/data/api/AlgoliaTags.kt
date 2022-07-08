package it.devddk.hackernewsclient.data.api

import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.lang.IllegalArgumentException

sealed class AlgoliaTags(open val string: String) {

    companion object {
        fun orOf(vararg tags: AlgoliaTags): Or {
            return Or(listOf(*tags))
        }

        fun andOf(vararg tags: AlgoliaTags): And {
            return And(listOf(*tags))
        }

        val AllTags = orOf(Story, Comment, Poll, ShowHN, AskHN, FrontPage, PollOpt, Poll)
        val NoPolls = orOf(Story, Comment, Poll, ShowHN, AskHN, FrontPage)

        private fun buildCommaList(operands : List<AlgoliaTags>) : String {
            return if(operands.isEmpty()) {
                ""
            } else {
                operands.drop(1).fold(operands[0].string) { str: String, new -> "$str,${new.string}" }
            }

        }
    }

    sealed class Literal(override val string: String) : AlgoliaTags(string)

    object Story : Literal("story")
    object Comment : Literal("comment")
    object Poll : Literal("poll")
    object PollOpt : Literal("pollopt")
    object ShowHN : Literal("show_hn")
    object AskHN : Literal("ask_hn")
    object FrontPage : Literal("front_page")
    data class Author(val userName: String) : Literal("author_$userName")
    data class StoryByUser(val id: ItemId) : Literal("story_$id")

    sealed class Operator(override val string: String) : AlgoliaTags(string)

    data class Or(val operands: List<AlgoliaTags>) : Operator("(${buildCommaList(operands)})")

    data class And(val operands: List<AlgoliaTags>) : Operator(buildCommaList(operands))

}
