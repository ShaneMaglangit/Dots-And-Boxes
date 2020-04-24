package com.shanemaglangit.dotsandboxes

data class Connection (
    var player: Player? = null,
    val direction: ConnectionDirection,
    val headDot: Dot,
    val tailDot: Dot
)