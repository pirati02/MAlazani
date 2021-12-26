package ge.baqar.gogia.storage

object CharConverter {
    private val charMap = hashMapOf<Char, String>(
        'ა' to "a",
        'ბ' to "b",
        'გ' to "g",
        'დ' to "d",
        'ე' to "e",
        'ვ' to "v",
        'ზ' to "z",
        'თ' to "t",
        'ი' to "i",
        'კ' to "k",
        'ლ' to "l",
        'მ' to "m",
        'ნ' to "n",
        'ო' to "o",
        'პ' to "p",
        'ჟ' to "jh",
        'რ' to "r",
        'ს' to "s",
        'ტ' to "t",
        'უ' to "u",
        'ფ' to "f",
        'ქ' to "q",
        'ღ' to "gh",
        'ყ' to "y",
        'შ' to "sh",
        'ჩ' to "ch",
        'ც' to "c",
        'ძ' to "zh",
        'წ' to "w",
        'ჭ' to "wh",
        'ხ' to "x",
        'ჯ' to "j",
        'ჰ' to "h"
    )

    fun toEng(text: String): String {
        return text.map {
            charMap[it]
        }.joinToString("")
    }
}