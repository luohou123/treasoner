package Help;

import java.util.Arrays;

/**
 * Класс для представления массива целых чисел и некоторых методов работы с ними.
 * @author Andrey Grigoryev
 */
public class IntArray {
    
    private int MaxSize = 16;
    private int[] a = new int[MaxSize];
    private int size = 0;
    
    /**
     * Увеличивает размер массива, если он оказывается заполненным.
     */
    private void IncreaseSize() {
        int[] old = a;
        MaxSize = MaxSize * 2;
        a = new int[MaxSize];
        
        for(int i = 0; i < size; i++)
            a[i] = old[i];
        
        old = null;
    }
    
    /**
     * Основной и единственный конструктор класса.
     */
    public IntArray() { }
    
    /**
     * Метод осуществляет сортировку элементов массива.
     */
    public void sort() {
        Arrays.sort(a, 0, size);
    }
    
    /**
     * Метод осуществляет перестановку элементов массива в обратном порядке.
     */
    public void reverse() {
        for(int i = 0; i < size / 2; i++) {
            int h = a[i];
            a[i] = a[size - i - 1];
            a[size - i - 1] = h;
        }
    }
    
    /**
     * Метод удаляет из массива все элементы значение которых равно заданному параметру.
     * @param x Значение элемента массива, которые необходимо удалить.
     */
    public void delete(int x) {
        for(int i = 0; i < size; i++) {
            if(a[i] == x) {
                for(int j = i; j < size - 1; j++)
                    a[j] = a[j + 1];

                size--; i--;
                //return;
            }
        }
    }
    
    /**
     * Метод осуществляет проверку наличия элемента в массиве
     * @param x Элемент поиск которого осуществляется.
     * @return Возвращает истина, если заданный элемент был найден и ложь в противном случае.
     */
    public boolean contain(int x) {
        for(int i = 0; i < size; i++)
            if(a[i] == x) return true;
        return false;
    }
            
    /**
     * Метод осуществляет проверку наличия всех элементов заданного массива в текущем.
     * @param x Заданный массив элементов.
     * @return Возвращает истина, если заданный массив элементов был найден и ложь в противном случае.
     */
    public boolean contain(IntArray x) {
        int ind1 = 0;
        int ind2 = 0;
        while(ind1 < size && ind2 < x.size()) {
            if(a[ind1] == x.get(ind2)) {
                ind1++;
                ind2++;
            } else {
                ind1++;
            }
            if(a[ind1] > x.get(ind2) && ind1 < size && ind2 < x.size()) return false;
            
        }
        if(ind2 < x.size()) return false;
        return true;
    }
    
    /**
     * Метод добавляет элемент в массив.
     * @param x Добавляемый элемент.
     */
    public void add(int x) {
        if(size == MaxSize - 1) IncreaseSize();
        a[size++] = x;
    }
    
    /**
     * Метод добавляет элемент в отсортированный массив с помощью бинарного поиска.
     * @param x Добавляемый элемент.
     */
    public void addSorted(int x) {
        if(size == MaxSize - 1) IncreaseSize();
        int l = 0, r = size;
        while(r - l > 1) {
            int m = (r + l) / 2;
            if(a[m] > x) r = m; else l = m;
        }
        for(int i = size; i > l; i--) {
            a[i] = a[i - 1];
        }
        a[l] = x;
        size++;
    }

    /**
     * Метод осуществляет добавление элемента в массив, если его там ещё не существует.
     * @param x Добавляемый элемент.
     */
    public void addOnce(int x) { //specail for taxonomy builder
        if(contain(x)) return;
        add(x);
    }
    
    /**
     * Метод осуществляет добавление множества элементов в массив.
     * @param x Множество добавляемых элементов.
     */
    public void add(IntArray x) {
        for(int i = 0; i < x.size(); i++) {
            add(x.get(i));
        }
    }
    
    /**
     * Метод осуществляет добавление множества элементов в массив, если их там ещё не существует.
     * @param x Множество добавляемых элементов.
     */
    public void addOnce(IntArray x) {
        for(int i = 0; i < x.size(); i++) {
            addOnce(x.get(i));
        }
    }
    
    /**
     * Возвращает элемент массива по его номеру.
     * @param x Номер элемента в массиве.
     * @return Значение элемент массива.
     */
    public int get(int x) {
        return a[x];
    }
    
    /**
     * Возвращает размер массива.
     * @return Размер массива.
     */
    public int size() {
        return size;
    }
    
    /**
     * Метод очищает массив.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Возвращает значение последнего элемента и удалает его из массива.
     * @return Значение последнего элемента массива.
     */
    public int pop() {
        size--;
        return a[size];
    }
    
}
