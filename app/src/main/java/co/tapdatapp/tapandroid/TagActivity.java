package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Map;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapTag;
import co.tapdatapp.tapandroid.service.TapUser;
import co.tapdatapp.tapandroid.user.Account;


public class TagActivity extends Activity {
    private TapUser mTapUser;
    private String mDefaultYapaString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

//        Intent intent = getIntent();
        //TODO: error checking to make sure auth token is not expired
        mTapUser = TapCloud.getTapUser(this);
//        loadTags();

    }
    @Override
    public void onResume(){
        super.onResume();
        loadTags();
    }

    private void loadTags(){
        final Account account = new Account();
        if (account.created()) {
//            mTapUser.LoadUser(AccountActivity.this, mAuthToken);
            Map<String, String> mtagMap = mTapUser.myTagHash();
            // @TODO: this is incomplete and should handle zero tags gracefully
            if (mtagMap != null) {
                GridView gridview = (GridView) findViewById(R.id.gridView);
                ImageAdapter imgAdp = new ImageAdapter(this, mtagMap);


                gridview.setAdapter(imgAdp);
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        // Toast.makeText(TagActivity.this, "" + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                        writeThisTag(account.getAuthToken(), parent.getItemAtPosition(position).toString());


                    }
                });
            }
            // Toast.makeText(this, (CharSequence) mtagMap.toString(), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tag, menu);
        return true;
    }

    private void writeThisTag(String auth_token, String tag_id){
        if (!tag_id.isEmpty()){
            String[] mTagValues = tag_id.split(",");

            Intent i = new Intent(this, WriteActivity.class);
            i.putExtra("AuthToken", new Account().getAuthToken());
            i.putExtra("TagID",mTagValues[0]);
            i.putExtra("TagName",mTagValues[1]);

            startActivity(i);

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void makeNewTag(View view){
        mDefaultYapaString = new Account().getProfilePicThumbUrl();
        new NewTagTask().execute(mDefaultYapaString);
    }


    private class NewTagTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                TapTag tapTag = new TapTag();
                String authToken = new Account().getAuthToken();
                tapTag.generateNewTag(authToken, mDefaultYapaString);
                mTapUser.getTags(authToken);
                return mDefaultYapaString;  
            } catch (Exception e) {
                TapApplication.unknownFailure(e);
                return e.getMessage();
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            loadTags();
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private Map<String, String> mTagMapImg;
        private String[] mTagKeys;
        private String[] mTagValues;

        public ImageAdapter(Context c, Map<String, String> mTags){
            mTagMapImg = mTags;
            mTagKeys = new String[mTags.size()];
            mTagValues = new String[mTags.size()];
            int i  = 0;
            for(Map.Entry<String,String> entry : mTagMapImg.entrySet()){
                mTagKeys[i]= (entry.getKey());
                mTagValues[i] =  entry.getValue();
                i++;
            }

            mContext = c;

        }
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
           return mTagMapImg.size();

        }

        public Object getItem(int position) {
            return mTagKeys[position] + ", " + mTagValues[position];
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
//mThumbIds[position]
            imageView.setImageResource(R.drawable.logo);
            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.ic_launcher, R.drawable.ic_action_refresh,
                R.drawable.ic_action_new
            /*, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7*/
        };
    }


}


