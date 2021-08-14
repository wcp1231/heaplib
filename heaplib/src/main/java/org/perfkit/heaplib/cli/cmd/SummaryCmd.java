package org.perfkit.heaplib.cli.cmd;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.gridkit.jvmtool.cli.CommandLauncher;
import org.gridkit.jvmtool.cli.CommandLauncher.CmdRef;
import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapSummary;
import org.netbeans.lib.profiler.heap.Instance;

public class SummaryCmd implements CmdRef {
    @Override
    public String getCommandName() {
        return "summary";
    }

    @Override
    public Runnable newCommand(CommandLauncher host) {
        return new SummaryRunner(host);
    }

    @Parameters(commandDescription = "HProf summary")
    public static class SummaryRunner implements Runnable {

        @ParametersDelegate
        private final CommandLauncher host;

        @ParametersDelegate
        private HeapProvider heapProvider = new HeapProvider();

        public SummaryRunner(CommandLauncher host) {
            this.host = host;
        }

        @Override
        public void run() {
            try {
                Heap heap = heapProvider.openHeap(host);
                HeapSummary summary = heap.getSummary();
                printHeader("概览");

                //System.out.format("%-10s%-10s%n", "HProf:", heap)
                printField("Size:", formatSize(summary.getTotalLiveBytes()));
                printField("Instances:", Long.toString(summary.getTotalLiveInstances()));
                printField("classes:", Long.toString(summary.getClassCount()));

                HeapHistogram histogram = accumulateHistogram(heap);
                printHeader("按实例总大小排序的 Classes");
                printHistogramBySize(histogram, 10);
                printHeader("按实例数排序的 Classes");
                printHistogramByCount(histogram, 10);
                printHeader("按大小排序的实例");
                printHistogramByInstanceCount(histogram, 10);

            } catch (Exception e) {
                throw host.fail("Heap dump processing error", e);
            }
        }

        private HeapHistogram accumulateHistogram(Heap heap) {
            HeapHistogram histogram = new HeapHistogram();

            for (Instance i : heap.getAllInstances()) {
                histogram.accumulate(i);
            }

            return histogram;
        }

        private void printHeader(String header) {
            System.out.format("%n==== %s%n%n", header);
        }

        private void printField(String key, String value) {
            System.out.format("%-10s%10s%n", key, value);
        }

        private void printHistogramBySize(HeapHistogram histogram, int top) {
            System.out.format("%10s%10s%10s%n", "Size", "Count", "Class");
            int n = 0;
            for(HeapHistogram.ClassRecord cr: histogram.getHistoBySize()) {
                n++;
                System.out.format("%10s%10d%5s%-10s%n", formatSize(cr.getTotalSize()), cr.getInstanceCount(), "", cr.getClassName());
                if (n == top) {
                    break;
                }
            }
        }

        private void printHistogramByCount(HeapHistogram histogram, int top) {
            System.out.format("%10s%10s%10s%n", "Count", "Size", "Class");
            int n = 0;
            for(HeapHistogram.ClassRecord cr: histogram.getHistoByCount()) {
                n++;
                System.out.format("%10d%10s%5s%-10s%n", cr.getInstanceCount(), formatSize(cr.getTotalSize()), "", cr.getClassName());
                if (n == top) {
                    break;
                }
            }
        }

        private void printHistogramByInstanceCount(HeapHistogram histogram, int top) {
            System.out.format("%10s%10s%n", "Size", "Instance");
            int n = 0;
            for(HeapHistogram.ClassRecord cr: histogram.getTopInstances()) {
                n++;
                System.out.format("%10s%2s%-10s%n", formatSize(cr.getTotalSize()), "", cr.getClassName());
                if (n == top) {
                    break;
                }
            }
        }

        private String formatSize(long size) {
            String unit = "B";
            double s = size;
            if (s > 1024) {
                s = s / 1024d;
                unit = "K";
            }
            if (s > 1024) {
                s = s / 1024d;
                unit = "M";
            }
            return String.format("%.1f%s", s, unit);
        }
    }
}
