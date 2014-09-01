package KnowledgeBase.RuleGraph;

import Enums.NodeType;
import Help.IntPair;
import Help.SHash;
import KnowledgeBase.ABox;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWLFacet;

/**
 * Этот класс содержит свойства и метода леса ограничений. 
 * Все аксиомы базы знаний из множества TBox представляются в виде аксиом эквивалентности
 * или аксиомы включения, левые и правые части этих аксиом представляются в виде дерева.
 * Древовидная структура преобразуется в графовидную путем нахождения общих подвыражений
 * и их замене.
 * Общий концепт TOP (THING) всегда имеет индекс 1, отрицание любого концепта представляется
 * в виде отрицания соответствующего индекса массива узлов.
 * @author Andrey Grigoryev
 */
final public class RuleGraph {

    private String thing = "TOP";
    private String nothing = "BOTTOM";
    
    private int MaxNodesCount = 8192;
    private RuleNode[] nodes = new RuleNode[MaxNodesCount];
    private int nodesSize = 0;
    private TBox t_box;
    private RBox r_box;
    private ABox a_box;
    
    private HashMap<String, Integer> concept2int = new HashMap<String, Integer>();
    private HashMap<String, Integer> individ2int = new HashMap<String, Integer>();
    private HashMap<String, Integer> liter2int = new HashMap<String, Integer>();

    private HashMap<String, Integer> c2int = new HashMap<String, Integer>();
    private int MaxConcepts = 1024;
    private String[] concepts = new String[MaxConcepts];
    private int conceptSize = 0;
    private int[] concept2num = new int[1024 * 1024];
    private boolean bl_O = false;
    private boolean bl_Q = false;
    private boolean bl_N = false;
    private boolean bl_D = false;
    
    /**
     * Основной и единственный конструктор класса.
     * Создает экземпляр класса и добавляет в массив вершин два концета: UNDEF c 
     * индексом 0 и THING с индексом 1.
     */
    public RuleGraph() {
        addNode2RuleTree(new RuleNode(NodeType.ntUNDEF));
        addNode2RuleTree(new RuleNode(NodeType.ntTHING));
    }
    
    private void increaseNodes() {
        RuleNode[] temp = nodes;

        MaxNodesCount = MaxNodesCount * 2;
        nodes = new RuleNode[MaxNodesCount];
        for(int i = 0; i < nodesSize; i++) {
            nodes[i] = temp[i];
        }
        temp = null;
    }
    
    /**
     * Возвращает истина если входной символ равен пробелу, переводу строк или табуляции.
     * @param ch Исходный символ.
     * @return Результат метода.
     */
    static public boolean isSkippedSymbol(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\t';
    }

    /**
     * Метод возвращает новую строку, которая строится на основе входной, удаляя из 
     * неё пробела, перевода строки и табуляции.
     * @param s Исходная строка
     * @return Строка без символов пробела, табуляции, перевода строки.
     */
    static public String skipDels(String s) {
        String ret = "";
        boolean bl = false;
        for(int i = 0; i < s.length(); i++) {
            if(!isSkippedSymbol(s.charAt(i))) bl = true;
            if(bl) ret += s.charAt(i);
        }
        return ret;
    }
    
    /**
     * Метод возвращает общее количество вершин леса ограничений.
     * @return Общее количество вершин леса ограничений.
     */
    public int getNodesCount() {
        return nodesSize;
    }
    
    /**
     * Метод осуществляет добавление GCI (General Concept Inclusion) в лес ограничений
     * в виде OR-правила
     * @param l Определяет номер вершины леса ограничений, представляющего левую часть GCI
     * @param r Определяет номер вершины леса ограничений, представляющего правую часть GCI
     * @return Возвращает номер вершины леса ограничений, соответствующей добавленному OR-правилу
     */
    public int addGCI(int l, int r) {
        addNode2RuleTree(new RuleNode(NodeType.ntOR));
        nodes[nodesSize - 1].addChild(-l);
        nodes[nodesSize - 1].addChild(r);
        return nodesSize - 1;
    }
    
    /**
     * Возвращает вершину по её номеру в лесу ограничений
     * @param x Номер вершины
     * @return Указатель на объект RuleNode, соответствующий номеру в массиве
     */
    public RuleNode getNode(int x) {
        return nodes[x];
    }
    
    /**
     * Метод определяет представляет ли вершина с заданным индексом вершину данных
     * @param nodeIndex Задает индекс вершины в массиве вершин
     * @return Возвращает истина, если вершина представляет собой ограничение на тип данных и ложь в противном случае
     */
    public boolean isDataNode(int nodeIndex) {
        if( nodes[nodeIndex].getNodeType() == NodeType.ntDATATYPE || 
            nodes[nodeIndex].getNodeType() == NodeType.ntLITER) {
            return true;
        }
        if(nodes[nodeIndex].getNodeType() == NodeType.ntAND || nodes[nodeIndex].getNodeType() == NodeType.ntOR) {
            for(int i = 0; i < nodes[nodeIndex].getChildrenSize(); i++) {
                if(isDataNode(Math.abs(nodes[nodeIndex].getChildren()[i]))) return true;
            }
        }
        return false;
    }
    
    /**
     * Метод добавляет OWL-выражение в лес ограничений.
     * @param expression Выражение на языке OWL.
     * @return Номер вершины, соответствующего концепту в лесу ограничений.
     */
    public int addExpr2Graph(OWLClassExpression expression) {
        return buildRuleTree(expression);
    }
    
    /**
     * Метод добавляет выражение, выраженное строковым представлением, в лес ограничений.
     * @param expression Выражение в строковом виде.
     * @return Номер вершины, соответствующего концепту в лесу ограничений.
     */
    public int addExpr2Graph(String expression) {
        return buildRuleTreeByStackHelp(expression);
    }
    
    /**
     * Метод добавляет OWL-выражение типа данных в лес ограничений.
     * @param data_expression Выражение, ограничивающее тип данных, записанное на языке OWL.
     * @return Номер вершины в лесу ограничений.
     */
    public int addDataExpr2Graph(OWLDataRange data_expression) {
        return buildDataRuleTree(data_expression);
    }
    
    private void increaseConcepts() {
        String[] temp = concepts;

        MaxConcepts = MaxConcepts * 2;
        concepts = new String[MaxConcepts];
        for(int i = 0; i < conceptSize; i++) {
            concepts[i] = temp[i];
        }
    }

    /**
     * Метод осуществляет поиск концепта с заданным именем в лесу ограничений
     * В методе осуществляется поиск вершины в лесу ограничений по заданному имени (в виде строки)
     * и возвращает индекс вершины соответствующей этому концепту. Если такого имени концепта
     * не существует в лесу ограничений, то концепт будет добавлен в массив ограничений 
     * в конец и возвращен его индекс
     * @param concept_name Задает имя концепта
     * @return Возвращает порядковый номер вершины, определяющей заданный конепт
     */
    public int findConcept(String concept_name)
    {
        if(concept_name == null) {
            return 0;
        }
        if(concept_name.equalsIgnoreCase("*BOTTOM*") || concept_name.equalsIgnoreCase("BOTTOM")) return -1;
        if(concept_name.equalsIgnoreCase("*TOP*") || concept_name.equalsIgnoreCase("TOP")) return 1;
        if(concept_name.equalsIgnoreCase("Thing") || concept_name.equalsIgnoreCase("Thing")) return 1;
        if(concept_name.equalsIgnoreCase("Nothing") || concept_name.equalsIgnoreCase("Nothing")) return -1;
        if(concept2int.containsKey(concept_name))
            return concept2int.get(concept_name);
        
        if(conceptSize >= MaxConcepts - 1) {
            increaseConcepts();
        }
        
        addNode2RuleTree(new RuleNode(concept_name));
        concepts[conceptSize++] = concept_name;
        c2int.put(concept_name, conceptSize - 1);
        concept2int.put(concept_name, nodesSize - 1);
        concept2num[conceptSize - 1] = nodesSize - 1;
        return nodesSize - 1;
    }
    
    public int getConceptIndex(String conceptName) {
        for(int i = 0; i < conceptSize; i++) {
            if(concepts[i].equals(conceptName)) {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * Возращает номер узла в графе ограничений по идентификатору концепта.
     * @param ID Идентификатор концепта.
     * @return Узел в графе, соответствующий концепту по заданному ID.
     */
    public int getConceptInRuleGraph(int ID) {
        return concept2num[ID];
    }
    
    /**
     * Метод осуществляет поиск индивида с заданным именем в лесу ограничений
     * В методе осуществляется поиск вершины в лесу ограничений по заданному имени (в виде объекта OWLIndividual)
     * и возвращает индекс вершины соответствующей этому индивиду. Если такого имени индивида
     * не существует в лесу ограничений, то концепт будет добавлен в массив ограничений 
     * в конец и возвращен его индекс
     * @param oi Задает индвида в виде OWLIndividual
     * @return Возвращает порядковый номер вершины, определяющей заданного индивида
     */
    public int findIndivid(OWLIndividual oi) {
        String nam = "";
        if(oi.isNamed()) {
            nam = oi.asOWLNamedIndividual().toStringID();
        } else {
            nam = oi.asOWLAnonymousIndividual().toStringID();            
        }
        if(individ2int.containsKey(nam)) {
            return individ2int.get(nam);
        }
        RuleNode help = new RuleNode(NodeType.ntINDIVID);
        help.setIndivid(oi);
        addNode2RuleTree(help);
        individ2int.put(nam, nodesSize - 1);
        
        help.setIndividNumber(a_box.getCount());
        a_box.addIndivid();
            
        return nodesSize - 1;
    }

    /**
     * Метод осуществляет поиск литеры с заданным именем в лесу ограничений
     * В методе осуществляется поиск вершины в лесу ограничений по заданному имени (в виде объекта OWLLiteral)
     * и возвращает индекс вершины соответствующей этой литере. Если такой литеры
     * не существует в лесу ограничений, то она будет добавлена в массив ограничений 
     * в конец и возвращен его индекс
     * @param ol Задает литеру в виде OWLLiteral
     * @return Возвращает порядковый номер вершины, определяющей заданную литеру
     */
    public int findLiter(OWLLiteral ol) {
        String nam = ol.getLiteral() + ol.getDatatype().toStringID();
        if(liter2int.containsKey(nam)) {
            return liter2int.get(nam);
        } else {
            if(ol.getDatatype().isBoolean()) {
                if(ol.getLiteral().equals("true"))
                    nam = "false" + ol.getDatatype().toStringID(); else
                    nam = "true" + ol.getDatatype().toStringID();                    
                
                if(liter2int.containsKey(nam)) {
                    return -liter2int.get(nam);
                }
            }
        }
        
        RuleNode help = new RuleNode(NodeType.ntLITER);
        help.setLiter(ol);
        help.setDatatype(ol.getDatatype());
        addNode2RuleTree(help);
        liter2int.put(nam, nodesSize - 1);
        
        return nodesSize - 1;
    }

    /**
     * Метод определяет номер концепта в порядке появления его в аксиомах.
     * @param conc Задает имя концепта в виде строки
     * @return Возвращает порядковый номер концепта или 0, если этот концепт ещё не был добавлен
     */
    public int getConceptID(String conc) {
        if(!c2int.containsKey(conc)) {
            return 0;
        }
        return c2int.get(conc);
    }
    
    /**
     * Метод возвращает имена всех концептов в виде ArrayList строк.
     * @return ArrayList имен концептов
     */
    public String[] getConcepts() {
        return concepts;
    }
    
    public int getConceptsSize() {
        return conceptSize;
    }
    
    /**
     * Метод осуществляет запись свойства TBox, соответствующего текущему лес ограничений.
     * @param new_t_box TBox, соответствующий текущему лесу ограничений.
     */
    public void setTBox(TBox new_t_box, ABox new_a_box) {
        t_box = new_t_box;
        a_box = new_a_box;
    }
    
    /**
     * Метод осуществляет чтение свойства TBox, соответствующего текущему лес ограничений.
     * @return TBox, соответствующий текущему лесу ограничений.
     */
    public TBox getTBox() {
        return t_box;
    }
    
    /**
     * Метод возвращает строку, соответствующую логике в которой описана база знаний.
     * @return Строковое представление логики.
     */
    public String getLogicString() {
        String ret = "";
        if(bl_O) ret += "O";
        if(bl_Q) ret += "Q"; else
            if(bl_N) ret += "N";
        if(bl_D) ret += "(D)";
        return ret;
    }
    
    /**
     * Метод осуществляет запись свойства RBox, соответствующего текущему лес ограничений.
     * @param new_r_box RBox, соответствующий текущему лесу ограничений.
     */
    public void setRBox(RBox new_r_box) {
        r_box = new_r_box;
    }
    
    /**
     * Массив для того, чтобы помечать те вершины леса ограничений, которые уже были отображены.
     */
    private int[] flag = new int[1024 * 1024];
    /**
     * Метод осуществляет вывод на экран описание концептов эквивалентных всем заданным в базе знаний.
     */
    public void showCD() {
        for(int i = 0; i < 1024 * 1024; i++)
            flag[i] = 0;
        
        for(int i = 0; i < t_box.getOrder().size(); i++) {
            System.out.println(concepts[t_box.getOrder().get(i)]);
            showTree(getNode(concept2int.get(concepts[t_box.getOrder().get(i)])).getDescription(), "");
            System.out.println();
        }
    }
    
    /**
     * Метод осуществляет вывод в консоль концепта с определенным индексом в структурированном виде.
     * @param curn Номер концепта в лесу ограничений, который необходимо вывести.
     * @param spaces Количество пробелов, необходимое для отступа.
     */
    public void showTree(int curn, String spaces) {
        int cur = curn; 
        if(cur < 0) cur = -cur;
        
        if(curn >= 1000) System.out.print(" "); else
            if(curn >= 100) System.out.print("  "); else
                if(curn >= 10) System.out.print("   "); else
                    if(curn >= 0) System.out.print("    "); else
                        if(curn < -100) System.out.print("   "); else
                            if(curn < -10) System.out.print("  "); else
                                if(curn < -0) System.out.print("   ");
        System.out.print(curn);
        System.out.print(" ");
        System.out.print(getNode(cur).getChildrenSize());

        if(curn == 1) System.out.println(spaces + "THING");
        if(curn == -1) System.out.println(spaces + "NOTHING");
        
        if(cur == 1 || cur == 0) return;
        
        System.out.print(spaces);
        if(curn < 0) {
            System.out.print("NOT ");
        }
        
        if(flag[cur] > 0) {
            //System.out.println(curn);
            if(getNode(cur).getNodeType() == NodeType.ntCONCEPT) {
                if(curn < 0) {
                    System.out.println(getNode(cur).getName());
                    return;
                } else {
                    System.out.println(getNode(cur).getName());
                    return;
                }
            }
            System.out.println(getNode(cur).getNodeType());
            return;
        }
        flag[cur] = 1;
        if(cur >= nodesSize) {
            System.out.printf("WTF!? NODE IN RULE GRAPH ISN'T EXISTS\n");
            System.out.flush();
            return;            
        }
        RuleNode current_node = getNode(cur);
        
        if(current_node.getNodeType() == NodeType.ntTHING || current_node.getNodeType() == NodeType.ntNOTHING) {
            System.out.printf("%s\n", current_node.getNodeType().toString());
            System.out.flush();
        } else
        if(current_node.getNodeType() == NodeType.ntCONCEPT) {
            if(curn < 0) {
                System.out.printf("%s\n", current_node.getName());
            } else {
                System.out.printf("%s\n", current_node.getName());
            }
            System.out.flush();
        } else {
            int rt = current_node.getRoleType();
            if(rt != -1) {
                if(current_node.getNodeType() != NodeType.ntSOME && current_node.getNodeType() != NodeType.ntALL) {
                    System.out.printf("%s %d %s\n", current_node.getNodeType().toString(), current_node.getNumberRestriction(), r_box.getRoleByIndex(rt).getName());                    
                } else {
                    System.out.printf("%s %s\n", current_node.getNodeType().toString(), r_box.getRoleByIndex(rt).getName());
                }
            } else System.out.printf("%s\n", current_node.getNodeType().toString());
            System.out.flush();
        }

        for(int i = 0; i < getNode(cur).getChildrenSize(); i++)
            showTree(getNode(cur).getChildren()[i], spaces + "   ");
    }
        
    /**
     * Метод осуществляет добавление вершины в массив всех вершин.
     * @param current_node Определяет объект {@link RuleNode} соответствующий добавляемой вершине
     * @return Порядковый номер вершины в массиве вершин.
     */
    public int addNode2RuleTree(RuleNode current_node) {
        if(nodesSize >= MaxNodesCount - 1) {
            increaseNodes();
        }
        nodes[nodesSize++] = current_node;
        return nodesSize - 1;
    }
    
    /**
     * Метод осуществляет добавление выражения в описание надконцепта.
     * Если описания надконцепта не существует то в его качестве будет установлено, 
     * заданное описание. Если концепт существует и не является конъюнкцией, то 
     * опсиание будет преобразовано в конъюнкцию, к которой будет добавлено заданное описание.
     * @param c_id Определяет номер концепта, к чьему описанию необходимо добавить концепт.
     * @param desc Определяет номер выражения, которое необходимо добавить к описанию над концепта.
     */
    public void addToSubDesciption(int c_id, int desc)
    {
        if(nodes[c_id].getSubDescription() == 0) {
            nodes[c_id].setSubDescription(desc);
        } else {
            int s_d = nodes[c_id].getSubDescription();
            if(s_d > 0 && nodes[Math.abs(s_d)].getNodeType() == NodeType.ntAND) {
                nodes[s_d].addChild(desc);
            } else {
                RuleNode rn = new RuleNode(NodeType.ntAND);
                int x = addNode2RuleTree(rn);
                rn.addChild(s_d);
                rn.addChild(desc);
                nodes[c_id].setSubDescription(x);
            }
        }
    }
        
    /**
     * Данный метод осуществляет нормализацию вершины леса ограничений.
     * В понятие нормализации входит раскрытие скобок если их наличие не меняет логику 
     * OWL-выражения.
     * @param node_index 
     */
    private void normalizeNode(int node_index) {
        if(f_nn[node_index] == 1) return;
        if(nodes[node_index].isEquivNode()) return;
        f_nn[node_index] = 1;
        
        int[] ar = nodes[node_index].getChildren();
        int arSize = nodes[node_index].getChildrenSize();
        ArrayList<Integer> temp = new ArrayList<Integer>();
        
        for(int i = 0; i < arSize; i++) {
            if(ar[i] < 0) continue;
            if((nodes[ar[i]].getNodeType() == NodeType.ntAND) && (nodes[node_index].getNodeType() == NodeType.ntAND)) {
                normalizeNode(ar[i]);
                for(int j = 0; j < nodes[ar[i]].getChildrenSize(); j++) {
                    temp.add(nodes[ar[i]].getChildren()[j]);
                }
            }
            if((nodes[ar[i]].getNodeType() == NodeType.ntOR) && (nodes[node_index].getNodeType() == NodeType.ntOR)) {
                normalizeNode(ar[i]);
                for(int j = 0; j < nodes[ar[i]].getChildrenSize(); j++) {
                    temp.add(nodes[ar[i]].getChildren()[j]);
                }
            }
        }
        
        for(int i = 0; i < arSize; i++) {
            if(ar[i] < 0) continue;
            if((nodes[ar[i]].getNodeType() == NodeType.ntAND) && (nodes[node_index].getNodeType() == NodeType.ntAND))
                if(nodes[node_index].deleteChild(ar[i])) {
                    i--; continue;
                }

            if((nodes[ar[i]].getNodeType() == NodeType.ntOR) && (nodes[node_index].getNodeType() == NodeType.ntOR))
                if(nodes[node_index].deleteChild(ar[i])) {
                    i--; continue;
                }
        }
        
        for(int i = 0; i < temp.size(); i++)
            nodes[node_index].addChild(temp.get(i));
        
    }
    
    /**
     * Массив для определения тех вершин лес ограничений, которые уже нормализованы.
     */
    private int[] f_nn;
    /**
     * Метод осуществляющий нормализацию всех вершин графа.
     */
    public void normalizeGraph() {
        f_nn = new int[nodesSize + 1];
        for(int i = 0; i < nodesSize + 1; i++)
            f_nn[i] = 0;

        for(int i = 0; i < nodesSize; i++) {
            normalizeNode(i);
        }
    }
    
    private SHash cache = new SHash();
    private int[] sortedVertices;
    private int[] heights;
    private IntPair[] NWH; // nodes with heights
    private int NWHCount = 0;
    private int[] exchanges;
    
    /**
     * Класс для осуществления сравнения объектов класса {@link IntPair}
     */
    private class PairComparor implements Comparator<IntPair> {
        @Override
        public int compare(IntPair o1, IntPair o2) {
            if(o1.y == o2.y) {
                if(o1.x > o2.x) 
                    return 1; else
                if(o1.x < o2.x)
                    return -1;
                return 0;
            }
            if(o1.y > o2.y) 
                return 1; else
            if(o1.y < o2.y)
                return -1;
            return 0;
        }
    }
    
    /**
     * Метод осуществляет топологическую сортировку вершин в лесу ограничений.
     * @param x Номер вершины, из которой осуществляется обход графа.
     */
    private void sortVerticesDFS(int x) {
        if(heights[x] != 0) return;
        heights[x] = 1;
        for(int i = 0; i < nodes[x].getChildrenSize(); i++) {
            sortVerticesDFS(Math.abs(nodes[x].getChildren()[i]));
            if( heights[x] < heights[Math.abs(nodes[x].getChildren()[i])] + 1)
                heights[x] = heights[Math.abs(nodes[x].getChildren()[i])] + 1;
        }
        NWH[NWHCount++] = new IntPair(x, heights[x]);
    }

    /**
     * В методе осуществляется замена вершины-конъюнкции или вершины-дизъюнкции если она имеют не более одного потомка.
     * @param node_index Номер вершины леса ограничений.
     */
    private void del(int node_index) {
        int[] ar = nodes[Math.abs(node_index)].getChildren();
        int arSize = nodes[Math.abs(node_index)].getChildrenSize();
        ArrayList<Integer> temp = new ArrayList<Integer>();
        
        for(int i = 0; i < arSize; i++) {
            del(ar[i]);
            if(((nodes[Math.abs(ar[i])].getNodeType() == NodeType.ntAND) || (nodes[Math.abs(ar[i])].getNodeType() == NodeType.ntOR)) && 
                (nodes[Math.abs(ar[i])].getChildrenSize() == 1)) {
                for(int j = 0; j < nodes[Math.abs(ar[i])].getChildrenSize(); j++) {
                    temp.add(nodes[Math.abs(ar[i])].getChildren()[j]);
                }
            }
        }

        for(int i = 0; i < arSize; i++) {
            if(((nodes[Math.abs(ar[i])].getNodeType() == NodeType.ntAND) || (nodes[Math.abs(ar[i])].getNodeType() == NodeType.ntOR)) && 
                (nodes[Math.abs(ar[i])].getChildrenSize() == 1))
                if(nodes[Math.abs(node_index)].deleteChild(ar[i])) {
                    i--; continue;
                }
            
        }
        for(int i = 0; i < temp.size(); i++)
            nodes[Math.abs(node_index)].addChild(temp.get(i));        

        Arrays.sort(nodes[Math.abs(node_index)].getChildren(), 0, nodes[Math.abs(node_index)].getChildrenSize());
        for(int i = 1; i < nodes[Math.abs(node_index)].getChildrenSize(); i++) {
            if(nodes[Math.abs(node_index)].getChildren()[i - 1] == nodes[Math.abs(node_index)].getChildren()[i]) {
                nodes[Math.abs(node_index)].deleteChild(nodes[Math.abs(node_index)].getChildren()[i]);
                i--;
            }
        }
    }

    /**
     * В методе осуществляется замена вершин-конъюнкций и вершин-дизъюнкций если они имеют не более одного потомка.
     */
    public void delOnes() {
        for(int i = 0; i < conceptSize; i++) {
            del(nodes[concept2int.get(concepts[i])].getDescription());
            del(nodes[concept2int.get(concepts[i])].getSubDescription());
        }
    }
    
    /**
     * В методе осуществляется добавление дизъюнктов из описания эквивалентного концепта
     * в описание надконцепта.
     */
    public void EqvDescriptionProcess() {
        for(int i = 0; i < conceptSize; i++) {
            int hlp = nodes[concept2int.get(concepts[i])].getDescription();
            if(hlp > 0) {
                if(nodes[hlp].getNodeType() == NodeType.ntOR) {
                    for(int j = 0; j < nodes[hlp].getChildrenSize(); j++) {
                        int h = nodes[hlp].getChildren()[j];
                        addToSubDesciption(h, concept2int.get(concepts[i]));
                    }
                }
            }
        }
    }
    
    private ArrayList<Integer> verts = new ArrayList<Integer>();
    private ArrayList<Integer> index = new ArrayList<Integer>();
    private int[] f = new int[1024 * 128];
    private HashSet<Integer> con = new HashSet<Integer>();
    private ArrayList<Integer> deleted = new ArrayList<Integer>();
    OWLDataFactory df = null;
    OWLOntologyManager manager = null;
    OWLOntology Ontology = null;
    
    /**
     * Метод осуществляет вывод всех аксиом содержащихся в лесу ограничений.
     * Все аксиомы выводятся в виде XML-синтаксиса в OWL-файл с заданным именем
     * @param file_name Задает имя файла для вывода онтологии
     */
    public void printToOWL(String file_name)
    {
        df = OWLManager.getOWLDataFactory();
        manager = OWLManager.createOWLOntologyManager();
        try {
            Ontology = manager.createOntology(IRI.create(new File(file_name)));
        } catch (OWLOntologyCreationException ex) {
            System.err.println("Can't create " + file_name);
        }

        for(int i = 0; i < conceptSize; i++) {
            if(nodes[concept2int.get(concepts[i])].getDescription() != 0) {
                OWLAxiom sca = null;
                sca = df.getOWLEquivalentClassesAxiom(
                        df.getOWLClass(IRI.create(nodes[concept2int.get(concepts[i])].getName())), 
                        recOutToOWL(nodes[concept2int.get(concepts[i])].getDescription()));
                AddAxiom addAxiom = new AddAxiom(Ontology, sca);
                manager.applyChange(addAxiom);
                try {
                    manager.saveOntology(Ontology);
                } catch (OWLOntologyStorageException ex) {
                    System.err.println("Can't save ontology to " + file_name);
                }
            }
            if(nodes[concept2int.get(concepts[i])].getSubDescription() != 0) {
                OWLAxiom sca = null;
                sca = df.getOWLSubClassOfAxiom(
                        df.getOWLClass(IRI.create(nodes[concept2int.get(concepts[i])].getName())), 
                        recOutToOWL(nodes[concept2int.get(concepts[i])].getSubDescription()));
                AddAxiom addAxiom = new AddAxiom(Ontology, sca);
                manager.applyChange(addAxiom);
                try {
                    manager.saveOntology(Ontology);
                } catch (OWLOntologyStorageException ex) {
                    System.err.println("Can't save ontology to " + file_name);
                }
            }
        }
    }
    
    /**
     * В методе осуществляется вывод концепта из леса ограничений в выражение OWL.
     * @param rut Номер вершины из леса ограничений.
     * @return OWL-выражение, соответствующее поддереву леса ограничений в заданной вершине.
     */
    private OWLClassExpression recOutToOWL(int rut)
    {
        if(rut < 0) {
            return df.getOWLObjectComplementOf(recOutToOWL(-rut));
        }
        if(nodes[rut].getNodeType() == NodeType.ntAND) {
            HashSet<OWLClassExpression> sce = new HashSet<OWLClassExpression>();
            for(int i = 0; i < nodes[rut].getChildrenSize(); i++) {
                sce.add(recOutToOWL(nodes[rut].getChildren()[i]));
            }
            return df.getOWLObjectIntersectionOf(sce);
        }
        if(nodes[rut].getNodeType() == NodeType.ntOR) {
            HashSet<OWLClassExpression> sce = new HashSet<OWLClassExpression>();
            for(int i = 0; i < nodes[rut].getChildrenSize(); i++) {
                sce.add(recOutToOWL(nodes[rut].getChildren()[i]));
            }
            return df.getOWLObjectUnionOf(sce);
        }
        if(nodes[rut].getNodeType() == NodeType.ntSOME) {
            return df.getOWLObjectSomeValuesFrom(
                    df.getOWLObjectProperty(IRI.create(r_box.getRoleByIndex(nodes[rut].getRoleType()).getName())),
                    recOutToOWL(nodes[rut].getChildren()[0]));
        }
        if(nodes[rut].getNodeType() == NodeType.ntALL) {
            return df.getOWLObjectAllValuesFrom(
                    df.getOWLObjectProperty(IRI.create(r_box.getRoleByIndex(nodes[rut].getRoleType()).getName())),
                    recOutToOWL(nodes[rut].getChildren()[0]));
        }
        if(nodes[rut].getNodeType() == NodeType.ntMAXCARD) {
            if(nodes[rut].getChildrenSize() == 0) {
                return df.getOWLObjectMaxCardinality(nodes[rut].getNumberRestriction(), 
                        df.getOWLObjectProperty(IRI.create(r_box.getRoleByIndex(nodes[rut].getRoleType()).getName())), 
                        df.getOWLThing());
            }
            return df.getOWLObjectMaxCardinality(nodes[rut].getNumberRestriction(), 
                    df.getOWLObjectProperty(IRI.create(r_box.getRoleByIndex(nodes[rut].getRoleType()).getName())), 
                    recOutToOWL(nodes[rut].getChildren()[0]));
        }
        if(nodes[rut].getNodeType() == NodeType.ntMINCARD) {
            if(nodes[rut].getChildrenSize() == 0) {
                return df.getOWLObjectMinCardinality(nodes[rut].getNumberRestriction(), 
                        df.getOWLObjectProperty(IRI.create(r_box.getRoleByIndex(nodes[rut].getRoleType()).getName())), 
                        df.getOWLThing());
            }
            return df.getOWLObjectMinCardinality(nodes[rut].getNumberRestriction(), 
                    df.getOWLObjectProperty(IRI.create(r_box.getRoleByIndex(nodes[rut].getRoleType()).getName())), 
                    recOutToOWL(nodes[rut].getChildren()[0]));
        }
        if(nodes[rut].getNodeType() == NodeType.ntTHING) {
            return df.getOWLThing();
        }
        if(nodes[rut].getNodeType() == NodeType.ntNOTHING) {
            return df.getOWLNothing();
        }
        if(nodes[rut].getName() == null) {
            int kor = 124;
            return null;
        } else
        return df.getOWLClass(IRI.create(nodes[rut].getName()));
    }
    
    /**
     * Метод осуществляет преобразование древовидной структуры леса ограничений в графовидную.
     */
    public void makeDAG() {
        sortedVertices = new int[nodesSize + 1];
        int[] roots = new int[nodesSize + 1];
        exchanges = new int[nodesSize + conceptSize];
        heights = new int[nodesSize + 1];
        NWH = new IntPair[nodesSize + 1];
        
        Arrays.fill(roots, 0);
        Arrays.fill(heights, 0);
        Arrays.fill(exchanges, 0);
        Arrays.fill(sortedVertices, 0);
        
        for(int i = 0; i < nodesSize; i++)
            for(int j = 0; j < nodes[i].getChildrenSize(); j++)
                roots[Math.abs(nodes[i].getChildren()[j])] = 1;

        //sort vertices by depth
        for(int i = 0; i < nodesSize; i++) {
            if(roots[i] == 1) continue;
            sortVerticesDFS(i);
        }
        
        Arrays.sort(NWH, 0, NWHCount, new PairComparor());
        
        for(int i = 0; i < NWHCount; i++) {
            for(int j = 0; j < nodes[NWH[i].x].getChildrenSize(); j++) {
                int x = nodes[NWH[i].x].getChildren()[j];
                if(exchanges[Math.abs(x)] != 0) {
                    int y = exchanges[Math.abs(x)];
                    if(x < 0) y *= -1;
                    nodes[NWH[i].x].getChildren()[j] = y;
                }
            }
            
            int res = cache.add(nodes[NWH[i].x].getChildren(), nodes[NWH[i].x].getChildrenSize(), NWH[i].x, nodes[NWH[i].x].getNodeType().ordinal(), nodes[NWH[i].x].getRoleType());
            if(res == 0) continue;
            
            if((nodes[NWH[i].x].getNodeType() == NodeType.ntAND) && 
               ((res < 0 && (nodes[Math.abs(res)].getNodeType() == NodeType.ntOR)) || 
                (res > 0 && (nodes[Math.abs(res)].getNodeType() == NodeType.ntAND)))) {
                exchanges[NWH[i].x] = res;
            }

            if((nodes[NWH[i].x].getNodeType() == NodeType.ntOR) && 
               ((res < 0 && (nodes[Math.abs(res)].getNodeType() == NodeType.ntAND)) || 
                (res > 0 && (nodes[Math.abs(res)].getNodeType() == NodeType.ntOR)))) {
                exchanges[NWH[i].x] = res;
            }

            if((nodes[NWH[i].x].getNodeType() == NodeType.ntSOME) && (nodes[NWH[i].x].getRoleType() == nodes[Math.abs(res)].getRoleType()) &&
               ((res < 0 && (nodes[Math.abs(res)].getNodeType() == NodeType.ntALL)) || 
                (res > 0 && (nodes[Math.abs(res)].getNodeType() == NodeType.ntSOME)))) {
                exchanges[NWH[i].x] = res;
            }

            if((nodes[NWH[i].x].getNodeType() == NodeType.ntALL) && (nodes[NWH[i].x].getRoleType() == nodes[Math.abs(res)].getRoleType()) &&
               ((res < 0 && (nodes[Math.abs(res)].getNodeType() == NodeType.ntSOME)) || 
                (res > 0 && (nodes[Math.abs(res)].getNodeType() == NodeType.ntALL)))) {
                exchanges[NWH[i].x] = res;
            }
        }
        
        for(int i = 0; i < NWHCount; i++) {
            int x = nodes[NWH[i].x].getDescription();
            if(x != 0) {
                int sign = 1; if(x < 0) sign = -1;
                if(exchanges[Math.abs(x)] != 0) {
                    nodes[NWH[i].x].setDescription(sign * exchanges[Math.abs(x)]);
                }
                if(sign > 0) {
                    int id = nodes[exchanges[x]].getDescription();
                    if(id == 0) {
                        nodes[exchanges[x]].setDescription(NWH[i].x);
                    } else
                    if(getNode(id).getNodeType() != NodeType.ntAND) {
                        int new_node = addNode2RuleTree(new RuleNode(NodeType.ntAND));
                        getNode(new_node).addChild(id);
                        getNode(new_node).addChild(NWH[i].x);
                        nodes[exchanges[x]].setDescription(new_node);                        
                    } else {
                        getNode(id).addChild(NWH[i].x);
                    }
                }
            }

            x = nodes[NWH[i].x].getSubDescription();
            if(x != 0) {
                int sign = 1; if(x < 0) sign = -1;
                if(exchanges[Math.abs(x)] != 0) nodes[NWH[i].x].setSubDescription(sign * exchanges[Math.abs(x)]);
            }
        }
        
        for(int i = 0; i < NWHCount; i++) { //Lazy unfolding preprocess - search cases when C = A1 and A2 and ... and AM and change it to A1 [= C or -A2 or -A3 
            if(nodes[NWH[i].x].getNodeType() == NodeType.ntCONCEPT && nodes[NWH[i].x].getDescription() != 0) {
                int x = nodes[NWH[i].x].getDescription();
                if(x > 0) if(nodes[x].getNodeType() == NodeType.ntAND) {
                    for(int j = 0; j < nodes[x].getChildrenSize(); j++) {
                        int y = nodes[x].getChildren()[j];
                        if(y < 0) {
                            //Must to do something!
                            continue;
                        }
                        if(nodes[y].getNodeType() == NodeType.ntCONCEPT) {
                            RuleNode rn = new RuleNode(NodeType.ntOR);
                            int sbd = nodes[y].getSubDescription();
                            for(int k = 0; k < nodes[x].getChildrenSize(); k++) {
                                if(k == j) continue;
                                rn.addChild(-nodes[x].getChildren()[k]);
                            }
                            rn.addChild(NWH[i].x);
                            if(rn.getChildrenSize() > 1) {
                                if(sbd == 0) {
                                    nodes[y].setSubDescription(addNode2RuleTree(rn));
                                } else {
                                    if(sbd < 0) {
                                        RuleNode new_rn = new RuleNode(NodeType.ntAND);
                                        new_rn.addChild(sbd);
                                        new_rn.addChild(addNode2RuleTree(rn));
                                        nodes[y].setSubDescription(addNode2RuleTree(new_rn));
                                    } else {
                                        if(nodes[sbd].getNodeType() == NodeType.ntAND) {
                                            nodes[sbd].addChild(addNode2RuleTree(rn));
                                        } else {
                                            RuleNode new_rn = new RuleNode(NodeType.ntAND);
                                            new_rn.addChild(sbd);
                                            new_rn.addChild(addNode2RuleTree(rn));
                                            nodes[y].setSubDescription(addNode2RuleTree(new_rn));
                                        }
                                    }
                                }
                            } else {
                                if(sbd == 0) {
                                    nodes[y].setSubDescription(rn.getChildren()[0]);
                                } else {
                                    if(sbd > 0 && nodes[sbd].getNodeType() == NodeType.ntAND) {
                                        nodes[sbd].addChild(addNode2RuleTree(rn));
                                    } else {
                                        RuleNode new_rn = new RuleNode(NodeType.ntAND);
                                        new_rn.addChild(sbd);
                                        new_rn.addChild(rn.getChildren()[0]);
                                        nodes[y].setSubDescription(addNode2RuleTree(new_rn));
                                    }
                                }
                            }
                            if(nodes[NWH[i].x].getSubDescription() == 0) {
                                nodes[NWH[i].x].setSubDescription(nodes[NWH[i].x].getDescription());
                            } else {
                                RuleNode rn1 = new RuleNode(NodeType.ntAND);
                                rn1.addChild(nodes[NWH[i].x].getDescription());
                                rn1.addChild(nodes[NWH[i].x].getSubDescription());                                
                                nodes[NWH[i].x].setSubDescription(addNode2RuleTree(rn1));
                            }
                            break;
                        }
                    }
                }
            }
        }
        //System.out.print("COUNT OF ELEMENTS IN HASH ");
        //System.out.println(cache.countOfElements());
    }
    
    /**
     * Очищает классы эквивалентности всех вершин для алгоритма поиска автоморфизмов.
     */
    public void clearCacheClasses() {
        for(int i = 0; i < nodesSize; i++)
            nodes[i].setCacheClass(0);
    }
    
    /**
     * Метод добавляет OWL-выражение в лес ограничений и возвращает номер вершины.
     * @param classExpr OWL-выражение концепта.
     * @return Номер вершины леса ограничений, соответствующей заданному выражению.
     */
    private int buildRuleTree(OWLClassExpression classExpr) {
        RuleNode help = null;
        int cn = 0;

        ExpressionTransformer expr_transform = new ExpressionTransformer(t_box);
        classExpr.accept(expr_transform);
        bl_N |= expr_transform.bl_N; bl_O |= expr_transform.bl_O; bl_Q |= expr_transform.bl_Q; bl_D |= expr_transform.bl_D;
        switch (classExpr.getClassExpressionType()) {
            case OBJECT_COMPLEMENT_OF: {
                return -buildRuleTree(expr_transform.getObjectFiller());            
            }
            case OBJECT_INTERSECTION_OF: {
                help = new RuleNode(NodeType.ntAND);
                for(OWLClassExpression current_class: expr_transform.getClassesList())
                    help.addChild(buildRuleTree(current_class));
                break;
            }
            case OBJECT_ONE_OF: {
                help = new RuleNode(NodeType.ntOR);
                for(OWLIndividual oi: expr_transform.getIndivids())
                    help.addChild(findIndivid(oi));
                break;
            }
            case OBJECT_HAS_VALUE: {
                help = new RuleNode(NodeType.ntSOME, expr_transform.getRoleType());
                help.addChild(findIndivid(expr_transform.getIndividual()));
                break;
            }
            case OBJECT_HAS_SELF: {
                help = new RuleNode(NodeType.ntHASSELF);
                break;
            }
            case OBJECT_UNION_OF: {
                help = new RuleNode(NodeType.ntOR);
                for(OWLClassExpression current_class: expr_transform.getClassesList())
                    help.addChild(buildRuleTree(current_class));
                break;
            }
            case OBJECT_ALL_VALUES_FROM: {
                help = new RuleNode(NodeType.ntALL, expr_transform.getRoleType());
                help.addChild(buildRuleTree(expr_transform.getObjectFiller()));
                break;
            }
            case OBJECT_SOME_VALUES_FROM: {
                help = new RuleNode(NodeType.ntSOME, expr_transform.getRoleType());
                help.addChild(buildRuleTree(expr_transform.getObjectFiller()));
                break;
            }
            case OBJECT_MAX_CARDINALITY: {
                help = new RuleNode(NodeType.ntMAXCARD, expr_transform.getRoleType(), expr_transform.getNumberR());
                help.addChild(buildRuleTree(expr_transform.getObjectFiller()));
                break;
            }
            case OBJECT_MIN_CARDINALITY: {
                help = new RuleNode(NodeType.ntMINCARD, expr_transform.getRoleType(), expr_transform.getNumberR());
                help.addChild(buildRuleTree(expr_transform.getObjectFiller()));
                break;
            }
            case OBJECT_EXACT_CARDINALITY: {
                if(expr_transform.getNumberR() == 0) {
                    help = new RuleNode(NodeType.ntALL, expr_transform.getRoleType());
                    help.addChild(-buildRuleTree(expr_transform.getObjectFiller()));
                    break;
                }
                help = new RuleNode(NodeType.ntAND);
                cn = buildRuleTree(expr_transform.getObjectFiller());
                RuleNode r1 = new RuleNode(
                        NodeType.ntMAXCARD, 
                        expr_transform.getRoleType(), 
                        expr_transform.getNumberR());

                RuleNode r2 = new RuleNode(
                        NodeType.ntMINCARD, 
                        expr_transform.getRoleType(), 
                        expr_transform.getNumberR());
                
                r1.addChild(cn); r2.addChild(cn);
                help.addChild(addNode2RuleTree(r1));
                help.addChild(addNode2RuleTree(r2));
                break;
            }
            case OWL_CLASS: {
                if(classExpr.isOWLThing()) {
                    thing = classExpr.asOWLClass().toStringID();
                    return  1;
                }
                if(classExpr.isOWLNothing()) {
                    nothing = classExpr.asOWLClass().toStringID();
                    return -1;
                }
                if(!classExpr.isAnonymous()) {
                    //return findConcept(classExpr.asOWLClass().getIRI().getFragment());
                    return findConcept(classExpr.asOWLClass().getIRI().toString());
                } else {
                    System.out.println("ERROR: anonymus class founded");
                    return -1;
                }
            }
            
            case DATA_ALL_VALUES_FROM: {
                help = new RuleNode(NodeType.ntALL, expr_transform.getRoleType());
                help.addChild(buildDataRuleTree(expr_transform.getDataFiller()));
                break;
            }
            case DATA_SOME_VALUES_FROM: {
                help = new RuleNode(NodeType.ntSOME, expr_transform.getRoleType());
                help.addChild(buildDataRuleTree(expr_transform.getDataFiller()));
                break;
            }
            case DATA_MAX_CARDINALITY: {
                help = new RuleNode(NodeType.ntMAXCARD, expr_transform.getRoleType(), expr_transform.getNumberR());
                help.addChild(buildDataRuleTree(expr_transform.getDataFiller()));
                break;
            }
            case DATA_MIN_CARDINALITY: {
                help = new RuleNode(NodeType.ntMINCARD, expr_transform.getRoleType(), expr_transform.getNumberR());
                help.addChild(buildDataRuleTree(expr_transform.getDataFiller()));
                break;
            }
            case DATA_EXACT_CARDINALITY: {
                help = new RuleNode(NodeType.ntAND);
                cn = buildDataRuleTree(expr_transform.getDataFiller());
                RuleNode r1 = new RuleNode(
                        NodeType.ntMAXCARD, 
                        expr_transform.getRoleType(), 
                        expr_transform.getNumberR());

                RuleNode r2 = new RuleNode(
                        NodeType.ntMINCARD, 
                        expr_transform.getRoleType(), 
                        expr_transform.getNumberR());
                r1.addChild(cn); r2.addChild(cn);
                help.addChild(addNode2RuleTree(r1));
                help.addChild(addNode2RuleTree(r2));
                break;
            }
            case DATA_HAS_VALUE: {
                help = new RuleNode(NodeType.ntSOME, expr_transform.getRoleType());
                help.addChild(findLiter(expr_transform.getDataValue()));
                break;
            }
        }
        
        cn = addNode2RuleTree(help);
        return cn;
    }
    
    ////////////////////////////////////////////////////////////////////////
    //Fast synthetic analysis with own stack for *.alc files and *.tkb files
    int[] stak;
    RuleNode[] f_op;
    int stak_size = 0;
    int root = 0;
    
    /**
     * Осуществляет добавление вершины в лес ограничений.
     * @param tokens Токены строки, в соответствии с которой определяются тип вершины, роль, количественные ограничения.
     * @param tokens_count Размерность массива токенов.
     */
    private void addVertice(String[] tokens, int tokens_count) {
        int k = 1;
        if(tokens_count > 0) {
            if(tokens[0].equalsIgnoreCase("NOT")) {
                f_op[stak_size - k] = new RuleNode(NodeType.ntNOT);
                if(stak_size > 0 && tokens_count > 1) {
                    int mult = 1, l = 1;
                    while(stak_size - l >= 0) {
                        if(f_op[stak_size - l].getNodeType() == NodeType.ntNOT)  mult = mult * -1; else {
                            f_op[stak_size - l].addChild(mult * findConcept(tokens[1])); //it must be only one token after NOT
                            break;
                        }
                        l++;
                    }
                    if(stak_size - l <= 0) {
                        root = mult * findConcept(tokens[1]); //it must be only one token after NOT
                    }
                } else {
                    if(tokens_count > 1) root = -findConcept(tokens[1]);
                }
            } else
            if(tokens[0].equalsIgnoreCase("AND")) {
                f_op[stak_size - k] = new RuleNode(NodeType.ntAND);
                for(int j = 1; j < tokens_count; j++)
                    f_op[stak_size - k].addChild(findConcept(tokens[j]));
            } else
            if(tokens[0].equalsIgnoreCase("OR")) {
                f_op[stak_size - k] = new RuleNode(NodeType.ntOR);
                for(int j = 1; j < tokens_count; j++)
                    f_op[stak_size - k].addChild(findConcept(tokens[j]));
            } else
            if(tokens[0].equalsIgnoreCase("SOME")) {
                f_op[stak_size - k] = new RuleNode(NodeType.ntSOME, r_box.findRole(tokens[1]));
                for(int j = 2; j < tokens_count; j++)
                    f_op[stak_size - k].addChild(findConcept(tokens[j]));
            } else
            if(tokens[0].equalsIgnoreCase("ALL")) {
                f_op[stak_size - k] = new RuleNode(NodeType.ntALL, r_box.findRole(tokens[1]));
                for(int j = 2; j < tokens_count; j++)
                    f_op[stak_size - k].addChild(findConcept(tokens[j]));
            } else
            if(tokens[0].equalsIgnoreCase("at-most")) {
                bl_N = true;
                if(tokens_count >= 3) bl_Q = true;
                f_op[stak_size - k] = new RuleNode(NodeType.ntMAXCARD, r_box.findRole(tokens[2]), Integer.parseInt(tokens[1]));
                for(int j = 3; j < tokens_count; j++)
                    f_op[stak_size - k].addChild(findConcept(tokens[j]));
            } else
            if(tokens[0].equalsIgnoreCase("at-least")) {
                bl_N = true;
                if(tokens_count >= 3) bl_Q = true;
                f_op[stak_size - k] = new RuleNode(NodeType.ntMINCARD, r_box.findRole(tokens[2]), Integer.parseInt(tokens[1]));
                for(int j = 3; j < tokens_count; j++)
                    f_op[stak_size - k].addChild(findConcept(tokens[j]));
            } else
            if(tokens[0].equalsIgnoreCase("exactly")) {
                bl_N = true;
                if(tokens_count >= 3) bl_Q = true;
                f_op[stak_size - k] = new RuleNode(NodeType.ntEXTCARD, r_box.findRole(tokens[2]), Integer.parseInt(tokens[1]));
                for(int j = 3; j < tokens_count; j++)
                    f_op[stak_size - k].addChild(findConcept(tokens[j]));
            } else {
                if(stak_size > 0) {
                    int mult = 1, l = 1;
                    while(stak_size - l >= 0) {
                        if(f_op[stak_size - l].getNodeType() == NodeType.ntNOT)  mult = mult * -1; else {
                            f_op[stak_size - l].addChild(mult * findConcept(tokens[0])); //it must be only one token after NOT
                            break;
                        }
                        l++;
                    }
                    if(stak_size - l == 0) {
                        root = mult * findConcept(tokens[0]); //it must be only one token after NOT
                    }
                } else root = -findConcept(tokens[0]);
            }
        }
    }
    
    /**
     * Метод добавляет строковое выражение в лес ограничений и возвращает номер вершины.
     * @param expr Строковое выражение концепта.
     * @return Номер вершины леса ограничений, соответствующей заданному выражению.
     */
    private int buildRuleTreeByStackHelp(String expr) {
        stak = new int[expr.length()];
        f_op = new RuleNode[expr.length()];
        for(int i = 0; i < expr.length(); i++)
            f_op[i] = null;
        
        String cur_token = "";
        String[] tokens = new String[1024];
        int tokens_count = 0;
        
        for(int i = 0; i < expr.length(); i++) {
            if(expr.charAt(i) == '(') {
                if(cur_token.length() > 0) tokens[tokens_count++] = skipDels(cur_token);
                addVertice(tokens, tokens_count);
                stak[stak_size++] = i;

                cur_token = "";
                tokens_count = 0;
            } else
            if(expr.charAt(i) == ')') {
                if(cur_token.length() > 0) tokens[tokens_count++] = skipDels(cur_token);
                addVertice(tokens, tokens_count);
                stak_size--;
                if(f_op[stak_size].getNodeType() == NodeType.ntNOT) {
                    cur_token = "";
                    tokens_count = 0;
                    continue;
                }
                int cn = addNode2RuleTree(f_op[stak_size]);
                if(stak_size > 0) {
                    int mult = 1, l = 1;
                    while(stak_size - l >= 0) { //there was the bug where NOT in the begin wasn't added
                        if(f_op[stak_size - l].getNodeType() == NodeType.ntNOT)  mult = mult * -1; else {
                            f_op[stak_size - l].addChild(mult * cn);
                            break;
                        }
                        l++;
                    }
                    if(stak_size - l < 0) {
                        root = mult * cn;
                    } else {
                        root = cn;
                    }
                } else root = cn;
                
                cur_token = "";
                tokens_count = 0;
            } else
            if(isSkippedSymbol(expr.charAt(i))) {
                if(cur_token.length() > 0) tokens[tokens_count++] = skipDels(cur_token);
                cur_token = "";
            } else {
                cur_token += expr.charAt(i);
            }
        }
        if(cur_token.length() > 0) return findConcept(cur_token);
        if(tokens_count > 0) return findConcept(tokens[0]);
        return root;
    }
    ////////////////////////////////////////////////////////////////////////
    
    /**
     * Метод добавляет OWL-выражение, соответствующее типу данных в лес ограничений и возвращает номер вершины.
     * @param dataExpr OWL-выражение, соответствующее типу данных концепта.
     * @return Номер вершины леса ограничений, соответствующей заданному выражению.
     */
    private int buildDataRuleTree(OWLDataRange dataExpr) {
        RuleNode help = null;
        DataExpressionTransformer data_trans = new DataExpressionTransformer();
        dataExpr.accept(data_trans);
        switch (dataExpr.getDataRangeType()) {
            case DATA_COMPLEMENT_OF: {
                help = new RuleNode(NodeType.ntNOT);
                help.addChild(addDataExpr2Graph(data_trans.getDataFiller()));
                break;
            }
            case DATA_INTERSECTION_OF: {
                help = new RuleNode(NodeType.ntAND);
                for(OWLDataRange iterator: data_trans.getDataFillerSet())
                    help.addChild(buildDataRuleTree(iterator));
                break;
            }
            case DATA_UNION_OF: {
                help = new RuleNode(NodeType.ntOR);
                for(OWLDataRange iterator: data_trans.getDataFillerSet())
                    help.addChild(buildDataRuleTree(iterator));
                break;
            }
            case DATA_ONE_OF: {
                help = new RuleNode(NodeType.ntOR);
                for(OWLLiteral iterator: data_trans.getLiters())
                    help.addChild(findLiter(iterator));
                break;
            }
            case DATATYPE: {
                help = new RuleNode(NodeType.ntDATATYPE);
                help.setDatatype(data_trans.getDatatype());
                break;
            }
            case DATATYPE_RESTRICTION: {
                help = new RuleNode(NodeType.ntAND);
                for(OWLFacet of: data_trans.getFacetSet()) {
                    RuleNode help1 = new RuleNode(NodeType.ntDATATYPE);
                    help1.setFacet(of);
                    help.addChild(addNode2RuleTree(help1));
                }
                break;
            }
        }
        return addNode2RuleTree(help);
    }
    
    /**
     * Класс реализующий шаблон визиторов visitor pattern.
     * Данный класс необходим, чтобы преобразовать выражения загруженные с помощью OWL API
     * в собственные структуры.
     */
    private class ExpressionTransformer implements OWLClassExpressionVisitor {
        private OWLClassExpression object_filler;
        private OWLDataRange data_filler;
        private OWLIndividual object_value;
        private OWLLiteral data_value;
        
        private List<OWLClassExpression> classes;
        private Set<OWLIndividual> individs;
        private TBox t_box;
        private RBox r_box;
        
        private int role_type;
        private int number_r;
        
        public boolean bl_N = false;
        public boolean bl_Q = false;
        public boolean bl_O = false;
        public boolean bl_D = false;
        
        /**
         * Основной конструтор класса, в котором устанавливается ссылка на лес ограничений, 
         * в который будут добавляться вершины.
         * @param new_t_box Ссылка на лес ограничений.
         */
        public ExpressionTransformer(TBox new_t_box) {
            t_box = new_t_box;
            r_box = t_box.getRBox();
            individs = null;
            classes = null;
            object_filler = null;
            data_filler = null;
            object_value = null;
            data_value = null;
        }
        
        /**
         * Метод возвращает массив OWL-выражений из правила AND или OR.
         * @return Массив выражений OWL.
         */
        public List<OWLClassExpression> getClassesList() {
            return classes;
        }
        
        /**
         * Возвращает индекс роли в RBox.
         * Метод создан для обработки таких структуру как кванторы и ограничения кардинальности.
         * @return Индекс роли RBox.
         */
        public int getRoleType() {
            return role_type;
        }
        
        /**
         * Возвращает ограничение кардинальности правила.
         * Метод создан для обработки таких структуру как ограничения кардинальности.
         * @return Ограничение кардинальности роли.
         */
        public int getNumberR() {
            return number_r;
        }
        
        /**
         * Возвращает ссылку на литеру, которая появилась в правиле.
         * @return Ссылка на литеру.
         */
        public OWLLiteral getDataValue() {
            return data_value;
        }
        
        /**
         * Возвращает ссылку на индивида, который появилась в правиле.
         * @return Ссылка на индивида.
         */
        public OWLIndividual getIndividual() {
            return object_value;
        }
        
        /**
         * Метод возвращает ссылку на объект-выражение OWL, который находится под квантором.
         * @return Ссылка на выражение OWL.
         */
        public OWLClassExpression getObjectFiller() {
            return object_filler;
        }
        
        /**
         * Метод возвращает ссылку на объект-выражение типа данных OWL, который находится под квантором.
         * @return Ссылка на выражение типа данных OWL.
         */
        public OWLDataRange getDataFiller() {
            return data_filler;
        }
        
        /**
         * Возвращает множество индивидов из правила ONE OF.
         * @return Множество индивидов.
         */
        public Set<OWLIndividual> getIndivids() {
            return individs;
        }
        
        /**
         * Метод для разбора выражения являющегося атомарным концептом.
         * Не используется при обработке концептов, поэтому имеет пустое тело.
         * @param owldmc Ссылка на класс OWL.
         */
        @Override
        public void visit(OWLClass owlc) {
        }

        /**
         * Метод для разбора AND-правила.
         * @param owloio Ссылка на AND-выражение.
         */
        @Override
        public void visit(OWLObjectIntersectionOf owloio) {
            classes = owloio.getOperandsAsList();
        }

        /**
         * Метод для разбора OR-правила.
         * @param owlouo Ссылка на OR-выражение.
         */
        @Override
        public void visit(OWLObjectUnionOf owlouo) {
            classes = owlouo.getOperandsAsList();
        }

        /**
         * Метод для разбора NOT-правила.
         * @param owloco Ссылка на NOT-выражение.
         */
        @Override
        public void visit(OWLObjectComplementOf owloco) {
            object_filler = owloco.getOperand();
        }

        /**
         * Метод для разбора EXISTS-правила.
         * @param owlsvf Ссылка на EXISTS-выражение.
         */
        @Override
        public void visit(OWLObjectSomeValuesFrom owlsvf) {
            role_type = r_box.findRole(owlsvf.getProperty());
            object_filler = owlsvf.getFiller();
        }

        /**
         * Метод для разбора FORALL-правила.
         * @param owlvf Ссылка на FORALL-выражение.
         */
        @Override
        public void visit(OWLObjectAllValuesFrom owlvf) {
            role_type = r_box.findRole(owlvf.getProperty());
            object_filler = owlvf.getFiller();
        }

        /**
         * Метод для разбора HASVALUE-правила.
         * @param owlohv Ссылка на HASVALUE-выражение.
         */
        @Override
        public void visit(OWLObjectHasValue owlohv) {
            role_type = r_box.findRole(owlohv.getProperty());
            object_value = owlohv.getValue();
            bl_O = true;
        }

        /**
         * Метод для разбора MINCARD-правила.
         * @param owlomc Ссылка на MINCARD-выражение.
         */
        @Override
        public void visit(OWLObjectMinCardinality owlomc) {
            role_type = r_box.findRole(owlomc.getProperty());
            number_r = owlomc.getCardinality();
            object_filler = owlomc.getFiller();
            bl_N = true;
        }

        /**
         * Метод для разбора EXACT-правила.
         * @param owloec Ссылка на EXACT-выражение.
         */
        @Override
        public void visit(OWLObjectExactCardinality owloec) {
            role_type = r_box.findRole(owloec.getProperty());
            number_r = owloec.getCardinality();
            object_filler = owloec.getFiller();
            bl_N = true;
        }

        /**
         * Метод для разбора MINCARD-правила.
         * @param owlomc Ссылка на MINCARD-выражение.
         */
        @Override
        public void visit(OWLObjectMaxCardinality owlomc) {
            role_type = r_box.findRole(owlomc.getProperty());
            number_r = owlomc.getCardinality();
            object_filler = owlomc.getFiller();
            bl_N = true;
        }

        /**
         * Метод для разбора HASSELF-правила.
         * @param owlohs Ссылка на HASSELF-выражение.
         */
        @Override
        public void visit(OWLObjectHasSelf owlohs) {
            role_type = r_box.findRole(owlohs.getProperty());
        }

        /**
         * Метод для разбора ONE OF-правила.
         * @param owlooo Ссылка на ONE OF-выражение.
         */
        @Override
        public void visit(OWLObjectOneOf owlooo) {
            individs = owlooo.getIndividuals();
        }

        /**
         * Метод для разбора DATA EXISTS-правила.
         * @param o Ссылка на DATA EXISTS-выражение.
         */
        @Override
        public void visit(OWLDataSomeValuesFrom o) {
            role_type = r_box.findRole(o.getProperty());
            data_filler = o.getFiller();
            bl_D = true;
        }

        /**
         * Метод для разбора DATA FORALL-правила.
         * @param owldvf Ссылка на DATA FORALL-выражение.
         */
        @Override
        public void visit(OWLDataAllValuesFrom owldvf) {
            role_type = r_box.findRole(owldvf.getProperty());
            data_filler = owldvf.getFiller();
            bl_D = true;
        }

        /**
         * Метод для разбора DATA HASVALUE-правила.
         * @param owldhv Ссылка на DATA HASVALUE-выражение.
         */
        @Override
        public void visit(OWLDataHasValue owldhv) {
            role_type = r_box.findRole(owldhv.getProperty());
            data_value = owldhv.getValue();
            bl_D = true;
        }

        /**
         * Метод для разбора DATA MINCARD-правила.
         * @param owldmc Ссылка на DATA MINCARD-выражение.
         */
        @Override
        public void visit(OWLDataMinCardinality owldmc) {
            role_type = r_box.findRole(owldmc.getProperty());
            number_r = owldmc.getCardinality();
            data_filler = owldmc.getFiller();
            bl_D = true;
        }

        /**
         * Метод для разбора DATA EXACT-правила.
         * @param owldec Ссылка на DATA EXACT-выражение.
         */
        @Override
        public void visit(OWLDataExactCardinality owldec) {
            role_type = r_box.findRole(owldec.getProperty());
            number_r = owldec.getCardinality();
            data_filler = owldec.getFiller();
            bl_D = true;
        }

        /**
         * Метод для разбора DATA MAXCARD-правила.
         * @param owldmc Ссылка на DATA MAXCARD-выражение.
         */
        @Override
        public void visit(OWLDataMaxCardinality owldmc) {
            role_type = r_box.findRole(owldmc.getProperty());
            number_r = owldmc.getCardinality();
            data_filler = owldmc.getFiller();
            bl_D = true;
        }
    }
    
    /**
     * Класс для преобразования выражений, которые представляют собой типы данных, ограничения на типы данных и т.д.
     */
    private class DataExpressionTransformer implements OWLDataVisitor
    {
        private Set<OWLLiteral> liters;
        private OWLLiteral liter;
        private OWLDataRange data_filler;
        private Set<OWLDataRange> data_filler_set;
        private OWLDatatype data_type;
        private OWLFacet facet;
        private ArrayList<OWLFacet> facet_set;
        
        /**
         * Основной конструктор класса.
         */
        public DataExpressionTransformer() {
            data_type = null;
            data_filler_set = null;
            data_filler = null;
            liter = null;
            liters = null;
            facet = null;
            facet_set= null;
        }
        
        /**
         * Метод возвращает ссылку на тип данных.
         * @return Ссылка на тип данных.
         */
        public OWLDatatype getDatatype() {
            return data_type;
        }
        
        /**
         * Метод возвращает ссылку на промежуток из области значений типа данных.
         * @return Ссылка на ограничение области значений.
         */
        public OWLDataRange getDataFiller() {
            return data_filler;
        }

        /**
         * Метод возвращает множество ссылок на промежутки из области значений типа данных.
         * @return Ссылка на множество ограничений области значений.
         */
        public Set<OWLDataRange> getDataFillerSet() {
            return data_filler_set;
        }

        /**
         * Метод возвращает множество литер появившихся в правиле данных.
         * @return Множество литер из правила данных.
         */
        public Set<OWLLiteral> getLiters() {
            return liters;
        }
        
        /**
         * Метод возвращает литеру появившуюся в правиле данных.
         * @return Литера из правила данных.
         */
        public OWLLiteral getLiter() {
            return liter;
        }
        
        /**
         * Метод возвращает ссылку на ограничение области значений типа данных.
         * @return Ссылка на ограничение области значений.
         */
        public OWLFacet getFacet() {
            return facet;
        }
        
        /**
         * Метод возвращает ссылку на множество ограничений области значений типа данных.
         * @return Ссылка на множество ограничений области значений.
         */
        public ArrayList<OWLFacet> getFacetSet() {
            return facet_set;
        }

        /**
         * Метод для разбора литеры появившейся в DATA-правиле.
         * @param owll Ссылка на литеру в базе знаний.
         */
        @Override
        public void visit(OWLLiteral owll) {
            data_type = owll.getDatatype();
            liter = owll;
        }

        /**
         * Метод для разбора фасетного ограничения в DATA-правиле.
         * @param owlfr Ссылка на фасет в базе знаний.
         */
        @Override
        public void visit(OWLFacetRestriction owlfr) {
            System.out.println(owlfr.getFacet().getSymbolicForm() + ":" + owlfr.getFacetValue().getLiteral());
            facet = owlfr.getFacet();
        }

        /**
         * Метод для разбора типа данных в DATA-правиле.
         * @param owlfr Ссылка на типа данных в базе знаний.
         */
        @Override
        public void visit(OWLDatatype owld) {
            data_type = owld;
        }

        /**
         * Метод для разбора DATA OR-правиле.
         * @param owldoo Ссылка DATA OR-правило в базе знаний.
         */
        @Override
        public void visit(OWLDataOneOf owldoo) {
            liters = owldoo.getValues();
        }

        /**
         * Метод для разбора DATA NOT-правиле.
         * @param owldco Ссылка DATA NOT-правило в базе знаний.
         */
        @Override
        public void visit(OWLDataComplementOf owldco) {
            data_filler = owldco.getDataRange();
        }

        /**
         * Метод для разбора DATA AND-правиле.
         * @param owldio Ссылка DATA AND-правило в базе знаний.
         */
        @Override
        public void visit(OWLDataIntersectionOf owldio) {
            data_filler_set = owldio.getOperands();
        }

        /**
         * Метод для разбора DATA OR-правиле.
         * @param owlfr Ссылка DATA OR-правило в базе знаний.
         */
        @Override
        public void visit(OWLDataUnionOf owlduo) {
            data_filler_set = owlduo.getOperands();
        }

        /**
         * Метод для разбора ограничения типа данных.
         * @param owlfr Ссылка на ограничение типа данных в базе знаний.
         */
        @Override
        public void visit(OWLDatatypeRestriction owldr) {
            facet_set = new ArrayList<OWLFacet>();
            data_type = owldr.getDatatype();
            for(OWLFacetRestriction ofr: owldr.getFacetRestrictions())
                facet_set.add(ofr.getFacet());
        }
    }
}