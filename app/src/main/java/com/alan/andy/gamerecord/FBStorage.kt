package com.alan.andy.gamerecord

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import com.google.firebase.storage.StorageMetadata
import java.io.File
import android.support.annotation.NonNull
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FileDownloadTask
import java.util.*


class FBeStorage () {

    private var storage = FirebaseStorage.getInstance()

    private var storageRef = storage.reference


    public fun uploadFile(context: Context, loacalFile: File, isDatabase: Boolean) {
        //val uuid = UUID.randomUUID().toString();

        var dirname = "files/"

        val file = Uri.fromFile(loacalFile)

        if (isDatabase) {
            dirname = "database/"
        }

        // Upload file to path
        val uploadTask = storageRef.child(dirname + file.lastPathSegment).putFile(file)

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener{ taskSnapshot ->
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            Log.w(LOG_TAG, "Upload is $progress% done")
        }
        uploadTask.addOnPausedListener {
            Log.w(LOG_TAG, "Upload is paused")
            // Handle unsuccessful uploads
        }
        uploadTask.addOnFailureListener{
            Log.w(LOG_TAG, "Upload failed")
            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
        }
        uploadTask.addOnSuccessListener{
            Log.w(LOG_TAG, "Upload succeed")
            Toast.makeText(context, "Upload succeed", Toast.LENGTH_SHORT).show()
        }
    }

    public fun downloadFile(context: Context, localFile: File, isDatabase: Boolean) {

        //val uuid = UUID.randomUUID().toString();

        var dirname = "files/"

        if (isDatabase) {
            dirname = "database/"
        }

        val downloadTask = storageRef.child(dirname + localFile.name).getFile(localFile)

        downloadTask.addOnSuccessListener {
            Log.w(LOG_TAG, "Download succeed")
            Toast.makeText(context, "Download succeed", Toast.LENGTH_SHORT).show()
        }

        downloadTask.addOnFailureListener{
            Log.w(LOG_TAG, "Download failed")
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }
}