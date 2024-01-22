package boggled.campaign.econ;

import boggled.campaign.econ.abilities.BoggledBaseAbility;
import boggled.campaign.econ.industries.BoggledCommonIndustry;
import boggled.scripts.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CircularFleetOrbit;
import com.fs.starfarer.campaign.CircularOrbit;
import com.fs.starfarer.campaign.CircularOrbitPointDown;
import com.fs.starfarer.campaign.CircularOrbitWithSpin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import boggled.scripts.BoggledIndustryEffect;
import illustratedEntities.helper.ImageHandler;
import illustratedEntities.helper.Settings;
import illustratedEntities.helper.TextHandler;
import illustratedEntities.memory.ImageDataMemory;
import illustratedEntities.memory.TextDataEntry;
import illustratedEntities.memory.TextDataMemory;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class boggledTools {
    public static void CheckSubmarketExists(String source, String submarketId) {
        for (SubmarketSpecAPI submarketSpec : Global.getSettings().getAllSubmarketSpecs()) {
            if (submarketSpec.getId().equals(submarketId)) {
                return;
            }
        }
        throw new IllegalArgumentException(source + ": Market condition ID '" + submarketId + "' doesn't exist");
    }

    public static void CheckMarketConditionExists(String source, String conditionId) {
        for (MarketConditionSpecAPI marketConditionSpec : Global.getSettings().getAllMarketConditionSpecs()) {
            if (marketConditionSpec.getId().equals(conditionId)) {
                return;
            }
        }
        throw new IllegalArgumentException(source + ": Condition ID '" + conditionId + "' doesn't exist");
    }

    public static void CheckCommodityExists(String source, String commodityId) {
        for (CommoditySpecAPI commoditySpec : Global.getSettings().getAllCommoditySpecs()) {
            if (commoditySpec.getId().equals(commodityId)) {
                return;
            }
        }
        throw new IllegalArgumentException(source + ": Commodity ID '" + commodityId + "' doesn't exist");
    }

    public static void CheckSpecialItemExists(String source, String specialItemId) {
        for (SpecialItemSpecAPI itemSpec : Global.getSettings().getAllSpecialItemSpecs()) {
            if (itemSpec.getId().equals(specialItemId)) {
                return;
            }
        }
        throw new IllegalArgumentException(source + ": Special Item ID '" + specialItemId + "' doesn't exist");
    }

    public static void CheckResourceExists(String source, String resourceId) {
        for (Map.Entry<String, ArrayList<String>> resourceProgression : getResourceProgressions().entrySet()) {
            if (resourceProgression.getKey().equals(resourceId)) {
                return;
            }
        }
        throw new IllegalArgumentException(source + ": Resource ID '" + resourceId + "' doesn't exist");
    }

    public static void CheckPlanetTypeExists(String source, String planetType) {
        for (PlanetSpecAPI planetSpec : Global.getSettings().getAllPlanetSpecs()) {
            if (planetSpec.getPlanetType().equals(planetType)) {
                return;
            }
        }
        throw new IllegalArgumentException(source + ": Planet type '" + planetType + "' doesn't exist");
    }

    public static void CheckIndustryExists(String source, String industryId) {
        for (IndustrySpecAPI industrySpec : Global.getSettings().getAllIndustrySpecs()) {
            if (industrySpec.getId().equals(industryId)) {
                return;
            }
        }
        throw new IllegalArgumentException(source + ": Industry ID '" + industryId + "' doesn't exist");
    }

    public static void CheckItemExists(String source, String itemId) {
        for (SpecialItemSpecAPI specialItemSpec : Global.getSettings().getAllSpecialItemSpecs()) {
            if (specialItemSpec.getId().equals(itemId)) {
                return;
            }
        }
        throw new IllegalArgumentException(source + ": Item ID '" + itemId + "' doesn't exist");
    }

    public static void CheckSkillExists(String skillId) {
        for (String skillSpecId : Global.getSettings().getSkillIds()) {
            if (skillSpecId.equals(skillId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Skill ID '" + skillId + "' doesn't exist");
    }

    public static class BoggledMods {
        public static final String lunalibModId = "lunalib";
        public static final String illustratedEntitiesModId = "illustrated_entities";
        public static final String tascModId = "Terraforming & Station Construction";
    }

    public static class BoggledSettings {
        // Overriding enable check, used in the ability checks. Don't bother putting it in terraforming_projects.csv
        public static final String terraformingContentEnabled = "boggledTerraformingContentEnabled";
        public static final String stationConstructionContentEnabled = "boggledStationConstructionContentEnabled";
        public static final String astropolisEnabled = "boggledAstropolisEnabled";
        public static final String miningStationEnabled = "boggledMiningStationEnabled";
        public static final String siphonStationEnabled = "boggledSiphonStationEnabled";
        public static final String stationColonizationEnabled = "boggledStationColonizationEnabled";

        public static final String perihelionProjectEnabled = "boggledPerihelionProjectEnabled";
        public static final String planetCrackerEnabled = "boggledPlanetCrackerEnabled";

        public static final String planetKillerEnabled = "boggledPlanetKillerEnabled";

        public static final String replaceAgreusTechMiningWithDomainArchaeology = "boggledReplaceAgreusTechMiningWithDomainArchaeology";

        public static final String addDomainTechBuildingsToVanillaColonies = "boggledAddDomainTechBuildingsToVanillaColonies";
        public static final String cryosanctumReplaceEverywhere = "boggledCryosanctumReplaceEverywhere";

        // Building enables, checked in campaign.econ.industries.*
        // May move them to a CSV later
        public static final String enableAIMiningDronesStructure = "boggledEnableAIMiningDronesStructure";
        public static final String domainTechContentEnabled = "boggledDomainTechContentEnabled";
        public static final String domainArchaeologyEnabled = "boggledDomainArchaeologyEnabled";
        public static final String CHAMELEONEnabled = "boggledCHAMELEONEnabled";
        public static final String cloningEnabled = "boggledCloningEnabled";
        public static final String cryosanctumPlayerBuildEnabled = "boggledCryosanctumPlayerBuildEnabled";
        public static final String domedCitiesEnabled = "boggledDomedCitiesEnabled";
        public static final String genelabEnabled = "boggledGenelabEnabled";
        public static final String harmonicDamperEnabled = "boggledHarmonicDamperEnabled";
        public static final String hydroponicsEnabled = "boggledHydroponicsEnabled";
        public static final String kletkaSimulatorEnabled = "boggledKletkaSimulatorEnabled";
        public static final String limelightNetworkPlayerBuildEnabled = "boggledLimelightNetworkPlayerBuildEnabled";
        public static final String magnetoshieldEnabled = "boggledMagnetoshieldEnabled";
        public static final String mesozoicParkEnabled = "boggledMesozoicParkEnabled";
        public static final String ouyangOptimizerEnabled = "boggledOuyangOptimizerEnabled";
        public static final String planetaryAgravFieldEnabled = "boggledPlanetaryAgravFieldEnabled";
        public static final String remnantStationEnabled = "boggledRemnantStationEnabled";
        public static final String stellarReflectorArrayEnabled = "boggledStellarReflectorArrayEnabled";

        public static final String domedCitiesDefensePenaltyEnabled = "boggledDomedCitiesDefensePenaltyEnabled";
        public static final String stationCrampedQuartersEnabled = "boggledStationCrampedQuartersEnabled";
        public static final String stationCrampedQuartersPlayerCanPayToIncreaseStationSize = "boggledStationCrampedQuartersPlayerCanPayToIncreaseStationSize";
        public static final String stationCrampedQuartersSizeGrowthReductionStarts = "boggledStationCrampedQuartersSizeGrowthReductionStarts";
        public static final String stationProgressIncreaseInCostsToExpandStation = "boggledStationProgressiveIncreaseInCostsToExpandStation";

        public static final String kletkaSimulatorTemperatureBasedUpkeep = "boggledKletkaSimulatorTemperatureBasedUpkeep";

        public static final String miningStationUltrarichOre = "boggledMiningStationUltrarichOre";
        public static final String miningStationRichOre = "boggledMiningStationRichOre";
        public static final String miningStationAbundantOre = "boggledMiningStationAbundantOre";
        public static final String miningStationModerateOre = "boggledMiningStationModerateOre";
        public static final String miningStationSparseOre = "boggledMiningStationSparseOre";

        public static final String domainTechCraftingArtifactCost = "boggledDomainTechCraftingArtifactCost";
        public static final String domainTechCraftingStoryPointCost = "boggledDomainTechCraftingStoryPointCost";

        public static final String miningStationLinkToResourceBelts = "boggledMiningStationLinkToResourceBelts";
        public static final String miningStationStaticAmount = "boggledMiningStationStaticAmount";

        public static final String siphonStationLinkToGasGiant = "boggledSiphonStationLinkToGasGiant";
        public static final String siphonStationStaticAmount = "boggledSiphonStationStaticAmount";

        public static final String terraformingTypeChangeAddVolatiles = "boggledTerraformingTypeChangeAddVolatiles";

        public static final String stableLocationGateCostHeavyMachinery = "boggledStableLocationGateCostHeavyMachinery";
        public static final String stableLocationGateCostMetals = "boggledStableLocationGateCostMetals";
        public static final String stableLocationGateCostTransplutonics = "boggledStableLocationGateCostTransplutonics";
        public static final String stableLocationGateCostDomainEraArtifacts = "boggledStableLocationGateCostDomainEraArtifacts";

        public static final String stableLocationDomainTechStructureCostHeavyMachinery = "boggledStableLocationDomainTechStructureCostHeavyMachinery";
        public static final String stableLocationDomainTechStructureCostMetals = "boggledStableLocationDomainTechStructureCostMetals";
        public static final String stableLocationDomainTechStructureCostTransplutonics = "boggledStableLocationDomainTechStructureCostTransplutonics";
        public static final String stableLocationDomainTechStructureCostDomainEraArtifacts = "boggledStableLocationDomainTechStructureCostDomainEraArtifacts";

        public static final String marketSizeRequiredToBuildInactiveGate = "boggledMarketSizeRequiredToBuildInactiveGate";

        public static final String planetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests = "boggledPlanetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests";

        public static final String perihelionProjectDaysToFinish = "boggledPerihelionProjectDaysToFinish";
    }

    public static class BoggledTags {
        public static final String constructionRequiredDays = "boggled_construction_required_days_";
        public static final String constructionProgressDays = "boggled_construction_progress_days_";
        public static final String constructionProgressLastDayChecked = "boggled_construction_progress_lastDayChecked_";

        public static final String stationNamePrefix = "boggled_station_name_";

        public static final String terraformingController = "boggledTerraformingController";

        public static final String lightsOverlayAstropolisAlphaSmall = "boggled_lights_overlay_astropolis_alpha_small";
        public static final String lightsOverlayAstropolisAlphaMedium = "boggled_lights_overlay_astropolis_alpha_medium";
        public static final String lightsOverlayAstropolisAlphaLarge = "boggled_lights_overlay_astropolis_alpha_large";

        public static final String lightsOverlayAstropolisBetaSmall = "boggled_lights_overlay_astropolis_beta_small";
        public static final String lightsOverlayAstropolisBetaMedium = "boggled_lights_overlay_astropolis_beta_medium";
        public static final String lightsOverlayAstropolisBetaLarge = "boggled_lights_overlay_astropolis_beta_large";

        public static final String lightsOverlayAstropolisGammaSmall = "boggled_lights_overlay_astropolis_gamma_small";
        public static final String lightsOverlayAstropolisGammaMedium = "boggled_lights_overlay_astropolis_gamma_medium";
        public static final String lightsOverlayAstropolisGammaLarge = "boggled_lights_overlay_astropolis_gamma_large";

        public static final String lightsOverlayMiningSmall = "boggled_lights_overlay_mining_small";
        public static final String lightsOverlayMiningMedium = "boggled_lights_overlay_mining_medium";
        public static final String lightsOverlaySiphonSmall = "boggled_lights_overlay_siphon_small";
        public static final String lightsOverlaySiphonMedium = "boggled_lights_overlay_siphon_medium";
        public static final String alreadyReappliedLightsOverlay = "boggled_already_reapplied_lights_overlay";

        public static final String miningStationSmall = "boggled_mining_station_small";
        public static final String miningStationMedium = "boggled_mining_station_medium";

        public static final String stationConstructionNumExpansionsOne = "boggled_station_construction_numExpansions_1";
        public static final String stationConstructionNumExpansions = "boggled_station_construction_numExpansions_";
    }

    public static class BoggledSounds {
        public static final String stationConstructed = "ui_boggled_station_constructed";
    }

    public static class BoggledCommodities {
        public static final String domainArtifacts = "domain_artifacts";
    }

    public static class BoggledIndustries {
        public static final String cryosanctumIndustryId = "BOGGLED_CRYOSANCTUM";
        public static final String domainArchaeologyIndustryId = "BOGGLED_DOMAIN_ARCHAEOLOGY";
        public static final String genelabIndustryId = "BOGGLED_GENELAB";
        public static final String ismaraSlingIndustryId = "BOGGLED_ISMARA_SLING";
        public static final String asteroidProcessingIndustryId = "BOGGLED_ASTEROID_PROCESSING";
        public static final String remnantStationIndustryId = "BOGGLED_REMNANT_STATION";
        public static final String stellarReflectorArrayIndustryId = "BOGGLED_STELLAR_REFLECTOR_ARRAY";
    }

    private static final String starPlanetId = "star";

    private static final String barrenPlanetId = "barren";
    public static final String desertPlanetId = "desert";
    public static final String frozenPlanetId = "frozen";
    public static final String gasGiantPlanetId = "gas_giant";
    public static final String junglePlanetId = "jungle";
    public static final String terranPlanetId = "terran";
    private static final String toxicPlanetId = "toxic";
    private static final String tundraPlanetId = "tundra";
    private static final String volcanicPlanetId = "volcanic";
    public static final String waterPlanetId = "water";

    public static final String unknownPlanetId = "unknown";

    public static class BoggledConditions {
        public static final String terraformingControllerConditionId = "terraforming_controller";
        public static final String spriteControllerConditionId = "sprite_controller";

        public static final String crampedQuartersConditionId = "cramped_quarters";
    }
    // A mistyped string compiles fine and leads to plenty of debugging. A mistyped constant gives an error.

    public static final String csvOptionSeparator = "\\s*\\|\\s*";
    public static final String noneProjectId = "None";

    public static final HashMap<String, BoggledTerraformingRequirementFactory.TerraformingRequirementFactory> terraformingRequirementFactories = new HashMap<>();
    public static final HashMap<String, BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory> terraformingDurationModifierFactories = new HashMap<>();
    public static final HashMap<String, BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory> terraformingProjectEffectFactories = new HashMap<>();
    public static Map<String, BoggledStationConstructionFactory.StationConstructionFactory> stationConstructionFactories = new HashMap<>();

    public static final HashMap<String, BoggledCommoditySupplyDemandFactory.CommodityDemandFactory> commodityDemandFactories = new HashMap<>();
    public static final HashMap<String, BoggledCommoditySupplyDemandFactory.CommoditySupplyFactory> commoditySupplyFactories = new HashMap<>();

    public static final HashMap<String, BoggledIndustryEffectFactory.IndustryEffectFactory> industryEffectFactories = new HashMap<>();

    @Nullable
    public static BoggledTerraformingRequirement.TerraformingRequirement getTerraformingRequirement(String terraformingRequirementType, String id, boolean invert, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingRequirementFactory.TerraformingRequirementFactory factory = terraformingRequirementFactories.get(terraformingRequirementType);
        if (factory == null) {
            log.error("Requirement " + id + " of type " + terraformingRequirementType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingRequirement.TerraformingRequirement req = factory.constructFromJSON(id, invert, data);
        if (req == null) {
            log.error("Requirement " + id + " of type " + terraformingRequirementType + " was null when created with data " + data);
        }
        return req;
    }

    @Nullable
    public static BoggledProjectRequirementsOR getTerraformingRequirements(String terraformingRequirementId, String type, String id) {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledProjectRequirementsOR req = terraformingRequirements.get(terraformingRequirementId);
        if (req == null) {
            log.error(type + " " + id + " has invalid requirement " + terraformingRequirementId);
        }
        return req;
    }

    @Nullable
    public static BoggledTerraformingDurationModifier.TerraformingDurationModifier getDurationModifier(String durationModifierType, String id, String data) {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory factory = terraformingDurationModifierFactories.get(durationModifierType);
        if (factory == null) {
            log.error("Duration modifier " + id + " of type " + durationModifierType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = factory.constructFromJSON(data);
        if (mod == null) {
            log.error("Duration modifier " + id + " of type " + durationModifierType + " was null when created with data " + data);
            return null;
        }
        return mod;
    }

    @Nullable
    public static BoggledCommoditySupplyDemand.CommodityDemand getCommodityDemand(String commodityDemandType, String id, String[] enableSettings, String commodity, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledCommoditySupplyDemandFactory.CommodityDemandFactory factory = commodityDemandFactories.get(commodityDemandType);
        if (factory == null) {
            log.error("Commodity demand " + id + " of type " + commodityDemandType + " has no assigned factory");
            return null;
        }
        BoggledCommoditySupplyDemand.CommodityDemand demand = factory.constructFromJSON(id, enableSettings, commodity, data);
        if (demand == null) {
            log.error("Commodity demand " + id + " of type " + commodityDemandType + " was null when created with data " + data);
            return null;
        }
        return demand;
    }

    @Nullable
    public static BoggledCommoditySupplyDemand.CommoditySupply getCommoditySupply(String commoditySupplyType, String id, String[] enableSettings, String commodity, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledCommoditySupplyDemandFactory.CommoditySupplyFactory factory = commoditySupplyFactories.get(commoditySupplyType);
        if (factory == null) {
            log.error("Commodity demand " + id + " of type " + commoditySupplyType + " has no assigned factory");
            return null;
        }
        BoggledCommoditySupplyDemand.CommoditySupply supply = factory.constructFromJSON(id, enableSettings, commodity, data);
        if (supply == null) {
            log.error("Commodity supply " + id + " of type " + commoditySupplyType + " was null when created with data " + data);
            return null;
        }
        return supply;
    }

    @Nullable
    public static BoggledIndustryEffect.IndustryEffect getIndustryEffect(String industryEffectType, String id, String[] enableSettings, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledIndustryEffectFactory.IndustryEffectFactory factory = industryEffectFactories.get(industryEffectType);
        if (factory == null) {
            log.error("Industry effect " + id + " of type " + industryEffectType + " has no assigned factory");
            return null;
        }
        BoggledIndustryEffect.IndustryEffect effect = factory.constructFromJSON(id, enableSettings, data);
        if (effect == null) {
            log.error("Industry effect " + id + " of type " + industryEffectType + " was null when created with data " + data);
            return null;
        }
        return effect;
    }

    @Nullable
    public static BoggledTerraformingProjectEffect.TerraformingProjectEffect getProjectEffect(String[] enableSettings, String projectEffectType, String id, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory factory = terraformingProjectEffectFactories.get(projectEffectType);
        if (factory == null) {
            log.error("Terraforming project effect " + id + " of type " + projectEffectType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = factory.constructFromJSON(id, enableSettings, data);
        if (effect == null) {
            log.error("Terraforming project effect " + id + " of type " + projectEffectType + " was null when created with data " + data);
        }
        return effect;
    }

    public static boolean optionsAllowThis(String... options) {
        for (String option : options) {
            if (option.isEmpty()) {
                continue;
            }
            if (!boggledTools.getBooleanSetting(option)) {
                return false;
            }
        }
        return true;
    }

    public static String doTokenReplacement(String replace, Map<String, String> tokenReplacements) {
        for (Map.Entry<String, String> replacement : tokenReplacements.entrySet()) {
            replace = replace.replace(replacement.getKey(), replacement.getValue());
        }
        return replace;
    }

    public static String buildCommodityList(BaseIndustry industry, String[] commoditiesDemanded) {
        if (commoditiesDemanded.length == 0) {
            return "";
        }

        List<Pair<String, Integer>> deficits = industry.getAllDeficit(commoditiesDemanded);
        String[] strings = new String[deficits.size()];
        for (int i = 0; i < deficits.size(); ++i) {
            strings[i] = Global.getSettings().getCommoditySpec(deficits.get(i).one).getLowerCaseName();
        }
        return Misc.getAndJoined(strings);
    }

    private static BoggledCommonIndustry.TooltipData buildUnavailableReason(@NotNull List<BoggledTerraformingProject.ProjectInstance> projects, BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
        List<BoggledCommonIndustry.TooltipData> tooltips = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        List<Color> highlightColors = new ArrayList<>();
        List<String> highlights = new ArrayList<>();
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            for (BoggledProjectRequirementsAND.RequirementAndThen req : project.getProject().getProjectRequirements()) {
                if (!req.checkRequirement(ctx)) {
                    tooltips.addAll(req.getTooltip(ctx, tokenReplacements));
                }
            }
        }

        for (BoggledCommonIndustry.TooltipData tooltip : tooltips) {
            if (builder.length() != 0) {
                builder.append("\n");
            }
            builder.append(tooltip.text);
            highlightColors.addAll(tooltip.highlightColors);
            highlights.addAll(tooltip.highlights);
        }

        return new BoggledCommonIndustry.TooltipData(builder.toString(), highlightColors, highlights);
    }
    @NotNull
    public static BoggledCommonIndustry.TooltipData getUnavailableReason(List<BoggledTerraformingProject.ProjectInstance> projects, String industry, BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            if (!boggledTools.optionsAllowThis(project.getProject().getEnableSettings())) {
                return new BoggledCommonIndustry.TooltipData("Error in getUnavailableReason() in " + industry + ". Please tell Boggled about this on the forums.");
            }
        }

        return buildUnavailableReason(projects, ctx, tokenReplacements);
    }

    public static void initialiseDefaultTerraformingRequirementFactories() {
        addTerraformingRequirementFactory("AlwaysTrue", new BoggledTerraformingRequirementFactory.AlwaysTrue());

        addTerraformingRequirementFactory("PlanetType", new BoggledTerraformingRequirementFactory.PlanetType());
        addTerraformingRequirementFactory("MarketHasCondition", new BoggledTerraformingRequirementFactory.MarketHasCondition());
        addTerraformingRequirementFactory("MarketHasIndustry", new BoggledTerraformingRequirementFactory.MarketHasIndustry());
        addTerraformingRequirementFactory("MarketHasIndustryWithItem", new BoggledTerraformingRequirementFactory.MarketHasIndustryWithItem());
        addTerraformingRequirementFactory("MarketHasIndustryWithAICore", new BoggledTerraformingRequirementFactory.MarketHasIndustryWithAICore());
        addTerraformingRequirementFactory("PlanetWaterLevel", new BoggledTerraformingRequirementFactory.PlanetWaterLevel());
        addTerraformingRequirementFactory("MarketHasWaterPresent", new BoggledTerraformingRequirementFactory.MarketHasWaterPresent());
        addTerraformingRequirementFactory("MarketIsAtLeastSize", new BoggledTerraformingRequirementFactory.MarketIsAtLeastSize());
        addTerraformingRequirementFactory("TerraformingPossibleOnMarket", new BoggledTerraformingRequirementFactory.TerraformingPossibleOnMarket());
        addTerraformingRequirementFactory("MarketHasTags", new BoggledTerraformingRequirementFactory.MarketHasTags());
        addTerraformingRequirementFactory("MarketStorageContainsAtLeast", new BoggledTerraformingRequirementFactory.MarketStorageContainsAtLeast());
        addTerraformingRequirementFactory("FleetContainsCreditsAtLeast", new BoggledTerraformingRequirementFactory.FleetContainsCreditsAtLeast());
        addTerraformingRequirementFactory("FleetStorageContainsAtLeast", new BoggledTerraformingRequirementFactory.FleetStorageContainsAtLeast());
        addTerraformingRequirementFactory("FleetTooCloseToJumpPoint", new BoggledTerraformingRequirementFactory.FleetTooCloseToJumpPoint());
        addTerraformingRequirementFactory("PlayerHasStoryPointsAtLeast", new BoggledTerraformingRequirementFactory.PlayerHasStoryPointsAtLeast());
        addTerraformingRequirementFactory("WorldTypeSupportsResourceImprovement", new BoggledTerraformingRequirementFactory.WorldTypeSupportsResourceImprovement());

        addTerraformingRequirementFactory("FocusPlanetType", new BoggledTerraformingRequirementFactory.FocusPlanetType());
        addTerraformingRequirementFactory("FocusMarketHasCondition", new BoggledTerraformingRequirementFactory.FocusMarketHasCondition());

        addTerraformingRequirementFactory("IntegerFromMarketTagSubstring", new BoggledTerraformingRequirementFactory.IntegerFromTagSubstring());

        addTerraformingRequirementFactory("PlayerHasSkill", new BoggledTerraformingRequirementFactory.PlayerHasSkill());

        addTerraformingRequirementFactory("SystemStarHasTags", new BoggledTerraformingRequirementFactory.SystemStarHasTags());
        addTerraformingRequirementFactory("SystemStarType", new BoggledTerraformingRequirementFactory.SystemStarType());

        addTerraformingRequirementFactory("FleetInHyperspace", new BoggledTerraformingRequirementFactory.FleetInHyperspace());
        addTerraformingRequirementFactory("SystemHasJumpPoints", new BoggledTerraformingRequirementFactory.SystemHasJumpPoints());
        addTerraformingRequirementFactory("SystemHasPlanets", new BoggledTerraformingRequirementFactory.SystemHasPlanets());
        addTerraformingRequirementFactory("TargetPlanetOwnedBy", new BoggledTerraformingRequirementFactory.TargetPlanetOwnedBy());
        addTerraformingRequirementFactory("TargetStationOwnedBy", new BoggledTerraformingRequirementFactory.TargetStationOwnedBy());
        addTerraformingRequirementFactory("TargetPlanetGovernedByPlayer", new BoggledTerraformingRequirementFactory.TargetPlanetGovernedByPlayer());
        addTerraformingRequirementFactory("TargetPlanetWithinDistance", new BoggledTerraformingRequirementFactory.TargetPlanetWithinDistance());
        addTerraformingRequirementFactory("TargetStationWithinDistance", new BoggledTerraformingRequirementFactory.TargetStationWithinDistance());
        addTerraformingRequirementFactory("TargetStationColonizable", new BoggledTerraformingRequirementFactory.TargetStationColonizable());
        addTerraformingRequirementFactory("TargetPlanetIsAtLeastSize", new BoggledTerraformingRequirementFactory.TargetPlanetIsAtLeastSize());
        addTerraformingRequirementFactory("TargetPlanetOrbitFocusWithinDistance", new BoggledTerraformingRequirementFactory.TargetPlanetOrbitFocusWithinDistance());
        addTerraformingRequirementFactory("TargetPlanetStarWithinDistance", new BoggledTerraformingRequirementFactory.TargetPlanetStarWithinDistance());
        addTerraformingRequirementFactory("TargetPlanetOrbitersWithinDistance", new BoggledTerraformingRequirementFactory.TargetPlanetOrbitersWithinDistance());
        addTerraformingRequirementFactory("TargetPlanetMoonCountLessThan", new BoggledTerraformingRequirementFactory.TargetPlanetMoonCountLessThan());
        addTerraformingRequirementFactory("TargetPlanetOrbitersTooClose", new BoggledTerraformingRequirementFactory.TargetPlanetOrbitersTooClose());
        addTerraformingRequirementFactory("TargetPlanetStationCountLessThan", new BoggledTerraformingRequirementFactory.TargetPlanetStationCountLessThan());
        addTerraformingRequirementFactory("TargetSystemStationCountLessThan", new BoggledTerraformingRequirementFactory.TargetSystemStationCountLessThan());

        addTerraformingRequirementFactory("FleetInAsteroidBelt", new BoggledTerraformingRequirementFactory.FleetInAsteroidBelt());
        addTerraformingRequirementFactory("FleetInAsteroidField", new BoggledTerraformingRequirementFactory.FleetInAsteroidField());

        addTerraformingRequirementFactory("TargetPlanetStoryCritical", new BoggledTerraformingRequirementFactory.TargetPlanetStoryCritical());
        addTerraformingRequirementFactory("TargetStationStoryCritical", new BoggledTerraformingRequirementFactory.TargetStationStoryCritical());

        addTerraformingRequirementFactory("BooleanSettingIsTrue", new BoggledTerraformingRequirementFactory.BooleanSettingIsTrue());
    }

    public static void addTerraformingRequirementFactory(String key, BoggledTerraformingRequirementFactory.TerraformingRequirementFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming requirement factory " + key);
        terraformingRequirementFactories.put(key, value);
    }

    public static void initialiseDefaultTerraformingDurationModifierFactories() {
        addTerraformingDurationModifierFactory("PlanetSize", new BoggledTerraformingDurationModifierFactory.PlanetSize());
    }

    public static void addTerraformingDurationModifierFactory(String key, BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming duration modifier factory " + key);
        terraformingDurationModifierFactories.put(key, value);
    }

    public static void initialiseDefaultCommoditySupplyAndDemandFactories() {
        addCommoditySupplyFactory("FlatCommoditySupply", new BoggledCommoditySupplyDemandFactory.FlatSupplyFactory());
        addCommoditySupplyFactory("MarketSizeSupply", new BoggledCommoditySupplyDemandFactory.MarketSizeSupplyFactory());

        addCommodityDemandFactory("FlatCommodityDemand", new BoggledCommoditySupplyDemandFactory.FlatDemandFactory());
        addCommodityDemandFactory("MarketSizeDemand", new BoggledCommoditySupplyDemandFactory.MarketSizeDemandFactory());
        addCommodityDemandFactory("PlayerMarketSizeElseFlatDemand", new BoggledCommoditySupplyDemandFactory.PlayerMarketSizeElseFlatDemandFactory());
    }

    public static void addCommoditySupplyFactory(String key, BoggledCommoditySupplyDemandFactory.CommoditySupplyFactory value) {
        Global.getLogger(boggledTools.class).info("Adding commodity supply factory " + key);
        commoditySupplyFactories.put(key ,value);
    }

    public static void addCommodityDemandFactory(String key, BoggledCommoditySupplyDemandFactory.CommodityDemandFactory value) {
        Global.getLogger(boggledTools.class).info("Adding commodity demand factory " + key);
        commodityDemandFactories.put(key, value);
    }

    public static void initialiseDefaultIndustryEffectFactories() {
        addIndustryEffectFactory("DeficitToInactive", new BoggledIndustryEffectFactory.DeficitToInactive());
        addIndustryEffectFactory("DeficitToCommodity", new BoggledIndustryEffectFactory.DeficitToCommodity());
        addIndustryEffectFactory("DeficitMultiplierToUpkeep", new BoggledIndustryEffectFactory.DeficitMultiplierToUpkeep());

        addIndustryEffectFactory("EffectToIndustry", new BoggledIndustryEffectFactory.EffectToIndustry());

        addIndustryEffectFactory("ModifyIncome", new BoggledIndustryEffectFactory.ModifyIncome());

        addIndustryEffectFactory("ModifyAccessibility", new BoggledIndustryEffectFactory.ModifyAccessibility());
        addIndustryEffectFactory("ModifyStability", new BoggledIndustryEffectFactory.ModifyStability());

        addIndustryEffectFactory("SupplyBonusToIndustryWithDeficit", new BoggledIndustryEffectFactory.SupplyBonusToIndustryWithDeficit());
        addIndustryEffectFactory("ModifyAllDemand", new BoggledIndustryEffectFactory.ModifyAllDemand());
        addIndustryEffectFactory("ModifyUpkeep", new BoggledIndustryEffectFactory.ModifyUpkeep());

        addIndustryEffectFactory("EliminatePatherInterest", new BoggledIndustryEffectFactory.EliminatePatherInterest());
        addIndustryEffectFactory("ModifyPatherInterest", new BoggledIndustryEffectFactory.ModifyPatherInterest());

        addIndustryEffectFactory("IncrementTag", new BoggledIndustryEffectFactory.IncrementTag());
        addIndustryEffectFactory("RemoveIndustry", new BoggledIndustryEffectFactory.RemoveIndustry());

        addIndustryEffectFactory("SuppressConditions", new BoggledIndustryEffectFactory.SuppressConditions());
        addIndustryEffectFactory("ModifyGroundDefense", new BoggledIndustryEffectFactory.ModifyGroundDefense());

        addIndustryEffectFactory("IndustryEffectWithRequirement", new BoggledIndustryEffectFactory.IndustryEffectWithRequirement());

        addIndustryEffectFactory("AddCondition", new BoggledIndustryEffectFactory.AddCondition());

        addIndustryEffectFactory("AddStellarReflectorsToMarket", new BoggledIndustryEffectFactory.AddStellarReflectorsToMarket());

        addIndustryEffectFactory("ModifyColonyGrowthRate", new BoggledIndustryEffectFactory.ModifyColonyGrowthRate());

        addIndustryEffectFactory("TagSubstringPowerModifyBuildCostFactory", new BoggledIndustryEffectFactory.TagSubstringPowerModifyBuildCost());

        addIndustryEffectFactory("MonthlyItemProductionChance", new BoggledIndustryEffectFactory.MonthlyItemProductionChance());
        addIndustryEffectFactory("MonthlyItemProductionChanceModifier", new BoggledIndustryEffectFactory.MonthlyItemProductionChanceModifier());
    }

    public static void addIndustryEffectFactory(String key, BoggledIndustryEffectFactory.IndustryEffectFactory value) {
        Global.getLogger(boggledTools.class).info("Adding industry effect factory " + key);
        industryEffectFactories.put(key, value);
    }

    public static void initialiseDefaultTerraformingProjectEffectFactories() {
        addTerraformingProjectEffectFactory("PlanetTypeChange", new BoggledTerraformingProjectEffectFactory.PlanetTypeChange());
        addTerraformingProjectEffectFactory("MarketAddCondition", new BoggledTerraformingProjectEffectFactory.MarketAddCondition());
        addTerraformingProjectEffectFactory("MarketRemoveCondition", new BoggledTerraformingProjectEffectFactory.MarketRemoveCondition());
        addTerraformingProjectEffectFactory("MarketOptionalCondition", new BoggledTerraformingProjectEffectFactory.MarketOptionalCondition());
        addTerraformingProjectEffectFactory("MarketProgressResource", new BoggledTerraformingProjectEffectFactory.MarketProgressResource());

        addTerraformingProjectEffectFactory("FocusMarketAddCondition", new BoggledTerraformingProjectEffectFactory.FocusMarketAddCondition());
        addTerraformingProjectEffectFactory("FocusMarketRemoveCondition", new BoggledTerraformingProjectEffectFactory.FocusMarketRemoveCondition());
        addTerraformingProjectEffectFactory("FocusMarketProgressResource", new BoggledTerraformingProjectEffectFactory.FocusMarketProgressResource());
        addTerraformingProjectEffectFactory("FocusMarketAndSiphonStationProgressResource", new BoggledTerraformingProjectEffectFactory.FocusMarketAndSiphonStationProgressResource());

        addTerraformingProjectEffectFactory("SystemAddCoronalTap", new BoggledTerraformingProjectEffectFactory.SystemAddCoronalTap());

        addTerraformingProjectEffectFactory("MarketRemoveIndustry", new BoggledTerraformingProjectEffectFactory.MarketRemoveIndustry());

        addTerraformingProjectEffectFactory("RemoveItemFromSubmarket", new BoggledTerraformingProjectEffectFactory.RemoveItemFromSubmarket());
        addTerraformingProjectEffectFactory("RemoveItemFromFleetStorage", new BoggledTerraformingProjectEffectFactory.RemoveItemFromFleetStorage());
        addTerraformingProjectEffectFactory("RemoveCreditsFromFleet", new BoggledTerraformingProjectEffectFactory.RemoveCreditsFromFleet());
        addTerraformingProjectEffectFactory("RemoveStoryPointsFromPlayer", new BoggledTerraformingProjectEffectFactory.RemoveStoryPointsFromPlayer());
        addTerraformingProjectEffectFactory("AddItemToSubmarket", new BoggledTerraformingProjectEffectFactory.AddItemToSubmarket());

        addTerraformingProjectEffectFactory("AddStationToOrbit", new BoggledTerraformingProjectEffectFactory.AddStationToOrbit());
        addTerraformingProjectEffectFactory("AddStationToAsteroids", new BoggledTerraformingProjectEffectFactory.AddStationToAsteroids());

        addTerraformingProjectEffectFactory("ColonizeAbandonedStation", new BoggledTerraformingProjectEffectFactory.ColonizeAbandonedStation());
    }

    public static void addTerraformingProjectEffectFactory(String key, BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming project effect factory " + key);
        terraformingProjectEffectFactories.put(key, value);
    }

    public static void initialiseDefaultStationConstructionFactories() {
        addStationConstructionFactory("boggled_astropolis", new BoggledStationConstructionFactory.AstropolisConstructionFactory());
        addStationConstructionFactory("boggled_mining", new BoggledStationConstructionFactory.MiningStationConstructionFactory());
        addStationConstructionFactory("boggled_siphon", new BoggledStationConstructionFactory.SiphonStationConstructionFactory());
    }

    public static void addStationConstructionFactory(String key, BoggledStationConstructionFactory.StationConstructionFactory value) {
        Global.getLogger(boggledTools.class).info("Adding station construction factory " + key);
        stationConstructionFactories.put(key, value);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    private static ArrayList<String> arrayListFromJSON(@NotNull JSONObject data, String key, String regex) throws JSONException {
        String toSplit = data.getString(key);
        if (toSplit.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(toSplit.split(regex)));
    }

    public static BoggledProjectRequirementsAND requirementsFromRequirementsArray(JSONArray requirementArray, String id, String requirementType) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        if (requirementArray == null) {
            return new BoggledProjectRequirementsAND();
        }

        List<BoggledProjectRequirementsAND.RequirementAndThen> reqs = new ArrayList<>();
        for (int i = 0; i < requirementArray.length(); ++i) {
            JSONObject requirementObject = requirementArray.getJSONObject(i);
            String requirementsString = requirementObject.getString("requirement_id");

            JSONArray andThenArray = requirementObject.optJSONArray("and_then");
            BoggledProjectRequirementsAND andThen = null;
            if (andThenArray != null) {
                andThen = requirementsFromRequirementsArray(andThenArray, id, requirementType);
            }

            BoggledProjectRequirementsOR req = terraformingRequirements.get(requirementsString);
            if (req == null) {
                log.info("Project " + id + " has invalid " + requirementType + " " + requirementsString);
            } else {
                reqs.add(new BoggledProjectRequirementsAND.RequirementAndThen(req, andThen));
            }
        }

        return new BoggledProjectRequirementsAND(reqs);
    }

    private static List<BoggledIndustryEffect.IndustryEffect> industryEffectsFromObject(JSONObject object, String key, String id, String type) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        String effectsString = object.getString(key);
        List<BoggledIndustryEffect.IndustryEffect> ret = new ArrayList<>();
        if (effectsString.isEmpty()) {
            return ret;
        }
        JSONArray effectArray = new JSONArray(effectsString);
        for (int i = 0; i < effectArray.length(); ++i) {
            String effectString = effectArray.getString(i);
            if (effectString.isEmpty()) {
                continue;
            }
            BoggledIndustryEffect.IndustryEffect effect = boggledTools.industryEffects.get(effectString);
            if (effect != null) {
                ret.add(effect);
            } else {
                log.info(type + " " + id + " has invalid " + key + " effect " + effectString);
            }
        }
        return ret;
    }

    private static Map<String, List<BoggledIndustryEffect.IndustryEffect>> aiCoreEffectsFromJsonObject(JSONObject object, String key, String id, String type) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        Map<String, List<BoggledIndustryEffect.IndustryEffect>> ret = new HashMap<>();
        String aiCoreEffectsString = object.optString(key);
        if (aiCoreEffectsString.isEmpty()) {
            return ret;
        }

        JSONObject aiCoreEffects = new JSONObject(aiCoreEffectsString);
        for (Iterator<String> it = aiCoreEffects.keys(); it.hasNext(); ) {
            String aiCoreId = it.next();
            List<BoggledIndustryEffect.IndustryEffect> aiCoreEffect = industryEffectsFromObject(aiCoreEffects, aiCoreId, id, type);
            ret.put(aiCoreId, aiCoreEffect);
        }

        return ret;
    }

    public static void initialisePlanetTypesFromJSON(@NotNull JSONArray planetTypesJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, PlanetType> planetTypesMap = new HashMap<>();

        planetTypesMap.put(unknownPlanetId, new PlanetType(unknownPlanetId, "unknown", false, 0, new ArrayList<Pair<BoggledProjectRequirementsOR, Integer>>()));

        for (int i = 0; i < planetTypesJSON.length(); ++i) {
            try {
                JSONObject row = planetTypesJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String[] conditions = row.getString("conditions").split(boggledTools.csvOptionSeparator);
                String planetTypeName = row.getString("name");
                String planetTypeId = row.getString("terraforming_type_id");
                boolean terraformingPossible = row.getBoolean("terraforming_possible");

                int baseWaterLevel = row.getInt("base_water_level");

                List<Pair<BoggledProjectRequirementsOR, Integer>> conditionalWaterRequirements = new ArrayList<>();
                String conditionalWaterLevelsString = row.getString("conditional_water_requirements");
                if (!conditionalWaterLevelsString.isEmpty()) {
                    JSONArray conditionalWaterLevels = new JSONArray(conditionalWaterLevelsString);
                    for (int j = 0; j < conditionalWaterLevels.length(); ++j) {
                        JSONObject conditionalWaterLevel = conditionalWaterLevels.getJSONObject(j);
                        String requirement = conditionalWaterLevel.getString("requirement_id");
                        int waterLevel = conditionalWaterLevel.getInt("water_level");

                        BoggledProjectRequirementsOR waterRequirement = terraformingRequirements.get(requirement);
                        if (waterRequirement != null) {
                            conditionalWaterRequirements.add(new Pair<>(waterRequirement, waterLevel));
                        }
                    }
                }

                PlanetType planetType = new PlanetType(planetTypeId, planetTypeName, terraformingPossible, baseWaterLevel, conditionalWaterRequirements);

                planetTypesMap.put(id, planetType);

            } catch (JSONException e) {
                log.error("Error in planet types map: " + e);
            }
        }

        boggledTools.planetTypesMap = planetTypesMap;
    }

    public static void initialiseResourceProgressionsFromJSON(@NotNull JSONArray resourceProgressionsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, ArrayList<String>> resourceProgressions = new HashMap<>();

        for (int i = 0; i < resourceProgressionsJSON.length(); ++i) {
            try {
                JSONObject row = resourceProgressionsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id.isEmpty()) {
                    continue;
                }

                ArrayList<String> resource_progression = arrayListFromJSON(row, "resource_progression", boggledTools.csvOptionSeparator);

                resourceProgressions.put(id, resource_progression);
            } catch (JSONException e) {
                log.error("Error in resource progressions: " + e);
            }
        }

        boggledTools.resourceProgressions = resourceProgressions;
    }

    public static void initialiseResourceLimitsFromJSON(@NotNull JSONArray resourceLimitsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<Pair<String, String>, String> resourceLimits = new HashMap<>();

        for (int i = 0; i < resourceLimitsJSON.length(); ++i) {
            try {
                JSONObject row = resourceLimitsJSON.getJSONObject(i);

                String[] id = row.getString("id").split(boggledTools.csvOptionSeparator);
                if (id[0].isEmpty()) {
                    continue;
                }

                String resourceMax = row.getString("resource_max");

                assert(id.length == 2);
                Pair<String, String> key = new Pair<>(id[0], id[1]);

                resourceLimits.put(key, resourceMax);
            } catch (JSONException e) {
                log.error("Error in resource limits: " + e);
            }
        }

        boggledTools.resourceLimits = resourceLimits;
    }

    public static void initialiseIndustryOptionsFromJSON(@NotNull JSONArray industryOptionsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledCommonIndustry> industryProjects = new HashMap<>();
        String idForErrors = "";
        for (int i = 0; i < industryOptionsJSON.length(); ++i) {
            try {
                JSONObject row = industryOptionsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String industry = row.getString("tooltip");

                String[] projectStrings = row.getString("projects").split(boggledTools.csvOptionSeparator);

                ArrayList<BoggledTerraformingProject> projects = new ArrayList<>();
                for (String projectString : projectStrings) {
                    BoggledTerraformingProject project = boggledTools.getProject(projectString);
                    if (project != null) {
                        projects.add(project);
                    }
                }
                
                String[] commoditySupplyStrings = row.getString("commodity_supply").split(boggledTools.csvOptionSeparator);
                List<BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply = new ArrayList<>();
                for (String commoditySupplyString : commoditySupplyStrings) {
                    if (commoditySupplyString.isEmpty()) {
                        continue;
                    }
                    BoggledCommoditySupplyDemand.CommoditySupply supply = boggledTools.commoditySupply.get(commoditySupplyString);
                    if (supply != null) {
                        commoditySupply.add(supply);
                    } else {
                        log.info("Industry " + id + " has invalid commodity supply " + commoditySupplyString);
                    }
                }

                String[] commodityDemandStrings = row.getString("commodity_demand").split(boggledTools.csvOptionSeparator);
                List<BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand = new ArrayList<>();
                for (String commodityDemandString : commodityDemandStrings) {
                    if (commodityDemandString.isEmpty()) {
                        continue;
                    }
                    BoggledCommoditySupplyDemand.CommodityDemand demand = boggledTools.commodityDemand.get(commodityDemandString);
                    if (demand != null) {
                        commodityDemand.add(demand);
                    } else {
                        log.info("Industry " + id + " has invalid commodity demand " + commodityDemandString);
                    }
                }

                List<BoggledIndustryEffect.IndustryEffect> buildingFinishedEffects = industryEffectsFromObject(row, "building_finished_effects",id, "Industry");
                List<BoggledIndustryEffect.IndustryEffect> industryEffects = industryEffectsFromObject(row, "industry_effects", id, "Industry");
                List<BoggledIndustryEffect.IndustryEffect> improveEffects = industryEffectsFromObject(row, "improve_effects", id, "Industry");
                List<BoggledIndustryEffect.IndustryEffect> preBuildEffects = industryEffectsFromObject(row, "pre_build_effects", id, "Industry");

                Map<String, List<BoggledIndustryEffect.IndustryEffect>> aiCoreEffects = aiCoreEffectsFromJsonObject(row, "ai_core_effects", id, "Industry");

                List<BoggledProjectRequirementsAND> disruptRequirements = new ArrayList<>();

                float basePatherInterest = (float) row.getDouble("base_pather_interest");

                String imageOverridesString = row.getString("image_overrides");
                List<BoggledCommonIndustry.ImageOverrideWithRequirement> imageOverrides = new ArrayList<>();
                if (!imageOverridesString.isEmpty()) {
                    JSONArray imageOverridesJson = new JSONArray(imageOverridesString);
                    for (int j = 0; j < imageOverridesJson.length(); ++j) {
                        JSONObject imageOverride = imageOverridesJson.getJSONObject(j);
                        JSONArray requirementsArray = imageOverride.getJSONArray("requirement_ids");
                        BoggledProjectRequirementsAND imageReqs = requirementsFromRequirementsArray(requirementsArray, id, "image_overrides");
                        String category = imageOverride.getString("category");
                        String imageId = imageOverride.getString("id");

                        imageOverrides.add(new BoggledCommonIndustry.ImageOverrideWithRequirement(imageReqs, category, imageId));
                    }
                }

                industryProjects.put(id, new BoggledCommonIndustry(id, industry, projects, commoditySupply, commodityDemand, buildingFinishedEffects, industryEffects, improveEffects, aiCoreEffects, disruptRequirements, basePatherInterest, imageOverrides, preBuildEffects));
            } catch (JSONException e) {
                log.error("Error in industry options " + idForErrors + ": " + e);
            }
        }
        boggledTools.industryProjects = industryProjects;
    }

    public static void initialiseCommoditySupplyAndDemandFromJSON(@NotNull JSONArray commodityDemandsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand = new HashMap<>();
        HashMap<String, BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply = new HashMap<>();

        for (int i = 0; i < commodityDemandsJSON.length(); ++i) {
            try {
                JSONObject row = commodityDemandsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String supplyOrDemand = row.getString("supply_or_demand");

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String commodityDemandType = row.getString("commodity_quantity_type");
                String commodityId = row.getString("commodity_id");
                String data = row.getString("data");

                switch (supplyOrDemand) {
                    case "supply":
                        BoggledCommoditySupplyDemand.CommoditySupply supply = getCommoditySupply(commodityDemandType, id, enableSettings, commodityId, data);

                        commoditySupply.put(id, supply);
                        break;
                    case "demand":
                        BoggledCommoditySupplyDemand.CommodityDemand demand = getCommodityDemand(commodityDemandType, id, enableSettings, commodityId, data);

                        commodityDemand.put(id, demand);
                        break;
                }

            } catch (JSONException e) {
                log.error("Error in commodity supply and demand: " + e);
            }
        }
        boggledTools.commodityDemand = commodityDemand;
        boggledTools.commoditySupply = commoditySupply;
    }

    public static void initialiseIndustryEffectsFromJSON(@NotNull JSONArray industryEffectsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        boggledTools.industryEffects = new HashMap<>();

        String idForErrors = "";
        for (int i = 0; i < industryEffectsJSON.length(); ++i) {
            try {
                JSONObject row = industryEffectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String industryEffectType = row.getString("industry_effect_type");
                String data = row.getString("data");

                BoggledIndustryEffect.IndustryEffect effect = getIndustryEffect(industryEffectType, id, enableSettings, data);

                boggledTools.industryEffects.put(id, effect);
            } catch (JSONException e) {
                log.error("Error in industry effect '" + idForErrors + "': " + e);
            }
        }
    }

    public static void initialiseTerraformingRequirementFromJSON(@NotNull JSONArray terraformingRequirementJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        terraformingRequirement = new HashMap<>();
        String idForErrors = "";
        for (int i = 0; i < terraformingRequirementJSON.length(); ++i) {
            try {
                JSONObject row = terraformingRequirementJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String requirementType = row.getString("requirement_type");
                boolean invert = row.getBoolean("invert");
                String data = row.getString("data");

                BoggledTerraformingRequirement.TerraformingRequirement req = getTerraformingRequirement(requirementType, id, invert, data);
                if (req != null) {
                    terraformingRequirement.put(id, req);
                }
            } catch (JSONException e) {
                log.error("Error in terraforming requirement " + idForErrors + ": " + e);
            }
        }
    }

    public static void initialiseTerraformingRequirementsFromJSON(@NotNull JSONArray terraformingRequirementsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledProjectRequirementsOR> terraformingRequirements = new HashMap<>();
        for (int i = 0; i < terraformingRequirementsJSON.length(); ++i) {
            try {
                JSONObject row = terraformingRequirementsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id.isEmpty()) {
                    continue;
                }

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String tooltipText = row.getString("tooltip");
                List<String> tooltipHighlightText = new ArrayList<>();
                List<Color> tooltipHighlight = new ArrayList<>();

                String tooltipHighlightString = row.optString("tooltip_highlights");
                if (!tooltipHighlightString.isEmpty()) {
                    JSONArray tooltipHighlightArray = new JSONArray(tooltipHighlightString);
                    for (int j = 0; j < tooltipHighlightArray.length(); ++j) {
                        tooltipHighlightText.add(tooltipHighlightArray.getString(j));
                        tooltipHighlight.add(Misc.getHighlightColor());
                    }
                }

                BoggledCommonIndustry.TooltipData tooltip = new BoggledCommonIndustry.TooltipData(tooltipText, tooltipHighlight, tooltipHighlightText);

                boolean invertAll = row.getBoolean("invert_all");
                String[] requirements = row.getString("requirements").split(boggledTools.csvOptionSeparator);

                ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> reqs = new ArrayList<>();
                for (String requirement : requirements) {
                    BoggledTerraformingRequirement.TerraformingRequirement req = terraformingRequirement.get(requirement);
                    if (req != null) {
                        reqs.add(req);
                    } else {
                        log.error("Requirements " + id + " has invalid requirement " + requirement);
                    }
                }

                BoggledProjectRequirementsOR terraformingReqs = new BoggledProjectRequirementsOR(id, tooltip, invertAll, reqs);
                terraformingRequirements.put(id, terraformingReqs);

            } catch (JSONException e) {
                log.error("Error in terraforming requirements: " + e);
            }
        }
        boggledTools.terraformingRequirements = terraformingRequirements;
    }

    public static void initialiseTerraformingDurationModifiersFromJSON(@NotNull JSONArray durationModifiersJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers = new HashMap<>();
        for (int i = 0; i < durationModifiersJSON.length(); ++i) {
            try {
                JSONObject row = durationModifiersJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String durationModifierType = row.getString("duration_modifier_type");
                String data = row.getString("data");

                BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = getDurationModifier(durationModifierType, id, data);
                if (mod != null) {
                    durationModifiers.put(id, mod);
                }
            } catch (JSONException e) {
                log.error("Error in duration modifiers: " + e);
            }
        }

        boggledTools.durationModifiers = durationModifiers;
    }

    public static void initialiseTerraformingProjectEffectsFromJSON(@NotNull JSONArray projectEffectsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        Map<String, BoggledTerraformingProjectEffect.TerraformingProjectEffect> terraformingProjectEffects = new HashMap<>();
        String idForErrors = "";
        for (int i = 0; i < projectEffectsJSON.length(); ++i) {
            try {
                JSONObject row = projectEffectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);
                String projectEffectType = row.getString("effect_type");
                String data = row.getString("data");

                BoggledTerraformingProjectEffect.TerraformingProjectEffect projectEffect = getProjectEffect(enableSettings, projectEffectType, id, data);
                if (projectEffect != null) {
                    terraformingProjectEffects.put(id, projectEffect);
                }
            } catch (JSONException e) {
                log.error("Error in project effect " + idForErrors + ": " + e);
            }
        }
        boggledTools.terraformingProjectEffects = terraformingProjectEffects;
    }

    public static void initialiseTerraformingProjectsFromJSON(@NotNull JSONArray terraformingProjectsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        Map<String, BoggledTerraformingProject> terraformingProjects = new LinkedHashMap<>();
        String idForErrors = "";
        for (int i = 0; i < terraformingProjectsJSON.length(); ++i) {
            try {
                JSONObject row = terraformingProjectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String projectType = row.getString("project_type");

                String tooltip = row.getString("tooltip");
                String intelCompleteMessage = row.getString("intel_complete_message");

                String incompleteMessage = row.getString("incomplete_message");
                List<String> incompleteHighlights = arrayListFromJSON(row, "incomplete_message_highlights", boggledTools.csvOptionSeparator);

                int baseProjectDuration = row.optInt("base_project_duration", 0);

                String projectDurationModifiersString = row.getString("dynamic_project_duration_modifiers");
                List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> terraformingDurationModifiers = new ArrayList<>();
                if (!projectDurationModifiersString.isEmpty()) {
                    JSONArray projectDurationModifiersArray = new JSONArray(projectDurationModifiersString);
                    for (int j = 0; j < projectDurationModifiersArray.length(); ++j) {
                        JSONObject projectDurationModifiersObject = projectDurationModifiersArray.getJSONObject(j);
                        String durationModifiersKey = projectDurationModifiersObject.getString("modifier_id");
                        BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = durationModifiers.get(durationModifiersKey);
                        if (mod == null) {
                            log.info("Project " + id + " has invalid dynamic project duration modifier " + durationModifiersKey);
                        } else {
                            terraformingDurationModifiers.add(mod);
                        }
                    }
                }

                List<BoggledProjectRequirementsAND> reqsStall = new ArrayList<>();
                String requirementsStallArrayString = row.getString("requirements_stall");
                if (!requirementsStallArrayString.isEmpty()) {
                    JSONArray requirementsStallArray = new JSONArray(requirementsStallArrayString);
                    for (int j = 0; j < requirementsStallArray.length(); ++j) {
                        JSONArray reqArray = requirementsStallArray.getJSONArray(j);
                        BoggledProjectRequirementsAND reqsAnd = requirementsFromRequirementsArray(reqArray, id, "requirements_stall");
                        reqsStall.add(reqsAnd);
                    }
                }

                List<BoggledProjectRequirementsAND> reqsReset = new ArrayList<>();
                String requirementsResetArrayString = row.getString("requirements_reset");
                if (!requirementsResetArrayString.isEmpty()) {
                    JSONArray requirementsResetArray = new JSONArray(requirementsResetArrayString);
                    for (int j = 0; j < requirementsResetArray.length(); ++j) {
                        JSONArray reqArray = requirementsResetArray.getJSONArray(j);
                        BoggledProjectRequirementsAND reqsAnd = requirementsFromRequirementsArray(reqArray, id, "requirements_reset");
                        reqsReset.add(reqsAnd);
                    }
                }

                List<BoggledTerraformingProjectEffect.ProjectEffectWithRequirement> terraformingProjectEffects = new ArrayList<>();
                String projectEffectsString = row.getString("project_effects");
                if (!projectEffectsString.isEmpty()) {
                    JSONArray projectEffectsArray = new JSONArray(projectEffectsString);
                    for (int j = 0; j < projectEffectsArray.length(); ++j) {
                        JSONObject projectEffect = projectEffectsArray.getJSONObject(j);
                        JSONArray reqArray = projectEffect.optJSONArray("requirement_ids");
                        BoggledProjectRequirementsAND req = requirementsFromRequirementsArray(reqArray, id, "project_effects");;
                        String effectId = projectEffect.getString("effect_id");
                        BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = boggledTools.terraformingProjectEffects.get(effectId);
                        if (effect != null) {
                            terraformingProjectEffects.add(new BoggledTerraformingProjectEffect.ProjectEffectWithRequirement(effect, req));
                        } else {
                            log.info("Project " + id + " has invalid project effect " + effectId);
                        }
                    }
                }

                BoggledProjectRequirementsAND reqs;
                String requirementsArrayString = row.getString("requirements");
                if (!requirementsArrayString.isEmpty()) {
                    JSONArray reqsArray = new JSONArray(requirementsArrayString);
                    reqs = requirementsFromRequirementsArray(reqsArray, id, "requirements");
                } else {
                    reqs = new BoggledProjectRequirementsAND();
                }

                BoggledProjectRequirementsAND reqsHidden;
                String requirementsHiddenArrayString = row.getString("requirements_hidden");
                if (!requirementsHiddenArrayString.isEmpty()) {
                    JSONArray reqsHiddenArray = new JSONArray(requirementsHiddenArrayString);
                    reqsHidden = requirementsFromRequirementsArray(reqsHiddenArray, id, "requirements_hidden");
                } else {
                    reqsHidden = new BoggledProjectRequirementsAND();
                }

                BoggledTerraformingProject terraformingProj = new BoggledTerraformingProject(id, enableSettings, projectType, tooltip, intelCompleteMessage, incompleteMessage, incompleteHighlights, reqs, reqsHidden, baseProjectDuration, terraformingDurationModifiers, reqsStall, reqsReset, terraformingProjectEffects);
                terraformingProjects.put(id, terraformingProj);

            } catch (JSONException e) {
                log.error("Error in terraforming projects " + idForErrors + ": " + e);
            }
        }
        boggledTools.terraformingProjects = terraformingProjects;
    }

    public static void initialiseTerraformingRequirementsOverrides(@NotNull JSONArray terraformingRequirementsOverrideJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        try {
            for (int i = 0; i < terraformingRequirementsOverrideJSON.length(); ++i) {
                JSONObject row = terraformingRequirementsOverrideJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String requirementsId = row.getString("requirements_id");
                BoggledProjectRequirementsOR reqs = terraformingRequirements.get(requirementsId);
                if (reqs == null) {
                    log.error("Mod " + id + " terraforming requirements " + requirementsId + " not found, ignoring");
                    continue;
                }

                String[] requirementAddedStrings = row.getString("requirement_added").split(boggledTools.csvOptionSeparator);
                String[] requirementRemovedStrings = row.getString("requirement_removed").split(boggledTools.csvOptionSeparator);

                ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> requirementAdded = new ArrayList<>();
                for (String requirementAddedString : requirementAddedStrings) {
                    BoggledTerraformingRequirement.TerraformingRequirement req = terraformingRequirement.get(requirementAddedString);
                    if (req != null) {
                        requirementAdded.add(req);
                    }
                }

                reqs.addRemoveProjectRequirement(requirementAdded, requirementRemovedStrings);

            }
        } catch (JSONException e) {
            log.error("Error in terraforming requirements overrides: " + e);
        }
    }

    public static void initialiseTerraformingProjectOverrides(@NotNull JSONArray terraformingProjectsOverrideJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        try {
            for (int i = 0; i < terraformingProjectsOverrideJSON.length(); ++i) {
                JSONObject row = terraformingProjectsOverrideJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String projectId = row.getString("project_id");
                BoggledTerraformingProject proj = terraformingProjects.get(projectId);
                if (proj == null) {
                    log.error("Mod " + id + " terraforming project " + projectId + " not found, ignoring");
                    continue;
                }

                String tooltipOverride = row.getString("tooltip_override");
                String tooltipAddition = row.getString("tooltip_addition");

                String[] requirementsAddedStrings = row.getString("requirements_added").split(boggledTools.csvOptionSeparator);
                String[] requirementsRemovedStrings = row.getString("requirements_removed").split(boggledTools.csvOptionSeparator);
                String planetTypeChangeOverride = row.getString("planet_type_change_override");
                ArrayList<String> conditionsAdded = arrayListFromJSON(row, "conditions_added", boggledTools.csvOptionSeparator);
                ArrayList<String> conditionsRemoved = arrayListFromJSON(row, "conditions_removed", boggledTools.csvOptionSeparator);
                ArrayList<String> conditionsOption = arrayListFromJSON(row, "conditions_option", boggledTools.csvOptionSeparator);
                String optionName = row.getString("option_name");
                ArrayList<String> conditionProgressAdded = arrayListFromJSON(row, "condition_progress_added", boggledTools.csvOptionSeparator);
                ArrayList<String> conditionProgressRemoved = arrayListFromJSON(row, "condition_progress_removed", boggledTools.csvOptionSeparator);

                ArrayList<BoggledProjectRequirementsOR> requirementsAdded = new ArrayList<>();
                for (String requirementAddedString : requirementsAddedStrings) {
                    BoggledProjectRequirementsOR req = terraformingRequirements.get(requirementAddedString);
                    if (req != null) {
                        requirementsAdded.add(terraformingRequirements.get(requirementAddedString));
                    }
                }

//                proj.overrideAddTooltip(tooltipOverride, tooltipAddition);
//                proj.addRemoveProjectRequirements(requirementsAdded, requirementsRemovedStrings);
//                proj.overridePlanetTypeChange(planetTypeChangeOverride);
//                proj.addRemoveConditionsAddedRemoved(conditionsAdded, conditionsRemoved);
//                proj.addRemoveConditionProgress(conditionProgressAdded, conditionProgressRemoved);
            }
        } catch (JSONException e) {
            log.error("Error in terraforming projects overrides: " + e);
        }
    }

    public static BoggledTerraformingProject getProject(String projectId) {
        return terraformingProjects.get(projectId);
    }

    public static BoggledIndustryEffect.IndustryEffect getIndustryEffect(String industryEffectId) {
        return industryEffects.get(industryEffectId);
    }

    public static BoggledBaseAbility getAbility(String abilityId) {
        BoggledTerraformingProject project = getProject(abilityId);
        if (project == null) {
            Global.getLogger(boggledTools.class).error("Ability " + abilityId + " has no associated project");
            return null;
        }
        return new BoggledBaseAbility(project.getProjectId(), project.getEnableSettings(), project);
    }

    public static Map<Pair<String, String>, String> getResourceLimits() { return resourceLimits; }
    public static Map<String, ArrayList<String>> getResourceProgressions() { return resourceProgressions; }
    public static Map<String, BoggledTerraformingRequirement.TerraformingRequirement> getTerraformingRequirements() { return terraformingRequirement; }

    private static Map<String, BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirement;
    private static Map<String, BoggledProjectRequirementsOR> terraformingRequirements;
    private static Map<String, BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers;

    private static Map<String, BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply;
    private static Map<String, BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand;

    private static Map<String, BoggledIndustryEffect.IndustryEffect> industryEffects;

    private static Map<String, BoggledTerraformingProjectEffect.TerraformingProjectEffect> terraformingProjectEffects;
    private static Map<String, BoggledCommonIndustry> industryProjects;
    private static Map<String, BoggledTerraformingProject> terraformingProjects;

    private static Map<String, ArrayList<String>> resourceProgressions;
    private static Map<Pair<String, String>, String> resourceLimits;

    private static Map<String, PlanetType> planetTypesMap;

    public static BoggledCommonIndustry getIndustryProject(String industry) {
        return industryProjects.get(industry);
    }
    public static BoggledTerraformingDurationModifier.TerraformingDurationModifier getDurationModifier(String modifier) { return durationModifiers.get(modifier); }

    public static HashMap<String, Integer> getNumProjects() {
        HashMap<String, Integer> ret = new HashMap<>();
        for (Map.Entry<String, BoggledTerraformingProject> entry : terraformingProjects.entrySet()) {
            if (!entry.getValue().isEnabled()) {
                continue;
            }
            Integer val = ret.get(entry.getValue().getProjectType());
            if (val != null) {
                ret.put(entry.getValue().getProjectType(), ++val);
            } else {
                ret.put(entry.getValue().getProjectType(), 1);
            }
        }
        return ret;
    }

    public static HashMap<String, LinkedHashMap<String, BoggledTerraformingProject>> getVisibleProjects(BoggledTerraformingRequirement.RequirementContext ctx) {
        HashMap<String, LinkedHashMap<String, BoggledTerraformingProject>> ret = new HashMap<>();
        for (Map.Entry<String, BoggledTerraformingProject> entry : terraformingProjects.entrySet()) {
            if (!entry.getValue().isEnabled()) {
                continue;
            }
            if (!entry.getValue().requirementsHiddenMet(ctx)) {
                continue;
            }
            LinkedHashMap<String, BoggledTerraformingProject> val = ret.get(entry.getValue().getProjectType());
            if (val == null) {
                val = new LinkedHashMap<>();
                ret.put(entry.getValue().getProjectType(), val);
            }
            val.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public static Map<String, String> getTokenReplacements(BoggledTerraformingRequirement.RequirementContext ctx) {
        LinkedHashMap<String, String> ret = new LinkedHashMap<>();
        ret.put("$player", Global.getSector().getPlayerPerson().getNameString());
        MarketAPI market = ctx.getMarket();
        if (market != null) {
            ret.put("$marketName", market.getName());
        }
        MarketAPI focusMarket = ctx.getFocusContext().getMarket();
        if (focusMarket != null) {
            ret.put("$focusMarketName", focusMarket.getName());
        }
        PlanetAPI targetPlanet = ctx.getPlanet();
        if (targetPlanet != null) {
            ret.put("$planetTypeName", getPlanetType(targetPlanet).getPlanetTypeName());
            ret.put("$planetName", targetPlanet.getName());
        }
        StarSystemAPI starSystem = ctx.getStarSystem();
        if (starSystem != null) {
            ret.put("$system", starSystem.getName());
        }
        return ret;
    }

    public static float getAngle(float focusX, float focusY, float playerX, float playerY) {
        float angle = (float) Math.toDegrees(Math.atan2(focusY - playerY, focusX - playerX));

        //Not entirely sure what math is going on behind the scenes but this works to get the station to spawn next to the player
        angle = angle + 180f;

        return angle;
    }

    public static float getAngleFromPlayerFleet(SectorEntityToken target) {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        return getAngle(target.getLocation().x, target.getLocation().y, playerFleet.getLocation().x, playerFleet.getLocation().y);
    }

    public static float getAngleFromEntity(SectorEntityToken entity, SectorEntityToken target) {
        return getAngle(target.getLocation().x, target.getLocation().y, entity.getLocation().x, entity.getLocation().y);
    }

    public static void surveyAll(MarketAPI market) {
        for (MarketConditionAPI condition : market.getConditions()) {
            condition.setSurveyed(true);
        }
    }

    public static void refreshSupplyAndDemand(MarketAPI market) {
        //Refreshes supply and demand for each industry on the market
        List<Industry> industries = market.getIndustries();
        for (Industry industry : industries) {
            industry.doPreSaveCleanup();
            industry.doPostSaveRestore();
        }
    }

    public static float getRandomOrbitalAngleFloat(float min, float max) {
        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    public static int numStationsInOrbit(PlanetAPI targetPlanet, String... stationTags) {
        int numStations = 0;
        for (String stationTag : stationTags) {
            List<SectorEntityToken> entities = targetPlanet.getStarSystem().getEntitiesWithTag(stationTag);
            for (SectorEntityToken entity : entities) {
                if (entity.getOrbitFocus() == null) {
                    continue;
                }
                if (!entity.getOrbitFocus().equals(targetPlanet)) {
                    continue;
                }
                numStations++;
            }
        }
        return numStations;
    }

    public static int numStationsInSystem(StarSystemAPI starSystem, String... stationTags) {
        int numStations = 0;
        for (String stationTag : stationTags) {
            List<SectorEntityToken> entities = starSystem.getEntitiesWithTag(stationTag);
            for (SectorEntityToken entity : entities) {
                if (!entity.getFaction().getId().equals(Factions.NEUTRAL)) {
                    numStations++;
                }
            }
        }
        return numStations;
    }

    public static boolean gateInSystem(StarSystemAPI system) {
        return !system.getEntitiesWithTag(Tags.GATE).isEmpty();
    }

    public static boolean playerMarketInSystem(SectorEntityToken playerFleet) {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity.getMarket() != null && entity.getMarket().isPlayerOwned()) {
                return true;
            }
        }

        return false;
    }

    public static Integer getSizeOfLargestPlayerMarketInSystem(StarSystemAPI system) {
        // Returns zero if there are no player markets in the system.
        // Counts markets where the player purchased governorship.
        int largestMarketSize = 0;
        for (MarketAPI market : Misc.getPlayerMarkets(true)) {
            if (market.getStarSystem().equals(system) && market.getSize() > largestMarketSize) {
                largestMarketSize = market.getSize();
            }
        }

        return largestMarketSize;
    }

    public static Integer getPlayerMarketSizeRequirementToBuildGate() {
        return boggledTools.getIntSetting(BoggledSettings.marketSizeRequiredToBuildInactiveGate);
    }

    public static SectorEntityToken getClosestPlayerMarketToken(SectorEntityToken playerFleet) {
        if (!playerMarketInSystem(playerFleet)) {
            return null;
        }
        ArrayList<SectorEntityToken> allPlayerMarketsInSystem = new ArrayList<>();

        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity.getMarket() != null && entity.getMarket().isPlayerOwned()) {
                allPlayerMarketsInSystem.add(entity);
            }
        }

        SectorEntityToken closestMarket = null;
        for (SectorEntityToken entity : allPlayerMarketsInSystem) {
            if (closestMarket == null) {
                closestMarket = entity;
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestMarket, playerFleet)) {
                closestMarket = entity;
            }
        }

        return closestMarket;
    }

    public static SectorEntityToken getClosestGasGiantToken(SectorEntityToken playerFleet) {
        List<SectorEntityToken> allGasGiantsInSystem = new ArrayList<>();
        for (Object object : playerFleet.getStarSystem().getEntities(PlanetAPI.class)) {
            PlanetAPI planet = (PlanetAPI) object;
            if (planet.isGasGiant()) {
                allGasGiantsInSystem.add(planet);
            }
        }

        SectorEntityToken closestGasGiant = null;
        for (SectorEntityToken entity : allGasGiantsInSystem) {
            if (closestGasGiant == null) {
                closestGasGiant = entity;
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestGasGiant, playerFleet)) {
                closestGasGiant = entity;
            }
        }

        return closestGasGiant;
    }

    public static SectorEntityToken getClosestColonizableStationInSystem(SectorEntityToken playerFleet) {
        List<SectorEntityToken> allColonizableStationsInSystem = new ArrayList<>();
        for (SectorEntityToken entity : playerFleet.getStarSystem().getEntitiesWithTag(Tags.STATION)) {
            if (entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION)) {
                allColonizableStationsInSystem.add(entity);
            }
        }

        SectorEntityToken closestStation = null;
        for (SectorEntityToken entity : allColonizableStationsInSystem) {
            if (closestStation == null) {
                closestStation = entity;
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestStation, playerFleet)) {
                closestStation = entity;
            }
        }

        return closestStation;
    }

    public static SectorEntityToken getClosestStationInSystem(SectorEntityToken playerFleet) {
        StarSystemAPI starSystem = playerFleet.getStarSystem();
        if (starSystem == null) {
            return null;
        }
        List<SectorEntityToken> allStationsInSystem = playerFleet.getStarSystem().getEntitiesWithTag(Tags.STATION);

        SectorEntityToken closestStation = null;
        for (SectorEntityToken entity : allStationsInSystem) {
            if (closestStation == null) {
                closestStation = entity;
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestStation, playerFleet)) {
                closestStation = entity;
            }
        }

        return closestStation;
    }

    public static ArrayList<String> getListOfFactionsWithMarketInSystem(StarSystemAPI system) {
        ArrayList<String> factionsWithMarketInSystem = new ArrayList<>();

        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
            if (!factionsWithMarketInSystem.contains(market.getFactionId())) {
                factionsWithMarketInSystem.add(market.getFactionId());
            }
        }

        return factionsWithMarketInSystem;
    }

    public static ArrayList<Integer> getCompanionListOfTotalMarketPopulation(StarSystemAPI system, ArrayList<String> factions) {
        ArrayList<Integer> totalFactionMarketSize = new ArrayList<>();
        int buffer = 0;

        for (String faction : factions) {
            for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
                if (market.getFactionId().equals(faction)) {
                    buffer = buffer + market.getSize();
                }
            }

            totalFactionMarketSize.add(buffer);
            buffer = 0;
        }

        return totalFactionMarketSize;
    }

    public static boolean planetInSystem(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(PlanetAPI.class)) {
            PlanetAPI planet = (PlanetAPI) object;
            if (!getPlanetType((planet)).getPlanetId().equals(starPlanetId)) {
                return true;
            }
        }

        return false;
    }

    public static PlanetAPI getClosestPlanetToken(CampaignFleetAPI playerFleet) {
        if (playerFleet.isInHyperspace() || playerFleet.isInHyperspaceTransition()) {
            return null;
        }

        if (!planetInSystem(playerFleet)) {
            return null;
        }

        PlanetAPI closestPlanet = null;
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (!(entity instanceof PlanetAPI)) {
                continue;
            }
            PlanetAPI planet = (PlanetAPI) entity;
            if (getPlanetType(planet).getPlanetId().equals(starPlanetId)) {
                continue;
            }

            if (closestPlanet == null) {
                closestPlanet = (PlanetAPI) entity;
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestPlanet, playerFleet)) {
                closestPlanet = (PlanetAPI) entity;
            }
        }

        return closestPlanet;
    }

    public static MarketAPI getClosestMarketToEntity(SectorEntityToken entity) {
        if(entity == null || entity.getStarSystem() == null || entity.isInHyperspace()) {
            return null;
        }

        List<MarketAPI> markets = Global.getSector().getEconomy().getMarkets(entity.getStarSystem());
        MarketAPI closestMarket = null;
        for(MarketAPI market : markets) {
            if(closestMarket == null || Misc.getDistance(entity, market.getPrimaryEntity()) < Misc.getDistance(entity, closestMarket.getPrimaryEntity())) {
                if(!market.getFactionId().equals(Factions.NEUTRAL)) {
                    closestMarket = market;
                }
            }
        }

        return closestMarket;
    }

    public static PlanetType getPlanetType(PlanetAPI planet) {
        // Sets the spec planet type, but not the actual planet type. Need the API fix from Alex to correct this.
        // All code should rely on this function to get the planet type so it should work without bugs.
        // String planetType = planet.getTypeId();
        if(planet == null || planet.getSpec() == null || planet.getSpec().getPlanetType() == null) {
            return planetTypesMap.get(unknownPlanetId); // Guaranteed to be there
        }

        PlanetType planetType = planetTypesMap.get(planet.getTypeId());
        if (planetType != null) {
            return planetType;
        }
        return planetTypesMap.get(unknownPlanetId); // Guaranteed to be there
    }

    public static ArrayList<MarketAPI> getNonStationMarketsPlayerControls()
    {
        ArrayList<MarketAPI> allPlayerMarkets = (ArrayList<MarketAPI>) Misc.getPlayerMarkets(true);
        ArrayList<MarketAPI> allNonStationPlayerMarkets = new ArrayList<>();
        for(MarketAPI market : allPlayerMarkets)
        {
            if(!boggledTools.marketIsStation(market))
            {
                if(!market.hasCondition(BoggledConditions.terraformingControllerConditionId))
                {
                    boggledTools.addCondition(market, BoggledConditions.terraformingControllerConditionId);
                }
                allNonStationPlayerMarkets.add(market);
            }
        }

        return allNonStationPlayerMarkets;
    }

    public static boolean marketIsStation(MarketAPI market) {
        return market.getPrimaryEntity() == null || market.getPlanetEntity() == null || market.getPrimaryEntity().hasTag(Tags.STATION);
    }

    public static boolean getCreateMirrorsOrShades(MarketAPI market) {
        // Return true for mirrors, false for shades
        // Go by temperature first. If not triggered, will check planet type. Otherwise, just return true.
        if (market.hasCondition(Conditions.POOR_LIGHT) || market.hasCondition(Conditions.VERY_COLD) || market.hasCondition(Conditions.COLD)) {
            return true;
        } else if (market.hasCondition(Conditions.VERY_HOT) || market.hasCondition(Conditions.HOT)) {
            return false;
        }

        if (boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals(desertPlanetId) || boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals(junglePlanetId)) {
            return false;
        } else if (boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals(tundraPlanetId) || boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals(frozenPlanetId)) {
            return true;
        }

        return true;
    }

    public static SectorEntityToken getFocusOfAsteroidBelt(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if ((terrainPlugin instanceof AsteroidBeltTerrainPlugin && !(terrainPlugin instanceof AsteroidFieldTerrainPlugin)) && terrainPlugin.containsEntity(playerFleet)) {
                return terrain.getOrbitFocus();
            }
        }

        return null;
    }

    public static OrbitAPI getAsteroidFieldOrbit(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                AsteroidFieldTerrainPlugin asteroidPlugin = (AsteroidFieldTerrainPlugin) terrain.getPlugin();
                return asteroidPlugin.getEntity().getOrbit();
            } else {
                return null;
            }
        }

        return null;
    }

    public static SectorEntityToken getAsteroidFieldEntity(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                return terrain;
            }
        }

        // Should never return null because this method can't be called unless playerFleetInAsteroidField returned true
        return null;
    }

    public static boolean playerFleetInAsteroidBelt(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if ((terrainPlugin instanceof AsteroidBeltTerrainPlugin && !(terrainPlugin instanceof AsteroidFieldTerrainPlugin)) && terrainPlugin.containsEntity(playerFleet)) {
                return true;
            }
        }

        return false;
    }

    public static boolean playerFleetInAsteroidField(SectorEntityToken playerFleet)
    {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                return true;
            }
        }

        return false;
    }

    public static boolean playerFleetTooCloseToJumpPoint(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(JumpPointAPI.class)) {
            JumpPointAPI entity = (JumpPointAPI) object;
            if (Misc.getDistance(playerFleet, entity) < 300f) {
                return true;
            }
        }

        return false;
    }

    public static int getNumAsteroidTerrainsInSystem(SectorEntityToken playerFleet) {
        int numRoids = 0;
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if (terrainPlugin instanceof AsteroidBeltTerrainPlugin) {
                numRoids++;
            }

            /*
            // For testing purposes only
            if(terrainId.equals("asteroid_belt"))
            {
                AsteroidBeltTerrainPlugin belt = (AsteroidBeltTerrainPlugin) terrain.getPlugin();
                CampaignUIAPI ui = Global.getSector().getCampaignUI();
                ui.addMessage("Radius: " + belt.getRingParams().middleRadius, Color.YELLOW);
            }
            */
        }

        return numRoids;
    }

    public static String getMiningStationResourceString(Integer numAsteroidTerrains) {
        if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationUltrarichOre)) {
            return "ultrarich";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationRichOre)) {
            return "rich";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationAbundantOre)) {
            return "abundant";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationModerateOre)) {
            return "moderate";
        } else if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationSparseOre)) {
            return "sparse";
        } else {
            return "abundant";
        }
    }

    public static int getNumberOfStationExpansions(MarketAPI market) {
        for (String tag : market.getTags()) {
            if (tag.contains(BoggledTags.stationConstructionNumExpansions)) {
                return Integer.parseInt(tag.substring(tag.length() - 1));
            }
        }

        return 0;
    }

    public static float randomOrbitalAngleFloat() {
        Random rand = new Random();
        return rand.nextFloat() * (360f);
    }

    public static void refreshAquacultureAndFarming(MarketAPI market) {
        if(market == null || market.getPrimaryEntity() == null || market.getPlanetEntity() == null || market.hasTag(Tags.STATION) || market.getPrimaryEntity().hasTag(Tags.STATION)) {
            return;
        } else {
            if(market.hasIndustry(Industries.FARMING) && market.hasCondition(Conditions.WATER_SURFACE)) {
                market.getIndustry(Industries.FARMING).init(Industries.AQUACULTURE, market);
            } else if(market.hasIndustry(Industries.AQUACULTURE) && !market.hasCondition(Conditions.WATER_SURFACE)) {
                market.getIndustry(Industries.AQUACULTURE).init(Industries.FARMING, market);
            }
        }
    }

    public static boolean playerTooClose(StarSystemAPI system)
    {
        return Global.getSector().getPlayerFleet().isInOrNearSystem(system);
    }

    public static void clearConnectedPlanets(MarketAPI market)
    {
        SectorEntityToken targetEntityToRemove = null;
        for (SectorEntityToken entity : market.getConnectedEntities()) {
            if (entity instanceof PlanetAPI && !entity.hasTag(Tags.STATION)) {
                targetEntityToRemove = entity;
            }
        }

        if(targetEntityToRemove != null)
        {
            market.getConnectedEntities().remove(targetEntityToRemove);
            clearConnectedPlanets(market);
        }
    }

    public static void clearConnectedStations(MarketAPI market)
    {
        SectorEntityToken targetEntityToRemove = null;
        for (SectorEntityToken entity : market.getConnectedEntities()) {
            if (entity.hasTag(Tags.STATION)) {
                targetEntityToRemove = entity;
            }
        }

        if(targetEntityToRemove != null)
        {
            market.getConnectedEntities().remove(targetEntityToRemove);
            clearConnectedStations(market);
        }
    }

    public static int numReflectorsInOrbit(MarketAPI market) {
        int numReflectors = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_MIRROR) || entity.getId().contains(Entities.STELLAR_SHADE) || entity.hasTag(Entities.STELLAR_MIRROR) || entity.hasTag(Entities.STELLAR_SHADE))) {
                numReflectors++;
            }
        }

        return numReflectors;
    }

    public static int numMirrorsInOrbit(MarketAPI market) {
        int numMirrors = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_MIRROR) || entity.hasTag(Entities.STELLAR_MIRROR))) {
                numMirrors++;
            }
        }

        return numMirrors;
    }

    public static int numShadesInOrbit(MarketAPI market)
    {
        int numShades = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_SHADE) || entity.hasTag(Entities.STELLAR_SHADE))) {
                numShades++;
            }
        }

        return numShades;
    }

    public static void clearReflectorsInOrbit(MarketAPI market) {
        Iterator<SectorEntityToken> allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext()) {
            SectorEntityToken entity = allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_MIRROR) || entity.getId().contains(Entities.STELLAR_SHADE) || entity.hasTag(Entities.STELLAR_MIRROR) || entity.hasTag(Entities.STELLAR_SHADE)))
            {
                allEntitiesInSystem.remove();
                market.getStarSystem().removeEntity(entity);
            }
        }
    }

    public static boolean hasIsmaraSling(MarketAPI market) {
        for (MarketAPI marketElement : Global.getSector().getEconomy().getMarkets(market.getStarSystem())) {
            if (!marketElement.getFactionId().equals(market.getFactionId())) {
                continue;
            }

            Industry ismaraSlingIndustry = marketElement.getIndustry(BoggledIndustries.ismaraSlingIndustryId);
            if (ismaraSlingIndustry != null && ismaraSlingIndustry.isFunctional()) {
                return true;
            }

            Industry asteroidProcessingIndustry = marketElement.getIndustry(BoggledIndustries.asteroidProcessingIndustryId);
            if (asteroidProcessingIndustry != null && asteroidProcessingIndustry.isFunctional()) {
                return true;
            }
        }

        return false;
    }

    public static void swapStationSprite(SectorEntityToken station, String stationType, String stationGreekLetter, int targetSize) {
        MarketAPI market = station.getMarket();
        StarSystemAPI system = market.getStarSystem();
        OrbitAPI orbit = null;
        if(station.getOrbit() != null) {
            orbit = station.getOrbit();
        }
        CampaignClockAPI clock = Global.getSector().getClock();
        SectorEntityToken newStation;
        SectorEntityToken newStationLights = null;

        String size = null;
        if(targetSize == 1) {
            size = "small";
        } else if(targetSize == 2) {
            size = "medium";
        } else if(targetSize == 3) {
            size = "large";
        } else {
            //Do nothing if an erroneous size value was passed.
            return;
        }


        switch (stationType) {
            case "astropolis":
                newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + stationGreekLetter + "_" + size, market.getFactionId());
                newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + stationGreekLetter + "_" + size + "_lights_overlay", market.getFactionId());
                break;
            case "mining":
                newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
                //We can't tell which lights overlay to delete earlier because there could be multiple mining stations in a single system.
                //Therefore we delete them all earlier, then recreate them all later.
                break;
            case "siphon":
                newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
                newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + size + "_lights_overlay", market.getFactionId());
                break;
            default:
                //Do nothing because the station type is unrecognized
                return;
        }

        if(newStation == null) {
            //Failed to create a new station likely because of erroneous passed values. Do nothing.
            return;
        }

        newStation.setContainingLocation(station.getContainingLocation());
        if(newStationLights != null) {
            newStationLights.setContainingLocation(station.getContainingLocation());
        }

        if(orbit != null) {
            newStation.setOrbit(orbit);
            if(newStationLights != null)
            {
                newStationLights.setOrbit(newStation.getOrbit().makeCopy());
            }
        }
        newStation.setMemory(station.getMemory());
        newStation.setFaction(market.getFactionId());
        station.setCircularOrbit(newStation, 0, 0, 1);

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station)) {
                if (entity.getOrbit().getClass().equals(CircularFleetOrbit.class)) {
                    ((CircularFleetOrbit) entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbit.class)) {
                    ((CircularOrbit) entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbitPointDown.class)) {
                    ((CircularOrbitPointDown) entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbitWithSpin.class)) {
                    ((CircularOrbitWithSpin) entity.getOrbit()).setFocus(newStation);
                }
            }
        }

        // Handle Illustrated Entities custom images and/or description.
        // TASC uses classes from the Illustrated Entities to do this - the Illustrated Entities JAR is imported as a library into TASC.
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.illustratedEntitiesModId)) {
            boolean customImageHasBeenSet = ImageHandler.hasImage(station);
            if(customImageHasBeenSet) {
                int customImageId = ImageHandler.getImageId(station);
                ImageHandler.removeImageFrom(station);
                ImageHandler.setImage(newStation, ImageDataMemory.getInstance().get(customImageId), false);
            }

            TextDataEntry textDataEntry = TextHandler.getDataForEntity(station);
            if(textDataEntry != null) {
                boggledTools.setEntityIllustratedEntitiesCustomDescription(newStation, textDataEntry);
            }
        }

        //Deletes the old station. May cause limited issues related to ships orbiting the old location
        clearConnectedStations(market);
        system.removeEntity(station);

        newStation.setMarket(market);
        market.setPrimaryEntity(newStation);

        surveyAll(market);
        refreshSupplyAndDemand(market);
    }

    public static void setEntityIllustratedEntitiesCustomDescription(SectorEntityToken sectorEntityToken, TextDataEntry textDataEntry) {
        // The passed SectorEntityToken will have the description lines from the passed TextDataEntry copied onto its own TextDataEntry.

        TextDataMemory dataMemory = TextDataMemory.getInstance();

        int i = dataMemory.getNexFreetNum();
        TextDataEntry newTextDataEntry = new TextDataEntry(i, sectorEntityToken.getId());

        for (int textNum = 1; textNum <= 2; textNum++)
        {
            for (int lineNum = 1; lineNum <= Settings.LINE_AMT; lineNum++)
            {
                String s = textDataEntry.getString(textNum, lineNum);
                newTextDataEntry.setString(textNum, lineNum, s);
            }
        }

        newTextDataEntry.apply();
        dataMemory.set(newTextDataEntry.descriptionNum, newTextDataEntry);
    }

    public static void deleteOldLightsOverlay(SectorEntityToken station, String stationType, String stationGreekLetter) {
        StarSystemAPI system = station.getStarSystem();

        SectorEntityToken targetTokenToDelete = null;
        switch (stationType) {
            case "astropolis": {
                String smallTag = null;
                String mediumTag = null;
                String largeTag = null;
                switch (stationGreekLetter) {
                    case "alpha": {
                        smallTag = BoggledTags.lightsOverlayAstropolisAlphaSmall;
                        mediumTag = BoggledTags.lightsOverlayAstropolisAlphaMedium;
                        largeTag = BoggledTags.lightsOverlayAstropolisAlphaLarge;

                        break;
                    }
                    case "beta": {
                        smallTag = BoggledTags.lightsOverlayAstropolisBetaSmall;
                        mediumTag = BoggledTags.lightsOverlayAstropolisBetaMedium;
                        largeTag = BoggledTags.lightsOverlayAstropolisBetaLarge;
                        break;
                    }
                    case "gamma": {
                        smallTag = BoggledTags.lightsOverlayAstropolisGammaSmall;
                        mediumTag = BoggledTags.lightsOverlayAstropolisGammaMedium;
                        largeTag = BoggledTags.lightsOverlayAstropolisGammaLarge;
                        break;
                    }
                }

                if (smallTag != null) {
                    for (SectorEntityToken entity : system.getAllEntities()) {
                        if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && entity.getCircularOrbitAngle() == station.getCircularOrbitAngle() && (entity.hasTag(smallTag) || entity.hasTag(mediumTag) || entity.hasTag(largeTag))) {
                            targetTokenToDelete = entity;
                            break;
                        }
                    }

                }
                break;
            }
            case "mining": {
                for (SectorEntityToken entity : system.getAllEntities()) {
                    if (entity.hasTag(BoggledTags.lightsOverlayMiningSmall) || entity.hasTag(BoggledTags.lightsOverlayMiningMedium)) {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                break;
            }
            case "siphon": {
                for (SectorEntityToken entity : system.getAllEntities()) {
                    if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag(BoggledTags.lightsOverlaySiphonSmall) || entity.hasTag(BoggledTags.lightsOverlaySiphonMedium))) {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                break;
            }
            default:
                //Do nothing because the station type is unrecognized
                return;
        }

        if (targetTokenToDelete != null) {
            system.removeEntity(targetTokenToDelete);
            deleteOldLightsOverlay(station, stationType, stationGreekLetter);
        }
    }

    public static void reapplyMiningStationLights(StarSystemAPI system) {
        SectorEntityToken stationToApplyOverlayTo = null;
        int stationsize = 0;

        for (SectorEntityToken entity : system.getAllEntities()) {
            if (entity.hasTag(BoggledTags.miningStationSmall) && !entity.hasTag(BoggledTags.alreadyReappliedLightsOverlay)) {
                stationToApplyOverlayTo = entity;
                stationsize = 1;
                entity.addTag(BoggledTags.alreadyReappliedLightsOverlay);
                break;
            } else if (entity.hasTag(BoggledTags.miningStationMedium) && !entity.hasTag(BoggledTags.alreadyReappliedLightsOverlay)) {
                stationToApplyOverlayTo = entity;
                stationsize = 2;
                entity.addTag(BoggledTags.alreadyReappliedLightsOverlay);
                break;
            }
        }

        if(stationToApplyOverlayTo != null) {
            if(stationsize == 1) {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals(Factions.NEUTRAL))
                {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_small_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            } else if(stationsize == 2) {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals(Factions.NEUTRAL)) {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_medium_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            }
        }
        else {
            for (SectorEntityToken entity : system.getAllEntities()) {
                if (entity.hasTag(BoggledTags.alreadyReappliedLightsOverlay)) {
                    entity.removeTag(BoggledTags.alreadyReappliedLightsOverlay);
                }
            }
        }
    }

    public static boolean marketHasOrbitalStation(MarketAPI market) {
        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && entity.hasTag(Tags.STATION)) {
                return true;
            }
        }

        return false;
    }

    public static void addCondition(MarketAPI market, String condition) {
        if(!market.hasCondition(condition)) {
            market.addCondition(condition);
            boggledTools.surveyAll(market);
            boggledTools.refreshSupplyAndDemand(market);
            boggledTools.refreshAquacultureAndFarming(market);
        }
    }

    public static void removeCondition(MarketAPI market, String condition) {
        if(market != null && market.hasCondition(condition)) {
            market.removeCondition(condition);
            boggledTools.surveyAll(market);
            boggledTools.refreshSupplyAndDemand(market);
            boggledTools.refreshAquacultureAndFarming(market);
        }
    }

    public static void changePlanetType(PlanetAPI planet, String newType) {
        PlanetSpecAPI planetSpec = planet.getSpec();
        for(PlanetSpecAPI targetSpec : Global.getSettings().getAllPlanetSpecs()) {
            if (targetSpec.getPlanetType().equals(newType)) {
                planetSpec.setAtmosphereColor(targetSpec.getAtmosphereColor());
                planetSpec.setAtmosphereThickness(targetSpec.getAtmosphereThickness());
                planetSpec.setAtmosphereThicknessMin(targetSpec.getAtmosphereThicknessMin());
                planetSpec.setCloudColor(targetSpec.getCloudColor());
                planetSpec.setCloudRotation(targetSpec.getCloudRotation());
                planetSpec.setCloudTexture(targetSpec.getCloudTexture());
                planetSpec.setGlowColor(targetSpec.getGlowColor());
                planetSpec.setGlowTexture(targetSpec.getGlowTexture());

                planetSpec.setIconColor(targetSpec.getIconColor());
                planetSpec.setPlanetColor(targetSpec.getPlanetColor());
                planetSpec.setStarscapeIcon(targetSpec.getStarscapeIcon());
                planetSpec.setTexture(targetSpec.getTexture());
                planetSpec.setUseReverseLightForGlow(targetSpec.isUseReverseLightForGlow());
                ((PlanetSpec) planetSpec).planetType = newType;
                ((PlanetSpec) planetSpec).name = targetSpec.getName();
                ((PlanetSpec) planetSpec).descriptionId = ((PlanetSpec) targetSpec).descriptionId;
                break;
            }
        }

        planet.applySpecChanges();
    }

    public static void applyPlanetKiller(MarketAPI market) {
        if(Misc.isStoryCritical(market) && !boggledTools.getBooleanSetting(BoggledSettings.planetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests)) {
            // Should never be reached because deployment will be disabled.
            return;
        } else if(marketIsStation(market)) {
            adjustRelationshipsDueToPlanetKillerUsage(market);
            triggerMilitaryResponseToPlanetKillerUsage(market);
            decivilizeMarketWithPlanetKiller(market);
        } else if(market.getPlanetEntity() != null && market.getPlanetEntity().getSpec() != null) {
            changePlanetTypeWithPlanetKiller(market);
            changePlanetConditionsWithPlanetKiller(market);

            adjustRelationshipsDueToPlanetKillerUsage(market);
            triggerMilitaryResponseToPlanetKillerUsage(market);
            decivilizeMarketWithPlanetKiller(market);
        }
    }

    public static void changePlanetTypeWithPlanetKiller(MarketAPI market) {
        String planetType = getPlanetType(market.getPlanetEntity()).getPlanetId();
        if(!planetType.equals(starPlanetId) && !planetType.equals(gasGiantPlanetId) && !planetType.equals(volcanicPlanetId) && !planetType.equals(unknownPlanetId))
        {
            changePlanetType(market.getPlanetEntity(), Conditions.IRRADIATED);
            market.addCondition(Conditions.IRRADIATED);
        }
    }

    public static void changePlanetConditionsWithPlanetKiller(MarketAPI market) {
        // Modded conditions

        // Vanilla Conditions
        removeCondition(market, Conditions.HABITABLE);
        removeCondition(market, Conditions.MILD_CLIMATE);
        removeCondition(market, Conditions.WATER_SURFACE);
        removeCondition(market, Conditions.VOLTURNIAN_LOBSTER_PENS);

        removeCondition(market, Conditions.INIMICAL_BIOSPHERE);

        removeCondition(market, Conditions.FARMLAND_POOR);
        removeCondition(market, Conditions.FARMLAND_ADEQUATE);
        removeCondition(market, Conditions.FARMLAND_RICH);
        removeCondition(market, Conditions.FARMLAND_BOUNTIFUL);

        String planetType = getPlanetType(market.getPlanetEntity()).getPlanetId();
        if(!planetType.equals(gasGiantPlanetId) && !planetType.equals(unknownPlanetId)) {
            removeCondition(market, Conditions.ORGANICS_TRACE);
            removeCondition(market, Conditions.ORGANICS_COMMON);
            removeCondition(market, Conditions.ORGANICS_ABUNDANT);
            removeCondition(market, Conditions.ORGANICS_PLENTIFUL);

            removeCondition(market, Conditions.VOLATILES_TRACE);
            removeCondition(market, Conditions.VOLATILES_DIFFUSE);
            removeCondition(market, Conditions.VOLATILES_ABUNDANT);
            removeCondition(market, Conditions.VOLATILES_PLENTIFUL);
        }
    }

    public static List<FactionAPI> factionsToMakeHostileDueToPlanetKillerUsage(MarketAPI market) {
        List<FactionAPI> factionsToMakeHostile = new ArrayList<>();
        for(FactionAPI faction : Global.getSector().getAllFactions())
        {
            String factionId = faction.getId();
            if(factionId.equals(Factions.LUDDIC_PATH) && market.getFactionId().equals(Factions.LUDDIC_PATH))
            {
                factionsToMakeHostile.add(faction);
            }

            if(!factionId.equals(Factions.PLAYER) && !factionId.equals(Factions.DERELICT) && !factionId.equals(Factions.LUDDIC_PATH) && !factionId.equals(Factions.OMEGA) && !factionId.equals(Factions.REMNANTS) && !factionId.equals(Factions.SLEEPER))
            {
                factionsToMakeHostile.add(faction);
            }
        }

        return factionsToMakeHostile;
    }

    public static void adjustRelationshipsDueToPlanetKillerUsage(MarketAPI market) {
        for(FactionAPI faction : factionsToMakeHostileDueToPlanetKillerUsage(market)) {
            faction.setRelationship(Factions.PLAYER, -100f);
        }
    }

    public static void decivilizeMarketWithPlanetKiller(MarketAPI market) {
        int atrocities = (int) Global.getSector().getCharacterData().getMemoryWithoutUpdate().getFloat(MemFlags.PLAYER_ATROCITIES);
        atrocities++;
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(MemFlags.PLAYER_ATROCITIES, atrocities);

        // Added per Histidine's comments in the forum - see Page 148, comment #2210 in the TASC thread.
        // If you're reading this because it's not working properly for what you're trying to do, let me know!
        //ListenerUtil.reportSaturationBombardmentFinished(null, market, null);
        MarketCMD.TempData actionData = new MarketCMD.TempData();
        actionData.bombardType = MarketCMD.BombardType.SATURATION;	/* probably not needed but just in case someone forgot which listener method they were using */
        actionData.willBecomeHostile = factionsToMakeHostileDueToPlanetKillerUsage(market);	/* Fill this with FactionAPI that will get mad */
        ListenerUtil.reportSaturationBombardmentFinished(null, market, actionData);
        
        DecivTracker.decivilize(market, true);
        MarketCMD.addBombardVisual(market.getPrimaryEntity());
        MarketCMD.addBombardVisual(market.getPrimaryEntity());
        MarketCMD.addBombardVisual(market.getPrimaryEntity());

        // Copied from MarketCMD saturation bombing code.
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
            if (dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getMarket() != null) {
                Global.getSector().setPaused(false);
                dialog.getInteractionTarget().getMarket().getMemoryWithoutUpdate().advance(0.0001f);
                Global.getSector().setPaused(true);
            }

            ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }

        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
            ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }
    }

    public static void triggerMilitaryResponseToPlanetKillerUsage(MarketAPI market) {
        // Copied from MarketCMD addMilitaryResponse()
        if (market == null) return;

        if (!market.getFaction().getCustomBoolean(Factions.CUSTOM_NO_WAR_SIM)) {
            MilitaryResponseScript.MilitaryResponseParams params = new MilitaryResponseScript.MilitaryResponseParams(CampaignFleetAIAPI.ActionType.HOSTILE,
                    "player_ground_raid_" + market.getId(),
                    market.getFaction(),
                    market.getPrimaryEntity(),
                    0.75f,
                    30f);
            market.getContainingLocation().addScript(new MilitaryResponseScript(params));
        }

        List<CampaignFleetAPI> fleets = market.getContainingLocation().getFleets();
        for (CampaignFleetAPI other : fleets) {
            if (other.getFaction() == market.getFaction()) {
                MemoryAPI mem = other.getMemoryWithoutUpdate();
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, "raidAlarm", true, 1f);
            }
        }
    }

    public static void showProjectCompleteIntelMessage(String project, String completedMessage, MarketAPI market) {
        if (completedMessage.isEmpty()) {
            return;
        }
        if (market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel(project + " on " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - " + completedMessage);
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static class PlanetType {
        private final String planetId;
        private final String planetTypeName;
        private final boolean terraformingPossible;
        private final int baseWaterLevel;
        private final List<Pair<BoggledProjectRequirementsOR, Integer>> conditionalWaterRequirements;

        public String getPlanetId() { return planetId; }
        public String getPlanetTypeName() { return planetTypeName; }
        public boolean getTerraformingPossible() { return terraformingPossible; }
        public int getWaterLevel(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (conditionalWaterRequirements.isEmpty()) {
                return baseWaterLevel;
            }

            for (Pair<BoggledProjectRequirementsOR, Integer> conditionalWaterRequirement : conditionalWaterRequirements) {
                if (conditionalWaterRequirement.one.checkRequirement(ctx)) {
                    return conditionalWaterRequirement.two;
                }
            }
            return baseWaterLevel;
        }

        public PlanetType(String planetId, String planetTypeName, boolean terraformingPossible, int baseWaterLevel, List<Pair<BoggledProjectRequirementsOR, Integer>> conditionalWaterRequirements) {
            this.planetId = planetId;
            this.planetTypeName = planetTypeName;
            this.terraformingPossible = terraformingPossible;
            this.baseWaterLevel = baseWaterLevel;
            this.conditionalWaterRequirements = conditionalWaterRequirements;
            Collections.sort(this.conditionalWaterRequirements, new Comparator<Pair<BoggledProjectRequirementsOR, Integer>>() {
                @Override
                public int compare(Pair<BoggledProjectRequirementsOR, Integer> p1, Pair<BoggledProjectRequirementsOR, Integer> p2) {
                    return p1.two.compareTo(p2.two);
                }
            });
        }
    }

    public static String getTooltipProjectName(BoggledTerraformingRequirement.RequirementContext ctx, BoggledTerraformingProject currentProject) {
        if(currentProject == null) {
            return noneProjectId;
        }

        return currentProject.getProjectTooltip(boggledTools.getTokenReplacements(ctx));
    }

    public static int getLastDayCheckedForConstruction(SectorEntityToken stationEntity) {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressLastDayChecked)) {
                return Integer.parseInt(tag.replaceAll(BoggledTags.constructionProgressLastDayChecked, ""));
            }
        }

        return 0;
    }

    public static void clearClockCheckTagsForConstruction(SectorEntityToken stationEntity) {
        String tagToDelete = null;
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressLastDayChecked)) {
                tagToDelete = tag;
                break;
            }
        }

        if(tagToDelete != null)
        {
            stationEntity.removeTag(tagToDelete);
            clearClockCheckTagsForConstruction(stationEntity);
        }
    }

    public static void clearBoggledTerraformingControllerTags(MarketAPI market) {
        String tagToDelete = null;
        for (String tag : market.getTags()) {
            if (tag.contains(BoggledTags.terraformingController)) {
                tagToDelete = tag;
                break;
            }
        }

        if(tagToDelete != null) {
            market.removeTag(tagToDelete);
            clearBoggledTerraformingControllerTags(market);
        }
    }

    public static String getStationTypeName(SectorEntityToken stationEntity) {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.stationNamePrefix)) {
                String workingTag = tag.toLowerCase();
                if (workingTag.endsWith(" station")) {
                    return workingTag.substring(BoggledTags.stationNamePrefix.length(), workingTag.length() - " station".length());
                }
                return workingTag.substring(BoggledTags.stationNamePrefix.length());
            }
        }
        return "unknown";
    }

    public static int getConstructionProgressDays(SectorEntityToken stationEntity) {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressDays)) {
                return Integer.parseInt(tag.substring(BoggledTags.constructionProgressDays.length()));
            }
        }
        return 0;
    }

    public static int getConstructionRequiredDays(SectorEntityToken stationEntity) {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionRequiredDays)) {
                return Integer.parseInt(tag.substring(BoggledTags.constructionRequiredDays.length()));
            }
        }
        return 0;
    }

    public static void clearProgressCheckTagsForConstruction(SectorEntityToken stationEntity) {
        String tagToDelete = null;
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressDays)) {
                tagToDelete = tag;
                break;
            }
        }

        if(tagToDelete != null)
        {
            stationEntity.removeTag(tagToDelete);
            clearProgressCheckTagsForConstruction(stationEntity);
        }
    }

    public static void incrementConstructionProgressDays(SectorEntityToken stationEntity, int amount) {
        int currentDays = getConstructionProgressDays(stationEntity);

        clearProgressCheckTagsForConstruction(stationEntity);

        currentDays = currentDays + amount;

        String strDays = currentDays + "";

        while(strDays.length() < 6)
        {
            strDays = "0" + strDays;
        }

        stationEntity.addTag(BoggledTags.constructionProgressDays + strDays);
    }

    public static int[] getQuantitiesForStableLocationConstruction(String type) {
        ArrayList<Integer> ret = new ArrayList<>();

        if (type.equals(Entities.INACTIVE_GATE)) {
            ret.addAll(asList(
                    boggledTools.getIntSetting(BoggledSettings.stableLocationGateCostHeavyMachinery),
                    boggledTools.getIntSetting(BoggledSettings.stableLocationGateCostMetals),
                    boggledTools.getIntSetting(BoggledSettings.stableLocationGateCostTransplutonics)
            ));
            if (boggledTools.getBooleanSetting(BoggledSettings.domainArchaeologyEnabled)) {
                ret.add(boggledTools.getIntSetting(BoggledSettings.stableLocationGateCostDomainEraArtifacts));
            }
        } else {
            ret.addAll(asList(
                    boggledTools.getIntSetting(BoggledSettings.stableLocationDomainTechStructureCostHeavyMachinery),
                    boggledTools.getIntSetting(BoggledSettings.stableLocationDomainTechStructureCostMetals),
                    boggledTools.getIntSetting(BoggledSettings.stableLocationDomainTechStructureCostTransplutonics)
            ));
            if (boggledTools.getBooleanSetting(BoggledSettings.domainArchaeologyEnabled)) {
                ret.add(boggledTools.getIntSetting(BoggledSettings.stableLocationDomainTechStructureCostDomainEraArtifacts));
            }
        }
        int[] ret2 = new int[ret.size()];
        Iterator<Integer> it = ret.iterator();
        for (int i = 0; i < ret.size(); i++) ret2[i] = it.next();
        return ret2;
    }

    public static SectorEntityToken getPlanetTokenForQuest(String systemId, String entityId) {
        StarSystemAPI system = Global.getSector().getStarSystem(systemId);
        if (system == null) {
            return null;
        }
        SectorEntityToken possibleTarget = system.getEntityById(entityId);
        if (possibleTarget == null) {
            return null;
        }
        if(!(possibleTarget instanceof PlanetAPI)) {
            return null;
        }
        return possibleTarget;
    }

    public static int getIntSetting(String key) {
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.lunalibModId)) {
            return LunaSettings.getInt(BoggledMods.tascModId, key);
        } else {
            return Global.getSettings().getInt(key);
        }
    }

    public static boolean getBooleanSetting(String key) {
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.lunalibModId)) {
            return LunaSettings.getBoolean(BoggledMods.tascModId, key);
        } else {
            return Global.getSettings().getBoolean(key);
        }
    }

    public static boolean isResearched(String key)
    {
        // Pass this.getId() as key if this function is called from an industry

        if(Global.getSettings().getModManager().isModEnabled("aod_core"))
        {
            Map<String,Boolean> researchSaved = (HashMap<String, Boolean>) Global.getSector().getPersistentData().get("researchsaved");
            return researchSaved != null ?  researchSaved.get(key) : false;
        }
        else
        {
            // TASC does not have built-in research functionality.
            // Always return true if the player is not using a mod that implements research.
            return true;
        }
    }

    public static void writeMessageToLog(String message)
    {
        Global.getLogger(boggledTools.class).info(message);
    }

    public static void sendDebugIntelMessage(String message) {
        MessageIntel intel = new MessageIntel(message, Misc.getBasePlayerColor());
        intel.addLine(message);
        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, null);
    }

    public static void terraformDebug(MarketAPI market) {
        market.getPlanetEntity().changeType(boggledTools.waterPlanetId, null);
        sendDebugIntelMessage(market.getPlanetEntity().getTypeId());
        sendDebugIntelMessage(market.getPlanetEntity().getSpec().getPlanetType());
    }
}