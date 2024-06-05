package ase.meditrack.service.algorithm;

import java.util.List;

/**
 * @param worksShifts                 shifts the employee is allowed to work
 * @param optimalWorkingHoursPerMonth The optimal working time an employee should work in a given month. Should already consider things like number of days in a month, working percentage, holidays, ...
 */
public record EmployeeInfo(
        List<Integer> worksShifts,
        int optimalWorkingHoursPerMonth
) {
}
