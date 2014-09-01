package KnowledgeBase;

import Enums.RoleChar;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Класс содержит поля и методы обеспечивающие доступ к ним для хранения свойств
 * отношений.
 * @author Andrey Grigoryev
 */
public class SimpleRole {
    
    private int range = 1; //TOP
    private int domain = 1; //TOP
    private String name;
    private int characteristics = 0; //have no characteristics
    private boolean is_data_role = false;
    private int id = -1;
    
    private HashSet<Integer> super_roles = new HashSet<Integer>();
    private HashSet<Integer> sub_roles = new HashSet<Integer>();
    private HashSet<Integer> eqv_roles = new HashSet<Integer>();
    private HashSet<Integer> dsj_roles = new HashSet<Integer>();
    private HashSet<Integer> inv_roles = new HashSet<Integer>();
    private ArrayList<ArrayList<Integer>> sub_chains = new ArrayList<ArrayList<Integer>>();
    
    //0 bit - Functional
    //1 bit - InverseFunctional
    //2 bit - Transitive
    //3 bit - Symmetric
    //4 bit - Asymmetric
    //5 bit - Reflexive
    //6 bit - Irreflexive
    
    /**
     * Возвращает все цепи отношений, подмножеством которых является данная роль.
     * @return Массив всех цепей.
     */
    public ArrayList<ArrayList<Integer>> getChains() {
        return sub_chains;
    }
    
    /**
     * Основной и единственный конструктор класса SimpleRole, который задает имя роли.
     * @param S Имя новой роли.
     */
    public SimpleRole(String S) {
        name = S;
    }    
    
    /**
     * Возвращает множество всех подролей.
     * @return Множество всех подролей данной роли.
     */
    public HashSet<Integer> getSubRoles() {
        return sub_roles;
    }
    
    /**
     * Возвращает множество всех надролей.
     * @return Множество всех надролей данной роли.
     */
    public HashSet<Integer> getSuperRoles() {
        return super_roles;
    }
    
    /**
     * Возвращает множество всех ролей эквивалентных данной.
     * @return Множество всех эквивалентных ролей.
     */
    public HashSet<Integer> getEqvRoles() {
        return eqv_roles;
    }
    
    /**
     * Возвращает множество всех ролей непересекающихся с данной.
     * @return Множество всех ролей непересекающихся с данной.
     */
    public HashSet<Integer> getDsjRoles() {
        return dsj_roles;
    }
    
    /**
     * Возвращает множество всех ролей обратных данной.
     * @return Множество всех эквивалентных ролей.
     */
    public HashSet<Integer> getInvRoles() {
        return inv_roles;
    }
        
    /**
     * Позволяет установить область значений отношения.
     * @param new_range Новая область значений отношения.
     */
    public void setRange(int new_range) {
        range = new_range;
    }
    
    /**
     * Позволяет получить область значений отношения.
     * @return Область значений отношения.
     */
    public int getRange() {
        return range;
    }
    
    /**
     * Позволяет установить домен отношения.
     * @param new_domain Новый домен отношения.
     */
    public void setDomain(int new_domain) {
        domain = new_domain;
    }
    
    /**
     * Позволяет получить домен отношения.
     * @return Домен отношения.
     */
    public int getDomain() {
        return domain;
    }
    
    /**
     * Метод добавляет новую цепь, подмножеством которой является текущая роль.
     * @param new_sub_chain Новая цепь.
     */
    public void addSubChain(ArrayList<Integer> new_sub_chain) {
        sub_chains.add(new_sub_chain);
    }
    
    /**
     * Позволяет добавлить новую роль обратную к текущей.
     * @param inverse_role_id Идентификатор обратной роли.
     */
    public void addInverseRoleIndex(int inverse_role_id) {
        inv_roles.add(inverse_role_id);
    }
    
    /**
     * Позволяет добавить характеристику к текущей роли.
     * @param new_characteristic Новая характеристика текущей роли.
     */
    public void setCharacteristic(RoleChar new_characteristic) {
        switch (new_characteristic) {
            case drFUNC: characteristics |= 1; break;
            case drINVFUNC: characteristics |= (1 << 1); break;
            case drTRANS: characteristics |= (1 << 2); break;
            case drSYMM: characteristics |= (1 << 3); break;
            case drASYMM: characteristics |= (1 << 4); break;
            case drREF: characteristics |= (1 << 5); break;
            case drIRREF: characteristics |= (1 << 6); break;
        }
    }
    
    /**
     * Определяет является ли роль функциональной.
     * @return Возвращает истина, если роль является функциональной и ложь в противном случае.
     */
    public boolean isFunctional() {
        return (characteristics & 1) > 0;
    }
    
    /**
     * Определяет является ли роль обратно-функциональной.
     * @return Возвращает истина, если роль является обратно-функциональной и ложь в противном случае.
     */
    public boolean isInverseFunctional() {
        return (characteristics & (1 << 1)) > 0;
    }
    
    /**
     * Определяет является ли роль транзитивной.
     * @return Возвращает истина, если роль является транзитивной и ложь в противном случае.
     */
    public boolean isTransitive() {
        return (characteristics & (1 << 2)) > 0;
    }
    
    /**
     * Определяет является ли роль симметричной.
     * @return Возвращает истина, если роль является симметричной и ложь в противном случае.
     */
    public boolean isSymmetric() {
        return (characteristics & (1 << 3)) > 0;
    }
    
    /**
     * Определяет является ли роль ассиметричной.
     * @return Возвращает истина, если роль является ассиметричной и ложь в противном случае.
     */
    public boolean isAsymmetric() {
        return (characteristics & (1 << 4)) > 0;
    }
    
    /**
     * Определяет является ли роль рефлексивной.
     * @return Возвращает истина, если роль является рефлексивной и ложь в противном случае.
     */
    public boolean isReflexive() {
        return (characteristics & (1 << 5)) > 0;
    }
    
    /**
     * Определяет является ли роль иррефлексивной.
     * @return Возвращает истина, если роль является иррефлексивной и ложь в противном случае.
     */
    public boolean isIrreflexive() {
        return (characteristics & (1 << 6)) > 0;
    }
    
    /**
     * Возвращает имя роли.
     * @return Имя роли.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Устанавливает новое имя роли.
     * @param S Новое имя роли.
     */
    public void setName(String S) {
        name = S;
    }
    
    /**
     * Устаналивает свойство, соответствующее тому, что роль является ролью с типом данных.
     */
    public void setDataRole() {
        is_data_role = true;
    }
    
    /**
     * Определяет связывает ли роль индивида с типом данных.
     * @return Возвращает истина, если роль связывает индивида с типом данных и ложь в противном случае.
     */
    public boolean isDataRole() {
        return is_data_role;
    }
    
}
