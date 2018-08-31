package biodivine.model.editor

import ace.worker.Mirror
import ace.worker.Sender
import biodivine.model.parser.Tokenizer
import biodivine.model.parser.TypeHinter

class AceWorker(sender: Sender) : Mirror(sender) {

    init {
        setTimeout(1000)
    }

    override fun onUpdate() {
        super.onUpdate()

        val model = document.getValue()
        val modelTokens = Tokenizer.scanModel(model)

        val typeHints = TypeHinter.extractHints(modelTokens)

        sender.emit("type_hints", typeHints.map { it.key to it.value.id }.toTypedArray())
    }
}