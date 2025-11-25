package juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

/**
 * Gestor centralizado de sonido y música para el juego.
 * Maneja la reproducción de música de fondo y efectos de sonido.
 */
public class GestorSonido {

    private static GestorSonido instancia;

    // Música de fondo
    private HashMap<String, Music> musicas;
    private Music musicaActual;

    // Efectos de sonido
    private HashMap<String, Sound> sonidos;

    // Volúmenes (0.0 a 1.0)
    private float volumenMusica = 0.5f;
    private float volumenEfectos = 0.7f;

    // Estado
    private boolean musicaHabilitada = true;
    private boolean efectosHabilitados = true;

    private GestorSonido() {
        musicas = new HashMap<>();
        sonidos = new HashMap<>();
    }


    public static GestorSonido getInstancia() {
        if (instancia == null) {
            instancia = new GestorSonido();
        }
        return instancia;
    }

    public void cargarMusica(String nombre, String rutaArchivo) {
        try {
            if (Gdx.files.internal(rutaArchivo).exists()) {
                Music musica = Gdx.audio.newMusic(Gdx.files.internal(rutaArchivo));
                musicas.put(nombre, musica);
                System.out.println("[SONIDO] Música cargada: " + nombre);
            } else {
                System.err.println("[SONIDO] No se encontró el archivo: " + rutaArchivo);
            }
        } catch (Exception e) {
            System.err.println("[SONIDO] Error cargando música " + nombre + ": " + e.getMessage());
        }
    }

    /**
     * Carga un efecto de sonido
     */
    public void cargarSonido(String nombre, String rutaArchivo) {
        try {
            if (Gdx.files.internal(rutaArchivo).exists()) {
                Sound sonido = Gdx.audio.newSound(Gdx.files.internal(rutaArchivo));
                sonidos.put(nombre, sonido);
                System.out.println("[SONIDO] Efecto cargado: " + nombre);
            } else {
                System.err.println("[SONIDO] No se encontró el archivo: " + rutaArchivo);
            }
        } catch (Exception e) {
            System.err.println("[SONIDO] Error cargando sonido " + nombre + ": " + e.getMessage());
        }
    }

    /**
     * Reproduce una música de fondo en loop
     */
    public void reproducirMusica(String nombre) {
        if (!musicaHabilitada) return;

        Music musica = musicas.get(nombre);
        if (musica != null) {
            // Detener música actual si existe
            if (musicaActual != null && musicaActual.isPlaying()) {
                musicaActual.stop();
            }

            musicaActual = musica;
            musicaActual.setLooping(true);
            musicaActual.setVolume(volumenMusica);
            musicaActual.play();

            System.out.println("[SONIDO] Reproduciendo música: " + nombre);
        } else {
            System.err.println("[SONIDO] Música no encontrada: " + nombre);
        }
    }

    public boolean existeMusica(String nombre) {
        return musicas.containsKey(nombre);
    }
    /**
     * Reproduce un efecto de sonido
     */
    public void reproducirSonido(String nombre) {
        if (!efectosHabilitados) return;

        Sound sonido = sonidos.get(nombre);
        if (sonido != null) {
            sonido.play(volumenEfectos);
        } else {
            System.err.println("[SONIDO] Sonido no encontrado: " + nombre);
        }
    }

    /**
     * Pausa la música actual
     */
    public void pausarMusica() {
        if (musicaActual != null && musicaActual.isPlaying()) {
            musicaActual.pause();
        }
    }

    /**
     * Reanuda la música pausada
     */
    public void reanudarMusica() {
        if (musicaActual != null && !musicaActual.isPlaying()) {
            musicaActual.play();
        }
    }

    /**
     * Detiene la música actual
     */
    public void detenerMusica() {
        if (musicaActual != null) {
            musicaActual.stop();
            musicaActual = null;
        }
    }

    /**
     * Establece el volumen de la música (0.0 a 1.0)
     */
    public void setVolumenMusica(float volumen) {
        this.volumenMusica = Math.max(0f, Math.min(1f, volumen));
        if (musicaActual != null) {
            musicaActual.setVolume(this.volumenMusica);
        }

        System.out.println("[SONIDO] Volumen música: " + (int)(this.volumenMusica * 100) + "%");
    }

    /**
     * Establece el volumen de los efectos (0.0 a 1.0)
     */
    public void setVolumenEfectos(float volumen) {
        this.volumenEfectos = Math.max(0f, Math.min(1f, volumen));
        System.out.println("[SONIDO] Volumen efectos: " + (int)(this.volumenEfectos * 100) + "%");
    }

    /**
     * Habilita o deshabilita la música
     */
    public void setMusicaHabilitada(boolean habilitada) {
        this.musicaHabilitada = habilitada;
        if (!habilitada && musicaActual != null) {
            musicaActual.pause();
        } else if (habilitada && musicaActual != null) {
            musicaActual.play();
        }
    }

    /**
     * Habilita o deshabilita los efectos de sonido
     */
    public void setEfectosHabilitados(boolean habilitados) {
        this.efectosHabilitados = habilitados;
    }

    // Getters
    public float getVolumenMusica() {
        return volumenMusica;
    }

    public float getVolumenEfectos() {
        return volumenEfectos;
    }

    public boolean isMusicaHabilitada() {
        return musicaHabilitada;
    }

    public boolean isEfectosHabilitados() {
        return efectosHabilitados;
    }

    /**
     * Libera todos los recursos de sonido
     */
    public void dispose() {
        // Detener y liberar música
        for (Music musica : musicas.values()) {
            if (musica != null) {
                musica.stop();
                musica.dispose();
            }
        }
        musicas.clear();

        // Liberar efectos de sonido
        for (Sound sonido : sonidos.values()) {
            if (sonido != null) {
                sonido.dispose();
            }
        }
        sonidos.clear();

        musicaActual = null;
        System.out.println("[SONIDO] Recursos liberados");
    }
}