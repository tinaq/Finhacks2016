package com.example.reimu.finhacks2016;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    LinearLayout chat;
    ScrollView scroll;
    double balance=0;
    double goal =0;
    double contributed =0;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatlayout);
        final EditText editText = (EditText)findViewById(R.id.editText);
        final RelativeLayout entireChatScreen = (RelativeLayout)findViewById(R.id.whole);
        chat = (LinearLayout) findViewById(R.id.chat);
        scroll = (ScrollView)findViewById(R.id.scroll);
        scroll.fullScroll(View.FOCUS_DOWN);
        init();
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String s = v.getText().toString();
                    if(s!="") {
                        editText.setText("");
                        displaySentMessage(s);
                        sendUserMessage(s);
                        handled = true;
                    }
                }
                return handled;
            }
        });
        entireChatScreen.post(new Runnable()
        {
            @Override
            public void run() {
                editText.getLayoutParams().height = entireChatScreen.getHeight()*1/6;

            }
        });


    }
    protected void init(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            String res;
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object ob =  snapshot.child("account").child("balance").getValue();
                    if (ob == null) {
                        mDatabase.child("account").child("balance").setValue(0);
                    }
                    else {
                        balance = (((Number) snapshot.child("account").child("balance").getValue())).doubleValue();
                    }
                ob =  snapshot.child("goal").child("goal").getValue();
                if (ob == null) {
                    mDatabase.child("goal").child("goal").setValue(0);
                }
                else {
                    goal = (((Number) snapshot.child("goal").child("goal").getValue())).doubleValue();
                }
                ob =  snapshot.child("goal").child("contributed").getValue();
                if (ob == null) {
                    mDatabase.child("goal").child("contributed").setValue(0);
                }
                else {
                    contributed = (((Number) snapshot.child("goal").child("contributed").getValue())).doubleValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    protected void displaySentMessage(String s){
        s = addNewline(s);
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(Color.BLACK);
        t.setPadding(10,10,10,10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        t.setBackgroundResource(R.drawable.usermessagebg);
        chat.addView(t,params);
        scroll.fullScroll(View.FOCUS_DOWN);
    }
    protected void displayReceivedMessaged(String s){
       s = addNewline(s);
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(Color.WHITE);
        t.setPadding(10,10,10,10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.LEFT;
        t.setBackgroundResource(R.drawable.botmessagebg);
        chat.addView(t,params);
        scroll.fullScroll(View.FOCUS_DOWN);
    }

    protected String addNewline(String s){
        double count = 0;
        double charCount= 0;
        int len = s.length();
        for (int i = 0; i < len; i++){
            if(s.charAt(i)==' '){
                count++;
                continue;
            }
            charCount ++;
            if(count==4 ){
                s = s.substring(0, i) + "\n" + s.substring(i,len);
                count=0;
                charCount =0;
                continue;
            }
            if(charCount ==20){
                s = s.substring(0, i) + "\n" + s.substring(i,len);
                count=0;
                charCount =0;
            }
        }
        return s;
    }
    protected void sendUserMessage(String s){
        s = s.toLowerCase();
        String reply = "";
        if(s.contains("contribute")||s.contains("add")|| s.contains("deposit")||s.contains("save")){
            double add = Double.parseDouble(s.replaceAll("[\\D]", ""));
            String money = String.valueOf(add);
            if(money.equals("")){
                reply="Please enter a number amount. Eg $10.00";
                return;
            }
            if(balance-add <0){
                reply = "You do not have enough balance to add $"+add+" towards your goal";
                return;
            }
                else{
                updateContribution(add);
                reply = "You've added $"+add+" towards goal."+" You have $"+(goal-contributed)+" until you reach your goal of $"+goal+
                        ". Your current balance is $"+balance;
                return;
            }
        }
        if(s.contains("balance")){
            reply = "Your balance is $"+balance;
        }
        displayReceivedMessaged(reply);
    }
//database
protected void updateBalance(double num){
    balance +=num;
    mDatabase.child("account").child("balance").setValue(balance);
}
    protected void updateContribution(double num){
        balance+=num;
        contributed+=num;
        mDatabase.child("account").child("balance").setValue(balance);
        mDatabase.child("goal").child("contributed").setValue(contributed);
    }
    protected void setGoal(double num){
        goal = num;
        mDatabase.child("goal").child("contributed").setValue(goal);
    }
}
