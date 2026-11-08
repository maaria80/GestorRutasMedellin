import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // OpenStreetMap rechaza peticiones sin identificarse: sin esto el mapa sale gris
        System.setProperty("http.agent", "GestorRutas/1.0 (proyecto academico Java)");
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
