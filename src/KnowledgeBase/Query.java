/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KnowledgeBase;

import Help.IntPair;

/**
 * Класс определяет поля и методы для хранения типов всех индивидов.
 * Класс был разработан для осуществления запроса в соответствии со спецификацией
 * DL98 Workshop.
 * @author Andrey Grigoryev
 */
public class Query {
    
    private final int MaxSize = 128;
    private int ii_size = 0;
    private IntPair[] ii = new IntPair[MaxSize];
    
    /**
     * Основной конструктор класса создает массив объектов представляющих собой пары (индивид, концепт)
     */
    public Query() {
        for(int i = 0; i < MaxSize; i++) {
            ii[i] = new IntPair(0, 0);
        }
    }
    
    /**
     * Метод осуществляет добавление номера концепта, которому соответствует индивид.
     * @param individId Идентификационный номер индивида.
     * @param conceptId Идентификационный номер концепта.
     */
    public void addIndividualInstance(int individId, int conceptId) {
        ii[ii_size].x = individId;
        ii[ii_size].y = conceptId;
        ii_size++;
    }
    
    /**
     * Возвращает общее количество определенных алгоритмом пар соответствия индивид/концепт.
     * Метод реализован для обеспечения доступа к решению задачи реализации.
     * @return Количество пар.
     */
    public int getIndividualInstanceSize() {
        return ii_size;
    }
    
    /**
     * Возвращает конкретную пару соотвествия индвид/концепт.
     * Метод реализован для обеспечения доступа к решению задачи реализации.
     * @param x Идентификационный номер пары.
     * @return Пара соответствия индивид/концепта.
     */
    public IntPair getIndividualInstance(int x) {
        return ii[x];
    }
    
}
