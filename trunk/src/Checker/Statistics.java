package Checker;

/**
 * Класс хранит свойства и методы хранения, обновления и вывода информации о 
 * количестве выполняемых операций по раскрытию правил.
 * @author Andrey Grigoryev
 */
class Statistics {
    
    private int countOfAnd = 0;
    private int countOfIndivids = 0;
    private int countOfOr = 0;
    private int countOfAll = 0;
    private int countOfSome = 0;
    private int countOfMin = 0;
    private int countOfMax = 0;
    private int countOfExact = 0;
    private int countOfChoose = 0;
    private int countOfNominal = 0;
    private int countOfConcepts = 0;
    private int restoreCount = 0;
    
    /**
     * Метод обнуляет все переменные отвечающие за количество выполняемых операций.
     */
    public void clear()
    {
        countOfIndivids = 0;
        countOfAnd = 0;
        countOfOr = 0;
        countOfAll = 0;
        countOfSome = 0;
        countOfMin = 0;
        //countOfMax = 0;
        countOfExact = 0;
        countOfChoose = 0;
        countOfNominal = 0;
        countOfConcepts = 0;
        //restoreCount = 0;
    }
    
    /**
     * Основной и единственный конструктор класса.
     */
    public Statistics() { }
    
    /**
     * Метод увеличивает количество операций при добавлении концептов
     */
    public void conceptAdd() {
        countOfConcepts++;
    }
    
    /**
     * Метод увеличивает количество операций при раскрытии AND-правила
     */
    public void andAdd() {
        countOfAnd++;
    }
    
    /**
     * Метод увеличивает количество операций при раскрытии OR-правила
     */
    public void orAdd() {
        countOfOr++;
    }
    
    /**
     * Метод увеличивает количество операций при раскрытии FORALL-правила
     */
    public void allAdd() {
        countOfAll++;
    }
    
    /**
     * Метод увеличивает количество операций при раскрытии EXISTS-правила
     */
    public void someAdd() {
        countOfSome++;
    }
    
    /**
     * Метод увеличивает количество операций при раскрытии GE-правила
     */
    public void minAdd() {
        countOfMin++;
    }
    
    /**
     * Метод увеличивает количество операций при раскрытии LE-правила
     */
    public void maxAdd() {
        countOfMax++;
    }
    
    /**
     * Метод увеличивает количество операций при раскрытии =-правила
     */
    public void exactAdd() {
        countOfExact++;
    }
    
    /**
     * Метод увеличивает количество операций при осуществлении выбора при раскрытии LE-правила
     */
    public void chooseAdd() {
        countOfChoose++;
    }
    
    /**
     * Метод увеличивает количество операций при добавлении индивида
     */
    public void individAdd() {
        countOfIndivids++;
    }
    
    /**
     * Метод увеличивает количество операций при возврате назад
     */
    public void restoreCountAdd() {
        restoreCount++;
    }
        
    /**
     * Метод осуществляет вывод информации о количестве выполняемых операций в виде таблицы.
     */
    public void printStats()
    {
        System.out.printf("+=================+==========+\n");
        System.out.printf("|    Operation    | count    |\n");
        System.out.printf("+=================+==========+\n");
        System.out.printf("|AND:             | %8d |\n", countOfAnd);
        System.out.printf("+=================+==========+\n");
        System.out.printf("|OR:              | %8d |\n", countOfOr);
        System.out.printf("+=================+==========+\n");
        System.out.printf("|ALL:             | %8d |\n", countOfAll);
        System.out.printf("+=================+==========+\n");
        System.out.printf("|SOME:            | %8d |\n", countOfSome);
        System.out.printf("+=================+==========+\n");
        System.out.printf("|MIN:             | %8d |\n", countOfMin);
        System.out.printf("+=================+==========+\n");
        System.out.printf("|MAX:             | %8d |\n", countOfMax);
        System.out.printf("+=================+==========+\n");
        System.out.printf("|CONCEPTS:        | %8d |\n", countOfConcepts);
        System.out.printf("+=================+==========+\n");
        System.out.printf("|INDIVIDS:        | %8d |\n", countOfIndivids);
        System.out.printf("+=================+==========+\n");
        System.out.printf("|RESTORES:        | %8d |\n", restoreCount);
        System.out.printf("+=================+==========+\n");
    }
    
}
