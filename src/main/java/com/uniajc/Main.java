package com.uniajc;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Usuario;
import com.uniajc.vista.VistaLoginSwing;
import com.uniajc.vista.VistaPrincipalSwing;

public class Main {

    public static void main(String[] args) {
        System.out.println("Bienvenido al Sistema Academico!");

        if (DatabaseConexion.getConnection() == null) {
            System.out.println("No se pudo conectar a la base de datos. Revisa config.properties.");
            return;
        }

        // Estilo uniforme en cualquier sistema operativo (respeta los colores de los botones)
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            // Si falla, se usa el estilo por defecto
        }

        // Swing debe arrancar en su propio hilo (Event Dispatch Thread)
        SwingUtilities.invokeLater(Main::iniciarSesion);

        // Otras vistas disponibles:
        //   new VistaEstudiante().mostrarMenu();        -> consola (Scanner)
        //   new VistaEstudianteOption().mostrarMenu();  -> ventanas JOptionPane
    }

    /**
     * Flujo del profe: primero el login; si el usuario existe se abre la
     * ventana principal; si cierran el login sin ingresar, se termina el programa.
     * Tambien se llama al "Cerrar sesion" de la ventana principal.
     */
    public static void iniciarSesion() {
        VistaLoginSwing login = new VistaLoginSwing();
        login.setVisible(true); // modal: se detiene aqui hasta que se cierre el login

        Usuario usuario = login.getUsuario();
        if (usuario == null) {
            DatabaseConexion.closeConnection();
            System.exit(0);
            return;
        }
        new VistaPrincipalSwing(usuario, Main::iniciarSesion).setVisible(true);
    }
}