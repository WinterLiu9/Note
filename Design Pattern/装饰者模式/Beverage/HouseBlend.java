package DesignPatterns.Beverage;

public class HouseBlend extends Beverage {

    public HouseBlend(String description){
        this.description = description;
    }
    @Override
    public double cost() {
        return 0.89;
    }
}
