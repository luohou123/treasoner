package Checker;

import Enums.NodeType;
import Help.IntArray;
import KnowledgeBase.TBox;
import KnowledgeBase.RuleGraph.RuleNode;
import org.semanticweb.owlapi.model.OWLDatatype;

/**
 * Класс реализует алгоритм проверки согласованности конъюнкции типов данных,
 * ограничений на эти типы данных и констант присутствующих в онтологии.
 * @author Andrey Grigoryev
 */
class DataChecker {

    private IntArray clause = new IntArray();
    private TBox t_box = null;
    
    /**
     * Основной и единственный конструктор класса DataChecker.
     * @param _t_box Определяет TBox рассматриваемой онтологии
     */
    public DataChecker(TBox _t_box) {
        t_box = _t_box;
    }
    
    /**
     * Метод устанавливает пусты поле, отвечающее за TBox онтологии
     */
    public void clear() {
        t_box = null;
    }
    
    /**
     * Метод позволяет переопределить используемый TBox
     * @param new_t_box Определяет новый TBox
     */
    public void setTBox(TBox new_t_box) {
        t_box = new_t_box;
    }
    
    /**
     * Метод определяет является ли тип данных передаваемый в качестве параметра 
     * числовым типом данных.
     * @param d Определяет OWL тип данных 
     * @return Возвращает истина, если тип данных является целым числовым и ложь в противном случае
     */
    static public boolean isNumber(OWLDatatype d) {
        return  d.isDouble() || d.isFloat() || d.isInteger() || 
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#nonNegativeInteger") ||
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#positiveInteger") ||
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#long") ||
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#int") || 
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#gYear") || 
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#unsignedLong") || 
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#unsignedInt") || 
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#unsignedShort") ||
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#decimal");
    }

    /**
     * Метод определяет является ли тип данных передаваемый в качестве параметра 
     * числовым типом данных.
     * @param d Определяет OWL тип данных 
     * @return Возвращает истина, если тип данных является строковым и ложь в противном случае
     */
    static public boolean isString(OWLDatatype d)
    {
        return  d.isString() || 
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#anyURI") ||
                d.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#ID");
    }
    
    /**
     * Метод определяет является ли тип данных передаваемый в качестве параметра 
     * типом данных дата/время.
     * @param d Определяет OWL тип данных 
     * @return Возвращает истина, если тип данных является дата/время и ложь в противном случае
     */
    static public boolean isDatatime(OWLDatatype d)
    {
        return  d.getIRI().getFragment().equalsIgnoreCase("dateTime") || 
                d.getIRI().getFragment().equalsIgnoreCase("time") ||
                d.getIRI().getFragment().equalsIgnoreCase("gMonth") ||
                d.getIRI().getFragment().equalsIgnoreCase("duration") ||
                d.getIRI().getFragment().equalsIgnoreCase("date");
    }
    
    /**
     * Метод определяет является ли тип данных передаваемый в качестве параметра 
     * булевым типом данных.
     * @param d Определяет OWL тип данных 
     * @return Возвращает истина, если тип данных является булевым и ложь в противном случае
     */
    static public boolean isBoolean(OWLDatatype d)
    {
        return d.isBoolean();
    }
    
    /**
     * Метод определяет поддерживается ли рассматриваемый тип данных системой TReasoner
     * @param d Определяет OWL тип данных 
     * @return Возвращает истина, если тип данных не поддерживается TReasoner и ложь в противном случае
     */
    public boolean isUnknown(OWLDatatype d)
    {
        return  !isString(d) && 
                !isNumber(d) && 
                !isDatatime(d) && 
                !isBoolean(d) && 
                !d.isRDFPlainLiteral() && 
                !d.isTopDatatype() && 
                !d.toString().equals("rdf:XMLLiteral");
    }
    
    /**
     * Метод осуществляет проверку соответствия рассматриваемого конъюнкта строковому типу данных.
     * @param cl Определяет текущий рассматриваемый конъюнкт
     * @param t_box Определяет TBox онтологии
     * @return Возвращает истина, конъюнкция всех членов соответствует строковому типу данных и ложь в противном случае
     */
    public boolean checkString(IntArray cl, TBox t_box) {
        boolean primitive = true;
        for(int i = 0; i < cl.size(); i++) {
            RuleNode rn = t_box.getRuleGraph().getNode(Math.abs(cl.get(i)));
            if(rn.getFacet() != null || rn.getLiter() != null) {
                primitive = false; break;
            }
            if(rn.getDatatype() != null && rn.getLiter() != null) {
                if(!rn.getDatatype().getBuiltInDatatype().equals(rn.getLiter().getDatatype())) {
                    return false;
                }
            }
        }
        if(!primitive) {
            //check different liters
            for(int i = 0; i < cl.size(); i++) {
                if(t_box.getRuleGraph().getNode(cl.get(i)).getLiter() == null) continue;
                for(int j = 0; j < cl.size(); j++) {
                    if(t_box.getRuleGraph().getNode(cl.get(j)).getLiter() == null) continue;
                    String l1 = t_box.getRuleGraph().getNode(cl.get(i)).getLiter().getLiteral();
                    String l2 = t_box.getRuleGraph().getNode(cl.get(j)).getLiter().getLiteral();
                    if(!l1.equals(l2)) {
                        return false;
                    }
                }
            }
            //check different datatypes
            for(int i = 0; i < cl.size(); i++) {
                OWLDatatype dt1 = null;
                if(t_box.getRuleGraph().getNode(cl.get(i)).getLiter() != null)
                    dt1 = t_box.getRuleGraph().getNode(cl.get(i)).getLiter().getDatatype();
                if(dt1 == null) {
                    if(cl.get(i) == -1) return false;
                    if(cl.get(i) == 1) continue;
                    dt1 = t_box.getRuleGraph().getNode(cl.get(i)).getDatatype().asOWLDatatype();
                }
                if(dt1 == null) continue;
                if(dt1.isTopDatatype()) continue;
                for(int j = 0; j < cl.size(); j++) {
                    OWLDatatype dt2 = null;
                    if(t_box.getRuleGraph().getNode(cl.get(j)).getLiter() != null)
                        dt2 = t_box.getRuleGraph().getNode(cl.get(j)).getLiter().getDatatype();
                    if(dt1 == null)
                        dt2 = t_box.getRuleGraph().getNode(cl.get(j)).getDatatype().asOWLDatatype();
                    if(dt2 == null) continue;
                    if(dt2.isTopDatatype()) continue;
                    if(!dt1.equals(dt2)) {
                        return false;
                    }
                }
            }
        } else {
            return true;
        }
        return true;
    }
    
    /**
     * Метод осуществляет проверку соответствия рассматриваемого конъюнкта числовому типу данных.
     * @param cl Определяет текущий рассматриваемый конъюнкт
     * @param t_box Определяет TBox онтологии
     * @return Возвращает истина, конъюнкция всех членов соответствует числовому типу данных и ложь в противном случае
     */
    static public boolean checkNumber(IntArray cl, TBox t_box) {
        boolean primitive = true;
        for(int i = 0; i < cl.size(); i++) {
            RuleNode rn = t_box.getRuleGraph().getNode(Math.abs(cl.get(i)));
            if(rn.getFacet() != null && rn.getLiter() != null) {
                primitive = false; break;
            }
        }
        if(!primitive) {
            System.err.println("ERROR: unsupported procedure for numeric datatype checking");
        } else {
            return true;
        }
        return false;
    }
    
    /**
     * Метод осуществляет проверку соответствия рассматриваемого конъюнкта типу данных дата/время.
     * @param cl Определяет текущий рассматриваемый конъюнкт
     * @param t_box Определяет TBox онтологии
     * @return Возвращает истина, конъюнкция всех членов соответствует типу данных дата/время и ложь в противном случае
     */
    static public boolean checkDatetime(IntArray cl, TBox t_box) {
        boolean primitive = true;
        for(int i = 0; i < cl.size(); i++) {
            RuleNode rn = t_box.getRuleGraph().getNode(Math.abs(cl.get(i)));
            if(rn.getFacet() != null && rn.getLiter() != null) {
                primitive = false; break;
            }
        }
        if(!primitive) {
            System.err.println("ERROR: unsupported procedure for datetime datatype checking");
        } else {
            return true;
        }
        return false;
    }    
    
    /**
     * Метод осуществляет проверку согласованности конъюнкта типа данных.
     * Первым шагов производится проверка на то, что содержаться контрарные литеры в конънкте.
     * После определяется к какому из типов данных принадлежит конъюнкт и в соответствии с типом данных
     * вызывается метод проверки конъюнкта.
     * @return Возвращает истина, если текущий конъюнкт является согласованным
     */
    private boolean checkClause() {
        int iS = 0, iN = 0, iD = 0, iB = 0;
        int inS = 0, inN = 0, inD = 0, inB = 0;
        for(int i = 0; i < clause.size(); i++) {
            for(int j = i + 1; j < clause.size(); j++) {
                if(clause.get(i) == -clause.get(j)) return false;
            }
        }
        for(int i = 0; i < clause.size(); i++) {
            if(clause.get(i) == 1) {
                continue;
            }
            if(t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getDatatype() == null) {
                //System.out.println(t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getNodeType() + " " + t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getName());
                return true;
            }
            if(isString(t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getDatatype())) {
                if(clause.get(i) != -1)
                    iS = 1; else
                    inS = 1;
            } else
            if(isNumber(t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getDatatype())) {
                if(clause.get(i) != -1)
                    iN = 1; else
                    inN = 1;
            } else
            if(isDatatime(t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getDatatype())) {
                if(clause.get(i) != -1)
                    iD = 1; else
                    inD = 1;
            } else
            if(isBoolean(t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getDatatype())) {
                if(clause.get(i) != -1)
                    iB = 1; else
                    inB = 1;
            }
            
            if(isUnknown(t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getDatatype())) {
                OWLDatatype d = t_box.getRuleGraph().getNode(Math.abs(clause.get(i))).getDatatype();
                if(d.getIRI().getFragment().equals("installationTypeCode")) {
                    continue;
                }
                System.out.println("DataChecker -> CheckClause");
                System.out.println(d.toStringID());
            }
        }
        if( (iS + iN + iD + iB > 1) || (iS == 1 && inS == 1) || (iB == 1 && inB == 1) ||
            (iN == 1 && inN == 1) || (iD == 1 && inD == 1)) {
            return false;
        }
        
        if(iS == 1) { //this is string clause
            return checkString(clause, t_box);
        }
        
        if(iN == 1) { //this is number clause
            return checkNumber(clause, t_box);
        }
        
        if(iD == 1) { //this is datatime clause
            return checkDatetime(clause, t_box);
        }
        return true;
    }
    
    /**
     * Метод обеспечивает доступ к реализуемому алгоритму из других классов
     * @param data Определяет члены конъюнкта
     * @return Возвращает истина, если такая вершина данных может существовать и ложь иначе
     */
    public boolean getDataCheck(IntArray data) {
        clause.clear();
        return doAllClauses(data, 0);
    }
    
    /**
     * В методе реализуется перебор всевозможных конъюнктов.
     * При вызове метода, члены конъюнкции могут быть как конъюнкциями так и дизъюнкциями,
     * поэтому в данном методе осуществляется постепенное раскрытие скобок и проверка 
     * каждого отдельного конъюнкта
     * @param data Определяет конъюнкцию других членов
     * @param x Определяет какое количество членов конъюнкта было обработано
     * @return Возвращает истина, если существует хотя бы один конъюнкт удовлетворяющий
     * одному из типов данных и ложь в противном случае
     */
    private boolean doAllClauses(IntArray data, int x)
    {
        //if(true) return true;
        for(int i = x; i < data.size(); i++)
        {
            if(     (data.get(i) > 0 && t_box.getRuleGraph().getNode(Math.abs(data.get(i))).getNodeType() == NodeType.ntOR) || 
                    (data.get(i) < 0 && t_box.getRuleGraph().getNode(Math.abs(data.get(i))).getNodeType() == NodeType.ntAND))
            {
                int z = 1;
                if(data.get(i) < 0) z = -1;                
                for(int j = 0; j < t_box.getRuleGraph().getNode(Math.abs(data.get(i))).getChildrenSize(); j++)
                {
                    int clause_old_size = clause.size();
                    int data_old_size = data.size();
                    data.add(z * t_box.getRuleGraph().getNode(Math.abs(data.get(i))).getChildren()[j]);
                    if(doAllClauses(data, i + 1)) return true;
                    while(clause.size() != clause_old_size) clause.pop();
                    while(data.size() != data_old_size) data.pop();
                }
                return false;
            } else
            if(     (data.get(i) < 0 && t_box.getRuleGraph().getNode(Math.abs(data.get(i))).getNodeType() == NodeType.ntOR) || 
                    (data.get(i) > 0 && t_box.getRuleGraph().getNode(Math.abs(data.get(i))).getNodeType() == NodeType.ntAND))                
            {
                int z = 1;
                if(data.get(i) < 0) z = -1;
                for(int j = 0; j < t_box.getRuleGraph().getNode(Math.abs(data.get(i))).getChildrenSize(); j++)
                    data.add(z * t_box.getRuleGraph().getNode(Math.abs(data.get(i))).getChildren()[j]);
            } else
            {
                clause.add(data.get(i));
            }
        }
        return checkClause();
    }
}
