package autils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import common.RetResult;
import common.error;

public class FileUtils {

	public static RetResult<byte[]> readFile(String filePath) {
		File file = new File(filePath);

		error err = null;
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(b);
			for (int i = 0; i < b.length; i++) {
				System.out.print((char) b[i]);
			}
		} catch (FileNotFoundException e) {
//             System.out.println("File Not Found.");
//             e.printStackTrace();
			err = new error("File Not Found: " + filePath);
		} catch (IOException e1) {
			err = new error("Error Reading The File: " + filePath);
			e1.printStackTrace();
		}

		return new RetResult<byte[]>(b, err);
	}

	public static RetResult<byte[]> readFileToByteArray(String filePath) {
		error err = null;
		byte[] b = null;
		try {
			b = Files.readAllBytes(Paths.get(filePath));
		} catch (IOException e) {
			err = new error("Error Reading The File: " + filePath);
//			e.printStackTrace();
		}
		return new RetResult<byte[]>(b, err);
	}

	public static void setPermission(Path path, int mod) throws IOException {

		// using PosixFilePermission to set file permissions 755
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		if (mod == 755) {
			// add owners permission
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_WRITE);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			// add group permissions
			perms.add(PosixFilePermission.GROUP_READ);
			perms.add(PosixFilePermission.GROUP_EXECUTE);
			// add others permissions
			perms.add(PosixFilePermission.OTHERS_READ);
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
		} else if (mod == 755) {
			// add owners permission
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_WRITE);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			// add group permissions
			// add others permissions
		}

		Files.setPosixFilePermissions(path, perms);
	}

	public static RetResult<File> createFile(String filePath, int mod) {
		File file = Paths.get(filePath).toFile();

		error err = null;
		if (!file.exists()) {
			file.mkdir();
			try {
				setPermission(file.toPath(), mod);
			} catch (IOException e) {
				err = error.Errorf("error writing to file " + filePath + ", returned msg= " + e.getMessage());
			}
		}
		return new RetResult<File>(file, err);
	}

	public static error writeToFile(File file, byte[] byteArray) {
		FileOutputStream fileOuputStream = null;
		try {
			fileOuputStream = new FileOutputStream(file);
			fileOuputStream.write(byteArray);
		} catch (IOException e) {
			return error.Errorf("error writing to file " + e.getMessage());
		} finally {
			if (fileOuputStream != null) {
				try {
					fileOuputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
