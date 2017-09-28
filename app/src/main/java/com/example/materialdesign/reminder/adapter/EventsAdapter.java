package com.example.materialdesign.reminder.adapter;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.materialdesign.reminder.fragments.CategoryFragment;
import com.example.materialdesign.reminder.activity.MainActivity;
import com.example.materialdesign.reminder.model.Category;
import com.example.materialdesign.reminder.model.Item;
import com.example.materialdesign.reminder.model.ReminderDatabase;
import com.example.materialdesign.reminder.R;
import com.example.materialdesign.reminder.utils.AlarmReceiver;
import com.example.materialdesign.reminder.utils.Utils;
import com.example.materialdesign.reminder.view.ColorCircle;

import java.util.ArrayList;
import java.util.List;

import static com.example.materialdesign.reminder.utils.Utils.months;

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<Item> allItems = new ArrayList();
    private HideOrShowListener hideOrShowListener;
    public static final int EVENT_TYPE = 1;
    public static final int CATEGORY_TYPE = 0;
    private int lastPosition;
    private boolean flag ;
    private ReminderDatabase dbHandler;
    private CategoryFragment categoryFragment;


    public EventsAdapter(Context context, List<Item> allItems, CategoryFragment fragment){
        this.context = context;
        hideOrShowListener =(HideOrShowListener) context;
        this.allItems = allItems;
        dbHandler = new ReminderDatabase(context);
        this.categoryFragment = fragment;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case CATEGORY_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.category_item,parent,false);
                return new CategoryViewHolder(view);
            case EVENT_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.events_item,parent,false);
                return new EventViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Item item = allItems.get(position);
        switch (item.getType()){
            case CATEGORY_TYPE:
                ((CategoryViewHolder)holder).tvCategoryTitle.setText(item.getTitle());
                ((CategoryViewHolder) holder).categoryColorIcon.setColor(Color.parseColor(item.getColor()));

                if(item.isEdit()){
                    ((CategoryViewHolder) holder).imgAddEvent.setVisibility(View.GONE);
                    ((CategoryViewHolder) holder).btnDeleteCat.setVisibility(View.VISIBLE);
                    ((CategoryViewHolder) holder).btnEditCat.setVisibility(View.VISIBLE);


                    ((CategoryViewHolder) holder).btnEditCat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Category category = new Category(item.getId(),item.getTitle(),item.getColor());
                            hideOrShowListener.showEditCategoryDialog(category);
                        }
                    });

                    ((CategoryViewHolder) holder).btnDeleteCat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //show confirm dialog to be sure
                            showConfirmDialog(item);


                        }
                    });

                }else{
                    ((CategoryViewHolder) holder).imgAddEvent.setVisibility(View.VISIBLE);
                    ((CategoryViewHolder) holder).btnDeleteCat.setVisibility(View.GONE);
                    ((CategoryViewHolder) holder).btnEditCat.setVisibility(View.GONE);

                    ((CategoryViewHolder)holder).imgAddEvent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideOrShowListener.setHideOrShow(item,false);
                        }
                    });
                }
                break;



            case EVENT_TYPE:
                String[] time = item.getTime().trim().split(":");
                String[] date = item.getDate().trim().split("/");
                int hour = Integer.parseInt(time[0]);

                ((EventViewHolder)holder).tvEventName.setText(item.getTitle());
                ((EventViewHolder)holder).tvTime.setText(hour<12?hour+" : "+time[1] +" am" : hour-12+" : "+time[1] +" pm" );
                ((EventViewHolder) holder).tvDate.setText(months[Integer.parseInt(date[0])] + " " + date[1]);
                ((EventViewHolder)holder).tvPlace.setText(item.getPlace());
                if(item.getDescription().length()==0) {
                    item.setDescription("No Detail");
                }
                ((EventViewHolder)holder).tvDescription.setText(item.getDescription());

                if(item.isShow()){

                    ((EventViewHolder) holder).tvDescription.setVisibility(View.VISIBLE);
                    ((EventViewHolder)holder).tvDescription.animate().alpha(1).setDuration(200).setInterpolator(new AccelerateInterpolator()).start();
                    ((EventViewHolder) holder).containerLayout.setSelected(true);
                    ((EventViewHolder)holder).tvEdit.setVisibility(View.VISIBLE);


                }else{
                    ((EventViewHolder) holder).tvDescription.setVisibility(View.GONE);
                    ((EventViewHolder)holder).tvDescription.animate().alpha(0).setDuration(500).start();
                    ((EventViewHolder) holder).containerLayout.setSelected(false);

                    ((EventViewHolder)holder).tvEdit.setVisibility(View.INVISIBLE);
                }

                ((EventViewHolder)holder).tvEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideOrShowListener.setHideOrShow(item,true);
                    }
                });

                break;
        }
    }

    public void showConfirmDialog(final Item item){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.confirm_sure);
        alertDialogBuilder.setMessage(R.string.delete_cofirm_message);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //find all events in deleted category and cancel all alarm
                        AlarmReceiver receiver = new AlarmReceiver();
                        String category = item.getCategory();
                        for(int i=0;i<allItems.size();i++){
                            Item item = allItems.get(i);
                            if(item.getType() == EVENT_TYPE){
                                if(item.getCategory().equals(category)){
                                    receiver.cancelAlarm(context,item.getId());
                                }
                            }
                        }
                        dbHandler.removeCategory(item.getId(),item.getTitle());
                        categoryFragment.refreshAfterRemovedCategory();
                        ((MainActivity)context).setEditMode(false);
                        Utils.showToastMessage("Deleted!",context);
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        Log.d("c",allItems.size()+"");
        return allItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(allItems!=null){
            return allItems.get(position).getType();
        }
        return 0;
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder{
        private TextView tvCategoryTitle;
        private ColorCircle categoryColorIcon;
        private ImageView imgAddEvent,btnDeleteCat,btnEditCat;
        public CategoryViewHolder(View itemView) {
            super(itemView);
            tvCategoryTitle = (TextView) itemView.findViewById(R.id.tvCategoryTitle);
            categoryColorIcon = (ColorCircle) itemView.findViewById(R.id.categoryColorIcon);
            imgAddEvent = (ImageView) itemView.findViewById(R.id.addEvent);
            btnDeleteCat = (ImageView) itemView.findViewById(R.id.imgDeleteCat);
            btnEditCat = (ImageView) itemView.findViewById(R.id.imgEditCat);
        }
    }

    public  class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        private LinearLayout descriptionLayout,containerLayout;
        private RelativeLayout eventContainer;
        private TextView tvEventName,tvTime,tvDate,tvPlace,tvDescription,tvEdit;
        private ColorCircle colorCircle;
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
            colorCircle = (ColorCircle) itemView.findViewById(R.id.color_circle);
            eventContainer.setOnClickListener(this);
            eventContainer.setOnCreateContextMenuListener(this);
            descriptionLayout.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if(flag){
                allItems.get(lastPosition).setShow(false);
            }
            allItems.get(getAdapterPosition()).setShow(true);
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
                switch (item.getItemId()){
                    case 0:
                        hideOrShowListener.setHideOrShow(allItems.get(getAdapterPosition()),true);
                        break;
                    case 1:
                        dbHandler.removeEvent(allItems.get(getAdapterPosition()).getId());
                        Utils.showToastMessage("Deleted",context);
                        new AlarmReceiver().cancelAlarm(context,allItems.get(getAdapterPosition()).getId());
                        allItems.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        break;
                }
                return true;
            }
        };
    }

    public interface HideOrShowListener{
        public void setHideOrShow(Item item , boolean isEdit);
        public void showEditCategoryDialog(Category category);
    }


}
