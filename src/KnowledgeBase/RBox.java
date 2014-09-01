package KnowledgeBase;

import Enums.NodeType;
import Enums.RoleChar;
import Help.IntArray;
import KnowledgeBase.RuleGraph.RuleNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * Класс хранит информацию и аксиомы о ролях заданных в RBox онтологии.
 * @author Andrey Grigoryev
 */
public class RBox {
    
    private SimpleRole[] roles;
    private int rolesSize = 0;
    private HashMap<String, Integer> role2int;
    private IntArray[] roleRanges;
    private IntArray[] roleDomains;
    private int MaxRoles = 128;
    
    /**
     * Основной и единственный конструктор класса.
     * Инициализирует массив ролей RBox.
     */
    public RBox() {
        roles = new SimpleRole[MaxRoles];
        role2int = new HashMap<String, Integer>();
    }
    
    private void increaseRoles() {
        SimpleRole[] temp = new SimpleRole[rolesSize];
        for(int i = 0; i < rolesSize; i++) {
            temp[i] = roles[i];
        }
        MaxRoles = MaxRoles * 2;
        roles = new SimpleRole[MaxRoles];
        for(int i = 0; i < rolesSize; i++) {
            roles[i] = temp[i];
        }
        temp = null;
    }
    
    /**
     * Предоставляет доступ для чтения свойства количество ролей.
     * @return Возвращает количество ролей.
     */
    public int getRoleSize() {
        return rolesSize;
    }
        
    /**
     * Метод предоставлет доступ к роли базы знаний по индексу.
     * @param role_index Индекс роли.
     * @return Ссылку на объект {@link SimpleRole}, соответствующий простой роли.
     */
    public SimpleRole getRoleByIndex(int role_index) {
        return roles[role_index];
    }

    /**
     * Метод осуществляет поиск роли в базе знаний по объекту {@link OWLObjectPropertyExpression}.
     * Если такой роли в базе знаний не существует, то она будет добавлена.
     * @param role_expr Ссылка на объект, соответствующий роли базы знаний.
     * @return Индекс заданной роли в базе знаний.
     */
    public int findRole(OWLObjectPropertyExpression role_expr) {
        return findRole(role_expr.asOWLObjectProperty().getIRI().toString());
    }
    
    /**
     * Метод осуществляет поиск роли в базе знаний по объекту {@link OWLDataPropertyExpression}.
     * Если такой роли в базе знаний не существует, то она будет добавлена.
     * @param data_role_expr Ссылка на объект, соответствующий роли базы знаний.
     * @return Индекс заданной роли в базе знаний.
     */
    public int findRole(OWLDataPropertyExpression data_role_expr) {
        return findRole(data_role_expr.asOWLDataProperty().getIRI().toString());
    }
    
    /**
     * Метод осуществляет поиск роли в базе знаний по строковому представлению.
     * Если такой роли в базе знаний не существует, то она будет добавлена.
     * @param role_name Ссылка на объект, соответствующий роли базы знаний.
     * @return Индекс заданной роли в базе знаний.
     */
    public int findRole(String role_name) {
        if(role2int.containsKey(role_name)) return role2int.get(role_name);
        
        if(rolesSize >= MaxRoles - 1) {
            increaseRoles();
        }
        
        roles[rolesSize++] = new SimpleRole(role_name);
        role2int.put(role_name, rolesSize - 1);
        return rolesSize - 1;
    }
    
    /**
     * Метод возвращает индексы вершин леса ограничений, соответствующие домену заданной роли.
     * Если заданная роль имеет надроли, то необходимо добавить и их домены тоже.
     * @param current_role Индекс роли, домены которой определяются методом.
     * @return Массив индексов вершин леса ограничений.
     */
    public IntArray getAllRanges(int current_role) {
        if(roleRanges == null) roleRanges = new IntArray[rolesSize];
        if(roleRanges[current_role] == null) {
            roleRanges[current_role] = new IntArray();
        } else {
            return roleRanges[current_role];
        }
        
        for(int it: roles[current_role].getSuperRoles()) {
            if(roles[it].getRange() != 0)
                roleRanges[current_role].add(roles[it].getRange());
            roleRanges[current_role].add(getAllRanges(it));
        }
        for(int it: roles[current_role].getEqvRoles()) {
            if(roles[it].getRange() != 0)
                roleRanges[current_role].add(roles[it].getRange());
            roleRanges[current_role].add(getAllRanges(it));
        }
        return roleRanges[current_role];
    }
    
    /**
     * Метод возвращает индексы вершин леса ограничений, соответствующие области значений заданной роли.
     * Если заданная роль имеет надроли, то необходимо добавить и их области значений тоже.
     * @param current_role Индекс роли, области значений которой определяются методом.
     * @return Массив индексов вершин леса ограничений.
     */
    public IntArray getAllDomains(int current_role)
    {
        if(roleDomains == null) roleDomains = new IntArray[rolesSize];
        if(roleDomains[current_role] == null) {
            roleDomains[current_role] = new IntArray();
        } else {
            return roleDomains[current_role];
        }
        
        for(int it: roles[current_role].getSuperRoles()) {
            if(roles[it].getDomain() != 0)
                roleDomains[current_role].add(roles[it].getDomain());
            roleDomains[current_role].add(getAllDomains(it));
        }
        for(int it: roles[current_role].getEqvRoles()) {
            if(roles[it].getDomain() != 0)
                roleDomains[current_role].add(roles[it].getDomain());
            roleDomains[current_role].add(getAllDomains(it));
        }
        return roleDomains[current_role];
    }
    
    /**
     * Метод осуществляет добавление аксиомы обратных ролей.
     * @param role_name1 Первая роль аксиомы.
     * @param role_name2 Вторая роль аксиомы.
     */
    public void setInverseRoles(OWLObjectPropertyExpression role_name1, OWLObjectPropertyExpression role_name2) {
        int index_of_role_name1 = findRole(role_name1);
        int index_of_role_name2 = findRole(role_name2);
        roles[index_of_role_name1].addInverseRoleIndex(index_of_role_name2);
        roles[index_of_role_name2].addInverseRoleIndex(index_of_role_name1);
    }
    
    /**
     * Метод устанавливает свойства роли.
     * @param role_name Ссылка на роль, свойства которой устанавливаются.
     * @param role_characteristics Свойства роли.
     */
    public void setRoleCharacteristic(OWLObjectPropertyExpression role_name, RoleChar role_characteristics) {
        int index_of_current_role = findRole(role_name);
        roles[index_of_current_role].setCharacteristic(role_characteristics);
    }
    
    /**
     * Метод устанавливает свойства роли данных.
     * @param data_role_name Ссылка на роль, свойства которой устанавливаются.
     * @param role_characteristics Свойства роли.
     */
    public void setDataRoleCharacteristic(OWLDataPropertyExpression data_role_name, RoleChar role_characteristics) {
        int index_of_current_data_role = findRole(data_role_name);
        roles[index_of_current_data_role].setCharacteristic(role_characteristics);
    }
    
    /**
     * Метод осуществляет добавление аксиомы включения для ролей данных.
     * @param sub_role Подроль в аксиоме.
     * @param super_role Надроль аксиоме. 
     */
    public void subDataRoles(OWLDataPropertyExpression sub_role, OWLDataPropertyExpression super_role) {
        int index = findRole(super_role);
        roles[index].getSubRoles().add(findRole(sub_role));
    }
    
    /**
     * Метод осуществляет добавление аксиомы включения ролей.
     * @param sub_role Подроль в аксиоме.
     * @param super_role Надроль аксиоме. 
     */
    public void subRoles(OWLObjectPropertyExpression sub_role, OWLObjectPropertyExpression super_role) {
        int index1 = findRole(super_role);
        int index2 = findRole(sub_role);
        roles[index1].getSubRoles().add(index2);
        roles[index2].getSuperRoles().add(index1);
    }
    
    /**
     * Метод осуществляет добавление аксиомы эквивалентности ролей.
     * @param first_property Левая часть аксиомы.
     * @param second_property Правая часть аксиомы. 
     */
    public void eqvRoles(OWLObjectPropertyExpression first_property, OWLObjectPropertyExpression second_property) {
        int first_index = findRole(first_property);
        int second_index = findRole(second_property);
        roles[first_index].getEqvRoles().add(second_index);
    }

    /**
     * Метод осуществляет добавление аксиомы эквивалентности для ролей данных.
     * @param first_property Левая часть аксиомы.
     * @param second_property Правая часть аксиомы. 
     */
    public void eqvDataRoles(OWLDataPropertyExpression first_property, OWLDataPropertyExpression second_property) {
        int first_index = findRole(first_property);
        int second_index = findRole(second_property);
        roles[first_index].getEqvRoles().add(second_index);
    }
    
    /**
     * Метод устаналивает свойство области значений роли.
     * @param role Ссылка на роль для которой устанавливается область значений.
     * @param range Индекс вершины леса ограничений, соответствующий области значений. 
     */
    public void setRangeToRole(OWLObjectPropertyExpression role, int range) {
        int index = findRole(role);
        roles[index].setRange(range);
    }
    
    /**
     * Метод устаналивает свойство области значений роли.
     * Добавляет в область значений роли ещё один член конъюнкции.
     * @param role Ссылка на роль для которой устанавливается область значений.
     * @param range Индекс вершины леса ограничений, соответствующий области значений. 
     * @param t_box Ссылка на TBox рассматриваемой базы знаний.
     */
    public void setRangeToDataRole(OWLDataPropertyExpression role, int range, TBox t_box) {
        int index = findRole(role);
        if(roles[index].getRange() == 0) {
            roles[index].setRange(range);
        } else {
            if(roles[index].getRange() < 0) {
                if(t_box.getRuleGraph().getNode(roles[index].getRange()).getNodeType() != NodeType.ntAND) {
                    RuleNode rn = new RuleNode(NodeType.ntAND);
                    rn.addChild(range);
                    rn.addChild(roles[index].getRange());
                    roles[index].setRange(t_box.getRuleGraph().addNode2RuleTree(rn));
                } else {
                    t_box.getRuleGraph().getNode(roles[index].getRange()).addChild(range);
                }
            } else {
                RuleNode rn = new RuleNode(NodeType.ntAND);
                rn.addChild(range);
                rn.addChild(roles[index].getRange());
                roles[index].setRange(t_box.getRuleGraph().addNode2RuleTree(rn));                
            }
        }
    }
    
    /**
     * Метод устаналивает свойство домена роли.
     * Добавляет в домен роли ещё один член конъюнкции.
     * @param role Ссылка на роль для которой устанавливается область значений.
     * @param domain Индекс вершины леса ограничений, соответствующий домену.
     * @param t_box Ссылка на TBox рассматриваемой базы знаний.
     */
    public void setDomainToRole(OWLObjectPropertyExpression role, int domain, TBox t_box) {
        int index = findRole(role);
        if(roles[index].getDomain() == 0) {
            roles[index].setDomain(domain);
        } else {
            if(roles[index].getDomain() < 0) {
                if(t_box.getRuleGraph().getNode(roles[index].getDomain()).getNodeType() != NodeType.ntAND) {
                    RuleNode rn = new RuleNode(NodeType.ntAND);
                    rn.addChild(domain);
                    rn.addChild(roles[index].getDomain());
                    roles[index].setDomain(t_box.getRuleGraph().addNode2RuleTree(rn));
                } else {
                    t_box.getRuleGraph().getNode(roles[index].getDomain()).addChild(domain);
                }
            } else {
                RuleNode rn = new RuleNode(NodeType.ntAND);
                rn.addChild(domain);
                rn.addChild(roles[index].getDomain());
                roles[index].setDomain(t_box.getRuleGraph().addNode2RuleTree(rn));                
            }
        }
    }
    
    /**
     * Метод устаналивает свойство домена роли.
     * @param role Ссылка на роль для которой устанавливается область значений.
     * @param domain Индекс вершины леса ограничений, соответствующий домену.
     */
    public void setDomainToDataRole(OWLDataPropertyExpression role, int domain) {
        int index = findRole(role);
        roles[index].setDomain(domain);
    }
    
    /**
     * Метод осуществляет добавление аксиомы непересекаемости двух ролей.
     * @param first_role Левая часть аксиомы.
     * @param second_role Правая часть аксиомы.
     */
    public void addDisjointRole(OWLObjectPropertyExpression first_role, OWLObjectPropertyExpression second_role) {
        int index1 = findRole(first_role);
        int index2 = findRole(second_role);
        roles[index1].getDsjRoles().add(index2);
    }
    
    /**
     * Метод осуществляет добавление аксиомы непересекаемости двух ролей данных.
     * @param first_role Левая часть аксиомы.
     * @param second_role Правая часть аксиомы.
     */
    public void addDisjointDataRole(OWLDataPropertyExpression first_role, OWLDataPropertyExpression second_role) {
        int index1 = findRole(first_role);
        int index2 = findRole(second_role);
        roles[index1].getDsjRoles().add(index2);
    }

    /**
     * Метод осуществляет добавление аксиомы включения роли в цепочку ролей.
     * @param role Левая часть аксиомы
     * @param chain Правая часть аксиомы.
     */
    public void addSubChainOf(OWLObjectPropertyExpression role, List<OWLObjectPropertyExpression> chain)
    {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for(OWLObjectPropertyExpression link: chain) {
            temp.add(findRole(link));
        }
        roles[findRole(role)].addSubChain(temp);
    }

    /**
     * Метод определяет являются ли роли непересекающимися.
     * @param role1 Индекс первой роли.
     * @param role2 Индекс второй роли.
     * @return Возвращает истина, если роли является непересекающимися и ложь в противном случае.
     */
    public boolean isDisjoint(int role1, int role2) {
        if(role1 == role2) return false;
        return roles[role1].getDsjRoles().contains(role2) || roles[role2].getDsjRoles().contains(role1);
    }

    
    public boolean hasCommonFuncAllRoles(int[] ari, int x)
    {
        if(f == null)
            f = new int[rolesSize + 4];
        Arrays.fill(f, 0);
        for(int i = 0; i < ari.length; i++) {
            markRoles(ari[i]);
        }
        return hasFunc(x);
    }
    
    /**
     * Метод помечает все надроли заданной роли.
     * @param x Индекс роли.
     */
    private void markRoles(int x) {
        if(f[x] == 1) return;
        f[x] = 1;
        for(int it: getRoleByIndex(x).getSuperRoles()) {
            markRoles(it);
        }
    }
    
    /**
     * Метод определяет является ли заданная роль функциональной.
     * @param x Индекс роли.
     * @return Возвращает истина, если роль является функциональной и ложь в противном случае.
     */
    private boolean hasFunc(int x) {
        if(f[x] == 1 && getRoleByIndex(x).isFunctional()) return true;
        for(int it: getRoleByIndex(x).getSuperRoles()) {
            if(hasFunc(it)) return true;
        }
        return false;
    }
    
    int f[] = null;
    /**
     * Метод осуществляет провереку имеют ли две заданные роли общую функциональную надроль.
     * @param role1 Индекс первой роли.
     * @param role2 Индекс второй роли.
     * @return Возвращает истина, если в базе знаний существует функциональная надроль заданных ролей.
     */
    public boolean hasCommonFuncAnc(int role1, int role2)
    {
        if(f == null)
            f = new int[rolesSize + 4];
        Arrays.fill(f, 0);
        markRoles(role1);
        return hasFunc(role2);
    }

    /**
     * В методе осуществляется проверка, является ли заданная роль подролью другой, или эквивалентной ей.
     * @param sub_role Предполагаемая подроль.
     * @param super_role Предполагаемая надроль.
     * @return Возвращает истина, если роли эквивалентны или входят в отношение включения и ложь в противном случае.
     */
    public boolean isSubOrEqual(int sub_role, int super_role) {
        return isSub(sub_role, super_role) || isEqual(sub_role, super_role);
    }
    
    /**
     * В методе осуществляется проверка, является ли заданная роль подролью другой.
     * @param sub_role Предполагаемая подроль.
     * @param super_role Предполагаемая надроль.
     * @return Возвращает истина, если входят в отношение включения и ложь в противном случае.
     */
    public boolean isSub(int sub_role, int super_role) {
        return roles[super_role].getSubRoles().contains(sub_role);
    }
    
    /**
     * В методе осуществляется проверка, является ли заданная роль эквивалентной другой.
     * @param sub_role Первая роль.
     * @param super_role Вторая роль.
     * @return Возвращает истина, если роли эквивалентны и ложь в противном случае.
     */
    public boolean isEqual(int sub_role, int super_role) {
        return roles[super_role].getEqvRoles().contains(sub_role) || sub_role == super_role;
    }
    
    /**
     * Метод осуществляет проверку того, существует ли в заданном массиве хотя бы одна подроль заданной роли.
     * @param sub_roles Массив ролей.
     * @param super_role Предполагаемая над роль.
     * @return Возвращает истина, если среди массива ролей существует подроль заданной роли.
     */
    public boolean isSubOrEqualAll(int[] sub_roles, int super_role) {
        for(int i = 0; i < sub_roles.length; i++)
            if(isSubOrEqual(sub_roles[i], super_role)) return true;
        return false;
    }
    
    /**
     * В методе осуществляется проверка содержится ли среди заданных ролей обратная надроль.
     * @param sub_role Индекс предполагаемой обратной подроли.
     * @param super_role Индекс предполагаемой надроли.
     * @return Возвращает истина, обратная к заданной первой роли является подролью второй заданной роли.
     */
    public boolean isReverseSubOrEqual(int sub_role, int super_role) { //sub or equal reverse role
        for(int it: roles[sub_role].getInvRoles()) {
            if (roles[Math.abs(super_role)].getSubRoles().contains(it) || 
                roles[Math.abs(super_role)].getEqvRoles().contains(it) || it == super_role) return true;
        }
        return false;
    }
    
    /**
     * В методе осуществляется проверка содержится ли среди заданных ролей обратная надроль.
     * @param sub_roles Массив ролей.
     * @param super_role Индекс роли в базе знаний.
     * @return Возвращает истина, если среди ролей массива найдется такая роль, обратная к которой является подролью заданной.
     */
    public boolean isReverseSubOrEqualAll(int[] sub_roles, int super_role) {
        for(int i = 0; i < sub_roles.length; i++)
            if(isReverseSubOrEqual(sub_roles[i], super_role)) return true;
        return false;
    }
    
    /**
     * Метод осуществляет проверку того, содержатся ли в двух заданных массивах ролей две непересекающиеся.
     * @param ar1 Первый массив ролей.
     * @param ar2 Второй массив ролей.
     * @return Возвращает истина, если в первом массиве существует роль непересекающаяся с ролью из второго массива.
     */
    public boolean haveDisjoint(int[] ar1, int[] ar2) {
        for(int i = 0; i < ar1.length; i++)
            for(int j = 0; j < ar2.length; j++)
                if(isDisjoint(ar1[i], ar2[j])) return true;
        return false;
    }
    
    /**
     * Метод возвращает массив номеров ролей, являющихся подролями заданной роли.
     * @param r Номер роли.
     * @return Возвращает массив индексов ролей, являющихся подролями заданной роли.
     */
    public Integer[] getSubAndEqvRoles(int r)
    {
        Integer[] ret = new Integer[roles[r].getSubRoles().size()];
        roles[r].getSubRoles().toArray(ret);
        return ret;
    }
    
}