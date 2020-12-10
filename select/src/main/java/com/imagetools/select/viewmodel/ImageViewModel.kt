package com.imagetools.select.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.imagetools.select.R
import com.imagetools.select.constant.Constants
import com.imagetools.select.entity.Album
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.LoadStatus
import com.imagetools.select.entity.PageNumber
import com.imagetools.select.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal class ImageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository by lazy {
        ImageRepository(
            application
        )
    }

    private val page = PageNumber(limit = 0)
    private var activeAlbumId = 0L

    var albumId: Long = Constants.DEFAULT_ALBUM_ID
    var albumName: String = application.getString(R.string.default_album_name)

    val albumsLiveData by lazy { MutableLiveData<List<Album>>() }
    val imagesLiveData by lazy { MutableLiveData<List<Image>>() }

    fun getAlbums() {
        viewModelScope.launch {
            albumsLiveData.value = withContext(Dispatchers.IO) { repository.getAlbums() }
        }
    }

    fun getImages(@LoadStatus status: Int) {
        activeAlbumId = albumId
        viewModelScope.launch {
            imagesLiveData.value = withContext(Dispatchers.IO) {
                repository.getImagesByAlbum(
                    albumId,
                    page.getPage(status)
                )
            }
        }
    }

    fun isCurrentAlbum(): Boolean {
        return activeAlbumId == albumId
    }

}