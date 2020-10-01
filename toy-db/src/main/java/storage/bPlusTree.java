package storage;

/**
 * @program: toy-db
 * @description: B+树
 * @author: scoronepion
 * @create: 2020-09-25 15:39
 * B+树定义
 * 1. 任意非叶子节点最多有M个子节点，多了就分裂；且M>2；M为B+树的阶数；
 * 2. 除根节点以外的非叶子节点至少有(M+1)/2个子节点，少了就合并；
 * 3. 根节点至少有2个子节点；
 * 4. 除根节点以外每个结点至少1个关键字，最多M-1个关键字；
 * 5. 非叶子节点的子树指针比关键字多1个；
 * 6. 非叶子节点的所有key按照升序存放；
 * 7. 所有叶子节点存放于同一层；
 * 8. 为所有叶子节点增加一个链指针；
 * 9. 所所有关键字都在叶子节点出现；
 **/
public class bPlusTree <K extends Comparable<K>, V>{
    // 根节点
    protected bPlusNode<K, V> root;

    // 阶数，M值
    protected int order;

    // 叶子节点的链表头
    protected bPlusNode<K, V> head;

    // 树高
    protected int height = 0;

    // ----------------

    public bPlusNode<K, V> getRoot() {
        return root;
    }

    public void setRoot(bPlusNode<K, V> root) {
        this.root = root;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public bPlusNode<K, V> getHead() {
        return head;
    }

    public void setHead(bPlusNode<K, V> head) {
        this.head = head;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // ----------------

    public V get(K key) {
        return root.get(key);
    }

    public V remove(K key) {
        return root.remove(key, this);
    }

    public void insertOrUpdate(K key, V value) {
        root.insertOrUpdate(key, value, this);
    }

    public bPlusTree(int order) {
        if (order < 3) {
            System.out.println("Order must be greater than 2");
            System.exit(0);
        }
        this.order = order;
        root = new bPlusNode<K, V>(true, true);
        head = root;
    }

    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
