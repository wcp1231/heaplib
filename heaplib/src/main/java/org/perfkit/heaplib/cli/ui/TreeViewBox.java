package org.perfkit.heaplib.cli.ui;

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import org.netbeans.lib.profiler.heap.Instance;

import java.util.ArrayList;
import java.util.List;

public class TreeViewBox<V> extends AbstractListBox<TreeNode, TreeViewBox<TreeNode>> {

    private TreeView treeView;
    private final List<TreeNode> items;
    private int selectedIndex;
    private int startColumns;

    public TreeViewBox(List<Instance> items) {
        List<TreeNode> roots = new ArrayList<>();
        for (Instance instance : items) roots.add(new TreeNode(instance, 0));
        this.treeView = new TreeView(roots);
        this.items = new ArrayList<>();
        this.selectedIndex = -1;
        this.startColumns = 0;
        this.updateItems();
    }

    public void toggleCollapse(int index) {
        this.getItemAt(index).toggleCollapse();
        this.updateItems();
    }

    public synchronized TreeViewBox updateItems() {
        List<TreeNode> newItems = this.treeView.getAllNodes();
        int idx = 0;
        for (; idx < newItems.size() && idx < items.size(); idx++) {
            items.set(idx, newItems.get(idx));
        }
        while (idx < items.size()) {
            items.remove(idx);
        }
        for (; idx < newItems.size(); idx++) {
            items.add(newItems.get(idx));
        }
        return this.self();
    }

    public synchronized TreeViewBox addItem(TreeNode item) {
        if (item == null) {
            return this.self();
        }
        this.items.add(item);
        if (this.selectedIndex == -1) {
            this.selectedIndex = 0;
        }

        this.invalidate();
        return this.self();
    }

    public synchronized TreeNode removeItem(int index) {
        TreeNode existing = this.items.remove(index);
        if (index < this.selectedIndex) {
            --this.selectedIndex;
        }

        while(this.selectedIndex >= this.items.size()) {
            --this.selectedIndex;
        }

        this.invalidate();
        return existing;
    }

    public synchronized TreeViewBox clearItems() {
        this.items.clear();
        this.selectedIndex = -1;
        this.invalidate();
        return this.self();
    }

    public boolean isFocusable() {
        return this.isEmpty() ? false : super.isFocusable();
    }

    public synchronized int indexOf(Instance item) {
        return this.items.indexOf(item);
    }

    public synchronized TreeNode getItemAt(int index) {
        return this.items.get(index);
    }

    public synchronized boolean isEmpty() {
        return this.items.isEmpty();
    }

    public synchronized int getItemCount() {
        return this.items.size();
    }

    public synchronized List<TreeNode> getItems() {
        return new ArrayList(this.items);
    }

    public synchronized TreeViewBox setSelectedIndex(int index) {
        this.selectedIndex = Math.max(0, Math.min(index, this.items.size() - 1));
        this.invalidate();
        return this.self();
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public synchronized TreeNode getSelectedItem() {
        return this.selectedIndex == -1 ? null : this.items.get(this.selectedIndex);
    }

    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        if (this.isKeyboardActivationStroke(keyStroke)) {
            this.toggleCollapse(this.getSelectedIndex());
            return Result.HANDLED;
        }
        Result result;
        switch (keyStroke.getKeyType()) {
            case ArrowDown:
                return handleDown();
            case ArrowUp:
                return handleUp();
            case ArrowLeft:
                return handleLeft();
            case ArrowRight:
                return handleRight();
            case Character:
                switch (keyStroke.getCharacter()) {
                    case 'j':
                        return handleDown();
                    case 'k':
                        return handleUp();
                    case 'h':
                        return handleLeft();
                    case 'l':
                        return handleRight();
                }
        }
        return super.handleKeyStroke(keyStroke);
    }

    public int getStartColumns() {
        return this.startColumns;
    }

    private Result handleDown() {
        if (!this.items.isEmpty() && this.selectedIndex != this.items.size() - 1) {
            ++this.selectedIndex;
            return Result.HANDLED;
        }
        return Result.MOVE_FOCUS_DOWN;
    }

    private Result handleUp() {
        if (!this.items.isEmpty() && this.selectedIndex != 0) {
            --this.selectedIndex;
            return Result.HANDLED;
        }
        return Result.MOVE_FOCUS_UP;
    }

    private Result handleLeft() {
        if (this.startColumns > 0) {
            this.startColumns--;
        }
        return Result.HANDLED;
    }

    private Result handleRight() {
        this.startColumns++;
        return Result.HANDLED;
    }

    protected AbstractListBox.ListItemRenderer<TreeNode, TreeViewBox<TreeNode>> createDefaultListItemRenderer() {
        return new TreeViewListItemRenderer();
    }

    public static class TreeViewListItemRenderer extends ListItemRenderer<TreeNode, TreeViewBox<TreeNode>> {
        private static final int SIZE_TEXT_LENGTH = 15;
        public int getHotSpotPositionOnLine(int selectedIndex) {
            return -1;
        }

        public void drawItem(TextGUIGraphics graphics, TreeViewBox<TreeNode> listBox, int index, TreeNode item, boolean selected, boolean focused) {
            ThemeDefinition themeDefinition = listBox.getTheme().getDefinition(AbstractListBox.class);
            if (selected && focused) {
                graphics.applyThemeStyle(themeDefinition.getSelected());
            } else {
                graphics.applyThemeStyle(themeDefinition.getNormal());
            }

            int fullColumns = graphics.getSize().getColumns();
            String sizeText = item.getSizeText();//getSizeText(item);
            String label = getItemText(item.getNameText(), listBox.getStartColumns(), fullColumns);

            for(label = TerminalTextUtils.fitString(label, fullColumns); TerminalTextUtils.getColumnWidth(label) + SIZE_TEXT_LENGTH < fullColumns; label = label + " ") {
            }
            label += sizeText;

            graphics.putString(0, 0, label);
        }

        private String getItemText(String nameText, int startCol, int fullColumns) {
            //String name = item.getJavaClass().getName() + '#' + item.getInstanceNumber();
            int availableSize = fullColumns - SIZE_TEXT_LENGTH;
            if (nameText.length() < startCol) {
                nameText = "";
            } else {
                nameText = nameText.substring(startCol);
            }
            if (nameText.length() > availableSize) {
                nameText = nameText.substring(0, availableSize-3) + "...";
            }
            return nameText;
        }


    }
}
