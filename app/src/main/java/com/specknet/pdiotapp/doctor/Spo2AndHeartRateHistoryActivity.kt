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
import kotlin.random.Random

class Spo2AndHeartRateHistoryActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "Spo2AndHeartRateHistoryActivity"
        private const val SLOT_SIZE = 24
    }


    private lateinit var calendarView: CalendarView

    val sdf = SimpleDateFormat("yyyy-MM-dd")

    private val sdfyyyyMMdd = SimpleDateFormat("yyyyMMdd")

    lateinit var lineChart: ScatterChart

    lateinit var scatterData: ScatterData

    lateinit var spo2DataSet: ScatterDataSet
    lateinit var heartRateDataSet: ScatterDataSet

    private var uid: String? = null

    private val database = Firebase.database

    private val gson = Gson()

    val dateFormat = SimpleDateFormat("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spo2_and_heart_rate_history)


        uid = intent.getStringExtra("uid") ?: ""
        Log.i(TAG, "onCreate: uid = $uid")


        calendarView = findViewById(R.id.calendarView)
        lineChart = findViewById(R.id.line_chart)

        initCalenderView()

        initChartsStyle()

        setupChartsData(mockList())

    }

    private fun mockList(): MutableList<Spo2AndHeartRateBean> {
        val result = mutableListOf<Spo2AndHeartRateBean>()

        val sdf = SimpleDateFormat("yyyy-MM-dd")

        val timestampStart = sdf.parse(sdf.format(Date())).time/* + (1000 * 60 * 60 * 12)*/
        val timestampEnd =
            sdf.parse(sdf.format(Date())).time + (1000 * 60 * 60 * 24)/* - (1000 * 60 * 60 * 11)*/

        var timestamp = timestampStart
        while (timestamp < (timestampEnd)) {
            val bean = Spo2AndHeartRateBean()
            bean.timestamp = timestamp
            bean.setHeartRate(Random.nextInt(80, 101))
            bean.spo2 = Random.nextDouble(80.0, 100.0)
            result.add(bean)

            timestamp += 50000
        }

        return result

    }

    private fun initChartsStyle() {

        lineChart.description.isEnabled = false


        lineChart.isScaleXEnabled = true

        val xAxis = lineChart.xAxis

        xAxis.mLabelWidth = 2

        xAxis.setDrawGridLines(false)

        xAxis.textSize = 12f
        xAxis.textColor = R.color.black

        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                Log.i(TAG, "getAxisLabel: $value")
                val toLong = value.toLong()
                return dateFormat.format(toLong)
            }
        }

        val yAxis = lineChart.axisLeft
        yAxis.textSize = 12f
        yAxis.textColor = R.color.black
        yAxis.setDrawGridLines(false)

        val axisRight = lineChart.axisRight
        axisRight.isEnabled = false

    }


    private fun setupChartsData(resultBeanList: List<Spo2AndHeartRateBean>) {

        if (resultBeanList.isEmpty()) {
            return
        }

        val spo2List = ArrayList<Entry>()
        val heartRateList = ArrayList<Entry>()

        spo2DataSet = ScatterDataSet(spo2List, "spo2")
        heartRateDataSet = ScatterDataSet(heartRateList, "heartRate")


        // 首先，对数据列表按照时间戳进行升序排序
        val sortedDataList = resultBeanList.sortedBy { it.timestamp }

        val timestampList = sortedDataList.map { it.timestamp }

        val min = timestampList.min()
        val max = timestampList.max()

        Log.i(TAG, "setupCharts: ${dateFormat.format(min)} - ${dateFormat.format(max)}")

        // 时间间隔 不管时间范围是多少 都分割成SLOT_SIZE份算平均值
        val interval = (max - min) / SLOT_SIZE

        // 创建SLOT_SIZE个时间段，并初始化为空数据列表
        // 初始化一个用于存储子列表的结果集合
        val result = MutableList(SLOT_SIZE) { mutableListOf<Spo2AndHeartRateBean>() }
        var startTime = min
        var endTime = startTime + interval

        for (i in 0 until SLOT_SIZE) {
            result[i] =
                sortedDataList.filter { it.timestamp in startTime until endTime }.toMutableList()
            startTime = endTime
            endTime += interval
        }

//        result.forEach { subList ->
//
//            if (subList.isNotEmpty()) {
//
//                val format = dateFormat.format(subList[0].timestamp)
//
//                Log.i(TAG, "setupCharts: format = $format, size = ${subList.size}")
//
//                spo2DataSet.addEntry(
//                    Entry(
//                        subList.map { it.timestamp }.average().toFloat(),
//                        subList.map { it.spo2 }.average().toFloat()
//                    )
//                )
//                heartRateDataSet.addEntry(
//                    Entry(
//                        subList.map { it.timestamp }.average().toFloat(),
//                        subList.map { it.heartRate }.average().toFloat()
//                    )
//                )
//            }
//
//        }

        resultBeanList.forEach {
            spo2DataSet.addEntry(
                Entry(
                    it.timestamp.toFloat(), it.spo2.toFloat()
                )
            )
            heartRateDataSet.addEntry(
                Entry(
                    it.timestamp.toFloat(),
                    it.heartRate.toFloat()
                )
            )
        }


//        spo2DataSet.setDrawCircles(false)
//        heartRateDataSet.setDrawCircles(false)

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

        val dataSetsRes = ArrayList<IScatterDataSet>()
        dataSetsRes.add(spo2DataSet)
//        dataSetsRes.add(heartRateDataSet)

        scatterData = ScatterData(dataSetsRes)
        lineChart.data = scatterData


        lineChart.setVisibleXRangeMaximum(resultBeanList[resultBeanList.size / 2].timestamp.toFloat())

        lineChart.invalidate()


    }

    override fun onResume() {
        super.onResume()

        // 把当前时间戳转成 yyyy-MM-dd
        val dateString = sdf.format(Date())

        // yyyy-MM-dd 转成时间戳
        val startTime = sdf.parse(dateString).time

        // initLineChartData(startTime)
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

                setupChartsData(resultBeanList)
            } else {
                setupChartsData(listOf())
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
            initLineChartData(calendar.timeInMillis)

        }

    }

}