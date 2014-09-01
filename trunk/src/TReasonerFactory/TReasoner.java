package TReasonerFactory;

import KnowledgeBase.ABox;
import KnowledgeBase.Query;
import KnowledgeBase.RBox;
import KnowledgeBase.TBox;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * Основной класс программы, предоставляющий доступ ко всем функциям системы логического анализа.
 * @author Andrey Grigoryev
 */
public class TReasoner {

    Classificator engine = null;
    
    private TBox t_box = null;
    private RBox r_box = null;
    private ABox a_box = null;
    private Query query = null;
    
    private TBox[] t_boxes = null;
    private RBox[] r_boxes = null;
    private ABox[] a_boxes = null;
    private int tBoxesCount = 0;
    
    private long timeLimit = 0;
    
    public int count = 0;
    
    /**
     * В методе осуществляется загрузка базы знаний из файла с расширением .AKB, в
     * котором задается база знаний в спецификации DL98 для проверки её согласованности.
     * @param file_name Путь к файлу онтологии.
     */
    private void loadFromAKB(String file_name) {
        t_box = null; a_box = null; r_box = null;
        AKBConnector akb_connect = new AKBConnector(file_name);
        t_box = akb_connect.getTBox();
        r_box = akb_connect.getRBox();
        a_box = akb_connect.getABox();
        query = akb_connect.getQuery();
        t_box.preProcess();
    }
    
    /**
     * В методе осуществляется загрузка базы знаний из файла с расширением .TKB, в
     * котором задается база знаний в спецификации DL98 для её классификации.
     * @param file_name Путь к файлу онтологии.
     */
    private void loadFromTKB(String file_name) {
        t_box = null; a_box = null; r_box = null;
        TKBConnector tkb_connect = new TKBConnector(file_name);
        t_boxes = tkb_connect.getTBoxes();
        r_boxes = tkb_connect.getRBoxes();
        a_boxes = tkb_connect.getABoxes();
        tBoxesCount = tkb_connect.getTBoxesCount();
        for(int i = 0; i < tBoxesCount; i++) {
            t_boxes[i].preProcess();
        }
    }

    /**
     * В методе осуществляется загрузка базы знаний из файла с расширением .ALC, в
     * котором задается база знаний в спецификации DL98 для проверки выполнимости всех её концептов.
     * @param file_name Путь к файлу онтологии.
     */
    private void loadFromALC(String file_name, int how_many) {
        t_box = null; a_box = null; r_box = null;
        ALCConnector alc_connect = new ALCConnector(file_name, how_many);
        t_box = alc_connect.getTBox();
        r_box = alc_connect.getRBox();
        a_box = alc_connect.getABox();
        t_box.preProcess();
    }
    
    /**
     * В методе осуществляется загрузка базы знаний из файла с расширением .OWL, в
     * котором задается база знаний в спецификации W3C.
     * @param file_name Путь к файлу онтологии.
     */
    private void loadFromOWL(String file_name) {
        t_box = null; a_box = null; r_box = null;
        OWLConnector owl_connect = new OWLConnector(file_name, 1);
        t_box = owl_connect.getTBox();
        r_box = owl_connect.getRBox();
        a_box = owl_connect.getABox();
        t_box.preProcess();
        String logStr = owl_connect.getLogicString() + "" + t_box.getRuleGraph().getLogicString();
        count = t_box.getRuleGraph().getConceptsSize();
        //System.out.println("CONCEPTS: " + t_box.getRuleGraph().getConceptsSize() + " \nROLES: " + r_box.getRoleSize() + " \nINDIVIDS: " + a_box.getCount());
        //System.out.println(t_box.getRuleGraph().getConceptsSize() + "\t" + r_box.getRoleSize() + "\t" + a_box.getCount());
        //System.out.println(t_box.getRuleGraph().getConceptsSize() + "\t" + r_box.getRoleSize() + "\t" + a_box.getCount());
        //if(logStr.contains("N") || logStr.contains("Q")) {
        //    System.out.println(file_name);
        //}
    }

    /**
     * Метод осуществляет загрузку базы знаний для файлов, описанных в спецификации DL98.
     * @param path Путь к файлу с базой знаний.
     * @param kbType Тип базы знаний.
     * @param howMany Параметр определяющий количество баз знаний, которое необходимо считать из файла (для ALC файлов).
     * @param uA Определяет необходимо ли использовать алгоритм семантико-синтаксического выбора.
     * @param uB Определяет необходимо ли использовать backJumping.
     * @param uC Определяет необходимо ли использовать кэширование.
     * @param uS Определяет необходимо ли показывать логи.
     * @param uG Определяет необходимо ли использовать глобальное кэширование.
     * @param tL Определяет лимит времени.
     */
    public void loadKB(String path, String kbType, int howMany, boolean uA, boolean uB, boolean uC, boolean uS, boolean uG, long tL) {
        timeLimit = tL;
        if(engine == null)
            engine = new Classificator(r_box, t_box, a_box, uA, uB, uC, uG, uS, tL);
        else {
            engine.clear();
        }
        if(kbType.equals("ALC")) {
            loadFromALC(path, howMany);
            engine.setABox(a_box);
            engine.setTBox(t_box);
            engine.setRBox(r_box);
        }
        if(kbType.equals("TKB")) {
            loadFromTKB(path);
        }
        if(kbType.equals("AKB")) {
            loadFromAKB(path);
            engine.setABox(a_box);
            engine.setTBox(t_box);
            engine.setRBox(r_box);
        }
    }

    /**
     * В методе осуществляется проверка согласованности концептов. 
     * Каждый концептов должен быть согласован или не согласован в соответствии с заданным
     * параметром.
     * @param cohr Параметр, определяющий должны ли быть согласованы концепты. 
     */
    public void classifyTBoxes(boolean cohr) {
        int cur_tb = 0;
        int real_size = cur_tb + 1;
        real_size = 21;
        for(int i = cur_tb; (i < tBoxesCount) && (i < real_size); i++) {
            long time1 = System.currentTimeMillis();
            engine.clear();
            engine.setABox(a_boxes[i]);
            engine.setTBox(t_boxes[i]);
            engine.setRBox(r_boxes[i]);
            if(tBoxesCount > 1) {
                System.out.print("TBOX #" + i + " ");
                if(isSat("TEST") != cohr) {
                    System.out.println("ERROR!");
                } else {
                    System.out.println("IT IS ALL RIGHT!");                    
                }
            }
            if(engine.classifyTBox(false, null, timeLimit, false, true) == null) break;
            System.out.println("TOTAL TIME: " + (System.currentTimeMillis() - time1) / 1000.0);
        }
    }
    
    /**
     * Метод осуществляет загрузку онтологии из OWL файла.
     * @param path Определяет путь к файлу с базой знаний.
     * @param uA Определяет необходимо ли использовать алгоритм семантико-синтаксического выбора.
     * @param uB Определяет необходимо ли использовать backJumping.
     * @param uC Определяет необходимо ли использовать кэширование.
     * @param uS Определяет необходимо ли показывать логи.
     * @param uG Определяет необходимо ли использовать глобальное кэширование.
     * @param tL Определяет лимит времени.
     */
    public void loadOntology(String path, boolean uA, boolean uB, boolean uC, boolean uS, boolean uG, int tL)
    {
        FileInputStream fis = null;
        try {
            File F = new File(path);
            fis = new FileInputStream(F);
            long sizeOfFile = fis.getChannel().size() / 1024 / 1024;
            timeLimit = tL;
            if(engine == null)
                engine = new Classificator(r_box, t_box, a_box, uA, uB, uC, uG, uS, tL);
            else
                engine.clear();
            try {
                loadFromOWL(path);
                engine.setABox(a_box);
                engine.setTBox(t_box);
                engine.setRBox(r_box);
            } catch (Exception e) {
                System.err.println("Can't parse an ontoloy: " + path);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TReasoner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TReasoner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(TReasoner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Метод запускает запроса к базе базы знаний описанной в спецификации DL98.
     */
    public void checkQuery() {
        engine.classifyTBox(false, null, timeLimit, false, true);
        engine.checkQuery(query);
    }

    /**
     * Метод запускает проверку согласованности заданного концепта базы знаний.
     * @param concept_name Определяет имя концепта, проверка согласованности которого определяется.
     * @return Возвращает истина, если концепт является согласованным и ложь в противном случае.
     */
    public boolean isSat(String concept_name) {
        if(!engine.checkABoxSat())
            return false;
        return engine.checkSat(concept_name);
    }

    /**
     * Метод запускает проверку согласованности каждого концепта базы знаний описанной в спецификации DL98.
     */
    public void checkALCTBox() {
        if(engine == null)
            engine = new Classificator(r_box, t_box, a_box, true, true, true, true, false, 7000);
        engine.checkALCTBoxSat();
    }
    
    /**
     * Метод запускает проверку согласованности базы знаний.
     * @return Возвращает истина, если база знаний является согласованной и ложь в противном случае.
     */
    public boolean isConsistent() {
        return engine.checkABoxSat();
    }
    
    /**
     * Метод вызывает алгоритм классификации онтологии по заданому пути к ней.
     * В данном методе устанавливается парамтр, в соответствии с которым выполняется
     * одна из 4 команд: проверка включаемости двух определенных концептов, проверка
     * выполнимости определенного концепта, проверка согласованности базы знаний
     * и классификация базы знаний. Данный метод используется для отладки программы.
     * @param outfile_path Полный путь онтологии.
     * @return Список аксиом включения концептов.
     */
    public Set<OWLSubClassOfAxiom> classifyOntology(String outfile_path, boolean experimentAttr) {
        IRI fil = null; if(outfile_path != null) fil = IRI.create(new File(outfile_path));
        int manage = 0;
        if(manage == 1) {
            int potSubsumed = t_box.getRuleGraph().getConceptID("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#Capricciosa");
            int potOvrsumed = t_box.getRuleGraph().getConceptID("http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#InterestingPizza");
            boolean res = engine.checkSubsumption(potSubsumed, potOvrsumed);
            System.out.println(res);
            return null;
        }
        if(manage == 2) {
            boolean res = engine.checkSat("http://erlangen-crm.org/111201/E9_Move");
            //boolean res = engine.checkSat("http://purl.obolibrary.org/obo/MCBCC_0000067#WNT3");
            System.out.println(res);
            return null;
        }
        if(manage == 3) {
            boolean res = engine.checkABoxSat();
            System.out.println(res);
            return null;
        }
        Set<OWLSubClassOfAxiom> ans = engine.classifyTBox(
                                                            false, //Must I show the model?
                                                            fil, //Out file
                                                            timeLimit, //time limit
                                                            false, 
                                                            experimentAttr
                                                            ); //Must I out equivalent class axioms?
        return ans;
    }
    
    /**
     * Метод осуществляет классификацию онтологий из ORE 2013.
     * Все онтологии были распределены по нескольким файлам, из которых производится
     * чтение. Файлы соответствуют результатам полученным системой TReasoner при выполнении
     * операции классификаций баз знаний OWL DL.
     */
    public static void testORE2013() {
        String prefix = "D:\\ORE 2013\\ore2013-ontologies-offline\\owlxml\\all\\";
        String pstfix = "D:\\ORE 2013\\ans\\";
        
        TReasoner t = new TReasoner();
        ArrayList<String> totest_files = new ArrayList<String>();

        //all my true files
        totest_files.clear();
        Scanner scan = null;
        try {
            scan = new Scanner(new File("D:\\ORE 2013\\true.txt"));
            while(scan.hasNextLine()) {
                String fileName = scan.nextLine();
                File F = new File(fileName);
                if(!F.exists() || !F.isFile()) continue;
                totest_files.add(fileName);
            }
            totest_files.remove("07752c0c-5724-4e83-80f3-ba0d58da9373_L_v315.owl");
        }
        catch(FileNotFoundException e) {
            System.err.println("File not found: D:\\ORE 2013\\true.txt");
        }

        //all my false files
        //totest_files.clear();
        try {
            scan = new Scanner(new File("D:\\ORE 2013\\false.txt"));
            while(scan.hasNextLine()) {
                String fileName = scan.nextLine();
                File F = new File(fileName);
                if(!F.exists() || !F.isFile()) continue;
                totest_files.add(fileName);
            }
        } catch(FileNotFoundException e) {
            System.err.println("File not found: D:\\ORE 2013\\false.txt");
        }
        
        //all my recheck files
        totest_files.clear();
        try {
            scan = new Scanner(new File("D:\\ORE 2013\\too_slow.txt"));
            while(scan.hasNextLine()) {
                String fileName = scan.nextLine();
                File F = new File(prefix + fileName);
                if(!F.exists() || !F.isFile()) continue;
                totest_files.add(fileName);
            }
        } catch(FileNotFoundException e) {
            System.err.println("File not found: D:\\ORE 2013\\too_slow.txt");
        }
        
        //all other files
        totest_files.clear();
        try {
            scan = new Scanner(new File("D:\\ORE 2013\\other.txt"));
            while(scan.hasNextLine()) {
                String fileName = scan.nextLine();
                File F = new File(prefix + fileName);
                if(!F.exists() || !F.isFile()) continue;
                totest_files.add(fileName);
            }
        } catch(FileNotFoundException e) {
            System.err.println("File not found: D:\\ORE 2013\\other.txt");
        }
        
        totest_files.clear();
        totest_files.add("9cae0d3f-2386-48c9-aa25-f598a33c7c05_aeo-2.owl");
        //totest_files.add("87800dc9-355e-4d61-b761-dbe9d7db4526_111201.owl");
        //totest_files.add("9cae0d3f-2386-48c9-aa25-f598a33c7c05_aeo-2.owl");
        
        for(int i = 0; i < totest_files.size(); i++) {
            System.out.println(prefix + totest_files.get(i));
            t.loadOntology(prefix + totest_files.get(i), true, true, true, false, false, 900000);
            t.classifyOntology(pstfix + totest_files.get(i) + "_ans.owl", true);
        }
    }
    
    /**
     * Метод осуществляет классификацию баз знаний из DL98 Workshop.
     */
    public static void testDL98() {
        String prefix = "C:\\Users\\Boris\\Downloads\\dl98-test\\Data\\";
        String pstfix = "C:\\Users\\Boris\\Downloads\\dl98-test\\Ans\\";
        
        TReasoner t = new TReasoner();
        ArrayList<String> totest_files = new ArrayList<String>();
        totest_files.add("k_branch_n.owl");
        totest_files.add("k_d4_n.owl");
        totest_files.add("k_dum_n.owl");
        totest_files.add("k_grz_n.owl");
        totest_files.add("k_lin_n.owl");
        totest_files.add("k_path_n.owl");
        totest_files.add("k_ph_n.owl");
        totest_files.add("k_poly_n.owl");
        totest_files.add("k_t4p_n.owl");
        totest_files.add("k_branch_p.owl");
        totest_files.add("k_d4_p.owl");
        totest_files.add("k_dum_p.owl");
        totest_files.add("k_grz_p.owl");
        totest_files.add("k_lin_p.owl");
        totest_files.add("k_path_p.owl");
        totest_files.add("k_ph_p.owl");
        totest_files.add("k_poly_p.owl");
        totest_files.add("k_t4p_p.owl");
        
        prefix = "C:\\Users\\Boris\\YandexDisk\\DL98OWL\\";
        /*File folder = new File(prefix);
        if(folder.isDirectory()) {
            File[] filelist = folder.listFiles();
            for(int i = 0; i < filelist.length; i++) {
                if(filelist[i].getAbsolutePath().endsWith(".tkb")) {
                    System.out.println(filelist[i].getName());
                }
            }
        }*/

        totest_files.clear();
        totest_files.add("kris151.tkb.owl");
        totest_files.add("kris301.tkb.owl");
        totest_files.add("kris451.tkb.owl");
        totest_files.add("kris601.tkb.owl");
        totest_files.add("kris751.tkb.owl");
        totest_files.add("kris901.tkb.owl");
        totest_files.add("kris1051.tkb.owl");
        totest_files.add("kris1201.tkb.owl");
        totest_files.add("kris1351.tkb.owl");
        totest_files.add("kris1501.tkb.owl");
        totest_files.add("kris2001.tkb.owl");
        totest_files.add("kris4001.tkb.owl");
        totest_files.add("kris6001.tkb.owl");
        totest_files.add("kris8001.tkb.owl");
        totest_files.add("kris10001.tkb.owl");
        totest_files.add("kris12001.tkb.owl");
        totest_files.add("kris14001.tkb.owl");
        totest_files.add("kris16001.tkb.owl");
        totest_files.add("kris18001.tkb.owl");
        totest_files.add("kris20001.tkb.owl");
        totest_files.add("kris25001.tkb.owl");
        totest_files.add("kris30001.tkb.owl");
        totest_files.add("kris35001.tkb.owl");
        totest_files.add("kris40001.tkb.owl");
        totest_files.add("kris45001.tkb.owl");
        totest_files.add("kris50001.tkb.owl");
        
        /*for(int i = 0; i < totest_files.size(); i++) {
            t.loadKB(prefix + totest_files.get(i), "TKB", 1, true, true, true, false, true, 900000);
            System.out.println(prefix + totest_files.get(i) + ".owl");
            t.t_boxes[0].outToOWL(prefix + totest_files.get(i) + ".owl");
        }*/
        
        //totest_files.clear();
        //totest_files.add("kris2001.tkb.owl");
        //String filename = "kris1351.tkb";
        for(int i = 0; i < totest_files.size(); i++) {
            System.out.println(prefix + totest_files.get(i));
            //t.loadKB(prefix + totest_files.get(i), "TKB", 1, true, true, true, false, true, 900000);        
            //t.classifyTBoxes(true);
            t.loadOntology(prefix + totest_files.get(i), true, true, true, true, true, 900000);
            //t.classifyOntology(prefix + totest_files.get(i) + "_ans.owl");
            long curTime = System.currentTimeMillis();
            t.classifyOntology(null, true);
            long total = System.currentTimeMillis() - curTime;
            System.out.println("Total time:\t" + total);
        }
    }
    
    /**
     * Метод осуществляет классификацию одной онтологии по заданному имени.
     * @param name Имя онтологии, классификация которой производится.
     */
    public static void testOneOntology(String name) {
        TReasoner t = new TReasoner();
        t.loadOntology(name, true, true, true, false, false, 900000);
        t.classifyOntology(name + "_ans.owl", true);
    }
    
    public static void testORE2014() {
        try {
            String prefix = "C:\\Users\\Boris\\Downloads\\dataset\\";
            Scanner s = new Scanner(new File(prefix + "dl\\classification\\fileorder.txt"));
            int count = 0;
            while(s.hasNextLine()) {
                String oneFileName = s.nextLine();
                //System.out.println(oneFileName);
                count++;
                testOneOntology(prefix + "files\\" + oneFileName);
            }
            System.out.println(count);
            
        } catch (FileNotFoundException ex) {
            System.err.println("Can't open file.");
        }
    }
    
    /**
     * Метод из которго начинается работа программы. 
     * @param args аргементы командной строки.
     */
    public static void main(String[] args) {
        long st = System.currentTimeMillis();
        //testDL98();
        testORE2013();
        //testORE2014();
        
        //testOneOntology("D:\\ORE 2013\\ore2013-ontologies-offline\\owlxml\\dl\\37e5e889-ad29-4d2f-a2e3-106e70a1a3c3_iduals.owl");
        //testOneOntology("D:\\ORE 2013\\ore2013-ontologies-offline\\owlxml\\dl\\1184523f-1b0c-43ef-b50c-8c561473a1df_1541.owl");
        //testOneOntology("D:\\ORE 2013\\ore2013-ontologies-offline\\owlxml\\dl\\4f1e9310-9320-452e-948c-5cd5dacc36da_rnao.owl");
        System.out.println((System.currentTimeMillis() - st) / 1000.0);
    }
}