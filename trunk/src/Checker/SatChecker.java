package Checker;

import Enums.NodeType;
import Help.DSet;
import Checker.Structures.Couple;
import Help.HashContainer;
import Help.IntArray;
import Help.IntPair;
import Checker.Model.InterpretationNode;
import KnowledgeBase.ABox;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import KnowledgeBase.RuleGraph.RuleNode;
import KnowledgeBase.Query;
import java.util.ArrayList;
import java.util.HashSet;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

/**
 * Класс SatChecker является сердцем TReasoner.
 * В этом классе реализован табличный алгоритм,
 * который основан на улучшенном переборе с возвратом (backJumping). Данный класс использует
 * класс BTState на основе которого реализуется самописный стек.
 * @author Andrey Grigoryev
 */
public class SatChecker {
    
    /**
     * Класс QSetPair реализован для поддержки оптимизации по численным ограничениям.
     * Данный класс является всего-лишь структурой для хранения двух различных полей: QSet и InterpretationNode.
     */
    private class QSetPair {
        public QSet[] q1;
        public InterpretationNode[][] q2;
        
        /**
         * 
         * @param x1 Массив объектов QSet
         * @param x2 Двумерный массив объектов InterpretationNode
         */
        public QSetPair(QSet[] x1, InterpretationNode[][] x2) {
            q1 = x1;
            q2 = x2;
        }
    }
    
    /**
     * Класс BTState является основой стека, который используется для осуществления улучшенного перебора с возвратом (backJumping).
     * В классе реализованы поля, которые необходимо запоминать в стек. Каждый объект данного класса соответствует одному из случаев
     * недетерминированного выбора.
     */
    private class BTState {
        public int IndividID = 0, toDoIndex = 0, queueSize = 0;
        public Object option = null;
        public Object lastOption = null;
        public QualSolver qo = null;
        
        public boolean[] skips = null; //is skipped vertice
        public boolean[] isDV = null; //is datatype vertice
        public boolean[] toDoPerfs = null; //is reasoner perform toDoList or perform MQfdToDoList
        public int[] curToDo = null;
        public int[] curMQfd = null;
        public int[] curFAll = null;
        
        public int skipsSize = 0;
        
        public DSet context = null;
        public int curBNum = 0;
        
        public BTState() {}
        
        /**
         * Метод очищает все поля объекта.
         */
        public void clear() {
            curBNum = 0;
            IndividID = 0;
            toDoIndex = 0;
            queueSize = 0;
            skipsSize = 0;

            curToDo = null;
            curMQfd = null;
            curFAll = null;
            isDV = null;
            skips = null;
            toDoPerfs = null;
            option = null;
            lastOption = null;
            context = null;
            qo = null;
        }
        
        /**
         * Основной и единственный конструктор элемента стека
         * @param new_IndividID Определяет номер текущего рассматриваемого индивида
         * @param new_toDoIndex Определяет текущий номер элемента toDoList
         * @param new_option Определяет альтернативу выбранную при текущем недетерминированном выборе
         * @param last_option Определяет последнюю альтернативу при осуществлении недетерминированного выбора
         * @param queSize Определяет текущий размер очереди интерпретации
         * @param new_perfs Определяет массив, каждый элемент которого соответствует индивиду очереди,
         * каждый элемент которого определяет выполняется ли на текущем индивиде toDoList или <= list
         * @param new_skips Определяет массив, каждый элемент которого соответствует индивиду очереди,
         * каждый элемент которого определяет нужно ли пропускать данного индивида
         * @param _skipsSize Определяет размер массива new_skips
         * @param _curBNum Определяет текущую альтернативу в последнем OR-правиле
         * @param ctx Определяет множество зависимостей которое необходимо учитывать при выборе следующих альтернатив
         * @param _cToDo Определяет массив, каждый жлемент которого соответствует
         * индивиду из очереди и определяет номер элемента toDoList выполняемого в текущий момент
         * @param _cMQfd Определяет массив, каждый жлемент которого соответствует
         * индивиду из очереди и определяет номер элемента <=List выполняемого в текущий момент
         * @param _cFAll Определяет массив, каждый жлемент которого соответствует
         * индивиду из очереди и определяет номер элемента FORALL-List выполняемого в текущий момент
         * @param _isDV Определяет массив, каждый жлемент которого соответствует
         * индивиду из очереди и определяет является ли текущий индивид вершиной данных
         */
        public BTState(int new_IndividID, int new_toDoIndex, Object new_option, Object last_option, int queSize, boolean[] new_perfs, boolean[] new_skips, int _skipsSize, int _curBNum, DSet ctx, int[] _cToDo, int[] _cMQfd, int[] _cFAll, boolean[] _isDV) {
            curBNum = _curBNum;
            IndividID = new_IndividID;
            toDoIndex = new_toDoIndex;
            option = new_option;
            lastOption = last_option;
            queueSize = queSize;
            skips = new_skips;
            toDoPerfs = new_perfs;
            context = new DSet();
            skipsSize = _skipsSize;
            curToDo = _cToDo;
            curMQfd = _cMQfd;
            curFAll = _cFAll;
            isDV = _isDV;
        }
        
        /**
         * Метод осуществляет доступ к полям объекта BTState
         * @param new_IndividID Определяет номер текущего рассматриваемого индивида
         * @param new_toDoIndex Определяет текущий номер элемента toDoList
         * @param new_option Определяет альтернативу выбранную при текущем недетерминированном выборе
         * @param last_option Определяет последнюю альтернативу при осуществлении недетерминированного выбора
         * @param queSize Определяет текущий размер очереди интерпретации
         * @param new_perfs Определяет массив, каждый элемент которого соответствует индивиду очереди,
         * каждый элемент которого определяет выполняется ли на текущем индивиде toDoList или <= list
         * @param new_skips Определяет массив, каждый элемент которого соответствует индивиду очереди,
         * каждый элемент которого определяет нужно ли пропускать данного индивида
         * @param _skipsSize Определяет размер массива new_skips
         * @param _curBNum Определяет текущую альтернативу в последнем OR-правиле
         * @param ctx Определяет множество зависимостей которое необходимо учитывать при выборе следующих альтернатив
         * @param _cToDo Определяет массив, каждый жлемент которого соответствует
         * индивиду из очереди и определяет номер элемента toDoList выполняемого в текущий момент
         * @param _cMQfd Определяет массив, каждый жлемент которого соответствует
         * индивиду из очереди и определяет номер элемента <=List выполняемого в текущий момент
         * @param _cFAll Определяет массив, каждый жлемент которого соответствует
         * индивиду из очереди и определяет номер элемента FORALL-List выполняемого в текущий момент
         * @param _isDV Определяет массив, каждый жлемент которого соответствует
         * индивиду из очереди и определяет является ли текущий индивид вершиной данных
         */
        public void set(int new_IndividID, int new_toDoIndex, Object new_option, Object last_option, int queSize, boolean[] new_perfs, boolean[] new_skips, int _skipsSize, int _curBNum, DSet ctx, int[] _cToDo, int[] _cMQfd, int[] _cFAll, boolean[] _isDV)
        {
            curBNum = _curBNum;
            IndividID = new_IndividID;
            toDoIndex = new_toDoIndex;
            option = new_option;
            lastOption = last_option;
            queueSize = queSize;
            
            skips = null;
            toDoPerfs = null;
            
            skips = new_skips;
            toDoPerfs = new_perfs;
            context = new DSet();
            skipsSize = _skipsSize;
            curToDo = _cToDo;
            curMQfd = _cMQfd;
            curFAll = _cFAll;
            isDV = _isDV;
        }
        
        /**
         * Метод проверяет является ли последней та опция, которая соответствует текущему элементу стека.
         * @return 
         */
        public boolean isLastOption() {
            if(option instanceof Integer) { //this is OR-rule
                return (int)((Integer) option) >= (int)((Integer) lastOption);
            }
            if(option instanceof IntPair) { //this is a <=-rule ?
                return (((IntPair) option).x >= ((IntPair) option).y) || 
                        (((IntPair) option).y == ((Integer)lastOption - 1) && ((IntPair) option).y > ((IntPair) option).x);
            }
            if(option instanceof ChooseState) { //this is a CHOOSE-rule
                return ((ChooseState)option).notC.isEmpty();
            }
            if(option instanceof QSetPair) { //this is a CHOOSE-rule
                return ((Boolean)lastOption).booleanValue();
            }
            return false;
        }
    }
    
    /**
     * Класс определяет текущее состояние объектов соседей индивида.
     * Поле ai содержит номера индивидов из очереди, которые являются соседом рассматриваемого индивида.
     * Поле notC содержит номера индивидов из очереди (подмножество массива ai), к которым будет добавлено отрицание концепта C.
     */
    private class ChooseState
    {
        public ArrayList<Integer> notC = new ArrayList<Integer>();
        public ArrayList<Integer> ai = new ArrayList<Integer>();
        
        public ChooseState(ArrayList<Integer> new_not_c, ArrayList<Integer> new_ai) {
            notC = new ArrayList<Integer>(new_not_c);
            ai = new ArrayList<Integer>(new_ai);
        }
    }
    
    private HashSet<InterpretationNode> flag = new HashSet<InterpretationNode>();
    public ArrayList<InterpretationNode> roots = new ArrayList<InterpretationNode>();

    //There is backTracking variables
    private int MaxBTSize = 1 << 12;
    private BTState[] BTStack = new BTState[MaxBTSize];
    private int BTSize = 0;
    //End backTracking variables

    private QualSolver currentQO = null;
    
    private RBox r_box;
    private TBox t_box;
    private ABox a_box;
    private Interpretation graph;    
    private AChecker a_checker;
    private TCache pos_cache = new TCache(null);
    private TCache neg_cache = new TCache(null);
    Automorphism aut = null;
    
    boolean[] conc = null;
    boolean[] nperfs = null;
    boolean[] nskips = null;
    
    //There is queue variables
    private int QMaxSize = 1 << 14;
    private InterpretationNode[] queue = new InterpretationNode[QMaxSize];
    private int[] whqueue = new int[QMaxSize];
    private int QSize = 0;
    //End queue variables
    
    public Cache[][] cache = null;
    
    private DSet curDS = null;
    private int curOr = 0;
    private ChooseState curChoose = null;
    private ArrayList<Integer> ai = new ArrayList<Integer>();
    private int curLevel = 0;
    private int st1 = 0, st2 = 1;
    private int curBNum = 0;
    private int newIndividID = 0;
    private boolean justAddedLE = false;
    
    private Statistics stats = new Statistics();
    private boolean use_a_checker = true;
    private boolean show_stats = false;
    private boolean use_back_jump = true;
    private boolean use_global_caching = false;
    private boolean add_to_end = false;
    
    private long sec_millis = 100000;
    
    private SatChecker sub_checker = null;
    private DataChecker data_checker = null;
    
    private DSet conflictDSet = null;
    private int IndividID = 0, toDoIndex = 0;
    public HashContainer[] no_sub_sum = null;
    
    public boolean experimentAttr = false;
    
    /**
     * Основной и единственный конструктор для класса SatChecker
     * @param s_check Определяет подчекер, объект класса SatChecker, необходимый для выполнения задач проверки согласованности в отдельных случаях
     * @param new_r_box Параметр определяет RBox онтологии
     * @param new_t_box Параметр определяет TBox онтологии
     * @param new_a_box Параметр определяет ABox онтологии
     * @param is_use_a_checker Параметр определяет нужно использовать AChecker
     * @param is_use_back_jump Параметр определяет нужно использовать backJumpnig
     * @param is_use_caching Параметр определяет нужно использовать кэширование
     * @param is_use_global_caching Параметр определяет нужно использовать глобальное кэширование
     * @param use_show_stats Параметр определяет нужно ли показывать статистику
     * @param secs Параметр определяет количество милисекунд, за которые reasoner должен ответить
     */
    public SatChecker(SatChecker s_check, RBox new_r_box, TBox new_t_box, ABox new_a_box, boolean is_use_a_checker, boolean is_use_back_jump, boolean is_use_caching, boolean is_use_global_caching, boolean use_show_stats, long secs)
    {
        add_to_end = true;
        
        for(int i = 0; i < MaxBTSize; i++)
            BTStack[i] = new BTState();
        
        for(int i = 0; i < QMaxSize; i++)
            queue[i] = new InterpretationNode();
        
        int current_size = 131072;
        if(new_t_box != null) current_size = new_t_box.getRuleGraph().getConceptsSize();
        cache = new Cache[2][current_size];
        
        t_box = new_t_box;
        r_box = new_r_box;
        a_box = new_a_box;
        if(t_box == null)
            aut = new Automorphism(null); else
            aut = new Automorphism(t_box.getRuleGraph());
        sub_checker = s_check;
        if(t_box != null)
        {
            pos_cache.setRuleGraph(t_box.getRuleGraph());
            neg_cache.setRuleGraph(t_box.getRuleGraph());
        }
        
        graph = new Interpretation(new_r_box);
        a_checker = new AChecker(new_t_box, false /*PH Checker*/);
        use_a_checker = is_use_a_checker;
        use_back_jump = is_use_back_jump;
        use_global_caching = is_use_global_caching;
        show_stats = use_show_stats;
        sec_millis = secs;
        data_checker = new DataChecker(new_t_box);
    }
    
    /**
     * Метод очищает все поля объекта.
     */
    public void clear() {
        data_checker.clear();
        IndividID = 0; toDoIndex = 0; conflictDSet = null;
        if(sub_checker != null) sub_checker.clear();
        stats.clear();
        if(ai != null) ai.clear(); 
        if(curChoose != null) {
            curChoose.ai.clear();
            curChoose.notC.clear();
        }
        
        for(int i = 0; i < 2; i++)
            for(int j = 0; j < cache[0].length; j++)
                cache[i][j] = null;
        
        curDS = null;
        queueClear();
        nperfs = null;
        nskips = null;

        flag.clear();
        roots.clear();

        for(int i = 0; i < BTSize; i++)
            BTStack[i].clear();
        
        for(int i = 0; i < QMaxSize; i++)
            queue[i].clear();
        
        a_checker.clear();
        a_box.clearNodes();
        
        pos_cache.clear();
        neg_cache.clear();
        //ph_checker.clear();
        conc = null;
    }
    
    /**
     * Метод очищает очередь индивидов используемых в табличном алгоритме.
     */
    private void queueClear() {
        QSize = 0;
        for(int i = 0; i < QMaxSize; i++)
            queue[i].clear();
    }
    
    /**
     * Метод для увеличения размера стека. 
     * Каждый раз при достижении размера стека равного MaxBTSize, он увеличивается в 1,5 раза.
     */
    private void increaseStack() {
        BTState[] oldStack = BTStack;
        MaxBTSize = MaxBTSize * 2 + 1;
        
        BTStack = new BTState[MaxBTSize];
        for(int i = 0; i < BTSize; i++)
            BTStack[i] = oldStack[i];
        
        for(int i = BTSize; i < MaxBTSize; i++)
            BTStack[i] = new BTState();
    }
    
    /**
     * Метод для увеличения размера очереди индивидов. 
     * Каждый раз при достижении размера очереди равного QMaxSize, она увеличивается в 1,5 раза.
     */
    private void increaseQueue() {
        InterpretationNode[] oldQueue = queue;
        int[] oldWhQueue = whqueue;
        
        QMaxSize = QMaxSize * 2 + 1;
        
        queue = new InterpretationNode[QMaxSize];
        whqueue = new int[QMaxSize];
        
        for(int i = 0; i < QSize; i++) {
            queue[i] = oldQueue[i];
            whqueue[i] = oldWhQueue[i];
        }
        
        for(int i = QSize; i < QMaxSize; i++)
            queue[i] = new InterpretationNode();
        
        oldQueue = null;
        oldWhQueue = null;
    }
        
    /**
     * Метод осуществляет добавление нового индивида.
     * В методе осуществляет выделение памяти под объект InterpretationNode и определении его места в очереди.
     * Параметр pos определяет в какое конкретное место в очереди должен быть добавлен очередной индивид.
     * Если текущий добавляемый индивид будет представлять тип данных то переменная isDataVertice становится равной true.
     * Эта переменная определяет является ли текущий индивид конкретным объектом - типом данных.
     * Если глобальный параметр add_to_end будет равен истине, то вне зависимости от параметра pos любой
     * индивид будет добавляться в конец очереди. Если параметр todo является ссылкой на AND-правило, то оно
     * будет раскрыто немедленно.
     * @param parent Определяет предшественника (родителя) текущего индивида
     * @param newRole Определяет идентификатор роли, по которой предшественник соединен с текущим индивидом
     * @param todo Определяет элемент леса ограничений который должен быть добавлен к текущему индивиду.
     * @param D Определяет {@link DSet} текущего todo
     * @param pos Определяет позицию в очереди на место которой должен встать текущий индивид
     * @param isDataVertice Определяет является ли текущая вершина вершиной данных.
     * @return Объект, соответствующий добавленному индивиду.
     */
    private InterpretationNode addNewIndivid(InterpretationNode parent, int newRole, int todo, DSet D, int pos, boolean isDataVertice) {
        if(t_box.getRuleGraph().isDataNode(Math.abs(todo)))
            isDataVertice = true;

        if(add_to_end) pos = -1;
        InterpretationNode current_node = null;
        int wh = BTSize;
        if(pos == -1) {
            queue[QSize].clear();
            whqueue[QSize] = wh;
            current_node = graph.addNode(parent, queue[QSize], newRole, wh);
            current_node.indexInQueue = QSize;
        } else {
            queue[QSize].clear();
            whqueue[QSize] = wh;
            current_node = graph.addNode(parent, queue[QSize], newRole, wh);
            current_node.indexInQueue = QSize;
            for(int i = QSize; i > pos; i--) {
                int h = whqueue[i];
                whqueue[i] = whqueue[i - 1];
                whqueue[i - 1] = h;
                
                InterpretationNode nh = queue[i];
                queue[i] = queue[i - 1];
                queue[i - 1] = nh;
                
                queue[i].indexInQueue = i;
                queue[i - 1].indexInQueue = i - 1;
            }
        }
        
        if(QSize == QMaxSize - 1) {
            increaseQueue();
        }
        
        QSize++;

        if(newRole >= 0) {
            addToDoRec(current_node, r_box.getRoleByIndex(newRole).getRange(), D, wh, null, -1);
            if(t_box.getRuleGraph().isDataNode(Math.abs(r_box.getRoleByIndex(newRole).getRange()))) {
                isDataVertice = true;
            }
            for(int it: r_box.getRoleByIndex(newRole).getInvRoles()) {
                current_node.addToDo(r_box.getRoleByIndex(it).getDomain(), new DSet(D), wh, t_box);
                if(t_box.getRuleGraph().isDataNode(Math.abs(r_box.getRoleByIndex(it).getDomain()))) {
                    isDataVertice = true;
                }
                parent.addToDo(r_box.getRoleByIndex(it).getRange(), new DSet(D), wh, t_box);
            }
        }
        
        DSet dd = null;
        if(D == null) dd = new DSet(); else dd = new DSet(D);
        if(todo != 0) { //set to individ some rule
            addToDoRec(current_node, todo, dd, wh, null, -1);
        }

        addToDoRec(current_node, t_box.getMetaConstraint(), new DSet(), wh, null, -1);
        
        current_node.createdBy = todo;
        current_node.isDataTypeVertice = isDataVertice;
        return current_node;
    }

    /**
     * Метод осуществляет раскрытие AND-правила
     * @return Возвращает значение ложь при обнаружении противоречия при раскрытии, иначе возвращает истина
     */
    private boolean performAnd() {
        stats.andAdd();
        InterpretationNode current_node = queue[IndividID];
        int c_id = current_node.getToDo()[toDoIndex];

        /*for(int i = 0; i < t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildren().size(); i++) {
            int it = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildren().get(i);
            if(c_id < 0) {
                if(current_node.isConflict(-it)) {
                    DSet new_d_set = new DSet(current_node.getToDoDSet()[toDoIndex]);
                    new_d_set.mergeWith(current_node.getConflictDSet(-it));
                    getRecentLevel(new_d_set);
                    return false;
                }
            } else {
                if(current_node.isConflict(it)) {
                    DSet new_d_set = new DSet(current_node.getToDoDSet()[toDoIndex]);
                    new_d_set.mergeWith(current_node.getConflictDSet(it));
                    getRecentLevel(new_d_set);
                    return false;
                }
            }
        }*/
        
        int cnt = 0;
        for(int i = 0; i < t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildrenSize(); i++) {
            int it = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildren()[i];
            if(c_id < 0) {
                DSet d = new DSet();
                d.mergeWith(current_node.getToDoDSet()[toDoIndex]);
                if(current_node.addToDoPos(-it, d, BTSize, toDoIndex + cnt + 1)) cnt++;
            } else {
                DSet d = new DSet();
                d.mergeWith(current_node.getToDoDSet()[toDoIndex]);
                if(current_node.addToDoPos( it, d, BTSize, toDoIndex + cnt + 1)) cnt++;
            }
        }
        return true;
    }
    
    /**
     * Метод осуществляет немедленную фильтрацию всех todo находящихся в ToDoList каждого из индивидов.
     * Реализует эвристический метод фильтрации для ускорения поиска при проверке согласованности для некоторых случаев.
     * @param concept_id Определяет номер правила в лесу ограничений
     * @param D Определяет множество зависимостей текущего todo
     */
    private void cutOff(int concept_id, DSet D) {
        InterpretationNode crnd = queue[IndividID];
        for(int i = toDoIndex + 1; i < crnd.getToDoSize(); i++) {
            int c_id = crnd.getToDo()[i];
            if(t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildrenSize() != 2) continue;
            if(t_box.getRuleGraph().getNode(Math.abs(c_id)).getNodeType() == NodeType.ntOR && c_id > 0 || 
               t_box.getRuleGraph().getNode(Math.abs(c_id)).getNodeType() == NodeType.ntAND && c_id < 0) {
                int it1 = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildren()[0];
                int it2 = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildren()[1];
                
                if(c_id < 0) {
                    it1 = -it1;
                    it2 = -it2;
                }
                DSet new_D = new DSet(D);
                
                if(use_a_checker && a_checker.isDisjoint(it1, concept_id, crnd)) {
                    new_D.mergeWith(crnd.getToDoDSet()[i]);
                    crnd.addToDoPos(it2, new_D, BTSize, toDoIndex + 1);
                } else {
                    if(use_a_checker && a_checker.isDisjoint(it2, concept_id, crnd)) {
                        new_D.mergeWith(crnd.getToDoDSet()[i]);
                        crnd.addToDoPos(it1, new_D, BTSize, toDoIndex + 1);
                    }
                }
            }
        }
    }
    
    /**
     * Метод определяет содержит ли текущий рассматриваемый индивид одну из альтернатив OR-правила.
     * Если одна из альтернатив уже содержится в множестве toDo индвида то такое OR-правило можно вообще не раскрывать.
     * @param c_id Определяет идентификатор концепта в лесу ограничений
     * @return Возвращает истина, если альтернатива содержится в toDoList текущего индивида.
     */
    private boolean isOrContain(int c_id) {
        InterpretationNode current_node = queue[IndividID];
        int n = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildrenSize();
        for(int j = 0; j < n; j++) {
            int it = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildren()[j];
            if(c_id < 0) it = -it;
            if(current_node.isContain(it)) return true;
            if((t_box.getRuleGraph().getNode(Math.abs(it)).getNodeType() == NodeType.ntOR  && it > 0) ||
               (t_box.getRuleGraph().getNode(Math.abs(it)).getNodeType() == NodeType.ntAND && it < 0)) {
                if(isOrContain(it)) return true;
            }
        }
        return false;
    }
    
    /**
     * Метод осуществляет раскрытие OR-правила.
     * При раскрытии используется проверка на пересекаемость реализованная в классе {@link AChecker}. При обнаружении 
     * противоречия одной альтернативы с каким-либо элементов toDoList рассматриваемого индивида такая 
     * альтернатива пропускается.
     * @return Возвращает значение ложь при обнаружении противоречия при раскрытии, иначе возвращает истина
     */
    private boolean performOr()
    {
        stats.orAdd();
        
        InterpretationNode current_node = queue[IndividID];
        boolean justAdded = false;
        if(curOr == 0) justAdded = true;
        int c_id = current_node.getToDo()[toDoIndex];
        DSet d = new DSet(current_node.getToDoDSet()[toDoIndex]);
        if(!justAdded) {
            d.mergeWith(curDS);
        }
        if(curDS != null) curDS.clear();
        
        int n = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildrenSize();
    
        //if(isOrContain(c_id)) return true;
        
        for(int j = 0; j < n; j++) {
            int it = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildren()[j];
            if(c_id < 0) it = -it;
            if(current_node.isContain(it)) return true;
        }
        
        for(int j = curOr; j < n; j++) {
            int it = t_box.getRuleGraph().getNode(Math.abs(c_id)).getChildren()[j];
            if(c_id < 0) it = -it;
            if(current_node.isConflict(it)) {
                d.mergeWith(current_node.getConflictDSet(it));
                continue;
            }
            
            ////////////////////////CHECK DISJOINTNESS//////////////////////////
            if(use_a_checker) {
                boolean is_cont = false;

                for(int i = toDoIndex + 1; i < current_node.getToDoSize(); i++)
                    if(a_checker.isDisjoint(current_node.getToDo()[i], it, current_node)) {
                        d.mergeWith(current_node.getToDoDSet()[i]);
                        is_cont = true;
                        break;
                    }

                if(!is_cont) {
                    for(int i = 0; i < current_node.getFAllSize(); i++) {
                        if(a_checker.isDisjoint(current_node.getFAll()[i], it, current_node)) {
                            d.mergeWith(current_node.getDSet(current_node.getFAll()[i]));
                            is_cont = true;
                            break;
                        }
                    }
                }

                if(!is_cont) {
                    for(int i = 0; i < current_node.getSomeSize(); i++) {
                        if(a_checker.isDisjoint(current_node.getIncr()[i], it, current_node)) {
                            d.mergeWith(current_node.getDSet(current_node.getIncr()[i]));
                            is_cont = true;
                            break;
                        }
                    }
                }

                if(is_cont) {
                    continue;
                }
            }
            ////////////////////////////////////////////////////////////////////
            
            //Choose one alternative
            remember(j, n, justAdded, d, null);
            current_node.addToDoPos(it, d, BTSize, toDoIndex + 1);
            curOr = 0;
            return true;
        }
        //remember(n, n, justAdded, d);
        getRecentLevel(d);
        return false;
    }
    
    /**
     * Метод осуществляет добавление концепта к рассматриваемому индивиду.
     * В данном методе осуществляется добавление всех аксиом, связанных с рассматриваемым концептом. 
     * Это касается аксиом вида: A = C, A [= C, -A [= C.
     * @return Возвращает значение ложь при обнаружении противоречия при раскрытии, иначе возвращает истина.
     */
    private boolean performAddConcept()
    {
        stats.conceptAdd();
        
        InterpretationNode current_node = queue[IndividID];
        int c_id = current_node.getToDo()[toDoIndex];//current node id
        
        int c_ds = t_box.getRuleGraph().getNode(Math.abs(c_id)).getSubDescription();//current node description
        int n_ds = t_box.getRuleGraph().getNode(Math.abs(c_id)).getNegativeDescription();//current node description
        int eq_ds = t_box.getRuleGraph().getNode(Math.abs(c_id)).getDescription();
        
        if(current_node.isConflict(c_id)) { //There is some clash!
            DSet new_d_set = new DSet(current_node.getToDoDSet()[toDoIndex]);
            new_d_set.mergeWith(current_node.getConflictDSet(c_id));
            getRecentLevel(new_d_set);
            return false;
        }

        DSet d = new DSet();
        //Positive lazy unfolding
        if(!current_node.isContain(c_ds)) { //This node haven't this concepts description
            if(c_ds != 0 && c_id > 0) {
                d.mergeWith(current_node.getToDoDSet()[toDoIndex]);
                current_node.addToDo(c_ds, d, BTSize, t_box);
            }
        }

        //Negative lazy unfolding //WORKS TOO SLOW!
        if(!current_node.isContain(n_ds)) { //This node haven't this concepts negative description
            if(n_ds != 0 && c_id < 0) {
                d.mergeWith(current_node.getToDoDSet()[toDoIndex]);
                current_node.addToDo(n_ds, d, BTSize, t_box);
            }
        }
        
        if(!current_node.isContain(eq_ds)) { //This node haven't this concepts description
            if(eq_ds != 0) {
                if(c_id > 0) { //if concept isn't complement - then add its description to ToDo List
                    d.mergeWith(current_node.getToDoDSet()[toDoIndex]);
                    current_node.addToDo(eq_ds, d, BTSize, t_box);
                } else {
                    //if concept C is named concept and have negation then add description or negation of its description to TODO
                    if(t_box.getRuleGraph().getNode(Math.abs(c_id)).isNamed()) { //this concept have equivalence
                        //if adding concept is negation concept - then we negate its equivalence
                        for(int i = 0; i < t_box.getRuleGraph().getNode(eq_ds).getChildrenSize(); i++) { //eq_ds must be positive AND-veritce
                            int nodeIndex = -t_box.getRuleGraph().getNode(eq_ds).getChildren()[i];
                            DSet cDs = new DSet(d);
                            cDs.mergeWith(current_node.getToDoDSet()[toDoIndex]);
                            current_node.addToDo(nodeIndex, cDs, BTSize, t_box);
                        }
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Метод осуществляет проверку того, является ли концепт подмножеством какого-либо концепта из множества меток.
     * @param concept Определяет номер концепта в лесу ограничений
     * @param label Определяет множество меток вершины
     * @param label_size Определяет размер множества меток
     * @return Возвращает истина если хотя бы одна из меток является над концептом рассматриваемого
     */
    private boolean isStraightSubsmed(int concept, int[] label, int label_size) //straight subsumption checking
    {
        if(use_a_checker) {
            for(int i = 0; i < label_size; i++) {
                if(a_checker.isDisjoint(-concept, label[i], null)) { //concept [= C1 and C2 and ... and CN
                    return true;
                }
            }
        }
        return false;
    }
    
    //add SOME-ruels and >= - rules
    /**
     * Метод осуществляет добавление индивида в интерпретацию.
     * В данном методе осуществляется определение количества индивидов, которое будет добавлено в интерпретацию,
     * так как метод вызывается при обработке ограничений кардинальности и квантора существования.
     * Одновременно с этим осуществляется добавление домена и области значений.
     * Также определяются индивиды соответствующие концепту под квантором и они не добавляются 
     * интерпретацию.
     * @return Возвращает истина, если при добавлении индивида не возникает явного противоречия и ложь в противном случае.
     */
    private boolean performIncrease() {
        stats.maxAdd();
        int hlp = queue[IndividID].getToDo()[toDoIndex];
        RuleNode tmp_rn = t_box.getRuleGraph().getNode(Math.abs(hlp));

        int td = 1;
        if(tmp_rn.getChildrenSize() > 0)
            td = tmp_rn.getChildren()[0];
        
        if(hlp < 0 && (tmp_rn.getNodeType() == NodeType.ntALL || tmp_rn.getNodeType() == NodeType.ntSOME)) td *= -1;
        if(td == -1) td = 1;
        int n = 1;
        if(tmp_rn.getNodeType() == NodeType.ntEXTCARD || tmp_rn.getNodeType() == NodeType.ntMINCARD || tmp_rn.getNodeType() == NodeType.ntMAXCARD) {
            n = tmp_rn.getNumberRestriction();
            if(hlp < 0) n++;
        }
        if(n == 0) {
            return true;
        }
        
        int dom = r_box.getRoleByIndex(t_box.getRuleGraph().getNode(Math.abs(queue[IndividID].getToDo()[toDoIndex])).getRoleType()).getDomain();
        int rng = r_box.getRoleByIndex(t_box.getRuleGraph().getNode(Math.abs(queue[IndividID].getToDo()[toDoIndex])).getRoleType()).getRange();
        if(dom > 1) {
            DSet d = new DSet(queue[IndividID].getToDoDSet()[toDoIndex]);
            queue[IndividID].addToDo(dom, d, BTSize, t_box);
        }

        boolean idv = false;
        boolean add_nv = true;
        if(r_box.getRoleByIndex(tmp_rn.getRoleType()).isDataRole())
            idv = true;
        
        if( t_box.getRuleGraph().getNode(Math.abs(td)).getDatatype() != null || 
            t_box.getRuleGraph().getNode(Math.abs(td)).getFacet() != null || 
            t_box.getRuleGraph().getNode(Math.abs(td)).getLiter() != null) {
            idv = true;
        }
        
        if(r_box.getRoleByIndex(tmp_rn.getRoleType()).isFunctional()) {
            if(n > 1) {
                DSet new_d_set = new DSet(queue[IndividID].getToDoDSet()[toDoIndex]);
                getRecentLevel(new_d_set);
                return false;
            } else {
                for(int i = 0; i < queue[IndividID].getChildSize(); i++) {
                    boolean bl = r_box.hasCommonFuncAllRoles(queue[IndividID].getChildren()[i].getRoles(), tmp_rn.getRoleType());
                    for(int j = 0; j < queue[IndividID].getChildren()[i].getRoles().length; j++) {
                        int rit = queue[IndividID].getChildren()[i].getRoles()[j];
                        if(r_box.isSubOrEqual(rit, tmp_rn.getRoleType()) || bl) {
                            add_nv = false;
                            queue[IndividID].getChildren()[i].getNode().addToDo(td, new DSet(queue[IndividID].getToDoDSet()[toDoIndex]), BTSize, t_box);
                            if(!queue[IndividID].getChildren()[i].contains(tmp_rn.getRoleType()))
                                queue[IndividID].getChildren()[i].addNewRole(tmp_rn.getRoleType());
                            break;
                        }
                    }
                    if(!add_nv)
                        break;
                }
            }
        }
        
        if(add_nv) {
            //how many children with the same concept
            int children_same_concept = 0;
            for(int i = 0; i < queue[IndividID].getChildSize(); i++) {
                boolean is_subrole = false;
                for(int j = 0; j < queue[IndividID].getChildren()[i].getRoles().length; j++) {
                    if(r_box.isSubOrEqual(tmp_rn.getRoleType(), queue[IndividID].getChildren()[i].getRoles()[j])) { //if role of some child is sub role of current role
                        is_subrole = true; break;
                    }                        
                }
                if(is_subrole) {
                    boolean is_subsumed = queue[IndividID].getChildren()[i].getNode().isContain(td) || (td == 1) || 
                            isStraightSubsmed(td, 
                                      queue[IndividID].getChildren()[i].getNode().getToDo(), 
                                      queue[IndividID].getChildren()[i].getNode().getToDoSize());
                    if(is_subsumed) {
                        children_same_concept++;
                    }
                }
            }
            for(int i = 0; i < queue[IndividID].getParentSize(); i++) {
                boolean is_subrole = false;
                for(int j = 0; j < queue[IndividID].getParents()[i].getRoles().length; j++) {
                    if(r_box.isReverseSubOrEqual(tmp_rn.getRoleType(), queue[IndividID].getParents()[i].getRoles()[j])) { //if role of some child is sub role of current role inverse
                        is_subrole = true; break;
                    }                        
                }
                if(is_subrole) {
                    boolean is_subsumed = queue[IndividID].getParents()[i].getNode().isContain(td) || (td == 1) || 
                            isStraightSubsmed(td, 
                                      queue[IndividID].getParents()[i].getNode().getToDo(), 
                                      queue[IndividID].getParents()[i].getNode().getToDoSize());
                    if(is_subsumed) {
                        children_same_concept++;
                    }
                }
            }
            if(n - children_same_concept > 0) {
                for(int i = 0; i < (n - children_same_concept); i++) {
                    stats.someAdd();
                    InterpretationNode new_child = addNewIndivid(queue[IndividID], tmp_rn.getRoleType(), td, new DSet(queue[IndividID].getToDoDSet()[toDoIndex]), IndividID + 1, idv);
                    new_child.createdBy = hlp;
                    new_child.addToDo(rng, new DSet(queue[IndividID].getToDoDSet()[toDoIndex]), BTSize, t_box);
                    //add range of all super properties and equivalent properties
                    IntArray ranges_sup_properties = r_box.getAllRanges(tmp_rn.getRoleType());
                    for(int j = 0; j < ranges_sup_properties.size(); j++) {
                        new_child.addToDo(ranges_sup_properties.get(j), new DSet(queue[IndividID].getToDoDSet()[toDoIndex]), BTSize, t_box);                            
                    }
                    ranges_sup_properties = null;

                    IntArray domains_sup_properties = r_box.getAllDomains(tmp_rn.getRoleType());
                    domains_sup_properties = r_box.getAllDomains(tmp_rn.getRoleType());
                    for(int j = 0; j < domains_sup_properties.size(); j++) {
                        queue[IndividID].addToDo(domains_sup_properties.get(j), new DSet(queue[IndividID].getToDoDSet()[toDoIndex]), BTSize, t_box);
                    }
                    domains_sup_properties = null;
                }
                queue[IndividID].addIncr(queue[IndividID].getToDo()[toDoIndex], BTSize);
            }
        }
        return true;
    }

    /**
     * Метод осуществляет добавление FORALL-правила для текущего индивида и записывает его в FORALL-очередь.
     */
    private void performAll() {
        queue[IndividID].addFAll(queue[IndividID].getToDo()[toDoIndex], BTSize);
    }
    
    //add <= - rules
    /**
     * Метод осуществляет добавление <=-правила для текущего индивида и записывает его в <=-очередь.
     */
    private void performDecrease() {
        queue[IndividID].addMQfd(queue[IndividID].getToDo()[toDoIndex], new DSet(queue[IndividID].getToDoDSet()[toDoIndex]), BTSize);
    }
    
    /**
     * Метод определяет новое распределение отрицания концепта C для текущего правила Qualified Restriction
     */
    private void nextChoose()
    {
        int el = 1;
        if(curChoose.notC.size() > 0) {
            el = curChoose.notC.get(curChoose.notC.size() - 1);
            curChoose.notC.remove(curChoose.notC.size() - 1);
        }
        for(int i = 0; i < ai.size(); i++)
            if(ai.get(i) > el)
                curChoose.notC.add(ai.get(i));
    }
    
    /**
     * В данном методе определяется могут ли быть объединены два индивида.
     * Данная проверка включает: содержат ли toDoList заданных индивидов контрарные значения,
     * содержаться ли среди предков неперескающиеся роли ведущие к индивидам, проверяется 
     * MINCARD-правило,
     * @param anc Определяет предка заданных индивидов.
     * @param n1 Определяет первого индивида для слияния.
     * @param n2 Определяет второго индивида для слияния.
     * @param D Определяет множество зависимостей в которое добавляется значения, от которых зависят концепты при слиянии.
     * @return Возвращает истина, если два индивида могут быть объединены и ложь в противном случае.
     */
    private boolean canMerge(InterpretationNode anc, InterpretationNode n1, InterpretationNode n2, DSet D) {
        //проверка на то что выполнено >= правило: прверяем, если оба индивида были созданы
        //по одному и тому же >= правилу, то их нельзя объединять, так как они создаются
        //в минимальном количестве.
        if(n1.createdBy == n2.createdBy && n1.createdBy != 0) {
            return false;
        }

        if(n1.indexInQueue == n2.indexInQueue) {
            return false;
        }
        if(n1.isDataTypeVertice && !n2.isDataTypeVertice || !n1.isDataTypeVertice && n2.isDataTypeVertice) {
            D.addValue(whqueue[n1.indexInQueue]);
            D.addValue(whqueue[n2.indexInQueue]);
            return false;
        }
        //проверка на то что ToDoList не пересекается
        for(int i = 0; i < n1.getToDoSize(); i++)
            for(int j = 0; j < n2.getToDoSize(); j++) {
                if(n1.getToDo()[i] == -n2.getToDo()[j]) {
                    D.mergeWith(n1.getToDoDSet()[i]);
                    D.mergeWith(n2.getToDoDSet()[j]);
                    return false;
                }
                //моя проверка на непересекаемость
                if(use_a_checker && a_checker.isDisjoint(n1.getToDo()[i], n2.getToDo()[j], queue[IndividID])) {
                    D.mergeWith(n1.getToDoDSet()[i]);
                    D.mergeWith(n2.getToDoDSet()[j]);
                    return false;
                }
            }

        //проверка на то что предки не могут быть объединены, так как у них один и тот же предок и от него идут не пересекающиеся роли
        for(int i = 0; i < n1.getParentSize(); i++)
            for(int j = 0; j < n2.getParentSize(); j++)
                if(n1.getParents()[i].getNode() == n2.getParents()[j].getNode())
                    if(r_box.haveDisjoint(n1.getParents()[i].getRoles(), n2.getParents()[j].getRoles())) {
                        //Make something with DSet
                        return false;
                    }

        return true;
    }
    
    /**
     * Метод объединяет двух индивидов.
     * В метод осуществляется объединение двух индивидов, при этом все данные второго индивида
     * добавляются к соответствующим полям первого индивида. Это касается всех предков, потомков, элементов toDoList,
     * и массивов конкретных ограничений по правилам: FORALL, SOME.
     * Для второго индивида устанавливается параметр isSkipped = false, для того, чтобы не рассматривать его 
     * при дальнейшей обработке.
     * @param n1 Определяет первого индивида для слияния, с которым будет объединен второй индивид.
     * @param n2 Определяет второго индивида.
     * @param d Определяет множество зависимостей содержащее текущий 
     */
    private void merge(InterpretationNode n1, InterpretationNode n2, DSet d)
    {
        if(n1.indexInQueue == n2.indexInQueue) { //need not to merge same individs
            return;
        }
        if(n1.getIndsSize() < n2.getIndsSize()) {
            merge(n2, n1, d);
            return;
        }
        //merge ancestors
        for(int i = 0; i < n2.getParentSize(); i++) {
            for(int j = 0; j < n2.getParents()[i].getRoles().length; j++) {
                n1.addParent(n2.getParents()[i].getNode(), n2.getParents()[i].getRoles()[j], BTSize, r_box);
                n2.getParents()[i].getNode().addChild(n1, n2.getParents()[i].getRoles()[j], BTSize);
                n1.updateParents(n2.getParents()[i].getRoles()[j], BTSize, r_box);
            }
        }

        //merge children
        for(int i = 0; i < n2.getChildSize(); i++) {
            for(int j = 0; j < n2.getChildren()[i].getRoles().length; j++) {
                n1.addChild(n2.getChildren()[i].getNode(), n2.getChildren()[i].getRoles()[j], BTSize);
            }
        }

        //merge ToDo
        if( newIndividID > n1.indexInQueue) {
            newIndividID = n1.indexInQueue;
        }
        
        for(int i = 0; i < n2.getToDoSize(); i++) {
            if(n1.isContain(n2.getToDo()[i])) continue;
            DSet q = new DSet(n2.getToDoDSet()[i]);
            q.mergeWith(d);
            n1.addToDo(n2.getToDo()[i], q, BTSize, t_box);
        }
        
        //all will be added on todo processing
        //merge individs
        for(int i = 0; i < n2.getIndsSize(); i++) {
            //n1.addIndivid(n2.getInds()[i], BTSize);
            //a_box.setNode(a_box.find(n2.getInds()[i]), n1);
        }
        //merge forall
        for(int i = 0; i < n2.getFAllSize(); i++) {
            n1.addFAll(n2.getFAll()[i], BTSize);
        }
        //merge some
        for(int i = 0; i < n2.getSomeSize(); i++) {
            n1.addIncr(n2.getIncr()[i], BTSize);
        }
        //merge mqfd
        for(int i = 0; i < n2.getMQfdSize(); i++) {
            n1.addMQfd(n2.getMQfd()[i], n2.getMxQfDSet()[i], BTSize);
        }
        
        //this node need not to process, it merged with n1 - skip it
        n2.setSkip(true);
    }
    
    /**
     * В данном методе проверяется возможность добавления заданного концепта к определенному индивиду.
     * @param tNode Индивид интерпретации.
     * @param cIndex Концепт для проверки.
     * @return Возвращает истина, если заданный концепт может быть добавлен к вершине интерпретации.
     */
    private boolean canAddConcept(InterpretationNode tNode, int cIndex) {
        for(int i = 0; i < tNode.getToDoSize(); i++) {
            if(tNode.getToDo()[i] == -cIndex) {
                //System.out.println("LOOSE! " + cIndex + " " + tNode.getToDo()[i]);
                return false;
            }
            if(use_a_checker) {
                if(a_checker.isDisjoint(tNode.getToDo()[i], cIndex, tNode)) return false;
            }
        }
        return true;
    }
    
    /**
     * В данном методе осуществляется сокращение количества потомков индивида за счет 
     * их объединения.
     * Если правило кардинальности имеет под квантором концепт отличный от общего концепта,
     * то осуществляется назначение этого концепта некоторым индивидам и его отрицание оставшимся.
     * Процесс объединения индивидов сделан в двух различных алгоритмах: с использованием
     * алгоритма Прима и прямым перебором.
     * @return Возвращает истина, если количство потомков может быть сокращено за счет 
     * объединения некоторых индивидов до заданного значения и ложь в противном случае.
     */
    private boolean makeDecrease() {
        InterpretationNode current_node = queue[IndividID];
        RuleNode rn = t_box.getRuleGraph().getNode(Math.abs(current_node.getMQfd()[queue[IndividID].currentMQfd]));
        ArrayList<InterpretationNode> ain = current_node.getNeighboursByRole(rn.getRoleType(), r_box); //"ain" contain all R-neighbours
        DSet D = new DSet(current_node.getMxQfDSet()[queue[IndividID].currentMQfd]);

        int n = rn.getNumberRestriction();
        for(int i = 0; i < ain.size(); i++) {
            if(ain.get(i).isSkipped()) {
                n++;
            }
        }
        if(current_node.getMQfd()[queue[IndividID].currentMQfd] < 0) n--;
        
        int C = 1;
        if(rn.getChildrenSize() != 0)
            C = rn.getChildren()[0];
        if(t_box.getRuleGraph().getNode(C).getDatatype() != null) {
            if(t_box.getRuleGraph().getNode(C).getDatatype().isBuiltIn()) if(t_box.getRuleGraph().getNode(C).getDatatype().getBuiltInDatatype() == OWL2Datatype.RDFS_LITERAL) {
                C = 1;
            }
        }

        if(C != 1) {
            //System.out.print(ain.size() + " ");
            if(curChoose != null) {
                //System.out.print(curChoose.notC.toString());
            }
            //System.out.println();
            //System.out.println("Q LETTER!");
        }
        boolean justAdded = (curChoose == null);
        if(!justAdded) {
            int kor = 123;
        }

        if(C != 1) { //there is a Qualified number restriction!
            if(st1 == 0 && st2 == 1) { //somewhere was a clash in choose rule or it is a first choose rule step: не нужно проводить choose если у нас клэш оказался при слиянии каких-то вершин
                //"ai" contains all numbers of all R-neighbours - ещё нужно запоминать и ai так как он хранит разные числа для <= правил 
                ai.clear();
                for(int i = 0; i < ain.size(); i++)
                    if(!ain.get(i).isContain(C))
                        ai.add(i);

                //make CHOICE - add C and -C concepts to all neighbours
                if(curChoose != null) {
                    nextChoose(); //делаем следующий выбор в который подставим -C
                } else {
                    curChoose = new ChooseState(new ArrayList<Integer>(), ai);
                    for(int i = 0; i < ai.size(); i++)
                        curChoose.notC.add(ai.get(i)); //если первый раз вызываем <= правило, тогда всем назначаем -C
                }
                
                //Делаем цикл пока не найдем удовлетворяющее распределение отрицания концепта C:
                boolean can_add = false;
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SatChecker.class.getName()).log(Level.SEVERE, null, ex);
                }*/
                //for(int i = 0; i < 10; i++)
                //    System.out.println();
                while(!can_add) {
                    can_add = true;
                    for(int i = 0; i < curChoose.notC.size(); i++) { //в этом цикле проверяем можно ли добавить -С там где это выбрано
                        if(!canAddConcept(ain.get(curChoose.notC.get(i)), -C)) {
                            can_add = false; break;
                        }
                    }
                    if(can_add) {
                        for(int i = 0; i < ai.size(); i++) { //а в этом цикле проверяем можно ли добавить C везде где это выбрано
                            if(!curChoose.notC.contains(ai.get(i))) {
                                if(!canAddConcept(ain.get(ai.get(i)), C)) {
                                    can_add = false; break;
                                }
                            }
                        }
                    }
                    if(!can_add) {
                        if(curChoose.notC.size() == 0) {
                            can_add = true;
                            DSet mqd = new DSet(D);
                            for(int i = 0; i < ain.size(); i++) {
                                if(!ain.get(i).isSkipped()) {
                                    if(whqueue[ain.get(i).indexInQueue] > 0) {
                                        mqd.addValue(whqueue[ain.get(i).indexInQueue]);
                                    }
                                }
                            }
                            mqd.mergeWith(current_node.getMxQfDSet()[queue[IndividID].currentMQfd]);
                            getRecentLevel(mqd);
                            justAddedLE = true;
                            return false;
                            //break;
                        }
                        nextChoose();
                        //System.out.println(curChoose.notC.toString());
                    }
                }

                DSet new_d_set = new DSet(D);
                remember(new ChooseState(curChoose.notC, curChoose.ai), 0, justAdded, new_d_set, null);

                for(int i = 0; i < curChoose.notC.size(); i++) { //в этом цикле делаем choose и добавляем везде -C где это выбрано
                    ain.get(curChoose.notC.get(i)).addToDo(-C, new_d_set, BTSize, t_box);
                }

                for(int i = 0; i < ai.size(); i++) { //а в этом цикле добавляем C везде где это выбрано
                    if(!curChoose.notC.contains(ai.get(i)))
                        ain.get(ai.get(i)).addToDo(C, new_d_set, BTSize, t_box);
                }
                //WE MADE THIS CHOICE!!
            } else {
                ai = new ArrayList<Integer>(curChoose.ai);
            }
        }
        
        //merge C-neighbours while there are >= n of its
        int count = ain.size(); //если появляется UNqualified number restriction, то в слиянии участвуют все потомки
        if(C != 1) count = ai.size() - curChoose.notC.size(); //count of R-neighbours with concept C
        
        boolean mustAdd = (false || (!justAddedLE));
        
        if(count <= n) { //если у нас теперь нужное количество R последователей, то выходим из циклов
            if(current_node.currentMQfd >= current_node.getMQfdSize())
                IndividID = newIndividID;
            return true;
        }

        if(!justAddedLE)
            D.mergeWith(curDS);
        if(curDS != null) curDS.clear();
        
        boolean use_qual_opt = experimentAttr;
        if(use_qual_opt) {
            InterpretationNode[] nodes2merge = new InterpretationNode[ain.size()];
            nodes2merge = ain.toArray(nodes2merge);
            IndividGraph g = new IndividGraph(nodes2merge, t_box);
            QualSolver Q = new QualSolver();
            if(!justAddedLE) {
                Q = currentQO;
            }
            QSet[] new_match = Q.getNewMatching(g, n);
            
            /*System.out.println(BTSize + " " + QSize + " " + IndividID + " " + toDoIndex + " NEED TO MERGE ON ROLE " + r_box.getRoleByIndex(rn.getRoleType()).getName() + ":");
            for(int i = 0; i < new_match.length; i++)
            {
                for(int j = 0; j < new_match[i].count; j++)
                {
                    System.out.print(new_match[i].a[j] + " ");
                }
                System.out.println();
            }*/

            DSet new_d_set = new DSet(D);
            boolean failed = false;
            for(int i = 0; i < new_match.length; i++) {
                for(int j = 1; j < new_match[i].count; j++) {
                    if(!canMerge(current_node, ain.get(new_match[i].a[0]), ain.get(new_match[i].a[j]), D)) {
                        failed = true; break;
                    }
                }
            }
            if(new_match.length <= n && !failed) {
                InterpretationNode[][] real_match = new InterpretationNode[new_match.length][32];
                for(int i = 0; i < new_match.length; i++)
                    for(int j = 0; j < new_match[i].count; j++)
                        real_match[i][j] = ain.get(new_match[i].a[j]);
                
                remember(new QSetPair(new_match, real_match), Boolean.FALSE, justAddedLE, new_d_set, Q);
                for(int i = 0; i < new_match.length; i++) {
                    for(int j = 1; j < new_match[i].count; j++) {
                        if(canMerge(current_node, ain.get(new_match[i].a[0]), ain.get(new_match[i].a[j]), D)) {
                            merge(ain.get(new_match[i].a[0]), ain.get(new_match[i].a[j]), new_d_set);                            
                        }
                    }
                }
                justAddedLE = true;
                Q = null;
                return true;
            } else {
                if(!justAddedLE)
                    BTStack[BTSize - 1].lastOption = Boolean.TRUE;
            }
        } else {
            for(int i = st1; i < ain.size(); i++, st1++, st2 = st1 + 1) {
                if(ain.get(i).isSkipped()) continue; //if this vertice is blocked or if this vertice is merged to another - skip it!
                if(C != 1) if(curChoose.notC.contains(i)) continue; //if i-th neighbour contains "-C" skip it!
                for(int j = st2; j < ain.size(); j++) {
                    if(ain.get(j).isSkipped()) continue; //if this vertice is blocked or if this vertice is merged to another - skip it!
                    if(C != 1) {
                        if(curChoose == null) {
                            int kor = 123;
                        }
                        if(curChoose.notC.contains(j)) continue;
                    } //if j-th neighbour contains "-C" skip it!

                    if(canMerge(current_node, ain.get(i), ain.get(j), D)) { //если можно слить вершины - сливаем их! - добавить сюда ещё проверку на >= правило!!!
                        DSet new_d_set = new DSet(D); //от чего зависит этот maxQfD (<= n) правило
                        if(mustAdd) {
                            new_d_set.addValue(BTSize); //от предыдущего также зависит новое состояние
                        }
                        remember(new IntPair(i, j), ain.size(), justAddedLE, new_d_set, null); //запоминаем текущее состояние
                        merge(ain.get(i), ain.get(j), new_d_set); //сливаем обе вершины
                        mustAdd = true; justAddedLE = true; //set it true for adding new BTStates

                        count--; //так как вершины слиты, значит у нас R последователей стало на 1 меньше
                        if(count <= n) { //если у нас теперь нужное количество R последователей, то выходим из циклов
                            st1 = 0; st2 = 1; //устанавливаем st1 = 0, st2 = 1 для новых ограничений кардинальности
                            //queue[0].show(r_box, t_box);
                            //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            return true;
                        }
                    }
                }
                //st2 = i + 2; //если второй цикл весь обратился, то st2 надо начинать на 1 больше чем i, поэтому для текущего надо поставить +2
            }
        }

        DSet mqd = new DSet(D);
        for(int i = 0; i < ain.size(); i++) {
            if(!ain.get(i).isSkipped()) {
                if(whqueue[ain.get(i).indexInQueue] > 0) {
                    mqd.addValue(whqueue[ain.get(i).indexInQueue]);
                }
            }
        }
        mqd.mergeWith(current_node.getMxQfDSet()[queue[IndividID].currentMQfd]);
        getRecentLevel(mqd);
        //ain = current_node.getNeighboursByRole(rn.getRoleType(), r_box);
        justAddedLE = true;
        return false;
    }
    
    /**
     * В методе осуществляется индукционное добавление элементов AND-правила.
     * @param node Определяет индивида к которому применяется AND-правило.
     * @param toDoEntry Определяет вершину леса ограничений, соответствующую AND-правилу
     * @param D Определяет множество зависимостей текущего правила
     * @param when Определяет размер стека на котором осуществляется раскрытие AND-правила
     * @param par Определяет предка текущей рассматриваемой вершины
     * @param role Определяет роль которая связывает предка и текущего индивида
     */
    private void addToDoRec(InterpretationNode node, int toDoEntry, DSet D, int when, InterpretationNode par, int role)
    {
        if(t_box.getRuleGraph().isDataNode(Math.abs(toDoEntry))) {
            node.isDataTypeVertice = true;
        }
        if(t_box.getRuleGraph().getNode(Math.abs(toDoEntry)).getNodeType() == NodeType.ntAND && toDoEntry > 0 || 
           t_box.getRuleGraph().getNode(Math.abs(toDoEntry)).getNodeType() == NodeType.ntOR && toDoEntry < 0) {
            for(int i = 0; i < t_box.getRuleGraph().getNode(Math.abs(toDoEntry)).getChildrenSize(); i++) {
                int it = t_box.getRuleGraph().getNode(Math.abs(toDoEntry)).getChildren()[i];
                if(toDoEntry < 0) it = -it;
                addToDoRec(node, it, new DSet(D), when, par, role);
            }
        } else  {
            if(par != null) {
                node.addParent(par, role, when, r_box);
                node.updateParents(role, when, r_box);
            }
            node.addToDo(toDoEntry, new DSet(D), when, t_box);
            if( newIndividID > node.indexInQueue) //если что-то было добавлено в очередь ранее чем текущая рассматриваемая вершина, то указатель переставляется на неё
                newIndividID = node.indexInQueue;
        }
    }
    
    /**
     * В методе осуществляет применение всех правил кванторов всеобщности.
     * Если квантор всеобщности применяется для блокированной вершины, то она разблокируется
     * и снова проверяется её блокировка.
     */
    private void makeIncrease() //после того как были добавлены все потомки, выполняем for all правила //add hierarchy of roles
    {
        InterpretationNode current_node = queue[IndividID];
        boolean cycle_just_added = false;
        for(; current_node.currentFAll < current_node.getFAllSize(); current_node.currentFAll++) {
            int i = current_node.currentFAll;
            int it = current_node.getFAll()[i];
            for(int j = 0; j < current_node.getChildSize(); j++) {
                Couple jt = current_node.getChildren()[j];
                RuleNode t = t_box.getRuleGraph().getNode(Math.abs(it));

                if(!r_box.isSubOrEqualAll(jt.getRoles(), t.getRoleType())) continue;
                
                DSet D = new DSet(current_node.getToDoDSet()[current_node.findInToDo(it)]);
                
                int td = t.getChildren()[0];
                if(it < 0) td *= -1;
                
                stats.allAdd();
                addToDoRec(jt.getNode(), td, new DSet(D), BTSize, current_node, t.getRoleType()); //and вершина раскрывается сразу

                if(r_box.getRoleByIndex(t.getRoleType()).isTransitive()) { //если роль транзитивна, то необходимо добавить тоже самое FORALL-rule
                    for(int k = 0; k < jt.getRoles().length; k++) {
                        if(r_box.isEqual(jt.getRoles()[k], t.getRoleType())) {
                            if(jt.getNode().isContain(it) && !cycle_just_added) {
                                //System.out.println("CYCLE FOUND ON ROLE: " + r_box.getRoleByIndex(t_box.getRuleGraph().getNode(Math.abs(it)).getRoleType()).getName());
                                //System.out.println(queue[IndividID].getInds()[0] + " " + jt.getNode().getInds()[0]);
                            }
                            cycle_just_added = true;
                            addToDoRec(jt.getNode(), it, new DSet(D), BTSize, current_node, jt.getRoles()[k]);                            
                        } else if(r_box.isSub(jt.getRoles()[k], t.getRoleType())) {
                            addToDoRec(jt.getNode(), t.getTransByRole(t_box, jt.getRoles()[k]), new DSet(D), BTSize, current_node, jt.getRoles()[k]);
                        }
                    }
                }
                graph.unBlock(jt.getNode());
                graph.makeBlocked(jt.getNode(), queue, IndividID);
                if( newIndividID > jt.getNode().indexInQueue) { //если что-то было добавлено в очередь ранее чем текущая рассматриваемая вершина, то указатель переставляется на неё
                    newIndividID = jt.getNode().indexInQueue;
                }
            }

            //inverse roles
            for(int j = 0; j < current_node.getParentSize(); j++) {
                Couple jt = current_node.getParents()[j];
                RuleNode t = t_box.getRuleGraph().getNode(Math.abs(it));

                if(!r_box.isReverseSubOrEqualAll(jt.getRoles(), t.getRoleType())) continue;
                
                DSet D = new DSet(current_node.getToDoDSet()[current_node.findInToDo(it)]);
                
                int td = t.getChildren()[0];
                if(it < 0) td *= -1;
                
                stats.allAdd();
                addToDoRec(jt.getNode(), td, new DSet(D), BTSize, current_node, t.getRoleType()); //and вершина раскрывается сразу
                if(r_box.getRoleByIndex(t.getRoleType()).isTransitive()) { //если роль транзитивна, то необходимо добавить тоже самое FORALL-rule
                    for(int k = 0; k < jt.getRoles().length; k++) {
                        if(r_box.isEqual(jt.getRoles()[k], t.getRoleType())) {
                            addToDoRec(jt.getNode(), it, new DSet(D), BTSize, current_node, jt.getRoles()[k]);                            
                        } else
                        if(r_box.isSub(jt.getRoles()[k], t.getRoleType())) {
                            addToDoRec(jt.getNode(), t.getTransByRole(t_box, jt.getRoles()[k]), new DSet(D), BTSize, current_node, jt.getRoles()[k]);
                        }
                    }
                }
                graph.unBlock(jt.getNode());
                graph.makeBlocked(jt.getNode(), queue, IndividID);
                if( newIndividID > jt.getNode().indexInQueue) { //если что-то было добавлено в очередь ранее чем текущая рассматриваемая вершина, то указатель переставляется на неё
                    newIndividID = jt.getNode().indexInQueue;
                }
            }
        }
    }
    
    /**
     * Метод осуществляет слияние индивида ABox с текущим рассматриваемым индивидом.
     * @return Возвращает истина если хотя бы одна из меток является надконцептом рассматриваемого концепта и ложь в противном случае.
     */
    private boolean performAddIndivid() {
        int c_id = queue[IndividID].getToDo()[toDoIndex];
        DSet cds = new DSet();
        cds.mergeWith(queue[IndividID].getToDoDSet()[toDoIndex]);
        int rn = t_box.getRuleGraph().getNode(Math.abs(queue[IndividID].getToDo()[toDoIndex])).getIndividNumber();
    
        if(c_id > 0) {
            int inq = a_box.getNode(rn).indexInQueue;
            if(inq != -1) {
                merge(queue[IndividID], queue[a_box.getNode(rn).indexInQueue], cds);
            } else {
                for(int i = 0; i < a_box.getNode(rn).getToDoSize(); i++) {
                    queue[IndividID].addToDoPos(a_box.getNode(rn).getToDo()[i], cds, BTSize, toDoIndex + 1);
                }
                a_box.getNode(rn).indexInQueue = queue[IndividID].indexInQueue;
            }
        }
        return true;
    }
    
    /**
     * В методе осуществляется раскрытие REFLEX-правила
     * @return Возвращает истина если хотя бы одна из меток является надконцептом рассматриваемого концепта и ложь в противном случае.
     */
    private boolean performAddSelf() {
        //TODO
        return true;
    }

    /**
     * В данном методе определяется тип правила, которое раскрывается в данный момент 
     * и вызывается соответствующий метод для её обработки.
     * @return Возвращает ложь если при добавлении очередного правила возникает явное противоречие и истина в противном случае.
     */
    private boolean updateToDo() {
        //System.out.println(IndividID + " " + toDoIndex + " " + BTSize);
        //if(IndividID > 2000) return false;
        int c_id = queue[IndividID].getToDo()[toDoIndex];
        NodeType c_nt = t_box.getRuleGraph().getNode(Math.abs(c_id)).getNodeType();
        
        //cutOff(c_id, queue[IndividID].getToDoDSet()[toDoIndex]);
        
        if(queue[IndividID].isConflict(c_id)) {
            DSet new_d_set = new DSet(queue[IndividID].getToDoDSet()[toDoIndex]);
            new_d_set.mergeWith(queue[IndividID].getConflictDSet(c_id));
            getRecentLevel(new_d_set);
            return false;
        }
        ///////////////////////////OTHER DESCRIPTION////////////////////////////
        ////////////////////////////SOME HEURISTIC//////////////////////////////
        /*if(c_nt != NodeType.ntCONCEPT) {
            if(t_box.getRuleGraph().getNode(Math.abs(c_id)).getDescription() != 0) {
                DSet d = new DSet();
                d.mergeWith(queue[IndividID].getToDoDSet()[toDoIndex]);
                int sign = 1;
                if(c_id < 0) sign = -1;
                queue[IndividID].addToDo(sign * t_box.getRuleGraph().getNode(Math.abs(c_id)).getDescription(), d, BTSize, t_box);
            }
            if(c_id > 0 && t_box.getRuleGraph().getNode(c_id).getSubDescription() != 0) {
                DSet d = new DSet();
                d.mergeWith(queue[IndividID].getToDoDSet()[toDoIndex]);
                queue[IndividID].addToDo(t_box.getRuleGraph().getNode(c_id).getSubDescription(), d, BTSize, t_box);
            }
        }*/
        ////////////////////////////////////////////////////////////////////////
        //AND
        if((c_nt == NodeType.ntAND && c_id > 0) || (c_nt == NodeType.ntOR && c_id < 0)) {
            if(!performAnd()) return false;
            queue[IndividID].currentToDo++;
            toDoIndex++;
            return true;
        } else
        //OR
        if((c_nt == NodeType.ntOR && c_id > 0) || (c_nt == NodeType.ntAND && c_id < 0)) {
            if(!performOr()) return false;
            queue[IndividID].currentToDo++;
            toDoIndex++;
            return true;
        } else
        //SOME
        if((c_nt == NodeType.ntSOME && c_id > 0) || (c_nt == NodeType.ntALL && c_id < 0)) {
            if(!performIncrease()) return false;
            queue[IndividID].currentToDo++;
            toDoIndex++;
            return true;
        } else
        //ALL
        if((c_nt == NodeType.ntALL && c_id > 0) || (c_nt == NodeType.ntSOME && c_id < 0)) {
            performAll();
            queue[IndividID].currentToDo++;
            toDoIndex++;
            return true;
        } else
        //CONCEPT
        if(c_nt == NodeType.ntCONCEPT) {
            if (!performAddConcept()) return false;
            queue[IndividID].currentToDo++;
            toDoIndex++;
            return true;
        } else
        //MINCARD
        if((c_nt == NodeType.ntMINCARD && c_id > 0) || (c_nt == NodeType.ntMAXCARD && c_id < 0)) {
            if(!performIncrease()) return false;
            queue[IndividID].currentToDo++;
            toDoIndex++;
            return true;
        } else
        //MAXCARD
        if((c_nt == NodeType.ntMAXCARD && c_id > 0) || (c_nt == NodeType.ntMINCARD && c_id < 0)) {
            performDecrease();
            queue[IndividID].currentToDo++;
            toDoIndex++;
            return true;
        } else
        //INDIVID
        if(c_nt == NodeType.ntINDIVID) {
            if(!performAddIndivid()) return false;
        } else
        //SELF
        if(c_nt == NodeType.ntHASSELF) {
            ////////////////////TODO////////////////////
            if(!performAddSelf()) return false;
        } else
        //NOTHING
        if((c_nt == NodeType.ntNOTHING && c_id > 0) || (c_nt == NodeType.ntTHING && c_id < 0)) {
            getRecentLevel(queue[IndividID].getToDoDSet()[toDoIndex]);
            return false;
        } else {
            //oops!
            System.out.println("THIS IS UNTYPED NODE! (or NOT node...)" + c_nt);
            //что-то пошло' не так...
        }
        queue[IndividID].currentToDo++;
        toDoIndex++;
        return true;
    }
        
    /**
     * Осуществляет запоминание текущего состояние интерпретации и добавление в стек.
     * @param option Определяет альтернативу на которой был осуществлен выбор.
     * @param lastOption Определяет альтернативу которая является последней в данном правиле.
     * @param justAdded Определяет добавляется ли элемент стека заново или осуществляется изменение текущего правила.
     * @param d Определяет множество зависимостей, которое необходимо добавлять после возврата назад на это правило.
     * @param Q Определяет множество индивидов, которое не может быть объединено.
     */
    private void remember(Object option, Object lastOption, boolean justAdded, DSet d, QualSolver Q) {
        if(option instanceof ChooseState) {
            st1 = 0; st2 = 1; curOr = 0; Q = null;
        }
        if(option instanceof IntPair) {
            curOr = 0; Q = null; //curChoose = null;
        }
        if(option instanceof Integer) {
            st1 = 0; st2 = 1; curChoose = null; Q = null;
        }
        if(option instanceof QSetPair) {
            curOr = 0; st1 = 0; st2 = 1; //curChoose = null;
        }
        
        nperfs = new boolean[QSize];
        nskips = new boolean[QSize];
        boolean[] iDV = new boolean[QSize];

        int skpSize = 0;
        for(int i = 0; i < QSize; i++) {
            nperfs[skpSize] = queue[i].toDoPerform;
            nskips[skpSize] = queue[i].isSkipped();
            iDV[skpSize] = queue[i].isDataTypeVertice;
            skpSize++;
        }
        
        int[] cToDo = new int[QSize];
        int[] cMQfd = new int[QSize];
        int[] cFAll = new int[QSize];
        for(int i = 0; i < QSize; i++) {
            cToDo[i] = queue[i].currentToDo;
            cMQfd[i] = queue[i].currentMQfd;
            cFAll[i] = queue[i].currentFAll;
        }
                
        if(justAdded) {
            if(BTSize == MaxBTSize - 1)
                increaseStack();

            DSet D = new DSet(d);
            BTStack[BTSize].clear();
            BTStack[BTSize].set(IndividID, toDoIndex, option, lastOption, QSize, nperfs, nskips, skpSize, curBNum, D, cToDo, cMQfd, cFAll, iDV);
            BTStack[BTSize].qo = Q;
            BTSize++;
            d.addValue(BTSize);
        } else {
            DSet D = new DSet(d);
            //BTStack[BTSize - 1].clear();
            BTStack[BTSize - 1].set(IndividID, toDoIndex, option, lastOption, QSize, nperfs, nskips, skpSize, curBNum, D, cToDo, cMQfd, cFAll, iDV);
            BTStack[BTSize - 1].qo = Q;
            d.addValue(BTSize);
        }
        curBNum++;
    }
    
    private InterpretationNode[] tmp1 = new InterpretationNode[32 * QMaxSize];
    private InterpretationNode[] tmp = new InterpretationNode[32 * QMaxSize];
    private int[] tmp_wh = new int[32 * QMaxSize];
    /**
     * Метод осуществляет возврат интерпретации к запомненному состонию.
     * @param level Глубина стека на которую осуществляется возврат.
     * @param to_start Параметр для отладки алгоритма возврата назад.
     * @return Возвращает ложь, если не существует модели заданного концепта и истина в противном случае.
     */
    private boolean restore(int level, boolean to_start) {
        //to_start = false;
        //if(use_global_caching)
        {
            //neg_cache.neg_add(queue[IndividID]);
        }
        //System.out.println(IndividID + " " + toDoIndex);
        //System.out.println("=================================================");
        //queue[0].show(r_box, t_box);
        stats.restoreCountAdd();
        int count = 0;
        BTSize = level;
        
        for(int i = BTSize - 1; i >= 0; i--) {
            if(!BTStack[i].isLastOption() || to_start) {
                BTSize -= count;
                level = BTSize;
                //set individID and toDoIndex of this individ
                int lastIndividID = IndividID;
                IndividID = BTStack[i].IndividID; newIndividID = IndividID + 1;
                toDoIndex = BTStack[i].toDoIndex;
                curBNum = BTStack[i].curBNum;
                //set current or alternative
                if(!to_start) {
                    if(BTStack[i].option instanceof Integer) {
                        curOr = ((Integer) BTStack[i].option) + 1;
                        BTStack[i].option = curOr;
                        BTStack[i].context.mergeWith(conflictDSet);
                        curDS = new DSet(BTStack[i].context);
                        currentQO = null;
                    } else
                    if(BTStack[i].option instanceof IntPair) {
                        st1 = ((IntPair)BTStack[i].option).x;
                        st2 = ((IntPair)BTStack[i].option).y + 1;
                        if(st2 >= (Integer)BTStack[i].lastOption) {
                            st1++;
                            st2 = st1 + 1;
                        }
                        ((IntPair)BTStack[i].option).x = st1;
                        ((IntPair)BTStack[i].option).y = st2;
                        BTStack[i].context.mergeWith(conflictDSet);
                        curDS = new DSet(BTStack[i].context);
                        justAddedLE = false;
                        currentQO = null;
                    }
                    if(BTStack[i].option instanceof ChooseState) {
                        st1 = 0;
                        st2 = 1;
                        curChoose = new ChooseState(((ChooseState)BTStack[i].option).notC, ((ChooseState)BTStack[i].option).ai);
                        BTStack[i].context.mergeWith(conflictDSet);
                        curDS = new DSet(BTStack[i].context);
                        currentQO = null;
                    }
                    if(BTStack[i].option instanceof QSetPair) {
                        InterpretationNode[] ancs = graph.getAncestors(queue[lastIndividID]);
                        QSetPair q = (QSetPair) BTStack[i].option;
                        boolean take_all = false;
                        for(int j = 0; j < q.q1.length; j++) {
                            for(int k = 0; k < q.q1[j].count; k++) {
                                for(int h = 0; h < ancs.length; h++) {
                                    if(q.q2[j][k].indexInQueue == ancs[h].indexInQueue) {
                                        if(q.q1[j].count == 1) {
                                            take_all = true;
                                            j = q.q1.length - 1;
                                            k = q.q1[j].count;
                                            break;
                                        }
                                        BTStack[i].qo.add(q.q1[j]);
                                        j = q.q1.length - 1;
                                        k = q.q1[j].count;
                                        break;
                                    }
                                }
                            }
                        }
                        if(take_all) {
                            for(int j = 0; j < q.q1.length; j++)
                                for(int k = 0; k < q.q1[j].count; k++)
                                    BTStack[i].qo.add(q.q1[j]);
                        }
                        BTStack[i].context.mergeWith(conflictDSet);
                        curDS = new DSet(BTStack[i].context);
                        justAddedLE = false;
                        currentQO = BTStack[i].qo;
                    }
                }

                int old_queue_size = QSize;
                int tmp_cnt = 0, tmp_cnt1 = 0;
                int OQSize = QSize - (old_queue_size - BTStack[i].queueSize);
                
                //кроме текущего рассматриваемого индивида будут добавлены все его предки с минимальный набором ToDo
                /*while(last_individ_id > OQSize)
                {
                    neg_cache.neg_add(queue[last_individ_id]);
                    if(queue[last_individ_id].getParents()[0] == null) break;
                    last_individ_id = queue[last_individ_id].getParents()[0].getNode().indexInQueue; //индекс предка в очереди, движемся только по первым предкам
                }*/
                
                for(int j = 0; j < old_queue_size; j++) {
                    if(whqueue[j] < level) {
                        tmp_wh[tmp_cnt] = whqueue[j];
                        tmp[tmp_cnt++] = queue[j];
                    } else {
                        tmp1[tmp_cnt1++] = queue[j];
                    }
                }

                for(int j = 0; j < tmp_cnt; j++) {
                    whqueue[j] = tmp_wh[j];
                    queue[j] = tmp[j];
                    queue[j].indexInQueue = j;
                }
                for(int j = 0; j < tmp_cnt1; j++) {
                    queue[tmp_cnt + j] = tmp1[j];
                    queue[tmp_cnt + j].indexInQueue = tmp_cnt + j;
                }

                QSize -= (old_queue_size - BTStack[i].queueSize);
                for(int j = 0; j < QSize; j++) {
                    queue[j].restore(BTStack[i].toDoPerfs[j], level, BTStack[i].curToDo[j], BTStack[i].curMQfd[j], BTStack[i].curFAll[j], BTStack[i].isDV[j]);
                    queue[j].setSkip(BTStack[i].skips[j]);
                    queue[j].setBlock(-1);
                }
                a_box.clearNodes(); //??????????????

                return true;
            }
            count++;
        }
        return false;
    }
    
    /**
     * Метод определяет глубину стека, к которой необходимо вернуть интерпретацию при возврате назад, по заданному множеству зависимостей.
     * @param d Множество зависимостей по которому определяется глубина стека стека.
     */
    private void getRecentLevel(DSet d) {
        conflictDSet = new DSet(d);
        
        if(conflictDSet.size() > 0) {
            while(BTStack[conflictDSet.getValues()[conflictDSet.size() - 1] - 1].isLastOption()) {
                conflictDSet.pop();
                if(conflictDSet.size() == 0) break;
            }
        }
        
        int[] a = d.getValues();
        curLevel = BTSize;
        
        if(a.length == 0) {
            curLevel = 0; return;
        }
        
        if(!use_back_jump) {
            for(int i = BTSize - 1; i >= 0; i--) {
                if(!BTStack[i].isLastOption()) {
                    curLevel = i + 1;
                    return;
                }
            }
        } else {
            curLevel = a[a.length - 1];
            for(int i = a.length - 1; i >= 0; i--) {
                if(!BTStack[a[i] - 1].isLastOption()) {
                    curLevel = a[i];
                    return;
                }
            }
            curLevel = 0;
            return;
        }
        
        curLevel = BTSize;
        if(curLevel < 0) {
            curLevel = 0;
        }
    }
    
    /**
     * В методе определяется является ли конъюнкция типов данных, соответствующих вершине, корректной.
     * Данный метод использует класс {@link DataChecker} для определения корректности данных.
     * @param data_node Определяет индивида представляющего индивида данных
     * @return Возвращает истина если текущая вершина является корректным типом данных, и ложь в противном случае
     */
    private boolean isCorrectData(InterpretationNode data_node) {
        boolean res = true;
        IntArray toDoCopy = new IntArray();
        toDoCopy.clear();
        for(int i = 0; i < data_node.getToDoSize(); i++)
            toDoCopy.add(data_node.getToDo()[i]);

        res = data_checker.getDataCheck(toDoCopy);
        
        if(!res) {
            DSet D = new DSet();
            for(int i = 0; i < data_node.getToDoSize(); i++)
                D.mergeWith(data_node.getToDoDSet()[i]);
            getRecentLevel(D);
        }
        return res;
    }
    
    /**
     * Метод запускает алгоритм проверки согласованности индивида.
     * @param IndID Определяет номер индивида в которого начинается провека интерпретации.
     * @return Возвращает истина, если заданный концепт или ABox являются не согласованными.
     */
    private boolean backTrack(int IndID) {
        a_box.clearNodes();
        if(pos_cache.getSize() > (1 << 8)) pos_cache.clear();
        neg_cache.clear();
        
        justAddedLE = true;
        IndividID = IndID;
        curOr = 0;
        if(curDS != null) curDS.clear();
        curChoose = null;
        curLevel = 0;
        newIndividID = 1;
        st1 = 0;
        st2 = 1;
        boolean neg_skip = false;
        while(true) {
            if(neg_cache.getSize() >= (1 << 7)) neg_cache.clear();            
            toDoIndex = queue[IndividID].currentToDo; //для каждой вершины интерпретации определена переменная currentToDo в которой хранится номер текущего обрабатываемого toDo
            neg_skip = false;
            if(QSize <= IndividID) { //если в очереди обработаны все индивиды, значит построена корректная интерпретация
                //if(class_mode)
                {
                    for(int it = 0; it < QSize; it++) {
                        for(int j = 0; j < queue[it].getToDoSize(); j++) {
                            RuleNode rn1 = t_box.getRuleGraph().getNode(Math.abs(queue[it].getToDo()[j]));
                            if(rn1.getNodeType() != NodeType.ntCONCEPT) continue;
                            for(int k = j + 1; k < queue[it].getToDoSize(); k++) {
                                RuleNode rn2 = t_box.getRuleGraph().getNode(Math.abs(queue[it].getToDo()[k]));
                                if(rn2.getNodeType() != NodeType.ntCONCEPT) continue;
                                if(queue[it].getToDo()[j] > 0 && queue[it].getToDo()[k] < 0) {
                                    int cid1 = t_box.getRuleGraph().getConceptID(rn1.getName());
                                    int cid2 = t_box.getRuleGraph().getConceptID(rn2.getName());
                                    //no_sub_sum[cid1].add(cid2);
                                } else
                                if(queue[it].getToDo()[j] < 0 && queue[it].getToDo()[k] > 0) {
                                    int cid1 = t_box.getRuleGraph().getConceptID(rn1.getName());
                                    int cid2 = t_box.getRuleGraph().getConceptID(rn2.getName());
                                    //no_sub_sum[cid2].add(cid1);
                                }
                            }
                        }
                        if(use_global_caching) pos_cache.add(queue[it]); //добавим всю интерпретацию в кэш
                    }
                }
                return true;
            } 

            if(queue[IndividID].isDataTypeVertice) {
                if(!isCorrectData(queue[IndividID])) { //написать обработчик DataVertice
                    if(!restore(curLevel, false)) {
                        break;
                    } else {
                        continue;
                    }
                }
                queue[IndividID].currentToDo = queue[IndividID].getToDoSize();
                IndividID = newIndividID; //переходим к нужной вершине в очереди
                newIndividID = IndividID + 1; //увеличиваем указатель на вершину на 1
                continue;
            }            
            
            if(queue[IndividID].currentToDo == 0 && queue[IndividID].toDoPerform) //кэшируем ветку только тогда когда она СРАЗУ ЖЕ только что закончилась
                if(IndividID > 0 && queue[IndividID - 1].getChildSize() == 0) { //если мы обработали полностью одну из ветвей интерпретации и она satisfiable то её можно cache
                    //если у предыдущего индивида нет потомков тогда нужно обработать всех его предков, 
                    //у которых нет потомков дальше чем рассматриваемый узел в интерпретации
                    //Arrays.fill(dfs_f, 0);
                    if(use_global_caching) posCacheUpdate(queue[IndividID - 1], IndividID, 0); 
                }

            if(queue[IndividID].currentToDo == 0 && queue[IndividID].toDoPerform) //проверяем есть ли данная вершина в положительном кэше тогда и только тогда когда начинается её обработка
                if(use_global_caching && IndividID != 0) if(pos_cache.find(queue[IndividID])) { //если в global caching есть вершина с таким же набором toDo значит мы уже обрабатывали её и она sat значит эту ветвь можно пропустить
                    queue[IndividID].currentToDo = queue[IndividID].getToDoSize();
                    IndividID = newIndividID; //переходим к нужной вершине в очереди
                    newIndividID = IndividID + 1; //увеличиваем указатель на вершину на 1
                    continue;
                }
            if(queue[IndividID].currentToDo == 0 && queue[IndividID].toDoPerform) { //вспомогательный if, который срабатывает только тогда когда вершина интерпретации только начинает обрабатываться
                if(graph.makeBlocked(queue[IndividID], queue, IndividID)) { //if this node is blocked - not add children
                    queue[IndividID].currentToDo = queue[IndividID].getToDoSize();
                    IndividID = newIndividID; //переходим к нужной вершине в очереди
                    newIndividID = IndividID + 1; //увеличиваем указатель на вершину на 1
                    continue;
                }
                //System.out.println(IndividID + " " + QSize + " " + BTSize + " " + queue[IndividID].getToDo()[0] + " " + pos_cache.getSize() + " " + neg_cache.getSize());
                queue[IndividID].origToDoWhen = 0;
                for(int i = 0; i < queue[IndividID].getToDoSize(); i++) {
                    if( queue[IndividID].origToDoWhen < queue[IndividID].getToDoWhen()[i])
                        queue[IndividID].origToDoWhen = queue[IndividID].getToDoWhen()[i];
                }
            }
            //if(queue[IndividID].currentToDo == 0 && queue[IndividID].toDoPerform) //проверяем есть ли данная вершина в отрицательном кэше тогда и только тогда когда начинается её обработка // а верно ли это ?
                if(use_global_caching) 
                    if(neg_skip /*|| neg_cache.neg_find(queue[IndividID])*/) { //если в neg cache содержит вершину с таким же toDoList то можно делать jump назад
                        DSet new_d_set = new DSet();
                        for(int i = 0; i < queue[IndividID].getToDoSize(); i++) {
                            new_d_set.mergeWith(queue[IndividID].getToDoDSet()[i]);
                        }
                        getRecentLevel(new_d_set);
                        if(!restore(curLevel, false)) {
                            break;
                        } else {
                            continue;
                        }
                    }
            if(queue[IndividID].isSkipped()) { //vertice can be skipped when it merged with another one or when it blocked
                queue[IndividID].currentToDo = queue[IndividID].getToDoSize();
                IndividID = newIndividID; //goto needed vertice in queue
                newIndividID = IndividID + 1; //increase iterator by 1
                continue;
            }
            
            if(!queue[IndividID].toDoPerform) { //если не выполняем toDo, тогда выполянем MQFd
                if(queue[IndividID].getMQfdSize() > queue[IndividID].currentMQfd) { //если в MQfdToDo что-то содержится
                    if(!makeDecrease())
                        if(!restore(curLevel, false)) {
                            break;
                        } else {
                            continue;
                        }
                    
                    queue[IndividID].currentMQfd++;
                    continue;
                } else {
                    queue[IndividID].currentToDo = queue[IndividID].getToDoSize();
                    IndividID = newIndividID; //переходим к нужной вершине в очереди
                    newIndividID = IndividID + 1; //увеличиваем указатель на вершину на 1
                    continue;
                }
            } else
            if(queue[IndividID].getToDoSize() <= queue[IndividID].currentToDo) { //если выполнены все toDo для текущей вершины
                if(graph.makeBlocked(queue[IndividID], queue, IndividID)) { //if this node is blocked - not add children
                    queue[IndividID].currentToDo = queue[IndividID].getToDoSize();
                    IndividID = newIndividID; //переходим к нужной вершине в очереди
                    newIndividID = IndividID + 1; //увеличиваем указатель на вершину на 1
                    continue;
                } else {
                    queue[IndividID].toDoPerform = false;
                    makeIncrease(); //if node isn't blocked then add all children
                    continue;
                }
            }

            if(!updateToDo()) { //perform ToDoEntry in toDoIndex
                if(!restore(curLevel, false)) {
                    break;
                }
            }
        }
        
        //обновление neg_cahce только тогда когда концепт вообще не выполним
        //if(use_global_caching) //neg_cache можно использовать всегда
        if(false) {
            InterpretationNode node = queue[IndividID];
            while(true) {
                node.indexInQueue = -1;
                neg_cache.neg_add(node); //должен быть добавлен минимальный набор концептов из ToDoList, который влечет UNSAT
                if(node.getParentSize() > 0) {
                    node = node.getParents()[0].getNode();
                    if(node.indexInQueue < 0) break;
                } else break;
            }
        }
        
        return false;
    }

    /**
     * Метод обеспечивает доступ к объекту класса {@link AChecker}.
     * @return Возвращает объект соответствующий текущему AChecker, используемому при выполнении табличного алгоритма
     */
    public AChecker getAChecker() {
        return a_checker;
    }
    
    /**
     * Метод определяет является ли концепт с номер p в лесу ограничений подконцептом концепта с номером q
     * @param p Номер предполагаемого подконцепта в лесу ограничений
     * @param q Номер предполагаемого надконцепта в лесу ограничений
     * @return Возвращает истина если выполняются включение концептов и ложь в противном случае
     */
    public boolean checkSubsumption(int p, int q) {
        QSize = 0; BTSize = 0;
        InterpretationNode current_node = addNewIndivid(null, -1, p, null, QSize, false);
        if(q != 0)
            current_node.addToDo(-q, new DSet(), BTSize, t_box);

        roots.add(current_node);
        
        //stats.clear();
        boolean reslt = backTrack(0);
        if(!reslt) {
            //System.out.println(QSize);
            //stats.printStats();
            //add concept(q) to description of concept(p)
            //t_box.getRuleGraph().addToSubDesciption(p, q);
            return true;
        }
        //System.out.println(QSize);
        //stats.printStats();
        //queue[0].show(r_box, t_box);
        return false;
    }
    
    public void showTemp() {
        queue[0].show(r_box, t_box);
    }
    
    /**
     * Метод осуществляет проверку запроса, опсианного в соответствии со спецификацией DL98 Workshop
     * @param q Определяет запрос для проверки
     */
    public void checkQuery(Query q) {
        QSize = 0;
        for(int j = 0; j < a_box.getCount(); j++) {
            whqueue[QSize] = 0;
            //a_box.createNode(a_box.getName(j), new DSet(), queue[QSize], t_box);
            queue[QSize].indexInQueue = QSize;
            QSize++;
        }
        int old_QS = QSize;
        //check individual instance
        for(int i = 0; i < q.getIndividualInstanceSize(); i++) {
            for(int j = 0; j < old_QS; j++) {
                queue[j].restore(true, 1, 0, 0, 0, false);
            }
            QSize = old_QS;
            
            //a_box.getNode(q.getIndividualInstance(i).x).addToDo(-q.getIndividualInstance(i).y, new DSet(), 0, t_box);
            pos_cache.clear();
            neg_cache.clear();
            if(!backTrack(0)) {
                //System.out.println(a_box.getName(q.getIndividualInstance(i).x) + " is " + t_box.getRuleGraph().getNode(q.getIndividualInstance(i).y).getName());
            } else {
                //System.out.println(a_box.getName(q.getIndividualInstance(i).x) + " is NOT " + t_box.getRuleGraph().getNode(q.getIndividualInstance(i).y).getName());
            }
            //a_box.getNode(q.getIndividualInstance(i).x).popToDo();
        }
    }
        
    /**
     * Метод инициирует проверку согласованности ABox.
     * В данном методе осуществляется инициализация элементов очереди и связей между ними.
     * Затем проводится проверка согласованности ABox.
     * @param all Определяет нужно ли проверять все элементы ABox
     * @return Возвращает истина если ABox является согласованным и ложь в противном случае
     */
    public boolean checkABoxSat(boolean all) {
        QSize = 0;
        BTSize = 0;

        remember(0, 0, true, new DSet(), null);
        if(!backTrack(0)) {
            stats.printStats();
            return false;
        }
        stats.printStats();
        restore(1, true);
        return true;
    }
    
    /**
     * Метод инициирует проверку согласованности концепта.
     * В методе осуществляется добавление индивида в очередь и инициирование его элементом
     * леса ограничений.
     * @param ps Определяет имя концепта для проверки
     * @param show_model Определяет параметр управляющий выводом модели интерпретации
     * @param negt Определяет, нужно ли проверять отрицание концепта
     * @param conc_id Определяет номер вершины, соответствующей метаправилу
     * @return Возвращает истина, если соответствующий концепт является выполнимым и ложь в противном случае
     */
    public boolean checkSat(int ps, boolean show_model, int negt, int conc_id) {
        //if negt == 1 ps = NOT ps, else if negt = 0 ps = ps; else nothing to cache
        BTSize = 0;
        QSize = 0;
        a_box.clearNodes();        
        int c_id = t_box.getRuleGraph().getConceptInRuleGraph(ps);
        if(negt == 1) c_id = -c_id;
        InterpretationNode current_node = addNewIndivid(null, -1, c_id, null, -1, false);
        roots.add(current_node);

        stats.clear();
        long sat_time = System.currentTimeMillis();
        if(!backTrack(0)) {
            if(show_model) current_node.show(r_box, t_box);
            if(System.currentTimeMillis() - sat_time < sec_millis) {
                if(negt < 2) {
                    cache[negt][conc_id] = new Cache(r_box.getRoleSize()); //empty cache
                    cache[negt][conc_id].setSize(-1);
                }
            } else {
                //nothing to cache
            }
            //stats.printStats();
            return false;
        } else {
            if(negt < 2)
                cache[negt][conc_id] = getCache(0); //non-empty cache
        }
        //current_node.show(r_box, t_box);
        if(show_model) {
            stats.printStats();
            //current_node.show(r_box, t_box);
        }
        return true;
    }
    
    /**
     * Метод осуществляет проверку согласованности концептов которые описаны в TBox.
     * Показывает количество концептов начинающихся с префикса "My". Данный метод
     * был создан специально для тестирования табличного алгоритма на данных DL98 Workshop
     * @param showModel Определяет нужно ли показывать модель построенных концептов
     * @param show_log Определяет нужно ли показывать лог
     */
    public void checkALCTBoxSat(boolean showModel, boolean show_log) {
        int sats = 0, unsats = 0;
        System.out.println("TBOX CONSISTENT CHECKING...");
        flag.clear();
        conc = new boolean[1024 * 1024];
                
        for(int i = 0; i < 1024 * 1024; i++)
            conc[i] = false;
     
        for(int i1 = 0; i1 < t_box.getOrder().size(); i1++) {
            int i = t_box.getOrder().get(i1);
            if(!conc[i]) {
                QSize = 0;
                int c_id = t_box.getRuleGraph().findConcept(t_box.getRuleGraph().getConcepts()[i]);
                BTSize = 0;
                InterpretationNode current_node = addNewIndivid(null, -1, c_id, null, -1, false);
                roots.add(current_node);
                stats.clear();
                pos_cache.clear();
                neg_cache.clear();
                long sat_time = System.currentTimeMillis();

                t_box.getRuleGraph().clearCacheClasses();
                if(t_box.getRuleGraph().getNode(Math.abs(c_id)).getNodeType() == NodeType.ntCONCEPT) {
                    if(t_box.getRuleGraph().getNode(Math.abs(c_id)).getDescription() != 0)
                        aut.labelTree(t_box.getRuleGraph().getNode(Math.abs(c_id)).getDescription());
                } else {
                    aut.labelTree(c_id);
                }
                
                if(!backTrack(QSize - 1)) {
                    if(System.currentTimeMillis() - sat_time < sec_millis) {
                        if(show_log) System.out.print(t_box.getRuleGraph().getConcepts()[i] + " is unsatisfiable\n");
                    } else {
                        break;
                    }
                    if(t_box.getRuleGraph().getConcepts()[i].startsWith("My"))
                        unsats++;
                } else {
                    if(t_box.getRuleGraph().getConcepts()[i].startsWith("My")) if(show_log) System.out.print(t_box.getRuleGraph().getConcepts()[i] + " is Satisfiable\n");
                    if(t_box.getRuleGraph().getConcepts()[i].startsWith("My")) {
                        sats++;
                    }
                }
                queueClear();
                if(showModel) current_node.show(r_box, t_box);
            }
        }
        System.out.println("SAT TOTAL:   " + sats);
        System.out.println("UNSAT TOTAL: " + unsats);
    }
    
    /**
     * Метод возвращает toDoList в виде кэша индивида из очереди с определенным номером.
     * @param x Определяет номер индивида в очереди
     * @return Возвращает элемент типа {@link Cache}, соответствующий кэшу элемента очереди
     */
    private Cache getCache(int x) {
        Cache ret = new Cache(r_box.getRoleSize());
        for(int i = 0; i < queue[x].getToDoSize(); i++) {
            NodeType n = t_box.getRuleGraph().getNode(Math.abs(queue[x].getToDo()[i])).getNodeType();
            if(n != NodeType.ntAND && n != NodeType.ntOR)
                ret.add(queue[x].getToDo()[i], r_box, t_box);
        }
        return ret;
    }
    
    private int dfs_f[] = new int[1 << 20];
    /**
     * Метод реализует алгоритм поиска в глубину для определения того, какие элементы нужно вносить
     * в положительный глобальный кэш.
     * Для того, чтобы предотвратить обрушение стека на интерпретациях больших размеров
     * используется пороговый показатель глубины равный.
     * @param in Вершина ветки интерпретации от которой осуществляется поиск в глубину
     * @param depth Определяет текущий уровень вершины интерпретации в ветке
     * @return Возвращает истина, если текущая вершина может быть добавлена в кэш, и ложь в противном случае
     */
    private boolean DFS(InterpretationNode in, int depth) {
        if(depth > 100) return false;
        dfs_f[in.indexInQueue] = 2;

        if(in.currentToDo < in.getToDoSize()) {
            dfs_f[in.indexInQueue] = -1;
            return false;
        }
        for(int i = 0; i < in.getChildSize(); i++) {
            if(!DFS(in.getChildren()[i].getNode(), depth + 1)) {
                dfs_f[in.indexInQueue] = -1;
                return false;
            }
        }
        dfs_f[in.indexInQueue] = 1;
        return true;
    }
    
    /**
     * Метод выполняет обновление (добавление элементов) глобального кэша после того как была
     * обнаружена непротиворечивая интерпретация какой-либо ветки.
     * В данном метод выполняется добавление toDoList каждого индивида из непротиворечивой ветки интерпретации
     * Алгоритм поиска в глубину определяет должен ли быть добавлен индивид в положительный кэш.
     * @param in Определяет корень ветки в интерпретации индивидов которого нужно добавлять в положительный кэш
     * @param ind_id Определеяет номер индивида в очереди до которого необходимо добавлять ветку в кэш
     * @param dpth Определяет то на какую глубину нужно проходить дерево интерпретации
     */
    private void posCacheUpdate(InterpretationNode in, int ind_id, int dpth) {
        if(in.isSkipped()) return;
        if(!DFS(in, 0)) return;
        for(int i = 0; i < in.getChildSize(); i++) {
            if(in.getChildren()[i].getNode().indexInQueue >= ind_id) {
                return;
            } 
            if(in.getChildren()[i].getNode().getChildSize() > 0) {
                return;
            }
            //если какой-либо потомок данной вершины есть в очереди за рассматриваемым элементом или у него есть потомки
            //то не рассматриваем его
        }
        for(int i = 0; i < in.getParentSize(); i++) {
            if(in.getParents()[i].getNode().indexInQueue >= ind_id) {
                return;
            }
            
        }
        pos_cache.add(in); //здесь добавляется что-то, что может быть не выполнимо
        for(int i = 0; i < in.getParentSize(); i++) {
            posCacheUpdate(in.getParents()[i].getNode(), ind_id, dpth + 1);
        }
    }
    
    /**
     * Метод обеспечивает переопределение TBox рассматриваемой онтологии.
     * @param _t_box Определяет новый TBox
     */
    public void setTBox(TBox _t_box) {
        pos_cache.setRuleGraph(_t_box.getRuleGraph());
        neg_cache.setRuleGraph(_t_box.getRuleGraph());
        t_box = _t_box;
        if(aut != null)
            aut.setRuleGraph(t_box.getRuleGraph());
        if(a_checker != null)
            a_checker.setTBox(_t_box);
        if(sub_checker != null)
            sub_checker.setTBox(_t_box);
        if(data_checker != null)
            data_checker.setTBox(_t_box);
    }
    
    /**
     * Метод обеспечивает переопределение RBox рассматриваемой онтологии.
     * @param _r_box Определяет новый RBox
     */
    public void setRBox(RBox _r_box) {
        r_box = _r_box;
        if(sub_checker != null)
            sub_checker.setRBox(_r_box);
        if(graph != null)
            graph.setRBox(_r_box);
    }
    
    /**
     * Метод обеспечивает переопределение ABox рассматриваемой онтологии.
     * @param _a_box Определяет новый ABox
     */
    public void setABox(ABox _a_box) {
        a_box = _a_box;
        if(sub_checker != null)
            sub_checker.setABox(_a_box);
    }
}