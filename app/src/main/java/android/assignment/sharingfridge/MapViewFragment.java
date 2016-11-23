package android.assignment.sharingfridge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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


public class MapViewFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                //add nearby markets
                addMarker(new LatLng(53.318994, -6.213717), "Tesco", "supermarket", false);
                addMarker(new LatLng(53.335628, -6.243302), "Tesco Metro", "supermarket", false);
                addMarker(new LatLng(53.325329, -6.254133), "Lidl", "discount supermarket", false);
                addMarker(new LatLng(53.307358, -6.215584), "Molloys Centra", "Store in UCD", false);
                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("GMAP", "NO PERMISSION");
                } else {
                    googleMap.setMyLocationEnabled(true);
                    Log.d("GMAP",googleMap+"");
                }
            }
        });

        return rootView;
    }

    /**
     *
     * @param ll    LatLng position
     * @param title the titile of mark
     * @param detail detail shown on the mark
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

    public void setCurrentLocation(Location loc){

    }
}