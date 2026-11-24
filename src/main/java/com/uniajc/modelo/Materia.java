package com.uniajc.modelo;

import java.sql.Timestamp;

public class Materia {
    
   private int id;
   private String name;
   private int credits;
   private Timestamp created_at;

    public Materia(int id, String name, int credits, Timestamp created_at) {
          this.id = id;
          this.name = name;
          this.credits = credits;
          this.created_at = created_at;
     }
    // Constructor sobrecargado sin timestamp (usado en registros y pruebas unitarias).
    
    public Materia(int id, String name, int credits) {
        this(id, name, credits, null);
    }
     public int getId() {
          return id;
     }
    
     public String getName() {
          return name;
     }
    
     public int getCredits() {
          return credits;
     }
    
     public Timestamp getCreatedAt() {
          return created_at;
     }

     public void setId(int id) {
        this.id = id;
    }

     public void setName(String name) {
         this.name = name;
     }

     public void setCredits(int credits) {
         this.credits = credits;
     }

     public void setCreated_at(Timestamp created_at) {
         this.created_at = created_at;
     }

     @Override
     public String toString() {
        return "Materia [name=" + name + "]";
     }




}


