package org.perfkit.heaplib.cli.ui;

import java.util.ArrayList;
import java.util.List;

public class TreeView {
    private List<TreeNode> roots;

    public TreeView(List<TreeNode> roots) {
        this.roots = roots;
    }

    public List<TreeNode> getAllNodes() {
        List<TreeNode> result = new ArrayList<>();
        result.add(getHeader());
        for (TreeNode root : roots) {
            dfs(root, result);
        }
        return result;
    }

    private void dfs(TreeNode cur, List<TreeNode> result) {
        if (cur == null) {
            return;
        }
        result.add(cur);
        if (!cur.isCollapse()) {
            for (TreeNode child : cur.getChildren()) {
                dfs(child, result);
            }
        }
    }

    private TreeNode getHeader() {
        TreeNode header = new TreeNode(null, "", 0);
        String sizeHeader = String.format("%7s%10s", "Size", "Retained");
        header.setNameAndSizeText("Name", sizeHeader);
        return header;
    }
}
