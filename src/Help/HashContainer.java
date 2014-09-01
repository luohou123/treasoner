package Help;

import java.util.HashSet;

/**
 * Класс создан для обеспечения возможности создания массива элементов HashSet.
 * @author Andrey Grigoryev
 */
public class HashContainer {
    
    private HashSet<Integer> base = new HashSet<Integer>();
    
    public HashContainer() { }

    /**
     * Метод проверяет содержит ли множество заданный элемент.
     * @param x Элемент множества.
     * @return Возвращает истина, если заданный элемент содержится в множестве и ложь в противном случае.
     */
    public boolean contain(int x) {
        return base.contains(x);
    }
    
    /**
     * Метод добавляет элемент в множество.
     * @param x Элемент, добавляемый в множество.
     */
    public void add(int x) {
        base.add(x);
    }
    
    /**
     * Метод добавляет множество элементов в текущее множество.
     * @param hsi Множество элементов для добавления.
     */
    public void add(HashSet<Integer> hsi) {
        base.addAll(hsi);
    }
    
    /**
     * Метод очищает множество концептов.
     */
    public void clear() {
        base.clear();
    }
}
