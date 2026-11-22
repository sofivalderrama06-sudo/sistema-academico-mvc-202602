package com.uniajc.vista;

import java.awt.BorderLayout;
import javax.swing.*;
import java.awt.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;

import com.uniajc.controlador.ControladorPeriodo;
import com.uniajc.modelo.Periodo;

/**
 * Vista Swing para consultar y administrar periodos académicos.
 * Las operaciones de persistencia se delegan al controlador.
 */
public class VistaPeriodo extends JPanel implements Vista {

    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);
    private static final Color COLOR_ACENTO = new Color(231, 76, 60);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);

    private static final String TITULO = "Gestion de periodos";
    private static final String[] COLUMNAS = {"ID", "Nombre", "Fecha inicio", "Fecha fin", "Estado"};
    private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");

    private final ControladorPeriodo controlador = new ControladorPeriodo();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField textoBusqueda;
    private JDialog dialogoFormulario;
    private JTextField campoNombre;
    private JDateChooser campoFechaInicio;
    private JDateChooser campoFechaFin;
    private JComboBox<String> campoEstado;
    private int idEnEdicion;

    public VistaPeriodo() {
        initComponents();
        cargarPeriodos();
    }
// Implementación de la interfaz Vista; se llama desde el controlador para actualizar la vista.
    @Override
    public void actualizarPanel() {
        cargarPeriodos();
    }

    // Organiza los controles de búsqueda, la tabla y las acciones CRUD.
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
        textoBusqueda.addActionListener(e -> buscarPeriodos());

        JButton botonBuscar = crearBoton("Buscar", COLOR_PRIMARIO);
        botonBuscar.addActionListener(e -> buscarPeriodos());

        JButton botonLimpiar = crearBoton("Limpiar", COLOR_NEUTRO);
        botonLimpiar.addActionListener(e -> {
            textoBusqueda.setText("");
            cargarPeriodos();
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
        botonEliminar.addActionListener(e -> eliminarPeriodo());

        JButton botonRefrescar = crearBoton("Refrescar", COLOR_NEUTRO);
        botonRefrescar.addActionListener(e -> cargarPeriodos());

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

    private void cargarPeriodos() {
        try {
            mostrarEnTabla(controlador.listar());
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Filtra en memoria por identificador, nombre, fechas o estado.
    private void buscarPeriodos() {
        String busqueda = textoBusqueda.getText().trim().toLowerCase(Locale.ROOT);
        if (busqueda.isEmpty()) {
            cargarPeriodos();
            return;
        }

        try {
            modeloTabla.setRowCount(0);
            for (Periodo periodo : controlador.listar()) {
                String inicio = formatearFecha(periodo.getStartDate()).toLowerCase(Locale.ROOT);
                String fin = formatearFecha(periodo.getEndDate()).toLowerCase(Locale.ROOT);
                String estado = periodo.isState() ? "activo" : "inactivo";
                if (String.valueOf(periodo.getId()).contains(busqueda)
                        || periodo.getName().toLowerCase(Locale.ROOT).contains(busqueda)
                        || inicio.contains(busqueda)
                        || fin.contains(busqueda)
                        || estado.contains(busqueda)) {
                    agregarFila(periodo);
                }
            }
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarEnTabla(List<Periodo> periodos) {
        modeloTabla.setRowCount(0);
        for (Periodo periodo : periodos) {
            agregarFila(periodo);
        }
    }

    private void agregarFila(Periodo periodo) {
        modeloTabla.addRow(new Object[]{
                periodo.getId(),
                periodo.getName(),
                formatearFecha(periodo.getStartDate()),
                formatearFecha(periodo.getEndDate()),
                periodo.isState() ? "Activo" : "Inactivo"
        });
    }

    private String formatearFecha(Timestamp fecha) {
        return fecha == null ? "" : FORMATO_FECHA.format(fecha);
    }

    // Abre el formulario vacío o carga los datos del periodo seleccionado.
    private void abrirFormulario(boolean esEdicion) {
        String nombre = "";
        Date fechaInicio = null;
        Date fechaFin = null;
        boolean estadoActivo = true;
        idEnEdicion = 0;

        if (esEdicion) {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            idEnEdicion = (Integer) modeloTabla.getValueAt(fila, 0);
            nombre = (String) modeloTabla.getValueAt(fila, 1);
            Periodo periodo = controlador.buscarPorId(idEnEdicion)
                    .orElseThrow(() -> new IllegalStateException("El periodo seleccionado ya no existe."));
            fechaInicio = periodo.getStartDate();
            fechaFin = periodo.getEndDate();
            estadoActivo = "Activo".equals(modeloTabla.getValueAt(fila, 4));
        }

        dialogoFormulario = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                esEdicion ? "Editar periodo" : "Registrar periodo",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialogoFormulario.setSize(520, 390);
        dialogoFormulario.setLocationRelativeTo(this);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titulo = new JLabel(esEdicion ? "Editar periodo" : "Registrar periodo");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COLOR_PRIMARIO);
        formulario.add(titulo, gbc);

        gbc.gridwidth = 1;
        campoNombre = agregarCampo(formulario, gbc, 1, "Nombre:", nombre);

        campoFechaInicio = crearSelectorFecha(fechaInicio);
        agregarComponente(formulario, gbc, 2, "Fecha inicio:", campoFechaInicio);

        campoFechaFin = crearSelectorFecha(fechaFin);
        agregarComponente(formulario, gbc, 3, "Fecha fin:", campoFechaFin);

        campoEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        campoEstado.setSelectedIndex(estadoActivo ? 0 : 1);
        agregarComponente(formulario, gbc, 4, "Estado:", campoEstado);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonGuardar = crearBoton("Guardar", COLOR_EXITO);
        botonGuardar.addActionListener(e -> guardarPeriodo(esEdicion));

        JButton botonCancelar = crearBoton("Cancelar", COLOR_NEUTRO);
        botonCancelar.addActionListener(e -> dialogoFormulario.dispose());

        panelBotones.add(botonGuardar);
        panelBotones.add(botonCancelar);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formulario.add(panelBotones, gbc);

        dialogoFormulario.add(formulario);
        dialogoFormulario.setVisible(true);
    }

    private JDateChooser crearSelectorFecha(Date fecha) {
        JDateChooser selector = new JDateChooser();
        selector.setDateFormatString("yyyy-MM-dd");
        selector.setDate(fecha);
        return selector;
    }

    private JTextField agregarCampo(
            JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, String valor) {
        JTextField campo = new JTextField(valor, 20);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        agregarComponente(panel, gbc, fila, etiqueta, campo);
        return campo;
    }

    private void agregarComponente(
            JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, java.awt.Component componente) {
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(componente, gbc);
    }

    // Valida que se hayan seleccionado ambas fechas y delega el guardado al controlador.
    private void guardarPeriodo(boolean esEdicion) {
        String nombre = campoNombre.getText().trim();
        Date fechaInicio = campoFechaInicio.getDate();
        Date fechaFin = campoFechaFin.getDate();
        if (fechaInicio == null || fechaFin == null) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario,
                    "Selecciona las fechas de inicio y fin.",
                    "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean estado = campoEstado.getSelectedIndex() == 0;
        Timestamp inicio = new Timestamp(fechaInicio.getTime());
        Timestamp fin = new Timestamp(fechaFin.getTime());
        try {
            if (esEdicion) {
                controlador.actualizar(idEnEdicion, nombre, inicio, fin, estado);
                JOptionPane.showMessageDialog(
                        dialogoFormulario, "Periodo actualizado.", TITULO, JOptionPane.INFORMATION_MESSAGE);
            } else {
                controlador.registrar(nombre, inicio, fin, estado);
                JOptionPane.showMessageDialog(
                        dialogoFormulario, "Periodo registrado.", TITULO, JOptionPane.INFORMATION_MESSAGE);
            }
            dialogoFormulario.dispose();
            cargarPeriodos();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario, ex.getMessage(), "Validacion", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPeriodo() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 1);
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Eliminar el periodo " + nombre + "?\nEsta accion no se puede deshacer.",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.eliminar(id);
            mostrarMensaje("Periodo eliminado.", JOptionPane.INFORMATION_MESSAGE);
            cargarPeriodos();
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
