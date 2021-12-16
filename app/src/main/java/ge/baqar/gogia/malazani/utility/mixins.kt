package ge.baqar.gogia.malazani.utility

inline fun <reified T : Enum<T>> enumFromIndex(i: Int) = enumValues<T>()[i]