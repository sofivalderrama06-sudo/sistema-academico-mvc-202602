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

import com.uniajc.controlador.ControladorMateria;
import com.uniajc.modelo.Materia;

/**
 * Vista Swing para consultar y administrar materias.
 * La vista captura las acciones del usuario y delega las operaciones al controlador.
 */
public class VistaMateriaSwing extends JPanel implements Vista {

    // Colores compartidos por la tabla, los botones y los formularios.
    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);
    private static final Color COLOR_ACENTO = new Color(231, 76, 60);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);

    // Define el título de los mensajes y las columnas visibles de materias.
    private static final String TITULO = "Gestion de materias";
    private static final String[] COLUMNAS = {"ID", "Nombre", "Creditos"};

    // Controlador que conecta esta vista con las reglas de negocio y el DAO.
    private final ControladorMateria controlador = new ControladorMateria();

    // Componentes de consulta y tabla que se actualizan durante la ejecución.
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField textoBusqueda;

    // Componentes del formulario modal para registrar o editar una materia.
    private JDialog dialogoFormulario;
    private JTextField campoNombre;
    private JTextField campoCreditos;
    private int idEnEdicion;

    // Construye la interfaz y carga las materias disponibles al mostrarla.
    public VistaMateriaSwing() {
        initComponents();
        cargarMaterias();
    }

    // Actualiza la tabla cada vez que la ventana principal muestra este panel.
    @Override
    public void actualizarPanel() {
        cargarMaterias();
    }

    // Organiza los paneles de búsqueda, tabla y acciones principales.
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_SECUNDARIO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(crearPanelBusqueda(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);
    }

    // Crea los controles para filtrar materias por sus datos visibles.
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(COLOR_SECUNDARIO);

        JLabel etiqueta = new JLabel("Buscar:");
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiqueta.setForeground(COLOR_PRIMARIO);

        textoBusqueda = new JTextField(25);
        textoBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textoBusqueda.setPreferredSize(new Dimension(220, 30));
        textoBusqueda.addActionListener(e -> buscarMaterias());

        JButton botonBuscar = crearBoton("Buscar", COLOR_PRIMARIO);
        botonBuscar.addActionListener(e -> buscarMaterias());

        JButton botonLimpiar = crearBoton("Limpiar", COLOR_NEUTRO);
        botonLimpiar.addActionListener(e -> {
            textoBusqueda.setText("");
            cargarMaterias();
        });

        panel.add(etiqueta);
        panel.add(textoBusqueda);
        panel.add(botonBuscar);
        panel.add(botonLimpiar);
        return panel;
    }

    // Crea una tabla no editable con identificador, nombre, créditos y fecha.
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
        scroll.setPreferredSize(new Dimension(600, 350));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // Crea las acciones CRUD y la opción para volver a consultar la base de datos.
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(COLOR_SECUNDARIO);

        JButton botonRegistrar = crearBoton("Registrar", COLOR_EXITO);
        botonRegistrar.addActionListener(e -> abrirFormulario(false));

        JButton botonEditar = crearBoton("Editar", COLOR_PRIMARIO);
        botonEditar.addActionListener(e -> abrirFormulario(true));

        JButton botonEliminar = crearBoton("Eliminar", COLOR_ACENTO);
        botonEliminar.addActionListener(e -> eliminarMateria());

        JButton botonRefrescar = crearBoton("Refrescar", COLOR_NEUTRO);
        botonRefrescar.addActionListener(e -> cargarMaterias());

        panel.add(botonRegistrar);
        panel.add(botonEditar);
        panel.add(botonEliminar);
        panel.add(botonRefrescar);
        return panel;
    }

    // Aplica el mismo estilo a todos los botones de la vista.
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

    // Solicita todas las materias al controlador y presenta errores en la interfaz.
    private void cargarMaterias() {
        try {
            mostrarEnTabla(controlador.listar());
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Filtra en memoria las materias cargadas según ID, nombre o créditos.
    private void buscarMaterias() {
        String busqueda = textoBusqueda.getText().trim().toLowerCase();
        if (busqueda.isEmpty()) {
            cargarMaterias();
            return;
        }

        try {
            modeloTabla.setRowCount(0);
            for (Materia materia : controlador.listar()) {
                boolean coincide = String.valueOf(materia.getId()).contains(busqueda)
                        || materia.getName().toLowerCase().contains(busqueda)
                        || String.valueOf(materia.getCredits()).contains(busqueda);
                if (coincide) {
                    agregarFila(materia);
                }
            }
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Limpia la tabla y la rellena con la lista entregada por el controlador.
    private void mostrarEnTabla(List<Materia> materias) {
        modeloTabla.setRowCount(0);
        for (Materia materia : materias) {
            agregarFila(materia);
        }
    }

    // Agrega los atributos de una materia como una fila de la tabla.
    private void agregarFila(Materia materia) {
        modeloTabla.addRow(new Object[]{
                materia.getId(),
                materia.getName(),
                materia.getCredits()
        });
    }

    // Abre el formulario vacío o carga en él los datos de la fila seleccionada.
    private void abrirFormulario(boolean esEdicion) {
        String nombre = "";
        String creditos = "";
        idEnEdicion = 0;

        if (esEdicion) {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            idEnEdicion = (Integer) modeloTabla.getValueAt(fila, 0);
            nombre = (String) modeloTabla.getValueAt(fila, 1);
            creditos = String.valueOf(modeloTabla.getValueAt(fila, 2));
        }

        dialogoFormulario = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                esEdicion ? "Editar materia" : "Registrar materia",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialogoFormulario.setSize(430, 250);
        dialogoFormulario.setLocationRelativeTo(this);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titulo = new JLabel(esEdicion ? "Editar materia" : "Registrar materia");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COLOR_PRIMARIO);
        formulario.add(titulo, gbc);

        gbc.gridwidth = 1;
        campoNombre = agregarCampo(formulario, gbc, 1, "Nombre:", nombre);
        campoCreditos = agregarCampo(formulario, gbc, 2, "Creditos:", creditos);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonGuardar = crearBoton("Guardar", COLOR_EXITO);
        botonGuardar.addActionListener(e -> guardarMateria(esEdicion));

        JButton botonCancelar = crearBoton("Cancelar", COLOR_NEUTRO);
        botonCancelar.addActionListener(e -> dialogoFormulario.dispose());

        panelBotones.add(botonGuardar);
        panelBotones.add(botonCancelar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formulario.add(panelBotones, gbc);

        dialogoFormulario.add(formulario);
        dialogoFormulario.setVisible(true);
    }

    // Añade una etiqueta y su campo de texto en la fila indicada del formulario.
    private JTextField agregarCampo(
            JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, String valor) {
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

    // Valida el formato de créditos y delega el registro o la actualización.
    private void guardarMateria(boolean esEdicion) {
        String nombre = campoNombre.getText().trim();
        int creditos;
        try {
            creditos = Integer.parseInt(campoCreditos.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario,
                    "Los creditos deben ser un numero entero.",
                    "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (esEdicion) {
                controlador.actualizar(idEnEdicion, nombre, creditos);
                JOptionPane.showMessageDialog(
                        dialogoFormulario, "Materia actualizada.", TITULO, JOptionPane.INFORMATION_MESSAGE);
            } else {
                controlador.registrar(nombre, creditos);
                JOptionPane.showMessageDialog(
                        dialogoFormulario, "Materia registrada.", TITULO, JOptionPane.INFORMATION_MESSAGE);
            }
            dialogoFormulario.dispose();
            cargarMaterias();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario, ex.getMessage(), "Validacion", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Pide confirmación antes de eliminar la materia seleccionada.
    private void eliminarMateria() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Eliminar la materia " + nombre + "?\nEsta accion no se puede deshacer.",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.eliminar(id);
            mostrarMensaje("Materia eliminada.", JOptionPane.INFORMATION_MESSAGE);
            cargarMaterias();
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(ex.getMessage(), JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Muestra un mensaje emergente asociado al panel.
    private void mostrarMensaje(String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, TITULO, tipo);
    }
}
