package co.tapdatapp.tapandroid;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import co.tapdatapp.tapandroid.service.TapCloud;


/**
 * A simple {@link Fragment} subclass.
 */
public class DataLoaderFragment extends Fragment {


    public DataLoaderFragment() {
        // Required empty public constructor
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                            Bundle savedInstanceState) {
//        //TextView textView = new TextView(getActivity());
//        //textView.setText(R.string.hello_blank_fragment);
//        //return textView;
//    }

    /**
     * Classes wishing to be notified of loading progress/completion
     * implement this.
     */
    public interface ProgressListener {
        /**
         * Notifies that the task has completed
         *
         * @param result Double result of the task
         */
        public void onCompletion(Double result);

        /**
         * Notifies of progress
         *
         * @param value int value from 0-100
         */
        public void onProgressUpdate(int value);
    }

    private ProgressListener mProgressListener;
    private Double mResult = Double.NaN;
    private LoadingTask mTask;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Keep this Fragment around even during config changes
        setRetainInstance(true);
    }

    /**
     * Returns the result or {@value Double#NaN}
     *
     * @return the result or {@value Double#NaN}
     */
    public Double getResult() {
        return mResult;
    }

    /**
     * Returns true if a result has already been calculated
     *
     * @return true if a result has already been calculated
     * @see #getResult()
     */
    public boolean hasResult() {
        return !Double.isNaN(mResult);
    }

    /**
     * Removes the ProgressListener
     *
     * @see #setProgressListener(ProgressListener)
     */
    public void removeProgressListener() {
        mProgressListener = null;
    }

    /**
     * Sets the ProgressListener to be notified of updates
     *
     * @param listener ProgressListener to notify
     * @see #removeProgressListener()
     */
    public void setProgressListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    /**
     * Starts loading the data
     */
    public void startLoading() {
        mTask = new LoadingTask();
        mTask.execute();
    }

    private class LoadingTask extends AsyncTask<Void, Integer, Double>
    {

        @Override
        protected Double doInBackground(Void... params) {


            MainActivity ma = null;

            while (ma==null) {
                ma = (MainActivity) getActivity();
                Log.v("trying","trying");
            }

            //Main Operation - get TapCloud, create user if one does not exist based on phone secret, log in / get auth token
            //Start of Tap Network Operations
            try {
                //shitty timing thing i have to do otheriwse it bombs out!  figure out what the race condition is here

                ma.mPreferences = ma.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);

                ma.mTapCloud = new TapCloud();

                //TODO: something funky going on here
                Thread.sleep(50);
                this.publishProgress(25);

                

                ma.mTapUser = TapCloud.getTapUser(getActivity());
                if (ma.mPreferences.contains("PhoneSecret")) {
                    ma.mPhoneSecret = ma.mPreferences.getString("PhoneSecret", "");
                }
                else {
                    ma.mPhoneSecret =  ma.mTapUser.generatePhoneSecret();
                    SharedPreferences.Editor editor = ma.mPreferences.edit();
                    editor.putString("PhoneSecret", ma.mPhoneSecret);
                    editor.commit();
                }
                // at this point we have a Phone Secret, let's try some network shit

                Thread.sleep(50);
                this.publishProgress(50);


                Boolean mNetwork = ma.mTapCloud.isNetworkAvailable(getActivity());
                if (mNetwork) {

                    if (ma.mPreferences.contains("AuthToken")) {
                        if (!ma.mPreferences.getString("AuthToken", "").isEmpty()){
                            ma.mAuthToken = ma.mPreferences.getString("AuthToken", "");
                            ma.mTapCloud.setAuthToken(ma.mAuthToken);
                            ma.mTapUser.LoadUser(ma.mAuthToken);

                            //TODO: Get Balance to get balance with all vouchers.
                            ma.mTapUser.getBalance(ma.mAuthToken);
                            //TODO: Store last used currency, set mode to that currency
                            ma.mTapUser.getTags(ma.mAuthToken);

                            //TODO: Failure case for when auth token has expired -> get error, get new auth token based on secret
                            //TODO: Failure case in case we can't get to tap or tap is down
                        }
                        else {
                            ma.mAuthToken =  ma.mTapUser.CreateUser(ma.mPhoneSecret);
                            SharedPreferences.Editor editor = ma.mPreferences.edit();
                            editor.putString("AuthToken", ma.mAuthToken);
                            editor.putString("NickName", ma.mTapUser.getNickname());
                            editor.commit();
                            ma.mTapCloud.setAuthToken(ma.mAuthToken);
                        }
                    }
                    else{
                        //Get Auth Token
                        ma.mAuthToken =  ma.mTapUser.CreateUser(ma.mPhoneSecret);
                        SharedPreferences.Editor editor = ma.mPreferences.edit();
                        editor.putString("AuthToken", ma.mAuthToken);
                        editor.putString("NickName", ma.mTapUser.getNickname());
                        editor.commit();


                        //We know user is null, but let's load user anyway to be consistent with above
                        ma.mTapCloud.setAuthToken(ma.mAuthToken);
                        ma.mTapUser.LoadUser(ma.mAuthToken);
                        ma.mTapUser.getBalance(ma.mAuthToken);
                        //TODO: Delete Auth Token on kill of application, so it gets a new one when it comes back OR NOT?
                    }
                }
                else {
                    Toast.makeText(getActivity(), (CharSequence) ("No NETWORK!  Going Home!"), Toast.LENGTH_SHORT).show();
                    //TODO: Code to send message, kill app, or figure out what to do next?
                }
                //end of Tap network Ops


                Thread.sleep(50);
                this.publishProgress(75);


                Thread.sleep(50);
                this.publishProgress(99);


            } catch (InterruptedException e) {
                return null;
            }

            return Double.valueOf(14);
        }

        @Override
        protected void onPostExecute(Double result) {
            mResult = result;
            mTask = null;
            if (mProgressListener != null) {
                mProgressListener.onCompletion(mResult);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mProgressListener != null) {
                mProgressListener.onProgressUpdate(values[0]);
            }
        }
    }
}
