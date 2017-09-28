package com.example.materialdesign.reminder.adapter;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.materialdesign.reminder.model.Item;
import com.example.materialdesign.reminder.model.ReminderDatabase;
import com.example.materialdesign.reminder.R;
import com.example.materialdesign.reminder.utils.AlarmReceiver;
import com.example.materialdesign.reminder.utils.Utils;
import com.example.materialdesign.reminder.view.ColorCircle;

import java.util.ArrayList;
import java.util.List;

import static com.example.materialdesign.reminder.utils.Utils.months;

public class AllEventsAdapter extends RecyclerView.Adapter<AllEventsAdapter.EventViewHolder> implements Filterable{

    private Context context;
    private List<Item> allItemList = new ArrayList();
    private List<Item> filterList = new ArrayList<>();
    private HideOrShowListener hideOrShowListener;
    public static final int EVENT_TYPE = 1;
    private int lastPosition;
    private boolean flag = false;
    private ReminderDatabase dbHandler;


    public AllEventsAdapter(Context context, List<Item> allItems) {
        this.context = context;
        hideOrShowListener = (HideOrShowListener) context;
        this.allItemList = allItems;
        filterList = allItems;
        dbHandler = new ReminderDatabase(context);
    }


    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.default_events_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        final Item item = filterList.get(position);

        String[] time = item.getTime().trim().split(":");
        String[] date = item.getDate().trim().split("/");
        int hour = Integer.parseInt(time[0]);

         holder.tvEventName.setText(item.getTitle());
         holder.colorCircleView.setColor(Color.parseColor(item.getColor()));

         holder.tvTime.setText(hour < 12 ? hour + " : " + time[1] + " am" : hour - 12 + " : " + time[1] + " pm");
         holder.tvDate.setText(months[Integer.parseInt(date[0])] + " " + date[1]);
         holder.tvPlace.setText(item.getPlace());
        if (item.getDescription().length() == 0) {
            item.setDescription(context.getResources().getString(R.string.no_detail));
        }
         holder.tvDescription.setText(item.getDescription());

         holder.tvDescription.setVisibility(View.VISIBLE);
         holder.tvDescription.animate().alpha(1).setDuration(200).setInterpolator(new AccelerateInterpolator()).start();
         holder.tvEdit.setVisibility(View.VISIBLE);


         holder.tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideOrShowListener.setHideOrShow(true, item);
            }
        });


    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString();
                if(query.isEmpty()){
                    filterList = allItemList;
                }else{
                    ArrayList<Item> filterItem = new ArrayList<>();
                    for(Item item : allItemList){
                        if(item.getTitle().toLowerCase().contains(query.toLowerCase()) || item.getDescription().toLowerCase().contains(query.toLowerCase())){
                            filterItem.add(item);
                        }
                    }
                    filterList = filterItem;
                }

                FilterResults results = new FilterResults();
                results.values = filterList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filterList = (List<Item>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        private LinearLayout descriptionLayout, containerLayout;
        private RelativeLayout eventContainer;
        private TextView tvEventName, tvTime,tvDate, tvPlace, tvDescription, tvEdit;
        private ColorCircle colorCircleView;

        public EventViewHolder(View itemView) {
            super(itemView);
            descriptionLayout = (LinearLayout) itemView.findViewById(R.id.descriptionLayout);
            containerLayout = (LinearLayout) itemView.findViewById(R.id.container);
            eventContainer = (RelativeLayout) itemView.findViewById(R.id.eventContainer);
            tvEventName = (TextView) itemView.findViewById(R.id.tvEventName);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvPlace = (TextView) itemView.findViewById(R.id.tvPlace);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvEdit = (TextView) itemView.findViewById(R.id.tvEdit);
            colorCircleView = (ColorCircle) itemView.findViewById(R.id.color_circle);
            eventContainer.setOnClickListener(this);
            eventContainer.setOnCreateContextMenuListener(this);
            descriptionLayout.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (flag) {
                filterList.get(lastPosition).setShow(false);
            }
            filterList.get(getAdapterPosition()).setShow(true);
            flag = true;
            lastPosition = getAdapterPosition();
            notifyDataSetChanged();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Choose");
            MenuItem Edit = menu.add(Menu.NONE,0,0,context.getResources().getString(R.string.menu_edit));
            MenuItem Delete = menu.add(Menu.NONE,1,0,context.getResources().getString(R.string.menu_delete));
            Edit.setOnMenuItemClickListener(onEditMenu);
            Delete.setOnMenuItemClickListener(onEditMenu);
        }

        private MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:
                        hideOrShowListener.setHideOrShow(true, filterList.get(getAdapterPosition()));
                        break;
                    case 1:
                        dbHandler.removeEvent(filterList.get(getAdapterPosition()).getId());
                        Utils.showToastMessage("Deleted", context);
                        new AlarmReceiver().cancelAlarm(context, filterList.get(getAdapterPosition()).getId());
                        Item deleteItem = filterList.get(getAdapterPosition());
                        filterList.remove(getAdapterPosition());
                        //refresh original datalist
                        for(int i=0;i<allItemList.size();i++){
                            if(deleteItem.getId() == allItemList.get(i).getId()){
                                allItemList.remove(i);
                            }
                        }
                        notifyItemRemoved(getAdapterPosition());
                        hideOrShowListener.deleteEventCallBack();

                        break;
                }
                return true;
            }
        };
    }



    public interface HideOrShowListener {
        void setHideOrShow(boolean isEdit, Item item);
        void deleteEventCallBack();
    }


}
