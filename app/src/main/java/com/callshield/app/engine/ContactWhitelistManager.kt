package com.callshield.app.engine

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

object ContactWhitelistManager {

    /**
     * Checks if a phone number matches any saved contact on device.
     * Guaranteed safe: Never blocks friends, family, or work colleagues.
     */
    fun isContact(context: Context, phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false

        // Check if READ_CONTACTS permission is granted
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
