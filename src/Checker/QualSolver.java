package Checker;

import Checker.Model.InterpretationNode;
import Enums.NodeType;
import Help.SHash;
import KnowledgeBase.RuleGraph.RuleGraph;
import KnowledgeBase.TBox;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

/**
 * Класс реализует алгоритм получения списков объединяемых вершин.
 * @author Andrey Grigoryev
 */
class QualSolver {
    private int[] p = null;
    private QSet[] set = null;
    private SHash hash = null;
    
    private QSet[] fails = new QSet[32];
    int cn = 0;
    
    /**
     * Основной и единственный конструктор класса.
     */
    public QualSolver() {
        hash = new SHash();
    }
    
    /**
     * Метод осуществляет добавление множества вершин, которое не может быть объединено
     * в список таких множеств.
     * @param fl Множество вершин, которое не может быть объединено.
     */
    public void add(QSet fl) {
        if( hash.add(fl.a, true) == 0) {
            if(cn < 31)
                fails[cn++] = new QSet(fl);
        }
    }
    
    /**
     * Метод определяет номер множества по номеру вершины.
     * @param x Номер вершины.
     * @return Номер множества.
     */
    private int P(int x) {
        if(p[x] != x) return P(p[x]);
        return x;
    }
    
    /**
     * Метод для слияния двух множеств.
     * Если объединение множеств содержится в списке множеств вершины из которых 
     * могут быть объединены.
     * @param x Определяет номер вершины из первого множества.
     * @param y Определяет номер вершины из второго множества. 
     * @return Возвращает истина, если множества могут быть объединены и ложь в противном случае.
     */
    private boolean MergeSet(int x, int y) {
        QSet st = new QSet(set[x]);
        st.MergeSet(set[y]);
        if(hash.add(st.a, false) != 0) {
            return false;
        }
        set[x].MergeSet(set[y]);
        set[y] = null;
        return true;
    }
    
    /**
     * Метод для определения того могут ли быть объединены два множества вершин графа.
     * @param x Определяет номер вершины из первого множества.
     * @param y Определяет номер вершины из второго множества.
     * @return Возвращает истина, если множества могут быть объединены и ложь в противном случае.
     */
    private boolean Merge(int x, int y) {
        Random rand = new Random();
        if(rand.nextBoolean()) {
            if(!MergeSet(P(y), P(x))) return false;
            p[P(x)] = P(y);
        } else {
            if(!MergeSet(P(x), P(y))) return false;
            p[P(y)] = P(x);
        }
        return true;
    }
    
    /**
     * Метод реализует алгоритм Краскала для определения множеств индивидов, которые 
     * необходимо объединить.
     * @param G Граф, на котором выполняется алгоритм Краскала.
     * @param countOfIndivids Количество индивидов, которое необходимо получить.
     * @return Массив множеств, каждое из которых соответствует тому как нужно объединять вершины.
     */
    public QSet[] getNewMatching(IndividGraph G, int countOfIndivids) {
        QSet[] new_matching = null;
        int[][] edges = G.getSortedEdges();
        p = new int[G.n];
        set = new QSet[G.n];
        if(hash == null) {
            hash = new SHash();
        }
        
        for(int i = 0; i < G.n; i++) {
            p[i] = i;
            if(set[i] == null)
                set[i] = new QSet();
            set[i].count = 1;
            set[i].a[0] = i;
        }
        
        int total = G.n;
        for(int i = 0; i < edges.length; i++) {
            if(P(edges[i][0]) != P(edges[i][1])) {
                if(Merge(edges[i][0], edges[i][1]))
                    total--;
            }
            if(total <= countOfIndivids) {
                break;
            }
        }

        int cnt = 0;
        for(int i = 0; i < G.n; i++) {
            if(set[i] != null) cnt++;
        }
        new_matching = new QSet[cnt];
        cnt = 0;
        for(int i = 0; i < G.n; i++) {
            if(set[i] != null) {
                new_matching[cnt++] = set[i];
            }
        }
        return new_matching;
    }
}

/**
 * Класс для представления индивидов в виде графа, где каждый потомок вершины представляется 
 * в виде вершины графа, а вес на ребрах между каждой парой вершин определяется как
 * вероятность того, что их конъюнкция будет согласованной.
 * Данный позволяет определить количество интерпретаций в которых может появиться 
 * определенный концепт.
 * @author Andrey Grigoryev
 */
class IndividGraph {
    public double[][] a = new double[32][32];
    public int n = 0;

    /**
     * Основной и единственный конструктор класса.
     * С помощью конструктора заполняется ссылка на TBox, и определяются веса всех
     * ребер.
     * @param nodes Массив индивидов, которых необходимо объединить.
     * @param t Ссылка на TBox рассматриваемой онтологии.
     */
    public IndividGraph(InterpretationNode[] nodes, TBox t) {
        n = nodes.length;
        for(int i = 0; i < n; i++) {
            a[i][i] = 0;
            for(int j = i + 1; j < n; j++) {
                a[j][i] = a[i][j] = getDistance(nodes[i], nodes[j], t);
            }
        }
    }

    /**
     * Класс для представления вектора имен концепто, в котором каждому концепту ставится
     * в соответствие число.
     */
    class LiteralVector {
        public HashMap<String, Double> vec;
        /**
         * Основной и единственный конструктор класса, который инициализирует ассоциативный 
         * массив.
         */
        public LiteralVector() {
            vec = new HashMap<String, Double>();
        }
        
        /**
         * Метод осуществляет добавление в ассоциативный массив вероятности появления 
         * имени концепта в метке индивида.
         * @param liter
         * @param value 
         */
        public void add(String liter, double value) {
            if(!vec.containsKey(liter))
                vec.put(liter, Double.valueOf(0));
            vec.put(liter, vec.get(liter) + value);
        }
        
        /**
         * Метод реализует суммирование двух векторов имен концептов.
         * @param l Вектор, который прибавляется к текущему.
         */
        public void addVector(LiteralVector l) {
            for(String ks: l.vec.keySet())
                add(ks, l.vec.get(ks));
        }
        
        /**
         * Метод реализует произведение двух векторов имен концептов.
         * @param l Вектор, который умножается на текущий.
         * @param c1 Общее количество интерпетаций первого индивида которое может быть построено
         * табличным алгоритмом.
         * @param c2 Общее количество интерпетаций второго индивида которое может быть построено
         * табличным алгоритмом.
         */
        public void multiplyVector(LiteralVector l, double c1, double c2) {
            for(String ks: l.vec.keySet()) {
                if(vec.containsKey(ks)) {
                    vec.put(ks, vec.get(ks) * c2 + l.vec.get(ks) * c1 - vec.get(ks) * l.vec.get(ks));
                } else {
                    add(ks, l.vec.get(ks));
                }
            }
        }
        
        /**
         * Метод определяет длину вектора.
         * @return Длина ветора.
         */
        public double calcLength() {
            double total_length = 0;
            for(double d: vec.values()) {
                total_length += d * d;
            }
            return Math.sqrt(total_length);
        }
        
        /**
         * Метод определяет угол между двумя векторами.
         * @param l Вектор, до которого необходимо вычислить угол.
         * @return Угол между векторами.
         */
        public double calcDistance(LiteralVector l) {
            double distance = 0;
            for(String ks: vec.keySet()) {
                if(l.vec.containsKey(ks)) {
                    distance += l.vec.get(ks) * vec.get(ks);
                }
            }
            if(l.calcLength() == 0) {
                return calcLength();
            }
            if(calcLength() == 0) {
                return l.calcLength();
            }
            return distance / l.calcLength() / calcLength();
        }
    }
    
    /**
     * Возвращает вектор имен концептов с количеством интерпретации в которых они появляются
     * @param x Определяет номер вершины в лесу ограничений.
     * @param rg Определеяет лес ограничений.
     * @param to_ret Определяет префикс ролей в кванторах, который соответствует текущему концепту.
     * @param G Определяет количество различных интерпретаций данного индивида.
     * @return Возвращает вектор концептов с указанием количества интерпретации, в которые входит каждый концепт.
     */
    public LiteralVector getCountVector(int x, RuleGraph rg, String to_ret, Double G)
    {
        if( rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntCONCEPT || 
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntDATATYPE || 
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntHASSELF ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntINDIVID ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntLITER ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntNOT ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntNOTHING ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntTHING ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntUNDEF)
        {
            LiteralVector L = new LiteralVector();
            L.add(to_ret + rg.getNode(Math.abs(x)).getName(), 1.0);
            G = 1.0;
            return L;
        } else
        if( rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntALL ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntSOME ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntEXTCARD ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntMINCARD ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntMAXCARD
            )
        {
            LiteralVector L = getCountVector(rg.getNode(Math.abs(x)).getChildren()[0], rg, to_ret + String.valueOf(rg.getNode(Math.abs(x)).getRoleType()), G);
            return L;
        } else
        if( rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntAND && x > 0 ||
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntOR && x < 0)
        {
            LiteralVector L = new LiteralVector();
            for(int i = 0; i < rg.getNode(Math.abs(x)).getChildrenSize(); i++)
            {
                Double new_G = 0.0;
                LiteralVector O = getCountVector(rg.getNode(Math.abs(x)).getChildren()[i], rg, to_ret, new_G);
                L.multiplyVector(O, G, new_G);
                G = G * new_G;
            }
            return L;
        } else
        if( rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntOR && x > 0 || 
            rg.getNode(Math.abs(x)).getNodeType() == NodeType.ntAND && x < 0)
        {
            LiteralVector L = new LiteralVector();
            for(int i = 0; i < rg.getNode(Math.abs(x)).getChildrenSize(); i++)
            {
                Double new_G = 0.0;
                L.addVector(getCountVector(rg.getNode(Math.abs(x)).getChildren()[i], rg, to_ret, new_G));
                G = G + new_G;
            }
            return L;
        }
        return null;
    }
    
    /**
     * Метод определяет расстоение между двумя вершинами, соответствующими индивидам интерпретации.
     * @param b Первый индивид для которого определяется расстояние.
     * @param c Второй индивид для которого определяется расстояние.
     * @param t TBox текущей рассматриваемой онтологии.
     * @return Расстояние между двумя индивидами, определяющее вероятность согласованности
     * слияния двух индивидов.
     */
    public double getDistance(InterpretationNode b, InterpretationNode c, TBox t)
    {
        LiteralVector L1 = new LiteralVector();
        LiteralVector L2 = new LiteralVector();
        Double total_G1 = 1.0;
        for(int i = 0; i < b.getToDoSize(); i++)
        {
            Double G = 0.0;
            LiteralVector L = getCountVector(b.getToDo()[i], t.getRuleGraph(), "", G);
            L1.multiplyVector(L, total_G1, G);
            total_G1 *= G;
        }
        Double total_G2 = 1.0;
        for(int i = 0; i < c.getToDoSize(); i++)
        {
            Double G = 0.0;
            LiteralVector L = getCountVector(c.getToDo()[i], t.getRuleGraph(), "", G);
            L2.multiplyVector(L, total_G2, G);
            total_G2 *= G;
        }
        return L1.calcDistance(L2);
    }
    
    /**
     * Класс для хранения одного ребра графа.
     */
    private class Edg
    {
        /**
         * Начало ребра.
         */
        public int x;
        /**
         * Конец ребра.
         */
        public int y;
        /**
         * Вес ребра.
         */
        public double w;
    }
    
    /**
     * Класс реализующий метод сравнения ребер.
     */
    private class EdgeComparer implements Comparator<Edg> {
        @Override
        /**
         * Метод для сравнения ребер.
         */
        public int compare(Edg t, Edg t1) {
            if(t.w < t1.w) return -1;
            if(t.w == t1.w) return 0;
            return 1;
        }
    }
    
    /**
     * Класс возвращает список ребер (без веса), отсортированный в порядке возрастания веса на ребре.
     * @return Двумерный массив, первый индекс которого соответствует номеру ребра,
     * второй индекс: 0 - начало ребра, 1 - конец ребра.
     */
    public int[][] getSortedEdges() {
        int m = 0;
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                if(a[i][j] >= 0 && i != j) m++;
        
        int[][] edge = new int[m][2];
        Edg[] EDGES = new Edg[m];
        m = 0;
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                if(a[i][j] >= 0 && i != j) {
                    if( EDGES[m] == null) {
                        EDGES[m] = new Edg();
                    }
                    EDGES[m].x = i;
                    EDGES[m].y = j;
                    EDGES[m++].w = a[i][j];
                }
            }
        }
        Arrays.sort(EDGES, 0, m, new EdgeComparer());
        for(int i = 0; i < m; i++) {
            edge[i][0] = EDGES[i].x;
            edge[i][1] = EDGES[i].y;
        }
        return edge;
    }
}

/**
 * Класс для хранения множества вершин, которые не могут быть объединены.
 * @author Andrey Grigoryev
 */
class QSet {
    
    public int[] a = new int[32];
    public int count = 0;
    
    /**
     * Создает пустой объект.
     */
    public QSet() { }
    
    /**
     * Создает объект на основе другого объекта, копируя все поля второго объекта.
     * @param another Объект на основе которого создается текущий.
     */
    public QSet(QSet another) {
        for(int i = 0; i < another.count; i++) {
            a[count++] = another.a[i];
        }
    }
    
    /**
     * Метод позволяет определить содержит ли множество заданный элемент.
     * @param x Элмент, наличие которого в множестве определяется методом.
     * @return Возвращает истина если заданный элемент содержится в множестве и ложь в противном случае.
     */
    public boolean contains(int x) {
        for(int i = 0; i < count; i++)
            if(a[i] == x) return true;
        return false;
    }
    
    /**
     * Метод объединяет два множества сохраняя отношение упорядоченности.
     * @param another Определяет множество для слияния.
     */
    public void MergeSet(QSet another)
    {
        if(another == null) return;
        int[] a1 = new int[count];
        System.arraycopy(a, 0, a1, 0, count);

        int[] a2 = new int[another.count];
        System.arraycopy(another.a, 0, a2, 0, another.count);
        
        int[] a3 = new int[a1.length + a2.length];
        
        int it1 = 0;
        int it2 = 0;
        int it3 = 0;
        
        while(it1 < a1.length && it2 < a2.length) {
            if(a1[it1] < a2[it2]) {
                a3[it3] = a1[it1];
                it1++; it3++;
            } else
            if(a1[it1] > a2[it2]) {
                a3[it3] = a2[it2];
                it2++; it3++;
            } else {
                a3[it3] = a1[it1];
                it1++; it2++; it3++;
            }
        }

        for(; it1 < a1.length; it1++) {
            a3[it3++] = a1[it1];
        }
        
        for(; it2 < a2.length; it2++) {
            a3[it3++] = a2[it2];
        }

        count = 0;
        for(int i = 0; i < it3; i++) {
            if(i > 0) if(a3[i] == a3[i - 1]) continue;
            a[count++] = a3[i];
        }
    }
}

