package hr.etfos.josipvojak.whereami;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Variables for Google maps
    private GoogleMap mGoogleMap;
    private MapFragment mMapFragment;
    private GoogleMap.OnMapClickListener mCustomOnMapClickListener;
    private TextView tvCurrentLocation, tvLocation;

    // Variables for sound pool
    private SoundPool myPool;
    boolean loaded;
    private int ID;

    // Variables for photos
    private Uri photo;

    // Variables for Location
    private LocationManager mLocationManager;
    private Location mLocation;
    private String mProvider, mPictureTimeStampLocation;
    private Criteria mCriteria;
    private Geocoder mGeocoder;
    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        this.initialize();
        this.initializeSound();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        this.mLocationManager.requestLocationUpdates(
                this.mProvider, 1000, 10, this.mLocationListener);
    }

    private void initializeSound() {
        myPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        myPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,int status) {
                loaded = true;
            }
        });
        ID = myPool.load(this, R.raw.sound1, 1);
    }

    private void initialize() {
        tvCurrentLocation = (TextView) findViewById(R.id.tvCurrentLocation);
        tvLocation = (TextView) findViewById(R.id.tvLocation);

        mPictureTimeStampLocation = "UnknownLocation";

        this.mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        this.mMapFragment.getMapAsync(this);
        this.mCustomOnMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions newMarkerOptions = new MarkerOptions();
                newMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon_purple));
                newMarkerOptions.title("Marked place");
                newMarkerOptions.snippet("MPM (Moj prvi marker)");
                newMarkerOptions.position(latLng);
                mGoogleMap.addMarker(newMarkerOptions);

                tvLocation.setText("Location: \n" + latLng.toString());

                if (loaded) {
                    myPool.play(ID, 1, 1, 1, 0, 1f);
                }
            }
        };

        this.mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.mCriteria = new Criteria();
        this.mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        this.mProvider = this.mLocationManager.getBestProvider(this.mCriteria, true);
        if (Geocoder.isPresent()) {
            this.mGeocoder = new Geocoder(this);
        }
        this.mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocationDisplay(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

    }

    private void updateLocationDisplay(Location location) {
        String locationText = "Unknown location";
        this.mLocation = location;
        if(null != this.mLocation)
        {
            locationText = "Lat: " + this.mLocation.getLatitude() + "\n" +
                    "Lon: " + this.mLocation.getLongitude() + "\n" +
                    "Alt: " + this.mLocation.getAltitude();
            // Careful with this, this is just a simple example, be sure to read the docs

            mPictureTimeStampLocation = this.mLocation.getLatitude() + "_" + this.mLocation.getLongitude();

            if(null != this.mGeocoder)
            {
                try {
                    ArrayList<Address> nearbyAddresses =
                            (ArrayList<Address>) this.mGeocoder.getFromLocation(
                                    this.mLocation.getLatitude(), this.mLocation.getLongitude(),1
                            );
                    if(null != nearbyAddresses && nearbyAddresses.size() > 0)
                    {
                        Address myAddress = nearbyAddresses.get(0);
                        locationText += "\n" + myAddress.getCountryName() + "\n" +
                                myAddress.getLocality() + "\n" + myAddress.getAddressLine(0);
                    }
                } catch (IOException e) {
                    Log.e("TAG", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        this.tvCurrentLocation.setText(locationText);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        UiSettings uiSettings = this.mGoogleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        this.mGoogleMap.setOnMapClickListener(this.mCustomOnMapClickListener);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.mGoogleMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       /* if (requestCode == 10 && resultCode == RESULT_OK &&      data != null && data.hasExtra("data")) {
            ((ImageView) findViewById(R.id.ivMyImage)).setImageBitmap((Bitmap) data.getParcelableExtra("data"));
        }  */

        if (requestCode == 20 && resultCode == RESULT_OK &&  photo != null){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(photo, "image/*");
            startActivity(intent);
        }
    }

    public void btnTakePicture(View view) {
        //startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 10);
        String timestamp = mPictureTimeStampLocation;

        String filename = "IMG_"+ timestamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(filename,".jpg", storageDir);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (image != null) {
                photo = Uri.fromFile(image);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo);
                startActivityForResult(takePictureIntent, 20);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}