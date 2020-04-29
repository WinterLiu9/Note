package DesignPatterns.Beverage;


public class WinterCoffee {
    public static void main(String[] args) {
        Beverage nespresso = new Nespresso();
        System.out.println(nespresso.getDescription() + " " + nespresso.cost());

        nespresso = new Mocha(nespresso);
        System.out.println(nespresso.getDescription() + " " + nespresso.cost());

        nespresso = new Mocha(nespresso);
        System.out.println(nespresso.getDescription() + " " + nespresso.cost());

        nespresso = new Milk(nespresso);
        System.out.println(nespresso.getDescription() + " " + nespresso.cost());

    }
}
