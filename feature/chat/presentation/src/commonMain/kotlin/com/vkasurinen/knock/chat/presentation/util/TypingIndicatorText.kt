package com.vkasurinen.knock.chat.presentation.util

import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.typing_one
import knock.feature.chat.presentation.generated.resources.typing_several
import knock.feature.chat.presentation.generated.resources.typing_three
import knock.feature.chat.presentation.generated.resources.typing_two
import com.vkasurinen.knock.core.presentation.util.UiText

fun typingIndicatorText(usernames: List<String>): UiText? {
    return when(usernames.size) {
        0 -> null
        1 -> UiText.Resource(Res.string.typing_one, arrayOf(usernames[0]))
        2 -> UiText.Resource(Res.string.typing_two, arrayOf(usernames[0], usernames[1]))
        3 -> UiText.Resource(Res.string.typing_three, arrayOf(usernames[0], usernames[1], usernames[2]))
        else -> UiText.Resource(Res.string.typing_several)
    }
}
