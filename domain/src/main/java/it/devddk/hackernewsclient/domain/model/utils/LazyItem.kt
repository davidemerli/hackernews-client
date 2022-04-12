package it.devddk.hackernewsclient.domain.model.utils

class Expandable<out Compressed : Any, Expanded> private constructor(
    comp: Compressed,
    exp: Expanded?,
) {

    val id: Compressed = comp
    val expanded: Expanded? = exp

    val isCompressed: Boolean
        get() = expanded == null

    val isExpanded: Boolean
        get() = expanded != null

    companion object {
        fun <Compressed : Any, Expanded> compressed(id: Compressed): Expandable<Compressed, Expanded> =
            Expandable(id, null)

        fun <Id : Any, Expanded : Identifiable<Id>> expanded(expanded: Expanded): Expandable<Id, Expanded> =
            Expandable(expanded.identificator, expanded)

        fun <Compressed : Any, Expanded> expanded(
            compressed: Compressed,
            expanded: Expanded,
        ): Expandable<Compressed, Expanded> = Expandable(compressed, expanded)
    }


    suspend fun expand(expander: suspend (Compressed) -> Expanded): Expandable<Compressed, Expanded> {
        return if (isExpanded) {
            this
        } else {
            expanded(this.id, expander(this.id))
        }
    }

    suspend fun expandSafe(safeExpander: suspend (Compressed) -> Expanded?): Expandable<Compressed, Expanded> {
        return if (isExpanded) {
            this
        } else {
            return try {
                val expanderResult = safeExpander(this.id)
                if (expanderResult != null) {
                    expanded(this.id, expanderResult)
                } else {
                    this
                }
            } catch (e: Exception) {
                this
            }
        }
    }

}