package tech.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import tech.domain.entity.PermissionGroup;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PermissionGroupService {

    public List<PermissionGroup> listActive() {
        return PermissionGroup.findAllActive();
    }

    public PermissionGroup findByName(String groupName) {
        var group = PermissionGroup.findByGroupName(groupName);

        if (group == null) {
            throw new NotFoundException("Grupo não encontrado: " + groupName);
        }

        return group;
    }

    public PermissionResolutionResult resolve(List<String> requestedPermissions) {
        var allGroups   = PermissionGroup.findAllActive();
        var allKnown    = allGroups.stream()
                .flatMap(g -> g.permissions.stream())
                .distinct()
                .toList();

        var invalid = requestedPermissions.stream()
                .filter(p -> !allKnown.contains(p))
                .toList();

        var missing = new ArrayList<String>();
        var matchedGroups = new ArrayList<String>();

        for (var group : allGroups) {
            boolean touched = group.permissions.stream()
                    .anyMatch(requestedPermissions::contains);
            if (touched) {
                matchedGroups.add(group.groupName);
                if (group.hasRequiredPermissions()) {
                    group.requiredPermissions.stream()
                            .filter(dep -> !requestedPermissions.contains(dep))
                            .filter(dep -> !missing.contains(dep))
                            .forEach(missing::add);
                }
            }
        }

        var resolved = new java.util.ArrayList<>(requestedPermissions);
        missing.stream().filter(d -> !resolved.contains(d)).forEach(resolved::add);

        return new PermissionResolutionResult(
                requestedPermissions,
                invalid,
                missing,
                resolved,
                matchedGroups
        );
    }

    public record PermissionResolutionResult(
            List<String> requested,
            List<String> invalid,
            List<String> missingDependencies,
            List<String> resolved,
            List<String> matchedGroups
    ) {}
}
