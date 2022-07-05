import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Folder {
	private String blockName;  //�ļ��е�����
	private File blockFile;    //���ļ��б���
	private File blockBitMap;  //��¼���пռ��λͼ
	private File recover;
	private FileWriter bitWriter;
	private FileWriter recoverWriter;
	private int fileNum;
	private double space;
	public int[][] bitmap = new int[32][32];
	private Map<String, int[][]> filesBit = new HashMap<String, int[][]>();
	private ArrayList<File> files = new ArrayList<File>();

	public Folder(String name, File file, boolean rec) throws IOException {
		blockName = name;  //�ļ��е�����
		blockFile = file;  //���ļ��б���
		blockBitMap = new File(blockFile.getPath() + File.separator + blockName + "-λͼ_FAT.txt");
		recover = new File(blockFile.getPath() + File.separator + "restore.txt");
		if (!rec) {
			//�ļ����ڲ����ݳ�ʼ��
			space = 0;
			fileNum = 0;
			blockFile.mkdir();  //���ɸ��ļ���
			blockBitMap.createNewFile();
			bitWriter = new FileWriter(blockBitMap);
			//���пռ�λͼ,ȫ����ʼ��Ϊ0
			for (int i = 0; i < 32; i++) {
				for (int k = 0; k < 32; k++) {
					bitmap[i][k] = 0;
					bitWriter.write("0");
				}
				bitWriter.write("\r\n");
			}
			bitWriter.flush();

			recover.createNewFile();
			recoverWriter = new FileWriter(recover);
			recoverWriter.write(String.valueOf(space) + "\r\n");
			recoverWriter.write(String.valueOf(fileNum) + "\r\n");
			for (int i = 0; i < 32; i++) {
				for (int k = 0; k < 32; k++) {
					if (bitmap[i][k] == 0) {
						recoverWriter.write("0\r\n");
					} else {
						recoverWriter.write("1\r\n");
					}
				}
			}
			recoverWriter.flush();
		}
		else {  //���rec=1, ��ʾrecover.txt������Ҫ�ָ����ļ�
			try {
				BufferedReader reader = new BufferedReader(new FileReader(recover));
				space = Double.parseDouble(reader.readLine()); //��stringת��Ϊdouble, Recover�ļ��ĵ�һ��
				fileNum = Integer.parseInt(reader.readLine()); //��stringת��Ϊint, Recover�ļ��ĵڶ���
				// ����recover�ļ����ָ�λͼ
				for (int i = 0; i < 32; i++) {
					for (int k = 0; k < 32; k++) {
						if (Integer.parseInt(reader.readLine()) == 0) {
							bitmap[i][k] = 0;
						} else {
							bitmap[i][k] = 1;
						}
					}
				}
				// ͨ��recover�м�¼�����ݽ��ļ��ָ�
				String temp;  //���ڼ�¼�ļ���
				while ((temp = reader.readLine()) != null) {
					File myFile = new File(blockFile.getPath() + File.separator + temp);
					files.add(myFile);
					int[][] tempBit = new int[32][32];
					for (int i = 0; i < 32; i++) {
						for (int k = 0; k < 32; k++) {
							if (Integer.parseInt(reader.readLine()) == 0) {
								tempBit[i][k] = 0;
							} else {
								tempBit[i][k] = 1;
							}
						}
					}
					filesBit.put(myFile.getName(), tempBit);
				}
				reader.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "��ɾ�����ļ����µ�\"my_fileSys\"���������д˳��򣡣�", "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}

	public void putFCB(File file, double capacity) throws IOException {
		FileWriter newFileWriter = new FileWriter(file);
		newFileWriter.write("File\r\n");
		newFileWriter.write(String.valueOf(capacity) + "\r\n");
		newFileWriter.write("Name: " + file.getName() + "\r\n");
		newFileWriter.write("Path: " + file.getPath() + "\r\n");
		String ctime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(file.lastModified()));
		newFileWriter.write("Date last updated: " + ctime + "\r\n");
		newFileWriter.write("--------------------------edit file blew ------------------------------\r\n");
		newFileWriter.close();
	}

	public void rewriteBitMap() throws IOException {
		bitWriter = new FileWriter(blockBitMap);
		bitWriter.write("");
		for (int i = 0; i < 32; i++) {
			for (int k = 0; k < 32; k++) {
				if (bitmap[i][k] == 0) {
					bitWriter.write("0");
				} else {
					bitWriter.write("1");
				}
			}
			bitWriter.write("\r\n");
		}
		for (int i = 0; i < files.size(); i++) {
			bitWriter.write(files.get(i).getName() + ":");
			for (int k = 0; k < 32; k++) {
				for (int j = 0; j < 32; j++) {
					try {
						if (filesBit.get(files.get(i).getName())[k][j] == 1) {
							bitWriter.write(String.valueOf(k * 32 + j) + " ");
						}
					} catch (Exception e) {
						System.out.println("wrong");
					}
				}
			}
			bitWriter.write("\r\n");
		}
		bitWriter.flush();
	}

	public void rewriteRecoverWriter() throws IOException {
		recoverWriter = new FileWriter(recover);
		recoverWriter.write("");

		recoverWriter.write(String.valueOf(space) + "\r\n");
		recoverWriter.write(String.valueOf(fileNum) + "\r\n");
		for (int i = 0; i < 32; i++) {
			for (int k = 0; k < 32; k++) {
				if (bitmap[i][k] == 0) {
					recoverWriter.write("0\r\n");
				} else {
					recoverWriter.write("1\r\n");
				}
			}
		}
		for (int i = 0; i < files.size(); i++) {
			recoverWriter.write(files.get(i).getName() + "\r\n");
			int[][] bitTemp = filesBit.get(files.get(i).getName());
			for (int k = 0; k < 32; k++) {
				for (int j = 0; j < 32; j++) {
					if (bitTemp[k][j] == 0) {
						recoverWriter.write("0\r\n");
					} else {
						recoverWriter.write("1\r\n");
					}
				}
			}
		}
		recoverWriter.flush();
	}

	//����ļ������½��ļ�
	public boolean createFile(File file, double capacity) throws IOException {
		files.add(file);
		file.createNewFile();
		int cap[][] = new int[32][32];
		for (int i = 0; i < 32; i++) {
			for (int k = 0; k < 32; k++)
				cap[i][k] = 0;
		}
		BufferedReader in = new BufferedReader(new FileReader(blockBitMap));
		int count = (int) capacity;
		for (int i = 0; i < 32; i++) {
			String line = in.readLine();
			for (int k = 0; k < 32; k++) {
				if (count > 0) {
					if (line.charAt(k) == '0') {
						count--;
						cap[i][k] = 1;
						bitmap[i][k] = 1;
					}
				}
			}
		}
		if (count > 0) {
			JOptionPane.showMessageDialog(null, "�洢�ռ䲻��!!", "Fail", JOptionPane.ERROR_MESSAGE);
			file.delete();
			for (int i = 0; i < 32; i++) {
				for (int k = 0; k < 32; k++) {
					if (cap[i][k] == 1) {
						bitmap[i][k] = 0;
					}
				}
			}
			return false;
		} else {
			fileNum++;
			space += capacity;
			filesBit.put(file.getName(), cap);
			rewriteBitMap();
			rewriteRecoverWriter();
			// Put FCB
			putFCB(file, capacity);

			return true;
		}
	}

	public boolean deleteFile(File file, double capacity) {
		if (file.getName().equals("C") || file.getName().equals("D") || file.getName().equals("E") || file.getName().equals("F")
				|| file.getName().equals("C-λͼ_FAT.txt") || file.getName().equals("D-λͼ_FAT.txt")
				|| file.getName().equals("E-λͼ_FAT.txt") || file.getName().equals("F-λͼ_FAT.txt")
				|| file.getName().equals("restore.txt")) {
			JOptionPane.showMessageDialog(null, "���ļ��ܱ���!!", "Access fail", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			if (file.isFile()) {
				try {
					file.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
				space -= capacity;
				fileNum--;
				int[][] fileStore = filesBit.get(file.getName());
				for (int i = 0; i < 32; i++) {
					for (int k = 0; k < 32; k++) {
						if (bitmap[i][k] == 1 && fileStore[i][k] == 1) {
							bitmap[i][k] = 0;
						}
					}
				}
				filesBit.remove(file.getName());
				for (int i = 0; i < files.size(); i++) {
					if (files.get(i).getName().equals(file.getName())) {
						files.remove(i);
						break;
					}
				}
			} else {
				File[] files = file.listFiles();
				for (File myFile : files) {
					deleteFile(myFile, capacity);
				}
				while (file.exists()) {
					file.delete();
				}
			}
			return true;
		} catch (Exception e) {
			System.out.println("fail");
			return false;
		}
	}


	public int getFileNum() {
		return fileNum;
	}
	public double getSpace() {
		return space;
	}
	public File getBlockFile() {
		return blockFile;
	}
}
