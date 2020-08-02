import javax.print.*;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.scene.control.Label;
import java.io.*;
import javax.print.*;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

public class Main {

    public static void main(String[] args) {

        //PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
//        PrintService printServices = PrintServiceLookup.lookupDefaultPrintService();
//        System.out.println("Number of print service: " + printServices.getName());

//        for (PrintService printer : printServices) {
//            System.out.println("Printer: " + printer.getName());
//        }




       // printUIInterface();
        //print();
       // printListerner("/Users/thienle/Documents/PrintService/src/test.txt");

        try {
            FileInputStream fis = new FileInputStream("/Users/thienle/Documents/PrintService/src/test.txt");
            new PrinterHandler(fis).startPrintJob();
        } catch (FileNotFoundException io) {
            System.out.println("File not found");
        }
    }

    private static void printDocFlavor() {
        DocFlavor df = DocFlavor.READER.TEXT_PLAIN;
        AttributeSet attribute = new HashAttributeSet();
        attribute.add(OrientationRequested.PORTRAIT);
        attribute.add(ColorSupported.SUPPORTED);
        PrintService[] services = PrintServiceLookup.lookupPrintServices(df, attribute);

        for (int i=0; i<services.length; i++) {
            if (services[i].isDocFlavorSupported(df)) {
                System.out.println(services[i].getName());
            }
        }
    }

    private static void print() {
        DocFlavor flavor = DocFlavor.INPUT_STREAM.POSTSCRIPT;
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);

        PrintService[] pservices =
                PrintServiceLookup.lookupPrintServices(flavor, aset);


        if (pservices.length > 0) {
            DocPrintJob pj = pservices[0].createPrintJob();


            try {
                FileInputStream fis = new FileInputStream("/Users/thienle/Documents/PrintService/src/test.txt");
                Doc doc = new SimpleDoc(fis, flavor, null);
                pj.print(doc, aset);
                fis.close();
            } catch (FileNotFoundException fe) {
                System.out.println("file not found");
            } catch (IOException io) {
                System.out.println("IOException");
            } catch (PrintException e) {
                System.out.println("Unable to print");
            }
        }
    }

    private static void print2(boolean isRangeSet, int from, int to, int copies) {
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
        PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
        patts.add(Sides.ONE_SIDED);
        patts.add(MediaSizeName.ISO_A4);
        patts.add(new Copies(copies));


        if (isRangeSet) {
            PageRanges pageRanges = new PageRanges(from,to);
            patts.add(pageRanges);
        }

        PrintService[] ps = PrintServiceLookup.lookupPrintServices(flavor, patts);
        if (ps.length == 0) {
            throw new IllegalStateException("No Printer found");
        }
        System.out.println("Available printers: " + Arrays.asList(ps));

        PrintService myService = null;
        for (PrintService printService : ps) {
            if (printService.getName().equals("Your printer name")) {
                myService = printService;
                break;
            }
        }

        if (myService == null) {
            throw new IllegalStateException("Printer not found");
        }

        try {
            FileInputStream fis = new FileInputStream("C:/Users/John Doe/Desktop/SamplePDF.pdf");
            Doc pdfDoc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            DocPrintJob printJob = myService.createPrintJob();
            printJob.print(pdfDoc, new HashPrintRequestAttributeSet());
            fis.close();
        } catch (IOException io) {
            System.out.println("IOException");
        } catch (PrintException pe) {
            System.out.println("Print exception");
        }
    }

    static void printUIInterface() {
        PrintService[] printServices = PrintServiceLookup
                .lookupPrintServices(null, null);
        PrintService defaultPrintService = PrintServiceLookup
                .lookupDefaultPrintService();
        PageRanges pageRanges = new PageRanges(1, 2);

        PrintRequestAttributeSet attrib =
                new HashPrintRequestAttributeSet();
        PrintService selectedPrintService =
                ServiceUI.printDialog(null, 150, 150,
                        printServices, defaultPrintService, null, attrib);
        if(selectedPrintService!=null)
            System.out.println("selected printer:"
                    +selectedPrintService.getName());
        else
            System.out.println("selection cancelled");
    }

    static void printListerner(String fileName) {
        PrintService ps=PrintServiceLookup.lookupDefaultPrintService();
        DocPrintJob job=ps.createPrintJob();
        job.addPrintJobListener(new PrintJobAdapter() {
            public void printDataTransferCompleted(PrintJobEvent event){
                System.out.println("data transfer complete");
            }
            public void printJobNoMoreEvents(PrintJobEvent event){
                System.out.println("received no more events");
            }
            public void printJobCanceled(PrintJobEvent event){
                System.out.println("print canceled");
            }
            public void printJobFailed(PrintJobEvent event){
                System.out.println("job failed");
            }
            public void printJobCompleted(PrintJobEvent event){
                System.out.println("finsihed");
            }
        });
        try {
            FileInputStream fis = new FileInputStream(fileName);
            Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
            // Doc doc=new SimpleDoc(fis, DocFlavor.INPUT_STREAM.JPEG, null);
            PrintRequestAttributeSet attrib = new HashPrintRequestAttributeSet();
            attrib.add(new Copies(1));
            job.print(doc, attrib);

            fis.close();

        } catch (FileNotFoundException fn) {
            System.out.println("file not found");
        } catch (PrintException pe) {
            System.out.println("Printer exception");
        } catch (IOException io) {
            System.out.println("IOexeption");
        }
    }




}
