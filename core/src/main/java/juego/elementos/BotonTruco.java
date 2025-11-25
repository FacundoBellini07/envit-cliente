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

    // ✅ NUEVO: Contador de clicks para debug
    private int contadorClicks = 0;
    private long ultimoClickTimestamp = 0;

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

        System.out.println("[BOTON_TRUCO] ✅ Creado en posición (" + x + ", " + y + ") con tamaño " + ancho + "x" + alto);
    }

    public void update(float delta) {
        animacionPulso += delta * 3f;
        if (animacionPulso > Math.PI * 2) {
            animacionPulso = 0f;
        }

        actualizarHover();
    }

    private boolean isTrucoDisponible() {
        boolean disponible = partida.puedoCantarTruco();

        // Log cada 2 segundos aprox
        if ((int)(animacionPulso * 100) % 600 == 0) {
            System.out.println("[BOTON_TRUCO] Disponible=" + disponible +
                    " | Estado=" + partida.getEstadoTruco() +
                    " | MiTurno=" + partida.esMiTurnoLocal());
        }

        return disponible;
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

        String textoBoton = partida.getSiguienteCanto();
        GlyphLayout layout = new GlyphLayout(font, textoBoton);

        float textX = btnRect.x + (btnRect.width - layout.width) / 2f;
        float textY = btnRect.y + (btnRect.height + layout.height) / 2f;

        font.draw(batch, textoBoton, textX, textY);

        if (partida.isTrucoActivoEnManoActual() && partida.getManoActual() == 0) {
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

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        contadorClicks++;
        long ahora = System.currentTimeMillis();
        long tiempoDesdeUltimoClick = ahora - ultimoClickTimestamp;
        ultimoClickTimestamp = ahora;

        System.out.println("\n[BOTON_TRUCO] ========================================");
        System.out.println("[BOTON_TRUCO] 🖱️ touchDown() LLAMADO");
        System.out.println("[BOTON_TRUCO] Click #" + contadorClicks + " (Δt=" + tiempoDesdeUltimoClick + "ms)");
        System.out.println("[BOTON_TRUCO] Screen coords: (" + screenX + ", " + screenY + ")");

        Vector2 touch = viewport.unproject(new Vector2(screenX, screenY));
        System.out.println("[BOTON_TRUCO] World coords: (" + touch.x + ", " + touch.y + ")");
        System.out.println("[BOTON_TRUCO] Botón rect: " + btnRect);

        boolean dentroDelBoton = btnRect.contains(touch.x, touch.y);
        boolean trucoDisponible = isTrucoDisponible();

        System.out.println("[BOTON_TRUCO] Dentro del botón: " + dentroDelBoton);
        System.out.println("[BOTON_TRUCO] Truco disponible: " + trucoDisponible);
        System.out.println("[BOTON_TRUCO] Estado Truco: " + partida.getEstadoTruco());
        System.out.println("[BOTON_TRUCO] Mano actual: " + partida.getManoActual());
        System.out.println("[BOTON_TRUCO] Es mi turno: " + partida.esMiTurnoLocal());
        System.out.println("[BOTON_TRUCO] Soy mano: " + partida.soyJugadorMano());
        System.out.println("[BOTON_TRUCO] Primer turno: " + partida.esPrimerTurnoEnMano());

        if (dentroDelBoton && trucoDisponible) {
            System.out.println("[BOTON_TRUCO] ✅ Condiciones cumplidas, intentando cantar truco...");
            boolean resultado = intentarCantarTruco();
            System.out.println("[BOTON_TRUCO] Resultado: " + resultado);
            System.out.println("[BOTON_TRUCO] ========================================\n");
            return resultado;
        } else {
            if (!dentroDelBoton) {
                System.out.println("[BOTON_TRUCO] ❌ Click fuera del botón");
            }
            if (!trucoDisponible) {
                System.out.println("[BOTON_TRUCO] ❌ Truco no disponible");
            }
            System.out.println("[BOTON_TRUCO] ⏭️ Pasando el evento al siguiente processor");
            System.out.println("[BOTON_TRUCO] ========================================\n");
            return false;
        }
    }

    private boolean intentarCantarTruco() {
        System.out.println("[BOTON_TRUCO] --> intentarCantarTruco()");

        boolean validacion = partida.cantarTruco();
        System.out.println("[BOTON_TRUCO] partida.cantarTruco() retornó: " + validacion);

        if (!validacion) {
            System.out.println("[BOTON_TRUCO] ❌ Validación falló en PartidaCliente");
            return false;
        }

        gestorSonido.reproducirSonido("truco");
        System.out.println("[BOTON_TRUCO] 🔊 Sonido reproducido");

        System.out.println("[BOTON_TRUCO] 📤 Enviando mensaje TRUCO al servidor...");
        hc.enviarMensaje("TRUCO");

        System.out.println("[BOTON_TRUCO] 🔒 Notificando truco enviado (bloqueo local)...");
        hc.notificarTrucoEnviado();

        System.out.println("[BOTON_TRUCO] ✅ Truco cantado exitosamente");
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