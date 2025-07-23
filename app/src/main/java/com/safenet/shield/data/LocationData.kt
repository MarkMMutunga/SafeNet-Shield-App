/*
 * SafeNet Shield - Personal Safety & Security Application
 * 
 * Copyright (c) 2024 Mark Mikile Mutunga
 * Email: markmiki03@gmail.com
 * Phone: +254 707 678 643
 * 
 * All rights reserved. This software and associated documentation files (the "Software"),
 * are proprietary to Mark Mikile Mutunga. Unauthorized copying, distribution, or modification
 * of this software is strictly prohibited without explicit written permission from the author.
 * 
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a particular
 * purpose and noninfringement. In no event shall the author be liable for any claim,
 * damages or other liability, whether in an action of contract, tort or otherwise,
 * arising from, out of or in connection with the software or the use or other dealings
 * in the software.
 */
package com.safenet.shield.data

data class Country(
    val name: String,
    val cities: List<String>,
    val countryCode: String
)

object LocationData {
    val countries = listOf(
        Country("Kenya", listOf(
            "Nairobi", "Mombasa", "Kisumu", "Nakuru", "Eldoret", "Thika", "Malindi", "Kitale",
            "Garissa", "Kakamega", "Nyeri", "Machakos", "Meru", "Lodwar", "Wajir", "Kapenguria",
            "Bungoma", "Busia", "Homa Bay", "Kisii", "Kericho", "Embu", "Isiolo", "Kitui",
            "Lamu", "Mandera", "Marsabit", "Migori", "Murang'a", "Narok", "Nyamira", "Nyahururu",
            "Samburu", "Siaya", "Taita-Taveta", "Tana River", "Trans Nzoia", "Turkana", "Uasin Gishu",
            "Vihiga", "Wajir", "West Pokot", "Bomet", "Baringo", "Elgeyo-Marakwet", "Kiambu",
            "Kilifi", "Kirinyaga", "Kajiado", "Kwale", "Laikipia", "Makueni", "Nyandarua", "Nandi",
            "Taita Taveta", "Tana River", "Tharaka-Nithi"
        ), "+254"),
        Country("Uganda", listOf("Kampala", "Entebbe", "Jinja", "Gulu", "Mbarara"), "+256"),
        Country("Tanzania", listOf("Dar es Salaam", "Dodoma", "Mwanza", "Arusha", "Zanzibar"), "+255"),
        Country("Ethiopia", listOf("Addis Ababa", "Dire Dawa", "Mekelle", "Gondar", "Bahir Dar"), "+251"),
        Country("Somalia", listOf("Mogadishu", "Hargeisa", "Kismayo", "Garowe", "Bosaso"), "+252"),
        Country("South Sudan", listOf("Juba", "Malakal", "Wau", "Yei", "Aweil"), "+211"),
        Country("Rwanda", listOf("Kigali", "Butare", "Gitarama", "Ruhengeri", "Gisenyi"), "+250"),
        Country("Burundi", listOf("Bujumbura", "Gitega", "Ngozi", "Rumonge", "Kayanza"), "+257")
    )
} 