package com.imagetools.select.repository

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import com.imagetools.select.R
import com.imagetools.select.constant.Constants
import com.imagetools.select.entity.Album
import com.imagetools.select.entity.Image
import java.util.*

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal class ImageRepository(private val application: Application) {

    fun getAlbums(): List<Album> {
        val albums = arrayListOf<Album>()
        val albumSet = HashSet<Long>()
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media._ID
        )
        val cursor = application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED
        )
            ?: return albums

        if (cursor.count == 0) return albums
        if (cursor.moveToLast()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(projection[0]))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(projection[1]))
                val coverId = cursor.getLong(cursor.getColumnIndexOrThrow(projection[2]))
                val coverUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    coverId
                )
                if (!albumSet.contains(id)) {
                    albums.add(Album(id, name, coverUri))
                    albumSet.add(id)
                }
            } while (cursor.moveToPrevious())
        }
        val tempAlbum = albums[0].copy()
        val defaultAlbumName = application.getString(R.string.default_album_name)
        albums.add(
            0,
            Album(
                Constants.DEFAULT_ID,
                defaultAlbumName,
                tempAlbum.coverUri
            )
        )

        cursor.close()
        return albums
    }

    fun getImagesByAlbum(albumId: Long, page: Int = 0): List<Image> {
        val images = arrayListOf<Image>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID
        )
        val cursor = application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            if (albumId == Constants.DEFAULT_ID) null else MediaStore.Images.Media.BUCKET_ID + " =?",
            if (albumId == Constants.DEFAULT_ID) null else arrayOf(albumId.toString()),
            "${MediaStore.Images.Media.DATE_ADDED} desc"
//            "${MediaStore.Images.Media.DATE_ADDED} desc limit ${page * Constants.PAGE_COUNT},${Constants.PAGE_COUNT}"
        )
            ?: return images

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                val uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                images.add(Image(id, uri))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return images
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun androidR() {
//        application.contentResolver.query(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            arrayOf("projection"),
//            createSqlQueryBundle(null, null, null),
//            null
//        )
//    }
//
//    private fun createSqlQueryBundle(
//        selection: String?,
//        selectionArgs: Array<String?>?,
//        sortOrder: String?, limitCount: Int = 0, offset: Int = 0
//    ): Bundle? {
//        if (selection == null && selectionArgs == null && sortOrder == null) {
//            return null
//        }
//        val queryArgs = Bundle()
//        if (selection != null) {
//            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
//        }
//        if (selectionArgs != null) {
//            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
//        }
//        if (sortOrder != null) {
//            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
//        }
//        queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, "$limitCount offset $offset")
//        return queryArgs
//    }

}