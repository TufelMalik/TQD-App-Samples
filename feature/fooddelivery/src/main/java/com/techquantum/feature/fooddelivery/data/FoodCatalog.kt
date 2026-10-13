package com.techquantum.feature.fooddelivery.data

import com.techquantum.feature.fooddelivery.domain.AddOn
import com.techquantum.feature.fooddelivery.domain.Cuisine
import com.techquantum.feature.fooddelivery.domain.Dish
import com.techquantum.feature.fooddelivery.domain.FoodArt
import com.techquantum.feature.fooddelivery.domain.MenuCategory
import com.techquantum.feature.fooddelivery.domain.Restaurant

/** Seeded locally so menus, illustrations and checkout work without connectivity. */
object FoodCatalog {
    private val cheese = AddOn("cheese", "Extra cheese", 30)
    private val butter = AddOn("butter", "Extra butter", 15)
    private val paneer = AddOn("paneer", "Extra paneer", 40)
    private val chutney = AddOn("chutney", "Extra chutney", 10)
    private val protein = AddOn("protein", "Roasted chickpeas", 25)
    val restaurants = listOf(
        Restaurant("surati", "Surati Rasoi", "Comforting Gujarati meals, made fresh", Cuisine.GUJARATI, 4.8, 462, 24, 1.8, FoodArt.THALI, listOf(
            Dish("rasoi-thali", "surati", "Surati Special Thali", "Rotli, two seasonal shaak, dal, rice, farsan and a sweet.", 249, MenuCategory.POPULAR, artwork = FoodArt.THALI, addOns = listOf(butter, paneer)),
            Dish("paneer-masala", "surati", "Paneer Butter Masala", "Soft paneer in a slow-cooked tomato gravy.", 219, MenuCategory.MAINS, artwork = FoodArt.BOWL, addOns = listOf(paneer, butter)),
            Dish("khaman", "surati", "Surati Khaman", "A fluffy, freshly steamed favourite with chutney.", 89, MenuCategory.SIDES, artwork = FoodArt.DESSERT, addOns = listOf(chutney)),
            Dish("chaas", "surati", "Masala Chaas", "Cooling buttermilk with cumin and fresh coriander.", 49, MenuCategory.DRINKS, spice = 0, artwork = FoodArt.DRINK),
        )),
        Restaurant("dosa", "Dosa District", "Crisp dosas, warm sambar, happy mornings", Cuisine.SOUTH_INDIAN, 4.6, 318, 20, 2.1, FoodArt.DOSA, listOf(
            Dish("masala-dosa", "dosa", "Ghee Masala Dosa", "Golden dosa, spiced potato, sambar and two chutneys.", 159, MenuCategory.POPULAR, artwork = FoodArt.DOSA, addOns = listOf(cheese, butter, chutney)),
            Dish("idli", "dosa", "Steamed Idli Plate", "Three pillowy idlis, warm sambar and coconut chutney.", 99, MenuCategory.MAINS, spice = 0, artwork = FoodArt.DESSERT, addOns = listOf(chutney)),
            Dish("uttapam", "dosa", "Garden Uttapam", "A soft rice pancake with onion, tomato and herbs.", 139, MenuCategory.MAINS, artwork = FoodArt.PIZZA, addOns = listOf(cheese, chutney)),
            Dish("filter-coffee", "dosa", "South Indian Filter Coffee", "Freshly brewed, strong and frothy.", 59, MenuCategory.DRINKS, spice = 0, artwork = FoodArt.DRINK),
        )),
        Restaurant("oven", "The Oven Room", "Hand-stretched pizzas, a little extra joy", Cuisine.PIZZA, 4.7, 205, 30, 3.2, FoodArt.PIZZA, listOf(
            Dish("garden-pizza", "oven", "Garden Feast Pizza", "A 9-inch pizza with mozzarella, corn, peppers and olives.", 269, MenuCategory.POPULAR, artwork = FoodArt.PIZZA, addOns = listOf(cheese, paneer)),
            Dish("chicken-pizza", "oven", "Smoky Chicken Pizza", "A 9-inch pizza with grilled chicken and smoky sauce.", 319, MenuCategory.MAINS, vegetarian = false, spice = 2, artwork = FoodArt.PIZZA, addOns = listOf(cheese)),
            Dish("garlic-bread", "oven", "Cheesy Garlic Bread", "Warm bread, garlic butter and melted cheese.", 119, MenuCategory.SIDES, spice = 0, artwork = FoodArt.WRAP, addOns = listOf(cheese)),
            Dish("lemon-soda", "oven", "Fresh Lime Soda", "Lime, soda and a refreshing hint of mint.", 69, MenuCategory.DRINKS, spice = 0, artwork = FoodArt.DRINK),
        )),
        Restaurant("green", "Green Bowl Co.", "Colourful bowls for a feel-good lunch", Cuisine.HEALTHY, 4.5, 174, 22, 1.5, FoodArt.BOWL, listOf(
            Dish("harvest-bowl", "green", "Harvest Grain Bowl", "Brown rice, roasted vegetables, hummus and lemon dressing.", 229, MenuCategory.POPULAR, spice = 0, artwork = FoodArt.BOWL, addOns = listOf(protein, paneer)),
            Dish("paneer-wrap", "green", "Grilled Paneer Wrap", "Wholewheat wrap, grilled paneer, greens and mint yoghurt.", 189, MenuCategory.MAINS, artwork = FoodArt.WRAP, addOns = listOf(paneer, cheese)),
            Dish("fruit-bowl", "green", "Seasonal Fruit Bowl", "Freshly cut local fruit with a squeeze of lime.", 129, MenuCategory.SIDES, spice = 0, artwork = FoodArt.DESSERT),
            Dish("mango-smoothie", "green", "Mango Yoghurt Smoothie", "Mango, yoghurt and a naturally sweet finish.", 119, MenuCategory.DRINKS, spice = 0, artwork = FoodArt.DRINK),
        )),
    )
}
