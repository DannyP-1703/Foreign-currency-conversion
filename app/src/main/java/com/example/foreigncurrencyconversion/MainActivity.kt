package com.example.foreigncurrencyconversion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
//import java.math.BigDecimal
//import java.math.RoundingMode
import java.util.*

class MainActivity : AppCompatActivity() {

    private val exchangeRates = mapOf(     //relative to RUB
        "RUB" to 1.0,
        "USD" to 69.1219,
        "EUR" to 78.5225,
        "GBP" to 87.6673,
        "UAH" to 25.9902
    )

    private fun convert(v: View) {
        val textFrom = etFrom.text.toString()
        if (textFrom != "") {
            val textTo = textFrom.toFloat() * exchangeRates[spFrom.selectedItem]!! / exchangeRates[spTo.selectedItem]!!
//                var formatter = BigDecimal(textTo)
//                formatter = formatter.setScale(2, RoundingMode.HALF_UP)
//                etTo.setText(formatter.toString())
            etTo.setText(String.format(Locale.getDefault(),"%.2f", textTo))
        } else {
            etTo.setText("")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spFrom.setSelection(1)
        spTo.setSelection(0)

        btnConvert.setOnClickListener(this::convert)

        btnSwapCur.setOnClickListener {
            val t = spFrom.selectedItemPosition
            spFrom.setSelection(spTo.selectedItemPosition).also { spTo.setSelection(t) }
            convert(it)
        }
    }
}