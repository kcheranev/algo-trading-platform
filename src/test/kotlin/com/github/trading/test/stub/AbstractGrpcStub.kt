package com.github.trading.test.stub

import com.github.trading.common.ResourceUtils.readResourceAsString

abstract class AbstractGrpcStub(
    private val testName: String
) {

    protected fun grpcRequest(fileName: String) =
        readResourceAsString("/stub/$testName/grpc/request/$fileName")

    protected fun grpcResponse(fileName: String) =
        readResourceAsString("/stub/$testName/grpc/response/$fileName")

}