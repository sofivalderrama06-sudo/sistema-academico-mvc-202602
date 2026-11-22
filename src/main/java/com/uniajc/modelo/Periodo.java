package com.uniajc.modelo;

import java.sql.Timestamp;

public class Periodo {
    private int id;
    private String name;
    private Timestamp startDate;
    private Timestamp endDate;
    private boolean state;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Timestamp getStartDate() {
        return startDate;
    }
    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }
    public Timestamp getEndDate() {
        return endDate;
    }
    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }
    public boolean isState() {
        return state;
    }
    public void setState(boolean state) {
        this.state = state;
    }
    @Override
    public String toString() {
        return "Periodo [name=" + name + "]";
    }

    
}
