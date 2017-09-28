package com.example.materialdesign.reminder.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.materialdesign.reminder.activity.MainActivity;
import com.example.materialdesign.reminder.R;

public class Utils {

    //global constants
    public static final int DARK_THEME = 0;
    public static final int LIGHT_THEME = 1;
    public static String[] months = {"Jan","Feb","Mar","Apr","May","Jun","July","Aug","Sep","Oct","Nov","Dec"};

    public static void showToastMessage(String str, Context context){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboard(Context context, View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    public static void hideKeyboard(Context context){
        ((MainActivity)context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    public static void updateThemeStyle(int theme,Context context){
        SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.user_pref),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(context.getResources().getString(R.string.theme_style),theme);
        editor.commit();
    }

    public static int getThemeStyle(Context context){
        SharedPreferences sharepref = context.getSharedPreferences(context.getResources().getString(R.string.user_pref),Context.MODE_PRIVATE);
        return sharepref.getInt(context.getResources().getString(R.string.theme_style),1); //default is dark theme
    }


    public static int getLastFragment(MainActivity activity){
        int index = activity.getSupportFragmentManager().getBackStackEntryCount()-1;
        String tag = activity.getSupportFragmentManager().getBackStackEntryAt(index).getName();
        if(tag.equals("AllEventsFragment")){
            return 1;
        }else{
            return 2;
        }
    }

}
