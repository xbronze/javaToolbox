package cn.javatoolbox.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xbronze
 * @date: 2023-04-06 16:36
 * @description: 最近最久未使用策略的缓存
 */
public class LRUCache<K, V> {

    private ConcurrentHashMap<Object, Node> concurrentHashMap;

    private int size;

    private Node head = new Node(-1, -1);
    private Node tail = new Node(-1, -1);


    public LRUCache(int size) {
        this.size = size;
        this.concurrentHashMap = new ConcurrentHashMap<Object, Node>(size);
        this.tail.prev = head;
        this.head.next = this.tail;
    }

    /**
     * 获取缓存
     * @param key
     * @return
     */
    public Object get(K key){
        checkNotNull(key);
        if (concurrentHashMap.isEmpty()) {
            return null;
        }
        if (concurrentHashMap.containsKey(key)) {
            Node currentNode = concurrentHashMap.get(key);
            // 从链表中移除，并添加在链表尾部
            removeFromNode(currentNode);
            return currentNode.value;
        } else {
            return null;
        }
    }

    /**
     * 添加缓存
     * @param key
     * @param value
     */
    public void put(K key, V value) {
        checkNotNull(key);
        checkNotNull(value);
        // 当缓存存在是，更新缓存
        if (concurrentHashMap.containsKey(key)) {
            Node currentNode = concurrentHashMap.get(key);
            // 从链表中移除，并添加在链表尾部
            removeFromNode(currentNode);
            return;
        }
        if (isFull()) {
            concurrentHashMap.remove(head.next.key);
            head.next.next.prev = head;
            head.next = head.next.next;
        }
        // 如果缓存还有空间
        Node node = new Node(key, value);
        concurrentHashMap.put(key, node);
        addToTail(node);
    }

    /**
     * 判断缓存空间是否已满
     * @return
     */
    private boolean isFull() {
        return concurrentHashMap.size() == this.size;
    }

    /**
     * 从链表中移除，并添加在链表尾部
     * @param node
     */
    private void removeFromNode(Node node){
        if (node == null) {
            return;
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
        // 然后添加到链表的尾部
        addToTail(node);
    }

    /**
     * 添加节点到链表尾部
     * @param currentNode
     */
    private void addToTail(Node currentNode) {
        this.tail.prev.next = currentNode;
        currentNode.prev = this.tail.prev;
        this.tail.prev = currentNode;
        currentNode.next = this.tail;
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    private class Node {

        Node prev;
        Node next;
        Object key;
        Object value;

        public Node(Object key, Object value) {
            this.key = key;
            this.value = value;
            this.prev = this.next = null;
        }
    }
}
