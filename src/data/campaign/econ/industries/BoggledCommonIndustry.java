package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledCommoditySupplyDemand;
import data.scripts.BoggledTerraformingProject;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class BoggledCommonIndustry {
    /*
    This class cannot be made into a base class of any of the Boggled industries because Remnant Station and Cryosanctum gets in the way, may be able to do something else though
     */
    private final String industryId;
    private final String industryTooltip;

    public ArrayList<BoggledTerraformingProject.ProjectInstance> projects;
    private ArrayList<BoggledCommoditySupplyDemand.CommoditySupplyAndDemand> commoditySupplyAndDemands;
    private ArrayList<BoggledCommoditySupplyDemand.CommodityDemandShortageEffect> commodityDemandShortageEffects;

    private boolean functional = true;

    private boolean building = false;
    private boolean built = false;

    public BoggledCommonIndustry(String industryId, String industryTooltip, ArrayList<BoggledTerraformingProject> projects, ArrayList<BoggledCommoditySupplyDemand.CommoditySupplyAndDemand> commoditySupplyAndDemands, ArrayList<BoggledCommoditySupplyDemand.CommodityDemandShortageEffect> commodityDemandShortageEffects) {
        this.industryId = industryId;
        this.industryTooltip = industryTooltip;

        this.projects = new ArrayList<>(projects.size());
        for (BoggledTerraformingProject project : projects) {
            this.projects.add(new BoggledTerraformingProject.ProjectInstance(project));
        }

        this.commoditySupplyAndDemands = commoditySupplyAndDemands;
        this.commodityDemandShortageEffects = commodityDemandShortageEffects;
    }

    protected Object readResolve() {
        Global.getLogger(this.getClass()).info("Doing readResolve for " + industryId + " " + industryTooltip);
        BoggledCommonIndustry that = boggledTools.getIndustryProject(industryId);
        this.projects = that.projects;
        this.commoditySupplyAndDemands = that.commoditySupplyAndDemands;
        this.commodityDemandShortageEffects = that.commodityDemandShortageEffects;
        return this;
    }

    public void overridesFromJSON(JSONObject data) throws JSONException {

    }

    public void advance(float amount, BaseIndustry industry) {
        if (!built) {
            return;
        }

        if (industry.isDisrupted() || !marketSuitableBoth(industry.getMarket())) {
            return;
        }

        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            project.advance(industry.getMarket());
        }
    }

    public int getPercentComplete(int projectIndex, MarketAPI market) {
        return (int) Math.min(99, ((float)projects.get(projectIndex).getDaysCompleted() / projects.get(projectIndex).getProject().getModifiedProjectDuration(getFocusMarketOrMarket(market))) * 100);
    }

    public int getDaysRemaining(int projectIndex, BaseIndustry industry) {
        BoggledTerraformingProject.ProjectInstance project = projects.get(projectIndex);
        return project.getProject().getModifiedProjectDuration(getFocusMarketOrMarket(industry.getMarket())) - project.getDaysCompleted();
    }

    public void tooltipIncomplete(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (!(marketSuitableBoth(industry.getMarket()) && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY && mode != Industry.IndustryTooltipMode.QUEUED)) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    public void tooltipComplete(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if(!(!marketSuitableBoth(industry.getMarket()) && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY && mode != Industry.IndustryTooltipMode.QUEUED && !industry.isBuilding())) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    public void tooltipDisrupted(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (!(industry.isDisrupted() && marketSuitableBoth(industry.getMarket()) && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY && mode != Industry.IndustryTooltipMode.QUEUED && !industry.isBuilding())) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    private boolean marketSuitableVisible(MarketAPI market) {
        boolean anyProjectValid = false;
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            anyProjectValid = anyProjectValid || project.getProject().requirementsMet(market);
        }
        return anyProjectValid;
    }

    private boolean marketSuitableHidden(MarketAPI market) {
        boolean anyProjectValid = false;
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            anyProjectValid = anyProjectValid || project.getProject().requirementsHiddenMet(market);
        }
        return anyProjectValid;
    }

    public boolean marketSuitableBoth(MarketAPI market) {
        return marketSuitableHidden(market) && marketSuitableVisible(market);
    }

    public static MarketAPI getFocusMarketOrMarket(MarketAPI market) {
        MarketAPI ret = market.getPrimaryEntity().getOrbitFocus().getMarket();
        if (ret == null) {
            return market;
        }
        return ret;
    }

    /*
    These are the main reason for this class
    Throw an instance of this on a type and just delegate to it for handling these BaseIndustry functions
     */
    public void startBuilding(BaseIndustry industry) {
        building = true;
        built = false;
    }

    public void startUpgrading(BaseIndustry industry) {
    }

    public void buildingFinished(BaseIndustry industry) {
        building = false;
        built = true;
    }

    public void upgradeFinished(BaseIndustry industry, Industry previous) {
    }

    public void finishBuildingOrUpgrading(BaseIndustry industry) {
    }

    public boolean isBuilding(BaseIndustry industry) {
        if (building) {
            return true;
        }
        if (!built) {
            // Stupid as hell but needs to be here for the industry to work same as vanilla structures
            return false;
        }
        for (int i = 0; i < projects.size(); ++i) {
            if (projects.get(i).getProject().requirementsMet(industry.getMarket()) && getDaysRemaining(i, industry) > 0) {
                return true;
            }
        }
        return false;
    }

    public void setFunctional(boolean functional) {
        this.functional = functional;
    }

    public boolean isFunctional() {
        return functional;
    }

    public boolean isUpgrading(BaseIndustry industry) {
        if (!built) {
            return false;
        }
        for (int i = 0; i < projects.size(); ++i) {
            if (projects.get(i).getProject().requirementsMet(industry.getMarket()) && getDaysRemaining(i, industry) > 0) {
                return true;
            }
        }
        return false;
    }

    public float getBuildOrUpgradeProgress(BaseIndustry industry) {
        if (industry.isDisrupted()) {
            return 0.0f;
        } else if (building || !built) {
            return Math.min(1.0f, industry.getBuildProgress() / industry.getBuildTime());
        }

        float progress = 0f;
        for (int i = 0; i < projects.size(); ++i) {
            progress = Math.max(getPercentComplete(i, industry.getMarket()) / 100f, progress);
        }
        return progress;
    }

    public String getBuildOrUpgradeDaysText(BaseIndustry industry) {
        int daysRemain;
        if (industry.isDisrupted()) {
            daysRemain = (int)(industry.getDisruptedDays());
        } else if (building || !built) {
            daysRemain = (int)(industry.getBuildTime() - industry.getBuildProgress());
        } else {
            daysRemain = Integer.MAX_VALUE;
            for (int i = 0; i < projects.size(); ++i) {
                daysRemain = Math.min(getDaysRemaining(i, industry), daysRemain);
            }
        }
        String dayOrDays = daysRemain == 1 ? "day" : "days";
        return daysRemain + " " + dayOrDays;
    }

    public String getBuildOrUpgradeProgressText(BaseIndustry industry) {
        String prefix;
        if (industry.isDisrupted()) {
            prefix = "Disrupted";
        } else if (building || !built) {
            prefix = "Building";
        } else {
            prefix = this.industryTooltip;
        }
        return prefix + ": " + getBuildOrUpgradeDaysText(industry) + " left";
    }

    public boolean isAvailableToBuild(BaseIndustry industry) {
        if (!projects.isEmpty()) {
            boolean anyEnabled = false;
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                if (project.getProject().isEnabled()) {
                    anyEnabled = true;
                    break;
                }
            }
            if (!anyEnabled) {
                return false;
            }

            boolean noneMet = true;
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                if (project.getProject().requirementsMet(industry.getMarket())) {
                    noneMet = false;
                    break;
                }
            }
            if (noneMet) {
                return false;
            }
        }

        return marketSuitableVisible(industry.getMarket()) && marketSuitableHidden(industry.getMarket());
    }

    public boolean showWhenUnavailable(BaseIndustry industry) {
        if (!projects.isEmpty()) {
            boolean anyEnabled = false;
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                if (project.getProject().isEnabled()) {
                    anyEnabled = true;
                    break;
                }
            }
            if (!anyEnabled) {
                return false;
            }

            boolean allHidden = true;
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                if (project.getProject().requirementsHiddenMet(industry.getMarket())) {
                    allHidden = false;
                    break;
                }
            }
            if (allHidden) {
                return false;
            }
        }

        return marketSuitableHidden(industry.getMarket());
    }

    public String getUnavailableReason(BaseIndustry industry) {
        return boggledTools.getUnavailableReason(projects, industryTooltip, industry.getMarket(), boggledTools.getTokenReplacements(industry.getMarket()));
    }

    public void apply(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
        for (BoggledCommoditySupplyDemand.CommoditySupplyAndDemand commoditySupplyAndDemand : commoditySupplyAndDemands) {
            commoditySupplyAndDemand.applySupplyDemand(industry);
        }

        for (BoggledCommoditySupplyDemand.CommodityDemandShortageEffect commodityDemandShortageEffect : commodityDemandShortageEffects) {
            commodityDemandShortageEffect.applyShortageEffect(industry, industryInterface);
        }

        if (!industry.isFunctional()) {
            industry.getAllSupply().clear();
            industry.unapply();
        }
    }

    public void addRightAfterDescriptionSection(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        float pad = 10.0f;
        for (int i = 0; i < projects.size(); ++i) {
            BoggledTerraformingProject project = projects.get(i).getProject();
            if (project.requirementsMet(industry.getMarket())) {
                Map<String, String> tokenReplacements = getTokenReplacements(industry.getMarket(), i);
                String[] highlights = project.getIncompleteMessageHighlights(tokenReplacements);
                addFormatTokenReplacement(tokenReplacements);
                String incompleteMessage = boggledTools.doTokenReplacement(project.getIncompleteMessage(), tokenReplacements);
                tooltipIncomplete(industry, tooltip, mode, incompleteMessage, pad, Misc.getHighlightColor(), highlights);
                tooltipDisrupted(industry, tooltip, mode, "Here's a message", pad, Misc.getNegativeHighlightColor());
            }
        }
    }

    public boolean hasPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        for (BoggledCommoditySupplyDemand.CommoditySupplyAndDemand commoditySupplyAndDemand : commoditySupplyAndDemands) {
            if (commoditySupplyAndDemand.isEnabled()) {
                return true;
            }
        }

        for (BoggledCommoditySupplyDemand.CommodityDemandShortageEffect commodityDemandShortageEffect : commodityDemandShortageEffects) {
            if (commodityDemandShortageEffect.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void addPostDemandSection(BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        for (BoggledCommoditySupplyDemand.CommoditySupplyAndDemand commoditySupplyAndDemand : commoditySupplyAndDemands) {
            if (commoditySupplyAndDemand.isEnabled()) {
                commoditySupplyAndDemand.addPostDemandSection(industry.getCurrentName(), industry, tooltip, hasDemand, mode);
            }
        }

        for (BoggledCommoditySupplyDemand.CommodityDemandShortageEffect commodityDemandShortageEffect : commodityDemandShortageEffects) {
            if (commodityDemandShortageEffect.isEnabled()) {
                commodityDemandShortageEffect.addPostDemandSection(industry.getCurrentName(), industry, tooltip, hasDemand, mode);
            }
        }
    }

    private Map<String, String> getTokenReplacements(MarketAPI market, int projectIndex) {
        Map<String, String> ret = boggledTools.getTokenReplacements(market);
        ret.put("$percentComplete", Integer.toString(getPercentComplete(projectIndex, market)));
        return ret;
    }

    private void addFormatTokenReplacement(Map<String, String> tokenReplacements) {
        tokenReplacements.put("%", "%%");
    }
}