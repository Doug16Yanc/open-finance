package tech.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "permission_groups")
public class PermissionGroup extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "group_name", nullable = false, unique = true)
    @NotBlank
    public String groupName;

    @Column(nullable = false)
    @NotBlank
    public String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "permissions", nullable = false, columnDefinition = "text[]")
    @NotNull
    public List<String> permissions;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "required_permissions", columnDefinition = "text[]")
    public List<String> requiredPermissions;

    @Column(name = "active", nullable = false)
    public boolean active = true;

    public static PermissionGroup findByGroupName(String groupName) {
        return find("groupName", groupName).firstResult();
    }

    public static List<PermissionGroup> findAllActive() {
        return list("active", true);
    }

    public static List<PermissionGroup> findByPermission(String permission) {
        return list("?1 MEMBER OF permissions", permission);
    }

    public boolean containsPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean hasRequiredPermissions() {
        return requiredPermissions != null && !requiredPermissions.isEmpty();
    }

    public boolean isSatisfiedBy(List<String> requestedPermissions) {
        if (!hasRequiredPermissions()) return true;
        return new HashSet<>(requestedPermissions).containsAll(requiredPermissions);
    }
}