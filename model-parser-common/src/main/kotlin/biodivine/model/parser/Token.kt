package biodivine.model.parser

import biodivine.model.parser.utils.StringInterval

data class Token<R: Rule<R>>(
        val rule: R, val value: String,
        val position: StringInterval
)