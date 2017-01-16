package com.untoc.ks_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {
    private LinearLayout container;
    private WebView mWebView;
    private TextView weatherview;
    private TextView jusoText;
    private ImageView WeaterimageView;
    public static  Bitmap bit;
    private ImageButton upBt;
    private ImageButton leftBt;
    private ImageButton rightBt;
    private ImageButton stopBt;
    private Switch swc;



    Document doc = null;

    private String urlStr;
    Boolean isGPSEnabled, isNetworkEnabled;

    Geocoder geoCoder;
    private Location myLocation = null;
    double latPoint = 0;
    double lngPoint = 0;

    //Bluetooth
    private final static int DEVICES_DIALOG = 1;
    private final static int ERROR_DIALOG = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    static BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;
    private OutputStream outputStream;
    private InputStream inputStream;

    volatile boolean stopWorker;
    int readBufferPosition;
    Thread workerThread;
    byte[] readBuffer;

    public static Context mContext;
    public static AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Main", "onCreate");

        container = (LinearLayout)findViewById(R.id.main_container);
        mWebView = (WebView) findViewById(R.id.webView);
        weatherview = (TextView) findViewById(R.id.tvWeather);
        WeaterimageView = (ImageView) findViewById(R.id.imageView);
        upBt = (ImageButton) findViewById(R.id.upBt);
        leftBt = (ImageButton) findViewById(R.id.leftBt);
        rightBt = (ImageButton) findViewById(R.id.rightBt);
        stopBt = (ImageButton) findViewById(R.id.stopBt);
        swc = (Switch) findViewById(R.id.modeSwitch);
        
        // 웹뷰에서 자바스크립트실행가능
        mWebView.getSettings().setJavaScriptEnabled(true);
        // 주소 지정
        mWebView.loadUrl("http://192.168.43.31:8220/?action=stream");
        // WebViewClient 지정
        mWebView.setWebViewClient(new WebViewClientClass());

        geoCoder = new Geocoder(this, Locale.KOREAN);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = this;
        activity = this;

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if (bluetoothAdapter == null) {
            ErrorDialog("This device is not implement Bluetooth.");
        }

        if(bluetoothAdapter.isEnabled()){
            DeviceDialog();
        }

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Main", "isNetworkEnabled=" + isNetworkEnabled);

        jusoText = (TextView) findViewById(R.id.tvAddress);
        jusoText.setText(String.valueOf("GPS 수신중..."));
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                myLocation = location;
                GetLocations();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);

        swc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // swich action
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        sendData("a");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        sendData("m");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        upBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData("u");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        leftBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData("l");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        rightBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData("r");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        stopBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData("s");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void GetLocations() {
        // 텍스트뷰를 찾음
        TextView latText = (TextView) findViewById(R.id.tvLatitude);
        StringBuffer juso = new StringBuffer();

        if (myLocation != null) {
            latPoint = myLocation.getLatitude();
            lngPoint = myLocation.getLongitude();
            GetXMLTask task = new GetXMLTask();
            urlStr = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx="+latPoint+"&gridy="+lngPoint;
            task.execute(urlStr);
            try {
                // 위도,경도를 이용하여 현재 위치의 주소를 가져온다.
                List<Address> addresses;
                addresses = geoCoder.getFromLocation(latPoint, lngPoint, 1);
                for(Address addr: addresses){
                    int index = addr.getMaxAddressLineIndex();
                    for(int i=0;i<=index;i++){
                        juso.append(addr.getAddressLine(i));
                        juso.append(" ");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            latText.setText(" [ " + String.valueOf(Double.parseDouble(String.format("%.3f",latPoint))) +"  ,  " + Double.parseDouble(String.format("%.3f",lngPoint)) + " ] ");
            jusoText.setText(String.valueOf(juso));
        }
    }

    //private inner class extending AsyncTask
    private class GetXMLTask extends AsyncTask<String, Void, Document>{

        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {

            String s = "";
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("data");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환

//            for(int i = 0; i< nodeList.getLength(); i++){
                //날씨 데이터를 추출
                Node node = nodeList.item(0); //data엘리먼트 노드
                Element fstElmnt = (Element) node;
                NodeList nameList  = fstElmnt.getElementsByTagName("temp");
                Element nameElement = (Element) nameList.item(0);
                nameList = nameElement.getChildNodes();
                s += "현재 날씨는   " + ((Node) nameList.item(0)).getNodeValue() + "c";

                NodeList websiteList = fstElmnt.getElementsByTagName("wfKor");
                //<wfKor>맑음</wfKor> =====> <wfKor> 태그의 첫번째 자식노드는 TextNode 이고 TextNode의 값은 맑음
//                s += "날씨 = "+  websiteList.item(0).getChildNodes().item(0).getNodeValue() +"\n";
//            }
            String weather = websiteList.item(0).getChildNodes().item(0).getNodeValue();

            if (weather.equals("맑음")){
                WeaterimageView.setImageResource(R.drawable.sun);}
            else if(weather.equals("구름 조금")){
                WeaterimageView.setImageResource(R.drawable.fewcloud);}
            else if(weather.equals("구름 많음")){
                WeaterimageView.setImageResource(R.drawable.manycloud);}
            else if(weather.equals("흐림")){
                WeaterimageView.setImageResource(R.drawable.cloud);}
            else if(weather.equals("비")){
                WeaterimageView.setImageResource(R.drawable.rain);}
            else if(weather.equals("눈/비")){
                WeaterimageView.setImageResource(R.drawable.snowandrain);}
            else if(weather.equals("눈")){
                WeaterimageView.setImageResource(R.drawable.snow);}
            else{
                WeaterimageView.setImageResource(R.drawable.manycloud);}

            weatherview.setText(s);
            super.onPostExecute(doc);
        }
    }//end inner class - GetXMLTask

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }





    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 블루투스 part ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ


    static public Set<BluetoothDevice> getPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    @Override
    public void onBackPressed() {
        doClose();
        super.onBackPressed();
    }

    public void doConnect(BluetoothDevice device){
        bluetoothDevice = device;
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try{
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothAdapter.cancelDiscovery();
            new ConnectTask().execute();
        }catch (IOException e) {
            Log.e("", e.toString(), e);
        }
    }

    public void doClose() {
        if (workerThread != null)
            workerThread.interrupt();
        new CloseTask().execute();
    }

    private class ConnectTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params){
            try{
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                beginListenForData();
            } catch (Throwable t) {
                Log.e( "", "connect? "+ t.getMessage() );
                doClose();
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable){
                Log.d("","ConnectTask "+result.toString() );
                ErrorDialog("ConnectTask "+result.toString());
            }
        }
    }

    private class CloseTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try {
                try{outputStream.close();}catch(Throwable t){/*ignore*/}
                try{inputStream.close();}catch(Throwable t){/*ignore*/}
                bluetoothSocket.close();
            } catch (Throwable t) {
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Throwable) {
                Log.e("",result.toString(),(Throwable)result);
                ErrorDialog(result.toString());
            }
        }
    }


    public void DeviceDialog() {
        if (isFinishing()) return;
        FragmentManager fm = MainActivity.this.getSupportFragmentManager();
        MyDialogFragment alertDialog = MyDialogFragment.newInstance(DEVICES_DIALOG, "");
        alertDialog.show(fm, "");
    }


    public void ErrorDialog(String text){
        if (activity.isFinishing()) return;
        FragmentManager fm = MainActivity.this.getSupportFragmentManager();
        MyDialogFragment alertDialog = MyDialogFragment.newInstance(ERROR_DIALOG, text);
        alertDialog.show(fm, "");
    }

    void sendData(String msg) throws IOException
    {
//        String msg = myTextbox.getText().toString();
        if ( msg.length() == 0 ) return;
//
        msg += "\n";
//        Log.d(msg, msg);
        if (outputStream != null)
            outputStream.write(msg.getBytes());
//        myLabel.setText("Data Sent");
//        myTextbox.setText(" ");
    }


    void beginListenForData() {
        final Handler handler = new Handler(Looper.getMainLooper());

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable(){
            public void run(){
                while(!Thread.currentThread().isInterrupted() && !stopWorker){
                    try{
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0){
                            //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 서비스 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
                            Intent intent = new Intent(MainActivity.this, MyService.class);
                            startService(intent);
                            //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 서비스 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for(int i=0; i<bytesAvailable; i++){
                                byte b = packetBytes[i];
                                if(b == '\n'){
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");

                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run()
                                        {
                                        }
                                    });
                                }else{
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex){
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }
}
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 블루투스 part ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ