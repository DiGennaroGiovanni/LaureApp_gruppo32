package it.uniba.dib.sms222332.tools;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class ThesisPDF implements PDFUtility.OnDocumentClose{

    private static final String TAG = ThesisPDF.class.getSimpleName();

    public void makePdf(Context context, Map<String, String> datiTesi) {

        try {
            File outputFile = new File(context.getExternalFilesDir(null), datiTesi.get("Name") + ".pdf");
            LinkedHashMap<String,String> lhDatiTesi = convertMaptoLinkedHashMap(datiTesi);
            PDFUtility.createPdf(context, ThesisPDF.this, lhDatiTesi, true, outputFile);
        } catch (Exception e) {
            Log.e(TAG, "Errore nella creazione del pdf");
        }
    }

    private LinkedHashMap<String, String> convertMaptoLinkedHashMap(Map<String, String> datiTesi) {

        LinkedHashMap<String, String> lhDatiTesi = new LinkedHashMap<>();
        lhDatiTesi.put("Name", datiTesi.get("Name"));
        lhDatiTesi.put("Type", datiTesi.get("Type"));
        lhDatiTesi.put("Faculty", datiTesi.get("Faculty"));
        lhDatiTesi.put("Professor", datiTesi.get("Professor"));
        lhDatiTesi.put("Correlator", datiTesi.get("Correlator"));
        lhDatiTesi.put("Description", datiTesi.get("Description"));
        lhDatiTesi.put("Estimated Time", datiTesi.get("Estimated Time"));
        lhDatiTesi.put("Required Exam", datiTesi.get("Required Exam"));
        lhDatiTesi.put("Average", datiTesi.get("Average"));
        lhDatiTesi.put("Related Project", datiTesi.get("Related Projects"));

        return lhDatiTesi;
    }

    @Override
    public void onPDFDocumentClose(File file) {
        if (file.exists()) Log.d(TAG, "File pdf creato.");
    }

}
