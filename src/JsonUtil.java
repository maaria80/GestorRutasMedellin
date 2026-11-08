import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Guarda y carga la ruta en un archivo JSON usando Jackson. */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

    /** Escribe las paradas (en orden) como un arreglo JSON. */
    public static void guardar(ListaParadas lista, File archivo) throws IOException {
        MAPPER.writeValue(archivo, lista.aLista());
    }

    /** Lee el arreglo JSON y lo carga en una lista ligada nueva. */
    public static ListaParadas cargar(File archivo) throws IOException {
        List<Parada> paradas = MAPPER.readValue(archivo, new TypeReference<List<Parada>>() { });
        ListaParadas lista = new ListaParadas();
        for (Parada p : paradas) lista.agregar(p);
        return lista;
    }
}
