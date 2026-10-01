package com.bloodbank.bloodgroup;

import com.bloodbank.bloodgroup.dto.BloodGroupResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Use %2B for '+' in the path, e.g. GET /blood-groups/AB%2B/compatible-donors ('-' needs no encoding). */
@RestController
@RequestMapping("/blood-groups")
@Tag(name = "Blood groups")
@SecurityRequirement(name = "bearerAuth")
public class BloodGroupController {

    private final BloodGroupService service;

    public BloodGroupController(BloodGroupService service) { this.service = service; }

    @GetMapping
    public List<BloodGroupResponse> list() { return service.list(); }

    @GetMapping("/{code}/compatible-donors")
    public List<BloodGroupResponse> compatibleDonors(@PathVariable String code) {
        return service.compatibleDonorGroups(code);
    }
}
