package DesignPatterns.Pizza;

public class NYPizzaStore extends PizzaStore {

    @Override
    protected Pizza createPizza(String type) {
        if ("Cheese".equals(type))
            return new NYStyleCheesePizza();
        else
            return null;
    }
}
