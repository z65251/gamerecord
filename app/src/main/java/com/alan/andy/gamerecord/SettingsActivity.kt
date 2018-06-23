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
import android.util.Log
import android.view.View
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
        private val mTitles: Array<String> = arrayOf(getString(R.string.settings_players), getString(R.string.settings_backup), getString(R.string.settings_restore))

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
}
