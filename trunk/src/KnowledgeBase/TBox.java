/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KnowledgeBase;

import Enums.NodeType;
import Help.IntArray;
import KnowledgeBase.RuleGraph.RuleGraph;
import KnowledgeBase.RuleGraph.RuleNode;
import java.util.ArrayList;

/**
 * Класс хранит аксиомы и лес ограничений базы знаний описанных в OWL онтологии.
 * @author Andrey Grigoryev
 */
public class TBox {
    
/**
 * Класс для хранения аксиом TBox: аксиомы эквивалентности или аксиомы включения.
 * @author Andrey Grigoryev
 */
public class TwoSidedAxiom {
    private int left, right;

        /**
         * Основной конструктор в который передаются номера левой и правой части аксиомы.
         * @param lft Номер вершины леса ограничений, соответствующей левой части аксиомы
         * @param rgt Номер вершины леса ограничений, соответствующей правой части аксиомы
         */
        public TwoSidedAxiom(int lft, int rgt) {
            left = lft;
            right = rgt;
        }

        /**
         * Осуществляет доступ к левой части аксиомы
         * @return Номер вершины соответствующей левой части аксиомы.
         */
        public int getSub() {
            return left;
        }

        /**
         * Осуществляет доступ к правой части аксиомы
         * @return Номер вершины соответствующей правой части аксиомы.
         */
        public int getSuper() {
            return right;
        }

        /**
         * Метод осуществляет добавление концепта к правой части аксиомы.
         * Концепт добавляется в виде конъюнкции к правой части. Если правая часть концепта
         * не является конъюнкцией, то создается новая вершина леса ограничений, которая
         * помечается конъюнкцией (становится AND-правилом) и к ней добавляются в качестве
         * потомков текущая правая часть и концепт который необходимо добавить к правой
         * части.
         * @param another_super Описание супер концепта заданной аксиомы.
         * @param rg Ссылка на лес ограничений
         * @param flag Флаг-указатель на параметр добавления.
         */
        public void addSuperConcept(int another_super, RuleGraph rg) {
            if(rg.getNode(Math.abs(right)).getNodeType() != NodeType.ntAND) {
                int cur = rg.addNode2RuleTree(new RuleNode(NodeType.ntAND));                                
                rg.getNode(cur).addChild(right);
                rg.getNode(cur).addChild(another_super);
                right = cur;
            } else {
                rg.getNode(Math.abs(right)).addChild(another_super);
            }
        }
    }    
            
    private RuleGraph rg = new RuleGraph();
    private IntArray order = null;
    private int[] f = null;
   
    private ArrayList<TwoSidedAxiom> EquivalenceAxioms = new ArrayList<TwoSidedAxiom>();
    private ArrayList<TwoSidedAxiom> GCIs = new ArrayList<TwoSidedAxiom>();
    private ArrayList<TwoSidedAxiom> NEG_GCIs = new ArrayList<TwoSidedAxiom>();
    private RBox r_box;
    private ABox a_box;
    private int meta_constraint = 1;

    /**
     * Конструктор создает новый экземпляр класса TBox.
     * В конструкторе происходит очистка всех полей объекта, устанавливается связь
     * между объектами RBox и TBox, для идентифицирования их как взаимосвязанных в
     * одной базе знаний.
     * @param rb Ссылка на объект RBox базы знаний.
     */
    public TBox(RBox rb, ABox new_a_box)
    {
        clear();
        a_box = new_a_box;
        rg.setTBox(this, new_a_box);
        rg.setRBox(rb);
        r_box = rb;
    }
    
    /**
     * Выводит все аксиомы содержащиеся в базе знаний в структурированный OWL файл
     * описанный XML синтаксисом.
     * @param file_name Имя файла в который будут записаны аксиомы базы знаний.
     */
    public void outToOWL(String file_name) {
        rg.printToOWL(file_name);
    }
    
    /**
     * Метод производит очистку множеств аксиом эквивалентности и аксиом включения.
     */
    final public void clear() {
        EquivalenceAxioms.clear();
        GCIs.clear();
        NEG_GCIs.clear();
    }
    
    /**
     * Метод осуществляет подмену объекта RBox, связанного с базой данных, на новый RBox.
     * Данный метод был создан для того, чтобы задавать RBox, если он был получен после
     * создания TBox и не мог быть добавлен при вызове конструктора TBox.
     * @param new_r_box Задает RBox соответствующий рассматриваемой базе знаний.
     */
    public void setRBox(RBox new_r_box) {
        r_box = new_r_box;
    }
    
    /**
     * Метод осуществляет доступ к объекту RBox, соответствующему TBox, которые определены
     * над единой базой знаний.
     * Данный метод был разработан для доступа к {@link RBox} из объектов, которые не имеют 
     * прямой ссылки на RBox, но имеют ссылку на TBox. Такими объектами являются объекты 
     * класса AChecker.
     * @return RBox соответствущий текущему TBox.
     */
    public RBox getRBox() {
        return r_box;
    }
    
    /**
     * Метод предоставляет доступ к лесу ограничений для того, чтобы осуществлять
     * раскрытие правил посредоством доступа только к объекту TBox.
     * @return Ссылку на объект леса ограничений.
     */
    public RuleGraph getRuleGraph() {
        return rg;
    }
        
    //axioms like C [= D (C is subsumed by D)
    public void addGCI(int sub_concept, int super_concept) {
        boolean is_exist = false;
        //С is a simple concept
        if(rg.getNode(Math.abs(sub_concept)).getNodeType() == NodeType.ntCONCEPT)
            for(TwoSidedAxiom gci: GCIs)
                if(gci.getSub() == sub_concept)
                {
                    is_exist = true;
                    gci.addSuperConcept(super_concept, rg);
                    break;
                }
        
        if(!is_exist)
            GCIs.add(new TwoSidedAxiom(sub_concept, super_concept));
        
        is_exist = false;
        if(rg.getNode(Math.abs(super_concept)).getNodeType() == NodeType.ntCONCEPT)
            for(TwoSidedAxiom neg_gci: NEG_GCIs)
                if(neg_gci.getSub() == -super_concept) {
                    is_exist = true;
                    neg_gci.addSuperConcept(-sub_concept, rg);
                    break;
                }

        if(!is_exist)
            NEG_GCIs.add(new TwoSidedAxiom(-super_concept, -sub_concept));
    }

    /**
     * Данный метод выполняет добавление аксиом эквивалентности.
     * В данном методе происходит объединение аксиом эквивалентности: при обнаружении 
     * двух аксиом эквивалентности в левой части которых находится один и тот же 
     * простой концепт, то эти две аксиомы объединяются в одну, правая часть новой
     * аксиомы содержит конъюнкцию правых частей найденных аксиом.
     * @param left_side Левая часть аксиомы эквивалентности
     * @param right_side Правая часть аксиомы эквивалентности 
     */
    public void addEquivalenceAxiom(int left_side, int right_side) {
        boolean is_exist = false;
        if(right_side > 0) rg.getNode(right_side).setDescription(left_side);
        if(rg.getNode(Math.abs(left_side)).getNodeType() == NodeType.ntCONCEPT)
            for(TwoSidedAxiom eq: EquivalenceAxioms)
                if(eq.getSub() == left_side)
                {
                    is_exist = true;
                    eq.addSuperConcept(right_side, rg);
                    break;
                }
        
        if(!is_exist) {
            RuleNode tmpRn = new RuleNode(NodeType.ntAND); tmpRn.addChild(right_side);
            tmpRn.setEquivNode(true);
            EquivalenceAxioms.add(new TwoSidedAxiom(left_side, rg.addNode2RuleTree(tmpRn)));
        }
    }
    
    /**
     * Данный метод выполняет нормализацию аксиом базы знаний.
     * При нахождении аксиомы эквивалентности C = D, при том, что левая часть аксиомы
     * является простым концептом, выполняется заполнение поля вершины леса ограничений
     * соответствующего эквивалентному описанию данного концепта. Те же самые действия
     * выполняются при обнаружении аксиом C [= D и -C [= D, за тем исключением, что
     * соответствующие правые части аксиом записываются в другие поля объекта.
     */
    public void normalize() {
        //if concept C is a simple concept, then put D into description of C from equivalence C = D
        for(int i = 0; i < EquivalenceAxioms.size(); i++) {
            TwoSidedAxiom eq = EquivalenceAxioms.get(i);
            if(eq.getSub() > 0)
                if(rg.getNode(eq.getSub()).getNodeType() == NodeType.ntCONCEPT) {
                    rg.getNode(eq.getSub()).setDescription(eq.getSuper());
                    rg.getNode(eq.getSub()).setNamed(true);
                    EquivalenceAxioms.remove(i); i--;
                }
        }
        
        //if concept C is a simple concept, then put D into description of C from GCI C [= D
        for(int i = 0; i < GCIs.size(); i++) {
            TwoSidedAxiom gci = GCIs.get(i);
            if(gci.getSub() > 0)
                if(rg.getNode(gci.getSub()).getNodeType() == NodeType.ntCONCEPT) {
                    rg.getNode(gci.getSub()).setSubDescription(gci.getSuper());
                    GCIs.remove(i);
                    i--;
                }
        }
        
        //if concept C is a simple concept and there exists D [= C then NOT C [= NOT D
        for(int i = 0; i < NEG_GCIs.size(); i++) {
            TwoSidedAxiom neg_gci = NEG_GCIs.get(i);
            if(neg_gci.getSub() > 0) continue;
            if(rg.getNode(-neg_gci.getSub()).getName() != null) {
            }
            if(rg.getNode(-neg_gci.getSub()).getNodeType() == NodeType.ntCONCEPT) {
                rg.getNode(-neg_gci.getSub()).setNegativeDescription(neg_gci.getSuper());
                NEG_GCIs.remove(i);
                i--;                
            }
        }
    }
    
    
    /**
     * Данный метод выполняет топологическую сортировку концептов, исходя из их описания.
     * В порядок следования сначала будут добавлены концепты, на основе которых описываеются
     * другие концепты.
     * @param x Определяет номер вершину в лесу ограничений.
     * @param isConcept Определяет является ли текущая вершина именем концепта.
     */
    private void TopSort(int x, boolean isConcept) {
        int old_x = x;
        int sds_x = -1;
        if(isConcept) {
            f[x] = 1;
            sds_x = Math.abs(rg.getNode(rg.findConcept(rg.getConcepts()[x])).getSubDescription());
            x = Math.abs(rg.getNode(rg.findConcept(rg.getConcepts()[x])).getDescription());
        }
        int[] ch = rg.getNode(Math.abs(x)).getChildren();
        int chsize = rg.getNode(Math.abs(x)).getChildrenSize();
        if(chsize > 0) {
            for(int i = 0; i < chsize; i++) {
                if(rg.getNode(Math.abs(ch[i])).getNodeType() == NodeType.ntCONCEPT) {
                    if(f[rg.getConceptID(rg.getNode(Math.abs(ch[i])).getName())] == 0)
                        TopSort(rg.getConceptID(rg.getNode(Math.abs(ch[i])).getName()), true);
                } else {
                    TopSort(Math.abs(ch[i]), false);
                }
            }
        } else
        if(sds_x > 0 && rg.getNode(Math.abs(sds_x)).getChildrenSize() > 0) {
            ch = rg.getNode(Math.abs(sds_x)).getChildren();
            for(int i = 0; i < chsize; i++) {
                if(rg.getNode(Math.abs(ch[i])).getNodeType() == NodeType.ntCONCEPT) {
                    if(f[rg.getConceptID(rg.getNode(Math.abs(ch[i])).getName())] == 0)
                        TopSort(rg.getConceptID(rg.getNode(Math.abs(ch[i])).getName()), true);
                } else {
                    TopSort(Math.abs(ch[i]), false);
                }
            }            
        } else
        if(x != 0 && x != 1 && rg.getNode(Math.abs(x)).getName() != null) {
            x = rg.getConceptID(rg.getNode(Math.abs(x)).getName());
            if(f[x] == 0)
                TopSort(x, true);
        }
        if(isConcept) {
            order.add(old_x);
        }
    }
    
    /**
     * Метод возвращает порядок в котором необходимо классифицировать концепты для 
     * достижения быстроты выполняемого кода.
     * @return Список номеров концептов в необходимом порядке.
     */
    public IntArray getOrder() {
        return order;
    }
    
    /**
     * Метод вызывает алгоритм топологической сортировки, для определения последовательности
     * классификации концептов.
     */
    private void makeOrder() {
        if(order == null) {
            order = new IntArray();
            f = new int[2 * rg.getConceptsSize()];
        }
        for(int i = 0; i < rg.getConceptsSize(); i++) {
            if(f[i] == 0) {
                TopSort(i, true);
            }
        }
        //order.reverse();
    }
    
    /**
     * В данном методе реализуется алгоритм поглощения (absorbtion).
     */
    private void absorb() {
        //part for adding only C [= D where C is atomic positive
        for(int i = 0; i < GCIs.size(); i++) {
            boolean removed = false;
            if(GCIs.get(i).getSuper() < 0) continue;
            if(rg.getNode(GCIs.get(i).getSuper()).getNodeType() == NodeType.ntOR) { //this is C [= D1 or D2 or ... not Di ... or Dn
                RuleNode tmp_n = rg.getNode(GCIs.get(i).getSuper());
                for(int j = 0; j < tmp_n.getChildrenSize(); j++) {
                    if(tmp_n.getChildren()[j] < 0) {
                        RuleNode tmp_m = rg.getNode(-tmp_n.getChildren()[j]);
                        if(tmp_m.getNodeType() == NodeType.ntCONCEPT) { //this is negative concept vertice
                            RuleNode nrn = new RuleNode(NodeType.ntOR);
                            nrn.addChild(-GCIs.get(i).getSub()); //add not C
                            for(int k = 0; k < tmp_n.getChildrenSize(); k++) {
                                if(j == k) continue;
                                nrn.addChild(tmp_n.getChildren()[k]); //add Di
                            }
                            if(tmp_m.getSubDescription() == 0) {
                                tmp_m.setSubDescription(rg.addNode2RuleTree(nrn));
                            } else {
                                if(rg.getNode(tmp_m.getSubDescription()).getNodeType() == NodeType.ntAND) {
                                    rg.getNode(tmp_m.getSubDescription()).addChild(rg.addNode2RuleTree(nrn));
                                } else {
                                    RuleNode and_node = new RuleNode(NodeType.ntAND);
                                    and_node.addChild(rg.addNode2RuleTree(nrn));
                                    and_node.addChild(tmp_m.getSubDescription());                                    
                                    tmp_m.setSubDescription(rg.addNode2RuleTree(and_node));
                                }
                            }
                            removed = true;                            
                        }
                    }
                }
            }
            
            if(rg.getNode(GCIs.get(i).getSub()).getNodeType() == NodeType.ntAND && !removed) { //this is C1 and C2 and ... and Cn [= D
                RuleNode tmp_n = rg.getNode(GCIs.get(i).getSub());                
                for(int j = 0; j < tmp_n.getChildrenSize(); j++) {
                    if(tmp_n.getChildren()[j] > 0) {
                        RuleNode tmp_m = rg.getNode(tmp_n.getChildren()[j]);
                        if(tmp_m.getNodeType() == NodeType.ntCONCEPT) { //this is positive concept vertice
                            RuleNode nrn = new RuleNode(NodeType.ntOR);
                            nrn.addChild(GCIs.get(i).getSuper()); //add D
                            for(int k = 0; k < tmp_n.getChildrenSize(); k++) {
                                if(j == k) continue;
                                nrn.addChild(-tmp_n.getChildren()[k]); //add not Ci
                            }
                            if(tmp_m.getSubDescription() == 0) {
                                tmp_m.setSubDescription(rg.addNode2RuleTree(nrn));
                            } else {
                                if(rg.getNode(tmp_m.getSubDescription()).getNodeType() == NodeType.ntAND) {
                                    rg.getNode(tmp_m.getSubDescription()).addChild(rg.addNode2RuleTree(nrn));
                                } else {
                                    RuleNode and_node = new RuleNode(NodeType.ntAND);
                                    and_node.addChild(rg.addNode2RuleTree(nrn));
                                    and_node.addChild(tmp_m.getSubDescription());                                    
                                    tmp_m.setSubDescription(rg.addNode2RuleTree(and_node));
                                }
                            }
                            removed = true;
                        }
                    }
                }
            }
            if(GCIs.get(i).getSuper() < 0) if(rg.getNode(-GCIs.get(i).getSuper()).getNodeType() == NodeType.ntCONCEPT && !removed) {
                RuleNode tmp_m = rg.getNode(-GCIs.get(i).getSuper());
                if(tmp_m.getSubDescription() == 0) {
                    tmp_m.setSubDescription(-GCIs.get(i).getSub());
                } else {
                    if(rg.getNode(tmp_m.getSubDescription()).getNodeType() == NodeType.ntAND) {
                        rg.getNode(tmp_m.getSubDescription()).addChild(-GCIs.get(i).getSub());
                    } else {
                        RuleNode and_node = new RuleNode(NodeType.ntAND);
                        and_node.addChild(-GCIs.get(i).getSub());
                        and_node.addChild(tmp_m.getSubDescription());                                    
                        tmp_m.setSubDescription(rg.addNode2RuleTree(and_node));
                    }
                }
                removed = true;                
            }
            if(removed) {
                GCIs.remove(i);
                i--;
            }
        }
        
        //Just UnComment!
        //part for adding only C [= D where C is atomic negative
        /*for(int i = 0; i < GCIs.size(); i++) {
            boolean removed = false;
            if(rg.getNode(GCIs.get(i).getSuper()).getNodeType() == NodeType.ntOR) { //this is C [= D1 or D2 or ... or Dn
                RuleNode tmp_n = rg.getNode(GCIs.get(i).getSuper());
                for(int j = 0; j < tmp_n.getChildren().size(); j++) {
                    if(tmp_n.getChildren().get(j) > 0) {
                        RuleNode tmp_m = rg.getNode(tmp_n.getChildren().get(j));
                        if(tmp_m.getNodeType() == NodeType.ntCONCEPT) { //this is positive concept vertice
                            RuleNode nrn = new RuleNode(NodeType.ntOR);
                            nrn.addChild(-GCIs.get(i).getSub()); //add not C
                            for(int k = 0; k < tmp_n.getChildren().size(); k++) {
                                if(j == k) continue;
                                nrn.addChild(tmp_n.getChildren().get(k)); //add Di
                            }
                            if(tmp_m.getNegativeDescription() == 0) {
                                tmp_m.setNegativeDescription(rg.addNode2RuleTree(nrn));
                            } else {
                                if(rg.getNode(tmp_m.getNegativeDescription()).getNodeType() == NodeType.ntAND) {
                                    rg.getNode(tmp_m.getNegativeDescription()).addChild(rg.addNode2RuleTree(nrn));
                                } else {
                                    RuleNode and_node = new RuleNode(NodeType.ntAND);
                                    and_node.addChild(rg.addNode2RuleTree(nrn));
                                    and_node.addChild(tmp_m.getNegativeDescription());                                    
                                    tmp_m.setNegativeDescription(rg.addNode2RuleTree(and_node));
                                }
                            }
                            removed = true;                            
                        }
                    }
                }
            }
            
            if(rg.getNode(GCIs.get(i).getSub()).getNodeType() == NodeType.ntAND && !removed) { //this is C1 and C2 and ... not Ci ... and Cn [= D
                RuleNode tmp_n = rg.getNode(GCIs.get(i).getSub());                
                for(int j = 0; j < tmp_n.getChildren().size(); j++) {
                    if(tmp_n.getChildren().get(j) < 0) {
                        RuleNode tmp_m = rg.getNode(-tmp_n.getChildren().get(j));
                        if(tmp_m.getNodeType() == NodeType.ntCONCEPT) { //this is negative concept vertice
                            RuleNode nrn = new RuleNode(NodeType.ntOR);
                            nrn.addChild(GCIs.get(i).getSuper()); //add D
                            for(int k = 0; k < tmp_n.getChildren().size(); k++) {
                                if(j == k) continue;
                                nrn.addChild(-tmp_n.getChildren().get(k)); //add not Ci
                            }
                            if(tmp_m.getNegativeDescription() == 0) {
                                tmp_m.setNegativeDescription(rg.addNode2RuleTree(nrn));
                            } else {
                                if(rg.getNode(tmp_m.getNegativeDescription()).getNodeType() == NodeType.ntAND) {
                                    rg.getNode(tmp_m.getNegativeDescription()).addChild(rg.addNode2RuleTree(nrn));
                                } else {
                                    RuleNode and_node = new RuleNode(NodeType.ntAND);
                                    and_node.addChild(rg.addNode2RuleTree(nrn));
                                    and_node.addChild(tmp_m.getNegativeDescription());                                    
                                    tmp_m.setNegativeDescription(rg.addNode2RuleTree(and_node));
                                }
                            }
                            removed = true;
                        }
                    }
                }
            }
            if(GCIs.get(i).getSuper() > 0) if(rg.getNode(-GCIs.get(i).getSuper()).getNodeType() == NodeType.ntCONCEPT && !removed) {
                RuleNode tmp_m = rg.getNode(GCIs.get(i).getSuper());
                if(tmp_m.getNegativeDescription() == 0) {
                    tmp_m.setNegativeDescription(-GCIs.get(i).getSub());
                } else {
                    if(rg.getNode(tmp_m.getNegativeDescription()).getNodeType() == NodeType.ntAND) {
                        rg.getNode(tmp_m.getNegativeDescription()).addChild(-GCIs.get(i).getSub());
                    } else {
                        RuleNode and_node = new RuleNode(NodeType.ntAND);
                        and_node.addChild(-GCIs.get(i).getSub());
                        and_node.addChild(tmp_m.getNegativeDescription());                                    
                        tmp_m.setNegativeDescription(rg.addNode2RuleTree(and_node));
                    }
                }
                removed = true;                
            }
            if(removed) {
                GCIs.remove(i);
                i--;
            }
        }*/
        RuleNode mc = new RuleNode(NodeType.ntAND);
        for(int i = 0; i < GCIs.size(); i++) {
            RuleNode mc1 = new RuleNode(NodeType.ntOR);
            mc1.addChild(-GCIs.get(i).getSub());
            mc1.addChild(GCIs.get(i).getSuper());
            mc.addChild(rg.addNode2RuleTree(mc1));
        }
            
        for(int i = 0; i < EquivalenceAxioms.size(); i++) {
            RuleNode emc1 = new RuleNode(NodeType.ntOR);
            emc1.addChild(-EquivalenceAxioms.get(i).getSub());
            emc1.addChild(EquivalenceAxioms.get(i).getSuper());

            RuleNode emc2 = new RuleNode(NodeType.ntOR);
            emc2.addChild(EquivalenceAxioms.get(i).getSub());
            emc2.addChild(-EquivalenceAxioms.get(i).getSuper());
            
            mc.addChild(rg.addNode2RuleTree(emc1));
            mc.addChild(rg.addNode2RuleTree(emc2));
        }

        if(GCIs.size() + EquivalenceAxioms.size() > 0)
            meta_constraint = rg.addNode2RuleTree(mc);            
    }
    
    /**
     * Данный метод последовательно вызывает методы упрощения аксиом и концептов базы знаний.
     * К таким методам относятся метод преобразования древовидного представления
     * в графовидное, метод поглощения, удаление единичных потомков у OR и AND правила,
     * создание порядка классификации концептов.
     */
    public void preProcess() {
        normalize();
        rg.normalizeGraph();
        absorb();
        rg.makeDAG(); //was maked only for ALC (not N or Q!!!)
        rg.delOnes();
        rg.EqvDescriptionProcess();
        makeOrder();
    }
    
    /**
     * Метод осуществляет вывод в консоль всех аксиом эквивалентности и аксиом включения
     */
    public void showAxioms() {
        //Show all equivalences
        for(TwoSidedAxiom it: EquivalenceAxioms) {
            rg.showTree(it.getSub(), "");
            System.out.println(" EQV");
            rg.showTree(it.getSuper(), "");
            System.out.println();
        }

        //Show all GCIs
        for(TwoSidedAxiom gci: GCIs) {
            rg.showTree(gci.getSub(), "");
            System.out.println(" SUB");
            rg.showTree(gci.getSuper(), "");
            System.out.println();
            System.out.flush();
        }
    }
    
    /**
     * Метод возвращает номер вершины соответствующей мета ограничению.
     * Метаограничение является конъюнкцией всех аксиом вида C [= D, переведенных 
     * к виду -C or D.
     * @return Номер вершины в графе ограничений соответствующий метаограничению.
     */
    public int getMetaConstraint() {
        return meta_constraint;
    }
    
    /**
     * Метод осуществляет вывод в консоль описание концепта эквивалентного заданному.
     * @param concept Задает имя концепта в строковом виде.
     */
    public void showConceptDescription(String concept) {
        rg.showTree(rg.getNode(rg.findConcept(concept)).getDescription(), "");
    }
    
    /**
     * Метод осуществляет вывод в консоль описание всех эквивалентных концептов.
     */
    public void showConceptsDescriptions() {
        rg.showCD();
    }
    
    Integer[] trans_roles = null;
    /**
     * В данном методе осуществляется добавление аксиом в соответствии с транзитивными ролями.
     * Если в базе знаний существует FORALL-правило или EXISTS-правило, и роль в этом
     * правиле является транзитивной, то необходимо добавить к концепту под квантором
     * текущее рассматриваемое правило.
     */
    public void processTransitive() {
        for(int i = 0; i < rg.getNodesCount(); i++) {
            if((rg.getNode(i).getNodeType() == NodeType.ntALL || 
                rg.getNode(i).getNodeType() == NodeType.ntSOME) && 
               (r_box.getRoleByIndex(rg.getNode(i).getRoleType()).isTransitive())) {
                trans_roles = r_box.getSubAndEqvRoles(rg.getNode(i).getRoleType());
                for(int j = 0; j < trans_roles.length; j++) {
                    RuleNode nrn = new RuleNode(rg.getNode(i).getNodeType(), trans_roles[j]);
                    nrn.addChild(rg.getNode(i).getChildren()[0]);
                    int x = rg.addNode2RuleTree(nrn);
                    rg.getNode(i).addTrans(x);
                }
            }
        }
    }
}