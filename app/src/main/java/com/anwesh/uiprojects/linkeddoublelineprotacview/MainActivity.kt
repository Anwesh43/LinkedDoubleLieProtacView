package com.anwesh.uiprojects.linkeddoublelineprotacview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.doublelineprotacview.DoubleLineProtacView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DoubleLineProtacView.create(this)
    }
}
