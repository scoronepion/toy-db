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
                    if (comp == 0) {
                        // 找到了
                        // mid + 1是因为children长度比entries多1，所以下标要偏移1
                        return children.get(mid + 1).get(key);
                    } else if (comp > 0) {
                        // key比mid还要大，左边界收缩
                        low = mid + 1;
                    } else {
                        // key比mid小，右边界收缩
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
        // 判断是否是叶子节点，如果不是叶子节点，则需要继续向下寻找
        if (isLeaf) {
            // 如果节点不包含该关键字，则直接返回
            if (contains(key) == -1) {
                return null;
            }

            // 如果即是叶子节点，也是根节点，则直接删除
            if (isRoot) {
                if (entries.size() == 1) {
                    tree.setHeight(0);
                }
                return remove(key);
            }

            // 如果本身关键字数量大于M/2，则可以直接删除
            if (entries.size() > tree.getOrder() / 2 && entries.size() > 2) {
                return remove(key);
            }

            // 如果本身关键字数量少于M/2，但是前面或者后面节点关键字数量大于M/2，那就从他们那拿一个元素过来
            // 从前驱节点拿
            if (previous != null && previous.parent == parent
                    && previous.entries.size() > tree.getOrder() / 2
                    && previous.entries.size() > 2) {
                // 获取previous最后一个元素，并添加到本身的首位
                int size = previous.entries.size();
                entries.add(0, previous.entries.remove(size - 1));
                // 修改父节点的索引
                int index = parent.children.indexOf(previous);
                // 为什么是index，因为因为children长度比entries多1，所以这里用previous的下标
                parent.entries.set(index, entries.get(0));
                return remove(key);
            }
            // 从后继节点拿
            if (next != null && next.parent == parent
                    && next.entries.size() > tree.getOrder() / 2
                    && next.entries.size() > 2) {
                // 获取next第一个元素，并将其添加到本身entry
                entries.add(next.entries.remove(0));
                // 为什么这里用this的下标，理由跟上面一样
                int index = parent.children.indexOf(this);
                parent.entries.set(index, next.entries.get(0));
                return remove(key);
            }

            // 如果本身关键字数量少于M/2，且前面或者后面节点关键字数量都小于M/2，则将自己与前面或者后面节点合并
            // 与前面节点合并
            if (previous != null && previous.parent == parent
                    && (previous.entries.size() <= tree.getOrder() / 2 || previous.entries.size() <= 2)) {
                // 先把key删掉
                V returnValue = remove(key);
                // 将当前节点所有关键字添加到previous末尾
                previous.entries.addAll(entries);
                // 这样的话可以避免头插导致空间移动，开销加大
                entries = previous.entries;
                // 从父节点孩子数组中删除previous
                parent.children.remove(previous);
                // 删除previous
                previous.parent = null;
                previous.entries = null;
                // 更新叶子节点链表
                if (previous.previous != null) {
                    // previous不是头节点
                    bPlusNode<K, V> temp = previous;
                    temp.previous.next = this;
                    previous = temp.previous;
                    temp.previous = null;
                    temp.next = null;
                } else {
                    // previous是头节点
                    tree.setHead(this);
                    previous.next = null;
                    previous = null;
                }
                // 删除parent里的索引，下标用了this的下标，因为已经融合完成了
                parent.entries.remove(parent.children.indexOf(this));
                // 如果父节点不是root，并且父节点孩子数量都是超过了M/2的
                // 或者父节点是root，并且父节点孩子个数大于等于2
                // 那么表明不用再调整了
                if ((!parent.isRoot && parent.children.size() >= tree.getOrder() / 2 && parent.children.size() >= 2)
                        || (parent.isRoot && parent.children.size() >= 2)) {
                    return returnValue;
                }
                // 处理中间节点
                parent.updateRemove(tree);
                return returnValue;
            }
            // 与后面节点合并
            if (next != null && next.parent == parent
                    && (next.entries.size() <= tree.getOrder() / 2 || next.entries.size() <= 2)) {
                // 先保存要删除的值
                V returnValue = remove(key);
                // 将next节点的所有关键字添加到自己关键字里
                entries.addAll(next.entries);
                // 删除next
                next.parent = null;
                next.entries = null;
                parent.children.remove(next);
                // 更新叶子节点链表
                if (next.next != null) {
                    // next不是最后一个节点
                    bPlusNode<K, V> temp = next;
                    temp.next.previous = this;
                    next = temp.next;
                    temp.previous = null;
                    temp.next = null;
                } else {
                    next.previous = null;
                    next = null;
                }
                // 删除parent里的索引，下标用了this的下标，因为已经融合完成了
                parent.entries.remove(parent.children.indexOf(this));
                // 如果父节点不是root，并且父节点孩子数量都是超过了M/2的
                // 或者父节点是root，并且父节点孩子个数大于等于2
                // 那么表明不用再调整了
                if ((!parent.isRoot && parent.children.size() >= tree.getOrder() / 2 && parent.children.size() >= 2)
                        || (parent.isRoot && parent.children.size() >= 2)) {
                    return returnValue;
                }
                // 处理中间节点
                parent.updateRemove(tree);
                return returnValue;
            }
        } else {
            // 不是叶子节点，开始二分查找
            // 不是叶子节点，要先在索引中查找
            // 如果key比当前节点最左边（第一个index）的key还小，那就沿着最左边的子节点（第一个节点）继续找。为什么没有等于？因为相等的都被分到了右边
            if (key.compareTo(entries.get(0).getKey()) < 0) {
                return children.get(0).remove(key);
                // 如果key比当前节点最右边（最后一个index）的key还大，那就沿着最右边的子节点（最后一个节点）继续找
            } else if (key.compareTo(entries.get(entries.size() - 1).getKey()) >= 0) {
                return children.get(children.size() - 1).remove(key);
                // 沿着比key大的前一个entry对应的子节点
            } else {
                // 二分查找
                int low = 0, high = entries.size() - 1, mid;
                int comp;
                while (low <= high) {
                    mid = (low + high) / 2;
                    comp = key.compareTo(entries.get(mid).getKey());
                    if (comp == 0) {
                        // 找到了
                        // mid + 1是因为children长度比entries多1，所以下标要偏移1
                        return children.get(mid + 1).remove(key);
                    } else if (comp > 0) {
                        // key比mid还要大，左边界收缩
                        low = mid + 1;
                    } else {
                        // key比mid小，右边界收缩
                        high = mid - 1;
                    }
                }
                // 为什么这里又是low？因为如果上面二分查找没找到的话，low正好对在第一个大于key的entry上
                // 所以对应到children里面，下标就应该是 (low - 1) + 1 = low，low - 1表示比key大的前一个entry，+1表示对应到children里的下标
                return children.get(low).remove(key);
            }
        }
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

                // 检查父节点是否需要更新
                parent.updateInsert(tree);
                // 删除父节点
                parent = null;
            } else {
                // 如果是根节点
                // 根节点标识置false
                isRoot = false;
                // 新建根节点
                bPlusNode<K, V> parent = new bPlusNode<K, V>(false, true);
                tree.setRoot(parent);
                tree.setHeight(tree.getHeight() + 1);
                left.parent = parent;
                right.parent = parent;
                parent.children.add(left);
                parent.children.add(right);
                // 将right的第一个关键字上升到父节点关键字数组中，作为索引
                parent.entries.add(right.entries.get(0));
                // 删除当前节点关键字
                entries = null;
                // 删除当前节点孩子节点
                children = null;
            }
            return;
        } else {
            // 不是叶子节点，那就要先找到在哪里插入，逻辑与get相似
            // 如果key比当前节点最左边（第一个index）的key还小，那就沿着最左边的子节点（第一个节点）继续找。为什么没有等于？因为相等的都被分到了右边
            if (key.compareTo(entries.get(0).getKey()) < 0) {
                children.get(0).insertOrUpdate(key, value, tree);
                // 如果key比当前节点最右边（最后一个index）的key还大，那就沿着最右边的子节点（最后一个节点）继续找
            } else if (key.compareTo(entries.get(entries.size() - 1).getKey()) >= 0) {
                children.get(children.size() - 1).insertOrUpdate(key, value, tree);
                // 沿着比key大的前一个entry对应的子节点
            } else {
                // 二分查找
                int low = 0, high = entries.size() - 1, mid;
                int comp;
                while (low <= high) {
                    mid = (low + high) / 2;
                    comp = key.compareTo(entries.get(mid).getKey());
                    if (comp == 0) {
                        // 找到了
                        // mid + 1是因为children长度比entries多1，所以下标要偏移1
                        children.get(mid + 1).insertOrUpdate(key, value, tree);
                        break;
                    } else if (comp > 0) {
                        // key比mid还要大，左边界收缩
                        low = mid + 1;
                    } else {
                        // key比mid小，右边界收缩
                        high = mid - 1;
                    }
                }
                if (low > high) {
                    // 为什么这里又是low？因为如果上面二分查找没找到的话，low正好对在第一个大于key的entry上
                    // 所以对应到children里面，下标就应该是 (low - 1) + 1 = low，low - 1表示比key大的前一个entry，+1表示对应到children里的下标
                    children.get(low).insertOrUpdate(key, value, tree);
                }
            }
            return;
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

    // 插入节点后父节点的更新（主要判断是否要分裂）
    protected void updateInsert(bPlusTree<K, V> tree) {
        // 判断是否要分裂
        if (children.size() < tree.getOrder()) {
            return;
        }

        // 分裂成左右两个节点
        bPlusNode<K, V> left = new bPlusNode<K, V>(false);
        bPlusNode<K, V> right = new bPlusNode<K, V>(false);
        // 分配左右节点的长度，逻辑同copy2Nodes
        int leftSize = (tree.getOrder() + 1) / 2 + (tree.getOrder() + 1) % 2;
        int rightSize = (tree.getOrder() + 1) / 2;
        // 分配孩子节点
        for (int i = 0; i < leftSize; i++) {
            left.children.add(children.get(i));
            children.get(i).parent = left;
        }
        for (int i = 0; i < rightSize; i++) {
            right.children.add(children.get(leftSize + i));
            children.get(leftSize + i).parent = right;
        }
        // 分配关键字，下面的代码会把leftSize - 1这个索引的内容空出来
        // leftSize - 1 这个关键字要升到上一层当索引
        for (int i = 0; i < leftSize - 1; i++) {
            left.entries.add(entries.get(i));
        }
        for (int i = 0; i < rightSize - 1; i++) {
            right.entries.add(entries.get(leftSize + i));
        }

        // 如果不是根节点
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
            // 将leftSize - 1上升到父节点关键字数组中，作为索引
            parent.entries.add(index, entries.get(leftSize - 1));
            // 删除当前节点关键字
            entries = null;
            // 删除当前节点孩子节点
            children = null;

            // 检查父节点是否需要更新
            parent.updateInsert(tree);
            // 删除父节点
            parent = null;
        } else {
            // 如果是根节点
            // 首先root标识置为false
            isRoot = false;
            // 新建root节点
            bPlusNode<K, V> parent = new bPlusNode<K, V>(false, true);
            // 设置为tree的root
            tree.setRoot(parent);
            // 树高加一
            tree.setHeight(tree.getHeight() + 1);
            // 设置孩子节点的父节点
            left.parent = parent;
            right.parent = parent;
            parent.children.add(left);
            parent.children.add(right);
            parent.entries.add(entries.get(leftSize - 1));
            // 删除当前节点关键字
            entries = null;
            // 删除当前节点孩子节点
            children = null;
        }
    }

    // 在节点上删除key关键字
    protected V remove(K key) {
        int index = contains(key);
        if (index == -1) {
            return null;
        } else {
            return entries.remove(index).getValue();
        }
    }

    // 删除节点后的中间节点更新
    // 为什么这里还要判断节点是否需要合并？因为这里需要递归调用上层parent，直到不需要合并为止
    protected void updateRemove(bPlusTree<K, V> tree) {
        // 如果孩子节点数量大于等于M/2且大于等于2，则不需要分裂
        if (children.size() >= tree.getOrder() / 2 && children.size() >= 2) {
            return;
        }

        // 需要分裂
        if (isRoot) {
            // 如果是根节点，且子节点数量大于等于2，则不需要进行操作
            if (children.size() >= 2) return;
            // 是根节点，但是子节点个数小于2，则将自己与子节点合并
            // 因为children比entries多一个元素，当children长度为1时，entries长度已经为0，所以直接取孩子节点作为root即可
            bPlusNode<K, V> root = children.get(0);
            tree.setRoot(root);
            tree.setHeight(tree.getHeight() - 1);
            root.parent = null;
            root.isRoot = true;
            entries = null;
            children = null;
            return;
        } else {
            // 不是根节点，需要想办法从前面/后面节点拿一个元素过来，或者与前面/后面节点合并
            // 计算前后节点
            int curIndex = parent.children.indexOf(this);
            int prevIndex = curIndex - 1;
            int nextIndex = curIndex + 1;
            bPlusNode<K, V> prevNode = null, nextNode = null;
            if (prevIndex >= 0) {
                prevNode = parent.children.get(prevIndex);
            }
            if (nextIndex < parent.children.size()) {
                nextNode = parent.children.get(nextIndex);
            }

            // 如果前/后节点子节点数大于M/2且大于2，则从他们那里拿一个过来
            // 从前面的节点拿
            if (prevNode != null
                    && prevNode.children.size() > tree.getOrder() / 2
                    && prevNode.children.size() > 2) {
                // 计算最后一个元素的下标
                int index = prevNode.children.size() - 1;
                bPlusNode<K, V> temp = prevNode.children.get(index);
                // 从前一个节点的孩子节点中删除index下标节点
                prevNode.children.remove(index);
                temp.parent = this;
                // 在当前节点的孩子数组前端插入temp
                children.add(0, temp);
                // 修改当前节点的索引，因为我们是从左边挪了一个节点，而这个节点的索引是存储在当前节点的父节点上的
                // 所以我们要找到prevNode在父节点孩子数组中的下标，按照之前的“索引长度比孩子数组长度少1”的规则，这个下标就是我们挪过来的节点的索引
                int preIndex = parent.children.indexOf(prevNode);
                entries.add(0, parent.entries.get(preIndex));
                // 将prevNode最后一个索引上升到父节点中
                parent.entries.set(preIndex, prevNode.entries.remove(index - 1));
                return;
            }
            // 从后面节点拿
            if (nextNode != null
                    && nextNode.children.size() > tree.getOrder() / 2
                    && nextNode.children.size() > 2) {
                // 取出要拿的这个节点
                bPlusNode<K, V> temp = nextNode.children.get(0);
                nextNode.children.remove(0);
                temp.parent = this;
                children.add(temp);
                // 这里的逻辑跟上面一样
                int preIndex = parent.children.indexOf(this);
                entries.add(parent.entries.get(preIndex));
                // 上升索引
                parent.entries.set(preIndex, nextNode.entries.remove(0));
                return;
            }

            // 前/后节点子节点数量都小于M/2，无法拿一个过来，所以选择合并
            // 与前面的节点合并
            if (prevNode != null
                    && (prevNode.children.size() <= tree.getOrder() / 2 || prevNode.children.size() <= 2)) {
                // 将当前节点的所有子节点添加到前一个节点的子节点数组中
                prevNode.children.addAll(children);
                // 将前一个节点的子节点的父指针指向当前的节点
                for (int i = 0; i < prevNode.children.size(); i++) {
                    prevNode.children.get(i).parent = this;
                }
                // 将父节点上的索引拉下来
                int preIndex = parent.children.indexOf(prevNode);
                prevNode.entries.add(parent.entries.get(preIndex));
                // 将当前节点的关键字加入到prevNode关键字里
                prevNode.entries.addAll(entries);
                // 将自己的children和entries指向前一个节点的children和entries
                children = prevNode.children;
                entries = prevNode.entries;

                // 更新父节点的关键字列表
                // 删除prevNode
                parent.children.remove(prevNode);
                prevNode.parent = null;
                prevNode.entries = null;
                prevNode.children = null;
                // 因为当前节点已经融合好了，所以删除索引直接用当前节点的下标
                parent.entries.remove(parent.children.indexOf(this));
                // 如果父节点不是root，并且父节点孩子数量都是超过了M/2的
                // 或者父节点是root，并且父节点孩子个数大于等于2
                // 那么表明不用再调整了
                if ((!parent.isRoot && parent.children.size() >= tree.getOrder() / 2 && parent.children.size() >= 2)
                        || (parent.isRoot && parent.children.size() >= 2)) {
                    return;
                }
                // 继续向上调整
                parent.updateRemove(tree);
                return;
            }

            // 与后面的节点合并
            if (nextNode != null
                    && (nextNode.children.size() <= tree.getOrder() / 2 || nextNode.children.size() <= 2)) {
                // 先把nextNode的所有孩子节点添加到自己的孩子数组中
                children.addAll(nextNode.children);
                for (int i = 0; i < nextNode.children.size(); i++) {
                    nextNode.children.get(i).parent = this;
                }
                // 把父节点上的索引拉下来
                int index = parent.children.indexOf(this);
                entries.add(parent.entries.get(index));
                // 将nextNode的关键字加入到当前节点关键字中
                entries.addAll(nextNode.entries);
                // 删除nextNode
                parent.children.remove(nextNode);
                nextNode.parent = null;
                nextNode.entries = null;
                nextNode.children = null;
                // 因为当前节点已经融合好了，所以删除索引直接用当前节点的下标
                parent.entries.remove(parent.children.indexOf(this));
                // 如果父节点不是root，并且父节点孩子数量都是超过了M/2的
                // 或者父节点是root，并且父节点孩子个数大于等于2
                // 那么表明不用再调整了
                if ((!parent.isRoot && parent.children.size() >= tree.getOrder() / 2 && parent.children.size() >= 2)
                        || (parent.isRoot && parent.children.size() >= 2)) {
                    return;
                }
                // 继续向上调整
                parent.updateRemove(tree);
                return;
            }
        }
    }
}
