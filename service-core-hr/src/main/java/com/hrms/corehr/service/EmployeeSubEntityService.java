package com.hrms.corehr.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.corehr.dto.*;
import com.hrms.corehr.entity.*;
import com.hrms.corehr.mapper.*;
import com.hrms.corehr.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeSubEntityService {

    private final EmployeeRepository          employeeRepository;
    private final AddressRepository           addressRepository;
    private final EmergencyContactRepository  emergencyContactRepository;
    private final FamilyMemberRepository      familyMemberRepository;
    private final EducationRepository         educationRepository;
    private final EmploymentHistoryRepository employmentHistoryRepository;

    private final AddressMapper           addressMapper;
    private final EmergencyContactMapper  emergencyContactMapper;
    private final FamilyMemberMapper      familyMemberMapper;
    private final EducationMapper         educationMapper;
    private final EmploymentHistoryMapper employmentHistoryMapper;

    // ── Addresses ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AddressDto.Response> listAddresses(String tenantId, UUID employeeId) {
        validateEmployee(tenantId, employeeId);
        return addressRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .stream().map(addressMapper::toResponse).toList();
    }

    @Transactional
    public AddressDto.Response addAddress(String tenantId, UUID employeeId,
                                           AddressDto.CreateRequest req, String currentUser) {
        validateEmployee(tenantId, employeeId);
        Address entity = addressMapper.toEntity(req);
        entity.setTenantId(tenantId);
        entity.setEmployeeId(employeeId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        Address saved = addressRepository.save(entity);
        log.info("[CoreHR] Address {} added for employee {} by {}", saved.getId(), employeeId, currentUser);
        return addressMapper.toResponse(saved);
    }

    @Transactional
    public AddressDto.Response updateAddress(String tenantId, UUID employeeId, UUID addressId,
                                              AddressDto.CreateRequest req, String currentUser) {
        validateEmployee(tenantId, employeeId);
        Address entity = addressRepository.findByIdAndEmployeeIdAndDeletedFalse(addressId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressMapper.updateEntity(req, entity);
        entity.setUpdatedBy(currentUser);
        return addressMapper.toResponse(addressRepository.save(entity));
    }

    @Transactional
    public void deleteAddress(String tenantId, UUID employeeId, UUID addressId, String currentUser) {
        validateEmployee(tenantId, employeeId);
        Address entity = addressRepository.findByIdAndEmployeeIdAndDeletedFalse(addressId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        addressRepository.save(entity);
        log.info("[CoreHR] Address {} soft-deleted for employee {} by {}", addressId, employeeId, currentUser);
    }

    // ── Emergency Contacts ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmergencyContactDto.Response> listEmergencyContacts(String tenantId, UUID employeeId) {
        validateEmployee(tenantId, employeeId);
        return emergencyContactRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .stream().map(emergencyContactMapper::toResponse).toList();
    }

    @Transactional
    public EmergencyContactDto.Response addEmergencyContact(String tenantId, UUID employeeId,
                                                             EmergencyContactDto.CreateRequest req,
                                                             String currentUser) {
        validateEmployee(tenantId, employeeId);
        EmergencyContact entity = emergencyContactMapper.toEntity(req);
        entity.setTenantId(tenantId);
        entity.setEmployeeId(employeeId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        EmergencyContact saved = emergencyContactRepository.save(entity);
        log.info("[CoreHR] EmergencyContact {} added for employee {} by {}", saved.getId(), employeeId, currentUser);
        return emergencyContactMapper.toResponse(saved);
    }

    @Transactional
    public EmergencyContactDto.Response updateEmergencyContact(String tenantId, UUID employeeId,
                                                                UUID contactId,
                                                                EmergencyContactDto.CreateRequest req,
                                                                String currentUser) {
        validateEmployee(tenantId, employeeId);
        EmergencyContact entity = emergencyContactRepository
                .findByIdAndEmployeeIdAndDeletedFalse(contactId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", "id", contactId));
        emergencyContactMapper.updateEntity(req, entity);
        entity.setUpdatedBy(currentUser);
        return emergencyContactMapper.toResponse(emergencyContactRepository.save(entity));
    }

    @Transactional
    public void deleteEmergencyContact(String tenantId, UUID employeeId, UUID contactId,
                                        String currentUser) {
        validateEmployee(tenantId, employeeId);
        EmergencyContact entity = emergencyContactRepository
                .findByIdAndEmployeeIdAndDeletedFalse(contactId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", "id", contactId));
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        emergencyContactRepository.save(entity);
        log.info("[CoreHR] EmergencyContact {} soft-deleted for employee {} by {}", contactId, employeeId, currentUser);
    }

    // ── Family Members ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FamilyMemberDto.Response> listFamilyMembers(String tenantId, UUID employeeId) {
        validateEmployee(tenantId, employeeId);
        return familyMemberRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .stream().map(familyMemberMapper::toResponse).toList();
    }

    @Transactional
    public FamilyMemberDto.Response addFamilyMember(String tenantId, UUID employeeId,
                                                     FamilyMemberDto.CreateRequest req,
                                                     String currentUser) {
        validateEmployee(tenantId, employeeId);
        FamilyMember entity = familyMemberMapper.toEntity(req);
        entity.setTenantId(tenantId);
        entity.setEmployeeId(employeeId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        FamilyMember saved = familyMemberRepository.save(entity);
        log.info("[CoreHR] FamilyMember {} added for employee {} by {}", saved.getId(), employeeId, currentUser);
        return familyMemberMapper.toResponse(saved);
    }

    @Transactional
    public FamilyMemberDto.Response updateFamilyMember(String tenantId, UUID employeeId,
                                                        UUID memberId,
                                                        FamilyMemberDto.CreateRequest req,
                                                        String currentUser) {
        validateEmployee(tenantId, employeeId);
        FamilyMember entity = familyMemberRepository
                .findByIdAndEmployeeIdAndDeletedFalse(memberId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("FamilyMember", "id", memberId));
        familyMemberMapper.updateEntity(req, entity);
        entity.setUpdatedBy(currentUser);
        return familyMemberMapper.toResponse(familyMemberRepository.save(entity));
    }

    @Transactional
    public void deleteFamilyMember(String tenantId, UUID employeeId, UUID memberId,
                                    String currentUser) {
        validateEmployee(tenantId, employeeId);
        FamilyMember entity = familyMemberRepository
                .findByIdAndEmployeeIdAndDeletedFalse(memberId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("FamilyMember", "id", memberId));
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        familyMemberRepository.save(entity);
        log.info("[CoreHR] FamilyMember {} soft-deleted for employee {} by {}", memberId, employeeId, currentUser);
    }

    // ── Education ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EducationDto.Response> listEducation(String tenantId, UUID employeeId) {
        validateEmployee(tenantId, employeeId);
        return educationRepository.findByEmployeeIdAndDeletedFalseOrderByEndYearDesc(employeeId)
                .stream().map(educationMapper::toResponse).toList();
    }

    @Transactional
    public EducationDto.Response addEducation(String tenantId, UUID employeeId,
                                               EducationDto.CreateRequest req, String currentUser) {
        validateEmployee(tenantId, employeeId);
        Education entity = educationMapper.toEntity(req);
        entity.setTenantId(tenantId);
        entity.setEmployeeId(employeeId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        Education saved = educationRepository.save(entity);
        log.info("[CoreHR] Education {} added for employee {} by {}", saved.getId(), employeeId, currentUser);
        return educationMapper.toResponse(saved);
    }

    @Transactional
    public EducationDto.Response updateEducation(String tenantId, UUID employeeId,
                                                  UUID educationId, EducationDto.CreateRequest req,
                                                  String currentUser) {
        validateEmployee(tenantId, employeeId);
        Education entity = educationRepository
                .findByIdAndEmployeeIdAndDeletedFalse(educationId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", educationId));
        educationMapper.updateEntity(req, entity);
        entity.setUpdatedBy(currentUser);
        return educationMapper.toResponse(educationRepository.save(entity));
    }

    @Transactional
    public void deleteEducation(String tenantId, UUID employeeId, UUID educationId,
                                 String currentUser) {
        validateEmployee(tenantId, employeeId);
        Education entity = educationRepository
                .findByIdAndEmployeeIdAndDeletedFalse(educationId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", educationId));
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        educationRepository.save(entity);
        log.info("[CoreHR] Education {} soft-deleted for employee {} by {}", educationId, employeeId, currentUser);
    }

    // ── Employment History ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmploymentHistoryDto.Response> listWorkHistory(String tenantId, UUID employeeId) {
        validateEmployee(tenantId, employeeId);
        return employmentHistoryRepository.findByEmployeeIdAndDeletedFalseOrderByStartDateDesc(employeeId)
                .stream().map(employmentHistoryMapper::toResponse).toList();
    }

    @Transactional
    public EmploymentHistoryDto.Response addWorkHistory(String tenantId, UUID employeeId,
                                                         EmploymentHistoryDto.CreateRequest req,
                                                         String currentUser) {
        validateEmployee(tenantId, employeeId);
        EmploymentHistory entity = employmentHistoryMapper.toEntity(req);
        entity.setTenantId(tenantId);
        entity.setEmployeeId(employeeId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        EmploymentHistory saved = employmentHistoryRepository.save(entity);
        log.info("[CoreHR] EmploymentHistory {} added for employee {} by {}", saved.getId(), employeeId, currentUser);
        return employmentHistoryMapper.toResponse(saved);
    }

    @Transactional
    public EmploymentHistoryDto.Response updateWorkHistory(String tenantId, UUID employeeId,
                                                            UUID historyId,
                                                            EmploymentHistoryDto.CreateRequest req,
                                                            String currentUser) {
        validateEmployee(tenantId, employeeId);
        EmploymentHistory entity = employmentHistoryRepository
                .findByIdAndEmployeeIdAndDeletedFalse(historyId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmploymentHistory", "id", historyId));
        employmentHistoryMapper.updateEntity(req, entity);
        entity.setUpdatedBy(currentUser);
        return employmentHistoryMapper.toResponse(employmentHistoryRepository.save(entity));
    }

    @Transactional
    public void deleteWorkHistory(String tenantId, UUID employeeId, UUID historyId,
                                   String currentUser) {
        validateEmployee(tenantId, employeeId);
        EmploymentHistory entity = employmentHistoryRepository
                .findByIdAndEmployeeIdAndDeletedFalse(historyId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmploymentHistory", "id", historyId));
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        employmentHistoryRepository.save(entity);
        log.info("[CoreHR] EmploymentHistory {} soft-deleted for employee {} by {}", historyId, employeeId, currentUser);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateEmployee(String tenantId, UUID employeeId) {
        employeeRepository.findByIdAndTenantIdAndDeletedFalse(employeeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
    }
}
