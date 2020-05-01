package DesignPatterns.Pizza;

public class PizzaTestDrive {
    public static void main(String[] args) {
        PizzaStore nyPizzaStore = new NYPizzaStore();
        Pizza pizza = nyPizzaStore.orderPizza("Cheese");
        System.out.println("Winter ordered a " + pizza.getName());
    }
}
