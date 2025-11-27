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

public class BotonRespuesta implements InputProcessor {

    private Rectangle btnRect;
    private boolean hovered = false;
    private float animacionPulso = 0f;

    private BitmapFont font;
    private Viewport viewport;
    private HiloCliente hc;
    private GestorSonido gestorSonido;
    private BotonTruco botonTrucoLocal;
    private Color colorNormal = new Color(0.9f, 0.1f, 0.1f, 0.9f);
    private Color colorHover = new Color(1.0f, 0.2f, 0.2f, 1f);
    private Color colorTexto = Color.WHITE;

    private boolean visible = false;

    private PartidaCliente partida;

    public BotonRespuesta(float x, float y, float ancho, float alto,
                          BitmapFont font, Viewport viewport, HiloCliente hc,
                          PartidaCliente partida) {
        this.btnRect = new Rectangle(x, y, ancho, alto);
        this.font = font;
        this.viewport = viewport;
        this.hc = hc;
        this.partida = partida;
        this.gestorSonido = GestorSonido.getInstancia();
    }

    public void mostrar() {
        this.visible = true;
    }

    public void ocultar() {
        this.visible = false;
    }

    public void update(float delta) {
        if (!visible) return;
        animacionPulso += delta * 3f;
        if (animacionPulso > Math.PI * 2) animacionPulso = 0f;
        actualizarHover();
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!visible) return;

        if (partida != null && partida.getEstadoTruco() == EstadoTruco.VALE_CUATRO_CANTADO) {
            return;
        }

        Color colorBtn;
        float escala = 1.3f;

        if (hovered) {
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
        shapeRenderer.rect(btnRect.x - offsetX, btnRect.y - offsetY, btnRect.width * escala, btnRect.height * escala);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(btnRect.x - offsetX, btnRect.y - offsetY, btnRect.width * escala, btnRect.height * escala);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(colorTexto);
        font.getData().setScale(0.8f);

        String textoBoton = "QUIERO";
        GlyphLayout layout = new GlyphLayout(font, textoBoton);
        float textX = btnRect.x + (btnRect.width - layout.width) / 2f;
        float textY = btnRect.y + (btnRect.height + layout.height) / 2f;

        font.draw(batch, textoBoton, textX, textY);
        batch.end();
    }

    private void actualizarHover() {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        hovered = btnRect.contains(mouse.x, mouse.y);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!visible) return false;

        Vector2 touch = viewport.unproject(new Vector2(screenX, screenY));

        if (btnRect.contains(touch.x, touch.y)) {
            System.out.println("[BOTON_RESPUESTA] ✅ Click detectado en QUIERO");
            gestorSonido.reproducirSonido("truco"); // Usamos sonido truco por consistencia o el que prefieras
            hc.enviarMensaje("QUIERO");
            ocultar();
            return true;
        }
        return false;
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