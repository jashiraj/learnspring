package com.ipdetails.pdfgenerator.controller;

import com.ipdetails.pdfgenerator.view.PdfViewer;
import com.ipdetails.pdfgenerator.model.Content;
import com.ipdetails.pdfgenerator.service.IpApiServiceGateway;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/ipapi")
@Log4j2
public class IpController {

    private IpApiServiceGateway details;
    private PdfViewer view;

    @Autowired
    public IpController(IpApiServiceGateway details , PdfViewer view) {
        this.details = details;
        this.view = view;
    }

    private static final List<String> supportedFormats = Arrays.asList("json", "jsonp", "xml", "csv", "yaml");

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getIpDetails(
            @RequestParam("ip") String ip,
            @RequestParam(value = "format", defaultValue = "json") String format
    ) {
        // Convert format to lowercase for comparison
        String formatLowerCase = format.toLowerCase();

        if (!supportedFormats.contains(formatLowerCase)) {
            // Return an error PDF with the error message embedded
            return generateErrorPdf("Invalid format. Supported formats: json, jsonp, xml, csv, yaml, pdf");
        }

        Content content = details.getInfoOfIp(ip, formatLowerCase);
        if (content == null) {
            // Return an error PDF with a custom error message
            return generateErrorPdf("Error fetching IP details from API.");
        }

        try {
            byte[] pdfContent = view.generatePdfFromResponse(content);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("ip_details.pdf", "ip_details.pdf");
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error generating PDF: " + e.getMessage());
            // Return an error PDF with a custom error message
            return generateErrorPdf("Error generating PDF: " + e.getMessage());
        }
    }
    private ResponseEntity<byte[]> generateErrorPdf(String errorMessage) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(errorMessage);
                contentStream.endText();
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            document.close();

            byte[] pdfContent = byteArrayOutputStream.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("error.pdf", "error.pdf");
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("Error generating error PDF: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
