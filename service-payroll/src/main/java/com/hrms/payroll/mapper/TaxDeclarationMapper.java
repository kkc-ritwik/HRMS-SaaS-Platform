package com.hrms.payroll.mapper;

import com.hrms.payroll.dto.TaxDeclarationDto;
import com.hrms.payroll.entity.TaxDeclaration;
import com.hrms.payroll.entity.TaxProof;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaxDeclarationMapper {

    @Mapping(target = "id",                    ignore = true)
    @Mapping(target = "tenantId",              ignore = true)
    @Mapping(target = "employeeId",            ignore = true)
    @Mapping(target = "createdBy",             ignore = true)
    @Mapping(target = "updatedBy",             ignore = true)
    @Mapping(target = "createdAt",             ignore = true)
    @Mapping(target = "updatedAt",             ignore = true)
    @Mapping(target = "deleted",               ignore = true)
    @Mapping(target = "status",                ignore = true)
    TaxDeclaration toEntity(TaxDeclarationDto.SubmitRequest req);

    @Mapping(target = "proofs", ignore = true)  // enriched in service
    TaxDeclarationDto.Response toResponse(TaxDeclaration entity);

    TaxDeclarationDto.ProofSummary toProofSummary(TaxProof proof);
}
