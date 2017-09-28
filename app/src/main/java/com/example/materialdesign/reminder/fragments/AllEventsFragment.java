package com.example.materialdesign.reminder.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.materialdesign.reminder.activity.MainActivity;
import com.example.materialdesign.reminder.adapter.AllEventsAdapter;
import com.example.materialdesign.reminder.model.Item;
import com.example.materialdesign.reminder.model.ReminderDatabase;
import com.example.materialdesign.reminder.R;

import java.util.ArrayList;
import java.util.List;


public class AllEventsFragment extends Fragment implements SearchView.OnQueryTextListener {

    private RecyclerView recyclerView;
    private AllEventsAdapter adapter;
    private List<Item> events;
    private ReminderDatabase dbHandler;
    //private LinearLayout show_message_layout;
    private RelativeLayout searchViewLayout;
    private List<Item> filterList;
    private SearchView searchView;
    private int viewHeight;
    private TextView tv_show_message;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHandler = new ReminderDatabase(getContext());
        events = dbHandler.getAllEvents();
        filterList = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.all_events_fragment, container, false);
        //show_message_layout = (LinearLayout) view.findViewById(R.id.show_message_layout);
        searchViewLayout = (RelativeLayout) view.findViewById(R.id.search_box_layout);
        searchView = (SearchView) searchViewLayout.findViewById(R.id.search_view);
        tv_show_message = (TextView) view.findViewById(R.id.tv_show_message);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        adapter = new AllEventsAdapter(getContext(),events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        showCreateEventMessage();

        searchViewLayout.setVisibility(View.GONE);


        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);


        ImageButton search_close_btn = (ImageButton) searchViewLayout.findViewById(R.id.search_btn_close);
        search_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getContext()).tvEdit.setClickable(true);
                hideViewAnim(searchViewLayout);
            }
        });

        return view;
    }


    public void refreshEvents(){
        events.clear();
        List<Item> allevents = dbHandler.getAllEvents();
        events.addAll(allevents);
        adapter.notifyDataSetChanged();
        showCreateEventMessage();
        //refresh searchview and clear query
        searchView.setQuery("",false);
        searchView.clearFocus();

    }



    public void showCreateEventMessage(){
        if(events.size()==0){
            tv_show_message.setVisibility(View.VISIBLE);
        }else{
            tv_show_message.setVisibility(View.GONE);
        }
    }

    public void showSearchLayout() {
        showViewAnim(searchViewLayout);
    }

    public void showViewAnim(final View view){
        view.setTranslationY(-126f);
        view.animate().translationY(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }
                })
                .setDuration(200).setInterpolator(new DecelerateInterpolator()).start();

//        recyclerView.animate().translationY(126f)
//                .setDuration(200).start();
    }

    public void hideViewAnim(final View view){
        view.animate().translationY(-(view.getHeight()))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        //clear query text
                        searchView.setQuery("",false);
                    }
                }).start();
//        recyclerView.animate().translationY(0)
//                .setDuration(200).start();

    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }


}
