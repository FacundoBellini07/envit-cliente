package juego.pantallas;

import juego.elementos.Hud;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import juego.personajes.Jugador;
import juego.personajes.TipoJugador;

public class PantallaFinal {

    private BitmapFont fontTitulo;
    private BitmapFont fontTexto;
    private BitmapFont fontPequeño;

    private Viewport viewport;
    private Hud hud;

    private float worldWidth;
    private float worldHeight;

    // Control de tiempo
    private float tiempoTranscurrido = 0f;
    private final float TIEMPO_ANTES_PERMITIR_SALIDA = 5.0f;
    private final float TIEMPO_AUTO_RETORNO = 10.0f;
    private TipoJugador miRol;

    // Jugadores
    private Jugador ganador;
    private Jugador jugador1;
    private Jugador jugador2;

    // Colores
    private Color colorVictoria = new Color(0.2f, 0.9f, 0.2f, 1f);
    private Color colorDerrota = new Color(0.9f, 0.2f, 0.2f, 1f);
    private Color colorFondoOverlay = new Color(0, 0, 0, 0.8f);

    // Estado
    private boolean activa = false;

    public PantallaFinal(Viewport viewport, Hud hud,
                         float worldWidth, float worldHeight, TipoJugador miRol) {
        this.viewport = viewport;
        this.hud = hud;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.miRol = miRol;

        cargarFuentes();
    }

    private void cargarFuentes() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                    Gdx.files.internal("fuentes/medieval.ttf")
            );

            // Fuente para título grande (victoria/derrota)
            FreeTypeFontGenerator.FreeTypeFontParameter paramTitulo =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramTitulo.size = 64;
            paramTitulo.borderWidth = 3;
            paramTitulo.borderColor = Color.BLACK;
            fontTitulo = generator.generateFont(paramTitulo);

            // Fuente para texto normal
            FreeTypeFontGenerator.FreeTypeFontParameter paramTexto =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramTexto.size = 36;
            paramTexto.borderWidth = 1;
            paramTexto.borderColor = Color.BLACK;
            fontTexto = generator.generateFont(paramTexto);

            // Fuente pequeña para instrucciones
            FreeTypeFontGenerator.FreeTypeFontParameter paramPequeño =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramPequeño.size = 24;
            fontPequeño = generator.generateFont(paramPequeño);

            generator.dispose();

            System.out.println("[PANTALLA_FINAL] Fuentes medieval.ttf cargadas correctamente");
        } catch (Exception e) {
            System.err.println("[PANTALLA_FINAL] Error cargando fuentes: " + e.getMessage());
            fontTitulo = new BitmapFont();
            fontTexto = new BitmapFont();
            fontPequeño = new BitmapFont();
        }
    }

    public void activar(Jugador ganador, Jugador jugador1, Jugador jugador2) {
        this.ganador = ganador;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.activa = true;
        this.tiempoTranscurrido = 0f;
    }

    public boolean update(float delta) {
        if (!activa) {
            return false;
        }

        tiempoTranscurrido += delta;

        // Auto-retorno después del tiempo límite extendido
        if (tiempoTranscurrido >= TIEMPO_ANTES_PERMITIR_SALIDA + TIEMPO_AUTO_RETORNO) {
            return true;
        }

        // Permitir salida manual después del tiempo mínimo
        if (tiempoTranscurrido >= TIEMPO_ANTES_PERMITIR_SALIDA) {
            if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                return true;
            }
        }

        return false;
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!activa) {
            return;
        }

        // 1. Dibujar overlay oscuro
        renderOverlay(shapeRenderer);

        // 2. Dibujar mensaje principal
        renderMensajePrincipal(batch);

        // 3. Dibujar puntuación
        renderPuntuacion(batch);

        // 4. Dibujar mensaje de instrucciones
        renderInstrucciones(batch);
    }

    private void renderOverlay(ShapeRenderer shapeRenderer) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(colorFondoOverlay);
        shapeRenderer.rect(0, 0, worldWidth, worldHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderMensajePrincipal(SpriteBatch batch) {
        boolean ganoJugador1 = (ganador == jugador1);
        String mensaje = ganoJugador1 ? "¡VICTORIA!" : "DERROTA";
        Color colorMensaje = ganoJugador1 ? colorVictoria : colorDerrota;

        batch.begin();

        fontTitulo.setColor(colorMensaje);
        GlyphLayout layout = new GlyphLayout(fontTitulo, mensaje);

        float x = (worldWidth - layout.width) / 2f;
        float y = (worldHeight + layout.height) / 2f + 50;

        fontTitulo.draw(batch, mensaje, x, y);

        batch.end();
    }

    private void renderPuntuacion(SpriteBatch batch) {
        batch.begin();

        String puntuacion;
        fontTexto.setColor(Color.WHITE);

            puntuacion = "TU: " + jugador1.getPuntos() + " - RIVAL: " + jugador2.getPuntos();


        GlyphLayout layout = new GlyphLayout(fontTexto, puntuacion);

        float x = (worldWidth - layout.width) / 2f;
        float y = worldHeight / 2f - 50f;

        fontTexto.draw(batch, puntuacion, x, y);

        batch.end();
    }

    private void renderInstrucciones(SpriteBatch batch) {
        batch.begin();

        fontPequeño.setColor(Color.WHITE);
        String mensaje;

        if (tiempoTranscurrido < TIEMPO_ANTES_PERMITIR_SALIDA) {
            // Cuenta regresiva
            int segundosRestantes = (int)(TIEMPO_ANTES_PERMITIR_SALIDA - tiempoTranscurrido) + 1;
            mensaje = "Volviendo al menú en " + segundosRestantes + "...";
        } else {
            // Permitir salida
            mensaje = "Presiona cualquier tecla para continuar";
        }

        GlyphLayout layout = new GlyphLayout(fontPequeño, mensaje);
        float x = (worldWidth - layout.width) / 2f;
        float y = 80f;

        fontPequeño.draw(batch, mensaje, x, y);

        batch.end();
    }

    public boolean isActiva() {
        return activa;
    }

    public float getTiempoTranscurrido() {
        return tiempoTranscurrido;
    }

    public void setColorVictoria(Color color) {
        this.colorVictoria = color;
    }

    public void setColorDerrota(Color color) {
        this.colorDerrota = color;
    }

    public void setColorFondoOverlay(Color color) {
        this.colorFondoOverlay = color;
    }

    public void dispose() {
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontTexto != null) fontTexto.dispose();
        if (fontPequeño != null) fontPequeño.dispose();
    }
}