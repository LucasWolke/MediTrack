package ase.meditrack.config;

import ase.meditrack.model.dto.UserDto;
import ase.meditrack.model.entity.HardConstraints;
import ase.meditrack.model.entity.Holiday;
import ase.meditrack.model.entity.MonthlyPlan;
import ase.meditrack.model.entity.Preferences;
import ase.meditrack.model.entity.Role;
import ase.meditrack.model.entity.Shift;
import ase.meditrack.model.entity.ShiftSwap;
import ase.meditrack.model.entity.ShiftSwapStatus;
import ase.meditrack.model.entity.ShiftType;
import ase.meditrack.model.entity.Team;
import ase.meditrack.model.entity.User;
import ase.meditrack.model.mapper.UserMapper;
import ase.meditrack.repository.HolidayRepository;
import ase.meditrack.repository.MonthlyPlanRepository;
import ase.meditrack.repository.PreferencesRepository;
import ase.meditrack.repository.RoleRepository;
import ase.meditrack.repository.ShiftRepository;
import ase.meditrack.repository.ShiftSwapRepository;
import ase.meditrack.repository.ShiftTypeRepository;
import ase.meditrack.repository.TeamRepository;
import ase.meditrack.service.UserService;
import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Profile("generate-data")
@Component
@DependsOn({"keycloakConfig", "postConstruct"})
@Slf4j
public class DataGeneratorBean {
    private final UserService userService;
    private final TeamRepository teamRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftSwapRepository shiftSwapRepository;
    private final RoleRepository roleRepository;
    private final PreferencesRepository preferencesRepository;
    private final MonthlyPlanRepository monthlyPlanRepository;
    private final HolidayRepository holidayRepository;
    private final ShiftTypeRepository shiftTypeRepository;
    private final UserMapper userMapper;

    public DataGeneratorBean(UserService userService, TeamRepository teamRepository,
                             ShiftRepository shiftRepository, ShiftSwapRepository shiftSwapRepository,
                             RoleRepository roleRepository, PreferencesRepository preferencesRepository,
                             MonthlyPlanRepository monthlyPlanRepository, HolidayRepository holidayRepository,
                             UserMapper userMapper, ShiftTypeRepository shiftTypeRepository) {
        this.userService = userService;
        this.teamRepository = teamRepository;
        this.shiftRepository = shiftRepository;
        this.shiftSwapRepository = shiftSwapRepository;
        this.roleRepository = roleRepository;
        this.preferencesRepository = preferencesRepository;
        this.monthlyPlanRepository = monthlyPlanRepository;
        this.holidayRepository = holidayRepository;
        this.shiftTypeRepository = shiftTypeRepository;
        this.userMapper = userMapper;
    }

    private static final Faker FAKER = new Faker();

    private static final Integer NUM_TEAMS = 1;
    private static final List<String> ROLES = List.of("Nurse", "QualifiedNurse", "Doctor", "Trainee");
    private static final Integer NUM_USERS_WITH_ROLES = 8;
    private static final Integer NUM_HOLIDAYS = 0;
    private static final Integer NUM_MONTHLY_PLANS = 1;

    private List<Role> roles;
    private List<Team> teams;
    private List<User> users;
    private List<Shift> shifts;
    private List<ShiftType> shiftTypes;
    private List<MonthlyPlan> monthlyPlans;

    @PostConstruct
    private void generateData() {
        try {
            log.info("Checking if database contains data...");
            if (userService.findAll().size() <= 1) {
                log.info("Generating data...");
                createTeams();
                createRoles();
                createUsers();
                createHolidays();
                createShiftTypes();
                createMonthlyPlan();
                createShifts();
                createShiftSwap();
                createPreferences();
                log.info("Data generation complete!");
            } else {
                log.info("Database is not empty, rebuild the docker containers to generate data!");
            }
        } catch (Exception e) {
            log.error("Error generating data: {}", e.getMessage());
        }
    }

    private void createTeams() {
        log.info("Generating {} teams...", NUM_TEAMS);
        teams = new ArrayList<>();
        for (int i = 0; i < NUM_TEAMS; i++) {
            Team team = new Team();
            team.setName(FAKER.team().name());
            team.setWorkingHours(FAKER.number().numberBetween(20, 40));
            team.setHardConstraints(new HardConstraints(
                    null,
                    40,
                    80,
                    2,
                    2,
                    1,
                    team
            ));
            teams.add(teamRepository.save(team));
        }
    }

    private void createRoles() {
        log.info("Creating every role of: {} for every team...", ROLES);
        roles = new ArrayList<>();
        for (Team team : teams) {
            for (String roleName : ROLES) {
                Role role = new Role();
                role.setName(roleName);
                role.setTeam(team);
                role.setAllowedFlextimeTotal(40);
                role.setAllowedFlextimePerMonth(20);
                role.setDaytimeRequiredPeople(0);
                role.setNighttimeRequiredPeople(0);
                role.setAbbreviation(roleName.substring(0, 2).toUpperCase());
                role.setColor(FAKER.color().hex());
                roles.add(roleRepository.save(role));
            }
        }
    }

    private void createUsers() {
        log.info("Generating {} users per role for every team...", NUM_USERS_WITH_ROLES);
        users = new ArrayList<>();

        for (Team team : teams) {
            String firstName = FAKER.name().firstName();
            String lastName = FAKER.name().lastName();
            String username = (firstName.charAt(0) + lastName).toLowerCase();
            String email = firstName.toLowerCase() + '.' + lastName.toLowerCase() + '@'
                    + FAKER.internet().domainName();

            UserDto userDm = new UserDto(
                    null,
                    username,
                    "password",
                    email,
                    firstName,
                    lastName,
                    List.of("dm"),
                    null,
                    (float) FAKER.number().numberBetween(20, 100),
                    null,
                    List.of(FAKER.educator().course(), FAKER.educator().course()),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            User dmEntity = userMapper.fromDto(userDm);
            dmEntity.setTeam(team);
            dmEntity.setRole(roles.get(0));
            users.add(userService.create(dmEntity));

            for (Role role : roles) {
                if (role.getTeam().getId().equals(team.getId())) {
                    for (int i = 0; i < NUM_USERS_WITH_ROLES; i++) {
                        firstName = FAKER.name().firstName();
                        // use UUID to avoid duplicates
                        lastName = FAKER.name().lastName() + "_" + UUID.randomUUID();
                        username = (firstName.charAt(0) + lastName).toLowerCase();
                        email = firstName.toLowerCase() + '.' + lastName.toLowerCase() + '@'
                                + FAKER.internet().domainName();

                        UserDto user = new UserDto(
                                null,
                                username,
                                "password",
                                email,
                                firstName,
                                lastName,
                                List.of("employee"),
                                null,
                                (float) FAKER.number().numberBetween(20, 100),
                                null,
                                List.of(FAKER.educator().course(), FAKER.educator().course()),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        );
                        User userEntity = userMapper.fromDto(user);
                        userEntity.setTeam(team);
                        userEntity.setRole(role);
                        users.add(userService.create(userEntity));
                    }
                }
            }
        }
    }

    private void createHolidays() {
        log.info("Generating {} holidays for every user...", NUM_HOLIDAYS);
        for (User user : users) {
            for (int j = 0; j < NUM_HOLIDAYS; j++) {
                Holiday holiday = new Holiday();
                holiday.setStartDate(generateValidRandomFutureDate());
                holiday.setEndDate(holiday.getStartDate().plusDays(1));
                holiday.setIsApproved(false);
                holiday.setUser(user);
                holidayRepository.save(holiday);
            }
        }
    }

    private LocalDate generateValidRandomFutureDate() {
        int day;
        int month = FAKER.number().numberBetween(LocalDate.now().getMonthValue(), 12);
        if (month == LocalDate.now().getMonthValue()) {
            day = FAKER.number().numberBetween(LocalDate.now().getDayOfMonth(), LocalDate.now().getMonth().minLength());
        } else {
            day = FAKER.number().numberBetween(1, LocalDate.now().getMonth().minLength());
        }
        return LocalDate.of(LocalDate.now().getYear(), month, day);
    }

    private void createShiftTypes() {
        log.info("Generating morning, evening and night shift types for every team...");
        shiftTypes = new ArrayList<>();
        for (Team team : teams) {
            ShiftType nightShift = new ShiftType();
            nightShift.setName("Night Shift");
            nightShift.setStartTime(LocalTime.of(0, 0));
            nightShift.setEndTime(LocalTime.of(8, 0));
            nightShift.setBreakStartTime(LocalTime.of(2, 0));
            nightShift.setBreakEndTime(LocalTime.of(2, 30));
            nightShift.setAbbreviation("N10");
            nightShift.setColor("#190933");
            nightShift.setTeam(team);
            shiftTypes.add(shiftTypeRepository.save(nightShift));
            ShiftType morningShift = new ShiftType();
            morningShift.setName("Morning Shift");
            morningShift.setStartTime(LocalTime.of(8, 0));
            morningShift.setEndTime(LocalTime.of(16, 0));
            morningShift.setBreakStartTime(LocalTime.of(10, 0));
            morningShift.setBreakEndTime(LocalTime.of(10, 30));
            morningShift.setAbbreviation("D6");
            morningShift.setColor("#ACFCD9");
            morningShift.setTeam(team);
            shiftTypes.add(shiftTypeRepository.save(morningShift));
            ShiftType eveningShift = new ShiftType();
            eveningShift.setName("Evening Shift");
            eveningShift.setStartTime(LocalTime.of(16, 0));
            eveningShift.setEndTime(LocalTime.of(0, 0));
            eveningShift.setBreakStartTime(LocalTime.of(18, 0));
            eveningShift.setBreakEndTime(LocalTime.of(18, 30));
            eveningShift.setAbbreviation("D14");
            eveningShift.setColor("#B084CC");
            eveningShift.setTeam(team);
            shiftTypes.add(shiftTypeRepository.save(eveningShift));
        }
    }

    private void createMonthlyPlan() {
        log.info("Generating the next {} monthly plans for every team...", NUM_MONTHLY_PLANS);
        monthlyPlans = new ArrayList<>();
        for (Team team : teams) {
            for (int i = 0; i < NUM_MONTHLY_PLANS; i++) {
                MonthlyPlan monthlyPlan = new MonthlyPlan();
                monthlyPlan.setMonth(LocalDate.now().getMonthValue() + i);
                monthlyPlan.setYear(LocalDate.now().getYear());
                monthlyPlan.setPublished(false);
                monthlyPlan.setTeam(team);
                monthlyPlans.add(monthlyPlanRepository.save(monthlyPlan));
            }
        }
    }

    private void createShifts() {
        log.info("Generating shifts with every shift type for every role, day and monthly plan...");
        shifts = new ArrayList<>();
        for (MonthlyPlan monthlyPlan : monthlyPlans) {
            Map<UUID, List<User>> teamUsersPerRole = getTeamUsersPerRole(monthlyPlan.getTeam());
            int days = YearMonth.of(monthlyPlan.getYear(), monthlyPlan.getMonth()).lengthOfMonth();

            for (int i = 1; i <= days; i++) {
                LocalDate date = LocalDate.of(monthlyPlan.getYear(), monthlyPlan.getMonth(), i);
                for (UUID key : teamUsersPerRole.keySet()) {
                    List<User> usersPerRole = teamUsersPerRole.get(key);
                    if (usersPerRole != null && !usersPerRole.isEmpty()) {
                        for (User user : usersPerRole) {
                            // Create a shift with a random shift type for each user
                            Shift shift = new Shift();
                            shift.setDate(date);
                            shift.setShiftType(shiftTypes.get((int) (Math.random() * shiftTypes.size())));
                            shift.setMonthlyPlan(monthlyPlan);
                            List<Shift> userShifts = new ArrayList<>();
                            if (user.getShifts() != null && !user.getShifts().isEmpty()) {
                                userShifts = user.getShifts();
                            }
                            shift.addUser(user);

                            // Add the shift 1/3rd of the time
                            if (Math.random() < (1.0 / 3.0)) {
                                Shift savedShift = shiftRepository.save(shift);
                                shifts.add(savedShift);

                                userShifts.add(savedShift);
                                user.setShifts(userShifts);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<UUID, List<User>> getTeamUsersPerRole(Team team) {
        Map<UUID, List<User>> teamUserPerRole = new HashMap<>();
        for (Role role : roles) {
            teamUserPerRole.put(role.getId(), users.stream()
                    .filter(u -> u.getTeam().getId().equals(team.getId())
                            && u.getRole().getId().equals(role.getId()))
                    .toList());
        }
        return teamUserPerRole;
    }

    private void createPreferences() {
        log.info("Generating preferences for every user...");
        for (User user : users) {
            Preferences preferences = new Preferences();
            preferences.setId(user.getId());
            preferences.setOffDays(List.of(generateValidRandomFutureDate(), generateValidRandomFutureDate()));
            preferences.setUser(user);
            preferencesRepository.save(preferences);
        }
    }

    private void createShiftSwap() {
        int shiftSwapAmount = 3;
        log.info("Generating {} simple shift swaps for every user...", shiftSwapAmount);
        for (User user : users) {
            if (user.getShifts() != null && !user.getShifts().isEmpty()) {
                for (int i = 0; i < shiftSwapAmount; i++) {
                    Shift selectedShift = user.getShifts().get(i % user.getShifts().size());
                    ShiftSwap shiftSwap = new ShiftSwap();
                    shiftSwap.setRequestedShiftSwapStatus(ShiftSwapStatus.ACCEPTED);
                    shiftSwap.setSwapRequestingUser(user);
                    shiftSwap.setRequestedShift(selectedShift);
                    List<ShiftSwap> shiftSwapList = new ArrayList<>();
                    if (user.getRequestedShiftSwaps() != null && !user.getRequestedShiftSwaps().isEmpty()) {
                        shiftSwapList = user.getRequestedShiftSwaps();
                    }
                    shiftSwapList.add(shiftSwap);
                    user.setRequestedShiftSwaps(shiftSwapList);
                    List<ShiftSwap> shiftSwaps = new ArrayList<>();
                    if (selectedShift.getRequestedShiftSwap() != null
                            && !selectedShift.getRequestedShiftSwap().isEmpty()) {
                        shiftSwaps = selectedShift.getRequestedShiftSwap();
                    }
                    shiftSwaps.add(shiftSwap);
                    selectedShift.setRequestedShiftSwap(shiftSwaps);
                    shiftSwapRepository.save(shiftSwap);
                }
            }
        }
    }
}
