package com.uniajc.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.uniajc.controlador.ControladorEstudiante;
import com.uniajc.modelo.Estudiante;

/**
 * Panel de estudiantes (JPanel con BorderLayout):
 *   NORTE  -> panel de busqueda
 *   CENTRO -> tabla con los estudiantes
 *   SUR    -> panel de botones
 * El formulario de registro/edicion es un JDialog (ventana flotante) y las
 * alertas son JOptionPane. La vista solo muestra y captura datos: la logica
 * la hace el controlador.
 */
public class VistaEstudianteSwing extends JPanel implements Vista {

    // Constantes: paleta de colores y textos (se cambian en un solo lugar)
    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);
    private static final Color COLOR_ACENTO = new Color(231, 76, 60);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);

    private static final String TITULO = "Gestion de estudiantes";
    private static final String[] COLUMNAS = {"ID", "Nombre", "Apellido", "Correo"};

    private final ControladorEstudiante controlador = new ControladorEstudiante();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField textoBusqueda;

    // Formulario flotante
    private JDialog dialogoFormulario;
    private JTextField campoNombre;
    private JTextField campoApellido;
    private JTextField campoEmail;
    private int idEnEdicion; // 0 = registrando uno nuevo

    public VistaEstudianteSwing() {
        initComponents();
        cargarEstudiantes();
    }

    /** Contrato Vista: la ventana principal lo llama para mostrar datos frescos. */
    @Override
    public void actualizarPanel() {
        cargarEstudiantes();
    }

    // ------------------------------------------------------------------
    // Construccion de la interfaz
    // ------------------------------------------------------------------

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
        textoBusqueda.addActionListener(e -> buscarEstudiantes()); // Enter tambien busca

        JButton botonBuscar = crearBoton("Buscar", COLOR_PRIMARIO);
        botonBuscar.addActionListener(e -> buscarEstudiantes());

        JButton botonLimpiar = crearBoton("Limpiar", COLOR_NEUTRO);
        botonLimpiar.addActionListener(e -> {
            textoBusqueda.setText("");
            cargarEstudiantes();
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
                return false; // la tabla solo muestra, no se edita aqui
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
        scroll.setPreferredSize(new Dimension(600, 350));

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
        botonEliminar.addActionListener(e -> eliminarEstudiante());

        JButton botonRefrescar = crearBoton("Refrescar", COLOR_NEUTRO);
        botonRefrescar.addActionListener(e -> cargarEstudiantes());

        panel.add(botonRegistrar);
        panel.add(botonEditar);
        panel.add(botonEliminar);
        panel.add(botonRefrescar);
        return panel;
    }

    // Funcion reutilizable: todos los botones comparten esta configuracion;
    // solo cambian el texto y el color.
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

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void cargarEstudiantes() {
        try {
            mostrarEnTabla(controlador.listar());
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarEstudiantes() {
        String busqueda = textoBusqueda.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            cargarEstudiantes();
            return;
        }
        try {
            modeloTabla.setRowCount(0);
            for (Estudiante e : controlador.listar()) {
                boolean coincide = String.valueOf(e.getId()).contains(busqueda)
                        || e.getNombre().toLowerCase().contains(busqueda)
                        || e.getApellido().toLowerCase().contains(busqueda)
                        || e.getEmail().toLowerCase().contains(busqueda);
                if (coincide) {
                    agregarFila(e);
                }
            }
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEnTabla(List<Estudiante> estudiantes) {
        modeloTabla.setRowCount(0);
        for (Estudiante e : estudiantes) {
            agregarFila(e);
        }
    }

    private void agregarFila(Estudiante e) {
        modeloTabla.addRow(new Object[]{e.getId(), e.getNombre(), e.getApellido(), e.getEmail()});
    }

    private void abrirFormulario(boolean esEdicion) {
        String nombre = "";
        String apellido = "";
        String email = "";
        idEnEdicion = 0;

        if (esEdicion) {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            idEnEdicion = (Integer) modeloTabla.getValueAt(fila, 0);
            nombre = (String) modeloTabla.getValueAt(fila, 1);
            apellido = (String) modeloTabla.getValueAt(fila, 2);
            email = (String) modeloTabla.getValueAt(fila, 3);
        }

        dialogoFormulario = new JDialog(SwingUtilities.getWindowAncestor(this),
                esEdicion ? "Editar estudiante" : "Registrar estudiante",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialogoFormulario.setSize(430, 300);
        dialogoFormulario.setLocationRelativeTo(this);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titulo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titulo = new JLabel(esEdicion ? "Editar estudiante" : "Registrar estudiante");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COLOR_PRIMARIO);
        formulario.add(titulo, gbc);

        gbc.gridwidth = 1;
        campoNombre = agregarCampo(formulario, gbc, 1, "Nombre:", nombre);
        campoApellido = agregarCampo(formulario, gbc, 2, "Apellido:", apellido);
        campoEmail = agregarCampo(formulario, gbc, 3, "Correo:", email);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonGuardar = crearBoton("Guardar", COLOR_EXITO);
        botonGuardar.addActionListener(e -> guardarEstudiante(esEdicion));

        JButton botonCancelar = crearBoton("Cancelar", COLOR_NEUTRO);
        botonCancelar.addActionListener(e -> dialogoFormulario.dispose());

        panelBotones.add(botonGuardar);
        panelBotones.add(botonCancelar);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formulario.add(panelBotones, gbc);

        dialogoFormulario.add(formulario);
        dialogoFormulario.setVisible(true); // modal: el codigo se detiene aqui hasta cerrar
    }

    private JTextField agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, String valor) {
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(label, gbc);

        JTextField campo = new JTextField(valor, 20);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(campo, gbc);
        return campo;
    }

    private void guardarEstudiante(boolean esEdicion) {
        String nombre = campoNombre.getText().trim();
        String apellido = campoApellido.getText().trim();
        String email = campoEmail.getText().trim();

        try {
            if (esEdicion) {
                controlador.actualizar(idEnEdicion, nombre, apellido, email);
                JOptionPane.showMessageDialog(dialogoFormulario, "Estudiante actualizado.", TITULO,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                controlador.registrar(nombre, apellido, email);
                JOptionPane.showMessageDialog(dialogoFormulario, "Estudiante registrado.", TITULO,
                        JOptionPane.INFORMATION_MESSAGE);
            }
            dialogoFormulario.dispose();
            cargarEstudiantes();
        } catch (IllegalArgumentException ex) {
            // Validaciones del servicio (campo vacio, correo invalido o repetido...)
            JOptionPane.showMessageDialog(dialogoFormulario, ex.getMessage(), "Validacion",
                    JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(dialogoFormulario, "Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarEstudiante() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (Integer) modeloTabla.getValueAt(fila, 0);
        String nombre = modeloTabla.getValueAt(fila, 1) + " " + modeloTabla.getValueAt(fila, 2);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "Eliminar a " + nombre + "?\nEsta accion no se puede deshacer.",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.eliminar(id);
            mostrarMensaje("Estudiante eliminado.", JOptionPane.INFORMATION_MESSAGE);
            cargarEstudiantes();
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(ex.getMessage(), JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarMensaje(String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, TITULO, tipo);
    }
}