package Checker;

import Enums.NodeType;
import Help.IntArray;
import KnowledgeBase.RuleGraph.RuleGraph;
import java.util.HashMap;

/**
 *
 * @author Andrey Grigoryev
 */
class PHChecker {
    
    /**
     * Класс определяет вершину в n-дольном графе в виде (Доля/Вершина)
     */
    class NPartVertice {
        int part, vertice;
    }
    
    /**
     * Класс для хранения клик n-дольного графа.
     */
    class IntArrayHash {
        /**
         * Класс определяет элемент хеш-таблицы.
         */
        class HashItem {
            public int hf1 = 0;
            public int hf2 = 0;
            public HashItem next = null;
        }
        
        final int hc1 = 107;
        final int hc2 = 1051;
        final int hashSize = 1024 * 1024;
        HashItem[] hashTable = new HashItem[hashSize];
        
        /**
         * Метод удаляет все данные из хеш-таблицы.
         */
        public void clear() {
            for(int i = 0; i < hashSize; i++) {
                hashTable[i] = null;
            }
        }
        
        /**
         * Метод осуществляет поиск элемента в хеш-таблице.
         * @param s Массив, поиск которого производится в хеш-таблице.
         * @return Возвращает истина, если элемент найден в хеш-таблице и ложь в противном случае.
         */
        public boolean find(IntArray s) {
            int pow1 = 1;
            int pow2 = 2;
            int hf1 = 0;
            int hf2 = 0;
            for(int i = 0; i < s.size(); i++) {
                hf1 = (hf1 * pow1 + s.get(i)) % hashSize;
                hf2 = (hf2 * pow2 + s.get(i)) % hashSize;
                pow1 = (pow1 * hc1) % hashSize;
                pow2 = (pow2 * hc1) % hashSize;
            }
            int addr = (hf1 + hf2) % hashSize;
            while(addr < 0)
                addr = addr + hashSize;

            for(HashItem it = hashTable[addr]; it != null; it = it.next) {
                if(it.hf1 == hf1 && it.hf2 == hf2) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Метод осуществляет добавление элемента в хеш-таблицу.
         * @param s Массив, который добавляется в хеш-таблицу.
         */
        public void add(IntArray s) {
            int pow1 = 1;
            int pow2 = 2;
            int hf1 = 0;
            int hf2 = 0;
            for(int i = 0; i < s.size(); i++) {
                hf1 = (hf1 * pow1 + s.get(i)) % hashSize;
                hf2 = (hf2 * pow2 + s.get(i)) % hashSize;
                pow1 = (pow1 * hc1) % hashSize;
                pow2 = (pow2 * hc1) % hashSize;
            }
            int addr = (hf1 + hf2) % hashSize;
            while(addr < 0)
                addr = addr + hashSize;
            HashItem it = new HashItem();
            it.hf1 = hf1;
            it.hf2 = hf2;
            it.next = hashTable[addr];
            hashTable[addr] = it;
        }
    }
    
    private RuleGraph tree = null;
    private int[][] cand = null;
    private int[] cand_count = null;
    private int parts_count = 0;
    private HashMap<Integer, Integer> part_num = null;
    HashMap<Integer, IntArray> list = null;
    IntArrayHash checked = new IntArrayHash();
    
    /**
     * Конструктор устанавливает ссылку на лес ограничений, на котором выполняется алгоритм.
     * @param t Ссылка на лес ограничений.
     */
    public PHChecker(RuleGraph t) {
        tree = t;
    }
    
    /**
     * Метод очищает все поля класса.
     */
    public void clear() {
        if(list != null) list.clear();
        if(part_num != null) part_num.clear();
        cand = null;
        cand_count = null;
        checked.clear();
        parts_count = 0;
    }
    
    /**
     * Метод устанавливает ссылку на лес ограничений, на котором выполняется алгоритм.
     * @param t Ссылка на лес ограничений.
     */
    public void setRuleGraph(RuleGraph t) {
        tree = t;
    }
    
    /**
     * Метод реализует алгоритм Брона-Кербоша для определения размера максимальной клики к графе.
     * @param part Определяет долю в графе, в которой перебираются вершины.
     * @return Размер максимальной клики.
     */
    private boolean BronKerbosch(int part)
    {
        if(part == parts_count) {
            return true;
        }
        for(int i = 0; i < cand_count[part]; i++) {
            int v = cand[part][i];
            IntArray ls = list.get(v);
            if(ls == null) {
                ls = new IntArray();
                list.put(v, ls);
            }
            IntArray to_add = new IntArray();
            for(int j = 0; j < ls.size(); j++) {
                int vert = ls.get(j);
                if(!part_num.containsKey(vert)) {
                    continue;
                }
                int pn = part_num.get(vert);
                for(int k = 0; k < cand_count[pn]; k++) {
                    if(cand[pn][k] == vert) {
                        int h = cand[pn][k];
                        cand[pn][k] = cand[pn][cand_count[pn] - 1];
                        cand[pn][cand_count[pn] - 1] = h;
                        cand_count[pn]--;
                        to_add.add(h);
                    }
                }                
            }
            
            IntArray to_hash = new IntArray();
            for(int j = part + 1; j < parts_count; j++) {
                for(int k = 0; k < cand_count[j]; k++) {
                    to_hash.addOnce(cand[j][k]);
                }
            }
            to_hash.sort();
            //if(!checked.find(to_hash))
            {
                if(BronKerbosch(part + 1)) {
                    return true;
                }
                //checked.add(to_hash);
            }

            for(int j = 0; j < to_add.size(); j++) {
                int vert = to_add.get(j);
                int pn = part_num.get(vert);
                cand[pn][cand_count[pn]++] = vert;
            }
        }
        return false;
    }
    
    /**
     * Метод осуществляет добавление ребра между вершинами.
     * @param v1 Начальная вершина ребра.
     * @param v2 Конечная вершина ребра.
     */
    private void addEdge(int v1, int v2) {
        if(list.containsKey(v1)) {
            list.get(v1).add(v2);
        } else {
            IntArray ar = new IntArray();
            ar.add(v2);
            list.put(v1, ar);
        }
    }
    
    /**
     * Метод осуществляет построение n-дольного графа по двум концептам.
     * @param c1 Идентификационный номер вершины леса ограничений, соответствующей первому концепту.
     * @param c2 Идентификационный номер вершины леса ограничений, соответствующей второму концепту.
     * @return Возвращает ложь, если концепты являются неперсекающимися и истина в противном случае.
     */
    private boolean check(int c1, int c2) {
        int[] c1child = tree.getNode(Math.abs(c1)).getChildren();
        int[] c2child = tree.getNode(Math.abs(c2)).getChildren();
        
        int c1chsize = tree.getNode(Math.abs(c1)).getChildrenSize();
        int c2chsize = tree.getNode(Math.abs(c2)).getChildrenSize();
        
        parts_count = c1chsize;
        if(tree.getNode(Math.abs(c1child[0])).getChildrenSize() == 0) return true;
        cand = new int[parts_count][tree.getNode(Math.abs(c1child[0])).getChildrenSize() + 10];
        cand_count = new int[parts_count];
        part_num = new HashMap<Integer, Integer>();
        //make new candidate list
        for(int i = 0; i < c1chsize; i++) {
            int zn = 1;
            if(c1 > 0 && c1child[i] < 0 || c1 < 0 && c1child[i] > 0) zn = -1;
            if( tree.getNode(Math.abs(c1child[i])).getNodeType() == NodeType.ntOR  && zn ==  1 ||
                tree.getNode(Math.abs(c1child[i])).getNodeType() == NodeType.ntAND && zn == -1) {
                cand_count[i] = tree.getNode(Math.abs(c1child[i])).getChildrenSize();
                for(int j = 0; j < tree.getNode(Math.abs(c1child[i])).getChildrenSize(); j++) {
                    cand[i][j] = zn * tree.getNode(Math.abs(c1child[i])).getChildren()[j];
                    part_num.put(cand[i][j], i);
                }                
            }
            if( tree.getNode(Math.abs(c1child[i])).getNodeType() != NodeType.ntOR && c1child[i] > 0 ||
                tree.getNode(Math.abs(c1child[i])).getNodeType() != NodeType.ntAND && c1child[i] < 0) {
                int z = 1;
                if(c1 < 0) z = -1;
                cand[i][0] = z * c1child[i];
                cand_count[i] = 1;
                part_num.put(cand[i][0], i);
            }
        }
        //add edges between vertices
        list = new HashMap<Integer, IntArray>();
        for(int i = 0; i < c2chsize; i++) {
            if( tree.getNode(Math.abs(c2child[i])).getChildrenSize() == 2 &&
                (tree.getNode(Math.abs(c2child[i])).getNodeType() == NodeType.ntOR && c2 > 0 || 
                 tree.getNode(Math.abs(c2child[i])).getNodeType() == NodeType.ntAND && c2 < 0)) {
                int zn = 1;
                if(c2 > 0 && c2child[i] < 0 || c2 < 0 && c2child[i] > 0) zn = -1;
                addEdge(-zn * tree.getNode(Math.abs(c2child[i])).getChildren()[0], -zn * tree.getNode(Math.abs(c2child[i])).getChildren()[1]);
                addEdge(-zn * tree.getNode(Math.abs(c2child[i])).getChildren()[1], -zn * tree.getNode(Math.abs(c2child[i])).getChildren()[0]);
            }
        }
        return BronKerbosch(0);
    }
    
    /**
     * Метод определяет являются ли два концепта непересекающимися между собой, 
     * если каждый из них является дизъюнкцией других концептов.
     * @param c1
     * @param c2
     * @return 
     */
    public boolean isDisjoint(int c1, int c2)
    {
        NodeType nt1 = tree.getNode(Math.abs(c1)).getNodeType();
        NodeType nt2 = tree.getNode(Math.abs(c2)).getNodeType();
        if(c1 < 0) {
            if(nt1 == NodeType.ntAND) nt1 = NodeType.ntOR;
            if(nt1 == NodeType.ntOR) nt1 = NodeType.ntAND;
        }
        
        if(c2 < 0) {
            if(nt2 == NodeType.ntAND) nt2 = NodeType.ntOR;
            if(nt2 == NodeType.ntOR) nt2 = NodeType.ntAND;
        }
        
        if(nt1 != NodeType.ntAND || nt2 != NodeType.ntAND) {
            return false;
        }
        
        clear();
        return check(c1, c2);
    }
}
