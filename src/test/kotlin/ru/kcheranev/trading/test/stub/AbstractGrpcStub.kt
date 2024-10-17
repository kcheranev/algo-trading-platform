package ru.kcheranev.trading.test.stub

import ru.kcheranev.trading.test.util.TestResourceUtils.readResourceAsString

abstract class AbstractGrpcStub(
    private val testName: String
) {

    protected fun grpcRequest(fileName: String) =
        readResourceAsString("stub/$testName/grpc/request/$fileName")

    protected fun grpcResponse(fileName: String) =
        readResourceAsString("stub/$testName/grpc/response/$fileName")

}