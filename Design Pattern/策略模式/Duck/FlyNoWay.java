package DesignPatterns.Duck;

public class FlyNoWay implements FlyBehavior {
    @Override
    public void Fly() {
        System.out.println("Fly No Way");
    }
}
