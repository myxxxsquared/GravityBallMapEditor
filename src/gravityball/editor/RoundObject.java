package gravityball.editor;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONObject;

public class RoundObject extends ScenesObject {

	public Image image;

	public RoundObject(JSONObject object, String type, Image image) {
		super(object);
		object.put("name", type);
		this.image = image;
	}

	@Override
	public void paint(Graphics g, Scenes scenes) {
		Point pt1 = scenes
				.world2screen(new Point2D.Float((float) (object.getDouble("locationX") - object.getDouble("radius")),
						(float) (object.getDouble("locationY") - object.getDouble("radius"))));
		Point pt2 = scenes
				.world2screen(new Point2D.Float((float) (object.getDouble("locationX") + object.getDouble("radius")),
						(float) (object.getDouble("locationY") + object.getDouble("radius"))));
		g.drawImage(image, pt1.x, pt1.y, pt2.x - pt1.x, pt2.y - pt1.y, scenes);
	}

	@Override
	public ObjectControlNode[] getControlNodes() {
		ObjectControlNode[] nodes = new ObjectControlNode[2];
		nodes[0] = new ObjectControlNode() {
			@Override
			public Point2D setLocation(Point2D point) {
				object.put("locationX", Double.toString(point.getX()));
				object.put("locationY", Double.toString(point.getY()));
				return point;
			}

			@Override
			public Point2D getLocation() {
				return new Point2D.Double(object.getDouble("locationX"), object.getDouble("locationY"));
			}
			
			@Override
			public ScenesObject getObject() {
				return RoundObject.this;
			}
		};

		nodes[1] = new ObjectControlNode() {
			@Override
			public Point2D setLocation(Point2D point) {
				double newradius = point.getX() - object.getDouble("locationX");
				if(newradius < 0.01)
					newradius = 0.01;
				object.put("radius", Double.toString(newradius));
				return getLocation();
			}

			@Override
			public Point2D getLocation() {
				return new Point2D.Double(object.getDouble("locationX") + object.getDouble("radius"),
						object.getDouble("locationY"));
			}

			@Override
			public ScenesObject getObject() {
				return RoundObject.this;
			}
		};

		return nodes;
	}
}
