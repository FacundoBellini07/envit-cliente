package juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import java.util.HashMap;


public class GestorFuentes {

    private static GestorFuentes instancia;
    private HashMap<String, BitmapFont> fuentes;
    private final String RUTA_FUENTE = "fuentes/medieval.ttf";
    private boolean fuentesDisponibles = false; //

    private GestorFuentes() {
        fuentes = new HashMap<>();
        cargarTodasLasFuentes();
    }

    public static GestorFuentes getInstancia() {
        if (instancia == null) {
            instancia = new GestorFuentes();
        }
        return instancia;
    }

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
            paramGrande.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            paramGrande.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            fuentes.put("grande", generator.generateFont(paramGrande));

            // Fuente MEDIANA para información del juego (32px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramMediana =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramMediana.size = 32;
            paramMediana.borderWidth = 1;
            paramMediana.borderColor = Color.BLACK;
            paramMediana.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            paramMediana.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            fuentes.put("mediana", generator.generateFont(paramMediana));

            // Fuente PEQUEÑA para detalles (24px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramPequeña =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramPequeña.size = 24;
            paramPequeña.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            paramPequeña.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            fuentes.put("pequeña", generator.generateFont(paramPequeña));

            // Fuente para botón Truco (28px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramBoton28 =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramBoton28.size = 28;
            paramBoton28.borderWidth = 1;
            paramBoton28.borderColor = Color.BLACK;
            paramBoton28.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            paramBoton28.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            fuentes.put("boton28", generator.generateFont(paramBoton28));

            // Fuente para botón Truco (20px) - ✅ AUMENTAR TAMAÑO A 24px
            FreeTypeFontGenerator.FreeTypeFontParameter paramBoton20 =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramBoton20.size = 24; // CAMBIAR de 20 a 24
            paramBoton20.borderWidth = 1;
            paramBoton20.borderColor = Color.BLACK;
            paramBoton20.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear; 
            paramBoton20.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            fuentes.put("boton20", generator.generateFont(paramBoton20));

            // Fuente extra grande para menú (36px)
            FreeTypeFontGenerator.FreeTypeFontParameter paramMenuTitle =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramMenuTitle.size = 36;
            paramMenuTitle.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            paramMenuTitle.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
            fuentes.put("menuTitle", generator.generateFont(paramMenuTitle));

            generator.dispose();

            fuentesDisponibles = true; // ✅ MARCAR como disponibles
            System.out.println("[FUENTES] ✅ Todas las fuentes medieval.ttf cargadas correctamente");

        } catch (Exception e) {
            System.err.println("[FUENTES] ❌ Error cargando fuentes: " + e.getMessage());
            e.printStackTrace();
            crearFuentesPorDefecto();
        }
    }

    private void crearFuentesPorDefecto() {
        System.out.println("[FUENTES] Creando fuentes por defecto...");
        fuentes.put("grande", new BitmapFont());
        fuentes.put("mediana", new BitmapFont());
        fuentes.put("pequeña", new BitmapFont());
        fuentes.put("boton28", new BitmapFont());
        fuentes.put("boton20", new BitmapFont());
        fuentes.put("menuTitle", new BitmapFont());
    }

    public BitmapFont obtener(String nombre) {
        BitmapFont font = fuentes.get(nombre);
        if (font == null) {
            System.err.println("[FUENTES] ⚠️ Fuente '" + nombre + "' no encontrada");
            return new BitmapFont();
        }
        return font;
    }

    // ✅ NUEVO: Verificar si las fuentes están disponibles
    public boolean estaDisponible() {
        return fuentesDisponibles;
    }

    public BitmapFont getGrande() {
        return obtener("grande");
    }

    public BitmapFont getMediana() {
        return obtener("mediana");
    }

    public BitmapFont getPequeña() {
        return obtener("pequeña");
    }

    public BitmapFont getBoton28() {
        return obtener("boton28");
    }

    public BitmapFont getBoton20() {
        return obtener("boton20");
    }

    public BitmapFont getMenuTitle() {
        return obtener("menuTitle");
    }

    public void dispose() {
        for (BitmapFont font : fuentes.values()) {
            if (font != null) {
                font.dispose();
            }
        }
        fuentes.clear();
        fuentesDisponibles = false; // ✅ MARCAR como no disponibles
        System.out.println("[FUENTES] Recursos liberados");
    }

    public static void reiniciar() {
        if (instancia != null) {
            instancia.dispose();
        }
        instancia = null;
    }
}



