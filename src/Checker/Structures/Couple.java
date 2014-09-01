package Checker.Structures;

import Checker.Model.InterpretationNode;

/**
 * Данный класс осуществляет хранение предка или потомка некоторого индивида интерпретации.
 * Каждый индивид может быть связан с потомком (соответственно и с предком) по средством
 * нескольких ролей. Для того, чтобы обеспечить не разрывность ролей и индивида с которым
 * они связаны, используется данный класс.
 * @author Andrey Grigoryev
 */
public final class Couple {
    private InterpretationNode node; //index of child
    private int[] roles = new int[4]; //index of role
    private int currentSize = 0;
    private int countOfRoles = 0;
            
    /**
     * Конструктор класса, который определяет новый объект, поля которого соответствуют 
     * вершине-индивиду интерпретации и ролям связывющего этого индивида с предком.
     * @param new_node Ссылка на вершину-индивида
     * @param new_role_index Ссылка на ArrayList ролей, которые связывают индивида
     * с предком.
     */
    public Couple(InterpretationNode new_node, int new_role_index) {
        node = new_node;
        roles[countOfRoles++] = new_role_index;
    }
    
    /**
     * Метод осуществляет добавление новой роли к потомку (или предку).
     * Это может происходить при слиянии нескольких индивидов.
     * @param new_role Индекс новой роли смежной вершины.
     */
    public void addNewRole(int new_role) {
        if(currentSize == countOfRoles) {
            int[] temp = roles;
            currentSize = currentSize * 3 / 2;
            roles = new int[currentSize];
            for(int i = 0; i < countOfRoles; i++) {
                roles[i] = temp[i];
            }
            roles[currentSize++] = new_role;
        }
    }
    
    /**
     * Метод осуществляет проверку того, содержится ли заданная роль в массиве ролей.
     * @param role_index Индекс заданной роли.
     * @return Возвращает истина, если роль содержится в массиве ролей и ложь в противном случае.
     */
    public boolean contains(int role_index) {
        for(int i = 0; i < countOfRoles; i++) {
            if(roles[i] == role_index) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Метод осуществляет проверку того, содержатся ли все заданные роли в массиве ролей.
     * @param an_roles Массив ролей.
     * @return Возвращает истина, если все роли содержатся в массиве ролей и ложь в противном случае.
     */
    public boolean containsAll(int[] an_roles) {
        for(int i = 0; i < an_roles.length; i++) {
            if(!contains(an_roles[i])) return false;
        }
        return true;
    }
    
    /**
     * Создает новый объект Couple, на основе другого объекта этого типа, копируя все его поля.
     * @param c Объект на основе которого создается текущий объект.
     */
    public Couple(Couple c) {
        node = c.getNode();

        currentSize = c.getRoles().length;
        roles = new int[currentSize];
        for(int i = 0; i < currentSize; i++)
            roles[i] = c.getRoles()[i];
    }

    /**
     * Возвращает ссылку на индивида интерпретации.
     * @return Ссылка на индивида.
     */
    public InterpretationNode getNode() {
        return node;
    }
    
    /**
     * Возвращает ссылку на роли связывающие текущего индивида с предком.
     * @return Массив ролей связывающих текущего индивида с предком.
     */
    public int[] getRoles() {
        int[] temp = new int[countOfRoles];
        for(int i = 0; i < countOfRoles; i++)
            temp[i] = roles[i];
        return temp;
    }
    
}