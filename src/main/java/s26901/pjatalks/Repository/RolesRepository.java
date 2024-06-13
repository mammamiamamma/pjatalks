package s26901.pjatalks.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import s26901.pjatalks.Entity.UserRole;

import javax.management.relation.Role;

public interface RolesRepository extends MongoRepository<UserRole, String> {
    UserRole findByName(String name);
}
