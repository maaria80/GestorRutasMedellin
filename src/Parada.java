/** Datos de una parada de la ruta. Compatible con Jackson (constructor vacío + getters/setters). */
public class Parada {
    private String nombre;
    private double latitud;
    private double longitud;
    private int tiempoEstimado; // minutos
    private String descripcion = "";

    public Parada() { }

    public Parada(String nombre, double latitud, double longitud,
                  int tiempoEstimado, String descripcion) {
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tiempoEstimado = tiempoEstimado;
        setDescripcion(descripcion);
    }

    public String getNombre() { return nombre; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public int getTiempoEstimado() { return tiempoEstimado; }
    public String getDescripcion() { return descripcion; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public void setTiempoEstimado(int tiempoEstimado) { this.tiempoEstimado = tiempoEstimado; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion == null ? "" : descripcion; }

    /** Distancia en km a otra parada (fórmula de Haversine). */
    public double distanciaA(Parada otra) {
        final double R = 6371.0;
        double dLat = Math.toRadians(otra.latitud - latitud);
        double dLon = Math.toRadians(otra.longitud - longitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(otra.latitud))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    public String toString() { return nombre; }
}
