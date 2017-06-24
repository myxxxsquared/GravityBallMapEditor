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

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
	JTree tree;
	JTable prop;
	Scenes scenes;
	File file;
	TreeNode treeNode;
	TableNode tableNode;

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

	private void setTitleByFile() {
		if (file == null)
			this.setTitle("GravityBall Editor");
		else
			this.setTitle("Gravity Ball Editor - " + file.getName());
	}

	public MainWindow() {
		MenuBar menuBar;
		Menu menuFile, menuEdit, menuInsert;
		MenuItem menuNew, menuOpen, menuSave, menuSaveAs, menuExit, menuDelete;
		JScrollPane pannelleft, pannelright;

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

		{
			JSONObject newobjects = new JSONObject(readall(MainWindow.class.getResourceAsStream("new.json")));
			for (Object n : newobjects.names()) {
				String name = (String) n;
				MenuItem insertNew = new MenuItem(name);
				insertNew.addActionListener((ActionEvent e) -> {
					scenes.addObject(new JSONObject(newobjects.getJSONObject(name).toString()));
					updateAll();
				});
				menuInsert.add(insertNew);
				newobjects.getJSONObject(name);

			}
		}
		menuNew.addActionListener((ActionEvent e) -> {
			scenes.newDoc();
			file = null;
			setTitleByFile();
		});
		menuOpen.addActionListener((ActionEvent e) -> {
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
		menuSave.addActionListener((ActionEvent e) -> {
			if (file == null) {
				JFileChooser fileChooser = new JFileChooser();
				if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
					this.file = fileChooser.getSelectedFile();
					setTitleByFile();
				} else
					return;
			}
			try {
				scenes.save(new FileOutputStream(this.file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		});
		menuSaveAs.addActionListener((ActionEvent e) -> {
			JFileChooser fileChooser = new JFileChooser();
			if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(this)) {
				this.file = fileChooser.getSelectedFile();
				setTitleByFile();
			} else
				return;
			try {
				scenes.save(new FileOutputStream(this.file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		});
		menuExit.addActionListener((ActionEvent e) -> {
			dispose();
		});
		menuDelete.addActionListener((ActionEvent e) -> {
			scenes.deleteSelection();
		});
		
		
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("GravityBall Editor");
		setLayout(new BorderLayout(5, 5));
		scenes.newDoc();

		treeNode = scenes.new TreeNode();
		tableNode = scenes.new TableNode();
		tree = new JTree(treeNode);
		prop = new JTable(tableNode);
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
		pannelleft.setPreferredSize(new Dimension(200, 100));
		pannelright.setPreferredSize(new Dimension(200, 100));
		
		pannelleft.getViewport().add(tree, null);
		pannelright.getViewport().add(prop, null);
		getContentPane().add("Center", scenes);
		getContentPane().add("West", pannelleft);
		getContentPane().add("East", pannelright);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
