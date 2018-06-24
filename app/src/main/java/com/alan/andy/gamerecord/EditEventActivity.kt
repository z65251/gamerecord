package com.alan.andy.gamerecord

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_edit_event.*
import kotlinx.android.synthetic.main.content_edit_event.*
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast


class EditEventActivity : AppCompatActivity() {

    private lateinit var mEvent: EventInfo
    private var mBitmap: Bitmap? = null
    //private var image: String ?= null
    //lateinit var isMajong: Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        fab.setOnClickListener {  _ ->
            //only when save is ok: which means info have been edited
            if (saveEdit(true)) {
                finish()
            }
        }

        mEvent = getEventRecordFromIntent(intent, true)

        editText_Time.setText(mEvent.time.toCharArray(), 0, mEvent.time.length)
        editText_Fee.setText(mEvent.fee.toCharArray(), 0, mEvent.fee.length)
        editText_Comments.setText(mEvent.comments.toCharArray(), 0, mEvent.comments.length)

        switch_IsMajong.isChecked = mEvent.gametype == "1"


        if (mEvent.photo != "0" && mEvent.photo != "") {
            val bitmap = readPlayerEventBitmap(applicationContext, mEvent.photo)

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
        button_choose_camera.setOnClickListener {_ ->
            chooseCamera()
        }

        button_choose_gallery.setOnClickListener{_ ->
            if (ContextCompat.checkSelfPermission(this@EditEventActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this@EditEventActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_READ_STORAGE);

            }else {
                choosePhoto()
            }
        }
    }

    override fun onBackPressed() {
        saveEdit(false)
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == PERMISSIONS_REQUEST_READ_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto()
            } else {
                // Permission Denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(req: Int, res: Int, intent: Intent?) {

        when (req) {
            REQUEST_CODE_PICK_IMAGE -> {
                if (res == RESULT_OK) {

                    val uri = intent?.data

                    //val bit = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                    val bitmap = getFitSampleBitmap(contentResolver.openInputStream(uri), 320, 320)
                    if (bitmap !=  null) {
                        image_preview.setImageBitmap(bitmap)
                        mBitmap = bitmap

                        //save the picture to file
                        //savePlayerEventBitmap(applicationContext, mPlayerInfoList[mCurrentClickPos].name, bitmap)
                    }
                }
            }

            REQUEST_CODE_TAKE_IMAGE -> {
                if (res == RESULT_OK) {

                    val bundle = intent?.extras
                    val bitmap = bundle?.get("data") as Bitmap?
                    if (bitmap != null) {
                        image_preview.setImageBitmap(bitmap)
                        mBitmap = bitmap

                        //save the picture to file
                        //savePlayerEventBitmap(applicationContext, mPlayerInfoList[mCurrentClickPos].name, bitmap)
                    }
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
                mEvent.time = time
                mEvent.fee = fee
                mEvent.comments = comments

                if (switch_IsMajong.isChecked) {
                    mEvent.gametype = "1"
                } else {
                    mEvent.gametype = "0"
                }

                mEvent.photo = "0"
                if (mBitmap != null) {
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")// HH:mm:ss
                    val date = Date(System.currentTimeMillis())
                    val name = "GameRecord_event_" + simpleDateFormat.format(date)

                    val res = savePlayerEventBitmap(applicationContext, name, mBitmap as Bitmap)
                    if (res == 0) {
                        mEvent.photo = name
                    }
                }

                //TODO get position
                mEvent.position = "0"

                setIntentExtraFromEvent(intent, mEvent)

                setResult(RESULT_OK, intent)
                return true
            }
        } else {
            setResult(RESULT_CANCELED, intent)
            return true
        }

        return false
    }

    private fun choosePhoto() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)

    }

    private fun chooseCamera() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        startActivityForResult(intent, REQUEST_CODE_TAKE_IMAGE)
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
        const val REQUEST_CODE_TAKE_IMAGE = 102

        const val PERMISSIONS_REQUEST_READ_STORAGE = 6
    }
}
