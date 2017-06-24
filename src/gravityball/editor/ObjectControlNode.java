package gravityball.editor;

import java.awt.geom.Point2D;

public interface ObjectControlNode {
	public Point2D getLocation();
	public Point2D setLocation(Point2D point);
	public ScenesObject getObject();
}
