package com.callshield.app.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.callshield.app.data.local.entity.FilterRule
import com.callshield.app.data.local.entity.RuleType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupImportResult(
    val isSuccess: Boolean,
    val importedRules: List<FilterRule> = emptyList(),
    val errorMessage: String? = null
)

object RuleBackupManager {

    private const val BACKUP_VERSION = 1
    private const val DEFAULT_PASS_PHRASE = "CallShield-Autonomous-Defense-Grid-2026"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val ITERATIONS = 10000

    /**
     * Exports active rules into an encrypted .callshield archive file.
     */
    fun exportBackup(
        context: Context,
        rules: List<FilterRule>,
        password: String? = null
    ): File {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(exportsDir, "CallShield_Backup_$timeStamp.callshield")

        // 1. Build JSON Payload
        val rootJson = JSONObject()
        rootJson.put("app", "CallShield AI")
        rootJson.put("version", BACKUP_VERSION)
        rootJson.put("exportedAt", System.currentTimeMillis())

        val rulesArray = JSONArray()
        for (r in rules) {
            val ruleObj = JSONObject()
            ruleObj.put("name", r.name)
            ruleObj.put("pattern", r.pattern)
            ruleObj.put("ruleType", r.ruleType.name)
            ruleObj.put("description", r.description)
            ruleObj.put("isEnabled", r.isEnabled)
            ruleObj.put("isBuiltIn", r.isBuiltIn)
            ruleObj.put("matchCount", r.matchCount)
            if (r.expiresAt != null) {
                ruleObj.put("expiresAt", r.expiresAt)
            }
            rulesArray.put(ruleObj)
        }
        rootJson.put("rules", rulesArray)

        val jsonString = rootJson.toString(2)
        val plaintextBytes = jsonString.toByteArray(StandardCharsets.UTF_8)

        // 2. Encrypt Payload using AES-GCM
        val pass = password?.takeIf { it.isNotBlank() } ?: DEFAULT_PASS_PHRASE
        val encryptedBytes = encryptPayload(plaintextBytes, pass)

        FileOutputStream(backupFile).use { fos ->
            fos.write(encryptedBytes)
        }

        return backupFile
    }

    /**
     * Imports rules from a .callshield archive.
     */
    fun importBackup(
        inputStream: InputStream,
        password: String? = null
    ): BackupImportResult {
        return try {
            val encryptedBytes = inputStream.readBytes()
            val pass = password?.takeIf { it.isNotBlank() } ?: DEFAULT_PASS_PHRASE

            val decryptedBytes = decryptPayload(encryptedBytes, pass)
            val jsonString = String(decryptedBytes, StandardCharsets.UTF_8)

            val rootJson = JSONObject(jsonString)
            if (!rootJson.has("rules")) {
                return BackupImportResult(isSuccess = false, errorMessage = "Invalid .callshield archive format")
            }

            val rulesArray = rootJson.getJSONArray("rules")
            val importedList = mutableListOf<FilterRule>()

            for (i in 0 until rulesArray.length()) {
                val obj = rulesArray.getJSONObject(i)
                val name = obj.getString("name")
                val pattern = obj.getString("pattern")
                val typeStr = obj.optString("ruleType", RuleType.PREFIX.name)
                val ruleType = try { RuleType.valueOf(typeStr) } catch (e: Exception) { RuleType.PREFIX }
                val description = obj.optString("description", "")
                val isEnabled = obj.optBoolean("isEnabled", true)
                val isBuiltIn = obj.optBoolean("isBuiltIn", false)
                val matchCount = obj.optInt("matchCount", 0)
                val expiresAt = if (obj.has("expiresAt")) obj.getLong("expiresAt") else null

                importedList.add(
                    FilterRule(
                        name = name,
                        pattern = pattern,
                        ruleType = ruleType,
                        description = description,
                        isEnabled = isEnabled,
                        isBuiltIn = isBuiltIn,
                        matchCount = matchCount,
                        expiresAt = expiresAt
                    )
                )
            }

            BackupImportResult(isSuccess = true, importedRules = importedList)
        } catch (e: Exception) {
            BackupImportResult(
                isSuccess = false,
                errorMessage = if (e.message?.contains("MAC") == true) "Incorrect password for encrypted backup" else "Failed to parse .callshield backup: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Creates a Share Intent for the .callshield backup file.
     */
    fun createShareIntent(context: Context, backupFile: File): Intent {
        val authority = "${context.packageName}.fileprovider"
        val fileUri: Uri = FileProvider.getUriForFile(context, authority, backupFile)

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "CallShield Encrypted Rule Backup (.callshield)")
            putExtra(Intent.EXTRA_TEXT, "Attached is a portable CallShield encrypted rule configuration archive.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // --- AES-256-GCM ENCRYPTION HELPERS ---

    private fun encryptPayload(plaintext: ByteArray, passPhrase: String): ByteArray {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }

        val key = deriveKey(passPhrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val ciphertext = cipher.doFinal(plaintext)

        // Structure: [Salt: 16 bytes] + [IV: 12 bytes] + [Ciphertext + AuthTag]
        val result = ByteArray(salt.size + iv.size + ciphertext.size)
        System.arraycopy(salt, 0, result, 0, salt.size)
        System.arraycopy(iv, 0, result, salt.size, iv.size)
        System.arraycopy(ciphertext, 0, result, salt.size + iv.size, ciphertext.size)

        return result
    }

    private fun decryptPayload(encrypted: ByteArray, passPhrase: String): ByteArray {
        if (encrypted.size < 28) {
            throw IllegalArgumentException("Corrupted backup archive payload")
        }

        val salt = ByteArray(16)
        val iv = ByteArray(GCM_IV_LENGTH)
        val ciphertextLength = encrypted.size - salt.size - iv.size
        val ciphertext = ByteArray(ciphertextLength)

        System.arraycopy(encrypted, 0, salt, 0, salt.size)
        System.arraycopy(encrypted, salt.size, iv, 0, iv.size)
        System.arraycopy(encrypted, salt.size + iv.size, ciphertext, 0, ciphertextLength)

        val key = deriveKey(passPhrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return cipher.doFinal(ciphertext)
    }

    private fun deriveKey(passPhrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passPhrase.toCharArray(), salt, ITERATIONS, 256)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, "AES")
    }
}
