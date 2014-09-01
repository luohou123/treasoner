package Checker;

import Enums.NodeType;
import KnowledgeBase.RuleGraph.RuleGraph;
import KnowledgeBase.RuleGraph.RuleNode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Класс реализует алгоритм Эдмондса для поиска классов эквивалентности вершины.
 * @author Andrey Grigoryev
 */
class Automorphism {

    /**
     * Класс для хранения пар вершина/высота вершины в дереве.
     */
    class NodeWithDepth {
        /**
         * Высота вершины.
         */
        public int d;
        /**
         * Ссылка на вершину.
         */
        public RuleNode r;
        public NodeWithDepth(RuleNode rn, int depth) {
            r = rn;
            d = depth;
        }
    }
    
    /**
     * Класс реализует метод для сравенения объектов {@link NodeWithDepth}.
     */
    class NwDComp implements Comparator<NodeWithDepth> {
        public NwDComp() { }
        public int compare(NodeWithDepth o1, NodeWithDepth o2) {
            if(o1.d < o2.d) {
                return -1;
            }
            if(o1.d > o2.d) {
                return 1;
            }
            return 0;
        }
    }
    
    private RuleGraph tree;
    private String label[] = null;
    private NodeWithDepth nodes[] = null;
    private int height[] = null;
    int n = 0;
    
    HashMap<RuleNode, Integer> hm = null;
    HashSet<String> hs = null;
    
    HashMap<RuleNode, String> lab = null;
    
    /**
     * Основной конструктор класса, который определяет ссылку на лес ограничений, на котором выоплняется 
     * поиск классов эквивалентности.
     * @param t Ссылка на лес ограничений TBox текущей онтологии.
     */
    public Automorphism(RuleGraph t) {
        tree = t;
    }

    /**
     * Метод осуществляет доступ для записи ссылки на на лес ограничений, на котором выоплняется
     * поиск классов эквивалентности.
     * @param t Ссылка на лес ограничений TBox текущей онтологии.
     */
    public void setRuleGraph(RuleGraph t) {
        tree = t;
    }
    
    int f[];
    /**
     * Метод реализует алгоритм поиска в глубину.
     * @param x Номер вершины в графе.
     * @return Высота вершины.
     */
    private int DFS(int x) {
        f[Math.abs(x)] = 1;
        RuleNode rn = tree.getNode(Math.abs(x));
        int max = 0;
        for(int i = 0; i < rn.getChildrenSize(); i++) {
            if(f[Math.abs(rn.getChildren()[0])] == 1) {
                if( max < height[Math.abs(rn.getChildren()[i])] + 1) {
                    max = height[Math.abs(rn.getChildren()[i])] + 1;
                }
                continue;
            }
            int m = DFS(Math.abs(rn.getChildren()[i]));
            if( max < m + 1) {
                max = m + 1;
            }
        }
        nodes[n] = new NodeWithDepth(tree.getNode(Math.abs(x)), max);
        height[Math.abs(x)] = max;
        n++;
        return max;
    }
    
    /**
     * Возвращает класс эквивалентности вершины.
     * @param rn Указатель на вершину.
     * @return Номер класса эквивалентности.
     */
    private int getID(RuleNode rn) {
        return hm.get(rn);
    }

    /**
     * Метод осуществления сортировки вершин дерева в соответствии с их высотой.
     * @param node_index Номер вершины в дереве.
     */
    private void sort(int node_index) {
        f = new int[tree.getNodesCount()];
        height = new int[tree.getNodesCount()];
        nodes = new NodeWithDepth[tree.getNodesCount()];
        n = 0;
        DFS(node_index);
        
        hm = new HashMap<RuleNode, Integer>();
        hs = new HashSet<String>();
        lab = new HashMap<RuleNode, String>();
        
        Arrays.sort(nodes, 0, n, new NwDComp());

        for(int i = 0; i < n; i++) {
            hm.put(nodes[i].r, i);
        }
    }
    
    /**
     * Метод реализует алгоритма Эдмондса в котором определяются метки каждой вершины 
     * и при этом выделются классы эквивалентности вершин.
     * @param node_index Корень дерева.
     */
    public void labelTree(int node_index) {
        label = new String[tree.getNodesCount()];
        sort(node_index);
        for(int i = 0; i < n; i++) {
            nodes[i].r.clearParent();
        }

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < nodes[i].r.getChildrenSize(); j++) {
                RuleNode rn = tree.getNode(Math.abs(nodes[i].r.getChildren()[j]));
                if(nodes[i].r.getChildren()[j] < 0) {
                    rn.addParent(-i);
                } else {
                    rn.addParent(i);
                }
            }
        }
        
        //go to down top direction
        for(int i = 0; i < n; i++) {
            //Sorting working labels on same depth and determining label of vertice
            if(i > 0) {
                if(nodes[i].d != nodes[i - 1].d)
                {
                    String temp_labels[] = new String[i];
                    int m = 0;
                    hs.clear();
                    for(int j = i - 1; j >= 0; j--) {
                        if(nodes[j].d != nodes[i - 1].d) break;
                        hs.add(label[j]);
                    }
                    
                    for(String s: hs) {
                        temp_labels[m++] = s;
                    }
                    Arrays.sort(temp_labels, 0, m);
                    
                    for(int j = i - 1; j >= 0; j--) {
                        if(nodes[j].d != nodes[i - 1].d) break;
                        int num = 0;
                        for(int k = 0; k < m; k++) {
                            if(label[j].equals(temp_labels[k])) {
                                num = k + 1; break;
                            }
                        }
                        label[j] = String.valueOf(num);
                    }
                }
            }
            //Determing working label
            String current_label = "";
            if(nodes[i].r.getNodeType() == NodeType.ntAND) {
                current_label = "2";
            } else
            if(nodes[i].r.getNodeType() == NodeType.ntOR) {
                current_label = "3";
            } else
            if(nodes[i].r.getNodeType() == NodeType.ntSOME) {
                current_label = "4";
            } else
            if(nodes[i].r.getNodeType() == NodeType.ntALL) {
                current_label = "5";
            } else
            if(nodes[i].r.getNodeType() == NodeType.ntNOT) {
                current_label = "6";
            } else {
                current_label = "1";
            }
            
            String labels[] = new String[nodes[i].r.getChildrenSize()];
            for(int j = 0; j < nodes[i].r.getChildrenSize(); j++) {
                labels[j] = "";
                int ind = nodes[i].r.getChildren()[j]; 
                if(ind < 0) labels[j] = "-";
                ind = Math.abs(ind);
                labels[j] = labels[j] + label[getID(tree.getNode(ind))];
            }
            Arrays.sort(labels, 0, nodes[i].r.getChildrenSize());
            for(int j = 0; j < nodes[i].r.getChildrenSize(); j++) {
                current_label = current_label + labels[j];
            }
            label[i] = current_label;
        }
        
        String temp_labels[] = new String[n];
        int m = 0;
        hs.clear();
        for(int j = n - 1; j >= 0; j--) {
            if(nodes[j].d != nodes[n - 1].d) break;
            hs.add(label[j]);
        }

        for(String s: hs) {
            temp_labels[m++] = s;
        }
        Arrays.sort(temp_labels, 0, m);

        for(int j = n - 1; j >= 0; j--) {
            if(nodes[j].d != nodes[n - 1].d) break;
            int num = 0;
            for(int k = 0; k < m; k++) {
                if(label[j].equals(temp_labels[k])) {
                    num = k + 1; break;
                }
            }
            label[j] = String.valueOf(num);
        }
        
        //go to top down direction
        String j_label[] = new String[tree.getNodesCount()];
        for(int i = n - 1; i >= 0; i--) {
            if(i < n - 1) {
                if(nodes[i].d != nodes[i + 1].d) {
                    //System.out.println();
                    String j_temp_labels[] = new String[n - i + 1];
                    int mm = 0;
                    hs.clear();
                    for(int j = i + 1; j < n; j++) {
                        if(nodes[j].d != nodes[i + 1].d) break;
                        hs.add(j_label[j]);
                    }
                    for(String s: hs) {
                        j_temp_labels[mm++] = s;
                    }
                    Arrays.sort(j_temp_labels, 0, mm);
                    for(int j = i + 1; j < n; j++) {
                        if(nodes[j].d != nodes[i + 1].d) break;
                        int nnum = 0;
                        for(int k = 0; k < mm; k++) {
                            if(j_temp_labels[k].equals(j_label[j])) {
                                nnum = k + 1; break;
                            }
                        }
                        j_label[j] = String.valueOf(nnum);
                    }
                }
            }            
            String current_label = label[i];
            String labels[] = new String[nodes[i].r.getParentsSize()];
            for(int j = 0; j < nodes[i].r.getParentsSize(); j++) {
                labels[j] = "";
                int ind = nodes[i].r.getParents()[j];
                if(ind < 0) {
                    labels[j] = "-";
                }
                ind = Math.abs(ind);
                labels[j] = labels[j] + j_label[ind];
            }
            Arrays.sort(labels, 0, nodes[i].r.getParentsSize());
            for(int j = 0; j < nodes[i].r.getParentsSize(); j++) {
                current_label = current_label + labels[j];
            }
            j_label[i] = current_label;
        }
        
        String j_temp_labels[] = new String[n];
        int mm = 0;
        hs.clear();
        for(int j = 0; j < n; j++) {
            if(nodes[j].d != nodes[0].d) break;
            hs.add(j_label[j]);
        }
        for(String s: hs) {
            j_temp_labels[mm++] = s;
        }
        Arrays.sort(j_temp_labels, 0, mm);
        for(int j = 0; j < n; j++) {
            if(nodes[j].d != nodes[0].d) break;
            int nnum = 0;
            for(int k = 0; k < mm; k++) {
                if(j_temp_labels[k].equals(j_label[j])) {
                    nnum = k + 1; break;
                }
            }
            j_label[j] = String.valueOf(nnum);
        }

        int[] flag = new int[nodes.length];
        Arrays.fill(flag, 0);
        for(int i = 0; i < n - 1; i++) {
            if(flag[i] == 1) continue;
            //System.out.print(nodes[i].r.getNodeType() + " ");
            int class_id = i + 100000;
            nodes[i].r.setCacheClass(class_id);
            for(int j = i + 1; nodes[j].d == nodes[i].d; j++) {
                if(j_label[i].equals(j_label[j])) {
                    nodes[j].r.setCacheClass(class_id);
                    flag[j] = 1;
                }
            }
        }
    }
}