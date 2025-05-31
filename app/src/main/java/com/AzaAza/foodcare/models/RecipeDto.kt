package com.AzaAza.foodcare.models

import com.AzaAza.foodcare.R
import com.google.gson.annotations.SerializedName

data class RecipeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("summary") val summary: String?,
    @SerializedName("ingredients") val ingredients: String,  // DB에는 텍스트로 저장되어 있음
    @SerializedName("instructions") val instructions: String,
    @SerializedName("timetaken") val timetaken: String,
    @SerializedName("difficultylevel") val difficultylevel: String,
    @SerializedName("allergies") val allergies: String?,
    @SerializedName("disease") val disease: String?,
    @SerializedName("diseasereason") val diseasereason: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("image_url") val imageUrl: String?
) {
    // RecipeDto를 Recipe 객체로 변환하는 함수
    fun toRecipe(userIngredients: List<String>): Recipe {
        val ingredientsList = ingredients.split(",").map { it.trim() }
        val matched = ingredientsList.filter { it in userIngredients }


        // 음식 이름에 따라 이미지 선택
        val imageRes = when (name) {
            "김치찌개" -> R.drawable.dish_img_kimchi_stew
            "김치볶음밥" -> R.drawable.dish_img_kimchi_bokkeumbap
            "콩국수" -> R.drawable.dish_img_bean_noodles
            "비빔밥" -> R.drawable.dish_img_bibimbap
            "부대찌개" -> R.drawable.dish_img_budaejjigae
            "청국장" -> R.drawable.dish_img_cheonggukjang
            "잡채" -> R.drawable.dish_img_japchae
            "카레" -> R.drawable.dish_img_curry
            "갈비찜" -> R.drawable.dish_img_galbijjim
            "감자볶음" -> R.drawable.dish_img_gamjabokkeum
            "감자전" -> R.drawable.dish_img_gamjajeon
            "김치라면" -> R.drawable.dish_img_kimchiramen
            "김치전" -> R.drawable.dish_img_kimchijeon
            "닭갈비" -> R.drawable.dish_img_dakgalbi
            "된장찌개" -> R.drawable.dish_img_doenjangjjigae
            "떡볶이" -> R.drawable.dish_img_tteokbokki
            "미역국" -> R.drawable.dish_img_miyeokguk
            "소불고기" -> R.drawable.dish_img_sobulgogi
            "소시지볶음" -> R.drawable.dish_img_sosisibokkeum
            "순두부찌개" -> R.drawable.dish_img_sundubujjigae
            "오이무침" -> R.drawable.dish_img_oimumchim
            "오트밀미역죽" -> R.drawable.dish_img_oatmealmiyukjuk
            "유부초밥" -> R.drawable.dish_img_yubuchobap
            "잔치국수" -> R.drawable.dish_img_janchiguksu
            "콩나물국" -> R.drawable.dish_img_kongnamulguk
            "토스트" -> R.drawable.dish_img_toast
            "호박죽" -> R.drawable.dish_img_hobakjuk
            "육개장" -> R.drawable.dish_img_yukgaejang
            "샤브샤브" -> R.drawable.dish_img_shabushabu
            "닭볶음탕" -> R.drawable.dish_img_chicken_bokkeumtang
            "대구탕" -> R.drawable.dish_img_daegutang
            "감자탕" -> R.drawable.dish_img_gamjatang
            "고르곤졸라피자" -> R.drawable.dish_img_gorgonzola_pizza
            "궁중떡볶이" -> R.drawable.dish_img_gungjung_tteokbokki
            "함박스테이크" -> R.drawable.dish_img_hamburger_steak
            "마라탕" -> R.drawable.dish_img_malatang
            "마파두부" -> R.drawable.dish_img_mapa_tofu
            "쌀국수" -> R.drawable.dish_img_pad_thai
            "봉골레파스타" -> R.drawable.dish_img_vongole_pasta
            "나시 고랭" -> R.drawable.dish_img_nasi_goreng

            "도토리묵" -> R.drawable.dish_img_acorn_jello
            "장어구이" -> R.drawable.dish_img_broiled_eels
            "관자버터구이" -> R.drawable.dish_img_grilled_tube_butter
            "마라샹궈" -> R.drawable.dish_img_malaxianguo
            "오코노미야끼" -> R.drawable.dish_img_okonomiyakki

            "고추잡채" -> R.drawable.dish_img_red_pepper_japchae
            "곱도리탕" -> R.drawable.dish_img_gopdoritang
            "동그랑땡" -> R.drawable.dish_img_donggeurangddang
            "부추전" -> R.drawable.dish_img_buchujeon
            "양장피" -> R.drawable.dish_img_sheepskin
            "오리탕" -> R.drawable.dish_img_duck_soup

            "LA 양념갈비" -> R.drawable.dish_img_la_ribs
            "감바스" -> R.drawable.dish_img_gambas
            "계란말이" -> R.drawable.dish_img_egg_rolled
            "계란볶음밥" -> R.drawable.dish_img_egg_fried_rice
            "고등어구이" -> R.drawable.dish_img_grilled_mackerel
            "꽃게탕" -> R.drawable.dish_img_crab_soup
            "돈까스" -> R.drawable.dish_img_pork_cutlet
            "동태탕" -> R.drawable.dish_img_pollack_soup
            "로제파스타" -> R.drawable.dish_img_rose_pasta
            "보쌈" -> R.drawable.dish_img_bossam
            "브라우니" -> R.drawable.dish_img_brownie
            "비빔국수" -> R.drawable.dish_img_bibimbap
            "소고기무국" -> R.drawable.dish_img_beef_radish_soup
            "수육" -> R.drawable.dish_img_boiled_pork
            "순대볶음" -> R.drawable.dish_img_soondae
            "스테이크" -> R.drawable.dish_img_steak
            "알탕" -> R.drawable.dish_img_fish_roe_soup
            "연포탕" -> R.drawable.dish_img_yeonpo_soup
            "오므라이스" -> R.drawable.dish_img_omelet_rice
            "조개탕" -> R.drawable.dish_img_clam_soup
            "족발" -> R.drawable.dish_img_pig_hocks
            "짜장면" -> R.drawable.dish_img_black_bean_sauce_noodles
            "짬뽕" -> R.drawable.dish_img_jjambbong
            "초밥" -> R.drawable.dish_img_sushi
            "초코칩 쿠키" -> R.drawable.dish_img_chocolate_chip_cookies
            "칼국수" -> R.drawable.dish_img_kalguksu
            "케이크" -> R.drawable.dish_img_cake
            "코다리찜" -> R.drawable.dish_img_kodarijjim
            "콩나물불고기" -> R.drawable.dish_img_bean_sprout_bulgogi
            "투움바파스타" -> R.drawable.dish_img_toowoomba_pasta
            "피넛버터 쿠키" -> R.drawable.dish_img_peanut_butter_cookies


            "제육볶음" -> R.drawable.dish_img_jeyuk_bokkeum
            "마제소바" -> R.drawable.dish_img_mazesoba
            "시래기국" -> R.drawable.dish_img_siraegikuk
            "크림소스 뇨끼" -> R.drawable.dish_img_cream_gnocchi
            "똠양꿍" -> R.drawable.dish_img_tomyumgoong
            "카레 프라이드 누들" -> R.drawable.dish_img_curry_fried_noodle
            "팟타이" -> R.drawable.dish_img_pad_thai
            "탄두리 치킨" -> R.drawable.dish_img_tandoori_chicken
            "고이꾸온" -> R.drawable.dish_img_goicuon
            "치킨 라이스" -> R.drawable.dish_img_chicken_rice
            "아도보" -> R.drawable.dish_img_adobo
            "팔보채" -> R.drawable.dish_img_eight_bodied_vegetables
            "깐풍기" -> R.drawable.dish_img_deep_fried_chickeninhot_pepper_sauce

            "비리야니" -> R.drawable.dish_img_biryani
            "까르보나라" -> R.drawable.dish_img_carbonara
            "치킨 커리" -> R.drawable.dish_img_chicken_curry
            "동파육" -> R.drawable.dish_img_dongpo_pork
            "깐쇼새우" -> R.drawable.dish_img_gansho_shrimp
            "망고 스티키 라이스" -> R.drawable.dish_img_mango_sticky_rice
            "멘보샤" -> R.drawable.dish_img_menbosha
            "나시 르막" -> R.drawable.dish_img_menbosha
            "매생이국" -> R.drawable.dish_img_maesaengi_guk
            "팔락 파니르" -> R.drawable.dish_img_palak_paneer
            "사테" -> R.drawable.dish_img_satay
            "탕수육" -> R.drawable.dish_img_tangsuyuk
            "똠양누들" -> R.drawable.dish_img_tomyam_noodle
            "샤오롱바오" -> R.drawable.dish_img_xiaolongbao
            "야끼소바" -> R.drawable.dish_img_yakisoba
            "엿" -> R.drawable.dish_img_yeot
            "유린기" -> R.drawable.dish_img_yurinji
            "짜사이무침" -> R.drawable.dish_img_zhacai_muchim




            else -> R.drawable.bell  // 기본 이미지
        }

        return Recipe(
            name = name,
            summary = summary,
            description = instructions.take(50) + if (instructions.length > 50) "..." else "",
            instructions = instructions,          // 전체 순서 저장
            imageResId = imageRes,
            imageUrl = imageUrl,
            ingredients = ingredientsList,
            matchedCount = matched.size,
            matchedIngredients = matched,
            timeTaken = timetaken,
            difficulty = difficultylevel,
            allergies = allergies,
            disease = disease,
            diseaseReason = diseasereason,
            category = category
        )

    }
}
