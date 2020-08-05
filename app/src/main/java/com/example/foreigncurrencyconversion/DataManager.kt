package com.example.foreigncurrencyconversion

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*

@Suppress("BlockingMethodInNonBlockingContext", "UNCHECKED_CAST")
object DataManager {

    var selection: Pair<Int, Int> = Pair(1, 0)
    private lateinit var date: String
    lateinit var exchangeRates: HashMap<String, Float>

    private lateinit var context: Context
    private lateinit var dataFile: File

    private const val TAG = "MyLog"

    private lateinit var tvLastUpdated: TextView
    private lateinit var spFrom: Spinner
    private lateinit var spTo: Spinner
    private lateinit var btnUpd: Button
    private lateinit var progBarUpd: ProgressBar


    fun setup(context: Context, dataFile: File) {
        try {
            //  Initializing fields
            this.context = context as Activity
            this.dataFile = dataFile
            tvLastUpdated = context.tvLastUpdated
            spFrom = context.spFrom
            spTo = context.spTo
            btnUpd = context.btnUpdate
            progBarUpd = context.progressBarUpd

            //  Getting data from internal storage
            val ois = ObjectInputStream(FileInputStream(this.dataFile))
            selection = ois.readObject() as Pair<Int, Int>
            date = ois.readObject() as String
            exchangeRates = ois.readObject() as HashMap<String, Float>
            ois.close()

            //  Views' settings
            tvLastUpdated.text = context.resources.getString(R.string.upd, date)
            spFrom.setSelection(selection.first)
            spTo.setSelection(selection.second)
            Log.d(TAG, "Data is loaded from memory")
        } catch (e: IOException) {
            Log.d(TAG, "Input stream failed, creating the file.")
            dataFile.createNewFile()
            update()
        }
    }

    fun update() {
        startProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = parseData(getDataFromAPI())
                val newDate = data!!.component1()
                val newRates = data.component2()
                date = newDate
                exchangeRates = newRates
                setNewDateOnTextView(date)
                Log.d(TAG, "Updated successfully")
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Couldn't update exchange rates.\nCheck your connection to internet.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    Log.e(TAG, "update() function. Request failed")
                }
            } catch (e: NullPointerException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "The rates are already latest", Toast.LENGTH_SHORT)
                        .show()
                    Log.d(TAG, "update() function. NPE - data is latest")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    stopProgress()
                }
            }
        }
    }

    private suspend fun setNewDateOnTextView(str: String) {
        withContext(Dispatchers.Main) {
            tvLastUpdated.text = context.resources.getString(R.string.upd, str)
        }
    }

    private suspend fun getDataFromAPI(): String? {
        val api = CoroutineScope(Dispatchers.IO).async {
            val requestClient = OkHttpClient()
            val url = "https://api.exchangeratesapi.io/latest"
            val request = Request.Builder().url(url).build()
            Log.d(TAG, "Getting data from API")
            requestClient.newCall(request).execute().use {
                if (!it.isSuccessful) throw IOException("Unexpected code $it")
                Log.d(TAG, "Response is successful")
                it.body()?.string()
            }
        }
        return api.await()
    }

    private fun parseData(currency: String?): Pair<String, HashMap<String, Float>>? {
        if (currency == null) return null
        val date: String =
            currency.substring(currency.indexOf("date") + 7, currency.indexOf("date") + 17)
        if (this.date == date) {
            Log.d(TAG, "The data is latest")
            return null
        }
        val exchangeRatesString: String = currency.substring(
            currency.indexOf("{", 2) + 1,
            currency.indexOf("}")
        )
        val map = exchangeRatesString.split(",").associateTo(HashMap()) {
            val (left, right) = it.split(":")
            left.replace("\"", "") to right.toFloat()
        }
        map["EUR"] = 1.0F
        Log.d(TAG, "The data is parsed")
        return Pair(date, map)
    }

    private fun startProgress() {
        btnUpd.visibility = Button.INVISIBLE
        progBarUpd.visibility = ProgressBar.VISIBLE
    }

    private fun stopProgress() {
        btnUpd.visibility = Button.VISIBLE
        progBarUpd.visibility = ProgressBar.INVISIBLE
    }

    fun saveData() {
        try {
            val oos = ObjectOutputStream(FileOutputStream(dataFile))
            oos.writeObject(selection)
            oos.writeObject(date)
            oos.writeObject(exchangeRates)
            oos.flush()
            oos.close()
            Log.d(TAG, "Data is saved to internal storage")
        } catch (e: IOException) {
            Log.e(TAG, "Couldn't save data to internal storage")
        }
    }
}