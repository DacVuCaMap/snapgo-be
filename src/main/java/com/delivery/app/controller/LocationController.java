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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @GetMapping("/route/price")
    public ResponseEntity<?> calPriceByRoute(@RequestParam Double distance, @RequestParam String weightLabel) {
        try {
            // Validate input
            if (distance == null || distance < 0 || weightLabel == null || weightLabel.isEmpty()) {
                return ResponseEntity.badRequest().body("Distance and weightLabel must be provided and valid.");
            }

            // Calculate base fee
            double baseFee;
            if (distance <= 1) {
                baseFee = 10000;
            } else {
                baseFee = Math.floor(distance - 1) * 2000 + 10000;
            }

            // Define weight fees
            Map<String, Integer> weightFees = new HashMap<>();
            weightFees.put("0-5kg", 0);
            weightFees.put("5-10kg", 15000);
            weightFees.put("10-15kg", 20000);
            weightFees.put("15-20kg", 25000);
            weightFees.put("20-25kg", 30000);
            weightFees.put("25-30kg", 35000);
            weightFees.put("30-50kg", 45000);

            // Get extra fee based on weightLabel
            Integer extraFee = weightFees.getOrDefault(weightLabel, 0);

            // Calculate total fee
            double totalFee = baseFee + extraFee;

            // Return response
            return ResponseEntity.ok().body(new DefaultResponse(200,"thanh cong",totalFee,true));
        } catch (Exception e) {

            return ResponseEntity.ok().body(new DefaultResponse(400,"Error calculating price: " + e.getMessage(),false));
        }
    }
}
