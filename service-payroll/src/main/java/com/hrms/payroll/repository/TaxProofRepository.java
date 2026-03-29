package com.hrms.payroll.repository;

import com.hrms.payroll.entity.TaxProof;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxProofRepository extends JpaRepository<TaxProof, UUID> {

    List<TaxProof> findByDeclarationId(UUID declarationId);

    Optional<TaxProof> findByIdAndDeclarationId(UUID id, UUID declarationId);
}
