package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapUser;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AccountFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TapUser mTapUser;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public AccountFragment() {
        // Required empty public constructor
    }
    public void setTapUser(TapUser tap_user){
        mTapUser = tap_user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

       // Toast.makeText(getActivity(), (CharSequence)("Howdy AccountFragment"), Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onResume(){
        super.onResume();
        MainActivity ma =  (MainActivity) getActivity();
        mTapUser=  ma.mTapUser;

        TextView nickName = (TextView)  getActivity().findViewById(R.id.etNickName);
        nickName.setText( mTapUser.getNickname());

        TextView email = (TextView)getActivity().findViewById(R.id.etEmail);
        String mEmailAddy = mTapUser.getEmail();
        if (mEmailAddy.equals("")){
            email.setText("your@email.addy");
        }else
        {
            email.setText(mTapUser.getEmail());

        }
        email.setEnabled(false);
        nickName.setEnabled(false);
        TextView balance = (TextView) getActivity().findViewById(R.id.txtBalance);
        balance.setText(  String.valueOf(mTapUser.getSatoshiBalance()) + " S");
      //  ImageView ivProfilePic = (ImageView) getActivity().findViewById(R.id.imageView);
        CircleImageView ivProfilePic = (CircleImageView) getActivity().findViewById(R.id.profile_image);
        String mThumb = mTapUser.getProfilePicThumb();
        if (mThumb.equals("") || mThumb.equals("null")){
            //do nothing or set it to some image?
            ivProfilePic.setImageResource(R.drawable.brienne);
        }
        else{
            //TODO: Check to see if we've already done this.. if not get it again
                 new TapCloud.DownloadImageTask(ivProfilePic)
                          .execute(mTapUser.getProfilePicThumb());

        }



    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_account, container, false);



    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {


            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
