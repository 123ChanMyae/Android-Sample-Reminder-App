package com.example.materialdesign.reminder.model;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.materialdesign.reminder.utils.AlarmReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReminderDatabase extends SQLiteOpenHelper{

    private static final String DB_NAME = "Remind";
    private static final String TB_CATEGORY = "Category";
    private static final String TV_EVENT = "Event";
    private SQLiteDatabase db;
    private Context context;

    public ReminderDatabase(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        String categoryTable = "CREATE TABLE IF NOT EXISTS " + TB_CATEGORY + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
//                "Title TEXT,Color TEXT)";
//        db.execSQL(categoryTable);
//        String eventTable = "CREATE TABLE IF NOT EXISTS " + TV_EVENT + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
//                "Title TEXT,Description TEXT,Place TEXT,Time TEXT,Date TEXT,Category TEXT,Def TEXT)";
//        db.execSQL(eventTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TB_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS "+ TV_EVENT);
        onCreate(db);
    }


    public List<Category> getAllCategory(){
        List<Category> categories = new ArrayList<>();
        db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from "+ TB_CATEGORY,null);
        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            Category category = new Category();
            category.setId(c.getInt(c.getColumnIndex("Id")));
            category.setTitle(c.getString(c.getColumnIndex("Title")));
            category.setColor(c.getString(c.getColumnIndex("Color")));
            category.setType(Category.CATEGORY_TYPE);
            categories.add(category);
        }
        db.close();
        Collections.sort(categories, new Comparator<Category>() {
            @Override
            public int compare(Category a, Category b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        return categories;
    }


    public List<Event> getEventsByCategory(String name){
        List<Event> events = new ArrayList<>();
        db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from "+TV_EVENT + " where Category=?",new String[]{name});
        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            Event event = new Event();
            event.setId(c.getInt(c.getColumnIndex("Id")));
            event.setTitle(c.getString(c.getColumnIndex("Title")));
            event.setDescription(c.getString(c.getColumnIndex("Description")));
            event.setPlace(c.getString(c.getColumnIndex("Place")));
            event.setTime(c.getString(c.getColumnIndex("Time")));
            event.setDate(c.getString(c.getColumnIndex("Date")));
            event.setNotify(c.getString(c.getColumnIndex("Notify")));
            event.setRepeatType(c.getString(c.getColumnIndex("RepeatType")));
            event.setRepeatCount(c.getString(c.getColumnIndex("RepeatNumber")));
            event.setRepeatMode(c.getString(c.getColumnIndex("RepeatMode")));
            event.setType(Event.EVENT_TYPE);
            String cat = c.getString(c.getColumnIndex("Category"));
            event.setCategory(cat);
            events.add(event);
        }
        db.close();
        return events;
    }


    public int createNewEvent(Event event){

        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Title",event.getTitle());
        cv.put("Description",event.getDescription());
        cv.put("Place",event.getPlace());
        cv.put("Time",event.getTime());
        cv.put("Date",event.getDate());
        cv.put("Category",event.getCategory());
        cv.put("Notify",event.getNotify());
        cv.put("RepeatType",event.getRepeatType());
        cv.put("RepeatNumber",event.getRepeatCount());
        cv.put("RepeatMode",event.getRepeatMode());
        long id = db.insert(TV_EVENT,null,cv);
        db.close();
        return (int)id;
    }


    public boolean createNewCategory(String title,String color){

        db = this.getWritableDatabase();
        //check if there is already existing category
        Cursor c = db.rawQuery("select * from "+ TB_CATEGORY + " where Title=?",new String[]{title});

        if(c.getCount()==0){
            ContentValues cv = new ContentValues();
            cv.put("Title",title);
            cv.put("Color",color);
            long id = db.insert(TB_CATEGORY,null,cv);
            db.close();
            return true;
        }else{
            return false;
        }
    }

    public boolean updateCategory(int id,String title,String color,String oldTitle){
        db = this.getWritableDatabase();

        Cursor c = db.rawQuery("select * from "+ TB_CATEGORY + " where Title=?",new String[]{title});

        if(c.getCount()==0 || c.getCount()==1 && title.toLowerCase().equals(oldTitle.toLowerCase())){
            ContentValues cv = new ContentValues();
            cv.put("Title",title);
            cv.put("Color",color);
            db.update(TB_CATEGORY,cv,"Id=?",new String[]{String.valueOf(id)});
            db.close();
            return true;
        }else{
            return false;
        }
    }

    public void removeCategory(int id,String name){
        db = this.getWritableDatabase();
        db.delete(TB_CATEGORY,"Id=?",new String[]{String.valueOf(id)});
        removeEventsByCategory(name);
        db.close();
    }


    public void removeEventsByCategory(String categoryName){
        Cursor c = db.rawQuery("select * from " + TV_EVENT + " where Category=?" ,new String[]{categoryName});
        if(c.getCount()!=0){
            for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
                int id = c.getInt(c.getColumnIndex("Id"));
                db.delete(TV_EVENT,"Id=?",new String[]{String.valueOf(id)});
                new AlarmReceiver().cancelAlarm(context,id);
            }
        }

    }

    public int updateEvent(Event event){
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Title",event.getTitle());
        cv.put("Description",event.getDescription());
        cv.put("Place",event.getPlace());
        cv.put("Time",event.getTime());
        cv.put("Date",event.getDate());
        cv.put("Category",event.getCategory());
        cv.put("Notify",event.getNotify());
        cv.put("RepeatType",event.getRepeatType());
        cv.put("RepeatNumber",event.getRepeatCount());
        cv.put("RepeatMode",event.getRepeatMode());
        int row = db.update(TV_EVENT,cv,"Id=?",new String[]{String.valueOf(event.getId())});
        db.close();
        return row;
    }

    public int updateItem(Item event){
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Title",event.getTitle());
        cv.put("Description",event.getDescription());
        cv.put("Place",event.getPlace());
        cv.put("Time",event.getTime());
        cv.put("Date",event.getDate());
        cv.put("Category",event.getCategory());
        cv.put("Notify",event.getNotify());
        cv.put("RepeatType",event.getRepeatType());
        cv.put("RepeatNumber",event.getRepeatCount());
        cv.put("RepeatMode",event.getRepeatMode());
        int row = db.update(TV_EVENT,cv,"Id=?",new String[]{String.valueOf(event.getId())});
        db.close();
        return row;
    }


    public void removeEvent(int id){
        db = this.getWritableDatabase();
        db.delete(TV_EVENT,"Id=?",new String[]{String.valueOf(id)});
        db.close();
    }

    public Event getEventId(int id){
        Event event = null;
        db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from "+ TV_EVENT + " where Id=?",new String[]{Integer.toString(id)} );
        if(c.getCount()==1){
            c.moveToFirst();
            event = new Event();
            event.setId(c.getInt(c.getColumnIndex("Id")));
            event.setTitle(c.getString(c.getColumnIndex("Title")));
            event.setDescription(c.getString(c.getColumnIndex("Description")));
            event.setPlace(c.getString(c.getColumnIndex("Place")));
            event.setTime(c.getString(c.getColumnIndex("Time")));
            event.setDate(c.getString(c.getColumnIndex("Date")));
            event.setNotify(c.getString(c.getColumnIndex("Notify")));
            event.setType(Event.EVENT_TYPE);
            String cat = c.getString(c.getColumnIndex("Category"));
            event.setCategory(cat);
            event.setRepeatType(c.getString(c.getColumnIndex("RepeatType")));
            event.setRepeatCount(c.getString(c.getColumnIndex("RepeatNumber")));
            event.setRepeatMode(c.getString(c.getColumnIndex("RepeatMode")));
        }
        db.close();
        return event;
    }

    public List<Item> getAllEvents(){
        List<Item> events = new ArrayList<>();
        List<Category> categories = getAllCategory();
        db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from "+ TV_EVENT, null);

        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            Item event = new Item();
            event.setId(c.getInt(c.getColumnIndex("Id")));
            event.setTitle(c.getString(c.getColumnIndex("Title")));
            event.setDescription(c.getString(c.getColumnIndex("Description")));
            event.setPlace(c.getString(c.getColumnIndex("Place")));
            event.setTime(c.getString(c.getColumnIndex("Time")));
            event.setDate(c.getString(c.getColumnIndex("Date")));
            event.setNotify(c.getString(c.getColumnIndex("Notify")));
            event.setType(Event.EVENT_TYPE);
            event.setRepeatType(c.getString(c.getColumnIndex("RepeatType")));
            event.setRepeatCount(c.getString(c.getColumnIndex("RepeatNumber")));
            event.setRepeatMode(c.getString(c.getColumnIndex("RepeatMode")));
            String cat = c.getString(c.getColumnIndex("Category"));
            event.setCategory(cat);
            event.setEdit(false);
            event.setShow(false);
            for(Category category : categories){
                if(event.getCategory().equals(category.getTitle())){
                    event.setColor(category.getColor());
                }
            }

            events.add(event);
        }
        db.close();
        Collections.sort(events, new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        return events;
    }

    public List<Item> getItemByCategory(String name){
        List<Item> events = new ArrayList<>();
        List<Category> categories = getAllCategory();
        db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from "+TV_EVENT + " where Category=?",new String[]{name});

        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            Item event = new Item();
            event.setId(c.getInt(c.getColumnIndex("Id")));
            event.setTitle(c.getString(c.getColumnIndex("Title")));
            event.setDescription(c.getString(c.getColumnIndex("Description")));
            event.setPlace(c.getString(c.getColumnIndex("Place")));
            event.setTime(c.getString(c.getColumnIndex("Time")));
            event.setDate(c.getString(c.getColumnIndex("Date")));
            event.setNotify(c.getString(c.getColumnIndex("Notify")));
            event.setType(Event.EVENT_TYPE);
            event.setRepeatType(c.getString(c.getColumnIndex("RepeatType")));
            event.setRepeatCount(c.getString(c.getColumnIndex("RepeatNumber")));
            event.setRepeatMode(c.getString(c.getColumnIndex("RepeatMode")));
            String cat = c.getString(c.getColumnIndex("Category"));
            event.setCategory(cat);
            event.setEdit(false);
            event.setShow(false);
            for(Category category : categories){
                if(event.getCategory().equals(category.getTitle())){
                    event.setColor(category.getColor());
                }
            }

            events.add(event);
        }
        db.close();
        return events;
    }


}
