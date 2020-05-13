package DesignPatterns.Weather;

import java.util.ArrayList;

public class WeatherData implements Subject {

    private ArrayList<Observer> list;
    private float temp;
    private float humidity;
    private float pressure;

    public WeatherData(){
        list = new ArrayList<>();
    }

    @Override
    public void registerObserver(Observer o) {
        list.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        int index = list.indexOf(o);
        if (index >= 0)
            list.remove(index);
    }

    @Override
    public void notifyObserver() {
        for (Observer o : list) {
            o.update(temp,humidity,pressure);
        }
    }

    public void measurementsChanged(){
        notifyObserver();
    }
    public void setMeasurements(float temp, float humidity, float pressure) {
        this.temp = temp;
        this.humidity = humidity;
        this.pressure = pressure;
        measurementsChanged();
    }
}
