package it.uniba.dib.sms222332.tools;

import android.util.Log;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class PageNumeration extends PdfPageEventHelper {

    private static final String TAG = PageNumeration.class.getSimpleName();
    private static final Font FONT_FOOTER = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL, BaseColor.DARK_GRAY);

    PageNumeration() {
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        try {
            PdfPCell cell;
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1});

            //1st Column
            Anchor anchor = new Anchor(new Phrase("LaureApp", FONT_FOOTER));
            cell = new PdfPCell(anchor);

            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(0);
            cell.setPadding(2f);
            table.addCell(cell);
            table.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            table.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 5, writer.getDirectContent());

            //2nd Column
            cell = new PdfPCell(new Phrase("Page - ".concat(String.valueOf(writer.getPageNumber())), FONT_FOOTER));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(0);
            cell.setPadding(2f);
            table.addCell(cell);
            table.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            table.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 5, writer.getDirectContent());
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, ex.toString());
        }
    }
}

