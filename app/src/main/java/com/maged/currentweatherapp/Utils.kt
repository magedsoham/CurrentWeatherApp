package com.maged.currentweatherapp

import android.annotation.SuppressLint
import android.content.Context
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.InvalidParameterSpecException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by MagedSoham on 21/09/2021
 */
object Utils {
    private val RANDOM: Random = SecureRandom()
    private const val ITERATIONS = 1000
    private const val KEY_LENGTH = 256 // bits
    private var saltGlobal: String? = null
    private var SecretKey: SecretKey? = null
    private var Iv: IvParameterSpec? = null
    private var iv = ByteArray(16)
    private var Ciphertext: ByteArray? = null

    fun getNextSalt() {
        val salt = ByteArray(16)
        RANDOM.nextBytes(salt)
        saltGlobal = salt.contentToString()
    }

    @Throws(InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    fun hashAPIkey(password: String) {
        val passwordChars = password.toCharArray()
        val saltBytes = saltGlobal?.toByteArray()
        val spec = PBEKeySpec(
            passwordChars,
            saltBytes,
            ITERATIONS,
            KEY_LENGTH
        )

        /* Derive the key, given password and salt. */
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val tmp = factory.generateSecret(spec)
        SecretKey = SecretKeySpec(tmp.encoded, "AES")
    }

    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidParameterSpecException::class,
        UnsupportedEncodingException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        InvalidKeyException::class
    )
    fun encrypt(context: Context) {

        /* encrypt the message. */
        @SuppressLint("GetInstance") val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKey)
        RANDOM.nextBytes(iv)
        Iv =
            IvParameterSpec(iv)
        Ciphertext = cipher.doFinal(
            context.getString(R.string.parse_application_id).toByteArray(
                StandardCharsets.UTF_8
            )
        )
    }

    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        UnsupportedEncodingException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class
    )
    fun decrypt(): String {
        /* decrypt the message, given derived key and initialization vector. */
        @SuppressLint("GetInstance") val cipher = Cipher.getInstance("AES")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKey,
            Iv
        )
        return String(
            cipher.doFinal(Ciphertext),
            StandardCharsets.UTF_8
        )
    }


}