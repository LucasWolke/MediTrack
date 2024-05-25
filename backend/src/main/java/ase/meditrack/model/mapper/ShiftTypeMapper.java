package ase.meditrack.model.mapper;

import ase.meditrack.model.dto.ShiftScheduleDto;
import ase.meditrack.model.dto.ShiftTypeDto;
import ase.meditrack.model.dto.ShiftTypeScheduleDto;
import ase.meditrack.model.entity.Shift;
import ase.meditrack.model.entity.ShiftType;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = EntityUuidMapper.class)
public interface ShiftTypeMapper {

    @Named("toDto")
    ShiftTypeDto toDto(ShiftType shiftType);

    ShiftType fromDto(ShiftTypeDto dto);

    @Named("toScheduleDto")
    @Mapping(target = "name", source = "shiftType.name")
    @Mapping(target = "startTime", source = "shiftType.startTime")
    @Mapping(target = "endTime", source = "shiftType.endTime")
    ShiftTypeScheduleDto toScheduleDto(ShiftType shiftType);

    @IterableMapping(qualifiedByName = "toDto")
    List<ShiftTypeDto> toDtoList(List<ShiftType> shiftTypes);
}
