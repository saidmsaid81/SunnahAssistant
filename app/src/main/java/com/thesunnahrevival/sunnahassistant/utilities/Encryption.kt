package com.thesunnahrevival.sunnahassistant.utilities

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Encryption {
    fun encrypt(dataToEncrypt: ByteArray, password: String): Map<String, ByteArray> {
        val random = SecureRandom()
        val salt = ByteArray(256)
        random.nextBytes(salt)
        val pbKeySpec = PBEKeySpec(password.toCharArray(), salt, 1324, 256)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val initializationVectorRandom = SecureRandom()
        val initializationVector = ByteArray(16)
        initializationVectorRandom.nextBytes(initializationVector)
        val initializationVectorSpec = IvParameterSpec(initializationVector)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, initializationVectorSpec)
        val encrypted = cipher.doFinal(dataToEncrypt)
        val map = mutableMapOf<String, ByteArray>()
        map["salt"] = salt
        map["iv"] = initializationVector
        map["encrypted"] = encrypted
        return map
    }

    fun decrypt(map: Map<String, ByteArray>, password: String): ByteArray? {
        val salt = map["salt"]
        val initializationVector = map["iv"]
        val encrypted = map["encrypted"]

        val pbKeySpec = PBEKeySpec(password.toCharArray(), salt, 1324, 256)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        val keySpec = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val initializationVectorSpec = IvParameterSpec(initializationVector)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, initializationVectorSpec)
        return cipher.doFinal(encrypted)
    }
}