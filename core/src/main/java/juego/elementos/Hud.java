package juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import juego.personajes.Jugador;

public class Hud {

    private BitmapFont fontGrande;
    private BitmapFont fontMediana;
    private BitmapFont fontPequeña;

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

    public Hud(Jugador jugador1, Jugador jugador2, float worldWidth, float worldHeight) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        cargarFuentes();
    }

    private void cargarFuentes() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                    Gdx.files.internal("fuentes/medieval.ttf")
            );

            // Fuente grande para mensajes importantes
            FreeTypeFontGenerator.FreeTypeFontParameter paramGrande =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramGrande.size = 48;
            paramGrande.borderWidth = 2;
            paramGrande.borderColor = Color.BLACK;
            fontGrande = generator.generateFont(paramGrande);

            // Fuente mediana para información del juego
            FreeTypeFontGenerator.FreeTypeFontParameter paramMediana =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramMediana.size = 32;
            paramMediana.borderWidth = 1;
            paramMediana.borderColor = Color.BLACK;
            fontMediana = generator.generateFont(paramMediana);

            // Fuente pequeña para detalles
            FreeTypeFontGenerator.FreeTypeFontParameter paramPequeña =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
            paramPequeña.size = 24;
            fontPequeña = generator.generateFont(paramPequeña);

            generator.dispose();

            System.out.println("[HUD] Fuentes medieval.ttf cargadas correctamente");
        } catch (Exception e) {
            System.err.println("[HUD] Error cargando fuentes: " + e.getMessage());
            fontGrande = new BitmapFont();
            fontMediana = new BitmapFont();
            fontPequeña = new BitmapFont();
        }
    }

    public void render(SpriteBatch batch, int manoActual, boolean esTurnoJugador,
                       boolean trucoActivo, int manoTruco) {
        batch.begin();

        dibujarPuntosJugador(batch, jugador1.getPuntos());
        dibujarPuntosRival(batch, jugador2.getPuntos());
        dibujarInfoMano(batch, manoActual);
        dibujarIndicadorTurno(batch, esTurnoJugador);

        if (trucoActivo && manoActual == manoTruco) {
            dibujarIndicadorTruco(batch, manoActual);
        }

        batch.end();
    }

    private void dibujarPuntosJugador(SpriteBatch batch, int puntos) {
        fontMediana.setColor(colorJugador);

        String textoJugador = "TU: " + puntos + " pts";

        float x = margen;
        float y = margen + 30;

        fontMediana.draw(batch, textoJugador, x, y);
    }

    private void dibujarPuntosRival(SpriteBatch batch, int puntos) {
        fontMediana.setColor(colorRival);

        String textoRival = "RIVAL: " + puntos + " pts";

        float x = margen;
        float y = worldHeight - margen;

        fontMediana.draw(batch, textoRival, x, y);
    }

    private void dibujarInfoMano(SpriteBatch batch, int manoActual) {
        if (manoActual < 0 || manoActual > 2) {
            return;
        }

        fontPequeña.setColor(colorNeutral);

        String textoMano = "MANO " + (manoActual + 1) + "/3";

        GlyphLayout layout = new GlyphLayout(fontPequeña, textoMano);

        float x = worldWidth - layout.width - margen;
        float y = worldHeight - margen;

        fontPequeña.draw(batch, textoMano, x, y);
    }

    private void dibujarIndicadorTurno(SpriteBatch batch, boolean esTurnoJugador) {
        fontPequeña.setColor(esTurnoJugador ? colorJugador : colorRival);

        String texto = esTurnoJugador ? "TU TURNO" : "TURNO RIVAL";

        GlyphLayout layout = new GlyphLayout(fontPequeña, texto);

        float x = worldWidth - layout.width - margen;
        float y = margen + 30;

        fontPequeña.draw(batch, texto, x, y);
    }

    private void dibujarIndicadorTruco(SpriteBatch batch, int manoActual) {
        fontGrande.setColor(colorTruco);

        String textoTruco = "¡TRUCO! x2";

        GlyphLayout layout = new GlyphLayout(fontGrande, textoTruco);

        float x = (worldWidth - layout.width) / 2f;
        float y = worldHeight - margen - 40;

        fontGrande.draw(batch, textoTruco, x, y);
    }

    public void dibujarMensajeCentral(SpriteBatch batch, String mensaje, Color color) {
        batch.begin();

        fontGrande.setColor(color);

        GlyphLayout layout = new GlyphLayout(fontGrande, mensaje);

        float x = (worldWidth - layout.width) / 2f;
        float y = (worldHeight + layout.height) / 2f;

        fontGrande.draw(batch, mensaje, x, y);

        batch.end();
    }

    public void dispose() {
        if (fontGrande != null) fontGrande.dispose();
        if (fontMediana != null) fontMediana.dispose();
        if (fontPequeña != null) fontPequeña.dispose();
    }
}