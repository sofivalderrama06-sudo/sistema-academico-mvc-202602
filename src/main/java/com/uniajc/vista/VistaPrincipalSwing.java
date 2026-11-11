package com.uniajc.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.uniajc.db.DatabaseConexion;
import com.uniajc.modelo.Usuario;

/**
 * Ventana principal (JFrame = contenedor de mayor jerarquia).
 * Contiene un menu y un panel con CardLayout: cada opcion del menu
 * muestra un JPanel distinto (inicio, estudiantes, y los que agregues luego).
 * Recibe el usuario que inicio sesion y un Runnable que se ejecuta al
 * cerrar sesion (el Main lo usa para volver a abrir el login).
 */
public class VistaPrincipalSwing extends JFrame {

    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);

    // Nombres de las "tarjetas" del CardLayout
    private static final String PANEL_INICIO = "inicio";
    private static final String PANEL_ESTUDIANTES = "estudiantes";
    private static final String PANEL_DOCENTES = "docentes";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);

    private final Usuario usuario;
    private final Runnable alCerrarSesion;

    // Paneles que cumplen el contrato Vista (se pueden refrescar)
    private final VistaEstudianteSwing vistaEstudiante = new VistaEstudianteSwing();
    private final VistaDocenteSwing vistaDocente = new VistaDocenteSwing();

    public VistaPrincipalSwing(Usuario usuario, Runnable alCerrarSesion) {
        this.usuario = usuario;
        this.alCerrarSesion = alCerrarSesion;

        setTitle("Sistema Academico UNIAJC - " + usuario.getUsername());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Paneles que se pueden mostrar. Para una entidad nueva (docentes, materias...):
        // crear su panel (que implemente Vista), agregarlo aqui con un nombre y
        // agregar un item al menu.
        panelContenedor.add(crearPanelBienvenida(), PANEL_INICIO);
        panelContenedor.add(vistaEstudiante, PANEL_ESTUDIANTES);
        panelContenedor.add(vistaDocente, PANEL_DOCENTES);

        setLayout(new BorderLayout());
        add(panelContenedor, BorderLayout.CENTER);
        configurarMenu();

        cardLayout.show(panelContenedor, PANEL_INICIO);

        // Al cerrar la ventana con la X, se cierra tambien la conexion a la base de datos.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseConexion.closeConnection();
            }
        });
    }

    private JPanel crearPanelBienvenida() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_SECUNDARIO);

        JLabel mensaje = new JLabel("Bienvenido, " + usuario.getNombreCompleto(), SwingConstants.CENTER);
        mensaje.setFont(new Font("Segoe UI", Font.BOLD, 26));
        mensaje.setForeground(COLOR_PRIMARIO);

        JLabel ayuda = new JLabel("Selecciona una opcion del menu", SwingConstants.CENTER);
        ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(mensaje, BorderLayout.CENTER);
        panel.add(ayuda, BorderLayout.SOUTH);
        return panel;
    }

    private void configurarMenu() {
        JMenuBar barra = new JMenuBar();
        barra.setBackground(COLOR_PRIMARIO);

        // Menu Archivo
        JMenu menuArchivo = crearMenu("Archivo");
        JMenuItem itemInicio = new JMenuItem("Inicio");
        itemInicio.addActionListener(e -> cardLayout.show(panelContenedor, PANEL_INICIO));
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar sesion");
        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> {
            DatabaseConexion.closeConnection();
            System.exit(0);
        });
        menuArchivo.add(itemInicio);
        menuArchivo.addSeparator();
        menuArchivo.add(itemCerrarSesion);
        menuArchivo.add(itemSalir);

        // Menu Estudiantes
        JMenu menuEstudiantes = crearMenu("Estudiantes");
        JMenuItem itemGestion = new JMenuItem("Gestion de estudiantes");
        itemGestion.addActionListener(e -> {
            vistaEstudiante.actualizarPanel(); // siempre muestra los datos mas recientes
            cardLayout.show(panelContenedor, PANEL_ESTUDIANTES);
        });
        menuEstudiantes.add(itemGestion);

        // Menu Docentes
        JMenu menuDocentes = crearMenu("Docentes");
        JMenuItem itemGestionDocentes = new JMenuItem("Gestion de docentes");
        itemGestionDocentes.addActionListener(e -> {
            vistaDocente.actualizarPanel();
            cardLayout.show(panelContenedor, PANEL_DOCENTES);
        });
        menuDocentes.add(itemGestionDocentes);

        barra.add(menuArchivo);
        barra.add(menuEstudiantes);
        barra.add(menuDocentes);
        setJMenuBar(barra);
    }

    private JMenu crearMenu(String texto) {
        JMenu menu = new JMenu(texto);
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return menu;
    }

    /**
     * Oculta esta ventana, deja que el Main muestre otra vez el login y,
     * cuando ese flujo termina, libera esta ventana. La conexion a la base
     * de datos NO se cierra: la sigue usando el siguiente usuario.
     */
    private void cerrarSesion() {
        setVisible(false);
        alCerrarSesion.run();
        dispose();
    }
}