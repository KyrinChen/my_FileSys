import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class myFileSys extends JFrame {
	private JTree tree;
	private JScrollPane treePane;
	private JScrollPane tablePane;
	private tableModel display_model = new tableModel();
	private JTable fileTable;

	private File rootFile;
	private File readMe;

	// 系统自带的文件夹
	private int folder_num = 4;
	private ArrayList<Folder> folders = new ArrayList<Folder>();

	// 信息条
	private JLabel blockName = new JLabel("所在根目录:");
	private JLabel nameField = new JLabel();
	private JLabel haveUsed = new JLabel("已使用空间:");
	private JLabel usedField = new JLabel();
	private JLabel freeYet = new JLabel("剩余空间:");
	private JLabel freeField = new JLabel();
	private JLabel fileNum = new JLabel("文件数:");
	private JLabel fileNumField = new JLabel();
	private JLabel pathField = new JLabel("my_fileSys");

	public static void main(String args[]) throws IOException {
		new myFileSys();
	}

	//----------------一下为鼠标右键的菜单功能-----------------
	private void menu_ope(){  //操作菜单,包括功能:新建文件，新建文件夹，删除，格式化，重命名
		// 菜单初始化
		final JPopupMenu myMenu = new JPopupMenu();  //JPopupMenu:弹出菜单是一个可弹出并显示一系列选项的小窗口
		myMenu.setPreferredSize(new Dimension(300, 150));
		// 新建文件夹
		new_folder(myMenu);
		// 新建文件
		new_file( myMenu);
		// 删除文件/文件夹
		delete_item(myMenu);
		// 格式化
		format_item(myMenu);
		// 给文件树加监听，鼠标右键显示菜单
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getButton() == MouseEvent.BUTTON3) {  //MouseEvent.BUTTON3鼠标右键
					myMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}
	private void format_item(JPopupMenu myMenu) {
		JMenuItem formatItem = new JMenuItem("格式化");
		formatItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				myFile temp = (myFile) node.getUserObject();
				int blokName = temp.getBlockName();
				Folder currentBlock = folders.get(blokName - 1);
				int choose = JOptionPane.showConfirmDialog(null, "确定格式化文件夹吗？", "confirm", JOptionPane.YES_NO_OPTION);
				if (choose == 0) {
					try {
						if (temp.getMyFile().isDirectory()) {
							for (File myfile : temp.getMyFile().listFiles()) {
								currentBlock.deleteFile(myfile, getSpace(myfile));
							}
							upDateBlock(currentBlock);
							JOptionPane.showMessageDialog(null, "格式化成功，请刷新文件夹！", "Success", JOptionPane.DEFAULT_OPTION);
							currentBlock.rewriteBitMap();
						}
					} catch (Exception E1) {
						JOptionPane.showMessageDialog(null, "格式化失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		myMenu.add(formatItem);
	}
	private void delete_item(JPopupMenu myMenu) {
		JMenuItem deleteItem = new JMenuItem("删除");
		deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				myFile temp = (myFile) node.getUserObject();
				int blokName = temp.getBlockName();
				Folder currentBlock = folders.get(blokName - 1);
				int choose = JOptionPane.showConfirmDialog(null, "确定删除？", "confirm", JOptionPane.YES_NO_OPTION);
				if (choose == 0) {
					if (currentBlock.deleteFile(temp.getMyFile(), temp.getSpace())) {
						try {
							currentBlock.rewriteBitMap();
							currentBlock.rewriteRecoverWriter();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						upDateBlock(currentBlock);
						JOptionPane.showMessageDialog(null, "删除成功，请刷新文件夹！", "Success", JOptionPane.DEFAULT_OPTION);
					} else {
						JOptionPane.showMessageDialog(null, "删除失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		myMenu.add(deleteItem);
	}
	private void new_folder(JPopupMenu myMenu) {
		JMenuItem createDirItem = new JMenuItem("新建文件夹");
		createDirItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				myFile temp = (myFile) node.getUserObject();  //得到树节点关联的值
				int blokName = temp.getBlockName();
				Folder currentBlock = folders.get(blokName - 1);
				String inputValue = JOptionPane.showInputDialog("文件夹名称:");
				if (inputValue == null) {
					return;
				}
				File newDir = new File(temp.getFilePath() + File.separator + inputValue);  //初始该文件夹的信息
				if (newDir.exists())
					deleteDirectory(newDir.getPath());
				try {
					newDir.mkdir();  //在硬盘上生成文件夹
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new myFile(newDir, blokName, 0));
					newNode.add(new DefaultMutableTreeNode("temp"));
					display_model.removeRows(0, display_model.getRowCount());
					display_model.addRow(new myFile(newDir, blokName, 0));
					fileTable.updateUI();
					upDateBlock(currentBlock);
					JOptionPane.showMessageDialog(null, "创建成功，请刷新文件夹！", "Success", JOptionPane.DEFAULT_OPTION);
				} catch (Exception E) {
					JOptionPane.showMessageDialog(null, "创建失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		myMenu.add(createDirItem);
	}
	private void new_file(JPopupMenu myMenu) {
		JMenuItem createFileItem = new JMenuItem("新建文件");
		createFileItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				myFile temp = (myFile) node.getUserObject();
				int blokName = temp.getBlockName();
				Folder currentBlock = folders.get(blokName - 1);

				String inputValue;
				double capacity;

				JOptionPane inputPane = new JOptionPane();
				inputPane.setPreferredSize(new Dimension(600, 600));
				inputPane.setInputValue(JOptionPane.showInputDialog("文件名："));
				if (inputPane.getInputValue() == null) {
					return;
				}
				inputValue = inputPane.getInputValue().toString();
				inputPane.setInputValue(JOptionPane.showInputDialog("文件大小(KB):"));
				if (inputPane.getInputValue() == null) {
					return;
				}
				capacity = Double.parseDouble(inputPane.getInputValue().toString());

				File newFile = new File(temp.getFilePath() + File.separator + inputValue + ".txt");
				if (!newFile.exists() && !inputValue.equals(null)) {
					try {
						if (currentBlock.createFile(newFile, capacity)) {
							DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
									new myFile(newFile, blokName, capacity));
							display_model.removeRows(0, display_model.getRowCount());
							display_model.addRow(new myFile(newFile, blokName, capacity));
							fileTable.updateUI();
							upDateBlock(currentBlock);
							JOptionPane.showMessageDialog(null, "创建成功，请刷新文件夹！", "Success", JOptionPane.DEFAULT_OPTION);
						}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "创建失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		myMenu.add(createFileItem);
	}

	// 界面
	public myFileSys() throws IOException {
		setTitle("OS-myFileSys");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.BLACK);


		// 图标初始化
		setIconImage(new ImageIcon(this.getClass().getResource("/image/folder.png")).getImage());
		//getContentPane().setBackground(Color.WHITE);

		ImageIcon folderImg = new ImageIcon(this.getClass().getResource("/image/folder.png"));
		folderImg.setImage(folderImg.getImage().getScaledInstance(30, 23, Image.SCALE_DEFAULT));
		Icon icon1 = folderImg; //foldIcon

		ImageIcon docImg = new ImageIcon(this.getClass().getResource("/image/document.png"));
		docImg.setImage(docImg.getImage().getScaledInstance(24, 30, Image.SCALE_DEFAULT));
		Icon icon2 = docImg;  //docIcon

		UIManager.put("Tree.openIcon", icon1);
		UIManager.put("Tree.closedIcon", icon1);
		UIManager.put("Tree.leafIcon", icon2);

		// 创建工作区——如果已有的话就直接用了
		rootFile = new File("my_fileSys");
		readMe = new File("my_fileSys" + File.separator + "ReadMe.txt");

		// 导航窗格
		navigation_pane();

		// 文件表初始化(右侧显示文件具体信息的部分
		file_table();

		// 鼠标右键的菜单功能
		menu_ope();

		// 显示在最下栏的文件夹信息
		folder_info();

		// 显示文件路径
		path_display();

		this.setBounds(150, 50, 800, 600);
		setVisible(true);
	}

	//导航窗格（左侧的文件树
	private void navigation_pane() throws IOException{
		boolean flag = true;
		// 文件树初始化
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new myFile(rootFile, 0, 10240));
		if (!rootFile.exists()) {
			flag = false;
			try {
				rootFile.mkdir();
				readMe.createNewFile();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "空闲空间不足!", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			FileWriter writer = new FileWriter(readMe.getPath());
			writer.write("Hello, this my file system!!!\n");
			writer.write("Space: 10 * 1024K = 10M\n");
			writer.write("Free-Space Management:bitmap\n");
			writer.write("Store-Space Management:FAT\n");
			writer.flush();
			writer.close();
		}

		// 创建初始文件夹
		for(char i = 1;i<=folder_num;i++){
			Folder tmp = new Folder(String.valueOf((char) (i+'B')), new File(rootFile.getPath() + File.separator + (char) (i+'B')), flag);
			folders.add(tmp);

			root.add(new DefaultMutableTreeNode(new myFile(tmp.getBlockFile(), i, 1024.0)));//加到树中
			display_model.addRow(new myFile(tmp.getBlockFile(), i, 1024.0));//在表中添加一行
			((DefaultMutableTreeNode) root.getChildAt(i-1)).add(new DefaultMutableTreeNode("temp"));
		}

		// 导航窗格树的初始化
		final DefaultTreeModel treeModel = new DefaultTreeModel(root); //参数是树的根节点，也就是文件的根目录
		tree = new JTree(treeModel);
		tree.setEditable(false);
		tree.setRootVisible(false);
		tree.putClientProperty("Jtree.lineStyle", "Horizontal");
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		//在JTree上点选任何一个节点，都会触发TreeSelectionEvent事件
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode parent = null;
				TreePath parentPath = e.getPath();
				if (parentPath == null) {
					parent = root;
				} else {
					parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
				}
				int blokName = ((myFile) parent.getUserObject()).getBlockName();
				Folder currentBlock = folders.get(blokName - 1);
				if (parentPath == null) {
					parent = root;
				} else {
					parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
				}

				nameField.setText(String.valueOf((char)(blokName+'B')));
				pathField.setText(((myFile) parent.getUserObject()).getFilePath());
				upDateBlock(currentBlock);

				display_model.removeRows(0, display_model.getRowCount());
				File rootFile = new File(((myFile) parent.getUserObject()).getFilePath());
				if (parent.getChildCount() > 0) {
					File[] childFiles = rootFile.listFiles();

					for (File file : childFiles) {
						display_model.addRow(new myFile(file, blokName, getSpace(file)));
					}
				} else {
					display_model.addRow(new myFile(rootFile, blokName, getSpace(rootFile)));
				}
				fileTable.updateUI();

			}
		});
		//tree折叠展开事件，Expand展开，Collapse折叠
		tree.addTreeWillExpandListener(new TreeWillExpandListener() { //内部生成几个初始文件
			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
				DefaultMutableTreeNode parent = null;
				TreePath parentPath = event.getPath();
				if (parentPath == null) {
					parent = root;
				} else {
					parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
				}

				int blokName = ((myFile) parent.getUserObject()).getBlockName();

				File rootFile = new File(((myFile) parent.getUserObject()).getFilePath());//所点击的节点
				File[] childFiles = rootFile.listFiles();  //所点击节点的子节点

				//以下为右侧详细信息的刷新
				display_model.removeRows(0, display_model.getRowCount()); //右侧显示部分全部清空
				for (File tmp : childFiles) {  //遍历子文件，把他们加入到树中
					//首先将该文件变成树的节点
					DefaultMutableTreeNode node = null;
					node = new DefaultMutableTreeNode(new myFile(tmp, blokName, getSpace(tmp)));
					if (tmp.isDirectory() && tmp.canRead()) {
						node.add(new DefaultMutableTreeNode("temp"));
					}

					treeModel.insertNodeInto(node, parent, parent.getChildCount());
					display_model.addRow(new myFile(tmp, blokName, getSpace(tmp)));
				}
				if (parent.getChildAt(0).toString().equals("temp") && parent.getChildCount() != 1)
					treeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(0));
				fileTable.updateUI();
			}

			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				DefaultMutableTreeNode parent = null;
				TreePath parentPath = event.getPath();
				if (parentPath == null) {
					parent = root;
				} else {
					parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
				}
				if (parent.getChildCount() > 0) {
					int count = parent.getChildCount();
					for (int i = count - 1; i >= 0; i--) {
						treeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(i));
					}
					treeModel.insertNodeInto(new DefaultMutableTreeNode("temp"), parent, parent.getChildCount());
				}
				display_model.removeRows(0, display_model.getRowCount());
				fileTable.updateUI();
			}
		});
		treePane = new JScrollPane(tree);

		treePane.setPreferredSize(new Dimension(180, 400));
		add(treePane, BorderLayout.WEST);
	}
	//文件表(右侧
	private void file_table() {
		fileTable = new JTable(display_model);
		fileTable.getTableHeader().setFont(new Font(Font.DIALOG, Font.CENTER_BASELINE, 24));
		fileTable.setSelectionBackground(Color.ORANGE);
		fileTable.setShowHorizontalLines(true);
		fileTable.setShowVerticalLines(false);
		fileTable.getTableHeader().setFont(new Font("宋体", Font.CENTER_BASELINE, 16));
		fileTable.getTableHeader().setForeground(Color.WHITE);
		fileTable.getTableHeader().setBackground(new Color(40, 40, 40));
		fileTable.setRowHeight(30);
		fileTable.setBackground(new Color(82, 82, 82));
		fileTable.setForeground(new Color(255, 255, 255));
		fileTable.setSelectionBackground(new Color(40, 40, 40));
		fileTable.setSelectionForeground(Color.WHITE);
		fileTable.setFont(new Font(Font.DIALOG, Font.CENTER_BASELINE, 14));
		fileTable.updateUI();
		// 右侧表的初始化
		tablePane = new JScrollPane(fileTable);
		add(tablePane, BorderLayout.CENTER);
		// 双击打开文件
		fileTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					String fileName = ((String) display_model.getValueAt(fileTable.getSelectedRow(), 0));
					String filePath = ((String) display_model.getValueAt(fileTable.getSelectedRow(), 1));
					try {
						if (Desktop.isDesktopSupported()) {
							Desktop desktop = Desktop.getDesktop();
							desktop.open(new File(filePath));
						}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "抱歉，出了一些错误！", "Fail to open", JOptionPane.ERROR_MESSAGE);
					}
					JOptionPane.showMessageDialog(null, "File Name: " + fileName + "\n File Path: " + filePath,
							"content", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}
	//显示在最下栏的文件夹信息
	private void folder_info() {
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0));
		panel.setForeground(Color.WHITE);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));

		add_into_panel(panel, blockName, nameField);
		add_into_panel(panel, haveUsed, usedField);
		add_into_panel(panel,freeYet, freeField);
		add_into_panel(panel,fileNum,fileNumField);

		add(panel, BorderLayout.SOUTH);
	}
	private void add_into_panel(JPanel panel, JLabel name, JLabel value) {
		name.setForeground(Color.WHITE);
		panel.add(name);
		value.setForeground(Color.WHITE);
		panel.add(value);
		panel.add(new JLabel(" | "));
	}
	// 文件路径
	private void path_display(){
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		panel.setBackground(new Color(0, 0, 0));
		panel.setForeground(Color.WHITE);

		JLabel text = new JLabel("path<< ");
		text.setForeground(Color.WHITE);
		panel.add(text);
		pathField.setForeground(Color.WHITE);
		panel.add(pathField);
		add(panel, BorderLayout.NORTH);
	}

	// 获取空闲空间
	public double getSpace(File file) {
		double space = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			reader.readLine();
			space = Double.parseDouble(reader.readLine());
			if (space > 1024) {
				space = 0.0;
			}
			reader.close();
		} catch (Exception e) {
		}
		;
		return space;
	}
	// 删除文件夹
	public static void deleteDirectory(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File myfile : files) {
				deleteDirectory(filePath + File.separator + myfile.getName());
			}
			file.delete();
		}
	}
	// 更新文件夹信息
	public void upDateBlock(Folder currentBlock) {
		fileNumField.setText(String.valueOf(currentBlock.getFileNum()));
		usedField.setText(String.valueOf(currentBlock.getSpace()) + " KB");
		freeField.setText(String.valueOf(1024 - currentBlock.getSpace()) + "KB");
	}
}
