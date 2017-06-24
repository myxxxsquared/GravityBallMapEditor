package gravityball.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONObject;

public class LineObject extends ScenesObject {

	public LineObject(JSONObject object) {
		super(object);
	}

	@Override
	public void paint(Graphics g, Scenes scenes) {
		Point2D pt1 = new Point2D.Double(object.getDouble("x1"), object.getDouble("y1"));
		Point2D pt2 = new Point2D.Double(object.getDouble("x2"), object.getDouble("y2"));
		Point pt_1 = scenes.world2screen(pt1);
		Point pt_2 = scenes.world2screen(pt2);
		g.setColor(Color.black);
		
		g.drawLine((int)pt_1.getX(), (int)pt_1.getY(), (int)pt_2.getX(), (int)pt_2.getY());
	}

	@Override
	public ObjectControlNode[] getControlNodes() {
		ObjectControlNode[] result = new ObjectControlNode[2];
		result[0] = new ObjectControlNode() {
			@Override
			public Point2D setLocation(Point2D point) {
				object.put("x1", Double.toString(point.getX()));
				object.put("y1", Double.toString(point.getY()));
				return point;
			}
			
			@Override
			public Point2D getLocation() {
				return new Point2D.Double(object.getDouble("x1"), object.getDouble("y1"));
			}
			@Override
			public ScenesObject getObject() {
				return LineObject.this;
			}
		};
		result[1] = new ObjectControlNode() {
			@Override
			public Point2D setLocation(Point2D point) {
				object.put("x2", Double.toString(point.getX()));
				object.put("y2", Double.toString(point.getY()));
				return point;
			}
			
			@Override
			public Point2D getLocation() {
				return new Point2D.Double(object.getDouble("x2"), object.getDouble("y2"));
			}

			@Override
			public ScenesObject getObject() {
				return LineObject.this;
			}
		};
		return result;
	}

}
