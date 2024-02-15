package ru.kcheranev.trading.test.stub

import org.awaitility.Awaitility.await
import org.awaitility.core.ThrowingRunnable
import ru.kcheranev.trading.test.util.TestUtils.readResourceAsString
import java.util.concurrent.TimeUnit

abstract class AbstractGrpcStub(
    private val testName: String
) {

    protected fun grpcRequest(fileName: String) =
        readResourceAsString("stub/$testName/grpc/request/$fileName")

    protected fun grpcResponse(fileName: String) =
        readResourceAsString("stub/$testName/grpc/response/$fileName")

    protected fun awaitedVerify(assertion: ThrowingRunnable) {
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(assertion)
    }

}