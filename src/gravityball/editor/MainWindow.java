package gravityball.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.json.JSONObject;

import gravityball.editor.Scenes.TableNode;
import gravityball.editor.Scenes.TreeNode;

/** 主窗口
 * 用于管理地图编辑器的主窗口*/
@SuppressWarnings("serial")
public class MainWindow extends JFrame {
	/** 左侧的树形列表框 */
	JTree tree;
	/** 右侧的列表框 */
	JTable prop;
	/** 表示一个场景，即一张地图 */
	Scenes scenes;
	/** 表示当前正在编辑的文件 */
	File file;
	/** 表示树形列表的模型 */
	TreeNode treeNode;
	/** 表示树形框的模型 */
	TableNode tableNode;

	/** 从指定文件读取全部的内容 */
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

	/** 根据当前打开的文件修改程序标题 */
	private void setTitleByFile() {
		if (file == null)
			this.setTitle("GravityBall Editor");
		else
			this.setTitle("Gravity Ball Editor - " + file.getName());
	}

	/** 初始化主窗口 */
	public MainWindow() {
		//菜单栏和布局的声明
		MenuBar menuBar;
		Menu menuFile, menuEdit, menuInsert;
		MenuItem menuNew, menuOpen, menuSave, menuSaveAs, menuExit, menuDelete;
		JScrollPane pannelleft, pannelright;

		//全部创建新的实例
		menuBar = new MenuBar();
		menuFile = new Menu("File");
		menuEdit = new Menu("Edit");
		menuNew = new MenuItem("New");
		menuOpen = new MenuItem("Open");
		menuSave = new MenuItem("Save");
		menuSaveAs = new MenuItem("Save As");
		menuExit = new MenuItem("Exit");
		menuDelete = new MenuItem("Delete");
		menuInsert = new Menu("Insert");
		pannelleft = new JScrollPane();
		pannelright = new JScrollPane();
		scenes = new Scenes(this);

		//菜单关系的添加
		menuFile.add(menuNew);
		menuFile.add(menuOpen);
		menuFile.add(menuSave);
		menuFile.add(menuSaveAs);
		menuFile.addSeparator();
		menuFile.add(menuExit);
		menuEdit.add(menuInsert);
		menuEdit.add(menuDelete);
		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		setMenuBar(menuBar);

		//Insert列表的添加
		{
			//从new.json文件读取全部内容并转化为json
			JSONObject newobjects = new JSONObject(readall(MainWindow.class.getResourceAsStream("new.json")));
			//对于每一项加入一个新的菜单项
			for (Object n : newobjects.names()) {
				String name = (String) n;
				MenuItem insertNew = new MenuItem(name);
				insertNew.addActionListener((ActionEvent e) -> {
					//向场景添加物体并刷新
					scenes.addObject(new JSONObject(newobjects.getJSONObject(name).toString()));
					updateAll();
				});
				menuInsert.add(insertNew);
				newobjects.getJSONObject(name);
			}
		}
		//新建菜单项，调用新建并修改标题
		menuNew.addActionListener((ActionEvent e) -> {
			scenes.newDoc();
			file = null;
			setTitleByFile();
		});
		//打开一个文件
		menuOpen.addActionListener((ActionEvent e) -> {
			//打开一个对话框如果点击了确定则读取文件
			JFileChooser fileChooser = new JFileChooser();
			if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(this)) {
				try {
					this.file = fileChooser.getSelectedFile();
					setTitleByFile();
					scenes.load(new FileInputStream(file));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		//保存菜单
		menuSave.addActionListener((ActionEvent e) -> {
			//如果文件为空，则先弹出对话框询问保存位置
			if (file == null) {
				JFileChooser fileChooser = new JFileChooser();
				if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
					this.file = fileChooser.getSelectedFile();
					setTitleByFile();
				} else
					return;
			}
			//将场景保存
			try {
				scenes.save(new FileOutputStream(this.file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		});
		//另存为菜单
		menuSaveAs.addActionListener((ActionEvent e) -> {
			//弹出文件框询问保存位置
			JFileChooser fileChooser = new JFileChooser();
			if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
				this.file = fileChooser.getSelectedFile();
				setTitleByFile();
			} else
				return;
			//保存文件
			try {
				scenes.save(new FileOutputStream(this.file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		});
		//退出菜单，直接退出
		menuExit.addActionListener((ActionEvent e) -> {
			dispose();
		});
		//删除菜单，删除所选的内容
		menuDelete.addActionListener((ActionEvent e) -> {
			scenes.deleteSelection();
		});
		
		//修改窗口的基本属性
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("GravityBall Editor");
		
		//修改程序的布局
		setLayout(new BorderLayout(5, 5));
		//创建文档
		scenes.newDoc();
		//创建属性表和树
		treeNode = scenes.new TreeNode();
		tableNode = scenes.new TableNode();
		tree = new JTree(treeNode);
		prop = new JTable(tableNode);
		//检测到选择改变则修改场景中的选择
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object selected = tree.getSelectionPath().getLastPathComponent();
				if (selected instanceof ScenesObject) {
					scenes.selected = (ScenesObject) selected;
					updateAll(false);
				}
			}
		});
		//修改并建立布局
		pannelleft.setPreferredSize(new Dimension(200, 100));
		pannelright.setPreferredSize(new Dimension(200, 100));
		pannelleft.getViewport().add(tree, null);
		pannelright.getViewport().add(prop, null);
		getContentPane().add("Center", scenes);
		getContentPane().add("West", pannelleft);
		getContentPane().add("East", pannelright);
	}

	public void updateAll() {
		try {
			treeNode.fireTreeStructureChanged();
			prop.updateUI();
			prop.invalidate();
			prop.repaint();
			tree.invalidate();
			tree.repaint();
			scenes.repaint();
			
		} catch (Exception e) {
		}
	}
	
	/** 刷新场景除树形框之外的内容 */
	public void updateAll(boolean b) {
		try {
			prop.invalidate();
			prop.updateUI();
			prop.repaint();
			tree.invalidate();
			tree.repaint();
			scenes.repaint();
			
		} catch (Exception e) {
		}
	}
}
