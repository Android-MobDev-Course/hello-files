package com.mobdev.hellofiles;

import java.util.Random;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Marco Picone (picone.m@gmail.com) 20/03/2020
 * History Fragment to show stored data
 */
public  class HistoryFragment extends Fragment {

	private RecyclerView mRecyclerView = null;
	private LinearLayoutManager mLayoutManager = null;
	private MyAdapter mAdapter = null;
	private ImageButton addButton = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.history_fragment, container, false);
		
		mRecyclerView  = (RecyclerView)rootView.findViewById(R.id.my_recycler_view);

        // use a linear layout manager
        mLayoutManager  = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.scrollToPosition(0);

        mRecyclerView.setLayoutManager(mLayoutManager);
        
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        
        // specify an adapter (see also next example)
        mAdapter  = new MyAdapter(LogDescriptorManager.getInstance(getActivity()).getLogList(), getActivity());
        mRecyclerView.setAdapter(mAdapter);

        addButton  = (ImageButton)rootView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(MainActivity.TAG,"Add Button CLicked !");
				
				Random rand = new Random();
				int number = rand.nextInt((1000 - 0) + 1);
				LogDescriptorManager.getInstance(getActivity()).addLogToHead(new LogDescriptor(System.currentTimeMillis(),Double.valueOf(number)));
				mAdapter.notifyItemInserted(0);
				
				mLayoutManager.scrollToPosition(0);
			}
		});

		return rootView;
	}

	public void updateHistory(){
		if(mRecyclerView != null && mAdapter != null){
			mAdapter.setmDataset(LogDescriptorManager.getInstance(getActivity()).getLogList());
			mAdapter.notifyDataSetChanged();
		}
	}
	
}