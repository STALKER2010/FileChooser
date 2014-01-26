package andrews.fm.utils;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {
	public final int compare(File pFirst, File pSecond) {

		if (pFirst.isDirectory() && pSecond.isFile()) {
			return -1;
		} else if (pFirst.isFile() && pSecond.isDirectory()) {
			return 1;
		}
		String name1 = pFirst.getName();
		String name2 = pSecond.getName();
		if (name1.compareToIgnoreCase(name2) < 0) {
			return -1;
		} else
			return 1;
	}
}
