package com.specknet.pdiotapp.doctor

import android.graphics.Color
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

class ActionRealtimeHistoryActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "Spo2AndHeartRateHistoryActivity"
    }


    private lateinit var calendarView: CalendarView

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    lateinit var scatterChart: ScatterChart

    lateinit var scatterData: ScatterData

    private var uid: String? = null

    private val database = Firebase.database

    private val gson = Gson()

    val dateFormat = SimpleDateFormat("HH:mm", Locale.CHINA)
    var selectDayZeroTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_realtime_history)


        uid = intent.getStringExtra("uid") ?: ""
        Log.i(TAG, "onCreate: uid = $uid")


        calendarView = findViewById(R.id.calendarView)
        scatterChart = findViewById(R.id.line_chart)

        initCalenderView()

        initChartsStyle()

    }

    private fun initChartsStyle() {

        scatterChart.description.isEnabled = false


        scatterChart.isScaleXEnabled = true
        scatterChart.isScaleYEnabled = false

        val xAxis = scatterChart.xAxis

        xAxis.setDrawGridLines(false)

        xAxis.textSize = 12f
        xAxis.textColor = R.color.black

        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // x轴显示的值自定义
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                Log.i(TAG, "getAxisLabel: $value")
                val toLong = value.toLong()
                return dateFormat.format(toLong + selectDayZeroTime)
            }
        }

        val yAxis = scatterChart.axisLeft
        yAxis.textSize = 12f
        yAxis.textColor = R.color.black
        yAxis.setDrawGridLines(false)

        val axisRight = scatterChart.axisRight
        axisRight.isEnabled = false

    }

    val labels = listOf(
        "Sitting straight",
        "Sitting bent forward",
        "Sitting bent backward",
        "Standing",
        "Lying down left",
        "Lying down right",
        "Lying down front",
        "Lying down back",
        "Walking",
        "Running",
        "Ascending stairs",
        "Descending stairs",
        "Desk work",
        "General movement"
    )

    val colors = listOf(
        Color.parseColor("#111111"),
        Color.parseColor("#222222"),
        Color.parseColor("#333333"),
        Color.parseColor("#444444"),
        Color.parseColor("#555555"),
        Color.parseColor("#666666"),
        Color.parseColor("#777777"),
        Color.parseColor("#888888"),
        Color.parseColor("#999999"),
        Color.parseColor("#AAAAAA"),
        Color.parseColor("#BBBBBB"),
        Color.parseColor("#CCCCCC"),
        Color.parseColor("#DDDDDD"),
        Color.parseColor("#EEEEEE"),
        Color.parseColor("#FFFFFF"),
        Color.parseColor("#000000"),
        Color.parseColor("#0000FF"),
        Color.parseColor("#00FF00"),
    )


    private fun mockList(): MutableList<RecognitionResultBean> {
        val result = mutableListOf<RecognitionResultBean>()

        val timestampStart = sdf.parse(sdf.format(Date())).time + (1000 * 60 * 60 * 12)
        val timestampEnd =
            sdf.parse(sdf.format(Date())).time + (1000 * 60 * 60 * 24) - (1000 * 60 * 60 * 11)

        selectDayZeroTime = timestampStart


        var timestamp = timestampStart
        while (timestamp < (timestampEnd)) {
            val bean = RecognitionResultBean()

            bean.timestamp = timestamp - selectDayZeroTime
            bean.labelIndex = Random.nextInt(0, 6)
            result.add(bean)

            timestamp += 5000
        }

        return result

    }


    private fun setupChartsData(resultBeanList: List<RecognitionResultBean>) {

        val groupBy = resultBeanList.groupBy { it.labelIndex }

        val dataSetsRes = ArrayList<IScatterDataSet>()

        groupBy.forEach { (labelIndex, list) ->

            val actionList = ArrayList<Entry>()

            val actionDataSet = ScatterDataSet(actionList, labels[labelIndex])

            list.forEach {
                actionDataSet.addEntry(
                    Entry(
                        it.timestamp.toFloat(), labelIndex.toFloat()
                    )
                )
            }

            actionDataSet.color = colors[labelIndex]

            dataSetsRes.add(actionDataSet)
        }


        scatterData = ScatterData(dataSetsRes)
        scatterChart.data = scatterData


        // 图表绘制完默认展示多少个点
        if (resultBeanList.isNotEmpty() && resultBeanList.size >= 40) {
            scatterChart.setVisibleXRangeMaximum(resultBeanList[39].timestamp.toFloat())
        }

        scatterChart.invalidate()


    }

    override fun onResume() {
        super.onResume()
        // 把当前时间戳转成 yyyy-MM-dd
        val dateString = sdf.format(Date())
        // yyyy-MM-dd 转成时间戳
        val startTime = sdf.parse(dateString).time
        initLineChartData(startTime)

        // 本地代码mock数据
//        setupChartsData(mockList())
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

        val sdfyyyyMMdd = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
        val path = "${uid}/date${sdfyyyyMMdd.format(Date(startTime))}/actionrealtime"
        Log.i(TAG, "initLineChartData: path = ${path}")
        val myRef = database.getReference(path)
        myRef.get().addOnSuccessListener {
            // 解析数据
            Log.i(TAG, "initLineChartData: ${gson.toJson(it.value)}")

            if (it.value != null) {
                val resultBeanList = mutableListOf<RecognitionResultBean>()

                (it.value as List<Map<String, Any>>).forEach {
                    val jsonString = gson.toJson(it)
                    val resultBean = gson.fromJson(jsonString, RecognitionResultBean::class.java)
                    resultBeanList.add(resultBean)
                }
                // 1697461588000 第一个点的时间戳
                // 1697461593000 第2个点的时间戳 1697461593000 - 1697461588000 = 5000
                // 1697461598000 第3个点的时间戳 .. = 5000
                //（0, 85） (5000,83) (10000, 81) ..
                if (resultBeanList.isNotEmpty()) {
                    resultBeanList.sortedBy { it.timestamp }
                    selectDayZeroTime = resultBeanList[0].timestamp // 这一天中第一个点的时间

                    resultBeanList.forEach { bean ->
                        bean.timestamp = bean.timestamp - selectDayZeroTime
                    }

                    setupChartsData(resultBeanList)
                }

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