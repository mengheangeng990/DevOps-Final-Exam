package net.orderzone.idcard.repository;

import net.orderzone.idcard.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUuid(String uuid);
    Optional<Profile> findByRegistrationNumber(String registrationNumber);
    boolean existsByUuid(String uuid);
    boolean existsByRegistrationNumber(String registrationNumber);
    List<Profile> findByFullNameContainingIgnoreCase(String fullName);
    List<Profile> findByDepartmentContainingIgnoreCase(String department);
}
