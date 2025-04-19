package com.delivery.app.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/vietmap")
public class VietMapController {
    @Value("${vietmap.api.key}")
    private String vietMapToken;

    private final RestTemplate restTemplate = new RestTemplate();


    // 1. Get Style
    @GetMapping("/style")
    public ResponseEntity<byte[]> getStyle() {
        String url = "https://maps.vietmap.vn/mt/tm/style.json?apikey=" + vietMapToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
    }

    // 2. Get Route
    @GetMapping("/route")
    public ResponseEntity<String> getRoute(
            @RequestParam String start,
            @RequestParam String end
    ) {
        String url = String.format(
                "https://maps.vietmap.vn/api/route?api-version=1.1&apikey=%s&point=%s&point=%s&vehicle=bike",
                vietMapToken,
                start,
                end
        );
        ResponseEntity<String> vietmapResponse = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        // Tạo header mới để trả về
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return new ResponseEntity<>(vietmapResponse.getBody(), headers, vietmapResponse.getStatusCode());
    }

    // 3. Get Address by RefId
    @GetMapping("/place")
    public ResponseEntity<String> getPlaceByRefId(@RequestParam String refId) {
        String url = String.format(
                "https://maps.vietmap.vn/api/place/v3?apikey=%s&refid=%s",
                vietMapToken,
                refId
        );
        ResponseEntity<String> vietmapResponse = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        // Tạo header mới để trả về
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return new ResponseEntity<>(vietmapResponse.getBody(), headers, vietmapResponse.getStatusCode());
    }

    // 4. Autocomplete by text
    @GetMapping("/autocomplete")
    public ResponseEntity<String> autocomplete(@RequestParam String text) {
        String url = String.format(
                "https://maps.vietmap.vn/api/autocomplete/v3?apikey=%s&text=%s",
                vietMapToken,
                text
        );
        // Gửi yêu cầu tới Vietmap API
        ResponseEntity<String> vietmapResponse = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        // Tạo header mới để trả về
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Trả về body từ Vietmap với header được kiểm soát
        return new ResponseEntity<>(vietmapResponse.getBody(), headers, vietmapResponse.getStatusCode());
    }
}
