package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.CargoAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoggledTerraformingRequirementFactory {
    public interface TerraformingRequirementFactory {
        BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException;
    }

    public static class AlwaysTrue implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.AlwaysTrue(id, invert);
        }
    }

    public static class PlanetType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            return new BoggledTerraformingRequirement.PlanetType(id, invert, data);
        }
    }

    public static class FocusPlanetType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            boggledTools.CheckPlanetTypeExists(id, data);
            return new BoggledTerraformingRequirement.FocusPlanetType(id, invert, data);
        }
    }

    public static class MarketHasCondition implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledTerraformingRequirement.MarketHasCondition(id, invert, data);
        }
    }

    public static class FocusMarketHasCondition implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledTerraformingRequirement.FocusMarketHasCondition(id, invert, data);
        }
    }

    public static class MarketHasIndustry implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            boggledTools.CheckIndustryExists(id, data);
            return new BoggledTerraformingRequirement.MarketHasIndustry(id, invert, data);
        }
    }

    public static class MarketHasIndustryWithItem implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String industryId = jsonData.getString("industry_id");
            String itemId = jsonData.getString("item_id");

            boggledTools.CheckIndustryExists(id, industryId);
            boggledTools.CheckItemExists(id, itemId);

            return new BoggledTerraformingRequirement.MarketHasIndustryWithItem(id, invert, industryId, itemId);
        }
    }

    public static class MarketHasIndustryWithAICore implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String industryId = jsonData.getString("industry_id");
            String aiCoreId = jsonData.getString("ai_core_id");

            boggledTools.CheckIndustryExists(id, industryId);
            boggledTools.CheckCommodityExists(id, aiCoreId);

            return new BoggledTerraformingRequirement.MarketHasIndustryWithAICore(id, invert, industryId, aiCoreId);
        }
    }

    public static class PlanetWaterLevel implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            int minWaterLevel = jsonData.getInt("min_water_level");
            int maxWaterLevel = jsonData.getInt("max_water_level");

            return new BoggledTerraformingRequirement.PlanetWaterLevel(id, invert, minWaterLevel, maxWaterLevel);
        }
    }

    public static class MarketHasWaterPresent implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            int minWaterLevel = jsonData.getInt("min_water_level");
            int maxWaterLevel = jsonData.getInt("max_water_level");

            return new BoggledTerraformingRequirement.MarketHasWaterPresent(id, invert, minWaterLevel, maxWaterLevel);
        }
    }

    public static class TerraformingPossibleOnMarket implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);

            ArrayList<String> invalidatingConditions = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); ++i) {
                invalidatingConditions.add(jsonArray.getString(i));
            }

            return new BoggledTerraformingRequirement.TerraformingPossibleOnMarket(id, invert, invalidatingConditions);
        }
    }

    public static class MarketHasTags implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            ArrayList<String> tags = new ArrayList<>(Arrays.asList(data.split(boggledTools.csvOptionSeparator)));

            return new BoggledTerraformingRequirement.MarketHasTags(id, invert, tags);
        }
    }

    public static class MarketIsAtLeastSize implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            int colonySize = Integer.parseInt(data);
            return new BoggledTerraformingRequirement.MarketIsAtLeastSize(id, invert, colonySize);
        }
    }

    public static class MarketStorageContainsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String submarketId = jsonData.getString("submarket_id");
            CargoAPI.CargoItemType itemType = null;
            String itemId = null;

            String commodityId = jsonData.optString("commodity_id");
            if (!commodityId.isEmpty()) {
                itemType = CargoAPI.CargoItemType.RESOURCES;
                itemId = commodityId;
                boggledTools.CheckCommodityExists(id, commodityId);
            }

            String specialItemId = jsonData.optString("special_item_id");
            if (!specialItemId.isEmpty()) {
                itemType = CargoAPI.CargoItemType.SPECIAL;
                itemId = specialItemId;
                boggledTools.CheckSpecialItemExists(id, specialItemId);
            }

            if (itemType == null) {
                throw new IllegalArgumentException(this.getClass().getName() + " " + id + " does not have a valid item ID specified");
            }

            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckSubmarketExists(id, submarketId);

            return new BoggledTerraformingRequirement.MarketStorageContainsAtLeast(id, invert, submarketId, itemType, itemId, quantity);
        }
    }

    public static class FleetStorageContainsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            CargoAPI.CargoItemType itemType = null;
            String itemId = null;

            String commodityId = jsonData.optString("commodity_id");
            if (!commodityId.isEmpty()) {
                itemType = CargoAPI.CargoItemType.RESOURCES;
                itemId = commodityId;
                boggledTools.CheckCommodityExists(id, commodityId);
            }

            String specialItemId = jsonData.optString("special_item_id");
            if (!specialItemId.isEmpty()) {
                itemType = CargoAPI.CargoItemType.SPECIAL;
                itemId = specialItemId;
                boggledTools.CheckSpecialItemExists(id, specialItemId);
            }

            if (itemType == null) {
                throw new IllegalArgumentException(this.getClass().getName() + " " + id + " does not have a valid item ID specified");
            }


            int quantity = jsonData.getInt("quantity");

            return new BoggledTerraformingRequirement.FleetStorageContainsAtLeast(id, invert, itemType, itemId, quantity);
        }
    }

    public static class FleetContainsCreditsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingRequirement.FleetContainsCreditsAtLeast(id, invert, quantity);
        }
    }

    public static class FleetTooCloseToJumpPoint implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            float distance = Float.parseFloat(data);
            return new BoggledTerraformingRequirement.FleetTooCloseToJumpPoint(id, invert, distance);
        }
    }

    public static class PlayerHasStoryPointsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledTerraformingRequirement.PlayerHasStoryPointsAtLeast(id, invert, quantity);
        }
    }

    public static class WorldTypeSupportsResourceImprovement implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            boggledTools.CheckResourceExists(id, data);
            return new BoggledTerraformingRequirement.WorldTypeSupportsResourceImprovement(id, invert, data);
        }
    }

    public static class IntegerFromTagSubstring implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String option = jsonData.getString("option");
            String tagSubstring = jsonData.getString("tag_substring");
            int maxValue = jsonData.getInt("max_value");

            return new BoggledTerraformingRequirement.IntegerFromTagSubstring(id, invert, option, tagSubstring, maxValue);
        }
    }

    public static class PlayerHasSkill implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            boggledTools.CheckSkillExists(data);
            return new BoggledTerraformingRequirement.PlayerHasSkill(id, invert, data);
        }
    }

    public static class SystemStarHasTags implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            ArrayList<String> tags = new ArrayList<>(Arrays.asList(data.split(boggledTools.csvOptionSeparator)));

            return new BoggledTerraformingRequirement.SystemStarHasTags(id, invert, tags);
        }
    }

    public static class SystemStarType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) {
            return new BoggledTerraformingRequirement.SystemStarType(id, invert, data);
        }
    }

    public static class FleetInHyperspace implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInHyperspace(id, invert);
        }
    }

    public static class SystemHasJumpPoints implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            int numJumpPoints = 1;
            if (!data.isEmpty()) {
                numJumpPoints = Integer.parseInt(data);
            }
            return new BoggledTerraformingRequirement.SystemHasJumpPoints(id, invert, numJumpPoints);
        }
    }

    public static class SystemHasPlanets implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            int numPlanets = 0;
            if (!data.isEmpty()) {
                numPlanets = Integer.parseInt(data);
            }
            return new BoggledTerraformingRequirement.SystemHasPlanets(id, invert, numPlanets);
        }
    }

    public static class TargetPlanetOwnedBy implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONArray jsonData = new JSONArray(data);
            List<String> factions = new ArrayList<>();
            for (int i = 0; i < jsonData.length(); ++i) {
                factions.add(jsonData.getString(i));
            }
            return new BoggledTerraformingRequirement.TargetPlanetOwnedBy(id, invert, factions);
        }
    }

    public static class TargetStationOwnedBy implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONArray jsonData = new JSONArray(data);
            List<String> factions = new ArrayList<>();
            for (int i = 0; i < jsonData.length(); ++i) {
                factions.add(jsonData.getString(i));
            }
            return new BoggledTerraformingRequirement.TargetStationOwnedBy(id, invert, factions);
        }
    }

    public static class TargetPlanetGovernedByPlayer implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetGovernedByPlayer(id, invert);
        }
    }

    public static class TargetPlanetWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetWithinDistance(id, invert, Float.parseFloat(data));
        }
    }

    public static class TargetStationWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetStationWithinDistance(id, invert, Float.parseFloat(data));
        }
    }

    public static class TargetStationColonizable implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetStationColonizable(id, invert);
        }
    }

    public static class TargetPlanetIsAtLeastSize implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetIsAtLeastSize(id, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetOrbitFocusWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitFocusWithinDistance(id, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetStarWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetStarWithinDistance(id, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetOrbitersWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitersWithinDistance(id, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetMoonCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetMoonCountLessThan(id, invert, Integer.parseInt(data));
        }
    }

    public static class TargetPlanetOrbitersTooClose implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitersTooClose(id, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetStationCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray stationTagsArray = jsonData.getJSONArray("station_tags");
            List<String> stationTags = new ArrayList<>();
            for (int i = 0; i < stationTagsArray.length(); ++i) {
                stationTags.add(stationTagsArray.getString(i));
            }
            int maxNum = jsonData.getInt("max_num");
            return new BoggledTerraformingRequirement.TargetPlanetStationCountLessThan(id, invert, stationTags, maxNum);
        }
    }

    public static class TargetSystemStationCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray stationTagsArray = jsonData.getJSONArray("station_tags");
            List<String> stationTags = new ArrayList<>();
            for (int i = 0; i < stationTagsArray.length(); ++i) {
                stationTags.add(stationTagsArray.getString(i));
            }
            int maxNum = jsonData.getInt("max_num");
            return new BoggledTerraformingRequirement.TargetSystemStationCountLessThan(id, invert, stationTags, maxNum);
        }
    }

    public static class FleetInAsteroidBelt implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInAsteroidBelt(id, invert);
        }
    }

    public static class FleetInAsteroidField implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInAsteroidField(id, invert);
        }
    }

    public static class TargetPlanetStoryCritical implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetStoryCritical(id, invert);
        }
    }

    public static class TargetStationStoryCritical implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetStationStoryCritical(id, invert);
        }
    }

    public static class BooleanSettingIsTrue implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String settingId = jsonData.getString("setting_id");
            boolean invertSetting = jsonData.optBoolean("invert_setting", false);
            String requirementId = jsonData.getString("requirement_id");
            BoggledTerraformingRequirement.TerraformingRequirement req = boggledTools.getTerraformingRequirements().get(requirementId);
            return new BoggledTerraformingRequirement.BooleanSettingIsTrue(id, invert, settingId, invertSetting, req);
        }
    }
}
