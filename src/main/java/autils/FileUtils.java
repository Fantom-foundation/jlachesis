package autils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
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

	public static boolean fileExist(String filePath) {
		File file = Paths.get(filePath).toFile();
		return file.exists();
	}

	/**
	 *
	 * @param filePath
	 * @param mod is the mod in the forms of "rwxrwxrwx" or "rw-r--r--"
	 * @return
	 */
	public static RetResult<Path> mkdirs(String filePath, String mod) {
		Path newDirectoryPath = Paths.get(filePath).getParent();
		error err = null;

		if (!Files.exists(newDirectoryPath)) {
			FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(mod));
			try {
				newDirectoryPath = Files.createDirectory(newDirectoryPath, fileAttributes);
			} catch (IOException e) {
				err = error.Errorf("error mkdir " + filePath + ", returned msg= " + e.getMessage());
			}
		}
		return new RetResult<>(newDirectoryPath, err);
	}

	/**
	 *
	 * @param filePath
	 * @param mod is the mod in the forms of "rwxrwxrwx" or "rw-r--r--"
	 * @return
	 */
	public static RetResult<Path> createFile(String filePath, String mod) {
		Path newFilePath = Paths.get(filePath);
		error err = null;

		if (!Files.exists(newFilePath)) {
			FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(mod));
			try {
				newFilePath = Files.createFile(newFilePath, fileAttributes);
			} catch (IOException e) {
				err = error.Errorf("error create a file " + filePath + ", returned msg= " + e.getMessage());
			}
		}

		return new RetResult<>(newFilePath, err);
	}

	public static error writeToFile(Path file, byte[] bytes) {
		try {
			Files.write(file, bytes);
		} catch (IOException e) {
			return error.Errorf("error writing to file " + e.getMessage());
		}
		return null;
	}


	public static error writeToFile(String file, byte[] bytes, String mod) {
		RetResult<Path> createFile = createFile(file, mod);
		if (createFile.err != null) {
			return createFile.err;
		}
		error err = writeToFile(createFile.result, bytes);
		return err;
	}

	public static final String MOD_666 = "rw-rw-rw-";
	public static final String MOD_777 = "rwxrwxrwx";
	public static final String MOD_700 = "rwx------";
	public static final String MOD_755 = "rwxr-xr-x";
}
