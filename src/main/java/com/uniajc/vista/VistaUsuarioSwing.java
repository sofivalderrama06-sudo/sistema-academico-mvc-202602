package com.uniajc.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.uniajc.controlador.ControladorUsuario;
import com.uniajc.modelo.Docente;
import com.uniajc.modelo.Estudiante;
import com.uniajc.modelo.Usuario;

/**
 * ============================================================================
 * CAPA DE VISTA: VistaUsuarioSwing
 * ============================================================================
 * Panel de gestión de usuarios exclusivo para el Administrador.
 * Permite:
 *   - Ver la lista de usuarios del sistema académico.
 *   - Crear y editar cuentas de usuario enlazándolas según su rol:
 *       * Rol ESTUDIANTE: Permite elegir un estudiante registrado para autorellenar
 *         y enlazar su nombre completo y correo institucional.
 *       * Rol DOCENTE: Permite elegir un docente registrado para autorellenar
 *         y enlazar sus datos.
 *       * Rol ADMIN: Permite crear administradores independientes.
 *   - Cifrar contraseñas con BCrypt de forma transparente.
 */
public class VistaUsuarioSwing extends JPanel implements Vista {

    // Paleta de colores institucional
    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);
    private static final Color COLOR_ACENTO = new Color(231, 76, 60);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);

    // Columnas de la tabla
    private static final String[] COLUMNAS = {"ID", "Usuario", "Nombre", "Correo", "Rol", "Estado"};

    // Roles disponibles para asignación
    private static final String[] ROLES_DISPONIBLES = {"ESTUDIANTE", "DOCENTE", "ADMIN"};

    private final ControladorUsuario controlador = new ControladorUsuario();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField textoBusqueda;

    // Componentes del formulario modal
    private JDialog dialogoFormulario;
    private JComboBox<String> comboRol;
    private JLabel labelEnlace;
    private JComboBox<String> comboEnlace;
    private JTextField campoUsuario;
    private JPasswordField campoPassword;
    private JTextField campoNombre;
    private JTextField campoCorreo;
    private JCheckBox checkActivo;
    private int idEnEdicion; // 0 = Modo Registro; > 0 = Modo Edición

    // Listas en memoria para poblar los combos de enlace
    private List<Estudiante> listaEstudiantes = new ArrayList<>();
    private List<Docente> listaDocentes = new ArrayList<>();

    public VistaUsuarioSwing() {
        initComponents();
        cargarUsuarios();
    }

    @Override
    public void actualizarPanel() {
        cargarUsuarios();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_SECUNDARIO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(crearPanelBusqueda(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(COLOR_SECUNDARIO);

        JLabel etiqueta = new JLabel("Buscar:");
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiqueta.setForeground(COLOR_PRIMARIO);

        textoBusqueda = new JTextField(25);
        textoBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textoBusqueda.setPreferredSize(new Dimension(220, 30));
        textoBusqueda.addActionListener(e -> buscarUsuarios());

        JButton botonBuscar = crearBoton("Buscar", COLOR_PRIMARIO);
        botonBuscar.addActionListener(e -> buscarUsuarios());

        JButton botonLimpiar = crearBoton("Limpiar", COLOR_NEUTRO);
        botonLimpiar.addActionListener(e -> {
            textoBusqueda.setText("");
            cargarUsuarios();
        });

        panel.add(etiqueta);
        panel.add(textoBusqueda);
        panel.add(botonBuscar);
        panel.add(botonLimpiar);
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_SECUNDARIO);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(25);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setGridColor(new Color(189, 195, 199));
        tabla.setBackground(Color.WHITE);
        tabla.setSelectionBackground(COLOR_PRIMARIO);
        tabla.setSelectionForeground(Color.WHITE);

        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(COLOR_PRIMARIO);
        tabla.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARIO, 2));
        scroll.setPreferredSize(new Dimension(700, 350));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(COLOR_SECUNDARIO);

        JButton botonRegistrar = crearBoton("Registrar", COLOR_EXITO);
        botonRegistrar.addActionListener(e -> abrirFormulario(false));

        JButton botonEditar = crearBoton("Editar", COLOR_PRIMARIO);
        botonEditar.addActionListener(e -> abrirFormulario(true));

        JButton botonEliminar = crearBoton("Eliminar", COLOR_ACENTO);
        botonEliminar.addActionListener(e -> eliminarUsuario());

        JButton botonRefrescar = crearBoton("Refrescar", COLOR_NEUTRO);
        botonRefrescar.addActionListener(e -> cargarUsuarios());

        panel.add(botonRegistrar);
        panel.add(botonEditar);
        panel.add(botonEliminar);
        panel.add(botonRefrescar);
        return panel;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setPreferredSize(new Dimension(110, 35));
        return boton;
    }

    private void cargarUsuarios() {
        try {
            mostrarEnTabla(controlador.listar());
        } catch (RuntimeException ex) {
            mostrarMensaje("Error al cargar usuarios: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarUsuarios() {
        String busqueda = textoBusqueda.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            cargarUsuarios();
            return;
        }
        try {
            modeloTabla.setRowCount(0);
            for (Usuario u : controlador.listar()) {
                boolean coincide = String.valueOf(u.getId()).contains(busqueda)
                        || u.getUsuario().toLowerCase().contains(busqueda)
                        || u.getNombreCompleto().toLowerCase().contains(busqueda)
                        || u.getCorreo().toLowerCase().contains(busqueda)
                        || u.getRol().toLowerCase().contains(busqueda);
                if (coincide) {
                    agregarFila(u);
                }
            }
        } catch (RuntimeException ex) {
            mostrarMensaje("Error en la búsqueda: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEnTabla(List<Usuario> usuarios) {
        modeloTabla.setRowCount(0);
        for (Usuario u : usuarios) {
            agregarFila(u);
        }
    }

    private void agregarFila(Usuario u) {
        modeloTabla.addRow(new Object[]{
                u.getId(),
                u.getUsuario(),
                u.getNombreCompleto(),
                u.getCorreo(),
                u.getRol(),
                u.isActivo() ? "Activo" : "Inactivo"
        });
    }

    // ========================================================================
    // FORMULARIO CON ENLACE DINÁMICO DE ESTUDIANTES / DOCENTES
    // ========================================================================

    private void abrirFormulario(boolean esEdicion) {
        String usuario = "";
        String nombre = "";
        String correo = "";
        String rol = "ESTUDIANTE";
        boolean activo = true;
        idEnEdicion = 0;

        // Cargar listas actualizadas de estudiantes y docentes desde la BD
        try {
            listaEstudiantes = controlador.listarEstudiantes();
            listaDocentes = controlador.listarDocentes();
        } catch (Exception ex) {
            listaEstudiantes = new ArrayList<>();
            listaDocentes = new ArrayList<>();
        }

        if (esEdicion) {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarMensaje("Selecciona un usuario de la tabla para editar.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            idEnEdicion = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
            usuario = (String) modeloTabla.getValueAt(fila, 1);
            nombre = (String) modeloTabla.getValueAt(fila, 2);
            correo = (String) modeloTabla.getValueAt(fila, 3);
            rol = (String) modeloTabla.getValueAt(fila, 4);
            activo = "Activo".equals(modeloTabla.getValueAt(fila, 5));
        }

        String tituloVentana = esEdicion ? "Editar Usuario" : "Registrar Usuario";
        dialogoFormulario = new JDialog(SwingUtilities.getWindowAncestor(this),
                tituloVentana,
                JDialog.DEFAULT_MODALITY_TYPE);
        dialogoFormulario.setSize(520, 510);
        dialogoFormulario.setLocationRelativeTo(this);
        dialogoFormulario.setResizable(false);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título del encabezado
        JLabel labelTitulo = new JLabel(esEdicion ? "✏ Editar Usuario" : "➕ Registrar y Enlazar Usuario");
        labelTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        labelTitulo.setForeground(COLOR_PRIMARIO);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 15, 5);
        panelFormulario.add(labelTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 6, 6, 6);

        // 1. Selector de Rol al inicio (define a quién se enlaza)
        comboRol = new JComboBox<>(ROLES_DISPONIBLES);
        comboRol.setSelectedItem(rol);

        // 2. Selector dinámico de persona a enlazar
        labelEnlace = new JLabel("Enlazar Estudiante:");
        labelEnlace.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelEnlace.setForeground(new Color(41, 128, 185));

        comboEnlace = new JComboBox<>();
        comboEnlace.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // 3. Campos de texto
        campoUsuario = new JTextField(usuario, 20);
        campoPassword = new JPasswordField(20);
        campoNombre = new JTextField(nombre, 20);
        campoCorreo = new JTextField(correo, 20);
        checkActivo = new JCheckBox("Usuario activo", activo);
        checkActivo.setBackground(Color.WHITE);

        // Configuración de cambio de Rol: refresca el comboEnlace dinámicamente
        comboRol.addActionListener(e -> actualizarComboEnlace());

        // Configuración de selección de persona: autocompleta nombre, correo y sugiere usuario
        comboEnlace.addActionListener(e -> autocompletarDatosEnlace());

        int filaY = 1;

        // Fila: Rol
        gbc.gridx = 0; gbc.gridy = filaY; panelFormulario.add(new JLabel("Rol del usuario:"), gbc);
        gbc.gridx = 1; panelFormulario.add(comboRol, gbc);
        filaY++;

        // Fila: Selector de enlace
        gbc.gridx = 0; gbc.gridy = filaY; panelFormulario.add(labelEnlace, gbc);
        gbc.gridx = 1; panelFormulario.add(comboEnlace, gbc);
        filaY++;

        // Fila: Nombre de Usuario
        agregarCampo(panelFormulario, gbc, filaY++, "Usuario (Login):", campoUsuario);

        // Fila: Contraseña
        String etiquetaPass = esEdicion ? "Nueva clave (opcional):" : "Contraseña (mín 6 car):";
        agregarCampo(panelFormulario, gbc, filaY++, etiquetaPass, campoPassword);

        // Fila: Nombre y Correo enlazados
        agregarCampo(panelFormulario, gbc, filaY++, "Nombre completo:", campoNombre);
        agregarCampo(panelFormulario, gbc, filaY++, "Correo institucional:", campoCorreo);

        if (esEdicion) {
            gbc.gridx = 1; gbc.gridy = filaY++;
            panelFormulario.add(checkActivo, gbc);
        }

        // Fila: Botones Guardar / Cancelar
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        panelAcciones.setBackground(Color.WHITE);

        JButton botonGuardar = crearBoton("Guardar", COLOR_EXITO);
        botonGuardar.addActionListener(e -> guardarUsuario(esEdicion));

        JButton botonCancelar = crearBoton("Cancelar", COLOR_NEUTRO);
        botonCancelar.addActionListener(e -> dialogoFormulario.dispose());

        panelAcciones.add(botonGuardar);
        panelAcciones.add(botonCancelar);

        gbc.gridx = 0; gbc.gridy = filaY; gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 0, 0, 0);
        panelFormulario.add(panelAcciones, gbc);

        // Inicializar el combo de enlace con el rol activo
        actualizarComboEnlace();

        dialogoFormulario.add(panelFormulario);
        dialogoFormulario.setVisible(true);
    }

    /**
     * Actualiza el comboEnlace según si se seleccionó ESTUDIANTE, DOCENTE o ADMIN.
     */
    private void actualizarComboEnlace() {
        String rolSeleccionado = (String) comboRol.getSelectedItem();
        comboEnlace.removeAllItems();

        if ("ESTUDIANTE".equals(rolSeleccionado)) {
            labelEnlace.setText("Enlazar Estudiante:");
            labelEnlace.setVisible(true);
            comboEnlace.setVisible(true);
            comboEnlace.addItem("-- Seleccione un estudiante --");
            for (Estudiante est : listaEstudiantes) {
                comboEnlace.addItem(est.getId() + " - " + est.getNombre() + " " + est.getApellido() + " (" + est.getEmail() + ")");
            }
        } else if ("DOCENTE".equals(rolSeleccionado)) {
            labelEnlace.setText("Enlazar Docente:");
            labelEnlace.setVisible(true);
            comboEnlace.setVisible(true);
            comboEnlace.addItem("-- Seleccione un docente --");
            for (Docente doc : listaDocentes) {
                comboEnlace.addItem(doc.getId() + " - " + doc.getNombre() + " (" + doc.getEmail() + ")");
            }
        } else {
            // ADMIN: no requiere enlazar a persona
            labelEnlace.setText("Enlace a persona:");
            comboEnlace.addItem("(Sin enlace - Administrador directo)");
            comboEnlace.setEnabled(false);
            campoNombre.setEditable(true);
            campoCorreo.setEditable(true);
            return;
        }

        comboEnlace.setEnabled(true);
    }

    /**
     * Cuando el administrador selecciona un estudiante o docente del combo,
     * se autocompletan el Nombre, el Correo y se sugiere el nombre de Usuario.
     */
    private void autocompletarDatosEnlace() {
        int index = comboEnlace.getSelectedIndex();
        if (index <= 0) {
            return; // Encabezado seleccionado
        }

        String rol = (String) comboRol.getSelectedItem();
        if ("ESTUDIANTE".equals(rol) && index - 1 < listaEstudiantes.size()) {
            Estudiante e = listaEstudiantes.get(index - 1);
            campoNombre.setText(e.getNombre() + " " + e.getApellido());
            campoCorreo.setText(e.getEmail());
            // Sugiere el usuario a partir del email si el campo está vacío
            if (campoUsuario.getText().trim().isEmpty() || idEnEdicion == 0) {
                String sugerido = e.getEmail().contains("@") ? e.getEmail().split("@")[0] : e.getNombre().toLowerCase().replace(" ", ".");
                campoUsuario.setText(sugerido);
            }
        } else if ("DOCENTE".equals(rol) && index - 1 < listaDocentes.size()) {
            Docente d = listaDocentes.get(index - 1);
            campoNombre.setText(d.getNombre());
            campoCorreo.setText(d.getEmail());
            if (campoUsuario.getText().trim().isEmpty() || idEnEdicion == 0) {
                String sugerido = d.getEmail().contains("@") ? d.getEmail().split("@")[0] : d.getNombre().toLowerCase().replace(" ", ".");
                campoUsuario.setText(sugerido);
            }
        }
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, javax.swing.JComponent campo) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = fila;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(campo, gbc);
    }

    private void guardarUsuario(boolean esEdicion) {
        try {
            String usuario = campoUsuario.getText().trim();
            String password = new String(campoPassword.getPassword()).trim();
            String nombre = campoNombre.getText().trim();
            String correo = campoCorreo.getText().trim();
            String rol = (String) comboRol.getSelectedItem();
            boolean activo = checkActivo.isSelected();

            if (esEdicion) {
                controlador.actualizar(idEnEdicion, usuario, password, nombre, correo, rol, activo);
                mostrarMensaje("Usuario actualizado con éxito.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                controlador.registrar(usuario, password, nombre, correo, rol);
                mostrarMensaje("Usuario registrado y enlazado con éxito.", JOptionPane.INFORMATION_MESSAGE);
            }

            dialogoFormulario.dispose();
            cargarUsuarios();
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(ex.getMessage(), JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            mostrarMensaje("Error al procesar usuario: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarUsuario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona un usuario de la tabla para eliminar.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
        String user = (String) modeloTabla.getValueAt(fila, 1);

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar el usuario '" + user + "'?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                controlador.eliminar(id);
                mostrarMensaje("Usuario eliminado exitosamente.", JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();
            } catch (RuntimeException ex) {
                mostrarMensaje("Error al eliminar usuario: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarMensaje(String texto, int tipo) {
        JOptionPane.showMessageDialog(this, texto, "Gestión de Usuarios", tipo);
    }
}
