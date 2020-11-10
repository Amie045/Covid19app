package com.ami.covid19app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ami.covid19app.model.CountriesItem
import com.ami.covid19app.model.InfoNegara
import com.ami.covid19app.network.ApiService
import com.ami.covid19app.network.InfoService
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.list_country.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class DetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_COUNTRY = "EXTRA COUNTRY"
        lateinit var simpanDataNegara: String
        lateinit var simpanDataFlag: String
    }


    private val sharedPrefFile = "kotlinssharedpreference"
    private lateinit var sharedPreference: SharedPreferences
    private var daycase = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        sharedPreference = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreference.edit()

        val data = intent.getParcelableExtra<CountriesItem>(EXTRA_COUNTRY)
        val formatter: NumberFormat = DecimalFormat("#,###")

        data.let {

            txt_name_country.text = data!!.country
            txt_latest_update.text = data.date
            total_confirm_currently.text = formatter.format(data.totalConfirmed?.toDouble())
            total_recovered_currently.text = formatter.format(data.totalRecovered?.toDouble())
            total_death_currently.text = formatter.format(data.totalDeaths?.toDouble())
            new_confirm_currently.text = formatter.format(data.newConfirmed?.toDouble())
            new_death_currently.text = formatter.format(data.newDeaths?.toDouble())
            new_recovered_currently.text = formatter.format(data.newRecovered?.toDouble())



            editor.putString(data.country, data.country)
            editor.apply() //nyimpen data yg di taro
            editor.commit() // untuk menerapkan shared
            val simpanNegara = sharedPreference.getString(data.country, data.country)
            val simpanFlag = sharedPreference.getString(data.countryCode, data.countryCode)
            simpanDataNegara = simpanNegara.toString()
            simpanDataFlag = simpanFlag.toString() + "/flat/64.png"

            if (simpanFlag != null) {
                Glide.with(this).load("https://www.countryflags.io/$simpanDataFlag")
                    .into(img_flag)

            } else {
                Toast.makeText(this, "Gambar Tidak valid", Toast.LENGTH_SHORT).show()
            }

            getChart()

        }


    }

    private fun getChart() {
        val okHttp = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.covid19api.com/dayone/country/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(InfoService::class.java)
        api.getInfoService(simpanDataNegara).enqueue(object : Callback<List<InfoNegara>> {
            override fun onFailure(call: Call<List<InfoNegara>>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error", Toast.LENGTH_SHORT).show()
            }

            @SuppressLint("SimpleDateFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<InfoNegara>>,
                response: Response<List<InfoNegara>>
            ) {
                val getListDataCorona: List<InfoNegara> = response.body()!!
                if (response.isSuccessful) {

                    val barEntries: ArrayList<BarEntry> = ArrayList()
                    val barEntries2: ArrayList<BarEntry> = ArrayList()
                    val barEntries3: ArrayList<BarEntry> = ArrayList()
                    val barEntries4: ArrayList<BarEntry> = ArrayList()
                    var i = 0

                    while (i < getListDataCorona.size) {
                        for (s in getListDataCorona) {
                            var barEntry = BarEntry(i.toFloat(), s.Confirmed?.toFloat() ?: 0f)
                            var barEntry2 = BarEntry(i.toFloat(), s.Recovered?.toFloat() ?: 0f)
                            var barEntry3 = BarEntry(i.toFloat(), s.Active?.toFloat() ?: 0f)
                            var barEntry4 = BarEntry(i.toFloat(), s.Death?.toFloat() ?: 0f)

                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'")
                            val outputFormat = SimpleDateFormat("dd-MM-yyyy")
                            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                            val date: Date = inputFormat.parse(s.Date!!)
                            val formatteDate: String = outputFormat.format(date)

                            daycase.add(formatteDate)

                            barEntries.add(barEntry)
                            barEntries2.add(barEntry2)
                            barEntries3.add(barEntry3)
                            barEntries4.add(barEntry4)

                            i++


                        }
                        val xAxis: XAxis = barchartView.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(daycase)
                        barchartView.axisLeft.axisMinimum = 0f
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.setCenterAxisLabels(true)
                        xAxis.isGranularityEnabled = true


                        val barDataSet = BarDataSet(barEntries, "Confirmed")
                        val barDataSet2 = BarDataSet(barEntries2, "Deaths")
                        val barDataSet3 = BarDataSet(barEntries3, "Recovered")
                        val barDataSet4 = BarDataSet(barEntries4, "Active")

                        barDataSet.setColor(Color.parseColor("#F44336"))
                        barDataSet2.setColor(Color.parseColor("#FFEB3B"))
                        barDataSet3.setColor(Color.parseColor("#03DAC5"))
                        barDataSet4.setColor(Color.parseColor("#2196F3"))

                        val data = BarData(barDataSet, barDataSet2, barDataSet3, barDataSet4)
                        barchartView.data = data

                        val barSpace = 0.02f
                        val groupSpace = 0.3f
                        val groupCount = 4f

                        data.barWidth = 0.15f

                        barchartView.invalidate()
                        barchartView.setNoDataTextColor(R.color.black)
                        barchartView.setTouchEnabled(true)
                        barchartView.description.isEnabled = false
                        barchartView.xAxis.axisMinimum = 0f
                        barchartView.setVisibleXRangeMaximum(
                            0f + barchartView.barData.getGroupWidth(
                                groupSpace,
                                barSpace
                            ) * groupCount
                        )
                        barchartView.groupBars(0f, groupSpace, barSpace)
                    }


                }
            }


        })


    }
}