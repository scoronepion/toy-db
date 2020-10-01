package storage;

import java.util.ArrayList;
import java.util.Map.Entry;

public class bPlusNode <K extends Comparable<K>, V> {
    // 是否为叶子节点
    protected boolean isLeaf;

    // 是否为根节点
    protected boolean isRoot;

    // 父节点
    protected bPlusNode<K, V> parent;

    // 叶节点的前驱节点
    protected bPlusNode<K, V> previous;

    // 叶节点的后继节点
    protected bPlusNode<K, V> next;

    // 叶节点的数据 / 非叶节点的索引值。这俩都叫关键字
    protected ArrayList<Entry<K, V>> entries;

    // 子节点
    protected ArrayList<bPlusNode<K, V>> children;

    public bPlusNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
        entries = new ArrayList<Entry<K, V>>();

        // 如果不是叶子节点，需要初始化自己的孩子节点
        if (!isLeaf) {
            children = new ArrayList<bPlusNode<K, V>>();
        }
    }

    public bPlusNode(boolean isLeaf, boolean isRoot) {
        this(isLeaf);
        this.isRoot = isRoot;
    }

    public V get(K key) {
        // 如果是叶子节点，这个叶子节点上存储了要查找的数据
        if (isLeaf) {
            // 二分查找
            int low = 0, high = entries.size() - 1, mid;
            int comp;
            while (low <= high) {
                mid = (low + high) / 2;
                comp = key.compareTo(entries.get(mid).getKey());
                if (comp == 0) {
                    return entries.get(mid).getValue();
                } else if (comp > 0) {
                    // 收缩左边界
                    low = mid + 1;
                } else {
                    // 收缩右边界
                    high = mid - 1;
                }
            }
            // 没有找到
            return null;
        // 不是叶子节点，要先在索引中查找
        } else {
            // 如果key比当前节点最左边（第一个index）的key还小，那就沿着最左边的子节点（第一个节点）继续找。为什么没有等于？因为相等的都被分到了右边
            if (key.compareTo(entries.get(0).getKey()) < 0) {
                return children.get(0).get(key);
            // 如果key比当前节点最右边（最后一个index）的key还大，那就沿着最右边的子节点（最后一个节点）继续找
            } else if (key.compareTo(entries.get(entries.size() - 1).getKey()) >= 0) {
                return children.get(children.size() - 1).get(key);
            // 沿着比key大的前一个entry对应的子节点
            } else {
                // 二分查找
                int low = 0, high = entries.size() - 1, mid;
                int comp;
                while (low <= high) {
                    mid = (low + high) / 2;
                    comp = key.compareTo(entries.get(mid).getKey());
                    // 找到了
                    if (comp == 0) {
                        // mid + 1是因为children长度比entries多1，所以下标要偏移1
                        return children.get(mid + 1).get(key);
                    // key比mid还要大，左边界收缩
                    } else if (comp > 0) {
                        low = mid + 1;
                    // key比mid小，右边界收缩
                    } else {
                        high = mid - 1;
                    }
                }
                // 为什么这里又是low？因为如果上面二分查找没找到的话，low正好对在第一个大于key的entry上
                // 所以对应到children里面，下标就应该是 (low - 1) + 1 = low，low - 1表示比key大的前一个entry，+1表示对应到children里的下标
                return children.get(low).get(key);
            }
        }
    }

    public V remove(K key, bPlusTree<K, V> tree) {
        return null;
    }

    public void insertOrUpdate(K key, V value, bPlusTree<K, V> tree) {

    }
}
