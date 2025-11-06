package com.github.trading.infra.util

import com.google.protobuf.Timestamp
import java.time.Instant

fun instantToTimestamp(instant: Instant): Timestamp =
    Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()