import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
 
import javax.swing.BorderFactory;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
 
/** Mapa de OpenStreetMap (JXMapViewer2) que dibuja la ruta con sus paradas numeradas. */
public class MapaRuta extends JXMapViewer {
 
    private List<Parada> paradas = new ArrayList<>();
    private int seleccionada = -1;
    private Consumer<GeoPosition> alDobleClic;
 
    public MapaRuta() {
        // La versión 2.6 usa http:// y OpenStreetMap exige https://, por eso se indica la URL segura
        DefaultTileFactory fabrica = new DefaultTileFactory(
                new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org"));
        fabrica.setUserAgent("GestorRutas/1.0 (proyecto academico Java)");
        fabrica.setThreadPoolSize(4);
        setTileFactory(fabrica);
        setZoom(6);
        setAddressLocation(new GeoPosition(6.2442, -75.5812)); // Medellín
        setBorder(BorderFactory.createTitledBorder("Mapa de la ruta (doble clic en el mapa = tomar coordenadas)"));
 
        // Arrastrar para mover y rueda del mouse para acercar/alejar
        PanMouseInputListener pan = new PanMouseInputListener(this);
        addMouseListener(pan);
        addMouseMotionListener(pan);
        addMouseWheelListener(new ZoomMouseWheelListenerCenter(this));
 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && alDobleClic != null)
                    alDobleClic.accept(convertPointToGeoPosition(e.getPoint()));
            }
        });
 
        setOverlayPainter(new Painter<JXMapViewer>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer mapa, int ancho, int alto) {
                dibujarRuta(g, mapa);
            }
        });
    }
 
    public void setAlDobleClic(Consumer<GeoPosition> accion) { this.alDobleClic = accion; }
 
    public void setParadas(List<Parada> paradas) {
        this.paradas = paradas;
        repaint();
    }
 
    /** Resalta una parada y centra el mapa en ella. */
    public void setSeleccionada(int indice) {
        this.seleccionada = indice;
        if (indice >= 0 && indice < paradas.size()) setAddressLocation(posicion(paradas.get(indice)));
        repaint();
    }
 
    /** Ajusta zoom y centro para que se vean todas las paradas. */
    public void ajustar() {
        if (paradas.isEmpty()) return;
        if (paradas.size() == 1) {
            setZoom(3);
            setAddressLocation(posicion(paradas.get(0)));
            return;
        }
        Set<GeoPosition> posiciones = new HashSet<>();
        for (Parada p : paradas) posiciones.add(posicion(p));
        zoomToBestFit(posiciones, 0.9);
    }
 
    private GeoPosition posicion(Parada p) {
        return new GeoPosition(p.getLatitud(), p.getLongitud());
    }
 
    private void dibujarRuta(Graphics2D g0, JXMapViewer mapa) {
        if (paradas.isEmpty()) return;
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
        Rectangle vista = mapa.getViewportBounds();
        g.translate(-vista.x, -vista.y);
 
        List<Point2D> pts = new ArrayList<>();
        for (Parada p : paradas)
            pts.add(mapa.getTileFactory().geoToPixel(posicion(p), mapa.getZoom()));
 
        // Línea de la ruta (borde blanco + línea azul)
        for (int pasada = 0; pasada < 2; pasada++) {
            g.setColor(pasada == 0 ? Color.WHITE : new Color(30, 100, 220));
            g.setStroke(new BasicStroke(pasada == 0 ? 7f : 4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < pts.size() - 1; i++) {
                Point2D a = pts.get(i), b = pts.get(i + 1);
                g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
            }
        }
 
        // Marcadores numerados
        g.setStroke(new BasicStroke(2f));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < pts.size(); i++) {
            Point2D p = pts.get(i);
            int r = (i == seleccionada) ? 15 : 12;
            Color relleno = (i == seleccionada) ? new Color(220, 60, 50)
                    : (i == 0 ? new Color(30, 150, 70) : new Color(30, 80, 170));
            g.setColor(relleno);
            g.fillOval((int) p.getX() - r, (int) p.getY() - r, 2 * r, 2 * r);
            g.setColor(Color.WHITE);
            g.drawOval((int) p.getX() - r, (int) p.getY() - r, 2 * r, 2 * r);
            String num = String.valueOf(i + 1);
            g.drawString(num, (int) p.getX() - fm.stringWidth(num) / 2, (int) p.getY() + fm.getAscent() / 2 - 1);
 
            // Nombre con fondo claro para que se lea sobre el mapa
            String nombre = paradas.get(i).getNombre();
            int tx = (int) p.getX() + r + 4, ty = (int) p.getY() + 5;
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(tx - 2, ty - fm.getAscent(), fm.stringWidth(nombre) + 4, fm.getHeight());
            g.setColor(Color.BLACK);
            g.drawString(nombre, tx, ty);
        }
        g.dispose();
    }
}
 