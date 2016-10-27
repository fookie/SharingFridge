package android.assignment.sharingfridge;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FridgeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FridgeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FridgeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<FridgeItem> fridgeItemList;
    private GridLayoutManager gridLayoutManager;
    private FridgeViewAdapter fridgeViewAdapter;

    private OnFragmentInteractionListener mListener;

    public FridgeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FridgeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FridgeFragment newInstance(String param1, String param2) {
        FridgeFragment fragment = new FridgeFragment();
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
        fridgeItemList = initTestList();
        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        fridgeViewAdapter = new FridgeViewAdapter(getContext(), fridgeItemList, ((SharingFridgeApplication) getContext().getApplicationContext()).getServerAddr());
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fridge, container, false);

        RecyclerView fridgeView = (RecyclerView) v.findViewById(R.id.fridgeView);
        fridgeView.setHasFixedSize(true);
        fridgeView.setLayoutManager(gridLayoutManager);
        fridgeView.setAdapter(fridgeViewAdapter);

        return v;
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
            //Here might need a listener for UI update(referring to TimelineFragment)
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

    public List<FridgeItem> initTestList() {
        List<FridgeItem> list = new ArrayList<FridgeItem>();
        FridgeItem fi = new FridgeItem("Test Name", "10 days left", "111.gif");
        FridgeItem fi2 = new FridgeItem("Test Name", "3 days left", "SampleJPG.jpg");
        FridgeItem fi3 = new FridgeItem("Test Name", "1 days left", "1.png");
        FridgeItem fi4 = new FridgeItem("Test Name", "2 days left", "2.jpg");
        FridgeItem fi5 = new FridgeItem("Test Name", "2 days left", "3.jpg");
        FridgeItem fi6 = new FridgeItem("Test Name", "2 days left", "4.jpg");
        FridgeItem fi7 = new FridgeItem("Test Name", "2 days left", "5.jpg");
        FridgeItem fi8 = new FridgeItem("Test Name", "2 days left", "6.jpg");
        FridgeItem fi9 = new FridgeItem("Test Name", "2 days left", "7.jpg");
        list.add(fi);
        list.add(fi2);
        list.add(fi3);
        list.add(fi4);
        list.add(fi5);
        list.add(fi6);
        list.add(fi7);
        list.add(fi8);
        list.add(fi9);
        return list;
    }
}
