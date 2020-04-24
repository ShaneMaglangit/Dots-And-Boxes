package com.shanemaglangit.dotsandboxes

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View


class Board(context: Context, attrs: AttributeSet) : View(context, attrs) {
    // constants
    private val defaultPlayerColor = ColorStateList.valueOf(Color.parseColor("#00A8CC"))
    private val defaultBotColor = ColorStateList.valueOf(Color.parseColor("#FFA41B"))

    // paint for drawing components
    private var dotPaint: Paint = Paint()
    private var connectionPaint: Paint = Paint()

    // player colors
    private var playerColor: ColorStateList
    private var botColor: ColorStateList

    // connection width
    private var connectionWidth: Float

    // dot radius
    private var radius: Float

    // list of dots
    private var dots: MutableList<MutableList<Dot>> = mutableListOf()
    private var connections: MutableList<Connection> = mutableListOf()

    // tracks the current player on turn
    private var activePlayer = Player.USER

    init {
        background = ColorDrawable(Color.WHITE)

        context.theme.obtainStyledAttributes(attrs, R.styleable.Board, 0, 0)
            .apply {
                try {
                    // get the colors from the declarable-styles (custom attributes)
                    playerColor =
                        getColorStateList(R.styleable.Board_playerColor) ?: defaultPlayerColor
                    botColor = getColorStateList(R.styleable.Board_botColor) ?: defaultBotColor
                    // get the radius or set it to 36F by default
                    radius = getFloat(R.styleable.Board_radius, 36F)
                    // get the connection width or set it to 24F by default
                    connectionWidth = getFloat(R.styleable.Board_connectionWidth, 24F)
                } finally {
                    recycle()
                }
            }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN) {
            for(con in connections.filter { it.player == null }) {
                val isHorizontal = con.direction == ConnectionDirection.HORIZONTAL
                val startBounds = (if(isHorizontal) con.headDot.x else con.headDot.y) + radius
                val endBounds = (if(isHorizontal) con.tailDot.x else con.tailDot.y) - radius
                val upperWidthBounds = (if(isHorizontal) con.headDot.y else con.headDot.x) - radius
                val lowerWidthBounds = (if(isHorizontal) con.headDot.y else con.headDot.x) + radius

                val withinBounds = when(isHorizontal) {
                    true -> event.y in upperWidthBounds..lowerWidthBounds && event.x in startBounds..endBounds
                    else -> event.x in upperWidthBounds..lowerWidthBounds && event.y in startBounds..endBounds
                }

                if(withinBounds) {
                    con.player = activePlayer
                    activePlayer = if (activePlayer == Player.USER) Player.BOT else Player.USER
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // only instantiate objects if they aren't yet created
        // calling it onSizeChanged ensures that it calculates with the proper size
        if (dots.size == 0) initializeDotsAndConnections()
    }

    private fun initializeDotsAndConnections() {
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
            for (col in 0 until dots[row].size) {
                if(col + 1 < dots[row].size) {
                    connections.add(
                        Connection(
                            direction = ConnectionDirection.HORIZONTAL,
                            headDot = dots[row][col],
                            tailDot = dots[row][col + 1]
                        ))
                }
                if(row + 1 < dots.size) {
                    connections.add(
                        Connection(
                            direction = ConnectionDirection.VERTICAL,
                            headDot = dots[row][col],
                            tailDot = dots[row + 1][col]
                        ))
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // iterate through the connections and draw them
        connectionPaint.strokeWidth = connectionWidth
        connectionPaint.setShadowLayer(2F, 4F, 4F, Color.GRAY)
        connections.forEach { connection ->
            // set the color depending on who owns the connection
            connectionPaint.color = when (connection.player) {
                Player.USER -> playerColor.defaultColor
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
        // set the paint color to the color for the dots
        dotPaint.color = Color.BLACK
        // iterate through the list of dots and draw them
        dots.forEach { row ->
            row.forEach { dot ->
                canvas?.drawCircle(dot.x, dot.y, radius, dotPaint)
            }
        }
    }
}