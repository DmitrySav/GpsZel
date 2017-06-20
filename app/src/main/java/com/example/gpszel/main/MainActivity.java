package com.example.gpszel.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.gpszel.R;
import com.example.gpszel.gpsproc.GPSData;
import com.example.gpszel.gpsproc.Observer;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends FragmentActivity implements Observer {
    private Toolbar toolbar;
    private String[] mItemTitles;
    private DrawerLayout drawer;
    private ListView mDrawerList;
    private Spinner mSpinner;
    DataModel[] drawerItem;
    BluetoothAdapter mBluetoothAdapter;
    //ArrayList<String> pairedDevices;
    GPSData gpsData = new GPSData(this);

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putStringArrayList("pairedDevices",pairedDevices);
//    }

    /*@Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        pairedDevices = savedInstanceState.getStringArrayList("pairedDevices");
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

//        if(savedInstanceState!=null){
//            pairedDevices = savedInstanceState.getStringArrayList("pairedDevices");
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pairedDevices);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            mSpinner.setAdapter(adapter);
//        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver,filter);
        registerReceiver(mReceiver,filter2);

        addHeader();
        addNavView();

        //drawer.setDrawerListener(mDrawerToggle);

        //навигационная кнопка
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //управление фрагментами
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = new MainFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // заполняем список устройств
        mReceiver.onReceive(this, getIntent());
    }

    @Override
    public void update() {

    }

    public void onClickConnect(View view) {
        // connect BT device
        gpsData.connect((BluetoothDevice)mSpinner.getSelectedItem());
    }

    // Слушатель для меню
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //что-то делать
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("seagull","Broadcast is given");
            mSpinner = (Spinner) findViewById(R.id.spinner);

            if (mSpinner==null) Log.d("raven","spinner null!!!");
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> rawDevices = mBluetoothAdapter.getBondedDevices();

//            pairedDevices = new ArrayList<String>();
//            for(BluetoothDevice bt : rawDevices)
//                pairedDevices.add(bt.getName());
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, pairedDevices);

            ArrayAdapter<BluetoothDevice> spinAdapter =
                    new ArrayAdapter<BluetoothDevice>
                            (context, android.R.layout.simple_spinner_item, new ArrayList<BluetoothDevice>(rawDevices))
                {
                    // And the "magic" goes here
                    // This is for the "passive" state of the spinner
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView view = (TextView) super.getView(position, convertView, parent);
                        view.setText(getItem(position).getName());
                        return view;
                    }

                    // And here is when the "chooser" is popped up
                    // Normally is the same view, but you can customize it if you want
                    @Override
                    public View getDropDownView(int position, View convertView,
                                                ViewGroup parent) {
                        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                        view.setText(getItem(position).getName());
                        return view;
                    }
                };
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (mSpinner!=null) mSpinner.setAdapter(spinAdapter);
        }
    };

    //управление кнопкой НАЗАД
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void checkBlu(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null){
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }

        }
    }
    private void addHeader(){
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.header, mDrawerList, false);
        mDrawerList.addHeaderView(header, null, false);
    }

    private void addNavView(){
        drawerItem = new DataModel[3];
        drawerItem[0] = new DataModel(R.drawable.ic_list_black_48dp,mItemTitles[0]);
        drawerItem[1] = new DataModel(R.drawable.ic_edit_location_black_48dp,mItemTitles[1]);
        drawerItem[2] = new DataModel(R.drawable.ic_build_black_48dp,mItemTitles[2]);

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.list_view_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }


}
