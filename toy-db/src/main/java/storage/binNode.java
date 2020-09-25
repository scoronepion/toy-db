package storage;

/**
 * @program: toy-db
 * @description: 节点
 * @author: scoronepion
 * @create: 2020-09-25 18:39
 **/
public class binNode<T> {
    private Integer key;
    private T value;
    private binNode<T> left;
    private binNode<T> right;
    private int size;

    public binNode(Integer key, T value, int size) {
        this.key = key;
        this.value = value;
        this.size = size;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public binNode<T> getLeft() {
        return left;
    }

    public void setLeft(binNode<T> left) {
        this.left = left;
    }

    public binNode<T> getRight() {
        return right;
    }

    public void setRight(binNode<T> right) {
        this.right = right;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
