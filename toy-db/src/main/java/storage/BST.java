package storage;

import java.util.ArrayList;

/**
 * @program: toy-db
 * @description: 二叉搜索树
 * @author: scoronepion
 * @create: 2020-09-25 18:55
 **/
public class BST<T> {
    public binNode<T> root;
    public ArrayList<T> midOrder;

    public BST() {
        midOrder = new ArrayList<T>();
    }

    public void add(Integer key, T value) {
        // 当前树为空，则填充根节点
        if (root == null) {
            root = new binNode<T>(key, value, 1);
            return;
        }
        root = add(root, key, value);
    }

    public int size(binNode<T> node) {
        if (node == null) return 0;
        return node.getSize();
    }

    private binNode<T> add(binNode<T> x, Integer key, T value) {
        // 如果x为空，意味着需要新建节点来存储key-value
        if (x == null) {
            return new binNode<T>(key, value, 1);
        }
        int cmp = key.compareTo(x.getKey());
        if (cmp < 0) {
            // key比x要小，放到x的左子树里
            x.setLeft(add(x.getLeft(), key, value));
        } else if (cmp > 0) {
            // key比x要大，放到x的右子树里
            x.setRight(add(x.getRight(), key, value));
        } else {
            System.out.printf("重复插入 (%d, %s), 当前已存在 (%d, %s)\n", key, value, x.getKey(), x.getValue());
        }

        x.setSize(size(x.getLeft()) + size(x.getRight()) + 1);
        return x;
    }

    public void visit(binNode<T> node) {
        if (node == null) return;
        visit(node.getLeft());
        midOrder.add(node.getValue());
        visit(node.getRight());
    }

    public static void main(String[] args) {
        BST<String> ins = new BST<String>();
        ins.add(1, "1");
        ins.add(2, "2");
        ins.add(3, "3");
        ins.add(4, "4");
        ins.add(5, "5");
        ins.add(6, "6");
        ins.visit(ins.root);
        System.out.println(ins.midOrder);
    }
}
