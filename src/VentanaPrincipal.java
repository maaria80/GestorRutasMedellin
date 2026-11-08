import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Locale;

/** Interfaz gráfica del gestor de rutas. */
public class VentanaPrincipal extends JFrame {

    private final ListaParadas ruta = new ListaParadas();

    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtLatitud = new JTextField(18);
    private final JTextField txtLongitud = new JTextField(18);
    private final JTextField txtTiempo = new JTextField(18);
    private final JTextArea txtDescripcion = new JTextArea(3, 18);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"#", "Nombre", "Latitud", "Longitud", "Llegada (min)", "Dist. anterior (km)", "Descripción"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final MapaRuta mapa = new MapaRuta();
    private final JLabel lblDistancia = new JLabel("Distancia total: 0.00 km");

    public VentanaPrincipal() {
        super("Gestor de rutas de transporte - Lista ligada de paradas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabla), mapa);
        division.setResizeWeight(0.3);
        add(crearFormulario(), BorderLayout.WEST);
        add(division, BorderLayout.CENTER);
        add(crearBotonesInferiores(), BorderLayout.SOUTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(40);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mapa.setSeleccionada(tabla.getSelectedRow());
        });
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        // Doble clic en el mapa -> llena latitud y longitud del formulario
        mapa.setAlDobleClic(gp -> {
            txtLatitud.setText(String.format(Locale.US, "%.6f", gp.getLatitude()));
            txtLongitud.setText(String.format(Locale.US, "%.6f", gp.getLongitude()));
        });

        setSize(1150, 760);
        setLocationRelativeTo(null);
        division.setDividerLocation(230);
    }

    // ---------- Construcción de la interfaz ----------
    private JPanel crearFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Datos de la parada"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        String[] etiquetas = {"Nombre:", "Latitud:", "Longitud:",
                "Tiempo estimado (min):", "Descripción (opcional):"};
        JComponent[] campos = {txtNombre, txtLatitud, txtLongitud, txtTiempo,
                new JScrollPane(txtDescripcion)};
        for (int i = 0; i < etiquetas.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            p.add(new JLabel(etiquetas[i]), g);
            g.gridx = 1; g.weightx = 1;
            p.add(campos[i], g);
        }

        JPanel botones = new JPanel(new GridLayout(2, 2, 6, 6));
        botones.add(boton("Agregar parada", e -> agregar()));
        botones.add(boton("Seleccionar parada", e -> seleccionar()));
        botones.add(boton("Modificar parada", e -> modificar()));
        botones.add(boton("Eliminar parada", e -> eliminar()));
        g.gridx = 0; g.gridy = etiquetas.length; g.gridwidth = 2;
        g.insets = new Insets(12, 4, 4, 4);
        p.add(botones, g);

        // Empuja todo hacia arriba
        g.gridy = etiquetas.length + 1; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        p.add(Box.createVerticalGlue(), g);
        return p;
    }

    private JPanel crearBotonesInferiores() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.add(boton("Mostrar paradas", e -> { refrescarTabla(); mapa.ajustar(); }));
        p.add(boton("Guardar ruta", e -> guardar()));
        p.add(boton("Cargar ruta", e -> cargar()));
        p.add(boton("Calcular distancia total", e -> calcularDistancia()));
        p.add(boton("Abrir en Google Maps", e -> abrirGoogleMaps()));
        p.add(boton("Limpiar formulario", e -> limpiarFormulario()));
        lblDistancia.setFont(lblDistancia.getFont().deriveFont(Font.BOLD));
        p.add(lblDistancia);
        return p;
    }

    private JButton boton(String texto, java.awt.event.ActionListener al) {
        JButton b = new JButton(texto);
        b.addActionListener(al);
        return b;
    }

    // ---------- Operaciones ----------
    private void agregar() {
        Parada p = leerFormulario();
        if (p == null) return;
        ruta.agregar(p);
        refrescarTabla();
        mapa.ajustar();
        limpiarFormulario();
    }

    private void seleccionar() {
        int i = tabla.getSelectedRow();
        if (i < 0) { aviso("Seleccione una parada de la tabla."); return; }
        Parada p = ruta.seleccionar(i);
        txtNombre.setText(p.getNombre());
        txtLatitud.setText(String.valueOf(p.getLatitud()));
        txtLongitud.setText(String.valueOf(p.getLongitud()));
        txtTiempo.setText(String.valueOf(p.getTiempoEstimado()));
        txtDescripcion.setText(p.getDescripcion());
    }

    private void modificar() {
        int i = tabla.getSelectedRow();
        if (i < 0) { aviso("Seleccione la parada que desea modificar."); return; }
        Parada p = leerFormulario();
        if (p == null) return;
        ruta.modificar(i, p);
        refrescarTabla();
        tabla.setRowSelectionInterval(i, i);
    }

    private void eliminar() {
        int i = tabla.getSelectedRow();
        if (i < 0) { aviso("Seleccione la parada que desea eliminar."); return; }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la parada \"" + ruta.seleccionar(i).getNombre() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            ruta.eliminar(i);
            refrescarTabla();
            mapa.ajustar();
            limpiarFormulario();
        }
    }

    private void guardar() {
        if (ruta.estaVacia()) { aviso("La ruta no tiene paradas."); return; }
        JFileChooser fc = new JFileChooser(new File("."));
        fc.setSelectedFile(new File("ruta.json"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".json"))
            f = new File(f.getPath() + ".json");
        try {
            JsonUtil.guardar(ruta, f);
            JOptionPane.showMessageDialog(this, "Ruta guardada en:\n" + f.getAbsolutePath());
        } catch (Exception ex) {
            error("No se pudo guardar: " + ex.getMessage());
        }
    }

    private void cargar() {
        JFileChooser fc = new JFileChooser(new File("."));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            ListaParadas leida = JsonUtil.cargar(fc.getSelectedFile());
            ruta.limpiar();
            for (Parada p : leida.aLista()) ruta.agregar(p);
            refrescarTabla();
            mapa.ajustar();
            limpiarFormulario();
        } catch (Exception ex) {
            error("No se pudo cargar el archivo: " + ex.getMessage());
        }
    }

    private void calcularDistancia() {
        if (ruta.tamano() < 2) { aviso("Se necesitan al menos 2 paradas."); return; }
        JOptionPane.showMessageDialog(this,
                String.format("Distancia total aproximada (línea recta entre paradas):\n%.2f km",
                        ruta.distanciaTotalKm()));
        actualizarDistancia();
    }

    private void abrirGoogleMaps() {
        if (ruta.tamano() < 2) { aviso("Se necesitan al menos 2 paradas."); return; }
        StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/");
        for (Parada p : ruta.aLista())
            url.append(p.getLatitud()).append(",").append(p.getLongitud()).append("/");
        try {
            Desktop.getDesktop().browse(new URI(url.toString()));
        } catch (Exception ex) {
            error("No se pudo abrir el navegador: " + ex.getMessage());
        }
    }

    // ---------- Utilidades ----------
    private Parada leerFormulario() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) { aviso("El nombre de la parada es obligatorio."); return null; }
        try {
            double lat = Double.parseDouble(txtLatitud.getText().trim().replace(',', '.'));
            double lon = Double.parseDouble(txtLongitud.getText().trim().replace(',', '.'));
            int t = Integer.parseInt(txtTiempo.getText().trim());
            if (lat < -90 || lat > 90) { aviso("La latitud debe estar entre -90 y 90."); return null; }
            if (lon < -180 || lon > 180) { aviso("La longitud debe estar entre -180 y 180."); return null; }
            if (t < 0) { aviso("El tiempo estimado no puede ser negativo."); return null; }
            return new Parada(nombre, lat, lon, t, txtDescripcion.getText().trim());
        } catch (NumberFormatException ex) {
            aviso("Latitud, longitud y tiempo deben ser numéricos (el tiempo, entero).");
            return null;
        }
    }

    private void refrescarTabla() {
        modelo.setRowCount(0);
        List<Parada> ps = ruta.aLista();
        Parada anterior = null;
        for (int i = 0; i < ps.size(); i++) {
            Parada p = ps.get(i);
            String dist = anterior == null ? "-" : String.format("%.2f", anterior.distanciaA(p));
            modelo.addRow(new Object[]{i + 1, p.getNombre(), p.getLatitud(), p.getLongitud(),
                    p.getTiempoEstimado(), dist, p.getDescripcion()});
            anterior = p;
        }
        mapa.setParadas(ps);
        actualizarDistancia();
    }

    private void actualizarDistancia() {
        lblDistancia.setText(String.format("Distancia total: %.2f km", ruta.distanciaTotalKm()));
    }

    private void limpiarFormulario() {
        txtNombre.setText(""); txtLatitud.setText(""); txtLongitud.setText("");
        txtTiempo.setText(""); txtDescripcion.setText("");
        tabla.clearSelection();
    }

    private void aviso(String m) { JOptionPane.showMessageDialog(this, m, "Aviso", JOptionPane.WARNING_MESSAGE); }
    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }
}
