package ase.meditrack.model.dto;

import ase.meditrack.model.CreateValidator;
import ase.meditrack.model.UpdateValidator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Map;
import java.util.UUID;

public record HardConstraintsDto(
        @NotNull(groups = { CreateValidator.class, UpdateValidator.class }) UUID id,
        @NotNull(groups = CreateValidator.class) Map<UUID, UUID> shiftOffShift,
        @NotNull(groups = CreateValidator.class) Map<UUID, Integer> daytimeRequiredRoles,
        @NotNull(groups = CreateValidator.class) Map<UUID, Integer> nighttimeRequiredRoles,
        @NotNull(groups = CreateValidator.class) @PositiveOrZero(
                groups = { CreateValidator.class, UpdateValidator.class }) Integer daytimeRequiredPeople,
        @NotNull(groups = CreateValidator.class) @PositiveOrZero(
                groups = { CreateValidator.class, UpdateValidator.class }) Integer nighttimeRequiredPeople,
        @NotNull(groups = CreateValidator.class) @PositiveOrZero(
                groups = { CreateValidator.class, UpdateValidator.class }) Integer allowedFlextimeTotal,
        @NotNull(groups = CreateValidator.class) @PositiveOrZero(
                groups = { CreateValidator.class, UpdateValidator.class }) Integer allowedFlextimePerMonth
) {
}

