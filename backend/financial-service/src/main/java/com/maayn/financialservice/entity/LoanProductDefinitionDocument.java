package com.maayn.financialservice.entity;

import com.maayn.financialservice.domain.loan.LoanInterestMethod;
import com.maayn.financialservice.domain.loan.LoanRateMode;
import com.maayn.financialservice.domain.loan.LoanRepaymentMethod;
import com.maayn.financialservice.domain.loan.LoanType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "loan_product_definitions")
@Getter
@Setter
@NoArgsConstructor
public class LoanProductDefinitionDocument {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String code;

    private String name;

    private LoanType loanType;

    private LoanInterestMethod interestMethod;

    private LoanRateMode rateMode;

    private LoanRepaymentMethod repaymentMethod;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal baseAnnualRate;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal monthlyFee;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal penaltyRate;

    private Integer defaultTenureMonths;

    private Integer minimumTenureMonths;

    private Integer maximumTenureMonths;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal minimumPrincipal;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal maximumPrincipal;

    private String variableRateIndexCode;

    private Boolean active = Boolean.TRUE;
}
