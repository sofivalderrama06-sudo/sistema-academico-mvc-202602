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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import com.uniajc.controlador.ControladorEstudiante;
import com.uniajc.controlador.ControladorGrupo;
import com.uniajc.controlador.ControladorInscripcion;
import com.uniajc.modelo.Estudiante;
import com.uniajc.modelo.Grupo;
import com.uniajc.modelo.Inscripcion;

public class VistaInscripcionSwing extends JPanel implements Vista {

    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);
    private static final Color COLOR_ACENTO = new Color(231, 76, 60);

    private static final String[] COLUMNAS = {"ID", "Estudiante", "Grupo", "Nota", "Estado", "id_estudiante", "id_grupo"};

    private final ControladorInscripcion controlador = new ControladorInscripcion();
    private final ControladorEstudiante controladorEstudiante = new ControladorEstudiante();
    private final ControladorGrupo controladorGrupo = new ControladorGrupo();

    private final Map<Integer, String> nombresEstudiantes = new HashMap<>();
    private final Map<Integer, String> nombresGrupos = new HashMap<>();

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField textoBusqueda;
    private JDialog dialogoFormulario;
    private JComboBox<Estudiante> campoIdEstudiante;
    private JComboBox<Grupo> campoIdGrupo;
    private JTextField campoFinalGrade;
    private JComboBox<String> campoStatus;
    private int idEnEdicion;

    /** Construye la vista y carga las inscripciones existentes. */
    public VistaInscripcionSwing() {
        initComponents();
        cargarInscripciones();
    }

    /** Recarga los datos cada vez que se muestra el panel. */
    @Override
    public void actualizarPanel() {
        cargarInscripciones();
    }

    /** Organiza los paneles de búsqueda, tabla y acciones. */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_SECUNDARIO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(crearPanelBusqueda(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);
    }

    /** Crea los controles para buscar y limpiar el filtro de inscripciones. */
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(COLOR_SECUNDARIO);

        JLabel etiqueta = new JLabel("Buscar:");
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiqueta.setForeground(COLOR_PRIMARIO);

        textoBusqueda = new JTextField(25);
        textoBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textoBusqueda.setPreferredSize(new Dimension(220, 30));
        textoBusqueda.addActionListener(e -> buscarInscripciones());

        JButton botonBuscar = crearBoton("Buscar", COLOR_PRIMARIO);
        botonBuscar.addActionListener(e -> buscarInscripciones());

        JButton botonLimpiar = crearBoton("Limpiar", COLOR_NEUTRO);
        botonLimpiar.addActionListener(e -> {
            textoBusqueda.setText("");
            cargarInscripciones();
        });

        panel.add(etiqueta);
        panel.add(textoBusqueda);
        panel.add(botonBuscar);
        panel.add(botonLimpiar);
        return panel;
    }

    /** Crea la tabla de inscripciones y oculta las columnas internas con IDs de FK. */
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_SECUNDARIO);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            /** Mantiene las celdas de la tabla solo para consulta. */
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

        tabla.removeColumn(tabla.getColumnModel().getColumn(6));
        tabla.removeColumn(tabla.getColumnModel().getColumn(5));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARIO, 2));
        scroll.setPreferredSize(new Dimension(750, 350));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /** Crea los botones para registrar, editar, eliminar y refrescar. */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(COLOR_SECUNDARIO);

        JButton botonRegistrar = crearBoton("Registrar", COLOR_EXITO);
        botonRegistrar.addActionListener(e -> abrirFormulario(false));

        JButton botonEditar = crearBoton("Editar", COLOR_PRIMARIO);
        botonEditar.addActionListener(e -> abrirFormulario(true));

        JButton botonEliminar = crearBoton("Eliminar", COLOR_ACENTO);
        botonEliminar.addActionListener(e -> eliminarInscripcion());

        JButton botonRefrescar = crearBoton("Refrescar", COLOR_NEUTRO);
        botonRefrescar.addActionListener(e -> cargarInscripciones());

        panel.add(botonRegistrar);
        panel.add(botonEditar);
        panel.add(botonEliminar);
        panel.add(botonRefrescar);
        return panel;
    }

    /** Aplica el estilo común a los botones de la vista. */
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

    /** Carga nombres relacionados y presenta las inscripciones en la tabla. */
    private void cargarInscripciones() {
        try {
            cargarNombresRelacionados();
            mostrarEnTabla(controlador.listar());
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Filtra las inscripciones por sus datos visibles y relacionados. */
    private void buscarInscripciones() {
        String busqueda = textoBusqueda.getText().trim().toLowerCase(Locale.ROOT);
        if (busqueda.isEmpty()) {
            cargarInscripciones();
            return;
        }

        try {
            cargarNombresRelacionados();
            modeloTabla.setRowCount(0);
            for (Inscripcion inscripcion : controlador.listar()) {
                if (coincide(inscripcion.getId(), busqueda)
                        || coincide(inscripcion.getId_estudiante(), busqueda)
                        || coincide(inscripcion.getId_grupo(), busqueda)
                        || coincide(nombreEstudiante(inscripcion.getId_estudiante()), busqueda)
                        || coincide(nombreGrupo(inscripcion.getId_grupo()), busqueda)
                        || coincide(inscripcion.getStatus(), busqueda)
                        || coincide(inscripcion.getFinal_grade(), busqueda)) {
                    agregarFila(inscripcion);
                }
            }
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Actualiza los mapas de nombres usados para presentar estudiantes y grupos. */
    private void cargarNombresRelacionados() {
        nombresEstudiantes.clear();
        for (Estudiante estudiante : controladorEstudiante.listar()) {
            nombresEstudiantes.put(estudiante.getId(), estudiante.getNombre() + " " + estudiante.getApellido());
        }

        nombresGrupos.clear();
        for (Grupo grupo : controladorGrupo.listar()) {
            nombresGrupos.put(grupo.getId(), descripcionGrupo(grupo));
        }
    }

    /** Indica si el valor recibido contiene el texto de búsqueda. */
    private boolean coincide(Object valor, String busqueda) {
        return valor != null && valor.toString().toLowerCase(Locale.ROOT).contains(busqueda);
    }

    /** Obtiene el nombre del estudiante asociado o una etiqueta de respaldo. */
    private String nombreEstudiante(int idEstudiante) {
        return nombresEstudiantes.getOrDefault(idEstudiante, "Estudiante #" + idEstudiante);
    }

    /** Obtiene la descripción guardada del grupo asociado. */
    private String nombreGrupo(int idGrupo) {
        return nombresGrupos.getOrDefault(idGrupo, "");
    }

    /** Forma la descripción del grupo con el aula y el horario almacenados. */
    private String descripcionGrupo(Grupo grupo) {
        return grupo.getClassroom() + " / " + grupo.getSchedule();
    }

    /** Reemplaza el contenido de la tabla por la lista de inscripciones recibida. */
    private void mostrarEnTabla(List<Inscripcion> inscripciones) {
        modeloTabla.setRowCount(0);
        for (Inscripcion inscripcion : inscripciones) {
            agregarFila(inscripcion);
        }
    }

    /** Agrega a la tabla una inscripción y mantiene sus IDs para edición. */
    private void agregarFila(Inscripcion inscripcion) {
        modeloTabla.addRow(new Object[] {
                inscripcion.getId(),
                nombreEstudiante(inscripcion.getId_estudiante()),
                nombreGrupo(inscripcion.getId_grupo()),
                inscripcion.getFinal_grade(),
                inscripcion.getStatus(),
                inscripcion.getId_estudiante(),
                inscripcion.getId_grupo()
        });
    }

    /** Abre el formulario de registro o carga los datos para editar una inscripción. */
    private void abrirFormulario(boolean esEdicion) {
        int idEstudiante = 0;
        int idGrupo = 0;
        double nota = 0.0;
        String estado = "ACTIVO";
        idEnEdicion = 0;

        if (esEdicion) {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarMensaje("Selecciona una fila de la tabla.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int filaModelo = tabla.convertRowIndexToModel(fila);
            idEnEdicion = ((Number) modeloTabla.getValueAt(filaModelo, 0)).intValue();
            idEstudiante = ((Number) modeloTabla.getValueAt(filaModelo, 5)).intValue();
            idGrupo = ((Number) modeloTabla.getValueAt(filaModelo, 6)).intValue();
            nota = ((Number) modeloTabla.getValueAt(filaModelo, 3)).doubleValue();
            estado = String.valueOf(modeloTabla.getValueAt(filaModelo, 4));
        }

        List<Estudiante> estudiantes;
        List<Grupo> grupos;
        try {
            estudiantes = controladorEstudiante.listar();
            grupos = controladorGrupo.listar();
        } catch (RuntimeException ex) {
            mostrarMensaje("Error al cargar estudiantes o grupos: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        dialogoFormulario = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                esEdicion ? "Editar inscripcion" : "Registrar inscripcion",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialogoFormulario.setSize(500, 330);
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
        JLabel titulo = new JLabel(esEdicion ? "Editar inscripcion" : "Registrar inscripcion");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COLOR_PRIMARIO);
        formulario.add(titulo, gbc);

        gbc.gridwidth = 1;
        campoIdEstudiante = crearComboEstudiante(estudiantes, idEstudiante);
        agregarCombo(formulario, gbc, 1, "Estudiante:", campoIdEstudiante);
        campoIdGrupo = crearComboGrupo(grupos, idGrupo);
        agregarCombo(formulario, gbc, 2, "Grupo:", campoIdGrupo);
        campoFinalGrade = agregarCampo(formulario, gbc, 3, "Nota final:", String.valueOf(nota));
        campoStatus = crearComboEstado(estado);
        agregarComboEstado(formulario, gbc, 4, "Estado:", campoStatus);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonGuardar = crearBoton("Guardar", COLOR_EXITO);
        botonGuardar.addActionListener(e -> guardarInscripcion(esEdicion));

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

    /** Agrega al formulario una etiqueta y un campo de texto en la fila indicada. */
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

    /** Crea un combo de estudiantes con objetos completos para obtener su ID al guardar. */
    private JComboBox<Estudiante> crearComboEstudiante(List<Estudiante> estudiantes, int idSeleccionado) {
        JComboBox<Estudiante> combo = new JComboBox<>();
        combo.addItem(null);
        Estudiante seleccionado = null;
        for (Estudiante estudiante : estudiantes) {
            combo.addItem(estudiante);
            if (estudiante.getId() == idSeleccionado) {
                seleccionado = estudiante;
            }
        }
        combo.setSelectedItem(seleccionado);
        combo.setRenderer(new DefaultListCellRenderer() {
            /** Muestra el nombre y apellido del estudiante en el combo. */
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText(valor == null
                        ? "Seleccione un estudiante"
                        : ((Estudiante) valor).getNombre() + " " + ((Estudiante) valor).getApellido());
                return this;
            }
        });
        return combo;
    }

    /** Crea un combo de grupos y preselecciona el grupo asociado al editar. */
    private JComboBox<Grupo> crearComboGrupo(List<Grupo> grupos, int idSeleccionado) {
        JComboBox<Grupo> combo = new JComboBox<>();
        combo.addItem(null);
        Grupo seleccionado = null;
        for (Grupo grupo : grupos) {
            combo.addItem(grupo);
            if (grupo.getId() == idSeleccionado) {
                seleccionado = grupo;
            }
        }
        combo.setSelectedItem(seleccionado);
        combo.setRenderer(new DefaultListCellRenderer() {
            /** Muestra en el combo únicamente el aula y horario del grupo. */
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText(valor == null
                        ? "Seleccione un grupo"
                        : descripcionGrupo((Grupo) valor));
                return this;
            }
        });
        return combo;
    }

    /** Crea el combo con los estados permitidos y selecciona el estado actual. */
    private JComboBox<String> crearComboEstado(String estadoActual) {
        String[] estados = {"ACTIVO", "APROBADO", "REPROBADO", "CANCELADO"};
        JComboBox<String> combo = new JComboBox<>(estados);
        if (estadoActual != null && !estadoActual.trim().isEmpty()) {
            combo.setSelectedItem(estadoActual.trim().toUpperCase(Locale.ROOT));
        }
        return combo;
    }

    /** Agrega al formulario una etiqueta y un combo en la fila indicada. */
    private void agregarCombo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComboBox<?> combo) {
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(label, gbc);

        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(combo, gbc);
    }

    /** Agrega al formulario una etiqueta y el combo de estado. */
    private void agregarComboEstado(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComboBox<String> combo) {
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(label, gbc);

        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(combo, gbc);
    }

    /** Valida los campos del formulario y registra o actualiza la inscripción. */
    private void guardarInscripcion(boolean esEdicion) {
        Estudiante estudiante = (Estudiante) campoIdEstudiante.getSelectedItem();
        Grupo grupo = (Grupo) campoIdGrupo.getSelectedItem();

        if (estudiante == null || grupo == null) {
            JOptionPane.showMessageDialog(dialogoFormulario,
                    "Selecciona un estudiante y un grupo.",
                    "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String textoNota = campoFinalGrade.getText().trim();
        double nota;
        try {
            nota = Double.parseDouble(textoNota);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialogoFormulario,
                    "La nota final debe ser un numero valido entre 0 y 5.",
                    "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String estado = (String) campoStatus.getSelectedItem();
        if (estado == null || estado.trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialogoFormulario,
                    "Selecciona un estado.",
                    "Validacion",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (esEdicion) {
                controlador.actualizar(idEnEdicion, estudiante.getId(), grupo.getId(), nota, estado);
            } else {
                controlador.registrar(estudiante.getId(), grupo.getId(), nota, estado);
            }
            dialogoFormulario.dispose();
            cargarInscripciones();
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Solicita confirmación y elimina la inscripción seleccionada. */
    private void eliminarInscripcion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Selecciona una fila de la tabla para eliminar.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);
        int id = ((Number) modeloTabla.getValueAt(filaModelo, 0)).intValue();
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "Deseas eliminar la inscripcion seleccionada?",
                "Confirmacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                controlador.eliminar(id);
                cargarInscripciones();
            } catch (RuntimeException ex) {
                mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Presenta un mensaje informativo o de error al usuario. */
    private void mostrarMensaje(String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, "Sistema Academico", tipo);
    }
}
