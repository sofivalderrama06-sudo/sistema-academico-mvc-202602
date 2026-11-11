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

     @Override
     public String toString() {
        return "Materia [name=" + name + "]";
     }




}


