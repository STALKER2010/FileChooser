package andrews.fm;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import andrews.fm.utils.FileComparator;
import andrews.fm.utils.FileUtils;
import andrews.fm.utils.MimeTypes;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public abstract class FMActivity extends ListActivity {
	protected Options mOpts = new Options();
	protected File mPath = mOpts.startDir;
	protected FMAdapter mAdapter;

	@Override
	public void onBackPressed() {
		if (!mPath.getAbsolutePath().equals("/"))
			setDirectory(mPath.getParentFile());
		else if (mOpts.showExitConfirmation) {
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle(mOpts.app_name);
			b.setMessage(R.string.quit_confirm);
			b.setPositiveButton(getString(android.R.string.yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							onCancel(null);
						}
					});
			b.setNegativeButton(getString(android.R.string.no), null);
			b.create().show();
		}
	}

	public final void choose() {
		AppCompat.setContext(this);
		mPath = mOpts.startDir;
		mAdapter = new FMAdapter(this, new ArrayList<File>());
		ListView listView = getListView();
		listView.setLongClickable(mOpts.allowFSModifying);
		if (mOpts.allowFSModifying) {
			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					view.setSelected(true);
					return AppCompat.createMenu(mAdapter.getItem(position));
				}
			});
		}
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				File sel = mAdapter.getItem(position);
				if (sel.isDirectory())
					setDirectory(sel);
				else {
					switch (mOpts.type) {
					case Options.TYPE_DIR:
						Intent intent = new Intent(Intent.ACTION_VIEW);
						final Uri apkuri = Uri.fromFile(sel);
						intent.setDataAndType(apkuri,
								new MimeTypes().getMimeType(apkuri));
						startActivity(intent);
						break;
					case Options.TYPE_FILE:
						onSelect(sel);
					default:
						break;
					}
				}
			}
		});
		setListAdapter(mAdapter);
		setDirectory(mPath);
	}

	protected void setDirectory(File newPath) {
		if (newPath.isDirectory()) {
			File[] filess = newPath.listFiles(fileFilter);
			if (filess == null)
				return;
			this.mPath = newPath;
			List<File> files = Arrays.asList(filess);
			Collections.sort(files, mOpts.sort);
			mAdapter.clear();
			for (File f : files) {
				mAdapter.add(f);
			}
			mAdapter.notifyDataSetChanged();
			AppCompat.setABSubtitle(mPath.getAbsolutePath());
		}
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mOpts.type == Options.TYPE_DIR) {
			MenuItem select = menu.add(R.string.select);
			select.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem p1) {
					onSelect(new File(mPath.getAbsolutePath()));
					return false;
				}
			});
			if (!AppCompat.isOld()) {
				select.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			}
		}
		return true;
	}

	public abstract void onSelect(File file);

	public void onCancel(Exception e) {
		finish();
	}

	/**
	 * Options class for FMActivity
	 */
	public class Options {
		public static final int TYPE_FILE = 0;
		public static final int TYPE_DIR = 1;
		public final Comparator<File> SORT_NAME = new FileComparator();
		public final Comparator<File> SORT_MODIFIED = new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				if (lhs.isDirectory() && rhs.isFile()) {
					return -1;
				} else if (lhs.isFile() && rhs.isDirectory()) {
					return 1;
				}
				return Long.valueOf(lhs.lastModified()).compareTo(
						Long.valueOf(rhs.lastModified()));
			}
		};
		public final Comparator<File> SORT_MODIFIED_INVERT = new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				if (lhs.isDirectory() && rhs.isFile()) {
					return -1;
				} else if (lhs.isFile() && rhs.isDirectory()) {
					return 1;
				}
				return Long.valueOf(rhs.lastModified()).compareTo(
						Long.valueOf(lhs.lastModified()));
			}
		};
		public Comparator<File> sort = SORT_NAME;
		public int type = Options.TYPE_FILE;
		public String app_name = "File Chooser";

		/**
		 * Allow renaming and deleting files or not.
		 */
		public boolean allowFSModifying = true;
		/**
		 * Directory to start browsing from.
		 */
		public File startDir = Environment.getExternalStorageDirectory();
		/**
		 * Extension for filtering files WITHOUT DOT!
		 */
		public String ext = "*";
		/**
		 * Shall we show confirmation dialog on exit?
		 */
		public boolean showExitConfirmation = true;
	}

	protected FilenameFilter fileFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return (mOpts.ext.equalsIgnoreCase("*")) ? true : name.toLowerCase(
					Locale.US).endsWith("." + mOpts.ext);
		}
	};

	public static class FMAdapter extends ArrayAdapter<File> {
		private List<File> mData;
		private LayoutInflater mInflater;
		private static int layout_id = R.layout.list_item;

		public FMAdapter(Context context, List<File> objects) {
			super(context, layout_id, objects);
			this.mData = objects;
			this.mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			if (v == null) {
				v = mInflater.inflate(layout_id, parent, false);
			}
			File cur = mData.get(position);
			TextView fname = (TextView) v.findViewById(R.id.file_name);
			TextView finfo = (TextView) v.findViewById(R.id.file_info);
			ImageView ficon = (ImageView) v.findViewById(R.id.file_icon);
			fname.setText(cur.getName());
			long lastModified = cur.lastModified();
			String info = DateFormat.getInstance().format(lastModified);
			info += "  ";
			if (cur.isFile()) {
				info += FileUtils.getReadableFileSize(cur.length());
			} else {
				info += "dir";
			}
			finfo.setText(info);
			ficon.setImageResource(cur.isDirectory() ? R.drawable.ic_folder
					: R.drawable.ic_file);
			return v;
		}
	}

	@SuppressLint("NewApi")
	protected static class AppCompat {
		protected static FMActivity mActivity;

		protected static void setContext(FMActivity activity) {
			mActivity = activity;
		}

		protected static void setABSubtitle(String str) {
			if (!isOld()) {
				mActivity.getActionBar().setSubtitle(str);
			}
		}

		protected static boolean createMenu(File mFile) {
			if (!isOld()) {
				return createMenuNew(mFile);
			} else {
				return createMenuOld(mFile);
			}
		}

		protected static boolean createMenuNew(File mFile) {
			if (FMActionCallback.mActionMode != null) {
				return false;
			}

			FMActionCallback.mActionMode = mActivity
					.startActionMode(new FMActionCallback(mFile));
			return true;
		}

		protected static boolean createMenuOld(final File mFile) {
			AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
			b.setTitle(mFile.getName());
			b.setItems(
					new CharSequence[] { mActivity.getString(R.string.rename),
							mActivity.getString(R.string.delete) },
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								rename(mFile);
							}
							if (which == 1) {
								delete(mFile);
							}
						}
					});
			b.show();
			return true;
		}

		public static void rename(final File mFile) {
			AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
			b.setTitle(mActivity.mOpts.app_name + " - "
					+ mActivity.getString(R.string.rename_title));
			LinearLayout layout = new LinearLayout(mActivity);
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(params);
			final EditText mField = new EditText(mActivity);
			mField.setLayoutParams(params);
			layout.addView(mField);
			b.setView(layout);
			b.setPositiveButton(mActivity.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						private static final String ReservedChars = "|\\?*<\":>+[]/'";

						@Override
						public void onClick(DialogInterface dialog, int which) {
							rename(mField.getText().toString());
							mActivity.setDirectory(mActivity.mPath);
						}

						public boolean rename(String newName) {
							for (char c : ReservedChars.toCharArray()) {
								newName = newName.replace(c, '_');
							}
							File parent = mFile.getParentFile();
							File newFile = new File(parent, newName);
							return mFile.renameTo(newFile);
						}
					});
			b.setNegativeButton(mActivity.getString(android.R.string.cancel),
					null);
			b.show();
		}

		public static void delete(final File mFile) {
			AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
			b.setTitle(mActivity.mOpts.app_name);
			b.setMessage(R.string.delete_confirm);
			b.setPositiveButton(mActivity.getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							delete(mFile);
							mActivity.setDirectory(mActivity.mPath);
						}

						public void delete(File file) {
							if (file.isDirectory()) {
								for (File c : file.listFiles()) {
									if (c.isDirectory())
										delete(c);
									c.delete();
								}
							}
							file.delete();
						}
					});
			b.setNegativeButton(mActivity.getString(android.R.string.cancel),
					null);
			b.show();
		}

		protected static boolean isOld() {
			return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
		}
	}
}
