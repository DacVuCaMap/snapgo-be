package com.delivery.app.service;

import com.delivery.app.dto.Request.ShipperLocationUpdateRequest;
import com.delivery.app.dto.ShipperLocationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LocationService {
    ResponseEntity<?> updateLocation(ShipperLocationUpdateRequest request,Long shipperId);
    List<ShipperLocationDto> findNearestShippers(double lat, double lng,Integer status);
    ResponseEntity<?> findNearStore(double lat,double lng,Integer status);
}
