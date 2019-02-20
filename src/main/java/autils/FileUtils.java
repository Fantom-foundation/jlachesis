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
import java.util.Set;

import common.RResult;
import common.error;

public class FileUtils {
	public static final String MOD_666 = "rw-rw-rw-";
	public static final String MOD_777 = "rwxrwxrwx";
	public static final String MOD_700 = "rwx------";
	public static final String MOD_755 = "rwxr-xr-x";

	public static RResult<byte[]> readFile(String filePath) {
		File file = new File(filePath);

		error err = null;
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream fis = new FileInputStream(file);
			fis.read(b);
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

		return new RResult<byte[]>(b, err);
	}

	public static RResult<byte[]> readFileToByteArray(String filePath) {
		error err = null;
		byte[] b = null;
		try {
			b = Files.readAllBytes(Paths.get(filePath));
		} catch (IOException e) {
			err = new error("Error Reading The File: " + filePath);
//			e.printStackTrace();
		}
		return new RResult<byte[]>(b, err);
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
	public static RResult<Path> mkdirs(String filePath, String mod) {
		return mkdir(Paths.get(filePath).getParent(), mod);
	}

	public static RResult<Path> mkdir(Path dirPath, String mod) {
		error err = null;

		if (!Files.exists(dirPath)) {
			FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(mod));
			try {
				dirPath = Files.createDirectory(dirPath, fileAttributes);
			} catch (IOException e) {
				err = error.Errorf("error mkdir " + dirPath + ", returned msg= " + e.getMessage());
			}
		}
		return new RResult<>(dirPath, err);
	}

	/**
	 *
	 * @param filePath
	 * @param mod is the mod in the forms of "rwxrwxrwx" or "rw-r--r--"
	 * @return
	 */
	public static RResult<Path> createFile(String filePath, String mod) {
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

		return new RResult<>(newFilePath, err);
	}

	public static error delete(String path) {
		deleteRecursive(Paths.get(path).toFile());
		return null;
	}

	static void deleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            deleteRecursive(child);

	    fileOrDirectory.delete();
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
		RResult<Path> createFile = createFile(file, mod);
		if (createFile.err != null) {
			return createFile.err;
		}
		error err = writeToFile(createFile.result, bytes);
		return err;
	}
}
