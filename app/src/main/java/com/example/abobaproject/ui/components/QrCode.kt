package com.example.abobaproject.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/**
 * Генерирует Bitmap с QR-кодом для переданного текста.
 */
fun generateQrBitmap(text: String, size: Int = 512): Bitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,                  // отступ от краёв (0-4)
        EncodeHintType.CHARACTER_SET to "UTF-8"
    )
    val matrix: BitMatrix = MultiFormatWriter().encode(
        text,
        BarcodeFormat.QR_CODE,
        size,
        size,
        hints
    )
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

/**
 * Composable-обёртка: рисует QR-код как Image.
 * Пересчитывает Bitmap только при изменении content.
 */
@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    sizePx: Int = 512
) {
    val bitmap = remember(content) { generateQrBitmap(content, sizePx) }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR-код для приглашения партнёра",
        modifier = modifier
    )
}