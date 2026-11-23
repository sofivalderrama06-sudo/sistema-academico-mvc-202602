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
import com.uniajc.util.PermisoManager;
import com.uniajc.util.PermisoManager.Modulo;

/**
 * Ventana principal (JFrame = contenedor de mayor jerarquia).
 * Contiene un menu y un panel con CardLayout: cada opcion del menu
 * muestra un JPanel distinto (inicio, estudiantes, inscripciones, etc.).
 *
 * Aplica Control de Acceso Basado en Roles mediante PermisoManager:
 * - ADMIN: Acceso total a todos los módulos.
 * - DOCENTE: Acceso a "Mis Grupos" y "Calificaciones" (registro de notas según grupo).
 * - ESTUDIANTE: Acceso a "Mis Calificaciones" (solo consulta de notas).
 */
public class VistaPrincipalSwing extends JFrame {

    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);

    // Nombres de las "tarjetas" del CardLayout
    private static final String PANEL_INICIO = "inicio";
    private static final String PANEL_ESTUDIANTES = "estudiantes";
    private static final String PANEL_DOCENTES = "docentes";
    private static final String PANEL_MATERIAS = "materias";
    private static final String PANEL_PERIODOS = "periodos";
    private static final String PANEL_GRUPOS = "grupos";
    private static final String PANEL_INSCRIPCIONES = "inscripciones";
    private static final String PANEL_USUARIOS = "usuarios";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);

    private final Usuario usuario;
    private final Runnable alCerrarSesion;

    // Paneles que cumplen el contrato Vista (adaptados según el usuario autenticado)
    private final VistaEstudianteSwing vistaEstudiante;
    private final VistaDocenteSwing vistaDocente;
    private final VistaMateriaSwing vistaMateria;
    private final VistaPeriodo vistaPeriodo;
    private final VistaGrupo vistaGrupo;
    private final VistaInscripcionSwing vistaInscripcion;
    private final VistaUsuarioSwing vistaUsuario;

    public VistaPrincipalSwing(Usuario usuario, Runnable alCerrarSesion) {
        this.usuario = usuario;
        this.alCerrarSesion = alCerrarSesion;

        // Se inicializan los paneles pasando el usuario actual para aplicar permisos contextuales
        this.vistaEstudiante = new VistaEstudianteSwing();
        this.vistaDocente = new VistaDocenteSwing();
        this.vistaMateria = new VistaMateriaSwing();
        this.vistaPeriodo = new VistaPeriodo();
        this.vistaGrupo = new VistaGrupo(usuario);
        this.vistaInscripcion = new VistaInscripcionSwing(usuario);
        this.vistaUsuario = new VistaUsuarioSwing();

        String rol = usuario != null && usuario.getRol() != null ? usuario.getRol().toUpperCase() : "ESTUDIANTE";
        setTitle("Sistema Academico UNIAJC - " + (usuario != null ? usuario.getUsername() : "") + " [" + rol + "]");
        setSize(950, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Agregamos paneles al contenedor CardLayout
        panelContenedor.add(crearPanelBienvenida(), PANEL_INICIO);
        panelContenedor.add(vistaEstudiante, PANEL_ESTUDIANTES);
        panelContenedor.add(vistaDocente, PANEL_DOCENTES);
        panelContenedor.add(vistaMateria, PANEL_MATERIAS);
        panelContenedor.add(vistaPeriodo, PANEL_PERIODOS);
        panelContenedor.add(vistaGrupo, PANEL_GRUPOS);
        panelContenedor.add(vistaInscripcion, PANEL_INSCRIPCIONES);
        panelContenedor.add(vistaUsuario, PANEL_USUARIOS);

        setLayout(new BorderLayout());
        add(panelContenedor, BorderLayout.CENTER);
        configurarMenu();

        cardLayout.show(panelContenedor, PANEL_INICIO);

        // Al cerrar la ventana con la X, se cierra también la conexión a la base de datos.
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

        String rol = usuario != null && usuario.getRol() != null ? usuario.getRol().toUpperCase() : "USUARIO";
        String nombre = usuario != null ? usuario.getNombreCompleto() : "";

        JLabel mensaje = new JLabel("Bienvenido, " + nombre, SwingConstants.CENTER);
        mensaje.setFont(new Font("Segoe UI", Font.BOLD, 26));
        mensaje.setForeground(COLOR_PRIMARIO);

        JLabel ayuda = new JLabel("Rol activo: " + rol + " | Selecciona una opcion del menu superior", SwingConstants.CENTER);
        ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(mensaje, BorderLayout.CENTER);
        panel.add(ayuda, BorderLayout.SOUTH);
        return panel;
    }

    private void configurarMenu() {
        JMenuBar barra = new JMenuBar();
        barra.setBackground(COLOR_PRIMARIO);

        // Menú Archivo (común a todos los usuarios)
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

        // Menú Estudiantes
        JMenu menuEstudiantes = crearMenu("Estudiantes");
        JMenuItem itemGestion = new JMenuItem("Gestion de estudiantes");
        itemGestion.addActionListener(e -> {
            vistaEstudiante.actualizarPanel();
            cardLayout.show(panelContenedor, PANEL_ESTUDIANTES);
        });
        menuEstudiantes.add(itemGestion);

        // Menú Docentes
        JMenu menuDocentes = crearMenu("Docentes");
        JMenuItem itemGestionDocentes = new JMenuItem("Gestion de docentes");
        itemGestionDocentes.addActionListener(e -> {
            vistaDocente.actualizarPanel();
            cardLayout.show(panelContenedor, PANEL_DOCENTES);
        });
        menuDocentes.add(itemGestionDocentes);

        // Menú Materias
        JMenu menuMaterias = crearMenu("Materias");
        JMenuItem itemGestionMaterias = new JMenuItem("Gestion de materias");
        itemGestionMaterias.addActionListener(e -> {
            vistaMateria.actualizarPanel();
            cardLayout.show(panelContenedor, PANEL_MATERIAS);
        });
        menuMaterias.add(itemGestionMaterias);

        // Menú Periodos
        JMenu menuPeriodos = crearMenu("Periodos");
        JMenuItem itemGestionPeriodos = new JMenuItem("Gestion de periodos");
        itemGestionPeriodos.addActionListener(e -> {
            vistaPeriodo.actualizarPanel();
            cardLayout.show(panelContenedor, PANEL_PERIODOS);
        });
        menuPeriodos.add(itemGestionPeriodos);

        // Menú Grupos (se adapta según el rol)
        JMenu menuGrupos = crearMenu("Grupos");
        JMenuItem itemGestionGrupos = new JMenuItem("Gestion de grupos");
        itemGestionGrupos.addActionListener(e -> {
            vistaGrupo.actualizarPanel();
            cardLayout.show(panelContenedor, PANEL_GRUPOS);
        });
        menuGrupos.add(itemGestionGrupos);

        // Menú Inscripciones / Calificaciones (se adapta según el rol)
        JMenu menuInscripciones = crearMenu("Inscripciones");
        JMenuItem itemGestionInscripciones = new JMenuItem("Gestion de inscripciones");
        itemGestionInscripciones.addActionListener(e -> {
            vistaInscripcion.actualizarPanel();
            cardLayout.show(panelContenedor, PANEL_INSCRIPCIONES);
        });
        menuInscripciones.add(itemGestionInscripciones);

        // Menú Usuarios (exclusivo para ADMIN)
        JMenu menuUsuarios = crearMenu("Usuarios");
        JMenuItem itemGestionUsuarios = new JMenuItem("Gestion de usuarios");
        itemGestionUsuarios.addActionListener(e -> {
            vistaUsuario.actualizarPanel();
            cardLayout.show(panelContenedor, PANEL_USUARIOS);
        });
        menuUsuarios.add(itemGestionUsuarios);

        // CONSTRUCCIÓN DINÁMICA DE LA BARRA DE MENÚS MEDIANTE PermisoManager
        barra.add(menuArchivo);

        String rol = usuario != null && usuario.getRol() != null ? usuario.getRol().toUpperCase() : "ESTUDIANTE";

        if (PermisoManager.tieneAccesoModulo(rol, Modulo.ESTUDIANTES)) {
            barra.add(menuEstudiantes);
        }
        if (PermisoManager.tieneAccesoModulo(rol, Modulo.DOCENTES)) {
            barra.add(menuDocentes);
        }
        if (PermisoManager.tieneAccesoModulo(rol, Modulo.MATERIAS)) {
            barra.add(menuMaterias);
        }
        if (PermisoManager.tieneAccesoModulo(rol, Modulo.PERIODOS)) {
            barra.add(menuPeriodos);
        }
        if (PermisoManager.tieneAccesoModulo(rol, Modulo.GRUPOS)) {
            if ("DOCENTE".equals(rol)) {
                menuGrupos.setText("Mis Grupos");
                itemGestionGrupos.setText("Grupos asignados");
            }
            barra.add(menuGrupos);
        }
        if (PermisoManager.tieneAccesoModulo(rol, Modulo.INSCRIPCIONES)) {
            if ("ESTUDIANTE".equals(rol)) {
                menuInscripciones.setText("Mis Calificaciones");
                itemGestionInscripciones.setText("Consultar mis notas");
            } else if ("DOCENTE".equals(rol)) {
                menuInscripciones.setText("Calificaciones");
                itemGestionInscripciones.setText("Registro de notas por grupo");
            }
            barra.add(menuInscripciones);
        }
        if (PermisoManager.tieneAccesoModulo(rol, Modulo.USUARIOS)) {
            barra.add(menuUsuarios);
        }

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