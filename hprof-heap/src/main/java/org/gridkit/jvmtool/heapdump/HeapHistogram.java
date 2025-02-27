/**
 * Copyright 2014 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.jvmtool.heapdump;

import java.util.*;

import org.netbeans.lib.profiler.heap.Instance;

public class HeapHistogram implements InstanceCallback {

    public static final Comparator<ClassRecord> BY_NAME = new Comparator<ClassRecord>() {

        @Override
        public int compare(ClassRecord o1, ClassRecord o2) {
            return o1.className.compareTo(o2.className);
        }

        public String toString() {
            return "BY_NAME";
        }
    };

    public static final Comparator<ClassRecord> BY_SIZE = new Comparator<ClassRecord>() {

        @Override
        public int compare(ClassRecord o1, ClassRecord o2) {
            return Long.valueOf(o2.totalSize).compareTo(o1.totalSize);
        }

        public String toString() {
            return "BY_SIZE";
        }
    };

    public static final Comparator<ClassRecord> BY_INSTANCE_SIZE = new Comparator<ClassRecord>() {

        @Override
        public int compare(ClassRecord o1, ClassRecord o2) {
            return Long.valueOf(o1.totalSize).compareTo(o2.totalSize);
        }

        public String toString() {
            return "BY_SIZE";
        }
    };

    public static final Comparator<ClassRecord> BY_COUNT = new Comparator<ClassRecord>() {

        @Override
        public int compare(ClassRecord o1, ClassRecord o2) {
            return Long.valueOf(o2.instanceCount).compareTo(o1.instanceCount);
        }

        public String toString() {
            return "BY_COUNT";
        }
    };

    private Map<String, ClassRecord> classes = new HashMap<String, ClassRecord>();
    private PriorityQueue<ClassRecord> topInstance = new PriorityQueue(BY_INSTANCE_SIZE);
    private ClassRecord total = new ClassRecord("Total heap");
    private RefSet known = null;

    public void enableInstanceTracking() {
        if (known == null) {
            known = new RefSet();
        }
    }

    @Override
    public void feed(Instance instance) {
        accumulate(instance);
    }

    public void accumulate(Instance i) {
        if (known != null) {
            if (known.getAndSet(i.getInstanceId(), true)) {
                // already accumulated
                return;
            }
        }
        total.add(i);
        String cn = i.getJavaClass().getName();
        ClassRecord cr = classes.get(cn);
        if (cr == null) {
            cr = new ClassRecord(cn);
            classes.put(cn, cr);
        }
        ++cr.instanceCount;
        cr.totalSize += i.getSize();

        // for instance
        ClassRecord lr = new ClassRecord(cn + "#" + i.getInstanceNumber());
        lr.totalSize += i.getSize();
        topInstance.add(lr);
        if (topInstance.size() > 15) {
            topInstance.poll();
        }
    }

    public Collection<ClassRecord> getTopInstances() {
        List<ClassRecord> result = new ArrayList<>(topInstance.size());
        while (!topInstance.isEmpty()) {
            result.add(topInstance.poll());
        }
        Collections.reverse(result);
        return result;
    }

    public long getTotalCount() {
        return total.getInstanceCount();
    }

    public long getTotalSize() {
        return total.getTotalSize();
    }

    public ClassRecord getClassInfo(String type) {
        return classes.get(type);
    }

    public Collection<ClassRecord> getHisto() {
        return classes.values();
    }

    public Collection<ClassRecord> getHistoByName() {
        List<ClassRecord> histo = new ArrayList<HeapHistogram.ClassRecord>(classes.values());
        Collections.sort(histo, BY_NAME);
        return histo;
    }

    public Collection<ClassRecord> getHistoBySize() {
        List<ClassRecord> histo = new ArrayList<HeapHistogram.ClassRecord>(classes.values());
        Collections.sort(histo, BY_SIZE);
        return histo;
    }

    public Collection<ClassRecord> getHistoByCount() {
        List<ClassRecord> histo = new ArrayList<HeapHistogram.ClassRecord>(classes.values());
        Collections.sort(histo, BY_COUNT);
        return histo;
    }

    public static class ClassRecord {

        String className;
        long instanceCount;
        long totalSize;

        public ClassRecord(String name) {
            className = name;
        }

        void add(Instance i) {
            ++instanceCount;
            totalSize += i.getSize();
        }

        public String getClassName() {
            return className;
        }

        public long getInstanceCount() {
            return instanceCount;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public String toString() {
            return className + " " + toMemorySize(totalSize) + "(" + instanceCount + ")";
        }
    }

    private static final String toMemorySize(long n) {
        if (n < (10l << 10)) {
            return String.valueOf(n);
        }
        else if (n < (10l << 20)) {
            return String.valueOf(n >> 10) + "k";
        }
        else if (n < (10l << 30)) {
            return String.valueOf(n >> 20) + "m";
        }
        else {
            return String.valueOf(n >> 30) + "g";
        }
    }

    public String formatTop(int top) {
        TextTable table = new TextTable();
        table.addRow("", "Size", " Count", " Type");
        int n = 0;
        for(ClassRecord cr: getHistoBySize()) {
            ++n;
            table.addRow("" + n, " " + cr.getTotalSize(), " " + cr.getInstanceCount(), " " + cr.getClassName());
            if (n == top) {
                break;
            }
        }
        table.addRow("TOTAL", " " + total.totalSize, " " + total.instanceCount, "");
        return table.formatTextTableUnbordered(180);
    }

    @Override
    public String toString() {
        TextTable table = new TextTable();
        table.addRow("", "Size", " Count", " Type");
        int n = 0;
        for(ClassRecord cr: getHistoBySize()) {
            table.addRow("" + n, " " + cr.getTotalSize(), " " + cr.getInstanceCount(), " " + cr.getClassName());
            ++n;
        }
        table.addRow("TOTAL", " " + total.totalSize, " " + total.instanceCount, "");
        return table.formatTextTableUnbordered(180);
    }
}
