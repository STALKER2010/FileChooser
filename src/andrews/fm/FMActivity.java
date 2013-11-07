package andrews.fm;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;
import andrews.fm.R;

public abstract class FMActivity extends ListActivity {
	protected File path = null;
	protected File[] cur = null;
	protected ActionBar ab;
	private final int LONGPRESS = 1;
	private int index = 0;
	public static final int TYPE_FILE = 0;
	public static final int TYPE_DIR = 1;
	protected Options opts = new Options();

	public void inflateList(File[] fs) {
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		Arrays.sort(fs, new FileComparator());
		for (File f : fs) {
			EFile fl = new EFile(f);
			Map<String, Object> listItem = new HashMap<String, Object>();
			if (fl.file.isDirectory()) {
				listItem.put("icon", R.drawable.ic_folder);
			} else {

				listItem.put("icon", R.drawable.ic_file);
			}
			listItem.put("filename", fl.file.getName());
			long modTime = fl.file.lastModified();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String sz = ((!fl.file.isDirectory()) ? " " + fl.size() : "");
			listItem.put("modify", dateFormat.format(new Date(modTime)) + sz);
			listItems.add(listItem);
		}
		SimpleAdapter adapter = new SimpleAdapter(FMActivity.this, listItems, R.layout.list_item, new String[] {
				"filename", "icon", "modify" }, new int[] { R.id.file_name, R.id.icon, R.id.file_modify });
		setListAdapter(adapter);
	}

	protected Dialog onCreateDialog(int id) {
		if (id == LONGPRESS && opts.allowFSModifying) {
			final File f = cur[index];
			return new AlertDialog.Builder(FMActivity.this).setItems(new String[] { "Rename", "Delete" },
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								final EditText et = new EditText(FMActivity.this);
								et.setText(f.getName());
								new AlertDialog.Builder(FMActivity.this)
										.setTitle(opts.app_name + ": New name")
										.setView(et)
										.setPositiveButton(getString(android.R.string.ok),
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														String newName = et.getText().toString();
														newName = f.getParentFile() + "/" + newName;
														f.renameTo(new File(newName));
														cur = path.listFiles();
														inflateList(cur);
													}
												}).setNegativeButton(getString(android.R.string.cancel), null).show();
								break;
							case 1:
								new AlertDialog.Builder(FMActivity.this)
										.setTitle(opts.app_name)
										.setMessage("Are you sure you want delete it?")
										.setPositiveButton(getString(android.R.string.ok),
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														delete(f);
														cur = path.listFiles();
														inflateList(cur);
													}

													public void delete(File file) {
														for (File c : file.listFiles()) {
															if (c.isDirectory())
																delete(c);
															c.delete();
														}
													}
												}).setNegativeButton(getString(android.R.string.cancel), null).show();
								break;
							}
						}
					}).create();
		}
		return null;
	}

	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
		if (paramInt == KeyEvent.KEYCODE_BACK)
			try {
				if (!path.getCanonicalPath().equals("/")) {
					path = path.getParentFile();
					cur = path.listFiles();
					inflateList(cur);
					if (opts.changeActionBar)
						ab.setSubtitle(path.getAbsolutePath());
				} else {
					AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
					localBuilder.setTitle(opts.app_name);
					localBuilder.setMessage("Are you sure you want to quit?");
					localBuilder.setPositiveButton(getString(android.R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
									onCancel(null);
									finish();
								}
							});
					localBuilder.setNegativeButton(getString(android.R.string.no), null);
					localBuilder.create().show();
				}
			} catch (Exception localException) {
			}
		return false;
	}

	public final void choose() {
		setContentView(R.layout.main);
		ab = getActionBar();
		path = opts.startDir;
		cur = path.listFiles();
		final ListView ls = getListView();
		if (opts.changeActionBar)
			ab.setSubtitle(path.getAbsolutePath());
		try {
			if (!opts.changeActionBar && opts.type == FMActivity.TYPE_DIR)
				throw new Exception("Impossible. We must change ActionBar for directory selection.");
		} catch (Exception e) {
			onCancel(e);
		}
		inflateList(cur);
		ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
				File sel = cur[p3];
				if (sel.isDirectory()) {
					path = sel;
					cur = path.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return (opts.ext.equalsIgnoreCase("*")) ? true : name.toLowerCase()
									.endsWith("." + opts.ext);
						}
					});
					inflateList(cur);
					if (opts.changeActionBar)
						ab.setSubtitle(path.getAbsolutePath());
				}
				if (sel.isFile()) {
					if (opts.type == FMActivity.TYPE_DIR) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						final Uri apkuri = Uri.fromFile(sel);
						intent.setDataAndType(apkuri, new MimeTypes().getMimeType(apkuri));
						startActivity(intent);
					} else if (opts.type == FMActivity.TYPE_FILE) {
						onSelect(sel);
					}
				}
			}
		});
		if (opts.allowFSModifying) {
			ls.setOnLongClickListener(new View.OnLongClickListener() {
				public boolean onLongClick(View p1) {
					index = ls.indexOfChild(p1);
					showDialog(LONGPRESS);
					return false;
				}
			});
		}
	}

	@SuppressLint("AlwaysShowAction")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (opts.type == FMActivity.TYPE_DIR) {
			menu.add("Select").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem p1) {
					onSelect(new File(path.getAbsolutePath()));
					return false;
				}
			}).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		return true;
	}

	public abstract void onSelect(File file);

	public abstract void onCancel(Exception e);

	public class Options {
		public int type = FMActivity.TYPE_FILE;
		public boolean changeActionBar = true;
		public boolean allowFSModifying = true;
		public boolean root = false;
		public File startDir = Environment.getExternalStorageDirectory();
		public String app_name = "File Chooser";
		public String ext = "*";
	}
}
