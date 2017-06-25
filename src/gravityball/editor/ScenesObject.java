package gravityball.editor;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.json.JSONObject;

/** 抽象类，表示场景中的一个物体 */
public abstract class ScenesObject {
	/** 表示对象中的所有属性 */
	public JSONObject object;

	/** 初始化物体 */
	public ScenesObject(JSONObject object) {
		this.object = object;
	}

	/** 绘制物体 */
	public abstract void paint(Graphics g, Scenes scenes);

	/** 获取控制节点列表 */
	public abstract ObjectControlNode[] getControlNodes();

	@Override
	/** 转换为字符串 */
	public String toString() {
		return object.getString("name");
	}

	/** 物体的节点列表 */
	static Image imgBall, imgCoin, imgFlag, imgQuestion, imgThorn;

	static {
		try {
			// 初始化所有的图像
			imgBall = ImageIO.read(ScenesObject.class.getResourceAsStream("image/ball.png"));
			imgCoin = ImageIO.read(ScenesObject.class.getResourceAsStream("image/coin.png"));
			imgFlag = ImageIO.read(ScenesObject.class.getResourceAsStream("image/flag.png"));
			imgQuestion = ImageIO.read(ScenesObject.class.getResourceAsStream("image/question.png"));
			imgThorn = ImageIO.read(ScenesObject.class.getResourceAsStream("image/thorn.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 根据名称新建物体 */
	public static ScenesObject newObject(JSONObject object) {
		String name = object.getString("name");

		// 对于球、墙、终点、硬币、刺新建一个圆形对象
		if (name.equals("ball"))
			return new RoundObject(object, "ball", imgBall);
		if (name.equals("final"))
			return new RoundObject(object, "final", imgFlag);
		if (name.equals("coin"))
			return new RoundObject(object, "coin", imgCoin);
		if (name.equals("thorn"))
			return new RoundObject(object, "thorn", imgThorn);
		if (name.equals("updateobject"))
			return new RoundObject(object, "updateobject", imgQuestion);

		// 对于墙使用线对象
		if (name.equals("wall"))
			return new LineObject(object);

		// 对于地面，建立一个空的对象
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
