package com.specknet.pdiotapp.doctor

import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class Spo2AndHeartRateHistoryActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "Spo2AndHeartRateHistoryActivity"
    }


    private lateinit var calendarView: CalendarView

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    lateinit var heartRateScatterChart: ScatterChart

    lateinit var heartRateScatterData: ScatterData

    lateinit var spo2DataSet: ScatterDataSet
    lateinit var heartRateDataSet: ScatterDataSet

    private var uid: String? = null

    private val database = Firebase.database

    private val gson = Gson()

    val dateFormat = SimpleDateFormat("HH:mm", Locale.CHINA)
    var selectDayHeartRateZeroTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spo2_and_heart_rate_history)


        uid = intent.getStringExtra("uid") ?: ""
        Log.i(TAG, "onCreate: uid = $uid")


        calendarView = findViewById(R.id.calendarView)
        heartRateScatterChart = findViewById(R.id.heart_rate_chart)

        initCalenderView()

        initHeartRateChartsStyle()

    }

    private fun initHeartRateChartsStyle() {

        heartRateScatterChart.description.isEnabled = false


        heartRateScatterChart.isScaleXEnabled = true
        heartRateScatterChart.isScaleYEnabled = false

        val xAxis = heartRateScatterChart.xAxis

        xAxis.setDrawGridLines(false)

        xAxis.textSize = 12f
        xAxis.textColor = R.color.black

        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // x轴显示的值自定义
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                Log.i(TAG, "getAxisLabel: $value")
                val toLong = value.toLong()
                return dateFormat.format(toLong + selectDayHeartRateZeroTime)
            }
        }

        val yAxis = heartRateScatterChart.axisLeft
        yAxis.textSize = 12f
        yAxis.textColor = R.color.black
        yAxis.setDrawGridLines(false)

        val axisRight = heartRateScatterChart.axisRight
        axisRight.isEnabled = false

    }

    private fun mockList(): MutableList<Spo2AndHeartRateBean> {
        val result = mutableListOf<Spo2AndHeartRateBean>()

        val timestampStart = sdf.parse(sdf.format(Date())).time + (1000 * 60 * 60 * 12)
        val timestampEnd =
            sdf.parse(sdf.format(Date())).time + (1000 * 60 * 60 * 24) - (1000 * 60 * 60 * 11)

        selectDayHeartRateZeroTime = timestampStart


        var timestamp = timestampStart
        while (timestamp < (timestampEnd)) {
            val bean = Spo2AndHeartRateBean()

            bean.timestamp = timestamp - selectDayHeartRateZeroTime
            bean.setHeartRate(Random.nextInt(80, 101))
            bean.spo2 = Random.nextDouble(80.0, 100.0)
            result.add(bean)

            timestamp += 5000
        }

        return result

    }


    private fun setupHeartRateChartsData(resultBeanList: List<Spo2AndHeartRateBean>) {

        val spo2List = ArrayList<Entry>()
        val heartRateList = ArrayList<Entry>()

        spo2DataSet = ScatterDataSet(spo2List, "spo2")
        heartRateDataSet = ScatterDataSet(heartRateList, "heartRate")

        resultBeanList.forEach {
            spo2DataSet.addEntry(
                Entry(
                    it.timestamp.toFloat(), it.spo2.toFloat()
                )
            )
            heartRateDataSet.addEntry(
                Entry(
                    it.timestamp.toFloat(), it.heartRate.toFloat()
                )
            )
        }

        spo2DataSet.color = ContextCompat.getColor(
            this, R.color.red
        )
        heartRateDataSet.color = ContextCompat.getColor(
            this, R.color.green
        )

        val dataSetsRes = ArrayList<IScatterDataSet>()
        dataSetsRes.add(spo2DataSet)
        dataSetsRes.add(heartRateDataSet)

        heartRateScatterData = ScatterData(dataSetsRes)
        heartRateScatterChart.data = heartRateScatterData


        // 图表绘制完默认展示多少个点
        if (resultBeanList.isNotEmpty() && resultBeanList.size >= 40) {
            heartRateScatterChart.setVisibleXRangeMaximum(resultBeanList[39].timestamp.toFloat())
        }

        heartRateScatterChart.invalidate()


    }

    override fun onResume() {
        super.onResume()
        // 把当前时间戳转成 yyyy-MM-dd
        val dateString = sdf.format(Date())
        // yyyy-MM-dd 转成时间戳
        val startTime = sdf.parse(dateString).time
        initHeartRateChartData(startTime)

        // 本地代码mock数据
//        setupChartsData(mockList())
    }


    /**
     * 日期
     * yyyy-MM-dd
     *
     * 2022-11-18
     */
    private fun initHeartRateChartData(startTime: Long) {

        Log.d(TAG, "initLineChartData() called with: startTime = $startTime")

        val format = sdf.format(Date(startTime))
        Log.d(TAG, "initLineChartData() called with: format = $format")

        val sdfyyyyMMdd = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
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
                // 1697461588000 第一个点的时间戳
                // 1697461593000 第2个点的时间戳 1697461593000 - 1697461588000 = 5000
                // 1697461598000 第3个点的时间戳 .. = 5000
                //（0, 85） (5000,83) (10000, 81) ..
                if (resultBeanList.isNotEmpty()) {
                    resultBeanList.sortedBy { it.timestamp }
                    selectDayHeartRateZeroTime = resultBeanList[0].timestamp // 这一天中第一个点的时间

                    resultBeanList.forEach { bean ->
                        bean.timestamp = bean.timestamp - selectDayHeartRateZeroTime
                    }

                    setupHeartRateChartsData(resultBeanList)
                }

            } else {
                setupHeartRateChartsData(listOf())
            }
        }.addOnFailureListener {
            Log.i(TAG, "initLineChartData: onFailure")
        }

    }


    private fun initCalenderView() {

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            Toast.makeText(this, "$year-${month + 1}-$dayOfMonth", Toast.LENGTH_SHORT).show()
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // 重新获取选中日期的数据
            initHeartRateChartData(calendar.timeInMillis)

        }

    }

}