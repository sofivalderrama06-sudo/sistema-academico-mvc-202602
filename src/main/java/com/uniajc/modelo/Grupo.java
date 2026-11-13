package com.uniajc.modelo;

import java.sql.Timestamp;

public class Grupo {
    private int id;
    private int id_materia;
    private int id_docente;
    private String classroom;
    private String schedule;
    private Timestamp created_at;

    // Constructor
    public Grupo(int id_materia, int id_docente, String classroom, String schedule, Timestamp created_at) {
        this.id_materia = id_materia;
        this.id_docente = id_docente;
        this.classroom = classroom;
        this.schedule = schedule;
        this.created_at = created_at;
    }

    // Constructor usado al reconstruir desde la base de datos o actualizar por ID.
    public Grupo(int id, int id_materia, int id_docente, String classroom, String schedule, Timestamp created_at) {
        this.id = id;
        this.id_materia = id_materia;
        this.id_docente = id_docente;
        this.classroom = classroom;
        this.schedule = schedule;
        this.created_at = created_at;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_materia() {
        return id_materia;
    }

    public void setId_materia(int id_materia) {
        this.id_materia = id_materia;
    }

    public int getId_docente() {
        return id_docente;
    }

    public void setId_docente(int id_docente) {
        this.id_docente = id_docente;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }
}
