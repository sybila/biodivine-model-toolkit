package biodivine.model.editor

import ace.EditSession
import ace.Tokenizer
import ace.mode.Mode
import ace.startWorkerFromBundle
import ace.worker.WorkerClient
import biodivine.model.parser.token.Identifier

object AceMode : Mode() {

    override fun getTokenizer(): Tokenizer<*, *> = AceTokenizer

    override fun createWorker(session: EditSession): WorkerClient? {
        val client = startWorkerFromBundle("AceWorker", "worker.bundle.js")

        client.on<WorkerClient.Event<Array<EPair<String, String>>>>("type_hints") { e ->
            val typeHints = e.data.map { (name, id) -> name to Identifier.fromId(id) }.toMap()
            AceTokenizer.typeHints = typeHints
            session.asDynamic().bgTokenizer.start(0)
            Unit
        }

        client.attachToDocument(session.getDocument())
        return client
    }
}