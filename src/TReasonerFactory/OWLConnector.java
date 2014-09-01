package TReasonerFactory;

import Enums.NodeType;
import Enums.RoleChar;
import KnowledgeBase.ABox;
import KnowledgeBase.RBox;
import KnowledgeBase.RuleGraph.RuleNode;
import KnowledgeBase.TBox;
import java.io.File;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

/**
 * Класс для чтения и разбора OWL-файлов.
 * Использует OWL API для преобразования файлов во внутренние структуры.
 * @author Andrey Grigoryev
 */

class OWLConnector {
    private OWLOntology Ontology;
    private AxiomTransformer axiom_transformer;

    /**
     * Конструктор класса в котором вызывается процедура разбора онтологии и результат
     * разбора записывается в свойство класса, соответствующее онтологии.
     * @param RES Путь к файлу онтологии в виде интернет-адреса.
     */
    public OWLConnector(String RES) {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            IRI iri = IRI.create(RES);
            Ontology = manager.loadOntologyFromOntologyDocument(iri);
            axiom_transformer = new AxiomTransformer();
        } catch (OWLOntologyCreationException ex) {
            System.err.println("Can't load ontology at " + RES);
        }
    }
    
    /**
     * Конструктор класса в котором вызывается процедура разбора онтологии и результат
     * разбора записывается в свойство класса, соответствующее онтологии.
     * В отличие от конструктора с одним параметром загружает онтологию из локального файла.
     * @param RES Путь к файлу онтологии.
     */
    public OWLConnector(String FileName, int x) {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            Ontology = manager.loadOntologyFromOntologyDocument(new File(FileName));
            axiom_transformer = new AxiomTransformer();
        } catch (OWLException ex) {
            System.err.println("Can't load ontology file " + FileName);
        }
    }
    
    /**
     * Возвращает строку соответствующую логике в которой описана база знаний.
     * @return Строковое представление логики в которой выражена онтология.
     */
    public String getLogicString() {
        String ret = "";
        if(axiom_transformer.bl_S) ret += "S";
        if(axiom_transformer.bl_R) ret += "R"; else 
            if(axiom_transformer.bl_H) ret += "H";
        if(axiom_transformer.bl_I) ret += "I";
        if(axiom_transformer.bl_F) ret += "F";
        return ret;
    }
    
    /**
     * Метод инициализирует преобразование аксиом базы знаний во внутренние структуры.
     */
    private void processAxioms() {
        Set<OWLClass> soc = Ontology.getClassesInSignature();
        for(OWLClass cl: soc) {
            if(cl.isOWLThing() || cl.isOWLNothing()) continue;
            axiom_transformer.getTBox().getRuleGraph().findConcept(cl.getIRI().toString());
        }
        Set<OWLLogicalAxiom> set_of_LogicalAxioms = Ontology.getLogicalAxioms();
        for(OWLLogicalAxiom logical_axiom: set_of_LogicalAxioms) {
            logical_axiom.accept(axiom_transformer);
        }
    }
    
    /**
     * Метод возвращает TBox базы знаний. В данном методе вызывается процедура преобразования
     * аксиом базы знаний.
     * @return TBox рассматриваемой базы знаний.
     */
    public TBox getTBox() {
        processAxioms();
        axiom_transformer.getTBox().processTransitive();
        return axiom_transformer.getTBox();
    }

    /**
     * Метод возвращает ABox базы знаний.
     * @return ABox базы знаний.
     */
    public ABox getABox() {
        return axiom_transformer.getABox();
    }
    
    /**
     * Метод возвращает RBox базы знаний.
     * @return RBox базы знаний.
     */
    public RBox getRBox() {
        return axiom_transformer.getRBox();
    }

    /**
     * Класс реализуеющий механизм шаблона визиторов.
     */
    private class AxiomTransformer implements OWLAxiomVisitor
    {
        private TBox t_box;
        private RBox r_box;
        private ABox a_box;
        
        public boolean bl_F = false;
        public boolean bl_S = false;
        public boolean bl_R = false;
        public boolean bl_H = false;
        public boolean bl_I = false;

        /**
         * Основной конструктор класса, в котором создаются объекты ABox, RBox, TBox.
         */
        public AxiomTransformer() {
            a_box = new ABox();
            r_box = new RBox();
            t_box = new TBox(r_box, a_box);
            t_box.setRBox(r_box);
        }
        
        /**
         * Метод возвращает TBox рассматриваемой онтологии.
         * @return TBox базы знаний.
         */
        public TBox getTBox() {
            return t_box;
        }
        
        /**
         * Метод возвращает ABox рассматриваемой онтологии.
         * @return ABox базы знаний.
         */
        public ABox getABox() {
            return a_box;
        }
        
        /**
         * Метод возвращает RBox рассматриваемой онтологии.
         * @return RBox базы знаний.
         */
        public RBox getRBox() {
            return r_box;
        }
        
        /**
         * Осуществляет разбор аксиомы описания.
         * @param owlda Ссылка на аксиому описания.
         */
        @Override
        public void visit(OWLDeclarationAxiom owlda) {
        }

        /**
         * Осуществляет разбор аксиомы включения концептов.
         * @param owlsc Ссылка на аксиому включения концептов.
         */
        @Override
        public void visit(OWLSubClassOfAxiom owlsc) {
            if(owlsc.getSuperClass().isOWLThing()) {
                int sub = t_box.getRuleGraph().addExpr2Graph(owlsc.getSubClass());
                t_box.addGCI(sub, 1);
                return;
            }
            if(owlsc.getSubClass().isOWLNothing()) {
                int sub = t_box.getRuleGraph().addExpr2Graph(owlsc.getSubClass());
                t_box.addGCI(sub, -1);
                return;
            }
            int sub = t_box.getRuleGraph().addExpr2Graph(owlsc.getSubClass());
            int sup = t_box.getRuleGraph().addExpr2Graph(owlsc.getSuperClass());
            t_box.addGCI(sub, sup);
        }

        /**
         * Осуществляет разбор аксиомы не принадлежности индивидов отношению.
         * @param owlnp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLNegativeObjectPropertyAssertionAxiom owlnp) {
            RuleNode rn = new RuleNode(NodeType.ntALL, r_box.findRole(owlnp.getProperty()));
            int nd = t_box.getRuleGraph().addNode2RuleTree(rn);

            int i1 = t_box.getRuleGraph().findIndivid(owlnp.getSubject());
            int i2 = t_box.getRuleGraph().findIndivid(owlnp.getObject());
            rn.addChild(-i2);
            a_box.add(t_box.getRuleGraph().getNode(i1).getIndividNumber(), nd);
        }

        /**
         * Осуществляет разбор аксиомы ассиметричного отношения.
         * @param owlp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLAsymmetricObjectPropertyAxiom owlp) {
            r_box.setRoleCharacteristic(owlp.getProperty().asOWLObjectProperty(), RoleChar.drASYMM);          
        }

        /**
         * Осуществляет разбор аксиомы рефелксивного отношения.
         * @param owlrp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLReflexiveObjectPropertyAxiom owlrp) {
            r_box.setRoleCharacteristic(owlrp.getProperty().asOWLObjectProperty(), RoleChar.drREF);            
        }

        /**
         * Осуществляет разбор аксиомы непересекающихся классов.
         * @param owldca Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLDisjointClassesAxiom owldca) {
            for(OWLSubClassOfAxiom cur_axiom : owldca.asOWLSubClassOfAxioms())
                cur_axiom.accept(this);
        }

        /**
         * Осуществляет разбор аксиомы устанавливающей домен отношения типа данных.
         * @param owldpd Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLDataPropertyDomainAxiom owldpd) {
            int cur = t_box.getRuleGraph().addExpr2Graph(owldpd.getDomain());
            r_box.setDomainToDataRole(owldpd.getProperty(), cur);
        }

        /**
         * Осуществляет разбор аксиомы устанавливающей домен отношения.
         * @param owlpd Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLObjectPropertyDomainAxiom owlpd) {
            int cur = t_box.getRuleGraph().addExpr2Graph(owlpd.getDomain());
            r_box.setDomainToRole(owlpd.getProperty(), cur, t_box);
        }

        /**
         * Осуществляет разбор аксиомы эквивалентных концептов.
         * @param owlp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLEquivalentObjectPropertiesAxiom owlp) {
            for(OWLObjectPropertyExpression first_iterator: owlp.getProperties()) {
                for(OWLObjectPropertyExpression second_iterator: owlp.getProperties()) {
                    if(first_iterator == second_iterator) continue;
                    r_box.eqvRoles(first_iterator, second_iterator);
                }
            }
        }

        /**
         * Осуществляет разбор аксиомы не принадлежности индивида и литеры отношению.
         * @param owlnp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLNegativeDataPropertyAssertionAxiom owlndp) {
            //skip
        }

        /**
         * Осуществляет разбор аксиомы различных индивидов.
         * @param owldia Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLDifferentIndividualsAxiom owldia) {
            for(OWLIndividual individ1: owldia.getIndividualsAsList()) {
                for(OWLIndividual individ2: owldia.getIndividualsAsList()) {
                    if(individ1 == individ2) continue;
                    int i1 = t_box.getRuleGraph().findIndivid(individ1);
                    int i2 = t_box.getRuleGraph().findIndivid(individ2);
                    a_box.addDiff(t_box.getRuleGraph().getNode(i1).getIndividNumber(), t_box.getRuleGraph().getNode(i2).getIndividNumber(), i1, i2);
                }
            }
        }

        /**
         * Осуществляет разбор аксиомы непересекаемости отношений данных.
         * @param owldp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLDisjointDataPropertiesAxiom owldp) {
            for(OWLDataPropertyExpression expr1: owldp.getProperties())
                for(OWLDataPropertyExpression expr2: owldp.getProperties()) {
                    if(expr1 == expr2) continue;
                    r_box.addDisjointDataRole(expr1, expr2);
                }                
        }

        /**
         * Осуществляет разбор аксиомы непересекаемости отношений.
         * @param owldp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLDisjointObjectPropertiesAxiom owldp) {
            for(OWLObjectPropertyExpression expr1: owldp.getProperties())
                for(OWLObjectPropertyExpression expr2: owldp.getProperties()) {
                    if(expr1 == expr2) continue;
                    r_box.addDisjointRole(expr1, expr2);
                }                
        }

        /**
         * Осуществляет разбор аксиомы устанавливающей область значений отношения.
         * @param owlpr Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLObjectPropertyRangeAxiom owlpr) {
            int cur = t_box.getRuleGraph().addExpr2Graph(owlpr.getRange());
            r_box.setRangeToRole(owlpr.getProperty(), cur);
        }

        /**
         * Осуществляет разбор аксиомы принадлежность пары индивидов отношению.
         * @param owlp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLObjectPropertyAssertionAxiom owlp) {
            RuleNode rn = new RuleNode(NodeType.ntSOME, r_box.findRole(owlp.getProperty()));
            int nd = t_box.getRuleGraph().addNode2RuleTree(rn);
            int i1 = t_box.getRuleGraph().findIndivid(owlp.getSubject());
            int i2 = t_box.getRuleGraph().findIndivid(owlp.getObject());
            rn.addChild(i2);
            a_box.add(t_box.getRuleGraph().getNode(i1).getIndividNumber(), nd);
        }

        /**
         * Осуществляет разбор аксиомы функциональности роли.
         * @param owlfp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLFunctionalObjectPropertyAxiom owlfp) {
            r_box.setRoleCharacteristic(owlfp.getProperty(), RoleChar.drFUNC);       
            bl_F = true;
        }

        /**
         * Осуществляет разбор аксиомы включения отношений.
         * @param owlsp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLSubObjectPropertyOfAxiom owlsp) {
            r_box.subRoles(owlsp.getSubProperty(), owlsp.getSuperProperty());
            bl_H = true;
        }

        /**
         * Осуществляет разбор аксиомы непересекаемости дизъюнкции.
         * @param owldua Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLDisjointUnionAxiom owldua) {
            owldua.getOWLDisjointClassesAxiom().accept(this);
            owldua.getOWLEquivalentClassesAxiom().accept(this);
        }

        /**
         * Осуществляет разбор аксиомы симметричных ролей.
         * @param owlsp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLSymmetricObjectPropertyAxiom owlsp) {
            r_box.setRoleCharacteristic(owlsp.getProperty(), RoleChar.drSYMM);       
        }

        /**
         * Осуществляет разбор аксиомы устанавливающей область значений роли данных.
         * @param owldpr Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLDataPropertyRangeAxiom owldpr) {
            int cur = t_box.getRuleGraph().addDataExpr2Graph(owldpr.getRange());
            r_box.setRangeToDataRole(owldpr.getProperty(), cur, t_box);
        }

        /**
         * Осуществляет разбор аксиомы функциональности роли данных.
         * @param owlfdp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLFunctionalDataPropertyAxiom owlfdp) {
            r_box.setDataRoleCharacteristic(owlfdp.getProperty(), RoleChar.drFUNC);
            bl_F = true;
        }

        /**
         * Осуществляет разбор аксиомы эквивалентных ролей данных.
         * @param owldз Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLEquivalentDataPropertiesAxiom owldp) {
            for(OWLDataPropertyExpression first_iterator: owldp.getProperties()) {
                for(OWLDataPropertyExpression second_iterator: owldp.getProperties()) {
                    if(first_iterator == second_iterator) continue;
                    r_box.eqvDataRoles(first_iterator, second_iterator);
                }
            }
        }

        /**
         * Осуществляет разбор аксиомы принадлежности индивида классу.
         * @param owlcaa Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLClassAssertionAxiom owlcaa) {
            int x = t_box.getRuleGraph().addExpr2Graph(owlcaa.getClassExpression());
            int y = t_box.getRuleGraph().findIndivid(owlcaa.getIndividual());
            a_box.add(t_box.getRuleGraph().getNode(y).getIndividNumber(), x);
        }

        /**
         * Осуществляет разбор аксиомы эквивалентных классов.
         * @param owleca Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLEquivalentClassesAxiom owleca) {
            int left_operand = 0;
            int right_operand = 0;
                            
            Set<OWLClassExpression> set_of_owleca = owleca.getClassExpressions();
            int k = 1;
            for(OWLClassExpression axiom: set_of_owleca) {
                k = 1 - k;
                if(k == 1) {
                    right_operand = t_box.getRuleGraph().addExpr2Graph(axiom);
                    if(right_operand > 0) {
                        if(t_box.getRuleGraph().getNode(right_operand).getName() != null) {
                            t_box.addEquivalenceAxiom(right_operand, left_operand);
                            if(left_operand > 0)
                                if(t_box.getRuleGraph().getNode(left_operand).getName() != null) {
                                    t_box.addEquivalenceAxiom(left_operand, right_operand);
                                    //t_box.addGCI(left_operand, right_operand);
                                    //t_box.addGCI(right_operand, left_operand);
                                }
                        } else {
                            t_box.addEquivalenceAxiom(left_operand, right_operand);
                            //t_box.addGCI(left_operand, right_operand);
                            //t_box.addGCI(right_operand, left_operand);
                        }
                    } else {
                        t_box.addEquivalenceAxiom(left_operand, right_operand);                            
                        //t_box.addGCI(left_operand, right_operand);
                        //t_box.addGCI(right_operand, left_operand);
                    }
                } else {
                    left_operand = t_box.getRuleGraph().addExpr2Graph(axiom);
                }
            }
        }

        @Override
        public void visit(OWLDataPropertyAssertionAxiom owldp) {
            //a_box.addDataRole(owldp.getProperty(), owldp.getSubject(), owldp.getObject(), 0);
        }

        /**
         * Осуществляет разбор аксиомы транзитивности роли.
         * @param owltp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLTransitiveObjectPropertyAxiom owltp) {
            r_box.setRoleCharacteristic(owltp.getProperty(), RoleChar.drTRANS);       
            bl_S = true;
        }

        /**
         * Осуществляет разбор аксиомы иррефлексивности роли.
         * @param owlp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLIrreflexiveObjectPropertyAxiom owlp) {
            r_box.setRoleCharacteristic(owlp.getProperty(), RoleChar.drIRREF);       
        }

        /**
         * Осуществляет разбор аксиомы устанавливающей отношение включаемости нал роляи данных.
         * @param owlsdp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLSubDataPropertyOfAxiom owlsdp) {
            r_box.subDataRoles(owlsdp.getSubProperty(), owlsdp.getSuperProperty());
        }

        /**
         * Осуществляет разбор аксиомы функиональности обратной роли.
         * @param owlfp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLInverseFunctionalObjectPropertyAxiom owlfp) {
            r_box.setRoleCharacteristic(owlfp.getProperty(), RoleChar.drINVFUNC);       
        }

        /**
         * Осуществляет разбор аксиомы устанавливающей отношение равенства индивидов.
         * @param owlsia Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLSameIndividualAxiom owlsia) {
            for(OWLIndividual individ1: owlsia.getIndividualsAsList()) {
                for(OWLIndividual individ2: owlsia.getIndividualsAsList()) {
                        if(individ1 == individ2) continue;
                        //a_box.addSame(individ1, individ2);
                }
            }
        }

        /**
         * Осуществляет разбор аксиомы устанавливающей включаемость роли в комбинацию ролей.
         * @param owlspc Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLSubPropertyChainOfAxiom owlspc) {
            r_box.addSubChainOf(owlspc.getSuperProperty(), owlspc.getPropertyChain());
            bl_R = true;
        }

        /**
         * Осуществляет разбор аксиомы обратных ролей.
         * @param owlp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLInverseObjectPropertiesAxiom owlp) {
            r_box.setInverseRoles(owlp.getFirstProperty(), owlp.getSecondProperty());
            bl_I = true;
        }

        /**
         * Осуществляет разбор аксиомы ключей.
         * @param owlhka Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLHasKeyAxiom owlhka) {
            //unsupported yet
        }

        /**
         * Осуществляет разбор аксиомы определения роли данных.
         * @param owldda Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLDatatypeDefinitionAxiom owldda) {
            //r_box.findRole();
        }

        /**
         * Осуществляет разбор аксиомы SWLR правила.
         * @param swrlr Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(SWRLRule swrlr) {
            //skip
        }

        /**
         * Осуществляет разбор аксиомы аннотации принадлежности индивида классу.
         * @param owlaaa Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLAnnotationAssertionAxiom owlaaa) {
            //skip
        }

        /**
         * Осуществляет разбор аксиомы аннотации подролей.
         * @param owlsp Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLSubAnnotationPropertyOfAxiom owlsp) {
            //skip
        }

        /**
         * Осуществляет разбор аксиомы аннотации домена роли.
         * @param owlpd Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLAnnotationPropertyDomainAxiom owlpd) {
            //skip
        }

        /**
         * Осуществляет разбор аксиомы аннотации области значений роли.
         * @param owlpr Ссылка на соответствующую аксиому.
         */
        @Override
        public void visit(OWLAnnotationPropertyRangeAxiom owlpr) {
            //skip
        }   
    }
}
