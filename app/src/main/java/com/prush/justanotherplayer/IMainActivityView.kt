package com.prush.justanotherplayer

import androidx.appcompat.app.AppCompatActivity
import com.prush.justanotherplayer.model.Track

interface IMainActivityView {

    fun displayLibraryTracks(trackList: MutableList<Track>)

    fun displayEmptyLibrary()

    fun displayError()

    fun getViewActivity(): AppCompatActivity

    fun showPermissionRationale(permission: String)
}
