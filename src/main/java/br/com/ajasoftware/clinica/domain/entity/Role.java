package br.com.ajasoftware.clinica.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "table_perfil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", unique = true, nullable = false, length = 50)
    private String name;
}