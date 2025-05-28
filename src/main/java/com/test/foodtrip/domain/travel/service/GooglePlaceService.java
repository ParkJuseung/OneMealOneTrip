package com.test.foodtrip.domain.travel.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GooglePlaceService {

    @Value("${google.api.key}")
    private String apiKey;

    //캐시 추가
    private final Map<String, List<String>> photoCache = new ConcurrentHashMap<>();

    public List<String> getPhotoUrlsByPlaceId(String placeId) {

        //캐시에 존재하면 바로 반환
        if (photoCache.containsKey(placeId)) {
            return photoCache.get(placeId);
        }

        List<String> photoUrls = new ArrayList<>();

        try {
            String placeDetailUrl = "https://maps.googleapis.com/maps/api/place/details/json?place_id="
                    + placeId + "&fields=photo&key=" + apiKey;

            HttpURLConnection conn = (HttpURLConnection) new URL(placeDetailUrl).openConnection();
            conn.setRequestMethod("GET");

            InputStream inputStream = conn.getInputStream();
            String response = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));

            JSONObject json = new JSONObject(response);
            JSONArray photos = json.getJSONObject("result").optJSONArray("photos");

            if (photos != null) {
                for (int i = 0; i < Math.min(photos.length(), 5); i++) {
                    String photoRef = photos.getJSONObject(i).getString("photo_reference");
                    String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference="
                            + photoRef + "&key=" + apiKey;
                    photoUrls.add(photoUrl);
                }
            }

            //성공 시 캐시에 저장
            photoCache.put(placeId, photoUrls);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return photoUrls;
    }

}
