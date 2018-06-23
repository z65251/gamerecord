package com.alan.andy.gamerecord


import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.Cursor.FIELD_TYPE_NULL
import android.graphics.Bitmap
import android.util.Log
import android.graphics.BitmapFactory
import java.io.*


/**
 * Created by z00325866 on 2018/2/22.
 */

//Constant below here
const val LOG_TAG = "gamerecord"

fun getPersonRecordFromIntent(intent: Intent, hasId: Boolean): PersonInfo {

    var id = -1
    if (hasId) {
        id = intent.extras[ID].toString().toInt()
    }

    val time = intent.extras[COLUMN_TIME].toString()
    val name = intent.extras[COLUMN_NAME].toString()
    val balance = intent.extras[COLUMN_BALANCE].toString()
    val comments = intent.extras[COLUMN_COMMENTS].toString()

    return PersonInfo(id, time, name, balance, comments)
}

fun setIntentExtraFromPerson(intent: Intent, person: PersonInfo) {
    intent.putExtra(ID, person.id)
    intent.putExtra(COLUMN_TIME, person.time)
    intent.putExtra(COLUMN_NAME, person.name)
    intent.putExtra(COLUMN_BALANCE, person.balance)
    intent.putExtra(COLUMN_COMMENTS, person.comments)
}

fun setMapFromCursorForPerson(context: Context, map: HashMap<String, Any>, cursor: Cursor) {
    /* read all the readable info from table
    |----------------------------------------------------------|
    |_id(int)|time(text)|name(text)|balance(int)|comments(text)|
    |1       |2018-2-14 |zhuhaitao |100         |Haha          |
    |----------------------------------------------------------|
    */

    map[ID] = cursor.getInt(cursor.getColumnIndex(ID))
    map[COLUMN_TIME] = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
    map[COLUMN_NAME] = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
    map[COLUMN_BALANCE] = cursor.getInt(cursor.getColumnIndex(COLUMN_BALANCE)).toString()
    map[COLUMN_COMMENTS] = cursor.getString(cursor.getColumnIndex(COLUMN_COMMENTS))

    //get default face img by name
    map["face"] = getColorIdFromName(map[COLUMN_NAME].toString())
    val bitmap = readPlayerBitmap(context, map[COLUMN_NAME].toString())
    if (bitmap != null) {
        map["pic"] = bitmap
    } else {
        map["pic"] = "null"
    }
    //map["pic"] = readPlayerBitmap(context, map[COLUMN_NAME].toString()) as Any?
}

fun createNewPersonValues(person: PersonInfo): ContentValues {
    val values = ContentValues()

    values.put(COLUMN_TIME, person.time)
    values.put(COLUMN_NAME, person.name)
    values.put(COLUMN_BALANCE, person.balance.toInt())
    values.put(COLUMN_COMMENTS, person.comments)

    return values
}

fun setMapFromPersonValue(map: HashMap<String, Any>, person: PersonInfo) {
    map[ID] = person.id
    map[COLUMN_TIME] = person.time
    map[COLUMN_NAME] = person.name
    map[COLUMN_BALANCE] = person.balance
    map[COLUMN_COMMENTS] = person.comments
}

fun getPersonFromListData(listData: ArrayList<HashMap<String, Any>>, index: Int): PersonInfo {
    val id = listData.get(index)[ID].toString().toInt()
    val time = listData.get(index)[COLUMN_TIME].toString()
    val name = listData.get(index)[COLUMN_NAME].toString()
    val balance = listData.get(index)[COLUMN_BALANCE].toString()
    val comments = listData.get(index)[COLUMN_COMMENTS].toString()

    return PersonInfo(id, time, name, balance, comments)
}

fun setMapFromCursorForEvent(map: HashMap<String, Any>, cursor: Cursor) {
    /* read all the readable info from table
    |----------------------------------------------------------------------------------------------------------------------------------------------|
    |index(int)|time(text)|fee(int)|photo(binary)|position(text)|gametype(int) | comments(text)|
    |1         |2018-2-14 |200     |binary       |              |0 means poke  |in zhoupu      |
    |----------------------------------------------------------------------------------------------------------------------------------------------|
    */

    map[ID] = cursor.getInt(cursor.getColumnIndex(ID))
    map[COLUMN_TIME] = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
    map[COLUMN_FEE] = cursor.getInt(cursor.getColumnIndex(COLUMN_FEE)).toString()
    //map[COLUMN_PHOTO] = cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO))
    if (cursor.getType(cursor.getColumnIndex(COLUMN_POSITON)) == FIELD_TYPE_NULL) {
        map[COLUMN_POSITON] = ""
    } else {
        map[COLUMN_POSITON] = cursor.getString(cursor.getColumnIndex(COLUMN_POSITON))
    }
    if (cursor.getType(cursor.getColumnIndex(COLUMN_GAME_TYPE)) == FIELD_TYPE_NULL) {
        map[COLUMN_GAME_TYPE] = ""
    }else {
        map[COLUMN_GAME_TYPE] = cursor.getInt(cursor.getColumnIndex(COLUMN_GAME_TYPE)).toString()
    }

    if (cursor.getType(cursor.getColumnIndex(COLUMN_COMMENTS)) == FIELD_TYPE_NULL) {
        map[COLUMN_COMMENTS] = ""
    } else {
        map[COLUMN_COMMENTS] = cursor.getString(cursor.getColumnIndex(COLUMN_COMMENTS))
    }
}

fun setIntentExtraFromEvent(intent: Intent, event: EventInfo) {
    intent.putExtra(ID, event.id)
    intent.putExtra(COLUMN_TIME, event.time)
    intent.putExtra(COLUMN_FEE, event.fee)
    intent.putExtra(COLUMN_GAME_TYPE, event.gametype)
    intent.putExtra(COLUMN_PHOTO, event.photo)
    intent.putExtra(COLUMN_POSITON, event.position)
    intent.putExtra(COLUMN_COMMENTS, event.comments)
}

fun getEventRecordFromIntent(intent: Intent, hasId: Boolean): EventInfo {

    var id = -1
    if (hasId) {
        id = intent.extras[ID].toString().toInt()
    }

    val time = intent.extras[COLUMN_TIME].toString()
    val fee = intent.extras[COLUMN_FEE].toString()
    val gametype = intent.extras[COLUMN_GAME_TYPE].toString()
    val photo = intent.extras[COLUMN_PHOTO].toString()
    val position = intent.extras[COLUMN_POSITON].toString()
    val comments = intent.extras[COLUMN_COMMENTS].toString()

    return EventInfo(id, time, fee, gametype, photo, position, comments)
}

fun createNewEventValues(event: EventInfo): ContentValues {
    val values = ContentValues()

    values.put(COLUMN_TIME, event.time)
    values.put(COLUMN_FEE, event.fee.toInt())
    values.put(COLUMN_GAME_TYPE, event.gametype.toInt())
    values.put(COLUMN_PHOTO, event.photo)
    values.put(COLUMN_POSITON, event.position)
    values.put(COLUMN_COMMENTS, event.comments)

    return values
}

fun setMapFromEventValue(map: HashMap<String, Any>, event: EventInfo) {
    map[ID] = event.id
    map[COLUMN_TIME] = event.time
    map[COLUMN_FEE] = event.fee
    map[COLUMN_GAME_TYPE] = event.gametype
    map[COLUMN_PHOTO] = event.photo
    map[COLUMN_POSITON] = event.position
    map[COLUMN_COMMENTS] = event.comments
}

fun getEventFromListData(listData: ArrayList<HashMap<String, Any>>, index: Int): EventInfo {
    val id = listData.get(index)[ID].toString().toInt()
    val time = listData.get(index)[COLUMN_TIME].toString()
    val fee = listData.get(index)[COLUMN_FEE].toString()
    val gametype = listData.get(index)[COLUMN_GAME_TYPE].toString()
    val photo = listData.get(index)[COLUMN_PHOTO].toString()
    val position = listData.get(index)[COLUMN_POSITON].toString()
    val comments = listData.get(index)[COLUMN_COMMENTS].toString()

    return EventInfo(id, time, fee, gametype, photo, position, comments)
}

fun getColorIdFromName(name: String): Int {
    var colorId: Int = R.color.colorOrangeLight

    when (name) {
        "朱海涛" -> {
            colorId = R.color.ColorPurple
        }
        "姚满海" -> {
            colorId = R.color.colorGreenLight
        }
        "滕志辉" -> {
            colorId = R.color.colorRedDark
        }
        "冷洪超" -> {
            colorId = R.color.colorBlueLight
        }
    }

    return colorId
}

fun getNameList(context: Context): java.util.ArrayList<String> {
    val nameList = java.util.ArrayList<String>()

    val db = RecordDbHelper(context).readableDatabase

    val cursor = db.query(true,
            TABLE_NAME_PERSON, // The table to query
            arrayOf(COLUMN_NAME), // The columns to return
            null,
            null,
            null,
            null,
            null,
            null)

    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {

            val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            nameList.add(name)

            cursor.moveToNext()
        }
    }

    cursor.close()
    db.close()

    return nameList
}

fun getTimeList(context: Context): ArrayList<String> {
    val timeList = ArrayList<String>()

    val db = RecordDbHelper(context).readableDatabase

    val cursor = db.query(true,
            TABLE_NAME_PERSON, // The table to query
            arrayOf(COLUMN_TIME), // The columns to return
            null,
            null,
            null,
            null,
            null,
            null)

    if (cursor.moveToFirst()) {
        while (!cursor.isAfterLast) {

            val time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
            timeList.add(time)

            cursor.moveToNext()
        }
    }

    cursor.close()
    db.close()

    return timeList
}

fun savePlayerBitmap(context: Context, playername: String, bitmap: Bitmap): Int {

    val filepath = context.filesDir.absolutePath + "/" + playername + ".png"

    val file = File(filepath)
    try {
        file.createNewFile()
    } catch (e: IOException) {
        Log.w(LOG_TAG, "create file failed：" + e.toString())
    }

    var fOut: FileOutputStream? = null
    try {
        fOut = FileOutputStream(file)
    } catch (e: FileNotFoundException) {
        Log.w(LOG_TAG, "out stream failed：" + e.toString())
    }

    try {
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, fOut)
    } catch (e: Exception) {
        return -1
    }

    try {
        fOut!!.flush()
    } catch (e: IOException) {
        Log.w(LOG_TAG, "flush file failed：" + e.toString())
    }

    try {
        fOut!!.close()
    } catch (e: IOException) {
        Log.w(LOG_TAG, "close file failed：" + e.toString())
    }

    return 0
}

fun readPlayerBitmap(context: Context, playername: String): Bitmap? {

    val filepath = context.filesDir.absolutePath + "/" + playername + ".png"

    var fis: FileInputStream? = null
    try {
        fis = FileInputStream(filepath)
    } catch (e: FileNotFoundException) {
        Log.w(LOG_TAG, "file $filepath read failed failed：" + e.toString())
    }

    if (fis != null) {
        return BitmapFactory.decodeStream(fis)
    } else {
        return null
    }
}

fun getFitInSampleSize(reqWidth: Int, reqHeight: Int, options: BitmapFactory.Options): Int {
    var inSampleSize = 1

    if (options.outWidth > reqWidth || options.outHeight > reqHeight) {
        val widthRatio = Math.round(options.outWidth.toFloat() / reqWidth.toFloat())
        val heightRatio = Math.round(options.outHeight.toFloat() / reqHeight.toFloat())
        inSampleSize = Math.min(widthRatio, heightRatio)
    }

    return inSampleSize
}

@Throws(Exception::class)
fun getFitSampleBitmap(inputStream: InputStream, width: Int, height: Int): Bitmap? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true

    val bytes = readStream(inputStream) ?: return null

    //BitmapFactory.decodeStream(inputStream, null, options);
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    options.inSampleSize = getFitInSampleSize(width, height, options)
    options.inJustDecodeBounds = false
    //return BitmapFactory.decodeStream(inputStream, null, options);

    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}

@Throws(Exception::class)
fun readStream(inStream: InputStream): ByteArray? {
    val outStream = ByteArrayOutputStream()
    val buffer = ByteArray(10*1024*1024)
    var len = 0

    len = inStream.read(buffer)
    //TODO simple way use 10MB
    if (len > 10*1024*1024) {
        inStream.close()
        return null
    }

    outStream.write(buffer, 0, len)

    outStream.close()
    inStream.close()

    return outStream.toByteArray()
}