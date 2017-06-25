package gravityball.editor;

import java.awt.geom.Point2D;

/** 控制节点接口 */
public interface ObjectControlNode {
	/** 获取控制节点位置 */
	public Point2D getLocation();

	/** 设置控制节点位置 */
	public Point2D setLocation(Point2D point);

	/** 获取控制节点关联的对象 */
	public ScenesObject getObject();
}
