package com.bloodbank.bloodgroup;

import com.bloodbank.bloodgroup.dto.BloodGroupResponse;
import com.bloodbank.exception.ResourceNotFound;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BloodGroupService {

    private final BloodGroupRepository groups;
    private final CompatibilityRepository compatibility;

    public BloodGroupService(BloodGroupRepository groups, CompatibilityRepository compatibility) {
        this.groups = groups;
        this.compatibility = compatibility;
    }

    /** Case-insensitive. Throws ResourceNotFound (404 NOT_FOUND) for unknown codes. */
    public BloodGroup findByCode(String code) {
        return groups.findByCodeIgnoreCase(normalize(code))
                .orElseThrow(() -> new ResourceNotFound("Blood group not found: " + code));
    }

    public List<BloodGroupResponse> list() {
        return groups.findAll(Sort.by("id")).stream().map(BloodGroupResponse::from).toList();
    }

    /** All donor groups the recipient can receive from; the exact group is first. */
    public List<BloodGroupResponse> compatibleDonorGroups(String recipientCode) {
        BloodGroup recipient = findByCode(recipientCode);
        List<Long> ids = compatibleDonorGroupIds(recipient.getId(), true);
        Map<Long, BloodGroup> byId = groups.findAllById(ids).stream()
                .collect(Collectors.toMap(BloodGroup::getId, Function.identity()));
        return ids.stream().map(byId::get).filter(Objects::nonNull).map(BloodGroupResponse::from).toList();
    }

    /**
     * emergency=false: only the exact group.
     * emergency=true: all compatible groups, exact group FIRST, the rest ordered by id.
     */
    public List<Long> compatibleDonorGroupIds(Long recipientGroupId, boolean emergency) {
        if (!emergency) {
            return List.of(recipientGroupId);
        }
        List<Long> compatible = compatibility.findDonorGroupIdsByRecipient(recipientGroupId);
        List<Long> result = new ArrayList<>(compatible.size() + 1);
        result.add(recipientGroupId);
        for (Long id : compatible) {
            if (!id.equals(recipientGroupId)) {
                result.add(id);
            }
        }
        return result;
    }

    /** Upper-cases, trims; a trailing space (a '+' decoded as space by some clients) is restored to '+'. */
    static String normalize(String code) {
        if (code == null) return "";
        String c = code.toUpperCase();
        if (c.endsWith(" ")) {
            return c.trim() + "+";
        }
        return c.trim();
    }
}
