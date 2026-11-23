package com.epdcl.webdisplay.service.security;


import org.apache.pdfbox.cos.COSStream;
import java.io.InputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Base64;

@Service
public class DocumentSecurityService {

    private static final Logger LOGGER =
            Logger.getLogger(DocumentSecurityService.class.getName());

    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final int MAX_PDF_PAGES = 20;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;
    private static final int CLAM_CHUNK_SIZE = 64 * 1024;
    private static final int MAX_SCANNER_RESPONSE = 8192;
    private static final int MAX_PDF_EMBEDDED_FILES = 20;
    private static final int MAX_PDF_EMBEDDED_FILE_BYTES = 5 * 1024 * 1024;
    private static final long MAX_PDF_EMBEDDED_TOTAL_BYTES = 10L * 1024 * 1024;

    private static final Set<String> UNSAFE_PDF_KEYS = Set.of(
            "OpenAction",
            "AA",
            "JavaScript",
            "JS",
            "EmbeddedFiles",
            "Launch",
            "RichMedia",
            "SubmitForm",
            "ImportData",
            "GoToR",
            "URI",
            "AcroForm",
            "XFA",
            "Perms",
            "Collection",
            "AF",
            "EF",
            "Sound",
            "Movie",
            "3D",
            "Rendition",
            "Metadata",
            "PieceInfo"
    );

    private final String clamHost;
    private final int clamPort;
    private final int timeoutMs;

    public DocumentSecurityService(
            @Value("${app.document.scan.enabled:true}") boolean scanEnabled,
            @Value("${app.document.scan.clamav-host:127.0.0.1}") String clamHost,
            @Value("${app.document.scan.clamav-port:3310}") int clamPort,
            @Value("${app.document.scan.timeout-ms:10000}") int timeoutMs) {

        if (!scanEnabled) {
            throw new IllegalStateException(
                    "Document uploads cannot be enabled without mandatory antivirus scanning.");
        }

        if (clamHost == null || clamHost.isBlank()
                || clamPort < 1 || clamPort > 65535
                || timeoutMs < 1000) {
            throw new IllegalStateException(
                    "Invalid ClamAV configuration.");
        }

        this.clamHost = clamHost;
        this.clamPort = clamPort;
        this.timeoutMs = timeoutMs;
    }

//    public ValidatedDocument validateAndScan(MultipartFile file) {
//
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException(
//                    "Please upload an ID document.");
//        }
//
//        if (file.getSize() > MAX_BYTES) {
//            throw new IllegalArgumentException(
//                    "File size must not exceed 5 MB.");
//        }
//
//        String originalName = file.getOriginalFilename();
//
//        if (originalName == null || originalName.isBlank()) {
//            throw new IllegalArgumentException(
//                    "Invalid document filename.");
//        }
//
//        // Handle both Windows and Unix-style path separators.
//        String safeName = originalName.replace('\\', '/');
//        safeName = safeName.substring(safeName.lastIndexOf('/') + 1);
//
//        int dot = safeName.lastIndexOf('.');
//
//        if (dot <= 0 || dot == safeName.length() - 1) {
//            throw new IllegalArgumentException(
//                    "Only PDF, JPG, JPEG and PNG files are allowed.");
//        }
//
//        String extension = safeName.substring(dot + 1)
//                .toLowerCase(Locale.ROOT);
//
//        if (!Set.of("pdf", "jpg", "jpeg", "png").contains(extension)) {
//            throw new IllegalArgumentException(
//                    "Only PDF, JPG, JPEG and PNG files are allowed.");
//        }
//
//        final byte[] originalBytes;
//
//        try {
//            originalBytes = file.getBytes();
//        } catch (IOException ex) {
//            throw new IllegalArgumentException(
//                    "Unable to read the uploaded document.", ex);
//        }
//
//        if (originalBytes.length == 0 || originalBytes.length > MAX_BYTES) {
//            throw new IllegalArgumentException(
//                    "Invalid document size.");
//        }
//
//        String detectedType;
//
//        try {
//            detectedType = validateStructure(originalBytes, extension);
//        } catch (Exception ex) {
//            throw new IllegalArgumentException(
//                    "The uploaded file is not a valid document.", ex);
//        }

//        validateDeclaredContentType(file.getContentType(), detectedType);
//
//        // Scan the original before processing it.
//        scanWithClamAv(originalBytes);


    public ValidatedDocument validateAndScan(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload an ID document.");
        }

        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "File size must not exceed 5 MB.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid document filename.");
        }

        String safeName = originalName.replace('\\', '/');
        safeName = safeName.substring(safeName.lastIndexOf('/') + 1);

        int dot = safeName.lastIndexOf('.');

        if (dot <= 0 || dot == safeName.length() - 1) {
            throw new IllegalArgumentException(
                    "Only PDF, JPG, JPEG and PNG files are allowed.");
        }

        String extension = safeName.substring(dot + 1)
                .toLowerCase(Locale.ROOT);

        if (!Set.of("pdf", "jpg", "jpeg", "png").contains(extension)) {
            throw new IllegalArgumentException(
                    "Only PDF, JPG, JPEG and PNG files are allowed.");
        }

        try {
            return validateAndScanBytes(
                    file.getBytes(),
                    extension,
                    file.getContentType()
            );
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Unable to read the uploaded document.", ex);
        }
    }

    /**
     * Validates a Base64 document without requiring a filename.
     * The actual format is identified from the decoded file bytes.
     */
    public ValidatedDocument validateBase64AndScan(String base64Data) {

        if (base64Data == null || base64Data.isBlank()) {
            throw new IllegalArgumentException(
                    "Base64 document data is required.");
        }

        String encoded = base64Data.trim();

        // Support data URLs such as data:image/png;base64,...
        if (encoded.startsWith("data:")) {

            int comma = encoded.indexOf(',');

            if (comma < 0
                    || !encoded.substring(0, comma)
                    .toLowerCase(Locale.ROOT).endsWith(";base64")) {
                throw new IllegalArgumentException(
                        "Invalid Base64 data URL.");
            }

            encoded = encoded.substring(comma + 1);
        }

        // Maximum Base64 length for a 5 MB decoded file.
        long maxEncodedLength = 4L * ((MAX_BYTES + 2L) / 3L);

        if (encoded.length() > maxEncodedLength) {
            throw new IllegalArgumentException(
                    "File size must not exceed 5 MB.");
        }

        final byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid Base64 document data.", ex);
        }

        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid document size. Maximum allowed size is 5 MB.");
        }

        // Identify the format using actual file signatures.
        String extension;

        if (bytes.length >= 8
                && (bytes[0] & 255) == 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G'
                && bytes[4] == 13
                && bytes[5] == 10
                && bytes[6] == 26
                && bytes[7] == 10) {

            extension = "png";

        } else if (bytes.length >= 3
                && (bytes[0] & 255) == 0xFF
                && (bytes[1] & 255) == 0xD8
                && (bytes[2] & 255) == 0xFF) {

            extension = "jpg";

        } else if (bytes.length >= 8
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-') {

            extension = "pdf";

        } else {
            throw new IllegalArgumentException(
                    "Unsupported or unrecognized document format. "
                            + "Only PDF, JPG, JPEG and PNG are allowed.");
        }

        return validateAndScanBytes(bytes, extension, null);
    }

    /**
     * Shared security pipeline for multipart and Base64 uploads.
     */

    private ValidatedDocument validateAndScanBytes(
            byte[] originalBytes,
            String extension,
            String declaredContentType) {

        if (originalBytes == null
                || originalBytes.length == 0
                || originalBytes.length > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid document size.");
        }

        String detectedType;

        try {
            detectedType = validateStructure(originalBytes, extension);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "The uploaded file is not a valid document.", ex);
        }

//        validateDeclaredContentType(declaredContentType, detectedType);
//
//    // Scan original bytes.
//        scanWithClamAv(originalBytes);
//
//        // Scan original bytes.
//        LOGGER.warning("DOCUMENT SECURITY: Scanning original uploaded bytes. Size: "
//                + originalBytes.length);
//
//        scanWithClamAv(originalBytes);

        validateDeclaredContentType(declaredContentType, detectedType);

// Scan embedded files separately before PDF sanitization removes them.
        if ("application/pdf".equals(detectedType)) {
            scanEmbeddedPdfFiles(originalBytes);
        }

// Scan the complete original document.
        LOGGER.warning("DOCUMENT SECURITY: Scanning original uploaded bytes. Size: "
                + originalBytes.length);

        scanWithClamAv(originalBytes);

        final byte[] cleanedBytes;

        try {
            if ("application/pdf".equals(detectedType)) {
                cleanedBytes = cleanPdf(originalBytes);
            } else {
                cleanedBytes = cleanImage(originalBytes, detectedType);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "The document could not be safely processed. "
                            + "Please upload another copy.",
                    ex);
        }

        if (cleanedBytes.length == 0 || cleanedBytes.length > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "The processed document exceeds the allowed size.");
        }

        // Validate processed file again.
        try {
            String cleanedType = validateStructure(
                    cleanedBytes,
                    extension);

            if (!detectedType.equals(cleanedType)) {
                throw new IllegalArgumentException(
                        "Processed document format mismatch.");
            }

        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "The processed document failed security validation.",
                    ex);
        }

        // Scan final processed bytes.
        scanWithClamAv(cleanedBytes);

        return new ValidatedDocument(cleanedBytes, detectedType);
    }


//    final byte[] cleanedBytes;

//        try {
//            if ("application/pdf".equals(detectedType)) {
//                cleanedBytes = cleanPdf(originalBytes);
//            } else {
//                cleanedBytes = cleanImage(originalBytes, detectedType);
//            }
//        } catch (Exception ex) {
//            throw new IllegalArgumentException(
//                    "The document could not be safely processed. Please upload another copy.",
//                    ex);
//        }
//
//        if (cleanedBytes.length == 0 || cleanedBytes.length > MAX_BYTES) {
//            throw new IllegalArgumentException(
//                    "The processed document exceeds the allowed size.");
//        }
//
//        // Validate the newly generated file again.
//        try {
//            String cleanedType = validateStructure(
//                    cleanedBytes,
//                    extension);
//
//            if (!detectedType.equals(cleanedType)) {
//                throw new IllegalArgumentException(
//                        "Processed document format mismatch.");
//            }
//        } catch (Exception ex) {
//            throw new IllegalArgumentException(
//                    "The processed document failed security validation.",
//                    ex);
//        }
//
//        // Scan the final stored version too.
//        scanWithClamAv(cleanedBytes);
//
//        return new ValidatedDocument(cleanedBytes, detectedType);
//    }

    private void validateDeclaredContentType(
            String declared,
            String detected) {

        if (declared == null || declared.isBlank()
                || "application/octet-stream".equalsIgnoreCase(declared)) {
            return;
        }

        boolean match =
                ("application/pdf".equals(detected)
                        && "application/pdf".equalsIgnoreCase(declared))
                        || ("image/jpeg".equals(detected)
                        && ("image/jpeg".equalsIgnoreCase(declared)
                        || "image/jpg".equalsIgnoreCase(declared)))
                        || ("image/png".equals(detected)
                        && "image/png".equalsIgnoreCase(declared));

        if (!match) {
            throw new IllegalArgumentException(
                    "The file content does not match its declared content type.");
        }
    }

    private String validateStructure(byte[] bytes, String extension)
            throws Exception {

        // PDF signature validation.
        if (bytes.length >= 8
                && bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-') {

            if (!"pdf".equals(extension)) {
                throw new IllegalArgumentException("Extension mismatch.");
            }

            try (PDDocument pdf = Loader.loadPDF(bytes)) {

                int pages = pdf.getNumberOfPages();

                if (pages < 1 || pages > MAX_PDF_PAGES) {
                    throw new IllegalArgumentException(
                            "PDF must contain between 1 and "
                                    + MAX_PDF_PAGES + " pages.");
                }
            }

            return "application/pdf";
        }

        // PNG signature validation.
        if (bytes.length >= 8
                && (bytes[0] & 255) == 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G'
                && bytes[4] == 13
                && bytes[5] == 10
                && bytes[6] == 26
                && bytes[7] == 10) {

            if (!"png".equals(extension)) {
                throw new IllegalArgumentException("Extension mismatch.");
            }

            validateImageStructure(bytes);
            return "image/png";
        }

        // JPEG signature validation.
        if (bytes.length >= 4
                && (bytes[0] & 255) == 0xFF
                && (bytes[1] & 255) == 0xD8) {

            if (!"jpg".equals(extension)
                    && !"jpeg".equals(extension)) {
                throw new IllegalArgumentException("Extension mismatch.");
            }

            validateImageStructure(bytes);
            return "image/jpeg";
        }

        throw new IllegalArgumentException(
                "Unrecognized file signature.");
    }

    private void validateImageStructure(byte[] bytes)
            throws IOException {

        try (ImageInputStream input =
                     ImageIO.createImageInputStream(
                             new ByteArrayInputStream(bytes))) {

            if (input == null) {
                throw new IllegalArgumentException(
                        "Unreadable image.");
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "Unsupported image encoding.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(input, true, true);

                reader.addIIOReadWarningListener((source, warning) -> {
                    throw new IllegalArgumentException(
                            "Image data is damaged or incomplete.");
                });

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width < 1 || height < 1
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IllegalArgumentException(
                            "Image dimensions exceed the allowed limit.");
                }

                BufferedImage decoded = reader.read(0);

                if (decoded == null) {
                    throw new IllegalArgumentException(
                            "Image cannot be decoded.");
                }

                decoded.flush();

            } finally {
                reader.dispose();
            }
        }
    }

    private byte[] cleanImage(byte[] original, String contentType)
            throws IOException {

        BufferedImage image;

        try (ImageInputStream input =
                     ImageIO.createImageInputStream(
                             new ByteArrayInputStream(original))) {

            if (input == null) {
                throw new IllegalArgumentException(
                        "Unable to read image.");
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "Unsupported image.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(input, true, true);
                image = reader.read(0);

                if (image == null) {
                    throw new IllegalArgumentException(
                            "Unable to decode image.");
                }

            } finally {
                reader.dispose();
            }
        }

        String format = "image/png".equals(contentType) ? "png" : "jpg";

        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            boolean written = ImageIO.write(image, format, output);

            if (!written) {
                throw new IllegalArgumentException(
                        "Image re-encoding is unavailable.");
            }

            return output.toByteArray();

        } finally {
            image.flush();
        }
    }


    /**
     * Extracts embedded PDF files and scans their decoded contents
     * independently using ClamAV.
     *
     * This is necessary because ClamAV may not detect malware inside
     * every embedded PDF attachment when scanning the parent PDF.
     */

    private void scanEmbeddedPdfFiles(byte[] pdfBytes) {

        try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {

            Set<COSBase> visited =
                    Collections.newSetFromMap(new IdentityHashMap<>());

            long[] totalBytes = {0};
            int[] fileCount = {0};

            scanEmbeddedPdfObjects(
                    pdf.getDocumentCatalog().getCOSObject(),
                    visited,
                    totalBytes,
                    fileCount
            );

        } catch (IOException ex) {

            throw new IllegalArgumentException(
                    "Unable to inspect embedded files in the PDF.",
                    ex
            );
        }
    }

    private void scanEmbeddedPdfObjects(
            COSBase object,
            Set<COSBase> visited,
            long[] totalBytes,
            int[] fileCount) throws IOException {

        if (object == null) {
            return;
        }

        if (object instanceof COSObject indirect) {
            scanEmbeddedPdfObjects(
                    indirect.getObject(),
                    visited,
                    totalBytes,
                    fileCount
            );
            return;
        }

        if (!visited.add(object)) {
            return;
        }

        if (object instanceof COSStream stream) {

            COSBase type = stream.getDictionaryObject(COSName.TYPE);

            if (type instanceof COSName typeName
                    && "EmbeddedFile".equals(typeName.getName())) {

                fileCount[0]++;

                if (fileCount[0] > MAX_PDF_EMBEDDED_FILES) {
                    throw new IllegalArgumentException(
                            "PDF contains too many embedded files.");
                }

                byte[] embeddedBytes;

                try (InputStream input = stream.createInputStream()) {

                    embeddedBytes = input.readNBytes(
                            MAX_PDF_EMBEDDED_FILE_BYTES + 1
                    );
                }

                if (embeddedBytes.length == 0
                        || embeddedBytes.length > MAX_PDF_EMBEDDED_FILE_BYTES) {

                    throw new IllegalArgumentException(
                            "An embedded PDF file exceeds the allowed size.");
                }

                totalBytes[0] += embeddedBytes.length;

                if (totalBytes[0] > MAX_PDF_EMBEDDED_TOTAL_BYTES) {
                    throw new IllegalArgumentException(
                            "Total embedded PDF content exceeds the allowed limit.");
                }

                LOGGER.info(
                        "Scanning embedded PDF file. Size: "
                                + embeddedBytes.length + " bytes");

                // Scan the extracted attachment independently.
                scanWithClamAv(embeddedBytes);
            }
        }

        if (object instanceof COSDictionary dictionary) {

            for (COSName key : dictionary.keySet()) {

                scanEmbeddedPdfObjects(
                        dictionary.getItem(key),
                        visited,
                        totalBytes,
                        fileCount
                );
            }
        }

        if (object instanceof COSArray array) {

            for (int i = 0; i < array.size(); i++) {

                scanEmbeddedPdfObjects(
                        array.get(i),
                        visited,
                        totalBytes,
                        fileCount
                );
            }
        }
    }

    private byte[] cleanPdf(byte[] original)
            throws IOException {

        try (PDDocument pdf = Loader.loadPDF(original);
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            if (pdf.getNumberOfPages() < 1
                    || pdf.getNumberOfPages() > MAX_PDF_PAGES) {
                throw new IllegalArgumentException(
                        "PDF page limit exceeded.");
            }

            Set<COSBase> visited =
                    Collections.newSetFromMap(
                            new IdentityHashMap<>());

            // Remove risky actions, attachments, forms and metadata
            // throughout the PDF object structure.
            sanitizeObject(
                    pdf.getDocumentCatalog().getCOSObject(),
                    visited);

            // Remove annotations from pages. This also removes
            // hyperlinks, comments and interactive page controls.
            for (PDPage page : pdf.getPages()) {
                page.getCOSObject().removeItem(
                        COSName.getPDFName("Annots"));
            }

            pdf.save(output);

            byte[] cleaned = output.toByteArray();

            if (cleaned.length == 0 || cleaned.length > MAX_BYTES) {
                throw new IllegalArgumentException(
                        "Cleaned PDF exceeds the allowed size.");
            }

            return cleaned;
        }
    }

    /**
     * Recursively examines PDF dictionaries and arrays.
     * The visited set prevents circular references.
     */
    private void sanitizeObject(
            COSBase object,
            Set<COSBase> visited) {

        if (object == null) {
            return;
        }

        if (object instanceof COSObject indirect) {
            sanitizeObject(indirect.getObject(), visited);
            return;
        }

        if (!visited.add(object)) {
            return;
        }

        if (object instanceof COSDictionary dictionary) {

            List<COSName> keys =
                    new ArrayList<>(dictionary.keySet());

            for (COSName key : keys) {

                String keyName = key.getName();

                if (UNSAFE_PDF_KEYS.contains(keyName)
                        || "A".equals(keyName)
                        || "AA".equals(keyName)) {

                    dictionary.removeItem(key);
                    continue;
                }

                sanitizeObject(dictionary.getItem(key), visited);
            }

        } else if (object instanceof COSArray array) {

            for (int i = 0; i < array.size(); i++) {
                sanitizeObject(array.get(i), visited);
            }
        }
    }

    private void scanWithClamAv(byte[] bytes) {

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(clamHost, clamPort),
                    timeoutMs);

            socket.setSoTimeout(timeoutMs);

            DataOutputStream output =
                    new DataOutputStream(socket.getOutputStream());

            // INSTREAM sends uncompressed data in length-prefixed chunks.
            output.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));

            for (int offset = 0; offset < bytes.length; ) {

                int length = Math.min(
                        CLAM_CHUNK_SIZE,
                        bytes.length - offset);

                output.writeInt(length);
                output.write(bytes, offset, length);

                offset += length;
            }

            // Zero-length chunk ends the stream.
            output.writeInt(0);
            output.flush();

            ByteArrayOutputStream response =
                    new ByteArrayOutputStream();

            int value;

            while ((value = socket.getInputStream().read()) != -1
                    && value != 0) {

                if (response.size() >= MAX_SCANNER_RESPONSE) {
                    throw new IllegalStateException(
                            "Antivirus response exceeded the allowed size.");
                }

                response.write(value);
            }

            if (value != 0) {
                throw new IllegalStateException(
                        "Antivirus scanner closed without a complete response.");
            }

            String result = response.toString(
                    StandardCharsets.UTF_8).trim();
            LOGGER.warning("CLAMAV RESPONSE RECEIVED: [" + result + "]");

            if (result.endsWith("FOUND")) {
                throw new IllegalArgumentException(
                        "The uploaded document was blocked because malware was detected.");
            }

            if (!result.endsWith("OK")) {
                throw new IllegalStateException(
                        "Antivirus scanner returned an inconclusive result.");
            }

        } catch (IllegalArgumentException ex) {

            LOGGER.warning(
                    "DOCUMENT SCAN REJECTED: " + ex.getMessage());

            throw ex;

        } catch (Exception ex) {

            LOGGER.log(
                    Level.SEVERE,
                    "DOCUMENT SCAN FAILURE",
                    ex);

            throw new IllegalStateException(
                    "Document security scanning is temporarily unavailable. "
                            + "The upload was blocked for your protection. "
                            + "Please try again later or contact the administrator.",
                    ex);
        }
    }

    // This record must remain at class level, outside all methods.
    public record ValidatedDocument(
            byte[] bytes,
            String contentType) {
    }
}