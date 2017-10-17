package template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
  * @author ycoppel@google.com (Yohann Coppel)
  * 
  * @param <Node>
  *          Object's type in the tree.
*/

public class Tree2<Node> {

  private Node head;

  private ArrayList<Tree<Node>> leafs = new ArrayList<Tree<Node>>();

  private Tree<Node> parent = null;

  private HashMap<Node, Tree<Node>> locate = new HashMap<Node, Tree<Node>>();

  public Tree(Node head) {
    this.head = head;
    locate.put(head, this);
  }

  public void addLeaf(Node root, Node leaf) {
    if (locate.containsKey(root)) {
      locate.get(root).addLeaf(leaf);
    } else {
      addLeaf(root).addLeaf(leaf);
    }
  }

  public Tree<Node> addLeaf(Node leaf) {
    Tree<Node> t = new Tree<Node>(leaf);
    leafs.add(t);
    t.parent = this;
    t.locate = this.locate;
    locate.put(leaf, t);
    return t;
  }

  public Tree<Node> setAsParent(Node parentRoot) {
    Tree<Node> t = new Tree<Node>(parentRoot);
    t.leafs.add(this);
    this.parent = t;
    t.locate = this.locate;
    t.locate.put(head, this);
    t.locate.put(parentRoot, t);
    return t;
  }

  public Node getHead() {
    return head;
  }

  public Tree<Node> getTree(Node element) {
    return locate.get(element);
  }

  public Tree<Node> getParent() {
    return parent;
  }

  public Collection<Node> getSuccessors(Node root) {
    Collection<Node> successors = new ArrayList<Node>();
    Tree<Node> tree = getTree(root);
    if (null != tree) {
      for (Tree<Node> leaf : tree.leafs) {
        successors.add(leaf.head);
      }
    }
    return successors;
  }

  public Collection<Tree<Node>> getSubTrees() {
    return leafs;
  }

  public static <Node> Collection<Node> getSuccessors(Node of, Collection<Tree<Node>> in) {
    for (Tree<Node> tree : in) {
      if (tree.locate.containsKey(of)) {
        return tree.getSuccessors(of);
      }
    }
    return new ArrayList<Node>();
  }

  @Override
  public String toString() {
    return printTree(0);
  }

  private static final int indent = 2;

  private String printTree(int increment) {
    String s = "";
    String inc = "";
    for (int i = 0; i < increment; ++i) {
      inc = inc + " ";
    }
    s = inc + head;
    for (Tree<Node> child : leafs) {
      s += "\n" + child.printTree(increment + indent);
    }
    return s;
  }
}