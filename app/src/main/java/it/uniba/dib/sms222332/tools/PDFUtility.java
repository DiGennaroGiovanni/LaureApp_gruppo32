package it.uniba.dib.sms222332.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Objects;

import it.uniba.dib.sms222332.R;

final public class PDFUtility {

    private static final String TAG = PDFUtility.class.getSimpleName();
    private static final Font FONT_TITLE = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static final Font FONT_CELL = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    private static final Font FONT_COLUMN = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL);

    public interface OnDocumentClose {
        void onPDFDocumentClose(File file);
    }

    public static void createPdf(@NonNull Context mContext, OnDocumentClose mCallback, LinkedHashMap<String, String> items, boolean isPortrait, File pdfFile) throws Exception {

        Document document = new Document();
        document.setMargins(24f, 24f, 32f, 32f);
        document.setPageSize(isPortrait ? PageSize.A4 : PageSize.A4.rotate());

        OutputStream outputStream = new FileOutputStream(pdfFile, false);
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        pdfWriter.setFullCompression();
        pdfWriter.setPageEvent(new PageNumeration());

        document.open();

        setMetaData(document);

        addHeader(mContext, document, items);
        addEmptyLine(document, 3);

        document.add(createDataTable(items));

        document.close();

        try {
            pdfWriter.close();
        } catch (Exception ex) {
            Log.e(TAG, "Error While Closing pdfWriter : " + ex);
        }

        if (mCallback != null) {
            mCallback.onPDFDocumentClose(pdfFile);
        }
    }

    private static void addEmptyLine(Document document, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            document.add(new Paragraph(" "));
        }
    }

    private static void setMetaData(Document document) {
        document.addCreationDate();
        //document.add(new Meta("",""));
        document.addAuthor("Asse FGC");
        document.addCreator("Asse FGC");
        document.addHeader("DEVELOPER", "Gruppo Asse FGC");
    }

    private static void addHeader(Context mContext, Document document, LinkedHashMap<String, String> items) throws Exception {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 7, 2});
        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        // Questa è la riga di intestazione del pdf che contiene 3 colonne:
        // nella prima e nella terza c'è il logo dell'app
        // nella seconda c'è il titolo del pdf
        PdfPCell cell;
        {
            /*LEFT TOP LOGO*/
            Drawable d = ContextCompat.getDrawable(mContext, R.drawable.icon_laurea_app);
            Bitmap bmp = ((BitmapDrawable) Objects.requireNonNull(d)).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

            Image logo = Image.getInstance(stream.toByteArray());
            logo.setWidthPercentage(80);
            logo.scaleToFit(105, 55);

            cell = new PdfPCell(logo);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setUseAscender(true);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(2f);
            table.addCell(cell);
        }

        {
            /*MIDDLE TEXT*/
            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(8f);
            cell.setPaddingTop(25f);
            cell.setUseAscender(true);

            Paragraph temp = new Paragraph(mContext.getString(R.string.pdf_title), FONT_TITLE);
            temp.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(temp);

            table.addCell(cell);
        }
        /* RIGHT TOP LOGO*/
        {
            Bitmap bitmap = QrGenerator.createQr(items.get("Name"));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            Image logo = Image.getInstance(stream.toByteArray());
            logo.setWidthPercentage(80);
            logo.scaleToFit(105, 55);

            cell = new PdfPCell(logo);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setUseAscender(true);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(2f);
            table.addCell(cell);
        }

        document.add(table);
    }

    private static PdfPTable createDataTable(LinkedHashMap<String, String> dataTable) throws DocumentException {
        PdfPTable table1 = new PdfPTable(2);
        table1.setWidthPercentage(100);
        table1.setWidths(new float[]{1f, 2f});
        table1.setHeaderRows(1);
        table1.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table1.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell;
        {
            cell = new PdfPCell(new Phrase("Thesis name", FONT_COLUMN));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4f);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(dataTable.get("Name"), FONT_COLUMN));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4f);
            table1.addCell(cell);
        }

        float top_bottom_Padding = 8f;
        float left_right_Padding = 8f;
        boolean alternate = false;

        BaseColor lt_gray = new BaseColor(221, 221, 221); //#DDDDDD
        BaseColor cell_color;
        dataTable.remove("Name");

        try {
            for (LinkedHashMap.Entry<String, String> entry : dataTable.entrySet()) {
                cell_color = alternate ? lt_gray : BaseColor.WHITE;


                cell = new PdfPCell(new Phrase(entry.getKey(), FONT_CELL));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPaddingLeft(left_right_Padding);
                cell.setPaddingRight(left_right_Padding);
                cell.setPaddingTop(top_bottom_Padding);
                cell.setPaddingBottom(top_bottom_Padding);
                cell.setBackgroundColor(cell_color);
                table1.addCell(cell);

                if (entry.getValue().equals("")) cell = new PdfPCell(new Phrase("-", FONT_CELL));
                else cell = new PdfPCell(new Phrase(entry.getValue(), FONT_CELL));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPaddingLeft(left_right_Padding);
                cell.setPaddingRight(left_right_Padding);
                cell.setPaddingTop(top_bottom_Padding);
                cell.setPaddingBottom(top_bottom_Padding);
                cell.setBackgroundColor(cell_color);
                table1.addCell(cell);

                alternate = !alternate;
            }

            } catch (Exception e) {
                Log.e("ERRORE", e.getMessage());
        }


        return table1;
    }

/*    private static Image getImage(byte[] imageByte, boolean isTintingRequired) throws BadElementException, IOException {
        Paint paint = new Paint();
        if (isTintingRequired) {
            paint.setColorFilter(new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN));
        }
        Bitmap input = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(input, 0, 0, paint);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        output.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image image = Image.getInstance(stream.toByteArray());
        image.setWidthPercentage(80);
        return image;
    }*/

}
