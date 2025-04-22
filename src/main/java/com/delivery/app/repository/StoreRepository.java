package com.delivery.app.repository;

import com.delivery.app.Entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store,Long> {
    List<Store> findAllByIsDeletedFalse();
}
