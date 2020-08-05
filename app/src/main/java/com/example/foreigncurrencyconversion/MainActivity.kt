package com.example.foreigncurrencyconversion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            when (parent.id) {
                spFrom.id -> DataManager.selection = DataManager.selection.copy(first = position)
                spTo.id -> DataManager.selection = DataManager.selection.copy(second = position)
            }
        }
    }

    private fun convert() {
        val textFrom = etFrom.text.toString()
        if (textFrom != "") {
            val from = spFrom.selectedItem.toString().substring(0, 3)
            val to = spTo.selectedItem.toString().substring(0, 3)
            val textTo =
                textFrom.toFloat() * DataManager.exchangeRates[to]!! / DataManager.exchangeRates[from]!!
            etTo.setText(String.format(Locale.getDefault(), "%.2f", textTo))
        } else {
            etTo.setText("")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ArrayAdapter.createFromResource(
            this,
            R.array.currencies,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            spFrom.adapter = it
            spTo.adapter = it
        }

        val dataFile = File(filesDir, "data.txt")
        DataManager.setup(this, dataFile)


        //          LISTENERS

        btnUpdate.setOnClickListener { DataManager.update() }

        btnConvert.setOnClickListener { convert() }

        etFrom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                convert()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSwapCur.setOnClickListener {
            DataManager.selection = Pair(DataManager.selection.second, DataManager.selection.first)
            spFrom.setSelection(DataManager.selection.first)
            spTo.setSelection(DataManager.selection.second)
            convert()
        }

        spFrom.onItemSelectedListener = this
        spTo.onItemSelectedListener = this
    }

    override fun onStop() {
        super.onStop()
        DataManager.saveData()
        Log.d("MyLog", "App is closed")
    }
}