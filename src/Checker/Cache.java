package Checker;

import Enums.NodeType;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import java.util.Arrays;

/**
 * Класс для хранения кэша.
 * Наряду с хранением данный класс реализует метод проверки того, может ли быть 
 * объединен кэш данного концепта с кэшем другого концепта.
 * @author Andrey Grigoryev
 */
public class Cache {
    
    private int MaxSize = 32;
    private int[] a = new int[MaxSize];
    private int size = 0;
    
    public int[] forAll = null;
    public int[] exists = null;
    public boolean[] mincar = null;
    
    public boolean hasIndivid = false;

    /**
     * Основной и единственный конструктор класса.
     */
    public Cache(int size) {
        forAll = new int[size];
        exists = new int[size];
        mincar = new boolean[size];
    }
    
    /**
     * Увеличивает размер базы кэша.
     */
    private void increase() {
        int[] old_a = a;
        MaxSize = MaxSize * 2;
        a = new int[MaxSize];
        for(int i = 0; i < size; i++)
            a[i] = old_a[i];
        old_a = null;
    }
    
    /**
     * Возвращает размер кэша.
     * @return Количество элементов кэша.
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Метод позволяет установить размер кэша.
     * Если размер кэша устанавливается равным -1, то концепт является не согласованным.
     * @param new_size Определяет новый размер кэша концепта.
     */
    public void setSize(int new_size) {
        size = new_size;
    }
    
    /**
     * Возвращает базу кэша.
     * @return Массив элементов кэша.
     */
    public int[] getCache() {
        return a;
    }
    
    /**
     * Метод очищает кэш, устанавливая его размер равным 0.
     */
    public void clear() {
        size = 0;
    }
    
    /**
     * Метод осуществляет разбор текущего добавлеяемого концепта.
     * @param x Номер концепта в лесу ограничений.
     * @param r_box RBox рассматриваемой базы знаний.
     * @param t TBox рамматриваемой базы знаний.
     */
    private void check(int x, RBox r_box, TBox t) {

        int rt = t.getRuleGraph().getNode(Math.abs(x)).getRoleType();
        
        if(t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntSOME && x > 0 ||
           t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntALL && x < 0) {
            
            if(exists[rt] != 0)
                exists[rt] = -1; else
                exists[rt] = t.getRuleGraph().getNode(Math.abs(x)).getChildren()[0];
        }

        if(t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntMINCARD && x > 0 ||
           t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntMAXCARD && x < 0) {

            if(exists[rt] != 0)
                exists[rt] = -1; else
                exists[rt] = t.getRuleGraph().getNode(Math.abs(x)).getChildren()[0];
        }

        if(t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntALL && x > 0 ||
           t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntSOME && x < 0) {
            if(forAll[rt] != 0)
                forAll[rt] = -1; else
                forAll[rt] = t.getRuleGraph().getNode(Math.abs(x)).getChildren()[0];
        }

        if(t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntMAXCARD && x > 0 ||
           t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntMINCARD && x < 0) {
            mincar[rt] = true;
        }
        
    }
    
    /**
     * Добавляет новый элемент в кэш концепта.
     * @param x Элемент кэша.
     */
    public void add(int x, RBox r_box, TBox t) {
        if(size >= MaxSize - 1) {
            increase();
        }

        hasIndivid |= (t.getRuleGraph().getNode(Math.abs(x)).getNodeType() == NodeType.ntINDIVID);
        if(size == 0) {
            a[size++] = x;
        } else {
            if(a[0] > x) {
                for(int i = size; i >= 1; i--)
                    a[i] = a[i - 1];
                a[0] = x;
                size++;
                check(x, r_box, t);
            } else {
                for(int i = 0; i < size - 1; i++) {
                    if(a[i] == x || a[i + 1] == x) return;
                    if(a[i] < x && x < a[i + 1]) {
                        for(int j = size; j >= i + 1; j--) {
                            a[j] = a[j - 1];
                        }
                        a[i + 1] = x;
                        size++;
                        check(x, r_box, t);
                        return;
                    }
                }
                a[size++] = x;
                check(x, r_box, t);
            }
        }
    }
    
    public boolean contains(int x) {
        int l = 0; int r = size;
        while(r - l > 1) {
            int m = (r + l) / 2;
            if(a[m] > x) r = m; else l = m;
        }
        if(a[l] == x) return true;
        return false;
    }
    
}