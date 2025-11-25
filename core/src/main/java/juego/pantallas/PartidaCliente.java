package juego.pantallas;

import juego.elementos.*;
import juego.personajes.Jugador;
import juego.personajes.TipoJugador;
import java.util.ArrayList;
import java.util.Collections;

public class PartidaCliente {

    private ArrayList<Carta> mazoRevuelto = new ArrayList<>();
    private int indiceMazo = 0;

    private EstadoTurno estadoActual;
    private int manoActual = 0;
    private ZonaJuego zonaJugador1;
    private ZonaJuego zonaJugador2;
    private Jugador jugador1;
    private Jugador jugador2;

    private TipoJugador jugadorLocal;
    private TipoJugador jugadorMano;

    // ✅ NUEVO: Sistema de Truco mejorado
    private EstadoTruco estadoTruco = EstadoTruco.SIN_TRUCO;
    private int manoTrucoUsada = -1;
    private TipoJugador ultimoQueCanto = null;
    private Jugador ganador = null;

    public PartidaCliente() {
        Mazo mazoOriginal = new Mazo();
        for (int i = 0; i < mazoOriginal.getCantCartas(); i++) {
            mazoRevuelto.add(mazoOriginal.getCarta(i));
        }
        Collections.shuffle(mazoRevuelto);
    }

    public void inicializar(Jugador jug1, Jugador jug2, int manoActual,
                            TipoJugador jugadorLocal, TipoJugador jugadorQueEmpieza) {
        this.jugador1 = jug1;
        this.jugador2 = jug2;
        this.jugadorLocal = jugadorLocal;
        this.jugadorMano = jugadorQueEmpieza;

        this.estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        this.manoActual = manoActual;
        this.estadoTruco = EstadoTruco.SIN_TRUCO;
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

    public boolean puedoCantarTruco() {
        // 1. No se puede si ya estamos en Vale Cuatro (tope máximo)
        if (!estadoTruco.puedeSubir()) {
            return false;
        }

        // 2. Solo se puede cantar/responder en la primera mano (índice 0)
        if (manoActual != 0) {
            return false;
        }

        // 3. Lógica dividida: INICIAR vs RESPONDER
        if (estadoTruco == EstadoTruco.SIN_TRUCO) {

            if (!esMiTurnoLocal()) return false;
            if (!soyJugadorMano()) return false;
            if (!esPrimerTurnoEnMano()) return false;

        } else {

            if (ultimoQueCanto == jugadorLocal) return false;

        }

        return true;
    }

    public boolean cantarTruco() {
        if (!puedoCantarTruco()) {
            return false;
        }

        System.out.println("[CLIENTE] Validación OK para cantar " + estadoTruco.siguiente());
        return true;
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

    public boolean isTrucoActivoEnManoActual() {
        // El truco está activo si se cantó en la mano 0 (primera mano)
        // y sus puntos aplican a TODA la ronda (manos 0, 1, 2)
        return estadoTruco != EstadoTruco.SIN_TRUCO && manoTrucoUsada == 0;
    }

    public int getManoTrucoUsada() {
        return manoTrucoUsada;
    }



    public EstadoTurno getEstadoActual() {
        return estadoActual;
    }

    public boolean esPrimerTurnoEnMano() {
        return zonaJugador1.getCantidadCartas() + zonaJugador2.getCantidadCartas() == 0;
    }

    public boolean soyJugadorMano() {
        return this.jugadorLocal == this.jugadorMano;
    }

    // ✅ NUEVO: Actualizar estado del truco desde el servidor
    public void actualizarTruco(String estadoTrucoStr, int manoTruco, String ultimoCantoStr) {
        this.estadoTruco = EstadoTruco.valueOf(estadoTrucoStr);
        this.manoTrucoUsada = manoTruco;
        this.ultimoQueCanto = ultimoCantoStr != null ? TipoJugador.valueOf(ultimoCantoStr) : null;

        System.out.println("[CLIENTE] Truco actualizado: " + estadoTruco + ", mano=" + manoTruco + ", último=" + ultimoQueCanto);
    }

    public void actualizarEstado(int mano, int p1, int p2, EstadoTurno nuevoTurno, TipoJugador jugadorMano) {
        this.manoActual = mano;
        this.estadoActual = nuevoTurno;
        this.jugadorMano = jugadorMano;

        if(jugadorLocal == TipoJugador.JUGADOR_1) {
            this.jugador1.setPuntos(p1);
            this.jugador2.setPuntos(p2);
        } else {
            this.jugador1.setPuntos(p2);
            this.jugador2.setPuntos(p1);
        }

        System.out.println("[PARTIDA CLIENTE] Estado forzado: mano=" + mano +
                ", turno=" + nuevoTurno + ", jugadorMano=" + jugadorMano);
    }

    public void setZonaJuegos(ZonaJuego zonaJug1, ZonaJuego zonaJug2) {
        this.zonaJugador1 = zonaJug1;
        this.zonaJugador2 = zonaJug2;
    }

    public EstadoTruco getEstadoTruco() {
        return estadoTruco;
    }

    public String getNombreCanto() {
        switch (estadoTruco) {
            case TRUCO_CANTADO: return "TRUCO";
            case RETRUCO_CANTADO: return "RETRUCO";
            case VALE_CUATRO_CANTADO: return "VALE CUATRO";
            default: return "";
        }
    }

    public String getSiguienteCanto() {
        switch (estadoTruco) {
            case SIN_TRUCO: return "TRUCO";
            case TRUCO_CANTADO: return "RETRUCO";
            case RETRUCO_CANTADO: return "VALE 4";
            default: return "";
        }
    }

    public TipoJugador getUltimoQueCanto() {
        return ultimoQueCanto;
    }
    public void setEstadoTruco(EstadoTruco estadoTruco) {
        this.estadoTruco = estadoTruco;
        System.out.println("[CLIENTE] Estado de Truco actualizado manualmente a: " + estadoTruco);
    }
}