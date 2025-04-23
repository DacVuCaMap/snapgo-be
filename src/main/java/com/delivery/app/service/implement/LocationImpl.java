package com.delivery.app.service.implement;

import com.delivery.app.Entity.Account;
import com.delivery.app.Entity.ShipperLocation;
import com.delivery.app.Entity.Store;
import com.delivery.app.dto.Request.ShipperLocationUpdateRequest;
import com.delivery.app.dto.Response.DefaultResponse;
import com.delivery.app.dto.ShipperLocationDto;
import com.delivery.app.dto.StoreDto;
import com.delivery.app.repository.AccountRepository;
import com.delivery.app.repository.ShipperLocationRepository;
import com.delivery.app.repository.StoreRepository;
import com.delivery.app.service.LocationService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LocationImpl implements LocationService {

    private final ShipperLocationRepository shipperLocationRepository;
    private final StoreRepository storeRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final double EARTH_RADIUS = 6371;
    @Override
    @Transactional
    public ResponseEntity<?> updateLocation(ShipperLocationUpdateRequest request,Long shipperId) {
        ShipperLocation shipperLocation = shipperLocationRepository.findByShipperId(shipperId).orElse(new ShipperLocation());
        shipperLocation.setShipperId(shipperId);
        shipperLocation.setLatitude(request.getLatitude());
        shipperLocation.setLongitude(request.getLongitude());
        shipperLocation.setOnline(request.isOnline());
        shipperLocation.setArea(request.getArea());
        shipperLocation.setStatus(request.getStatus());
        shipperLocationRepository.save(shipperLocation);
        return ResponseEntity.ok().body(new DefaultResponse(200,"Cập nhập shipper online thành công",true));
    }


    public List<ShipperLocationDto> findNearestShippers(double lat, double lng,Integer status) {
        List<ShipperLocation> shippers = shipperLocationRepository.findNearbyShippers(status);
        return shippers.stream()
                .map(sl -> {
                    Account account = accountRepository.findById(sl.getShipperId()).orElse(null);
                    if (account == null) {
                        return null;
                    }
                    double distance = calculateDistance(lat, lng, sl.getLatitude(), sl.getLongitude());
                    return new ShipperLocationDto(
                            sl.getShipperId(),
                            account.getAvatar(),
                            account.getFirstName(),
                            account.getLastName(),
                            account.getEmail(),
                            account.getPhoneNumber(),
                            sl.getLatitude(),
                            sl.getLongitude(),
                            distance
                    );
                })
//                .filter(dto -> dto != null && dto.getDistance() <= 20) // Bán kính 10km
                .sorted(Comparator.comparingDouble(ShipperLocationDto::getDistance))
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> findNearStore(double lat, double lng, Integer status) {
        List<Store> stores = storeRepository.findAllByIsDeletedFalse();

        List<StoreDto> nearbyStores = stores.stream()
                .filter(store -> store.getLat() != null && store.getLng() != null)
                .map(store -> {
                    double distance = calculateDistance(lat, lng, store.getLat(), store.getLng());
                    return new AbstractMap.SimpleEntry<>(store, distance);
                })
//                .filter(entry -> entry.getValue() <= 30.0)
                .sorted(Map.Entry.comparingByValue())
                .limit(20)
                .map(entry -> {
                    StoreDto dto = modelMapper.map(entry.getKey(), StoreDto.class);
                    return dto;
                })
                .collect(Collectors.toList());
        if (nearbyStores.isEmpty()) {
            return ResponseEntity.ok(new DefaultResponse(200, "Không tìm thấy cửa hàng nào trong bán kính 30km.", false));
        }
        return ResponseEntity.ok().body(new DefaultResponse(200,"Đã tìm thấy cửa hàng",nearbyStores,true));
    }

    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    @Scheduled(fixedRate = 300000) // Mỗi 5 phút
    @Transactional
    public void cleanUpOfflineShippers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        shipperLocationRepository.deleteByLastUpdatedBeforeAndIsOnlineTrue(threshold);
    }
}
