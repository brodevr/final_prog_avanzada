package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Sistema de diseño centralizado para el café.
 * Todos los paneles usan estas constantes y helpers para garantizar
 * consistencia visual sin duplicar código.
 */
public final class EstiloUI {

    private EstiloUI() {}

    // ------------------------------------------------------------------
    // Paleta de colores
    // ------------------------------------------------------------------
    public static final Color C_CAFE     = new Color(52, 32, 18);
    public static final Color C_CARAMEL  = new Color(150, 88, 38);
    public static final Color C_CREMA    = new Color(252, 246, 237);
    public static final Color C_LIBRE    = new Color(46, 139, 87);
    public static final Color C_OCUPADA  = new Color(185, 55, 40);
    public static final Color C_BORDE    = new Color(210, 190, 165);
    public static final Color C_FILA_ALT = new Color(253, 246, 235);
    public static final Color C_NAVBAR   = new Color(40, 24, 12);

    // ------------------------------------------------------------------
    // Tipografía Segoe UI
    // ------------------------------------------------------------------
    public static final Font F_TITULO  = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font F_SECCION = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font F_NORMAL  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_NEGRITA = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font F_TOTAL   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font F_MONO    = new Font("Consolas", Font.PLAIN, 13);
    public static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_NAVBAR  = new Font("Segoe UI", Font.PLAIN, 13);

    // ------------------------------------------------------------------
    // Botones
    // ------------------------------------------------------------------

    private static JButton btnSolido(String texto, Color fondo, Color fg) {
        JButton b = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(getModel().isPressed()
                        ? fondo.darker() : getModel().isRollover() ? fondo.brighter() : fondo);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        b.setFont(F_NEGRITA);
        b.setBackground(fondo);
        b.setForeground(fg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        return b;
    }

    public static JButton btnPrimario(String texto) {
        return btnSolido(texto, C_CAFE, Color.WHITE);
    }

    public static JButton btnVerde(String texto) {
        return btnSolido(texto, C_LIBRE, Color.WHITE);
    }

    public static JButton btnRojo(String texto) {
        return btnSolido(texto, C_OCUPADA, Color.WHITE);
    }

    public static JButton btnAcento(String texto) {
        return btnSolido(texto, C_CARAMEL, Color.WHITE);
    }

    /** Botón con borde fino para acciones secundarias. */
    public static JButton btnBorde(String texto) {
        JButton b = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(getModel().isRollover() ? new Color(245, 235, 220) : Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        b.setFont(F_NORMAL);
        b.setForeground(C_CAFE);
        b.setBackground(Color.WHITE);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE, 1),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        return b;
    }

    /** Botón para la barra de navegación sobre fondo oscuro. */
    public static JButton btnNavbar(String texto) {
        final Color bg = new Color(65, 42, 26);
        JButton b = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        b.setFont(F_NAVBAR);
        b.setForeground(new Color(210, 185, 155));
        b.setBackground(bg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }

    // ------------------------------------------------------------------
    // Componentes de layout
    // ------------------------------------------------------------------

    /** Barra de sección con fondo crema oscuro: reemplaza TitledBorder. */
    public static JPanel barraTitulo(String texto) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(230, 212, 190));
        p.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        JLabel l = new JLabel(texto);
        l.setFont(F_NEGRITA);
        l.setForeground(C_CAFE);
        p.add(l, BorderLayout.WEST);
        return p;
    }

    /**
     * Barra de acciones para los ABM.
     *
     * Las acciones habituales van agrupadas a la izquierda y la destructiva se
     * manda al extremo opuesto. Separarlas no es capricho estetico: evita que
     * un clic apurado sobre "Desactivar" termine en "Eliminar", que no tiene
     * vuelta atras. El gap de 12px tambien da aire suficiente para no errarle
     * al boton de al lado.
     */
    public static JPanel barraAcciones(JButton[] habituales, JButton destructivo) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(248, 242, 234));
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDE));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        izquierda.setOpaque(false);
        for (JButton boton : habituales) {
            izquierda.add(boton);
        }
        barra.add(izquierda, BorderLayout.WEST);

        if (destructivo != null) {
            JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            derecha.setOpaque(false);
            derecha.add(destructivo);
            barra.add(derecha, BorderLayout.EAST);
        }
        return barra;
    }

    /** Card con sombra y borde fino, para agrupar contenido. */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        return p;
    }

    /** ScrollPane con borde fino del sistema de diseño. */
    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDE));
        return sp;
    }

    /** Campo de texto estilizado. */
    public static JTextField campo(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(F_NORMAL);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    public static JPasswordField campoClave(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(F_NORMAL);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return pf;
    }

    // ------------------------------------------------------------------
    // Tabla
    // ------------------------------------------------------------------

    /** Aplica el estilo unificado a cualquier JTable del sistema. */
    public static void estilizarTabla(JTable t) {
        t.setFont(F_NORMAL);
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(Color.WHITE);
        t.setSelectionBackground(new Color(195, 155, 105));
        t.setSelectionForeground(Color.WHITE);

        JTableHeader h = t.getTableHeader();
        h.setFont(F_NEGRITA);
        h.setBackground(new Color(65, 42, 26));
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(0, 32));
        h.setBorder(null);
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBackground(new Color(65, 42, 26));
                setForeground(Color.WHITE);
                setFont(F_NEGRITA);
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : C_FILA_ALT);
                    setForeground(C_CAFE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }
}
