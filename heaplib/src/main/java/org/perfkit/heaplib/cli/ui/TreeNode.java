package org.perfkit.heaplib.cli.ui;

import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.netbeans.lib.profiler.heap.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TreeNode {
    private static final String INDENT = "  ";
    private int deep;
    private boolean collapse;
    private Instance instance;
    private String namePrefix = "";
    private String nameText;
    private String sizeText;
    private long size = 0;
    private List<TreeNode> children = null;

    public TreeNode(FieldValue fieldValue, int deep) {
        this.deep = deep;
        this.collapse = true;
        this.instance = getInstanceFromField(fieldValue);
        this.nameText = getNameText(instance);
        setSizeText(instance);
    }

    public TreeNode(Instance instance, int deep) {
        this(instance, "", deep);
    }

    public TreeNode(Instance instance, String prefix, int deep) {
        this.deep = deep;
        this.collapse = true;
        this.instance = instance;
        this.namePrefix = prefix;
        this.nameText = getNameText(instance);
        setSizeText(instance);
    }

    public String getNameText() {
        return nameText;
    }

    public String getSizeText() {
        return sizeText;
    }

    public Instance getInstance() {
        return instance;
    }

    public boolean isCollapse() {
        return collapse;
    }

    public void toggleCollapse() {
        this.collapse = !this.collapse;
    }

    public List<TreeNode> getChildren() {
        if (this.children != null) {
            return this.children;
        }
        if (this.instance == null) {
            this.children = new ArrayList<>();
            return this.children;
        }
        List<TreeNode> children = new ArrayList<>();
        if (instance instanceof ObjectArrayDump) {
            ObjectArrayDump oad = (ObjectArrayDump) this.instance;
            for (Instance instance : oad.getValues()) {
                children.add(new TreeNode(instance, deep+1));
            }
        }
        for (FieldValue field : instance.getStaticFieldValues()) {
            children.add(new TreeNode(field, deep+1));
        }
        for (FieldValue field : instance.getFieldValues()) {
            children.add(new TreeNode(field, deep+1));
        }
        for (Value ref : instance.getReferences()) {
            if (ref instanceof ObjectFieldValue) {
                ObjectFieldValue ofv = (ObjectFieldValue) ref;
                children.add(new TreeNode(ofv.getDefiningInstance(), "<ref> ", deep+1));
            } else if (ref instanceof ArrayItemValue) {
                ArrayItemValue aiv = (ArrayItemValue) ref;
                children.add(new TreeNode(aiv.getDefiningInstance(), "<ref> ", deep+1));
            }
        }
        Collections.sort(children, BY_SIZE);
        this.children = children;
        return this.children;
    }

    private Instance getInstanceFromField(FieldValue fieldValue) {
        Field field = fieldValue.getField();
        if (field.isStatic()) {
            this.namePrefix = "<static> ";
        }
        this.namePrefix += field.getName() + " ";
        if (fieldValue instanceof ClassLoaderFieldValue) {
            ClassLoaderFieldValue clfv = (ClassLoaderFieldValue) fieldValue;
            return clfv.getInstance();
        }
        if (fieldValue instanceof HprofFieldObjectValue) {
            HprofFieldObjectValue hfov = (HprofFieldObjectValue) fieldValue;
            return hfov.getInstance();
        }
        if (fieldValue instanceof HprofInstanceObjectValue) {
            HprofInstanceObjectValue hfov = (HprofInstanceObjectValue) fieldValue;
            return hfov.getInstance();
        }
        return null;
    }

    private String getNameText(Instance item) {
        String name = "";
        if (item != null) {
            name = item.getJavaClass().getName() + '#' + item.getInstanceNumber();
        }
        String prefix = "";
        for (int i = 0; i < this.deep; i++) {
            prefix += INDENT;
        }
        return prefix + this.namePrefix + name;
    }

    private void setSizeText(Instance item) {
        if (item == null) {
            this.sizeText = "";
            return;
        }

        this.size = item.getRetainedSize();
        String size = formatSize(item.getSize());
        String retained = formatSize(this.size);
        this.sizeText = String.format("%7s%7s", size, retained);

    }

    private String formatSize(double size) {
        String unit = "B";
        if (size > 1024) {
            size /= 1024d;
            unit = "K";
        }
        if (size > 1024) {
            size /= 1024d;
            unit = "M";
        }
        return String.format("%.1f%s", size, unit);
    }

    public static final Comparator<TreeNode> BY_SIZE = new Comparator<TreeNode>() {

        @Override
        public int compare(TreeNode o1, TreeNode o2) {
            if (o1.size == o2.size) return 0;
            return o1.size < o2.size ? 1 : -1;
        }

        public String toString() {
            return "BY_SIZE";
        }
    };
}
