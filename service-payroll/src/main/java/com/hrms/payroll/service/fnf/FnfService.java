package com.hrms.payroll.service.fnf;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.payroll.service.statutory.GratuityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Full & Final settlement engine. Orchestrates earnings, statutory dues,
 * recoveries and outputs a settlement document. Inputs supplied by caller
 * (numbers come from leave-service, payroll, expense, loan tables — typically
 * fetched via Feign in real flow).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FnfService {

    private final GratuityCalculator gratuity;
    private final EventPublisher events;

    @Transactional
    public FullAndFinalSettlement compute(FnfInput in) {
        List<FullAndFinalSettlement.FnfLine> lines = new ArrayList<>();

        BigDecimal monthly = in.lastDrawnBasic().add(in.lastDrawnDa());
        BigDecimal encashmentPerDay = monthly.divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
        BigDecimal encashment = encashmentPerDay.multiply(in.unusedLeaveDays())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalSalary = monthly
                .multiply(new BigDecimal(in.workedDaysInFinalMonth()))
                .divide(new BigDecimal(in.totalDaysInFinalMonth()), 2, RoundingMode.HALF_UP);

        GratuityCalculator.Result grat = gratuity.calculate(monthly, in.joinDate(), in.exitDate());

        BigDecimal earnings = finalSalary.add(encashment).add(grat.amount())
                .add(zeroIfNull(in.bonusPayout())).add(zeroIfNull(in.reimbursementsPayout()));
        BigDecimal deductions = zeroIfNull(in.noticePeriodRecovery())
                .add(zeroIfNull(in.loanRecovery()))
                .add(zeroIfNull(in.advanceRecovery()))
                .add(zeroIfNull(in.tdsWithheld()));

        addLine(lines, "Final Month Pro-rated Salary", "EARNING", finalSalary,
                in.workedDaysInFinalMonth() + " / " + in.totalDaysInFinalMonth() + " days");
        addLine(lines, "Leave Encashment", "EARNING", encashment,
                in.unusedLeaveDays() + " days at " + encashmentPerDay + " / day");
        addLine(lines, "Gratuity", "EARNING", grat.amount(),
                grat.eligible() ? grat.countedYears() + " years" : grat.reason());
        if (in.bonusPayout() != null && in.bonusPayout().signum() != 0)
            addLine(lines, "Bonus / Variable Pay", "EARNING", in.bonusPayout(), null);
        if (in.reimbursementsPayout() != null && in.reimbursementsPayout().signum() != 0)
            addLine(lines, "Pending Reimbursements", "EARNING", in.reimbursementsPayout(), null);
        if (in.noticePeriodRecovery() != null && in.noticePeriodRecovery().signum() != 0)
            addLine(lines, "Notice Period Recovery", "DEDUCTION", in.noticePeriodRecovery(), null);
        if (in.loanRecovery() != null && in.loanRecovery().signum() != 0)
            addLine(lines, "Loan Recovery", "DEDUCTION", in.loanRecovery(), null);
        if (in.advanceRecovery() != null && in.advanceRecovery().signum() != 0)
            addLine(lines, "Advance Recovery", "DEDUCTION", in.advanceRecovery(), null);
        if (in.tdsWithheld() != null && in.tdsWithheld().signum() != 0)
            addLine(lines, "TDS", "DEDUCTION", in.tdsWithheld(), null);

        BigDecimal net = earnings.subtract(deductions).setScale(2, RoundingMode.HALF_UP);

        FullAndFinalSettlement fnf = FullAndFinalSettlement.builder()
                .tenantId(in.tenantId()).employeeId(in.employeeId())
                .joinDate(in.joinDate()).exitDate(in.exitDate())
                .lastWorkingDate(in.lastWorkingDate())
                .finalMonthSalary(finalSalary)
                .leaveEncashment(encashment).leaveEncashmentDays(in.unusedLeaveDays())
                .gratuity(grat.amount())
                .noticePeriodRecovery(in.noticePeriodRecovery())
                .loanRecovery(in.loanRecovery())
                .advanceRecovery(in.advanceRecovery())
                .reimbursementsPayout(in.reimbursementsPayout())
                .bonusPayout(in.bonusPayout())
                .tdsWithheld(in.tdsWithheld())
                .netSettlement(net)
                .lines(lines)
                .build();

        events.publish(Topics.OFFBOARDING, DomainEvent.of("fnf.computed", "payroll",
                in.tenantId(), in.employeeId().toString(), "FnF",
                Map.of("net", net, "gratuity", grat.amount(), "encashment", encashment)));
        return fnf;
    }

    private void addLine(List<FullAndFinalSettlement.FnfLine> lines, String label, String type,
                         BigDecimal amount, String note) {
        if (amount == null) return;
        lines.add(FullAndFinalSettlement.FnfLine.builder()
                .label(label).type(type).amount(amount).note(note).build());
    }

    private BigDecimal zeroIfNull(BigDecimal b) { return b == null ? BigDecimal.ZERO : b; }

    public record FnfInput(
            String tenantId, UUID employeeId,
            LocalDate joinDate, LocalDate exitDate, LocalDate lastWorkingDate,
            BigDecimal lastDrawnBasic, BigDecimal lastDrawnDa,
            BigDecimal unusedLeaveDays,
            int workedDaysInFinalMonth, int totalDaysInFinalMonth,
            BigDecimal bonusPayout,
            BigDecimal reimbursementsPayout,
            BigDecimal noticePeriodRecovery,
            BigDecimal loanRecovery,
            BigDecimal advanceRecovery,
            BigDecimal tdsWithheld
    ) {}
}
