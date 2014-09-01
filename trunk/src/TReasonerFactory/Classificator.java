package TReasonerFactory;

import Checker.Cache;
import Checker.SatChecker;
import Help.HashContainer;
import Help.IntArray;
import KnowledgeBase.ABox;
import KnowledgeBase.Query;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * Класс Classificator является посредником между основным классом TReasoner и табличным алгоритмом, реализованным в классе {@link SatChecker}.
 * В данном классе реализуются метод расширенной классификации: Traversal Classification Method.
 * @author Andrey Grigoryev
 */
class Classificator {
    
    private TBox t_box = null;
    private RBox r_box = null;
    private ABox a_box = null;

    private int concept_count = 0;
    private final boolean a_box_reuse = false;
    
    private HashContainer[] no_sub_sum = null;
    private final int no_sub_sum_size = 0;
    
    private OWLOntology Ontology = null;
    private OWLDataFactory df = null;
    private OWLOntologyManager manager = null;

    //sub[x] contains all super-concepts of x
    //sup[x] contains all sub-concepts of x
    private IntArray[] sub = null;
    private IntArray[] sup = null;
    
    private SatChecker s_checker = null;
    
    private boolean use_a_checker = false;
    private boolean use_caching = false;
    
    private IntArray[] eq = null;
    private boolean[] visited = null;
    private int[] f = null;
    private int delete_mark[] = null;
    private final IntArray toDelete = new IntArray();
    private final boolean must_show = false;
    
    private final int taxSize = 0;

    /**
     * Основной конструктор для класса Classificator
     * 
     * @param new_r_box RBox классифицируемой онтологии
     * @param new_t_box TBox классифицируемой онтологии
     * @param new_a_box ABox классифицируемой онтологии
     * @param is_use_a_checker Параметр определяет нужно ли использовать AChecker
     * @param is_use_back_jump Параметр определяет нужно ли использовать расширенный перебор с возвратом
     * @param is_use_caching Параметр определяет нужно ли использовать кэширование
     * @param is_use_global_caching Параметр определяет нужно ли использовать глобальное кэширование
     * @param use_show_stats Параметр определяет нужно ли показывать статистику
     * @param secs Параметр устанавливает предел времени
     */
    public Classificator(RBox new_r_box, TBox new_t_box, ABox new_a_box, boolean is_use_a_checker, boolean is_use_back_jump, boolean is_use_caching, boolean is_use_global_caching, boolean use_show_stats, long secs)
    {
        use_caching = is_use_caching;
        use_a_checker = is_use_a_checker;
        t_box = new_t_box;
        r_box = new_r_box;
        a_box = new_a_box;
        s_checker = new SatChecker(null, r_box, t_box, a_box, 
                is_use_a_checker, 
                is_use_back_jump, 
                is_use_caching, 
                is_use_global_caching, 
                use_show_stats, 
                secs);
    }
    
    /**
     * Метод очищает все поля объекта. В объекте, отвечающем за проверку согласованности выполняется метод clear();
     */
    public void clear()
    {
        if(sub != null)
            for(int i = 0; i < sub.length; i++)
                sub[i] = null;
        sub = null;
        
        if(sup != null)
            for(int i = 0; i < sup.length; i++)
                sup[i] = null;
        sup = null;
        
        if(no_sub_sum != null)
            for(int i = 0; i < no_sub_sum_size; i++)
                if(no_sub_sum[i] != null)
                {
                    no_sub_sum[i].clear();
                    no_sub_sum[i] = null;
                }
        
        s_checker.clear();
    }
    
    /**
     * Метод определяет новый TBox онтологии
     * @param tbox Новый TBox
     */
    public void setTBox(TBox tbox)
    {
        t_box = tbox;
        s_checker.setTBox(t_box);
    }
    
    /**
     * Метод определяет новый RBox онтологии
     * @param rbox Новый RBox
     */
    public void setRBox(RBox rbox)
    {
        r_box = rbox;
        s_checker.setRBox(r_box);
    }
    
    /**
     * Метод определяет новый ABox онтологии
     * @param abox Новый ABox
     */
    public void setABox(ABox abox)
    {
        a_box = abox;
        s_checker.setABox(a_box);
    }
    
    /**
     * Метод добавляет в множество эквивалентных классов ещё два класса.
     * В методе определяются объекты OWLClass и утсаналиваются имена для них.
     * @param x Определяет номер первого эквивалентного класса 
     * @param y Определяет номер второго эквивалентного класса 
     * @param eqx Определяет множество эквивалентных классов в который будут добавлены для определенных
     */
    private void addEquivalenceAxiom(int x, int y, Set<OWLClass> eqx)
    {
        if(x == t_box.getRuleGraph().getConceptsSize() + 2) {
            OWLClass clsE1 = df.getOWLNothing();
            OWLClass clsE2 = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts()[y]));
            eqx.add(clsE1); eqx.add(clsE2);
        } else
        if(y == t_box.getRuleGraph().getConceptsSize() + 2) {
            OWLClass clsE1 = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts()[x]));
            OWLClass clsE2 = df.getOWLNothing();
            eqx.add(clsE1); eqx.add(clsE2);
        } else
        if(x == t_box.getRuleGraph().getConceptsSize() + 1) {
            OWLClass clsE1 = df.getOWLThing();
            OWLClass clsE2 = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts()[y]));
            eqx.add(clsE1); eqx.add(clsE2);
        } else
        if(y == t_box.getRuleGraph().getConceptsSize() + 1) {
            OWLClass clsE1 = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts()[x]));
            OWLClass clsE2 = df.getOWLThing();
            eqx.add(clsE1); eqx.add(clsE2);
        } else {
            OWLClass clsE1 = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts()[x]));
            OWLClass clsE2 = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts()[y]));
            eqx.add(clsE1); eqx.add(clsE2);
        }
    }
    
    /**
     * Метод необходим для определения класса эквивалентности концептов.
     * @param x Элемент, соответствующий номеру класса из которого определяются эквивалентные
     * @param list Параметр для записи всех эквивалентных концептов.
     */
    private void printEquals(int x, ArrayList<Integer> list)
    {
        visited[x] = true;
        list.add(x);
        for(int i = 0; i < eq[x].size(); i++) {
            if(!visited[eq[x].get(i)]) {
                printEquals(eq[x].get(i), list);
            }
        }
    }
    
    /**
     * Метод обходит классы в порядке построения классификации.
     * Для определения подклассов используется алгоритм DFS
     * @param x Начальный элемент 
     * @param prefix Определяет количество пробелов которые ставятся перед именем класса (необходим при отладке)
     * @param tx Множество аксиом включения
     * @param detailedOut Определяет необходимо ли выводить дополнительные аксиомы
     */
    public void printTaxonomy(int x, String prefix, Set<OWLSubClassOfAxiom> tx, boolean detailedOut) {
        //if(f[x] == 1) return;
        f[x] = 1;
        if(x == t_box.getRuleGraph().getConceptsSize() + 1) {
            //System.out.println(prefix + "TOP");
        } else {
            //System.out.println(prefix + t_box.getRuleGraph().getConcepts().get(x));
        }
        if(sup[x] != null)
            for(int i = 0; i < sup[x].size(); i++) {
                OWLClass clsSUP = null;
                OWLClass clsSUB = null;
                if(sup[x].get(i) >= t_box.getRuleGraph().getConceptsSize() + 1) { //this is BOTTOM concept
                    //continue; //it is not necessary to add BOTTOM [= X axiom
                    clsSUP = df.getOWLNothing();
                } else {
                    clsSUB = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts()[sup[x].get(i)]));
                }
                if(x == t_box.getRuleGraph().getConceptsSize() + 1) {
                    clsSUP = df.getOWLThing();
                } else {
                    clsSUP = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts()[x]));
                }
                
                if(s_checker.cache[0][sup[x].get(i)] != null) if(s_checker.cache[0][sup[x].get(i)].getSize() != -1) {
                    if((!clsSUP.isOWLThing() || detailedOut) && (!clsSUP.isOWLNothing() || detailedOut)) {
                        OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(clsSUB, clsSUP);
                        tx.add(ax);
                    }
                } else {
                    if(!detailedOut) {
                        OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(clsSUB, df.getOWLNothing());
                        tx.add(ax);
                    }
                }
                
                if(sup[x].get(i) < t_box.getRuleGraph().getConceptsSize())
                    printTaxonomy(sup[x].get(i), prefix + "  ", tx, detailedOut);
            }
    }
    
    /**
     * Метод осуществляет поиск пути в графе классификации между двумя концептами.
     * @param x Определяет номер концепта, который является подконцептом.
     * @param y Определяет номер концепта, который является надконцептом.
     * @return Возвращает истина, если концепт x является подконцептом концепта x.
     */
    private boolean findPath(int x, int y) {
        if(delete_mark[x] == 1) return false;
        delete_mark[x] = 1;
        for(int i = 0; i < sup[x].size(); i++) {
            if(sup[x].get(i) == y) return true;
            if(findPath(sup[x].get(i), y)) return true;
        }
        return false;
    }

    /**
     * Метод осуществляет удаление лишних, не значащих аксиом связанных с концептом под заданным номером.
     * Удаляются все незначащие аксиомы. Например если существует три аксиомы C [= D,
     * D [= E, C [= E, то будут удалены певрые две аксиомы.
     * @param c Определяет номер концепта, который обрабатывается в данном методе.
     */
    private void deleteUnsign(int c)
    {
        delete_mark = new int[t_box.getRuleGraph().getConceptsSize() + 4];
        toDelete.clear();
        for(int i = 0; i < sup[c].size(); i++) {
            int h = sup[c].get(i);
            for(int j = 0; j < sup[c].size(); j++) {
                if(i == j) continue;
                Arrays.fill(delete_mark, 0);
                if(findPath(sup[c].get(j), h)) {
                    toDelete.add(h); break;
                }
            }
        }
        for(int i = 0; i < toDelete.size(); i++) {
            int z = toDelete.get(i);
            sup[c].delete(z);
            sub[z].delete(c);
        }
    }
    
    private int[] mark = null;
    /**
     * В методе осуществляется проверка включаемости двух заданных концептов.
     * Данный метод необходим для работы фазы поиска сверху в методе перекрестного построения классификации.
     * @param y Предполагаемый подконцепт.
     * @param c Предполагаемый надконцепт.
     * @return Возвращает истина, если концепты включаются и ложь в противном случае.
     */
    private boolean simpleTopSubs(int y, int c) {
        if(mark[y] == 1) {
            return true;
        } else
        if(mark[y] == -1) {
            return false;
        }
        
        if(y == t_box.getRuleGraph().getConceptsSize() + 2) return false; //y is BOTTOM
        if(y == t_box.getRuleGraph().getConceptsSize() + 1) return true; //y is TOP
        if(c == t_box.getRuleGraph().getConceptsSize() + 2) return true; //c is BOTTOM //return !checkSat(y)
        if(c == t_box.getRuleGraph().getConceptsSize() + 1) return false; //c is TOP
        
        //enhanced top search
        for(int i = 0; i < sup[y].size(); i++) { //проверяем если среди тех концептов Z, которые являются подмножеством концепта Y есть надмножество концепта C, то возвращаем true
            int z = sup[y].get(i);
            if(simpleTopSubs(z, c)) {
                mark[y] = 1;
                return true;
            }
        }
        
        if(checkSubsumption(c, y)) {
            mark[y] = 1;
            return true;
        } else {
            mark[y] = -1;
            return false;
        }
    }
    
    /**
     * В методе осуществляется проверка включаемости двух заданных концептов.
     * Данный метод необходим для работы фазы поиска снизу в методе перекрестного построения классификации.
     * @param y Предполагаемый надконцепт.
     * @param c Предполагаемый поддконцепт.
     * @return Возвращает истина, если концепты включаются и ложь в противном случае.
     */
    private boolean simpleBottomSubs(int y, int c) {
        if(mark[y] == 1) {
            return true;
        } else
        if(mark[y] == -1) {
            return false;
        }
        
        if(c == t_box.getRuleGraph().getConceptsSize() + 2) return false; //y is BOTTOM
        if(c == t_box.getRuleGraph().getConceptsSize() + 1) return true; //y is TOP
        if(y == t_box.getRuleGraph().getConceptsSize() + 2) return true; //c is BOTTOM //return !checkSat(y)
        if(y == t_box.getRuleGraph().getConceptsSize() + 1) return false; //c is TOP

        /*for(int i = 0; i < sub[c].size(); i++) {
            int z = sub[c].get(i);
            if(simpleBottomSubs(y, z)) {
                mark[y] = 1;
                return true;
            }
        }*/
        
        if(checkSubsumption(y, c)) {
            mark[y] = 1;
            return true;
        } else {
            mark[y] = -1;
            return false;
        }
    }
    
    private boolean checkTwoRoles(Cache c1, Cache c2, int i, int j) {

        if((c1.exists[i] != 0 && c2.mincar[j]) || (c1.mincar[i] && c2.exists[j] != 0)) {
            return false;
        }

        if(c1.exists[i] != 0 && c2.forAll[j] != 0) {
            if(c1.exists[i] != -1 && c2.forAll[j] != -1) {
                /*String nam1 = t_box.getRuleGraph().getNode(c1.exists[i]).getName();
                String nam2 = t_box.getRuleGraph().getNode(c2.forAll[j]).getName();
                if(nam1 != null && nam2 != null && nam1.length() != 0 && nam2.length() != 0) {
                    return canMerge(
                            s_checker.cache[0][t_box.getRuleGraph().getConceptIndex(nam1)], 
                            s_checker.cache[0][t_box.getRuleGraph().getConceptIndex(nam2)]);
                }*/
                return false;
            } else return false;
        }

        if(c2.exists[j] != 0 && c1.forAll[i] != 0) {
            if(c2.exists[j] != -1 && c1.forAll[i] != -1) {
                /*String nam1 = t_box.getRuleGraph().getNode(c2.exists[j]).getName();
                String nam2 = t_box.getRuleGraph().getNode(c1.forAll[i]).getName();
                if(nam1 != null && nam2 != null && nam1.length() != 0 && nam2.length() != 0) {
                    return canMerge(
                            s_checker.cache[0][t_box.getRuleGraph().getConceptIndex(nam1)], 
                            s_checker.cache[0][t_box.getRuleGraph().getConceptIndex(nam2)]);
                }*/
                return false;
            } else return false;
        }
        return true;
    }
    
    /**
     * Метод реализует алгоритм проверки того, может ли существовать индивид представляющий конъюнкцию двух концептов.
     * В методе реализуются следующие правила:
     * если кэши содержат концепт и его отрицание, они не могут быть слиты;
     * если в одном из кэшей есть ссылка на индивид, то они не могут быть слиты;
     * если в кэшах есть несколько SOME и FORALL, имеющие под квантором
     * одни и те же роли, то такие концепты не могут быть слиты;
     * если в кэшах по-одному есть SOME и FORALL, имеющие под квантором
     * одни и те же роли, то необходимо проверть кеш концептов под квантором;
     * если в кэшах есть SOME и MAXCARD, имеющие под квантором
     * одни и те же роли, то такие концепты не могут быть слиты;
     * @return Возвращает истина, если конъюнкция может быть согласованной и ложь в противном случае.
     */
    private boolean canMerge(Cache c1, Cache c2) {
        if(c1.hasIndivid && c2.hasIndivid) return false;
        for(int i = 0; i < c1.getSize(); i++) {
            if(c2.contains(-c1.getCache()[i])) {
                return false;
            }
        }
        for(int i = 0; i < r_box.getRoleSize(); i++) {
            
            Integer[] allSSRoles = new Integer[0];
            allSSRoles = r_box.getRoleByIndex(i).getSuperRoles().toArray(allSSRoles);
            
            for(int j: allSSRoles) {
                if(!checkTwoRoles(c1, c2, i, j)) return false;
            }

            allSSRoles = new Integer[0];
            allSSRoles = r_box.getRoleByIndex(i).getSuperRoles().toArray(allSSRoles);
            for(int j: allSSRoles) {
                if(!checkTwoRoles(c1, c2, i, j)) return false;
            }

            allSSRoles = new Integer[0];
            allSSRoles = r_box.getRoleByIndex(i).getEqvRoles().toArray(allSSRoles);
            for(int j: allSSRoles) {
                if(!checkTwoRoles(c1, c2, i, j)) return false;
            }
            if(!checkTwoRoles(c1, c2, i, i)) return false;
        }
        return true;
    }
    
    /**
     * В методе осуществляется проверка включаемости двух заданных концептов, по их именам.
     * @param ps Имя первого концепта.
     * @param qs Имя второго концепта.
     * @return Возвращает истина, если концепты включается и ложь в противном случае.
     */
    public boolean checkSubsumption(int p_ind, int q_ind)
    {
        //System.out.println(p_ind + " [= " + q_ind);
        /*int p_ind = t_box.getRuleGraph().getConceptID(ps);
        int q_ind = t_box.getRuleGraph().getConceptID(qs);*/

        int p = t_box.getRuleGraph().getConceptInRuleGraph(p_ind);
        int q = t_box.getRuleGraph().getConceptInRuleGraph(q_ind);
        
        if(s_checker.cache[0][p_ind] != null) if(s_checker.cache[0][p_ind].getSize() == -1) return true;
        if(s_checker.cache[1][q_ind] != null) if(s_checker.cache[1][q_ind].getSize() == -1) return true;

        if(s_checker.cache[0][p_ind] != null && s_checker.cache[1][q_ind] != null)
        {
            if(use_caching && canMerge(s_checker.cache[0][p_ind], s_checker.cache[1][q_ind]))
                return false;
        }

        if(no_sub_sum != null) if(no_sub_sum[p_ind] != null) if(no_sub_sum[p_ind].contain(q_ind)) return false;

        boolean ABL = true;
        /*if(use_a_checker) {
            s_checker.getAChecker().total_count++;
            ABL = s_checker.getAChecker().isDisjoint(p, -q, null);
            if(ABL) {
                s_checker.getAChecker().true_count++;
                return true;
            }
        }*/
        boolean res = s_checker.checkSubsumption(p, q);
        if(!res) {
            if(no_sub_sum != null) if(no_sub_sum[p_ind] != null) no_sub_sum[p_ind].add(q_ind);
            //s_checker.cache[0][p_ind].canMerge(s_checker.cache[1][q_ind], r_box, t_box, s_checker.cache);
        } else {
            if(!ABL) { //Метод isDisjoint класса AChecker не сработал но тем не менее концепты являются вложенными
                int kor = 123; //
            }
        }
        return res;
    }
    
    public void showTemp() {
        s_checker.showTemp();
    }

    /**
     * Метод реализует фазу поиска сверху в методе перекрестного построения классификации.
     * @param c Заданный надконцепт.
     * @param x Заданный подконцепт.
     * @return Возвращает массив концептов, которые являются подконцептами заданного подконцепта.
     */
    private IntArray traversalTopSearch(int c, int x)
    {
        visited[x] = true;
        IntArray ret = new IntArray();
        IntArray succ = new IntArray();
        //it is known that c [= x
        //check x [= c
        if(x != t_box.getRuleGraph().getConceptsSize() + 1)
            if(checkSubsumption(x, c)) {
                eq[x].add(c); eq[c].add(x); //Concepts are equal!
                return new IntArray();
            }
        
        for(int i = 0; i < sup[x].size(); i++) {
            if(sup[x].get(i) == t_box.getRuleGraph().getConceptsSize() + 2) continue;
            if(simpleTopSubs(sup[x].get(i), c)) { //проверяю является ли какой-нибудь из подконцептов X надконцептом C
                succ.addOnce(sup[x].get(i)); //формирую список всех надконцептов концепта C на текущем уровне
            }
        }
        if(succ.size() == 0) {
            ret.add(x);
            return ret;
        } else {
            for(int i = 0; i < succ.size(); i++) {
                if(!visited[succ.get(i)]) {
                    IntArray res = traversalTopSearch(c, succ.get(i));
                    ret.addOnce(res);
                }
            }
            if(ret.size() == 0) ret.add(x);
            return ret; //все надконцепты концепта C на всех уровнях
        }
    }
    
    /**
     * Метод реализует фазу поиска снизу в методе перекрестного построения классификации.
     * @param c Заданный надконцепт.
     * @param x Заданный подконцепт.
     * @return Возвращает массив концептов, которые являются надконцептами заданного подконцепта.
     */
    private IntArray traversalBottomSearch(int c, int x) {
        visited[x] = true;
        IntArray ret = new IntArray();
        IntArray pred = new IntArray();
        //it is known that x [= c
        //check c [= x
        if(x != t_box.getRuleGraph().getConceptsSize() + 2)
            if(checkSubsumption(c, x)) {
                eq[x].add(c); eq[c].add(x); //Concepts are equal!
                return sup[x];
            }
        for(int i = 0; i < sub[x].size(); i++) {
            if(sub[x].get(i) == t_box.getRuleGraph().getConceptsSize() + 1) continue;
            if(simpleBottomSubs(sub[x].get(i), c)) {
                pred.addOnce(sub[x].get(i));
            }
        }
        if(pred.size() == 0) {
            ret.add(x);
            return ret;
        } else {
            for(int i = 0; i < pred.size(); i++) {
                if(!visited[pred.get(i)]) {
                    ret.add(traversalBottomSearch(c, pred.get(i)));
                }
            }
            return ret;
        }
    }
    
    
    private void deleteSub(int x, int y) {
        sup[x].delete(y);
        sub[y].delete(x);
    }
    
    /**
     * Метод реализует алгоритм перекрестного построения классификации.
     * Franz Baader, Bernhard Hollunder, Bernhard Nebel, Hans-Jurgen Profitlich.
     * An empirical analysis of optimization techniques for terminological representation systems.
     * @param beg_time Время начала запуска данной процедуры.
     * @param timelimit Заданный предел времени работы системы.
     * @return Возвращает истина, если метод классификации выполняется за установленный предел времени и ложь в противном случае.
     */
    private boolean traversalClassification(long beg_time, long timelimit) {
        //need to load all ABox to queue and reason it, then make traversal classification
        if(a_box_reuse)
            s_checker.checkABoxSat(true);

        //sub[IT] contains all super concepts of IT
        //sup[IT] contains all sub concepts of IT
        int N = t_box.getOrder().size(); //count of all concepts
        visited = new boolean[N + 4];
        mark = new int[N + 4];
        
        //N + 1 is TOP concept
        //N + 2 is bottom concept
        sup[N + 1] = new IntArray();
        sup[N + 2] = new IntArray();
        sub[N + 1] = new IntArray();
        sub[N + 2] = new IntArray();

        for(int i = 0; i < N; i++) {
            //TOP is super concept for all concepts
            if(sup[i] == null) sup[i] = new IntArray();
            if(sub[i] == null) sub[i] = new IntArray();
            
            sup[N + 1].add(i);
            sub[i].add(N + 1);
            
            //BOTTOM is sub concept for all concepts
            sub[N + 2].add(i);
            sup[i].add(N + 2);
        }
        
        for(int i1 = N - 1; i1 >= 0; i1--) {
            int i = t_box.getOrder().get(i1);
            if(must_show) System.out.println(i + " " + t_box.getRuleGraph().getConcepts()[i]);
            if(System.currentTimeMillis() - beg_time > timelimit) {
                return false;
            }
            //there is taxonomy of all first i concepts
            //add concept with number i to taxonomy
            //Top Search Phase
            Arrays.fill(visited, false);
            Arrays.fill(mark, 0);
            IntArray res = new IntArray();
            for(int j = 0; j < sub[i].size(); j++) {
                IntArray res1 = traversalTopSearch(i, sub[i].get(j));
                res.addOnce(res1);
            }
            //res contains all super concepts for i-concept

            //Delete concept #i from all subconcepts of all straight superconcepts 
            for(int j = 0; j < sub[i].size(); j++) {
                sup[sub[i].get(j)].delete(i);
            }
            sub[i].clear();
            
            for(int j = 0; j < res.size(); j++) {
                sub[i].addOnce(res.get(j));
                sup[res.get(j)].addOnce(i);

                for(int k = 0; k < sup[i].size(); k++) {
                    deleteSub(res.get(j), sup[i].get(k));
                }
            }
            //Bottom Search Phase
            //Arrays.fill(visited, false);
            //Arrays.fill(mark, 0);

            /*res = new IntArray();
            for(int j = 0; j < sup[i].size(); j++) {
                IntArray res1 = traversalBottomSearch(i, sup[i].get(j));
                res.addOnce(res1);
            }

            for(int j = 0; j < sup[i].size(); j++) {
                sub[sup[i].get(j)].delete(i);
            }
            sup[i].clear();
            
            for(int j = 0; j < res.size(); j++) {
                sup[i].addOnce(res.get(j));
                sub[res.get(j)].addOnce(i);

                //for(int k = 0; k < sup[i].size(); k++) {
                //    deleteSub(res.get(j), sup[i].get(k));
                //}
            }*/
            
            deleteUnsign(i);
        }
        return true;
    }
    
    /**
     * Метод осуществляет классификацию концептов TBox.
     * @param showModel Определяет нужно ли показывать данные для отладки.
     * @param filename Определяет имя файла для вывода аксиом.
     * @param timelimit Определяет предел времени работы системы.
     * @param detailedOut Определяет нужно ли выводить аксиомы эквивалентности.
     * @return Возвращает множество аксиом включаемости концептов.
     */
    public Set<OWLSubClassOfAxiom> classifyTBox(boolean showModel, IRI filename, long timelimit, boolean detailedOut, boolean experimentAttr) {
        concept_count = t_box.getRuleGraph().getConceptsSize();
        sub = new IntArray[concept_count + 4];
        sup = new IntArray[concept_count + 4];
        //check sat of each concept and its negation
        long beg_time = System.currentTimeMillis();
        
        no_sub_sum = new HashContainer[concept_count + 1];
        s_checker.no_sub_sum = no_sub_sum;
        eq = new IntArray[concept_count + 4];
        for(int i = 0; i < eq.length; i++)
            eq[i] = new IntArray();
        
        for(int i = 0; i < concept_count; i++) {
            no_sub_sum[i] = new HashContainer();
        }
        
        s_checker.getAChecker().true_count = 0;
        s_checker.getAChecker().total_count = 0;
        for(int i1 = 0; i1 < concept_count; i1++) {
            int i = t_box.getOrder().get(i1);
            if(must_show) System.out.println("#" + i1 + " " + t_box.getRuleGraph().getConcepts()[i]);
            boolean pos_res = s_checker.checkSat(i, showModel, 0, i);
            if(!pos_res) {
                eq[i].add(t_box.getRuleGraph().getConceptsSize() + 2);
                eq[t_box.getRuleGraph().getConceptsSize() + 2].add(i);
            }
            boolean neg_res = s_checker.checkSat(i, showModel, 1, i);
            if(!neg_res) {
                eq[i].add(t_box.getRuleGraph().getConceptsSize() + 1);
                eq[t_box.getRuleGraph().getConceptsSize() + 1].add(i);
                //equal_to_thing.add(i);
            }
        }

        if(System.currentTimeMillis() - beg_time > timelimit) //Time Limit Exceeded
            return null;
        
        //if(true) {
        //    return null;
        //}
        
        s_checker.getAChecker().true_count = 0;
        s_checker.getAChecker().total_count = 0;
        
        s_checker.experimentAttr = experimentAttr;
        
        if(!traversalClassification(beg_time, timelimit)) {
            return null;
        }
        //System.out.println("CLASSIFICATION TIME: " + (System.currentTimeMillis() - beg_time));
        
        f = new int[t_box.getRuleGraph().getConceptsSize() + 4];
        for(int i = 0; i < t_box.getRuleGraph().getConceptsSize(); i++) {
            if(i == t_box.getRuleGraph().getConceptsSize()) continue;
            deleteUnsign(i);
        }
        HashSet<OWLSubClassOfAxiom> tax = new HashSet<OWLSubClassOfAxiom>();
        HashSet<OWLEquivalentClassesAxiom> eqx = new HashSet<OWLEquivalentClassesAxiom>();
        df = OWLManager.getOWLDataFactory();
        
        /*for(int j = 0; j < equal_to_thing.size(); j++) {
            OWLClass clsSUP = null;
            clsSUP = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts().get(equal_to_thing.get(j))));
            for(int i = 0; i < t_box.getRuleGraph().getConceptsSize(); i++) {
                if(equal_to_thing.contains(i)) continue;
                OWLClass clsSUB = null;
                clsSUB = df.getOWLClass(IRI.create(t_box.getRuleGraph().getConcepts().get(i)));
                OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(clsSUB, clsSUP);
                tax.add(ax);
            }
        }*/

        printTaxonomy(t_box.getRuleGraph().getConceptsSize() + 1, "", tax, detailedOut); //out sub classes
        ArrayList<Integer> all = new ArrayList<Integer>();
        HashSet<OWLClass> eqi = new HashSet<OWLClass>();
        if(detailedOut) {
            Arrays.fill(visited, false);
            for(int i = 0; i < concept_count; i++) {
                if(!visited[i]) {
                    all.clear();
                    printEquals(i, all);
                    eqi.clear();
                    for(int j = 0; j < all.size(); j++) {
                        for(int k = j + 1; k < all.size(); k++) {
                            addEquivalenceAxiom(all.get(j), all.get(k), eqi);
                        }
                    }

                    eqx.add(df.getOWLEquivalentClassesAxiom(eqi));
                }
            }
        } //out equal classes
        
        if(filename != null) {
            manager = OWLManager.createOWLOntologyManager();
            try {
                Ontology = manager.createOntology(filename);
            }
            catch(OWLOntologyCreationException e) {
                System.err.println(e.getMessage());
            }
            for(OWLSubClassOfAxiom sca: tax) {
                AddAxiom addAxiom = new AddAxiom(Ontology, sca);
                manager.applyChange(addAxiom);
            }
            if(detailedOut) {
                for(OWLEquivalentClassesAxiom sca: eqx) {
                    AddAxiom addAxiom = new AddAxiom(Ontology, sca);
                    manager.applyChange(addAxiom);
                }
            }
            try {
                manager.saveOntology(Ontology);
            } catch (OWLOntologyStorageException e) {
                System.err.println(e.getMessage());
            }
        }
        return tax;
    }
    
    /**
     * В методе определяется является ли выполнимым заданный концепт.
     * @param concept_name Имя концепта.
     * @return Возвращает истина, если концепт является согласованным и ложь в противном случае.
     */
    public boolean checkSat(String concept_name) {
        return s_checker.checkSat(t_box.getRuleGraph().getConceptIndex(concept_name), false, 2, 0);
    }
    
    /**
     * В методе определяется согласованность ABox базы знаний.
     * @return Возвращает истина, если ABox является согласованным и ложь в противном случае.
     */
    public boolean checkABoxSat() {
        return s_checker.checkABoxSat(true);
    }
    
    /**
     * В методе определяется согласованность концептов TBox базы знаний.
     */
    public void checkALCTBoxSat() {
        s_checker.checkALCTBoxSat(false, true);
    }
    
    /**
     * В методе выполняется обработка запроса базы знаний.
     */
    public void checkQuery(Query q) {
        s_checker.checkQuery(q);
    }
    
}