package Help;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Класс определяет методы и поля для хранения хеша вершин леса ограничений.
 * Данный класс был разработан для того, чтобы осуществлять быстрое преобразование
 * древовидной структуры леса ограничений в графовидную структуру.
 * @author Andrey Grigoryev
 */
public class SHash {
    
    /**
     * Класс определяет поля и конструктор для элемента хеша.
     */
    private class HashE {
        public HashE next;
        public int hf1, hf2, n, hf3, hf4;
        /**
         * Основной конструктор класса 
         * @param _hf1 Значение первой хеш-функции.
         * @param _hf2 Значение второй хеш-функции.
         * @param _hf3 Значение третьей хеш-функции.
         * @param _hf4 Значение четвертой хеш-функции.
         * @param _n Номер вершины, которому соответствует заданная четверка хеш-функций.
         */
        public HashE(int _hf1, int _hf2, int _hf3, int _hf4, int _n) {
            hf1 = _hf1;
            hf2 = _hf2;
            hf3 = _hf3;
            hf4 = _hf4;
            n = _n;
            next = null;
        }
    }
    
    /**
     * Класс определяет объект для хранения пары целых чисел.
     */
    private class Pair {
        /**
         * Первый элемент пары.
         */
        public int x;
        /**
         * Второй элемент пары.
         */
        public int y;
        /**
         * Конструктор, который присваивает значения элементов пары, заданным значениям.
         * @param _x Первый элемент пары.
         * @param _y Второй элемент пары. 
         */
        public Pair(int _x, int _y) {
            x = _x; y = _y;
        }
    }
    
    /**
     * Класс для сравнения объектов пары.
     */
    private class PairComparer implements Comparator<Pair> {
        /**
         * Определяет метод сравнения двух объектов {@link Pair}
         * @param o1 Первая пара.
         * @param o2 Вторая пара.
         * @return Результат сравнения пары.
         */
        @Override
        public int compare(Pair o1, Pair o2) {
            if(o1.x == o2.x) {
                if(o1.y < o2.y) return -1;
                if(o1.y > o2.y) return 1;
                return 0;
            }
            if (o1.x < o2.x) {
                return -1;
            }
            if (o1.x > o2.x) {
                return 1;
            }
            return 0;
        }
        
    }
    
    final int hC1 = 107;
    final int hC2 = 1051;
    final int hSize = (1 << 12);
    private HashE[] table = new HashE[hSize];
    final int hModul = (1 << 21);
    
    /**
     * Основной и единственный конструктор класса.
     */
    public SHash() { }
    
    /**
     * Метод возращает 1 если входной параметр отрицательный, 2 если положительный и
     * 0 если равен 0.
     * @param x Исходное число
     * @return 0, 1 или 2 в зависимости от входного параметра
     */
    static public int Sign(int x) {
        if(x < 0) return 1;
        if(x > 0) return 2;
        return 0;
    }

    /**
     * Преобразует правило AND в OR и наоборот и EXISTS в FORALL и наоборот.
     * Входные и выходные данные задаются в виде чисел.
     * @param x Числовое значение операци в вершине.
     * @return Результат преобразования вершины по правилам Де-Моргана в числовом виде.
     */
    static private int Inv(int x) {
        if(x == 0) return 2;
        if(x == 2) return 0;
        if(x == 3) return 4;
        if(x == 4) return 3;
        return 1;
    }
    
    /**
     * Добавляет элемент в хеш.
     * @param al Массив номер предков данной вершины.
     * @param n Номер вершины в лесу ограничений.
     * @param type Тип вершины (1 - AND, 2 - OR, 3 - EXISTS, 4 - FORALL)
     * @param role Тип роли связывающий вершину и её потомка.
     * @return Номер элемента найденного в хеше или 0 если такого элемента не существует.
     */
    public int add(int[] al, int alsize, int n, int type, int role) {
        int pow1 = 1, pow2 = 1;
        int hf1 = 0, hf2 = 0, hf3 = 0, hf4 = 0;
        Pair[] p = new Pair[alsize];
        for(int i = 0; i < alsize; i++) {
            p[i] = new Pair(Math.abs(al[i]), Sign(al[i]));
        }
        Arrays.sort(p, new PairComparer());
        
        hf1 = (hf1 + pow1 * type) % hModul;
        pow1 = (pow1 * hC1) % hModul;
        
        hf1 = (hf1 + pow1 * role) % hModul;
        pow1 = (pow1 * hC1) % hModul;
        
        pow1 = 1;
        hf3 = (hf3 + pow1 * role) % hModul;
        hf4 = (hf4 + pow1 * role) % hModul;
        
        pow1 = (pow1 * hC1) % hModul;
        hf3 = (hf3 + pow1 * type) % hModul;
        hf4 = (hf4 + pow1 * Inv(type)) % hModul;
        
        for(int i = 0; i < alsize; i++) {
            hf1 = (hf1 + pow1 * p[i].x) % hModul;
            hf2 = (hf2 + pow2 * p[i].x) % hModul;
            
            int inv = p[i].y;
            if(inv == 1) inv = 2; else
                if(inv == 2) inv = 1;
            
            hf3 = (hf3 + pow1 * p[i].y) % hModul;
            hf4 = (hf4 + pow1 * inv) % hModul;
            
            pow1 = (pow1 * hC1) % hModul;
            pow2 = (pow2 * hC2) % hModul;
        }
        
        hf1 %= hSize;
        while(hf1 < 0) hf1 += hSize;
        HashE element = new HashE(hf1, hf2, hf3, hf4, n);
        if(table[hf1] == null) {
            table[hf1] = element;
        } else {
            for(HashE el = table[hf1]; el != null; el = el.next) {
                if(el.hf1 == hf1 && el.hf2 == hf2) {
                    if(el.hf3 == hf3) return el.n;
                    if(el.hf3 == hf4) return -el.n;
                }
            }

            element.next = table[hf1];
            table[hf1] = element;
        }
        return 0;
    }
    
    /**
     * Метод производит поиск и добавляет элемент в хеш.
     * @param al Массив номер предков данной вершины.
     * @param need_to_add Определяет то, нужно ли добавлять элемент в хеш или простого его найти.
     * @return Номер элемента найденного в хеше или 0 если такого элемента не существует.
     */
    public int add(int[] al, boolean need_to_add) {
        int pow1 = 1, pow2 = 1;
        int hf1 = 0, hf2 = 0, hf3 = 0, hf4 = 0;
        Pair[] p = new Pair[al.length];
        for(int i = 0; i < al.length; i++) {
            p[i] = new Pair(Math.abs(al[i]), Sign(al[i]));
        }
        Arrays.sort(p, new PairComparer());
        
        pow1 = 1;        
        for(int i = 0; i < al.length; i++) {
            hf1 = (hf1 + pow1 * p[i].x) % hModul;
            hf2 = (hf2 + pow2 * p[i].x) % hModul;
            
            int inv = p[i].y;
            if(inv == 1) inv = 2; else
                if(inv == 2) inv = 1;
            
            hf3 = (hf3 + pow1 * p[i].y) % hModul;
            hf4 = (hf4 + pow1 * inv) % hModul;
            
            pow1 = (pow1 * hC1) % hModul;
            pow2 = (pow2 * hC2) % hModul;
        }
        
        hf1 %= hSize;
        while(hf1 < 0) hf1 += hSize;
        HashE element = new HashE(hf1, hf2, hf3, hf4, 107);
        if(table[hf1] == null) {
            if(need_to_add)
                table[hf1] = element;
        } else {
            for(HashE el = table[hf1]; el != null; el = el.next) {
                if(el.hf1 == hf1 && el.hf2 == hf2) {
                    if(el.hf3 == hf3) return el.n;
                    if(el.hf3 == hf4) return -el.n;
                }
            }

            if(need_to_add) {
                element.next = table[hf1];
                table[hf1] = element;
            }
        }
        return 0;
    }
    
    /**
     * Возвращает количество элементов в хеше.
     * @return Количество элементов в хеше.
     */
    public int countOfElements() {
        int ret = 0;
        for(int i = 0; i < hSize; i++) {
            for(HashE el = table[i]; el != null; el = el.next)
                ret++;
        }
        return ret;
    }
    
}
