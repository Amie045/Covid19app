package com.ami.covid19app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ami.covid19app.adapter.AdapterCountry
import com.ami.covid19app.model.CountriesItem
import com.ami.covid19app.model.InfoNegara
import com.ami.covid19app.model.ResponseCountry
import com.ami.covid19app.network.ApiService
import com.ami.covid19app.network.RetrofitBuilder.retrofit
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progress_bar
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.list_country.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var ascending = true



    companion object {
        private lateinit var adapters: AdapterCountry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapters.filter.filter(newText)
                return false
            }

        })

        swipe_refresh.setOnRefreshListener {
            getNegara()
            swipe_refresh.isRefreshing = false
        }

        initializedView()
        getNegara()

    }

    private fun initializedView() {
        btn_sequence.setOnClickListener {
            sequenceWithoutInternet(ascending)
            ascending = !ascending

        }

    }

    private fun sequenceWithoutInternet(ascending: Boolean) {
        rv_country.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            if (ascending) {
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).stackFromEnd = true
                Toast.makeText(this@MainActivity, "Z - A", Toast.LENGTH_SHORT).show()
            } else {
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).stackFromEnd = true
                Toast.makeText(this@MainActivity, "A - Z", Toast.LENGTH_SHORT).show()
            }
            adapter = adapter
        }
    }

    private fun getNegara() {
        val api = retrofit.create(ApiService::class.java).also {
            it.getAllNegara().enqueue(object : Callback<ResponseCountry> {
                override fun onFailure(call: Call<ResponseCountry>, t: Throwable) {
                    progress_bar.visibility = View.GONE
                }

                override fun onResponse(call: Call<ResponseCountry>, response: retrofit2.Response<ResponseCountry>) {
                    if (response.isSuccessful) {
                        val getListDataCorona = response.body()!!.global
                        val formatter: java.text.NumberFormat = java.text.DecimalFormat("#,###")
                        txt_confirmed_globe.text =
                            formatter.format(getListDataCorona?.totalConfirmed?.toDouble())
                        txt_recovered_globe.text =
                            formatter.format(getListDataCorona?.totalRecovered?.toDouble())
                        txt_death_globe.text =
                            formatter.format(getListDataCorona?.totalDeaths?.toDouble())
                        rv_country.apply {
                            setHasFixedSize(true)
                            layoutManager = LinearLayoutManager(this@MainActivity)
                            progress_bar?.visibility = View.GONE
                            adapters = AdapterCountry(
                                response.body()!!.countries as ArrayList<CountriesItem>
                            ){negara -> itemClicked(negara)}

                            adapter = adapters
                        }

                    }else{
                        progress_bar?.visibility = View.GONE

                    }
                }
            })
        }





    }

    private fun itemClicked(negara: CountriesItem){
        val moveWithData = Intent(this@MainActivity, DetailActivity::class.java)
        moveWithData.putExtra(DetailActivity.EXTRA_COUNTRY, negara)
        startActivity(moveWithData)
    }
}