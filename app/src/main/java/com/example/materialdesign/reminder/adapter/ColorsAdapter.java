package com.example.materialdesign.reminder.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.materialdesign.reminder.fragments.CategoryDialogFragment;
import com.example.materialdesign.reminder.R;
import com.example.materialdesign.reminder.view.ColorCircle;

import java.util.List;

public class ColorsAdapter extends BaseAdapter {

    Context context;
    private List<CategoryDialogFragment.Colors> colors;

    public ColorsAdapter(Context context,List<CategoryDialogFragment.Colors>colors) {
        this.context = context;
        this.colors = colors;
    }

    @Override
    public int getCount() {
        return colors.size();
    }

    @Override
    public Object getItem(int position) {
        return colors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder{
        private ColorCircle colorCircle;

        public ViewHolder(View convertView) {
            colorCircle = (ColorCircle) convertView.findViewById(R.id.item);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vHolder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.color_items,parent,false);
            vHolder = new ViewHolder(convertView);
            convertView.setTag(vHolder);
        }else{
            vHolder = (ViewHolder) convertView.getTag();
        }

        if(colors.get(position).isSelected()){
            vHolder.colorCircle.setColorAndConer(Color.parseColor(colors.get(position).getName()),true);
        }else{
            vHolder.colorCircle.setColorAndConer(Color.parseColor(colors.get(position).getName()),false);
        }


        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return super.getDropDownView(position, convertView, parent);
    }
}
