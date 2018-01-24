package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.services.CognitoService
import com.google.gson.Gson

/**
 * @author Vladimir Budilov
 */
class CognitoSignupLambda : RequestHandler<ApiGatewayRequest.Input,
        ApiGatewayResponse> {

    val cognito = CognitoService()

    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): ApiGatewayResponse {

        val username = request?.headers?.get("username")
        val password = request?.headers?.get("password")

        var status = 400
        var response = ""

        if (username != null && password != null) {
            val result = cognito.signUp(username = username,
                    password = password)
            status = 200
            response = Gson().toJson(result)
        }

        return ApiGatewayResponse(statusCode = status, body = response)
    }
}