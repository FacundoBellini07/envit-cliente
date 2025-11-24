package juego.pantallas;

import com.badlogic.gdx.audio.Sound;
import juego.Principal;
import juego.utilidades.GestorSonido;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import juego.utilidades.GestorFuentes;

import java.util.Random;

public class PantallaMenu implements Screen {

    private final Principal game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture backgroundTexture;
    private Texture optionsBackgroundTexture;
    private boolean inOptionsMode = false;

    // Efectos CRT
    private boolean crtEnabled = true;
    private boolean flickerEnabled = true;
    private boolean shakeEnabled = true;
    private float scanlineOffset = 0f;
    private float crtFlicker = 0f;

    private Random random = new Random();
    private static final float VIRTUAL_WIDTH = 1280;
    private static final float VIRTUAL_HEIGHT = 720;
    private Viewport viewport;

    // Botones principales
    private Rectangle btnPlayRect = new Rectangle();
    private Rectangle btnOptionsRect = new Rectangle();
    private Rectangle btnExitRect = new Rectangle();

    // Botones de opciones
    private Rectangle btnCloseOptionsRect = new Rectangle();
    private Rectangle chkCRTBox = new Rectangle();
    private Rectangle chkFlickerBox = new Rectangle();
    private Rectangle chkShakeBox = new Rectangle();
    private Rectangle chkMusicBox = new Rectangle();

    //  Barra de volumen
    private Rectangle sliderBarRect = new Rectangle();
    private Rectangle sliderKnobRect = new Rectangle();
    private boolean draggingVolume = false;

    private Texture btnPlayTexture, btnOptionsTexture, btnExitTexture, btnCloseOptionsTexture;
    private Texture chkCheckedTexture, chkUncheckedTexture;
    private Texture titleTexture;
    private Texture whitePixel;

    //  Gestor de sonido
    private GestorSonido gestorSonido;

    public PantallaMenu(final Principal game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Inicializar gestor de sonido
        gestorSonido = GestorSonido.getInstancia();

        loadFont();
        loadBackgrounds();
        loadButtonTextures();
        setButtonRects();
        cargarSonidos();
    }

    private void loadFont() {
        try {
            GestorFuentes gestorFuentes = GestorFuentes.getInstancia();
            font = gestorFuentes.getMenuTitle();
            Gdx.app.log("PantallaMenu", "Fuente medieval.ttf cargada desde el gestor");
        } catch (Exception e) {
            font = new BitmapFont();
            Gdx.app.log("PantallaMenu", "No se pudo cargar la fuente medieval.ttf, usando fuente por defecto");
        }
    }

    private void loadBackgrounds() {
        if (Gdx.files.internal("fondos/fondo.png").exists()) {
            backgroundTexture = new Texture(Gdx.files.internal("fondos/fondo.png"));
            Gdx.app.log("PantallaMenu", "Fondo principal cargado correctamente");
        } else {
            backgroundTexture = null;
            Gdx.app.error("PantallaMenu", "No se encontró fondos/fondo.png, usando fondo procedural");
        }

        if (Gdx.files.internal("fondos/fondoOpciones.png").exists()) {
            optionsBackgroundTexture = new Texture(Gdx.files.internal("fondos/fondoOpciones.png"));
            Gdx.app.log("PantallaMenu", "Fondo de opciones cargado correctamente");
        } else {
            optionsBackgroundTexture = null;
            Gdx.app.error("PantallaMenu", "No se encontró fondos/fondoOpciones.png, usando fondo procedural para opciones");
        }
    }

    private void loadButtonTextures() {
        // Texturas opcionales para botones
    }

    private void setButtonRects() {
        float w = VIRTUAL_WIDTH;
        float h = VIRTUAL_HEIGHT;
        float btnW = 240, btnH = 60;
        float espacio = 32;
        float totalH = 3 * btnH + 2 * espacio;
        float startY = h / 3.5f - totalH / 2f;

        float centerX = w / 1.15f - btnW / 2f;
        btnPlayRect.set(centerX, startY + 2 * (btnH + espacio), btnW, btnH);
        btnOptionsRect.set(centerX, startY + (btnH + espacio), btnW, btnH);
        btnExitRect.set(centerX, startY, btnW, btnH);

        // Botón cerrar opciones
        btnCloseOptionsRect.set(centerX - 64, -startY + 2 * (btnH + espacio), btnW, btnH);

        // Checkboxes en opciones
        float chkW = 56, chkH = 56;
        float chkEspacio = 40;
        float chkX = w / 2.5f - chkW / 2f;
        float chkStartY = h / 2.5f;

        chkCRTBox.set(chkX, chkStartY, chkW, chkH);
        chkFlickerBox.set(chkX, chkStartY - (chkH + chkEspacio), chkW, chkH);
        chkShakeBox.set(chkX, chkStartY - 2 * (chkH + chkEspacio), chkW, chkH);
        chkMusicBox.set(chkX, chkStartY - 3 * (chkH + chkEspacio), chkW, chkH);

        //  Barra de volumen
        float sliderW = 300;
        float sliderH = 20;
        float sliderX = w / 5f - sliderW / 1.5f;
        float sliderY = chkStartY;
        sliderBarRect.set(sliderX, sliderY, sliderW, sliderH);

        // Knob del slider
        float knobSize = 30;
        float knobX = sliderX + (sliderW * gestorSonido.getVolumenMusica()) - knobSize / 2f;
        sliderKnobRect.set(knobX, sliderY - 5, knobSize, knobSize);
    }

    private void cargarSonidos() {

        gestorSonido.cargarMusica("menu","sounds/fuego.mp3");
        gestorSonido.cargarSonido("click","sounds/click.wav");
        gestorSonido.reproducirMusica("menu");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        setButtonRects();

    }

    @Override
    public void render(float delta) {
        if (crtEnabled) {
            scanlineOffset += 60 * delta * 0.5f;
            if (scanlineOffset > 4) scanlineOffset = 0;
        }

        if (flickerEnabled) {
            crtFlicker += 60 * delta * 0.1f;
            if (crtFlicker > Math.PI * 2) crtFlicker = 0;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Dibujar fondo
        if (!inOptionsMode) {
            if (backgroundTexture != null) {
                batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            } else {
                drawProceduralBackground(batch);
            }
        } else {
            if (optionsBackgroundTexture != null) {
                batch.draw(optionsBackgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            } else {
                drawProceduralOptionsBackground(batch);
            }
        }

        // Dibujar título
        if (titleTexture != null) {
            batch.draw(titleTexture, VIRTUAL_WIDTH/2f - titleTexture.getWidth()/2f, VIRTUAL_HEIGHT - 120);
        } else if (font != null) {
            font.setColor(Color.valueOf("F0C850"));
            font.getData().setScale(4.75f);
            font.draw(batch, "Envit", VIRTUAL_WIDTH/2f - 170, VIRTUAL_HEIGHT - 220);
        }

        // Dibujar botones
        if (!inOptionsMode) {
            drawButton(batch, btnPlayTexture, btnPlayRect, "JUGAR");
            drawButton(batch, btnOptionsTexture, btnOptionsRect, "OPCIONES");
            drawButton(batch, btnExitTexture, btnExitRect, "SALIR");
        } else {
            drawButton(batch, btnCloseOptionsTexture, btnCloseOptionsRect, "CERRAR");

            // Checkboxes
            drawCheckbox(batch, chkCRTBox, crtEnabled, "Efectos CRT");
            drawCheckbox(batch, chkFlickerBox, flickerEnabled, "Destellos");
            drawCheckbox(batch, chkShakeBox, shakeEnabled, "Temblor");
            drawCheckbox(batch, chkMusicBox, gestorSonido.isMusicaHabilitada(), "Musica");

            // Etiqueta de volumen
            font.setColor(Color.WHITE);
            font.getData().setScale(1.0f);
            font.draw(batch, "Volumen", sliderBarRect.x, sliderBarRect.y + 60);
        }

        batch.end();

        // Dibujar barra de volumen (con ShapeRenderer)
        if (inOptionsMode) {
            drawVolumeSlider();
        }

        // Efectos CRT
        if (crtEnabled) {
            drawCRTEffect();
        }

        handleInput();
    }

    private void drawButton(SpriteBatch batch, Texture texture, Rectangle rect, String texto) {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean hovered = rect.contains(mouse.x, mouse.y);

        if (texture != null) {
            batch.setColor(hovered ? new Color(15, 55, 175, 1) : Color.WHITE);
            batch.draw(texture, rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.GOLDENROD);
        } else if (font != null) {
            batch.setColor(hovered ? new Color(15, 55, 175, 0.25f) : new Color(0,0,0,0.25f));
            batch.draw(getWhitePixel(), rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.WHITE);
        }

        if (font != null) {
            font.setColor(hovered ? Color.valueOf("F0C850") : Color.WHITE);
            font.getData().setScale(1.2f);

            GlyphLayout layout = new GlyphLayout(font, texto);

            float textX = rect.x + (rect.width - layout.width) / 2f;
            float textY = rect.y + (rect.height + layout.height) / 2f;

            font.draw(batch, layout, textX, textY);
        }
    }

    private void drawCheckbox(SpriteBatch batch, Rectangle rect, boolean checked, String label) {
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean hovered = rect.contains(mouse.x, mouse.y);

        Texture tex = checked ? chkCheckedTexture : chkUncheckedTexture;
        if (tex != null) {
            batch.setColor(hovered ? new Color(0.7f, 1f, 1f, 1f) : Color.WHITE);
            batch.draw(tex, rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.WHITE);
        } else {
            batch.setColor(hovered ? new Color(0.3f, 0.5f, 0.8f, 0.8f) : new Color(0.2f, 0.2f, 0.4f, 0.8f));
            batch.draw(getWhitePixel(), rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.WHITE);

            if (checked) {
                batch.setColor(Color.valueOf("F0C850"));
                float margin = 10;
                batch.draw(getWhitePixel(), rect.x + margin, rect.y + margin,
                        rect.width - margin * 2, rect.height - margin * 2);
                batch.setColor(Color.WHITE);
            }
        }

        if (font != null) {
            font.setColor(hovered ? Color.valueOf("F0C850") : Color.WHITE);
            font.getData().setScale(1f);
            float labelX = rect.x + rect.width + 16;
            float labelY = rect.y + rect.height - 12;
            font.draw(batch, label, labelX, labelY);
        }
    }

    //  Dibuja la barra de volumen con shader
    private void drawVolumeSlider() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Barra de fondo
        shapeRenderer.setColor(0.2f, 0.2f, 0.4f, 0.8f);
        shapeRenderer.rect(sliderBarRect.x, sliderBarRect.y, sliderBarRect.width, sliderBarRect.height);

        // Barra de progreso (dorada)
        float progressWidth = sliderBarRect.width * gestorSonido.getVolumenMusica();
        shapeRenderer.setColor(Color.valueOf("F0C850"));
        shapeRenderer.rect(sliderBarRect.x, sliderBarRect.y, progressWidth, sliderBarRect.height);

        // Knob (círculo)
        Vector2 mouse = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean hovered = sliderKnobRect.contains(mouse.x, mouse.y) || draggingVolume;

        shapeRenderer.setColor(hovered ? Color.WHITE : Color.LIGHT_GRAY);
        shapeRenderer.circle(
                sliderKnobRect.x + sliderKnobRect.width / 2f,
                sliderKnobRect.y + sliderKnobRect.height / 2f,
                sliderKnobRect.width / 2f
        );

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Texto del porcentaje
        batch.begin();
        String volText = (int)(gestorSonido.getVolumenMusica() * 100) + "%";
        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f);
        font.draw(batch, volText,
                sliderBarRect.x + sliderBarRect.width + 20,
                sliderBarRect.y + sliderBarRect.height
        );
        batch.end();
    }

    private void handleInput() {
        Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        float x = touch.x;
        float y = touch.y;

        //  Manejar arrastre del slider de volumen
        if (inOptionsMode) {
            if (Gdx.input.isTouched()) {

                if (draggingVolume || sliderBarRect.contains(x, y) || sliderKnobRect.contains(x, y)) {
                    draggingVolume = true;

                    // Calcular nuevo volumen
                    float newVolume = (x - sliderBarRect.x) / sliderBarRect.width;
                    newVolume = Math.max(0f, Math.min(1f, newVolume));
                    gestorSonido.setVolumenMusica(newVolume);

                    // Actualizar posición del knob
                    float knobX = sliderBarRect.x + (sliderBarRect.width * newVolume) - sliderKnobRect.width / 2f;
                    sliderKnobRect.x = knobX;
                }
            } else {
                draggingVolume = false;
            }
        }

        // Clicks en botones
        if (Gdx.input.justTouched()) {
            if (!inOptionsMode) {
                if (btnPlayRect.contains(x, y)) {
                    gestorSonido.reproducirSonido("click");
                    startGame();
                }
                else if (btnOptionsRect.contains(x, y)) {
                    gestorSonido.reproducirSonido("click");
                    toggleOptions();
                }
                else if (btnExitRect.contains(x, y)) {
                    gestorSonido.reproducirSonido("click");
                    Gdx.app.exit();
                }
            } else {
                if (btnCloseOptionsRect.contains(x, y)) {
                    gestorSonido.reproducirSonido("click");
                    toggleOptions();
                }
                else if (chkCRTBox.contains(x, y)) {
                    gestorSonido.reproducirSonido("click");
                    crtEnabled = !crtEnabled;
                }
                else if (chkFlickerBox.contains(x, y)) {
                    gestorSonido.reproducirSonido("click");
                    flickerEnabled = !flickerEnabled;
                }
                else if (chkShakeBox.contains(x, y)) {
                    gestorSonido.reproducirSonido("click");
                    shakeEnabled = !shakeEnabled;
                }
                else if (chkMusicBox.contains(x, y)) {
                    gestorSonido.reproducirSonido("click");
                    boolean nuevoEstado = !gestorSonido.isMusicaHabilitada();
                    gestorSonido.setMusicaHabilitada(nuevoEstado);
                }
            }
        }
    }

    private void toggleOptions() {
        inOptionsMode = !inOptionsMode;
    }

    private void startGame() {
        gestorSonido.detenerMusica();
        game.setScreen(new PantallaPartida(game));
    }

    private void drawProceduralBackground(SpriteBatch batch) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int w = (int)VIRTUAL_WIDTH;
        int h = (int)VIRTUAL_HEIGHT;
        shapeRenderer.rect(0, 0, w, h,
                new Color(0.04f, 0.04f, 0.12f,1),
                new Color(0.16f, 0.04f, 0.24f,1),
                new Color(0.24f, 0.08f, 0.39f,1),
                new Color(0.04f, 0.16f, 0.39f,1));
        shapeRenderer.end();
        batch.begin();
    }

    private void drawProceduralOptionsBackground(SpriteBatch batch) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int w = (int)VIRTUAL_WIDTH;
        int h = (int)VIRTUAL_HEIGHT;
        shapeRenderer.rect(0, 0, w, h,
                new Color(0.04f, 0.04f, 0.12f,1),
                new Color(0.16f, 0.04f, 0.24f,1),
                new Color(0.24f, 0.08f, 0.39f,1),
                new Color(0.04f, 0.16f, 0.39f,1));
        shapeRenderer.end();
        batch.begin();
    }

    private void drawCRTEffect() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int w = (int)VIRTUAL_WIDTH;
        int h = (int)VIRTUAL_HEIGHT;

        shapeRenderer.setColor(0, 0, 0, 0.13f);
        for (int y = (int) scanlineOffset; y < h; y += 3) {
            shapeRenderer.rect(0, y, w, 1);
        }

        if (random.nextInt(60) < 2) {
            shapeRenderer.setColor(0, 0, 0, 0.06f);
            for (int x = 0; x < w; x += 2) {
                shapeRenderer.rect(x, 0, 1, h);
            }
        }

        shapeRenderer.end();

        if (flickerEnabled && random.nextInt(100) < 8) {
            batch.begin();
            Color flickerColor = new Color(1,1,1, 0.04f + 0.07f * (float)Math.sin(crtFlicker));
            batch.setColor(flickerColor);
            batch.draw(getWhitePixel(), 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            batch.setColor(Color.WHITE);
            batch.end();
        }

        if (random.nextInt(150) < 2) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < 40; i++) {
                float x = random.nextInt(w);
                float y = random.nextInt(h);
                float alpha = random.nextFloat() * 0.2f;
                shapeRenderer.setColor(1, 1, 1, alpha);
                shapeRenderer.rect(x, y, 2, 2);
            }
            shapeRenderer.end();
        }

        if (shakeEnabled && random.nextInt(300) < 2) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            int distortY = random.nextInt(h);
            shapeRenderer.setColor(1, 1, 1, 0.08f);
            shapeRenderer.rect(0, distortY, w, 3);
            shapeRenderer.end();
        }
    }

    private Texture getWhitePixel() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return whitePixel;
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        gestorSonido.pausarMusica();
    }

    @Override
    public void show() {
        gestorSonido.reproducirMusica("menu");
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (optionsBackgroundTexture != null) optionsBackgroundTexture.dispose();
        if (whitePixel != null) whitePixel.dispose();
        if (btnPlayTexture != null) btnPlayTexture.dispose();
        if (btnOptionsTexture != null) btnOptionsTexture.dispose();
        if (btnExitTexture != null) btnExitTexture.dispose();
        if (btnCloseOptionsTexture != null) btnCloseOptionsTexture.dispose();
        if (chkCheckedTexture != null) chkCheckedTexture.dispose();
        if (chkUncheckedTexture != null) chkUncheckedTexture.dispose();
        if (titleTexture != null) titleTexture.dispose();
    }
}