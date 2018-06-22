package com.alan.andy.gamerecord

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_edit_event.*
import kotlinx.android.synthetic.main.content_edit_event.*
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment


class EditEventActivity : AppCompatActivity() {

    private val TAKE_PHOTO = 100

    lateinit var event: EventInfo
    private var image: String ?= null

    //lateinit var isMajong: Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)
        //setSupportActionBar(toolbar)

        fab.setOnClickListener {  _ ->
            //only when save is ok: which means info have been edited
            if (saveEdit(true)) {
                finish()
            }
        }

        event = getEventRecordFromIntent(intent, true)

        editText_Time.setText(event.time.toCharArray(), 0, event.time.length)
        editText_Fee.setText(event.fee.toCharArray(), 0, event.fee.length)
        editText_Comments.setText(event.comments.toCharArray(), 0, event.comments.length)

        switch_IsMajong.isChecked = event.gametype == "1"


        if (event.photo != "0" && event.photo != "") {
            val bitmap = BitmapFactory.decodeFile(event.photo)

            image_preview.setImageBitmap(bitmap)
        }
        /*switch_IsMajong.setOnCheckedChangeListener{_, isChecked ->
            isMajong = isChecked
        }*/

        //set today button click handler
        button_today.setOnClickListener {_ ->
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date = Date(System.currentTimeMillis())
            val dateStr = simpleDateFormat.format(date)

            editText_Time.setText(dateStr.toCharArray(), 0, dateStr.length)
        }

        //set camera button click handler
        button_camera.setOnClickListener {_ ->
            getPhoto()
        }
    }

    override fun onBackPressed() {
        saveEdit(false)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        when (requestCode) {
            TAKE_PHOTO -> {
                var photo: Bitmap? = null

                if (intent!!.data != null || intent.extras != null) { //防止没有返回结果
                    val uri = intent.data
                    if (uri != null) {
                        photo = BitmapFactory.decodeFile(uri.path) //拿到图片
                    }

                    if (photo == null) {
                        photo = intent.extras.get("data") as Bitmap
                    }
                }

                if (photo != null) {
                    image_preview.setImageBitmap(photo)

                    //val name = "event_" + System.currentTimeMillis() + ".png"
                    //val path = getExternalFilesDir(name).absolutePath.toString()

                    //val fos = openFileOutput(path, MODE_PRIVATE)
                    //val fos = FileOutputStream(File(path), false)
                    //val fos = applicationContext.openFileOutput(path, MODE_PRIVATE)

                    //photo.compress(Bitmap.CompressFormat.PNG, 100, fos)

                    //image = applicationContext.getExternalFilesDir(name).path
                }
            }
        }
    }

    private fun saveEdit(isSave: Boolean): Boolean {

        val intent = Intent()

        if (isSave) {
            //get all info from edit text
            val time = editText_Time.text.toString()
            val fee = editText_Fee.text.toString()
            val comments = editText_Comments.text.toString()

            //only when the text are not empty, otherwise do nothing
            if (time.isNotEmpty() || fee.isNotEmpty() || comments.isNotEmpty()) {
                //set all info into intent
                event.time = time
                event.fee = fee
                event.comments = comments

                if (switch_IsMajong.isChecked) {
                    event.gametype = "1"
                } else {
                    event.gametype = "0"
                }

                if (image != null) {
                    event.photo = image as String
                } else {
                    event.photo = "0"
                }

                //TODO get position
                event.position = "0"

                setIntentExtraFromEvent(intent, event)

                setResult(RESULT_OK, intent)
                return true
            }
        } else {
            setResult(RESULT_CANCELED, intent)
            return true
        }

        return false
    }

    private fun getPhoto() {
        /**
         * 在启动拍照之前先判断一下sdcard是否可用
         */
        val state = Environment.getExternalStorageState()
        if (state == Environment.MEDIA_MOUNTED) {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivityForResult(intent, TAKE_PHOTO)
        } else {
            Toast.makeText(this, "sdcard not exist", Toast.LENGTH_SHORT).show()
        }
    }
}
