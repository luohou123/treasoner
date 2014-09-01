package Help;

/**
 * Класс для хранения зависимостей данного правила.
 * В классе все зависимости хранятся в виде массива упорядоченного по возрастанию.
 * @author Andrey Grigoryev
 */
public class DSet {

    int MaxSize = 4;
    private int[] ar = new int[MaxSize];
    int Size = 0;
    
    /**
     * Основной конструктор класса.
     */
    public DSet() { }
    
    /**
     * Метод устанавливает размер массива зависимостей равный 0.
     */
    public void clear() {
        Size = 0;
    }
    
    /**
     * Удаляет последний элемент массива зависимостей.
     */
    public void pop() {
        Size--;
    }
    
    /**
     * Возвращает количество элементов массива зависимостей.
     * @return Количество элементов массива.
     */
    public int size() {
        return Size;
    }
    
    /**
     * Метод увеличивает размер массива зависимостей в 1,5 раза при его полном заполнении.
     */
    public void increase() {
        int[] old = ar;
        MaxSize = (MaxSize) * 2 + 1;
        
        ar = new int[MaxSize];
        for(int i = 0; i < Size; i++)
            ar[i] = old[i];
    }
    
    /**
     * Метод осуществляет добавление элемента в массив зависимостей.
     * @param x Новый элемент массива.
     */
    public void add(int x) {
        if(Size == MaxSize - 1) {
            increase();
        }
        ar[Size] = x;
        Size++;
    }
            
    /**
     * Конструктор класса, который создает новый объект на основе другого объекта 
     * данного класса, копируя все элементы массива зависимостей.
     * @param d Объект, на основе которого создается данный объект.
     */
    public DSet(DSet d) {
        if(d == null) return;
        if(d.size() > Size)
        {
            MaxSize = d.size();
            ar = new int[MaxSize];
        }
        for(int i = 0; i < d.getValues().length; i++)
            add(d.getValues()[i]);
    }
    
    /**
     * Добавляет элемент в массив зависимостей, сохраняя его упорядоченность.
     * @param x Новый элемент массива зависимостей.
     */
    public void addValue(int x) {
        if(Size == MaxSize - 1) {
            increase();
        }
        
        if(ar[0] == x) return;
        if(ar[0] > x) {
            for(int j = Size; j > 0; j--) {
                ar[j] = ar[j - 1];
            }
            ar[0] = x;
            Size++;
            return;
        }
        
        for(int i = 0; i < Size - 1; i++)
            if(ar[i] <= x && x <= ar[i + 1]) {
                if(ar[i] == x || ar[i + 1] == x) return; //not add x if this is already exists
                for(int j = Size; j > i + 1; j--) {
                    ar[j] = ar[j - 1];
                }
                ar[i + 1] = x;
                Size++;
                return;
            }
        ar[Size] = x;
        Size++;
    }
    
    /**
     * Возвращает все элементы массива зависимостей в виде массива чисел.
     * @return Массив всех элементов зависимостей.
     */
    public int[] getValues() {
        int[] b = new int[Size];
        for(int i = 0; i < Size; i++)
            b[i] = ar[i];
        return b;
    }
    
    /**
     * Объединяет два массива зависимостей, сохраняя упорядоченность элементов.
     * @param d Добавляемый массив зависимостей.
     */
    public void mergeWith(DSet d) {
        if(d == null) return;
        int[] a1 = new int[Size];
        for(int i = 0; i < Size; i++)
            a1[i] = ar[i];

        int[] a2 = d.getValues();
        int[] a3 = new int[a1.length + a2.length];
        
        int it1 = 0;
        int it2 = 0;
        int it3 = 0;
        
        while(it1 < a1.length && it2 < a2.length) {
            if(a1[it1] < a2[it2]) {
                a3[it3] = a1[it1];
                it1++; it3++;
            } else
            if(a1[it1] > a2[it2]) {
                a3[it3] = a2[it2];
                it2++; it3++;
            } else {
                a3[it3] = a1[it1];
                it1++; it2++; it3++;
            }
        }

        for(; it1 < a1.length; it1++) {
            a3[it3++] = a1[it1];
        }
        
        for(; it2 < a2.length; it2++) {
            a3[it3++] = a2[it2];
        }

        Size = 0;
        for(int i = 0; i < it3; i++) {
            if(i > 0) if(a3[i] == a3[i - 1]) continue;
            if(Size == MaxSize - 1) {
                increase();
            }
            ar[Size] = a3[i];
            Size++;
        }
    }
}
