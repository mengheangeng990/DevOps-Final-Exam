package net.orderzone.idcard.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;

public final class BarcodeGenerator {

    private BarcodeGenerator() {
    }

    public static BufferedImage generateCode128(String text, int width, int height) {
        return generateBarcode(text, BarcodeFormat.CODE_128, width, height);
    }

    public static BufferedImage generateEan13(String text, int width, int height) {
        return generateBarcode(text, BarcodeFormat.EAN_13, width, height);
    }

    private static BufferedImage generateBarcode(String text, BarcodeFormat format, int width, int height) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(text, format, width, height);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new IllegalStateException("Unable to generate barcode", e);
        }
    }
}
