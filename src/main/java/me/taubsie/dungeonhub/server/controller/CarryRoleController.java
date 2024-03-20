package me.taubsie.dungeonhub.server.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.taubsie.dungeonhub.common.model.carryrole.CarryRoleModel;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.CarryRoleRequirementModel;
import me.taubsie.dungeonhub.server.service.CarryRoleRequirementService;
import me.taubsie.dungeonhub.server.service.CarryRoleService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/v1/carry-roles/")
@AllArgsConstructor
@Getter(AccessLevel.PROTECTED)
//TODO security
public class CarryRoleController {

    private final CarryRoleService roleService;
    private final CarryRoleRequirementService roleRequirementService;

    @GetMapping("all")
    public @NotNull CarryRoleModel[] getRoles()
    {
        return getRoleService().findAll().toArray(CarryRoleModel[]::new);
    }

    @GetMapping("requirements/{id}")
    public @NotNull CarryRoleRequirementModel[] getRequirement(@PathVariable("id") long roleId)
    {
        Supplier<ResponseStatusException> badRequest = () -> new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return getRoleService().loadById(roleId).map(CarryRoleModel::getRequirements).orElseThrow(badRequest);
    }
}
