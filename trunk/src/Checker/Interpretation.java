package Checker;

import Checker.Model.InterpretationNode;
import Checker.Structures.Couple;
import KnowledgeBase.RBox;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Класс реализует алгоритм выполнения блокирования индивида
 * @author Andrey Grigoryev
 */
class Interpretation {
    
    private RBox r_box = null;

    /**
     * Конструктор класса, который задает ссылку на RBox базы знаний.
     * @param new_r_box Определяет RBox на который ссылается интерпретация.
     */
    public Interpretation(RBox new_r_box) {
        r_box = new_r_box;
    }
    
    /**
     * Метод задает ссылку на RBox базы знаний.
     * @param new_r_box Определяет RBox на который ссылается интерпретация.
     */
    public void setRBox(RBox new_r_box) {
        r_box = new_r_box;
    }
        
    /**
     * Метод осуществляет добавление заданного индивида в интерпретацию.
     * @param parent_node Определяет предка заданного индивида.
     * @param new_node Определяет добавляемого индивида.
     * @param role Определяет роль по которой связан текущий индивид с предком.
     * @param when Определяет размер стека при котором был добавлен индивид.
     * @return Ссылка на добавлемого индивида.
     */
    public InterpretationNode addNode(InterpretationNode parent_node, InterpretationNode new_node, int role, int when)
    {
        if(role < 0) return new_node;
        
        new_node.addParent(parent_node, role, when, r_box);
        new_node.updateParents(role, when, r_box);
        for(int it: r_box.getRoleByIndex(role).getEqvRoles()) { //add all equivalent roles
            new_node.getParents()[new_node.getParentSize() - 1].addNewRole(it);
        }

        if(r_box.getRoleByIndex(role).isTransitive()) {
            for(int i = 0; i < parent_node.getParentSize(); i++)
                for(int j = 0; j < parent_node.getParents()[i].getRoles().length; j++)
                    if(r_box.isSubOrEqual(role, parent_node.getParents()[i].getRoles()[j])) {
                        new_node.addParent(parent_node.getParents()[i].getNode(), role, when, r_box);
                        new_node.updateParents(role, when, r_box);
                        parent_node.getParents()[i].getNode().addChild(new_node, role, when);
                    }
        }

        parent_node.addChild(new_node, role, when);
        return new_node;
    }
    
    /**
     * Определяет является ли toDoList первого индивида, подмножеством toDoList второго индивида.
     * @param super_a_node Определяет первого индивида.
     * @param sub_a_node Определяет второго индивида.
     * @return 
     */
    private boolean blocking(InterpretationNode super_a_node, InterpretationNode sub_a_node) {
        for(int i = 0; i < sub_a_node.getToDoSize(); i++)
            if(!super_a_node.isContain(sub_a_node.getToDo()[i])) {
                return false;
            }

        for(int i = 0; i < sub_a_node.getIndsSize(); i++) {
            boolean same_found = false;
            for(int j = 0; j < super_a_node.getIndsSize(); j++) {
                if(super_a_node.getInds()[j].equals(sub_a_node.getInds()[i])) {
                    same_found = true;
                    break;
                }
            }
            if(!same_found) return false;
        }
        
        return true;
    }

    /**
     * Метод проверяет блокирует ли первый заданный индивид второго.
     * @param super_node Ссылка на первого индивида.
     * @param sub_node Ссылка на второго индивида.
     * @return Возвращает истина, если первый индивид блокирует второго и ложь в противном случае.
     */
    private boolean generalBlocking(InterpretationNode super_node, InterpretationNode sub_node) {
        if(!blocking(super_node, sub_node)) return false;
        //    else return true;
        
        for(int i = 0; i < super_node.getParentSize(); i++)
            for(int j = 0; j < sub_node.getParentSize(); j++)
                if(blocking(super_node.getParents()[i].getNode(), sub_node.getParents()[j].getNode())) {
                    if(super_node.getParents()[i].containsAll(sub_node.getParents()[j].getRoles()))
                        return true;
                }

        return false;
    }

    HashSet<InterpretationNode> flag = new HashSet<InterpretationNode>();
    private boolean upToTree(InterpretationNode current_node, InterpretationNode check_node, int depth) {
        //if(depth > 20) return false;
        if(generalBlocking(current_node, check_node)) return true;
        flag.add(current_node);
        for(int i = 0; i < current_node.getParentSize(); i++) {
            Couple it = current_node.getParents()[i];
            if(!flag.contains(it.getNode()))
                if(upToTree(it.getNode(), check_node, depth + 1)) return true;
        }

        return false;
    }
    
    /**
     * Метод проверяет может ли быть заблокирован заданный индивид.
     * @param current_node Ссылка на индивида, блокировка которого проверяется.
     * @param Q Определяет всех индивидов интерпретации.
     * @param QS Определяет размер очереди.
     * @return Возвращает истина, если заданный индивид блокируется каким либо индивидом интерпретации и ложь в противном случае.
     */
    public boolean makeBlocked(InterpretationNode current_node, InterpretationNode[] Q, int QS) {
        if(current_node.getBlock() == 1) return current_node.isBlocked();
        //if(current_node.getBlock() != -1) return current_node.isBlocked();
        flag.clear();
        flag.add(current_node);
        
        for(int i = 0; i < QS; i++) {
            if(generalBlocking(Q[i], current_node)) {
                return true;
            }
        }
        
        for(int i = 0; i < current_node.getParentSize(); i++) {
            Couple it = current_node.getParents()[i];
            if(upToTree(it.getNode(), current_node, 0)) {
                current_node.setBlock(1);
                for(int j = 0; j < current_node.getChildSize(); j++) {
                    Couple jt = current_node.getChildren()[j];
                    jt.getNode().setSkip(true);
                }
                return true;
            }
        }
        
        current_node.setBlock(0);
        return false;
    }
    
    /**
     * Метод осуществляет разблокирование индивида интерпретации и всех её потомков.
     * @param current_node Определяет вершину интерпретации, которую нужно разблокировать.
     */
    public void unBlock(InterpretationNode current_node) {
        current_node.setBlock(-1);
        current_node.setSkip(false);
        for(int i = 0; i < current_node.getChildSize(); i++) {
            current_node.getChildren()[i].getNode().setSkip(false);
        }
    }
    
    /**
     * Метод определяет всех прямых и не прямых предков индивида интерпретации.
     * @param vert Определяет индивида интерпретации, предков которого нужно определить.
     * @return Массив всех прямых и не прямых предков вершины.
     */
    public InterpretationNode[] getAncestors(InterpretationNode vert) {
        InterpretationNode[] queue = new InterpretationNode[32];
        boolean[] f = new boolean[1024];
        Arrays.fill(f, false);
        int qs = 0, qf = 0;
        queue[qf++] = vert;
        f[vert.indexInQueue] = true;
        while(qs != qf) {
            vert = queue[qs++];
            for(int i = 0; i < vert.getParentSize(); i++) {
                if(!f[vert.getParents()[i].getNode().indexInQueue]) {
                    queue[qf++] = vert.getParents()[i].getNode();
                    f[vert.getParents()[i].getNode().indexInQueue] = true;
                }
            }
        }

        InterpretationNode[] rst = new InterpretationNode[qf];
        for(int i = 0; i < qf; i++)
            rst[i] = queue[i];
        return rst;
    }
}