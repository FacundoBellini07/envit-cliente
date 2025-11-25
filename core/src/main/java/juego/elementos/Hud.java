package juego.elementos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import juego.personajes.Jugador;
import juego.personajes.TipoJugador;
import juego.utilidades.GestorFuentes;

public class Hud {

    private TipoJugador miRol;
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

    public Hud(Jugador jugador1, Jugador jugador2, float worldWidth, float worldHeight, TipoJugador miRol) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.miRol = miRol;
        cargarFuentes();
    }

    private void cargarFuentes() {
        try {
            // ✅ Obtener fuentes del gestor centralizado
            GestorFuentes gestor = GestorFuentes.getInstancia();

            this.fontGrande = gestor.getGrande();
            this.fontMediana = gestor.getMediana();
            this.fontPequeña = gestor.getPequeña();

            System.out.println("[HUD] ✅ Fuentes medieval.ttf cargadas correctamente desde AplicarFuentes");
        } catch (Exception e) {
            System.err.println("[HUD] ❌ Error cargando fuentes: " + e.getMessage());

            // Fallback: crear fuentes por defecto
            fontGrande = new BitmapFont();
            fontMediana = new BitmapFont();
            fontPequeña = new BitmapFont();
        }
    }

    public void render(SpriteBatch batch, int manoActual, boolean esTurnoJugador,
                       EstadoTruco estadoTruco) {
        batch.begin();

        dibujarPuntosJugador(batch, jugador1.getPuntos());
        dibujarPuntosRival(batch, jugador2.getPuntos());

        dibujarInfoMano(batch, manoActual);
        dibujarIndicadorTurno(batch, esTurnoJugador);

        // ✅ CORRECCIÓN: Mostrar truco si está activo (sin importar la mano actual)
        if (estadoTruco != EstadoTruco.SIN_TRUCO && manoActual == 0) {
            dibujarIndicadorTruco(batch, estadoTruco);
        }

        batch.end();
    }

    private void dibujarIndicadorTruco(SpriteBatch batch, EstadoTruco estadoTruco) {
        fontGrande.setColor(colorTruco);

        String textoTruco;
        switch (estadoTruco) {
            case TRUCO_CANTADO:
                textoTruco = "¡TRUCO! x2";
                break;
            case RETRUCO_CANTADO:
                textoTruco = "¡RETRUCO! x3";
                break;
            case VALE_CUATRO_CANTADO:
                textoTruco = "¡VALE 4! x4";
                break;
            default:
                return;
        }

        GlyphLayout layout = new GlyphLayout(fontGrande, textoTruco);

        float x = (worldWidth - layout.width) / 2f;
        float y = worldHeight - margen - 40;

        fontGrande.draw(batch, textoTruco, x, y);
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
        System.out.println("[HUD] Dispose llamado (fuentes no se liberan aquí, se liberan de forma centralizada)");
    }
}