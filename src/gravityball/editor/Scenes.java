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
public class Scenes extends JPanel implements MouseMotionListener, MouseListener {

	MainWindow mainWindow;
	JSONObject sceneSettings;

	Scenes(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	public ArrayList<ScenesObject> objects;
	ScenesObject ball;
	ScenesObject selected;

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

	public void newDoc() {
		load(Scenes.class.getResourceAsStream("default.json"));
		selected = ball;
	}
	
	public void addObject(JSONObject obj){
		objects.add(ScenesObject.newObject(obj));
	}
	
	public void deleteSelection(){
		if(selected == null)
			return;
		
		String name = selected.object.getString("name");
		if(name.equals("ground")|| name.equals("ball") || name.equals("scene"))
			return;
		
		for(int i = 0; i < objects.size(); ++i)
		{
			if(selected == objects.get(i)){
				objects.remove(i);
				mainWindow.updateAll();
				return;
			}
		}
	}

	public void load(InputStream stream) {
		JSONObject object = new JSONObject(readall(stream));
		JSONObject ball = object.getJSONObject("ball");
		objects = new ArrayList<>();
		ball.put("name", "ball");
		JSONArray list = object.getJSONArray("objects");

		this.ball = ScenesObject.newObject(ball);
		for (Object object2 : list) {
			JSONObject obj = (JSONObject) object2;
			addObject(obj);
		}

		object.remove("ball");
		object.remove("objects");
		sceneSettings = object;
		sceneSettings.put("name", "scene");

		if (mainWindow != null)
			mainWindow.updateAll();
	}

	public void save(OutputStream stream) {
		JSONObject object = new JSONObject(sceneSettings.toString());
		object.remove("name");
		JSONArray array = new JSONArray();

		object.put("ball", this.ball.object);
		for (ScenesObject obj : objects)
			array.put(obj.object);
		object.put("objects", array);
		try (OutputStreamWriter writer = new OutputStreamWriter(stream, Charset.forName("utf-8"))) {
			object.write(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		ball.paint(g, this);
		for (ScenesObject scenesObject : objects) {
			scenesObject.paint(g, this);
		}

		paintControlNode(g);
	}

	ArrayList<ObjectControlNode> controlNodes;
	ArrayList<Point2D> controlNodesPoints;
	ObjectControlNode selectedControlNode;
	Point2D lastPoint;
	Point2D originPoint;

	public void paintControlNode(Graphics g) {
		controlNodes = new ArrayList<>();
		controlNodesPoints = new ArrayList<>();
		{
			ScenesObject scenesObject = ball;
			for (ObjectControlNode objectControlNode : scenesObject.getControlNodes()) {
				controlNodes.add(objectControlNode);
				Point2D location = objectControlNode.getLocation();
				controlNodesPoints.add(location);
				g.setColor(Color.RED);
				Point loc = world2screen(location);
				g.drawRect(loc.x - 3, loc.y - 3, 6, 6);
			}
		}

		for (ScenesObject scenesObject : objects) {
			for (ObjectControlNode objectControlNode : scenesObject.getControlNodes()) {
				controlNodes.add(objectControlNode);
				Point2D location = objectControlNode.getLocation();
				controlNodesPoints.add(location);
				g.setColor(Color.RED);
				Point loc = world2screen(location);
				g.drawRect(loc.x - 3, loc.y - 3, 6, 6);
			}
		}
	}

	public Point world2screen(Point2D p) {
		int width = getWidth();
		int height = getHeight();
		int size = (int) (Math.min(width, height) * 0.95) / 2;

		return new Point((int) ((p.getX()) * size) + width / 2, (int) (-(p.getY()) * size) + height / 2);
	}

	public Point2D screen2wrold(Point p) {
		int width = getWidth();
		int height = getHeight();
		int size = (int) (Math.min(width, height) * 0.95) / 2;

		return new Point2D.Double(((double) (p.getX() - width / 2) / size), -((double) (p.getY() - height / 2) / size));
	}

	public class TreeNode implements TreeModel {
		
		public String root = "root";
		public String o = "objects";
		
		public void fireTreeStructureChanged() {
			for (int i = 0 ; i < listeners.size(); i ++) {
				listeners.get(i).treeStructureChanged(new TreeModelEvent(this, new TreePath(new Object[]{root})));
			}
		}

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof String) {
				String par = (String) parent;
				if (par==root) {
					if (index == 0)
						return ball;
					else if (index == 1)
						return new ScenesObject(sceneSettings) {

							@Override
							public void paint(Graphics g, Scenes scenes) {}

							@Override
							public ObjectControlNode[] getControlNodes() {return null;
							}
						};
					else if (index == 2)
						return o;
					else
						throw new IndexOutOfBoundsException();
				} else if (par == o) {
					return objects.get(index);
				} else {
					throw new InvalidParameterException();
				}
			} else
				throw new InvalidParameterException();
		}

		@Override
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
		public boolean isLeaf(Object node) {
			return (node instanceof ScenesObject);
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
		}

		@Override
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
		
		ArrayList<TreeModelListener> listeners = new ArrayList<>();

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(l);
		}

	}

	public class TableNode implements TableModel {

		@Override
		public int getRowCount() {
			if (selected == null) {
				return 0;
			} else {
				return selected.object.length();
			}
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Name";
			if (columnIndex == 1)
				return "Value";
			throw new IndexOutOfBoundsException();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return selected.object.names().getString(rowIndex);
			if (columnIndex == 1)
				return selected.object.getString((String) getValueAt(rowIndex, 0));
			throw new IndexOutOfBoundsException();
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 1) {
				selected.object.put((String) getValueAt(rowIndex, 0), (String) aValue);
				mainWindow.updateAll();
			}

		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub

		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (selectedControlNode != null) {
			Point2D mouse = screen2wrold(e.getPoint());
			selectedControlNode.setLocation(new Point2D.Double(originPoint.getX() + mouse.getX() - lastPoint.getX(),
					originPoint.getY() + mouse.getY() - lastPoint.getY()));
			repaint();
			mainWindow.updateAll();

		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Point2D mouse = screen2wrold(e.getPoint());

		for (ObjectControlNode objectControlNode : controlNodes) {
			Point2D loc = objectControlNode.getLocation();
			if (Math.abs(loc.getX() - mouse.getX()) + Math.abs(loc.getY() - mouse.getY()) < 0.03) {
				lastPoint = mouse;
				selectedControlNode = objectControlNode;
				originPoint = loc;
				selected = selectedControlNode.getObject();
				return;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		selectedControlNode = null;
		mainWindow.updateAll();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
