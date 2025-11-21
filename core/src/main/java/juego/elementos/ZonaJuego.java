package juego.elementos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;

public class ZonaJuego {

    private Rectangle limites;
    private ArrayList<Carta> cartasJugadas;
    private Color colorFondo;
    private Color colorBorde;

    private CartaRenderer cartaRenderer;

    public ZonaJuego(float x, float y, float ancho, float alto) {
        this.limites = new Rectangle(x, y, ancho, alto);
        this.cartasJugadas = new ArrayList<>();

        this.colorFondo = new Color(0.2f, 0.3f, 0.2f, 0.3f);
        this.colorBorde = new Color(0.9f, 0.9f, 0.5f, 0.6f);
    }

    // Vincular el renderer
    public void setCartaRenderer(CartaRenderer renderer) {
        this.cartaRenderer = renderer;
    }


    public boolean contieneCarta(Carta carta) {
        Rectangle limitesCarta = carta.getLimites();

        float centroX = limitesCarta.x + limitesCarta.width / 2f;
        float centroY = limitesCarta.y + limitesCarta.height / 2f;

        return limites.contains(centroX, centroY);
    }

    /** Agrega una carta a la zona de juego*/
    public void agregarCarta(Carta carta) {
        if (!cartasJugadas.contains(carta)) {
            cartasJugadas.add(carta);

            // Posiciona la carta en el centro de la zona
            float cartaX = limites.x + (limites.width - carta.getLimites().width) / 2f;
            float cartaY = limites.y + (limites.height - carta.getLimites().height) / 2f+5;

            // Apila las cartas con un pequeño offset
            float offsetY = (cartasJugadas.size() - 1) * 5f;

            carta.updateLimites(cartaX, cartaY + offsetY,
                    carta.getLimites().width,
                    carta.getLimites().height);
        }
    }

    /**
     * Dibuja el fondo de la zona
     */
    public void renderFondo(SpriteBatch batch, Texture casilla) {

        batch.begin();
        batch.draw(casilla, limites.x, limites.y, limites.width, limites.height+10);
        batch.end();
    }


    // ✅ NUEVO: Dibuja las cartas dentro de la zona
    public void renderCartas() {
        if (cartaRenderer == null) {
            System.err.println("ERROR: CartaRenderer no está vinculado a ZonaJuego");
            return;
        }

        for (Carta carta : cartasJugadas) {
            Rectangle lim = carta.getLimites();
            cartaRenderer.render(carta, lim.x, lim.y, lim.width, lim.height);
        }
    }

    public void limpiar() {
        cartasJugadas.clear();
    }

    public boolean contieneCartaJugada(Carta carta) {
        return cartasJugadas.contains(carta);
    }

    public ArrayList<Carta> getCartasJugadas() {
        return cartasJugadas;
    }

    public Rectangle getLimites() {
        return limites;
    }

    public int getCantidadCartas() {
        return cartasJugadas.size();
    }

}