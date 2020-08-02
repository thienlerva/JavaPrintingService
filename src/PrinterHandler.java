import javafx.scene.control.Label;
import javax.print.*;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrinterHandler {

    private void delay(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public PrinterHandler (FileInputStream fis) {
       // this.statusLabel = statusLabel;
        this.fis = fis;
    }
    private FileInputStream fis;
    private Label statusLabel;
    private String state;

    public void startPrintJob () {
        try {

            delay(5000);
            InputStream is = new BufferedInputStream(this.fis);
            DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
            PrintService service = PrintServiceLookup.lookupDefaultPrintService();
            DocPrintJob printJob = service.createPrintJob();
            //JobMonitor monitor = new JobMonitor();
            JobCompleteMonitor monitor = new JobCompleteMonitor();
            printJob.addPrintJobListener(monitor);
            Doc doc = new SimpleDoc(is, flavor, null);
            printJob.print(doc, null);
            monitor.waitForJobCompletion();
            is.close();
        } catch (PrintException | IOException e) {
            e.printStackTrace();
        }
    }

    private class JobMonitor extends PrintJobAdapter {
        private boolean notify = false;
        final int DATA_TRANSFERRED      = 10;
        final int JOB_COMPLETE          = 11;
        final int JOB_FAILED            = 12;
        final int JOB_CANCELED          = 13;
        final int JOB_NO_MORE_EVENTS    = 14;
        final int JOB_NEEDS_ATTENTION   = 15;

        private int status;
        @Override
        public void printDataTransferCompleted(PrintJobEvent pje) {
            status = DATA_TRANSFERRED;
            markAction();
        }
        @Override
        public void printJobCompleted(PrintJobEvent pje) {
            status = JOB_COMPLETE;

            markAction();
        }
        @Override
        public void printJobFailed(PrintJobEvent pje) {
            status = JOB_FAILED;

            markAction();
        }
        @Override
        public void printJobCanceled(PrintJobEvent pje) {
            status = JOB_CANCELED;
            markAction();
        }
        @Override
        public void printJobNoMoreEvents(PrintJobEvent pje) {
            status = JOB_NO_MORE_EVENTS;
            markAction();
        }
        @Override
        public void printJobRequiresAttention(PrintJobEvent pje) {
            status = JOB_NEEDS_ATTENTION;

            markAction();
        }
        private void markAction() {
            synchronized (JobMonitor.this) {
                notify = true;
                JobMonitor.this.notify();
            }
        }
        public synchronized void waitForJobCompletion() {
            Runnable runner = ()->{
                boolean keepRunning = true;
                while (keepRunning) {
                    try {
                        while (!notify) {
                            wait();
                        }
                        switch(this.status){
                            case DATA_TRANSFERRED:
                                state = "DATA_TRANSFERRED";
                                System.out.println("data transfer");
                                break;
                            case JOB_COMPLETE:
                                state = "JOB_FINISHED";
                                System.out.println("jon finised");
                                keepRunning = false;
                                break;
                            case JOB_FAILED:
                                state = "JOB_FAILED";
                                System.out.println("job failed");
                                keepRunning = false;
                                break;
                            case JOB_CANCELED:
                                state = "JOB_CANCELED";
                                System.out.println("job cancled");
                                keepRunning = false;
                                break;
                            case JOB_NO_MORE_EVENTS:
                                state = "JOB_COMPLETE";
                                System.out.println("no mor event");
                                keepRunning = false;
                                break;
                            case JOB_NEEDS_ATTENTION:
                                state = "JOB_NEEDS_ATTENTION";
                                break;

                        }

                        delay(5000);
                        System.out.println(state);
                        notify = false;
                    }
                    catch (InterruptedException e) {}
                }
                delay(5000);

            };
            Thread monitor = new Thread(runner);
            monitor.start();
        }
    }

    private static class JobCompleteMonitor extends PrintJobAdapter {

        private boolean completed = false;

        @Override
        public void printJobCanceled(PrintJobEvent pje) {
            signalCompletion();
        }

        @Override
        public void printJobCompleted(PrintJobEvent pje) {
            signalCompletion();
        }

        @Override
        public void printJobFailed(PrintJobEvent pje) {
            signalCompletion();
        }

        @Override
        public void printJobNoMoreEvents(PrintJobEvent pje) {
            signalCompletion();
        }

        private void signalCompletion() {

            synchronized (JobCompleteMonitor.this) {

                completed = true;

                JobCompleteMonitor.this.notify();

            }

        }

        public synchronized void waitForJobCompletion() {

            try {
                System.out.println(completed);
                while (!completed) {

                    wait();

                }

            } catch (InterruptedException e) {

            }
        }

    }

}
