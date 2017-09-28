package com.example.materialdesign.reminder.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.materialdesign.reminder.adapter.EventsAdapter;
import com.example.materialdesign.reminder.model.Category;
import com.example.materialdesign.reminder.model.Event;
import com.example.materialdesign.reminder.model.Item;
import com.example.materialdesign.reminder.model.ReminderDatabase;
import com.example.materialdesign.reminder.R;
import com.example.materialdesign.reminder.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class CategoryFragment extends Fragment{

    private  RecyclerView recyclerView;
    private  EventsAdapter adapter;

    private  List<Category> categories;
    private  List<Item> allItems = new ArrayList();
    private ReminderDatabase dbHandler;
    private TextView tv_show_message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!checkDatabase()){
            copyDatabase();
        }

        dbHandler = new ReminderDatabase(getContext());
        allItems = fillAllItems(false);
    }

    public List<Item> fillAllItems(boolean isEdit){
        List<Item> allItems = new ArrayList<>();
        categories = dbHandler.getAllCategory();

        for(int i=0;i<categories.size();i++){
            Category category = categories.get(i);
            Item categoryItem = new Item(category.getId(),category.getTitle(),category.getColor(),Category.CATEGORY_TYPE,isEdit);
            allItems.add(categoryItem);
            List<Event> events = dbHandler.getEventsByCategory(category.getTitle());

            for(int j=0;j<events.size();j++){
                Event e = events.get(j);
                    Item eventItem = new Item(e.getId(),e.getTitle(),e.getDescription(),e.getPlace(),e.getCategory(),e.getTime(),e.getDate(),categoryItem.getColor(),e.isShow(),Event.EVENT_TYPE,e.getNotify(),e.getRepeatMode(),e.getRepeatType(),e.getRepeatCount());
                    allItems.add(eventItem);
                }
            }
        return allItems;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_fragment, container, false);
        tv_show_message = (TextView) v.findViewById(R.id.tv_show_message);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        adapter = new EventsAdapter(getContext(),allItems,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        //show create event message
        showCreateEventMessage();
        return v;
    }

    public void showCreateEventMessage(){
        if(allItems.size()==0){
            tv_show_message.setVisibility(View.VISIBLE);
        }else{
            tv_show_message.setVisibility(View.GONE);
        }
    }

    public boolean checkDatabase(){
        String path = "/data/data/com.example.materialdesign.reminder/databases/";
        String filename = "Remind";
        File file = new File(path+filename);
        Log.d("Database","File exists -> "+file.exists());
        return file.exists();
    }

    public void copyDatabase(){
        String path = "/data/data/com.example.materialdesign.reminder/databases/Remind";
        ReminderDatabase dbHandler = new ReminderDatabase(getContext());
        dbHandler.getWritableDatabase();
        InputStream fin;
        OutputStream fout;
        byte[] bytes = new byte[1024];
        try {
            fin = getActivity().getAssets().open("Remind");
            fout = new FileOutputStream(path);
            int length=0;
            while((length = fin.read(bytes))>0){
                fout.write(bytes,0,length);
            }
            fout.flush();
            fout.close();
            fin.close();
            Log.d("Database","successfully copied database");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("Database","-Error" +e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Database","-Error" +e.getMessage());
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        Utils.hideKeyboard(getContext());
    }

    public void refresh(Item item){
        categories.clear();
        allItems.clear();
        List<Item> items = fillAllItems(false);
        allItems.addAll(items);
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(searchPosition(item));
        showCreateEventMessage();


    }

    public void refreshAfterRemovedCategory(){
        categories.clear();
        allItems.clear();
        List<Item> items = fillAllItems(false);
        allItems.addAll(items);
        adapter.notifyDataSetChanged();
        showCreateEventMessage();
    }

    public void editCategory(boolean isEdit){
        categories.clear();
        allItems.clear();
        List<Item> items = fillAllItems(isEdit);
        allItems.addAll(items);
        adapter.notifyDataSetChanged();
        showCreateEventMessage();
    }

    public int searchPosition(Item item){
        int pos = 0;
        for(int i=0;i<allItems.size();i++){
            if(item.getType() == allItems.get(i).getType() && item.getTitle().equals(allItems.get(i).getTitle())){
                pos = i;
            }
        }
        return pos;
    }

}
