package TReasonerFactory;

import KnowledgeBase.ABox;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Класс позволяет загружать базы знаний в формате TKB (в спецификации системы KRIS).
 * Такие базы знаний содрежат только TBox, при чем в одном файле может быть более 
 * одного TBox, и для них выполняются задачи проверки согласованности и классификации 
 * концептов.
 * @author Andrey Grigoryev
 */
class TKBConnector {
    
    private int N = 0;
    private char[] a = new char[1024 * 1024 * 8];
    
    private TBox[] t_boxes = new TBox[32];
    private RBox[] r_boxes = new RBox[32];
    private ABox[] a_boxes = new ABox[32];
    
    ABox a_box = null;
    RBox r_box = null;
    TBox t_box = null;
    
    int tBoxCount = 0;

    private char[] buf = new char[1024 * 1024 * 8];
    
    /**
     * Возвращает истина если входной символ равен пробелу, переводу строк или табуляции.
     * @param ch Исходный символ.
     * @return Результат метода.
     */
    static public boolean isSkippedSymbol(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\t';
    }

    /**
     * Основной конструктор класса, который считывает все базы знаний из заданного файла.
     * @param file_name Имя файла из которого считываются базы знаний.
     */
    public TKBConnector(String file_name) {
        Reader R = null;
        try {
            R = new FileReader(file_name);
            R.read(buf);
        }
        catch(IOException e) {
            System.out.println("File " + file_name + " not found on TKB loading");
        }
        
        int C = 0;
        while(buf[C] != 0) C++;
        
        boolean hasOneTBox = false;
        boolean isComment = false;
        int skcnt = 0;
        for(int i = 0; i < C; i ++) {
            if(buf[i] == ';') {
                isComment = true;
                continue;
            }
            if(buf[i] == '\n') isComment = false;
            if(isComment) continue;
            if(buf[i] == '(') skcnt++;
            if(skcnt == 2) {
                hasOneTBox = false; break;
            }
            if(isSkippedSymbol(buf[i]) || buf[i] == '(') continue;
            hasOneTBox = true; break;
        }
        
        if(hasOneTBox) {
            buf[C] = ')'; C++;
            for(int i = C; i > 0; i--) {
                buf[i] = buf[i - 1];
            }
            buf[0] = '(';
            C++;
        }
        
        int l = 0;
        while(buf[l] != '(') l++;
        
        int balance = 0;
        for(int i = l; i < C; i++) {
            if(buf[i] == '(') balance++;
            if(buf[i] == ')') balance--;
            a[N++] = buf[i];
            if(balance == 0 && N > 0) {
                a_box = new ABox();
                r_box = new RBox();
                t_box = new TBox(r_box, a_box);
                processAxioms();
                t_boxes[tBoxCount] = t_box;
                a_boxes[tBoxCount] = a_box;
                r_boxes[tBoxCount] = r_box;
                tBoxCount++;
                N = 0;
                while(buf[i] != '(')
                {
                    i++; if(i >= C) break;
                } 
                i--;
            }
        }
    }
    
    /**
     * Возвращает массив все TBox обработанных баз знаний.
     * @return TBox баз знаний.
     */
    public TBox[] getTBoxes()
    {
        return t_boxes;
    }
    
    /**
     * Возвращает массив все RBox обработанных баз знаний.
     * @return RBox баз знаний.
     */
    public RBox[] getRBoxes()
    {
        return r_boxes;
    }
    
    /**
     * Возвращает массив все ABox обработанных баз знаний.
     * @return ABox баз знаний.
     */
    public ABox[] getABoxes()
    {
        return a_boxes;
    }
    
    /**
     * Возвращает количество баз знаний, которые были считаны из файла.
     * @return Количество баз знаний.
     */
    public int getTBoxesCount()
    {
        return tBoxCount;
    }
    
    /**
     * Данный метод производит обработку базы знаний, выделяя в ней строки отвечающие за аксимы.
     */
    public void processAxioms()
    {
        a_box = new ABox();
        r_box = new RBox();
        t_box = new TBox(r_box, a_box);
        t_box.setRBox(r_box);
        
        int i = 0;
        while(a[i] != '(') i++; i++;
        while(a[N - 1] != ')' && N > 0) N--; N--;

        for(; i < N; i++) {
            int l = i;
            while((a[l] != '(') && l < N) l++;
            int r = l + 1;
            int balance = 1;
            while(balance > 0 && r < N) {
                if(a[r] == ')') balance--;
                if(a[r] == '(') balance++;
                r++;
            }
            processString(l + 1, r - 1);
            i = r;
        }
    }
    
    /**
     * Обрабатывает входной буфер с левого по правый заданные символы, при этом определяется 
     * аксиома, которую необходимо добавить в базу знаний.
     * @param l Номер символа с которого начинается обработки строки
     * @param r Номер символа на котором останавливается обработка строки.
     */
    public void processString(int l, int r)
    {
        String token = "";
        for(int i = l; i < r; i++) {
            if(a[i] == ' ') {
                l = i + 1;
                break;
            } else {
                token += a[i];
            }
            l = i + 1;
        }
        
        String concept_name = "";
        while(a[l] == ' ') l++;
        
        for(int i = l; i < r; i++) {
            if(a[i] == ' ') {
                l = i + 1;
                break;
            } else {
                concept_name += a[i];
            }
            l = i + 1;
        }
        
        String desc = "";
        for(int i = l; i < r; i++)
            desc += a[i];
        
        if(token.equalsIgnoreCase("define-concept")) { //equivalence axiom
            if(desc.length() > 0) {
                int sub = t_box.getRuleGraph().addExpr2Graph(concept_name);
                int sup = t_box.getRuleGraph().addExpr2Graph(desc);
                //t_box.addEquivalenceAxiom(sub, sup);
                t_box.addGCI(sub, sup);
            } else {
                t_box.getRuleGraph().findConcept(concept_name);
            }
        } else
        if(token.equalsIgnoreCase("define-primitive-concept")) { //general inclusion axiom
            if(desc.length() > 0) {
                int sub = t_box.getRuleGraph().addExpr2Graph(concept_name);
                int sup = t_box.getRuleGraph().addExpr2Graph(desc);
                t_box.addGCI(sub, sup);
            } else {
                t_box.getRuleGraph().findConcept(concept_name);
            }
        } else
        if(token.equalsIgnoreCase("define-disjoint-primitive-concept")) { //disjointness axiom
            if(desc.length() > 0) {
                int sub = t_box.getRuleGraph().addExpr2Graph(concept_name);
                int bal = 0, it;
                for(it = 0; it < desc.length(); it++)
                    if(!isSkippedSymbol(desc.charAt(it))) break;

                for(; it < desc.length(); it++) {
                    if(desc.charAt(it) == '(') bal++;
                    if(desc.charAt(it) == ')') bal--;
                    if(bal == 0) break;
                }
                desc = desc.substring(it + 1, desc.length());
                int sup = t_box.getRuleGraph().addExpr2Graph(desc);
                t_box.addGCI(sub, sup);
            } else {
                t_box.getRuleGraph().findConcept(concept_name);
            }
        } else
        if(token.equalsIgnoreCase("define-primitive-role")) {
            r_box.findRole(concept_name);
        } else
        if(token.equalsIgnoreCase("define-role")) {
            r_box.findRole(concept_name);
        }
    }
    
}