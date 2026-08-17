package com.example.copilot.util;

import com.example.copilot.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class PdfExtractorUtil {

    /**
     * Extracts plain text from a PDF file.
     * Returns empty string if the PDF is scanned/image-based (no extractable text).
     */
    public String extractText(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();

            if (bytes.length == 0) {
                throw new BadRequestException("The uploaded file is empty.");
            }

            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                if (text == null || text.isBlank()) {
                    log.warn("PDF '{}' contains no extractable text — may be scanned or image-based.",
                            file.getOriginalFilename());
                    return "";
                }

                return text.trim();
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to extract text from PDF '{}': {}", file.getOriginalFilename(), e.getMessage());
            throw new BadRequestException("Failed to process the PDF. Please ensure it is a valid, non-corrupted PDF file.");
        }
    }
}
