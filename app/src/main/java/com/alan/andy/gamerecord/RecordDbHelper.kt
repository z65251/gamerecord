package com.alan.andy.gamerecord

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase.CursorFactory




/**
 * Created by z00325866 on 2018/2/13.
 */

/* Table name is person, structure as following
|----------------------------------------------------------------------------------------------------------------------------------------------|
|index(int)|time(text)|name(text)|balance(int)|comments(text)|
|1         |2018-2-14 |zhuhaitao |100         |Haha          |
|----------------------------------------------------------------------------------------------------------------------------------------------|
*/
const val TABLE_NAME_PERSON = "person"
const val COLUMN_TIME = "time"
const val COLUMN_NAME = "name"
const val COLUMN_BALANCE= "balance"
const val COLUMN_COMMENTS = "comments"

/* Table name is person, structure as following
|----------------------------------------------------------------------------------------------------------------------------------------------|
|index(int)|time(text)|fee(int)|photo(binary)|position(text)|gametype(int) |comments(text) |
|1         |2018-2-14 |200     |binary       |              |0 means poke  |in zhoupu      |
|----------------------------------------------------------------------------------------------------------------------------------------------|
*/
const val TABLE_NAME_EVENT = "event"
//const val COLUMN_TIME = "time"
const val COLUMN_FEE = "fee"
const val COLUMN_GAME_TYPE = "gametype"
const val COLUMN_PHOTO = "photo"
const val COLUMN_POSITON = "position"
//const val COLUMN_COMMENTS = "comments"

const val ID = "_id"
const val TEXT_TYPE = " TEXT"
const val INT_TYPE = " INTEGER"
const val BINARY_TYPE = " BINARY"
const val COMMA_SEP = ","

const val SQL_CREATE_PERSON = "CREATE TABLE " + TABLE_NAME_PERSON + " (" +
        ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        COLUMN_TIME + TEXT_TYPE + COMMA_SEP +
        COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
        COLUMN_BALANCE + INT_TYPE + COMMA_SEP +
        COLUMN_COMMENTS + TEXT_TYPE + " )"

const val SQL_CREATE_EVENT = "CREATE TABLE " + TABLE_NAME_EVENT + " (" +
        ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        COLUMN_TIME + TEXT_TYPE + COMMA_SEP +
        COLUMN_FEE + INT_TYPE + COMMA_SEP +
        COLUMN_GAME_TYPE + INT_TYPE + COMMA_SEP +
        COLUMN_PHOTO + BINARY_TYPE + COMMA_SEP +
        COLUMN_POSITON + TEXT_TYPE + COMMA_SEP +
        COLUMN_COMMENTS + TEXT_TYPE + " )"

const val SQL_DELETE_PERSON = "DROP TABLE IF EXISTS " + TABLE_NAME_PERSON
const val SQL_DELETE_EVENT = "DROP TABLE IF EXISTS " + TABLE_NAME_EVENT


class RecordDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_PERSON)
        db.execSQL(SQL_CREATE_EVENT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //just rebuild
        db.execSQL(SQL_DELETE_PERSON)
        db.execSQL(SQL_DELETE_EVENT)

        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "gamerecord.db"
    }
}

//object of 1 person person row info
data class PersonInfo(var id: Int = -1, var time: String = "", var name: String = "", var balance: String = "", var comments:String = "")

//object of 1 event info
data class EventInfo(var id: Int = -1, var time: String = "", var fee: String = "", var gametype: String = "", var photo: String = "", var position: String = "", var comments:String = "")