package biodivine.model.parser

sealed class Rule<R: Rule<R>> {

    abstract fun readToken(string: String, startIndex: Int): Token<R>?


    object Number : Rule<Number>() {
        override fun readToken(string: String, startIndex: Int): Token<Number>? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

}
