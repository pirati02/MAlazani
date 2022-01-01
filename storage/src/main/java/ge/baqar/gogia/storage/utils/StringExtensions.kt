package ge.baqar.gogia.storage.utils

fun String?.nullIfEmpty(): String? {
    return this?.takeUnless { it.isEmpty() }
}

fun String.suffixOrEmpty(): String {
    return substringAfterLast(DOT, EMPTY_STRING)
}

fun String.suffixOrNull(): String? {
    return suffixOrEmpty().nullIfEmpty()
}

fun String.endsWithMp3(): Boolean {
    return this.endsWith(".mp3")
}