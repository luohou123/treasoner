/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Checker.Model;

import Enums.NodeType;
import Help.DSet;
import Checker.Structures.Couple;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Класс для хранения информации об индивиде интерпретации.
 * В качестве такой информации определяется toDoList для каждого из индивидов, список
 * предков и список потомков, а также дополнительные множества для правил с кванторами
 * существования, всеобщности и конструкторов кардинальности.
 * @author Andrey Grigoryev
 */
public class InterpretationNode {

    private final int StandMaxSize = 4;
    public int origToDoWhen = 0; //переменная которая нужна для того чтобы определить минимальный набор ToDo
    public boolean isDataTypeVertice = false;
    
    private int ChildMaxSize = StandMaxSize;
    private Couple[] children = new Couple[ChildMaxSize];
    private int[] whchild = new int[ChildMaxSize];
    private int ChildSize = 0;
    
    private int ParentMaxSize = StandMaxSize;
    private Couple[] parents = new Couple[ParentMaxSize];
    private int[] whparent = new int[ParentMaxSize];
    private int ParentSize = 0;
    
    int ToDoMaxSize = StandMaxSize;
    private int[] ToDo = new int[ToDoMaxSize];
    private int[] whToDo = new int[ToDoMaxSize];
    private DSet[] ToDoDSet = new DSet[ToDoMaxSize];
    private int ToDoSize = 0;
    
    int MQfdMaxSize = StandMaxSize;
    private int[] MQfd = new int[MQfdMaxSize];
    private int[] whMQfd = new int[MQfdMaxSize];
    private DSet[] MQfdDSet = new DSet[MQfdMaxSize];
    private int MQfdSize = 0;

    int SomeMaxSize = StandMaxSize;
    private int[] Some = new int[SomeMaxSize];
    private int[] whSome = new int[SomeMaxSize];
    private int SomeSize = 0;
        
    int FAllMaxSize = StandMaxSize;
    private int[] FAll = new int[FAllMaxSize];
    private int[] whFAll = new int[FAllMaxSize];
    private int FAllSize = 0;
    
    int IndsMaxSize = StandMaxSize;
    private String[] inds = new String[IndsMaxSize];
    private int[] whInds = new int[IndsMaxSize];
    private int IndsSize = 0;
    
    public boolean toDoPerform = true;
    public int currentToDo = 0;
    public int currentMQfd = 0;
    public int currentFAll = 0;
    public int indexInQueue = 0;
    public int createdBy = 0;
    
    private boolean skip = false; //skip means that this vertice merged to another
    private int block = -1;
    
    private ArrayList<InterpretationNode> toRet = new ArrayList<InterpretationNode>();
    HashSet<InterpretationNode> flag = new HashSet<InterpretationNode>();

    /**
     * Данное свойство определяет были ли обработаны все правила toDoList и необходимо ли
     * переходить к обработке правил с кванторами.
     */
    public boolean done = false;
    
    /**
     * Метод очищает все поля класса, которые были заполнены при построении интерпретации.
     * Этот метод позволяет использовать один и тот же объект для проверки различных 
     * концептов.
     */
    public void clear()
    {
        isDataTypeVertice = false;
        indexInQueue = 0;
        currentToDo = 0;
        currentFAll = 0;
        done = false;
        toDoPerform = true;
        skip = false;
        block = -1;
        ParentSize = 0;
        ChildSize = 0;
        ToDoSize = 0;
        MQfdSize = 0;
        SomeSize = 0;
        FAllSize = 0;
        currentMQfd = 0;
        IndsSize = 0;
        createdBy = 0;
        
        for(int i = 0; i < ToDoSize; i++) {
            whToDo[i] = 0;
            ToDoDSet[i].clear();
            ToDoDSet[i] = null;
        }
        
        for(int i = 0; i < MQfdSize; i++) {
            whMQfd[i] = 0;
            MQfdDSet[i] = null;
        }
        
        /*for(int i = 0; i < ChildSize; i++)
            children[i] = null;
        
        for(int i = 0; i < ParentSize; i++)
            parents[i] = null;*/
    }
    
    /**
     * Метод возвращает размер toDoList.
     * @return Размер toDoList.
     */
    public int getToDoSize() {
        return ToDoSize;
    }
    
    /**
     * Метод возвращает размер toDoList для ограничений кардинальности.
     * @return Размер toDoList для ограничений кардинальности.
     */
    public int getMQfdSize() {
        return MQfdSize;
    }
    
    /**
     * Метод возвращает размер toDoList для квантора существования.
     * @return Размер toDoList для квантора существования.
     */
    public int getSomeSize() {
        return SomeSize;
    }
    
    /**
     * Метод возвращает размер toDoList для квантора всеобщности.
     * @return Размер toDoList для квантора всеобщности.
     */
    public int getFAllSize() {
        return FAllSize;
    }

    /**
     * Метод возвращает количество потомков данного индивида.
     * @return Количество потомков данного индивида.
     */
    public int getChildSize() {
        return ChildSize;
    }
    
    /**
     * Метод возвращает количество предков данного индивида.
     * @return Количество предков данного индивида.
     */
    public int getParentSize() {
        return ParentSize;
    }

    /**
     * Метод возвращает количество индивидов, которыми является данный индивид.
     * @return Количество индивидов, которые являются эквивалентными данному.
     */
    public int getIndsSize() {
        return IndsSize;
    }
    
    /**
     * Метод осуществляет увеличивание размера toDoList данного индивида.
     */
    private void increaseToDo() {
        int[] oldToDo = ToDo;
        int[] oldWhToDo = whToDo;
        DSet[] oldToDoDSet = ToDoDSet;
        ToDoMaxSize = ToDoMaxSize * 2 + 1; //increase in 1.5
        
        ToDo = new int[ToDoMaxSize];
        whToDo = new int[ToDoMaxSize];
        ToDoDSet = new DSet[ToDoMaxSize];
        
        for(int i = 0; i < ToDoSize; i++) {
            ToDo[i] = oldToDo[i];
            whToDo[i] = oldWhToDo[i];
            ToDoDSet[i] = oldToDoDSet[i];
            
            oldToDoDSet[i] = null;
        }
        
        oldToDo = null;
        oldToDoDSet = null;
        oldWhToDo = null;
    }
    
    /**
     * Метод осуществляет увеличивание размера toDoList ограничений кардинальности данного индивида.
     */
    private void increaseMQfd() {
        int[] oldMQfd = MQfd;
        int[] oldWhMqfd = whMQfd;
        DSet[] oldMqfdDSet = MQfdDSet;
        MQfdMaxSize = MQfdMaxSize * 2 + 1;
        
        MQfd = new int[MQfdMaxSize];
        whMQfd = new int[MQfdMaxSize];
        MQfdDSet = new DSet[MQfdMaxSize];
        
        for(int i = 0; i < MQfdSize; i++) {
            MQfd[i] = oldMQfd[i];
            whMQfd[i] = oldWhMqfd[i];
            MQfdDSet[i] = oldMqfdDSet[i];
        }
    }
    
    /**
     * Метод осуществляет увеличивание размера toDoList для кванторов существования.
     */
    private void increaseSome() {
        int[] oldSome = Some;
        int[] oldWhSome = whSome;
        SomeMaxSize = SomeMaxSize * 2 + 1;
        
        Some = new int[SomeMaxSize];
        whSome = new int[SomeMaxSize];
        
        for(int i = 0; i < SomeSize; i++) {
            Some[i] = oldSome[i];
            whSome[i] = oldWhSome[i];
        }
    }

    /**
     * Метод осуществляет увеличивание размера toDoList для кванторов всеобщности.
     */
    private void increaseFAll() {
        int[] oldFAll = FAll;
        int[] oldWhFAll = whFAll;
        FAllMaxSize = FAllMaxSize * 2 + 1;
        
        FAll = new int[FAllMaxSize];
        whFAll = new int[FAllMaxSize];
        
        for(int i = 0; i < FAllSize; i++) {
            FAll[i] = oldFAll[i];
            whFAll[i] = oldWhFAll[i];
        }
        
        oldFAll = null;
        oldWhFAll = null;
    }
    
    /**
     * Метод осуществляет увеличивание размера массива для хранения потомков индивида.
     */
    private void increaseChild() {
        Couple[] oldChildren = children;
        int[] oldWhChild = whchild;
        ChildMaxSize = ChildMaxSize * 2 + 1;
        
        children = new Couple[ChildMaxSize];
        whchild = new int[ChildMaxSize];
        
        for(int i = 0; i < ChildSize; i++) {
            children[i] = oldChildren[i];
            whchild[i] = oldWhChild[i];
            oldChildren[i] = null;
        }
        oldChildren = null;
        oldWhChild = null;
    }
    
    /**
     * Метод осуществляет увеличивание размера массива для хранения предков индивида.
     */
    private void increaseParent() {
        Couple[] oldParents = parents;
        int[] oldWhParents = whparent;
        ParentMaxSize = ParentMaxSize * 2 + 1;
        
        parents = new Couple[ParentMaxSize];
        whparent = new int[ParentMaxSize];
        
        for(int i = 0; i < ParentSize; i++) {
            parents[i] = oldParents[i];
            whparent[i] = oldWhParents[i];
            oldParents[i] = null;
        }
        oldParents = null;
        oldWhParents = null;
    }
    
    /**
     * Метод осуществляет увеличивание размера массива для хранения идентичных индивидов.
     */
    private void increaseInds() {
        String[] oldInds = inds;
        int[] oldWhInds = whInds;
        IndsMaxSize = IndsMaxSize * 2 + 1;
        
        inds = new String[IndsMaxSize];
        whInds = new int[IndsMaxSize];
        for(int i = 0; i < IndsSize; i++) {
            inds[i] = oldInds[i];
            whInds[i] = oldWhInds[i];
        }
    }

    /**
     * В методе осуществляется возвращение к запомненному состоянию текущего индивида.
     * @param toDoPerf Определеяет какой toDoList должен быть выполнен.
     * @param when Определяет к какому именно состоянию должен вернуться данный индивид.
     * @param _curToDo Определяет номер текущего обрабатываемого элемента toDoList
     * @param _curMQfd Определяет номер текущего обрабатываемого элемента toDoList ограничений кардинальности.
     * @param _curFAll Определяет номер текущего обрабатываемого элемента toDoList кванторов всеобщности.
     * @param isDTV Определяет является ли данный индивид индивидом, представляющим тип данных.
     */
    public void restore(boolean toDoPerf, int when, int _curToDo, int _curMQfd, int _curFAll, boolean isDTV) {
        toDoPerform = toDoPerf;
        
        currentToDo = _curToDo;
        currentFAll = _curFAll;
        currentMQfd = _curMQfd;
        isDataTypeVertice = isDTV;
        
        while(ParentSize > 0 && whparent[ParentSize - 1] >= when) ParentSize--;
        while(ChildSize > 0 && whchild[ChildSize - 1] >= when) ChildSize--;
        while(ToDoSize > 0 && whToDo[ToDoSize - 1] >= when) {
            ToDoSize--;
            //toDoSet.remove(ToDo[ToDoSize]);
            ToDoDSet[ToDoSize] = null;
        }
        
        //restore To Do List
        int curSize = 0;
        int[] oldToDo = new int[ToDoMaxSize];
        int[] oldWhToDo = new int[ToDoMaxSize];
        DSet[] oldToDoDSet = new DSet[ToDoMaxSize];
        for(int i = 0; i < ToDoSize; i++) {
            if(whToDo[i] < when) {
                oldToDo[curSize] = ToDo[i];
                oldWhToDo[curSize] = whToDo[i];
                oldToDoDSet[curSize] = ToDoDSet[i];
                curSize++;
            } else {
                ToDoDSet[i] = null;
            }
        }
        ToDoSize = curSize;
        ToDo = oldToDo;
        whToDo = oldWhToDo;
        ToDoDSet = oldToDoDSet;
         
        while(SomeSize > 0 && whSome[SomeSize - 1] >= when) SomeSize--;
        while(FAllSize > 0 && whFAll[FAllSize - 1] >= when) FAllSize--;
        while(MQfdSize > 0 && whMQfd[MQfdSize - 1] >= when) MQfdSize--;
        while(IndsSize > 0 && whInds[IndsSize - 1] >= when) IndsSize--;
    }
    
    private ArrayList<String> ids = new ArrayList<String>();
    /**
     * Метод возвращает индивидов, которые были удалены после возврата к определенному состоянию.
     * @param when Определяет номер состояния к которому возвращается индивид.
     * @return Массив индивидов, которые удаляются из toDoList индивидов, соответствующих данному.
     */
    public ArrayList<String> getRemovedIndivids(int when) {
        ids.clear();
        for(int i = IndsSize - 1; i >= 0; i--) {
            if(whInds[i] >= when)
                ids.add(inds[i]);
        }
        return ids;
    }
    
    /**
     * Добавляет индивида в список потомков данной вершины интерпретации.
     * @param child Ссылка на индивида интерпретации, который является потомком данной вершины.
     * @param role Роль по которой связан потомок.
     * @param when Определяет размер стека на котором был добавлен текущий индивид.
     */
    public void addChild(InterpretationNode child, int role, int when)
    {
        if(ChildSize == ChildMaxSize - 1) {
            increaseChild();
        }
        
        boolean added = false;
        for(int i = 0; i < ChildSize; i++) {
            if(children[i].getNode() == child)
                if(children[i].contains(role)) return; else {
                    children[i].addNewRole(role);
                    added = true; break;
                }
        }
        
        if(!added) {
            children[ChildSize] = new Couple(child, role);
            whchild[ChildSize] = when;
            ChildSize++;
        }
    }

    /**
     * Метод осуществляет добавление всех педков вершины, связанных по транзитивной роли.
     * @param role Ссылка на роль по которой связан предок.
     * @param when Размер стека на котором добавляются предки.
     * @param r_box Ссылка на RBox текущей базы знаний.
     */
    public void updateParents(int role, int when, RBox r_box) { //if there is some parent with transitive role - add it
        if(role < 0) return;
        if(r_box.getRoleByIndex(role).isTransitive()) {
            for(int i = 0; i < ParentSize; i++) {
                if(!r_box.isSubOrEqualAll(parents[i].getRoles(), role)) continue;
                for(int k = 0; k < parents[i].getNode().getParentSize(); k++) {
                    Couple pr = parents[i].getNode().getParents()[k];
                    for(int j = 0; j < pr.getRoles().length; j++) {
                        if(r_box.isSubOrEqual(pr.getRoles()[j], role)) {
                            addParent(pr.getNode(), role, when, r_box);
                            pr.getNode().addChild(this, role, when);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Добавляет индивида в список предков данной вершины интерпретации.
     * @param parent Ссылка на индивида интерпретации, который является предком данной вершины.
     * @param role Роль по которой связан потомок.
     * @param when Определяет размер стека на котором был добавлен текущий индивид.
     * @param r_box Ссылка на RBox текущей базы знаний.
     */
    public void addParent(InterpretationNode parent, int role, int when, RBox r_box) {
        if(ParentSize == ParentMaxSize - 1) {
            increaseParent();
        }
        
        boolean added = false;
        for(int i = 0; i < ParentSize; i++) {
            if(parents[i].getNode() == parent) {
                if(parents[i].contains(role)) {
                    return;
                } else {
                    parents[i].addNewRole(role);
                    added = true; break;
                }
            }
        }
        
        if(!added) {
            parents[ParentSize] = new Couple(parent, role);
            whparent[ParentSize] = when;
            ParentSize++;
        }
        //updateParents(role, when, r_box);
    }

    /**
     * Определяет всех индивидов связанных с данным по заданному отношению.
     * @param role Номер отношения в пределах рассматриваемой базы знаний.
     * @param r_box Ссылка на RBox рассматриваемой базы знаний.
     * @return Возвращает всех индивидов связанных с данным по заданному отношению.
     */
    public ArrayList<InterpretationNode> getNeighboursByRole(int role, RBox r_box) {
        toRet = new ArrayList<InterpretationNode>();
        for(int i = 0; i < ChildSize; i++) {
            //if(children[i].getNode().isSkipped()) continue;
            for(int j = 0; j < children[i].getRoles().length; j++) {
                if(r_box.isSubOrEqual(children[i].getRoles()[j], role)) {
                    if(!toRet.contains(children[i].getNode())) {
                        toRet.add(children[i].getNode());
                    }
                }
            }
        }
        
        for(int i = 0; i < ParentSize; i++) {
            //if(parents[i].getNode().isSkipped()) continue;
            for(int j = 0; j < parents[i].getRoles().length; j++) {
                if(r_box.isReverseSubOrEqual(parents[i].getRoles()[j], role)) {
                    if(!toRet.contains(parents[i].getNode())) {
                        toRet.add(parents[i].getNode());
                    }
                }
            }
        }
        
        return toRet;
    }
    
    /**
     * Метод определяет индекс toDoList текущего индивида в котором находится заданное правило.
     * @param x Определяет элемент, который ищется в toDoList
     * @return Возвращает индекс заданного элемента toDoList, или -1 если такого правила нет в toDoList
     */
    public int findInToDo(int x) {
        for(int i = 0; i < ToDoSize; i++)
            if(ToDo[i] == x) return i;
        return -1;
    }
    
    /**
     * Метод определяет является ли заданный концепт отрицанием какого-либо концепта в toDoList.
     * @param toDoEntry Определяет номер вершины леса ограничений.
     * @return Возвращает истина, если в toDoList текущего индивида существует отрицание заданного концепта и ложь в противном случае.
     */
    public boolean isConflict(int toDoEntry) {
        return isContain(-toDoEntry);
    }
    
    /**
     * Возвращает множество зависимостей конфликтующего концепта.
     * @param concept_id Идентификатор концепта леса ограничений.
     * @return Возвращает множество зависимостей или null, если данный концепт не имеет конфликтов в toDoList.
     */
    public DSet getConflictDSet(int concept_id) {
        for(int i = 0; i < ToDoSize; i++)
            if(concept_id == -ToDo[i]) return ToDoDSet[i];
        return null;
    }
    
    /**
     * Метод определяет существует ли заданный концепт в toDoList индивида.
     * @param toDoEntry Определяет идентификатор вершины леса ограничений, который ищется в toDoList.
     * @return Возвращает истина, если в toDoList существует заданный концепт и ложь в противном случае.
     */
    public boolean isContain(int toDoEntry) {
        for(int i = 0; i < ToDoSize; i++)
            if(ToDo[i] == toDoEntry) {
                return true;
            }
        return false;
    }
    
    /**
     * Метод добавляет индивида в toDoList индивидов.
     * @param abn Имя добавляемого индивида.
     * @param when Размер стека на котором добавляется данный индивид.
     * @return Возвращает истина если индивид был добавлен и ложь в противном случае.
     */
    public boolean addIndivid(String abn, int when) {
        for(int i = 0; i < IndsSize; i++) {
            if(abn.equals(inds[i])) return false;
        }

        if(IndsSize == IndsMaxSize - 1) {
            increaseInds();
        }
        
        inds[IndsSize] = abn;
        whInds[IndsSize] = when;
        IndsSize++;
        return true;
    }
    
    /**
     * Возвращет список всех потомков вершины.
     * @return Массив потомков вершины.
     */
    public Couple[] getChildren() {
        return children;
    }
    
    /**
     * Возвращет список всех предков вершины.
     * @return Массив предков вершины.
     */
    public Couple[] getParents() {
        return parents;
    }
    
    /**
     * Устанавливает параметр блокировки вершины.
     * @param new_block Определяет нужно ли блокировать вершину.
     */
    public void setBlock(int new_block) {
        block = new_block;
    }
    
    /**
     * Метод возвращет была ли блокирована вершина.
     * @return Блокировка вершины.
     */
    public int getBlock() {
        return block;
    }
    
    /**
     * Метод определяет была ли блокирована вершина.
     * @return Блокировка вершины.
     */
    public boolean isBlocked() {
        return block == 1;
    }
    
    /**
     * Метод устаналивает была ли объединена вершина.
     * @param new_skip Определяет была ли объединена вершина.
     */
    public void setSkip(boolean new_skip) {
        skip = new_skip;
    }
    
    /**
     * Метод определяет была ли данная вершина объединена с другой.
     * @return Нужно ли обрабатывать вершину.
     */
    public boolean isSkipped() { //seems this means that this vertice merged to another
        return skip;
    }

    /**
     * Метод возвращает toDoList.
     * @return Массив элементов toDoList.
     */
    public int[] getToDo() {
        return ToDo;
    }
    
    /**
     * Метод возвращает множества зависимостей каждого элемента toDoList.
     * @return Массив множеств зависимостей каждого из элементов toDoList.
     */
    public DSet[] getToDoDSet() {
        return ToDoDSet;
    }
    
    /**
     * Метод возвращает массив того когда были добавлены каждый из элементов.
     * @return Массив элементов, когда был добавлен каждый элемент toDoList.
     */
    public int[] getToDoWhen() {
        return whToDo;
    }
    
    /**
     * Возвращает множество зависимостей заданного концепта.
     * @param concept_id Концепт множество зависимостей которого определяется в данном методе.
     * @return Множество зависимостей концепта.
     */
    public DSet getDSet(int concept_id) {
        for(int i = 0; i < ToDoSize; i++) {
            if(concept_id == ToDo[i]) return ToDoDSet[i];
        }
        return null;
    }
    
    /**
     * Метод возвращает множества зависимостей каждого элемента toDoList ограничений кардинальности.
     * @return Массив множеств зависимостей каждого из элементов toDoList ограничений кардинальности.
     */
    public DSet[] getMxQfDSet() {
        return MQfdDSet;
    }
    
    /**
     * Метод возвращает множества зависимостей каждого элемента toDoList индивидов.
     * @return Массив множеств зависимостей каждого из элементов toDoList индивидов.
     */
    public String[] getInds() {
        return inds;
    }
    
    /**
     * Удаляет последний элемент toDoList.
     */
    public void popToDo() {
        if(ToDoSize > 0) ToDoSize--;
    }
    
    /**
     * Добавляет элемент в toDoList данной вершины интерпретации.
     * @param new_to_do Определяет новый элемент toDoList.
     * @param d Множество зависимостей данного правила.
     * @param when Определяет размер стека на котором был добавлен текущий индивид.
     * @param t_box Ссылка на TBox текущей базы знаний.
     */
    public boolean addToDo(Integer new_to_do, DSet d, int when, TBox t_box)
    {
        if(new_to_do == 1) return false; // if new_to_do is TOP
        if(isContain(new_to_do)) return false;
        if(ToDoSize == ToDoMaxSize - 1) {
            increaseToDo();
        }
        
        toDoPerform = true;
        //toDoSet.add(new_to_do);
        ToDo[ToDoSize] = new_to_do;
        ToDoDSet[ToDoSize] = d;
        whToDo[ToDoSize] = when;
        ToDoSize++;
        return true;
    }
    
    /**
     * Добавляет элемент в toDoList данной вершины интерпретации в конкретную позицию.
     * @param new_to_do Определяет новый элемент toDoList.
     * @param d Множество зависимостей данного правила.
     * @param when Определяет размер стека на котором был добавлен текущий индивид.
     * @param pos Позиция toDoList в которую необходимо вставить новый элемент.
     * @return Возвращает истина, если элемент был добавлен в toDoList.
     */
    public boolean addToDoPos(int new_to_do, DSet d, int when, int pos) //return true only iff new_to_do was added to the toDoList
    {
        //addToDo(new_to_do, d, when); if(true) return true;
        if(new_to_do == 1) return false; // if new_to_do is TOP
        if(isContain(new_to_do)) return false;
        if(ToDoSize == ToDoMaxSize - 1) {
            increaseToDo();
        }
        
        for(int i = ToDoSize; i > pos; i--) {
            int h = ToDo[i];            
            ToDo[i] = ToDo[i - 1];
            ToDo[i - 1] = h;
            
            h = whToDo[i];            
            whToDo[i] = whToDo[i - 1];
            whToDo[i - 1] = h;
            
            DSet dh = ToDoDSet[i];
            ToDoDSet[i] = ToDoDSet[i - 1];
            ToDoDSet[i - 1] = dh;
        }
        //toDoSet.add(new_to_do);        
        ToDo[pos] = new_to_do;
        ToDoDSet[pos] = d;
        whToDo[pos] = when;
        
        ToDoSize++;
        return true;
    }
    
    /**
     * Метод массив элементов toDoList кванторов всеобщности.
     * @return Массив множеств зависимостей каждого из элементов toDoList кванторов всеобщности.
     */
    public int[] getFAll() {
        return FAll;
    }
    
    /**
     * Добавляет элемент в toDoList кванторов всеобщности данной вершины интерпретации.
     * @param new_to_do Определяет новый элемент toDoList.
     * @param when Определяет размер стека на котором был добавлен текущий элемент.
     */
    public void addFAll(int new_to_do, int when) {
        if(FAllSize == FAllMaxSize - 1) {
            increaseFAll();
        }
        FAll[FAllSize] = new_to_do;
        whFAll[FAllSize] = when;
        FAllSize++;
    }
        
    /**
     * Метод массив элементов toDoList ограничений кардинальности.
     * @return Массив множеств зависимостей каждого из элементов toDoList ограничений кардинальности.
     */
    public int[] getMQfd() {
        return MQfd;
    }
    
    /**
     * Добавляет элемент в toDoList ограничений кардинальности данной вершины интерпретации.
     * @param new_to_do Определяет новый элемент toDoList.
     * @param when Определяет размер стека на котором был добавлен текущий элемент.
     */
    public void addMQfd(int new_to_do, DSet d, int when) {
        if(MQfdSize == MQfdMaxSize - 1) increaseMQfd();
        MQfd[MQfdSize] = new_to_do;
        MQfdDSet[MQfdSize] = d;
        whMQfd[MQfdSize] = when;
        MQfdSize++;
    }
    
    /**
     * Добавляет элемент в toDoList кванторов существования данной вершины интерпретации.
     * @param new_to_do Определяет новый элемент toDoList.
     * @param when Определяет размер стека на котором был добавлен текущий элемент.
     */
    public void addIncr(int new_to_do, int when) {
        if(SomeSize == SomeMaxSize - 1) {
            increaseSome();
        }
        Some[SomeSize] = new_to_do;
        whSome[SomeSize] = when;
        SomeSize++;
    }
    
    /**
     * Метод массив элементов toDoList кванторов существования.
     * @return Массив множеств зависимостей каждого из элементов toDoList кванторов существования.
     */
    public int[] getIncr() {
        return Some;
    }
    
    /**
     * Удаляет потомка данной вершины по заданной ссылке на потомка.
     * @param child Ссылка на потомка данного индивида.
     */
    public void deleteChild(InterpretationNode child) {
        int index = -1;
        for(int i = 0; i < ChildSize; i++)
            if(children[i].getNode().equals(child)) index = i;

        if(index == -1)
            System.out.printf("Can not find the child!");
        else {
            ChildSize--;
        }
    }
    
    /**
     * Удаляет предка данной вершины по заданной ссылке на потомка.
     * @param parent Ссылка на предка данного индивида.
     */
    public void deleteParent(InterpretationNode parent) {
        int index = -1;
        for(int i = 0; i < ParentSize; i++)
            if(parents[i].getNode().equals(parent)) index = i;

        if(index == -1)
            System.out.printf("Can not find the parent!");
        else {
            ParentSize--;
        }
    }
    
    /**
     * Метод осуществляет вывод на экран ветки интерпретации.
     * @param node Ссылка на индивида интерпретации, который обрабатывается в данный момент.
     * @param spaces Определяет отсутпы для форматированного вывода.
     * @param r_box Ссылка на RBox текущей базы знаний.
     * @param t_box Ссылка на TBox текущей базы знаний.
     * @param gl Параметр - целое число.
     */
    private void DownTree(InterpretationNode node, String spaces, RBox r_box, TBox t_box, int gl) {
        if(node.isSkipped()) {
            System.out.println("SKIPPED!");
        }
        
        System.out.print(spaces);
        for(int i = 0; i < node.getToDoSize(); i++) {
            System.out.print(node.getToDo()[i] + " ");
        }
        System.out.println();
        
        System.out.println(spaces + node.indexInQueue);
        System.out.print(spaces);
        for(int i = 0; i < node.getIndsSize(); i++) {
            System.out.print(node.getInds()[i] + " ");
        }
        System.out.println();
        for(int i = 0; i < node.getIndsSize(); i++) {
            System.out.println(spaces + (node.indexInQueue) + node.getInds()[i]);
        }
        for(int i = 0; i < node.getToDoSize(); i++) {
            int it = node.getToDo()[i];
            if(t_box.getRuleGraph().getNode(Math.abs(it)).getChildrenSize() > 0) {
                int chld = t_box.getRuleGraph().getNode(Math.abs(it)).getChildren()[0];
                if(t_box.getRuleGraph().getNode(Math.abs(chld)).getName() != null) {
                    if(t_box.getRuleGraph().getNode(Math.abs(chld)).getName().indexOf("Cycle") != -1) {
                        //System.out.println(spaces + "CYCLE: " + it + " " + r_box.getRoleByIndex(t_box.getRuleGraph().getNode(Math.abs(it)).getRoleType()).getName());
                        int kor = 123;
                    }
                }
            }
            if(t_box.getRuleGraph().getNode(Math.abs(it)).getNodeType() != NodeType.ntCONCEPT) continue;
            if(it < 0) {
                System.out.println(spaces + (node.indexInQueue) + "NOT " + t_box.getRuleGraph().getNode(Math.abs(it)).getName());
            } else {
                System.out.println(spaces + (node.indexInQueue) + t_box.getRuleGraph().getNode(Math.abs(it)).getName());
            }
            //System.out.print(" ");
            //System.out.print(node.getToDoDSet()[i].size());
            //System.out.println();
        }
        flag.add(node);
        for(int i = 0; i < node.getChildSize(); i++) {
            if(node.getChildren()[i].getNode().isSkipped()) continue;
            System.out.print(spaces);
            for(int j = 0; j < node.getChildren()[i].getRoles().length; j++)
                System.out.println(r_box.getRoleByIndex(node.getChildren()[i].getRoles()[j]).getName());
             
            if(!flag.contains(node.getChildren()[i].getNode())) {
                DownTree(node.getChildren()[i].getNode(), spaces + " ", r_box, t_box, gl);
            } else {
                System.out.println(spaces + " " + node.getChildren()[i].getNode().indexInQueue);
            }
        }
    }
    
    /**
     * Метод выводит в консоль все элементы toDoList данного индивида.
     * @param t_box Ссылка на TBox текущей базы знаний.
     */
    public void showToDo(TBox t_box) {
        System.out.print(" ");
        for(int i = 0; i < getToDoSize(); i++) {
            System.out.print(getToDo()[i]);
            System.out.print(" ");
        }
        System.out.println();
    }
    
    /**
     * Метод осуществляет вывод в консоль ветки интерпретации с корнем в текущем индивиде.
     * @param r_box Ссылка на RBox текущей базы знаний.
     * @param t_box Ссылка на TBox текущей базы знаний.
     */
    public void show(RBox r_box, TBox t_box) {
        System.out.println();
        flag.clear();
        DownTree(this, "", r_box, t_box, 0);
        System.out.println();
    }
}