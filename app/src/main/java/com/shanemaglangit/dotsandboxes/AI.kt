package com.shanemaglangit.dotsandboxes

fun botMove(boxes: MutableList<Box>) : Connection? {
    // moves that will complete a box
    var goodMoves = mutableListOf<Connection>()
    // moves that won't complete a box and won't let the user complete it at next turn
    var safeMoves = mutableListOf<Connection>()
    // moves that will allow the user to complete a box on next turn
    var badMoves = mutableListOf<Connection>()

    for(box in boxes) {
        // skip box if already owned
        if(box.owner != null) continue

        // get all the possible connections
        val possibleMoves = mutableListOf<Connection>()

        // if the connection is not yet active / owned then add it as possible move
        if(box.topConnection.player == null) possibleMoves.add(box.topConnection)
        if(box.leftConnection.player == null) possibleMoves.add(box.leftConnection)
        if(box.rightConnection.player == null) possibleMoves.add(box.rightConnection)
        if(box.bottomConnection.player == null) possibleMoves.add(box.bottomConnection)

        // if the connection can be completed this turn then it is a good move
        if(possibleMoves.size == 1) goodMoves.addAll(possibleMoves)
        // if the connection can be completed only after 2 turns, then it is a bad move
        else if(possibleMoves.size == 2) badMoves.addAll(possibleMoves)
        // if the box cannot be completed even after 2 moves, then it is a safe move
        else safeMoves.addAll(possibleMoves)
    }

    // remove all bad moves in a box that is considered a good move in its neighbouring boxes.
    badMoves = badMoves.filter { !goodMoves.contains(it) }.toMutableList()
    // remove all moves that may be safe in a box but bad in its neighbouring boxes.
    safeMoves = safeMoves.filter { !badMoves.contains(it) }.toMutableList()

    // return a random good move
    if(goodMoves.isNotEmpty()) return goodMoves[(0 until goodMoves.size).random()]
    // return a safe bad move
    else if(safeMoves.isNotEmpty()) return safeMoves[(0 until safeMoves.size).random()]
    // return a random bad move
    else if(badMoves.isNotEmpty()) return badMoves[(0 until badMoves.size).random()]
    // no possible move
    return null
}