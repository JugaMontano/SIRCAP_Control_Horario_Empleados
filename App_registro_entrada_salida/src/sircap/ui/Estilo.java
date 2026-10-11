package sircap.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/** Colores, fuentes y componentes reutilizables para que toda la aplicación se vea igual. */
public final class Estilo {
    public static final Color AZUL = new Color(0x1F3A5F);
    public static final Color AZUL_CLARO = new Color(0x2E6DA4);
    public static final Color FONDO = new Color(0xF4F6F9);
    public static final Color VERDE = new Color(0x2E8B57);
    public static final Color ROJO = new Color(0xC0392B);
    public static final Color AMBAR = new Color(0xB7791F);
    public static final Color GRIS = new Color(0x6B7280);
    public static final Color BORDE = new Color(0xD5DAE1);

    public static final Font FUENTE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font NEGRITA = new Font("SansSerif", Font.BOLD, 14);

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter HORA_SEG = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Estilo() { }

    public static void aplicarTema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorada) {
            // se queda con el aspecto por defecto
        }
        Enumeration<Object> llaves = UIManager.getDefaults().keys();
        while (llaves.hasMoreElements()) {
            Object llave = llaves.nextElement();
            if (UIManager.get(llave) instanceof FontUIResource) {
                UIManager.put(llave, new FontUIResource(FUENTE));
            }
        }
    }

    // ---------- Botón con color propio ----------
    public static class Boton extends JButton {
        private final Color base;

        public Boton(String texto, Color base) {
            super(texto);
            this.base = base;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setForeground(Color.WHITE);
            setFont(NEGRITA);
            setBorder(new EmptyBorder(9, 20, 9, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = base;
            if (!isEnabled()) c = new Color(0xB8BEC8);
            else if (getModel().isPressed()) c = base.darker();
            else if (getModel().isRollover()) c = base.brighter();
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton boton(String texto, Color color) {
        return new Boton(texto, color);
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(AZUL);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FUENTE);
        l.setForeground(GRIS);
        return l;
    }

    // ---------- Tablas ----------
    public static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    public static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int fila, int columna) {
                Component c = super.prepareRenderer(r, fila, columna);
                if (!isRowSelected(fila)) c.setBackground(fila % 2 == 0 ? Color.WHITE : new Color(0xF3F6FA));
                return c;
            }
        };
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0xCFE2F7));
        t.setSelectionForeground(Color.BLACK);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader h = t.getTableHeader();
        h.setFont(NEGRITA);
        h.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        return t;
    }

    /** Pone en color (verde / ámbar / rojo) el texto de una columna de estados. */
    public static void colorearEstado(JTable t, int columna) {
        t.getColumnModel().getColumn(columna).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foco, int f, int c) {
                Component comp = super.getTableCellRendererComponent(tb, v, sel, foco, f, c);
                String s = String.valueOf(v);
                Color color = Color.BLACK;
                if (s.equals("Aprobada")) color = VERDE;
                else if (s.equals("Rechazada")) color = ROJO;
                else if (s.equals("Pendiente")) color = AMBAR;
                else if (s.equals("Activo")) color = VERDE;
                else if (s.equals("Inactivo")) color = ROJO;
                comp.setForeground(color);
                comp.setFont(NEGRITA);
                return comp;
            }
        });
    }

    public static JScrollPane conScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDE));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ---------- Selectores de fecha y hora ----------
    public static JSpinner selectorFecha(LocalDate fecha) {
        Date d = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner s = new JSpinner(new SpinnerDateModel(d, null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy"));
        return s;
    }

    public static LocalDate fecha(JSpinner s) {
        try { s.commitEdit(); } catch (ParseException ignorada) { /* se usa el último valor válido */ }
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static JSpinner selectorHora(LocalTime hora) {
        Date d = Date.from(LocalDate.now().atTime(hora).atZone(ZoneId.systemDefault()).toInstant());
        JSpinner s = new JSpinner(new SpinnerDateModel(d, null, null, Calendar.MINUTE));
        s.setEditor(new JSpinner.DateEditor(s, "HH:mm"));
        return s;
    }

    public static LocalTime hora(JSpinner s) {
        try { s.commitEdit(); } catch (ParseException ignorada) { /* se usa el último valor válido */ }
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    // ---------- Mensajes ----------
    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "No se pudo completar", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SIRCAP", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- Tarjeta con un indicador ----------
    public static JPanel tarjeta(String titulo, JLabel valor, Color acento) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, acento),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), new EmptyBorder(12, 14, 12, 14))));
        JLabel t = new JLabel(titulo);
        t.setForeground(GRIS);
        valor.setFont(new Font("SansSerif", Font.BOLD, 30));
        valor.setForeground(AZUL);
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    /** Formulario sencillo de dos columnas: etiqueta + campo. */
    public static class Formulario extends JPanel {
        private int fila = 0;

        public Formulario() {
            super(new GridBagLayout());
            setOpaque(false);
        }

        public void agregar(String etiqueta, JComponent campo) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = fila; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(5, 0, 5, 12);
            JLabel l = new JLabel(etiqueta);
            l.setForeground(GRIS);
            add(l, c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5, 0, 5, 0);
            add(campo, c);
            fila++;
        }

        public void agregarAncho(JComponent componente) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = fila; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(10, 0, 0, 0);
            add(componente, c);
            fila++;
        }
    }

    /** Panel blanco con borde, para agrupar formularios. */
    public static JPanel caja() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    static void soloClic(JComponent c, Runnable accion) {
        c.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { accion.run(); }
        });
    }
}
