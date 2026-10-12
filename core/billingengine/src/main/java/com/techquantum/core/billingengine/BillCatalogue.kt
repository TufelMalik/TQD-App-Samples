package com.techquantum.core.billingengine

/** Shared counter menu for the POS and bill-printing showcase. No network assets are required. */
object BillCatalogue {
    val items = listOf(
        MenuItem("masala-dosa", "Masala Dosa", "Breakfast", 120, spiceLevel = 1),
        MenuItem("idli-sambar", "Idli Sambar", "Breakfast", 85, spiceLevel = 1),
        MenuItem("poha", "Indori Poha", "Breakfast", 65, spiceLevel = 1),
        MenuItem("paneer-tikka", "Paneer Tikka", "Starters", 220, spiceLevel = 2),
        MenuItem("haraa-kebab", "Hara Bhara Kebab", "Starters", 180, spiceLevel = 1),
        MenuItem("chicken-tikka", "Chicken Tikka", "Starters", 280, isVeg = false, spiceLevel = 2),
        MenuItem("paneer-butter", "Paneer Butter Masala", "Mains", 260, spiceLevel = 1),
        MenuItem("dal-tadka", "Dal Tadka", "Mains", 190, spiceLevel = 2),
        MenuItem("chicken-biryani", "Chicken Dum Biryani", "Mains", 320, isVeg = false, spiceLevel = 3),
        MenuItem("butter-naan", "Butter Naan", "Breads", 55),
        MenuItem("tandoori-roti", "Tandoori Roti", "Breads", 35),
        MenuItem("masala-chaas", "Masala Chaas", "Drinks", 60),
        MenuItem("mango-lassi", "Mango Lassi", "Drinks", 95),
        MenuItem("gulab-jamun", "Gulab Jamun", "Desserts", 90),
        MenuItem("kulfi", "Pista Kulfi", "Desserts", 110),
    )

    val products: List<MenuItem> get() = items
}
