package it.uniba.dib.sms222332.tools;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

public class QrGenerator {

    /**
     * Il metodo createQr riceve in input il nome della tesi che verrà inserito nel QR-code da generare.
     *
     * @param name nome della tesi di cui generare il QR-code
     * @return bitmap oggetto che conterrà la renderizzazione del QR-code
     */
    public static Bitmap createQr(String name) {

        int width = 700;
        int height = 700;
        // NEW
        JSONObject jsonDatiTesi = new JSONObject();
        Bitmap bitmap = null;

        try {
            jsonDatiTesi.put("name", name);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(jsonDatiTesi.toString(), BarcodeFormat.QR_CODE, width, height);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException | JSONException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
