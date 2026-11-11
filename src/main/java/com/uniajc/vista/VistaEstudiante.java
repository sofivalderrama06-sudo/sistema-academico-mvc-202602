package com.uniajc.vista;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
 
import com.uniajc.controlador.ControladorEstudiante;
import com.uniajc.modelo.Estudiante;

public class VistaEstudiante {

    private final ControladorEstudiante controlador = new ControladorEstudiante();
    private final Scanner scanner = new Scanner(System.in);
 
    public void mostrarMenu() {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== GESTION DE ESTUDIANTES ===");
            System.out.println("1. Registrar estudiante");
            System.out.println("2. Listar estudiantes");
            System.out.println("3. Buscar por ID");
            System.out.println("4. Actualizar estudiante");
            System.out.println("5. Eliminar estudiante");
            System.out.println("0. Salir");
            opcion = leerEntero("Opcion: ");
 
            switch (opcion) {
                case 1:
                    registrar();
                    break;
                case 2:
                    listar();
                    break;
                case 3:
                    buscarPorId();
                    break;
                case 4:
                    actualizar();
                    break;
                case 5:
                    eliminar();
                    break;
                case 0:
                    System.out.println("Hasta luego.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }
 
    private void registrar() {
        System.out.println("--- Registrar estudiante ---");
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String email = leerTexto("Correo: ");
        try {
            controlador.registrar(nombre, apellido, email);
            System.out.println("Estudiante registrado.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Validacion: " + ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
 
    private void listar() {
        System.out.println("--- Lista de estudiantes ---");
        try {
            List<Estudiante> estudiantes = controlador.listar();
            if (estudiantes.isEmpty()) {
                System.out.println("No hay estudiantes registrados.");
            }
            for (Estudiante e : estudiantes) {
                System.out.println(e);
            }
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
 
    private void buscarPorId() {
        int id = leerEntero("ID a buscar: ");
        try {
            Optional<Estudiante> estudiante = controlador.buscarPorId(id);
            if (estudiante.isPresent()) {
                System.out.println(estudiante.get());
            } else {
                System.out.println("No existe un estudiante con ese ID.");
            }
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
 
    private void actualizar() {
        int id = leerEntero("ID a actualizar: ");
        try {
            Optional<Estudiante> actual = controlador.buscarPorId(id);
            if (!actual.isPresent()) {
                System.out.println("No existe un estudiante con ese ID.");
                return;
            }
            Estudiante e = actual.get();
            System.out.println("Actual: " + e);
            System.out.println("(Pulsa Enter para dejar el valor actual)");
            String nombre = leerTextoOpcional("Nombre [" + e.getNombre() + "]: ", e.getNombre());
            String apellido = leerTextoOpcional("Apellido [" + e.getApellido() + "]: ", e.getApellido());
            String email = leerTextoOpcional("Correo [" + e.getEmail() + "]: ", e.getEmail());
            controlador.actualizar(id, nombre, apellido, email);
            System.out.println("Estudiante actualizado.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Validacion: " + ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
 
    private void eliminar() {
        int id = leerEntero("ID a eliminar: ");
        String confirmacion = leerTexto("Seguro que quieres eliminarlo? (s/n): ");
        if (!confirmacion.equalsIgnoreCase("s")) {
            System.out.println("Operacion cancelada.");
            return;
        }
        try {
            controlador.eliminar(id);
            System.out.println("Estudiante eliminado.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Validacion: " + ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
 
    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }
 
    private String leerTextoOpcional(String mensaje, String valorActual) {
        String texto = leerTexto(mensaje);
        return texto.isEmpty() ? valorActual : texto;
    }
 
    private int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException ex) {
                System.out.println("Escribe un numero valido.");
            }
        }
    }
}
