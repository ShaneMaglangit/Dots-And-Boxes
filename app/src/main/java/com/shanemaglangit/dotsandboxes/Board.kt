package com.shanemaglangit.dotsandboxes

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class Board(context: Context, attrs: AttributeSet) : View(context, attrs) {
    // constants
    private val defaultUserColor = ColorStateList.valueOf(Color.parseColor("#00A8CC"))
    private val defaultBotColor = ColorStateList.valueOf(Color.parseColor("#FFA41B"))

    // paint for drawing components
    var dotPaint: Paint = Paint()
    var connectionPaint: Paint = Paint()
    var boxPaint: Paint = Paint()

    // player colors
    var userColor: ColorStateList
    var botColor: ColorStateList

    // connection width
    var connectionWidth: Float

    // dot radius
    var radius: Float

    // list of objects
    var dots: MutableList<MutableList<Dot>> = mutableListOf()
    var connections: MutableList<MutableList<Connection>> = mutableListOf()
    var boxes: MutableList<Box> = mutableListOf()

    // tracks the current player on turn
    var activePlayer: Player = Player.USER
    var gameRunning = true

    // scores
    var userScore: Int
    var botScore: Int

    // listener for when a user scores and game ends
    var scoreChangedListener : ScoreChangedListener? = null
    var gameEndListener : GameEndListener? = null

    init {
        background = ColorDrawable(Color.WHITE)

        // get the styles set from the custom view's xml attributes declared at /res/values/attrs.xml
        context.theme.obtainStyledAttributes(attrs, R.styleable.Board, 0, 0)
            .apply {
                try {
                    // get the color for the user and bot
                    userColor =
                        getColorStateList(R.styleable.Board_userColor) ?: defaultUserColor
                    botColor = getColorStateList(R.styleable.Board_botColor) ?: defaultBotColor
                    // get the radius or set it to 36F by default
                    radius = getFloat(R.styleable.Board_radius, 36F)
                    // get the connection width or set it to 24F by default
                    connectionWidth = getFloat(R.styleable.Board_connectionWidth, 24F)
                    // get score
                    userScore = getInt(R.styleable.Board_userScore, 0)
                    botScore = getInt(R.styleable.Board_botScore, 0)
                } finally {
                    recycle()
                }
            }
    }

    /**
     * handle user clicks
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // only handle the touch if it's his/her turn and the action is down.
        if (event?.action == MotionEvent.ACTION_DOWN && activePlayer == Player.USER && gameRunning) {
            // loop through the connections
            for(row in connections) {
                // filter the next loop to only iterate over inactive connections
                for (con in row.filter { it.player == null }) {
                    // checks the orientation of the connection
                    val isHorizontal = con.direction == ConnectionDirection.HORIZONTAL

                    // compute for the boundary of the connection
                    // the bounds is extended by the radius to improve the accessibility
                    val startBounds = (if (isHorizontal) con.headDot.x else con.headDot.y) + radius
                    val endBounds = (if (isHorizontal) con.tailDot.x else con.tailDot.y) - radius
                    val upperWidthBounds =
                        (if (isHorizontal) con.headDot.y else con.headDot.x) - radius
                    val lowerWidthBounds =
                        (if (isHorizontal) con.headDot.y else con.headDot.x) + radius

                    // flag to check whether the click is within the bounds of the connection
                    val withinBounds = when (isHorizontal) {
                        true -> event.y in upperWidthBounds..lowerWidthBounds && event.x in startBounds..endBounds
                        else -> event.x in upperWidthBounds..lowerWidthBounds && event.y in startBounds..endBounds
                    }

                    // if the click falls between the bounds
                    if (withinBounds) {
                        performMove(con)
                        break
                    }
                }
            }

            // start performing the bot moves
            performBotMove()
        }
        return super.onTouchEvent(event)
    }

    private fun performMove(connection: Connection) {
        // set the connection's owner to the player that is on turn
        connection.player = activePlayer
        // check if a box is created from the move
        val boxCreated = isBoxCreated(connection)
        // if a box is not created, then switch turn
        if(!boxCreated) activePlayer = if(activePlayer == Player.USER) Player.BOT else Player.USER
        // tell the view to redraw its component
        invalidate()
        // check if the game ended
        checkIfGameEnds()
    }

    /**
     * Check if the game ended
     */
    private fun checkIfGameEnds() {
        // count all the boxes that isn't owned
        val inactiveBoxes = boxes.filter { it.owner == null }.size

        if(inactiveBoxes == 0) {
            // get the player with the higher score
            val winner = when {
                userScore > botScore -> Player.USER
                botScore > userScore -> Player.BOT
                else -> null
            }

            gameRunning = false
            gameEndListener?.gameEnd(winner)
        }
    }

    /**
     * callback that is invoked when the screen changes.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // only instantiate objects if they aren't yet created
        // calling it onSizeChanged ensures that it calculates with the proper size
        initializeComponents()
    }

    /**
     * draw the components
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // iterate through the boxes and draw them
        boxes.forEach {
            // set the paint color for the color of the boxes
            boxPaint.color = when (it.owner) {
                Player.USER -> userColor.defaultColor
                Player.BOT -> botColor.defaultColor
                else -> Color.WHITE
            }

            canvas?.drawRect(
                it.topConnection.headDot.x, // origin x
                it.topConnection.headDot.y, // origin y
                it.bottomConnection.tailDot.x, // end x
                it.bottomConnection.tailDot.y, // end y
                boxPaint
            )
        }
        // iterate through the connections and draw them
        connectionPaint.strokeWidth = connectionWidth
        connectionPaint.setShadowLayer(6F, 0F, 0F, Color.parseColor("#121212"))
        connections.forEach { row ->
            row.forEach { connection ->
                // set the color depending on who owns the connection
                connectionPaint.color = when (connection.player) {
                    Player.USER -> userColor.defaultColor
                    Player.BOT -> botColor.defaultColor
                    else -> Color.LTGRAY
                }

                // draw a line from head to tail
                canvas?.drawLine(
                    connection.headDot.x,
                    connection.headDot.y,
                    connection.tailDot.x,
                    connection.tailDot.y,
                    connectionPaint
                )
            }
        }
        // set the paint color to the color for the dots
        dotPaint.color = Color.BLACK
        // iterate through the list of dots and draw them
        dots.forEach { row ->
            row.forEach { dot ->
                canvas?.drawCircle(dot.x, dot.y, radius, dotPaint)
            }
        }
    }

    /**
     * used to check if a box is created after activating a connection
     */
    private fun isBoxCreated(newConnection: Connection) : Boolean {
        var boxCreated = false

        // get all the boxes with a connection equal to the new connection
        val relativeBoxes = boxes.filter{
            listOf(it.topConnection, it.leftConnection, it.rightConnection, it.bottomConnection)
                .contains(newConnection)
        }

        relativeBoxes.forEach {
            // check if all connections are owned by a player
            if(it.topConnection.player != null &&
                it.leftConnection.player != null &&
                it.rightConnection.player != null &&
                it.bottomConnection.player != null
            ) {
                // set the owner if the box to the player currently in turn
                it.owner = activePlayer
                // add score and update
                if(activePlayer == Player.USER) userScore++ else botScore++
                scoreChangedListener?.scoreChanged(userScore, botScore)
                boxCreated = true
            }
        }

        return boxCreated
    }

    /**
     * used to initialize the objects for connection, dots, and boxes.
     */
    private fun initializeComponents() {
        // Create the dots
        for (row in 0 until 5) {
            val rowOfDots = mutableListOf<Dot>()
            for (col in 0 until 5) {
                val widthWithPadding = width - paddingLeft - paddingRight - (radius * 2)
                val heightWithPadding = height - paddingTop - paddingBottom - (radius * 2)
                val xPos = paddingLeft + radius + (widthWithPadding / 4 * col)
                val yPos = paddingTop + radius + (heightWithPadding / 4 * row)
                rowOfDots.add(Dot(xPos, yPos))
            }
            dots.add(rowOfDots)
        }
        // Create the connections
        for (row in 0 until dots.size) {
            val horizontalRow = mutableListOf<Connection>()
            val verticalRow = mutableListOf<Connection>()
            for (col in 0 until dots[row].size) {
                if (col + 1 < dots[row].size) {
                    horizontalRow.add(
                        Connection(
                            direction = ConnectionDirection.HORIZONTAL,
                            headDot = dots[row][col],
                            tailDot = dots[row][col + 1]
                        )
                    )
                }
                if (row + 1 < dots.size) {
                    verticalRow.add(
                        Connection(
                            direction = ConnectionDirection.VERTICAL,
                            headDot = dots[row][col],
                            tailDot = dots[row + 1][col]
                        )
                    )
                }
            }
            connections.add(horizontalRow)
            connections.add(verticalRow)
        }
        // create a boxes
        for(row in 0 until connections.size - 2 step 2) {
            for(col in 0 until connections[row].size) {
                boxes.add(Box(null, connections[row][col], connections[row + 1][col], connections[row + 1][col + 1], connections[row + 2][col]))
            }
        }
    }

    /**
     * A recursive to keep performing move until the bot's turn end
     */
    private fun performBotMove() {
        if(activePlayer == Player.BOT && gameRunning) {
            Handler().postDelayed({
                // get the best move
                val move = botMove(boxes)

                // perform move if there is a possible move
                if(move != null) {
                    performMove(move)
                    performBotMove()
                }
            }, 150)
        }
    }

    /**
     * reset the state of the board
     */
    fun reset() {
        gameRunning = true
        activePlayer = Player.USER
        userScore = 0
        botScore = 0

        dots.clear()
        connections.clear()
        boxes.clear()

        scoreChangedListener?.scoreChanged(userScore, botScore)

        initializeComponents()
        invalidate()
    }
}