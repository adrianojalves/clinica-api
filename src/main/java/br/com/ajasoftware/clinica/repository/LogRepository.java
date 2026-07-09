package br.com.ajasoftware.clinica.repository;

import br.com.ajasoftware.clinica.domain.entity.log.Log;
import br.com.ajasoftware.clinica.domain.filter.log.LogFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    @Query(value = """
            SELECT l FROM Log l
            LEFT JOIN FETCH l.user
            WHERE (:#{#filter.id}        IS NULL OR l.id = :#{#filter.id})
            AND   (:#{#filter.name}      IS NULL OR LOWER(l.log) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')))
            AND   (:#{#filter.log}       IS NULL OR LOWER(l.log) LIKE LOWER(CONCAT('%', :#{#filter.log}, '%')))
            AND   (:#{#filter.userId}    IS NULL OR l.user.id = :#{#filter.userId})
            AND   (:#{#filter.userName}  IS NULL OR LOWER(l.user.name) LIKE LOWER(CONCAT('%', :#{#filter.userName}, '%')))
            AND   (:startDate            IS NULL OR l.dateTime >= :startDate)
            AND   (:endDate              IS NULL OR l.dateTime <= :endDate)
            """,
            countQuery = """
            SELECT COUNT(l) FROM Log l
            WHERE (:#{#filter.id}        IS NULL OR l.id = :#{#filter.id})
            AND   (:#{#filter.name}      IS NULL OR LOWER(l.log) LIKE LOWER(CONCAT('%', :#{#filter.name}, '%')))
            AND   (:#{#filter.log}       IS NULL OR LOWER(l.log) LIKE LOWER(CONCAT('%', :#{#filter.log}, '%')))
            AND   (:#{#filter.userId}    IS NULL OR l.user.id = :#{#filter.userId})
            AND   (:#{#filter.userName}  IS NULL OR LOWER(l.user.name) LIKE LOWER(CONCAT('%', :#{#filter.userName}, '%')))
            AND   (:startDate            IS NULL OR l.dateTime >= :startDate)
            AND   (:endDate              IS NULL OR l.dateTime <= :endDate)
            """)
    Page<Log> findWithFilter(
            @Param("filter") LogFilter filter,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
