package com.example.materialdesign.reminder.activity;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.materialdesign.reminder.adapter.AllEventsAdapter;
import com.example.materialdesign.reminder.adapter.EventsAdapter;
import com.example.materialdesign.reminder.fragments.AllEventsFragment;
import com.example.materialdesign.reminder.fragments.CategoryDialogFragment;
import com.example.materialdesign.reminder.fragments.CategoryFragment;
import com.example.materialdesign.reminder.model.Category;
import com.example.materialdesign.reminder.model.Event;
import com.example.materialdesign.reminder.model.Item;
import com.example.materialdesign.reminder.model.NotifyInterface;
import com.example.materialdesign.reminder.model.ReminderDatabase;
import com.example.materialdesign.reminder.R;
import com.example.materialdesign.reminder.utils.AlarmReceiver;
import com.example.materialdesign.reminder.utils.Utils;
import com.example.materialdesign.reminder.view.ColorCircle;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import static com.example.materialdesign.reminder.utils.Utils.months;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,EventsAdapter.HideOrShowListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,NotifyInterface,AllEventsAdapter.HideOrShowListener {

    private Button btnAll,btnCategory;
    private ImageView ic_setting;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private boolean flag = false;
    private boolean isEdit = false;
    private boolean isNotify = true;
    private String repeatCount;
    private String repeatType;
    private long repeatTime;

    private static final int REPEAT_MODE_ON = 0;
    private static final int REPEAT_MODE_OFF = 1;
    private static final long milMinute = 60000L;
    private static final long milHour = 3600000L;

    private Item item;
    private ColorCircle categoryColorIcon;
    private TextView tvAddOrEdit,tvCategoryTitle,tvHour,tvMinute,tvDay,tvMonth,tvYear;
    private TextView tv_repeat_count,tv_repeat_type;
    private Switch repeat_control_switch;
    private EditText eventTitle,eventPlace,eventDetail;
    private ImageButton ic_notify;
    private int hour,minute,day,month,year;
    private ReminderDatabase dbHandler;
    private CategoryFragment categoryFragment;
    private AllEventsFragment allEventsFragment;
    private AlarmReceiver receiver;
    public TextView tvEdit,tvAddCategory;
    private boolean isEditMode = false;

    private String currentFragment;
    private Resources resources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //switch theme based on sharepreference
        switch (Utils.getThemeStyle(this)){
            case Utils.DARK_THEME:
                setTheme(R.style.AppThemeDark);
                break;
            case Utils.LIGHT_THEME:
                setTheme(R.style.AppThemeLight);
                break;
            default:
                break;
        }

        setContentView(R.layout.activity_main);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        btnAll = (Button) findViewById(R.id.btnAll);
        btnCategory = (Button) findViewById(R.id.btnCategory);
        ic_setting = (ImageView) findViewById(R.id.setting_icon);
        tvAddOrEdit = (TextView) findViewById(R.id.addoredit);
        tvCategoryTitle = (TextView) findViewById(R.id.tvCategoryTitle);
        eventTitle = (EditText) findViewById(R.id.eventTitle);
        eventPlace = (EditText) findViewById(R.id.eventPlace);
        eventDetail = (EditText) findViewById(R.id.eventDetail);
        categoryColorIcon = (ColorCircle) findViewById(R.id.categoryColorIcon);
        ic_notify = (ImageButton) findViewById(R.id.notifyBtn);
        tvHour = (TextView) findViewById(R.id.hour);
        tvMinute = (TextView) findViewById(R.id.minute);
        tvDay = (TextView) findViewById(R.id.day);
        tvMonth = (TextView) findViewById(R.id.month);
        tvYear = (TextView) findViewById(R.id.year);
        tvEdit = (TextView) findViewById(R.id.tvEdit);
        tvAddCategory = (TextView) findViewById(R.id.tvAddCategory);
        tv_repeat_count = (TextView) findViewById(R.id.tv_repeat_count);
        tv_repeat_type = (TextView) findViewById(R.id.tv_repeat_type);
        repeat_control_switch = (Switch) findViewById(R.id.repeat_control_switch);
        resources = getResources();

        dbHandler = new ReminderDatabase(getApplicationContext());
        receiver = new AlarmReceiver();

        btnAll.setOnClickListener(this);
        btnCategory.setOnClickListener(this);
        ic_setting.setOnClickListener(this);

        tvEdit.setOnClickListener(new ClickListener());
        tvAddCategory.setOnClickListener(new ClickListener());

        ImageButton ic_close = (ImageButton) findViewById(R.id.close_btn);
        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
            }
        });
        ic_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNotify){
                    ic_notify.setImageResource(R.drawable.ic_notifications_off);
                    isNotify = false;
                }else{
                    ic_notify.setImageResource(R.drawable.ic_notifications_active);
                    isNotify = true;
                }
            }
        });


        slidingUpPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

            }
        });
        if(slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }

        if(savedInstanceState!=null){
            //change last fragment to visible fragment
            int lastVisibleFragment = Utils.getLastFragment(this);
            viewFragment(lastVisibleFragment);
        }else{
            viewFragment(2);
        }

    }


    //show repeat dialog when click repeat interval layout
    public void showRepeatIntervalDialog(View view){
        final String[] items = new String[5];
        items[0] = "5 " + getResources().getString(R.string.Min);
        items[1] = "10 " + getResources().getString(R.string.Min);
        items[2] = "15 " + getResources().getString(R.string.Min);
        items[3] = "30 " + getResources().getString(R.string.Min);
        items[4] ="1 " + getResources().getString(R.string.Hour);

        // Create List Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                String item = items[position];
                String[] repeat_info = item.split(" ");
                repeatCount = repeat_info[0];
                repeatType = repeat_info[1];
                if(repeatType.equals(resources.getString(R.string.Min))){
                    repeatTime = Integer.parseInt(repeatCount) * milMinute;
                }else if(repeatType.equals(resources.getString(R.string.Hour))){
                    repeatTime = Integer.parseInt(repeatCount) * milHour;
                }

                tv_repeat_count.setText(repeatCount);
                tv_repeat_type.setText(repeatType);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }




    class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tvEdit:

                    if(!isEditMode){
                        categoryFragment.editCategory(true);
                    }else if(isEditMode){
                        categoryFragment.editCategory(false);
                    }
                    isEditMode = !isEditMode;
                    break;

                case R.id.tvAddCategory:
                    CategoryDialogFragment dialog = CategoryDialogFragment.getInstance(new Category(),false);
                    dialog.show(getSupportFragmentManager(),"ColorsDialog");
                    break;
            }
        }
    }

    class SearchClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //show search edittext
            allEventsFragment.showSearchLayout();
            tvEdit.setClickable(false);
        }
    }


    public void viewFragment(int position){
        switch (position){
            case 1:
                allEventsFragment = new AllEventsFragment();
                currentFragment = "AllEventsFragment";
                isEditMode = false;
                btnCategory.setSelected(false);
                btnAll.setSelected(true);
                btnAll.setOnClickListener(null);
                btnCategory.setOnClickListener(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.containerFragment, allEventsFragment)
                        .addToBackStack("AllEventsFragment")
                        .commit();

                tvEdit.setText(getResources().getString(R.string.search));
                tvEdit.setOnClickListener(new SearchClickListener());

                break;
            case 2:
                categoryFragment = new CategoryFragment();
                currentFragment = "CategoryFragment";
                btnCategory.setSelected(true);
                btnAll.setSelected(false);
                btnCategory.setOnClickListener(null);
                btnAll.setOnClickListener(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.containerFragment, categoryFragment)
                        .addToBackStack("CategoryFragment")
                        .commit();
                tvEdit.setText(getResources().getString(R.string.edit));
                tvEdit.setOnClickListener(new ClickListener());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAll:
                viewFragment(1);
                break;
            case R.id.btnCategory:
                viewFragment(2);
                break;
            case R.id.setting_icon:
                createSettingDialog();
                break;
            default:
                break;
        }
    }

    public void createSettingDialog(){
        int apptheme = Utils.getThemeStyle(this);
        String[] types_of_themes = resources.getStringArray(R.array.type_of_theme);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(resources.getString(R.string.choose_theme));
        builder.setSingleChoiceItems(types_of_themes, apptheme, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int theme) {
                //change theme
                Utils.updateThemeStyle(theme,MainActivity.this);
                //call recreate to apply changing theme
                MainActivity.this.recreate();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    //event listener from all events adapter
    @Override
    public void setHideOrShow(boolean isEdit, Item item) {
        showEditLayout(item,isEdit);
    }

    @Override
    public void deleteEventCallBack() {
        allEventsFragment.showCreateEventMessage();
    }

    //event listener from category and event adapter
    @Override
    public void setHideOrShow(Item item, boolean isEdit) {
            showEditLayout(item,isEdit);
    }

    public void showEditLayout(Item item, boolean isEdit){
        this.item = item;
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        slidingUpPanelLayout.setTouchEnabled(false);
        //clear text in all inputText
        eventTitle.setText("");
        eventPlace.setText("");
        eventDetail.setText("");

        if (isEdit) {
            tvCategoryTitle.setText(item.getCategory());
            tvAddOrEdit.setText("Edit Event");
            eventTitle.setHint(item.getTitle());
            eventPlace.setHint(item.getPlace());
            eventDetail.setHint(item.getDescription());
            categoryColorIcon.setColor(Color.parseColor(item.getColor()));

            if(item.getNotify().equals("on")){
                ic_notify.setImageResource(R.drawable.ic_notifications_active);
                isNotify = true;
            }else{
                ic_notify.setImageResource(R.drawable.ic_notifications_off);
                isNotify = false;
            }

            if(item.getRepeatMode().equals("on")){
                repeat_control_switch.setChecked(true);
            }else if(item.getRepeatMode().equals("off")){
                repeat_control_switch.setChecked(false);
            }

            tv_repeat_type.setText(item.getRepeatType());
            tv_repeat_count.setText(item.getRepeatCount());

            String[] time = item.getTime().trim().split(":");
            String[] date = item.getDate().trim().split("/");
            hour = Integer.parseInt(time[0]);
            minute = Integer.parseInt(time[1]);
            day = Integer.parseInt(date[1]);
            month = Integer.parseInt(date[0]);
            year = Integer.parseInt(date[2]);
            repeatType = item.getRepeatType();
            repeatCount = item.getRepeatCount();


        } else if (!isEdit) {
            tvCategoryTitle.setText(item.getTitle());
            tvAddOrEdit.setText("Create New Event");
            eventTitle.setHint("Title");
            eventPlace.setHint("Place");
            eventDetail.setHint("Detail");
            repeat_control_switch.setChecked(false);
            tv_repeat_type.setText(getResources().getString(R.string.Min));
            tv_repeat_count.setText("5");
            categoryColorIcon.setColor(Color.parseColor(item.getColor()));
            ic_notify.setImageResource(R.drawable.ic_notifications_active);
            isNotify = true;
            Calendar now = Calendar.getInstance();
            hour = now.get(Calendar.HOUR_OF_DAY);
            minute = now.get(Calendar.MINUTE);
            day = now.get(Calendar.DATE);
            month = now.get(Calendar.MONTH);
            year = now.get(Calendar.YEAR);
            repeatType = getResources().getString(R.string.Min);
            repeatCount = "5";

        }



        tvHour.setText(String.valueOf(hour < 12 ? hour : hour - 12));
        tvMinute.setText(String.valueOf(minute > 9 ? minute : "0" + minute));
        tvDay.setText(String.valueOf(day));
        tvMonth.setText(months[month]);
        tvYear.setText(String.valueOf(year));

        flag = true;
        this.isEdit = isEdit;
    }


    @Override
    public void showEditCategoryDialog(Category category) {
        CategoryDialogFragment dialog = CategoryDialogFragment.getInstance(category,true);
        dialog.show(getSupportFragmentManager(),"ColorsDialog");
    }

    @Override
    public void onBackPressed() {
        if(isEditMode){
            categoryFragment.editCategory(false);
            isEditMode = !isEditMode;
        }else if(flag){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            eventTitle.setText("");
            eventPlace.setText("");
            eventDetail.setText("");
            flag = false;
        }else{
            finish();
        }
    }

    //click date picker
    public void setDate(View v){
         DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                this,
                year,
                month,
                day
        );


        datePickerDialog.setThemeDark(true);
        datePickerDialog.show(getFragmentManager(),"DatePickerDialog");
    }

    //click time picker
    public void setTime(View v){
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                this,
                hour,
                minute,
                false
        );

        timePickerDialog.setThemeDark(true);
        timePickerDialog.show(getFragmentManager(),"TimePickerDialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        month = monthOfYear;
        day = dayOfMonth;
        this.year = year;
        tvDay.setText(String.valueOf(day));
        tvMonth.setText(months[month]);
        tvYear.setText(String.valueOf(year));
    }


    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        hour = hourOfDay;
        this.minute = minute;
        tvHour.setText(String.valueOf(hour<12?hour:hour-12));
        tvMinute.setText(String.valueOf(minute>9?minute: "0"+minute));

    }

   // save button click in event add layout
    public void saveEvent(View v){

        String title = eventTitle.getText().toString().trim();
        String place = eventPlace.getText().toString().trim();
        String detail = eventDetail.getText().toString().trim();

        if(!title.equals("") && title.length()!=0){
            title = title.substring(0,1).toUpperCase()+title.substring(1);

            //save event
            if(!isEdit){
                Event e = new Event(title,detail,place,item.getTitle(),minute>9? hour+":"+minute : hour+":0"+minute,month+"/"+day+"/"+year,Event.EVENT_TYPE,isNotify?"on":"off",repeat_control_switch.isChecked()?"on":"off",repeatType,repeatCount);
                int row = dbHandler.createNewEvent(e);
                Log.i("Row",row+"");
                Utils.showToastMessage("Event save",getApplicationContext());
                categoryFragment.refresh(new Item(e.getTitle(),"",Event.EVENT_TYPE,false));
                if(e.getNotify().equals("on")) {
                    if(e.getRepeatMode().equals("on")){
                        callBroadcastReceiver(row,REPEAT_MODE_ON);
                    }else{
                        callBroadcastReceiver(row,REPEAT_MODE_OFF);
                    }

                }

            //update event
            }else if(isEdit){
                Event event = new Event();
                event.setId(item.getId());
                event.setTitle(title);
                event.setPlace(place);
                event.setDescription(detail);
                event.setTime(minute>9? hour+":"+minute : hour+":0"+minute);
                event.setDate(month+"/"+day+"/"+year);
                event.setCategory(item.getCategory());
                event.setNotify(isNotify?"on":"off");
                event.setRepeatMode(repeat_control_switch.isChecked()?"on":"off");
                event.setRepeatType(repeatType);
                event.setRepeatCount(repeatCount);
                int row = dbHandler.updateEvent(event);
                Utils.showToastMessage("Event Edited",getApplicationContext());

                //refresh adapter based on current fragment
                if(currentFragment.equals("CategoryFragment")) {
                    categoryFragment.refresh(new Item(event.getTitle(), "", Event.EVENT_TYPE, false));
                }else{
                    allEventsFragment.refreshEvents();
                }
                //cancel old alarm
                receiver.cancelAlarm(getApplicationContext(),event.getId());

                //notify alarm
                if(event.getNotify().equals("on")) {
                    if(event.getRepeatMode().equals("on")){
                        callBroadcastReceiver(event.getId(),REPEAT_MODE_ON);
                    }else{
                        callBroadcastReceiver(event.getId(),REPEAT_MODE_OFF);
                    }
                }

                Log.i("ID",event.getId()+"  event.getId");
                Log.i("ID",row+"  row");

            }

            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            eventTitle.setText("");
            eventPlace.setText("");
            eventDetail.setText("");

        //show message when event title empty
        }else{
            Utils.showToastMessage("Title Cannot be empty",this);
        }

    }

    public void setEditMode(boolean mode){
        this.isEditMode = mode;
    }



    @Override
    public void onInserted(Item item) {
        tvEdit.setText("Edit");
        categoryFragment.refresh(item);
    }

    @Override
    public void showExistingDialog(final String title, final String color) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        builder.setTitle("Already Existing Category")
                .setMessage("Create Event?")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setHideOrShow(new Item(title,color, Category.CATEGORY_TYPE,false),false);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void callBroadcastReceiver(int row,int mode){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
        switch (mode){
            case REPEAT_MODE_ON:
                receiver.setRepeatAlarm(this,calendar,row,repeatTime);
                break;
            case REPEAT_MODE_OFF:
                receiver.setAlarm(getApplicationContext(),calendar,row);
                break;
        }
    }


}
