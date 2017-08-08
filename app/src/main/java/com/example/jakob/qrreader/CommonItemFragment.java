package com.example.jakob.qrreader;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommonItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CommonItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommonItemFragment extends Fragment {

    private static final String TYPE = "type";
    private static final String DATA = "data";

    private String type;
    private String data;

    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private RecyclerAdapterCommon mAdapter;
    private RecyclerAdapterCommonAlerts mAdapterAlerts;
    private RecyclerView.LayoutManager mLayoutManager;

    public CommonItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CommonItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommonItemFragment newInstance(String param1, String param2) {
        CommonItemFragment fragment = new CommonItemFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, param1);
        args.putString(DATA, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(TYPE);
            data = getArguments().getString(DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_common_item, container, false);

        // recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.common_recycler_view);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // get data to display
        JSONArray items = null;
        if (data != null) {
            try {
                items = new JSONArray(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // specify an adapter (see also next example)
        if (type.equals("alerts")) {
            mAdapterAlerts = new RecyclerAdapterCommonAlerts(items);
            mRecyclerView.setAdapter(mAdapterAlerts);
        } else if (type.equals("cargo")) {
            mAdapter = new RecyclerAdapterCommon(items);
            mRecyclerView.setAdapter(mAdapter);
        }

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
}
