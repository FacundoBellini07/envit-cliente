package juego.interfaces;

import juego.elementos.EstadoTurno;
import juego.elementos.Palo;
import juego.personajes.TipoJugador;

public interface GameController {
    void onConectado(int id);
    void onEstadoActualizado(int mano, int p1, int p2, EstadoTurno turno, TipoJugador jugadorMano);
    void onCartaRival(int valor, Palo palo);
    void onTrucoRival();
    void startGame(int idMano);
    void onJuegoTerminado(int idGanador);
    void onCartaRecibida(int valor, Palo palo);
    void onNuevaRonda();
    void onVolverAlMenu();
    void onTrucoActualizado(String estadoTruco, int manoTruco, String ultimoCanto);
    void onTrucoRespondido(String respuesta, String nuevoEstadoTruco); // ✅ NUEVO
    void onTrucoEnviadoLocal();
    void onServidorDesconectado();
}