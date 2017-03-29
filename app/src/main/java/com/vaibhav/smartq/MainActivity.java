package com.vaibhav.smartq;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.vaibhav.smartq.host.HostingDetailsActivity;
import com.vaibhav.smartq.join.ScanTabsActivity;
import com.vaibhav.smartq.utils.MyUtilOperations;

import static com.vaibhav.smartq.MyVariables.HOSTQID;
import static com.vaibhav.smartq.MyVariables.MYPREFERENCES;
import static com.vaibhav.smartq.MyVariables.TAG;
import static com.vaibhav.smartq.MyVariables.editor;
import static com.vaibhav.smartq.MyVariables.sharedPreferences;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int RC_SIGN_IN = 1;
    private TextView tv_status;
    private Button b_host,b_join;
    private FirebaseAuth auth;
    private Intent intent;
    private Toolbar toolbar;

    private void initilizeSharedPrefs(){
        if(null==sharedPreferences) {
            Log.i(TAG,"Initilizing Shared Preferences..");
            sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);
        initilizeSharedPrefs();
        intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        tv_status = (TextView) findViewById(R.id.tv_status);

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            //Already Signed In
            editor.putString(HOSTQID,MyUtilOperations.encodeEmail(auth.getCurrentUser().getEmail()));
            editor.putString(MyVariables.MYEMAIL,auth.getCurrentUser().getEmail());
            editor.putString(MyVariables.MYUSERNAME,auth.getCurrentUser().getDisplayName());
            editor.commit();
            tv_status.setText("Welcome "+auth.getCurrentUser().getDisplayName());
        }else{
            //Yet to sign in
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.AppTheme)
                    .setLogo(R.drawable.logo)
                    .setProviders(AuthUI.GOOGLE_PROVIDER).build(), 1);
        }

        b_host = (Button) findViewById(R.id.b_host);
        b_join = (Button) findViewById(R.id.b_join);

        b_host.setOnClickListener(this);
        b_join.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_signOut :
                signOut();
                return true;
            case R.id.action_AboutUs :
                MyUtilOperations.showAboutUsDialog(this);
                return true;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.b_host : host();  break;

            case R.id.b_join : join();  break;

            default://Nothing
        }
    }

    //Signout Code
    private void signOut(){
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i(TAG,"Signing Off...");
                editor.putString(MyVariables.MYEMAIL,null);
                editor.putString(HOSTQID,null);
                editor.putString(MyVariables.MYUSERNAME,null);
                editor.commit();
                startActivity(intent);
                finish();
            }
        });

    }
    //Hosting code
    private void host(){
        Intent intent = new Intent(this,HostingDetailsActivity.class);
        startActivity(intent);
    }
    //Join code
    private void join(){
        Intent intent = new Intent(this,ScanTabsActivity.class);
        startActivity(intent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            if(resultCode == RESULT_OK){
                Toast.makeText(this,"Login email : "+auth.getCurrentUser().getEmail(),Toast.LENGTH_SHORT).show();
                editor.putString(HOSTQID, MyUtilOperations.encodeEmail(auth.getCurrentUser().getEmail()));
                editor.putString(MyVariables.MYEMAIL,auth.getCurrentUser().getEmail());
                editor.putString(MyVariables.MYUSERNAME,auth.getCurrentUser().getDisplayName());
                editor.commit();
               // Toast.makeText(this, auth.getCurrentUser().getEmail().replaceAll("[^a-z0-9]", ""), Toast.LENGTH_LONG).show();
                Log.d(TAG,"QID : "+auth.getCurrentUser().getEmail().replaceAll("[^a-z0-9]", ""));
                tv_status.setText("Welcome "+auth.getCurrentUser().getDisplayName());
            }else{
                Log.d(TAG,"User Not Authenticated");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"No Network !\nPlease Check Your Internet Connection",Toast.LENGTH_LONG).show();
    }
}
