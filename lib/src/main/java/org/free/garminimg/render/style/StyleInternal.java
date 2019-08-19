package org.free.garminimg.render.style;

import android.graphics.Color;
import android.graphics.Paint;

import org.free.garminimg.Label;
import org.free.garminimg.UtilsGarminImg;
import org.free.garminimg.render.paint.BasicStroke;
import org.free.garminimg.render.paint.InternationalBorderStroke;
import org.free.garminimg.render.paint.PaintPath;
import org.free.garminimg.render.paint.PaintPattern;
import org.free.garminimg.render.paint.PaintPattern.PatternStyle;
import org.free.garminimg.render.paint.RocksPaint;
import org.free.garminimg.render.painters.LinePainterDouble;
import org.free.garminimg.render.painters.LinePainterSimple;
import org.free.garminimg.render.painters.LinePolyPainter;
import org.free.garminimg.render.painters.PolyPainterSimple;
import org.free.garminimg.render.painters.UtilsRender;
import org.free.garminimg.utils.ImgConstants;
import org.free.garminimg.utils.ImgConstants.DrawLabel;
import org.free.garminimg.utils.ImgConstants.LinePolyDrawSpec;
import org.free.garminimg.utils.ImgConstants.PointDrawSpec;
import org.free.garminimg.utils.Utils;

import java.util.Hashtable;
import java.util.Map;

import timber.log.Timber;

import static org.free.garminimg.utils.ImgConstants.AIRPORT;
import static org.free.garminimg.utils.ImgConstants.BACKGROUND;
import static org.free.garminimg.utils.ImgConstants.BG_COLOR;
import static org.free.garminimg.utils.ImgConstants.DEFINITION_AREA;
import static org.free.garminimg.utils.ImgConstants.ELEVATION;
import static org.free.garminimg.utils.ImgConstants.ELEVATION1;
import static org.free.garminimg.utils.ImgConstants.FOREST;
import static org.free.garminimg.utils.ImgConstants.GRAVEL_AREA;
import static org.free.garminimg.utils.ImgConstants.ID_LINE_PAVEMENT;
import static org.free.garminimg.utils.ImgConstants.ID_LINE_RAILROAD;
import static org.free.garminimg.utils.ImgConstants.ID_LINE_TRAIL;
import static org.free.garminimg.utils.ImgConstants.INTERMEDIATE_DEPTH_CONTOUR;
import static org.free.garminimg.utils.ImgConstants.INTERMEDIATE_LAND_CONTOUR;
import static org.free.garminimg.utils.ImgConstants.MAJOR_DEPTH_CONTOUR;
import static org.free.garminimg.utils.ImgConstants.MAJOR_LAND_CONTOUR;
import static org.free.garminimg.utils.ImgConstants.MINOR_DEPTH_CONTOUR;
import static org.free.garminimg.utils.ImgConstants.MINOR_LAND_CONTOUR;
import static org.free.garminimg.utils.ImgConstants.POLYGON_BASE_PRIORITY;
import static org.free.garminimg.utils.ImgConstants.POLYLINE_BASE_PRIORITY;
import static org.free.garminimg.utils.ImgConstants.RUINS;
import static org.free.garminimg.utils.ImgConstants.SHOPPING_CENTER;
import static org.free.garminimg.utils.ImgConstants.STATION_AREA;
import static org.free.garminimg.utils.ImgConstants.TUNNEL_SHIFT;
import static org.free.garminimg.utils.ImgConstants.ZOOM_MAX;

public class StyleInternal {

	private static final String TAG = StyleInternal.class.getSimpleName();
	
	private static StyleInternal instance;
	
	public static StyleInternal getInstance() {
		if (instance == null) {
			instance = new StyleInternal();
		}
		return instance;
	}
	
	/**************************************************/
	/*                 PUBLIC PART                    */
	/**************************************************/
	
	private static final String NO_POINT = "no point";
	
	// CONTAINERS
	
	private Map<Integer, PointDrawSpec> POINT_TYPES = new Hashtable<>();
	private Map<Integer, LinePolyDrawSpec> LINE_TYPES = new Hashtable<>();
	private Map<Integer, LinePolyDrawSpec> POLYGON_TYPES = new Hashtable<>();
	
	// COLORS
	
	private static final int cContourLand = Color.rgb(128, 64, 0);
//	private static final int cContourWater = Color.BLUE;
	private static final int cPark = Color.parseColor("#b9d7aa");
	private static final int cUrbanArea = Color.rgb(220, 220, 220);
	private static final int cWater = Color.rgb(74, 216, 255);

	// BASIC STYLES

	private static final Paint swampPaint = 
			new PaintPattern(PatternStyle.HORIZONTAL, cWater);
	private static final Paint rocksPaint = 
			new RocksPaint();
	
	/**************************************************/
	/*                POINT PARAMETERS                */
	/**************************************************/
	
	// DEFINITIONS
	
	public PointDrawSpec getPointType(int type, int subType) {
		int newType = type << 8 | subType;

		// get type from cache
		PointDrawSpec result = POINT_TYPES.get(newType);
		if (result != null) {
			return result;
		}
		
		// create and cache new style
		result = createPointSpec(newType);
		POINT_TYPES.put(newType, result);
		return result;
	}
	
	private PointDrawSpec createPointSpec(int type) {
		PointDrawSpec result = null;
		switch (type) {
		case 0x0100:
			result = finishPointSpec(0x0100, "City (pop. over 8M)", "large_city");
			break;
		case 0x0200:
			result = finishPointSpec(0x0200, "City (pop. 4-8M)", "large_city");
			break;
		case 0x0300:
			result = finishPointSpec(0x0300, "City (pop. 2-4M)", "large_city");
			break;
		case 0x0400:
			result = finishPointSpec(0x0400, "City (pop. 1-2M)", "large_city");
			break;
		case 0x0500:
			result = finishPointSpec(0x0500, "City (pop. 0.5-1M)", "medium_city");
			break;
		case 0x0600:
			result = finishPointSpec(0x0600, "City (pop. 200-500K)", "medium_city");
			break;
		case 0x0700:
			result = finishPointSpec(0x0700, "City (pop. 100-200K)", "medium_city");
			break;
		case 0x0800:
			result = finishPointSpec(0x0800, "City (pop. 50-100K)", "medium_city");
			break;
		case 0x0900:
			result = finishPointSpec(0x0900, "City (pop. 20-50K)", "medium_city");
			break;
		case 0x0a00:
			result = finishPointSpec(0x0a00, "Town (pop. 10-20K)", "medium_city");
			break;
		case 0x0b00:
			result = finishPointSpec(0x0b00, "Town (pop. 4-10K)", "small_city");
			break;
		case 0x0c00:
			result = finishPointSpec(0x0c00, "Town (pop. 2-4K)", "small_city");
			break;
		case 0x0d00:
			result = finishPointSpec(0x0d00, "Town (pop. 1-2K)", "small_city");
			break;
		case 0x0e00:
			result = finishPointSpec(0x0e00, "Town (pop. 0.5-1K)", "small_city");
			break;
		case 0x0f00:
			result = finishPointSpec(0x0f00, "Town (pop. 200-500)", "small_city");
			break;
		case 0x1000:
			result = finishPointSpec(0x1000, "Town (pop. 100-200)", "small_city");
			break;
		case 0x1100:
			result = finishPointSpec(0x1100, "Town (pop. under 100)", "small_city");
			break;
		case 0x1300:
			result = finishPointSpec(0x1300, "Town", "small_city");
			break;
		case 0x1400:
			result = finishPointSpec(0x1400, "Region, large", NO_POINT, -2);
			break;
		case 0x1500:
			result = finishPointSpec(0x1500, "Region, large", NO_POINT, -2);
			break;
		case 0x1600:
			result = finishPointSpec(0x1600, "Navaid");
			break;
		case 0x1601:
			result = finishPointSpec(0x1601, "Forg horn", "white_horn");
			break;
		case 0x1602:
			result = finishPointSpec(0x1602, "Radio beacon", "radio_beacon");
			break;
		case 0x1603:
			result = finishPointSpec(0x1603, "Racon", "radio_beacon");
			break;
		case 0x1604:
			result = finishPointSpec(0x1604, "Daybeacon (red triangle)", "radio_beacon");
			break;
		case 0x1605:
			result = finishPointSpec(0x1605, "Daybeacon (green square)", "radio_beacon");
			break;
		case 0x1606:
			result = finishPointSpec(0x1606, "Unlit navaid (white diamond)");
			break;
		case 0x1607:
			result = finishPointSpec(0x1607, "Unlit navaid (white)");
			break;
		case 0x1608:
			result = finishPointSpec(0x1608, "Unlit navaid (red)");
			break;
		case 0x1609:
			result = finishPointSpec(0x1609, "Unlit navaid (green)");
			break;
		case 0x160a:
			result = finishPointSpec(0x160a, "Unlit navaid (black)");
			break;
		case 0x160b:
			result = finishPointSpec(0x160b, "Unlit navaid (yellow/amber)");
			break;
		case 0x160c:
			result = finishPointSpec(0x160c, "Unlit navaid (orange)");
			break;
		case 0x160d:
			result = finishPointSpec(0x160d, "Unlit navaid (multi-colored)");
			break;
		case 0x160e:
			result = finishPointSpec(0x160e, "Navaid");
			break;
		case 0x160f:
			result = finishPointSpec(0x160f, "Navaid (white)");
			break;
		case 0x1610:
			result = finishPointSpec(0x1610, "Navaid (red)");
			break;
		case 0x1611:
			result = finishPointSpec(0x1611, "Navaid (green)");
			break;
		case 0x1612:
			result = finishPointSpec(0x1612, "Navaid (yellow/amber)");
			break;
		case 0x1613:
			result = finishPointSpec(0x1613, "Navaid (orange)");
			break;
		case 0x1614:
			result = finishPointSpec(0x1614, "Navaid (violet)");
			break;
		case 0x1615:
			result = finishPointSpec(0x1615, "Navaid (blue)");
			break;
		case 0x1616:
			result = finishPointSpec(0x1616, "Navaid (multi-colored)");
			break;
		case 0x1c00:
			result = finishPointSpec(0x1c00, "Obstruction");
			break;
		case 0x1c01:
			result = finishPointSpec(0x1c01, "Wreck", "white_wreck");
			break;
		case 0x1c02:
			result = finishPointSpec(0x1c02, "Submerged wreck, dangerous", "white_wreck");
			break;
		case 0x1c03:
			result = finishPointSpec(0x1c03, "Submerged wreck, non-dangerous", "white_wreck");
			break;
		case 0x1c04:
			result = finishPointSpec(0x1c04, "Wreck, cleared by wire-drag", "white_wreck");
			break;
		case 0x1c05:
			result = finishPointSpec(0x1c05, "Obstruction, visible at high water");
			break;
		case 0x1c06:
			result = finishPointSpec(0x1c06, "Obstruction, awash");
			break;
		case 0x1c07:
			result = finishPointSpec(0x1c07, "Obstruction, submerged");
			break;
		case 0x1c08:
			result = finishPointSpec(0x1c08, "Obstruction, cleared by wire-drag");
			break;
		case 0x1c09:
			result = finishPointSpec(0x1c09, "Rock, awash");
			break;
		case 0x1c0a:
			result = finishPointSpec(0x1c0a, "Rock, submerged at low water");
			break;
		case 0x1c0b:
			result = finishPointSpec(0x1c0b, "Sounding");
			break;
		case 0x1d00:
			result = finishPointSpec(0x1d00, "Tide");
			break;
		case 0x1d01:
			result = finishPointSpec(0x1d01, "Tide prediction");
			break;
		case 0x1d02:
			result = finishPointSpec(0x1d02, "Tide prediction");
			break;
		case 0x1e00:
			result = finishPointSpec(0x1e00, "Region, medium", NO_POINT, -1);
			break;
		case 0x1f00:
			result = finishPointSpec(0x1f00, "Region, medium", NO_POINT, -1);
			break;
		case 0x2000:
			result = finishPointSpec(0x2000, "Exit");
			break;
		case 0x2100:
			result = finishPointSpec(0x2100, "Exit, with facilities");
			break;
		case 0x210f:
			result = finishPointSpec(0x210f, "Exit, service");
			break;
		case 0x2200:
			result = finishPointSpec(0x2200, "Exit, restroom");
			break;
		case 0x2300:
			result = finishPointSpec(0x2300, "Exit, convenience store");
			break;
		case 0x2400:
			result = finishPointSpec(0x2400, "Exit, weigh station");
			break;
		case 0x2500:
			result = finishPointSpec(0x2500, "Exit, toll booth");
			break;
		case 0x2600:
			result = finishPointSpec(0x2600, "Exit, information");
			break;
		case 0x2700:
			result = finishPointSpec(0x2700, "Exit");
			break;
		case 0x2800:
			result = finishPointSpec(0x2800, "Region, small", NO_POINT, 0);
			break;
		case 0x2900:
			result = finishPointSpec(0x2900, "Region", null, 0);
			break;
		case 0x2a00:
			result = finishPointSpec(0x2a00, "Food & Drink", "white_knife_fork");
			break;
		case 0x2a01:
			result = finishPointSpec(0x2a01, "Food & Drink, American", "fast_food");
			break;
		case 0x2a02:
			result = finishPointSpec(0x2a02, "Food & Drink, Asian", "white_knife_fork");
			break;
		case 0x2a03:
			result = finishPointSpec(0x2a03, "Food & Drink, Barbeque", "white_knife_fork");
			break;
		case 0x2a04:
			result = finishPointSpec(0x2a04, "Food & Drink, Chinese", "white_knife_fork");
			break;
		case 0x2a05:
			result = finishPointSpec(0x2a05, "Food & Drink, Deli/Bakery", "white_knife_fork");
			break;
		case 0x2a06:
			result = finishPointSpec(0x2a06, "Food & Drink, International", "white_knife_fork");
			break;
		case 0x2a07:
			result = finishPointSpec(0x2a07, "Food & Drink, Fast Food", "fast_food");
			break;
		case 0x2a08:
			result = finishPointSpec(0x2a08, "Food & Drink, Italian", "white_knife_fork");
			break;
		case 0x2a09:
			result = finishPointSpec(0x2a09, "Food & Drink, Mexican", "white_knife_fork");
			break;
		case 0x2a0a:
			result = finishPointSpec(0x2a0a, "Food & Drink, Pizza", "white_knife_fork");
			break;
		case 0x2a0b:
			result = finishPointSpec(0x2a0b, "Food & Drink, Seafood", "white_knife_fork");
			break;
		case 0x2a0c:
			result = finishPointSpec(0x2a0c, "Food & Drink, Steak/Grill", "white_knife_fork");
			break;
		case 0x2a0d:
			result = finishPointSpec(0x2a0d, "Food & Drink, Bagel/Doughnut", "white_knife_fork");
			break;
		case 0x2a0e:
			result = finishPointSpec(0x2a0e, "Food & Drink, Cafe/Diner", "white_knife_fork");
			break;
		case 0x2a0f:
			result = finishPointSpec(0x2a0f, "Food & Drink, French", "white_knife_fork");
			break;
		case 0x2a10:
			result = finishPointSpec(0x2a10, "Food & Drink, German", "white_mug");
			break;
		case 0x2a11:
			result = finishPointSpec(0x2a11, "Food & Drink, British Isles", "white_knife_fork");
			break;
		case 0x2b00:
			result = finishPointSpec(0x2b00, "Lodging", "lodging");
			break;
		case 0x2b01:
			result = finishPointSpec(0x2b01, "Lodging, Hotel/Motel", "lodging");
			break;
		case 0x2b02:
			result = finishPointSpec(0x2b02, "Lodging, Bed & Breakfast/Inn", "lodging");
			break;
		case 0x2b03:
			result = finishPointSpec(0x2b03, "Lodging, Campground/RV Park", "lodging");
			break;
		case 0x2b04:
			result = finishPointSpec(0x2b04, "Lodging, Resort", "lodging");
			break;
		case 0x2c00:
			result = finishPointSpec(0x2c00, "Attraction", "museum");
			break;
		case 0x2c01:
			result = finishPointSpec(0x2c01, "Recreation, Amusement/Theme Park", "amusement_park");
			break;
		case 0x2c02:
			result = finishPointSpec(0x2c02, "Attraction, Museum/Historical", "museum");
			break;
		case 0x2c03:
			result = finishPointSpec(0x2c03, "Community, Library", "museum");
			break;
		case 0x2c04:
			result = finishPointSpec(0x2c04, "Attraction, Landmark", "amber_map_buoy");
			break;
		case 0x2c05:
			result = finishPointSpec(0x2c05, "Community, School", "school");
			break;
		case 0x2c06:
			result = finishPointSpec(0x2c06, "Attraction, Park/Garden", "park");
			break;
		case 0x2c07:
			result = finishPointSpec(0x2c07, "Attraction, Zoo/Aquarium", "zoo");
			break;
		case 0x2c08:
			result = finishPointSpec(0x2c08, "Recreation, Arena/Track", "stadium");
			break;
		case 0x2c09:
			result = finishPointSpec(0x2c09, "Attraction, Hall/Auditorium", "movie");
			break;
		case 0x2c0a:
			result = finishPointSpec(0x2c0a, "Attraction, Winery");
			break;
		case 0x2c0b:
			result = finishPointSpec(0x2c0b, "Community, Place of Worship", "church");
			break;
		case 0x2c0c:
			result = finishPointSpec(0x2c0c, "Attraction, Hot Spring", "drinking_water");
			break;
		case 0x2d00:
			result = finishPointSpec(0x2d00, "Entertainment", "movie");
			break;
		case 0x2d01:
			result = finishPointSpec(0x2d01, "Entertainment, Live Theater", "live_theater");
			break;
		case 0x2d02:
			result = finishPointSpec(0x2d02, "Entertainment, Bar/Nightclub", "white_mug");
			break;
		case 0x2d03:
			result = finishPointSpec(0x2d03, "Entertainment, Cinema", "movie");
			break;
		case 0x2d04:
			result = finishPointSpec(0x2d04, "Entertainment, Casino", "white_dollar");
			break;
		case 0x2d05:
			result = finishPointSpec(0x2d05, "Entertainment, Golf Course", "golf");
			break;
		case 0x2d06:
			result = finishPointSpec(0x2d06, "Recreation, Skiing Center/Resort", "skiing");
			break;
		case 0x2d07:
			result = finishPointSpec(0x2d07, "Entertainment, Bowling", "bowling");
			break;
		case 0x2d08:
			result = finishPointSpec(0x2d08, "Entertainment, Ice Skating");
			break;
		case 0x2d09:
			result = finishPointSpec(0x2d09, "Entertainment, Swimming Pool", "swimming");
			break;
		case 0x2d0a:
			result = finishPointSpec(0x2d0a, "Entertainment, Sports/Fitness Center", "fitness");
			break;
		case 0x2d0b:
			result = finishPointSpec(0x2d0b, "Entertainment, Sport Airport", "airport");
			break;
		case 0x2e00:
			result = finishPointSpec(0x2e00, "Shopping", "shopping_cart");
			break;
		case 0x2e01:
			result = finishPointSpec(0x2e01, "Shopping, Department Store", "dept_store");
			break;
		case 0x2e02:
			result = finishPointSpec(0x2e02, "Shopping, Grocery Store", "shopping_cart");
			break;
		case 0x2e03:
			result = finishPointSpec(0x2e03, "Shopping, General Merchandise", "shopping_cart");
			break;
		case 0x2e04:
			result = finishPointSpec(0x2e04, "Shopping Center", "shopping_cart");
			break;
		case 0x2e05:
			result = finishPointSpec(0x2e05, "Shopping, Pharmacy", "pharmacy");
			break;
		case 0x2e06:
			result = finishPointSpec(0x2e06, "Shopping, Convenience Store", "shopping_cart");
			break;
		case 0x2e07:
			result = finishPointSpec(0x2e07, "Shopping, Apparel", "shopping_cart");
			break;
		case 0x2e08:
			result = finishPointSpec(0x2e08, "Shopping, Home and Garden", "shopping_cart");
			break;
		case 0x2e09:
			result = finishPointSpec(0x2e09, "Shopping, Home Furnishings", "shopping_cart");
			break;
		case 0x2e0a:
			result = finishPointSpec(0x2e0a, "Shopping, Specialty Retail", "shopping_cart");
			break;
		case 0x2e0b:
			result = finishPointSpec(0x2e0b, "Shopping, Computer/Software", "shopping_cart");
			break;
		case 0x2e0c:
			result = finishPointSpec(0x2e0c, "Shopping, Other", "shopping_cart");
			break;
		case 0x2f00:
			result = finishPointSpec(0x2f00, "Service", "convenience_store");
			break;
		case 0x2f01:
			result = finishPointSpec(0x2f01, "Service, Auto Fuel", "convenience_store");
			break;
		case 0x2f02:
			result = finishPointSpec(0x2f02, "Service, Auto Rental", "car_rental");
			break;
		case 0x2f03:
			result = finishPointSpec(0x2f03, "Service, Auto Repair", "car_repair");
			break;
		case 0x2f04:
			result = finishPointSpec(0x2f04, "Service, Air Transportation", "airport");
			break;
		case 0x2f05:
			result = finishPointSpec(0x2f05, "Service, Post Office", "post_office");
			break;
		case 0x2f06:
			result = finishPointSpec(0x2f06, "Service, Bank/ATM", "white_dollar");
			break;
		case 0x2f07:
			result = finishPointSpec(0x2f07, "Service, Dealer/Auto Parts", "car_repair");
			break;
		case 0x2f08:
			result = finishPointSpec(0x2f08, "Service, Ground Transportation", "car");
			break;
		case 0x2f09:
			result = finishPointSpec(0x2f09, "Service, Marina/Boat Repair", "boat_ramp");
			break;
		case 0x2f0a:
			result = finishPointSpec(0x2f0a, "Service, Wrecker");
			break;
		case 0x2f0b:
			result = finishPointSpec(0x2f0b, "Service, Parking", "parking");
			break;
		case 0x2f0c:
			result = finishPointSpec(0x2f0c, "Service, Rest Area/Information", "restrooms");
			break;
		case 0x2f0d:
			result = finishPointSpec(0x2f0d, "Service, Auto Club", "car");
			break;
		case 0x2f0e:
			result = finishPointSpec(0x2f0e, "Service, Car Wash", "car");
			break;
		case 0x2f0f:
			result = finishPointSpec(0x2f0f, "Service, Garmin Dealer");
			break;
		case 0x2f10:
			result = finishPointSpec(0x2f10, "Service, Personal"); result = finishPointSpec(0x2f11, "Service, Business");
			break;
		case 0x2f12:
			result = finishPointSpec(0x2f12, "Service, Communication");
			break;
		case 0x2f13:
			result = finishPointSpec(0x2f13, "Service, Repair"); result = finishPointSpec(0x2f14, "Service, Social");
			break;
		case 0x2f15:
			result = finishPointSpec(0x2f15, "Service, Public Utility");
			break;
		case 0x3000:
			result = finishPointSpec(0x3000, "Emergency/Government", "white_house");
			break;
		case 0x3005:
			result = finishPointSpec(0x3005, "City Hall", "flag");
			break;
		case 0x3010:
			result = finishPointSpec(0x3010, "Community, Police Station", "flag");
			break;
		case 0x3020:
			result = finishPointSpec(0x3020, "Hospital", "first_aid");
			break;
		case 0x3030:
			result = finishPointSpec(0x3030, "Community, City Hall", "flag");
			break;
		case 0x3040:
			result = finishPointSpec(0x3040, "Community, Court House", "inspection_weigh_station");
			break;
		case 0x3050:
			result = finishPointSpec(0x3050, "Community, Community Center", "flag");
			break;
		case 0x3060:
			result = finishPointSpec(0x3060, "Community, Border Crossing", "crossing");
			break;

		case 0x4000:
			result = finishPointSpec(0x4000, "Golf", "golf");
			break;
		case 0x4100:
			result = finishPointSpec(0x4100, "Fishing", "white_fish");
			break;
		case 0x4200:
			result = finishPointSpec(0x4200, "Wreck", "white_wreck");
			break;
		case 0x4300:
			result = finishPointSpec(0x4300, "Marina", "white_anchor");
			break;
		case 0x4400:
			result = finishPointSpec(0x4400, "Gas Station", "white_fuel");
			break;
		case 0x4500:
			result = finishPointSpec(0x4500, "Food & Drink", "white_knife_fork");
			break;
		case 0x4600:
			result = finishPointSpec(0x4600, "Bar", "white_mug");
			break;
		case 0x4700:
			result = finishPointSpec(0x4700, "Boat Ramp", "boat_ramp");
			break;
		case 0x4800:
			result = finishPointSpec(0x4800, "Camping", "campground");
			break;
		case 0x4900:
			result = finishPointSpec(0x4900, "Park", "park");
			break;
		case 0x4a00:
			result = finishPointSpec(0x4a00, "Picnic Area", "picnic");
			break;
		case 0x4b00:
			result = finishPointSpec(0x4b00, "First Aid", "first_aid");
			break;
		case 0x4c00:
			result = finishPointSpec(0x4c00, "Information", "information");
			break;
		case 0x4d00:
			result = finishPointSpec(0x4d00, "Parking", "parking");
			break;
		case 0x4e00:
			result = finishPointSpec(0x4e00, "Restroom", "restrooms");
			break;
		case 0x4f00:
			result = finishPointSpec(0x4f00, "Shower", "shower");
			break;
		case 0x5000:
			result = finishPointSpec(0x5000, "Drinking Water", "drinking_water");
			break;
		case 0x5100:
			result = finishPointSpec(0x5100, "Telephone", "telephone");
			break;
		case 0x5200:
			result = finishPointSpec(0x5200, "Scenic Area", "orange_map_buoy");
			break;
		case 0x5300:
			result = finishPointSpec(0x5300, "Skiing", "skiing");
			break;
		case 0x5400:
			result = finishPointSpec(0x5400, "Swimming", "swimming");
			break;
		case 0x5500:
			result = finishPointSpec(0x5500, "Dam", "dam");
			break;
		case 0x5700:
			result = finishPointSpec(0x5700, "Danger", "danger");
			break;
		case 0x5800:
			result = finishPointSpec(0x5800, "Restrcited Area", "restricted_area");
			break;
		case 0x5900:
			result = finishPointSpec(0x5900, "Airport", "airport");
			break;
		case 0x5901:
			result = finishPointSpec(0x5901, "Airport, Large", "airport");
			break;
		case 0x5902:
			result = finishPointSpec(0x5902, "Airport, Medium", "airport");
			break;
		case 0x5903:
			result = finishPointSpec(0x5903, "Airport, Small", "airport");
			break;
		case 0x5904:
			result = finishPointSpec(0x5904, "Heliport", "heliport");
			break;
		case 0x5905:
			result = finishPointSpec(0x5905, "Airport", "airport");
			break;
		case 0x5d00:
			result = finishPointSpec(0x5d00, "Daymark, green square");
			break;
		case 0x5e00:
			result = finishPointSpec(0x5e00, "Daymark, red triangle");
			break;
		case 0x6100:
			result = finishPointSpec(0x6100, "Place");
			break;
		case 0x6200:
			result = finishPointSpec(0x6200, "Depth");
			break;
		case ELEVATION:
			result = finishPointSpec(type, "Elevation", "summit");
			break;
		case 0x6400:
			result = finishPointSpec(0x6400, "Man-made Feature", "building");
			break;
		case 0x6401:
			result = finishPointSpec(0x6401, "Bridge", "bridge");
			break;
		case 0x6402:
			result = finishPointSpec(0x6402, "Building", "building");
			break;
		case 0x6403:
			result = finishPointSpec(0x6403, "Cemetary", "cemetery");
			break;
		case 0x6404:
			result = finishPointSpec(0x6404, "Church", "church");
			break;
		case 0x6405:
			result = finishPointSpec(0x6405, "Civil Building", "civil_location");
			break;
		case 0x6406:
			result = finishPointSpec(0x6406, "Crossing", "crossing");
			break;
		case 0x6407:
			result = finishPointSpec(0x6407, "Dam", "dam");
			break;
		case 0x6408:
			result = finishPointSpec(0x6408, "Hospital", "first_aid");
			break;
		case 0x6409:
			result = finishPointSpec(0x6409, "Levee");
			break;
		case 0x640a:
			result = finishPointSpec(0x640a, "Locale");
			break;
		case 0x640b:
			result = finishPointSpec(0x640b, "Military", "military_location");
			break;
		case 0x640c:
			result = finishPointSpec(0x640c, "Mine", "mine");
			break;
		case 0x640d:
			result = finishPointSpec(0x640d, "Oil Field", "oil_field");
			break;
		case 0x640e:
			result = finishPointSpec(0x640e, "Park", "park");
			break;
		case 0x640f:
			result = finishPointSpec(0x640f, "Post Office", "post_office");
			break;
		case 0x6410:
			result = finishPointSpec(0x6410, "School", "school");
			break;
		case 0x6411:
			result = finishPointSpec(0x6411, "Tower", "tall_tower");
			break;
		case 0x6412:
			result = finishPointSpec(0x6412, "Trail", "trail_head");
			break;
		case 0x6413:
			result = finishPointSpec(0x6413, "Tunnel", "tunnel");
			break;
		case 0x6414:
			result = finishPointSpec(0x6414, "Well", "drinking_water");
			break;
		case 0x6415:
			result = finishPointSpec(0x6415, "Ghost Town", "historical_town");
			break;
		case 0x6416:
			result = finishPointSpec(0x6416, "Subdivision");
			break;
		case 0x6500:
			result = finishPointSpec(0x6500, "Water Feature");
			break;
		case 0x6501:
			result = finishPointSpec(0x6501, "Arroyo");
			break;
		case 0x6502:
			result = finishPointSpec(0x6502, "Sand Bar");
			break;
		case 0x6503:
			result = finishPointSpec(0x6503, "Bay");
			break;
		case 0x6504:
			result = finishPointSpec(0x6504, "Bend");
			break;
		case 0x6505:
			result = finishPointSpec(0x6505, "Canal");
			break;
		case 0x6506:
			result = finishPointSpec(0x6506, "Channel");
			break;
		case 0x6507:
			result = finishPointSpec(0x6507, "Cove");
			break;
		case 0x6508:
			result = finishPointSpec(0x6508, "Falls");
			break;
		case 0x6509:
			result = finishPointSpec(0x6509, "Geyser");
			break;
		case 0x650a:
			result = finishPointSpec(0x650a, "Glacier");
			break;
		case 0x650b:
			result = finishPointSpec(0x650b, "Harbor");
			break;
		case 0x650c:
			result = finishPointSpec(0x650c, "Island");
			break;
		case 0x650d:
			result = finishPointSpec(0x650d, "Lake");
			break;
		case 0x650e:
			result = finishPointSpec(0x650e, "Rapids");
			break;
		case 0x650f:
			result = finishPointSpec(0x650f, "Resevoir");
			break;
		case 0x6510:
			result = finishPointSpec(0x6510, "Sea");
			break;
		case 0x6511:
			result = finishPointSpec(0x6511, "Spring");
			break;
		case 0x6512:
			result = finishPointSpec(0x6512, "Stream");
			break;
		case 0x6513:
			result = finishPointSpec(0x6513, "Swamp");
			break;
		case 0x6600:
			result = finishPointSpec(0x6600, "Land Feature");
			break;
		case 0x6601:
			result = finishPointSpec(0x6601, "Arch");
			break;
		case 0x6602:
			result = finishPointSpec(0x6602, "Area");
			break;
		case 0x6603:
			result = finishPointSpec(0x6603, "Basin");
			break;
		case 0x6604:
			result = finishPointSpec(0x6604, "Beach");
			break;
		case 0x6605:
			result = finishPointSpec(0x6605, "Bench");
			break;
		case 0x6606:
			result = finishPointSpec(0x6606, "Cape");
			break;
		case 0x6607:
			result = finishPointSpec(0x6607, "Cliff");
			break;
		case 0x6608:
			result = finishPointSpec(0x6608, "Crater");
			break;
		case 0x6609:
			result = finishPointSpec(0x6609, "Flat");
			break;
		case 0x660a:
			result = finishPointSpec(0x660a, "Forest");
			break;
		case 0x660b:
			result = finishPointSpec(0x660b, "Gap");
			break;
		case 0x660c:
			result = finishPointSpec(0x660c, "Gut");
			break;
		case 0x660d:
			result = finishPointSpec(0x660d, "Isthmus");
			break;
		case 0x660e:
			result = finishPointSpec(0x660e, "Lava");
			break;
		case 0x660f:
			result = finishPointSpec(0x660f, "Pillar");
			break;
		case 0x6610:
			result = finishPointSpec(0x6610, "Plain");
			break;
		case 0x6611:
			result = finishPointSpec(0x6611, "Range");
			break;
		case 0x6612:
			result = finishPointSpec(0x6612, "Reserve");
			break;
		case 0x6613:
			result = finishPointSpec(0x6613, "Ridge");
			break;
		case 0x6614:
			result = finishPointSpec(0x6614, "Rock");
			break;
		case 0x6615:
			result = finishPointSpec(0x6615, "Slope");
			break;
		case ELEVATION1:
			result = finishPointSpec(type, "Summit", "summit");
			break;
		case 0x6617:
			result = finishPointSpec(0x6617, "Valley");
			break;
		case 0x6618:
			result = finishPointSpec(0x6618, "Woods", "forest");
			break;
		default:
			Timber.w("createPointSpec(" + type + "), unknown type");
			result = new PointDrawSpec(type, String.valueOf(type), "", true, 255);
			break;
		}
		
		// return result
		return result;
	}
	
	private PointDrawSpec finishPointSpec(int type, String name) {
		return finishPointSpec(type, name, null);
	}
	
	private PointDrawSpec finishPointSpec(int type, String name, String iconName) {
		return finishPointSpec(type, name, iconName, type >> 8);
	}
	
	private PointDrawSpec finishPointSpec(int type, String name, String iconName, int priority) {
		boolean point = true;
		if (iconName == NO_POINT) {
			point = false;
			iconName = null;
		}
		
		PointDrawSpec spec = new PointDrawSpec(type, name, iconName, point, priority);
		spec.setDrawLabelsParams(12, ImgConstants.ZOOM_MAX);
		return spec;
	}
	
	/**************************************************/
	/*                 LINE PARAMETERS                */
    /**************************************************/

    private static final float[] dashDots = {
            UtilsGarminImg.getDpPixels(3.0f), UtilsGarminImg.getDpPixels(3.0f)};
    private static final float[] dashTiny = {
            UtilsGarminImg.getDpPixels(5.0f), UtilsGarminImg.getDpPixels(2.0f)};
    private static final float[] dashSmall = {
            UtilsGarminImg.getDpPixels(5.0f), UtilsGarminImg.getDpPixels(5.0f)};
    private static final float[] dashLong = {
            UtilsGarminImg.getDpPixels(10.0f), UtilsGarminImg.getDpPixels(10.0f)};
    private static final float[] dashPowerline = {
            UtilsGarminImg.getDpPixels(2.0f), UtilsGarminImg.getDpPixels(10.0f)};
	
	// DEFINITIONS

	public LinePolyDrawSpec getLineType(int type, Label label) {
		// handle by type
		if (type >= MINOR_LAND_CONTOUR &&
				type <= MAJOR_DEPTH_CONTOUR) {
			// check label
			if (label == null || label.getNameNoException().equals("")) {
				return getLineType(type);
			}

			// separate contours by my own
			int contour = Utils.parseInt(label.getNameNoException(), 0);
			if (contour >= 0) {
				if (contour % 100 == 0) {
					return getLineType(MAJOR_LAND_CONTOUR);
				} else if (contour % 50 == 0) {
					return getLineType(INTERMEDIATE_LAND_CONTOUR);
				} else {
					return getLineType(MINOR_LAND_CONTOUR);
				}
			} else {
				if (contour % 100 == 0) {
					return getLineType(MAJOR_DEPTH_CONTOUR);
				} else if (contour % 50 == 0) {
					return getLineType(INTERMEDIATE_DEPTH_CONTOUR);
				} else {
					return getLineType(MINOR_DEPTH_CONTOUR);
				}
			}
		} else {
			return getLineType(type);
		}
	}
	
	public LinePolyDrawSpec getLineType(int type) {
		// get type from cache
		LinePolyDrawSpec result = LINE_TYPES.get(type);
		if (result != null) {
			return result;
		}
		
		// create and cache new style
		result = createLineSpec(type);
		result.setType(type);
		LINE_TYPES.put(type, result);
		return result;
	}

	private LinePolyDrawSpec createLineSpec(int type) {
		LinePolyDrawSpec result = null;
		switch (type) {
		case 0x29: /* 41 */
		case 0x10f00:
			result = finishLineSpec(type, 10, "Powerline", 
					new LinePainterDouble(
							new BasicStroke(2.0f, dashPowerline, Color.parseColor("#c18878")),
							new BasicStroke(1.0f, Color.parseColor("#c18878"))));
			break;
			
		// WALLS
			
		case 0x10102:
			result = finishLineSpec(type, 20, "Wall", 
					1.0f, Color.parseColor("#7E7C77"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
			
		// ROAD EXTENSIONS & BRIDGES
			
		case 0x1000b:
		case 0x10004: // bridge - railroad
			result = createLineSpec(ID_LINE_RAILROAD);
			result.setPriority(30);
			break;			
		case 0x10007: // bridge - arterial road
			result = createLineSpec(0x04);
			result.setPriority(30);
			break;	
		case 0x10603: // bridge - trail
			result = createLineSpec(0x10);
			result.setPriority(30);
			break;
		case 0x1040c: // direction arrows
		case 0x10702:
		case 0x10707:
			result = finishLineSpec(type, 30, "Direction arrows",
					new LinePainterSimple(new PaintPath(UtilsRender.createPathArrows(), 
							Color.parseColor("#373634"), 2.0f, 30.0f)));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x10e06:
			result = finishLineSpec(type, 30, "\'Blind\' street",
					2.0f, dashSmall, Color.parseColor("#83122F"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
			
		// ROADS & WAYS & TRAILS
			
		case ID_LINE_RAILROAD: // 20
		case 0x19: // 25
		case 0x10010:
			result = finishLineSpec(type, 150, "Railroad",
					new LinePainterDouble(
							new BasicStroke(1.0f, dashLong, Color.WHITE),
							new BasicStroke(2.0f, Color.BLACK)));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x01:
		case 0x1060d:
		case 0x10f09: // Low zoom level
		case 0x10013: // Lowest zoom level
			result = finishLineSpec(type, 25, "Major highway", 
					4.0f, Color.parseColor("#7B95D5"), Color.BLACK);
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(15, ZOOM_MAX);
			break;
		case 0x02:
		case 0x1060f:
		case 0x10f04: // Low zoom level
		case 0x10f0c: // Lowest zoom level
			result = finishLineSpec(type, 30, "Principal highway",
					3.0f, Color.parseColor("#7BCA8B"), Color.BLACK);
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(16, ZOOM_MAX);
			break;
		case 0x03:
		case 0x12:  // Secondary road
			result = finishLineSpec(type, 40, "Other highway",
					1.5f, Color.WHITE, Color.BLACK);
			break;
		case 0x04:    // Secondary highway
		case 0x10f0e: // Low zoom level
		case 0x1061c: // Roundabout
			result = finishLineSpec(type, 50, "Arterial road",
					2.5f, Color.parseColor("#FF6500"), Color.BLACK);
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(15, ZOOM_MAX);
			break;
		case 0x05:    //
		case 0x10f0f: // Low zoom level
			result = finishLineSpec(type, 60, "Tertiary road",
					2.0f, Color.parseColor("#FFB93C"), Color.BLACK);
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(15, ZOOM_MAX);
			break;
		case 0x06:
		case 0x10706:
		case 0x10e0c:
		case 0x1001e: // Low zoom level
			result = finishLineSpec(type, 70, "Residential street",
					2.0f, Color.WHITE, Color.BLACK);
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(17, ZOOM_MAX);
			break;
		case 0x10008: // Residential street - lowest
			result = finishLineSpec(type, 70, "Residential street",
					1.5f, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x07:
			result = finishLineSpec(type, 80, "Alley/Private road",
					2.0f, Color.BLACK);
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(19, ZOOM_MAX);
			break;
		case 0x08:
			result = finishLineSpec(0x08, 90, "Highway ramp, low speed",
					1.0f, Color.BLACK);
			break;
		case 0x09:
			result = finishLineSpec(0x09, 100, "Highway ramp, high speed",
					1.0f, Color.BLACK);
			break;
		case 0x0b: // 11, unclassified
			result = finishLineSpec(type, 120, "Major highway connector",
					2.0f, Color.parseColor("#FFCA41"), Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x10f15:
			result = finishLineSpec(type, 120, "Principal highway connector",
					1.5f, Color.parseColor("#7BCA8B"), Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x0c: // 12
			result = finishLineSpec(0x0c, 130, "Roundabout",
					1.0f, Color.BLACK);
			break;
		case 0x10605:
			result = finishLineSpec(type, 100, "Trail",
					2.0f, Color.parseColor("#878787"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x10:
		case 0x0d:
		case 0x0e:
			result = finishLineSpec(type, 100, "Trail",
					2.0f, dashLong, Color.parseColor("#FF0020"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x0a:
			result = finishLineSpec(0x0a, 110, "Unnpaved road",
					1.0f, dashSmall, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x0f: // 15
			/* Probably pavement, maybe hike or cycle road */
		case 0x10f01:
			/* used on Finland maps as border in see, but also a border around football stadium! */
			result = finishLineSpec(0x0f, 140, "Pavement",
					1.0f, dashSmall, Color.BLACK);
			break;
		case ID_LINE_PAVEMENT: // 0x13 - 19
			result = finishLineSpec(type, 140, "Pavement",
					1.0f, dashSmall, Color.parseColor("#BB6400"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x10610:
			result = finishLineSpec(type, 140, "Pavement",
					2.0f, dashDots, Color.parseColor("#595959"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x1060c:
		case 0x10100:
			result = finishLineSpec(type, 140, "Pavement",
					1.0f, dashDots, Color.parseColor("#595959"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x15: // 21
			result = finishLineSpec(0x15, 160, "Shoreline",
					1.0f, Color.rgb(128, 64, 0));
			break;
		case ID_LINE_TRAIL: // 22
		case 0x10109:
			result = finishLineSpec(type, 170, "Trail",
					2.0f, Color.parseColor("#B36B17"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x10e09:
		case 0x10011:
			result = finishLineSpec(type, 170, "Trail",
					2.0f, Color.parseColor("#FF0020"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x1001f: // 65567
		case 0x10e08: // bridge pavements
			result = createLineSpec(ID_LINE_PAVEMENT);
			break;
		case 0x10e0d: // 69133
			result = finishLineSpec(type, 160, "Trail",
					2.0f, dashSmall, Color.parseColor("#BB6400"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x18:
			result = finishLineSpec(type, 180, "Stream",
					1.0f, cWater);
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(19, ZOOM_MAX);
			break;
		case 0x10108:
			result = finishLineSpec(type, 190, "Weir", 
					2.0f, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x1a:
			result = finishLineSpec(type, 190, "Ferry", 
					1.0f, Color.BLACK);
			break;
		case 0x1b:
			result = finishLineSpec(type, 200, "Ferry", 
					1.0f, Color.BLACK);
			break;
		case 0x1c:
			result = finishLineSpec(type, 210, "State/province border", 
					1.0f, dashDots, Color.LTGRAY);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x1d:
			result = finishLineSpec(type, 220, "County/parish border", 
					1.0f, Color.LTGRAY);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x1e:
			result = finishLineSpec(type, 230, "International border",
					new LinePainterSimple(new InternationalBorderStroke(10f, 6f, 1.5f), Color.BLACK));
			break;
			
		// WATER & RIVERS
			
		case 0x1f:
			result = finishLineSpec(0x1f, 240, "River", 
					2.0f, Color.parseColor("#7B95B4"));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x26:
			result = finishLineSpec(0x26, 250, "Intermittent stream", 
					1.0f, cWater);
			break;
		case 0x27:
			result = finishLineSpec(0x27, 260, "Airport runway", 
					5.0f, Color.BLACK);
			break;
		case 0x28:
			result = finishLineSpec(0x28, 270, "Pipeline", 
					1.0f, Color.BLUE);
			break;
		case 0x2a:
			result = finishLineSpec(0x2a, 280, "Marine boundary",
					1.0f, Color.BLUE);
			break;
		case 0x2b:
			result = finishLineSpec(0x2b, 290, "Hazard boundary", 
					1.0f, Color.RED);
			break;
		case TUNNEL_SHIFT + 0x01:
			// roads or rail in tunnel for Swiss Topo 50
			result = finishLineSpec(type, 300, "Major highway (tunnel)",
					2.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x02:
			result = finishLineSpec(type, 310, "Principal highway (tunnel)",
					2.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x03:
			result = finishLineSpec(type, 320, "Other highway (tunnel)",
					2.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x04:
			result = finishLineSpec(type, 330, "Arterial road (tunnel)",
					2.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x05:
			result = finishLineSpec(type, 340, "Collector road (tunnel)",
					1.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x06:
			result = finishLineSpec(type, 350, "Residential street (tunnel)",
					1.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x07:
			result = finishLineSpec(type, 360, "Alley/Private road (tunnel)",
					0.5f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x08:
			result = finishLineSpec(type, 370, "Highway ramp, low speed (tunnel)",
					1.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x09:
			result = finishLineSpec(type, 380, "Highway ramp, high speed (tunnel)",
					1.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x0a:
			result = finishLineSpec(type, 390, "Unpaved road (tunnel)",
					0.5f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x0b:
			result = finishLineSpec(type, 400, "Major highway connector (tunnel)",
					1.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x0c:
			result = finishLineSpec(type, 410, "Roundabout (tunnel)",
					1.0f, dashTiny, Color.BLACK);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case TUNNEL_SHIFT + 0x14:
			result = finishLineSpec(type, 420, "Railroad (tunnel)",
					new LinePainterDouble(
							new BasicStroke(1.0f, dashLong, Color.WHITE),
							new BasicStroke(2.0f, Color.BLACK)));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case RUINS:
			result = finishLineSpec(type, 430, "Ruins",
					4.0f, Color.GRAY);
			break;
			
		case MAJOR_LAND_CONTOUR:
			result = finishLineSpec(type, 1000, "Major land contour",
					new LinePainterSimple(new BasicStroke(0.5f, cContourLand)));
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(15, ZOOM_MAX);
			break;
		case INTERMEDIATE_LAND_CONTOUR:
			result = finishLineSpec(type, 1010, "Intermediate land contour",
					new LinePainterSimple(new BasicStroke(0.2f, dashTiny, cContourLand)));
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(17, ZOOM_MAX);
			break;
		case MINOR_LAND_CONTOUR:
			result = finishLineSpec(type, 1020, "Minor land contour",
					new LinePainterSimple(new BasicStroke(0.1f, dashTiny, cContourLand)));
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(19, ZOOM_MAX);
			break;

		case MAJOR_DEPTH_CONTOUR:
			result = finishLineSpec(type, 1030, "Major depth contour",
					new LinePainterSimple(new BasicStroke(0.5f, cContourLand)));
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(15, ZOOM_MAX);
			break;
		case INTERMEDIATE_DEPTH_CONTOUR:
			result = finishLineSpec(type, 1040, "Intermediate depth contour",
					new LinePainterSimple(new BasicStroke(0.2f, dashTiny, cContourLand)));
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(17, ZOOM_MAX);
			break;
		case MINOR_DEPTH_CONTOUR:
			result = finishLineSpec(type, 1050, "Minor depth contour",
					new LinePainterSimple(new BasicStroke(0.1f, dashTiny, cContourLand)));
			result.setDrawLabel(DrawLabel.HANDLE);
			result.setDrawLabelsParams(19, ZOOM_MAX);
			break;
		default:
			Timber.w(TAG, "createLineSpec(" + type + "), unknown type");
			result = finishLineSpec(type, 1, "Unknown",
					1.5f, Color.MAGENTA);
			break;
		}
		
		// return result
		return result;
	}
	
	private LinePolyDrawSpec finishLineSpec(int type, int priority, 
			String name, LinePolyPainter painter) {
		return new LinePolyDrawSpec(type, name,
				painter, POLYLINE_BASE_PRIORITY, priority);
	}
	
	private LinePolyDrawSpec finishLineSpec(int type, int priority, 
			String name, float strokeWidth, int color) {
		LinePainterSimple painter = new LinePainterSimple(
				new BasicStroke(strokeWidth, color));
		return new LinePolyDrawSpec(type, name,
				painter, POLYLINE_BASE_PRIORITY, priority);
	}
	
	private LinePolyDrawSpec finishLineSpec(int type, int priority, 
			String name, float strokeWidth, int colorFg, int colorBg) {
		LinePainterDouble painter = new LinePainterDouble(
				new BasicStroke(strokeWidth, colorFg),
				new BasicStroke(strokeWidth + 1.0f, colorBg));
		return new LinePolyDrawSpec(type, name,
				painter, POLYLINE_BASE_PRIORITY, priority);
	}
	
	private LinePolyDrawSpec finishLineSpec(int type, int priority, 
			String name, float strokeWidth, float[] dashType, int color) {
		LinePainterSimple painter = new LinePainterSimple(
				new BasicStroke(strokeWidth, dashType, color));
		return new LinePolyDrawSpec(type, name,
				painter, POLYLINE_BASE_PRIORITY, priority);
	}
	
	/**************************************************/
	/*               POLYGONS PARAMETERS              */
	/**************************************************/
	
	// DEFINITIONS
	
	private static final int POLY_PRIOR_BG_AREAS = 20000;
	
	public LinePolyDrawSpec getPolygonType(int type) {
		// get type from cache
		LinePolyDrawSpec result = POLYGON_TYPES.get(type);
		if (result != null) {
			return result;
		}
		
		// create and cache new style
		result = createPolySpec(type);
		POLYGON_TYPES.put(type, result);
		return result;
	}
	
	private LinePolyDrawSpec createPolySpec(int type) {
		LinePolyDrawSpec result = null;
		switch (type) {
		case 0x06:
			result = finishPolySpec(type, 10, "Parking garage",
					Color.GRAY);
			break;
		case AIRPORT:
			result = finishPolySpec(type, 40, "Airport", 
					Color.GRAY);
			break;
		case 0x0c: /* 12 */
			result = finishPolySpec(type, 40, "Industrial complex", 
						Color.parseColor("#E7D2FF"), true);
			break;
		case 0x0e:
			result = finishPolySpec(type, 40, "Airport runnway",
					Color.GRAY);
			break;
		case 0x13: // Man-made area
		case 0x10: // Area of building in lower zoom level
		case 0x10f05:
		case 0x10f16:
			result = finishPolySpec(type, 40, "Building",
					Color.parseColor("#C5C5C5"));
			break;
		case 0x34:
			result = finishPolySpec(type, 40, "University, School",
					Color.parseColor("#949494"));
			break;
			
		// SPORT & ACTIVITY
			
		case 0x18:
			result = finishPolySpec(type, 40, "Golf course", 
					Color.GREEN);
			break;
		case 0x19:
			result = finishPolySpec(type, 40, "Sports complex",
					Color.parseColor("#BD956A"));
			break;
		case 0x1d:
			result = finishPolySpec(type, 40, "Stadium",
					Color.parseColor("#BD956A"));
			break;
		case 0x11012:
			result = finishPolySpec(type, 40, "Soccer", 
					Color.parseColor("#525252")); 
			break;
			
		// SOCIAL & STATE

		case 0x1a:
			result = finishPolySpec(type, 40, "Cemetary",
					Color.GRAY);
			break;
		case SHOPPING_CENTER:
			result = finishPolySpec(type, 40, "Shopping center",
					Color.GRAY);
			break;
		case 0x10901: // Unknown
			result = finishPolySpec(type, 40, "", 
					Color.LTGRAY);
			break;
		case 0x10903: // Unknown
			result = finishPolySpec(type, 40, "", 
					Color.LTGRAY);
			break;
		case 0x10400:
			result = finishPolySpec(type, 40, "Query (deeper)",
					Color.DKGRAY);
			break;
		case 0x10500:
			result = finishPolySpec(type, 40, "Query",
					Color.LTGRAY);
			break;
		case 0x09:
			result = finishPolySpec(type, 40, "Marina", 
					Color.GRAY);
			break;
		case 0x0a:
			result = finishPolySpec(type, 50, "University/college",
					Color.GRAY);
			break;
		case 0x0b:
			result = finishPolySpec(type, 60, "Hospital",
					Color.GRAY);
			break;
		case 0x11008:
			result = finishPolySpec(type, 60, "Bank",
					Color.parseColor("#838383"));
			break;
		case 0x1100a:
			result = finishPolySpec(type, 60, "Pharmacy",
					Color.parseColor("#838383"));
			break;
		case 0x1100b:
			result = finishPolySpec(type, 60, "Administrative builing",
					Color.parseColor("#737373"));
			break;
		case STATION_AREA:
			result = finishPolySpec(type, 60, "Station area", 
					Color.GRAY, false);
			break;
		case GRAVEL_AREA:
			result = finishPolySpec(type, 60, "Gravel area",
					Color.LTGRAY, true);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x05: /* 05 */
			result = finishPolySpec(type, 60, "Parking lot", 
					new PolyPainterSimple(new PaintPattern(
							PatternStyle.DOTS, Color.parseColor("#B4B4B4")), true));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x04: /* 04 */
			result = finishPolySpec(type, 60, "Military base", 
					new PolyPainterSimple(Color.GRAY, true));
			break;
			
		// BASIC WATERS
			
		case 0x3c:
			result = finishPolySpec(type, 250, "Large lake",
					cWater);
			break;
		case 0x3d:
			result = finishPolySpec(type, 250, "Large lake", 
					cWater);
			break;
		case 0x3e:
			result = finishPolySpec(type, 250, "Medium lake",
					cWater);
			break;
		case 0x3f:
			result = finishPolySpec(type, 250, "Medium lake",
					cWater);
			break;
		case 0x40:
			result = finishPolySpec(type, 250, "Small lake",
					cWater);
			break;
		case 0x41: /* 65 */
			result = finishPolySpec(type, 250, "Lake",
					cWater);
			break;
		case 0x42:
			result = finishPolySpec(type, 250, "Major lake",
					cWater);
			break;
		case 0x43:
			result = finishPolySpec(type, 250, "Major lake", 
					cWater);
			break;
		case 0x44:
			result = finishPolySpec(type, 250, "Large lake",
					cWater);
			break;
		case 0x45:
			result = finishPolySpec(type, 250, "Blue (unknown)", 
					cWater);
			break;
		case 0x46:
			result = finishPolySpec(type, 250, "Major River", 
					cWater);
			break;
		case 0x47:
			result = finishPolySpec(type, 250, "Large River", 
					cWater);
			break;
		case 0x48:
			result = finishPolySpec(type, 250, "Medium River",
					cWater);
			break;
		case 0x49:
			result = finishPolySpec(type, 250, "Small River", 
					cWater);
			break;
			
		// GREEN AREAS & VARIOUS

		case 0x11018:
			result = finishPolySpec(type, 300, "Garden",
					Color.parseColor("#BDFFB4"));
			break;
		case 0x17: /* 23 */
		case 0x27:
		case 0x10106:
			result = finishPolySpec(type, 300, "City park",
					Color.parseColor("#D1FFAD"));
			break;
		case 0x0d:
			result = finishPolySpec(type, 300, "Reservation",
					Color.GREEN);
			break;
		case 0x14:
		case 0x15:
		case 0x16: /* 22 */
			result = finishPolySpec(type, 300, "National park",
					cPark);
			break;
		case 0x1e: /* 30 */
			result = finishPolySpec(type, 300, "State park",
					Color.parseColor("#c7f1a3"), false);
			break;
		case 0x1f: /* 31 */
			result = finishPolySpec(type, 300, "State park",
					Color.parseColor("#c7f1a3"), false);
			break;
		case 0x20: /* 32 */
			result = finishPolySpec(type, 300, "State park", 
					Color.parseColor("#c7f1a3"), false);
			break;
		case 0x4e:
			result = finishPolySpec(type, 300, "Orchard/Plantation", Color.rgb(210, 255, 210));
			break;
		case 0x4f: /* 79 */
		case 0x11017:
			result = finishPolySpec(type, 300, "Scrub",
					new PolyPainterSimple(new PaintPattern(PatternStyle.DIAGONAL_1,
							Color.parseColor("#C1E0B1")), false));
			break;
		case 0x50:
		case FOREST:
		case 0x10700:
			result = finishPolySpec(type, 300, "Forest",
					Color.parseColor("#E2F8E0"));
			result.getPainter().setBackgroundLayer(true);
			break;
		case 0x51:
			result = finishPolySpec(type, 300, "Wetland/Swamp", 
					new PolyPainterSimple(swampPaint, true));
			break;
		case 0x33:
		case 0x10101:
		case 0x10102:
		case 0x10103:
		case 0x10600:
			result = finishPolySpec(type, 300, "Meadow", 
					Color.parseColor("#D0FCA4"));
			break;
		case 0x11:
			result = finishPolySpec(type, 300, "Quarry",
					new PolyPainterSimple(new PaintPattern(PatternStyle.CROSS,
							Color.parseColor("#626262")), false));
			break;
		case 0x52:
			result = finishPolySpec(type, 300, "Tundra", 
					Color.MAGENTA);
			break;
		case 0x53:
			result = finishPolySpec(type, 300, "Flat", 
					new PolyPainterSimple(rocksPaint, false));
			break;
		case 0x10900:
			result = finishPolySpec(type, 400, "Unknown area",
					Color.LTGRAY);
			break;
		case 0x1101c:
			result = finishPolySpec(type, 400, "Wetland",
					new PolyPainterSimple(new PaintPattern(PatternStyle.DIAGONAL_1,
							Color.parseColor("#3965D5")), false));
			break;
			
		// BIG WATERS
			
		case 0x28:
			result = finishPolySpec(type, 500, "Ocean",
					cWater);
			break;
		case 0x29:
			result = finishPolySpec(type, 500, "Blue (unknown)", 
					cWater);
			break;
		case 0x32:
			result = finishPolySpec(type, 500, "Sea",
					cWater);
			break;
		case 0x3b:
			result = finishPolySpec(type, 500, "Blue (unknown)", 
					cWater);
			break;
		case DEFINITION_AREA:
			result = finishPolySpec(type, 500, "Definition area", 
					BG_COLOR, true);
			break;
		case 0x4c:
			result = finishPolySpec(type, 500, "Intermittent water", 
					Color.BLUE);
			break;
		case 0x4d:
			result = finishPolySpec(type, 500, "Glacier", 
					Color.rgb(220, 220, 255));
			break;
		
		// BACKGROUND AREAS
			
		case 0x10105: // unknown
			result = finishPolySpec(type, POLY_PRIOR_BG_AREAS + 70, "",
					Color.parseColor("#D1C7D1"));
			
		case 0x03: /* 03 */
			result = finishPolySpec(type, POLY_PRIOR_BG_AREAS + 70, "Rural housing area",
					new PolyPainterSimple(cUrbanArea, false));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x02: /* 02 */
			result = finishPolySpec(type, POLY_PRIOR_BG_AREAS + 80, "Small urban area (<200K)", 
					new PolyPainterSimple(cUrbanArea, false));
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		case 0x01: /* 01 */
			result = finishPolySpec(type, POLY_PRIOR_BG_AREAS + 90, "Large urban area (>200K)", 
					new PolyPainterSimple(cUrbanArea, false));
			result.setDrawLabel(DrawLabel.NEVER);
			break; 
		case 0x10100:
			result = finishPolySpec(type, POLY_PRIOR_BG_AREAS, "Unknown area",
					Color.parseColor("#FFFFFF"));
			result.setDrawLabel(DrawLabel.NEVER);
			result.getPainter().setBackgroundLayer(true);
			break;
		case 0x11019:
			result = finishPolySpec(type, POLY_PRIOR_BG_AREAS + 100, "Farm",
					Color.parseColor("#FFFDF1"));
			result.setDrawLabel(DrawLabel.NEVER);
			result.getPainter().setBackgroundLayer(true);
			break;
		case BACKGROUND: /* 75 */
			result = finishPolySpec(type, Integer.MAX_VALUE, "Background",
					BG_COLOR);
			result.setDrawLabel(DrawLabel.NEVER);
			result.getPainter().setBackgroundLayer(true);
			break;
		default:
			Timber.w(TAG, "createPolySpec(" + type + "), unknown type");
			result = finishPolySpec(type, Integer.MAX_VALUE, "Unknown area",
					Color.MAGENTA);
			result.setDrawLabel(DrawLabel.NEVER);
			break;
		}
		
		// return result
		return result;
	}
	
	private LinePolyDrawSpec finishPolySpec(int type, int priority, 
			String name, int color) {
		return finishPolySpec(type, priority, name, color, false);
	}
	
	private LinePolyDrawSpec finishPolySpec(int type, int priority, 
			String name, int color, boolean border) {
		PolyPainterSimple painter = new PolyPainterSimple(color, border);
		return finishPolySpec(type, priority, name, painter);
	}
	
	private LinePolyDrawSpec finishPolySpec(int type, int priority, 
			String name, LinePolyPainter painter) {
		return new LinePolyDrawSpec(type, name,
				painter, POLYGON_BASE_PRIORITY, priority);
	}
}
