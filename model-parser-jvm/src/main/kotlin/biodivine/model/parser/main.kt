package biodivine.model.parser

fun main(args: Array<String>) {
    val model = """

var x in [true, false]
var y in [true, false]
var z in [true, false]

event {
    y = x
}

event {
    z = { mode == "and" => " }
}

const k = 1

param mode in ["and", "or"]
    """.trimIndent()

    val tokens = Tokenizer.scanModel(model)

    tokens.forEach {
        println("Token(${it.rule?.id}): '${it.value}'")
    }
}