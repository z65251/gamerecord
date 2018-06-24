package com.alan.andy.gamerecord

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import com.alan.andy.gamerecord.R.layout.item_playersetting
import kotlinx.android.synthetic.main.activity_players_setting.*
import android.widget.ImageButton
import android.content.Intent
import android.widget.Toast
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.app.Activity
import android.provider.MediaStore


class PlayersSettingActivity : AppCompatActivity() {

    private var mPlayerInfoList = arrayListOf<PlayerInfo>()
    private var mCurrentClickPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_players_setting)

        players_settings_recycler_view.layoutManager = GridLayoutManager(this, 2)
        players_settings_recycler_view.adapter = PlayerSettingRecyclerViewAdapter(this)

        //init player info
        initData()

    }

    public override fun onActivityResult(req: Int, res: Int, intent: Intent?) {
        when (req) {

            REQUEST_CODE_PICK_IMAGE -> {
                if (res == RESULT_OK) {

                    val uri = intent?.data

                    //val bit = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                    val bitmap = getFitSampleBitmap(contentResolver.openInputStream(uri), 160, 160)
                    if (bitmap !=  null) {
                        mPlayerInfoList[mCurrentClickPos].picture = bitmap

                        players_settings_recycler_view.adapter?.notifyItemChanged(mCurrentClickPos)

                        //save the picture to file
                        savePlayerEventBitmap(applicationContext, mPlayerInfoList[mCurrentClickPos].name, bitmap)
                    }
                }
            }

            REQUEST_CODE_TAKE_IMAGE -> {
                if (res == Activity.RESULT_OK) {

                    val bundle = intent?.extras
                    val bitmap = bundle?.get("data") as Bitmap?
                    if (bitmap != null) {
                        mPlayerInfoList[mCurrentClickPos].picture = bitmap

                        players_settings_recycler_view.adapter?.notifyItemChanged(mCurrentClickPos)

                        //save the picture to file
                        savePlayerEventBitmap(applicationContext, mPlayerInfoList[mCurrentClickPos].name, bitmap)
                    }
                }
            }

        }
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

    inner class PlayerSettingRecyclerViewAdapter(context: Context) : RecyclerView.Adapter<PlayerSettingRecyclerViewAdapter.PlayerSettingViewHolder>() {
        private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerSettingViewHolder {
            return PlayerSettingViewHolder(mLayoutInflater.inflate(item_playersetting, parent, false))
        }

        override fun onBindViewHolder(holder: PlayerSettingViewHolder, position: Int) {

            holder.mTextView.text = mPlayerInfoList[position].name

            if (mPlayerInfoList[position].picture != null) {
                holder.mImageView.clearColorFilter()
                holder.mImageView.setImageBitmap(mPlayerInfoList[position].picture)
            } else {
                holder.mImageView.setColorFilter(mPlayerInfoList[position].colorId)
            }
        }

        override fun getItemCount(): Int {
            return mPlayerInfoList.size
        }

        inner class PlayerSettingViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
            var mTextView: TextView = view.findViewById(R.id.text_player_name)
            var mImageView: ImageView = view.findViewById(R.id.image_player_picture)
            var mButton_Gallery: ImageButton = view.findViewById(R.id.button_choose_gallery)
            var mButton_Camera: ImageButton = view.findViewById(R.id.button_choose_camera)

            init {
                view.setOnClickListener { _ ->
                    Log.d("NormalTextViewHolder", "onClick--> position = $layoutPosition")
                }

                view.setOnLongClickListener { _ ->
                    Log.d("NormalTextViewHolder", "onLongClick--> position = $layoutPosition")
                    true
                }

                mButton_Gallery.setOnClickListener { _ ->
                    Log.d("NormalTextViewHolder", "Gallery Button onClick--> position = $layoutPosition")
                    mCurrentClickPos = layoutPosition

                    if (ContextCompat.checkSelfPermission(this@PlayersSettingActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(this@PlayersSettingActivity,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                PERMISSIONS_REQUEST_READ_STORAGE);

                    }else {
                        choosePhoto()
                    }
                }

                mButton_Camera.setOnClickListener { _ ->
                    Log.d("NormalTextViewHolder", "Camera Button onClick--> position = $layoutPosition")
                    mCurrentClickPos = layoutPosition

                    chooseCamera()
                }
            }
        }
    }

    private fun initData() {
        val nameList = getNameList(this)

        for (name in nameList) {
            var bitmap = readPlayerEventBitmap(applicationContext, name)
            val colorId = getColorIdFromName(name)
            val playerinfo = PlayerInfo(name, bitmap, colorId)
            mPlayerInfoList.add(playerinfo)
        }
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

    data class PlayerInfo(val name: String, var picture: Bitmap?, val colorId: Int)

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
        const val REQUEST_CODE_TAKE_IMAGE = 102

        const val PERMISSIONS_REQUEST_READ_STORAGE = 6
        //const val PERMISSIONS_REQUEST_TAKE_PICTURE = 7
    }
}
