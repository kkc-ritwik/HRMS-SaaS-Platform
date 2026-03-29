package com.hrms.corehr.repository;

import com.hrms.corehr.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByEmployeeIdAndDeletedFalse(UUID employeeId);

    Optional<Address> findByEmployeeIdAndAddressTypeAndDeletedFalse(
            UUID employeeId, Address.AddressType addressType);

    Optional<Address> findByIdAndEmployeeIdAndDeletedFalse(UUID id, UUID employeeId);
}
