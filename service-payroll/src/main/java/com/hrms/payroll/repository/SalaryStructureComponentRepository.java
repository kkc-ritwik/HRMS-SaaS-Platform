package com.hrms.payroll.repository;

import com.hrms.payroll.entity.SalaryStructureComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryStructureComponentRepository extends JpaRepository<SalaryStructureComponent, UUID> {

    List<SalaryStructureComponent> findBySalaryStructureId(UUID salaryStructureId);

    Optional<SalaryStructureComponent> findBySalaryStructureIdAndComponentId(
            UUID salaryStructureId, UUID componentId);

    boolean existsBySalaryStructureIdAndComponentId(UUID salaryStructureId, UUID componentId);

    @Modifying
    @Query("DELETE FROM SalaryStructureComponent ssc WHERE ssc.salaryStructureId = :structureId " +
           "AND ssc.componentId = :componentId")
    void deleteByStructureAndComponent(@Param("structureId") UUID structureId,
                                        @Param("componentId") UUID componentId);

    @Modifying
    @Query("DELETE FROM SalaryStructureComponent ssc WHERE ssc.salaryStructureId = :structureId")
    void deleteAllByStructureId(@Param("structureId") UUID structureId);
}
