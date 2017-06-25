package gravityball.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONObject;

/** 一个线状的对象 */
public class LineObject extends ScenesObject {
	/** 创建一个线状对象 */
	public LineObject(JSONObject object) {
		super(object);
	}

	@Override
	/** 绘制对象 */
	public void paint(Graphics g, Scenes scenes) {
		// 计算出各个点的位置
		Point2D pt1 = new Point2D.Double(object.getDouble("x1"), object.getDouble("y1"));
		Point2D pt2 = new Point2D.Double(object.getDouble("x2"), object.getDouble("y2"));
		Point pt_1 = scenes.world2screen(pt1);
		Point pt_2 = scenes.world2screen(pt2);

		// 绘制线的位置
		g.setColor(Color.black);
		g.drawLine((int) pt_1.getX(), (int) pt_1.getY(), (int) pt_2.getX(), (int) pt_2.getY());
	}

	@Override
	/** 获取控制节点列表 */
	public ObjectControlNode[] getControlNodes() {
		// 控制节点列表
		ObjectControlNode[] result = new ObjectControlNode[2];

		// 第一个节点
		result[0] = new ObjectControlNode() {
			@Override
			/** 设置节点位置 */
			public Point2D setLocation(Point2D point) {
				object.put("x1", Double.toString(point.getX()));
				object.put("y1", Double.toString(point.getY()));
				return point;
			}

			@Override
			/** 获取节点位置 */
			public Point2D getLocation() {
				return new Point2D.Double(object.getDouble("x1"), object.getDouble("y1"));
			}

			@Override
			/** 获取对象 */
			public ScenesObject getObject() {
				return LineObject.this;
			}
		};

		// 第二个节点
		result[1] = new ObjectControlNode() {
			@Override
			/** 设置节点位置 */
			public Point2D setLocation(Point2D point) {
				object.put("x2", Double.toString(point.getX()));
				object.put("y2", Double.toString(point.getY()));
				return point;
			}

			@Override
			/** 获取节点位置 */
			public Point2D getLocation() {
				return new Point2D.Double(object.getDouble("x2"), object.getDouble("y2"));
			}

			@Override
			/** 获取对象 */
			public ScenesObject getObject() {
				return LineObject.this;
			}
		};
		return result;
	}

}
