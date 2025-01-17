package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;

public class BoggledStationConstructors {
    public static abstract class StationConstructionData {
        private String stationType;

        private List<String> industriesToQueue;

        public StationConstructionData(String stationType, List<String> industriesToQueue) {
            this.stationType = stationType;
            this.industriesToQueue = industriesToQueue;
        }

        public String getStationType() {
            return stationType;
        }

        protected MarketAPI createDefaultMarket(SectorEntityToken stationEntity, String hostName, String marketType) {
            MarketAPI market = Global.getFactory().createMarket(stationEntity.getId() + hostName + marketType, stationEntity.getName(), 3);

            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            market.setPrimaryEntity(stationEntity);

            market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
            market.setPlayerOwned(true);

            market.addCondition(Conditions.POPULATION_3);

            market.addCondition(boggledTools.BoggledConditions.spriteControllerConditionId);
            market.addCondition(boggledTools.BoggledConditions.crampedQuartersConditionId);

            market.addIndustry(Industries.POPULATION);
            market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
            for (String industryToQueue : industriesToQueue) {
                market.getConstructionQueue().addToEnd(industryToQueue, 0);
            }

            stationEntity.setMarket(market);

            Global.getSector().getEconomy().addMarket(market, true);

            // If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
            // Still bugged as of 0.95.1a
            Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);

            market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            StoragePlugin storage = (StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin();
            storage.setPlayerPaidToUnlock(true);
            market.addSubmarket(Submarkets.LOCAL_RESOURCES);

            market.getMemoryWithoutUpdate().set("$startingFactionId", market.getFactionId());

            boggledTools.surveyAll(market);

            return market;
        }

        public abstract MarketAPI createMarket(SectorEntityToken stationEntity);

        public void addTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara) {
            String durationString = String.format("%,d", ctx.getProject().getModifiedProjectDuration(ctx));
            BoggledTerraformingProjectEffect.EffectTooltipPara para = new BoggledTerraformingProjectEffect.EffectTooltipPara("Building a station here will take " + durationString + " days.", "");
            para.highlights.add(durationString);
            para.highlightColors.add(Misc.getHighlightColor());
            effectTypeToPara.put("StationConstructionModifiedBuildTime", para);
        }
    }

    public static class DefaultConstructionData extends StationConstructionData {
        public DefaultConstructionData(String stationType, List<String> industriesToQueue) {
            super(stationType, industriesToQueue);
        }

        @Override
        public MarketAPI createMarket(SectorEntityToken stationEntity) {
            SectorEntityToken hostPlanet = stationEntity.getOrbitFocus();
            String hostName = "";
            if (hostPlanet != null) {
                hostName = hostPlanet.getName();
            }
            MarketAPI market = createDefaultMarket(stationEntity, hostName, "DefaultMarket");

            Global.getSoundPlayer().playUISound(boggledTools.BoggledSounds.stationConstructed, 1.0F, 1.0F);
            return market;
        }
    }

    public static class AstropolisConstructionData extends StationConstructionData {
        public AstropolisConstructionData(String stationType, List<String> industriesToQueue) {
            super(stationType, industriesToQueue);
        }

        @Override
        public MarketAPI createMarket(SectorEntityToken stationEntity) {
            SectorEntityToken hostPlanet = stationEntity.getOrbitFocus();
            MarketAPI market = createDefaultMarket(stationEntity, hostPlanet.getName(), "AstropolisMarket");

            Global.getSoundPlayer().playUISound(boggledTools.BoggledSounds.stationConstructed, 1.0F, 1.0F);
            return market;
        }
    }

    public static class MiningStationConstructionData extends StationConstructionData {
        private List<String> resourcesToHighlight;

        public MiningStationConstructionData(String stationType, List<String> industriesToQueue, List<String> resourcesToHighlight) {
            super(stationType, industriesToQueue);
            this.resourcesToHighlight = resourcesToHighlight;
        }

        @Override
        public void addTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara) {
            super.addTooltipInfo(ctx, effectTypeToPara);

            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return;
            }

            int numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(ctx.getFleet());
            String numAsteroidBeltsInSystemString = String.format("%,d", numAsteroidBeltsInSystem);
            String resourceString = boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem);
            BoggledTerraformingProjectEffect.EffectTooltipPara para = new BoggledTerraformingProjectEffect.EffectTooltipPara("There are " + numAsteroidBeltsInSystemString + " asteroid belts in the " + ctx.getStarSystem() + ". A mining station would have " + resourceString + " resources.", "");
            para.highlights.add(numAsteroidBeltsInSystemString);
            para.highlights.add(resourceString);
            para.highlightColors.add(Misc.getHighlightColor());
            para.highlightColors.add(Misc.getHighlightColor());
            effectTypeToPara.put("StationConditionsAndReason", para);
        }

        @Override
        public MarketAPI createMarket(SectorEntityToken stationEntity) {
            StarSystemAPI system = stationEntity.getStarSystem();

            MarketAPI market = createDefaultMarket(stationEntity, system.getName(), "MiningStationMarket");

            String resourceLevel = boggledTools.getMiningStationResourceString(boggledTools.getNumAsteroidTerrainsInSystem(stationEntity));
            boggledTools.addCondition(market, "ore_" + resourceLevel);
            boggledTools.addCondition(market, "rare_ore_" + resourceLevel);

            Global.getSoundPlayer().playUISound(boggledTools.BoggledSounds.stationConstructed, 1.0F, 1.0F);
            return market;
        }
    }

    public static class SiphonStationConstructionData extends StationConstructionData {
        private final List<String> resourcesToHighlight;

        public SiphonStationConstructionData(String stationType, List<String> industriesToQueue, List<String> resourcesToHighlight) {
            super(stationType,  industriesToQueue);
            this.resourcesToHighlight = resourcesToHighlight;
        }

        @Override
        public void addTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara) {
            super.addTooltipInfo(ctx, effectTypeToPara);

            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }

            for (String resource : resourcesToHighlight) {
                String resourceName = null;
                for (MarketConditionAPI condition : targetPlanet.getMarket().getConditions()) {
                    if (condition.getId().contains(resource)) {
                        resourceName = condition.getName().toLowerCase().replace(" " + resource, "");
                    }
                }
                if (resourceName != null) {
                    BoggledTerraformingProjectEffect.EffectTooltipPara para = new BoggledTerraformingProjectEffect.EffectTooltipPara("A siphon station constructed here would have " + resourceName + " " + resource + ".", "");
                    para.highlights.add(resourceName);
                    para.highlightColors.add(Misc.getHighlightColor());
                    effectTypeToPara.put("StationConditionsAndReason", para);
                }
            }
        }

        @Override
        public MarketAPI createMarket(SectorEntityToken stationEntity) {
            SectorEntityToken hostPlanet = stationEntity.getOrbitFocus();
            MarketAPI market = createDefaultMarket(stationEntity, hostPlanet.getName(), "MiningStationMarket");

            if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.siphonStationLinkToGasGiant)) {
                if(hostPlanet.getMarket().hasCondition(Conditions.VOLATILES_TRACE)) {
                    boggledTools.addCondition(market, Conditions.VOLATILES_TRACE);
                } else if(hostPlanet.getMarket().hasCondition(Conditions.VOLATILES_DIFFUSE)) {
                    boggledTools.addCondition(market, Conditions.VOLATILES_DIFFUSE);
                } else if(hostPlanet.getMarket().hasCondition(Conditions.VOLATILES_ABUNDANT)) {
                    boggledTools.addCondition(market, Conditions.VOLATILES_ABUNDANT);
                } else if(hostPlanet.getMarket().hasCondition(Conditions.VOLATILES_PLENTIFUL)) {
                    boggledTools.addCondition(market, Conditions.VOLATILES_PLENTIFUL);
                } else { // Can a gas giant not have any volatiles at all?
                    boggledTools.addCondition(market, Conditions.VOLATILES_TRACE);
                }
            } else {
                String resourceLevel = "diffuse";
                int staticAmountPerSettings = boggledTools.getIntSetting(boggledTools.BoggledSettings.siphonStationStaticAmount);
                switch(staticAmountPerSettings) {
                    case 1:
                        resourceLevel = "trace";
                        break;
                    case 2:
                        resourceLevel = "diffuse";
                        break;
                    case 3:
                        resourceLevel = "abundant";
                        break;
                    case 4:
                        resourceLevel = "plentiful";
                        break;
                }
                boggledTools.addCondition(market, "volatiles_" + resourceLevel);
            }

            Global.getSoundPlayer().playUISound(boggledTools.BoggledSounds.stationConstructed, 1.0F, 1.0F);
            return market;
        }
    }
}
