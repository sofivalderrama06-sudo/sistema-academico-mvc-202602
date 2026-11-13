package com.uniajc.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.uniajc.controlador.ControladorDocente;
import com.uniajc.controlador.ControladorGrupo;
import com.uniajc.controlador.ControladorMateria;
import com.uniajc.modelo.Docente;
import com.uniajc.modelo.Grupo;
import com.uniajc.modelo.Materia;

/**
 * Vista Swing para consultar y administrar grupos.
 * La vista captura las acciones del usuario y delega las operaciones al controlador.
 */
public class VistaGrupo extends JPanel implements Vista {

    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);
    private static final Color COLOR_ACENTO = new Color(231, 76, 60);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);

    private static final String TITULO = "Gestion de grupos";
    private static final String[] COLUMNAS = {
            "ID", "Materia", "Docente", "Aula", "Horario", "ID materia", "ID docente"
    };

    private final ControladorGrupo controlador = new ControladorGrupo();
    private final ControladorMateria controladorMateria = new ControladorMateria();
    private final ControladorDocente controladorDocente = new ControladorDocente();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private Map<Integer, String> nombresMaterias = new HashMap<>();
    private Map<Integer, String> nombresDocentes = new HashMap<>();
    private JTextField textoBusqueda;
    private JDialog dialogoFormulario;
    // Combos que almacenan los objetos completos (Materia/Docente) para obtener sus IDs al guardar,
    // pero muestran solo el nombre en la interfaz gracias al renderer personalizado.
    private JComboBox<Materia> campoIdMateria;
    private JComboBox<Docente> campoIdDocente;
    private JTextField campoAula;
    private JTextField campoHorario;
    private int idEnEdicion;

    public VistaGrupo() {
        initComponents();
        cargarGrupos();
    }

    @Override
    public void actualizarPanel() {
        cargarGrupos();
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
        textoBusqueda.addActionListener(e -> buscarGrupos());

        JButton botonBuscar = crearBoton("Buscar", COLOR_PRIMARIO);
        botonBuscar.addActionListener(e -> buscarGrupos());

        JButton botonLimpiar = crearBoton("Limpiar", COLOR_NEUTRO);
        botonLimpiar.addActionListener(e -> {
            textoBusqueda.setText("");
            cargarGrupos();
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

        // El modelo de tabla incluye 7 columnas (índices 0-6), pero las dos últimas
        // (índices 5 y 6) son los IDs de materia y docente que se ocultan visualmente.
        // Se usan internamente al editar para saber qué IDs preseleccionar en los combos.
        tabla.removeColumn(tabla.getColumnModel().getColumn(6));
        tabla.removeColumn(tabla.getColumnModel().getColumn(5));

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
        botonEliminar.addActionListener(e -> eliminarGrupo());

        JButton botonRefrescar = crearBoton("Refrescar", COLOR_NEUTRO);
        botonRefrescar.addActionListener(e -> cargarGrupos());

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

    private void cargarGrupos() {
        try {
            cargarNombresRelacionados();
            mostrarEnTabla(controlador.listar());
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarGrupos() {
        String busqueda = textoBusqueda.getText().trim().toLowerCase(Locale.ROOT);
        if (busqueda.isEmpty()) {
            cargarGrupos();
            return;
        }

        try {
            cargarNombresRelacionados();
            modeloTabla.setRowCount(0);
            for (Grupo grupo : controlador.listar()) {
                if (coincide(grupo.getId(), busqueda)
                        || coincide(grupo.getId_materia(), busqueda)
                        || coincide(grupo.getId_docente(), busqueda)
                        || coincide(nombreMateria(grupo.getId_materia()), busqueda)
                        || coincide(nombreDocente(grupo.getId_docente()), busqueda)
                        || coincide(grupo.getClassroom(), busqueda)
                        || coincide(grupo.getSchedule(), busqueda)
                        || coincide(grupo.getCreated_at(), busqueda)) {
                    agregarFila(grupo);
                }
            }
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carga en memoria los mapas ID -> Nombre de materias y docentes para resolver
    // los nombres al mostrar la tabla (evita consultar BD por cada fila).
    private void cargarNombresRelacionados() {
        Map<Integer, String> materias = new HashMap<>();
        for (Materia materia : controladorMateria.listar()) {
            materias.put(materia.getId(), materia.getName());
        }

        Map<Integer, String> docentes = new HashMap<>();
        for (Docente docente : controladorDocente.listar()) {
            docentes.put(docente.getId(), docente.getNombre());
        }

        nombresMaterias = materias;
        nombresDocentes = docentes;
    }

    private boolean coincide(Object valor, String busqueda) {
        return valor != null && valor.toString().toLowerCase(Locale.ROOT).contains(busqueda);
    }

    // Resuelve el nombre de la materia a partir de su ID usando el mapa cargado en memoria.
    // Si no existe en el mapa, devuelve un fallback con el ID.
    private String nombreMateria(int idMateria) {
        return nombresMaterias.getOrDefault(idMateria, "Materia #" + idMateria);
    }

    // Resuelve el nombre del docente a partir de su ID usando el mapa cargado en memoria.
    // Si no existe en el mapa, devuelve un fallback con el ID.
    private String nombreDocente(int idDocente) {
        return nombresDocentes.getOrDefault(idDocente, "Docente #" + idDocente);
    }

    private void mostrarEnTabla(List<Grupo> grupos) {
        modeloTabla.setRowCount(0);
        for (Grupo grupo : grupos) {
            agregarFila(grupo);
        }
    }

    // Agrega una fila a la tabla mostrando los NOMBRES de materia y docente (no sus IDs).
    // No se muestra la fecha de creación en la vista, aunque sí se almacena en la base de datos.
    // Los IDs se guardan en columnas ocultas (índices 5 y 6) para usarlos al editar.
    private void agregarFila(Grupo grupo) {
        modeloTabla.addRow(new Object[]{
                grupo.getId(),
                nombreMateria(grupo.getId_materia()),   // Nombre resuelto desde el mapa
                nombreDocente(grupo.getId_docente()),   // Nombre resuelto desde el mapa
                grupo.getClassroom(),
                grupo.getSchedule(),
                grupo.getId_materia(),  // Columna oculta: ID materia para edición
                grupo.getId_docente()   // Columna oculta: ID docente para edición
        });
    }

    private void abrirFormulario(boolean esEdicion) {
        int idMateria = 0;
        int idDocente = 0;
        String aula = "";
        String horario = "";
        idEnEdicion = 0;

        if (esEdicion) {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int filaModelo = tabla.convertRowIndexToModel(fila);
            idEnEdicion = ((Number) modeloTabla.getValueAt(filaModelo, 0)).intValue();
            // Obtiene los IDs de las columnas ocultas (índices 5 y 6) para preseleccionar en los combos
            idMateria = ((Number) modeloTabla.getValueAt(filaModelo, 5)).intValue();
            idDocente = ((Number) modeloTabla.getValueAt(filaModelo, 6)).intValue();
            aula = String.valueOf(modeloTabla.getValueAt(filaModelo, 3));
            horario = String.valueOf(modeloTabla.getValueAt(filaModelo, 4));
        }

        List<Materia> materias;
        List<Docente> docentes;
        try {
            materias = controladorMateria.listar();
            docentes = controladorDocente.listar();
        } catch (RuntimeException ex) {
            mostrarMensaje("Error al cargar materias o docentes: " + ex.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        dialogoFormulario = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                esEdicion ? "Editar grupo" : "Registrar grupo",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialogoFormulario.setSize(480, 330);
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
        JLabel titulo = new JLabel(esEdicion ? "Editar grupo" : "Registrar grupo");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COLOR_PRIMARIO);
        formulario.add(titulo, gbc);

        gbc.gridwidth = 1;
        // Los combos reciben la lista completa y el ID a preseleccionar (0 = sin selección)
        campoIdMateria = crearComboMateria(materias, idMateria);
        agregarCombo(formulario, gbc, 1, "Materia:", campoIdMateria);
        campoIdDocente = crearComboDocente(docentes, idDocente);
        agregarCombo(formulario, gbc, 2, "Docente:", campoIdDocente);
        campoAula = agregarCampo(formulario, gbc, 3, "Aula:", aula);
        campoHorario = agregarCampo(formulario, gbc, 4, "Horario:", horario);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonGuardar = crearBoton("Guardar", COLOR_EXITO);
        botonGuardar.addActionListener(e -> guardarGrupo(esEdicion));

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

    // Crea el combo de materias: guarda objetos Materia completos para acceder a su ID al guardar,
    // pero el renderer muestra solo el nombre (sin el ID) para mejor UX.
    private JComboBox<Materia> crearComboMateria(List<Materia> materias, int idSeleccionado) {
        JComboBox<Materia> combo = new JComboBox<>();
        combo.addItem(null); // Item vacío como placeholder
        Materia seleccionada = null;
        for (Materia materia : materias) {
            combo.addItem(materia);
            if (materia.getId() == idSeleccionado) {
                seleccionada = materia;
            }
        }
        combo.setSelectedItem(seleccionada);
        // Renderer personalizado: muestra solo el nombre de la materia en el combo desplegable
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText(valor == null
                        ? "Seleccione una materia"
                        : ((Materia) valor).getName()); // Solo el nombre, sin ID
                return this;
            }
        });
        return combo;
    }

    // Crea el combo de docentes: guarda objetos Docente completos para acceder a su ID al guardar,
    // pero el renderer muestra solo el nombre (sin el ID) para mejor UX.
    private JComboBox<Docente> crearComboDocente(List<Docente> docentes, int idSeleccionado) {
        JComboBox<Docente> combo = new JComboBox<>();
        combo.addItem(null); // Item vacío como placeholder
        Docente seleccionado = null;
        for (Docente docente : docentes) {
            combo.addItem(docente);
            if (docente.getId() == idSeleccionado) {
                seleccionado = docente;
            }
        }
        combo.setSelectedItem(seleccionado);
        // Renderer personalizado: muestra solo el nombre del docente en el combo desplegable
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText(valor == null
                        ? "Seleccione un docente"
                        : ((Docente) valor).getNombre()); // Solo el nombre, sin ID
                return this;
            }
        });
        return combo;
    }

    private void agregarCombo(
            JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComboBox<?> combo) {
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(label, gbc);

        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(combo, gbc);
    }

    private void guardarGrupo(boolean esEdicion) {
        // Obtiene los objetos completos seleccionados en los combos (no solo el texto visible)
        Materia materia = (Materia) campoIdMateria.getSelectedItem();
        Docente docente = (Docente) campoIdDocente.getSelectedItem();
        if (materia == null || docente == null) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario,
                    "Selecciona una materia y un docente.",
                    "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // El controlador recibe solo los IDs numéricos (extraídos de los objetos seleccionados),
        // no las etiquetas de texto que se muestran en los combos.
        int idMateria = materia.getId();
        int idDocente = docente.getId();
        String aula = campoAula.getText().trim();
        String horario = campoHorario.getText().trim();
        try {
            if (esEdicion) {
                controlador.actualizar(idEnEdicion, idMateria, idDocente, aula, horario);
                JOptionPane.showMessageDialog(
                        dialogoFormulario, "Grupo actualizado.", TITULO, JOptionPane.INFORMATION_MESSAGE);
            } else {
                controlador.registrar(idMateria, idDocente, aula, horario);
                JOptionPane.showMessageDialog(
                        dialogoFormulario, "Grupo registrado.", TITULO, JOptionPane.INFORMATION_MESSAGE);
            }
            dialogoFormulario.dispose();
            cargarGrupos();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario, ex.getMessage(), "Validacion", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    dialogoFormulario, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarGrupo() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) modeloTabla.getValueAt(fila, 0);
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Eliminar el grupo con ID " + id + "?\nEsta accion no se puede deshacer.",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.eliminar(id);
            mostrarMensaje("Grupo eliminado.", JOptionPane.INFORMATION_MESSAGE);
            cargarGrupos();
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
