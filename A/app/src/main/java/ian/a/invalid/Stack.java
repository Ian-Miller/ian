package ian.a.invalid;

import android.support.annotation.NonNull;

/**
 * Created by Ian on 8/23/2017.
 */

public class Stack<L> {
    public final Node<L> head = new Node<L>(Node.TYPE_HEAD);
    public final Node<L> end = new Node<L>(Node.TYPE_END);
    public final int CAPACITY;

    public Stack(L[] lists, int capacity){
        CAPACITY = capacity;
        if(lists == null || lists.length == 0){
            head.next = end;
            end.previous = head;
            return;
        }
        int l = lists.length;
        int p = 0;
        Node<L> next;
        Node<L> previous = head;
        do {
            next = new Node<>(lists[p]);
            previous.next = next;
            next.previous = previous;
            previous = next;
            p++;
        } while (p < l);
        next.next = end;
        end.previous = next;
    }

    private Node<L> previous(Node<L> node){
        if(node == null){
            return null;
        }
        int type = node.type;
        if(node.type == Node.TYPE_HEAD || !in(node, true)){
            return null;
        }
        return node.previous;
    }

    private Node<L> next(Node<L> node){
        if(node == null){
            return null;
        }
        int type = node.type;
        if(node.type == Node.TYPE_END || !in(node, true)){
            return null;
        }
        return node.next;

    }

    private boolean in(Node<L> node, boolean strict){
        Node<L> now = head;
        if(node == null){
            return false;
        }
        while (now.next != end){
            if(strict){
                if (now.next.strictEquals(node)){
                    return true;
                }
                now = now.next;
            }else {
                if (now.next.equals(node)){
                    return true;
                }
                now = now.next;
            }
        }
        return false;
    }

    private @NonNull Node<L> get(Node<L> node){
        Node<L> now = head;
        while (now != end){
            if(now.equals(node)){
                return now;
            }
            now = now.next;
        }
        throw new RuntimeException("wrong use of get(Node<L> node) method!");
    }

    public int size(){
        int size = 0;
        Node<L> now = head;
        if(head.next == end){
            return 0;
        }
        while (now.next.type != Node.TYPE_END){
            size++;
            now = now.next;
        }
        return size;
    }
    private Node<L> removeTheEnd(){
        if(size() < 1){
            return null;
        }
        Node<L> end = this.end.previous;
        Node<L> previous = end.previous;

        previous.next = this.end;
        this.end.previous = previous;

        end.next = null;
        end.previous = null;
        return end;
    }

    private boolean remove(Node<L> node){
        if(node == null){
            return false;
        }
        Node<L> previous = node.previous;
        Node<L> next = node.next;

        previous.next = next;
        next.previous = previous;

        node.clear();
        return true;
    }

    public L removeLast(){
        Node<L> go = removeTheEnd();
        if(go == null){
            return null;
        }
        L ret = go.child;
        go.recycle();
        return ret;
    }

    public void put(L child){
        Node<L> node = new Node<L>(child);
        if(head.next == end){
            head.next = node;
            node.previous = head;
            node.next = end;
            end.previous = node;
            return;
        }
        if(in(node, false)){
            Node<L> toRemove = get(node);
            remove(toRemove);
        }
        if(size() >= CAPACITY){
            removeLast();
        }
        Node<L> first = head.next;
        head.next = node;
        node.next = first;
        node.previous = head;
        first.previous = node;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stack: ");
        sb.append(this.getClass().getName());
        String a;
        Node<L> now = head;
        int i = 0;
        while (now.next != end){
            if(i % 10 == 0){
                sb.append("\n");
            }
            now = now.next;
            a = "node" + i + ": " + (now.child == null ? "" : now.child.toString()) + "; ";
            i++;
            sb.append(a);
        }
        return sb.toString();
    }

    private static final class Node<L>{
        private Node<L> next = null;
        private Node<L> previous = null;
        private L child;

        public static final int TYPE = 1;

        static final int TYPE_HEAD = 2;
        static final int TYPE_END = 3;
        static final int TYPE_NORMAL = 4;

        private int type = 0;
        Node(L child){
            this.child = child;
            type = TYPE_NORMAL;
        }
        Node(int type){
            this.type = type;
        }
        public boolean isHead(){
            return type == TYPE_HEAD;
        }
        public boolean isEND(){
            return type == TYPE_END;
        }
        public boolean isNormal() {
            return type == TYPE_NORMAL;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj != null) &&
                    (obj instanceof Node) &&
                    type == ((Node) obj).type &&
                    child.equals(((Node) obj).child);
        }

        public boolean strictEquals(Object obj){
            return (obj != null) &&
                    (obj instanceof Node) &&
                    type == ((Node) obj).type &&
                    child.equals(((Node) obj).child) &&
                    next == ((Node) obj).next &&
                    previous == ((Node) obj).previous;

        }
        public void recycle(){
            type = 0;
            child = null;
            previous = null;
            next = null;
        }
        public void clear(){
            child = null;
        }
    }


}
