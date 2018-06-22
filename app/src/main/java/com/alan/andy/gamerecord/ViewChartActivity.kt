package com.alan.andy.gamerecord

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_view_chart.*
import kotlinx.android.synthetic.main.content_view_chart.*

import java.util.ArrayList
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import android.graphics.DashPathEffect
import android.util.Log
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet


class ViewChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_chart)
        //setSupportActionBar(toolbar)

        toolbar.setTitle(R.string.menu_show_chart)

        //fill data
        setData(line_chart)

        //set description
        line_chart.description.text = "balance chart"

        //set basic property
        line_chart.setTouchEnabled(true)
        line_chart.isDragEnabled = true
        line_chart.setScaleEnabled(true)
        line_chart.setPinchZoom(true)

        //set draw label
        line_chart.xAxis.setDrawLabels(false)
        //line_chart.xAxis.setDrawGridLines(false)

        //not draw right
        line_chart.axisRight.isEnabled = false

        //set animation duration
        line_chart.animateX(2500)
    }

    private fun getNameList(): ArrayList<String> {
        val nameList = ArrayList<String>()

        val db = RecordDbHelper(this).readableDatabase

        val cursor = db.query(true,
                TABLE_NAME_PERSON, // The table to query
                arrayOf(COLUMN_NAME), // The columns to return
                null,
                null,
                null,
                null,
                null,
                null)

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {

                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                nameList.add(name)

                cursor.moveToNext()
            }
        }

        cursor.close()
        db.close()

        return nameList
    }

    private fun getTimeList(): ArrayList<String> {
        val timeList = ArrayList<String>()

        val db = RecordDbHelper(this).readableDatabase

        val cursor = db.query(true,
                TABLE_NAME_PERSON, // The table to query
                arrayOf(COLUMN_TIME), // The columns to return
                null,
                null,
                null,
                null,
                null,
                null)

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {

                val time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
                timeList.add(time)

                cursor.moveToNext()
            }
        }

        cursor.close()
        db.close()

        return timeList
    }

    private fun getIndexByTime(timeList: ArrayList<String>, time: String): Int {
        for (i in 0 until timeList.size) if (time == timeList[i]) {
            return i
        }

        return -1
    }

    private fun setData(chart: LineChart) {

        val db = RecordDbHelper(this).readableDatabase

        //get name list and time list firstly
        val nameList = getNameList()
        val timeList = getTimeList()

        val dataSets = ArrayList<ILineDataSet>()

        //fill value from database by name
        for (i in 0 until nameList.size) {
            val name = nameList[i]
            val values = ArrayList<Entry>()

            //first we fill all points with 0, because someone could skip some time
            for (j in 0 until timeList.size) {
                values.add(Entry(j.toFloat(), 0F, R.drawable.star))
            }

            //select * from column balance and colume time where name = ?
            val cursor = db.query(TABLE_NAME_PERSON, // The table to query
                    arrayOf(COLUMN_TIME, COLUMN_BALANCE), // The columns to return
                    "name =?",
                    arrayOf(name),
                    null,
                    null,
                    null)

            var sum = 0

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {

                    val time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
                    val balance = cursor.getInt(cursor.getColumnIndex(COLUMN_BALANCE))

                    //only update those have been in time list balance
                    val index = getIndexByTime(timeList, time)
                    if (index > -1) {
                        val entry = values[index]
                        entry.x = index.toFloat()
                        entry.y = balance.toFloat()
                    }

                    sum += balance

                    cursor.moveToNext()
                }
            }

            // create a dataset and give it a type
            val set = LineDataSet(values, name + ":" + sum.toString())

            // set the line to be drawn like this "- - - - - -"
            set.enableDashedLine(10f, 5f, 0f)
            set.enableDashedHighlightLine(10f, 5f, 0f)

            //set color
            set.color = getColor(getColorIdFromName(name))
            set.setCircleColor(getColor(getColorIdFromName(name)))

            set.lineWidth = 2f
            set.circleRadius = 3f
            set.setDrawCircleHole(false)
            set.valueTextSize = 10f
            //set1.setDrawFilled(true)
            set.formLineWidth = 1f
            set.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set.formSize = 15f

            dataSets.add(set)

            cursor.close()
        }

        db.close()

        chart.data = LineData(dataSets)
    }

}
