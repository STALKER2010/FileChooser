package andrews.fm;

import java.io.File;
import android.os.Environment;
import java.io.IOException;
import android.os.ParcelFileDescriptor;
import java.io.FileReader;
import android.content.Context;
import android.net.Uri;

public class EFile
{
	public File file = null;
	public Context c = null;
	public EFile()
	{
		file = Environment.getExternalStorageDirectory();
	}
	public EFile(String str) {
		file = new File(str);
	}
	public EFile(File f) {
		file = f;
	}
	public void setContext(Context con) {
		this.c = con;
	}
	public boolean canAccess() {
		if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) return false;
		return (file.exists() && file.canRead());
	}
	public String readFile()
	{
		String res = null;
		try
		{
			ParcelFileDescriptor pfd = c.getContentResolver().openFileDescriptor(Uri.fromFile(file), "r");
			FileReader fis = new FileReader(pfd.getFileDescriptor());
			char[] buffer;
			buffer = new char[1100];
			StringBuffer result = new StringBuffer();
			int read = 0;
			do {
				read = fis.read(buffer);
				if (read >= 0)
				{
					result.append(buffer, 0, read);
				}
			} while (read >= 0);
			res = result.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	public void delete() {
		if (file.isDirectory()) delete(file);
		file.delete();
	}
	private void delete(File f) {
		for (File fl:f.listFiles()) {
			if (fl.isDirectory()) delete(fl);
			fl.delete();
		}
	}
	public String size() {
		return FileUtils.getReadableFileSize(file.length());
	}
}
