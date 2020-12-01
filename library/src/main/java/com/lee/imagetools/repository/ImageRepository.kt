package com.lee.imagetools.repository

import android.app.Application
import android.provider.MediaStore
import com.lee.imagetools.R
import com.lee.imagetools.constant.Constants
import com.lee.imagetools.entity.Album
import com.lee.imagetools.entity.Image
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
            MediaStore.Images.Media.DATA
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
                val cover =
                    cursor.getString(cursor.getColumnIndexOrThrow(projection[2]))
                if (!albumSet.contains(id)) {
                    albums.add(Album(id, name, cover))
                    albumSet.add(id)
                }
            } while (cursor.moveToPrevious())
        }
        val tempAlbum = albums[0].copy()
        val defaultAlbumName = application.getString(R.string.default_album_name)
        albums.add(0,
            Album(
                Constants.DEFAULT_ALBUM_ID,
                defaultAlbumName,
                tempAlbum.cover
            )
        )

        cursor.close()
        return albums
    }

    fun getImagesByAlbum(albumId: Long): List<Image> {
        val images = arrayListOf<Image>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
        )
        val cursor = if (albumId == Constants.DEFAULT_ALBUM_ID) {
            application.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED
            )
        } else {
            application.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Images.Media.BUCKET_ID + " =?",
                arrayOf(albumId.toString()),
                MediaStore.Images.Media.DATE_ADDED
            )
        } ?: return images

        if (cursor.moveToLast()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                val name = cursor.getString(cursor.getColumnIndex(projection[1]))
                val path = cursor.getString(cursor.getColumnIndex(projection[2]))
                val timestamp = cursor.getLong(cursor.getColumnIndex(projection[3]))
                images.add(
                    Image(
                        id,
                        name,
                        path,
                        timestamp
                    )
                )
            } while (cursor.moveToPrevious())
        }
        cursor.close()

        return images
    }

}