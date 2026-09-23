package com.callshield.app.service

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.callshield.app.AegisApplication
import com.callshield.app.R

class CallDefenseTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        try {
            val repository = AegisApplication.repository
            val currentArmed = repository.isShieldArmed.value
            val newArmed = !currentArmed
            repository.setShieldArmed(newArmed)
            updateTileState()
        } catch (e: Exception) {
            // Aegis not yet initialized
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        try {
            val repository = AegisApplication.repository
            val isArmed = repository.isShieldArmed.value

            if (isArmed) {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "CallShield"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "ARMED • Defense Active"
                }
            } else {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "CallShield"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "DISARMED • Tap to Arm"
                }
            }

            // Set tile icon
            tile.icon = Icon.createWithResource(this, R.mipmap.ic_launcher)
            tile.updateTile()
        } catch (e: Exception) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "CallShield"
            tile.updateTile()
        }
    }

    companion object {
        fun requestTileUpdate(context: Context) {
            try {
                requestListeningState(context, ComponentName(context, CallDefenseTileService::class.java))
            } catch (e: Exception) {
                // Ignore if tile not placed
            }
        }
    }
}
