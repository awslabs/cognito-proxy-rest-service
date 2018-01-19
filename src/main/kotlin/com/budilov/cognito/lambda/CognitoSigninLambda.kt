package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoSigninLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {


    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse? {

        val logger = context?.logger
        val username = request?.headers?.get("username")
        val password = request?.headers?.get("password")

        var status = 400
        var response = ""

        if (username != null && password != null) {
            val result = cognito.signInNoSRP(username = username,
                    password = password)
            status = 200
            response = Gson().toJson(result)
            logger?.log("Got a body from Cognito: $response")
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}