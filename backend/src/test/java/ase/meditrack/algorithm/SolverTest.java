package ase.meditrack.algorithm;


import ase.meditrack.service.algorithm.AlgorithmInput;
import ase.meditrack.service.algorithm.EmployeeInfo;
import ase.meditrack.service.algorithm.RoleInfo;
import ase.meditrack.service.algorithm.SchedulingSolver;
import ase.meditrack.service.algorithm.ShiftTypeInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SolverTest {


    @Test
    void simpleTest() {
        List<EmployeeInfo> employeeInfos =
                List.of(new EmployeeInfo(List.of(0), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(), Set.of(),
                        Optional.of(0)));
        List<ShiftTypeInfo> shiftTypeInfos = List.of(new ShiftTypeInfo(LocalTime.of(8, 0), LocalTime.of(16, 0), 8));
        List<RoleInfo> roles = List.of(new RoleInfo("Rolename", 0, 0));
        AlgorithmInput input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 0, 0);
        assertTrue(SchedulingSolver.solve(input).isPresent());
    }

    @Test
    void testCanWorkShift() {
        // worksShiftTypes is empty -> no solution
        List<EmployeeInfo> employeeInfos =
                List.of(new EmployeeInfo(List.of(), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(), Set.of(),
                        Optional.of(0)));
        List<ShiftTypeInfo> shiftTypeInfos = List.of(new ShiftTypeInfo(LocalTime.of(8, 0), LocalTime.of(18, 0), 8));
        List<RoleInfo> roles = List.of(new RoleInfo("Rolename", 0, 0));
        AlgorithmInput input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 0, 0);
        assertTrue(SchedulingSolver.solve(input).isEmpty());

        // worksShiftTypes contains shiftType -> solution
        employeeInfos = List.of(new EmployeeInfo(List.of(0), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(), Set.of(),
                Optional.of(0)));
        input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 0, 0);
        assertTrue(SchedulingSolver.solve(input).isPresent());
    }

    @Test
    void testMaxWorkingHours() {
        // Employee needs to work 28 days with 12 hours each. So 28 * 12 - 1 -> no solution
        List<EmployeeInfo> employeeInfos =
                List.of(new EmployeeInfo(List.of(0), 28 * 6, 28 * 12 - 1, 28 * 8, Set.of(), Set.of(), Optional.of(0)));
        List<ShiftTypeInfo> shiftTypeInfos = List.of(new ShiftTypeInfo(LocalTime.of(8, 0), LocalTime.of(20, 0), 12));
        List<RoleInfo> roles = List.of(new RoleInfo("Rolename", 0, 0));
        AlgorithmInput input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 1, 0);
        assertTrue(SchedulingSolver.solve(input).isEmpty());

        // 28 * 12 -> solution
        employeeInfos =
                List.of(new EmployeeInfo(List.of(0), 28 * 6, 28 * 12, 28 * 8, Set.of(), Set.of(), Optional.of(0)));
        input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 1, 0);
        assertTrue(SchedulingSolver.solve(input).isPresent());
    }

    @Test
    void testHoliday() {
        // holiday with only 1 user and required people of 1 -> no solution
        List<EmployeeInfo> employeeInfos = new ArrayList<>();
        employeeInfos.add(
                new EmployeeInfo(List.of(0), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(0), Set.of(), Optional.of(0)));
        List<ShiftTypeInfo> shiftTypeInfos = List.of(new ShiftTypeInfo(LocalTime.of(8, 0), LocalTime.of(20, 0), 12));
        List<RoleInfo> roles = List.of(new RoleInfo("Rolename", 0, 0));
        AlgorithmInput input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 1, 0);
        assertTrue(SchedulingSolver.solve(input).isEmpty());

        // holiday with 2 user -> solution
        employeeInfos.add(
                new EmployeeInfo(List.of(0), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(), Set.of(), Optional.of(0)));
        input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 1, 0);
        assertTrue(SchedulingSolver.solve(input).isPresent());
    }

    @Test
    void testRequiredPeople() {
        // employee can only work from 8-16; 16-20 is therefore empty -> no solution
        List<EmployeeInfo> employeeInfos = new ArrayList<>();
        employeeInfos.add(
                new EmployeeInfo(List.of(0), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(), Set.of(), Optional.of(0)));
        List<ShiftTypeInfo> shiftTypeInfos = List.of(
                new ShiftTypeInfo(LocalTime.of(8, 0), LocalTime.of(16, 0), 8),
                new ShiftTypeInfo(LocalTime.of(16, 0), LocalTime.of(0, 0), 8)
        );
        List<RoleInfo> roles = List.of(new RoleInfo("Rolename", 0, 0));
        AlgorithmInput input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 1, 0);
        assertTrue(SchedulingSolver.solve(input).isEmpty());

        // second employee can work both shiftType -> solution
        employeeInfos.add(
                new EmployeeInfo(List.of(0, 1), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(), Set.of(), Optional.of(0)));
        assertTrue(SchedulingSolver.solve(input).isPresent());
    }

    @Test
    void testRequiredPeoplePerRole() {
        // employee can only work from 8-16; 16-20 is therefore empty -> no solution
        List<EmployeeInfo> employeeInfos = new ArrayList<>();
        employeeInfos.add(
                new EmployeeInfo(List.of(0), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(), Set.of(), Optional.of(0)));
        List<ShiftTypeInfo> shiftTypeInfos = List.of(
                new ShiftTypeInfo(LocalTime.of(8, 0), LocalTime.of(16, 0), 8),
                new ShiftTypeInfo(LocalTime.of(16, 0), LocalTime.of(0, 0), 8)
        );
        List<RoleInfo> roles = List.of(new RoleInfo("Rolename", 1, 0));
        AlgorithmInput input = new AlgorithmInput(28, employeeInfos, shiftTypeInfos, roles, 0, 0);
        assertTrue(SchedulingSolver.solve(input).isEmpty());

        // second employee can work both shiftType -> solution
        employeeInfos.add(
                new EmployeeInfo(List.of(0, 1), 28 * 8 - 20, 28 * 8 + 20, 28 * 8, Set.of(), Set.of(), Optional.of(0)));
        assertTrue(SchedulingSolver.solve(input).isPresent());
    }

}
