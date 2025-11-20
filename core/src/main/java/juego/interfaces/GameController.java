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

    // Métodos existentes
    void onCartaRecibida(int valor, Palo palo);
    void onNuevaRonda();

    // ✅ NUEVO: Recibir actualización del estado del truco desde el servidor
    void onTrucoActualizado(boolean trucoUsado, int manoTruco);
}