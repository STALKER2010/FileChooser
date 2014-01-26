package andrews.fm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.os.Environment;

import java.io.IOException;

import java.io.FileReader;

/*
 * EFile is a helper for apps, that allows you to read and write it quickly.
 */
public class EFile {
	
	public static boolean canAccess(File mFile) {
		if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
			return false;
		return (mFile.exists() && mFile.canRead());
	}

	public static String readFile(File mFile) throws IOException {
		String res = null;
		FileReader fis = new FileReader(mFile);
		char[] buffer;
		buffer = new char[1100];
		StringBuffer result = new StringBuffer();
		int read = 0;
		do {
			read = fis.read(buffer);
			if (read >= 0) {
				result.append(buffer, 0, read);
			}
		} while (read >= 0);
		res = result.toString();
		fis.close();
		return res;
	}

	public static void saveFile(File mFile, String content) throws IOException {
		FileWriter fstream = new FileWriter(mFile);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(content);
		out.close();
	}

	public static void delete(File mFile) {
		if (mFile.isDirectory()) {
			for (File fl : mFile.listFiles()) {
				if (fl.isDirectory())
					delete(fl);
				fl.delete();
			}
		}
		mFile.delete();
	}
}
