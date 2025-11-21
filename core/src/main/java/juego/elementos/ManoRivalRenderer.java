package juego.elementos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class ManoRivalRenderer {

    private final Texture dorsoTexture;
    private final ZonaJuego zonaJuego;

    private final float WORLD_WIDTH;
    private final float WORLD_HEIGHT;
    private final float CARTA_ANCHO;
    private final float CARTA_ALTO;
    private final float ESPACIADO;

    private int cartasDisponibles = 3; // Siempre empieza con 3

    // Posiciones calculadas de los dorsos
    private Rectangle[] posicionesDorsos = new Rectangle[3];

    public ManoRivalRenderer(Texture dorso, ZonaJuego zona,
                             float worldWidth, float worldHeight,
                             float cartaAncho, float cartaAlto) {
        this.dorsoTexture = dorso;
        this.zonaJuego = zona;
        this.WORLD_WIDTH = worldWidth;
        this.WORLD_HEIGHT = worldHeight;
        this.CARTA_ANCHO = cartaAncho;
        this.CARTA_ALTO = cartaAlto;
        this.ESPACIADO = CARTA_ANCHO * 0.2f;

        // Inicializar los rectángulos de posición
        for (int i = 0; i < 3; i++) {
            posicionesDorsos[i] = new Rectangle();
        }

        // Calcular posiciones iniciales
        calcularPosiciones();
    }

    private void calcularPosiciones() {
        float anchoTotalMano = (3 * CARTA_ANCHO) + (2 * ESPACIADO);
        float startX = (WORLD_WIDTH - anchoTotalMano) / 2f;

        final float MARGEN_SUPERIOR = 20f;
        float yRival = WORLD_HEIGHT - CARTA_ALTO /2;

        for (int i = 0; i < 3; i++) {
            float currentX = startX + (i * CARTA_ANCHO) + (i * ESPACIADO);
            posicionesDorsos[i].set(currentX, yRival, CARTA_ANCHO, CARTA_ALTO);
        }
    }

    public void render(SpriteBatch batch) {
        // Dibujar solo los dorsos de las cartas que aún no han sido jugadas
        for (int i = 0; i < cartasDisponibles; i++) {
            Rectangle pos = posicionesDorsos[i];
            batch.draw(dorsoTexture, pos.x, pos.y, pos.width, pos.height);
        }
    }

    public void rivalJugoCarta() {
        if (cartasDisponibles > 0) {
            cartasDisponibles--;
            System.out.println("[MANO_RIVAL] Rival jugó una carta. Quedan: " + cartasDisponibles);
        }
    }

    public void reiniciar() {
        cartasDisponibles = 3;
        System.out.println("[MANO_RIVAL] Dorsos reiniciados a 3");
    }

    public int getCartasDisponibles() {
        return cartasDisponibles;
    }
}