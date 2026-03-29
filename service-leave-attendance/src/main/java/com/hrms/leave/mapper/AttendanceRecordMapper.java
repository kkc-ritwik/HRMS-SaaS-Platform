package com.hrms.leave.mapper;

import com.hrms.leave.dto.AttendanceDto;
import com.hrms.leave.entity.AttendancePunch;
import com.hrms.leave.entity.AttendanceRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface AttendanceRecordMapper {

    @Mapping(target = "shiftName", ignore = true)
    @Mapping(target = "punches",   ignore = true)
    AttendanceDto.RecordResponse toRecordResponse(AttendanceRecord entity);

    AttendanceDto.PunchInfo toPunchInfo(AttendancePunch entity);
}
