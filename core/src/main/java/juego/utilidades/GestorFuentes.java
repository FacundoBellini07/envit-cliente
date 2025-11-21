package juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import java.util.HashMap;

/**
 * Gestor centralizado de fuentes para el juego.
 * Evita duplicación de código y garantiza que las fuentes siempre estén disponibles.
 */
public class GestorFuentes {

    private static GestorFuentes instancia;
    private HashMap<String, BitmapFont> fuentes;
    private final String RUTA_FUENTE = "fuentes/medieval.ttf";

    private GestorFuentes() {
        fuentes = new HashMap<>();
        cargarTodasLasFuentes();
    }

    /**
     * Obtiene la instancia única del gestor de fuentes
     */
    public static GestorFuentes getInstancia() {
        if (instancia == null) {
            instancia = new GestorFuentes();
        }
        return instancia;
    }

    /**
     * Carga todas las fuentes que necesita el juego
     */
    private void cargarTodasLasFuentes() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                    Gdx.files.internal(RUTA_FUENTE)
            );

            // Fuente GRANDE para mensajes importantes (48px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramGrande =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramGrande.size = 48;
            paramGrande.borderWidth = 2;
            paramGrande.borderColor = Color.BLACK;
            fuentes.put("grande", generator.generateFont(paramGrande));

            // Fuente MEDIANA para información del juego (32px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramMediana =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramMediana.size = 32;
            paramMediana.borderWidth = 1;
            paramMediana.borderColor = Color.BLACK;
            fuentes.put("mediana", generator.generateFont(paramMediana));

            // Fuente PEQUEÑA para detalles (24px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramPequeña =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramPequeña.size = 24;
            fuentes.put("pequeña", generator.generateFont(paramPequeña));

            // Fuente para botón Truco (28px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramBoton28 =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramBoton28.size = 28;
            paramBoton28.borderWidth = 1;
            paramBoton28.borderColor = Color.BLACK;
            fuentes.put("boton28", generator.generateFont(paramBoton28));

            // Fuente para botón Truco (20px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramBoton20 =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramBoton20.size = 20;
            paramBoton20.borderWidth = 1;
            paramBoton20.borderColor = Color.BLACK;
            fuentes.put("boton20", generator.generateFont(paramBoton20));

            // Fuente extra grande para menú (36px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramMenuTitle =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramMenuTitle.size = 36;
            fuentes.put("menuTitle", generator.generateFont(paramMenuTitle));

            generator.dispose();

            System.out.println("[FUENTES] ✅ Todas las fuentes medieval.ttf cargadas correctamente");

        } catch (Exception e) {
            System.err.println("[FUENTES] ❌ Error cargando fuentes: " + e.getMessage());
            e.printStackTrace();
            crearFuentesPorDefecto();
        }
    }

    /**
     * Crea fuentes por defecto si fallan las de medieval.ttf
     */
    private void crearFuentesPorDefecto() {
        System.out.println("[FUENTES] Creando fuentes por defecto...");
        fuentes.put("grande", new BitmapFont());
        fuentes.put("mediana", new BitmapFont());
        fuentes.put("pequeña", new BitmapFont());
        fuentes.put("boton28", new BitmapFont());
        fuentes.put("boton20", new BitmapFont());
        fuentes.put("menuTitle", new BitmapFont());
    }

    /**
     * Obtiene una fuente por nombre
     * @param nombre Nombre de la fuente (grande, mediana, pequeña, boton28, boton20, menuTitle)
     * @return BitmapFont o fuente por defecto si no existe
     */
    public BitmapFont obtener(String nombre) {
        BitmapFont font = fuentes.get(nombre);
        if (font == null) {
            System.err.println("[FUENTES] ⚠️ Fuente '" + nombre + "' no encontrada, devolviendo por defecto");
            return new BitmapFont();
        }
        return font;
    }

    /**
     * Obtiene la fuente grande
     */
    public BitmapFont getGrande() {
        return obtener("grande");
    }

    /**
     * Obtiene la fuente mediana
     */
    public BitmapFont getMediana() {
        return obtener("mediana");
    }

    /**
     * Obtiene la fuente pequeña
     */
    public BitmapFont getPequeña() {
        return obtener("pequeña");
    }

    /**
     * Obtiene la fuente para botón (28px)
     */
    public BitmapFont getBoton28() {
        return obtener("boton28");
    }

    /**
     * Obtiene la fuente para botón (20px)
     */
    public BitmapFont getBoton20() {
        return obtener("boton20");
    }

    /**
     * Obtiene la fuente para títulos de menú
     */
    public BitmapFont getMenuTitle() {
        return obtener("menuTitle");
    }

    /**
     * Libera todas las fuentes
     */
    public void dispose() {
        for (BitmapFont font : fuentes.values()) {
            if (font != null) {
                font.dispose();
            }
        }
        fuentes.clear();
        System.out.println("[FUENTES] Recursos liberados");
    }

    /**
     * Reinicia el gestor (útil para tests)
     */
    public static void reiniciar() {
        if (instancia != null) {
            instancia.dispose();
        }
        instancia = null;
    }
}