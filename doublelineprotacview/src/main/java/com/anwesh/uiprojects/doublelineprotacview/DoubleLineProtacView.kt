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

fun Canvas.drawRotatedLine(i : Int, size : Float, scale: Float, paint : Paint) {
    save()
    translate(0f, -size)
    rotate(rotDeg * i.jsf() * scale)
    drawLine(0f, 0f, 0f, size * 2, paint)
    restore()
}

fun Canvas.drawHorizLine(i : Int, y : Float, size : Float, sc1 : Float, sc2 : Float, paint : Paint) {
    save()
    translate(-size * sc2 * i.jsf(), y)
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
    val xGap : Float = size * Math.sin(rotDeg * Math.PI / 180).toFloat()
    save()
    translate(w/2, gap * (i + 1))
    for (j in 0..(lines - 1)) {
        val scj1 : Float = sc1.divideScale(j, lines)
        val scj2 : Float = sc2.divideScale(j, lines)
        drawRotatedLine(j, yGap, scj1, paint)
        drawHorizLine(j, yGap, xGap, scj1, scj2, paint)
    }
    restore()
}

class DoubleLineProtacView(ctx : Context) : View(ctx) {
    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                
            }
        }
        return true
    }
}