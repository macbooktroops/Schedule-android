package com.playgilround.schedule.client.locationschedule;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.playgilround.schedule.client.locationschedule.model.SearchLocationResult;
import com.playgilround.schedule.client.model.LocationInfo;
import com.playgilround.schedule.client.model.ZoomLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.playgilround.schedule.client.locationschedule.LocationScheduleActivity.LOCATION_CURRENT;
import static com.playgilround.schedule.client.locationschedule.LocationScheduleActivity.LOCATION_DESTINATION;

/**
 * 19-02-18
 * 위치 관련 데이터 처리 MVP
 */
public class LocationSchedulePresenter implements LocationScheduleContract.Presenter {

    private static final String TAG = LocationSchedulePresenter.class.getSimpleName();

    private final LocationScheduleContract.View mView;
    private final Context mContext;

    private Geocoder geocoder;

    private double currentLatitude; //위도
    private double currentLongitude; //경도

    private double intentLatitude; //저장된 위도
    private double intentLongitude; //저장된 경도

    private double searchLatitude;
    private double searchLongitude;
    private String searchLocation;

    LocationSchedulePresenter(Context context, LocationScheduleContract.View view) {
        mView = view;
        mContext = context;

        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void setMapDisplay(double latitude, double longitude, double intentLatitude, double intentLongitude) {
        geocoder = new Geocoder(mContext);
        currentLatitude = latitude;
        currentLongitude = longitude;

        this.intentLatitude = intentLatitude;
        this.intentLongitude = intentLongitude;

        LatLng curMap = new LatLng(latitude, longitude);
        LatLng destMap = new LatLng(intentLatitude, intentLongitude);

        mView.setMapMarker(curMap, destMap);
    }

    //장소 검색
    @Override
    public void onSearchConfirmed(CharSequence text) {
        // java.lang.NullPointerException error fixed.
        List<Address> addressList = new ArrayList<>();

        try {
            addressList = geocoder.getFromLocationName(text.toString(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList.size() != 0) {
            String title = addressList.get(0).getFeatureName();
            String snippet = addressList.get(0).getCountryName();

            searchLatitude = addressList.get(0).getLatitude();
            searchLongitude = addressList.get(0).getLongitude();

            //현재 자기 위치 좌표 생성
            LatLng currentMap = new LatLng(currentLatitude, currentLongitude);

            //검색 된 위치 좌표 생성
            LatLng searchMap = new LatLng(searchLatitude, searchLongitude);

            searchLocation = text.toString();

            SearchLocationResult result = new SearchLocationResult();
            result.setTitle(title);
            result.setSnippet(snippet);
            result.setCurrentLocation(currentMap);
            result.setSearchResultLocation(searchMap);
            result.setZoomLevel(setMapZoomLevel(currentMap, searchMap));

            mView.mapSearchResultComplete(result);
        } else {
            // 장소 결과가 없을 경우 View 로 어떻게 넘길 지?
            mView.mapSearchResultError();
        }
    }

    @Override
    public int setMapZoomLevel(LatLng currentMap, LatLng destMap) {
        //내 위치와 목적지 거리 계산
        Location currentLocation = new Location(LOCATION_CURRENT);
        currentLocation.setLatitude(currentMap.latitude);
        currentLocation.setLongitude(currentMap.longitude);

        Location destLocation = new Location(LOCATION_DESTINATION);
        destLocation.setLatitude(destMap.latitude);
        destLocation.setLongitude(destMap.longitude);

        double distance = currentLocation.distanceTo(destLocation);

        Log.d(TAG, "distance ----> " + distance);
        return ZoomLevel.getZoomLevel(distance);
    }

    @Override
    public ArrayList<LocationInfo> getLocationInfo() {
        ArrayList<LocationInfo> arrLocationInfo = new ArrayList<>();

        arrLocationInfo.add(new LocationInfo(searchLocation, searchLatitude, searchLongitude));

        return arrLocationInfo;
    }


}
