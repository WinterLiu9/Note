package DesignPatterns.Beverage;

public class Nespresso extends Beverage {


    public Nespresso(){
        this.description = "Nespresso";
    }

    @Override
    public double cost() {
        return 1.99;
    }

}
