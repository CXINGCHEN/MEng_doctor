package com.specknet.pdiotapp.doctor

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var calendarView: CalendarView

    val TAG = "HistoryFragment"

    val sdf = SimpleDateFormat("yyyy-MM-dd")

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

    private val database = Firebase.database

    private val sdfyyyyMMdd = SimpleDateFormat("yyyyMMdd")
    private val gson = Gson()

    private var uid:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = arguments?.getString("uid")

        Log.i(TAG, "onCreate: uid = ${uid}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        barChart = view.findViewById(R.id.barChart)
        initCalenderView()
        initBarChartStyle()

    }

    /**
     * 每次fragment从不可见到可见时就会调用
     */
    override fun onResume() {
        super.onResume()

        // 把当前时间戳转成 yyyy-MM-dd
        val dateString = sdf.format(Date())

        // yyyy-MM-dd 转成时间戳
        val startTime = sdf.parse(dateString).time

        initBarChartData(startTime)
    }

    private fun initBarChartStyle() {


        barChart.description.isEnabled = false

        val xAxis = barChart.xAxis

        xAxis.textSize = 12f
        xAxis.textColor = R.color.black
        xAxis.labelRotationAngle = 90f

        xAxis.valueFormatter = IndexAxisValueFormatter()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        // xAxis.setDrawAxisLine(true)

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return when (value.toInt()) {
                    1 -> {
                        "Sitting"
                    }

                    2 -> {
                        "Lying"
                    }

                    3 -> {
                        "Standing"
                    }

                    4 -> {
                        "Walking"
                    }

                    5 -> {
                        "Running"
                    }

                    6 -> {
                        "Ascending"
                    }

                    7 -> {
                        "Descending"
                    }

                    8 -> {
                        "Movement"
                    }

                    else -> {
                        ""
                    }
                }

            }
        }


        val yAxis = barChart.axisLeft
        yAxis.textSize = 12f
        yAxis.textColor = R.color.black
        // yAxis.isEnabled = false
        yAxis.setDrawGridLines(false)


        val axisRight = barChart.axisRight
        axisRight.isEnabled = false

        val legend = barChart.legend
        legend.textColor = R.color.black
        legend.textSize = 10f


    }


    /**
     * 日期
     * yyyy-MM-dd
     *
     * 2022-11-18
     */
    private fun initBarChartData(startTime: Long) {

        Log.d(TAG, "initBarChartData() called with: startTime = $startTime")

        val format = sdf.format(Date(startTime))
        Log.d(TAG, "initBarChartData() called with: format = $format")


        val dayEnd = startTime + 86400000

        // 所有数据
        // val userRecognitionData = RecognitionResultHelper.getUserRecognitionData()

        // 筛选出来的某一天的数据
//        val resultBeanList =
//            RecognitionResultHelper.getUserRecognitionData()
//                .filter { it.timestamp > startTime && it.timestamp <= dayEnd }


        val path = "${uid}/date${sdfyyyyMMdd.format(Date(startTime))}/action"
        Log.i(TAG, "initBarChartData: path = ${path}")
        val myRef = database.getReference(path)
        myRef.get().addOnSuccessListener {
            // 解析数据
            Log.i(TAG, "initBarChartData: ${gson.toJson(it.value)}")

            if (it.value != null) {
                val resultBeanList = mutableListOf<RecognitionResultBean>()

                (it.value as List<Map<String, Any>>).forEach {
                    val jsonString = gson.toJson(it)
                    val resultBean = gson.fromJson(jsonString, RecognitionResultBean::class.java)
                    resultBeanList.add(resultBean)
                }

                initBarUI(resultBeanList)
            } else {
                initBarUI(listOf())
            }
        }.addOnFailureListener {
            Log.i(TAG, "initBarChartData: onFailure")
        }

    }

    private fun initBarUI(resultBeanList: List<RecognitionResultBean>) {

        // 计算每个动作的数据量

        val groupBy = resultBeanList.groupBy { it.label }

        // 一共有多少种动作类型
        // val size = groupBy.size

        // sit
        val list0 = groupBy[labels[0]] ?: emptyList()
        val list1 = groupBy[labels[1]] ?: emptyList()
        val list2 = groupBy[labels[2]] ?: emptyList()
        val list13 = groupBy[labels[12]] ?: emptyList()

        val sitList = mutableListOf<RecognitionResultBean>()
        sitList.addAll(list0)
        sitList.addAll(list1)
        sitList.addAll(list2)
        sitList.addAll(list13)

        // lying
        val list4 = groupBy[labels[4]] ?: emptyList()
        val list5 = groupBy[labels[5]] ?: emptyList()
        val list6 = groupBy[labels[6]] ?: emptyList()
        val list7 = groupBy[labels[7]] ?: emptyList()


        val lyingList = mutableListOf<RecognitionResultBean>()
        lyingList.addAll(list4)
        lyingList.addAll(list5)
        lyingList.addAll(list6)
        lyingList.addAll(list7)

        val standList = groupBy[labels[3]] ?: emptyList()
        val walkingList = groupBy[labels[8]] ?: emptyList()
        val runningList = groupBy[labels[9]] ?: emptyList()
        val ascendingList = groupBy[labels[10]] ?: emptyList()
        val descendingList = groupBy[labels[11]] ?: emptyList()
        val movementList = groupBy[labels[13]] ?: emptyList()


        val dataSets: MutableList<IBarDataSet> = ArrayList()
        val yVals: MutableList<BarEntry> = ArrayList()

        yVals.add(BarEntry(1f, sitList.map { it.count }.sum() * 0.2f))
        yVals.add(BarEntry(2f, lyingList.map { it.count }.sum() * 0.2f))
        yVals.add(BarEntry(3f, standList.map { it.count }.sum() * 0.2f))
        yVals.add(BarEntry(4f, walkingList.map { it.count }.sum() * 0.2f))
        yVals.add(BarEntry(5f, runningList.map { it.count }.sum() * 0.2f))
        yVals.add(BarEntry(6f, ascendingList.map { it.count }.sum() * 0.2f))
        yVals.add(BarEntry(7f, descendingList.map { it.count }.sum() * 0.2f))
        yVals.add(BarEntry(8f, movementList.map { it.count }.sum() * 0.2f))


        val stepText =
            (walkingList.map { it.count }.sum() * 2f) + (runningList.map { it.count }
                .sum() * 4f) + (movementList.map { it.count }
                .sum() * 1f) + (ascendingList.map { it.count }
                .sum() * 2f) + (descendingList.map { it.count }.sum() * 2f)

        val barDataSet = BarDataSet(yVals, "time/seconds")

        barDataSet.valueTextSize = 12f
        barDataSet.valueTextColor = Color.WHITE
        // barDataSet.setColors(Color.BLUE,Color.CYAN,Color.BLUE,Color.CYAN,Color.BLUE,Color.CYAN,Color.BLUE,Color.CYAN)

        dataSets.add(barDataSet)
        val barData = BarData(dataSets)
        barChart.data = barData

        barChart.invalidate()
    }

    private fun initCalenderView() {

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            Toast.makeText(activity, "$year-$month-$dayOfMonth", Toast.LENGTH_SHORT).show()
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            // 重新获取选中日期的数据
            initBarChartData(calendar.timeInMillis)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }
}