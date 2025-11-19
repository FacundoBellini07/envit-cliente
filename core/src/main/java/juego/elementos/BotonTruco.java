package juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import juego.pantallas.Partida;
import juego.personajes.TipoJugador;
import juego.red.HiloCliente;

/**
 * Botón visual para cantar Truco en el juego.
 * Solo disponible si:
 * 1. Es el primer turno en la mano (nadie jugó carta).
 * 2. El jugador local es el "Mano" (el que empieza a tirar).
 * 3. El Truco no ha sido cantado/aceptado aún en la ronda.
 */
public class BotonTruco {

    private Rectangle btnRect;
    private boolean hovered = false;
    private float animacionPulso = 0f;

    private BitmapFont font;
    private Viewport viewport;
    private Partida partida;

    // Colores
    private Color colorDeshabilitado = new Color(0.3f, 0.3f, 0.3f, 0.5f);
    private Color colorNormal = new Color(0.9f, 0.1f, 0.1f, 0.9f);
    private Color colorHover = new Color(1.0f, 0.2f, 0.2f, 1f);
    private Color colorTexto = Color.WHITE;
    private Color colorTextoDeshabilitado = new Color(0.5f, 0.5f, 0.5f, 1f);
    private Color colorIndicador = Color.YELLOW;

    // Dimensiones
    private float btnAncho;
    private float btnAlto;

    private HiloCliente hc;

    public BotonTruco(float x, float y, float ancho, float alto,
                      BitmapFont font, Viewport viewport, Partida partida, HiloCliente hc) {
        this.btnAncho = ancho;
        this.btnAlto = alto;
        this.font = font;
        this.viewport = viewport;
        this.partida = partida;

        this.btnRect = new Rectangle(x, y, ancho, alto);
        this.animacionPulso = 0f;
        this.hc = hc;
    }

    public void update(float delta) {
        // Actualizar animación del pulso
        animacionPulso += delta * 3f;
        if (animacionPulso > Math.PI * 2) {
            animacionPulso = 0f;
        }

        // Detectar hover
        actualizarHover();
    }

    /**
     * Dibuja el botón
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {

        // --- LÓGICA DE DISPONIBILIDAD CORREGIDA ---
        boolean trucoDisponible = !partida.isTrucoUsado()           // Verifica si se usó Truco en la ronda
                && partida.esPrimerTurnoEnMano()                   // Verifica si nadie jugó carta en la mano actual
                && partida.esMiTurnoLocal()                         // Verifica si es mi turno
                && partida.soyJugadorMano();                       // Verifica si soy el "Mano" (el pie)

        Color colorBtn;
        float escala = 1.0f;

        if (!trucoDisponible) {
            colorBtn = colorDeshabilitado;
        } else if (hovered) {
            float pulso = (float)Math.sin(animacionPulso) * 0.1f + 0.9f;
            colorBtn = new Color(
                    colorHover.r * pulso,
                    colorHover.g,
                    colorHover.b,
                    colorHover.a
            );
            escala = 1.1f;
        } else {
            colorBtn = colorNormal;
        }

        float offsetX = (btnRect.width * escala - btnRect.width) / 2f;
        float offsetY = (btnRect.height * escala - btnRect.height) / 2f;

        // Dibujar fondo del botón
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(colorBtn);
        shapeRenderer.rect(
                btnRect.x - offsetX,
                btnRect.y - offsetY,
                btnRect.width * escala,
                btnRect.height * escala
        );
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
                btnRect.x - offsetX,
                btnRect.y - offsetY,
                btnRect.width * escala,
                btnRect.height * escala
        );
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(trucoDisponible ? colorTexto : colorTextoDeshabilitado);
        font.getData().setScale(1.8f);

        String textoTruco = "TRUCO";
        GlyphLayout layout = new GlyphLayout(font, textoTruco);

        float textX = btnRect.x + (btnRect.width - layout.width) / 2f;
        float textY = btnRect.y + (btnRect.height + layout.height) / 2f;

        font.draw(batch, textoTruco, textX, textY);

        // Indicador de valor de Truco activo
        if (partida.isTrucoActivoEnManoActual()) {
            font.getData().setScale(0.8f);
            font.setColor(colorIndicador);
            String textoActivo = "x2";
            layout = new GlyphLayout(font, textoActivo);
            float x2X = btnRect.x + (btnRect.width - layout.width) / 2f;
            float x2Y = btnRect.y - 5f;
            font.draw(batch, textoActivo, x2X, x2Y);
        }

        batch.end();
    }

    private void actualizarHover() {

        // --- LÓGICA DE DISPONIBILIDAD CORREGIDA ---
        boolean trucoDisponible = !partida.isTrucoUsado()
                && partida.esPrimerTurnoEnMano()
                && partida.esMiTurnoLocal()
                && partida.soyJugadorMano();

        Vector2 mouse = viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY())
        );

        hovered = btnRect.contains(mouse.x, mouse.y) && trucoDisponible;
    }

    public boolean detectarClick() {
        if (!Gdx.input.justTouched()) {
            return false;
        }

        Vector2 touch = viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY())
        );

        if (btnRect.contains(touch.x, touch.y)) {
            return intentarCantarTruco();
        }

        return false;
    }

    private boolean intentarCantarTruco() {
        // Llamamos al método local en Partida que valida las condiciones de la UI
        // y actualiza la bandera temporalmente.
        boolean exito = partida.cantarTruco(partida.getJugadorLocal()); // Pasamos mi TipoJugador

        if (exito) {
            System.out.println("¡TRUCO cantado por el jugador!");
            // IMPORTANTE: Enviamos el mensaje al servidor para que él valide y notifique a todos
            hc.enviarMensaje("TRUCO");
        }

        return exito;
    }

    // Asumo que agregaste getJugadorLocal() en Partida.java para obtener mi rol.
}