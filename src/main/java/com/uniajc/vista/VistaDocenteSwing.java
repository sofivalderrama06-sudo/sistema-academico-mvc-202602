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

import com.uniajc.controlador.ControladorDocente;
import com.uniajc.modelo.Docente;

/**
 * Panel de docentes (JPanel con BorderLayout):
 *   NORTE  -> panel de busqueda
 *   CENTRO -> tabla con los docentes
 *   SUR    -> panel de botones
 * El formulario de registro/edicion es un JDialog (ventana flotante) y las
 * alertas son JOptionPane. La vista solo muestra y captura datos: la logica
 * la hace el controlador.
 */
public class VistaDocenteSwing extends JPanel implements Vista {

    // Constantes: paleta de colores y textos (se cambian en un solo lugar)
    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);
    private static final Color COLOR_ACENTO = new Color(231, 76, 60);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);

    private static final String TITULO = "Gestion de docentes";
    private static final String[] COLUMNAS = {"ID", "Nombre", "Especialidad", "Correo"};

    private final ControladorDocente controlador = new ControladorDocente();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField textoBusqueda;

    // Formulario flotante
    private JDialog dialogoFormulario;
    private JTextField campoNombre;
    private JTextField campoEspecialidad;
    private JTextField campoEmail;
    private int idEnEdicion; // 0 = registrando uno nuevo

    public VistaDocenteSwing() {
        initComponents();
        cargarDocentes();
    }

    /** Contrato Vista: la ventana principal lo llama para mostrar datos frescos. */
    @Override
    public void actualizarPanel() {
        cargarDocentes();
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
        textoBusqueda.addActionListener(e -> buscarDocentes()); // Enter tambien busca

        JButton botonBuscar = crearBoton("Buscar", COLOR_PRIMARIO);
        botonBuscar.addActionListener(e -> buscarDocentes());

        JButton botonLimpiar = crearBoton("Limpiar", COLOR_NEUTRO);
        botonLimpiar.addActionListener(e -> {
            textoBusqueda.setText("");
            cargarDocentes();
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
        botonEliminar.addActionListener(e -> eliminarDocente());

        JButton botonRefrescar = crearBoton("Refrescar", COLOR_NEUTRO);
        botonRefrescar.addActionListener(e -> cargarDocentes());

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

    private void cargarDocentes() {
        try {
            mostrarEnTabla(controlador.listar());
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarDocentes() {
        String busqueda = textoBusqueda.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            cargarDocentes();
            return;
        }
        try {
            modeloTabla.setRowCount(0);
            for (Docente e : controlador.listar()) {
                boolean coincide = String.valueOf(e.getId()).contains(busqueda)
                        || e.getNombre().toLowerCase().contains(busqueda)
                        || e.getEspecialidad().toLowerCase().contains(busqueda)
                        || e.getEmail().toLowerCase().contains(busqueda);
                if (coincide) {
                    agregarFila(e);
                }
            }
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEnTabla(List<Docente> docentes) {
        modeloTabla.setRowCount(0);
        for (Docente e : docentes) {
            agregarFila(e);
        }
    }

    private void agregarFila(Docente e) {
        modeloTabla.addRow(new Object[]{e.getId(), e.getNombre(), e.getEspecialidad(), e.getEmail()});
    }

    private void abrirFormulario(boolean esEdicion) {
        String nombre = "";
        String especialidad = "";
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
            especialidad = (String) modeloTabla.getValueAt(fila, 2);
            email = (String) modeloTabla.getValueAt(fila, 3);
        }

        dialogoFormulario = new JDialog(SwingUtilities.getWindowAncestor(this),
                esEdicion ? "Editar docente" : "Registrar docente",
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
        JLabel titulo = new JLabel(esEdicion ? "Editar docente" : "Registrar docente");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COLOR_PRIMARIO);
        formulario.add(titulo, gbc);

        gbc.gridwidth = 1;
        campoNombre = agregarCampo(formulario, gbc, 1, "Nombre:", nombre);
        campoEspecialidad = agregarCampo(formulario, gbc, 2, "Especialidad:", especialidad);
        campoEmail = agregarCampo(formulario, gbc, 3, "Correo:", email);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonGuardar = crearBoton("Guardar", COLOR_EXITO);
        botonGuardar.addActionListener(e -> guardarDocente(esEdicion));

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

    private void guardarDocente(boolean esEdicion) {
        String nombre = campoNombre.getText().trim();
        String especialidad = campoEspecialidad.getText().trim();
        String email = campoEmail.getText().trim();

        try {
            if (esEdicion) {
                controlador.actualizar(idEnEdicion, nombre, especialidad, email);
                JOptionPane.showMessageDialog(dialogoFormulario, "Docente actualizado.", TITULO,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                controlador.registrar(nombre, especialidad, email);
                JOptionPane.showMessageDialog(dialogoFormulario, "Docente registrado.", TITULO,
                        JOptionPane.INFORMATION_MESSAGE);
            }
            dialogoFormulario.dispose();
            cargarDocentes();
        } catch (IllegalArgumentException ex) {
            // Validaciones del servicio (campo vacio, correo invalido o repetido...)
            JOptionPane.showMessageDialog(dialogoFormulario, ex.getMessage(), "Validacion",
                    JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(dialogoFormulario, "Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarDocente() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (Integer) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "Eliminar a " + nombre + "?\nEsta accion no se puede deshacer.",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controlador.eliminar(id);
            mostrarMensaje("Docente eliminado.", JOptionPane.INFORMATION_MESSAGE);
            cargarDocentes();
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