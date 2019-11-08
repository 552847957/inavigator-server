package org.apache.pdfbox.tests;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

public class PdfBoxToImageTest {

    @Test
    public void test1() throws IOException, PrinterException {
        String filename = "src/test/resources/data/PdfBoxToImageTest/jrc108255_blockchain_in_education.pdf";
        String outPathName = "test/tmp/directory/PdfBoxToImageTest";
        String outFileName = outPathName + "/jrc108255_blockchain_in_education";
        directoryReset(outPathName);

/*
        Detector dt = new DefaultDetector();
        Metadata md = new Metadata();
        MediaType mt;
        FileInputStream fis = new FileInputStream(filename);
        try {
            mt = dt.detect(fis, md);
        } finally {
            fis.close();
        }
        System.out.println(mt);
*/


        toThumbnail(filename, outFileName);
    }

    public void toThumbnail(String filename, String outFileName) throws IOException {
        PDDocument document = PDDocument.load(new File(filename));
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        int page = 0;
        try {
            //BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
            BufferedImage bim = pdfRenderer.renderImage(page, 0.25f);
            //ImageIOUtil.writeImage(bim, filename + "-" + (page + 1) + ".png", 300);
            ImageIOUtil.writeImage(bim, outFileName + "-" + (page + 1) + ".png", 18);
        } finally {
            document.close();
        }
    }

    public static void directoryReset(String outPathName) throws IOException {
        File f;
        boolean fst = true;
        String cr = ".";
        StringTokenizer st = new StringTokenizer(outPathName, "/");
        while (st.hasMoreTokens()) {
            cr = cr + "/" + st.nextToken();
            f = new File(cr);
            if (fst && f.exists()) {
                FileUtils.deleteDirectory(f);
                fst = false;
            }
            if (!f.exists()) {
                Assert.assertTrue(f.mkdir());
            }
        }
    }

/*
    @Test
    public void test2() throws IOException, Docx4JException {
        String filename = "src/test/resources/data/PdfBoxToImageTest/Инструкция по настройке и выявлению ошибок push-сервисов.docx";
        String outPathName = "test/tmp/directory/PdfBoxToImageTest/tmp";
        String outFileName = outPathName + "/Инструкция по настройке и выявлению ошибок push-сервисов.pdf";
        directoryReset(outPathName);

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(filename));

        Docx4J.toPDF(wordMLPackage, new FileOutputStream(outFileName));

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(filename));
        org.docx4j.convert.out.pdf.PdfConversion c = new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
        OutputStream os = new FileOutputStream(outFileName);
        try {
            c.output(os, new PdfSettings());
        } finally {
            os.flush();
            os.close();
        }

        InputStream doc = new FileInputStream(new File(filename));
        XWPFDocument document = new XWPFDocument(doc);
        document.getPart();
        PdfOptions options = PdfOptions.create();
        OutputStream out = new FileOutputStream(new File(outFileName));
        PdfConverter.getInstance().convert(document, out, options);

        toThumbnail(outFileName, "test/tmp/directory/PdfBoxToImageTest/Инструкция по настройке и выявлению ошибок push-сервисов");
    }

    @Test
    public void test3() throws IOException, Docx4JException {
        String filename = "src/test/resources/data/PdfBoxToImageTest/Проверка корректности установки iNavigator на новой среде_v1_0.xlsx";
        String outPathName = "test/tmp/directory/PdfBoxToImageTest/tmp";
        String outFileName = outPathName + "/Проверка корректности установки iNavigator на новой среде_v1_0.pdf";
        directoryReset(outPathName);

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(filename));

        Docx4J.toPDF(wordMLPackage, new FileOutputStream(outFileName));


        SpreadsheetMLPackage wordMLPackage = SpreadsheetMLPackage.load(new File(filename));
        org.docx4j.convert.out.pdf.PdfConversion c = new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
        OutputStream os = new FileOutputStream(outFileName);
        try {
            c.output(os, new PdfSettings());
        } finally {
            os.flush();
            os.close();
        }

        InputStream doc = new FileInputStream(new File(filename));
        XWPFDocument document = new XWPFDocument(doc);
        document.getPart();
        PdfOptions options = PdfOptions.create();
        OutputStream out = new FileOutputStream(new File(outFileName));
        PdfConverter.getInstance().convert(document, out, options);

        toThumbnail(outFileName, "test/tmp/directory/PdfBoxToImageTest/Проверка корректности установки iNavigator на новой среде_v1_0");
    }
*/

}
