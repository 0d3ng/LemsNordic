package com.sinaungoding.lemsnordic.api;

public class Tenki {
    private String date_pollen;
    private String pollen;
    private String weather;
    private int temperature_high;
    private int temperature_low;
    private int precipitation;

    public Tenki() {
    }

    public Tenki(int precipitation, int temperature_low, int temperature_high, String weather, String pollen, String date_pollen) {
        this.precipitation = precipitation;
        this.temperature_low = temperature_low;
        this.temperature_high = temperature_high;
        this.weather = weather;
        this.pollen = pollen;
        this.date_pollen = date_pollen;
    }

    public String getDate_pollen() {
        return date_pollen;
    }

    public void setDate_pollen(String date_pollen) {
        this.date_pollen = date_pollen;
    }

    public String getPollen() {
        return pollen;
    }

    public void setPollen(String pollen) {
        this.pollen = pollen;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getTemperature_high() {
        return temperature_high;
    }

    public void setTemperature_high(int temperature_high) {
        this.temperature_high = temperature_high;
    }

    public int getTemperature_low() {
        return temperature_low;
    }

    public void setTemperature_low(int temperature_low) {
        this.temperature_low = temperature_low;
    }

    public int getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(int precipitation) {
        this.precipitation = precipitation;
    }

    @Override
    public String toString() {
        return "Tenki{" +
                "date_pollen='" + date_pollen + '\'' +
                ", pollen='" + pollen + '\'' +
                ", weather='" + weather + '\'' +
                ", temperature_high=" + temperature_high +
                ", temperature_low=" + temperature_low +
                ", precipitation=" + precipitation +
                '}';
    }
}
