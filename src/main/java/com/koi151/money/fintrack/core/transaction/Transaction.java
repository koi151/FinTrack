package com.koi151.money.fintrack.core.transaction;

import com.koi151.money.fintrack.common.domain.BaseEntity;
import com.koi151.money.fintrack.core.category.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SoftDelete(columnName = "is_deleted")
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private UUID userId; // temporary hard code during development process

    @Column(nullable = false)
    private Instant transactionDate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String note;

    public void changeCategory(Category newCategory) {
        if (newCategory == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        // only update if the category is different from the current one
        if (this.category == null || !this.category.getId().equals(newCategory.getId())) {
            this.category = newCategory;
        }
    }
}

