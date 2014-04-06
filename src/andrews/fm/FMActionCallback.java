package andrews.fm;

import java.io.File;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FMActionCallback implements ActionMode.Callback {
	static ActionMode mActionMode = null;
	private File mFile;

	public FMActionCallback(File f) {
		this.mFile = f;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.filechooser_edit_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_rename) {
			FMActivity.AppCompat.rename(mFile);
			mode.finish();
			return true;
		} else if (itemId == R.id.action_delete) {
			FMActivity.AppCompat.delete(mFile);
			mode.finish();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mActionMode = null;
	}
};