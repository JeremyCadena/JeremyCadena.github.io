package com.example.shoppingassistant.models;

public class BusS {
    private String buslId;
    private String bussId;
    private String bussMainStreet;
    private String bussName;
    private int bussNode;
    private String bussSecondaryStreet;
    private String bussSense;

    public BusS() {
    }

    public BusS(String buslId, String bussId, String bussMainStreet, String bussName, int bussNode, String bussSecondaryStreet, String bussSense) {
        this.buslId = buslId;
        this.bussId = bussId;
        this.bussMainStreet = bussMainStreet;
        this.bussName = bussName;
        this.bussNode = bussNode;
        this.bussSecondaryStreet = bussSecondaryStreet;
        this.bussSense = bussSense;
    }

    public String getBuslId() {
        return buslId;
    }

    public String getBussId() {
        return bussId;
    }

    public String getBussMainStreet() {
        return bussMainStreet;
    }

    public String getBussName() {
        return bussName;
    }

    public int getBussNode() {
        return bussNode;
    }

    public String getBussSecondaryStreet() {
        return bussSecondaryStreet;
    }

    public String getBussSense() {
        return bussSense;
    }

    @Override
    public String toString() {
        return "BusS{" +
                "buslId='" + buslId + '\'' +
                ", bussId='" + bussId + '\'' +
                ", bussMainStreet='" + bussMainStreet + '\'' +
                ", bussName='" + bussName + '\'' +
                ", bussNode=" + bussNode +
                ", bussSecondaryStreet='" + bussSecondaryStreet + '\'' +
                ", bussSense='" + bussSense + '\'' +
                '}';
    }

    public void setBuslId(String buslId) {
        this.buslId = buslId;
    }

    public void setBussId(String bussId) {
        this.bussId = bussId;
    }

    public void setBussMainStreet(String bussMainStreet) {
        this.bussMainStreet = bussMainStreet;
    }

    public void setBussName(String bussName) {
        this.bussName = bussName;
    }

    public void setBussNode(int bussNode) {
        this.bussNode = bussNode;
    }

    public void setBussSecondaryStreet(String bussSecondaryStreet) {
        this.bussSecondaryStreet = bussSecondaryStreet;
    }

    public void setBussSense(String bussSense) {
        this.bussSense = bussSense;
    }
}
