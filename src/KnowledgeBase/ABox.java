package KnowledgeBase;

import Help.DSet;
import Checker.Model.InterpretationNode;

/**
 * Класс отвечает за хранение аксиом ABox предметной области.
 * @author Andrey Grigoryev
 */
public class ABox {

    private int MaxCount = 8;
    private int names_count = 0;
    private InterpretationNode[] nodes = new InterpretationNode[MaxCount];
    
    /**
     * Основной и единственный конструктор класса.
     * Устанавливает текущее количество индивидов равное 0. Создает списки смежности
     * для каждой вершины.
     */
    public ABox() {
        names_count = 0;
    }
    
    /**
     * Метод очищает все поля объектов InterpretationNode отвечающих за всех индивидов
     */
    public void clearNodes() {
        for(int i = 0; i < MaxCount; i++) {
            if(nodes[i] == null) nodes[i] = new InterpretationNode();
            nodes[i].indexInQueue = -1;
        }
    }
    
    /**
     * Метод предоставляет доступ к полю, отвечающему за общее количество индивидов
     * предметной области.
     * @return Общее количество индивидов.
     */
    public int getCount() {
        return names_count;
    }
    
    public void addIndivid() {
        if(names_count >= MaxCount - 1) {
            MaxCount = MaxCount * 3 / 2;
            
            InterpretationNode[] temp = new InterpretationNode[names_count];
            System.arraycopy(nodes, 0, temp, 0, names_count);
            
            nodes = new InterpretationNode[MaxCount];
            System.arraycopy(temp, 0, nodes, 0, names_count);
            
            temp = null;
        }
        nodes[names_count] = new InterpretationNode();
        nodes[names_count].indexInQueue = 0;
        names_count++;
    }
    
    /**
     * Метод осуществляет добавление правила леса ограничений к индивиду ABox, представленному объектом класса OWLIndividual.
     * Данный метод был создан для добавления аксиомы принадлежности ABox.
     * @param individ Индивид интерпретации.
     * @param toDoEntry Идентификатор вершины леса ограничений.
     */
    public void add(int individ, int toDoEntry) {
        nodes[individ].addToDo(toDoEntry, new DSet(), 0, null);
    }

    /**
     * Метод добавляет аксиому различных индвивидов.
     * При чтении базы знаний данный метод вызывается таким образом, что toDoEntry1 
     * является ссылкой на вершину леса ограничений, которая представляет индивида1,
     * соответственно toDoEntry2 представляет собой индивида2.
     * @param individ1 Первый индивид аксиомы.
     * @param individ2 Второй индивид аксиомы.
     * @param toDoEntry1 Номер вершины леса ограничений, представляющей певрого индивида.
     * @param toDoEntry2 Номер вершины леса ограничений, представляющей второго индивида.
     */
    public void addDiff(int individ1, int individ2, int toDoEntry1, int toDoEntry2) {
        nodes[individ1].addToDo(-toDoEntry2, new DSet(), 0, null);
        nodes[individ2].addToDo(-toDoEntry1, new DSet(), 0, null);
    }
    
    public InterpretationNode getNode(int individ_num) {
        return nodes[individ_num];
    }
    
}