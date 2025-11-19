// Nuevo archivo: core/src/main/java/juego/red/EventoRedListener.java
package juego.interfaces;

import juego.elementos.Palo;
import juego.utilidades.Global;

public interface EventoRedListener {
    void onCartaRivalRecibida(int valor, Palo palo);
    void onTrucoRivalRecibido();
}