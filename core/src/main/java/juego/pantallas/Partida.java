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

    public void inicializar(RivalBot bot,
                            Jugador jug1, Jugador jug2, int manoActual,
                            TipoJugador jugadorLocal, TipoJugador jugadorQueEmpieza) {
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
        if (trucoUsado) {
            System.out.println("[CLIENTE] El truco ya fue cantado en esta ronda");
            return false;
        }

        if (!esPrimerTurnoEnMano()) {
            System.out.println("[CLIENTE] Solo se puede cantar truco en el primer turno");
            return false;
        }

        if (!soyJugadorMano()) {
            System.out.println("[CLIENTE] Solo el jugador 'Mano' puede cantar truco");
            return false;
        }

        if (!esMiTurnoLocal()) {
            System.out.println("[CLIENTE] No es tu turno");
            return false;
        }
        trucoUsado = true;
        manoTrucoUsada = manoActual;
        jugadorQueCanto = jugador;

        return true;
    }

    public boolean isTrucoUsado() {
        return this.trucoUsado;
    }

    public boolean esPrimerTurnoEnMano() {

        return zonaJugador1.getCantidadCartas() + zonaJugador2.getCantidadCartas() == 0;
    }

    public boolean soyJugadorMano() {
        System.out.println(this.jugadorLocal == this.jugadorMano ? "SOY MANO JIJI" : "NO SOY MANO uy");
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

        System.out.println("[PARTIDA CLIENTE] Estado forzado: mano=" + mano +
                ", turno=" + nuevoTurno + ", jugadorMano=" + jugadorMano);
    }
    public void setZonaJuegos(ZonaJuego zonaJug1, ZonaJuego zonaJug2) {
        this.zonaJugador1 = zonaJug1;
        this.zonaJugador2 = zonaJug2;
    }
}