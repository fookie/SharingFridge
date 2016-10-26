package android.assignment.sharingfridge;

import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MemberFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MemberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MemberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberFragment newInstance(String param1, String param2) {
        MemberFragment fragment = new MemberFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        List<MemberItem> memberItemList = initMemberList();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        MemberViewAdapter memberViewAdapter = new MemberViewAdapter(getContext(), memberItemList, ((SharingFridgeApplication) getContext().getApplicationContext()).getServerAddr());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_member, container, false);

        RecyclerView memberView = (RecyclerView) view.findViewById(R.id.memberView);
        memberView.setHasFixedSize(true);
        memberView.setLayoutManager(gridLayoutManager);
        memberView.setAdapter(memberViewAdapter);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
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
        void onFragmentInteraction(Uri uri);
    }

    public List<MemberItem> initMemberList() {
        List<MemberItem> memberItems = new LinkedList<>();
        memberItems.add(new MemberItem("Makun1", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun2", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun3", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun4", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun5", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun6", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun7", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun8", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun9", "Prefers curry to curried shit", "makun.jpg"));
        memberItems.add(new MemberItem("Makun0", "Prefers curry to curried shit", "makun.jpg"));
        return memberItems;
    }

}
