package ge.baqar.gogia.storage

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun char_convertion_count_are_equal() {
        val string = "ბაქარი"
        val converted = CharConverter.toEng(string)

        assertEquals(string.length, converted.length)
    }

    @Test
    fun char_convertion_count_are_equal_with_undefined_character() {
        val string = "ბაქარი(("
        val converted = CharConverter.toEng(string)

        assertEquals(string.length, converted.length)
    }
}