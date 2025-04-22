package com.delivery.app.controller;

import com.delivery.app.Entity.Account;
import com.delivery.app.Entity.Store;
import com.delivery.app.dto.Request.ShipperLocationUpdateRequest;
import com.delivery.app.dto.Response.DefaultResponse;
import com.delivery.app.dto.ShipperLocationDto;
import com.delivery.app.dto.StoreDto;
import com.delivery.app.repository.AccountRepository;
import com.delivery.app.service.AccountService;
import com.delivery.app.service.LocationService;
import com.delivery.app.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/location")
@AllArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    @PostMapping("/shipper-locations/update")
    public ResponseEntity<?> updateLocation(@RequestBody ShipperLocationUpdateRequest request, HttpServletRequest httpRequest){
        String jwt = jwtUtil.getJWTFromRequest(httpRequest);
        if (jwt==null){
            return ResponseEntity.ok().body(new DefaultResponse(400,"Session không hợp lệ",false));
        }
        String userName = jwtUtil.extractUsername(jwt);
        Account account = accountRepository.findByEmail(userName).orElse(null);
        if (account==null || !account.getRole().getName().equals("SHIPPER")){
            return ResponseEntity.ok().body(new DefaultResponse(400,"Tài khoản không hợp lệ",false));
        }

        return locationService.updateLocation(request,account.getId());
    }
    @GetMapping("/shipper-locations/near")
    public ResponseEntity<?> findNearShipper(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam("status") Integer status) {

        List<ShipperLocationDto> shipperLocationDtos = locationService.findNearestShippers(lat,lng,status);
        return ResponseEntity.ok().body(new DefaultResponse(200,"Tìm thành công",shipperLocationDtos,true));
    }
    @GetMapping("/store-location/near")
    public ResponseEntity<?> findNearStore(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam("status") Integer status ){
        return locationService.findNearStore(lat,lng,status);
    }
}
