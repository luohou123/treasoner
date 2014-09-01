package Checker;

import Enums.NodeType;
import Checker.Model.InterpretationNode;
import KnowledgeBase.TBox;
import KnowledgeBase.RuleGraph.RuleNode;

/**
 * Класс реаилизует алгоритм семантико-синтаксического выбора.
 * @author Andrey Grigoryev
 */
public class AChecker {

    /**
     * Класс для работы с парой целых чисел.
     * Был разработан для того, чтобы хранить в хеш-таблице значения концептов, которые
     * уже были обработаны алгоритмом семантико-синтаксического выбора.
     */
    private class Pair {
        private final int first;
        private final int second;
        
        /**
         * Конструктор класса, который определяет новую пару.
         * @param rule1 Первый элемент новой пары.
         * @param rule2 Второй элемент новой пары. 
         */
        public Pair(int rule1, int rule2) {
            first = rule1;
            second = rule2;
        }
        
        /**
         * Метод возвращает первый элемент пары.
         * @return Первый элемент пары.
         */
        public int getFirst() {
            return first;
        }

        /**
         * Метод возвращает второй элемент пары.
         * @return Второй элемент пары.
         */
        public int getSecond() {
            return second;
        }
        
        /**
         * Переопределяет метод сравнения двух пар.
         * @param obj Определяет пару для сравнения.
         * @return Возвращает истина, если левые и правые компоненты пар равны и ложь в противном случае.
         */
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Pair) {
                Pair temp = (Pair) obj;
                return temp.getFirst() == first && temp.getSecond() == second;
            }
            return this == obj;
        }

        /**
         * Возвращает хеш-код данной пары.
         * @return Хеш-код пары для её идентификации в таблице.
         */
        @Override
        public int hashCode() {
            int hash = 0;
            hash = first * 107 + second;
            if(hash < 0) hash = -hash;
            return hash;
        }
    }
    
    /**
     * Класс для хранения хеш-таблицы.
     */
    private class myHash {
        
        /**
         * Новый конструктор класса, инициализирующий поля объекта.
         */
        public myHash() {
            clear();
        }
        
        private int hashSize = 1024 * 1024;
        private int currentSize = 0;
        
        /**
         * Класс определяет элемент хеш-таблицы.
         */
        private class Pr {
            
            /**
             * Определяет первый элемент пары.
             */
            public int x1;
            /**
             * Определяет второй элемент пары.
             */
            public int x2;
            /**
             * Определяет являются ли заданные концепты непересекающимися.
             */
            public int value;
            /**
             * Определяет ссылку на следующий элемент хеш-таблицы.
             */
            public Pr next = null;
            
            /**
             * Конструктор класса определяющий все поля объекта.
             * @param y1 Первый элемент пары.
             * @param y2 Второй элемент пары.
             * @param val Определяет являются ли заданные концепты непересекающимися.
             */
            public Pr(int y1, int y2, int val) {
                x1 = y1;
                x2 = y2;
                value = val;
            }
        }
        
        private final Pr[] table = new Pr[hashSize];
        /**
         * Метод добавляет пару в хеш-таблицу.
         * @param x1 Первый элемент пары.
         * @param x2 Второй элемент пары.
         * @param v Определяет являются ли заданные концепты непересекающимися.
         */
        public void add(int x1, int x2, int v) {
            int hf = (x1 * 106033 + x2 * 107);
            if(hf < 0) hf = -hf;
            hf %= hashSize;
            int branchSize = 0;
            for(Pr p = table[hf]; p != null; p = p.next) {
                branchSize++;
                if(p.x1 == x1 && p.x2 == x2) {
                    p.value = v;
                    return;
                }
            }

            Pr nw = new Pr(x1, x2, v);
            nw.next = table[hf];
            table[hf] = nw;
            if( currentSize < branchSize + 1) {
                currentSize = branchSize + 1;
            }
        }
        
        /**
         * Метод определяет содержится ли такая пара в хеш-таблице.
         * @param x1 Первый элемент пары.
         * @param x2 Второй элемент пары.
         * @return Возвращает истина, если такая пара содержится в хеш-таблице и ложь в противном случае.
         */
        public boolean contains(int x1, int x2) {
            int hf = (x1 * 106033 + x2 * 107);
            if(hf < 0) hf = -hf;
            hf %= hashSize;
            for(Pr p = table[hf]; p != null; p = p.next)
                if(p.x1 == x1 && p.x2 == x2)
                    return true;

            return false;
        }
        
        /**
         * Метод определяет являются ли заданные концепты непересекающимися.
         * @param x1 Первый элемент пары.
         * @param x2 Второй элемент пары.
         * @return Значение 1 если концепты могут пересекаться и 0 в противном случае.
         */
        public int get(int x1, int x2) {
            int hf = (x1 * 106033 + x2 * 107);
            if(hf < 0) hf = -hf;
            hf %= hashSize;
            for(Pr p = table[hf]; p != null; p = p.next)
                if(p.x1 == x1 && p.x2 == x2)
                    return p.value;

            return 0;
        }
        
        /**
         * Возвращает размер хеш-таблицы.
         * @return Количество элементов в хеш-таблице.
         */
        public int getSize() {
            return currentSize;
        }
        
        /**
         * Очищает хеш-таблицу.
         */
        final public void clear() {
            currentSize = 0;
            for(int i = 0; i < hashSize; i++)
                table[i] = null;
        }
    }
    
    myHash disjoint = new myHash();
    private boolean use_ph_checker = false;
    private TBox t_box;
    private PHChecker ph_checker = null;
    public int true_count = 0;
    public int total_count = 0;
    //private int[] haveRole = new int [1024];
    //InterpretationNode current_node = null;
    //private int tmpSize = 0;
    
    /**
     * Конструктор, определяющий ссылку на TBox базы знаний и задающий параметр испольования алгоритма Брона-Кербоша.
     * @param new_t_box Ссылка на TBox рассматриваемой базы знаний.
     * @param uph Определяет нужно ли использовать алгоритм Брона-Кербоша при проверке непересекаемости концептов.
     */
    public AChecker(TBox new_t_box, boolean uph) {
        use_ph_checker = uph;
        t_box = new_t_box;
        if(t_box != null)
            ph_checker = new PHChecker(t_box.getRuleGraph()); else
            ph_checker = new PHChecker(null);
    }
    
    /**
     * Метод определяет могут ли пересекаться два концепта.
     * Данный метод реализует алгоритм семантико-синтаксического выбора.
     * @param p1 Определяет ссылку на вершину леса ограничений, соответствующую первому концепту.
     * @param p2 Определяет ссылку на вершину леса ограничений, соответствующую второму концепту.
     * @return Возвращает 0 если концепты не пересекаются и 1 если они могут пересекаться.
     */
    private int D(int p1, int p2) {
        //System.out.println(p1 + " " + p2);
        if(p1 == -p2) {
            return 0;
        }
        if(disjoint.contains(p1, p2)) return disjoint.get(p1, p2);
        if(disjoint.contains(p2, p1)) return disjoint.get(p2, p1);
        
        disjoint.add(p1, p2, 1);
       
        RuleNode fir = t_box.getRuleGraph().getNode(Math.abs(p1));
        RuleNode sec = t_box.getRuleGraph().getNode(Math.abs(p2));
        
        int s1 = 1, s2 = 1;
        if(p1 < 0) s1 = -1;
        if(p2 < 0) s2 = -1;
        
        //find description of concept if exists
        int lsd = fir.getSubDescription() * s1;
        int rsd = sec.getSubDescription() * s2;                
        int led = fir.getDescription() * s1;
        int red = sec.getDescription() * s2;                
        int ls = lsd, rs = rsd;
        if(fir.isNamed()) {
            ls = led;
        } else {
            if(s1 < 0 || ls == 0) ls = p1;
        }
        if(sec.isNamed()) {
            rs = red;
        } else {
            if(s2 < 0 || rs == 0) rs = p2;
        }
        /////////////////////////////////////

        if((fir.getNodeType() == NodeType.ntOR && p1 > 0) || (fir.getNodeType() == NodeType.ntAND && p1 < 0)) {
            if((sec.getNodeType() == NodeType.ntOR && p2 > 0) || (sec.getNodeType() == NodeType.ntAND && p2 < 0)) {
                int flag = 0;
                for(int it: fir.getChildren()) {
                    int flag1 = D(it * s1, p2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            } else
            if((sec.getNodeType() == NodeType.ntAND && p2 > 0) || (sec.getNodeType() == NodeType.ntOR && p2 < 0)) {
                int flag = 0;
                for(int it: fir.getChildren()) {
                    int flag1 = D(it * s1, p2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            } else
            if(sec.getNodeType() == NodeType.ntCONCEPT || 
                    (sec.getNodeType() == NodeType.ntSOME) || 
                    (sec.getNodeType() == NodeType.ntALL) ||
                    (sec.getNodeType() == NodeType.ntMAXCARD) ||
                    (sec.getNodeType() == NodeType.ntMINCARD)) {
                int flag = 0;
                for(int it: fir.getChildren()) {
                    int flag1 = D(it * s1, p2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            }
        }
        if((fir.getNodeType() == NodeType.ntAND && p1 > 0) || (fir.getNodeType() == NodeType.ntOR && p1 < 0)) {
            if((sec.getNodeType() == NodeType.ntOR && p2 > 0) || (sec.getNodeType() == NodeType.ntAND && p2 < 0)) {
                int flag = 0;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            } else
            if((sec.getNodeType() == NodeType.ntAND && p2 > 0) || (sec.getNodeType() == NodeType.ntOR && p2 < 0)) {
                //AND - AND vertices
                int flag1 = 1;
                for(int it: fir.getChildren()) {
                    int flag2 = D(it * s1, p2);
                    if(flag2 == 0) flag1 = 0;
                }
                if(flag1 == 0) {
                    disjoint.add(p1, p2, 0);
                } else {
                    if(use_ph_checker) {
                        if(ph_checker.isDisjoint(p1, p2)) {
                            //System.out.println("DISJOINT: " + p1 + " " + p2);
                            disjoint.add(p1, p2, 0);
                        } else
                        if(ph_checker.isDisjoint(p2, p1)) {
                            //System.out.println("DISJOINT: " + p1 + " " + p2);
                            disjoint.add(p1, p2, 0);
                        } else
                        disjoint.add(p1, p2, 1);
                    } else {
                        disjoint.add(p1, p2, 1);
                    }  
                }
            } else
            if(sec.getNodeType() == NodeType.ntCONCEPT || 
                    (sec.getNodeType() == NodeType.ntSOME) || 
                    (sec.getNodeType() == NodeType.ntALL) ||
                    (sec.getNodeType() == NodeType.ntMAXCARD) ||
                    (sec.getNodeType() == NodeType.ntMINCARD)) {
                int flag = 1;
                for(int it: fir.getChildren()) {
                    int flag1 = D(it * s1, p2);
                    if(flag1 == 0) flag = 0;
                }
                disjoint.add(p1, p2, flag);
            }
        }
        if(fir.getNodeType() == NodeType.ntCONCEPT) {
            if((sec.getNodeType() == NodeType.ntOR && p2 > 0) || (sec.getNodeType() == NodeType.ntAND && p2 < 0)) {
                int flag = 0;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            } else
            if((sec.getNodeType() == NodeType.ntOR && p2 < 0) || (sec.getNodeType() == NodeType.ntAND && p2 > 0)) {
                int flag = 1;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 0) flag = 0;
                }
                disjoint.add(p1, p2, flag);
            } else
            if(sec.getNodeType() == NodeType.ntCONCEPT) {
                if(ls != p1 || rs != p2)
                    disjoint.add(p1, p2, D(ls, rs)); else
                    disjoint.add(p1, p2, 1);
            } else
            if(sec.getNodeType() == NodeType.ntSOME && p2 > 0 || sec.getNodeType() == NodeType.ntALL && p2 < 0) {
                //need domain
                int res = 1;
                if(t_box.getRBox().getRoleByIndex(sec.getRoleType()).getDomain() != 0) {
                    res = D(ls, t_box.getRBox().getRoleByIndex(sec.getRoleType()).getDomain());
                    disjoint.add(p1, p2, res);
                }
                if(res > 0) {
                    if(ls != p1 || rs != p2)
                        disjoint.add(p1, p2, D(ls, rs)); else
                        disjoint.add(p1, p2, 1);
                }
            } else
            if(sec.getNodeType() == NodeType.ntSOME && p2 < 0 || sec.getNodeType() == NodeType.ntALL && p2 > 0) {
                if(ls != p1 && rs != p2)
                    disjoint.add(p1, p2, D(ls, rs)); else
                    disjoint.add(p1, p2, 1);
            }
        }
        if(fir.getNodeType() == NodeType.ntSOME && p1 > 0 || fir.getNodeType() == NodeType.ntALL && p1 < 0) {
            if((sec.getNodeType() == NodeType.ntOR && p2 > 0) || (sec.getNodeType() == NodeType.ntAND && p2 < 0)) {
                int flag = 0;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            } else
            if((sec.getNodeType() == NodeType.ntOR && p2 < 0) || (sec.getNodeType() == NodeType.ntAND && p2 > 0)) {
                int flag = 1;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 0) flag = 0;
                }
                disjoint.add(p1, p2, flag);
            } else
            if(sec.getNodeType() == NodeType.ntCONCEPT) {
                int res = 1;
                if(t_box.getRBox().getRoleByIndex(fir.getRoleType()).getDomain() != 0) {
                    res = D(t_box.getRBox().getRoleByIndex(fir.getRoleType()).getDomain(), rs);
                    disjoint.add(p1, p2, res);
                }
                if(res > 0) {
                    if(ls != p1 || rs != p2)
                        disjoint.add(p1, p2, D(ls, rs)); else
                        disjoint.add(p1, p2, 1);
                }
            } else
            if(sec.getNodeType() == NodeType.ntSOME && p2 > 0 || sec.getNodeType() == NodeType.ntALL && p2 < 0) {
                disjoint.add(p1, p2, 1);
            } else
            if(sec.getNodeType() == NodeType.ntSOME && p2 < 0 || sec.getNodeType() == NodeType.ntALL && p2 > 0) {
                if(fir.getRoleType() == sec.getRoleType()) {
                    disjoint.add(p1, p2, D(fir.getChildren()[0] * s1, sec.getChildren()[0] * s2));
                } else {
                    disjoint.add(p1, p2, 1);
                }
            }
        }
        if(fir.getNodeType() == NodeType.ntALL && p1 > 0 || fir.getNodeType() == NodeType.ntSOME && p1 < 0) {
            if((sec.getNodeType() == NodeType.ntOR && p2 > 0) || (sec.getNodeType() == NodeType.ntAND && p2 < 0)) {
                int flag = 0;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            } else
            if((sec.getNodeType() == NodeType.ntOR && p2 < 0) || (sec.getNodeType() == NodeType.ntAND && p2 > 0)) {
                int flag = 1;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 0) flag = 0;
                }
                disjoint.add(p1, p2, flag);
            } else
            if(sec.getNodeType() == NodeType.ntCONCEPT) {
                if(ls != p1 && rs != p2)
                    disjoint.add(p1, p2, D(ls, rs)); else
                    disjoint.add(p1, p2, 1);
            } else
            if(sec.getNodeType() == NodeType.ntSOME && p2 > 0 || sec.getNodeType() == NodeType.ntALL && p2 < 0) {
                if(fir.getRoleType() == sec.getRoleType()) {
                    disjoint.add(p1, p2, D(fir.getChildren()[0] * s1, sec.getChildren()[0] * s2));
                } else {
                    disjoint.add(p1, p2, 1);
                }
            }/* else
            if(sec.getNodeType() == NodeType.ntSOME && p2 < 0 || sec.getNodeType() == NodeType.ntALL && p2 > 0)
            {
                if(fir.getRoleType() == sec.getRoleType() && haveRole[sec.getRoleType()] == 1)
                {
                    disjoint.add(p1, p2, D(fir.getChildren().get(0), sec.getChildren().get(0)));
                } else
                {
                    disjoint.add(p1, p2, 1);
                }
            }*/
        }
        
        //NUMBER RESTRICTION
        if( (fir.getNodeType() == NodeType.ntMAXCARD && p1 > 0) || (fir.getNodeType() == NodeType.ntMINCARD && p1 < 0)) {
            int cn1 = fir.getNumberRestriction();
            if(p1 < 0) cn1--;
            if((sec.getNodeType() == NodeType.ntMINCARD && p2 > 0) || (sec.getNodeType() == NodeType.ntMAXCARD && p2 < 0)) {
                int cn2 = sec.getNumberRestriction();
                if(p2 < 0) cn2++;
                if(fir.getRoleType() == sec.getRoleType()) {
                    if(cn1 < cn2) return 0;
                }
            } else
            if((sec.getNodeType() == NodeType.ntOR && p2 > 0) || (sec.getNodeType() == NodeType.ntAND && p2 < 0)) {
                int flag = 0;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            } else
            if((sec.getNodeType() == NodeType.ntOR && p2 < 0) || (sec.getNodeType() == NodeType.ntAND && p2 > 0)) {
                int flag = 1;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 0) flag = 0;
                }
                disjoint.add(p1, p2, flag);
            } else
            if(sec.getNodeType() == NodeType.ntCONCEPT) {
                if(ls != p1 && rs != p2)
                    disjoint.add(p1, p2, D(ls, rs)); else
                    disjoint.add(p1, p2, 1);
            } else
            if(sec.getNodeType() == NodeType.ntSOME && p2 > 0 || sec.getNodeType() == NodeType.ntALL && p2 < 0) {
                if(fir.getRoleType() == sec.getRoleType()) {
                    if(cn1 == 0) {
                        disjoint.add(p1, p2, 0);
                    } else {
                        disjoint.add(p1, p2, 1);
                    }
                }
            } else {
                disjoint.add(p1, p2, 1);
            }
        }
        if( (fir.getNodeType() == NodeType.ntMINCARD && p1 > 0) || (fir.getNodeType() == NodeType.ntMAXCARD && p1 < 0)) {
            int cn1 = fir.getNumberRestriction();
            if(p1 < 0) cn1++;
            if((sec.getNodeType() == NodeType.ntMINCARD && p2 < 0) || (sec.getNodeType() == NodeType.ntMAXCARD && p2 > 0)) {
                int cn2 = sec.getNumberRestriction();
                if(p2 < 0) cn2--;
                if(fir.getRoleType() == sec.getRoleType()) {
                    if(cn1 > cn2) return 0;
                }
            } else
            if((sec.getNodeType() == NodeType.ntOR && p2 > 0) || (sec.getNodeType() == NodeType.ntAND && p2 < 0)) {
                int flag = 0;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 1) flag = 1;
                }
                disjoint.add(p1, p2, flag);
            } else
            if((sec.getNodeType() == NodeType.ntOR && p2 < 0) || (sec.getNodeType() == NodeType.ntAND && p2 > 0)) {
                int flag = 1;
                for(int it: sec.getChildren()) {
                    int flag1 = D(p1, it * s2);
                    if(flag1 == 0) flag = 0;
                }
                disjoint.add(p1, p2, flag);
            } else
            if(sec.getNodeType() == NodeType.ntCONCEPT) {
                if(ls != p1 && rs != p2)
                    disjoint.add(p1, p2, D(ls, rs)); else
                    disjoint.add(p1, p2, 1);
            } else {
                disjoint.add(p1, p2, 1);
            }
        }
        if(disjoint.contains(p1, p2)) return disjoint.get(p1, p2);
        return 1;
    }
    
    /**
     * Метод определяет могут ли пересекаться два концепта.
     * Данный метод обращается к методу, реализующему алгоритм семантико-синтаксического выбора.
     * @param rule1 Определяет ссылку на вершину леса ограничений, соответствующую первому концепту.
     * @param rule2 Определяет ссылку на вершину леса ограничений, соответствующую второму концепту.
     * @param cur_node Определяет индивида, которому принадлежат данные концепты.
     * @return Возвращает 0 если концепты не пересекаются и 1 если они могут пересекаться.
     */
    public boolean isDisjoint(int rule1, int rule2, InterpretationNode cur_node) {
        //if(true) return false;
        /*current_node = cur_node;
        for(int i = 0; i < t_box.getRBox().getRoleSize(); i++)
            haveRole[i] = 0;
        
        for(int i = 0; i < cur_node.getChildSize(); i++)
            for(int j = 0; j < cur_node.getChildren()[i].getRoles().size(); j++)
                haveRole[cur_node.getChildren()[i].getRoles().get(j)] = 1;

        tmpSize = 0;*/
        if(disjoint.getSize() >= 63) { //size of branch with maximal length
            disjoint.clear();
        }
        boolean BL = D(rule1, rule2) == 0;
        //total_count++;
        //if(BL) true_count++;
        return BL;
    }
    
    /**
     * Метод очищает кэш сохраненных результатов тестирования непересекаемости концептов
     */
    public void clear() {
        disjoint.clear();
    }
    
    /**
     * Метод задает новую ссылку на TBox базы знаний.
     * @param _t_box Новый TBox базы знаний.
     */
    public void setTBox(TBox _t_box) {
        t_box = _t_box;
        ph_checker.setRuleGraph(t_box.getRuleGraph());
    }
    
}