package buoy.widget;

import buoy.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * A BTree is a Widget that displays a hierarchical list of objects (or
 * "nodes"). The user can collapse or expand particular nodes to hide or show
 * their child nodes. It optionally can allow the user to select nodes from the
 * tree, or to edit the contents of nodes.
 * <p>
 * Whenever you want to refer to a particular node in the tree, you do so with a
 * <code>TreePath</code> object. A <code>TreePath</code> describes the path to
 * the specified node: its parent node, the parent's parent, and so on up to the
 * root node of the tree. There are various methods for obtaining
 * <code>TreePath</code>s to specific nodes: the root node, the currently
 * selected node or nodes, the children of a particular node, or the parent of a
 * particular node.
 * <p>
 * A BTree always has a single root node. If you want to create the appearance
 * of a tree with multiple roots, you can hide the root node by calling
 * {@link buoy.widget.BTree#setRootNodeShown setRootNodeShown()}.
 * <p>
 * BTree provides methods for modifying the tree by adding or removing nodes.
 * These methods assume that the nodes in question implement the
 * <code>javax.swing.tree.MutableTreeNode</code> interface. If you want to add
 * other types of objects to the tree, the easiest way is to wrap them in
 * <code>javax.swing.tree.DefaultMutableTreeNode</code> objects. Alternatively,
 * you can provide your own <code>TreeModel</code> to represent a hierarchy of
 * arbitrary objects.
 * <p>
 * BTree does not provide scrolling automatically. Normally, it is used inside a
 * BScrollPane to allow the user to scroll through the tree.
 * <p>
 * If you want to detect mouse clicks on nodes independently of whether they are
 * selected, you can do this by listening for mouse events. The following
 * example detects whenever the user double-clicks on a leaf node:
 * <p>
 * <
 * pre>
 * tree.addEventLink(MouseClickedEvent.class, new Object() {
 *   void processEvent(MouseClickedEvent ev)
 *   {
 *     if (ev.getClickCount() == 2)
 *     {
 *       TreePath path = tree.findNode(ev.getPoint());
 *       if (path != null && tree.isLeafNode(path))
 *         System.out.println("Double click on "+path.getLastPathComponent());
 *     }
 *   }
 * });
 * </pre>
 * <p>
 * The appearance of each node is controlled by a <code>TreeCellRenderer</code>,
 * which by default is a <code>javax.swing.tree.DefaultTreeCellRenderer</code>.
 * You can modify or replace the default renderer to customize the appearance of
 * the tree.
 * <p>
 * A BTree is a wrapper around a JTree and its associated classes, which
 * together form a powerful but also very complex API. BTree exposes only the
 * most commonly used features of this API. To use other features, call
 * <code>getComponent()</code> to get the underlying JTree, then manipulate it
 * directly. For example, you can set a custom TreeCellEditor to control the
 * user interface for editing nodes.
 * <p>
 * In addition to the event types generated by all Widgets, BTrees generate the
 * following event types:
 * <ul>
 * <li>{@link buoy.event.ValueChangedEvent ValueChangedEvent}</li>
 * <li>{@link buoy.event.SelectionChangedEvent SelectionChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BTree extends Widget<JTree> {

    private TreeModelListener modelListener;
    private int suppressEvents;
    private boolean selectionEnabled;

    /**
     * Create a BTree whose model is a <code>DefaultTreeModel</code>. It
     * contains a single root node which is a
     * <code>DefaultMutableTreeNode</code>.
     */
    public BTree() {
        this(new DefaultTreeModel(new DefaultMutableTreeNode("Tree")));
    }

    /**
     * Create a BTree whose model is a <code>DefaultTreeModel</code>.
     *
     * @param root the root node of the tree
     */
    public BTree(TreeNode root) {
        this(new DefaultTreeModel(root));
    }

    /**
     * Create a BTree whose contents are determined by a TreeModel.
     */
    public BTree(TreeModel model) {
        component = createComponent(model);
        component.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent ev) {
                if (suppressEvents == 0) {
                    dispatchEvent(new SelectionChangedEvent(BTree.this, false));
                }
            }
        });
        final Runnable scrollPaneUpdater = () -> updateScrollPane();
        component.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                SwingUtilities.invokeLater(scrollPaneUpdater);
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                SwingUtilities.invokeLater(scrollPaneUpdater);
            }
        });
        modelListener = new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent ev) {
                if (suppressEvents == 0) {
                    dispatchEvent(new ValueChangedEvent(BTree.this));
                }
            }

            @Override
            public void treeNodesInserted(TreeModelEvent ev) {
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent ev) {
            }

            @Override
            public void treeStructureChanged(TreeModelEvent ev) {
            }
        };
        model.addTreeModelListener(modelListener);
        if (model instanceof DefaultTreeModel) {
            ((DefaultTreeModel) model).setAsksAllowsChildren(true);
        }
        component.setSelectionModel(new BTreeSelectionModel());
        selectionEnabled = true;
        component.setShowsRootHandles(true);
    }

    /**
     * Create the JTree which serves as this Widget's Component. This method is
     * protected so that subclasses can override it.
     *
     * @param model the tree's model
     */
    protected JTree createComponent(TreeModel model) {
        return new JTree(model);
    }

    /**
     * Get the TreeModel which controls the contents of this BTree.
     */
    public TreeModel getModel() {
        return component.getModel();
    }

    /**
     * Set the TreeModel which controls the contents of this BTree.
     */
    public void setModel(TreeModel model) {
        component.getModel().removeTreeModelListener(modelListener);
        component.setModel(model);
        model.addTreeModelListener(modelListener);
        invalidateSize();
    }

    /**
     * Get the path to the root node. If the tree contains no nodes, this
     * returns null.
     */
    public TreePath getRootNode() {
        Object root = getModel().getRoot();
        if (root == null) {
            return null;
        }
        return new TreePath(root);
    }

    /**
     * Given the path to a node, return the number of children it has.
     *
     * @param path the path to the node for which to count the children
     */
    public int getChildNodeCount(TreePath path) {
        return getModel().getChildCount(path.getLastPathComponent());
    }

    /**
     * Given the path to a node, return the path to one of its children.
     *
     * @param path the path to the node for which to get children
     * @param index the index of the child node to get
     * @return the path to the specified child node
     */
    public TreePath getChildNode(TreePath path, int index) {
        Object child = getModel().getChild(path.getLastPathComponent(), index);
        return path.pathByAddingChild(child);
    }

    /**
     * Given the path to a node, return the path to its parent node.
     *
     * @param path the path whose parent should be returned
     */
    public TreePath getParentNode(TreePath path) {
        return path.getParentPath();
    }

    /**
     * Determine whether a particular node is a leaf node. A leaf node is one
     * which is not permitted to have children, as opposed to one which could
     * have children but does not. For example, in a tree representing the
     * contents of a file system, the node representing a file would be a leaf
     * node. The node representing a folder would not be a leaf node, even if
     * that folder happens to be empty and therefore has no children.
     *
     * @param path the path to node
     */
    public boolean isLeafNode(TreePath path) {
        return getModel().isLeaf(path.getLastPathComponent());
    }

    /**
     * Add a new node to the tree. This method assumes that the parent node
     * implements the javax.swing.tree.MutableTreeNode interface.
     *
     * @param parent the path to the parent node which the new node should be
     * added to
     * @param node the new node to add
     * @return the path to the newly added node
     */
    public TreePath addNode(TreePath parent, MutableTreeNode node) {
        return addNode(parent, node, getChildNodeCount(parent));
    }

    /**
     * Add a new node to the tree. This method assumes that the parent node
     * implements the javax.swing.tree.MutableTreeNode interface.
     *
     * @param parent the path to the parent node which the new node should be
     * added to
     * @param node the new node to add
     * @param index the index in the parent node's list of children where the
     * new node should be added
     * @return the path to the newly added node
     */
    public TreePath addNode(TreePath parent, MutableTreeNode node, int index) {
        MutableTreeNode parentNode = (MutableTreeNode) parent.getLastPathComponent();
        try {
            suppressEvents++;
            parentNode.insert(node, index);
            TreeModel model = getModel();
            if (model instanceof DefaultTreeModel) {
                ((DefaultTreeModel) model).nodesWereInserted(parentNode, new int[]{index});
            }
        } finally {
            suppressEvents--;
        }
        updateScrollPane();
        return parent.pathByAddingChild(node);
    }

    /**
     * Remove a node from the tree. This method assumes that the node being
     * removed implements the javax.swing.tree.MutableTreeNode interface.
     *
     * @param path the path to the node which should be removed
     */
    public void removeNode(TreePath path) {
        try {
            suppressEvents++;
            MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
            TreeNode parentNode = node.getParent();
            int index = parentNode.getIndex(node);
            node.removeFromParent();
            TreeModel model = getModel();
            if (model instanceof DefaultTreeModel) {
                ((DefaultTreeModel) model).nodesWereRemoved(parentNode, new int[]{index}, new Object[]{node});
            }
        } finally {
            suppressEvents--;
        }
        updateScrollPane();
    }

    /**
     * Determine whether this tree allows nodes to be selected.
     */
    public boolean isSelectionEnabled() {
        return selectionEnabled;
    }

    /**
     * Set whether this tree should allow nodes to be selected.
     */
    public void setSelectionEnabled(boolean enabled) {
        selectionEnabled = enabled;
        if (!enabled) {
            clearSelection();
        }
    }

    /**
     * Determine whether this tree allows multiple objects to be selected at the
     * same time.
     */
    public boolean isMultipleSelectionEnabled() {
        return component.getSelectionModel().getSelectionMode() != TreeSelectionModel.SINGLE_TREE_SELECTION;
    }

    /**
     * Set whether this tree should allow multiple objects to be selected at the
     * same time.
     */
    public void setMultipleSelectionEnabled(boolean multiple) {
        getComponent().getSelectionModel().setSelectionMode(multiple ? TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION : TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * Get the number of nodes which are currently selected.
     */
    public int getSelectionCount() {
        return component.getSelectionCount();
    }

    /**
     * Get the path to the first selected node.
     */
    public TreePath getSelectedNode() {
        return component.getSelectionPath();
    }

    /**
     * Get an array containing the paths to all selected nodes.
     */
    public TreePath[] getSelectedNodes() {
        return component.getSelectionPaths();
    }

    /**
     * Determine whether a particular node is selected.
     *
     * @param path the path to the node
     */
    public boolean isNodeSelected(TreePath path) {
        return component.isPathSelected(path);
    }

    /**
     * Set whether a particular node is selected.
     *
     * @param path the path to the node
     * @param selected specifies whether the node should be selected
     */
    public void setNodeSelected(TreePath path, boolean selected) {
        try {
            suppressEvents++;
            if (selected) {
                component.addSelectionPath(path);
            } else {
                component.removeSelectionPath(path);
            }
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Deselect all nodes in the tree.
     */
    public void clearSelection() {
        try {
            suppressEvents++;
            component.clearSelection();
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Determine whether the user is allowed to edit nodes in this tree.
     */
    public boolean isEditable() {
        return component.isEditable();
    }

    /**
     * Set whether the user is allowed to edit nodes in this tree.
     */
    public void setEditable(boolean editable) {
        component.setEditable(editable);
    }

    /**
     * Programmatically begin editing a specified node, if editing is allowed.
     *
     * @param path the path to the node
     */
    public void editNode(TreePath path) {
        component.startEditingAtPath(path);
    }

    /**
     * Given a Point which represents a pixel location, find which node the
     * Point lies on.
     *
     * @param pos the point of interest
     * @return the path to the node, or null if the Point is not on any node
     */
    public TreePath findNode(Point pos) {
        return component.getPathForLocation(pos.x, pos.y);
    }

    /**
     * Determine whether a node is currently expanded.
     *
     * @param path the path to the node
     */
    public boolean isNodeExpanded(TreePath path) {
        return component.isExpanded(path);
    }

    /**
     * Set whether a node is currently expanded.
     *
     * @param path the path to the node
     * @param expanded specifies whether the node should be expanded or
     * collapsed
     */
    public void setNodeExpanded(TreePath path, boolean expanded) {
        if (expanded) {
            component.expandPath(path);
        } else {
            component.collapsePath(path);
        }
        updateScrollPane();
    }

    /**
     * Determine whether a node is currently visible. This means that all of its
     * parent nodes are expanded.
     *
     * @param path the path to the node
     */
    public boolean isNodeVisible(TreePath path) {
        return component.isVisible(path);
    }

    /**
     * Make a node visible by expanding all of its parent nodes.
     *
     * @param path the path to the node
     */
    public void makeNodeVisible(TreePath path) {
        component.makeVisible(path);
    }

    /**
     * Scroll the BTree's parent BScrollPane to ensure that a particular node is
     * visible. If the parent is not a BScrollPane, the results of calling this
     * method are undefined, but usually it will have no effect at all.
     * <p>
     * If the specified node is hidden because one of its parent nodes is
     * currently collapsed, this method has no effect. Usually you will first
     * call <code>makeNodeVisible()</code> before calling this method.
     *
     * @param path the path to the node
     */
    public void scrollToNode(TreePath path) {
        Rectangle bounds = component.getPathBounds(path);
        if (bounds != null) {
            getComponent().scrollRectToVisible(bounds);
        }
    }

    /**
     * Get whether the root node of the tree should be shown. If this is false,
     * then the children of the root node will appear to be the top level of
     * tree. This allows you to create the illusion of a tree with multiple
     * roots.
     */
    public boolean isRootNodeShown() {
        return component.isRootVisible();
    }

    /**
     * Set whether the root node of the tree should be shown. If this is false,
     * then the children of the root node will appear to be the top level of
     * tree. This allows you to create the illusion of a tree with multiple
     * roots.
     */
    public void setRootNodeShown(boolean shown) {
        component.setRootVisible(shown);
    }

    /**
     * Get the preferred number of rows which should be visible without using a
     * scrollbar.
     */
    public int getPreferredVisibleRows() {
        return component.getVisibleRowCount();
    }

    /**
     * Set the preferred number of rows which should be visible without using a
     * scrollbar.
     */
    public void setPreferredVisibleRows(int rows) {
        component.setVisibleRowCount(rows);
        invalidateSize();
    }

    /**
     * Get the TreeCellRenderer which draws the individual nodes in the tree.
     */
    public TreeCellRenderer getCellRenderer() {
        return component.getCellRenderer();
    }

    /**
     * Set the TreeCellRenderer which draws the individual nodes in the tree.
     */
    public void setCellRenderer(TreeCellRenderer renderer) {
        component.setCellRenderer(renderer);
    }

    /**
     * If this BTree is a child of a BScrollPane, update the parent whenever the
     * contents of the tree changes.
     */
    private void updateScrollPane() {
        invalidateSize();
        if (getParent() instanceof BScrollPane) {
            getParent().layoutChildren();
        }
    }

    /**
     * Inner class which implements the tree's selection model. This allows
     * selection to be enabled and disabled by setting a flag.
     */
    private class BTreeSelectionModel extends DefaultTreeSelectionModel {

        @Override
        public void setSelectionPaths(TreePath[] path) {
            if (selectionEnabled) {
                super.setSelectionPaths(path);
            }
        }

        @Override
        public void addSelectionPaths(TreePath[] path) {
            if (selectionEnabled) {
                super.addSelectionPaths(path);
            }
        }

        @Override
        public void removeSelectionPaths(TreePath[] path) {
            if (selectionEnabled) {
                super.removeSelectionPaths(path);
            }
        }
    }
}
