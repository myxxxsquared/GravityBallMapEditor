package gravityball.editor;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.json.JSONObject;

public abstract class ScenesObject {
	public JSONObject object;

	public ScenesObject(JSONObject object) {
		this.object = object;
	}

	public abstract void paint(Graphics g, Scenes scenes);

	public abstract ObjectControlNode[] getControlNodes();

	@Override
	public String toString() {
		return object.getString("name");
	}

	static Image imgBall, imgCoin, imgFlag, imgQuestion, imgThorn;

	static {
		try {
			imgBall = ImageIO.read(ScenesObject.class.getResourceAsStream("image/ball.png"));
			imgCoin = ImageIO.read(ScenesObject.class.getResourceAsStream("image/coin.png"));
			imgFlag = ImageIO.read(ScenesObject.class.getResourceAsStream("image/flag.png"));
			imgQuestion = ImageIO.read(ScenesObject.class.getResourceAsStream("image/question.png"));
			imgThorn = ImageIO.read(ScenesObject.class.getResourceAsStream("image/thorn.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ScenesObject newObject(JSONObject object) {
		String name = object.getString("name");

		if (name.equals("ball"))
			return new RoundObject(object, "ball", imgBall);
		if (name.equals("wall"))
			return new LineObject(object);
		if (name.equals("final"))
			return new RoundObject(object, "final", imgFlag);
		if (name.equals("coin"))
			return new RoundObject(object, "coin", imgCoin);
		if (name.equals("thorn"))
			return new RoundObject(object, "thorn", imgThorn);
		if (name.equals("updateobject"))
			return new RoundObject(object, "updateobject", imgQuestion);
		if (name.equals("ground"))
			return new ScenesObject(object) {

				@Override
				public void paint(Graphics g, Scenes scenes) {
				}

				@Override
				public ObjectControlNode[] getControlNodes() {
					return new ObjectControlNode[0];
				}
			};
		throw new RuntimeException(name);
	}

}
