package gravityball.editor;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONObject;

/** 表示一个圆形物体 */
public class RoundObject extends ScenesObject {

	/** 表示当前的图片的对象 */
	public Image image;

	/** 新建一个圆形对象 */
	public RoundObject(JSONObject object, String type, Image image) {
		super(object);
		object.put("name", type);
		this.image = image;
	}

	@Override
	/** 绘制物体 */
	public void paint(Graphics g, Scenes scenes) {
		// 计算点的位置
		Point pt1 = scenes
				.world2screen(new Point2D.Float((float) (object.getDouble("locationX") - object.getDouble("radius")),
						(float) (object.getDouble("locationY") - object.getDouble("radius"))));
		Point pt2 = scenes
				.world2screen(new Point2D.Float((float) (object.getDouble("locationX") + object.getDouble("radius")),
						(float) (object.getDouble("locationY") + object.getDouble("radius"))));

		// 绘制图像
		g.drawImage(image, pt1.x, pt1.y, pt2.x - pt1.x, pt2.y - pt1.y, scenes);
	}

	@Override
	/** 获取控制节点 */
	public ObjectControlNode[] getControlNodes() {
		// 控制节点列表
		ObjectControlNode[] nodes = new ObjectControlNode[2];

		// 圆心控制节点
		nodes[0] = new ObjectControlNode() {
			@Override
			/** 设置节点位置 */
			public Point2D setLocation(Point2D point) {
				object.put("locationX", Double.toString(point.getX()));
				object.put("locationY", Double.toString(point.getY()));
				return point;
			}

			@Override
			/** 获取节点位置 */
			public Point2D getLocation() {
				return new Point2D.Double(object.getDouble("locationX"), object.getDouble("locationY"));
			}

			@Override
			/** 获取对象 */
			public ScenesObject getObject() {
				return RoundObject.this;
			}
		};

		// 半径控制节点
		nodes[1] = new ObjectControlNode() {
			@Override
			/** 设置节点位置 */
			public Point2D setLocation(Point2D point) {
				double newradius = point.getX() - object.getDouble("locationX");
				if (newradius < 0.01)
					newradius = 0.01;
				object.put("radius", Double.toString(newradius));
				return getLocation();
			}

			@Override
			/** 获取节点位置 */
			public Point2D getLocation() {
				return new Point2D.Double(object.getDouble("locationX") + object.getDouble("radius"),
						object.getDouble("locationY"));
			}

			@Override
			/** 获取对象 */
			public ScenesObject getObject() {
				return RoundObject.this;
			}
		};

		return nodes;
	}
}
