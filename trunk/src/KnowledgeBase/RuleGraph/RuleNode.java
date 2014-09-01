package KnowledgeBase.RuleGraph;

import Enums.NodeType;
import KnowledgeBase.TBox;
import java.util.Arrays;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWLFacet;

/**
 * Класс содержит описание и методы для одной вершины леса ограничений.
 * @author Andrey Grigoryev
 */
public class RuleNode {
    
    private NodeType nodeType;
    private int roleType;
    private int numberRestriction;
    private String conceptName = null;
    private boolean isNamed = false;
    private int description = 0;
    private int sub_description = 0;
    private int neg_description = 0;
    private OWLLiteral liter = null;
    private OWLDatatype datatype = null;
    private OWLFacet facet = null;
    private int cache_class = 0;
    private boolean isEqNod = false;
    private int individNumber = 0;
    
    private int MaxChild = 4;
    private int MaxParent = 4;
    
    private int childSize = 0;
    private int parentSize = 0;
    private int transCount = 0;
    
    private int[] children = new int[MaxChild];
    private int[] parents = new int[MaxParent];
    private int[] trans = null; //FORALL-правила имеют часть по транзитивным подролям. FORALL R.C при S [= R, влечет FORALL S.C. trans обозначает номер концепта FORALL S.C;
    
    /**
     * Метод инициализирует свойства класса: тип вершины, тип рли и числовое ограничение
     */
    public final void defaultCreate() {
        roleType = -1;
        nodeType = NodeType.ntUNDEF;
        numberRestriction = 0;
    }
        
    /**
     * Пустой конструктор класса, который инициализирует поля класса.
     */
    public RuleNode() {
        defaultCreate();
    }  
    
    /**
     * Конструктор класса, инициализирующий поле тип вершины заданным параметром.
     * @param node_type Тип создаваемой вершины.
     */
    public RuleNode(NodeType node_type) {
        defaultCreate();
        nodeType = node_type;
    }  
    
    /**
     * Конструктор класса, инициализирующий поле тип вершины заданным параметром
     * и поле тип роли.
     * Данный констуктор применяется для создания вершин с квантором существования
     * или квантором всеобщности.
     * @param node_type Тип создаваемой вершины.
     * @param role_type Идентификационный номер роли.
     */
    public RuleNode(NodeType node_type, int role_type) {
        defaultCreate();
        roleType = role_type;
        nodeType = node_type;
    }
        
    /**
     * Конструктор класса, инициализирующий поле тип вершины заданным параметром,
     * поле тип роли и поле количественное ограничение.
     * Данный констуктор применяется для создания вершин с квантором максимальной 
     * или минимальной кардинальности.
     * @param node_type Тип создаваемой вершины.
     * @param role_type Идентификационный номер роли.
     * @param number_restriction Численное ограничение.
     */
    public RuleNode(NodeType node_type, int role_type, int number_restriction) {
        defaultCreate();
        roleType = role_type;
        nodeType = node_type;
        numberRestriction = number_restriction;
    }  

    /**
     * Конструктор класса, инициализирующий поле имя концепта. Данный конструктор 
     * был создан для того, чтобы инициализировать вершину-концепт.
     * @param concept_name Имя концепта.
     */
    public RuleNode(String concept_name) {
        defaultCreate();
        nodeType = NodeType.ntCONCEPT;
        conceptName = concept_name;
    }
    
    /**
     * Метод для осуществления записи свойства класс кэширования.
     * Данный метод разработан для реализации алгоритма Эдмондса для поиска сходных 
     * структур в лесу ограничений.
     * @param cc Новый класс кэширования вершины.
     */
    public void setCacheClass(int cc) {
        cache_class = cc;
    }
    
    /**
     * Метод для чтения свойства класса кэширования.
     * @return Класс кэширования вершины.
     */
    public int getCacheClass() {
        return cache_class;
    }
    
    /**
     * Метод класс для записи литеры правила.
     * Данный метод позволяет осуществлять работу с конкретными литерами определенного типа данных.
     * @param l Новая литера правила.
     */
    public void setLiter(OWLLiteral l) {
        liter = l;
    }
    
    /**
     * Метод класс для записи типа данных правила.
     * Данный метод позволяет осуществлять работу с конкретными типами данных.
     * @param d Новый тип данных правила.
     */
    public void setDatatype(OWLDatatype d) {
        datatype = d;
    }
    
    /**
     * Метод класс для записи литеры вершины.
     * Данный метод позволяет осуществлять работу с конкретными ограничениями
     * накладываемыми на типа данных.
     * @param f Новое ограничение типа данных правила.
     */
    public void setFacet(OWLFacet f) {
        facet = f;
    }
    
    /**
     * Метод осуществляет доступ для чтения ограничения типа данных правила.
     * @return Ограничение типа данных правила.
     */
    public OWLFacet getFacet() {
        return facet;
    }
    
    /**
     * Метод осуществляет доступ для чтения типа данных правила.
     * @return Тип данных правила. 
     */
    public OWLDatatype getDatatype() {
        return datatype;
    }
    
    /**
     * Метод осуществляет доступ для чтения ограничения литеры правила.
     * @return Литера типа данных правила.
     */
    public OWLLiteral getLiter() {
        return liter;
    }

    /**
     * Метод для записи имени концепта.
     * @param new_name Новое имя концепта.
     */
    public void setName(String new_name) {
        conceptName = new_name;
    }
    
    /**
     * Метод для чтения имени концепта.
     * @return Имя концепта, соответствующего вершине.
     */
    public String getName() {
        return conceptName;
    }
    
    /**
     * Метод для записи свойства эквивалентного описания концепта.
     * @param new_desc Номер вершины леса ограничений соответствующей эквивалентному описанию концепта.
     */
    public void setDescription(int new_desc) {
        description = new_desc;
    }
    
    /**
     * Метод для чтения свойства эквивалентного описания концепта.
     * @return Номер вершины леса ограничений соответствующей эквивалентному описанию концепта.
     */
    public int getDescription() {
        return description;
    }
    
    /**
     * Метод для записи свойства описания надконцепта.
     * @param new_sub_desc Номер вершины леса ограничений соответствующей описанию надконцепта.
     */
    public void setSubDescription(int new_sub_desc) {
        sub_description = new_sub_desc;
    }
    
    /**
     * Метод для чтения свойства описания надконцепта.
     * @return Номер вершины леса ограничений соответствующей описанию надконцепта.
     */
    public int getSubDescription() {
        return sub_description;
    }
    
    /**
     * Метод для записи свойства описания надконцепта отрицания текущего концепта.
     * @param new_desc Номер вершины леса ограничений соответствующей описанию надконцепта.
     */
    public void setNegativeDescription(int new_desc) {
        neg_description = new_desc;
    }
    
    /**
     * Метод для чтения свойства описания надконцепта отрицания текущего концепта.
     * @return Номер вершины леса ограничений соответствующей описанию надконцепта.
     */
    public int getNegativeDescription() {
        return neg_description;
    }
    
    /**
     * Метод для записи свойства, определяющего имеет ли концепт эквивалентное описание.
     * @param nam Параметр, определяющий наличие эквивалентного концепта.
     */
    public void setNamed(boolean nam) {
        isNamed = nam;
    }

    /**
     * Метод для чтения свойства, определяющего имеет ли концепт эквивалентное описание.
     * @return Возвращает истина, если концепт имеет эквивалентное описание.
     */
    public boolean isNamed() {
        return isNamed;
    }
    
    /**
     * Метод возвращает номера всех потомков данной вершины.
     * @return ArrayList номеров всех потомков вершины.
     */
    public int[] getChildren() {
        return children;
    }
    
    /**
     * Метод возвращает номера всех прямых предков данной вершины.
     * @return Массив номеров всех прямых предков вершины.
     */
    public int[] getParents() {
        return parents;
    }
    
    /**
     * Метод удаляет ссылки на всех прямых предков вершины.
     */
    public void clearParent() {
        Arrays.fill(trans, 0);
    }
    
    private void increaseParents() {
        int[] temp = parents;

        MaxParent = MaxParent * 2;
        parents = new int[MaxParent];
        for(int i = 0; i < parentSize; i++)
            parents[i] = temp[i];
    }
    
    /**
     * Метод добавляет потомка вершины.
     * @param x Номер предка, получаемые из алгоритма определения автоморфизмов дерева.
     */
    public void addParent(int x) {
        if(parentSize >= MaxParent - 1) {
            increaseParents();
        }
        parents[parentSize++] = x;
    }
    
    private void increaseChildren() {
        int[] temp = children;

        MaxChild = MaxChild * 2;
        children = new int[MaxChild];
        for(int i = 0; i < childSize; i++)
            children[i] = temp[i];
    }
    
    /**
     * Метод осуществляет добавление нескольких потомков вершины.
     * @param child_array Массив потомков вершины.
     * @param size Размер массива потомков вершины.
     */
    public void addChild(int[] child_array, int size) {
        for(int i = 0; i < size; i++)
            addChild(child_array[i]);
    }
    
    /**
     * Метод осуществляет добавление одного потомка вершины.
     * @param new_child Номер потомка вершины.
     */
    public void addChild(int new_child) {
        if(childSize >= MaxChild - 1) {
            increaseChildren();
        }
        children[childSize++] = new_child;
    }
    
    /**
     * Устанавливает индивида вершины леса ограничений.
     * @param oi Новый индивид вершины.
     */
    public void setIndivid(OWLIndividual oi) {
        if(oi.isNamed()) {
            conceptName = oi.asOWLNamedIndividual().toStringID();
        } else {
            conceptName = oi.asOWLAnonymousIndividual().toStringID();
        }
    }
    
    /**
     * Метод для чтения свойства типа вершины.
     * @return Тип вершины леса ограничений соответствующей данному концепту.
     */
    public NodeType getNodeType() {
        return nodeType;
    }
    
    /**
     * Метод для чтения свойства роли квантора правила, соответствующего вершине.
     * @return Роль квантора вершины леса ограничений соответствующей данному концепту.
     */
    public int getRoleType() {
        return roleType;
    }
    
    /**
     * Метод для чтения свойства количественного ограничения.
     * @return Количественное ограничение вершины леса ограничений соответствующей данному концепту.
     */
    public int getNumberRestriction() {
        return numberRestriction;
    }
        
    /**
     * В данном методе осуществляется добавление аксиом в соответствии с транзитивными ролями.
     * Если в базе знаний существует FORALL-правило или EXISTS-правило, и роль в этом
     * правиле является транзитивной, то необходимо добавить к концепту под квантором
     * текущее рассматриваемое правило.
     * @param concept_id Номер концепта в лесу ограничений.
     */    public void addTrans(int concept_id) {
        if(trans == null) {
            trans = new int[16];
        }
        trans[transCount++] = concept_id;
    }

     /**
      * Метод возвращает номер концепта в лесу ограничений, соответствующего заданной транзитивной роли.
      * @param t_box TBox онтологии.
      * @param role Номер роли, для которой необходимо вернуть транзитивный концепт.
      * @return Номер вершину в лесу ограничений.
      */
    public int getTransByRole(TBox t_box, int role) {
        for(int i = 0; i < transCount; i++) {
            if(t_box.getRuleGraph().getNode(trans[i]).getRoleType() == role) {
                return trans[i];
            }
        }
        return 0;
    }
    
    /**
     * Метод возвращает значение того является ли вершина эквивалентным описанием.
     * @return Возвращает истина если вершина является эквивалентным описание концепта и ложь в противном случае.
     */
    public boolean isEquivNode() {
        return isEqNod;
    }
    
    /**
     * Метод осуществляет доступ для записи параметра isEqNod, который отвечает 
     * за то, что определяет вершину как эквивалент какого-либо концепта. Данный 
     * параметр необходим чтобы не осуществлять двойственное преобразование AND правила в OR-правило
     * при использовании отрицания концепта.
     * @param par Определяет явлется ли данная вершина описанием какого-либо концепта.
     */
    public void setEquivNode(boolean par) {
        isEqNod = par;
    }
    
    public void setIndividNumber(int number) {
        individNumber = number;
    }
    
    public int getIndividNumber() {
        return individNumber;
    }
    
    public int getChildrenSize() {
        return childSize;
    }
    
    public int getParentsSize() {
        return parentSize;
    }
    
    public boolean deleteChild(int child) {
        for(int i = 0; i < childSize; i++) {
            if(children[i] == child) {
                for(int j = i + 1; j < childSize; j++) {
                    children[j - 1] = children[j];
                }
                childSize --;
                return true;
            }
        }
        return false;
    }
}