package Checker;

import Checker.Model.InterpretationNode;
import Help.IntArray;
import KnowledgeBase.RuleGraph.RuleGraph;

/**
 * Класс хранит свойства и реализует алгоритмы глобального кэширования.
 * Каждый объект класса содержит массив объектов {@link  IntArray}, каждый из которых
 * содержит все элементы toDoList индивида.
 * @author Andrey Grigoryev
 */
class TCache {
    
    private int MaxSize = 1 << 13;
    private IntArray[] cac = new IntArray[MaxSize];
    private int size = 0;
    private RuleGraph tree = null;

    IntArray temp = new IntArray();
    
    /**
     * Основной и единственный конструктор данного класса. 
     * Устаналивает ссылку на объект леса ограничений.
     * @param t Объект леса ограничений текущей базы знаний.
     */
    public TCache(RuleGraph t) {
        tree = t;
        for(int i = 0; i < MaxSize; i++)
            cac[i] = new IntArray();
        size = 0;
    }
    
    /**
     * Метод реализует доступ для записи поля, соответствующего лесу ограничений.
     * @param t Объект леса ограничений.
     */
    public void setRuleGraph(RuleGraph t) {
        tree = t;
    }
    
    /**
     * Метод позволяет объединить кэш текущего объекта с кэшем другого объекта.
     * @param cache Объект для объединения.
     */
    public void merge(TCache cache) {
        for(int i = 0; i < cache.getSize(); i++) {
            if(size == MaxSize) return;
            cac[size++] = cache.getCache(i);
        }
    }
    
    /**
     * Возвращает кэш элемента
     * @param x
     * @return 
     */
    public IntArray getCache(int x) {
        return cac[x];
    }
    
    /**
     * Возвращает размер кэша.
     * @return Размер кэша.
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Очищает кэш и устанавливает размер равный 0.
     */
    public void clear() {
        temp.clear();
        for(int i = 0; i < size; i++)
            cac[i].clear();
        size = 0;
    }
    
    /**
     * Реализует метод добавления вершины в кэш хранящий согласованные концепты.
     * @param node Элемент интерпретации.
     */
    public void add(InterpretationNode node)
    {
        if(size == MaxSize) return;
        if(!find(node)) {
            for(int i = 0; i < temp.size(); i++) {
                cac[size].add(temp.get(i));
            }
            size++;
        }
    }
    
    /**
     * Реализует метод добавления вершины в кэш хранящий несогласованные концепты.
     * @param node Элемент интерпретации.
     */
    public boolean neg_add(InterpretationNode node)
    {
        if(size == MaxSize) return false;
        if(!neg_find(node)) {
            for(int i = 0; i < temp.size(); i++) {
                cac[size].add(temp.get(i));
            }
            size++;
            return true;
        }
        return false;
    }
    
    /**
     * Реализует алгоритм поиска индивида в кэше хранящем несогласованные концепты.
     * @param node Индивид, поиск которого осуществляется в методе
     * @return Возвращает истина, если такой индивид содержится в кэше и ложь в противном случае.
     */
    public boolean neg_find(InterpretationNode node)
    {
        temp.clear();
        for(int i = 0; i < node.getToDoSize(); i++) {
            //int v = tree.getNode(Math.abs(node.getToDo()[i])).getCacheClass();
            int v = node.getToDo()[i];
            if(node.getToDo()[i] < 0) v = -v;
            //if(v == 0) v = node.getToDo()[i];
            temp.addSorted(v);
        }
        //temp.sort();
        
        for(int i = 0; i < size; i++) {
            if(temp.contain(cac[i])) return true;
        }
        return false;
    }
 
    /**
     * Реализует алгоритм поиска индивида в кэше хранящем согласованные концепты.
     * @param node Индивид, поиск которого осуществляется в методе
     * @return Возвращает истина, если такой индивид содержится в кэше и ложь в противном случае.
     */
    public boolean find(InterpretationNode node) {
        temp.clear();
        for(int i = 0; i < node.getToDoSize(); i++) {
            int v = tree.getNode(Math.abs(node.getToDo()[i])).getCacheClass();
            if(node.getToDo()[i] < 0) v = -v;
            if(v == 0) v = node.getToDo()[i];
            temp.add(v);
        }
        temp.sort();
        for(int i = 0; i < size; i++) {
            if(cac[i].contain(temp)) return true;
        }
        return false;
    }
}
