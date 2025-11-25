package juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import juego.pantallas.PartidaCliente;
import juego.red.HiloCliente;
import juego.utilidades.GestorSonido;

public class BotonTruco implements InputProcessor {

    private Rectangle btnRect;
    private boolean hovered = false;
    private float animacionPulso = 0f;

    private BitmapFont font;
    private Viewport viewport;
    private PartidaCliente partida;

    private Color colorDeshabilitado = new Color(0.3f, 0.3f, 0.3f, 0.5f);
    private Color colorNormal = new Color(0.9f, 0.1f, 0.1f, 0.9f);
    private Color colorHover = new Color(1.0f, 0.2f, 0.2f, 1f);
    private Color colorTexto = Color.WHITE;
    private Color colorTextoDeshabilitado = new Color(0.5f, 0.5f, 0.5f, 1f);
    private Color colorIndicador = Color.YELLOW;

    private float btnAncho;
    private float btnAlto;

    private HiloCliente hc;
    private GestorSonido gestorSonido;

    public BotonTruco(float x, float y, float ancho, float alto,
                      BitmapFont font, Viewport viewport, PartidaCliente partida, HiloCliente hc) {
        this.btnAncho = ancho;
        this.btnAlto = alto;
        this.font = font;
        this.viewport = viewport;
        this.partida = partida;

        this.btnRect = new Rectangle(x, y, ancho, alto);
        this.animacionPulso = 0f;
        this.hc = hc;

        this.gestorSonido = GestorSonido.getInstancia();
    }

    public void update(float delta) {
        animacionPulso += delta * 3f;
        if (animacionPulso > Math.PI * 2) {
            animacionPulso = 0f;
        }


        actualizarHover();

        if ((int)(animacionPulso * 100) % 200 == 0) {
            String ultimoCanto = "nadie";
            if (partida.getEstadoTruco() != EstadoTruco.SIN_TRUCO) {
                // Necesitamos obtener esta info de Partida
                ultimoCanto = "verificar en partida";
            }

            System.out.println("[BOTON_TRUCO DEBUG] " +
                    "EstadoTruco=" + partida.getEstadoTruco() +
                    ", ManoActual=" + partida.getManoActual() +
                    ", EsMiTurno=" + partida.esMiTurnoLocal() +
                    ", SoyMano=" + partida.soyJugadorMano() +
                    ", PrimerTurno=" + partida.esPrimerTurnoEnMano() +
                    ", Disponible=" + isTrucoDisponible());
        }
    }

    private boolean isTrucoDisponible() {
        // ✅ NUEVO: Usar el método del partida que ya valida todo
        return partida.puedoCantarTruco();
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {

        boolean trucoDisponible = isTrucoDisponible();

        Color colorBtn;
        float escala = 1.3f;

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
        font.getData().setScale(0.8f);

        // ✅ NUEVO: Mostrar el canto correspondiente
        String textoBoton = partida.getSiguienteCanto();
        GlyphLayout layout = new GlyphLayout(font, textoBoton);

        float textX = btnRect.x + (btnRect.width - layout.width) / 2f;
        float textY = btnRect.y + (btnRect.height + layout.height) / 2f;

        font.draw(batch, textoBoton, textX, textY);

        // ✅ NUEVO: Indicador del estado actual del truco
        if (partida.isTrucoActivoEnManoActual()) {
            font.getData().setScale(0.5f);
            font.setColor(colorIndicador);
            String textoActivo = "x" + partida.getEstadoTruco().getPuntos();
            layout = new GlyphLayout(font, textoActivo);
            float x2X = btnRect.x + (btnRect.width - layout.width) / 2f;
            float x2Y = btnRect.y - 5f;
            font.draw(batch, textoActivo, x2X, x2Y);
        }

        batch.end();
    }

    private void actualizarHover() {
        boolean trucoDisponible = isTrucoDisponible();

        Vector2 mouse = viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY())
        );

        hovered = btnRect.contains(mouse.x, mouse.y) && trucoDisponible;
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Convertimos la coordenada del click (pantalla) a coordenadas del mundo
        Vector2 touch = viewport.unproject(new Vector2(screenX, screenY));

        if (btnRect.contains(touch.x, touch.y)) {
            // Si tocamos el botón, intentamos cantar truco y devolvemos 'true' (evento consumido)
            return intentarCantarTruco();
        }
        return false;
    }

    private boolean intentarCantarTruco() {
        if (!partida.cantarTruco()) {
            System.out.println("[CLIENTE] No puedes cantar truco ahora");
            return false;
        }

        gestorSonido.reproducirSonido("truco");
        System.out.println("[CLIENTE] Enviando TRUCO al servidor...");
        hc.enviarMensaje("TRUCO");
        hc.notificarTrucoEnviado();
        return true;
    }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}