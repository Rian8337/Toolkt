package com.reco1l.toolkt.kotlin

import kotlin.math.abs
import kotlin.math.min


/**
 * Returns a copy of this string having its first letter in uppercase.
 */
fun String.capitalize() = replaceFirstChar { it.uppercase() }

/**
 * Returns a copy of this string having its first letter in lowercase.
 */
fun String.decapitalize() = replaceFirstChar { it.lowercase() }


// Ranges

infix fun String.isBetween(range: CharRange) = startsWith(range.first) && endsWith(range.last)

/**
 * Returns a substring cropped between the specified characters or `null` if it doesn't contains
 * those characters or the part between them is empty.
 */
fun String.between(first: Char, last: Char): String?
{
    val firstIndex = indexOf(first)
    val secondIndex = lastIndexOf(last)

    if (firstIndex == -1 || firstIndex + 1 >= secondIndex)
        return null

    return substring(firstIndex + 1, secondIndex).takeUnless { it.isEmpty() }
}

/**
 * Multiply the characters and create a sequence
 */
operator fun Char.times(times: Int): CharSequence
{
    var result = ""
    for (i in 0 until abs(times))
        result += this
    return result
}


// Regex

fun String.takeIfMatches(regex: Regex) = takeIf { regex.matches(it) }


/**
 * Pack of constants with defaults regular expressions.
 */
object Regexs
{

    /**
     * Regex for integer numbers.
     *
     * Valid cases:
     * * `123`
     * * `-123`
     */
    val INTEGER = Regex("^-?\\d+$")

    /**
     * Regex for numbers with decimal (floating points).
     *
     * Valid cases:
     * * `123.123`
     * * `-123.123`
     * * `123`
     * * `-123`
     */
    val DECIMAL = Regex("^NaN|Infinity|-?\\d+(\\.\\d+$)?$")

    /**
     * Regex for an array of integer numbers.
     *
     * The valid cases are the same as [INTEGER] regex allowing one or more arguments delimited
     * by a comma.
     */
    val INTEGER_ARRAY = Regex("^-?\\d+(?:\\s*,\\s*-?\\d+)*\$")


    /**
     * Regex for an array of numbers with decimal.
     *
     * Same as [INTEGER_ARRAY].
     */
    val DECIMAL_ARRAY = Regex("^-?\\d+(\\.\\d+)?(?:\\s*,\\s*-?\\d+(\\.\\d+)?)*\$")

    /**
     * Regex for alphanumeric strings.
     *
     * Valid cases:
     * * `abc123`
     * * `abc`
     * * `123`
     */
    val ALPHANUMERIC = Regex("^[a-zA-Z0-9]+\$")
}


// Escapes

fun String.withTranslatedEscapes(ignoreInvalidSequences: Boolean = true): String
{
    if (isEmpty())
        return ""

    val sequence = toCharArray()
    val length = sequence.size

    var from = 0
    var to = 0

    while (from < length)
    {
        var ch = sequence[from++]

        if (ch == '\\')
        {
            ch = if (from < length) sequence[from++] else '\u0000'

            when (ch)
            {
                'b' -> ch = '\b'
                'f' -> ch = '\u000c'
                'n' -> ch = '\n'
                'r' -> ch = '\r'
                's' -> ch = ' '
                't' -> ch = '\t'
                '\'', '\"', '\\' -> Unit

                '0', '1', '2', '3', '4', '5', '6', '7' ->
                {
                    val limit = min(from + if (ch <= '3') 2 else 1, length)
                    var code = ch.code - '0'.code

                    while (from < limit)
                    {
                        ch = sequence[from]

                        if (ch < '0' || '7' < ch)
                            break

                        from++
                        code = code shl 3 or ch.code - '0'.code
                    }
                    ch = code.toChar()
                }

                '\n' -> continue
                '\r' ->
                {
                    if (from < length && sequence[from] == '\n')
                        from++

                    continue
                }

                else ->
                {
                    if (!ignoreInvalidSequences)
                        throw IllegalArgumentException("Invalid escape sequence: \\%c \\\\u%04X".format(ch, ch.code))
                }
            }
        }

        sequence[to++] = ch
    }
    return sequence.concatToString(endIndex = to)
}


// Conversion

/**
 * Splits a camel case string into a string separated by a defined character between words.
 * As expected from a camel case string, this methods splits words by the first letter case considering
 * an uppercase sequence as the next word.
 *
 * On the other hand there's some edge case handling for words abreviation such as 'ID' or 'URL',
 * since those are abreviated there are several uppercase letters in a row and in that case the
 * sequence is considered as a full word instead. The sequence will be considered as ended if the
 * last character has a following lowercase character or the next character is a whitespace.
 *
 * Examples:
 * * `userID` is splited into `User ID`
 * * `userName` is splited into `User name`
 * * `userURLForAvatar` is splited into `User URL for avatar`
 *
 * @param input The camel case string.
 * @param separator The separator character, by default a single whitespace.
 * @param lowercaseWords Whether the words (excluding the first one) should be lowercased.
 * @param capitalize Whether the first word should be capitalized.
 */
@JvmOverloads
fun splitCamelCase(
    input: String,
    separator: String = " ",
    lowercaseWords: Boolean = true,
    capitalize: Boolean = true
    ): String
{
    var r = ""

    for (i in input.indices)
    {
        val char = input[i]

        val p = if (i > 0) input[i - 1] else ' '
        val n = if (i < input.length - 1) input[i + 1] else ' '

        if (char.isLowerCase())
            r += if (i == 0 && capitalize)
                char.uppercaseChar()
            else
                char
        else
            r += if (p.isUpperCase() && n.isUpperCase() || n == ' ')
                char
            else
                separator + if (lowercaseWords)
                    char.lowercaseChar()
                else
                    char
    }

    return r
}