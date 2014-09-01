package TReasonerFactory;

import KnowledgeBase.ABox;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Класс позволяет загружать базы знаний в формате ALC (в спецификации системы KRIS).
 * Такие базы знаний содрежат только TBox, при чем в одном файле может быть не более 
 * одного TBox в котором описываются до 21 концепта, и для них выполняется только задача проверки
 * согласованности концептов.
 * @author Andrey Grigoryev
 */
class ALCConnector {
    
    private TBox t_box;
    private RBox r_box;
    private ABox a_box;
    
    /**
     * В конструктор происходит считывание всего текста из файла, и добавление в базу знаний концептов.
     * @param file_name Файл из которого считывается база знаний.
     * @param how_many Определяет количество концептов, которое необходимо считать.
     */
    public ALCConnector(String file_name, int how_many) {
        a_box = new ABox();
        r_box = new RBox();
        t_box = new TBox(r_box, a_box);
        
        t_box.setRBox(r_box);
        int concept_count = 0;
        String concept_string = "";
        
        Scanner S = null;
        try {
            S = new Scanner(new File(file_name));
        }
        catch(FileNotFoundException e) {
            System.out.println("File " + file_name + " not found on ALC loading");
        }
        System.out.print("LOADING FILES: ");
        while(S.hasNextLine())
        {
            String s = S.nextLine();
            if(s.charAt(0) == ';') {
                if(concept_string.length() == 0) continue;
                System.out.print(concept_count + 1);
                System.out.print(" ");
                
                int sub = t_box.getRuleGraph().addExpr2Graph("MyConcept" + (concept_count + 1));
                int sup = t_box.getRuleGraph().addExpr2Graph(concept_string);
                t_box.addEquivalenceAxiom(sub, sup);
                
                concept_string = "";
                concept_count++;
                if(how_many > 0)
                    if(concept_count == how_many) break;
                continue;
            }
            
            for(int i = 0; i < s.length(); i++) {
                concept_string += s.charAt(i);
            }
        }
        
        if(concept_string.length() > 0) {
            System.out.println(concept_count + 1);
            int sub = t_box.getRuleGraph().addExpr2Graph("MyConcept" + (concept_count + 1));
            int sup = t_box.getRuleGraph().addExpr2Graph(concept_string);
            t_box.addEquivalenceAxiom(sub, sup);
            concept_string = "";
            concept_count++;
        } else {
            System.out.println();
        }
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
        
}
