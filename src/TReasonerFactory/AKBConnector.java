package TReasonerFactory;

import KnowledgeBase.Query;
import KnowledgeBase.ABox;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Класс позволяет загружать базы знаний в формате AKB (в спецификации системы KRIS).
 * Такие базы знаний содрежат TBox и ABox, при чем в одном файле может быть не более 
 * одной базы знаний, и для них выполняется только задача выполнения запроса: принадлжености индивида концепту.
 * @author Andrey Grigoryev
 */
class AKBConnector {
    
    private TBox t_box;
    private ABox a_box;
    private RBox r_box;
    private Query q;
    
    /**
     * Осуществляет обработку строки, которая представляет собой TBox, ABox или запрос.
     * @param s Строка для обработки.
     * @param mode Mode = 0 если обрабатывается TBox, 1 - ABox, 2 - запрос.
     */
    public void parse(String s, int mode)
    {
        if(s.length() < 1) return;
        int l = 0;
        int r = s.length();
        while(s.charAt(l) != '(') l++; l++;
        while(s.charAt(r - 1) != ')') r--; r--;
        
        int balance = 0;
        String sent = "";
        for(int i = l; i < r; i++) {
            if(s.charAt(i) == '(') balance++;
            if(s.charAt(i) == ')') balance--;
            sent += s.charAt(i);
            if(balance == 0 && sent.length() > 1) {
                int l1 = 0;
                int r1 = sent.length();
                while(sent.charAt(l1) != '(') l1++; l1++;
                while(sent.charAt(r1 - 1) != ')') r1--; r1--;
                for(int j = l1; j < r1; j++) {
                    String token = "", token1 = "", token2 = "";
                    while(sent.charAt(j) != ' ' && j < r1) {token += sent.charAt(j); j++;} j++;
                    while(sent.charAt(j) != ' ' && j < r1) {token1 += sent.charAt(j); j++;} j++;
                    
                    if(mode == 0) { //t_box
                        if(token.equalsIgnoreCase("DEFINE-PRIMITIVE-ROLE")) {
                            r_box.findRole(token1);
                        }
                        if(token.equalsIgnoreCase("DEFINE-PRIMITIVE-CONCEPT")) {
                            t_box.getRuleGraph().findConcept(token1);
                        }
                        if(token.equalsIgnoreCase("DEFINE-CONCEPT")) {
                            for(; j < r1; j++) token2 += sent.charAt(j);
                            int sub = t_box.getRuleGraph().addExpr2Graph(token1);
                            int sup = t_box.getRuleGraph().addExpr2Graph(token2);
                            t_box.addEquivalenceAxiom(sub, sup);
                        }
                    } else
                    if(mode == 1) { //a_box
                        if(token.equalsIgnoreCase("instance")) {
                            for(; j < r1; j++) token2 += sent.charAt(j);
                            int concept = t_box.getRuleGraph().addExpr2Graph(token2);
                            
                            int oldSize = t_box.getRuleGraph().getNodesCount();
                            /*int i1 = t_box.getRuleGraph().findIndivid(token1);
                            t_box.getRuleGraph().getNode(i1).setIndividNumber(a_box.getCount());
                            a_box.add(a_box.getCount(), concept);
                            a_box.addIndivid();*/
                        }
                        if(token.equalsIgnoreCase("related")) {
                            while(sent.charAt(j) != ' ') {token2 += sent.charAt(j); j++;} j++;
                            String token3 = "";
                            for(; j < r1; j++) token3 += sent.charAt(j);

                            /*int i1 = t_box.getRuleGraph().findIndivid(token1);
                            t_box.getRuleGraph().getNode(i1).setIndividNumber(a_box.getCount());
                            i1 = a_box.getCount();
                            a_box.addIndivid();

                            int i2 = t_box.getRuleGraph().findIndivid(token2);
                            t_box.getRuleGraph().getNode(i2).setIndividNumber(a_box.getCount());
                            i2 = a_box.getCount();
                            a_box.addIndivid();*/
                            
                            int role = r_box.findRole(token3);
                            //a_box.addRelation(individ1, individ2, role);
                        }
                    } else
                    if(mode == 2) { //query
                        if(token.equalsIgnoreCase("individual-instance?")) {
                            for(; j < r1; j++) token2 += sent.charAt(j);
                            //int individ = a_box.find(token1);
                            int concept = t_box.getRuleGraph().addExpr2Graph(token2);
                            //q.addIndividualInstance(individ, concept);
                        }
                    }
                }
                sent = "";
            }
        }
    }
    
    /**
     * Конструктор класса, который считывает из файла TBox, RBox и ABox онтологии
     * @param file_name Файл из которого считываются онтологии.
     */
    public AKBConnector(String file_name) {
        a_box = new ABox();
        r_box = new RBox();
        t_box = new TBox(r_box, a_box);
        q = new Query();
        Scanner S = null;
        
        try {
            S = new Scanner(new File(file_name));
        }
        catch(FileNotFoundException e) {
            System.out.println("File " + file_name + " not found on AKB loading");
        }
        String full_string = "";
        while(S.hasNextLine()) {
            String str = S.nextLine();
            if(str.length() == 0) continue;
            if(str.charAt(0) == ';') continue;
            full_string = full_string + "\n" + str;
        }
        int l = 0;
        while(full_string.charAt(l) != '(') l++; l++;
        
        int balance = 0;
        int r;
        while(full_string.charAt(l) != '(') l++;
        String t_box_string = "";
        for(r = l; r < full_string.length(); r++) {
            if(full_string.charAt(r) == '(') balance++;
            if(full_string.charAt(r) == ')') balance--;
            t_box_string += full_string.charAt(r);
            if(balance == 0) break;
        }
        l = r + 1;
        parse(t_box_string, 0); //t_box_string is t_box;
        
        balance = 0;
        while(full_string.charAt(l) != '(') l++;
        String a_box_string = "";
        for(r = l; r < full_string.length(); r++) {
            if(full_string.charAt(r) == '(') balance++;
            if(full_string.charAt(r) == ')') balance--;
            a_box_string += full_string.charAt(r);
            if(balance == 0) break;
        }
        l = r + 1;
        parse(a_box_string, 1); //a_box_string is t_box;
        
        String query_string = "";
        balance = 0;
        for(int i = r + 1; i < full_string.length(); i++) {
            if(full_string.charAt(i) == '(') balance++;
            if(full_string.charAt(i) == ')') balance--;
            if(balance < 0) break;
            query_string += full_string.charAt(i);
        }
        parse("(" + query_string + ")", 2); //query_string is Query;
    }
    
    /**
     * Возвращает TBox загруженной онтологии.
     * @return TBox текущей онтологии.
     */
    public TBox getTBox() {
        return t_box;
    }
    
    /**
     * Возвращает RBox загруженной онтологии.
     * @return RBox текущей онтологии.
     */
    public RBox getRBox() {
        return r_box;
    }
    
    /**
     * Возвращает ABox загруженной онтологии.
     * @return ABox текущей онтологии.
     */
    public ABox getABox() {
        return a_box;
    }
    
    /**
     * Возвращает запрос загруженной онтологии.
     * @return Запрос текущей онтологии.
     */
    public Query getQuery() {
        return q;
    }
    
}
