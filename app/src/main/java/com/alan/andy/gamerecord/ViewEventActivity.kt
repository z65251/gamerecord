package com.alan.andy.gamerecord

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.SimpleAdapter
import kotlinx.android.synthetic.main.content_view_event.*
import kotlinx.android.synthetic.main.activity_view_event.*
import java.util.ArrayList
import java.util.HashMap


class ViewEventActivity : AppCompatActivity() {

    private val REQUEST_EDIT_EVENT = 101
    private val REQUEST_ADD_EVENT = 102

    private lateinit var mListData: ArrayList<HashMap<String, Any>>
    private lateinit var mListSimpleAdapter: SimpleAdapter
    private lateinit var mDbHelper: RecordDbHelper

    private var mClickedIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)

        //toolbar.setTitle(R.string.title_activity_view_event)

        fab.setOnClickListener{ _ ->
            val record = EventInfo()
            moveToEditEvent(record, true)
        }

        //set list adapter for the main list
        val mapKey = arrayOf(COLUMN_PHOTO, COLUMN_TIME, COLUMN_FEE, COLUMN_POSITON, COLUMN_GAME_TYPE, COLUMN_COMMENTS)
        val ids = intArrayOf(R.id.photo, R.id.time, R.id.fee, R.id.position, R.id.gametype,  R.id.comments)

        mDbHelper = RecordDbHelper(this)
        mListData = getListData()
        mListSimpleAdapter = SimpleAdapter(this, mListData, R.layout.list_item_event, mapKey, ids)

        view_event_list.adapter = mListSimpleAdapter

        //set list item click listener, move to edit activity with the text of the item
        view_event_list.setOnItemClickListener { _, _, position, _ ->
            moveToEditEvent(getEventFromListData(mListData, position), false)

            mClickedIndex = position
        }

        view_event_list.setOnItemLongClickListener {_, _, position, _ ->
            AlertDialog.Builder(this)
                    //.setIcon(R.id.icon)
                    .setTitle(R.string.text_delete_info)
                    .setPositiveButton(R.string.text_yes) { _, _ ->
                        deleteListData(position)
                    }
                    .setNegativeButton(R.string.text_no, null).create()
                    .show()

            true
        }
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
        mListSimpleAdapter.notifyDataSetChanged()
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
        mListSimpleAdapter.notifyDataSetChanged()
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
        mListSimpleAdapter.notifyDataSetChanged()
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
}
