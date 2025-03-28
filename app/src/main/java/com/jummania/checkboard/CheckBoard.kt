package com.jummania.checkboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt


/**
 * Created by Jummania on 24/3/25.
 * Email: sharifuddinjumman@gmail.com
 * Dhaka, Bangladesh.
 */
class CheckBoard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val chessFont = ResourcesCompat.getFont(context, R.font.symbola)
    private val paint = Paint()

    private val shadowPaint = Paint().apply {
        style = Paint.Style.FILL
        typeface = chessFont
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private var darkColor = "#739654".toColorInt()
    private var lightColor = "#ebeed3".toColorInt()

    private var touchedX: Float = 0f
    private var touchedY: Float = 0f


    private var isSelected = false
    private var selectedRowNumber = -1
    private var selectedPiece = ""

    private var isInvalidate = false

    private var isWhiteTurn = true

    private var isWhitePawnReachedEnd = false
    private var isBlackPawnReachedEnd = false

    val bitmap = getBitmapFromVectorDrawable(context, R.drawable.transform)


    //  private var needToMove = false


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val min = minOf(height, width) - 55
        val x = (width - min) / 2f
        val y = (height - min) / 2f
        val size = min / 8

        var count = 0
        var rowNumber = 0

        var selected = false

        for (s in 0 until 8) {
            val top = y + (size * s)
            val bottom = top + size

            for (i in 0 until 8) {

                rowNumber++

                val left = x + (size * i)
                val right = left + size

                if (touchedX in left..right && touchedY in top..bottom) {

                    if (selectedRowNumber == rowNumber && isSelected) {
                        isSelected = false
                        selectedRowNumber = -1
                        selectedPiece = ""
                    } else if (isSelected && selectedPiece.isNotEmpty()) {
                        swapTo(selectedRowNumber - 1, rowNumber - 1)
                        isSelected = false
                        selectedRowNumber = -1
                        isInvalidate = true
                        selectedPiece = ""
                        invalidate()
                    } else if (!isInvalidate) {
                        isSelected = true
                        selectedRowNumber = rowNumber
                        selectedPiece = chessBoard[rowNumber - 1]?.symbol ?: ""
                    }

                    selected = true
                }

                paint.style = Paint.Style.FILL
                paint.color = if (count % 2 == 0) darkColor else lightColor
                canvas.drawRect(left, top, right, bottom, paint)


                if (selectedRowNumber == rowNumber && selected && isSelected) {
                    paint.style = Paint.Style.STROKE
                    val padding = size * 0.05f
                    paint.strokeWidth = padding
                    paint.color = Color.RED

                    canvas.drawRect(
                        left + padding, top + padding, right - padding, bottom - padding, paint
                    )
                }

                fun drawPieces(text: String, chessColor: Int) {
                    drawText(
                        canvas,
                        chessColor,
                        size * 0.6f,
                        text,
                        left + (size / 2),
                        top + (size / 1.5f)
                    )
                }

                val chess = chessBoard[rowNumber - 1] ?: emptyChess
                drawPieces(chess.symbol, chess.color)

                count += 1
            }
            count += 1
        }

        if (!selected) isSelected = false

        // Draw the border
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        val startX = x + min
        val startY = y + min

        canvas.drawRect(x, y, startX, startY, paint)

        drawText(canvas, Color.BLACK, 100f, "Chess"/*"♔♕♖♗♘♙ ♚♛♜♝♞♟"*/, width / 2f, y - size)

        val halfSize = size / 2f

        paint.color = Color.RED
        canvas.drawCircle(
            width / 2f, if (isWhiteTurn) y - halfSize else startY + halfSize, 22f, paint
        )

        canvas.drawBitmap(bitmap, x + halfSize / 2, y - halfSize, paint)

    }

    private fun drawText(
        canvas: Canvas, color: Int, textSize: Float, text: String, x: Float, y: Float
    ) {
        shadowPaint.textSize = textSize
        shadowPaint.setShadowLayer(
            15f, 0f, 0f, if (color == whiteColor) blackColor else whiteColor
        ) // Shadow properties
        shadowPaint.color = color
        canvas.drawText(text, x, y, shadowPaint)
    }

    fun onTouch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            touchedX = event.x
            touchedY = event.y
            invalidate()
            isInvalidate = false
        }
    }

    private fun swapTo(fromIndex: Int, toIndex: Int) {
        if (fromIndex in chessBoard.indices && toIndex in chessBoard.indices) {
            val fromPiece = chessBoard[fromIndex] ?: return

            if (fromPiece.isEmpty()) return

            val toPiece = chessBoard[toIndex]

            val isFromWhitePiece = fromPiece.isWhite()

            if (isFromWhitePiece && !isWhiteTurn) {
                message("It's not your turn!")
                return
            } else if (!isFromWhitePiece && isWhiteTurn) {
                message("It's not your turn!")
                return
            }

            fun getSequence(
                sequence: Int, horizontal: Boolean, vertical: Boolean, diagonal: Boolean
            ): List<Int> {
                return getSequence(
                    fromIndex, sequence, isFromWhitePiece, horizontal, vertical, diagonal
                )
            }

            if (fromPiece.isKing()) {
                val sequence = getSequence(2, true, true, false)
                if (toIndex !in sequence) {
                    message("The King can only move one square in any direction.")
                    return
                }
            } else if (fromPiece.isRook()) {
                val sequence = getSequence(8, true, true, false)
                if (toIndex !in sequence) {
                    message("The Rook can only move one square in any direction.")
                    return
                }
            } else if (fromPiece.isBishop()) {
                val sequence = getSequence(8, false, false, true)
                if (toIndex !in sequence) {
                    message("The Bishop can only move one square diagonally.")
                    return
                }
            } else if (fromPiece.isQueen()) {
                val sequence = getSequence(8, true, true, true)
                if (toIndex !in sequence) {
                    message("The Queen can only move one square in any direction.")
                    return
                }
            } else if (fromPiece.isKnight()) {
                val sequence = getKnightSequence(fromIndex, isFromWhitePiece)
                if (toIndex !in sequence) {
                    message("The Knight can only move in an L shape.")
                    return
                }
            } else if (fromPiece.isPawn()) {
                Log.d("Jjj", "swapTo: true")
                val sequence = getPawnSequence(fromIndex, isFromWhitePiece)
                if (toIndex !in sequence) {
                    message("The Pawn can only move one square forward.")
                    return
                }
            } else return


            val isToWhitePiece = toPiece.isWhite()

            val isFromBlackPiece = fromPiece.isBlack()
            val isToBlackPiece = toPiece.isBlack()

            if (isFromWhitePiece && isToWhitePiece) {
                message("White cannot move to white")
            } else if (isFromBlackPiece && isToBlackPiece) {
                message("Black cannot move to black")
            } else if (fromPiece.isNotEmpty()) {
                chessBoard[toIndex] = fromPiece
                chessBoard[fromIndex] = null

                if (toPiece.isNotEmpty()) {
                    message(
                        String.format(
                            "%s attacks and captures %s!", fromPiece.symbol, toPiece?.symbol
                        )
                    )
                }

            }

            for (i in 0 until 8) {
                isWhitePawnReachedEnd = isWhitePawnReachedEnd || chessBoard[56 + i].isWhitePawn()
                isBlackPawnReachedEnd = isBlackPawnReachedEnd || chessBoard[i].isBlackPawn()
                if (isWhitePawnReachedEnd && isBlackPawnReachedEnd) break
            }

            isWhiteTurn = !isFromWhitePiece
        }
    }

    private fun message(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getSequence(
        position: Int,
        sequence: Int,
        isWhitePiece: Boolean,
        horizontal: Boolean,
        vertical: Boolean,
        diagonal: Boolean
    ): List<Int> {
        val list = mutableListOf<Int>()

        var left = horizontal
        var right = horizontal
        var up = vertical
        var down = vertical
        var upLeft = diagonal
        var upRight = diagonal
        var downLeft = diagonal
        var downRight = diagonal

        val leftLimit = position - (position % 8)
        val rightLimit = leftLimit + 8

        fun add(pos: Int): Boolean {
            return add(list, pos, isWhitePiece)
        }

        for (i in 1 until sequence) {

            if (left) {
                val pos = position - 1 * i
                left = if (pos >= leftLimit) add(pos)
                else false

            }

            if (right) {
                val pos = position + 1 * i
                right = if (pos <= rightLimit) add(pos)
                else false
            }

            if (up) up = add(position + 8 * i)

            if (down) down = add(position - 8 * i)

            if (upLeft) upLeft = add(position + 7 * i)

            if (upRight) upRight = add(position + 9 * i)

            if (downLeft) downLeft = add(position - 9 * i)

            if (downRight) downRight = add(position - 7 * i)
        }
        return list
    }

    private fun add(list: MutableList<Int>, pos: Int, isWhitePiece: Boolean): Boolean {
        if (pos !in chessBoard.indices) return false
        val piece = chessBoard[pos]
        return if (piece.isEmpty()) {
            list.add(pos)
        } else if (piece.isEnemy(isWhitePiece)) {
            !list.add(pos)
        } else false
    }

    private fun getKnightSequence(position: Int, isWhitePiece: Boolean): List<Int> {
        val list = mutableListOf<Int>()

        fun add(pos: Int) {
            add(list, pos, isWhitePiece)
        }

        add(position + 6)
        add(position - 6)
        add(position + 10)
        add(position - 10)
        add(position + 15)
        add(position - 15)
        add(position + 17)
        add(position - 17)

        return list
    }

    private fun getPawnSequence(position: Int, isWhitePiece: Boolean): List<Int> {
        val list = mutableListOf<Int>()

        fun add(pos: Int) {
            add(list, pos, isWhitePiece)
        }

        var animePosition = if (isWhitePiece) position + 7 else position - 7
        if (animePosition in chessBoard.indices && chessBoard[animePosition].isEnemy(isWhitePiece)) {
            add(animePosition)
        }

        animePosition = if (isWhitePiece) position + 8 else position - 8
        if (animePosition !in chessBoard.indices || !chessBoard[animePosition].isEnemy(isWhitePiece)) add(
            if (isWhitePiece) position + 8 else position - 8
        )

        animePosition = if (isWhitePiece) position + 9 else position - 9
        if (animePosition in chessBoard.indices && chessBoard[animePosition].isEnemy(isWhitePiece)) {
            add(animePosition)
        }

        if (position in 8..16 || position in 48..56) {
            add(if (isWhitePiece) position + 16 else position - 16)
        }

        return list
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return createBitmap(1, 1)

        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }


}