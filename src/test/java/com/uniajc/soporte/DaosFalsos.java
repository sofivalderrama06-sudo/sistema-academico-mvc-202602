package com.uniajc.soporte;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.uniajc.dao.DocenteDao;
import com.uniajc.dao.EstudianteDao;
import com.uniajc.dao.GrupoDao;
import com.uniajc.dao.MateriaDao;
import com.uniajc.dao.UsuarioDao;
import com.uniajc.modelo.Docente;
import com.uniajc.modelo.Estudiante;
import com.uniajc.modelo.Grupo;
import com.uniajc.modelo.Materia;
import com.uniajc.modelo.Usuario;

/**
 * DOBLES DE PRUEBA: DaosFalsos 
 * 
 * ¿Por qué existe esta clase?
 * 1. Aislamiento de Pruebas: Permite probar la lógica de los SERVICIOS sin
 *    conectarse a la base de datos real (PostgreSQL / Neon).
 * 2. Velocidad: Al operar sobre listas en memoria RAM (ArrayList), las pruebas
 *    se ejecutan en milisegundos y funcionan sin conexión a internet.
 * 3. Polimorfismo: Cada clase falsa hereda ("extends") del DAO original. Por
 *    ende, los Servicios pueden recibir un DAO falso sin notar la diferencia.
 * 4. Espías y Contadores: Las variables públicas (guardados, actualizados,
 *    eliminados) permiten verificar mediante 'assertEquals' que el servicio
 *    realmente invocó la persistencia.
 * 5. Fluent Interface: El método '.con(...)' permite precargar datos de prueba
 *    en una sola línea encadenada antes de ejecutar el test.
 */
public final class DaosFalsos {
    private DaosFalsos() {
    }

    // DAO FALSO: DOCENTES
    
    public static class DocenteDaoFalso extends DocenteDao {
        public int guardados;
        public int actualizados;
        public int eliminados;
        public final List<Docente> datos = new ArrayList<>(); // Almacén en RAM

        /**
         * Permite precargar docentes de prueba encadenando llamadas:
         * dao.con(1, "Ana", "Física", "ana@uni.edu.co").con(2, ...);
         */
        public DocenteDaoFalso con(int id, String nombre, String especialidad, String email) {
            datos.add(new Docente(id, nombre, especialidad, email));
            return this;
        }

        @Override public void guardar(Docente docente) {
            datos.add(docente);
            guardados++;
        }
        @Override public List<Docente> obtenerTodos() { return new ArrayList<>(datos); }
        @Override public Optional<Docente> buscarPorId(int id) {
            return datos.stream().filter(d -> d.getId() == id).findFirst();
        }
        @Override public Optional<Docente> buscarPorEmail(String email) {
            return datos.stream()
                    .filter(d -> d.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }
        @Override public void actualizar(Docente docente) {
            datos.removeIf(d -> d.getId() == docente.getId());
            datos.add(docente);
            actualizados++;
        }
        @Override public void eliminar(int id) {
            datos.removeIf(d -> d.getId() == id);
            eliminados++;
        }
    }

    // DAO FALSO: ESTUDIANTES
    public static class EstudianteDaoFalso extends EstudianteDao {
        public int guardados;
        public int actualizados;
        public int eliminados;
        public final List<Estudiante> datos = new ArrayList<>(); // Almacén en RAM

        public EstudianteDaoFalso con(int id, String nombre, String apellido, String email) {
            datos.add(new Estudiante(id, nombre, apellido, email));
            return this;
        }

        @Override public void guardar(Estudiante estudiante) {
            datos.add(estudiante);
            guardados++;
        }
        @Override public List<Estudiante> obtenerTodos() { return new ArrayList<>(datos); }
        @Override public Optional<Estudiante> buscarPorId(int id) {
            return datos.stream().filter(e -> e.getId() == id).findFirst();
        }
        @Override public Optional<Estudiante> buscarPorEmail(String email) {
            return datos.stream()
                    .filter(e -> e.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }
        @Override public void actualizar(Estudiante estudiante) {
            datos.removeIf(e -> e.getId() == estudiante.getId());
            datos.add(estudiante);
            actualizados++;
        }
        @Override public void eliminar(int id) {
            datos.removeIf(e -> e.getId() == id);
            eliminados++;
        }
    }

    // DAO FALSO: MATERIAS

    public static class MateriaDaoFalso extends MateriaDao {
        public int guardados;
        public int actualizados;
        public int eliminados;
        public final List<Materia> datos = new ArrayList<>(); // Almacén en RAM

        public MateriaDaoFalso con(int id, String nombre, int creditos) {
            datos.add(new Materia(id, nombre, creditos));
            return this;
        }
        @Override public void guardar(Materia materia) {
            datos.add(materia); guardados++;
        }
        @Override public List<Materia> obtenerTodos() { return new ArrayList<>(datos); }
        @Override public Optional<Materia> buscarPorId(int id) {
            return datos.stream().filter(m -> m.getId() == id).findFirst();
        }
        @Override public Optional<Materia> buscarPorCampo(String campo) {
            return datos.stream().filter(m -> m.getName().equalsIgnoreCase(campo)).findFirst();
        }
        @Override public void actualizar(Materia materia) {
            datos.removeIf(m -> m.getId() == materia.getId());
            datos.add(materia);
            actualizados++;
        }
        @Override public void eliminar(int id) {
            datos.removeIf(m -> m.getId() == id);
            eliminados++;
        }
    }

    // DAO FALSO: GRUPOS
    public static class GrupoDaoFalso extends GrupoDao {
        public int guardados;
        public int actualizados;
        public int eliminados;
        public final List<Grupo> datos = new ArrayList<>(); // Almacén en RAM

        public GrupoDaoFalso con(int id, int materia, int docente, String aula, String horario) {
            datos.add(new Grupo(id, materia, docente, aula, horario, null));
            return this;
        }
        @Override public void guardar(Grupo grupo) {
            datos.add(grupo); guardados++;
        }
        @Override public List<Grupo> obtenerTodos() { return new ArrayList<>(datos); }
        @Override public Optional<Grupo> buscarPorId(int id) {
            return datos.stream().filter(g -> g.getId() == id).findFirst();
        }
        @Override public Optional<Grupo> buscarPorCampo(String campo) {
            return datos.stream().filter(g -> g.getClassroom().equalsIgnoreCase(campo)
                    || g.getSchedule().equalsIgnoreCase(campo)).findFirst();
        }
        @Override public void actualizar(Grupo grupo) {
            datos.removeIf(g -> g.getId() == grupo.getId());
            datos.add(grupo);
            actualizados++;
        }
        @Override public void eliminar(int id) {
            datos.removeIf(g -> g.getId() == id);
            eliminados++;
        }
    }

    // DAO FALSO: USUARIOS (Soporta validaciones de BCrypt y login)
    public static class UsuarioDaoFalso extends UsuarioDao {
        public int llamadas;
        public String usuarioRecibido;
        public String claveRecibida;
        public int guardados;
        public int actualizados;
        public int eliminados;
        public String ultimoHashGuardado;
        public final List<Usuario> datos = new ArrayList<>(); // Almacén en RAM

        public UsuarioDaoFalso() {
        }

        public UsuarioDaoFalso(Usuario usuario) {
            if (usuario != null) {
                datos.add(usuario);
            }
        }

        @Override
        public Optional<Usuario> autenticar(String username, String password) {
            llamadas++;
            usuarioRecibido = username;
            claveRecibida = password;
            return datos.stream()
                    .filter(u -> u.getUsername().equals(username))
                    .findFirst();
        }

        @Override
        public List<Usuario> obtenerTodos() {
            return new ArrayList<>(datos);
        }

        @Override
        public Optional<Usuario> buscarPorId(int id) {
            return datos.stream()
                    .filter(u -> u.getId().equals(String.valueOf(id)))
                    .findFirst();
        }

        @Override
        public void guardar(Usuario usuario, String passwordHasheado) {
            guardados++;
            ultimoHashGuardado = passwordHasheado;
            datos.add(usuario);
        }

        @Override
        public void actualizar(Usuario usuario, String nuevaPasswordHasheada) {
            actualizados++;
            if (nuevaPasswordHasheada != null) {
                ultimoHashGuardado = nuevaPasswordHasheada;
            }
            datos.removeIf(u -> u.getId().equals(usuario.getId()));
            datos.add(usuario);
        }

        @Override
        public void eliminar(int id) {
            eliminados++;
            datos.removeIf(u -> u.getId().equals(String.valueOf(id)));
        }
    }
}
