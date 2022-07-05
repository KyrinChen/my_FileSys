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

	// ϵͳ�Դ����ļ���
	private int folder_num = 4;
	private ArrayList<Folder> folders = new ArrayList<Folder>();

	// ��Ϣ��
	private JLabel blockName = new JLabel("���ڸ�Ŀ¼:");
	private JLabel nameField = new JLabel();
	private JLabel haveUsed = new JLabel("��ʹ�ÿռ�:");
	private JLabel usedField = new JLabel();
	private JLabel freeYet = new JLabel("ʣ��ռ�:");
	private JLabel freeField = new JLabel();
	private JLabel fileNum = new JLabel("�ļ���:");
	private JLabel fileNumField = new JLabel();
	private JLabel pathField = new JLabel("my_fileSys");

	public static void main(String args[]) throws IOException {
		new myFileSys();
	}

	//----------------һ��Ϊ����Ҽ��Ĳ˵�����-----------------
	private void menu_ope(){  //�����˵�,��������:�½��ļ����½��ļ��У�ɾ������ʽ����������
		// �˵���ʼ��
		final JPopupMenu myMenu = new JPopupMenu();  //JPopupMenu:�����˵���һ���ɵ�������ʾһϵ��ѡ���С����
		myMenu.setPreferredSize(new Dimension(300, 150));
		// �½��ļ���
		new_folder(myMenu);
		// �½��ļ�
		new_file( myMenu);
		// ɾ���ļ�/�ļ���
		delete_item(myMenu);
		// ��ʽ��
		format_item(myMenu);
		// ���ļ����Ӽ���������Ҽ���ʾ�˵�
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getButton() == MouseEvent.BUTTON3) {  //MouseEvent.BUTTON3����Ҽ�
					myMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}
	private void format_item(JPopupMenu myMenu) {
		JMenuItem formatItem = new JMenuItem("��ʽ��");
		formatItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				myFile temp = (myFile) node.getUserObject();
				int blokName = temp.getBlockName();
				Folder currentBlock = folders.get(blokName - 1);
				int choose = JOptionPane.showConfirmDialog(null, "ȷ����ʽ���ļ�����", "confirm", JOptionPane.YES_NO_OPTION);
				if (choose == 0) {
					try {
						if (temp.getMyFile().isDirectory()) {
							for (File myfile : temp.getMyFile().listFiles()) {
								currentBlock.deleteFile(myfile, getSpace(myfile));
							}
							upDateBlock(currentBlock);
							JOptionPane.showMessageDialog(null, "��ʽ���ɹ�����ˢ���ļ��У�", "Success", JOptionPane.DEFAULT_OPTION);
							currentBlock.rewriteBitMap();
						}
					} catch (Exception E1) {
						JOptionPane.showMessageDialog(null, "��ʽ��ʧ��!!!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		myMenu.add(formatItem);
	}
	private void delete_item(JPopupMenu myMenu) {
		JMenuItem deleteItem = new JMenuItem("ɾ��");
		deleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				myFile temp = (myFile) node.getUserObject();
				int blokName = temp.getBlockName();
				Folder currentBlock = folders.get(blokName - 1);
				int choose = JOptionPane.showConfirmDialog(null, "ȷ��ɾ����", "confirm", JOptionPane.YES_NO_OPTION);
				if (choose == 0) {
					if (currentBlock.deleteFile(temp.getMyFile(), temp.getSpace())) {
						try {
							currentBlock.rewriteBitMap();
							currentBlock.rewriteRecoverWriter();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						upDateBlock(currentBlock);
						JOptionPane.showMessageDialog(null, "ɾ���ɹ�����ˢ���ļ��У�", "Success", JOptionPane.DEFAULT_OPTION);
					} else {
						JOptionPane.showMessageDialog(null, "ɾ��ʧ��!!!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		myMenu.add(deleteItem);
	}
	private void new_folder(JPopupMenu myMenu) {
		JMenuItem createDirItem = new JMenuItem("�½��ļ���");
		createDirItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				myFile temp = (myFile) node.getUserObject();  //�õ����ڵ������ֵ
				int blokName = temp.getBlockName();
				Folder currentBlock = folders.get(blokName - 1);
				String inputValue = JOptionPane.showInputDialog("�ļ�������:");
				if (inputValue == null) {
					return;
				}
				File newDir = new File(temp.getFilePath() + File.separator + inputValue);  //��ʼ���ļ��е���Ϣ
				if (newDir.exists())
					deleteDirectory(newDir.getPath());
				try {
					newDir.mkdir();  //��Ӳ���������ļ���
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new myFile(newDir, blokName, 0));
					newNode.add(new DefaultMutableTreeNode("temp"));
					display_model.removeRows(0, display_model.getRowCount());
					display_model.addRow(new myFile(newDir, blokName, 0));
					fileTable.updateUI();
					upDateBlock(currentBlock);
					JOptionPane.showMessageDialog(null, "�����ɹ�����ˢ���ļ��У�", "Success", JOptionPane.DEFAULT_OPTION);
				} catch (Exception E) {
					JOptionPane.showMessageDialog(null, "����ʧ��!!!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		myMenu.add(createDirItem);
	}
	private void new_file(JPopupMenu myMenu) {
		JMenuItem createFileItem = new JMenuItem("�½��ļ�");
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
				inputPane.setInputValue(JOptionPane.showInputDialog("�ļ�����"));
				if (inputPane.getInputValue() == null) {
					return;
				}
				inputValue = inputPane.getInputValue().toString();
				inputPane.setInputValue(JOptionPane.showInputDialog("�ļ���С(KB):"));
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
							JOptionPane.showMessageDialog(null, "�����ɹ�����ˢ���ļ��У�", "Success", JOptionPane.DEFAULT_OPTION);
						}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "����ʧ��!!!", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		myMenu.add(createFileItem);
	}

	// ����
	public myFileSys() throws IOException {
		setTitle("OS-myFileSys");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.BLACK);


		// ͼ���ʼ��
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

		// ��������������������еĻ���ֱ������
		rootFile = new File("my_fileSys");
		readMe = new File("my_fileSys" + File.separator + "ReadMe.txt");

		// ��������
		navigation_pane();

		// �ļ����ʼ��(�Ҳ���ʾ�ļ�������Ϣ�Ĳ���
		file_table();

		// ����Ҽ��Ĳ˵�����
		menu_ope();

		// ��ʾ�����������ļ�����Ϣ
		folder_info();

		// ��ʾ�ļ�·��
		path_display();

		this.setBounds(150, 50, 800, 600);
		setVisible(true);
	}

	//�������������ļ���
	private void navigation_pane() throws IOException{
		boolean flag = true;
		// �ļ�����ʼ��
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new myFile(rootFile, 0, 10240));
		if (!rootFile.exists()) {
			flag = false;
			try {
				rootFile.mkdir();
				readMe.createNewFile();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "���пռ䲻��!", "Error", JOptionPane.ERROR_MESSAGE);
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

		// ������ʼ�ļ���
		for(char i = 1;i<=folder_num;i++){
			Folder tmp = new Folder(String.valueOf((char) (i+'B')), new File(rootFile.getPath() + File.separator + (char) (i+'B')), flag);
			folders.add(tmp);

			root.add(new DefaultMutableTreeNode(new myFile(tmp.getBlockFile(), i, 1024.0)));//�ӵ�����
			display_model.addRow(new myFile(tmp.getBlockFile(), i, 1024.0));//�ڱ������һ��
			((DefaultMutableTreeNode) root.getChildAt(i-1)).add(new DefaultMutableTreeNode("temp"));
		}

		// �����������ĳ�ʼ��
		final DefaultTreeModel treeModel = new DefaultTreeModel(root); //���������ĸ��ڵ㣬Ҳ�����ļ��ĸ�Ŀ¼
		tree = new JTree(treeModel);
		tree.setEditable(false);
		tree.setRootVisible(false);
		tree.putClientProperty("Jtree.lineStyle", "Horizontal");
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		//��JTree�ϵ�ѡ�κ�һ���ڵ㣬���ᴥ��TreeSelectionEvent�¼�
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
		//tree�۵�չ���¼���Expandչ����Collapse�۵�
		tree.addTreeWillExpandListener(new TreeWillExpandListener() { //�ڲ����ɼ�����ʼ�ļ�
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

				File rootFile = new File(((myFile) parent.getUserObject()).getFilePath());//������Ľڵ�
				File[] childFiles = rootFile.listFiles();  //������ڵ���ӽڵ�

				//����Ϊ�Ҳ���ϸ��Ϣ��ˢ��
				display_model.removeRows(0, display_model.getRowCount()); //�Ҳ���ʾ����ȫ�����
				for (File tmp : childFiles) {  //�������ļ��������Ǽ��뵽����
					//���Ƚ����ļ�������Ľڵ�
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
	//�ļ���(�Ҳ�
	private void file_table() {
		fileTable = new JTable(display_model);
		fileTable.getTableHeader().setFont(new Font(Font.DIALOG, Font.CENTER_BASELINE, 24));
		fileTable.setSelectionBackground(Color.ORANGE);
		fileTable.setShowHorizontalLines(true);
		fileTable.setShowVerticalLines(false);
		fileTable.getTableHeader().setFont(new Font("����", Font.CENTER_BASELINE, 16));
		fileTable.getTableHeader().setForeground(Color.WHITE);
		fileTable.getTableHeader().setBackground(new Color(40, 40, 40));
		fileTable.setRowHeight(30);
		fileTable.setBackground(new Color(82, 82, 82));
		fileTable.setForeground(new Color(255, 255, 255));
		fileTable.setSelectionBackground(new Color(40, 40, 40));
		fileTable.setSelectionForeground(Color.WHITE);
		fileTable.setFont(new Font(Font.DIALOG, Font.CENTER_BASELINE, 14));
		fileTable.updateUI();
		// �Ҳ��ĳ�ʼ��
		tablePane = new JScrollPane(fileTable);
		add(tablePane, BorderLayout.CENTER);
		// ˫�����ļ�
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
						JOptionPane.showMessageDialog(null, "��Ǹ������һЩ����", "Fail to open", JOptionPane.ERROR_MESSAGE);
					}
					JOptionPane.showMessageDialog(null, "File Name: " + fileName + "\n File Path: " + filePath,
							"content", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}
	//��ʾ�����������ļ�����Ϣ
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
	// �ļ�·��
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

	// ��ȡ���пռ�
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
	// ɾ���ļ���
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
	// �����ļ�����Ϣ
	public void upDateBlock(Folder currentBlock) {
		fileNumField.setText(String.valueOf(currentBlock.getFileNum()));
		usedField.setText(String.valueOf(currentBlock.getSpace()) + " KB");
		freeField.setText(String.valueOf(1024 - currentBlock.getSpace()) + "KB");
	}
}
