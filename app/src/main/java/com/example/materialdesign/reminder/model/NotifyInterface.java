package com.example.materialdesign.reminder.model;


public interface NotifyInterface {
     void onInserted(Item item);
     void showExistingDialog(String title,String color);
}
