/**
 * Main tag management screen
 */

package co.tapdatapp.tapandroid.tags;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;

public class TagManagementActivity extends Activity implements SyncTagsTask.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_management);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new SyncTagsTask().execute(this);
    }

    /**
     * When the CREATE NEW TAG button is tapped, launch the new
     * tag Activity
     *
     * @param v button View object
     */
    public void btnNewTag(View v) {

    }

    @Override
    public void onTagsSynced() {
        // @TODO refresh the screen
    }

    @Override
    public void onTagSyncFailed(Throwable t) {
        // @TODO: friendlier error message
        TapApplication.unknownFailure(t);
    }
}
