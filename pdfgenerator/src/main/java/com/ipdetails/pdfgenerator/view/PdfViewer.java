package com.ipdetails.pdfgenerator.view;

import com.ipdetails.pdfgenerator.model.Content;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

@Component
public class PdfViewer {

    public byte[] generatePdfFromResponse(Content content) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);

        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
            // Use the "Helvetica" font with "WinAnsiEncoding" encoding
            contentStream.setFont(PDType1Font.HELVETICA, 12);

            // Draw the header "IP DETAILS"
            contentStream.beginText();
            contentStream.newLineAtOffset(50, page.getMediaBox().getHeight() - 30);
            contentStream.showText("IP details of the mentioned IP is as follows ");
            contentStream.endText();

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float tableHeight = 20f;
            float rowHeight = 15f;
            float cellMargin = 2f;

            // Helper method to add content to the PDF table
            // Each cell will have a label and its corresponding value
            // Add as many rows as needed for your content
            // Adjust the yPosition for each new row
            // Use cellWidth for the width of each cell
            // You can experiment with the values to adjust the layout
            String[] labels = {"IP:", "City:", "Network:", "Country Population:", "Currency:", "Postal:", "Languages:", "Country TLD:",
                    "Version:", "Region:", "Country:", "Country Name:", "Country Code:", "Country Capital:", "Country TLD:", "Continent Code:",
                    "Timezone:", "UTC Offset:", "Country Calling Code:", "Org:", "Hostname:", "ASN:"};
            String[] values = {content.getIp(), content.getCity(), content.getNetwork(), String.valueOf(content.getCountry_population()),
                    content.getCurrency(), content.getPostal(), content.getLanguages(), content.getCountry_tld(), content.getVersion(), content.getRegion(),
                    content.getCountry(), content.getCountry_name(), content.getCountry_code(), content.getCountry_capital(), content.getCountry_tld(), content.getContinent_code(),
                    content.getTimezone(), content.getUtc_offset(), content.getCountry_calling_code(), content.getOrg(), content.getHostname(), content.getAsn()};
            float cellWidth = tableWidth / 2;

            for (int i = 0; i < labels.length; i++) {
                // Check for null or empty values and provide a default value if needed
                String label = labels[i];
                String value = values[i];
                if (value == null || value.isEmpty()) {
                    value = "N/A"; // Replace with any default value you prefer
                }

                drawTableRow(contentStream, margin, yPosition, tableWidth, tableHeight, cellWidth, rowHeight, cellMargin, label, value);
                yPosition -= rowHeight;
            }
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        document.save(byteArrayOutputStream);
        document.close();

        return byteArrayOutputStream.toByteArray();
    }

    private void drawTableRow(PDPageContentStream contentStream, float x, float y, float tableWidth, float tableHeight,
                              float cellWidth, float rowHeight, float cellMargin, String label, String value) throws IOException {
        // Draw the table cell borders
        contentStream.setLineWidth(1f);
        contentStream.drawLine(x, y, x + tableWidth, y);
        contentStream.drawLine(x, y - tableHeight, x + tableWidth, y - tableHeight);
        contentStream.drawLine(x, y, x, y - tableHeight);
        contentStream.drawLine(x + tableWidth, y, x + tableWidth, y - tableHeight);

        // Draw the table cell content (label and value)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(x + cellMargin, y - rowHeight + cellMargin);
        contentStream.showText(label);
        contentStream.endText();

        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(x + cellWidth + cellMargin, y - rowHeight + cellMargin);
        contentStream.showText(value);
        contentStream.endText();
    }
}
