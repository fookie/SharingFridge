package android.assignment.sharingfridge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


public class MapViewFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    SendRequestTask mAuthTask;
    Hashtable<String, LatLng> markers = new Hashtable<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mAuthTask =new SendRequestTask();
        mAuthTask.execute();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return rootView;
    }

    /**
     * @param ll           LatLng position
     * @param title        the titile of mark
     * @param detail       detail shown on the mark
     * @param switchcamera if switch camera to this Mark
     */
    private void addMarker(LatLng ll, String title, String detail, boolean switchcamera) {
        googleMap.addMarker(new MarkerOptions().position(ll).title(title).snippet(detail));
        if (switchcamera) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(ll).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private class SendRequestTask extends AsyncTask<String, Void, String> {
        private String urlString = "http://178.62.93.103/SharingFridge/location.php";

        public SendRequestTask() {

        }

        protected String doInBackground(String... params) {
            return performPostCall();
        }

        public String performPostCall() {
            Log.d("send post-loc-", "performPostCall");
            String response = "";
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);/* milliseconds */
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //conn.setRequestProperty("Content-Type", "application/json");
                //make json object
                JSONObject jo = new JSONObject();
                jo.put("action", "download");
                jo.put("user", UserStatus.username);
                String tosend = jo.toString();
                Log.d("JSON", tosend);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                outputStreamWriter.write("location=" + tosend);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                int responseCode = conn.getResponseCode();
                InputStream inputStream = conn.getInputStream();
                // Convert the InputStream into a string
                int length = 500;
                String contentAsString = convertInputStreamToString(inputStream, length);
                conn.disconnect();
                return contentAsString;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        public String convertInputStreamToString(InputStream stream, int length) throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[length];
            reader.read(buffer);
            return new String(buffer);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("send post-loc-","exx");
            mAuthTask = null;
            try {
                JSONArray jr = new JSONArray(result);
                for (int i = 0; i < jr.length(); i++) {
                    JSONObject jo = jr.getJSONObject(i);
                    markers.put(jo.getString("username"), new LatLng(jo.getDouble("la"), jo.getDouble("lo")));
                }

                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap mMap) {
                        googleMap = mMap;
                        //add nearby markets
                        Iterator it = markers.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            addMarker((LatLng) entry.getValue(), (String) entry.getKey(), "", false);
                        }
//                addMarker(new LatLng(53.318994, -6.213717), "Tesco", "supermarket", false);
//                addMarker(new LatLng(53.335628, -6.243302), "Tesco Metro", "supermarket", false);
//                addMarker(new LatLng(53.325329, -6.254133), "Lidl", "discount supermarket", false);
//                addMarker(new LatLng(53.307358, -6.215584), "Molloys Centra", "Store in UCD", false);
                        // For showing a move to my location button
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d("GMAP", "NO PERMISSION");
                        } else {
                            googleMap.setMyLocationEnabled(true);
                            Log.d("GMAP", googleMap + "");
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}