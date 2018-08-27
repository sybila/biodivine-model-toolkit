package biodivine.model.editor

import ace.Tokenizer
import ace.mode.Mode

object AceMode : Mode() {

    override fun getTokenizer(): Tokenizer<*, *> = AceTokenizer

}