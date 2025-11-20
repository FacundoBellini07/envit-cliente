package juego.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import juego.elementos.*;
import juego.interfaces.GameController;
import juego.personajes.Jugador;
import juego.personajes.RivalBot;
import juego.personajes.TipoJugador;
import juego.red.HiloCliente;

import java.util.ArrayList;

public class PantallaPartida implements Screen, GameController {

    private Game game;
    private Mazo mazo;
    private Partida partida;
    private ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
    private SpriteBatch batch = new SpriteBatch();
    private Texture fondoPartida;
    private Texture mazoSprite;
    private Texture dorsoCartaSprite;
    private boolean inicioRonda = true;
    private Animacion animacion;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private CartaRenderer cartaRenderer;
    private ManoManager manoManager;

    private RivalBot rivalBot;
    private ManoRivalRenderer manoRivalRenderer;
    private ZonaJuego zonaJuegoRival;
    private ZonaJuego zonaJuegoJugador;

    private Hud hud;
    private BitmapFont font;

    private final float WORLD_WIDTH = 640;
    private final float WORLD_HEIGHT = 480;

    private final float CARTA_PROPORCION_ANCHO = 0.1f;
    private final float CARTA_RELACION_ASPECTO = 1.4f;

    private final float CARTA_ANCHO = WORLD_WIDTH * CARTA_PROPORCION_ANCHO;
    private final float CARTA_ALTO = CARTA_ANCHO * CARTA_RELACION_ASPECTO;
    private boolean empieza = false;
    private PantallaFinal pantallaFinal;

    private BotonTruco botonTruco;

    private boolean debeVolverAlMenu = false;
    private HiloCliente hc;
    private int mano = 0;

    private int miID = -1;
    private TipoJugador miRol;
    private int quienEmpieza;
    private boolean mostrarMensajeTrucoRival;
    private float tiempoMensajeTrucoRival = 0f;
    private final float DURACION_MENSAJE_TRUCO = 5.0f;

    public PantallaPartida(Game game) {
        this.game = game;
        this.crearJugadores();
        partida = new Partida();
    }

    private void crearJugadores() {
        Jugador jugador = new Jugador("Tú");
        Jugador rival = new Jugador("Rival");

        jugadores.add(jugador);
        jugadores.add(rival);
    }

    @Override
    public void show() {
        hc = new HiloCliente(this);
        hc.start();

        fondoPartida = new Texture(Gdx.files.internal("fondos/fondoPartida.png"));
        mazoSprite = new Texture(Gdx.files.internal("sprites/mazo_sprite.png"));
        dorsoCartaSprite = new Texture(Gdx.files.internal("sprites/dorso.png"));
        mazo = new Mazo();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        shapeRenderer = new ShapeRenderer();
        cartaRenderer = new CartaRenderer(batch, shapeRenderer, viewport);

        font = new BitmapFont();
        font.getData().setScale(1.2f);
        hud = new Hud(font, jugadores.get(0), jugadores.get(1), WORLD_WIDTH, WORLD_HEIGHT);

        // Crear las dos zonas de juego
        float zonaAncho = CARTA_ANCHO * 1.5f;
        float zonaAlto = CARTA_ALTO * 1.3f;

        float zonaJugadorX = (WORLD_WIDTH - zonaAncho) / 2f;
        float zonaJugadorY = (WORLD_HEIGHT / 2f) - zonaAlto;
        zonaJuegoJugador = new ZonaJuego(zonaJugadorX, zonaJugadorY, zonaAncho, zonaAlto);
        zonaJuegoJugador.setCartaRenderer(cartaRenderer);

        float zonaRivalX = (WORLD_WIDTH - zonaAncho) / 2f;
        float zonaRivalY = (WORLD_HEIGHT / 2f) + 20;
        zonaJuegoRival = new ZonaJuego(zonaRivalX, zonaRivalY, zonaAncho, zonaAlto);
        zonaJuegoRival.setCartaRenderer(cartaRenderer);

        // ✅ IMPORTANTE: ManoManager SIEMPRE maneja jugadores.get(0) (YO)
        // No importa si soy J1 o J2, ManoManager siempre dibuja MIS cartas abajo
        manoManager = new ManoManager(
                jugadores.get(0),  // Siempre el índice 0 es "yo"
                cartaRenderer,
                viewport,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                CARTA_ANCHO,
                CARTA_ALTO,
                hc
        );
        manoManager.setZonaJuego(zonaJuegoJugador);

        // ✅ IMPORTANTE: ManoRivalRenderer SIEMPRE maneja jugadores.get(1) (RIVAL)
        manoRivalRenderer = new ManoRivalRenderer(
                jugadores.get(1),  // Siempre el índice 1 es "rival"
                cartaRenderer,
                dorsoCartaSprite,
                zonaJuegoRival,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                CARTA_ANCHO,
                CARTA_ALTO
        );

        animacion = new Animacion(
                WORLD_WIDTH,
                WORLD_HEIGHT,
                CARTA_ANCHO,
                CARTA_ALTO,
                dorsoCartaSprite,
                manoManager
        );

        Gdx.input.setInputProcessor(manoManager.getInputMultiplexer());

        float btnTrucoAncho = 80f;
        float btnTrucoAlto = 60f;
        float margenIzq = 20f;
        float btnTrucoY = (WORLD_HEIGHT - btnTrucoAlto) / 2f;

        botonTruco = new BotonTruco(
                margenIzq,
                btnTrucoY,
                btnTrucoAncho,
                btnTrucoAlto,
                font,
                viewport,
                partida,
                hc
        );

        pantallaFinal = new PantallaFinal(
                font,
                viewport,
                hud,
                WORLD_WIDTH,
                WORLD_HEIGHT
        );

        partida.setZonaJuegos(zonaJuegoJugador, zonaJuegoRival);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {

        if(!empieza){
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            hud.dibujarMensajeCentral(batch, "ESPERANDO RIVAL...", Color.WHITE);
        }
        else {
            if (debeVolverAlMenu) {
                game.setScreen(new PantallaMenu((juego.Principal) game));
                dispose();
                return;
            }

            update(delta);
            Gdx.gl.glClearColor(0, 0.1f, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


            if (pantallaFinal.isActiva()) {
                pantallaFinal.render(batch, shapeRenderer);
                return;
            }

            // 1. DIBUJAR FONDO Y MAZO
            this.batch.setProjectionMatrix(viewport.getCamera().combined);
            this.batch.begin();
            batch.draw(fondoPartida, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

            float mazoAncho = CARTA_ANCHO;
            float mazoAlto = CARTA_ALTO;
            float margenHorizontal = WORLD_WIDTH * 0.05f;
            float mazoX = WORLD_WIDTH - mazoAncho - margenHorizontal;
            float mazoY = (WORLD_HEIGHT - mazoAlto) / 2f;

            animacion.render(batch);
            batch.draw(mazoSprite, mazoX, mazoY, mazoAncho, mazoAlto);

            this.batch.end();

            // 2. DIBUJAR LOS FONDOS DE LAS ZONAS DE JUEGO
            zonaJuegoJugador.renderFondo(shapeRenderer);
            zonaJuegoRival.renderFondo(shapeRenderer);

            // 3. DIBUJAR LAS CARTAS EN MANO
            this.batch.setProjectionMatrix(viewport.getCamera().combined);

            this.batch.begin();
            manoRivalRenderer.render(batch);
            this.batch.end();
            manoRivalRenderer.render(batch);
            manoManager.render();

            // 4. DIBUJAR LAS CARTAS DENTRO DE LAS ZONAS (jugadas)
            zonaJuegoJugador.renderCartas();
            zonaJuegoRival.renderCartas();

            // 5. DIBUJAR BOTÓN DE TRUCO
            botonTruco.render(batch, shapeRenderer);

            // 6. DIBUJAR HUD
            this.batch.setProjectionMatrix(viewport.getCamera().combined);
            hud.render(batch, partida.getManoActual(), partida.esMiTurnoLocal(),
                    partida.isTrucoActivoEnManoActual(), partida.getManoTrucoUsada());
        }
    }

    public void update(float delta) {
        animacion.update(delta);

        if (mostrarMensajeTrucoRival) {
            tiempoMensajeTrucoRival -= delta;
            if (tiempoMensajeTrucoRival <= 0) {
                mostrarMensajeTrucoRival = false;
            }
        }

        manoManager.setEsMiTurno(partida.esMiTurnoLocal());

        if (!pantallaFinal.isActiva() && !partida.partidaTerminada()) {
            botonTruco.update(delta);
            botonTruco.detectarClick();
        }

        if (pantallaFinal.isActiva()) {
            boolean solicitudVolver = pantallaFinal.update(delta);
            if (solicitudVolver) {
                debeVolverAlMenu = true;
            }
            return;
        }

        if (partida.partidaTerminada() && !pantallaFinal.isActiva()) {
            pantallaFinal.activar(
                    partida.getGanador(),
                    jugadores.get(0),
                    jugadores.get(1)
            );

            Gdx.input.setInputProcessor(null);
            System.out.println("¡PARTIDA TERMINADA! Ganador: " + partida.getGanador().getNombre());
            return;
        }

        // ✅ Reinicializar cuando tengamos 3 cartas
        if (inicioRonda) {
            // Verificar MIS cartas (jugadores.get(0) siempre soy YO)
            Carta[] miMano = jugadores.get(0).getMano();

            int cartasDisponibles = 0;
            for (Carta c : miMano) {
                if (c != null) cartasDisponibles++;
            }

            System.out.println("[UPDATE] inicioRonda=true, cartas disponibles: " + cartasDisponibles);

            if (cartasDisponibles == 3) {
                System.out.println("[UPDATE] ✅ Inicializando mano con 3 cartas");

                manoManager.inicializarMano();
                manoRivalRenderer.inicializarPosiciones();
                animacion.iniciarAnimacionReparto();

                inicioRonda = false;
                System.out.println("[UPDATE] inicioRonda ahora es false");
            }
        }
    }


    private void volverAlMenu() {
        debeVolverAlMenu = true;
    }

    @Override
    public void startGame(int idMano) {
        this.empieza = true;
        this.quienEmpieza = idMano;
        TipoJugador tipoMano = (idMano == 0) ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        // ✅ IMPORTANTE: rivalBot siempre es null en red, no lo necesitamos
        partida.inicializar(null,
                jugadores.get(0), jugadores.get(1), mano, miRol, tipoMano);

        System.out.println("[CLIENTE] Partida iniciada. Mi rol: " + miRol + ", Empieza: " + tipoMano);
    }
    public void onConectado(int id) {
        this.miID = id;
        if (miID == 0) {
            // Yo soy el Jugador 1
            this.miRol = TipoJugador.JUGADOR_1;
            jugadores.get(0).setNombre("Tú (J1)");
            jugadores.get(1).setNombre("Rival (J2)");
            System.out.println("[CLIENTE] Conectado como Jugador 1");
        } else {
            // Yo soy el Jugador 2
            this.miRol = TipoJugador.JUGADOR_2;
            jugadores.get(0).setNombre("Rival (J1)");
            jugadores.get(1).setNombre("Tú (J2)");
            System.out.println("[CLIENTE] Conectado como Jugador 2");
        }
        System.out.println("[CLIENTE] Mi rol asignado: " + miRol);
    }

    public void onEstadoActualizado(int mano, int p1, int p2, EstadoTurno turno, TipoJugador jugadorMano){
        partida.actualizarEstado(mano, p1, p2, turno, jugadorMano);
        System.out.println("[CLIENTE] Estado actualizado recibido: mano=" + mano + ", p1=" + p1 + ", p2=" + p2 + ", turno=" + turno + ", jugadorMano=" + jugadorMano);

    }
    public void onCartaRival(int valor, Palo palo){
        Carta cartaRival = new Carta(valor, palo);

        if (zonaJuegoRival != null) {

            com.badlogic.gdx.math.Rectangle limitesZona = zonaJuegoRival.getLimites();


            float xCarta = limitesZona.x + (limitesZona.width - CARTA_ANCHO) / 2f;
            float yCarta = limitesZona.y + (limitesZona.height - CARTA_ALTO) / 2f;

            cartaRival.updateLimites(xCarta, yCarta, CARTA_ANCHO, CARTA_ALTO);

            zonaJuegoRival.agregarCarta(cartaRival);

            System.out.println("[PANTALLA] Carta del rival agregada y dimensionada.");
        }
    }
    public void onTrucoRival(){
        this.mostrarMensajeTrucoRival = true;
        this.tiempoMensajeTrucoRival = DURACION_MENSAJE_TRUCO;
    }

    public void onCartaRecibida(int valor, Palo palo) {
        Carta carta = new Carta(valor, palo);

        jugadores.get(0).agregarCarta(carta);
        System.out.println("[CLIENTE " + miRol + "] Carta agregada a MI mano (índice 0): " + valor + " de " + palo);

        // Verificar cuántas cartas tengo
        Carta[] miMano = jugadores.get(0).getMano();

        int cartasRecibidas = 0;
        for (Carta c : miMano) {
            if (c != null) cartasRecibidas++;
        }

        System.out.println("[CLIENTE] Cartas en mi mano: " + cartasRecibidas + "/3");

        if (cartasRecibidas == 3) {
            inicioRonda = true;
            System.out.println("[CLIENTE] ¡Tengo 3 cartas! Marcando inicioRonda=true");
        }
    }

    public void onNuevaRonda() {
        System.out.println("[PANTALLA] Nueva ronda iniciada por el servidor");

        // Limpiar zonas de juego
        if (zonaJuegoJugador != null) zonaJuegoJugador.limpiar();
        if (zonaJuegoRival != null) zonaJuegoRival.limpiar();

        // Limpiar las manos de los jugadores
        jugadores.get(0).limpiarMazo();
        jugadores.get(1).limpiarMazo();

        // ✅ IMPORTANTE: Marcar que necesitamos reinicializar cuando lleguen las cartas
        inicioRonda = true;

        System.out.println("[PANTALLA] Manos limpiadas, esperando nuevas cartas del servidor");
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("Se ha redimensionado la pantalla a: " + width + "x" + height);
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (fondoPartida != null) fondoPartida.dispose();
        if (mazoSprite != null) mazoSprite.dispose();
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
}