package DesignPatterns.Duck;

public class FlyWithWings implements FlyBehavior{
    @Override
    public void Fly() {
        System.out.println("Fly With Wings");
    }
}
