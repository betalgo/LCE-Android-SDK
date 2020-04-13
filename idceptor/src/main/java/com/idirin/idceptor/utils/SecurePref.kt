package com.idirin.idceptor.utils

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Base64
import androidx.preference.PreferenceManager
import org.kxml2.wap.Wbxml
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Created by
 * idirin on 9.11.2018...
 */

class SecurePref(context: Context) : SharedPreferences {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA1"
        private const val CONTENT_RESOLVER_NAME = "android_id"
        private const val AES_ALGORITHM = "AES"
        private const val CHARSET_NAME = "UTF-8"
        private lateinit var sFile: SharedPreferences
        private lateinit var sKey: ByteArray


        private fun encrypt(cipherText: String): String {
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(1, SecretKeySpec(sKey, AES_ALGORITHM))
            return encode(cipher.doFinal(cipherText.toByteArray(charset(CHARSET_NAME))))
        }

        private fun decrypt(cipherText: String): String {
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(2, SecretKeySpec(sKey, AES_ALGORITHM))
            return String(cipher.doFinal(decode(cipherText)), charset(CHARSET_NAME))
        }

        private fun encode(input: ByteArray): String {
            return Base64.encodeToString(input, 3)
        }

        private fun decode(input: String): ByteArray {
            return Base64.decode(input, 3)
        }

        @Throws(InvalidKeySpecException::class, NoSuchAlgorithmException::class)
        private fun generateAesKeyName(context: Context): String {
            return encode(SecretKeyFactory.getInstance(ALGORITHM).generateSecret(PBEKeySpec(context.packageName.toCharArray(), Settings.Secure.getString(context.contentResolver, CONTENT_RESOLVER_NAME).toByteArray(), 1000, 256)).encoded)
        }

        @Throws(NoSuchAlgorithmException::class)
        private fun generateAesKeyValue(): String {
            val random = SecureRandom()
            val generator = KeyGenerator.getInstance(AES_ALGORITHM)
            try {
                generator.init(256, random)
            } catch (e: Exception) {
                try {
                    generator.init(Wbxml.EXT_0, random)
                } catch (e2: Exception) {
                    generator.init(128, random)
                }

            }

            return encode(generator.generateKey().encoded)
        }
    }

    private class Editor : SharedPreferences.Editor {

        private val mEditor: SharedPreferences.Editor = sFile.edit()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value == null) return this
            this.mEditor.putString(encrypt(key), encrypt(value))
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            if (values == null) return this
            val encryptedValues = HashSet<String>(values.size)
            for (value in values) {
                encryptedValues.add(encrypt(value))
            }
            this.mEditor.putStringSet(encrypt(key), encryptedValues)
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            this.mEditor.putString(encrypt(key), encrypt(value.toString()))
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            this.mEditor.putString(encrypt(key), encrypt(value.toString()))
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            this.mEditor.putString(encrypt(key), encrypt(value.toString()))
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            this.mEditor.putString(encrypt(key), encrypt(value.toString()))
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            this.mEditor.remove(encrypt(key))
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            this.mEditor.clear()
            return this
        }

        override fun commit(): Boolean {
            return this.mEditor.commit()
        }

        override fun apply() {
            this.mEditor.apply()
        }
    }

    init {
        sFile = PreferenceManager.getDefaultSharedPreferences(context)
        val key = generateAesKeyName(context)
        var value = sFile.getString(key, null)
        if (value == null) {
            value = generateAesKeyValue()
            sFile.edit().putString(key, value).apply()
        }
        sKey = decode(value)
    }

    fun getRandomKey(): String {
        return generateAesKeyValue()
    }

    override fun contains(key: String): Boolean {
        return sFile.contains(encrypt(key))
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val encryptedValue = sFile.getString(encrypt(key), null)
        if (encryptedValue != null) {
            return decrypt(encryptedValue).toBoolean()
        }
        return defValue
    }

    override fun getString(key: String, defValue: String?): String? {
        val encryptedValue = sFile.getString(encrypt(key), null)
        return if (encryptedValue != null) decrypt(encryptedValue) else defValue
    }

    override fun getInt(key: String, defValue: Int): Int {
        val encryptedValue = sFile.getString(encrypt(key), null)
        if (encryptedValue != null) {
            return decrypt(encryptedValue).toInt()
        }
        return defValue
    }

    override fun getAll(): MutableMap<String, String> {
        val encryptedMap = sFile.all
        val decryptedMap = HashMap<String, String>(encryptedMap.size)
        for ((key, value) in encryptedMap) {
            decryptedMap[decrypt(key as String)] = decrypt(value.toString())
        }
        return decryptedMap
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor()
    }

    override fun getLong(key: String, defValue: Long): Long {
        val encryptedValue = sFile.getString(encrypt(key), null)
        if (encryptedValue != null) {
            return decrypt(encryptedValue).toLong()
        }
        return defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        val encryptedValue = sFile.getString(encrypt(key), null)
        if (encryptedValue != null) {
            return decrypt(encryptedValue).toFloat()
        }
        return defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        val encryptedSet = sFile.getStringSet(encrypt(key), null) ?: return defValues
        val decryptedSet = HashSet<String>(encryptedSet.size)
        for (encryptedValue in encryptedSet) {
            decryptedSet.add(decrypt(encryptedValue))
        }
        return decryptedSet
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sFile.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sFile.unregisterOnSharedPreferenceChangeListener(listener)
    }


}




