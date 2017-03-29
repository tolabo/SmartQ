package com.vaibhav.smartq.join;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.vaibhav.smartq.MyVariables;
import com.vaibhav.smartq.R;
import com.vaibhav.smartq.model.ClientBean;
import com.vaibhav.smartq.model.QueueListItem;
import com.vaibhav.smartq.utils.MyUtilOperations;
import com.vaibhav.smartq.utils.QRCodeOperations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.vaibhav.smartq.MyVariables.ISQJOINED;
import static com.vaibhav.smartq.MyVariables.JOINEDQTOKEN;
import static com.vaibhav.smartq.MyVariables.JOINQID;
import static com.vaibhav.smartq.MyVariables.TAG;
import static com.vaibhav.smartq.MyVariables.editor;
import static com.vaibhav.smartq.MyVariables.sharedPreferences;


/**
 * Created by vaibhav on 11/5/2016.
 */

public class ScanPlaceholderFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "TabNumber";

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_ENABLEBT = 1001;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1002;
    private static final int PERMISSION_READ_REQUEST = 1003;
    private Button b_scan,b_browse;
    private String qid;
    private DatabaseReference mref;
    private int token;

    private BluetoothAdapter mBTAdapter;
    private TextView tv_bstatus;
    private Button b_bscan;
    private List<QueueListItem> queueList;
    private ArrayAdapter<QueueListItem> myArrayAdapter;
    private ListView lv_blist;
    private boolean isReceiverRegistered;
    private ProgressBar loadingProgressBar;

    private View.OnClickListener myOnClickListener;

    public ScanPlaceholderFragment(){
    }

    public static ScanPlaceholderFragment newInstance(int sectionNumber){
        ScanPlaceholderFragment fragment = new ScanPlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initilizeOnClickListener();
        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1 :
                View rootView1 = inflater.inflate(R.layout.fragment_scan_qr, container, false);
                b_scan = (Button) rootView1.findViewById(R.id.b_scan);
                b_browse = (Button) rootView1.findViewById(R.id.b_browse);

                b_scan.setOnClickListener(myOnClickListener);
                b_browse.setOnClickListener(myOnClickListener);
                return rootView1;
            case 2 :
                View rootView2 = inflater.inflate(R.layout.fragment_bluetooth, container, false);
                tv_bstatus = (TextView) rootView2.findViewById(R.id.tv_bstatus);
                b_bscan = (Button) rootView2.findViewById(R.id.b_bscan);
                b_bscan.setOnClickListener(myOnClickListener);
                queueList = new ArrayList<QueueListItem>();
                loadingProgressBar = (ProgressBar) rootView2.findViewById(R.id.pb_bluetooth);
                loadingProgressBar.setVisibility(View.INVISIBLE);
                lv_blist = (ListView) rootView2.findViewById(R.id.lv_blist);
                registerClickCallback();

                mBTAdapter = BluetoothAdapter.getDefaultAdapter();
                if(mBTAdapter==null){
                    tv_bstatus.setText("Device Not Supported");
                    b_bscan.setVisibility(View.GONE);
                }
                getActivity().registerReceiver(mReceiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
                getActivity().registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
                isReceiverRegistered = true;

                myArrayAdapter = new MyArrayAdapter<QueueListItem>();
                return rootView2;
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (mBTAdapter != null) {
            if(mBTAdapter.isEnabled())
                mBTAdapter.disable();
        }
        if(isReceiverRegistered){
            getActivity().unregisterReceiver(mReceiver);
            isReceiverRegistered=false;
        }
    }

    private void registerClickCallback(){
        lv_blist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View viewClicked, int position, long id) {
                mBTAdapter.cancelDiscovery();
                QueueListItem clickedQItem = queueList.get(position);
                if(clickedQItem.getqName()!="None"){
                    qid = MyUtilOperations.decrypt(clickedQItem.getqId(),clickedQItem.getqName());
                    if(qid!=null)
                        joinQueue();
                }else {
                    Toast.makeText(getActivity(), "No Queues.. Scan Again Or Use QRCode",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initilizeOnClickListener() {
        myOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.b_scan:
                        Toast.makeText(getActivity(), "Scanning start...", Toast.LENGTH_SHORT).show();
                        QRCodeOperations.scanQr(getActivity());
                        break;
                    case R.id.b_browse:
                        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getContext(), getActivity())){
                            startImageBrowseActivity();
                        }else{
                            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,PERMISSION_READ_REQUEST,getContext(),getActivity());
                        }
                        break;
                    case R.id.b_bscan :
                        if(checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,getContext(),getActivity())) {
                            startBluetoothScan();
                        }else {
                            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION,PERMISSION_REQUEST_CODE_LOCATION,getContext(),getActivity());
                        }
                        break;
                    default://
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_CODE_LOCATION :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBluetoothScan();
                } else {
                    Toast.makeText(getContext(),"Permission Denied, Cannot Join Via Bluetooth",Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSION_READ_REQUEST :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startImageBrowseActivity();
                } else {
                    Toast.makeText(getContext(),"Permission Denied To Read Storage",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void startBluetoothScan() {
        if(!mBTAdapter.isEnabled()){
            enableBluetooth();
        }else {
            startBluetoothDiscovery();
        }
    }

    private void startBluetoothDiscovery(){
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
        }

        mBTAdapter.startDiscovery();
        loadingProgressBar.setVisibility(View.VISIBLE);
        tv_bstatus.setText("Scanning....");
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
            ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
        } else {

            ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
        }
    }

    private void enableBluetooth(){
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, REQUEST_ENABLEBT);
    }

    private void startImageBrowseActivity() {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select QR Image"), PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//TODO change structure if time permits
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            Toast.makeText(getActivity(),"Scan Result",Toast.LENGTH_SHORT).show();
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "Scanning Interrupted", Toast.LENGTH_SHORT).show();
                super.onActivityResult(requestCode, resultCode, data);
            } else {
                qid = result.getContents();
                Toast.makeText(getActivity(),qid,Toast.LENGTH_SHORT).show();
                joinQueue();
                Log.d(TAG, "Qid :" + result.getContents());
            }
        }else {
            if(requestCode == REQUEST_ENABLEBT){
                if(resultCode==RESULT_OK){
                    startBluetoothScan();
                }else{
                    Toast.makeText(getActivity(),"No Bluetooth Access Granted",Toast.LENGTH_SHORT).show();
                    tv_bstatus.setText("Permission Denied For Bluetooth Access");
                }
            }else if (requestCode == PICK_IMAGE_REQUEST) {
                    if (resultCode == RESULT_OK) {
                        Uri uri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                            // Log.d(TAG, String.valueOf(bitmap));
                            qid = QRCodeOperations.readQrFromBitmap(getActivity(), bitmap);
                            if (qid != null) {
                                joinQueue();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else {

                super.onActivityResult(requestCode, resultCode, data);
            }
        }
        }

    private void joinQueue(){
        token=-1;
        Log.i(MyVariables.TAG,"qid : "+qid);
        mref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid).child("newtoken");
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    Toast.makeText(getActivity(),"Invalid QR Code",Toast.LENGTH_SHORT).show();
                }else{
                    mref.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(final MutableData currentData) {
                            if(null == currentData.getValue()){
                                Log.i(TAG,"Current Value is null...");
                                currentData.setValue(2);
                            }else{
                                Log.i(TAG,"Current Value is not null...\nvalue :"+currentData.getValue());
                                currentData.setValue((Long)currentData.getValue()+1);
                            }
                            return  Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(DatabaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {

                            if (firebaseError != null) {
                                Log.i(TAG,"Firebase counter increment failed.");
                                Log.i(TAG,"ERROR : "+ firebaseError.getMessage());
                                Toast.makeText(getActivity(),"Try Again",Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i(TAG,"Firebase counter increment succeeded.");
                                token = (int)(((Long)dataSnapshot.getValue())-1);
                                Log.i(TAG,dataSnapshot.getValue()+" \nboolean :"+b);
                                editor.putBoolean(ISQJOINED,true);
                                editor.putString(JOINQID,qid);
                                editor.putInt(JOINEDQTOKEN,token);
                                editor.commit();
                                if(-1 != token) {
                                    // Unregister broadcast listeners
                                    if(isReceiverRegistered) {
                                        getActivity().unregisterReceiver(mReceiver);
                                        isReceiverRegistered = false;
                                    }
                                    logDetails(sharedPreferences.getString(MyVariables.MYUSERNAME,null),sharedPreferences.getString(MyVariables.MYEMAIL,null));
                                    startActivity(new Intent(getActivity(),JoiningActivity.class));
                                    getActivity().finish();
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

    }

    private void logDetails(String userName,String email){
        mref = null;
        mref = FirebaseDatabase.getInstance().getReferenceFromUrl(MyVariables.BASEURL).child("Queues").child("tokens").child(qid).child("clients").child(String.valueOf(token));
        ClientBean userBean = new ClientBean(userName,email);
        mref.setValue(userBean);
    }

    private class MyArrayAdapter<T> extends ArrayAdapter<QueueListItem> {
        public MyArrayAdapter(){
            super(getActivity(),R.layout.listitemview,queueList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Make sure to have view to work with
            View itemView = convertView;
            if(itemView == null){
                itemView = getActivity().getLayoutInflater().inflate(R.layout.listitemview,parent,false);
            }
            //Find Device to work with
            final QueueListItem currentQItem = queueList.get(position);
            TextView tv__listitem_name = (TextView) itemView.findViewById(R.id.tv_listitem_name);
            TextView tv_listitem_desc = (TextView) itemView.findViewById(R.id.tv_listitem_desc);
            tv__listitem_name.setText(currentQItem.getqName());
            tv_listitem_desc.setText(currentQItem.getqDesc());
            return itemView;
        }
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,action);

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                if(deviceName==null){
                    deviceName="None";
                }
                if(deviceName.startsWith(TAG)){
                    String[] queueDetails = deviceName.split(";");
                    Log.d(TAG,"QueueName :"+queueDetails[1]+"\nQ Desc :"+queueDetails[2]+"\nQueue ID : "+queueDetails[3]);
                    queueList.add(new QueueListItem(queueDetails[1],queueDetails[2],queueDetails[3]));
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                tv_bstatus.setText("Select Queue To Join");
                if (queueList.size() == 0) {
                    tv_bstatus.setText("No Queues Nearby");
                    //TODO Remove this for loop later
                    for(int i=0;i<10;i++) {
                        //keep this after removing for loop
                        queueList.add(new QueueListItem("None", "https://www.none.com", "none"));
                    }
                }
                mBTAdapter.cancelDiscovery();
                mBTAdapter.disable();
                loadingProgressBar.setVisibility(View.GONE);
                lv_blist.setAdapter(myArrayAdapter);
            }

        }
    };
}
