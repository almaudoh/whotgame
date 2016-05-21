package org.anieanie.card.whot;

import org.testng.annotations.DataProvider;

/**
 * Provides data providers for WhotCardTest
 *
 * Created by almaudoh on 5/21/16.
 */

public class WhotCardTestProvider {

    @DataProvider(name = "legal_values")
    public static Object[][] legalValues() {
        return new Object[][] {
            { WhotCard.WHOT, 20 },
            { WhotCard.STAR, 1 },   { WhotCard.STAR, 8 },   { WhotCard.STAR, 4 },   { WhotCard.STAR, 5 },   { WhotCard.STAR, 7 },
            { WhotCard.ANGLE, 1 },  { WhotCard.ANGLE, 8 },  { WhotCard.ANGLE, 4 },  { WhotCard.ANGLE, 7 },   { WhotCard.ANGLE, 14 },
            { WhotCard.CARPET, 1 }, { WhotCard.CARPET, 5 }, { WhotCard.CARPET, 7 }, { WhotCard.CARPET, 10 }, { WhotCard.CARPET, 14 },
            { WhotCard.BALL, 1 },   { WhotCard.BALL, 8 },   { WhotCard.BALL, 7 },  { WhotCard.BALL, 12 },    { WhotCard.BALL, 14 },
            { WhotCard.CROSS, 1 },  { WhotCard.CROSS, 5 },  { WhotCard.CROSS, 7 },  { WhotCard.CROSS, 11 },  { WhotCard.CROSS, 14 },
        };
    }

    @DataProvider(name = "illegal_values")
    public static Object[][] illegalValues() {
        return new Object[][] {
            { WhotCard.WHOT, 1 },   { WhotCard.WHOT, 3 },   { WhotCard.WHOT, 14 },  { WhotCard.WHOT, 30 },
            { WhotCard.STAR, 6 },   { WhotCard.STAR, 9 },   { WhotCard.STAR, 10 },   { WhotCard.STAR, 16 },
            { WhotCard.ANGLE, 9 },  { WhotCard.ANGLE, 6 },
            { WhotCard.CARPET, 9 }, { WhotCard.CARPET, 4 }, { WhotCard.CARPET, 6 }, { WhotCard.CARPET, 12 },
            { WhotCard.BALL, 9 },   { WhotCard.BALL, 6 },
            { WhotCard.CROSS, 9 },  { WhotCard.CROSS, 8 },  { WhotCard.CROSS, 6 },  { WhotCard.CROSS, 12 },
        };
    }

    @DataProvider(name = "illegal_pairs")
    public static Object[][] illegalPairs() {
        return new Object[][] {
            { 0, 34 },
            { -1, 10 },
            { -4, 34 },
            { 2, 16 },
            { 3, 15 },
            { 5, 21 },
            { 7, 10 },
        };
    }

    @DataProvider(name = "shape_names")
    public static Object[][] shapeNames() {
        return new Object[][] {
            { 0, 1, "Star" },
            { 1, 1, "Cross" },
            { 2, 1, "Circle" },
            { 3, 1, "Square" },
            { 4, 1, "Triangle" }, 
            { 5, 20, "Whot" },
        };
    }

    @DataProvider(name = "from_string")
    public static Object[][] fromString() {
        return new Object[][]{
            { "Star 1", new WhotCard(WhotCard.STAR, 1)},
            { "Cross 10", new WhotCard(WhotCard.CROSS, 10)},
            { "Triangle 5", new WhotCard(WhotCard.ANGLE, 5)},
            { "Circle 12", new WhotCard(WhotCard.BALL, 12)},
            { "Square 7", new WhotCard(WhotCard.CARPET, 7)},
            { "Whot 20", new WhotCard(WhotCard.WHOT, 20)},
        };
    }


}
