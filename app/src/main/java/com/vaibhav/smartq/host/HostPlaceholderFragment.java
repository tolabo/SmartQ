package com.vaibhav.smartq.host;

/**
 * Created by vaibhav on 10/22/2016.
 */

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vaibhav.smartq.MainActivity;
import com.vaibhav.smartq.MyVariables;
import com.vaibhav.smartq.R;
import com.vaibhav.smartq.model.ClientBean;
import com.vaibhav.smartq.utils.MyFileOperations;
import com.vaibhav.smartq.utils.MyFontTypeFaces;
import com.vaibhav.smartq.utils.MyUtilOperations;
import com.vaibhav.smartq.utils.QRCodeOperations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.vaibhav.smartq.MyVariables.HOSTEDQUEUEDESC;
import static com.vaibhav.smartq.MyVariables.HOSTEDQUEUENAME;
import static com.vaibhav.smartq.MyVariables.HOSTQID;
import static com.vaibhav.smartq.MyVariables.ISQHOSTED;
import static com.vaibhav.smartq.MyVariables.MYEMAIL;
import static com.vaibhav.smartq.MyVariables.TAG;
import static com.vaibhav.smartq.MyVariables.editor;
import static com.vaibhav.smartq.MyVariables.sharedPreferences;

/**
 * A placeholder fragment containing a simple view.
 */
public class HostPlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "TabNumber";
    private static final int REQUEST_ENABLEBT = 1001;
    private static final int REQUEST_DISCOVER = 1002;
    private static final int PERMISSION_WRITE_REQUEST = 1003;
    private static final int PERMISSION_READ_REQUEST = 1004;

    private static String qid;

    //Variables for MyQueue Tab
    private Button b_t_next;
    private TextView tv_t_token,tv_clientname,tv_clientemail;
    private static DatabaseReference mref;
    private DatabaseReference clientref;
    private static DatabaseReference timeref;
    private static DatabaseReference newTokenref;
    private static ValueEventListener mValueEventListener,newTokenValueEventListener;
    private int currentToken;
    private ClientBean clientBean;
    private  static int nextTokenNumber;

    //Variables for QR Code Tab
    private ImageButton ib_share;
    private ImageView iv_qr;
    private Bitmap qrBitmap;
    private CheckBox cb_bluetooth;

    private BluetoothAdapter myBTAdapter;
    private String defaultBluetoothName;

    int x1=0;
    private View.OnClickListener myOnClickListener;
    private CompoundButton.OnCheckedChangeListener myOnCheckedChangedListener;
    private static boolean isMValueEventListenerRemoved;
    private static boolean isnewTokenValueEventListenerRemoved;

    private List timeCheckPoints;
    private static Long initial,current;

    public HostPlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static HostPlaceholderFragment newInstance(int sectionNumber) {
        HostPlaceholderFragment fragment = new HostPlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initilizeMyOnClickListener();
        initilizeMyOnCheckChangedListener();
        timeCheckPoints = new ArrayList<Long>();
        switch(getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1 :
                View rootView1 = inflater.inflate(R.layout.fragment_host, container, false);
                qid = sharedPreferences.getString(HOSTQID,null);
                if(null==qid){
                    Log.i(TAG,"qid is NULL");
                    startActivity(new Intent(getActivity(),MainActivity.class));
                }
                currentToken= 0;
                tv_t_token = (TextView) rootView1.findViewById(R.id.tv_t_token);
                tv_t_token.setTypeface(MyFontTypeFaces.getNumberFont(getContext()));
                tv_clientname = (TextView) rootView1.findViewById(R.id.tv_clientname);
                tv_clientemail = (TextView) rootView1.findViewById(R.id.tv_clientemail);
                b_t_next = (Button) rootView1.findViewById(R.id.b_t_next);
                mref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child(qid).child("current");
                newTokenref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid).child("newtoken");
                timeref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid).child("avgTime");
                initilzeMyValueEventListener();
                mref.addValueEventListener(mValueEventListener);
                isMValueEventListenerRemoved = false;
                newTokenref.addValueEventListener(newTokenValueEventListener);
                isnewTokenValueEventListenerRemoved = false;
                b_t_next.setOnClickListener(myOnClickListener);

                return rootView1;
            case 2 :
                View rootView2 = inflater.inflate(R.layout.fragment_qr, container, false);
                iv_qr = (ImageView) rootView2.findViewById(R.id.iv_qr);
                ib_share = (ImageButton) rootView2.findViewById(R.id.ib_share);
                cb_bluetooth = (CheckBox) rootView2.findViewById(R.id.cb_bluetooth);
                cb_bluetooth.setOnCheckedChangeListener(myOnCheckedChangedListener);
                qid = sharedPreferences.getString(HOSTQID,null);
                if(null==qid){
                    Log.i(TAG,"qid is NULL");
                    Toast.makeText(getActivity(),"Qid is invalid",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(),MainActivity.class));
                }
                qrBitmap = QRCodeOperations.generateQrBitmap(qid);
                iv_qr.setImageBitmap(qrBitmap);
                ib_share.setOnClickListener(myOnClickListener);
                return rootView2;

        }
        return  null;
    }

    private void timeUpdate(){
        if(initial==null){
            initial = System.currentTimeMillis();
        }else{
            current = System.currentTimeMillis();
            timeCheckPoints.add((Long)(current-initial));
            initial = current;
            timeref.setValue(getAvgTime());
        }
    }

    private float getAvgTime(){
        Iterator iterator = timeCheckPoints.iterator();
        Long avg = Long.valueOf(0);
        while(iterator.hasNext()){
            avg+= (Long)iterator.next();
        }
        return (avg/timeCheckPoints.size());
    }

    private void initilizeMyOnCheckChangedListener() {
        myOnCheckedChangedListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(myBTAdapter==null){
                        myBTAdapter = BluetoothAdapter.getDefaultAdapter();
                    }
                    if(!myBTAdapter.isEnabled()){
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOn, REQUEST_ENABLEBT);
                    }else{
                        enableBluetoothDiscovery();
                    }
                }else{
                    myBTAdapter.cancelDiscovery();
                    myBTAdapter.setName(defaultBluetoothName);
                    myBTAdapter.disable();
                    Toast.makeText(getActivity(),"Disabled",Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLEBT){
            enableBluetoothDiscovery();
        }else if(requestCode == REQUEST_DISCOVER){
            Toast.makeText(getActivity(),"Enabled",Toast.LENGTH_SHORT).show();
        }
    }

    private void enableBluetoothDiscovery(){
        if(myBTAdapter.isDiscovering()){
            myBTAdapter.cancelDiscovery();
        }
        String qName = sharedPreferences.getString(HOSTEDQUEUENAME,null);
        String qid = sharedPreferences.getString(HOSTQID,null);
        String qDesc = sharedPreferences.getString(HOSTEDQUEUEDESC,null);
        defaultBluetoothName = myBTAdapter.getName();
        Log.d(TAG,"Default Name :"+defaultBluetoothName);
        myBTAdapter.setName(TAG+";"+qName+";"+qDesc+";"+ MyUtilOperations.encrypt(qid,qName));
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(discoverableIntent,REQUEST_DISCOVER);
    }

    private void initilzeMyValueEventListener() {
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!isMValueEventListenerRemoved) {
                    currentToken = (Integer) dataSnapshot.getValue(Integer.class);
                    tv_t_token.setText(currentToken + "");
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        };

        newTokenValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!isnewTokenValueEventListenerRemoved) {
                    HostPlaceholderFragment.nextTokenNumber = ((Long) dataSnapshot.getValue(Long.class)).intValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void initilizeMyOnClickListener() {
        myOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.b_t_next :
                        callNextToken();
                        break;
                    case R.id.ib_share :
                        if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,getContext(),getActivity())){
                            if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,getContext(),getActivity())){
                                sendEmail(MyFileOperations.generateUriForQrBitmap(qrBitmap));
                            }else{
                                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,PERMISSION_READ_REQUEST,getContext(),getActivity());
                            }
                        }else{
                            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,PERMISSION_WRITE_REQUEST,getContext(),getActivity());
                        }
                        break;
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterValueEventListeners();
    }

    private static void unRegisterValueEventListeners(){
        if(!isMValueEventListenerRemoved) {
            newTokenref.removeEventListener(newTokenValueEventListener);
            isMValueEventListenerRemoved = true;
        }
        if(isnewTokenValueEventListenerRemoved){
            mref.removeEventListener(mValueEventListener);
            isnewTokenValueEventListenerRemoved = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_WRITE_REQUEST :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,getContext(),getActivity())){
                        sendEmail(MyFileOperations.generateUriForQrBitmap(qrBitmap));
                    }else{
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,PERMISSION_READ_REQUEST,getContext(),getActivity());
                    }
                } else {
                    Toast.makeText(getActivity(),"Permission Denied For Write Access",Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSION_READ_REQUEST :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendEmail(MyFileOperations.generateUriForQrBitmap(qrBitmap));
                }else{
                    Toast.makeText(getActivity(),"Permission Denied For Read Access",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    public static boolean checkPermission(String strPermission, Context _c, Activity _a){
        int result = ContextCompat.checkSelfPermission(_c, strPermission);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public void requestPermission(String strPermission, int perCode, Context _c, Activity _a){

        if (ActivityCompat.shouldShowRequestPermissionRationale(_a,strPermission)){
            Toast.makeText(getContext(),"Needs External Storage Access To Share QR",Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
        } else {

            ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
        }
    }

    private void callNextToken(){
        if(isNextTokenValid()) {
            mref.setValue(currentToken + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    getClientDetails();
                }
            });
        }else{
            Toast.makeText(getActivity(),"No Clients in your queue\nPlease try later",Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(Uri qrFileUri){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/image");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{sharedPreferences.getString(MYEMAIL,null)});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "SmartQ QR");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Scan this QR for joining the queue");
        emailIntent.putExtra(Intent.EXTRA_STREAM, qrFileUri);
        startActivity(Intent.createChooser(emailIntent, "Share QR..."));
    }

    public static void deleteQueue(){
        unRegisterValueEventListeners();
        mref.getParent().removeValue();
        timeref =null;
        mref =null;
        newTokenref=null;
        initial=null;
        current = null;
        editor.putBoolean(ISQHOSTED,false);
        editor.putString(HOSTEDQUEUENAME,null);
        editor.putString(HOSTEDQUEUEDESC,null);
        editor.commit();
        mref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid);
        mref.removeValue();
    }

    private void getClientDetails(){
        clientref = null;
        clientref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid).child("clients").child(String.valueOf(currentToken));
        clientref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    timeUpdate();
                    clientBean = dataSnapshot.getValue(ClientBean.class);
                    tv_clientname.setText(clientBean.getClient_name());
                    tv_clientemail.setText(clientBean.getClient_email());
                }else{
                    callNextToken();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isNextTokenValid(){

        if(currentToken+1 < nextTokenNumber)
            return true;
        else
            return false;
    }

}