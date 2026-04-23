package util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;

public class QRCodeGenerator {

    public static BufferedImage generateQR(String text) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    250,
                    250
            );

            return MatrixToImageWriter.toBufferedImage(matrix);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}