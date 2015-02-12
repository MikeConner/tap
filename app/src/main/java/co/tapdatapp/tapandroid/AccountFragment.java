package co.tapdatapp.tapandroid;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.user.Account;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AccountFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onResume(){
        super.onResume();

        TextView nickName = (TextView)  getActivity().findViewById(R.id.etNickName);
        nickName.setText(new Account().getNickname());

        TextView email = (TextView)getActivity().findViewById(R.id.etEmail);
        String mEmailAddy = new Account().getEmail();
        if (mEmailAddy.equals("")){
            email.setText("no@email.addy");
        }else
        {
            email.setText(mEmailAddy);

        }
        email.setEnabled(false);
        nickName.setEnabled(false);

        ImageView ivProfilePic = (ImageView) getActivity().findViewById(R.id.profile_image);
        String mThumb = new Account().getProfilePicThumbUrl();
        if (mThumb.isEmpty()){
            //do nothing or set it to some image?
            ivProfilePic.setImageResource(R.drawable.brienne);
        }
        else{
            //TODO: Check to see if we've already done this.. if not get it again
            new TapCloud.DownloadImageTask(ivProfilePic)
                    .execute(mThumb);

        }


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_account, container, false);


    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


}
