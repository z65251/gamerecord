package com.alan.andy.gamerecord

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_edit_person.*
import kotlinx.android.synthetic.main.content_edit_person.*
import java.text.SimpleDateFormat
import java.util.*

class EditPersonActivity : AppCompatActivity() {

    lateinit var person: PersonInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_person)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            //only when save is ok: which means info have been edited
            if (saveEdit(true)) {
                finish()
            }
        }

        //read all info from other activity, and set as original text
        person = getPersonRecordFromIntent(intent, true)

        editText_Time.setText(person.time.toCharArray(), 0, person.time.length)
        editText_Name.setText(person.name.toCharArray(), 0, person.name.length)
        editText_Balance.setText(person.balance.toCharArray(), 0, person.balance.length)
        //editText_Fee.setText(person.fee.toCharArray(), 0, person.fee.length)
        editText_Comments.setText(person.comments.toCharArray(), 0, person.comments.length)

        //Log.i(LOG_TAG, "$time $name $balance $fee $comments")
        //set today button click handler
        button_today.setOnClickListener {_ ->
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date = Date(System.currentTimeMillis())
            val dateStr = simpleDateFormat.format(date)

            editText_Time.setText(dateStr.toCharArray(), 0, dateStr.length)
        }
    }

    override fun onBackPressed() {

        saveEdit(false)
        super.onBackPressed()
    }


    private fun saveEdit(isSave: Boolean): Boolean {

        val intent = Intent()

        if (isSave) {
            //get all info from edit text
            val time = editText_Time.text.toString()
            val name = editText_Name.text.toString()
            val balance = editText_Balance.text.toString()
            //val fee = editText_Fee.text.toString()
            val comments = editText_Comments.text.toString()

            //only when the text are not empty, otherwise do nothing
            if (time.isNotEmpty() || name.isNotEmpty() || balance.isNotEmpty() || /*fee.isNotEmpty() ||*/ comments.isNotEmpty()) {
                //set all info into intent
                person.time = time
                person.name = name
                person.balance = balance
                //person.fee = fee
                person.comments = comments

                setIntentExtraFromPerson(intent, person)

                setResult(RESULT_OK, intent)
                return true
            }

        } else {
            setResult(RESULT_CANCELED, intent)
            return true
        }

        return false
    }
}
