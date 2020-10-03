package storage;

import java.util.AbstractMap;
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

    // 查找当前节点是否包含key这一关键字，不存在则返回-1
    protected int contains(K key) {
        // 二分查找
        int low = 0, high = entries.size() - 1, mid;
        int comp;
        while (low <= high) {
            mid = (low + high) / 2;
            comp = key.compareTo(entries.get(mid).getKey());
            if (comp == 0) {
                return mid;
            } else if (comp > 0) {
                // 收缩左边界
                low = mid + 1;
            } else {
                // 收缩右边界
                high = mid - 1;
            }
        }
        // 没有找到
        return -1;
    }

    public V get(K key) {
        // 如果是叶子节点，这个叶子节点上存储了要查找的数据
        if (isLeaf) {

//            // 二分查找
//            int low = 0, high = entries.size() - 1, mid;
//            int comp;
//            while (low <= high) {
//                mid = (low + high) / 2;
//                comp = key.compareTo(entries.get(mid).getKey());
//                if (comp == 0) {
//                    return entries.get(mid).getValue();
//                } else if (comp > 0) {
//                    // 收缩左边界
//                    low = mid + 1;
//                } else {
//                    // 收缩右边界
//                    high = mid - 1;
//                }
//            }
//            // 没有找到
//            return null;

            int res = contains(key);
            if (res == -1) {
                return null;
            } else {
                return entries.get(res).getValue();
            }
        } else {
            // 不是叶子节点，要先在索引中查找
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

    // 将key-value插入到当前节点的关键字中
    protected void insertOrUpdate(K key, V value) {
        // 二叉查找
        int low = 0, high = entries.size() - 1, mid;
        int comp;
        while (low <= high) {
            mid = (low + high) / 2;
            comp = key.compareTo(entries.get(mid).getKey());
            if (comp == 0) {
                // 找到了已存在的key
                entries.get(mid).setValue(value);
                break;
            } else if (comp > 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        if (low > high) {
            // 是个新元素，没找着，此时low记录了它应该插入的位置
            entries.add(low, new AbstractMap.SimpleEntry<K, V>(key, value));
        }
    }

    public void insertOrUpdate(K key, V value, bPlusTree<K, V> tree) {
        // 如果是叶子节点
        if (isLeaf) {
            // 判断是否需要分裂，不需要的话直接插入或更新
            // 先判断key是否存在当前节点上，存在的话直接更新，不存在的话需要插入，所以再判断当前是否需要分裂
            if (contains(key) != -1 || entries.size() < tree.getOrder()) {
                insertOrUpdate(key, value);
                if (tree.getHeight() == 0) {
                    tree.setHeight(1);
                }
                return;
            }

            // 需要分裂，分裂成左右两个叶子节点
            bPlusNode<K, V> left = new bPlusNode<K, V>(true);
            bPlusNode<K, V> right = new bPlusNode<K, V>(true);

            // 该叶子节点前驱存在
            if (previous != null) {
                previous.next = left;
                left.previous = previous;
            } else {
                // 该叶子节点前驱不存在，意味着它自己就是第一个节点，分裂后需要left充当第一个节点
                tree.setHead(left);
            }
            if (next != null) {
                next.previous = right;
                right.next = next;
            }

            left.next = right;
            right.previous = left;
            previous = null;
            next = null;

            // 将原来节点的关键字分配到left和right中，并插入目标key-value
            copy2Nodes(key, value, left, right, tree);

            // 如果这个叶子节点不是根节点
            // TODO: 此处是否可换成isRoot？
            if (parent != null) {
                // 获取当前节点在父节点的孩子数组中的下标
                int index = parent.children.indexOf(this);
                // 从父节点的孩子数组中删掉自己
                parent.children.remove(this);
                // left节点父亲指向当前的父节点
                left.parent = parent;
                // right节点父亲指向当前父节点
                right.parent = parent;
                // 父节点孩子数组中，index下标处插入left
                parent.children.add(index, left);
                // 父节点孩子数组中，index + 1下标处插入right
                parent.children.add(index + 1, right);
                // 将right的第一个关键字上升到父节点关键字数组中，作为索引
                parent.entries.add(index, right.entries.get(0));
                // 删除当前节点关键字
                entries = null;
                // 删除当前节点孩子节点
                children = null;

                // TODO 检查父节点是否需要更新
                // 删除父节点
                parent = null;
            }
        }
    }

    // 当叶子节点分裂的时候，会调用此函数来将节点里的关键字均分到left和right里，并且在恰当的位置插入目标key-value
    private void copy2Nodes(K key, V value, bPlusNode<K, V> left, bPlusNode<K, V> right, bPlusTree<K, V> tree) {
        // 计算左右两个节点的关键字长度。两个节点长度之和为m+1，所以两边的各自的节点长度应该为(m+1)/2，余数统一加到左边
        int leftSize = (tree.getOrder() + 1) / 2 + (tree.getOrder() + 1) % 2;
        // 用于记录目标元素是否被插入
        boolean inserted = false;
        // 遍历该节点的entry，将其分配到两个节点上
        for (int i = 0; i < entries.size(); i++) {
            // 选择插入哪边，先处理左边
            if (leftSize != 0) {
                leftSize--;
                if (!inserted && entries.get(i).getKey().compareTo(key) > 0) {
                    // 未插入过，且要插入的位置就是i
                    left.entries.add(new AbstractMap.SimpleEntry<K, V>(key, value));
                    inserted = true;
                    // i--因为i这个kv还未插入，需要下次循环插入
                    i--;
                } else {
                    // 处理普通情况
                    left.entries.add(entries.get(i));
                }
            } else {
                // 插入右边
                if (!inserted && entries.get(i).getKey().compareTo(key) > 0) {
                    right.entries.add(new AbstractMap.SimpleEntry<K, V>(key, value));
                    inserted = true;
                    i--;
                } else {
                    right.entries.add(entries.get(i));
                }
            }
        }
        // 如果此时还未插入，说明目标kv应该插入right的最后一个位置
        if (!inserted) {
            right.entries.add(new AbstractMap.SimpleEntry<K, V>(key, value));
        }
    }
}
