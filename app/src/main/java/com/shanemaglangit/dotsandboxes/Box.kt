package com.shanemaglangit.dotsandboxes

data class Box (
    var owner: Player? = null,
    val topConnection: Connection,
    val leftConnection: Connection,
    val rightConnection: Connection,
    val bottomConnection: Connection
)