package gravityball.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("serial")
/** 表示一个场景即一张地图，监听了鼠标事件 */
public class Scenes extends JPanel implements MouseMotionListener, MouseListener {

	/** 表示当前的主窗口 */
	MainWindow mainWindow;
	/** 表示当前场景的属性 */
	JSONObject sceneSettings;
	/** 表示当前的场景中的所有物体 */
	public ArrayList<ScenesObject> objects;
	/** 表示当前场景中的球 */
	ScenesObject ball;
	/** 表示当前已选中的物体 */
	ScenesObject selected;

	/** 初始化场景 */
	Scenes(MainWindow mainWindow) {
		// 设置主窗口并添加监听器
		this.mainWindow = mainWindow;
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	/** 从文件中读取全部内容 */
	static String readall(InputStream input) {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		try {
			for (int n; (n = input.read(b)) != -1;) {
				out.append(new String(b, 0, n, "UTF-8"));
			}
		} catch (IOException e) {
		}
		return out.toString();
	}

	/** 新建文档 */
	public void newDoc() {
		// 从预先确定的默认文件中加载
		load(Scenes.class.getResourceAsStream("default.json"));
		selected = ball;
	}

	/** 向场景添加一个物体 */
	public void addObject(JSONObject obj) {
		// 添加新物体到列表之中
		objects.add(ScenesObject.newObject(obj));
	}

	/** 删除当前的选择的内容 */
	public void deleteSelection() {
		// 判断所选内容是否为空
		if (selected == null)
			return;

		// 判断选中的内容是地面、球、场景则禁止删除
		String name = selected.object.getString("name");
		if (name.equals("ground") || name.equals("ball") || name.equals("scene"))
			return;

		// 枚举找到需要删除的内容
		for (int i = 0; i < objects.size(); ++i) {
			if (selected == objects.get(i)) {
				objects.remove(i);
				mainWindow.updateAll();
				return;
			}
		}
	}

	/** 从文件读取场景 */
	public void load(InputStream stream) {
		// 读取全部内容到一个JSON文件
		JSONObject object = new JSONObject(readall(stream));

		// 读取球物体
		JSONObject ball = object.getJSONObject("ball");
		ball.put("name", "ball");
		JSONArray list = object.getJSONArray("objects");

		// 读取对象列表
		objects = new ArrayList<>();
		this.ball = ScenesObject.newObject(ball);
		for (Object object2 : list) {
			JSONObject obj = (JSONObject) object2;
			addObject(obj);
		}

		// 存入其他属性对象
		object.remove("ball");
		object.remove("objects");
		sceneSettings = object;
		sceneSettings.put("name", "scene");

		// 刷新主窗口
		if (mainWindow != null)
			mainWindow.updateAll();
	}

	/** 保存到文件 */
	public void save(OutputStream stream) {
		// 准备场景对象
		JSONObject object = new JSONObject(sceneSettings.toString());
		object.remove("name");

		// 存入球对象
		object.put("ball", this.ball.object);

		// 存入列表对象
		JSONArray array = new JSONArray();
		for (ScenesObject obj : objects)
			array.put(obj.object);
		object.put("objects", array);

		// 写入文件
		try (OutputStreamWriter writer = new OutputStreamWriter(stream, Charset.forName("utf-8"))) {
			object.write(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	/** 绘制场景 */
	public void paint(Graphics g) {
		super.paint(g);

		// 绘制背景色
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		// 绘制球
		ball.paint(g, this);

		// 绘制每个物体
		for (ScenesObject scenesObject : objects) {
			scenesObject.paint(g, this);
		}

		// 绘制控制节点
		paintControlNode(g);
	}

	/** 控制节点列表 */
	ArrayList<ObjectControlNode> controlNodes;
	/** 控制节点的位置列表 */
	ArrayList<Point2D> controlNodesPoints;
	/** 已选择的控制节点 */
	ObjectControlNode selectedControlNode;
	/** 上一次选择的点 */
	Point2D lastPoint;
	/** 初始选择列表 */
	Point2D originPoint;

	/** 绘制控制节点 */
	public void paintControlNode(Graphics g) {
		// 创建新的控制节点列表
		controlNodes = new ArrayList<>();
		controlNodesPoints = new ArrayList<>();

		// 创建球的控制节点
		{
			ScenesObject scenesObject = ball;
			for (ObjectControlNode objectControlNode : scenesObject.getControlNodes()) {
				// 添加控制节点
				controlNodes.add(objectControlNode);
				Point2D location = objectControlNode.getLocation();
				controlNodesPoints.add(location);

				// 绘制控制节点
				g.setColor(Color.RED);
				Point loc = world2screen(location);
				g.drawRect(loc.x - 3, loc.y - 3, 6, 6);
			}
		}

		// 创建每一个物体的控制节点
		for (ScenesObject scenesObject : objects) {
			for (ObjectControlNode objectControlNode : scenesObject.getControlNodes()) {
				// 添加控制节点
				controlNodes.add(objectControlNode);
				Point2D location = objectControlNode.getLocation();
				controlNodesPoints.add(location);

				// 添加控制节点
				g.setColor(Color.RED);
				Point loc = world2screen(location);
				g.drawRect(loc.x - 3, loc.y - 3, 6, 6);
			}
		}
	}

	/** 将世界坐标转换为屏幕坐标 */
	public Point world2screen(Point2D p) {
		// 使用长和宽计算场景的尺寸
		int width = getWidth();
		int height = getHeight();
		int size = (int) (Math.min(width, height) * 0.95) / 2;

		// 根据尺寸换算屏幕坐标
		return new Point((int) ((p.getX()) * size) + width / 2, (int) (-(p.getY()) * size) + height / 2);
	}

	/** 将屏幕坐标转换为屏幕坐标 */
	public Point2D screen2wrold(Point p) {
		// 使用长和宽计算场景的尺寸
		int width = getWidth();
		int height = getHeight();
		int size = (int) (Math.min(width, height) * 0.95) / 2;

		// 根据尺寸换算屏幕坐标
		return new Point2D.Double(((double) (p.getX() - width / 2) / size), -((double) (p.getY() - height / 2) / size));
	}

	/** 树状列表的模型 */
	public class TreeNode implements TreeModel {

		/** 定义根节点和对象节点 */
		String root = "root";
		String o = "objects";
		ScenesObject scenesobject = new ScenesObject(sceneSettings) {
			@Override
			public void paint(Graphics g, Scenes scenes) {
			}

			@Override
			public ObjectControlNode[] getControlNodes() {
				return null;
			}
		};

		/** 监听器列表 */
		ArrayList<TreeModelListener> listeners = new ArrayList<>();

		/** 触发树状列表更新 */
		public void fireTreeStructureChanged() {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).treeStructureChanged(new TreeModelEvent(this, new TreePath(new Object[] { root })));
			}
		}

		@Override
		/** 获取根节点 */
		public Object getRoot() {
			return root;
		}

		@Override
		/** 获取子节点 */
		public Object getChild(Object parent, int index) {
			if (parent instanceof String) {
				String par = (String) parent;
				if (par == root) {
					// 如果是获取根节点的子节点
					if (index == 0)
						return ball;
					else if (index == 1)
						return scenesobject;
					else if (index == 2)
						return o;
					else
						throw new IndexOutOfBoundsException();
				} else if (par == o) {
					// 如果是对象节点的子节点
					return objects.get(index);
				} else {
					throw new InvalidParameterException();
				}
			} else
				throw new InvalidParameterException();
		}

		@Override
		/** 获取子节点数目 */
		public int getChildCount(Object parent) {
			if (parent instanceof String) {
				String par = (String) parent;
				if (par == root) {
					return 3;
				} else if (par == o) {
					return objects.size();
				} else {
					throw new InvalidParameterException();
				}
			} else
				return 0;
		}

		@Override
		/** 获取是否是跟节点 */
		public boolean isLeaf(Object node) {
			return (node instanceof ScenesObject);
		}

		@Override
		/** 选择列表改变 */
		public void valueForPathChanged(TreePath path, Object newValue) {
		}

		@Override
		/** 获取对象是第几个子节点 */
		public int getIndexOfChild(Object parent, Object child) {
			if (parent instanceof String) {
				String par = (String) parent;
				if (par.equals("root")) {
					if (child == ball)
						return 0;
					else if (child instanceof String && ((String) child).equals("objects"))
						return 1;
					else
						throw new InvalidParameterException();
				} else if (par.equals("objects")) {
					for (int i = 0; i < objects.size(); ++i)
						if (objects.get(i) == child)
							return i;
					throw new InvalidParameterException();
				} else {
					throw new InvalidParameterException();
				}
			} else
				throw new InvalidParameterException();
		}

		@Override
		/** 添加监听器 */
		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}

		@Override
		/** 删除监听器 */
		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(l);
		}

	}

	/** 属性列表模型 */
	public class TableNode implements TableModel {

		@Override
		/** 获取行数 */
		public int getRowCount() {
			if (selected == null) {
				return 0;
			} else {
				return selected.object.length();
			}
		}

		@Override
		/** 获取列数 */
		public int getColumnCount() {
			return 2;
		}

		@Override
		/** 获取列名称 */
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Name";
			if (columnIndex == 1)
				return "Value";
			throw new IndexOutOfBoundsException();
		}

		@Override
		/** 每一列的类型 */
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		/** 获取是否可编辑 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// 仅第二列可以编辑
			return columnIndex == 1;
		}

		@Override
		/** 获取值 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)// 第一列为名称
				return selected.object.names().getString(rowIndex);
			if (columnIndex == 1)// 第二列为内容
				return selected.object.getString((String) getValueAt(rowIndex, 0));
			throw new IndexOutOfBoundsException();
		}

		@Override
		// 设置指定列的值
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				// 将值设置并刷新全部
				selected.object.put((String) getValueAt(rowIndex, 0), (String) aValue);
				mainWindow.updateAll();
			}
		}

		@Override
		/** 添加监听器，什么都不做 */
		public void addTableModelListener(TableModelListener l) {
		}

		@Override
		/** 删除监听器，什么多不做 */
		public void removeTableModelListener(TableModelListener l) {
		}

	}

	@Override
	/** 鼠标拖拽 */
	public void mouseDragged(MouseEvent e) {
		// 如果已选节点不为空，才进行节点更新
		if (selectedControlNode != null) {
			// 获取鼠标位置
			Point2D mouse = screen2wrold(e.getPoint());

			// 计算新的坐标位置
			selectedControlNode.setLocation(new Point2D.Double(originPoint.getX() + mouse.getX() - lastPoint.getX(),
					originPoint.getY() + mouse.getY() - lastPoint.getY()));

			// 重绘场景
			mainWindow.updateAll();
		}
	}

	@Override
	/** 鼠标移动，什么都不做 */
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	/** 鼠标单击，什么都不做 */
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	/** 鼠标按下的事件 */
	public void mousePressed(MouseEvent e) {
		// 获取鼠标位置
		Point2D mouse = screen2wrold(e.getPoint());

		// 针对每一个节点，判断距离是否足够近
		for (ObjectControlNode objectControlNode : controlNodes) {
			Point2D loc = objectControlNode.getLocation();
			if (Math.abs(loc.getX() - mouse.getX()) + Math.abs(loc.getY() - mouse.getY()) < 0.03) {
				// 设置前一个节点和已选节点
				lastPoint = mouse;
				selectedControlNode = objectControlNode;
				originPoint = loc;
				selected = selectedControlNode.getObject();
				return;
			}
		}
	}

	@Override
	/** 鼠标松开事件 */
	public void mouseReleased(MouseEvent e) {
		// 选中的控制节点清空
		selectedControlNode = null;
		mainWindow.updateAll();
	}

	@Override
	/** 鼠标进入事件，什么都不做 */
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	/** 鼠标离开事件，什么都不做 */
	public void mouseExited(MouseEvent e) {
	}

}
