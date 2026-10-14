package com.vkasurinen.knock.core.domain.util

class DataErrorException(
    val error: DataError
): Exception()