package com.example.festimo.exception

open class CustomException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)