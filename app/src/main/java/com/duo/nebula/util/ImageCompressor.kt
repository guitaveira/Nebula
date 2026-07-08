package com.duo.nebula.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * Converte a Uri de uma imagem escolhida pelo usuário (galeria) em um array
 * de bytes JPEG comprimido, pronto para envio ao Cloud Storage.
 */
object ImageCompressor {

    private const val MAX_DIMENSION = 1280
    private const val JPEG_QUALITY = 82

    fun compress(context: Context, uri: Uri): ByteArray? {
        val original = decodeBitmap(context, uri) ?: return null
        val scaled = scaleDown(original)
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        return output.toByteArray()
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val largestSide = maxOf(bitmap.width, bitmap.height)
        if (largestSide <= MAX_DIMENSION) return bitmap
        val ratio = MAX_DIMENSION.toFloat() / largestSide
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
