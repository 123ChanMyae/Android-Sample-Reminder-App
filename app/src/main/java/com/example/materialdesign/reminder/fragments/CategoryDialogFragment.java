package com.example.materialdesign.reminder.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.example.materialdesign.reminder.adapter.ColorsAdapter;
import com.example.materialdesign.reminder.model.Category;
import com.example.materialdesign.reminder.model.Item;
import com.example.materialdesign.reminder.model.NotifyInterface;
import com.example.materialdesign.reminder.model.ReminderDatabase;
import com.example.materialdesign.reminder.R;
import com.example.materialdesign.reminder.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class CategoryDialogFragment extends DialogFragment {

    private Category category;
    private List<Colors> colors = new ArrayList<>();
    private GridView colorGrids;
    private EditText inputText;
    private int pos;
    private ColorsAdapter adapter;
    private boolean isError = true;
    private boolean isEdit;
    private NotifyInterface notifyInterface;
    private String[] color;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        notifyInterface = (NotifyInterface) context;
        color = context.getResources().getStringArray(R.array.colors);
    }

    public static CategoryDialogFragment getInstance(Category category,boolean isedit){
        CategoryDialogFragment fragment = new CategoryDialogFragment();
        Bundle bdn = new Bundle();
        bdn.putInt("ID",category.getId());
        bdn.putString("TITLE",category.getTitle());
        bdn.putString("COLOR",category.getColor());
        bdn.putBoolean("ISEDIT",isedit);
        fragment.setArguments(bdn);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bdn = getArguments();
        this.isEdit = bdn.getBoolean("ISEDIT");
        int id = bdn.getInt("ID");
        String title = bdn.getString("TITLE");
        String color = bdn.getString("COLOR");
        this.category = new Category(id,title,color);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_dialog,null);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        colorGrids = (GridView) v.findViewById(R.id.colorGrid);
        inputText = (EditText) v.findViewById(R.id.inputName);

        if(isEdit){
            pos = findPosition(category.getColor());
        }else{
            pos = new Random().nextInt(7);
        }
        addColors(pos);
        adapter = new ColorsAdapter(getActivity(),colors);
        colorGrids.setAdapter(adapter);


        colorGrids.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                colors.clear();
                addColors(position);
                adapter.notifyDataSetChanged();
                pos = position;
            }
        });

        inputText.setText(category.getTitle());
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(inputText.getText().toString().trim().length()>20){
                    inputText.setError("Name too long");
                    isError = true;
                }if(inputText.getText().toString().trim().length()==0) {
                    isError = true;
                    inputText.setError("Name Invalid");
                }else {
                    isError = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button btnSave = (Button) v.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputText.getText().toString().trim();
                if(text.length()==0 && isError){
                    inputText.setError("Name Invalid");
                    isError = true;
                //insert category into database
                }else if(text.length()!=0 &&!isError){
                    String title = text.substring(0,1).toUpperCase()+text.substring(1);
                    Item item = new Item(title,colors.get(pos).getName(),Category.CATEGORY_TYPE,false);
                    ReminderDatabase dbHandler = new ReminderDatabase(getContext());
                    boolean res = false;
                    if(!isEdit) {
                         res = dbHandler.createNewCategory(title, colors.get(pos).getName());
                        Utils.showToastMessage("New Category Created", getContext());
                    }else{
                        res = dbHandler.updateCategory(category.getId(),title,colors.get(pos).getName(),category.getTitle());
                        Utils.showToastMessage("Category Edited", getContext());
                        //get all events and change category
                        List<Item> items = dbHandler.getItemByCategory(category.getTitle());
                        for(Item event: items){
                            event.setCategory(title);
                            dbHandler.updateItem(event);
                        }
                    }
                    if(res) {
                        notifyInterface.onInserted(item);
                    }else{
                        notifyInterface.showExistingDialog(item.getTitle(),item.getColor());
                    }
                    getDialog().dismiss();
                    Utils.hideKeyboard(getContext(),inputText);
                }
            }
        });

        return v;
    }

    public void addColors(int position){
        for(int i=0; i<color.length;i++){
            Colors c = new Colors();
            c.setName(color[i]);
            c.setSelected(i==position?true:false);
            colors.add(c);
        }
    }

    public int findPosition(String c){
        int pos = 0;
        for (int i=0;i<color.length;i++){
            if(c.equals(color[i])){
                pos = i;
            }
        }
        return pos;
    }

    public class Colors{
        private String name;
        private boolean isSelected;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public boolean isSelected() {
            return isSelected;
        }
        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }


}

