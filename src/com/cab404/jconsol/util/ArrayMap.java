package com.cab404.jconsol.util;

import java.util.*;

/**
 * Simple array of map entries
 *
 * @author cab404
 */
public class ArrayMap<K, V> {
    List<Map.Entry<K, V>> data = new ArrayList<>();

    public void add(K key, V value) {
        AbstractMap.SimpleEntry<K, V> entry = new AbstractMap.SimpleEntry<>(key, value);
        data.add(entry);
    }

    public Iterable<V> getValues(final K key) {
        return new Iterable<V>() {
            @Override public Iterator<V> iterator() {
                return new Iterator<V>() {
                    int index;

                    @Override public boolean hasNext() {
                        for (int i = index; i < data.size(); i++)
                            if (data.get(i).getKey().equals(key))
                                return true;
                        return false;
                    }

                    @Override public V next() {
                        try {
                            while (!data.get(index).getKey().equals(key)) index++;
                            return data.get(index++).getValue();
                        } catch (IndexOutOfBoundsException e) {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override public void remove() {
                        data.remove(index);
                    }
                };
            }
        };
    }

    public Iterable<V> values() {
        return new Iterable<V>() {
            @Override public Iterator<V> iterator() {
                return new Iterator<V>() {
                    int index = -1;
                    @Override public boolean hasNext() { return index < data.size() - 1;}
                    @Override public V next() { return data.get(++index).getValue();}
                    @Override public void remove() { data.remove(index);}
                };
            }
        };
    }

    @Override public String toString() {
        return data.toString();
    }
}
