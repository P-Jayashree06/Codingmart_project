package com.bloodbank.bloodgroup;

import com.bloodbank.bloodgroup.dto.BloodGroupResponse;
import com.bloodbank.exception.ResourceNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BloodGroupServiceTest {

    private static final List<String> CODES = List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    /** recipient -> donors it can receive from (standard ABO/Rh matrix). */
    private static final Map<String, Set<String>> MATRIX = Map.of(
            "A+",  Set.of("A+", "A-", "O+", "O-"),
            "A-",  Set.of("A-", "O-"),
            "B+",  Set.of("B+", "B-", "O+", "O-"),
            "B-",  Set.of("B-", "O-"),
            "AB+", Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
            "AB-", Set.of("A-", "B-", "AB-", "O-"),
            "O+",  Set.of("O+", "O-"),
            "O-",  Set.of("O-"));

    @Mock BloodGroupRepository groups;
    @Mock CompatibilityRepository compat;

    private BloodGroupService service;
    private final Map<String, BloodGroup> byCode = new HashMap<>();
    private final Map<Long, BloodGroup> byId = new HashMap<>();

    @BeforeEach
    void setUp() {
        for (int i = 0; i < CODES.size(); i++) {
            BloodGroup g = new BloodGroup((long) (i + 1), CODES.get(i));
            byCode.put(g.getCode(), g);
            byId.put(g.getId(), g);
        }
        when(groups.findByCodeIgnoreCase(anyString())).thenAnswer(inv ->
                Optional.ofNullable(byCode.get(((String) inv.getArgument(0)).toUpperCase())));
        when(groups.findAllById(any())).thenAnswer(inv -> {
            List<BloodGroup> out = new ArrayList<>();
            for (Long id : (Iterable<Long>) inv.getArgument(0)) {
                if (byId.containsKey(id)) out.add(byId.get(id));
            }
            Collections.shuffle(out); // service must not depend on repository ordering
            return out;
        });
        when(compat.findDonorGroupIdsByRecipient(anyLong())).thenAnswer(inv -> {
            String recipient = byId.get((Long) inv.getArgument(0)).getCode();
            return MATRIX.get(recipient).stream()
                    .map(c -> byCode.get(c).getId()).sorted().collect(Collectors.toList());
        });
        service = new BloodGroupService(groups, compat);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"})
    void compatibleDonorGroups_returnsExpectedSetWithExactGroupFirst(String recipient) {
        List<String> codes = service.compatibleDonorGroups(recipient).stream()
                .map(BloodGroupResponse::code).toList();

        assertThat(codes.get(0)).isEqualTo(recipient);
        assertThat(codes).doesNotHaveDuplicates();
        assertThat(new HashSet<>(codes)).isEqualTo(MATRIX.get(recipient));
    }

    @ParameterizedTest
    @ValueSource(strings = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"})
    void compatibleDonorGroupIds_nonEmergency_returnsOnlyExactGroup(String recipient) {
        Long id = byCode.get(recipient).getId();
        assertThat(service.compatibleDonorGroupIds(id, false)).containsExactly(id);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"})
    void compatibleDonorGroupIds_emergency_exactFirstThenOthers(String recipient) {
        Long id = byCode.get(recipient).getId();
        List<Long> ids = service.compatibleDonorGroupIds(id, true);

        Set<String> expected = MATRIX.get(recipient);
        assertThat(ids.get(0)).isEqualTo(id);
        assertThat(ids).doesNotHaveDuplicates().hasSize(expected.size());
        assertThat(ids.stream().map(i -> byId.get(i).getCode()).collect(Collectors.toSet())).isEqualTo(expected);
    }

    @Test
    void abPositive_receivesFromAllEightGroups() {
        assertThat(service.compatibleDonorGroups("AB+")).hasSize(8);
    }

    @Test
    void oNegative_receivesOnlyFromONegative() {
        assertThat(service.compatibleDonorGroups("O-")).extracting(BloodGroupResponse::code).containsExactly("O-");
    }

    @Test
    void findByCode_isCaseInsensitiveAndRestoresPlusFromSpace() {
        assertThat(service.findByCode("ab+").getCode()).isEqualTo("AB+");
        assertThat(service.findByCode("AB ").getCode()).isEqualTo("AB+");
        assertThat(service.findByCode("o-").getCode()).isEqualTo("O-");
    }

    @Test
    void unknownCode_throwsResourceNotFound() {
        assertThatThrownBy(() -> service.findByCode("XX")).isInstanceOf(ResourceNotFound.class);
        assertThatThrownBy(() -> service.compatibleDonorGroups("C+")).isInstanceOf(ResourceNotFound.class);
    }

    @Test
    void list_returnsAllEightOrderedById() {
        when(groups.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(new ArrayList<>(byId.values().stream()
                        .sorted(Comparator.comparing(BloodGroup::getId)).toList()));
        assertThat(service.list()).extracting(BloodGroupResponse::code).containsExactlyElementsOf(CODES);
    }
}
