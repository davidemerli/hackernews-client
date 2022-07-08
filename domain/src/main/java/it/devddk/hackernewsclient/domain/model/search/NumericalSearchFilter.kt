package it.devddk.hackernewsclient.domain.model.search



enum class SearchFilter(val string: String) {
    CREATED_AT_INSTANT("created_at_i"),
    POINTS("points"),
    NUM_COMMENTS("num_comments");

    override fun toString() : String {
        return string
    }

    infix fun greaterThan(x : Int) = NumericalSearchFilter(this, NumericalSearchFilter.Operator.GREATER, x)
    infix fun lessThan(x : Int) = NumericalSearchFilter(this, NumericalSearchFilter.Operator.LESS, x)
    infix fun equal(x : Int) = NumericalSearchFilter(this, NumericalSearchFilter.Operator.EQUAL, x)
    infix fun greaterEqualThan(x : Int) = NumericalSearchFilter(this, NumericalSearchFilter.Operator.GREATER_EQUAL, x)
    infix fun lessEqualThan(x : Int) = NumericalSearchFilter(this, NumericalSearchFilter.Operator.LESS_EQUAL, x)
}

@JvmInline
value class NumericalSearchFilter private constructor(val string : String) {

    constructor(subject : SearchFilter, operator : Operator, value: Int) : this("${subject}${operator}${value}")

    enum class Operator(val string: String) {
        LESS("<"),
        GREATER(">"),
        EQUAL("="),
        LESS_EQUAL("<="),
        GREATER_EQUAL(">=");

        override fun toString() : String {
            return string
        }
    }

    override fun toString() : String {
        return string
    }

}


class NumericalSearchFilters constructor(private val filters : List<NumericalSearchFilter>) {
    override fun toString() : String {
        return if(filters.isEmpty()) {
            ""
        } else {
            filters.drop(1).fold(filters[0].toString()) { str, new ->
                "$str,$new"
            }
        }
    }

    operator fun plus(other : NumericalSearchFilters) = NumericalSearchFilters(this.filters + other.filters)
    operator fun plus(other : NumericalSearchFilter) = NumericalSearchFilters(this.filters + other)

    fun isEmpty() : Boolean = toString() == ""
}

