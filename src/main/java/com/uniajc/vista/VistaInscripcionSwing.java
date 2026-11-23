package com.uniajc.vista;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.uniajc.controlador.ControladorEstudiante;
import com.uniajc.controlador.ControladorGrupo;
import com.uniajc.controlador.ControladorInscripcion;
import com.uniajc.controlador.ControladorMateria;
import com.uniajc.modelo.Estudiante;
import com.uniajc.modelo.Grupo;
import com.uniajc.modelo.Inscripcion;
import com.uniajc.modelo.Materia;
import com.uniajc.modelo.Usuario;
import com.uniajc.util.PermisoManager;

/**
 * ============================================================================
 * CAPA DE VISTA (View Layer - MVC con Swing)
 * ============================================================================
 * RESPONSABILIDAD EXCLUSIVA:
 * 1. Renderizar la interfaz gráfica de usuario (tablas, formularios, botones).
 * 2. Capturar eventos del usuario (clics, textos, selecciones de combos).
 * 3. Aplicar Control de Acceso Basado en Roles (RBAC) para ocultar/habilitar componentes.
 * 4. Despachar las acciones del usuario invocando a los CONTROLADORES pertinentes.
 * 
 * REGLA DE SEPARACIÓN DE CAPAS:
 * - NO se conecta directamente a la base de datos ni importa clases DAO (estricto MVC).
 * - Se comunica únicamente a través de ControladorInscripcion, ControladorGrupo, etc.
 * - Toda la lógica de negocio y SQL está delegada a las capas de Servicio y DAO.
 */
public class VistaInscripcionSwing extends JPanel implements Vista {

    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_SECUNDARIO = new Color(236, 240, 241);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);
    private static final Color COLOR_ACENTO = new Color(231, 76, 60);

    // Columnas de la tabla: se incluye explícitamente "Materia" para consulta de estudiantes y docentes
    private static final String[] COLUMNAS = {
            "ID", "Estudiante", "Materia", "Grupo / Horario", "Nota", "Estado", "id_estudiante", "id_grupo"
    };

    // CONTROLADORES: Únicos puntos de comunicación con la capa de negocio
    private final ControladorInscripcion controlador = new ControladorInscripcion();
    private final ControladorEstudiante controladorEstudiante = new ControladorEstudiante();
    private final ControladorGrupo controladorGrupo = new ControladorGrupo();
    private final ControladorMateria controladorMateria = new ControladorMateria();

    private final Map<Integer, String> nombresEstudiantes = new HashMap<>();
    private final Map<Integer, String> nombresMaterias = new HashMap<>();
    private final Map<Integer, Grupo> mapaGrupos = new HashMap<>();
    private final Map<Integer, String> datosGruposSP = new LinkedHashMap<>();

    // Usuario autenticado y datos de vinculación RBAC
    private final Usuario usuarioActual;
    private int idEstudianteVinculado;
    private int idDocenteVinculado;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField textoBusqueda;
    private JDialog dialogoFormulario;
    private JComboBox<Estudiante> campoIdEstudiante;
    private JComboBox<String> comboGruposSP;
    private JTextField campoFinalGrade;
    private JComboBox<String> campoStatus;
    private int idEnEdicion;

    // Botones de acción del panel inferior
    private JButton botonRegistrar;
    private JButton botonEditar;
    private JButton botonEliminar;
    private JButton botonRefrescar;

    /** Construye la vista por defecto (usado para compatibilidad y pruebas). */
    public VistaInscripcionSwing() {
        this(null);
    }

    /** Construye la vista aplicando permisos según el usuario conectado. */
    public VistaInscripcionSwing(Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario != null && usuario.getCorreo() != null) {
            this.idEstudianteVinculado = PermisoManager.obtenerIdEstudianteVinculado(usuario.getCorreo());
            this.idDocenteVinculado = PermisoManager.obtenerIdDocenteVinculado(usuario.getCorreo());
        }
        initComponents();
        cargarInscripciones();
    }

    /** Recarga los datos cada vez que se muestra el panel, cumpliendo con la interfaz Vista. */
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

        tabla.removeColumn(tabla.getColumnModel().getColumn(7));
        tabla.removeColumn(tabla.getColumnModel().getColumn(6));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARIO, 2));
        scroll.setPreferredSize(new Dimension(850, 350));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /** Crea los botones para registrar, editar, eliminar y refrescar aplicando RBAC. */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(COLOR_SECUNDARIO);

        botonRegistrar = crearBoton("Registrar", COLOR_EXITO);
        botonRegistrar.addActionListener(e -> abrirFormulario(false));

        botonEditar = crearBoton("Editar", COLOR_PRIMARIO);
        botonEditar.addActionListener(e -> abrirFormulario(true));

        botonEliminar = crearBoton("Eliminar", COLOR_ACENTO);
        botonEliminar.addActionListener(e -> eliminarInscripcion());

        botonRefrescar = crearBoton("Refrescar", COLOR_NEUTRO);
        botonRefrescar.addActionListener(e -> cargarInscripciones());

        String rol = usuarioActual != null && usuarioActual.getRol() != null ? usuarioActual.getRol() : "ADMIN";

        // ====================================================================
        // PERMISOS DE BOTONES SEGÚN EL ROL (RBAC)
        // ====================================================================
        // 1. Matricular/Registrar: solo ADMIN
        if (!PermisoManager.puedeCrearInscripcion(rol)) {
            botonRegistrar.setVisible(false);
        }

        // 2. Modificar/Calificar: ADMIN y DOCENTE (ESTUDIANTE solo consulta)
        if (!PermisoManager.puedeEditarInscripcion(rol)) {
            botonEditar.setVisible(false);
        } else if ("DOCENTE".equalsIgnoreCase(rol)) {
            botonEditar.setText("Asignar Nota");
            botonEditar.setPreferredSize(new Dimension(130, 35));
        }

        // 3. Eliminar inscripción: solo ADMIN
        if (!PermisoManager.puedeEliminarInscripcion(rol)) {
            botonEliminar.setVisible(false);
        }

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

    /**
     * Retorna las inscripciones filtradas según el rol del usuario conectado:
     * - ESTUDIANTE: Solo ve las calificaciones de su propio ID de estudiante.
     * - DOCENTE: Solo ve los estudiantes inscritos en los grupos que él dicta.
     * - ADMIN: Acceso global a todas las inscripciones del sistema.
     */
    private List<Inscripcion> obtenerInscripcionesPermitidas() {
        List<Inscripcion> todas = controlador.listar();
        String rol = usuarioActual != null && usuarioActual.getRol() != null ? usuarioActual.getRol() : "ADMIN";

        if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
            return todas.stream()
                    .filter(i -> i.getId_estudiante() == idEstudianteVinculado)
                    .toList();
        } else if ("DOCENTE".equalsIgnoreCase(rol)) {
            Set<Integer> gruposDelDocente = controladorGrupo.listar().stream()
                    .filter(g -> g.getId_docente() == idDocenteVinculado)
                    .map(Grupo::getId)
                    .collect(Collectors.toSet());

            return todas.stream()
                    .filter(i -> gruposDelDocente.contains(i.getId_grupo()))
                    .toList();
        }
        return todas;
    }

    /** Carga nombres relacionados y presenta las inscripciones autorizadas en la tabla. */
    private void cargarInscripciones() {
        try {
            cargarNombresRelacionados();
            mostrarEnTabla(obtenerInscripcionesPermitidas());
        } catch (RuntimeException ex) {
            mostrarMensaje("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Filtra las inscripciones autorizadas por sus datos visibles y relacionados. */
    private void buscarInscripciones() {
        String busqueda = textoBusqueda.getText().trim().toLowerCase(Locale.ROOT);
        if (busqueda.isEmpty()) {
            cargarInscripciones();
            return;
        }

        try {
            cargarNombresRelacionados();
            modeloTabla.setRowCount(0);
            for (Inscripcion inscripcion : obtenerInscripcionesPermitidas()) {
                if (coincide(inscripcion.getId(), busqueda)
                        || coincide(inscripcion.getId_estudiante(), busqueda)
                        || coincide(inscripcion.getId_grupo(), busqueda)
                        || coincide(nombreEstudiante(inscripcion.getId_estudiante()), busqueda)
                        || coincide(nombreMateriaPorGrupo(inscripcion.getId_grupo()), busqueda)
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

    /** Actualiza los mapas de nombres usados para presentar estudiantes, materias y grupos. */
    private void cargarNombresRelacionados() {
        nombresEstudiantes.clear();
        for (Estudiante estudiante : controladorEstudiante.listar()) {
            nombresEstudiantes.put(estudiante.getId(), estudiante.getNombre() + " " + estudiante.getApellido());
        }

        nombresMaterias.clear();
        for (Materia materia : controladorMateria.listar()) {
            nombresMaterias.put(materia.getId(), materia.getName());
        }

        // Obtiene datos de grupos a través del controlador (desacoplamiento MVC)
        datosGruposSP.clear();
        mapaGrupos.clear();
        datosGruposSP.putAll(controladorGrupo.obtenerGruposParaCombo());
        for (Grupo grupo : controladorGrupo.listar()) {
            mapaGrupos.put(grupo.getId(), grupo);
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

    /** Obtiene el nombre de la materia a la que pertenece el grupo para mostrarla al estudiante/docente. */
    private String nombreMateriaPorGrupo(int idGrupo) {
        Grupo g = mapaGrupos.get(idGrupo);
        if (g != null) {
            return nombresMaterias.getOrDefault(g.getId_materia(), "Materia #" + g.getId_materia());
        }
        return "Materia no asignada";
    }

    /** Obtiene la descripción del aula y horario del grupo asociado. */
    private String nombreGrupo(int idGrupo) {
        Grupo g = mapaGrupos.get(idGrupo);
        if (g != null) {
            return g.getClassroom() + " (" + g.getSchedule() + ")";
        }
        return "Grupo #" + idGrupo;
    }

    /** Forma la descripción del grupo para los combos incluyendo la materia, aula y horario. */
    private String descripcionGrupo(Grupo grupo) {
        String materia = nombresMaterias.getOrDefault(grupo.getId_materia(), "");
        String aulaHorario = grupo.getClassroom() + " / " + grupo.getSchedule();
        return materia.isEmpty() ? aulaHorario : materia + " [" + aulaHorario + "]";
    }

    /** Reemplaza el contenido de la tabla por la lista de inscripciones recibida. */
    private void mostrarEnTabla(List<Inscripcion> inscripciones) {
        modeloTabla.setRowCount(0);
        for (Inscripcion inscripcion : inscripciones) {
            agregarFila(inscripcion);
        }
    }

    /** Agrega a la tabla una inscripción incluyendo la columna Materia y mantiene sus IDs para edición. */
    private void agregarFila(Inscripcion inscripcion) {
        modeloTabla.addRow(new Object[] {
                inscripcion.getId(),
                nombreEstudiante(inscripcion.getId_estudiante()),
                nombreMateriaPorGrupo(inscripcion.getId_grupo()),
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
            // Índice 1: Estudiante | Índice 2: Materia | Índice 3: Grupo / Horario
            nota = ((Number) modeloTabla.getValueAt(filaModelo, 4)).doubleValue();
            estado = String.valueOf(modeloTabla.getValueAt(filaModelo, 5));
            idEstudiante = ((Number) modeloTabla.getValueAt(filaModelo, 6)).intValue();
            idGrupo = ((Number) modeloTabla.getValueAt(filaModelo, 7)).intValue();
        }

        List<Estudiante> estudiantes;
        try {
            estudiantes = controladorEstudiante.listar();
        } catch (RuntimeException ex) {
            mostrarMensaje("Error al cargar estudiantes: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String rol = usuarioActual != null && usuarioActual.getRol() != null ? usuarioActual.getRol() : "ADMIN";
        String tituloTexto = esEdicion ? "Editar inscripcion" : "Registrar inscripcion";
        if ("DOCENTE".equalsIgnoreCase(rol)) {
            tituloTexto = "Asignar / Registrar Calificación";
        }

        dialogoFormulario = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                tituloTexto,
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
        JLabel titulo = new JLabel(tituloTexto);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(COLOR_PRIMARIO);
        formulario.add(titulo, gbc);

        gbc.gridwidth = 1;
        campoIdEstudiante = crearComboEstudiante(estudiantes, idEstudiante);
        agregarCombo(formulario, gbc, 1, "Estudiante:", campoIdEstudiante);

        // Usa la función almacenada para poblar el combo de grupos
        cargarComboGruposSP(idGrupo);
        agregarCombo(formulario, gbc, 2, "Grupo:", comboGruposSP);

        campoFinalGrade = agregarCampo(formulario, gbc, 3, "Nota final:", String.valueOf(nota));

        // Combobox de estado: selección manual para ambos roles (ADMIN y DOCENTE)
        campoStatus = crearComboEstado(estado);
        campoStatus.setEnabled(true);
        agregarComboEstado(formulario, gbc, 4, "Estado:", campoStatus);

        // Si el usuario es DOCENTE, no altera la matrícula (estudiante ni grupo), pero SÍ califica y define estado manualmente
        if (!PermisoManager.puedeModificarEstructuraInscripcion(rol)) {
            campoIdEstudiante.setEnabled(false);
            comboGruposSP.setEnabled(false);
        }

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonGuardar = crearBoton("Guardar", COLOR_EXITO);
        botonGuardar.addActionListener(e -> guardarInscripcion(esEdicion));

        JButton botonCancelar = crearBoton("Cancelar", COLOR_NEUTRO);
        botonCancelar.addActionListener(e -> dialogoFormulario.dispose());

        panelBotones.add(botonGuardar);
        panelBotones.add(botonCancelar);

        int filaBotones = 5;
        gbc.gridx = 0;
        gbc.gridy = filaBotones;
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

    /** Carga el combo de grupos usando la función almacenada fn_consultar_grupos_combobox vía Controlador. */
    private void cargarComboGruposSP(int idSeleccionado) {
        datosGruposSP.clear();
        datosGruposSP.putAll(controladorGrupo.obtenerGruposParaCombo());

        comboGruposSP = new JComboBox<>();
        comboGruposSP.addItem(null);
        String seleccionado = null;
        for (Map.Entry<Integer, String> entry : datosGruposSP.entrySet()) {
            comboGruposSP.addItem(entry.getValue());
            if (entry.getKey() == idSeleccionado) {
                seleccionado = entry.getValue();
            }
        }
        comboGruposSP.setSelectedItem(seleccionado);
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

    /**
     * Valida los campos del formulario y registra o actualiza la inscripción.
     *   - DOCENTE editando → usa UPDATE directo (SQL plano) vía controlador.actualizar().
     *   - ADMIN registrando → llama al SP sp_inscribir_estudiante vía
     *     controlador.registrarConProcedimiento(). El SP valida unicidad en BD.
     *   - ADMIN editando → usa el UPDATE directo (SQL plano) vía controlador.actualizar().
     */
    private void guardarInscripcion(boolean esEdicion) {
        Estudiante estudiante = (Estudiante) campoIdEstudiante.getSelectedItem();

        // Obtener ID del grupo desde el combo SP (buscar por descripción)
        String grupoDesc = (String) comboGruposSP.getSelectedItem();
        Integer idGrupo = null;
        if (grupoDesc != null) {
            for (Map.Entry<Integer, String> entry : datosGruposSP.entrySet()) {
                if (entry.getValue().equals(grupoDesc)) {
                    idGrupo = entry.getKey();
                    break;
                }
            }
        }

        if (estudiante == null || idGrupo == null) {
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
                // ACTUALIZACIÓN MANUAL: Aplica para ambos roles (ADMIN y DOCENTE)
                // Permite registrar la nota y seleccionar el estado manualmente en el ComboBox
                controlador.actualizar(idEnEdicion, estudiante.getId(), idGrupo, nota, estado);

            } else {
                // PROCEDIMIENTO ALMACENADO: sp_inscribir_estudiante
                // Matricula el estudiante nuevo delegando al SP con validaciones de unicidad en BD
                controlador.registrarConProcedimiento(
                        estudiante.getId(), idGrupo, nota, estado);
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
                // PROCEDIMIENTO ALMACENADO: sp_eliminar_inscripcion_segura
                // Valida en base de datos que no se pueda eliminar una inscripción con estado APROBADO
                controlador.eliminarConProcedimiento(id);
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
