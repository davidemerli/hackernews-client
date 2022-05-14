package it.devddk.hackernewsclient.domain.model.items

import it.devddk.hackernewsclient.domain.model.User
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime


data class Item(
    val id: Int,
    val type: ItemType,
    val deleted: Boolean = false,
    val by: Expandable<String, User>?,
    val time: LocalDateTime?,
    val dead: Boolean = false,
    val parent: Expandable<ItemId, Item>? = null,
    val text: String? = null,
    val kids: Map<ItemId, ICommentItem?> = emptyMap(),
    val title: String? = null,
    val descendants: Int? = 0,
    val parts: Map<ItemId, PollOptItem?> = emptyMap(),
    val poll: Expandable<ItemId, PollItem>? = null,
    val score: Int? = null,
    val url: String? = null,
) {

    companion object {
        fun fromBaseItem(item: IBaseItem): Item {
            return when (item) {
                is StoryItem -> Item(item)
                is JobItem -> Item(item)
                is PollItem -> Item(item)
                is ICommentItem -> Item(item)
                is PollOptItem -> Item(item)
                is ICommentableItem -> Item(item)
                else -> Item(item)
            }
        }
    }

    constructor(story: IStoryItem) : this(
        story.id,
        ItemType.STORY,
        story.deleted,
        story.by,
        story.time,
        story.dead,
        text = story.text,
        kids = story.kids,
        title = story.title,
        descendants = story.descendants,
        score = story.score,
        url = story.url
    )

    constructor(job: IJobItem) : this(
        job.id,
        ItemType.JOB,
        job.deleted,
        job.by,
        job.time,
        job.dead,
        title = job.title,
        score = job.score,
        url = job.url
    )

    constructor(poll: IPollItem) : this(
        poll.id,
        ItemType.POLL,
        poll.deleted,
        poll.by,
        poll.time,
        poll.dead,
        kids = poll.kids,
        title = poll.title,
        descendants = poll.descendants,
        parts = poll.parts,
        score = poll.score)

    constructor(comment: ICommentItem) : this(
        comment.id,
        ItemType.COMMENT,
        comment.deleted,
        comment.by,
        comment.time,
        comment.dead,
        comment.parent,
        comment.text,
        comment.kids)

    constructor(pollOpt: IPollOptItem) : this(
        pollOpt.id,
        ItemType.POLL_OPT,
        pollOpt.deleted,
        pollOpt.by,
        pollOpt.time,
        pollOpt.dead,
        text = pollOpt.text,
        poll = pollOpt.poll,
        score = pollOpt.score
    )

    constructor(item: IBaseItem) : this(
        item.id,
        item.type,
        item.deleted,
        item.by,
        item.time,
        item.dead
    )

    constructor(item: ICommentableItem) : this(
        item.id,
        item.type,
        item.deleted,
        item.by,
        item.time,
        item.dead,
        kids = item.kids
    )

    fun asStoryItem(): StoryItem =
        StoryItem(id, deleted, by, time, dead, kids, title, score, descendants, url, text)

    fun asJobItem(): JobItem =
        JobItem(id, deleted, by, time, dead, title, score, text)

    fun asPollItem(): PollItem =
        PollItem(id, deleted, by, time, dead, title, score, descendants, kids, parts)

    fun asPollOptItem(): PollOptItem =
        PollOptItem(id, deleted, by, time, dead, text, poll, score)

    fun asCommentItem(): CommentItem =
        CommentItem(id, deleted, by, time, dead, kids, parent, text)

    fun asCommentableItem(): CommentableItem =
        CommentableItem(id, type, deleted, by, time, dead, kids)

    fun asBaseItem(): IBaseItem =
        BaseItem(id, type, deleted, by, time, dead)

}