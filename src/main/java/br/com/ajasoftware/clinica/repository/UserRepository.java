package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.filter.users.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.login = :login")
    Optional<User> findByLoginWithRoles(@Param("login") String login);

    boolean existsByLogin(String login);

    @Query("SELECT u FROM User u WHERE " +
            " (:#{#filter.id} IS NULL OR u.id = :#{#filter.id}) " +
            "AND (:#{#filter.name} IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')))")
    Page<User> findWithFilter(@Param("filter") UserFilter filter, Pageable pageable);
}