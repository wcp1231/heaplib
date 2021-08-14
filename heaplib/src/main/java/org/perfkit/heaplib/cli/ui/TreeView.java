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
}
