/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Containers;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import lt.lb.commons.Log;

/**
 *
 * @author Lemmin
 * @param <T>
 */
public class PrefillArrayMap<T> implements List<T>, Collection<T>, RandomAccess {

    private ArrayList<T> list = new ArrayList<>();
    private int size = 0;

    private Predicate<T> nullCheck;
    private T nullValue;

    public PrefillArrayMap(Predicate<T> nullCheck, T fillValue) {
        this.nullCheck = nullCheck;
        this.nullValue = fillValue;
    }

    public PrefillArrayMap(T fillValue) {
        this((T t) -> Objects.equals(t, fillValue), fillValue);
    }

    public PrefillArrayMap() {
        this((T t) -> t == null, null);
    }
    
    public boolean isNull(int index){
        return this.nullCheck.test(this.get(index));
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int size = this.size();
        T[] copyOf = Arrays.copyOf(a, size);
        for (int i = 0; i < size; i++) {
            copyOf[i] = (T) this.get(i);
        }
        return copyOf;
    }

    @Override
    public boolean add(T e) {
        this.size++;
        return list.add(e);
    }

    @Override
    public boolean remove(Object o) {
        int indexOf = this.indexOf(o);
        if (indexOf >= 0) {
            this.remove(indexOf);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return this.list.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        this.list.clear();
        size= 0;
    }

    public void preFill(int toIndex) {
        int curSize = list.size();
        for(int i = curSize; i <= toIndex; i++){
            this.list.add(nullValue);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new SkippingListIterator(0, this.nullCheck, this);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> clctn) {
        this.size+= clctn.size();
        return this.list.addAll(i, clctn);
    }

    @Override
    public T get(int i) {
//        if(i >= list.size()){
//            return this.nullValue;
//        }
        this.preFill(i);
        return list.get(i);
    }

    @Override
    public T set(int i, T e) {
        this.preFill(i);
        if(this.isNull(i)){
            this.size++;
        }
        return list.set(i, e);
    }

    @Override
    public void add(int i, T e) {
        this.preFill(i - 1);
        this.size++;
        list.add(i, e);
    }

    @Override
    public T remove(int i) {
        if(!this.isNull(i)){
            this.size--;
        }
        return this.set(i, nullValue);
    }

    @Override
    public int indexOf(Object o) {
        ListIterator<T> it = this.listIterator();
        while (it.hasNext()) {
            T next = it.next();
            if (Objects.equals(next, o)) {
                return it.previousIndex();
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        ListIterator<T> it = this.listIterator();
        int lastIndex = -1;
        while (it.hasNext()) {
            T next = it.next();
            if (Objects.equals(next, o)) {
                lastIndex = it.previousIndex();
            }
        }
        return lastIndex;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new SkippingListIterator(0, this.nullCheck, this);
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return new SkippingListIterator(i, this.nullCheck, this);
    }

    @Override
    public List<T> subList(int i, int i1) {
       throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    
    public boolean containsKey(Object key) {
        if(key instanceof Integer){
            return !this.isNull((Integer)key);
        }else{
            return false;
        }
    }


    public T put(Integer key, T value) {
        return this.set(key, value);
    }

    public void putAll(Map<? extends Integer, ? extends T> m) {
        for(Map.Entry<? extends Integer, ? extends T> entry:m.entrySet()){
            this.set(entry.getKey(), entry.getValue());
        }
    }

    public Set<Integer> keySet() {
        Set<Integer> set = new HashSet<>();
        ListIterator<T> iter = this.listIterator();
        while(iter.hasNext()){
            set.add(iter.nextIndex());
            iter.next();
        }
        return set;
    }

    public Collection<T> values() {
        Set<T> set = new HashSet<>();
        ListIterator<T> iter = this.listIterator();
        while(iter.hasNext()){
            set.add(iter.next());
        }
        return set;
    }

    public Set<Entry<Integer, T>> entrySet() {
        Set<Entry<Integer, T>> set = new HashSet<>();
        for(int i =0; i < list.size(); i++){
            
            if(!this.isNull(i)){
                final Integer key = i;
                final T value = this.get(key);
                Entry<Integer,T> en = new Entry() {
                    @Override
                    public Object getKey() {
                        return key;
                    }

                    @Override
                    public Object getValue() {
                        return value;
                    }

                    @Override
                    public Object setValue(Object val) {
                        throw new UnsupportedOperationException();
                    }
                    
                    @Override
                    public String toString(){
                        return this.getKey() +" = "+this.getValue();
                    }
                };
                set.add(en);
            }
            
        }
        return set;
    }

}