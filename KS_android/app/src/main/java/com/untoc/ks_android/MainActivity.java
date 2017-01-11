package com.untoc.ks_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends Activity {
    private WebView mWebView;
    TextView textview;
    Document doc = null;

    private Button noti_btn;
    private String urlStr;
    Boolean isGPSEnabled, isNetworkEnabled;

    Geocoder geoCoder;
    private Location myLocation = null;
    double latPoint = 0;
    double lngPoint = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.untoc.ks_android.R.layout.activity_main);

        Log.d("Main", "onCreate");

        mWebView = (WebView) findViewById(R.id.webView);
        textview = (TextView) findViewById(R.id.tvWeather);

        // 웹뷰에서 자바스크립트실행가능
        mWebView.getSettings().setJavaScriptEnabled(true);
        // 주소 지정
        mWebView.loadUrl("https://youtu.be/HKFDrBzarAs");
        // WebViewClient 지정
        mWebView.setWebViewClient(new WebViewClientClass());

        geoCoder = new Geocoder(this, Locale.KOREAN);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Main", "isNetworkEnabled=" + isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                myLocation = location;
                GetLocations();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
//                logView.setText("onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
//                logView.setText("onProviderEnabled");
            }

            public void onProviderDisabled(String provider) {
//                logView.setText("onProviderDisabled");
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);

        final Button gpsButton = (Button) findViewById(com.untoc.ks_android.R.id.gpsButton);
        gpsButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                GetLocations();
                Log.d("location", "button pressed");
            }
        });

        noti_btn = (Button)findViewById(R.id.noti_service);

        noti_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BackGround.class);
                startActivity(intent);
            }
        });
    }
    public void GetLocations() {
        // 텍스트뷰를 찾음
        TextView latText = (TextView) findViewById(com.untoc.ks_android.R.id.tvLatitude);
        TextView lngText = (TextView) findViewById(com.untoc.ks_android.R.id.tvLongitude);
        TextView jusoText = (TextView) findViewById(com.untoc.ks_android.R.id.tvAddress);
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
//                    juso.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            latText.setText(String.valueOf(latPoint));
            lngText.setText(String.valueOf(lngPoint));
            jusoText.setText(String.valueOf(juso));
        }
        else{// 위치정보 null 일때
            jusoText.setText(String.valueOf("수신중..."));
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
                s += "온도 = "+ ((Node) nameList.item(0)).getNodeValue() +" ,";

                NodeList websiteList = fstElmnt.getElementsByTagName("wfKor");
                //<wfKor>맑음</wfKor> =====> <wfKor> 태그의 첫번째 자식노드는 TextNode 이고 TextNode의 값은 맑음
                s += "날씨 = "+  websiteList.item(0).getChildNodes().item(0).getNodeValue() +"\n";
//            }
            textview.setText(s);
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
}
