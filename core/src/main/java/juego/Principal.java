package juego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import juego.pantallas.PantallaMenu;
import juego.utilidades.GestorFuentes;

public class Principal extends Game {

	public SpriteBatch batch;

	@Override
	public void create() {
		batch = new SpriteBatch();
		setScreen(new PantallaMenu(this));
	}

	@Override
	public void render() {
		super.render(); // Esto llama al render() de la pantalla actual
	}

	@Override
	public void dispose() {
		batch.dispose();
		super.dispose();
        GestorFuentes.getInstancia().dispose();
        System.out.println("Cerrando juego completamente...");
        System.exit(0);
	}
}
