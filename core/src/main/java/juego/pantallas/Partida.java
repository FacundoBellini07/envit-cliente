package juego.pantallas;

import juego.elementos.*;
import juego.personajes.Jugador;
import juego.personajes.RivalBot;
import juego.personajes.TipoJugador;
import java.util.ArrayList;
import java.util.Collections;

public class Partida {

    // Elementos visuales y lógicos básicos
    private ArrayList<Carta> mazoRevuelto = new ArrayList<>();
    private int indiceMazo = 0;

    // Estados y control de flujo
    private EstadoTurno estadoActual;
    private int manoActual = 0;
    private final int MAX_MANOS = 3;

    // Referencias a objetos de la pantalla
    private ZonaJuego zonaJugador1;
    private ZonaJuego zonaJugador2;
    private Jugador jugador1;
    private Jugador jugador2;
    private RivalBot rivalBot; // Se mantiene por compatibilidad, aunque no decide en red

    // Quién soy yo y quién tiene el turno
    private TipoJugador jugadorLocal;
    private TipoJugador jugadorMano; // Quien empieza la ronda actual

    // Lógica de Truco y Ganador
    private boolean trucoUsado = false;
    private int manoTrucoUsada = -1;
    private TipoJugador jugadorQueCanto = null;
    private Jugador ganador = null;

    // Constructor
    public Partida() {
        // Creamos un mazo visual para las animaciones de reparto
        Mazo mazoOriginal = new Mazo();
        for (int i = 0; i < mazoOriginal.getCantCartas(); i++) {
            mazoRevuelto.add(mazoOriginal.getCarta(i));
        }
        // En el cliente barajamos solo para que visualmente no salgan siempre las mismas
        // al repartir, aunque las cartas reales las define el servidor luego.
        Collections.shuffle(mazoRevuelto);
    }

    public void inicializar(ZonaJuego zonaJug1, ZonaJuego zonaJug2, RivalBot bot,
                            Jugador jug1, Jugador jug2, int manoActual,
                            TipoJugador jugadorLocal, TipoJugador jugadorQueEmpieza) {

        this.zonaJugador1 = zonaJug1;
        this.zonaJugador2 = zonaJug2;
        this.rivalBot = bot;
        this.jugador1 = jug1;
        this.jugador2 = jug2;
        this.jugadorLocal = jugadorLocal;

        // ASIGNADO POR EL SERVIDOR (Ya no es random)
        this.jugadorMano = jugadorQueEmpieza;

        // Configuramos el estado inicial basado en quién empieza
        this.estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        this.manoActual = manoActual;
        this.trucoUsado = false;
        this.ganador = null;

        System.out.println("[CLIENTE] Partida inicializada. Rol Local: " + jugadorLocal + " - Empieza: " + jugadorQueEmpieza);
    }

    /**
     * Método CLAVE: El cliente recibe el estado del servidor y actualiza sus datos.
     */
    public void forzarEstado(int nuevaMano, int puntosJ1, int puntosJ2, EstadoTurno nuevoTurno) {

        if (nuevaMano > this.manoActual) {
            System.out.println("[CLIENTE] El servidor indica nueva mano (" + nuevaMano + "). Limpiando mesa.");
            this.manoActual = nuevaMano;
            if (zonaJugador1 != null) zonaJugador1.limpiar();
            if (zonaJugador2 != null) zonaJugador2.limpiar();

        }


        if (jugador1 != null) jugador1.setPuntos(puntosJ1);
        if (jugador2 != null) jugador2.setPuntos(puntosJ2);

        // 3. Actualizar Turno
        this.estadoActual = nuevoTurno;

        // 4. Chequear fin de partida (El servidor manda PARTIDA_TERMINADA)
        if (estadoActual == EstadoTurno.PARTIDA_TERMINADA) {
            // Determinamos ganador localmente solo para mostrar el mensaje final
            if (jugador1.getPuntos() > jugador2.getPuntos()) ganador = jugador1;
            else ganador = jugador2;
        }
    }

    // --------------------------------------------------------------------------------
    // MÉTODOS DE BUCLE (Refactorizados para ser pasivos)
    // --------------------------------------------------------------------------------

    public void update(float delta) {

        if (rivalBot != null) {

        }
    }

    // --------------------------------------------------------------------------------
    // CONSULTAS DE ESTADO (Para el HUD y PantallaPartida)
    // --------------------------------------------------------------------------------

    public boolean esMiTurnoLocal() {
        if (estadoActual == EstadoTurno.PARTIDA_TERMINADA) return false;

        if (jugadorLocal == TipoJugador.JUGADOR_1) {
            return estadoActual == EstadoTurno.ESPERANDO_JUGADOR_1;
        } else if (jugadorLocal == TipoJugador.JUGADOR_2) {
            return estadoActual == EstadoTurno.ESPERANDO_JUGADOR_2;
        }
        return false;
    }

    public int getManoActual() {
        return manoActual;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public boolean partidaTerminada() {
        return estadoActual == EstadoTurno.PARTIDA_TERMINADA;
    }

    // Métodos para el HUD de Truco
    public boolean isTrucoActivoEnManoActual() {
        return trucoUsado && manoTrucoUsada == manoActual;
    }

    public int getManoTrucoUsada() {
        return manoTrucoUsada;
    }

    // --------------------------------------------------------------------------------
    // MÉTODOS VISUALES O DE COMPATIBILIDAD
    // --------------------------------------------------------------------------------

    // Se mantiene para repartir visualmente al inicio (animación)
    public void repartirCartas(Jugador jugador1, Jugador jugador2) {
        if (indiceMazo + 6 > mazoRevuelto.size()) {
            indiceMazo = 0;
            Collections.shuffle(mazoRevuelto);
        }
        // Repartimos 3 cartas a cada uno para que tengan algo en la mano visualmente
        // (Nota: En un juego de red ideal, el servidor mandaría QUÉ cartas son exactamente)
        for (int i = 0; i < 3; i++) {
            jugador1.agregarCarta(mazoRevuelto.get(indiceMazo++));
            jugador2.agregarCarta(mazoRevuelto.get(indiceMazo++));
        }
    }

    // Estos métodos ya no deberían usarse para controlar flujo lógico en el cliente,
    // pero los dejamos vacíos o con retorno simple para evitar errores de compilación
    // si PantallaPartida los llama.
    public boolean rondaTerminada() {
        return false; // El servidor controla el fin de ronda
    }

    public void nuevaRonda() {
        // El servidor controla el reinicio
        if (zonaJugador1 != null) zonaJugador1.limpiar();
        if (zonaJugador2 != null) zonaJugador2.limpiar();
        this.manoActual = 0;
    }

    // Getters de estado puro
    public EstadoTurno getEstadoActual() { return estadoActual; }
    public boolean esTurnoJugador1() { return estadoActual == EstadoTurno.ESPERANDO_JUGADOR_1; }
    public boolean esTurnoJugador2() { return estadoActual == EstadoTurno.ESPERANDO_JUGADOR_2; }

    // El cliente puede llamar a esto para pintar el botón, pero la validez real la da el server
    public boolean cantarTruco(TipoJugador jugador) {
        // Solo visual
        trucoUsado = true;
        manoTrucoUsada = manoActual;
        return true;
    }
    public boolean isTrucoUsado() {
        return this.trucoUsado;
    }

    public boolean esPrimerTurnoEnMano() {
        // La mesa está vacía si la zona de juego del Jugador y la del Rival no tienen cartas.
        // ASUMIMOS que el servidor limpia la mesa al inicio de cada mano y la notifica.
        // Usamos las zonas de juego que se pasaron en inicializar (J1 y J2 del servidor).
        if (zonaJugador1 == null || zonaJugador2 == null) return false;
        return zonaJugador1.getCantidadCartas() + zonaJugador2.getCantidadCartas() == 0;
    }

    public boolean soyJugadorMano() {
        return this.jugadorLocal == this.jugadorMano;
    }
    public TipoJugador getJugadorLocal(){
        return this.jugadorLocal;
    }

    public void actualizarEstado(int mano, int p1, int p2, EstadoTurno nuevoTurno, TipoJugador jugadorMano) {
        this.manoActual = mano;
        this.jugador1.setPuntos(p1);
        this.jugador2.setPuntos(p2);
        this.estadoActual = nuevoTurno;
        this.jugadorMano = jugadorMano;

        System.out.println("[PARTIDA CLIENTE] Estado forzado a: " + nuevoTurno);
    }
}