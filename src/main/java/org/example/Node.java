package org.example;

public class Node {
    private final String row;
    private final int number;
    private int size = 1;
    private Node parent = null;

    private Node(String row, int number) {
        this.row = row;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return getParent().size;
    }

    public String getRow() {
        return row;
    }

    public static void unionNodes(Node left, Node right) {
        Node topLeft = left.getParent();
        Node topRight = right.getParent();

        if (topLeft.number == topRight.number) {
            return;
        }

        if (topLeft.number < topRight.number) {
            topRight.parent = topLeft;
            topLeft.size += topRight.size;
        } else {
            topLeft.parent = topRight;
            topRight.size += topLeft.size;
        }
    }

    public Node getParent() {
        if (parent != null) {
            parent = parent.getParent();
            return parent;
        }

        return this;
    }

    public static Node createNode(String src, int number) {
        return new Node(src, number);
    }

    public static class NodeElement {
        private final String data;
        private final Node parentNode;

        private NodeElement(String data, Node parentNode) {
            this.data = data;
            this.parentNode = parentNode;
        }

        public String getData() {
            return data;
        }

        public Node getParentNode() {
            return parentNode;
        }

        public static NodeElement createElement(String data, Node parentNode) {
            return new NodeElement(data, parentNode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NodeElement that = (NodeElement) o;

            return data.equals(that.data);
        }
    }
}
