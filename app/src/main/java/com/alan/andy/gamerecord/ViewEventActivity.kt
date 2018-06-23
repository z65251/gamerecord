package com.alan.andy.gamerecord

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.content_view_event.*
import kotlinx.android.synthetic.main.activity_view_event.*
import java.util.ArrayList
import java.util.HashMap


class ViewEventActivity : AppCompatActivity() {

    private lateinit var mListData: ArrayList<HashMap<String, Any>>
    private lateinit var mAdapter: ViewEventRecyclerViewAdapter
    private lateinit var mDbHelper: RecordDbHelper

    private var mClickedIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)

        fab.setOnClickListener{ _ ->
            val record = EventInfo()
            moveToEditEvent(record, true)
        }

        mDbHelper = RecordDbHelper(this)
        mListData = getListData()

        //set layout manager and adapter
        view_event_recycler_view.layoutManager = LinearLayoutManager(this)
        mAdapter = ViewEventRecyclerViewAdapter(this)
        view_event_recycler_view.adapter = mAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        //Log.d(LOG_TAG, "$requestCode $resultCode")
        when (requestCode) {
            REQUEST_ADD_EVENT -> {
                if (resultCode == RESULT_OK) {
                    //add to list data and append to db
                    addListData(getEventRecordFromIntent(intent!!, false))
                }
            }

            REQUEST_EDIT_EVENT -> {
                if (resultCode == RESULT_OK) {
                    //update click info
                    updateListData(getEventRecordFromIntent(intent!!, true))
                }
            }
        }
    }

    inner class ViewEventRecyclerViewAdapter(context: Context) : RecyclerView.Adapter<ViewEventRecyclerViewAdapter.ViewEventViewHolder>() {
        private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewEventViewHolder {
            return ViewEventViewHolder(mLayoutInflater.inflate(R.layout.item_event, parent, false))
        }

        override fun onBindViewHolder(holder: ViewEventViewHolder, position: Int) {

            //val mapKey = arrayOf(COLUMN_PHOTO, COLUMN_TIME, COLUMN_FEE, COLUMN_POSITON, COLUMN_GAME_TYPE, COLUMN_COMMENTS)
            holder.mTimeTextView.text = mListData[position][COLUMN_TIME].toString()
            holder.mFeeTextView.text = mListData[position][COLUMN_FEE].toString()
            holder.mCommentView.text = mListData[position][COLUMN_COMMENTS].toString()

        }

        override fun getItemCount(): Int {
            return mListData.size
        }

        inner class ViewEventViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {

            //var mPhotoImageView: ImageView = view.findViewById(R.id.face)
            var mTimeTextView: TextView = view.findViewById(R.id.time)
            var mFeeTextView: TextView = view.findViewById(R.id.fee)
            //var mPositonTextView: TextView = view.findViewById(R.id.position)
            //var mGameTypeTextView: TextView = view.findViewById(R.id.gametype)
            var mCommentView: TextView = view.findViewById(R.id.comments)

            init {
                view.setOnClickListener { _ ->
                    Log.d("MainViewHolder", "onClick--> position = $layoutPosition")

                    moveToEditEvent(getEventFromListData(mListData, layoutPosition), false)

                    mClickedIndex = layoutPosition
                }

                view.setOnLongClickListener { _ ->
                    Log.d("MainViewHolder", "onLongClick--> position = $layoutPosition")
                    AlertDialog.Builder(this@ViewEventActivity)
                            //.setIcon(R.id.icon)
                            .setTitle(R.string.text_delete_info)
                            .setPositiveButton(R.string.text_yes) { _, _ ->
                                deleteListData(layoutPosition)
                            }
                            .setNegativeButton(R.string.text_no, null).create()
                            .show()

                    true
                }

            }
        }
    }

    //internal fun for main
    private fun getListData(): ArrayList<HashMap<String, Any>> {
        val list = ArrayList<HashMap<String, Any>>()

        val db = mDbHelper.readableDatabase

        val c = db.query(TABLE_NAME_EVENT, // The table to query
                null, // The columns to return
                null, // The columns for the WHERE clause
                null,
                null,  // don't group the rows
                null,   // don't filter by row groups
                null   // The sort order
        )// The values for the WHERE clause

        if (c.moveToFirst()) {
            while (!c.isAfterLast) {

                val map: HashMap<String, Any> = HashMap()
                setMapFromCursorForEvent(map, c)

                list.add(map)

                c.moveToNext()
            }
        }

        c.close()
        db.close()

        return list
    }

    private fun updateListData(event: EventInfo) {
        val map: HashMap<String, Any> = mListData.get(mClickedIndex)

        //gets the data repository in write mode
        val db = mDbHelper.writableDatabase

        val selection = "_id = ?"
        val selectionArgs = arrayOf(map["_id"].toString())

        db.update(TABLE_NAME_EVENT, createNewEventValues(event), selection, selectionArgs)
        db.close()

        //update list data and view
        setMapFromEventValue(map, event)
        mAdapter.notifyDataSetChanged()
    }

    private fun addListData(event: EventInfo) {
        val map: HashMap<String, Any> = HashMap()

        //gets the data repository in write mode
        val db = mDbHelper.writableDatabase

        //create a new map of values, where column names are the keys
        //insert the new row, returning the primary key value of the new row
        val index = db.insert(TABLE_NAME_EVENT, null, createNewEventValues(event))
        db.close()

        //update list data and view
        event.id = index.toInt()
        setMapFromEventValue(map, event)

        mListData.add(map)
        mAdapter.notifyDataSetChanged()
    }

    private fun deleteListData(index: Int) {
        val map: HashMap<String, Any> = mListData.get(index)

        //gets the data repository in write mode
        val db = mDbHelper.writableDatabase

        val selection = "_id = ?"
        val selectionArgs = arrayOf(map["_id"].toString())

        db.delete(TABLE_NAME_EVENT, selection, selectionArgs)
        db.close()

        //update list data and view
        mListData.removeAt(index)
        mAdapter.notifyDataSetChanged()
    }

    private fun moveToEditEvent(event: EventInfo, isAdd: Boolean) {
        val intent = Intent()

        intent.setClass(this, EditEventActivity::class.java)
        setIntentExtraFromEvent(intent, event)

        var requestCode: Int = REQUEST_EDIT_EVENT
        if (isAdd) {
            requestCode = REQUEST_ADD_EVENT
        }

        startActivityForResult(intent, requestCode)
    }

    companion object {
        const val REQUEST_EDIT_EVENT = 101
        const val REQUEST_ADD_EVENT = 102
    }
}
