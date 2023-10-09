package com.specknet.pdiotapp.doctor

import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class Spo2AndHeartRateHistoryActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "Spo2AndHeartRateHistoryActivity"
    }


    private lateinit var calendarView: CalendarView

    val sdf = SimpleDateFormat("yyyy-MM-dd")

    private val sdfyyyyMMdd = SimpleDateFormat("yyyyMMdd")

    lateinit var thingy1Chart: LineChart

    lateinit var allThingy1Data: LineData

    lateinit var spo2DataSet: LineDataSet
    lateinit var heartRateDataSet: LineDataSet

    private var uid: String? = null

    private val database = Firebase.database

    private val gson = Gson()

    val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spo2_and_heart_rate_history)


        uid = intent.getStringExtra("uid") ?: ""
        Log.i(TAG, "onCreate: uid = $uid")


        calendarView = findViewById(R.id.calendarView)
        thingy1Chart = findViewById(R.id.thingy1_chart)

        initCalenderView()

        setupCharts(mutableListOf())

    }


    fun setupCharts(resultBeanList: List<Spo2AndHeartRateBean>) {

        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()

        spo2DataSet = LineDataSet(entries_res_accel_x, "spo2")
        heartRateDataSet = LineDataSet(entries_res_accel_y, "heartRate")

        // 这里是开发调试阶段的假数据
//        spo2DataSet.addEntry(Entry(0.0f, 15.0f))
//        spo2DataSet.addEntry(Entry(1.0f, 10.0f))
//        spo2DataSet.addEntry(Entry(2.0f, 20.0f))
//        spo2DataSet.addEntry(Entry(3.0f, 17.0f))
//        spo2DataSet.addEntry(Entry(4.0f, 26.0f))
//        spo2DataSet.addEntry(Entry(5.0f, 18.0f))
//
//
//        heartRateDataSet.addEntry(Entry(0.0f, 35.0f))
//        heartRateDataSet.addEntry(Entry(1.0f, 40.0f))
//        heartRateDataSet.addEntry(Entry(2.0f, 60.0f))
//        heartRateDataSet.addEntry(Entry(3.0f, 57.0f))
//        heartRateDataSet.addEntry(Entry(4.0f, 86.0f))
//        heartRateDataSet.addEntry(Entry(5.0f, 18.0f))

        resultBeanList.forEachIndexed { index, spo2AndHeartRateBean ->

            val heartRate = spo2AndHeartRateBean.heartRate
            val spo2 = spo2AndHeartRateBean.spo2
            val timestamp = spo2AndHeartRateBean.timestamp


            calendar.timeInMillis = timestamp

            Log.i(TAG, "setupCharts: ${calendar.get(Calendar.HOUR_OF_DAY).toFloat()}")

            // timestamp需要转换成 0 - 24 这个范围的数字

            spo2DataSet.addEntry(Entry(calendar.get(Calendar.HOUR_OF_DAY).toFloat(), spo2.toFloat()))
            heartRateDataSet.addEntry(Entry(calendar.get(Calendar.HOUR_OF_DAY).toFloat(), heartRate.toFloat()))

        }

        spo2DataSet.setDrawCircles(false)
        heartRateDataSet.setDrawCircles(false)

        spo2DataSet.setColor(
            ContextCompat.getColor(
                this, // !! 告诉编译器 activity强制非空 肯定不为空
                R.color.red
            )
        )
        heartRateDataSet.setColor(
            ContextCompat.getColor(
                this, R.color.green
            )
        )

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(spo2DataSet)
        dataSetsRes.add(heartRateDataSet)

        allThingy1Data = LineData(dataSetsRes)
        thingy1Chart.data = allThingy1Data
        thingy1Chart.invalidate()


    }

    override fun onResume() {
        super.onResume()

        // 把当前时间戳转成 yyyy-MM-dd
        val dateString = sdf.format(Date())

        // yyyy-MM-dd 转成时间戳
        val startTime = sdf.parse(dateString).time

        initLineChartData(startTime)
    }


    /**
     * 日期
     * yyyy-MM-dd
     *
     * 2022-11-18
     */
    private fun initLineChartData(startTime: Long) {

        Log.d(TAG, "initLineChartData() called with: startTime = $startTime")

        val format = sdf.format(Date(startTime))
        Log.d(TAG, "initLineChartData() called with: format = $format")


        val path = "${uid}/date${sdfyyyyMMdd.format(Date(startTime))}/spo2AndHeartRate"
        Log.i(TAG, "initLineChartData: path = ${path}")
        val myRef = database.getReference(path)
        myRef.get().addOnSuccessListener {
            // 解析数据
            Log.i(TAG, "initLineChartData: ${gson.toJson(it.value)}")

            if (it.value != null) {
                val resultBeanList = mutableListOf<Spo2AndHeartRateBean>()

                (it.value as List<Map<String, Any>>).forEach {
                    val jsonString = gson.toJson(it)
                    val resultBean = gson.fromJson(jsonString, Spo2AndHeartRateBean::class.java)
                    resultBeanList.add(resultBean)
                }

                setupCharts(resultBeanList)
            } else {
                setupCharts(listOf())
            }
        }.addOnFailureListener {
            Log.i(TAG, "initLineChartData: onFailure")
        }

    }


    private fun initCalenderView() {

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            Toast.makeText(this, "$year-$month-$dayOfMonth", Toast.LENGTH_SHORT).show()
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // 重新获取选中日期的数据
            initLineChartData(calendar.timeInMillis)

        }

    }

}