package com.vaibhav.smartq.join;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vaibhav.smartq.MainActivity;
import com.vaibhav.smartq.MyVariables;
import com.vaibhav.smartq.R;
import com.vaibhav.smartq.model.QueueBean;
import com.vaibhav.smartq.utils.MyFontTypeFaces;
import com.vaibhav.smartq.utils.MyUtilOperations;

import static com.vaibhav.smartq.MyVariables.ISQJOINED;
import static com.vaibhav.smartq.MyVariables.JOINEDQTOKEN;
import static com.vaibhav.smartq.MyVariables.JOINQID;
import static com.vaibhav.smartq.MyVariables.TAG;
import static com.vaibhav.smartq.MyVariables.editor;
import static com.vaibhav.smartq.MyVariables.sharedPreferences;

public class JoiningActivity extends AppCompatActivity {

    private String qid;
    private DatabaseReference mref;
    private DatabaseReference timeref;
    private QueueBean queue;
    private int myToken;
    private int current;

    private static ValueEventListener mValueEventListener;
    private boolean isValueEventListenerRemoved;

    private static ValueEventListener timeValueEventListener;
    private boolean isTimeValueEventListenerRemoved;

    private TextView tv_qname,tv_qdescription,tv_current,tv_max,tv_mytoken,tv_waitime;
    private Toolbar toolbar;

    private void initilizeValueEventListener(){
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!isValueEventListenerRemoved) {
                    if(dataSnapshot.exists()) {
                        queue = dataSnapshot.getValue(QueueBean.class);
                        updateUI(queue);
                    }else{
                        updateUI(new QueueBean("Queue No Longer Valid","Please Leave this Queue",0,0));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        };

        timeValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!isValueEventListenerRemoved) {
                    if (dataSnapshot.exists()) {
                        if((current<myToken)) {
                            //TODO Debug Error Here.... :)
                            tv_waitime.setText(getAvgTime(dataSnapshot.getValue(Long.class)));
                        }else{
                            tv_waitime.setText("0 Minutes");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private String getAvgTime(Long milliseconds){
        return (((milliseconds/1000)/60) *(myToken-current))+" Minutes";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joining_new);
        toolbar = (Toolbar) findViewById(R.id.toolbar_join);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);
        qid = sharedPreferences.getString(JOINQID,null);
        if(null==qid){
            startActivity(new Intent(this,ScanTabsActivity.class));
        }

        myToken = sharedPreferences.getInt(JOINEDQTOKEN,-1);
        if(-1 == myToken){
            Log.i(TAG,"my Token is -1");
            Toast.makeText(this,"Scan the Code first for yout token id",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,ScanTabsActivity.class));
        }

        tv_qname = (TextView)findViewById(R.id.tv_qname_new);
        tv_qdescription = (TextView) findViewById(R.id.tv_qdescription_new);
        tv_current = (TextView) findViewById(R.id.tv_current_new);
        tv_max = (TextView) findViewById(R.id.tv_max_new);
        tv_mytoken = (TextView) findViewById(R.id.tv_mytoken_new);
        tv_waitime = (TextView)findViewById(R.id.tv_waittime);
        tv_mytoken.setTypeface(MyFontTypeFaces.getNumberFont(getBaseContext()));
        tv_current.setTypeface(MyFontTypeFaces.getNumberFont(getBaseContext()));
        tv_max.setTypeface(MyFontTypeFaces.getNumberFont(getBaseContext()));
        tv_mytoken.setText(myToken+"");
        initilizeValueEventListener();
        mref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child(qid);
        timeref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid).child("avgTime");
        timeref.addValueEventListener(timeValueEventListener);
        mref.addValueEventListener(mValueEventListener);
        isValueEventListenerRemoved = false;
        isTimeValueEventListenerRemoved = false;
    }

    private void updateUI(QueueBean queue){

        tv_qname.setText(queue.getName());
        tv_qdescription.setText(queue.getDescription());
        tv_current.setText(queue.getCurrent()+"");
        tv_max.setText(queue.getMax()+"");
        current = queue.getCurrent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterValueEventListernes();
    }

    private void unRegisterValueEventListernes(){
        if(!isValueEventListenerRemoved){
            mref.removeEventListener(mValueEventListener);
            isValueEventListenerRemoved=true;
        }
        if(!isTimeValueEventListenerRemoved){
            timeref.removeEventListener(timeValueEventListener);
            isTimeValueEventListenerRemoved = true;
        }
    }

    private void leaveQ(){
        clearLog();
        unRegisterValueEventListernes();
        editor.putBoolean(ISQJOINED,false);
        editor.putString(JOINQID,null);
        editor.putInt(JOINEDQTOKEN,-1);
        editor.commit();
        Toast.makeText(this,"Leaving Queue...",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void clearLog(){
        mref = null;
        mref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid).child("clients").child(String.valueOf(myToken));
        mref.removeValue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_leaveQueue :
                leaveQ();
                return true;
            case R.id.action_AboutUs_j :
                MyUtilOperations.showAboutUsDialog(this);
                return true;
        }

        return true;
    }
}
