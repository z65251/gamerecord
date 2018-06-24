package com.alan.andy.gamerecord

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_settings.*
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import com.alan.andy.gamerecord.R.layout.item_settings


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings_recycler_view.layoutManager = LinearLayoutManager(this)
        settings_recycler_view.adapter = SettingsRecyclerViewAdapter(this)
    }

    inner class SettingsRecyclerViewAdapter(context: Context) : RecyclerView.Adapter<SettingsRecyclerViewAdapter.SettingsViewHolder>() {
        private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)
        private val mTitles: Array<String> = arrayOf(getString(R.string.settings_players),
                getString(R.string.settings_backup_cloud),
                getString(R.string.settings_restore_cloud),
                getString(R.string.settings_backup_sd),
                getString(R.string.settings_restore_sd))

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
            return SettingsViewHolder(mLayoutInflater.inflate(item_settings, parent, false))
        }

        override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
            holder.mTextView.text = mTitles[position]
        }

        override fun getItemCount(): Int {
            return mTitles.size
        }

        inner class SettingsViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
            var mTextView: TextView = view.findViewById(R.id.text_view)

            init {

                view.setOnClickListener { _ ->
                    Log.d("NormalTextViewHolder", "onClick--> position = $layoutPosition")

                    when (layoutPosition) {
                        0 -> moveToPlayersSettings()

                        1 -> AlertDialog.Builder(this@SettingsActivity)
                                //.setIcon(R.id.icon)
                                .setTitle(R.string.backup_database_cloud)
                                .setPositiveButton(R.string.text_yes) { _, _ ->
                                    backupRestorDbFileToCloud(true)
                                }
                                .setNegativeButton(R.string.text_no, null).create()
                                .show()

                        2 -> AlertDialog.Builder(this@SettingsActivity)
                                //.setIcon(R.id.icon)
                                .setTitle(R.string.restore_database_cloud)
                                .setPositiveButton(R.string.text_yes) { _, _ ->
                                    backupRestorDbFileToCloud(false)
                                }
                                .setNegativeButton(R.string.text_no, null).create()
                                .show()
                        3 -> AlertDialog.Builder(this@SettingsActivity)
                                //.setIcon(R.id.icon)
                                .setTitle(R.string.backup_database_sdcard)
                                .setPositiveButton(R.string.text_yes) { _, _ ->
                                    backupRestorDbFileToSd(true)
                                }
                                .setNegativeButton(R.string.text_no, null).create()
                                .show()
                        4 -> AlertDialog.Builder(this@SettingsActivity)
                                //.setIcon(R.id.icon)
                                .setTitle(R.string.restore_database_sdcard)
                                .setPositiveButton(R.string.text_yes) { _, _ ->
                                    backupRestorDbFileToSd(false)
                                }
                                .setNegativeButton(R.string.text_no, null).create()
                                .show()
                    }
                }
            }
        }
    }

    private fun moveToPlayersSettings() {
        val intent = Intent()

        intent.setClass(this, PlayersSettingActivity::class.java)

        startActivity(intent)
    }

    private fun backupRestorDbFileToCloud(isBackup: Boolean) {
        val fBeStorage = FBeStorage()

        val dbFile = applicationContext.getDatabasePath(RecordDbHelper.DATABASE_NAME)

        if (isBackup) {
            fBeStorage.uploadFile(applicationContext, dbFile, true)
        } else {
            fBeStorage.downloadFile(applicationContext, dbFile, true)
        }
    }

    private fun backupRestorDbFileToSd(isBackup: Boolean) {

        val dbFile = applicationContext.getDatabasePath(RecordDbHelper.DATABASE_NAME)
        val bkFile = applicationContext.getExternalFilesDir(RecordDbHelper.DATABASE_NAME)

        if (isBackup) {
            dbFile.copyTo(bkFile, true)
            Toast.makeText(this, "Backup succeed", Toast.LENGTH_SHORT).show()

        } else {
            bkFile.copyTo(dbFile, true)
            Toast.makeText(this, "Restore succeed", Toast.LENGTH_SHORT).show()
        }
    }
}
