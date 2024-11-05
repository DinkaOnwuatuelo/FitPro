package com.example.fitpro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import java.io.IOException;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap; // GoogleMap object to control the map.
    private SearchView searchView; // SearchView for entering location queries.

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize the Places API with an API key.
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "<AIzaSyCYKTZWzXT6EUFaNCj38x24qtvcayQb6BE>");
        }

        // Get the SupportMapFragment and request the Google Map to be asynchronous.
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        // Initialize the SearchView and set a listener to handle search queries.
        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Call searchLocation() when a query is submitted.
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // No action on text change in this implementation.
                return false;
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Called when the map is ready for use.
        mMap = googleMap;
        // Enable the user's location on the map.
        enableMyLocation();
    }

    private void enableMyLocation() {
        // Check for location permissions and request them if not already granted.
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            return;
        }
        mMap.setMyLocationEnabled(true); // Enable showing the user's location on the map.
    }

    private void searchLocation(String location) {
        // Use the Geocoder class to search for the location's latitude and longitude.
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(location, 1);
            if (!addressList.isEmpty()) {
                // If the location is found, move the camera and add a marker to the location.
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            } else {
                // Show a toast message if the location is not found.
                Toast.makeText(getActivity(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
