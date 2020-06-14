package com.example.foreigncurrencyconversion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private val rubINusd = 74

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConvert.setOnClickListener {
            val textFrom = etFrom.text.toString()
            if (textFrom != "") {
                val textTo = (textFrom.toDouble() / rubINusd).toString()
                var formatter = BigDecimal(textTo)
                formatter = formatter.setScale(2, RoundingMode.HALF_UP)
                etTo.setText(formatter.toString())
            }
        }
    }
}