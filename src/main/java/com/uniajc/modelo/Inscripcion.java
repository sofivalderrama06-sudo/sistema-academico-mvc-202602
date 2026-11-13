package com.uniajc.modelo;

import java.sql.Timestamp;

public class Inscripcion {
    private int id;
    private int id_estudiante;
    private int id_grupo;
    private double final_grade;
    private String status;
    private Timestamp created_at;

    /** Crea una inscripción vacía para completar sus datos mediante setters. */
    public Inscripcion() {
    }

    /** Crea una inscripción nueva con estudiante, grupo, nota y estado. */
    public Inscripcion(int id_estudiante, int id_grupo, double final_grade, String status) {
        this.id_estudiante = id_estudiante;
        this.id_grupo = id_grupo;
        this.final_grade = final_grade;
        this.status = status;
    }

    /** Reconstruye una inscripción con todos sus datos, incluidos los de la base de datos. */
    public Inscripcion(int id, int id_estudiante, int id_grupo, double final_grade, String status, Timestamp created_at) {
        this.id = id;
        this.id_estudiante = id_estudiante;
        this.id_grupo = id_grupo;
        this.final_grade = final_grade;
        this.status = status;
        this.created_at = created_at;
    }

    /** Devuelve el identificador de la inscripción. */
    public int getId() {
        return id;
    }

    /** Actualiza el identificador de la inscripción. */
    public void setId(int id) {
        this.id = id;
    }

    /** Devuelve el identificador del estudiante asociado. */
    public int getId_estudiante() {
        return id_estudiante;
    }

    /** Actualiza el identificador del estudiante asociado. */
    public void setId_estudiante(int id_estudiante) {
        this.id_estudiante = id_estudiante;
    }

    /** Devuelve el identificador del grupo asociado. */
    public int getId_grupo() {
        return id_grupo;
    }

    /** Actualiza el identificador del grupo asociado. */
    public void setId_grupo(int id_grupo) {
        this.id_grupo = id_grupo;
    }

    /** Devuelve la nota final de la inscripción. */
    public double getFinal_grade() {
        return final_grade;
    }

    /** Actualiza la nota final de la inscripción. */
    public void setFinal_grade(double final_grade) {
        this.final_grade = final_grade;
    }

    /** Devuelve el estado actual de la inscripción. */
    public String getStatus() {
        return status;
    }

    /** Actualiza el estado de la inscripción. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Devuelve la fecha de creación registrada en la base de datos. */
    public Timestamp getCreated_at() {
        return created_at;
    }

    /** Actualiza la fecha de creación de la inscripción. */
    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    /** Devuelve una representación breve para identificar la inscripción. */
    @Override
    public String toString() {
        return "Inscripcion #" + id + " | Estudiante " + id_estudiante + " | Grupo " + id_grupo;
    }
}
