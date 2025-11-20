package juego.elementos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import juego.personajes.Jugador;
import juego.personajes.TipoJugador;

public class Hud {

    private TipoJugador miRol;
    private BitmapFont font;
    private Jugador jugador1;
    private Jugador jugador2;

    private float worldWidth;
    private float worldHeight;

    // Colores
    private Color colorJugador = new Color(0.2f, 0.8f, 0.2f, 1f); // Verde
    private Color colorRival = new Color(0.9f, 0.3f, 0.3f, 1f);   // Rojo
    private Color colorNeutral = new Color(0.9f, 0.9f, 0.7f, 1f); // Amarillo claro
    private Color colorTruco = new Color(1f, 0.8f, 0f, 1f);       // Dorado

    // Posiciones
    private float margen = 20f;

    public Hud(BitmapFont font, Jugador jugador1, Jugador jugador2, float worldWidth, float worldHeight, TipoJugador miRol) {
        this.font = font;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.miRol = miRol;
    }

    /**
     * Dibuja el HUD completo
     */


    public void render(SpriteBatch batch, int manoActual, boolean esTurnoJugador,
                       boolean trucoActivo, int manoTruco) {
        batch.begin();

        if(miRol == TipoJugador.JUGADOR_1){
            dibujarPuntosJugador(batch, jugador1.getPuntos());
            dibujarPuntosRival(batch, jugador2.getPuntos());
        } else {
            dibujarPuntosRival(batch, jugador2.getPuntos());
            dibujarPuntosJugador(batch, jugador1.getPuntos());
        }

        // Información de mano actual (arriba centro)
        dibujarInfoMano(batch, manoActual);

        // Indicador de turno (centro derecha)
        dibujarIndicadorTurno(batch, esTurnoJugador);

        if (trucoActivo && manoActual == manoTruco) {
            dibujarIndicadorTruco(batch, manoActual);
        }

        batch.end();
    }

    private void dibujarPuntosJugador(SpriteBatch batch, int puntos) {
        font.setColor(colorJugador);
        font.getData().setScale(1.5f);

        String textoJugador = "TU: " + puntos + " pts";

        float x = margen;
        float y = margen + 30;

        font.draw(batch, textoJugador, x, y);
    }

    private void dibujarPuntosRival(SpriteBatch batch, int puntos) {
        font.setColor(colorRival);
        font.getData().setScale(1.5f);

        String textoRival = "RIVAL: " + puntos + " pts";

        float x = margen;
        float y = worldHeight - margen;

        font.draw(batch, textoRival, x, y);
    }

    private void dibujarInfoMano(SpriteBatch batch, int manoActual) {
        if (manoActual < 0 || manoActual > 2) {
            return; // No mostrar si no hay mano en curso
        }

        font.setColor(colorNeutral);
        font.getData().setScale(1.2f);

        String textoMano = "MANO " + (manoActual + 1) + "/3";

        // Arriba centro
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, textoMano);

        float x = worldWidth - layout.width - margen;
        float y = worldHeight - margen;

        font.draw(batch, textoMano, x, y);
    }

    /**
     * Dibuja un indicador visual de quién tiene el turno
     */
    private void dibujarIndicadorTurno(SpriteBatch batch, boolean esTurnoJugador) {
        font.getData().setScale(1.0f);

        String texto;
        Color color;

        if (esTurnoJugador) {
            texto = "TU TURNO";
            color = colorJugador;
        } else {
            texto = "TURNO RIVAL";
            color = colorRival;
        }

        font.setColor(color);

        // Derecha centro
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, texto);

        float x = worldWidth - layout.width - margen;
        float y = margen + 30;

        font.draw(batch, texto, x, y);
    }

    /**
     * ✅ NUEVO: Dibuja el indicador de que el truco está activo
     */
    private void dibujarIndicadorTruco(SpriteBatch batch, int manoActual) {
        font.setColor(colorTruco);
        font.getData().setScale(1.8f);

        String textoTruco = "¡TRUCO! x2";

        // Centro superior
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, textoTruco);

        float x = (worldWidth - layout.width) / 2f;
        float y = worldHeight - margen - 40;

        font.draw(batch, textoTruco, x, y);
    }

    /**
     * Dibuja un mensaje temporal grande en el centro (para anuncios)
     */
    public void dibujarMensajeCentral(SpriteBatch batch, String mensaje, Color color) {
        batch.begin();

        font.setColor(color);
        font.getData().setScale(2.5f);

        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, mensaje);

        float x = (worldWidth - layout.width) / 2f;
        float y = (worldHeight + layout.height) / 2f;

        font.draw(batch, mensaje, x, y);

        batch.end();
    }

}