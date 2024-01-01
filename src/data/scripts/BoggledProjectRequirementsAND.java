package data.scripts;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.campaign.econ.boggledTools;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class BoggledProjectRequirementsAND implements Iterable<BoggledProjectRequirementsAND.RequirementWithTooltipOverride> {
    public static class RequirementWithTooltipOverride {
        private BoggledProjectRequirementsOR requirements;
        private String tooltipOverride;

        public RequirementWithTooltipOverride(BoggledProjectRequirementsOR requirements, String tooltipOverride) {
            this.requirements = requirements;
            this.tooltipOverride = tooltipOverride;
        }

        public boolean checkRequirement(MarketAPI market) {
            return requirements.checkRequirement(market);
        }
        public String getTooltip(LinkedHashMap<String, String> tokenReplacements) {
            String ret = tooltipOverride;
            if (tooltipOverride.isEmpty()) {
                ret = requirements.getTooltip();
            }
            requirements.addTokenReplacements(tokenReplacements);
            ret = boggledTools.doTokenReplacement(ret, tokenReplacements);
            return ret;
        }
    }

    private final ArrayList<RequirementWithTooltipOverride> requirementsOR;

    @NotNull
    @Override
    public Iterator<RequirementWithTooltipOverride> iterator() {
        return requirementsOR.iterator();
    }

    public boolean add(RequirementWithTooltipOverride req) {
        return requirementsOR.add(req);
    }

    public boolean requirementsMet(MarketAPI market) {
        for (BoggledProjectRequirementsAND.RequirementWithTooltipOverride req : requirementsOR) {
            if (!req.checkRequirement(market)) {
                return false;
            }
        }
        return true;
    }

    public BoggledProjectRequirementsAND(ArrayList<RequirementWithTooltipOverride> requirementsOR) {
        this.requirementsOR = requirementsOR;
    }
}
