package juego.pantallas;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import juego.personajes.TipoJugador;
import juego.red.HiloCliente;
import juego.utilidades.GestorFuentes;
import juego.utilidades.GestorSonido;
import juego.elementos.Hud;
import java.util.ArrayList;

public class PantallaPartida implements Screen, GameController {

    private Game game;
    private Mazo mazo;
    private PartidaCliente partida;
    private ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
    private SpriteBatch batch = new SpriteBatch();
    private Texture fondoPartida;
    private Texture mazoSprite;
    private Texture dorsoCartaSprite;
    private Texture casilla;
    private boolean inicioRonda = true;
    private Animacion animacion;

    private boolean esperandoRespuestaRival = false; // Si yo (P1) canté Truco
    private boolean deboResponderTruco = false;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private CartaRenderer cartaRenderer;
    private ManoManager manoManager;

    private ManoRivalRenderer manoRivalRenderer;
    private ZonaJuego zonaJuegoRival;
    private ZonaJuego zonaJuegoJugador;

    private Hud hud;
    private BitmapFont font;
    private BitmapFont fontBotonTruco;

    private final float WORLD_WIDTH = 640;
    private final float WORLD_HEIGHT = 480;

    private final float CARTA_PROPORCION_ANCHO = 0.1f;
    private final float CARTA_RELACION_ASPECTO = 1.4f;

    private final float CARTA_ANCHO = WORLD_WIDTH * CARTA_PROPORCION_ANCHO;
    private final float CARTA_ALTO = CARTA_ANCHO * CARTA_RELACION_ASPECTO;
    private boolean empieza = false;
    private PantallaFinal pantallaFinal;

    private BotonTruco botonTruco;
    private BotonRespuesta botonRespuesta;

    private boolean rivalDesconectado = false;
    private boolean servidorDesconectado = false;
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
        partida = new PartidaCliente();
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

        casilla = new Texture(Gdx.files.internal("sprites/casilla.png"));
        fondoPartida = new Texture(Gdx.files.internal("fondos/fondoPartida.png"));
        mazoSprite = new Texture(Gdx.files.internal("sprites/mazo_sprite.png"));
        dorsoCartaSprite = new Texture(Gdx.files.internal("sprites/dorso.png"));
        mazo = new Mazo();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        shapeRenderer = new ShapeRenderer();
        cartaRenderer = new CartaRenderer(batch, shapeRenderer, viewport);

        hud = new Hud(jugadores.get(0), jugadores.get(1), WORLD_WIDTH, WORLD_HEIGHT, miRol);

        GestorFuentes gestorFuentes = GestorFuentes.getInstancia();
        fontBotonTruco = gestorFuentes.getBoton20();
        font = gestorFuentes.getMediana();

        GestorSonido gestorSonido = GestorSonido.getInstancia();
        gestorSonido.cargarSonido("carta", "sounds/carta.wav");
        gestorSonido.cargarSonido("truco", "sounds/truco.mp3");
        gestorSonido.cargarSonido("victoria", "sounds/victoria.wav");
        gestorSonido.cargarSonido("derrota", "sounds/derrota.wav");

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

        manoManager = new ManoManager(
                jugadores.get(0),
                cartaRenderer,
                viewport,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                CARTA_ANCHO,
                CARTA_ALTO,
                hc
        );
        manoManager.setZonaJuego(zonaJuegoJugador);

        manoRivalRenderer = new ManoRivalRenderer(
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




            crearBotonTruco();
            crearBotonRespuesta(); // ✅ NUEVO

            System.out.println("\n[PANTALLA] ========================================");
            System.out.println("[PANTALLA] Configurando InputMultiplexer");
            System.out.println("[PANTALLA] ========================================\n");

            InputMultiplexer multiplexer = manoManager.getInputMultiplexer();

            // ✅ AGREGAR BOTÓN DE RESPUESTA PRIMERO (máxima prioridad cuando está visible)
            System.out.println("[PANTALLA] Agregando BotonRespuesta como PRIMER processor");
            multiplexer.addProcessor(botonRespuesta);

            System.out.println("[PANTALLA] Agregando BotonTruco como SEGUNDO processor");
            multiplexer.addProcessor(botonTruco);

            System.out.println("[PANTALLA] Configurando multiplexer como InputProcessor principal");
            Gdx.input.setInputProcessor(multiplexer);

            // ✅ VERIFICAR ORDEN
            System.out.println("[PANTALLA] Orden de processors:");
            for (int i = 0; i < multiplexer.getProcessors().size; i++) {
                System.out.println("[PANTALLA]   " + i + ": " + multiplexer.getProcessors().get(i).getClass().getSimpleName());
            }
            System.out.println("[PANTALLA] ========================================\n");

            pantallaFinal = new PantallaFinal(
                    viewport, hud, WORLD_WIDTH, WORLD_HEIGHT, miRol
            );

            partida.setZonaJuegos(zonaJuegoJugador, zonaJuegoRival);
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }


    @Override
    public void render(float delta) {

        if(!empieza){
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            viewport.apply();

            batch.setProjectionMatrix(viewport.getCamera().combined);

            hud.dibujarMensajeCentral(batch, "ESPERANDO RIVAL...", Color.WHITE);
        }
        else {
            if (debeVolverAlMenu) {
                game.setScreen(new PantallaMenu((juego.Principal) game));
                dispose();
                return;
            }
            if (rivalDesconectado) {
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                viewport.apply();
                batch.setProjectionMatrix(viewport.getCamera().combined);
                batch.begin();

                font.getData().setScale(1.5f);
                font.setColor(Color.RED);

                String msg = "¡RIVAL DESCONECTADO!";
                GlyphLayout layout = new GlyphLayout(font, msg);
                float textX = (WORLD_WIDTH - layout.width) / 2f;
                float textY = (WORLD_HEIGHT + layout.height) / 2f + 50;
                font.draw(batch, msg, textX, textY);

                font.getData().setScale(1.0f);
                font.setColor(Color.WHITE);
                String msg2 = "Toca la pantalla para volver al menú";
                layout = new GlyphLayout(font, msg2);
                textX = (WORLD_WIDTH - layout.width) / 2f;
                textY = (WORLD_HEIGHT - layout.height) / 2f - 50;
                font.draw(batch, msg2, textX, textY);

                batch.end();

                if (Gdx.input.justTouched()) {
                    game.setScreen(new PantallaMenu((juego.Principal) game));
                    dispose();
                }
                return;
            }
            if (servidorDesconectado) {
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                hc.detener();
                viewport.apply();
                batch.setProjectionMatrix(viewport.getCamera().combined);
                batch.begin();

                font.getData().setScale(1.5f);
                font.setColor(Color.RED);

                String msg = "¡SERVIDOR DESCONECTADO!";
                GlyphLayout layout = new GlyphLayout(font, msg);
                float textX = (WORLD_WIDTH - layout.width) / 2f;
                float textY = (WORLD_HEIGHT + layout.height) / 2f + 50;
                font.draw(batch, msg, textX, textY);

                font.getData().setScale(1.0f);
                font.setColor(Color.WHITE);
                String msg2 = "Toca la pantalla para volver al menú";
                layout = new GlyphLayout(font, msg2);
                textX = (WORLD_WIDTH - layout.width) / 2f;
                textY = (WORLD_HEIGHT - layout.height) / 2f - 50;
                font.draw(batch, msg2, textX, textY);

                batch.end();

                if (Gdx.input.justTouched()) {
                    game.setScreen(new PantallaMenu((juego.Principal) game));
                    dispose();
                }
                return;
            }

            update(delta);
            Gdx.gl.glClearColor(0, 0.1f, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


            if (pantallaFinal.isActiva()) {
                pantallaFinal.render(batch, shapeRenderer);
                return;
            }
            boolean esMiTurnoDeCartas = partida.esMiTurnoLocal();
            boolean bloqueoPorTruco = partida.estaBloqueoPorTruco() || deboResponderTruco;

            // Solo permito input si es mi turno Y NO estoy bloqueado por un truco pendiente
            manoManager.setEsMiTurno(esMiTurnoDeCartas && !bloqueoPorTruco);

            // DIBUJAR GUI DE RESPUESTA SI ES NECESARIO
            if (deboResponderTruco) {

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
            zonaJuegoJugador.renderFondo(batch, casilla);
            zonaJuegoRival.renderFondo(batch, casilla);


            // 3. DIBUJAR LAS CARTAS EN MANO
            this.batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            manoRivalRenderer.render(batch);
            batch.end();

            manoManager.render();
            zonaJuegoJugador.renderCartas();
            zonaJuegoRival.renderCartas();


            botonTruco.render(batch, shapeRenderer);
            botonRespuesta.render(batch, shapeRenderer);
            // 6. DIBUJAR HUD
            this.batch.setProjectionMatrix(viewport.getCamera().combined);
            hud.render(batch, partida.getManoActual(), partida.esMiTurnoLocal(),
                    partida.getEstadoTruco());
        }
    }

    public void update(float delta) {
        if (!pantallaFinal.isActiva() && !partida.partidaTerminada()) {
            botonTruco.update(delta);
            botonRespuesta.update(delta);
        }

        // ===============================================
        // 2. RESTO DE ACTUALIZACIONES (Animaciones, timers, etc.)
        // ===============================================
        animacion.update(delta);

        if (partida.getManoActual() > 0) {
            mostrarMensajeTrucoRival = false;
        }

        if (mostrarMensajeTrucoRival) {
            tiempoMensajeTrucoRival -= delta;
            if (tiempoMensajeTrucoRival <= 0) {
                mostrarMensajeTrucoRival = false;
            }
        }

        // 3. Control de Turno del ManoManager (que hace el drag de cartas)
        // Este código DEBE IR DESPUÉS del chequeo del botón de Truco.
        manoManager.setEsMiTurno(partida.esMiTurnoLocal());

        // 4. Chequeos de fin de partida y finalización
        if (pantallaFinal.isActiva()) {
            boolean solicitudVolver = pantallaFinal.update(delta);
            if (solicitudVolver) {
                debeVolverAlMenu = true;
            }
            return;
        }
        if (partida.partidaTerminada() && !pantallaFinal.isActiva()) {
            // ... (Tu código para activar pantalla final) ...
            return;
        }

        // 5. Lógica de inicio de ronda
        if (inicioRonda) {
            Carta[] miMano = jugadores.get(0).getMano();

            int cartasDisponibles = 0;
            for (Carta c : miMano) {
                if (c != null) {
                    cartasDisponibles++;
                    System.out.println("[UPDATE] Carta " + cartasDisponibles + ": " + c.getNombre());
                }
            }

            System.out.println("[UPDATE] inicioRonda=true, cartas disponibles: " + cartasDisponibles);

            if (cartasDisponibles == 3) {
                System.out.println("\n[PANTALLA] ========================================");
                System.out.println("[PANTALLA] Inicializando mano con 3 cartas");
                System.out.println("[PANTALLA] ========================================\n");

                InputMultiplexer multiplexer = manoManager.getInputMultiplexer();

                System.out.println("[PANTALLA] Limpiando multiplexer...");
                multiplexer.clear();

                System.out.println("[PANTALLA] RE-agregando BotonTruco como PRIMER processor");
                multiplexer.addProcessor(botonTruco);
                multiplexer.addProcessor(botonRespuesta);

                System.out.println("[PANTALLA] Inicializando cartas (se agregarán al multiplexer)");
                manoManager.inicializarMano();

                System.out.println("[PANTALLA] Re-configurando InputProcessor");
                Gdx.input.setInputProcessor(multiplexer);

                // ✅ VERIFICAR ORDEN NUEVAMENTE
                System.out.println("[PANTALLA] Orden FINAL de processors:");
                for (int i = 0; i < multiplexer.getProcessors().size; i++) {
                    System.out.println("[PANTALLA]   " + i + ": " + multiplexer.getProcessors().get(i).getClass().getSimpleName());
                }
                System.out.println("[PANTALLA] ========================================\n");

                animacion.iniciarAnimacionReparto();
                inicioRonda = false;
            }
        }
    }

    private void crearBotonTruco() {
        float btnTrucoAncho = 80f;
        float btnTrucoAlto = 60f;
        float margenIzq = 20f;
        float btnTrucoY = (WORLD_HEIGHT - btnTrucoAlto) / 2f;

        //  Asegurar que fontBotonTruco no sea null
        if (fontBotonTruco == null) {
            GestorFuentes gestorFuentes = GestorFuentes.getInstancia();
            fontBotonTruco = gestorFuentes.getBoton20();
            System.out.println("[PANTALLA] ⚠️ fontBotonTruco era null, reinicializado desde AplicarFuentes");
        }

        botonTruco = new BotonTruco(
                margenIzq,
                btnTrucoY,
                btnTrucoAncho,
                btnTrucoAlto,
                fontBotonTruco,
                viewport,
                partida,
                hc
        );
    }
    private void crearBotonRespuesta() {
        float btnRespuestaAncho = 80f;
        float btnRespuestaAlto = 60f;
        float margenIzq = 20f;
        float espacioEntreBotones = 30f;

        float btnRespuestaX = margenIzq + 80f + espacioEntreBotones; // 80f es el ancho del botón truco
        float btnRespuestaY = (WORLD_HEIGHT - btnRespuestaAlto) / 2f;

        if (fontBotonTruco == null) {
            GestorFuentes gestorFuentes = GestorFuentes.getInstancia();
            fontBotonTruco = gestorFuentes.getBoton20();
            System.out.println("[PANTALLA] ⚠️ fontBotonTruco era null, reinicializado desde GestorFuentes");
        }

         botonRespuesta = new BotonRespuesta(
                btnRespuestaX,
                btnRespuestaY,
                btnRespuestaAncho,
                btnRespuestaAlto,
                fontBotonTruco,
                viewport,
                hc,
                partida
        );

        // Inicialmente oculto
        botonRespuesta.ocultar();
    }

    @Override
    public void startGame(int idMano) {
        this.empieza = true;
        this.quienEmpieza = idMano;
        TipoJugador tipoMano = (idMano == 0) ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        partida.inicializar(jugadores.get(0), jugadores.get(1), mano, miRol, tipoMano);

        inicioRonda = true;

        System.out.println("[CLIENTE] Partida iniciada. Mi rol: " + miRol + ", Empieza: " + tipoMano);
        System.out.println("[CLIENTE] inicioRonda marcado como true");
        GestorSonido gestor = GestorSonido.getInstancia();

        gestor.detenerMusica(); // 👈 NECESITAS ESTE MÉTODO EN GestorSonido
        if (!gestor.existeMusica("musica_partida")) {
            gestor.cargarMusica("musica_partida", "sounds/envit.ogg");
        }

        gestor.reproducirMusica("musica_partida");
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
            if (manoRivalRenderer != null) {
                manoRivalRenderer.rivalJugoCarta();
            }
            zonaJuegoRival.agregarCarta(cartaRival);

            System.out.println("[PANTALLA] Carta del rival agregada y dimensionada.");
        }
    }
    public void onTrucoRival(){
        this.mostrarMensajeTrucoRival = true;
        this.deboResponderTruco = true;
        this.tiempoMensajeTrucoRival = DURACION_MENSAJE_TRUCO;
        botonRespuesta.mostrar();

    }
    public void onServidorDesconectado() { // ✅ NUEVO
        System.out.println("[PANTALLA] Servidor desconectado (TIMEOUT)");
        this.servidorDesconectado = true;

    }
    public void onTrucoRespondido(String respuesta, String nuevoEstadoTruco) {
        System.out.println("[PANTALLA] Rival respondió: " + respuesta);

        this.esperandoRespuestaRival = false;
        botonRespuesta.ocultar();

        if (respuesta.equals("QUIERO")) {
            System.out.println("[PANTALLA] Truco aceptado, el juego continúa.");
            this.deboResponderTruco = false;
            partida.setTrucoQuerido(true);
            partida.confirmarTrucoEnviado();
        }
        else if (respuesta.equals("SUBIDA")) {
            System.out.println("[PANTALLA] El rival subió a: " + nuevoEstadoTruco);

            partida.setEstadoTruco(EstadoTruco.valueOf(nuevoEstadoTruco));

            if (nuevoEstadoTruco.equals("VALE_CUATRO_CANTADO")) {
                System.out.println("[PANTALLA] Vale 4 alcanzado. Ocultando botón de respuesta.");
                this.deboResponderTruco = false;
                botonRespuesta.ocultar();
                partida.confirmarTrucoEnviado();
            } else {
                this.deboResponderTruco = true;
                botonRespuesta.mostrar();
            }
        }
    }
    public void onTrucoEnviadoLocal() {
        this.esperandoRespuestaRival = true; // 🔒 BLOQUEA MIS CARTAS y botones de canto
        this.deboResponderTruco = false;
        if (botonRespuesta != null) {
            botonRespuesta.ocultar();
        }
        System.out.println("[PANTALLA] Truco enviado. Esperando respuesta del rival...");
    }

    public void onCartaRecibida(int valor, Palo palo) {
        Carta carta = new Carta(valor, palo);

        //  SIEMPRE agregar a jugadores.get(0) que es "yo"
        jugadores.get(0).agregarCarta(carta);
        System.out.println("[CLIENTE " + miRol + "] Carta agregada a MI mano: " + valor + " de " + palo);

        // Verificar cuántas cartas tengo
        Carta[] miMano = jugadores.get(0).getMano();

        int cartasRecibidas = 0;
        for (Carta c : miMano) {
            if (c != null) {
                cartasRecibidas++;
                System.out.println("[CLIENTE] Carta " + cartasRecibidas + ": " + c.getNombre());
            }
        }

        System.out.println("[CLIENTE] Cartas en mi mano: " + cartasRecibidas + "/3");

        if (cartasRecibidas == 3) {
            System.out.println("[CLIENTE] ¡Tengo 3 cartas! Listo para inicializar");
            //  NO marcamos inicioRonda aquí, lo hacemos en startGame
        }
    }

    public void onTrucoActualizado(String estadoTruco, int manoTruco, String ultimoCanto) {
        if (partida != null) {
            partida.actualizarTruco(estadoTruco, manoTruco, ultimoCanto);
            System.out.println("[PANTALLA] Truco actualizado: " + estadoTruco + ", mano=" + manoTruco + ", último=" + ultimoCanto);
        }
    }

    public void onNuevaRonda() {
        System.out.println("[PANTALLA] Nueva ronda iniciada por el servidor");
        zonaJuegoJugador.limpiar();
        zonaJuegoRival.limpiar();
        jugadores.get(0).limpiarMazo();
        jugadores.get(1).limpiarMazo();
        partida.setTrucoQuerido(false);

        if (manoRivalRenderer != null) {
            manoRivalRenderer.reiniciar();
        }

        inicioRonda = true;

        System.out.println("[PANTALLA] Manos limpiadas, esperando nuevas cartas del servidor");
    }

    // Implementación del método de la interfaz
    @Override
    public void onJuegoTerminado(int idGanador) {
        System.out.println("[PANTALLA] Juego terminado. Ganador global ID: " + idGanador);

        // Determinar quién es el ganador basado en mi ID local
        Jugador objetoGanador;

        // Si el ID del ganador coincide con MI ID asignado (miID)
        if (idGanador == this.miID) {
            objetoGanador = jugadores.get(0); // Jugador 0 siempre soy "YO" en mi lista local
            GestorSonido.getInstancia().reproducirSonido("victoria");
        } else {
            objetoGanador = jugadores.get(1); // Jugador 1 siempre es "RIVAL" en mi lista local
            GestorSonido.getInstancia().reproducirSonido("derrota");
        }

        // Activar la pantalla final
        pantallaFinal.activar(
                objetoGanador,
                jugadores.get(0), // Yo
                jugadores.get(1)  // Rival
        );

        // Desactivar controles de juego
        Gdx.input.setInputProcessor(null);
    }


    public void onVolverAlMenu() {

        System.out.println("El rival se desconectó");
        this.rivalDesconectado = true;
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
        GestorSonido.getInstancia().detenerMusica();
            if (fontBotonTruco != null) {
                fontBotonTruco = null;
            }
            if (font != null) {
                font = null;
            }
            if (fondoPartida != null) fondoPartida.dispose();
            if (mazoSprite != null) mazoSprite.dispose();
            if (dorsoCartaSprite != null) dorsoCartaSprite.dispose();
            if (casilla != null) casilla.dispose();
            if (batch != null) batch.dispose();
            if (shapeRenderer != null) shapeRenderer.dispose();
            if (hud != null) hud.dispose();
            if (hc != null) {
                hc.detener();
            }
        }

    }

