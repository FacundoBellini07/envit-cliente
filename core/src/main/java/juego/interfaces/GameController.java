package juego.interfaces;

import juego.elementos.EstadoTurno;
import juego.elementos.Palo;

public interface GameController {
    void onConectado(int id);
    void onInicioPartida();
    void onEstadoActualizado(int mano, int p1, int p2, EstadoTurno turno);
    void onCartaRival(int valor, Palo palo);
    void onTrucoRival();
    void startGame();
}

