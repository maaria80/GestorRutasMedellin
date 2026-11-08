import java.util.ArrayList;
import java.util.List;

/** Lista ligada simple de paradas (implementada manualmente, sin java.util.LinkedList). */
public class ListaParadas {
    private Nodo cabeza;
    private int tamano;

    public int tamano() { return tamano; }
    public boolean estaVacia() { return cabeza == null; }

    /** Agrega una parada al final de la ruta. */
    public void agregar(Parada p) {
        Nodo nuevo = new Nodo(p);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo actual = cabeza;
            while (actual.siguiente != null) actual = actual.siguiente;
            actual.siguiente = nuevo;
        }
        tamano++;
    }

    /** Selecciona (obtiene) la parada en la posición indicada (base 0). */
    public Parada seleccionar(int indice) {
        validar(indice);
        Nodo actual = cabeza;
        for (int i = 0; i < indice; i++) actual = actual.siguiente;
        return actual.dato;
    }

    /** Reemplaza los datos de la parada en la posición indicada. */
    public void modificar(int indice, Parada nueva) {
        validar(indice);
        Nodo actual = cabeza;
        for (int i = 0; i < indice; i++) actual = actual.siguiente;
        actual.dato = nueva;
    }

    /** Elimina la parada en la posición indicada. */
    public void eliminar(int indice) {
        validar(indice);
        if (indice == 0) {
            cabeza = cabeza.siguiente;
        } else {
            Nodo anterior = cabeza;
            for (int i = 0; i < indice - 1; i++) anterior = anterior.siguiente;
            anterior.siguiente = anterior.siguiente.siguiente;
        }
        tamano--;
    }

    public void limpiar() {
        cabeza = null;
        tamano = 0;
    }

    /** Recorre la lista y devuelve las paradas en orden (para mostrarlas). */
    public List<Parada> aLista() {
        List<Parada> res = new ArrayList<>();
        for (Nodo n = cabeza; n != null; n = n.siguiente) res.add(n.dato);
        return res;
    }

    /** Suma de distancias entre paradas consecutivas (km). */
    public double distanciaTotalKm() {
        double total = 0;
        for (Nodo n = cabeza; n != null && n.siguiente != null; n = n.siguiente)
            total += n.dato.distanciaA(n.siguiente.dato);
        return total;
    }

    private void validar(int indice) {
        if (indice < 0 || indice >= tamano)
            throw new IndexOutOfBoundsException("Posición inválida: " + indice);
    }
}
