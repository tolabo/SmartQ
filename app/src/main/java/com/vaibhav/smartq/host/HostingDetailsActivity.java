package com.vaibhav.smartq.host;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vaibhav.smartq.MyVariables;
import com.vaibhav.smartq.R;
import com.vaibhav.smartq.model.QueueBean;

import static com.vaibhav.smartq.MyVariables.HOSTEDQUEUEDESC;
import static com.vaibhav.smartq.MyVariables.HOSTEDQUEUENAME;
import static com.vaibhav.smartq.MyVariables.HOSTQID;
import static com.vaibhav.smartq.MyVariables.ISQHOSTED;
import static com.vaibhav.smartq.MyVariables.TAG;
import static com.vaibhav.smartq.MyVariables.editor;
import static com.vaibhav.smartq.MyVariables.sharedPreferences;

public class HostingDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button b_hostQueue;
    private EditText et_name,et_desc,et_max;
    private DatabaseReference mref;
    private String qid;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostingdetails);
        toolbar = (Toolbar) findViewById(R.id.toolbar_hdetails);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);
        Log.i(TAG,"ISQHOSTED : "+sharedPreferences.getBoolean(ISQHOSTED,false));
        Log.i(TAG,"QID : "+sharedPreferences.getString(HOSTQID,null));

        if(sharedPreferences.getBoolean(ISQHOSTED,false)){
            startActivity(new Intent(this, HostTabsActivity.class));
            finish();
        }

        b_hostQueue = (Button) findViewById(R.id.b_hostQueue);
        et_name = (EditText) findViewById(R.id.et_name);
        et_desc = (EditText) findViewById(R.id.et_desc);
        et_max = (EditText) findViewById(R.id.et_max);

        qid = sharedPreferences.getString(HOSTQID,null);
        b_hostQueue.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.b_hostQueue : hostQueue(); break;

            default://
        }
    }

    private void hostQueue(){
        String qName = et_name.getText().toString().trim();
        String qDesc = et_desc.getText().toString().trim();
        String max = et_max.getText().toString().trim();
        if(qName.equals("") || qName.equals(null)){
            Toast.makeText(this, "Enter Valid Queue Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(qDesc.equals("") || qDesc.equals(null)){
            Toast.makeText(this, "Enter Valid Queue Description", Toast.LENGTH_SHORT).show();
            return;
        }
        if(max.equals("") || max.equals(null)){
            Toast.makeText(this, "Enter Valid Maximum Number", Toast.LENGTH_SHORT).show();
            return;
        }
        QueueBean queue = new QueueBean(qName,qDesc,Integer.parseInt(max),0);
        //Creating Queue Json in Firebase
        mref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child(qid);
        mref.setValue(queue);
        editor.putBoolean(ISQHOSTED,true);
        editor.putString(HOSTEDQUEUENAME,qName);
        editor.putString(HOSTEDQUEUEDESC,qDesc);
        editor.commit();
        //Creating TokensGenerator in Firebase
        mref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid).child("newtoken");
        mref.setValue(Long.valueOf(1));

        startActivity(new Intent(this,HostTabsActivity.class));
        finish();
    }
}
