package com.anwesh.uiprojects.doublelineprotacview

/**
 * Created by anweshmishra on 25/01/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val lines : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val sizeFactor : Float = 2.7f
val strokeFactor : Int = 90
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val rotDeg : Float = 45f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * scGap * dir
fun Int.jsf() : Float = 1f - 2 * this

fun Canvas.drawRotatedLine(i : Int, y : Float, size : Float, scale: Float, paint : Paint) {
    save()
    translate(0f, -y)
    rotate(rotDeg * i.jsf() * scale)
    drawLine(0f, 0f, 0f, size, paint)
    restore()
}

fun Canvas.drawHorizLine(i : Int, oy : Float, y : Float, size : Float, sc1 : Float, sc2 : Float, paint : Paint) {
    save()
    translate(-size * sc2 * i.jsf(), oy + (y - oy) * sc1)
    drawLine(0f, 0f, -size * i.jsf() * sc1, 0f, paint)
    restore()
}

fun Canvas.drawDLPNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val yGap : Float = size * Math.cos(rotDeg * Math.PI / 180).toFloat()
    val xGap : Float = 2 * size * Math.sin(rotDeg * Math.PI / 180).toFloat()
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w/2, gap * (i + 1))
    for (j in 0..(lines - 1)) {
        val scj1 : Float = sc1.divideScale(j, lines)
        val scj2 : Float = sc2.divideScale(j, lines)
        drawRotatedLine(j, yGap, 2 * size, scj1, paint)
        drawHorizLine(j, size, yGap, xGap, scj1, scj2, paint)
    }
    restore()
}

class DoubleLineProtacView(ctx : Context) : View(ctx) {
    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, lines)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class DLPNode(var i : Int, val state : State = State()) {
        private var next : DLPNode? = null
        private var prev : DLPNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = DLPNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawDLPNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : DLPNode {
            var curr : DLPNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class DoubleLineProtac(var i : Int) {

        private val root : DLPNode = DLPNode(0)
        private var curr : DLPNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : DoubleLineProtacView) {

        private val animator : Animator = Animator(view)
        private var dlp : DoubleLineProtac = DoubleLineProtac(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            dlp.draw(canvas, paint)
            animator.animate {
                dlp.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            dlp.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : DoubleLineProtacView {
            val view : DoubleLineProtacView = DoubleLineProtacView(activity)
            activity.setContentView(view)
            return view
        }
    }
}