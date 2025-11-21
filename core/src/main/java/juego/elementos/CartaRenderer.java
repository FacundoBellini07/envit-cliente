package juego.elementos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CartaRenderer {

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final Viewport viewport;
    private final Color backgroundColor = new Color(0.95f, 0.95f, 0.95f, 1);

    public CartaRenderer(SpriteBatch batch, ShapeRenderer shapeRenderer, Viewport viewport) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.viewport = viewport;
    }

    public void render(Carta carta, float x, float y, float width, float height) {

        carta.updateLimites(x, y, width, height);
        Rectangle limites = carta.getLimites();

        float r = 6f;

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);

        shapeRenderer.rect(limites.x + r, limites.y, limites.width - 2 * r, limites.height);
        shapeRenderer.rect(limites.x, limites.y + r, limites.width, limites.height - 2 * r);

        shapeRenderer.arc(limites.x + r, limites.y + r, r, 180, 90);
        shapeRenderer.arc(limites.x + limites.width - r, limites.y + r, r, 270, 90);
        shapeRenderer.arc(limites.x + r, limites.y + limites.height - r, r, 90, 90);
        shapeRenderer.arc(limites.x + limites.width - r, limites.y + limites.height - r, r, 0, 90);

        shapeRenderer.end();

        batch.begin();
        carta.draw(batch, x, y, width, height);
        batch.end();
    }
}
