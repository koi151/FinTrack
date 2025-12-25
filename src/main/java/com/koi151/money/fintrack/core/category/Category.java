package com.koi151.money.fintrack.core.category;

import com.koi151.money.fintrack.common.domain.BaseEntity;
import com.koi151.money.fintrack.core.transaction.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SoftDelete(columnName = "is_deleted")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100) // unique ensured by partial index
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(length = 50)
    private String iconCode;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Invalid color code")
    private String colorCode;
}
