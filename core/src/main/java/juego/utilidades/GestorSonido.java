package juego.utilidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

public class GestorSonido {

    private static GestorSonido instancia;

    // Música de fondo
    private HashMap<String, Music> musicas;
    private Music musicaActual;
    private String nombreMusicaActual;

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

    public void reproducirMusica(String nombre) {
        reproducirMusica(nombre, false);
    }

    public void reproducirMusica(String nombre, boolean reiniciar) {
        synchronized (this) {
            if (!musicaHabilitada) return;

            Music musica = musicas.get(nombre);
            if (musica == null) {
                System.err.println("[SONIDO] Música no encontrada: " + nombre);
                return;
            }

            if (musicaActual == musica && nombre.equals(nombreMusicaActual)) {

                if (reiniciar) {
                    String rutaArchivo = obtenerRutaMusica(nombre);
                    if (rutaArchivo != null) {
                        recargarMusicaLimpia(nombre, rutaArchivo);
                    }
                } else {
                    // Si ya está sonando y no pedimos reiniciar, no hacemos nada
                    if (musicaActual.isPlaying()) {
                        return;
                    }
                    // Si estaba pausada, reanudar
                    musicaActual.play();
                }
                return;
            }

            if (musicaActual != null && musicaActual != musica) {
                musicaActual.stop();
            }

            try {
                musicaActual = musica;
                nombreMusicaActual = nombre;
                musicaActual.setVolume(volumenMusica);
                musicaActual.setLooping(true);
                musicaActual.play();

                System.out.println("[SONIDO] ▶️ Reproduciendo: " + nombre);
            } catch (Exception e) {
                System.err.println("[SONIDO] Error reproduciendo música: " + e.getMessage());
            }
        }
    }

    private String obtenerRutaMusica(String nombre) {
        // Intentar OGG primero
        String rutaOgg = "sounds/" + nombre + ".ogg";
        if (Gdx.files.internal(rutaOgg).exists()) {
            return rutaOgg;
        }

        // Intentar MP3
        String rutaMp3 = "sounds/" + nombre + ".mp3";
        if (Gdx.files.internal(rutaMp3).exists()) {
            return rutaMp3;
        }

        System.err.println("[SONIDO] No se encontró archivo para: " + nombre);
        return null;
    }

    private void recargarMusicaLimpia(String nombre, String rutaArchivo) {
        try {
            System.out.println("[SONIDO] 🔄 Recargando música: " + nombre);

            // 1. Detener y disponer la instancia vieja
            if (musicaActual != null) {
                musicaActual.stop();
                musicaActual.dispose();
            }

            // 2. Crear nueva instancia fresca
            Music musicaNueva = Gdx.audio.newMusic(Gdx.files.internal(rutaArchivo));
            musicas.put(nombre, musicaNueva);

            // 3. Configurar y reproducir
            musicaActual = musicaNueva;
            nombreMusicaActual = nombre;
            musicaActual.setVolume(volumenMusica);
            musicaActual.setLooping(true);
            musicaActual.play();

            System.out.println("[SONIDO] ✅ Música recargada exitosamente");
        } catch (Exception e) {
            System.err.println("[SONIDO] ❌ Error crítico recargando música: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean existeMusica(String nombre) {
        return musicas.containsKey(nombre);
    }

    public void reproducirSonido(String nombre) {
        if (!efectosHabilitados) return;

        Sound sonido = sonidos.get(nombre);
        if (sonido != null) {
            try {
                sonido.play(volumenEfectos);
            } catch (Exception e) {
                System.err.println("[SONIDO] Error reproduciendo sonido " + nombre + ": " + e.getMessage());
            }
        } else {
            System.err.println("[SONIDO] Sonido no encontrado: " + nombre);
        }
    }

    public void pausarMusica() {
        synchronized (this) {
            if (musicaActual != null && musicaActual.isPlaying()) {
                try {
                    musicaActual.pause();
                } catch (Exception e) {
                    System.err.println("[SONIDO] Error pausando música: " + e.getMessage());
                }
            }
        }
    }

    public void reanudarMusica() {
        synchronized (this) {
            if (musicaActual != null && !musicaActual.isPlaying()) {
                try {
                    musicaActual.play();
                } catch (Exception e) {
                    System.err.println("[SONIDO] Error reanudando música: " + e.getMessage());
                }
            }
        }
    }

    public void detenerMusica() {
        synchronized (this) {
            if (musicaActual != null) {
                try {
                    musicaActual.stop();
                } catch (Exception e) {
                    System.err.println("[SONIDO] Error deteniendo música: " + e.getMessage());
                }
                musicaActual = null;
                nombreMusicaActual = null;
            }
        }
    }

    public void setVolumenMusica(float volumen) {
        this.volumenMusica = Math.max(0f, Math.min(1f, volumen));
        if (musicaActual != null) {
            try {
                musicaActual.setVolume(this.volumenMusica);
            } catch (Exception e) {
                System.err.println("[SONIDO] Error ajustando volumen: " + e.getMessage());
            }
        }
        System.out.println("[SONIDO] Volumen música: " + (int)(this.volumenMusica * 100) + "%");
    }

    public void setVolumenEfectos(float volumen) {
        this.volumenEfectos = Math.max(0f, Math.min(1f, volumen));
        System.out.println("[SONIDO] Volumen efectos: " + (int)(this.volumenEfectos * 100) + "%");
    }

    public void setMusicaHabilitada(boolean habilitada) {
        synchronized (this) {
            this.musicaHabilitada = habilitada;
            if (!habilitada && musicaActual != null) {
                try {
                    musicaActual.pause();
                } catch (Exception e) {
                    System.err.println("[SONIDO] Error deshabilitando música: " + e.getMessage());
                }
            } else if (habilitada && musicaActual != null) {
                try {
                    musicaActual.play();
                } catch (Exception e) {
                    System.err.println("[SONIDO] Error habilitando música: " + e.getMessage());
                }
            }
        }
    }

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


    public void dispose() {
        synchronized (this) {
            System.out.println("[SONIDO] Iniciando limpieza de recursos...");

            // Detener música actual
            if (musicaActual != null) {
                try {
                    musicaActual.stop();
                } catch (Exception e) {
                    System.err.println("[SONIDO] Error deteniendo música en dispose: " + e.getMessage());
                }
            }

            // Liberar todas las músicas
            for (String nombre : musicas.keySet()) {
                Music musica = musicas.get(nombre);
                if (musica != null) {
                    try {
                        musica.stop();
                        musica.dispose();
                    } catch (Exception e) {
                        System.err.println("[SONIDO] Error liberando música " + nombre + ": " + e.getMessage());
                    }
                }
            }
            musicas.clear();

            // Liberar efectos de sonido
            for (String nombre : sonidos.keySet()) {
                Sound sonido = sonidos.get(nombre);
                if (sonido != null) {
                    try {
                        sonido.dispose();
                    } catch (Exception e) {
                        System.err.println("[SONIDO] Error liberando sonido " + nombre + ": " + e.getMessage());
                    }
                }
            }
            sonidos.clear();

            musicaActual = null;
            nombreMusicaActual = null;

            System.out.println("[SONIDO] ✅ Recursos liberados");
        }
    }
}