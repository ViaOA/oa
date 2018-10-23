package test.homework.dave;

import java.util.ArrayList;

/**
 * Tree structure for holding nodes, with methods to display the tree by leg or row.
 */
public class Tree {

    protected Node rootNode;
    
    // node with children
    protected static class Node {
        char ch;
        ArrayList<Node> alChildren;
        public Node(char ch) {
            this.ch = ch;
        }
        public ArrayList<Node> getChildren() {
            if (alChildren == null) alChildren = new ArrayList<>();
            return alChildren;
        }
        
    }
    
    
    // recursive listing by tree leg
    protected void displayByLeg() {
        final StringBuilder sb = new StringBuilder(32);
        _displayByLeg(rootNode, sb);
        System.out.printf("DisplayByLeg: \"%s\"\n", sb.toString());
        
    }
    private void _displayByLeg(final Node node, final StringBuilder sb) {
        if (node == null || sb == null) return;
        if (sb.length() > 0) sb.append(' ');
        sb.append(node.ch);
        if (node.alChildren == null) return;
        
        for (Node childNode : node.alChildren) {
            _displayByLeg(childNode, sb);
        }
    }

    
    
    // recursive listing by tree row
    protected void displayByRow() {
        final StringBuilder sb = new StringBuilder(32);
        if (rootNode != null) sb.append(rootNode.ch);

        _displayByRow(rootNode, sb);
        System.out.printf("DisplayByRow: \"%s\"\n", sb.toString());
    }
    private void _displayByRow(final Node node, final StringBuilder sb) {
        if (node == null || sb == null) return;
        
        if (node.alChildren == null) return;
        
        final StringBuilder sb2 = new StringBuilder(32);
        for (Node childNode : node.alChildren) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(childNode.ch);
            _displayByRow(childNode, sb2);
        }
        if (sb2.length() > 0) {
            sb.append(' ');
            sb.append(sb2);
        }
    }
    
    
    
    
    public static void main(String[] args) {

        
        // populate this tree
        /*
                C
              / | \
             /  |  \
            E   F   S
           / \     / \
          H   B   P   D
          
        */        
        
        Tree tree = new Tree();
        tree.rootNode = new Node('C');
        
        // 1st  leg
        Node tn = new Node('E');
        tree.rootNode.getChildren().add(tn);
        Node tn2 = new Node('H');
        tn.getChildren().add(tn2);
        tn2 = new Node('B');
        tn.getChildren().add(tn2);
        
        // 2nd  leg
        tn = new Node('F');
        tree.rootNode.getChildren().add(tn);
        
        // 3rd  leg
        tn = new Node('S');
        tree.rootNode.getChildren().add(tn);
        tn2 = new Node('P');
        tn.getChildren().add(tn2);
        tn2 = new Node('D');
        tn.getChildren().add(tn2);
        
        // listings
        tree.displayByLeg();  // => DisplayByLeg: "C E H B F S P D"
        tree.displayByRow();  // => DisplayByRow: "C E F S H B P D"
        
        
    }
    
}
