package andrews.fm;
import java.io.*;

public class EFileFilter
{
	public String Fpath_regex = null;
	public String Fext = null;
	public File Fmust_be_parent = null;
	public String FneedFileName = null;
	public String FneedDirName = null;
	public final boolean compare(File f)
	{
		if (Fpath_regex != null && f.getAbsolutePath().matches(Fpath_regex)) return true;
		if (Fext != null && FileUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase(Fext)) return true;
		if (Fmust_be_parent != null && f.getAbsolutePath().contains(Fmust_be_parent.getAbsolutePath())) return true;
		if (FneedFileName != null) return true;
		return false;
	}
	public EFileFilter set(int what, Object value)
	{
		switch (what)
		{
			case 1: {
					this.Fpath_regex = (String)value;
					break;
				}
			case 2: {
					this.Fext = (String)value;
					break;
				}
			case 3: {
					this.Fmust_be_parent = (File)value;
					break;
				}
			case 4: {
					this.FneedFileName = (String)value;
					break;
				}
			case 5: {
					this.FneedDirName = (String)value;
					break;
				}
		}
		return this;
	}
}
