package com.maayn.notificationservice.dto;

import com.maayn.notificationservice.enums.ComparisonOperator;
import com.maayn.notificationservice.enums.LogicalOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Actually it must be in veld
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Condition {
    private LogicalOperator logicalOperator; // AND // OR // I think this enum must be in veld too
    private List<Condition> conditions;
    private String field;
    private ComparisonOperator comparison;
    private Object value;
    public Boolean isLeaf() {
        return field != null;
    }
}
