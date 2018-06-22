package com.alan.andy.gamerecord

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.SimpleAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.content.DialogInterface
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.alan.andy.gamerecord.RecordDbHelper.Companion.DATABASE_NAME
import java.util.*



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //members below here
    private val REQUEST_ADD_PERSON = 100
    private val REQUEST_EDIT_PERSON = 101

    private lateinit var mListData: ArrayList<HashMap<String, Any>>
    private lateinit var mListSimpleAdapter: SimpleAdapter
    private lateinit var mDbHelper: RecordDbHelper

    private var mClickedIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener{ _ ->
            val record = PersonInfo()
            moveToEditPerson(record, true)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //set list adapter for the main list
        val mapKey = arrayOf("face", COLUMN_TIME, COLUMN_NAME, COLUMN_BALANCE, COLUMN_FEE, COLUMN_COMMENTS)
        val ids = intArrayOf(R.id.face, R.id.time, R.id.name, R.id.balance, R.id.fee, R.id.comments)

        mDbHelper = RecordDbHelper(this)
        mListData = getListData()
        mListSimpleAdapter = SimpleAdapter(this, mListData, R.layout.list_item_main, mapKey, ids)

        main_list.adapter = mListSimpleAdapter

        //set list item click listener, move to edit activity with the text of the item
        main_list.setOnItemClickListener { _, _, position, _ ->
            moveToEditPerson(getPersonFromListData(mListData, position), false)

            mClickedIndex = position
        }

        main_list.setOnItemLongClickListener {_, _, position, _ ->
            //Toast.makeText(this, "Position Long Clicked:"+" "+position, Toast.LENGTH_SHORT).show()
            AlertDialog.Builder(this)
                    //.setIcon(R.id.icon)
                    .setTitle(R.string.text_delete_info)
                    .setPositiveButton(R.string.text_yes,
                            DialogInterface.OnClickListener { _, _ ->
                                deleteListData(position)
                            })
                    .setNegativeButton(R.string.text_no, null).create()
                    .show()
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        //Log.d(LOG_TAG, "$requestCode $resultCode")
        when (requestCode) {
            REQUEST_ADD_PERSON -> {
                if (resultCode == RESULT_OK) {
                    //add to list data and append to db
                    addListData(getPersonRecordFromIntent(intent!!, false))
                }
            }

            REQUEST_EDIT_PERSON -> {
                if (resultCode == RESULT_OK) {
                    //update click info
                    updateListData(getPersonRecordFromIntent(intent!!, true))
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_view_event -> {
                moveToViewEvent()
            }
            R.id.nav_show_chart -> {
                moveToViewChart()
            }
            R.id.nav_manage -> {
                syncDatabase()
            }
            R.id.nav_share -> {

                AlertDialog.Builder(this)
                        //.setIcon(R.id.icon)
                        .setTitle(R.string.restore_database)
                        .setPositiveButton(R.string.text_yes,
                                DialogInterface.OnClickListener { _, _ ->
                                    backupRestorDbFile(false)
                                })
                        .setNegativeButton(R.string.text_no, null).create()
                        .show()
            }
            R.id.nav_send -> {

                AlertDialog.Builder(this)
                        //.setIcon(R.id.icon)
                        .setTitle(R.string.backup_database)
                        .setPositiveButton(R.string.text_yes,
                                DialogInterface.OnClickListener { _, _ ->
                                    backupRestorDbFile(true)
                                })
                        .setNegativeButton(R.string.text_no, null).create()
                        .show()
                //val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")// HH:mm:ss
                //val date = Date(System.currentTimeMillis())

                //val addresses = arrayOf("z65251@gmail.com")
                //val subject = "GameRecord_backup_" + simpleDateFormat.format(date)

                //val bkFile = File(applicationContext.filesDir, "backup/backup.db")
                //dbFile.copyTo(bkFile, true)

                //val contentUri = getUriForFile(applicationContext, "com.alan.andy.fileprovider", bkFile)
                //composeEmail(addresses, subject, contentUri)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    //internal fun for main
    private fun getListData(): ArrayList<HashMap<String, Any>> {
        val list = ArrayList<HashMap<String, Any>>()

        val db = mDbHelper.readableDatabase

        val c = db.query(TABLE_NAME_PERSON, // The table to query
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
               setMapFromCursorForPerson(map, c)

               list.add(map)

               c.moveToNext()
           }
        }

        c.close()
        db.close()

        return list
    }

    private fun addListData(person: PersonInfo) {
        val map: HashMap<String, Any> = HashMap()

        //gets the data repository in write mode
        val db = mDbHelper.writableDatabase

        //create a new map of values, where column names are the keys
        //insert the new row, returning the primary key value of the new row
        val index = db.insert(TABLE_NAME_PERSON, null, createNewPersonValues(person))
        db.close()

        //update list data and view
        person.id = index.toInt()
        setMapFromPersonValue(map, person)
        mListData.add(map)
        mListSimpleAdapter.notifyDataSetChanged()
    }

    private fun updateListData(person: PersonInfo) {
        val map: HashMap<String, Any> = mListData.get(mClickedIndex)

        //gets the data repository in write mode
        val db = mDbHelper.writableDatabase

        val selection = "_id = ?"
        val selectionArgs = arrayOf(map["_id"].toString())

        db.update(TABLE_NAME_PERSON, createNewPersonValues(person), selection, selectionArgs)
        db.close()

        //update list data and view
        setMapFromPersonValue(map, person)
        mListSimpleAdapter.notifyDataSetChanged()
    }

    private fun deleteListData(index: Int) {
        val map: HashMap<String, Any> = mListData.get(index)

        //gets the data repository in write mode
        val db = mDbHelper.writableDatabase

        val selection = "_id = ?"
        val selectionArgs = arrayOf(map["_id"].toString())

        db.delete(TABLE_NAME_PERSON, selection, selectionArgs)
        db.close()

        //update list data and view
        mListData.removeAt(index)
        mListSimpleAdapter.notifyDataSetChanged()
    }

    private fun composeEmail(addresses: Array<String>, subject: String, attachment: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_STREAM, attachment)
        intent.flags = FLAG_GRANT_READ_URI_PERMISSION

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun backupRestorDbFile(isBackup: Boolean) {
        val dbFile = applicationContext.getDatabasePath(DATABASE_NAME)
        val bkFile = applicationContext.getExternalFilesDir(DATABASE_NAME)

        if (isBackup) {
            dbFile.copyTo(bkFile, true)
        } else {
            bkFile.copyTo(dbFile, true)
        }
    }

    private fun moveToEditPerson(person: PersonInfo, isAdd: Boolean) {
        val intent = Intent()

        intent.setClass(this, EditPersonActivity::class.java)
        setIntentExtraFromPerson(intent, person)

        var requestCode: Int = REQUEST_EDIT_PERSON
        if (isAdd) {
            requestCode = REQUEST_ADD_PERSON
        }

        startActivityForResult(intent, requestCode)
    }

    private fun moveToViewEvent() {
        val intent = Intent()

        intent.setClass(this, ViewEventActivity::class.java)

        startActivity(intent)
    }

    private fun moveToViewChart() {
        val intent = Intent()

        intent.setClass(this, ViewChartActivity::class.java)

        startActivity(intent)
    }

    private fun syncDatabase() {

    }
}

