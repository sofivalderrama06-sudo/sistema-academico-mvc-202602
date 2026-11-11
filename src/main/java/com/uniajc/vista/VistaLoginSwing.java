package com.uniajc.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.uniajc.controlador.ControladorLogin;
import com.uniajc.modelo.Usuario;

/**
 * Ventana de login (JDialog modal con un JPanel adentro).
 * Es modal: el codigo que la abre se detiene en setVisible(true) hasta que
 * se cierre. Despues se consulta getUsuario(): si es null, no hubo login.
 */
public class VistaLoginSwing extends JDialog {

    private static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    private static final Color COLOR_EXITO = new Color(39, 174, 96);
    private static final Color COLOR_NEUTRO = new Color(149, 165, 166);
    private static final String TITULO = "Iniciar sesion";

    private final ControladorLogin controlador = new ControladorLogin();

    private JTextField campoUsuario;
    private JPasswordField campoClave;
    private Usuario usuario; // null mientras no se haya autenticado

    public VistaLoginSwing() {
        super((Frame) null, TITULO, true);
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titulo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titulo = new JLabel("Sistema Academico", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(COLOR_PRIMARIO);
        panel.add(titulo, gbc);

        gbc.gridy = 1;
        JLabel subtitulo = new JLabel("Ingresa con tu usuario", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(subtitulo, gbc);

        // Campos
        gbc.gridwidth = 1;
        campoUsuario = new JTextField(18);
        campoClave = new JPasswordField(18);
        agregarFila(panel, gbc, 2, "Usuario:", campoUsuario);
        agregarFila(panel, gbc, 3, "Contrasena:", campoClave);

        // Enter en cualquiera de los dos campos intenta el login
        campoUsuario.addActionListener(e -> realizarLogin());
        campoClave.addActionListener(e -> realizarLogin());

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);

        JButton botonIngresar = crearBoton("Ingresar", COLOR_EXITO);
        botonIngresar.addActionListener(e -> realizarLogin());

        JButton botonSalir = crearBoton("Salir", COLOR_NEUTRO);
        botonSalir.addActionListener(e -> dispose());

        panelBotones.add(botonIngresar);
        panelBotones.add(botonSalir);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        getRootPane().setDefaultButton(botonIngresar);
        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta,
                             java.awt.Component campo) {
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(label, gbc);

        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(campo, gbc);
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

    // ------------------------------------------------------------------
    // Accion
    // ------------------------------------------------------------------

    private void realizarLogin() {
        String username = campoUsuario.getText();
        char[] clave = campoClave.getPassword();
        try {
            Optional<Usuario> resultado = controlador.iniciarSesion(username, new String(clave));
            if (resultado.isPresent()) {
                usuario = resultado.get();
                dispose(); // cierra el dialogo y libera al codigo que lo abrio
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contrasena incorrectos.", TITULO,
                        JOptionPane.ERROR_MESSAGE);
                campoClave.setText("");
                campoClave.requestFocusInWindow();
            }
        } catch (IllegalArgumentException ex) {
            // Campo vacio
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validacion", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(clave, '\0'); // no dejamos la contrasena en memoria
        }
    }

    /** @return el usuario autenticado, o null si se cerro la ventana sin ingresar. */
    public Usuario getUsuario() {
        return usuario;
    }
}