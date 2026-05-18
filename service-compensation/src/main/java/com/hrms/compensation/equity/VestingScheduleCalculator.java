package com.hrms.compensation.equity;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes per-tranche vesting schedule for a stock grant. Common patterns supported:
 *   - 4-year monthly with 1-year cliff (standard SV ESOP)
 *   - 4-year quarterly
 *   - Custom (cliff_months + total_vesting_months + vest_frequency_months on the grant)
 *
 * Output is one row per vesting date with units vested cumulatively + that-tranche units.
 */
@Service
public class VestingScheduleCalculator {

    public List<VestingTranche> compute(StockGrant g, LocalDate asOf) {
        List<VestingTranche> tranches = new ArrayList<>();
        int total = g.getTotalUnits() == null ? 0 : g.getTotalUnits();
        int cliff = g.getCliffMonths() == null ? 12 : g.getCliffMonths();
        int totalMonths = g.getTotalVestingMonths() == null ? 48 : g.getTotalVestingMonths();
        int freq = g.getVestFrequencyMonths() == null ? 1 : g.getVestFrequencyMonths();
        if (total <= 0 || totalMonths <= 0 || g.getVestingStartDate() == null) return tranches;

        int monthsPostCliff = totalMonths - cliff;
        int postCliffTranches = freq == 0 ? 0 : Math.max(0, monthsPostCliff / freq);
        int cliffUnits = Math.round((float) total * cliff / totalMonths);
        int perTranche = postCliffTranches == 0 ? 0 : (total - cliffUnits) / postCliffTranches;
        int remainder = (total - cliffUnits) - (perTranche * postCliffTranches);

        // cliff tranche
        LocalDate cliffDate = g.getVestingStartDate().plusMonths(cliff);
        int cumulative = cliffUnits;
        tranches.add(new VestingTranche(cliffDate, cliffUnits, cumulative,
                isVested(cliffDate, asOf)));

        for (int i = 1; i <= postCliffTranches; i++) {
            LocalDate vd = cliffDate.plusMonths((long) i * freq);
            int u = perTranche + (i == postCliffTranches ? remainder : 0);
            cumulative += u;
            tranches.add(new VestingTranche(vd, u, cumulative, isVested(vd, asOf)));
        }
        return tranches;
    }

    public int vestedUnitsAsOf(StockGrant g, LocalDate asOf) {
        int vested = 0;
        for (VestingTranche t : compute(g, asOf)) {
            if (t.vested()) vested = t.cumulativeUnits();
        }
        return vested;
    }

    private boolean isVested(LocalDate vestDate, LocalDate asOf) {
        return asOf != null && !vestDate.isAfter(asOf);
    }

    public record VestingTranche(LocalDate vestDate, int trancheUnits, int cumulativeUnits, boolean vested) {}
}
